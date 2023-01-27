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

package de.gematik.ti.erp.app.idp

import de.gematik.ti.erp.app.BCProvider
import de.gematik.ti.erp.app.generateRandomAES256Key
import kotlinx.coroutines.runBlocking
import org.bouncycastle.jce.provider.BouncyCastleProvider
import org.jose4j.jwe.JsonWebEncryption
import org.jose4j.jws.EcdsaUsingShaAlgorithm
import org.jose4j.jws.JsonWebSignature
import org.jose4j.jwx.JsonWebStructure
import org.json.JSONObject
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.security.KeyPairGenerator
import java.security.Security
import java.security.Signature
import java.security.interfaces.ECPublicKey
import java.security.spec.ECGenParameterSpec

class JWTExtensionsTest {
    init {
        Security.insertProviderAt(BCProvider, 1)
    }

    @Test
    fun `buildKeyVerifier - check structure`() {
        val keyPair = KeyPairGenerator.getInstance("EC", BCProvider).apply {
            initialize(ECGenParameterSpec("secp256r1"))
        }.genKeyPair()
        val secretKey = generateRandomAES256Key()

        val keyVerifierJWEString = buildKeyVerifier(
            tokenKey = secretKey,
            codeVerifier = "123456",
            publicEncKey = keyPair.public as ECPublicKey
        ).compactSerialization

        val keyVerifierJWE = JsonWebStructure.fromCompactSerialization(keyVerifierJWEString) as JsonWebEncryption

        keyVerifierJWE.headers!!.let {
            assertEquals("JSON", it.getStringHeaderValue("cty"))
            assertEquals("ECDH-ES", it.getStringHeaderValue("alg"))
            assertEquals("A256GCM", it.getStringHeaderValue("enc"))
        }

        keyVerifierJWE.key = keyPair.private

        assertTrue("""^[A-Za-z0-9_-]+$""".toRegex().matches(JSONObject(keyVerifierJWE.payload)["token_key"] as String))
    }

    @Test
    fun `test buildJsonWebSignatureWithHealthCard`() {
        EllipticCurvesExtending.init()

        val keyPair = KeyPairGenerator.getInstance("EC", BCProvider).apply {
            initialize(ECGenParameterSpec("brainpoolP256R1"))
        }.genKeyPair()

        val jwsString = runBlocking {
            buildJsonWebSignatureWithHealthCard(
                {
                    algorithmHeaderValue = "BP256R1"
                    payload = """{}"""
                },
                {
                    val signed = Signature.getInstance("NoneWithECDSA").apply {
                        initSign(keyPair.private)
                        update(it)
                    }.sign()
                    EcdsaUsingShaAlgorithm.convertDerToConcatenated(signed, 64)
                }
            )
        }

        val jws = JsonWebStructure.fromCompactSerialization(jwsString) as JsonWebSignature
        jws.key = keyPair.public
        assertEquals("""{}""", jws.payload)
    }

    @Test
    fun `test buildJsonWebSignatureWithSecureElement`() {
        EllipticCurvesExtending.init()

        val keyPair = KeyPairGenerator.getInstance("EC", BouncyCastleProvider()).apply {
            initialize(ECGenParameterSpec("brainpoolP256R1"))
        }.genKeyPair()

        val jwsString = buildJsonWebSignatureWithSecureElement(
            {
                algorithmHeaderValue = "BP256R1"
                payload = """{}"""
            },
            privateKey = keyPair.private,
            signature = Signature.getInstance("SHA256WithECDSA")
        )

        val jws = JsonWebStructure.fromCompactSerialization(jwsString) as JsonWebSignature
        jws.key = keyPair.public
        assertEquals("""{}""", jws.payload)
    }
}
