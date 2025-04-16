/*
 * Copyright 2025, gematik GmbH
 *
 * Licensed under the EUPL, Version 1.2 or - as soon they will be approved by the
 * European Commission – subsequent versions of the EUPL (the "Licence").
 * You may not use this work except in compliance with the Licence.
 *
 * You find a copy of the Licence in the "Licence" file or at
 * https://joinup.ec.europa.eu/collection/eupl/eupl-text-eupl-12
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the Licence is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either expressed or implied.
 * In case of changes by gematik find details in the "Readme" file.
 *
 * See the Licence for the specific language governing permissions and limitations under the Licence.
 */

@file:Suppress("LongParameterList", "TooGenericExceptionCaught", "MagicNumber", "ThrowsCount")

package de.gematik.ti.erp.app.idp.usecase

import de.gematik.ti.erp.app.Requirement
import de.gematik.ti.erp.app.api.ApiCallException
import de.gematik.ti.erp.app.idp.api.EXT_AUTH_REDIRECT_URI
import de.gematik.ti.erp.app.idp.api.REDIRECT_URI
import de.gematik.ti.erp.app.idp.api.models.IdpAuthFlowResult
import de.gematik.ti.erp.app.idp.api.models.IdpInitialData
import de.gematik.ti.erp.app.idp.api.models.IdpScope
import de.gematik.ti.erp.app.idp.api.models.PairingData
import de.gematik.ti.erp.app.idp.api.models.PairingResponseEntry
import de.gematik.ti.erp.app.idp.model.IdpData
import de.gematik.ti.erp.app.idp.repository.AccessToken
import de.gematik.ti.erp.app.idp.repository.IdpPairingRepository
import de.gematik.ti.erp.app.idp.repository.IdpRepository
import de.gematik.ti.erp.app.profiles.repository.ProfileIdentifier
import de.gematik.ti.erp.app.profiles.repository.ProfileRepository
import io.github.aakira.napier.Napier
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import org.bouncycastle.util.encoders.Base64
import java.net.HttpURLConnection.HTTP_FORBIDDEN
import java.net.HttpURLConnection.HTTP_UNAUTHORIZED
import java.security.KeyStore
import java.security.PrivateKey
import java.security.PublicKey
import java.security.Signature

