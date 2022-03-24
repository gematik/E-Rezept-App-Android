/*
 * Copyright (c) 2022 gematik GmbH
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

import android.annotation.SuppressLint
import android.content.SharedPreferences
import android.net.Uri
import de.gematik.ti.erp.app.api.ApiCallException
import de.gematik.ti.erp.app.di.NetworkSecureSharedPreferences
import de.gematik.ti.erp.app.idp.api.EXT_AUTH_REDIRECT_URI
import de.gematik.ti.erp.app.idp.api.IdpService
import de.gematik.ti.erp.app.idp.api.models.AuthenticationID
import de.gematik.ti.erp.app.idp.repository.IdpRepository
import de.gematik.ti.erp.app.idp.repository.SingleSignOnToken
import de.gematik.ti.erp.app.profiles.repository.ProfilesRepository
import de.gematik.ti.erp.app.vau.extractECPublicKey
import java.io.IOException
import java.net.URI
import java.security.KeyStore
import java.security.PrivateKey
import java.security.PublicKey
import java.security.Signature
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import timber.log.Timber

/**
 * Exception thrown by [IdpUseCase.loadAccessToken].
 */
class RefreshFlowException : IOException {
    /**
     * Is true if the sso token is not valid anymore and the user is required to authenticate again.
     */
    val userActionRequired: Boolean
    val ssoToken: SingleSignOnToken?

    constructor(
        userActionRequired: Boolean,
        ssoToken: SingleSignOnToken?,
        cause: Throwable
    ) : super(cause) {
        this.userActionRequired = userActionRequired
        this.ssoToken = ssoToken
    }

    constructor(
        userActionRequired: Boolean,
        ssoToken: SingleSignOnToken?,
        message: String
    ) : super(message) {
        this.userActionRequired = userActionRequired
        this.ssoToken = ssoToken
    }
}

class IDPConfigException(cause: Throwable) : IOException(cause)

class AltAuthenticationCryptoException(cause: Throwable) : IllegalStateException(cause)

private const val EXT_AUTH_CODE_CHALLENGE: String = "EXT_AUTH_CODE_CHALLENGE"
private const val EXT_AUTH_CODE_VERIFIER: String = "EXT_AUTH_CODE_VERIFIER"
private const val EXT_AUTH_STATE: String = "EXT_AUTH_STATE"
private const val EXT_AUTH_NONCE: String = "EXT_AUTH_NONCE"

