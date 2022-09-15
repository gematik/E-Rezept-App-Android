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

package de.gematik.ti.erp.app.idp.repository

import de.gematik.ti.erp.app.api.ApiCallException
import de.gematik.ti.erp.app.api.Result
import de.gematik.ti.erp.app.api.safeApiCall
import de.gematik.ti.erp.app.api.safeApiCallRaw
import de.gematik.ti.erp.app.idp.api.IdpService
import de.gematik.ti.erp.app.idp.api.REDIRECT_URI
import de.gematik.ti.erp.app.idp.usecase.IdpUseCase
import okhttp3.ResponseBody
import retrofit2.Response
import java.net.HttpURLConnection
import javax.inject.Inject

private const val defaultScope = "e-rezept openid"
private const val pairingScope = "pairing openid"

class IdpRemoteDataSource @Inject constructor(
    private val service: IdpService
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

    suspend fun requestAuthorizationRedirect(
        url: String,
        externalAppId: String,
        nonce: String,
        state: String,
        codeChallenge: String,
    ) = postToEndpointExpectingLocationRedirect {
        service.requestAuthenticationRedirect(
            url,
            externalAppId = externalAppId,
            codeChallenge = codeChallenge,
            nonce = nonce,
            state = state
        )
    }

    suspend fun fetchChallenge(
        url: String,
        codeChallenge: String,
        state: String,
        nonce: String,
        isDeviceRegistration: Boolean
    ) =
        safeApiCall("error loading challenge") {
            service.fetchTokenChallenge(
                url,
                codeChallenge = codeChallenge,
                state = state,
                nonce = nonce,
                scope = if (isDeviceRegistration) pairingScope else defaultScope
            )
        }

    suspend fun postPairing(url: String, token: String, encryptedRegistrationData: String) =
        safeApiCall("failed to pair device") {
            service.pairing(url, "Bearer $token", encryptedRegistrationData)
        }

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
    suspend fun authorizeExtern(
        url: String,
        externalAuthorizationData: IdpUseCase.ExternalAuthorizationData
    ) = postToEndpointExpectingLocationRedirect {
        service.externalAuthorization(
            url = url,
            code = externalAuthorizationData.code,
            state = externalAuthorizationData.state,
            kk_app_redirect_uri = externalAuthorizationData.kkAppRedirectUri
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

    private suspend inline fun postToEndpointExpectingLocationRedirect(crossinline call: suspend () -> Response<ResponseBody>) =
        safeApiCallRaw("error posting to redirecting endpoint") {
            val response = call()
            if (response.code() == HttpURLConnection.HTTP_MOVED_TEMP) {
                val headers = response.headers()
                val location = requireNotNull(headers["Location"])

                Result.Success(location)
            } else {
                Result.Error(
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
        redirectUri: String = REDIRECT_URI
    ) = safeApiCall("error posting for token") {
        service.token(
            url = url,
            keyVerifier = keyVerifier,
            code = code,
            redirectUri = redirectUri
        )
    }
}