class DefaultIdpUseCase(
    private val repository: IdpRepository,
    private val pairingRepository: IdpPairingRepository,
    private val altAuthUseCase: IdpAlternateAuthenticationUseCase,
    private val profilesRepository: ProfileRepository,
    private val basicUseCase: IdpBasicUseCase,
    private val cryptoProvider: IdpCryptoProvider,
    private val lock: Mutex
) : IdpUseCase {

    /**
     * If no bearer token is set or [refresh] is true, this will trigger [IdpBasicUseCase.refreshAccessTokenWithSsoFlow].
     */
    @Requirement(
        "A_20283-01#1",
        sourceSpecification = "gemSpec_IDP_Frontend",
        rationale = "Load and decrypt access token."
    )
    override suspend fun loadAccessToken(
        profileId: ProfileIdentifier,
        refresh: Boolean,
        scope: IdpScope
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
                    }
                ) { accessToken, expiresOn ->
                    repository.saveDecryptedAccessToken(
                        profileId,
                        AccessToken(accessToken, expiresOn)
                    )
                }

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
                    }
                ) { accessToken, expiresOn ->
                    pairingRepository.saveDecryptedAccessToken(
                        profileId,
                        AccessToken(accessToken, expiresOn)
                    )
                }
        }
    }

    @Suppress("CyclomaticComplexMethod", "NestedBlockDepth")
    private suspend fun loadAccessToken(
        refresh: Boolean = false,
        profileId: ProfileIdentifier,
        scope: IdpScope,
        singleSignOnTokenScope: suspend () -> IdpData.SingleSignOnTokenScope?,
        decryptedAccessToken: suspend () -> AccessToken?,
        invalidateDecryptedAccessToken: suspend () -> Unit,
        invalidateSingleSignOnTokenRetainingScope: suspend () -> Unit,
        saveDecryptedAccessToken: suspend (decryptedAccessToken: String, expiresOn: Instant) -> Unit
    ): String {
        val ssoTokenScope = singleSignOnTokenScope()
        val savedAccessToken = decryptedAccessToken()
        val accessToken = savedAccessToken?.accessToken

        val isExpired = if (savedAccessToken == null) {
            true
        } else {
            savedAccessToken.expiresOn <= Clock.System.now()
        }

        Napier.d(tag = "IdpUseCase") {
            """Loading access token with:
              |ssoTokenScope.present: ${ssoTokenScope != null}
              |refresh: $refresh
              |profileId: $profileId
              |scope: $scope
              |access-token: $accessToken
              |isExpired: $isExpired
              |isExpiredOn: ${savedAccessToken?.expiresOn}
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

            if (ssoTokenScope.token?.isValid() == false) {
                Napier.e(tag = "IdpUseCase") { "expired SSO Token ${ssoTokenScope.token?.token}" }
            }

            if (refresh || accessToken == null || isExpired) {
                invalidateDecryptedAccessToken()

                ssoTokenScope.token?.token?.let { actualToken ->
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
                        saveDecryptedAccessToken(refreshData.accessToken, refreshData.expiresOn)
                        refreshData.accessToken
                    } catch (e: Exception) {
                        Napier.e(tag = "IdpUseCase", message = "Couldn't refresh access token", throwable = e)
                        (e as? ApiCallException)?.also {
                            when (it.response.code()) {
                                // 400 returned by redirect call if sso token is not valid anymore
                                400, 401, 403 -> {
                                    Napier.e(tag = "IdpUseCase") { "RefreshFlowException due to ApiCallException.code ${it.response.code()}" }
                                    invalidateSingleSignOnTokenRetainingScope()
                                    throw RefreshFlowException(true, ssoTokenScope, e)
                                }
                            }
                        }
                        throw RefreshFlowException(false, null, e)
                    }
                } ?: run {
                    Napier.e(tag = "IdpUseCase", message = "ssoTokenScope.token?.token is null!")
                    invalidateDecryptedAccessToken()
                    throw RefreshFlowException(false, null, NullPointerException("SSO token is null"))
                }
            } else {
                savedAccessToken.accessToken
            }
        } else {
            invalidateDecryptedAccessToken()
            throw RefreshFlowException(
                true,
                null,
                "SSO token-scope is null for $profileId!"
            )
        }
    }

    /**
     * Initial flow fetching the sso & access token requiring the health card to sign the challenge.
     */
    // // A_20601-01
    @Requirement(
        "A_20167-02#1",
        sourceSpecification = "gemSpec_eRp_FdV",
        rationale = "Authenticate to the IDP using the health card certificate"
    )
    override suspend fun authenticationFlowWithHealthCard(
        profileId: ProfileIdentifier,
        scope: IdpScope,
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
                        repository.saveDecryptedAccessToken(
                            profileId,
                            AccessToken(basicData.accessToken, basicData.expiresOn)
                        )
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
     * Pairing flow fetching the sso & access token requiring the health card and generated key material.
     */
    @Requirement(
        "A_20167-02#2",
        sourceSpecification = "gemSpec_eRp_FdV",
        rationale = "Authenticate to the IDP using the health card certificate an secure element."
    )
    override suspend fun alternatePairingFlowWithSecureElement(
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
        "A_20167-02#3",
        sourceSpecification = "gemSpec_eRp_FdV",
        rationale = "Authenticate to the IDP using an secure element."
    )
    override suspend fun alternateAuthenticationFlowWithSecureElement(
        profileId: ProfileIdentifier,
        scope: IdpScope
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
                        repository.saveDecryptedAccessToken(
                            profileId,
                            AccessToken(authData.accessToken, authData.expiresOn)
                        )
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
        val ssoTokenScope = requireNotNull(repository.authenticationData(profileId).first().singleSignOnTokenScope) // TODO: Throws IllegalStateException

        val authTokenScope =
            requireNotNull(ssoTokenScope as? IdpData.TokenWithKeyStoreAliasScope) { "Wrong authentication scope!" } // TODO: Throws IllegalStateException

        val healthCardCertificate = authTokenScope.healthCardCertificate
        val aliasOfSecureElementEntry = authTokenScope.aliasOfSecureElementEntry

        @Requirement(
            "O.Cryp_7#1",
            sourceSpecification = "BSI-eRp-ePA",
            rationale = "java.security.PrivateKey used as the private-key container.",
            codeLines = 2
        )
        lateinit var privateKeyOfSecureElementEntry: PrivateKey
        lateinit var signatureObjectOfSecureElementEntry: Signature
        @Requirement(
            "O.Cryp_1#2",
            sourceSpecification = "BSI-eRp-ePA",
            rationale = "Signature via ecdh ephemeral-static [one time usage]",
            codeLines = 30
        )
        @Requirement(
            "O.Cryp_4#3",
            sourceSpecification = "BSI-eRp-ePA",
            rationale = "One time usage for JWE ECDH-ES Encryption"
        )
        @Requirement(
            "O.Cryp_6#1",
            sourceSpecification = "BSI-eRp-ePA",
            rationale = "Secure enclave key generation",
            codeLines = 18
        )
        @Requirement(
            "A_21590#2",
            sourceSpecification = "gemSpec_IDP_Frontend",
            rationale = "Key generation for authentication",
            codeLines = 18
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
     * [de.gematik.ti.erp.app.cardwall.usecase.AuthenticationUseCase.authenticateWithSecureElement] will be called if an alternate authentication is required.
     * [de.gematik.ti.erp.app.cardwall.usecase.AuthenticationUseCase.authenticateWithHealthCard] will be called if a health card authentication is required
     *                                   which needs to sign [hash].
     */
    override suspend fun getPairedDevices(profileId: ProfileIdentifier):
        Result<List<Pair<PairingResponseEntry, PairingData>>> =
        redoOnce {
            val accessToken = loadAccessToken(
                refresh = it,
                profileId = profileId,
                scope = IdpScope.BiometricPairing
            )

            Napier.e { "access token $accessToken" }

            altAuthUseCase.getPairedDevices(
                initialData = basicUseCase.initializeConfigurationAndKeys(),
                accessToken = accessToken
            )
        }

    /**
     * Deletes the device identified by [deviceAlias].
     */
    override suspend fun deletePairedDevice(profileId: ProfileIdentifier, deviceAlias: String) =
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
        }.recoverCatching { exception ->
            Napier.e { "retrying due to ${exception.stackTraceToString()}" }
            val isRetryable = (exception as? ApiCallException)?.let {
                it.response.code() == HTTP_FORBIDDEN || it.response.code() == HTTP_UNAUTHORIZED
            } ?: false
            if (isRetryable) {
                block(true)
            } else {
                throw exception
            }
        }
}
