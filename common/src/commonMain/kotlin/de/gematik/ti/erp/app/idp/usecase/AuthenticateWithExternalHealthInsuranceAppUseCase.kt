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
import de.gematik.ti.erp.app.idp.api.EXT_AUTH_REDIRECT_URI
import de.gematik.ti.erp.app.idp.api.models.IdpNonce
import de.gematik.ti.erp.app.idp.api.models.IdpScope
import de.gematik.ti.erp.app.idp.api.models.IdpTokenResult
import de.gematik.ti.erp.app.idp.api.models.UniversalLinkToken.Companion.requireUniversalLinkToken
import de.gematik.ti.erp.app.idp.extension.extractNullableQueryParameter
import de.gematik.ti.erp.app.idp.extension.extractRequiredQueryParameter
import de.gematik.ti.erp.app.idp.model.IdpData
import de.gematik.ti.erp.app.idp.model.error.DecryptAccessTokenError
import de.gematik.ti.erp.app.idp.model.error.SingleSignOnTokenError
import de.gematik.ti.erp.app.idp.model.error.UniversalLinkError
import de.gematik.ti.erp.app.idp.repository.AccessToken
import de.gematik.ti.erp.app.idp.repository.IdpPairingRepository
import de.gematik.ti.erp.app.idp.repository.IdpRepository
import de.gematik.ti.erp.app.profiles.repository.ProfileRepository
import de.gematik.ti.erp.app.utils.letNotNull
import io.github.aakira.napier.Napier
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import java.net.URI

