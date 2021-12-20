/*
 * Copyright (c) 2021 gematik GmbH
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
import de.gematik.ti.erp.app.api.Result
import de.gematik.ti.erp.app.di.NetworkSecureSharedPreferences
import de.gematik.ti.erp.app.idp.api.EXT_AUTH_REDIRECT_URI
import de.gematik.ti.erp.app.idp.api.IdpService
import de.gematik.ti.erp.app.idp.api.models.AuthenticationID
import de.gematik.ti.erp.app.idp.repository.IdpRepository
import de.gematik.ti.erp.app.idp.repository.SingleSignOnToken
import de.gematik.ti.erp.app.vau.extractECPublicKey
import kotlinx.coroutines.sync.withLock
import de.gematik.ti.erp.app.profiles.usecase.ProfilesUseCase
import kotlinx.coroutines.ExperimentalCoroutinesApi
import java.io.IOException
import java.security.KeyStore
import java.security.PrivateKey
import java.security.PublicKey
import java.security.Signature
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.sync.Mutex
import timber.log.Timber

/**
 * Exception thrown by [IdpUseCase.loadAccessToken].
 */
class RefreshFlowException : IOException {
    /**
     * Is true if the sso token is not valid anymore and the user is required to authenticate again.
     */
    val userActionRequired: Boolean
    val tokenScope: SingleSignOnToken.Scope?

    constructor(
        userActionRequired: Boolean,
        tokenScope: SingleSignOnToken.Scope?,
        cause: Throwable
    ) : super(cause) {
        this.userActionRequired = userActionRequired
        this.tokenScope = tokenScope
    }

    constructor(
        userActionRequired: Boolean,
        tokenScope: SingleSignOnToken.Scope?,
        message: String
    ) : super(message) {
        this.userActionRequired = userActionRequired
        this.tokenScope = tokenScope
    }
}

class AltAuthenticationCryptoException(cause: Throwable) : IllegalStateException(cause)

private const val EXT_AUTH_CODE_CHALLENGE: String = "EXT_AUTH_CODE_CHALLENGE"
private const val EXT_AUTH_CODE_VERIFIER: String = "EXT_AUTH_CODE_VERIFIER"
private const val EXT_AUTH_STATE: String = "EXT_AUTH_STATE"
private const val EXT_AUTH_NONCE: String = "EXT_AUTH_NONCE"

