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

import de.gematik.ti.erp.app.core.ApiCallException
import de.gematik.ti.erp.app.core.safeApiCall
import de.gematik.ti.erp.app.core.safeApiCallRaw
import de.gematik.ti.erp.app.idp.api.IdpService
import okhttp3.ResponseBody
import retrofit2.Response
import java.net.HttpURLConnection

private const val defaultScope = "e-rezept openid"
private const val pairingScope = "pairing openid"

class IdpRemoteDataSource(
    private val service: IdpService
) {

    suspend fun fetchDiscoveryDocument() =
        safeApiCall("error loading discovery document") { service.discoveryDocument() }

    suspend fun fetchIdpPukSig(url: String) =
        safeApiCall("error fetching idpPucSig") { service.idpPukSig(url) }

    suspend fun fetchIdpPukEnc(url: String) =
        safeApiCall("error fetching idpPucEnc") { service.idpPukEnc(url) }

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

    suspend fun postChallenge(url: String, signedChallenge: String) =
        postToEndpointExpectingLocationRedirect { service.authorization(url, signedChallenge) }

    suspend fun postChallenge(url: String, ssoToken: String, unsignedChallenge: String) =
        postToEndpointExpectingLocationRedirect { service.ssoToken(url, ssoToken, unsignedChallenge) }

    private suspend inline fun postToEndpointExpectingLocationRedirect(crossinline call: suspend () -> Response<ResponseBody>) =
        safeApiCallRaw("error posting to redirecting endpoint") {
            val response = call()
            if (response.code() == HttpURLConnection.HTTP_MOVED_TEMP) {
                val headers = response.headers()
                val location = requireNotNull(headers["Location"])

                Result.success(location)
            } else {
                Result.failure(
                    ApiCallException("Expected redirect ${response.code()} ${response.message()}", response)
                )
            }
        }

    suspend fun postToken(
        url: String,
        keyVerifier: String,
        code: String,
    ) = safeApiCall("error posting for token") {
        service.token(
            url = url,
            keyVerifier = keyVerifier,
            code = code
        )
    }
}
