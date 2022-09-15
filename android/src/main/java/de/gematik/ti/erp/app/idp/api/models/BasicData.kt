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

package de.gematik.ti.erp.app.idp.api.models

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import org.jose4j.jwk.JsonWebKey
import org.jose4j.jwk.PublicJsonWebKey
import org.jose4j.jws.JsonWebSignature

@JsonClass(generateAdapter = true)
data class IdpDiscoveryInfo(
    @Json(name = "authorization_endpoint") val authorizationURL: String,
    @Json(name = "sso_endpoint") val ssoURL: String,
    @Json(name = "token_endpoint") val tokenURL: String,
    @Json(name = "uri_pair") val pairingURL: String,
    @Json(name = "auth_pair_endpoint") val authenticationURL: String,
    @Json(name = "uri_puk_idp_enc") val uriPukIdpEnc: String,
    @Json(name = "uri_puk_idp_sig") val uriPukIdpSig: String,
    @Json(name = "exp") val expirationTime: Long,
    @Json(name = "iat") val issuedAt: Long,
    @Json(name = "kk_app_list_uri") val krankenkassenAppURL: String? = null,
    @Json(name = "third_party_authorization_endpoint") val thirdPartyAuthorizationURL: String? = null
)

@JsonClass(generateAdapter = true)
data class AuthenticationID(
    @Json(name = "kk_app_name") val name: String,
    @Json(name = "kk_app_id") val authenticationID: String
)
@JsonClass(generateAdapter = true)
data class AuthenticationIDList(
    @Json(name = "kk_app_list") val authenticationIDList: List<AuthenticationID>,
)

@JsonClass(generateAdapter = true)
data class AuthorizationRedirectInfo(
    @Json(name = "client_id") val clientId: String,
    @Json(name = "state") val state: String,
    @Json(name = "redirect_uri") val redirectUri: String,
    @Json(name = "code_challenge") val codeChallenge: String,
    @Json(name = "code_challenge_method") val codeChallengeMethod: String,
    @Json(name = "response_type")val responseType: String,
    @Json(name = "nonce")val nonce: String,
    @Json(name = "scope")val scope: String
)

@JvmInline
value class JWSPublicKey(val jws: PublicJsonWebKey)

@JvmInline
value class JWSKey(val jws: JsonWebKey)

data class JWSChallenge(val jws: JsonWebSignature, val raw: String)

@JsonClass(generateAdapter = true)
data class Challenge(
    val challenge: JWSChallenge
)

@JsonClass(generateAdapter = true)
data class TokenResponse(
    @Json(name = "access_token") val accessToken: String,
    @Json(name = "expires_in") val expiresIn: Long,
    @Json(name = "id_token") val idToken: String,
    @Json(name = "sso_token") val ssoToken: String?,
    @Json(name = "token_type") val tokenType: String
)
