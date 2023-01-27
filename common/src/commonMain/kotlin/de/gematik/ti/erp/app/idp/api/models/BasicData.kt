/*
 * Copyright (c) 2023 gematik GmbH
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

@file:UseSerializers(JWSSerializer::class)

package de.gematik.ti.erp.app.idp.api.models

import de.gematik.ti.erp.app.idp.api.IdpService
import de.gematik.ti.erp.app.idp.model.IdpData
import de.gematik.ti.erp.app.secureRandomInstance
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.UseSerializers
import org.jose4j.base64url.Base64Url
import org.jose4j.jwk.JsonWebKey
import org.jose4j.jwk.PublicJsonWebKey
import org.jose4j.jws.JsonWebSignature
import java.net.URI

@Serializable
data class IdpDiscoveryInfo(
    @SerialName("authorization_endpoint") val authorizationURL: String,
    @SerialName("sso_endpoint") val ssoURL: String,
    @SerialName("token_endpoint") val tokenURL: String,
    @SerialName("uri_pair") val pairingURL: String,
    @SerialName("auth_pair_endpoint") val authenticationURL: String,
    @SerialName("uri_puk_idp_enc") val uriPukIdpEnc: String,
    @SerialName("uri_puk_idp_sig") val uriPukIdpSig: String,
    @SerialName("exp") val expirationTime: Long,
    @SerialName("iat") val issuedAt: Long,
    @SerialName("kk_app_list_uri") val krankenkassenAppURL: String? = null,
    @SerialName("third_party_authorization_endpoint") val thirdPartyAuthorizationURL: String? = null
)

@Serializable
data class AuthenticationId(
    @SerialName("kk_app_name") val name: String,
    @SerialName("kk_app_id") val id: String
)

@Serializable
data class AuthenticationIdList(
    @SerialName("kk_app_list") val authenticationList: List<AuthenticationId>
)

@Serializable
data class AuthorizationRedirectInfo(
    @SerialName("client_id") val clientId: String,
    @SerialName("state") val state: String,
    @SerialName("redirect_uri") val redirectUri: String,
    @SerialName("code_challenge") val codeChallenge: String,
    @SerialName("code_challenge_method") val codeChallengeMethod: String,
    @SerialName("response_type") val responseType: String,
    @SerialName("nonce") val nonce: String,
    @SerialName("scope") val scope: String
)

// TODO https://youtrack.jetbrains.com/issue/KT-50649 conflicts with result class of Kotlin
// @JvmInline
// value class JWSPublicKey(val jws: PublicJsonWebKey)
//
// @JvmInline
// value class JWSKey(val jws: JsonWebKey)

class JWSPublicKey(val jws: PublicJsonWebKey)

class JWSKey(val jws: JsonWebKey)

data class JWSChallenge(val jws: JsonWebSignature, val raw: String)

@Serializable
data class Challenge(
    val challenge: JWSChallenge
)

@Serializable
data class TokenResponse(
    @SerialName("access_token") val accessToken: String,
    @SerialName("expires_in") val expiresIn: Long,
    @SerialName("id_token") val idToken: String,
    @SerialName("sso_token") val ssoToken: String? = null,
    @SerialName("token_type") val tokenType: String
)

enum class IdpScope {
    Default,
    BiometricPairing
}

data class IdpChallengeFlowResult(
    val scope: IdpScope,
    val challenge: IdpUnsignedChallenge
)

data class IdpAuthFlowResult(
    val accessToken: String,
    val ssoToken: String,
    val idTokenInsurantName: String,
    val idTokenInsuranceIdentifier: String,
    val idTokenInsuranceName: String
)

data class IdpRefreshFlowResult(
    val scope: IdpScope,
    val accessToken: String
)

data class IdpInitialData(
    val config: IdpData.IdpConfiguration,
    val pukSigKey: JWSPublicKey,
    val pukEncKey: JWSPublicKey,
    val state: IdpState,
    val nonce: IdpNonce,
    val codeVerifier: String,
    val codeChallenge: String
)

data class IdpUnsignedChallenge(
    val signedChallenge: String, // raw jws
    val challenge: String, // payload extracted from the jws
    val expires: Long // expiry timestamp parsed from challenge
)

data class IdpTokenResult(
    val decryptedAccessToken: String,
    val idTokenPayload: String
)

@JvmInline
value class IdpState(val state: String) {
    operator fun component1(): String = state

    companion object {
        fun create(outLength: Int = 32) = IdpState(generateRandomUrlSafeStringSecure(outLength))
    }
}

@JvmInline
value class IdpNonce(val nonce: String) {
    operator fun component1(): String = nonce

    companion object {
        fun create() = IdpNonce(
            generateRandomUrlSafeStringSecure(32)
        )
    }
}

internal fun generateRandomUrlSafeStringSecure(outLength: Int = 32): String {
    require(outLength >= 1)
    val chars = Base64Url.encode(
        ByteArray((outLength / 4 + 1) * 3).apply {
            secureRandomInstance().nextBytes(this)
        }
    )
    return chars.substring(0 until outLength)
}
class ExternalAuthorizationData(uri: URI) {
    val code = IdpService.extractQueryParameter(uri, "code")
    val state = IdpService.extractQueryParameter(uri, "state")
    val kkAppRedirectUri = IdpService.extractQueryParameter(uri, "kk_app_redirect_uri")
}
