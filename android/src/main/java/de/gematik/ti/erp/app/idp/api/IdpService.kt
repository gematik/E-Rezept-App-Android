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

package de.gematik.ti.erp.app.idp.api

import de.gematik.ti.erp.app.idp.api.models.Challenge
import de.gematik.ti.erp.app.idp.api.models.JWSPublicKey
import de.gematik.ti.erp.app.idp.api.models.PairingResponseEntries
import de.gematik.ti.erp.app.idp.api.models.PairingResponseEntry
import de.gematik.ti.erp.app.idp.api.models.TokenResponse
import de.gematik.ti.erp.app.idp.repository.JWSDiscoveryDocument
import java.net.URI
import okhttp3.ResponseBody
import org.jose4j.jws.JsonWebSignature
import retrofit2.Response
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Headers
import retrofit2.http.POST
import retrofit2.http.Query
import retrofit2.http.Url

const val REDIRECT_URI = "https://redirect.gematik.de/erezept"
const val CLIENT_ID = "eRezeptApp"
const val EXT_AUTH_REDIRECT_URI: String = "https://das-e-rezept-fuer-deutschland.de/extauth"

interface IdpService {

    @Headers(
        "Accept: application/jwt;charset=UTF-8",
    )
    @GET("openid-configuration")
    suspend fun discoveryDocument(): Response<JWSDiscoveryDocument>

    @GET
    suspend fun idpPukSig(
        @Url url: String
    ): Response<JWSPublicKey>

    @GET
    suspend fun idpPukEnc(
        @Url url: String
    ): Response<JWSPublicKey>

    @GET
    suspend fun externalAuthenticationIDList(
        @Url url: String
    ): Response<JsonWebSignature>

    @GET
    suspend fun requestAuthenticationRedirect(
        @Url url: String,
        @Query("kk_app_id")externalAppId: String,
        @Query("nonce")nonce: String,
        @Query("state")state: String,
        @Query("client_id")clientID: String = "eRezeptApp",
        @Query("redirect_uri")redirectUri: String = EXT_AUTH_REDIRECT_URI,
        @Query("code_challenge_method")codeChallengeMethod: String = "S256",
        @Query("response_type")responseType: String = "code",
        @Query("scope")scope: String = "e-rezept openid",
        @Query("code_challenge")codeChallenge: String
    ): Response<ResponseBody>

    @GET
    suspend fun fetchTokenChallenge(
        @Url url: String,
        @Query("client_id") clientId: String = CLIENT_ID,
        @Query("response_type") responseType: String = "code",
        @Query("redirect_uri") redirect_uri: String = REDIRECT_URI,
        @Query("state") state: String,
        @Query("code_challenge") codeChallenge: String,
        @Query("code_challenge_method") codeChallengeMethod: String = "S256",
        @Query("scope") scope: String,
        @Query("nonce") nonce: String
    ): Response<Challenge>

    @FormUrlEncoded
    @POST
    @Headers(
        "Accept: application/json",
    )
    suspend fun authorization(
        @Url url: String,
        @Field("signed_challenge") signedChallenge: String
    ): Response<ResponseBody>

    @FormUrlEncoded
    @POST
    @Headers(
        "Accept: application/json",
    )
    suspend fun token(
        @Url url: String,
        @Field("grant_type") grantType: String = "authorization_code",
        @Field("redirect_uri") redirectUri: String = REDIRECT_URI,
        @Field("client_id") clientId: String = CLIENT_ID,
        @Field("key_verifier") keyVerifier: String,
        @Field("code") code: String
    ): Response<TokenResponse>

    @FormUrlEncoded
    @POST
    @Headers(
        "Accept: application/json",
    )
    suspend fun ssoToken(
        @Url url: String,
        @Field("ssotoken") ssoToken: String,
        @Field("unsigned_challenge") unsignedChallenge: String,
    ): Response<ResponseBody>

    /**
     * `gemF_Biometrie 4.1.3`
     */

    /**
     * Registration `gemF_Biometrie 4.1.3.1`
     */
    @FormUrlEncoded
    @POST
    @Headers(
        "Accept: application/json",
    )
    suspend fun postPairing(
        @Url url: String,
        @Header("Authorization") bearerToken: String,
        @Field("encrypted_registration_data") data: String,
    ): Response<PairingResponseEntry>

    /**
     * Registration `gemF_Biometrie 4.1.3.3`
     */
    @GET
    @Headers(
        "Accept: application/json",
    )
    suspend fun getPairing(
        @Url url: String,
        @Header("Authorization") bearerToken: String
    ): Response<PairingResponseEntries>

    /**
     * Authentication `gemF_Biometrie 4.1.3.2`
     */
    @FormUrlEncoded
    @POST
    @Headers(
        "Accept: application/json",
    )
    suspend fun authenticate(
        @Url url: String,
        @Field("encrypted_signed_authentication_data") data: String,
    ): Response<ResponseBody>

    /**
     * Authorization External App
     */
    @FormUrlEncoded
    @POST
    suspend fun externalAuthorization(
        @Url url: String,
        @Field("code")code: String,
        @Field("state")state: String,
        @Field("kk_app_redirect_uri")kk_app_redirect_uri: String
    ): Response<ResponseBody>

    companion object {
        fun extractQueryParameter(location: URI, key: String): String {
            return location.query
                .split("&")
                .map {
                    val (k, v) = it.split("=", limit = 2)
                    Pair(k, v)
                }
                .find { it.first == key }?.second ?: error("no parameter for key: $key")
        }
    }
}
