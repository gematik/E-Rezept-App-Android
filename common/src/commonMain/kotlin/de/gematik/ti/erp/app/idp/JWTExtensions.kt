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

package de.gematik.ti.erp.app.idp

import de.gematik.ti.erp.app.Requirement
import org.jose4j.base64url.Base64Url
import org.jose4j.json.internal.json_simple.JSONObject
import org.jose4j.jwe.ContentEncryptionAlgorithmIdentifiers
import org.jose4j.jwe.JsonWebEncryption
import org.jose4j.jwe.KeyManagementAlgorithmIdentifiers
import org.jose4j.jws.EcdsaUsingShaAlgorithm
import org.jose4j.jws.JsonWebSignature
import java.security.MessageDigest
import java.security.PrivateKey
import java.security.PublicKey
import java.security.Signature
import javax.crypto.SecretKey

@Requirement(
    "A_21324",
    sourceSpecification = "gemSpec_eRp_FdV",
    rationale = "Generate Key-Verifier using Token-Key and Code-Verifier."
)
fun buildKeyVerifier(
    tokenKey: SecretKey,
    codeVerifier: String,
    publicEncKey: PublicKey
): JsonWebEncryption {
    val payload = JSONObject().apply {
        put("token_key", Base64Url.encode(tokenKey.encoded))
        put("code_verifier", codeVerifier)
    }.toString()

    return JsonWebEncryption().apply {
        setHeader("cty", "JSON")
        algorithmHeaderValue = KeyManagementAlgorithmIdentifiers.ECDH_ES
        encryptionMethodHeaderParameter = ContentEncryptionAlgorithmIdentifiers.AES_256_GCM
        key = publicEncKey
        this.payload = payload
    }
}

private class JsonWebSignatureWithHealthCard : JsonWebSignature() {
    fun encodedHeaderAndPayload(): String =
        "$encodedHeader.$encodedPayload"
}

suspend fun buildJsonWebSignatureWithHealthCard(
    builder: JsonWebSignature.() -> Unit,
    sign: suspend (hash: ByteArray) -> ByteArray
): String {
    val jwsWithHealthCard = JsonWebSignatureWithHealthCard()
    builder(jwsWithHealthCard)

    val headerAndPayload = jwsWithHealthCard.encodedHeaderAndPayload()

    jwsWithHealthCard.algorithmHeaderValue.let {
        require(it.startsWith("BP"))
        require(it.endsWith("R1"))
    }

    val hashed = MessageDigest.getInstance("SHA-256").digest(headerAndPayload.toByteArray(Charsets.UTF_8))
    val signed = sign(hashed)

    return "$headerAndPayload.${Base64Url().base64UrlEncode(signed)}"
}

@Requirement(
    "O.Cryp_1#5",
    "O.Cryp_4#5",
    sourceSpecification = "BSI-eRp-ePA",
    rationale = "Signature via ecdh ephemeral-static (one time usage)"
)
fun buildJsonWebSignatureWithSecureElement(
    builder: JsonWebSignature.() -> Unit,
    privateKey: PrivateKey,
    signature: Signature
): String {
    val jwsWithHealthCard = JsonWebSignatureWithHealthCard()
    builder(jwsWithHealthCard)

    val headerAndPayload = jwsWithHealthCard.encodedHeaderAndPayload()

    val signed = signature.apply {
        initSign(privateKey)
        update(headerAndPayload.toByteArray(Charsets.UTF_8))
    }.sign()

    val concatenatedSigned = EcdsaUsingShaAlgorithm.convertDerToConcatenated(signed, 64)

    return "$headerAndPayload.${Base64Url().base64UrlEncode(concatenatedSigned)}"
}