@Requirement(
    "A_20527#2",
    "A_20600#2",
    "A_20601",
    "A_20601-01",
    "A_22301",
    sourceSpecification = "gemSpec_IDP_Frontend",
    rationale = "External authentication (Fast-Track / Gesundheit-Id)"
)
@Requirement(
    "O.Plat_10#1",
    sourceSpecification = "BSI-eRp-ePA",
    rationale = "Follow redirect"
)
class AuthenticateWithExternalHealthInsuranceAppUseCase(
    private val idpRepository: IdpRepository,
    private val basicUseCase: IdpBasicUseCase,
    private val preferences: IdpPreferenceProvider,
    private val profilesRepository: ProfileRepository,
    private val pairingRepository: IdpPairingRepository,
    private val lock: Mutex,
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO
) {

    suspend operator fun invoke(
        uri: URI
    ) {
        try {
            lock.withLock {
                withContext(dispatcher) {
                    letNotNull(
                        preferences.externalAuthenticationPreferences.extAuthScope,
                        preferences.externalAuthenticationPreferences.extAuthProfile
                    ) { scope, profileId ->
                        val initialData = basicUseCase.initializeConfigurationAndKeys()

                        Napier.d { "URI $uri" }
                        val universalLinkToken = uri.requireUniversalLinkToken()

                        val authorizationEndPoint = initialData.config.requiredAuthorizationEndPoint()

                        idpRepository
                            .authorizeExternalHealthInsuranceAppDataAsGiD(
                                url = authorizationEndPoint,
                                token = universalLinkToken
                            )
                            .fold(
                                onSuccess = { absoluteUrl ->
                                    val redirectUri = URI(absoluteUrl)
                                    Napier.d { "Authorized redirect url ${redirectUri.query} " }

                                    val redirectCodeJwe = redirectUri.extractRequiredQueryParameter(key = "code")

                                    if (universalLinkToken.isGid) {
                                        val state = redirectUri.extractRequiredQueryParameter(key = "state")

                                        assert(state == preferences.externalAuthenticationPreferences.extAuthState)
                                    } else {
                                        assert(
                                            universalLinkToken.state ==
                                                preferences.externalAuthenticationPreferences.extAuthState
                                        )
                                    }
                                    val redirectSsoToken = redirectUri.extractNullableQueryParameter(
                                        key = "ssotoken"
                                    )
                                    val idpTokenResult = letNotNull(
                                        preferences.externalAuthenticationPreferences.extAuthNonce,
                                        preferences.externalAuthenticationPreferences.extAuthCodeVerifier
                                    ) { nonce, codeVerifier ->
                                        basicUseCase.postCodeAndDecryptAccessToken(
                                            url = initialData.config.tokenEndpoint,
                                            nonce = IdpNonce(nonce),
                                            codeVerifier = codeVerifier,
                                            code = redirectCodeJwe,
                                            pukEncKey = initialData.pukEncKey,
                                            pukSigKey = initialData.pukSigKey,
                                            redirectUri = EXT_AUTH_REDIRECT_URI
                                        )
                                    }

                                    val authenticationId = preferences.externalAuthenticationPreferences.extAuthId
                                    val authenticationName = preferences.externalAuthenticationPreferences.extAuthName

                                    preferences.clear()

                                    when (scope) {
                                        IdpScope.Default.name -> {
                                            val idTokenJson = idpTokenResult?.let {
                                                Json.parseToJsonElement(it.idTokenPayload)
                                            }

                                            profilesRepository.saveInsuranceInformation(
                                                profileId = profileId,
                                                insurantName = insurantName(idTokenJson),
                                                insuranceIdentifier = insurantIdentifier(idTokenJson),
                                                insuranceName = insuranceName(idTokenJson)
                                            )

                                            saveDefaultSsoToken(
                                                authenticationId,
                                                authenticationName,
                                                redirectSsoToken,
                                                profileId
                                            )

                                            saveAccessToken(idpTokenResult, profileId)
                                        }

                                        IdpScope.BiometricPairing.name -> {
                                            saveBiometricSsoToken(redirectSsoToken, profileId)
                                            saveAccessToken(idpTokenResult, profileId)
                                        }

                                        else -> {
                                            throw UniversalLinkError("Unknown scope on external insurance handling")
                                        }
                                    }
                                },
                                onFailure = {
                                    Napier.i { "failure on authorizing the external url data (Fast-track or GiD)" }
                                    throw UniversalLinkError(it.message)
                                }
                            )
                    }
                        ?: UniversalLinkError(
                            """
                            Missing externalAuthenticationPreferences on authentication with extern app
                            """.trimIndent()
                        )
                }
            }
        } catch (e: Throwable) {
            Napier.e { "error on gid/fast-track authentication process ${e.message}" }
            throw UniversalLinkError(e.message)
        }
    }

    /**
     * @throws IllegalArgumentException
     */
    private fun IdpData.IdpConfiguration.requiredAuthorizationEndPoint(
        // isGid: Boolean
    ) = requireNotNull(
        federationAuthorizationEndpoint
        /*when (isGid) {
            true -> federationAuthorizationEndpoint
            false -> thirdPartyAuthorizationEndpoint
        }*/
    ) {
        "authorizationEndPoint null, Fast-track or Gid not available"
    }

    private fun insurantIdentifier(idTokenJson: JsonElement?) =
        idTokenJson?.jsonObject?.get("idNummer")?.jsonPrimitive?.content ?: ""

    private fun insuranceName(idTokenJson: JsonElement?) =
        idTokenJson?.jsonObject?.get("organizationName")?.jsonPrimitive?.content
            ?: ""

    private fun insurantName(idTokenJson: JsonElement?) =
        idTokenJson?.jsonObject?.get("given_name")?.jsonPrimitive?.content
            ?.let {
                "$it ${
                idTokenJson.jsonObject["family_name"]
                    ?.jsonPrimitive?.content
                }"
            } ?: ""

    private fun saveAccessToken(
        idpTokenResult: IdpTokenResult?,
        profileId: String
    ) {
        idpTokenResult?.let {
            idpRepository.saveDecryptedAccessToken(
                profileId,
                AccessToken(
                    it.decryptedAccessToken,
                    it.expiresOn
                )
            )
        } ?: {
            Napier.i { "idpTokenResult null, access token not saved" }
            throw DecryptAccessTokenError
        }
    }

    private suspend fun saveDefaultSsoToken(
        authenticationId: String?,
        authenticationName: String?,
        redirectSsoToken: String?,
        profileId: String
    ) {
        letNotNull(
            authenticationId,
            authenticationName,
            redirectSsoToken
        ) { id, name, sso ->
            idpRepository.saveSingleSignOnToken(
                profileId,
                IdpData.ExternalAuthenticationToken(
                    token = IdpData.SingleSignOnToken(sso),
                    authenticatorId = id,
                    authenticatorName = name
                )
            )
        } ?: {
            Napier.i { "authId, authName and sso not obtained, so not saved" }
            throw SingleSignOnTokenError
        }
    }

    private fun saveBiometricSsoToken(
        redirectSsoToken: String?,
        profileId: String
    ) {
        redirectSsoToken?.let { sso ->
            pairingRepository.saveSingleSignOnToken(
                profileId,
                IdpData.SingleSignOnToken(sso)
            )
        } ?: {
            Napier.i { "sso token not obtained so not saving" }
            throw SingleSignOnTokenError
        }
    }
}
