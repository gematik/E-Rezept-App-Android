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

@file:Suppress("TopLevelPropertyNaming")

package de.gematik.ti.erp.app.idp.repository

import de.gematik.ti.erp.app.api.ApiCallException
import de.gematik.ti.erp.app.api.safeApiCall
import de.gematik.ti.erp.app.api.safeApiCallRaw
import de.gematik.ti.erp.app.idp.api.IdpService
import okhttp3.ResponseBody
import retrofit2.Response
import java.net.HttpURLConnection

private const val pairingScope = "pairing openid"

@Suppress("TooManyFunctions")
class IdpRemoteDataSource(
    private val service: IdpService,
    private val defaultScope: () -> String
) {

    suspend fun fetchDiscoveryDocument() =
        safeApiCall("error loading discovery document") { service.discoveryDocument() }

    suspend fun fetchIdpPukSig(url: String) =
        safeApiCall("error fetching idpPucSig") { service.idpPukSig(url) }

    suspend fun fetchIdpPukEnc(url: String) =
        safeApiCall("error fetching idpPucEnc") { service.idpPukEnc(url) }

    suspend fun fetchExternalAuthorizationIDList(url: String) =
        safeApiCall("error fetching external authentication ID List from $url") {
            service.externalAuthenticationIDList(
                url
            )
        }

    suspend fun getGidAuthorizationRedirectUrl(
        url: String,
        externalAppId: String,
        nonce: String,
        state: String,
        codeChallenge: String,
        isPairingScope: Boolean
    ) = service.requestGidAuthenticationRedirect(
        url = url,
        externalAppId = externalAppId,
        codeChallenge = codeChallenge,
        nonce = nonce,
        state = state,
        scope = if (isPairingScope) pairingScope else defaultScope()
    )

    suspend fun fetchChallenge(
        url: String,
        codeChallenge: String,
        state: String,
        nonce: String,
        isDeviceRegistration: Boolean,
        redirectUri: String
    ) =
        safeApiCall("error loading challenge") {
            service.fetchTokenChallenge(
                url = url,
                codeChallenge = codeChallenge,
                state = state,
                nonce = nonce,
                scope = if (isDeviceRegistration) pairingScope else defaultScope(),
                redirectUri = redirectUri
            )
        }

    suspend fun postPairing(url: String, token: String, encryptedRegistrationData: String) =
        safeApiCall("failed to pair device") {
            service.postPairing(url, "Bearer $token", encryptedRegistrationData)
        }

    suspend fun getPairing(url: String, token: String) =
        safeApiCall("failed to get paired devices") {
            service.getPairing(url, "Bearer $token")
        }

    // tag::DeletePairedDevicesRepository[]
    suspend fun deletePairing(url: String, token: String, alias: String) =
        safeApiCallRaw("failed to delete paired device") {
            val response = service.deletePairing("$url/$alias", "Bearer $token")
            if (response.code() == HttpURLConnection.HTTP_NO_CONTENT) {
                Result.success(Unit)
            } else {
                Result.failure(
                    ApiCallException(
                        "Expected no content but received: ${response.code()} ${response.message()}",
                        response
                    )
                )
            }
        }
    // end::DeletePairedDevicesRepository[]

    /**
     * Authorization with Card
     */
    suspend fun postChallenge(url: String, signedChallenge: String) =
        postToEndpointExpectingLocationRedirect { service.authorization(url, signedChallenge) }

    suspend fun postChallenge(url: String, ssoToken: String, unsignedChallenge: String) =
        postToEndpointExpectingLocationRedirect {
            service.ssoToken(
                url,
                ssoToken,
                unsignedChallenge
            )
        }

    /**
     * Authorization with External App
     */
    suspend fun authorizeExternalAppDataWithFastTrack(
        url: String,
        code: String,
        state: String,
        redirectUri: String
    ) = postToEndpointExpectingLocationRedirect {
        service.externalFastTrackAuthorization(
            url = url,
            code = code,
            state = state,
            redirectUri = redirectUri
        )
    }

    suspend fun authorizeExternalAppDataWithGid(
        url: String,
        code: String,
        state: String
    ) = postToEndpointExpectingLocationRedirect {
        service.externalGidAuthorization(
            url = url,
            code = code,
            state = state
        )
    }

    /**
     * Authorization with Biometrics
     */
    suspend fun authorizeBiometric(url: String, encryptedSignedAuthenticationData: String) =
        postToEndpointExpectingLocationRedirect {
            service.authenticate(
                url,
                encryptedSignedAuthenticationData
            )
        }

    private suspend inline fun postToEndpointExpectingLocationRedirect(
        crossinline call: suspend () -> Response<ResponseBody>
    ) =
        safeApiCallRaw("error posting to redirecting endpoint") {
            val response = call()
            if (response.code() == HttpURLConnection.HTTP_MOVED_TEMP) {
                val headers = response.headers()
                val location = requireNotNull(headers["Location"])

                Result.success(location)
            } else {
                Result.failure(
                    ApiCallException(
                        "Expected redirect ${response.code()} ${response.message()}",
                        response
                    )
                )
            }
        }

    suspend fun postToken(
        url: String,
        keyVerifier: String,
        code: String,
        redirectUri: String
    ) = safeApiCall("error posting for token") {
        service.token(
            url = url,
            keyVerifier = keyVerifier,
            code = code,
            redirectUri = redirectUri
        )
    }
}
