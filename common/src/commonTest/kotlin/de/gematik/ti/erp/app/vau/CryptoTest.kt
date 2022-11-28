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

package de.gematik.ti.erp.app.vau

import okio.ByteString.Companion.decodeHex
import org.bouncycastle.jce.ECNamedCurveTable
import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import kotlin.test.Test
import java.security.KeyFactory
import java.security.KeyPair
import java.security.KeyPairGenerator
import java.security.interfaces.ECPublicKey
import java.security.spec.ECGenParameterSpec
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.spec.IvParameterSpec

class CryptoTest {

    @Test
    fun `alice sends bob a ECIES message`() {
        val bobKeyPair = KeyPairGenerator.getInstance("EC", BCProvider)
            .apply { initialize(ECGenParameterSpec("brainpoolP256r1")) }
            .generateKeyPair()

        val ciphertext = Ecies.encrypt(
            bobKeyPair.public as ECPublicKey,
            VauEciesSpec.V1,
            "Hallo Test".toByteArray(),
            TestCryptoConfig
        )

        Ecies.decrypt(bobKeyPair, VauEciesSpec.V1, ciphertext, TestCryptoConfig).let {
            assertEquals("Hallo Test", it.decodeToString())
        }
    }

    @Test
    fun `alice sends bob a AESGCM message`() {
        val symKey = KeyGenerator.getInstance("AES", BCProvider).apply {
            init(128)
        }.generateKey()

        val ciphertext = AesGcm.encrypt(symKey, VauAesGcmSpec.V1, "Hallo Test".toByteArray(), TestCryptoConfig)

        AesGcm.decrypt(symKey, VauAesGcmSpec.V1, ciphertext, TestCryptoConfig).let {
            assertEquals("Hallo Test", it.decodeToString())
        }
    }

    @Test
    fun `test according specification`() {
        val curveSpec = ECNamedCurveTable.getParameterSpec("brainpoolP256r1")

        val certPubKey = org.bouncycastle.jce.spec.ECPublicKeySpec(
            curveSpec.curve.createPoint(
                TestCrypto.CertPublicKeyX.toBigInteger(16),
                TestCrypto.CertPublicKeyY.toBigInteger(16)
            ),
            curveSpec
        ).let { pubKeySpec ->
            KeyFactory.getInstance("EC", BCProvider).generatePublic(pubKeySpec) as ECPublicKey
        }

        val epKeyPair = Pair(
            org.bouncycastle.jce.spec.ECPublicKeySpec(
                curveSpec.curve.createPoint(
                    TestCrypto.EphemeralPublicKeyX.toBigInteger(16),
                    TestCrypto.EphemeralPublicKeyY.toBigInteger(16)
                ),
                curveSpec
            ),
            org.bouncycastle.jce.spec.ECPrivateKeySpec(
                TestCrypto.EccPrivateKey.toBigInteger(16),
                curveSpec
            )
        )
            .let { keySpec ->
                KeyPair(
                    KeyFactory.getInstance("EC", BCProvider).generatePublic(keySpec.first),
                    KeyFactory.getInstance("EC", BCProvider).generatePrivate(keySpec.second)
                )
            }

        val ivSpec = IvParameterSpec(TestCrypto.IVBytes.decodeHex().toByteArray())
        val cipher =
            Ecies.generateCipher(
                ivSpec,
                epKeyPair,
                certPubKey,
                VauEciesSpec.V1,
                Cipher.ENCRYPT_MODE,
                TestCryptoConfig
            )

        val cipherText = Ecies.encrypt(
            VauEciesSpec.V1,
            TestCrypto.Message.toByteArray(),
            ivSpec,
            epKeyPair.public as ECPublicKey,
            cipher
        )

        assertArrayEquals(TestCrypto.CipherText.decodeHex().toByteArray(), cipherText)
    }
}