@Singleton
class IdpUseCase @Inject constructor(
    private val repository: IdpRepository,
    private val basicUseCase: IdpBasicUseCase,
    private val altAuthUseCase: IdpAlternateAuthenticationUseCase,
    private val profilesUseCase: ProfilesUseCase,
    @NetworkSecureSharedPreferences
    private val sharedPreferences: SharedPreferences
) {
    private val lock = Mutex()

    /**
     * If no bearer token is set or [refresh] is true, this will trigger [IdpBasicUseCase.refreshAccessTokenWithSsoFlow].
     */
    suspend fun loadAccessToken(refresh: Boolean = false, profileName: String): String =
        lock.withLock {
            val ssoToken = repository.getSingleSignOnToken(profileName).first()
            if (ssoToken == null) {
                repository.invalidateDecryptedAccessToken(profileName)
                throw RefreshFlowException(
                    true,
                    repository.getSingleSignOnTokenScope(profileName).first(),
                    "SSO token not set for $profileName!"
                )
            }

            val accToken = repository.decryptedAccessTokenMap.value[profileName]

            if (refresh || accToken == null) {
                repository.invalidateDecryptedAccessToken(profileName)

                val initialData = basicUseCase.initializeConfigurationAndKeys()
                try {
                    val refreshData = basicUseCase.refreshAccessTokenWithSsoFlow(
                        initialData,
                        scope = IdpScope.Default,
                        ssoToken = ssoToken.token
                    )
                    refreshData.accessToken
                } catch (e: Exception) {
                    Timber.e(e, "Couldn't refresh access token")
                    (e as? ApiCallException)?.also {
                        when (it.response.code()) {
                            // 400 returned by redirect call if sso token is not valid anymore
                            400, 401, 403 -> {
                                repository.invalidateSingleSignOnTokenRetainingScope(profileName)
                                throw RefreshFlowException(true, ssoToken.scope, e)
                            }
                        }
                    }
                    throw RefreshFlowException(false, null, e)
                }
            } else {
                accToken
            }.also {
                repository.decryptedAccessTokenMap.update { decryptedAccessTokenMap ->
                    decryptedAccessTokenMap + (profileName to it)
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
        val challengeData = basicUseCase.challengeFlow(initialData, scope = IdpScope.Default)
        val activeProfileName = profilesUseCase.activeProfileName().first()
        val basicData = basicUseCase.basicAuthFlow(
            initialData = initialData,
            challengeData = challengeData,
            healthCardCertificate = healthCardCertificate(),
            sign = sign
        )
        val ssoToken = SingleSignOnToken(
            token = basicData.ssoToken
        )
        repository.setSingleSignOnToken(activeProfileName, ssoToken)
        repository.decryptedAccessTokenMap.update { decryptedAccessTokenMap ->
            decryptedAccessTokenMap + (activeProfileName to basicData.accessToken)
        }
    }

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

    class ExternalAuthorizationData(uri: Uri) {
        val code = IdpService.extractQueryParameter(uri, "code")
        val state = IdpService.extractQueryParameter(uri, "state")
        val kkAppRedirectUri = IdpService.extractQueryParameter(uri, "kk_app_redirect_uri")
    }

    suspend fun authenticateWithExternalAppAuthorization(uri: Uri) {

        val externalAuthorizationData = ExternalAuthorizationData(uri)

        require(externalAuthorizationData.state == sharedPreferences.getString(EXT_AUTH_STATE, ""))

        val initialData = basicUseCase.initializeConfigurationAndKeys()
        val redirectStringResult = repository.postExternAppAuthorizationData(
            url = initialData.config.thirdPartyAuthorizationEndpoint ?: error("Fasttrack is not available"),
            externalAuthorizationData = externalAuthorizationData
        )
        if (redirectStringResult is Result.Error) {
            error(redirectStringResult.exception)
        }
        val redirect = Uri.parse((redirectStringResult as Result.Success).data)

        val redirectCodeJwe = IdpService.extractQueryParameter(redirect, "code")
        val redirectSsoToken = IdpService.extractQueryParameter(redirect, "ssotoken")

        val accessToken = basicUseCase.postCodeAndDecryptAccessToken(
            url = initialData.config.tokenEndpoint,
            nonce = IdpNonce(sharedPreferences.getString(EXT_AUTH_NONCE, "")!!),
            codeVerifier = sharedPreferences.getString(EXT_AUTH_CODE_VERIFIER, "")!!,
            code = redirectCodeJwe,
            pukEncKey = initialData.pukEncKey,
            pukSigKey = initialData.pukSigKey,
            redirectUri = EXT_AUTH_REDIRECT_URI
        )
        val activeProfileName = profilesUseCase.activeProfileName().first()

        repository.setSingleSignOnToken(activeProfileName, SingleSignOnToken(redirectSsoToken, scope = SingleSignOnToken.Scope.Default,))
        repository.decryptedAccessTokenMap.update { decryptedAccessTokenMap ->
            decryptedAccessTokenMap + (activeProfileName to accessToken)
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
            basicUseCase.challengeFlow(initialData, scope = IdpScope.BiometricPairing)
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
        val activeProfileName = profilesUseCase.activeProfileName().first()

        repository.setHealthCardCertificate(activeProfileName, healthCardCert)
        repository.setScopeToPairing(activeProfileName)
        repository.setAliasOfSecureElementEntry(activeProfileName, aliasOfSecureElementEntry)
    }

    /**
     * Actual authentication with secure element key material. Just like the [authenticationFlowWithHealthCard] it
     * sets the sso & access token within the repository.
     */
    suspend fun alternateAuthenticationFlowWithSecureElement(profileName: String) = lock.withLock {
        val healthCardCertificate =
            requireNotNull(repository.getHealthCardCertificate(profileName).first()) { "Health card certificate not set! Maybe you forgot to call alternatePairingFlowWithSecureElement before." }
        val aliasOfSecureElementEntry =
            requireNotNull(repository.getAliasOfSecureElementEntry(profileName).first()) { "Alias of secure element entry not set! Maybe you forgot to call alternatePairingFlowWithSecureElement before." }

        lateinit var privateKeyOfSecureElementEntry: PrivateKey
        lateinit var signatureObjectOfSecureElementEntry: Signature

        try {
            privateKeyOfSecureElementEntry = (
                KeyStore.getInstance("AndroidKeyStore")
                    .apply { load(null) }
                    .getEntry(
                        aliasOfSecureElementEntry.decodeToString(),
                        null
                    ) as KeyStore.PrivateKeyEntry
                ).privateKey
            signatureObjectOfSecureElementEntry =
                Signature.getInstance("SHA256withECDSA", "AndroidKeyStoreBCWorkaround")
        } catch (e: Exception) {
            // the system might have removed the key during biometric reenrollment
            // therefore their is no choice but to delete everything
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

        repository.setSingleSignOnToken(
            profileName,
            SingleSignOnToken(
                token = authData.ssoToken,
                scope = SingleSignOnToken.Scope.AlternateAuthentication
            )
        )
        repository.decryptedAccessTokenMap.update { decryptedAccessTokenMap ->
            decryptedAccessTokenMap + (profileName to authData.accessToken)
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    fun isCanAvailable() =
        profilesUseCase.activeProfileName().flatMapLatest {
            repository.cardAccessNumber(it).map { can ->
                can != null
            }
        }
}
