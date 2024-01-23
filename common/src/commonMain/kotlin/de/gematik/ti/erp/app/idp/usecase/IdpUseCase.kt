/*
 * Copyright (c) 2024 gematik GmbH
 * 
 * Licensed under the EUPL, Version 1.2 or – as soon they will be approved by
 * the European Commission - subsequent versions of the EUPL (the Licence);
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at:
 * 
 *     https://joinup.ec.europa.eu/software/page/eupl
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the Licence is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Licence for the specific language governing permissions and
 * limitations under the Licence.
 * 
 */

package de.gematik.ti.erp.app.idp.usecase

import de.gematik.ti.erp.app.Requirement
import de.gematik.ti.erp.app.api.ApiCallException
import de.gematik.ti.erp.app.idp.api.EXT_AUTH_REDIRECT_URI
import de.gematik.ti.erp.app.idp.api.IdpService
import de.gematik.ti.erp.app.idp.api.REDIRECT_URI
import de.gematik.ti.erp.app.idp.api.models.AuthenticationId
import de.gematik.ti.erp.app.idp.api.models.ExternalAuthorizationData
import de.gematik.ti.erp.app.idp.api.models.IdpAuthFlowResult
import de.gematik.ti.erp.app.idp.api.models.IdpInitialData
import de.gematik.ti.erp.app.idp.api.models.IdpNonce
import de.gematik.ti.erp.app.idp.api.models.IdpScope
import de.gematik.ti.erp.app.idp.api.models.PairingData
import de.gematik.ti.erp.app.idp.api.models.PairingResponseEntry
import de.gematik.ti.erp.app.idp.model.IdpData
import de.gematik.ti.erp.app.idp.repository.IdpPairingRepository
import de.gematik.ti.erp.app.idp.repository.IdpRepository
import de.gematik.ti.erp.app.profiles.repository.ProfileIdentifier
import de.gematik.ti.erp.app.profiles.repository.ProfilesRepository
import de.gematik.ti.erp.app.vau.extractECPublicKey
import java.io.IOException
import java.net.URI
import java.security.KeyStore
import java.security.PrivateKey
import java.security.PublicKey
import java.security.Signature
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import org.bouncycastle.util.encoders.Base64
import io.github.aakira.napier.Napier
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import java.net.HttpURLConnection

/**
 * Exception thrown by [IdpUseCase.loadAccessToken].
 */
class RefreshFlowException : IOException {
    /**
     * Is true if the sso token is not valid anymore and the user is required to authenticate again.
     */
    val userActionRequired: Boolean
    val ssoToken: IdpData.SingleSignOnTokenScope?

    constructor(
        userActionRequired: Boolean,
        ssoToken: IdpData.SingleSignOnTokenScope?,
        cause: Throwable
    ) : super(cause) {
        this.userActionRequired = userActionRequired
        this.ssoToken = ssoToken
    }

    constructor(
        userActionRequired: Boolean,
        ssoToken: IdpData.SingleSignOnTokenScope?,
        message: String
    ) : super(message) {
        this.userActionRequired = userActionRequired
        this.ssoToken = ssoToken
    }
}

class IDPConfigException(cause: Throwable) : IOException(cause)

class AltAuthenticationCryptoException(cause: Throwable) : IllegalStateException(cause)

