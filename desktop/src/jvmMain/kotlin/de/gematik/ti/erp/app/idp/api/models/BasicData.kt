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

@file:UseSerializers(JWSSerializer::class)

package de.gematik.ti.erp.app.idp.api.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.UseSerializers
import org.jose4j.jwk.JsonWebKey
import org.jose4j.jwk.PublicJsonWebKey
import org.jose4j.jws.JsonWebSignature

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
    @SerialName("iat") val issuedAt: Long
)

@JvmInline
value class JWSPublicKey(val jws: PublicJsonWebKey)

@JvmInline
value class JWSKey(val jws: JsonWebKey)

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
