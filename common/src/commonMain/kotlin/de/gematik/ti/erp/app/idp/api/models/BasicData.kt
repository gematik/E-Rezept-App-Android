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

@file:UseSerializers(JWSSerializer::class)
@file:Suppress("MagicNumber")

package de.gematik.ti.erp.app.idp.api.models

import de.gematik.ti.erp.app.Requirement
import de.gematik.ti.erp.app.idp.model.IdpData
import de.gematik.ti.erp.app.secureRandomInstance
import kotlinx.datetime.Instant
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.UseSerializers
import org.jose4j.base64url.Base64Url
import org.jose4j.jwk.JsonWebKey
import org.jose4j.jwk.PublicJsonWebKey
import org.jose4j.jws.JsonWebSignature

@Serializable
data class IdpDiscoveryInfo(
    @SerialName("authorization_endpoint") val authorizationUrl: String,
    @SerialName("sso_endpoint") val ssoUrl: String,
    @SerialName("token_endpoint") val tokenUrl: String,
    @SerialName("uri_pair") val pairingUrl: String,
    @SerialName("auth_pair_endpoint") val authenticationUrl: String,
    @SerialName("uri_puk_idp_enc") val uriPukIdpEnc: String,
    @SerialName("uri_puk_idp_sig") val uriPukIdpSig: String,
    @SerialName("exp") val expirationTime: Long,
    @SerialName("iat") val issuedAt: Long,
    @SerialName("kk_app_list_uri") val healthInsuranceAppV1Url: String? = null,
    @SerialName("fed_idp_list_uri") val healthInsuranceAppV2Url: String? = null,
    @SerialName("third_party_authorization_endpoint") val thirdPartyAuthorizationV1Url: String? = null,
    @SerialName("federation_authorization_endpoint") val thirdPartyAuthorizationV2Url: String? = null
)

@Serializable
data class RemoteFastTrackIdp(
    @SerialName("kk_app_name") val name: String,
    @SerialName("kk_app_id") val id: String
)

@Requirement(
    "A_22302-01#1",
    sourceSpecification = "gemSpec_IDP_Frontend",
    rationale = "Data class for Forwarding the Authorization Code to the IDP service."
)
@Serializable
data class RemoteFederationIdp(
    @SerialName("idp_name") val name: String,
    @SerialName("idp_iss") val id: String,
    @SerialName("idp_sek_2") val isGid: Boolean,
    @SerialName("idp_logo") val logo: String?
)

@Serializable
data class RemoteFederationIdps(
    @SerialName("fed_idp_list") val items: List<RemoteFederationIdp>
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
    val expiresOn: Instant,
    val ssoToken: String,
    val idTokenInsurantName: String,
    val idTokenInsuranceIdentifier: String,
    val idTokenInsuranceName: String
)

data class IdpRefreshFlowResult(
    val scope: IdpScope,
    val accessToken: String,
    val expiresOn: Instant
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
    val expiresOn: Instant,
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