class IdpUseCase(
    private val repository: IdpRepository,
    private val pairingRepository: IdpPairingRepository,
    private val altAuthUseCase: IdpAlternateAuthenticationUseCase,
    private val profilesRepository: ProfilesRepository,
    private val basicUseCase: IdpBasicUseCase,
    private val preferences: IdpPreferenceProvider,
    private val cryptoProvider: IdpCryptoProvider
) {
    private val lock = Mutex()

    /**
     * If no bearer token is set or [refresh] is true, this will trigger [IdpBasicUseCase.refreshAccessTokenWithSsoFlow].
     */
    @Requirement(
        "A_20283-01#1",
        "A_21326",
        "A_21327",
        sourceSpecification = "gemSpec_eRp_FdV",
        rationale = "Load and decrypt access token."
    )
    suspend fun loadAccessToken(
        refresh: Boolean = false,
        profileId: ProfileIdentifier,
        scope: IdpScope = IdpScope.Default
    ): String = lock.withLock {
        when (scope) {
            IdpScope.Default ->
                loadAccessToken(
                    refresh = refresh,
                    profileId = profileId,
                    scope = IdpScope.Default,
                    singleSignOnTokenScope = {
                        repository.authenticationData(profileId).first().singleSignOnTokenScope
                    },
                    decryptedAccessToken = { repository.decryptedAccessToken(profileId).first() },
                    invalidateDecryptedAccessToken = { repository.invalidateDecryptedAccessToken(profileId) },
                    invalidateSingleSignOnTokenRetainingScope = {
                        repository.invalidateSingleSignOnTokenRetainingScope(
                            profileId
                        )
                    },
                    saveDecryptedAccessToken = { repository.saveDecryptedAccessToken(profileId, it) }
                )
            IdpScope.BiometricPairing ->
                loadAccessToken(
                    refresh = refresh,
                    profileId = profileId,
                    scope = IdpScope.BiometricPairing,
                    singleSignOnTokenScope = { pairingRepository.singleSignOnTokenScope(profileId).first() },
                    decryptedAccessToken = { pairingRepository.decryptedAccessToken(profileId).first() },
                    invalidateDecryptedAccessToken = { pairingRepository.invalidateDecryptedAccessToken(profileId) },
                    invalidateSingleSignOnTokenRetainingScope = {
                        pairingRepository.invalidateSingleSignOnToken(
                            profileId
                        )
                    },
                    saveDecryptedAccessToken = { pairingRepository.saveDecryptedAccessToken(profileId, it) }
                )
        }
    }

    private suspend fun loadAccessToken(
        refresh: Boolean = false,
        profileId: ProfileIdentifier,
        scope: IdpScope,
        singleSignOnTokenScope: suspend () -> IdpData.SingleSignOnTokenScope?,
        decryptedAccessToken: suspend () -> String?,
        invalidateDecryptedAccessToken: suspend () -> Unit,
        invalidateSingleSignOnTokenRetainingScope: suspend () -> Unit,
        saveDecryptedAccessToken: suspend (decryptedAccessToken: String) -> Unit
    ): String {
        val ssoTokenScope = singleSignOnTokenScope()

        Napier.d {
            """Loading access token with:
              |refresh: $refresh
              |profileId: $profileId
              |scope: $scope
            """.trimMargin()
        }

        return if (ssoTokenScope != null) {
            if (ssoTokenScope.token?.token == null) {
                invalidateDecryptedAccessToken()
                throw RefreshFlowException(
                    true,
                    ssoTokenScope,
                    "SSO token not set for $profileId!"
                )
            }

            val accToken = decryptedAccessToken()

            if (refresh || accToken == null) {
                invalidateDecryptedAccessToken()

                val actualToken = ssoTokenScope.token!!.token

                val initialData = try {
                    basicUseCase.initializeConfigurationAndKeys()
                } catch (e: Exception) {
                    throw IDPConfigException(e)
                }
                try {
                    val refreshData = basicUseCase.refreshAccessTokenWithSsoFlow(
                        initialData,
                        scope = scope,
                        ssoToken = actualToken,
                        redirectUri = if (ssoTokenScope is IdpData.ExternalAuthenticationToken) {
                            EXT_AUTH_REDIRECT_URI
                        } else {
                            REDIRECT_URI
                        }
                    )
                    refreshData.accessToken
                } catch (e: Exception) {
                    Napier.e("Couldn't refresh access token", e)
                    (e as? ApiCallException)?.also {
                        when (it.response.code()) {
                            // 400 returned by redirect call if sso token is not valid anymore
                            400, 401, 403 -> {
                                invalidateSingleSignOnTokenRetainingScope()
                                throw RefreshFlowException(true, ssoTokenScope, e)
                            }
                        }
                    }
                    throw RefreshFlowException(false, null, e)
                }
            } else {
                accToken
            }
                .also {
                    saveDecryptedAccessToken(it)
                }
        } else {
            invalidateDecryptedAccessToken()
            throw RefreshFlowException(
                true,
                ssoTokenScope,
                "SSO token not set for $profileId!"
            )
        }
    }

    /**
     * Initial flow fetching the sso & access token requiring the health card to sign the challenge.
     */
    @Requirement(
        "A_20600#1",
        "A_20601",
        "A_20601-01",
        "A_21598#2",
        sourceSpecification = "gemSpec_IDP_Frontend",
        rationale = "Authenticate to the IDP using the health card certificate."
    )
    suspend fun authenticationFlowWithHealthCard(
        profileId: ProfileIdentifier,
        scope: IdpScope = IdpScope.Default,
        cardAccessNumber: String,
        healthCardCertificate: suspend () -> ByteArray,
        sign: suspend (hash: ByteArray) -> ByteArray
    ) {
        lock.withLock {
            authenticationFlowWithHealthCard(
                cardAccessNumber = cardAccessNumber,
                scope = scope,
                healthCardCertificate = healthCardCertificate,
                sign = sign
            ) { _, _, basicData, ssoToken ->
                when (scope) {
                    IdpScope.Default -> {
                        profilesRepository.saveInsuranceInformation(
                            profileId,
                            basicData.idTokenInsurantName,
                            basicData.idTokenInsuranceIdentifier,
                            basicData.idTokenInsuranceName
                        )
                        repository.saveSingleSignOnToken(profileId, ssoToken)
                        repository.saveDecryptedAccessToken(profileId, basicData.accessToken)
                    }
                    IdpScope.BiometricPairing -> {
                        pairingRepository.saveSingleSignOnToken(
                            profileId,
                            IdpData.SingleSignOnToken(basicData.ssoToken)
                        )
                    }
                }
            }
        }
    }

    private suspend fun <R> authenticationFlowWithHealthCard(
        cardAccessNumber: String,
        scope: IdpScope,
        healthCardCertificate: suspend () -> ByteArray,
        sign: suspend (hash: ByteArray) -> ByteArray,
        finally: suspend (
            initialData: IdpInitialData,
            healthCardCertificate: ByteArray,
            basicData: IdpAuthFlowResult,
            ssoToken: IdpData.DefaultToken
        ) -> R
    ): R {
        val initialData = basicUseCase.initializeConfigurationAndKeys()
        val challengeData =
            basicUseCase.challengeFlow(initialData, scope = scope, redirectUri = REDIRECT_URI)
        val cert = healthCardCertificate()
        val basicData = basicUseCase.basicAuthFlow(
            initialData = initialData,
            challengeData = challengeData,
            healthCardCertificate = cert,
            sign = sign
        )
        val ssoToken = IdpData.DefaultToken(
            token = IdpData.SingleSignOnToken(basicData.ssoToken),
            healthCardCertificate = cert,
            cardAccessNumber = cardAccessNumber
        )

        return finally(
            initialData,
            cert,
            basicData,
            ssoToken
        )
    }

    /**
     * Get all the information for the correct endpoints from the discovery document and request
     * the external Health Insurance Companies which are capable of authenticate you with their app
     */
    @Requirement(
        "A_22296-01#1",
        sourceSpecification = "gemSpec_IDP_Frontend",
        rationale = "Load list of external authenticators for Fast Track."
    )
    suspend fun loadExternAuthenticatorIDs(): List<AuthenticationId> {
        val initialData = basicUseCase.initializeConfigurationAndKeys()
        return repository.fetchExternalAuthorizationIDList(
            url = initialData.config.externalAuthorizationIDsEndpoint ?: error("Fasttrack is not available"),
            idpPukSigKey = initialData.config.certificate.extractECPublicKey()
        ).sortedBy {
            it.name.lowercase()
        }
    }

    /**
     * With chosen Health Insurance Company, request IDP for Authentication information,
     * sent as a redirect which is supposed to be fired as an Intent
     * @param externalAuthorizationId identifier of the health insurance company
     */
    suspend fun getUniversalLinkForExternalAuthorization(
        profileId: ProfileIdentifier,
        authenticatorId: String,
        authenticatorName: String,
        scope: IdpScope = IdpScope.Default
    ): URI {
        val initialData = basicUseCase.initializeConfigurationAndKeys()

        val redirectUri = repository.getAuthorizationRedirect(
            url = initialData.config.thirdPartyAuthorizationEndpoint ?: error("Fasttrack is not available"),
            state = initialData.state,
            codeChallenge = initialData.codeChallenge,
            nonce = initialData.nonce,
            kkAppId = authenticatorId,
            scope = scope
        )

        val parsedUri = URI(redirectUri)

        preferences.externalAuthenticationPreferences =
            ExternalAuthenticationPreferences(
                extAuthCodeChallenge = initialData.codeChallenge,
                extAuthCodeVerifier = initialData.codeVerifier,
                extAuthState = IdpService.extractQueryParameter(parsedUri, "state"),
                extAuthNonce = initialData.nonce.nonce,
                extAuthId = authenticatorId,
                extAuthScope = scope.name,
                extAuthName = authenticatorName,
                extAuthProfile = profileId
            )

        return parsedUri
    }

    /**
     * The scope is determined by the previously saved value within the shared prefs as `EXT_AUTH_SCOPE`.
     */
    @Requirement(
        "A_20527#2",
        "A_20600#2",
        "A_20601",
        "A_20601-01",
        "A_22301",
        sourceSpecification = "gemSpec_IDP_Frontend",
        rationale = "External authentication (fast track)"
    )
    @Requirement(
        "O.Plat_10#1",
        sourceSpecification = "BSI-eRp-ePA",
        rationale = "Follow redirect"
    )
    suspend fun authenticateWithExternalAppAuthorization(
        uri: URI
    ) {
        lock.withLock {
            val scope = preferences.externalAuthenticationPreferences.extAuthScope!!
            val profileId = preferences.externalAuthenticationPreferences.extAuthProfile!!

            val externalAuthorizationData = ExternalAuthorizationData(uri)

            require(externalAuthorizationData.state == preferences.externalAuthenticationPreferences.extAuthState)

            val initialData = basicUseCase.initializeConfigurationAndKeys()
            val redirectStringResult = repository.postExternAppAuthorizationData(
                url = initialData.config.thirdPartyAuthorizationEndpoint ?: error("Fasttrack is not available"),
                externalAuthorizationData = externalAuthorizationData
            )
            val redirect = URI(redirectStringResult.getOrThrow())

            val redirectCodeJwe = IdpService.extractQueryParameter(redirect, "code")
            val redirectSsoToken = IdpService.extractQueryParameter(redirect, "ssotoken")

            val idpTokenResult = basicUseCase.postCodeAndDecryptAccessToken(
                url = initialData.config.tokenEndpoint,
                nonce = IdpNonce(preferences.externalAuthenticationPreferences.extAuthNonce!!),
                codeVerifier = preferences.externalAuthenticationPreferences.extAuthCodeVerifier!!,
                code = redirectCodeJwe,
                pukEncKey = initialData.pukEncKey,
                pukSigKey = initialData.pukSigKey,
                redirectUri = EXT_AUTH_REDIRECT_URI
            )

            val authId = preferences.externalAuthenticationPreferences.extAuthId!!
            val authName = preferences.externalAuthenticationPreferences.extAuthName!!

            preferences.clear()

            when (scope) {
                IdpScope.Default.name -> {
                    val idTokenJson = Json.parseToJsonElement(idpTokenResult.idTokenPayload)

                    val idTokenInsuranceIdentifier = idTokenJson.jsonObject["idNummer"]?.jsonPrimitive?.content ?: ""
                    val idTokenInsuranceName = idTokenJson.jsonObject["organizationName"]?.jsonPrimitive?.content ?: ""
                    val idTokenInsurantName = idTokenJson.jsonObject["given_name"]?.jsonPrimitive?.content
                        ?.let {
                            "$it ${idTokenJson.jsonObject["family_name"]?.jsonPrimitive?.content}"
                        } ?: ""

                    profilesRepository.saveInsuranceInformation(
                        profileId = profileId,
                        insurantName = idTokenInsurantName,
                        insuranceIdentifier = idTokenInsuranceIdentifier,
                        insuranceName = idTokenInsuranceName
                    )

                    repository.saveSingleSignOnToken(
                        profileId,
                        IdpData.ExternalAuthenticationToken(
                            token = IdpData.SingleSignOnToken(redirectSsoToken),
                            authenticatorId = authId,
                            authenticatorName = authName
                        )
                    )
                    repository.saveDecryptedAccessToken(profileId, idpTokenResult.decryptedAccessToken)
                }
                IdpScope.BiometricPairing.name -> {
                    pairingRepository.saveSingleSignOnToken(
                        profileId,
                        IdpData.SingleSignOnToken(redirectSsoToken)
                    )
                }
            }
        }
    }

    /**
     * Pairing flow fetching the sso & access token requiring the health card and generated key material.
     */
    suspend fun alternatePairingFlowWithSecureElement(
        profileId: ProfileIdentifier,
        cardAccessNumber: String,
        publicKeyOfSecureElementEntry: PublicKey,
        aliasOfSecureElementEntry: ByteArray,
        healthCardCertificate: suspend () -> ByteArray,
        signWithHealthCard: suspend (hash: ByteArray) -> ByteArray
    ) = lock.withLock {
        val initialData = basicUseCase.initializeConfigurationAndKeys()
        val challengeData =
            basicUseCase.challengeFlow(
                initialData,
                scope = IdpScope.BiometricPairing,
                redirectUri = REDIRECT_URI
            )
        val healthCardCert = healthCardCertificate()
        val basicData = basicUseCase.basicAuthFlow(
            initialData = initialData,
            challengeData = challengeData,
            healthCardCertificate = healthCardCert,
            sign = signWithHealthCard
        )

        altAuthUseCase.registerDeviceWithHealthCard(
            initialData = initialData,
            accessToken = basicData.accessToken,
            healthCardCertificate = healthCardCert,
            publicKeyOfSecureElementEntry = publicKeyOfSecureElementEntry,
            aliasOfSecureElementEntry = aliasOfSecureElementEntry,
            signWithHealthCard = signWithHealthCard
        )
        profilesRepository.saveInsuranceInformation(
            profileId,
            basicData.idTokenInsurantName,
            basicData.idTokenInsuranceIdentifier,
            basicData.idTokenInsuranceName
        )
        // set pairing scope
        repository.saveSingleSignOnToken(
            profileId,
            IdpData.AlternateAuthenticationWithoutToken(
                cardAccessNumber = cardAccessNumber,
                aliasOfSecureElementEntry = aliasOfSecureElementEntry,
                healthCardCertificate = healthCardCert
            )
        )
    }

    /**
     * Actual authentication with secure element key material. Just like the [authenticationFlowWithHealthCard] it
     * sets the sso & access token within the repository.
     */
    @Requirement(
        "A_21598#1",
        sourceSpecification = "gemSpec_IDP_Frontend",
        rationale = "Authentication flow with health card and secure element."
    )
    suspend fun alternateAuthenticationFlowWithSecureElement(
        profileId: ProfileIdentifier,
        scope: IdpScope = IdpScope.Default
    ) {
        lock.withLock {
            alternateAuthenticationFlowWithSecureElement(
                profileId = profileId,
                scope = IdpScope.Default
            ) { _, authTokenScope, authData ->
                when (scope) {
                    IdpScope.Default -> {
                        profilesRepository.saveInsuranceInformation(
                            profileId,
                            authData.idTokenInsurantName,
                            authData.idTokenInsuranceIdentifier,
                            authData.idTokenInsuranceName
                        )
                        repository.saveSingleSignOnToken(
                            profileId,
                            IdpData.AlternateAuthenticationToken(
                                IdpData.SingleSignOnToken(authData.ssoToken),
                                cardAccessNumber = authTokenScope.cardAccessNumber,
                                aliasOfSecureElementEntry = authTokenScope.aliasOfSecureElementEntry,
                                healthCardCertificate = authTokenScope.healthCardCertificate.encoded
                            )
                        )
                        repository.saveDecryptedAccessToken(profileId, authData.accessToken)
                    }
                    IdpScope.BiometricPairing -> {
                        pairingRepository.saveSingleSignOnToken(
                            profileId,
                            IdpData.SingleSignOnToken(authData.ssoToken)
                        )
                    }
                }
            }
        }
    }

    private suspend fun <R> alternateAuthenticationFlowWithSecureElement(
        profileId: ProfileIdentifier,
        scope: IdpScope,
        finally: suspend (
            initialData: IdpInitialData,
            authTokenScope: IdpData.TokenWithKeyStoreAliasScope,
            authData: IdpAuthFlowResult
        ) -> R
    ): R {
        val ssoTokenScope = requireNotNull(repository.authenticationData(profileId).first().singleSignOnTokenScope)

        val authTokenScope =
            requireNotNull(ssoTokenScope as? IdpData.TokenWithKeyStoreAliasScope) { "Wrong authentication scope!" }

        val healthCardCertificate = authTokenScope.healthCardCertificate
        val aliasOfSecureElementEntry = authTokenScope.aliasOfSecureElementEntry

        lateinit var privateKeyOfSecureElementEntry: PrivateKey
        lateinit var signatureObjectOfSecureElementEntry: Signature
        @Requirement(
            "O.Cryp_1#2",
            "O.Cryp_4#2",
            sourceSpecification = "BSI-eRp-ePA",
            rationale = "Signature via ecdh ephemeral-static (one time usage)"
        )
        @Requirement(
            "O.Cryp_6",
            sourceSpecification = "BSI-eRp-ePA",
            rationale = "Persisted cryptographic keys are created within the devices key store. " +
                "Temporal keys are discarded as soon as usage is no longer needed."
        )
        @Requirement(
            "O.Cryp_7",
            sourceSpecification = "BSI-eRp-ePA",
            rationale = "As Brainpool256R1 is not available within key store but enforced by BSI where possible, " +
                "we use secure enclave encryption only for biometric authentication. " +
                "Everywhere else, cryptographic operations are ephemeral or use the eGK " +
                "as a secure execution environment."
        )
        try {
            privateKeyOfSecureElementEntry = (
                cryptoProvider.keyStoreInstance()
                    .apply { load(null) }
                    .getEntry(
                        Base64.toBase64String(aliasOfSecureElementEntry),
                        null
                    ) as KeyStore.PrivateKeyEntry
                ).privateKey
            signatureObjectOfSecureElementEntry = cryptoProvider.signatureInstance()
        } catch (e: Exception) {
            // the system might have removed the key during biometric re-enrollment
            // therefore there's no choice but to delete everything
            repository.invalidate(profileId)
            throw AltAuthenticationCryptoException(e)
        }

        val initialData = basicUseCase.initializeConfigurationAndKeys()
        val challengeData = basicUseCase.challengeFlow(initialData, scope = scope, redirectUri = REDIRECT_URI)

        val authData = altAuthUseCase.authenticateWithSecureElement(
            initialData = initialData,
            challenge = challengeData.challenge,
            healthCardCertificate = healthCardCertificate.encoded,
            authenticationMethod = IdpAlternateAuthenticationUseCase.AuthenticationMethod.Strong,
            aliasOfSecureElementEntry = aliasOfSecureElementEntry,
            privateKeyOfSecureElementEntry = privateKeyOfSecureElementEntry,
            signatureObjectOfSecureElementEntry = signatureObjectOfSecureElementEntry
        )

        return finally(
            initialData,
            authTokenScope,
            authData
        )
    }

    /**
     * Returns the paired devices associated with the [profileId]s sso token scope.
     *
     * @param authenticateWithSecureElement will be called if an alternate authentication is required.
     * @param authenticateWithHealthCard will be called if a health card authentication is required
     *                                   which needs to sign [hash].
     */
    suspend fun getPairedDevices(profileId: ProfileIdentifier): Result<List<Pair<PairingResponseEntry, PairingData>>> =
        redoOnce {
            val accessToken = loadAccessToken(
                refresh = it,
                profileId = profileId,
                scope = IdpScope.BiometricPairing
            )

            altAuthUseCase.getPairedDevices(
                initialData = basicUseCase.initializeConfigurationAndKeys(),
                accessToken = accessToken
            )
        }

    /**
     * Deletes the device identified by [deviceAlias].
     */
    suspend fun deletePairedDevice(profileId: ProfileIdentifier, deviceAlias: String) =
        redoOnce {
            val accessToken = loadAccessToken(
                refresh = it,
                profileId = profileId,
                scope = IdpScope.BiometricPairing
            )

            altAuthUseCase.deletePairedDevice(
                initialData = basicUseCase.initializeConfigurationAndKeys(),
                accessToken = accessToken,
                deviceAlias = deviceAlias
            )
        }

    private suspend fun <R> redoOnce(
        block: suspend (retry: Boolean) -> R
    ) =
        runCatching {
            block(false)
        }.recoverCatching { e ->
            val isRetryable = (e as? ApiCallException)?.let {
                it.response.code() == HttpURLConnection.HTTP_FORBIDDEN ||
                    it.response.code() == HttpURLConnection.HTTP_UNAUTHORIZED
            } ?: false
            if (isRetryable) {
                block(true)
            } else {
                throw e
            }
        }
}