@Singleton
class IdpUseCase @Inject constructor(
    private val repository: IdpRepository,
    private val altAuthUseCase: IdpAlternateAuthenticationUseCase,
    private val profilesRepository: ProfilesRepository,
    private val basicUseCase: IdpBasicUseCase,
    @NetworkSecureSharedPreferences
    private val sharedPreferences: SharedPreferences,
    private val cryptoProvider: IdpCryptoProvider
) {
    private val lock = Mutex()

    /**
     * If no bearer token is set or [refresh] is true, this will trigger [IdpBasicUseCase.refreshAccessTokenWithSsoFlow].
     */
    suspend fun loadAccessToken(refresh: Boolean = false, profileName: String): String = lock.withLock {
        val ssoToken = repository.getSingleSignOnToken(profileName).first()

        when (ssoToken) {
            null,
            is SingleSignOnToken.AlternateAuthenticationWithoutToken -> {
                repository.invalidateDecryptedAccessToken(profileName)
                throw RefreshFlowException(
                    true,
                    ssoToken,
                    "SSO token not set for $profileName!"
                )
            }
            is SingleSignOnToken.AlternateAuthenticationToken,
            is SingleSignOnToken.DefaultToken -> {
                val accToken = repository.decryptedAccessTokenMap.value[profileName]

                if (refresh || accToken == null) {
                    repository.invalidateDecryptedAccessToken(profileName)

                    val actualToken = when (ssoToken) {
                        is SingleSignOnToken.AlternateAuthenticationToken -> ssoToken.token
                        is SingleSignOnToken.DefaultToken -> ssoToken.token
                        else -> error("Unknown token scope")
                    }

                    val initialData = try {
                        basicUseCase.initializeConfigurationAndKeys()
                    } catch (e: Exception) {
                        throw IDPConfigException(e)
                    }
                    try {
                        val refreshData = basicUseCase.refreshAccessTokenWithSsoFlow(
                            initialData,
                            scope = IdpScope.Default,
                            ssoToken = actualToken
                        )
                        refreshData.accessToken
                    } catch (e: Exception) {
                        Timber.e(e, "Couldn't refresh access token")
                        (e as? ApiCallException)?.also {
                            when (it.response.code()) {
                                // 400 returned by redirect call if sso token is not valid anymore
                                400, 401, 403 -> {
                                    repository.invalidateSingleSignOnTokenRetainingScope(profileName)
                                    throw RefreshFlowException(true, ssoToken, e)
                                }
                            }
                        }
                        throw RefreshFlowException(false, null, e)
                    }
                } else {
                    accToken
                }
                    .also {
                        repository.decryptedAccessTokenMap.update { decryptedAccessTokenMap ->
                            decryptedAccessTokenMap + (profileName to it)
                        }
                    }
            }
        }
    }

    /**
     * Initial flow fetching the sso & access token requiring the health card to sign the challenge.
     */
    suspend fun authenticationFlowWithHealthCard(
        healthCardCertificate: suspend () -> ByteArray,
        sign: suspend (hash: ByteArray) -> ByteArray
    ) = lock.withLock {
        val initialData = basicUseCase.initializeConfigurationAndKeys()
        val challengeData =
            basicUseCase.challengeFlow(initialData, scope = IdpScope.Default)
        val activeProfileName = getActiveProfileName()
        val basicData = basicUseCase.basicAuthFlow(
            initialData = initialData,
            challengeData = challengeData,
            healthCardCertificate = healthCardCertificate(),
            sign = sign
        )
        val ssoToken = SingleSignOnToken.DefaultToken(
            token = basicData.ssoToken
        )
        profilesRepository.setInsuranceInformation(
            activeProfileName,
            basicData.idTokenInsurantName,
            basicData.idTokenInsuranceIdentifier,
            basicData.idTokenInsuranceName
        )
        repository.setSingleSignOnToken(activeProfileName, ssoToken)
        repository.decryptedAccessTokenMap.update { decryptedAccessTokenMap ->
            decryptedAccessTokenMap + (activeProfileName to basicData.accessToken)
        }
    }

    private suspend fun getActiveProfileName() =
        profilesRepository.activeProfile().map { it.profileName }.first()

    /**
     * Get all the information for the correct endpoints from the discovery document and request
     * the external Health Insurance Companies which are capable of authenticate you with their app
     */
    suspend fun downloadDiscoveryDocumentAndGetExternAuthenticatorIDs(): List<AuthenticationID> {
        val initialData = basicUseCase.initializeConfigurationAndKeys()
        return repository.fetchExternalAuthorizationIDList(
            initialData.config.externalAuthorizationIDsEndpoint ?: error("Fasttrack is not available"),
            idpPukSigKey = initialData.config.certificate.extractECPublicKey()
        )
    }

    /**
     * With chosen Health Insurance Company, request IDP for Authentication information,
     * sent as a redirect which is supposed to be fired as an Intent
     * @param externalAuthorizationID identifier of the health insurance company
     */
    @SuppressLint("ApplySharedPref")
    suspend fun getUniversalLinkForExternalAuthorization(externalAuthorizationID: String): Uri {
        val initialData = basicUseCase.initializeConfigurationAndKeys()

        val redirectUri = repository.getAuthorizationRedirect(
            url = initialData.config.thirdPartyAuthorizationEndpoint ?: error("Fasttrack is not available"),
            state = initialData.state,
            codeChallenge = initialData.codeChallenge,
            nonce = initialData.nonce,
            kkAppId = externalAuthorizationID
        )

        val parsedUri = Uri.parse(redirectUri)

        sharedPreferences.edit()
            .putString(EXT_AUTH_STATE, parsedUri.getQueryParameter("state"))
            .putString(EXT_AUTH_NONCE, initialData.nonce.nonce)
            .putString(EXT_AUTH_CODE_VERIFIER, initialData.codeVerifier)
            .putString(EXT_AUTH_CODE_CHALLENGE, initialData.codeChallenge).commit()

        return parsedUri
    }

    class ExternalAuthorizationData(uri: URI) {
        val code = IdpService.extractQueryParameter(uri, "code")
        val state = IdpService.extractQueryParameter(uri, "state")
        val kkAppRedirectUri = IdpService.extractQueryParameter(uri, "kk_app_redirect_uri")
    }

    suspend fun authenticateWithExternalAppAuthorization(uri: URI) {

        val externalAuthorizationData = ExternalAuthorizationData(uri)

        require(externalAuthorizationData.state == sharedPreferences.getString(EXT_AUTH_STATE, ""))

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
            nonce = IdpNonce(sharedPreferences.getString(EXT_AUTH_NONCE, "")!!),
            codeVerifier = sharedPreferences.getString(EXT_AUTH_CODE_VERIFIER, "")!!,
            code = redirectCodeJwe,
            pukEncKey = initialData.pukEncKey,
            pukSigKey = initialData.pukSigKey,
            redirectUri = EXT_AUTH_REDIRECT_URI
        )
        val activeProfileName = getActiveProfileName()

        repository.setSingleSignOnToken(
            activeProfileName,
            SingleSignOnToken.DefaultToken(redirectSsoToken)
        )
        repository.decryptedAccessTokenMap.update { decryptedAccessTokenMap ->
            decryptedAccessTokenMap + (activeProfileName to idpTokenResult.decryptedAccessToken)
        }
    }

    /**
     * Pairing flow fetching the sso & access token requiring the health card and generated key material.
     */
    suspend fun alternatePairingFlowWithSecureElement(
        publicKeyOfSecureElementEntry: PublicKey,
        aliasOfSecureElementEntry: ByteArray,
        healthCardCertificate: suspend () -> ByteArray,
        signWithHealthCard: suspend (hash: ByteArray) -> ByteArray
    ) = lock.withLock {
        val initialData = basicUseCase.initializeConfigurationAndKeys()
        val challengeData =
            basicUseCase.challengeFlow(
                initialData,
                scope = IdpScope.BiometricPairing
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
            signWithHealthCard = signWithHealthCard,
        )
        val activeProfileName = getActiveProfileName()
        profilesRepository.setInsuranceInformation(
            activeProfileName,
            basicData.idTokenInsurantName,
            basicData.idTokenInsuranceIdentifier,
            basicData.idTokenInsuranceName
        )
        repository.setHealthCardCertificate(activeProfileName, healthCardCert)
        // set pairing scope
        repository.setSingleSignOnToken(activeProfileName, SingleSignOnToken.AlternateAuthenticationWithoutToken())
        repository.setAliasOfSecureElementEntry(activeProfileName, aliasOfSecureElementEntry)
    }

    /**
     * Actual authentication with secure element key material. Just like the [authenticationFlowWithHealthCard] it
     * sets the sso & access token within the repository.
     */
    suspend fun alternateAuthenticationFlowWithSecureElement(profileName: String) = lock.withLock {
        val healthCardCertificate =
            requireNotNull(
                repository.getHealthCardCertificate(profileName).first()
            ) { "Health card certificate not set! Maybe you forgot to call alternatePairingFlowWithSecureElement before." }
        val aliasOfSecureElementEntry =
            requireNotNull(
                repository.getAliasOfSecureElementEntry(profileName).first()
            ) { "Alias of secure element entry not set! Maybe you forgot to call alternatePairingFlowWithSecureElement before." }

        lateinit var privateKeyOfSecureElementEntry: PrivateKey
        lateinit var signatureObjectOfSecureElementEntry: Signature

        try {
            privateKeyOfSecureElementEntry = (
                cryptoProvider.keyStoreInstance()
                    .apply { load(null) }
                    .getEntry(
                        aliasOfSecureElementEntry.decodeToString(),
                        null
                    ) as KeyStore.PrivateKeyEntry
                ).privateKey
            signatureObjectOfSecureElementEntry = cryptoProvider.signatureInstance()
        } catch (e: Exception) {
            // the system might have removed the key during biometric reenrollment
            // therefore there's no choice but to delete everything
            repository.invalidate(profileName)
            throw AltAuthenticationCryptoException(e)
        }

        val initialData = basicUseCase.initializeConfigurationAndKeys()
        val challengeData = basicUseCase.challengeFlow(initialData, scope = IdpScope.Default)

        val authData = altAuthUseCase.authenticateWithSecureElement(
            initialData = initialData,
            challenge = challengeData.challenge,
            healthCardCertificate = healthCardCertificate,
            authenticationMethod = IdpAlternateAuthenticationUseCase.AuthenticationMethod.Strong,
            aliasOfSecureElementEntry = aliasOfSecureElementEntry,
            privateKeyOfSecureElementEntry = privateKeyOfSecureElementEntry,
            signatureObjectOfSecureElementEntry = signatureObjectOfSecureElementEntry,
        )

        profilesRepository.setInsuranceInformation(
            profileName,
            authData.idTokenInsurantName,
            authData.idTokenInsuranceIdentifier,
            authData.idTokenInsuranceName
        )
        repository.setSingleSignOnToken(
            profileName,
            SingleSignOnToken.AlternateAuthenticationToken(
                token = authData.ssoToken,
            )
        )
        repository.decryptedAccessTokenMap.update { decryptedAccessTokenMap ->
            decryptedAccessTokenMap + (profileName to authData.accessToken)
        }
    }

    suspend fun getPairedDevicesWithSecureElement(profileName: String) = lock.withLock {
        val healthCardCertificate =
            requireNotNull(
                repository.getHealthCardCertificate(profileName).first()
            ) { "Health card certificate not set! Maybe you forgot to call alternatePairingFlowWithSecureElement before." }
        val aliasOfSecureElementEntry =
            requireNotNull(
                repository.getAliasOfSecureElementEntry(profileName).first()
            ) { "Alias of secure element entry not set! Maybe you forgot to call alternatePairingFlowWithSecureElement before." }

        lateinit var privateKeyOfSecureElementEntry: PrivateKey
        lateinit var signatureObjectOfSecureElementEntry: Signature

        try {
            privateKeyOfSecureElementEntry = (
                cryptoProvider.keyStoreInstance()
                    .apply { load(null) }
                    .getEntry(
                        aliasOfSecureElementEntry.decodeToString(),
                        null
                    ) as KeyStore.PrivateKeyEntry
                ).privateKey
            signatureObjectOfSecureElementEntry =
                cryptoProvider.signatureInstance()
        } catch (e: Exception) {
            // the system might have removed the key during biometric reenrollment
            // therefore there's no choice but to delete everything
            repository.invalidate(profileName)
            throw AltAuthenticationCryptoException(e)
        }

        val initialData = basicUseCase.initializeConfigurationAndKeys()
        val challengeData = basicUseCase.challengeFlow(initialData, scope = IdpScope.BiometricPairing)

        val authData = altAuthUseCase.authenticateWithSecureElement(
            initialData = initialData,
            challenge = challengeData.challenge,
            healthCardCertificate = healthCardCertificate,
            authenticationMethod = IdpAlternateAuthenticationUseCase.AuthenticationMethod.Strong,
            aliasOfSecureElementEntry = aliasOfSecureElementEntry,
            privateKeyOfSecureElementEntry = privateKeyOfSecureElementEntry,
            signatureObjectOfSecureElementEntry = signatureObjectOfSecureElementEntry,
        )

        altAuthUseCase.getPairedDevices(
            initialData = initialData,
            accessToken = authData.accessToken
        )
    }

    suspend fun getPairedDevices(
        healthCardCertificate: suspend () -> ByteArray,
        sign: suspend (hash: ByteArray) -> ByteArray
    ) = lock.withLock {
        val initialData = basicUseCase.initializeConfigurationAndKeys()
        val challengeData =
            basicUseCase.challengeFlow(
                initialData,
                scope = IdpScope.BiometricPairing
            )
        val healthCardCert = healthCardCertificate()
        val basicData = basicUseCase.basicAuthFlow(
            initialData = initialData,
            challengeData = challengeData,
            healthCardCertificate = healthCardCert,
            sign = sign
        )

        altAuthUseCase.getPairedDevices(
            initialData = initialData,
            accessToken = basicData.accessToken
        )
    }

    suspend fun isCanAvailable() =
        repository.cardAccessNumber(getActiveProfileName()).map { can -> can != null }.first()
}
