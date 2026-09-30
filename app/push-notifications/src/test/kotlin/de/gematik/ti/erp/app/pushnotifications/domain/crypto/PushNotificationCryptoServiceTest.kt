/*
 * Copyright (Change Date see Readme), gematik GmbH
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
 * In case of changes by gematik GmbH find details in the "Readme" file.
 *
 * See the Licence for the specific language governing permissions and limitations under the Licence.
 *
 * *******
 *
 * For additional notes and disclaimer from gematik and in case of changes by gematik find details in the "Readme" file.
 */

package de.gematik.ti.erp.app.pushnotifications.domain.crypto

import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import java.security.SecureRandom
import javax.crypto.AEADBadTagException
import javax.crypto.Cipher
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.SecretKeySpec

class PushNotificationCryptoServiceTest {

    private lateinit var keyRotationService: PushNotificationKeyRotationService
    private lateinit var cryptoService: PushNotificationCryptoService

    companion object {
        // Known test vector from spec example A_27176.
        const val TEST_ISS = "f2ca1bb6c7e907d06dafe4687e579fce76b37e4e93b7605022da52e6ccc26fd2"

        // time_iss_created = "the first period the key is generated for"
        // HKDF(ISS, info="2023-10") → first generation for October 2023.
        const val TEST_TIME_ISS_CREATED = "2023-10"

        const val TEST_KEY_IDENTIFIER = "test-key-id"

        // Generation 1: HKDF(ISS, info="2023-10", L=64)
        const val EXPECTED_SECRET_OCT_2023 =
            "185fed66ea5cabbe00147bbd298b5dab0ed41b57ab254d35897b3a4504306e3b"
        const val EXPECTED_KEY_OCT_2023 =
            "3b4adcd58dea98db8e9cb0f5763fcd04fe932d67926cc04b20ba2a2f304ffff9"

        // Generation 2: HKDF(shared-secret-2023-10, info="2023-11", L=64)
        const val EXPECTED_SECRET_NOV_2023 =
            "0c8662d90b04818afb317406fe7fcfcf8d103cd9bc6ad7847890d28620e85ec3"
        const val EXPECTED_KEY_NOV_2023 =
            "39aa5dacd538f53f4b956d84c9b8f2e26933274d160b9fd1a263a27681c6331b"

        val PNM1_PREFIX = "PNM1".toByteArray(Charsets.UTF_8)
        const val PADDED_SIZE = 1024
        const val PREFIX_SIZE = 4
        const val LENGTH_FIELD_SIZE = 2
        const val MAX_TEST_PAYLOAD_SIZE = PADDED_SIZE - PREFIX_SIZE - LENGTH_FIELD_SIZE
        const val GCM_IV_LENGTH = 12
        const val GCM_TAG_BITS = 128
    }

    @Before
    fun setup() {
        keyRotationService = buildKeyRotationService()
        cryptoService = buildCryptoService(keyRotationService)
    }

    private fun buildKeyRotationService(
        iss: String = TEST_ISS,
        timeIssCreated: String = TEST_TIME_ISS_CREATED,
        keyIdentifier: String = TEST_KEY_IDENTIFIER,
        currentMonthProvider: () -> String = { "2024-06" }
    ) = PushNotificationKeyRotationService.create(
        initialSharedSecret = iss,
        timeIssCreated = timeIssCreated,
        keyIdentifier = keyIdentifier,
        hkdf = DefaultHkdfSha256(),
        currentMonthProvider = currentMonthProvider
    )

    private fun buildCryptoService(
        rotation: PushNotificationKeyRotationService,
        currentMonthProvider: () -> String = { "2024-06" }
    ): PushNotificationCryptoService =
        DefaultPushNotificationCryptoService(
            keyChain = SingleChainAdvancer(rotation),
            currentMonthProvider = currentMonthProvider
        )

    /** Adapts a single [PushNotificationKeyRotationService] to the multi-chain [PushKeyChainAdvancer]. */
    private class SingleChainAdvancer(
        private val rotation: PushNotificationKeyRotationService
    ) : PushKeyChainAdvancer {
        override suspend fun knownKeyIdentifiers(): Set<String> = setOf(rotation.keyIdentifier)

        override suspend fun getLatestGeneration(keyIdentifier: String) =
            rotation.getLatestGeneration().takeIf { keyIdentifier == rotation.keyIdentifier }

        override suspend fun advanceToMonth(keyIdentifier: String, targetMonth: String) {
            if (keyIdentifier == rotation.keyIdentifier) rotation.advanceToMonth(targetMonth)
        }

        override suspend fun getGenerationForMonth(keyIdentifier: String, month: String) =
            if (keyIdentifier == rotation.keyIdentifier) rotation.getGenerationForMonth(month) else null
    }

    private suspend fun encryptPayload(
        rotation: PushNotificationKeyRotationService,
        payload: ByteArray
    ): ByteArray {
        val generation = rotation.getLatestGeneration()
            ?: throw PushNotificationCryptoError.NoKeyAvailable()

        val paddingLength = MAX_TEST_PAYLOAD_SIZE - payload.size
        if (paddingLength < 0) throw PushNotificationCryptoError.InvalidPayloadLength()

        val lengthBytes = byteArrayOf(
            (paddingLength shr 8).toByte(),
            (paddingLength and 0xFF).toByte()
        )
        val plaintext = PNM1_PREFIX + lengthBytes + ByteArray(paddingLength) { 0x20 } + payload
        return encryptPlaintext(generation.encryptionKey.hexToByteArray(), plaintext)
    }

    private fun encryptPlaintext(keyBytes: ByteArray, plaintext: ByteArray): ByteArray {
        val secretKey = SecretKeySpec(keyBytes, "AES")
        val iv = ByteArray(GCM_IV_LENGTH).also(SecureRandom()::nextBytes)
        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        cipher.init(Cipher.ENCRYPT_MODE, secretKey, GCMParameterSpec(GCM_TAG_BITS, iv))
        return iv + cipher.doFinal(plaintext)
    }

    @Test
    fun `HKDF output for Oct 2023 matches spec known test vector`() = runTest {
        keyRotationService.addGeneration()
        val gen = keyRotationService.getLatestGeneration()!!

        assertEquals("2023-10", gen.month)
        // first 32 bytes = shared-secret-2023-10
        assertEquals(EXPECTED_SECRET_OCT_2023, gen.secret)
        // last 32 bytes = AES/GCM-Schlüssel-2023-10
        assertEquals(EXPECTED_KEY_OCT_2023, gen.encryptionKey)
        assertEquals(TEST_KEY_IDENTIFIER, gen.keyIdentifier)
    }

    @Test
    fun `HKDF chain output for Nov 2023 matches spec known test vector`() = runTest {
        keyRotationService.addGeneration() // Oct 2023
        keyRotationService.addGeneration() // Nov 2023
        val gen = keyRotationService.getGenerationForMonth("2023-11")!!

        assertEquals("2023-11", gen.month)
        // HKDF(shared-secret-2023-10, info="2023-11")
        assertEquals(EXPECTED_SECRET_NOV_2023, gen.secret)
        assertEquals(EXPECTED_KEY_NOV_2023, gen.encryptionKey)
    }

    @Test
    fun `addGeneration increments month on each call without old key removal`() = runTest {
        keyRotationService.addGeneration() // 2023-10
        keyRotationService.addGeneration() // 2023-11
        keyRotationService.addGeneration() // 2023-12
        keyRotationService.addGeneration() // 2024-01

        val generations = keyRotationService.getGenerations()
        assertEquals(4, generations.size)
        assertEquals("2024-01", generations[0].month)
        assertEquals("2023-12", generations[1].month)
        assertEquals("2023-11", generations[2].month)
        assertEquals("2023-10", generations[3].month)
    }

    @Test
    fun `year rollover works correctly`() = runTest {
        assertEquals("2024-01", PushNotificationKeyRotationService.incrementMonth("2023-12"))
        assertEquals("2023-02", PushNotificationKeyRotationService.incrementMonth("2023-01"))
        assertEquals("2023-10", PushNotificationKeyRotationService.incrementMonth("2023-09"))
    }

    @Test
    fun `decrementMonth works correctly`() = runTest {
        assertEquals("2023-09", PushNotificationKeyRotationService.decrementMonth("2023-10"))
        assertEquals("2022-12", PushNotificationKeyRotationService.decrementMonth("2023-01"))
        assertEquals("2023-11", PushNotificationKeyRotationService.decrementMonth("2023-12"))
    }

    @Test(expected = IllegalArgumentException::class)
    fun `constructor rejects time_iss_created without yyyy-MM format`() = runTest {
        buildKeyRotationService(timeIssCreated = "2023-9")
    }

    @Test(expected = IllegalArgumentException::class)
    fun `incrementMonth rejects invalid calendar month`() = runTest {
        PushNotificationKeyRotationService.incrementMonth("2023-13")
    }

    @Test(expected = IllegalArgumentException::class)
    fun `advanceToMonth rejects target month without yyyy-MM format`() = runTest {
        keyRotationService.advanceToMonth("2024-3")
    }

    @Test
    fun `advanceToMonth derives correct target month`() = runTest {
        keyRotationService.advanceToMonth("2024-03")

        val latest = keyRotationService.getLatestGeneration()
        assertEquals("2024-03", latest!!.month)
    }

    @Test
    fun `advanceToMonth accepts max future month across year rollover`() = runTest {
        val rotation = buildKeyRotationService(currentMonthProvider = { "2023-12" })

        rotation.advanceToMonth("2024-02")

        assertEquals("2024-02", rotation.getLatestGeneration()!!.month)
    }

    @Test
    fun `advanceToMonth limits retained generations to 2`() = runTest {
        keyRotationService.advanceToMonth("2024-03")

        val generations = keyRotationService.getGenerations()
        assertEquals(PushNotificationKeyRotationService.RETAINED_GENERATION_COUNT, generations.size)
        assertEquals("2024-03", generations[0].month)
        assertEquals("2024-02", generations[1].month)
    }

    @Test
    fun `advanceToMonth is idempotent - calling again with same month does nothing`() = runTest {
        keyRotationService.advanceToMonth("2024-03")
        val firstCount = keyRotationService.getGenerations().size

        keyRotationService.advanceToMonth("2024-03")
        assertEquals(firstCount, keyRotationService.getGenerations().size)
    }

    @Test(expected = PushNotificationCryptoError.StaleMonthRejected::class)
    fun `advanceToMonth throws when target month is before current head`() = runTest {
        keyRotationService.advanceToMonth("2024-03")
        keyRotationService.advanceToMonth("2023-11")
    }

    @Test
    fun `getGenerationForMonth returns correct generation within the 2-month limit`() = runTest {
        keyRotationService.advanceToMonth("2024-03")

        val gen = keyRotationService.getGenerationForMonth("2024-03")
        assertNotNull(gen)
        assertEquals("2024-03", gen!!.month)
        assertEquals(TEST_KEY_IDENTIFIER, gen.keyIdentifier)
    }

    @Test
    fun `getGenerationForMonth returns null for month outside the 2-month limit`() = runTest {
        keyRotationService.advanceToMonth("2024-03")

        // "2023-10" is many months back and outside the 2-month limit
        assertNull(keyRotationService.getGenerationForMonth("2023-10"))
    }

    @Test
    fun `decryptForMonth decrypts message from previous month within the 2-month limit`() = runTest {
        // Encrypt with 2024-06
        val encryptRotation = buildKeyRotationService()
        encryptRotation.advanceToMonth("2024-06")
        val payload = "previous month payload".toByteArray(Charsets.UTF_8)
        val encrypted = encryptPayload(encryptRotation, payload)

        // Decrypt service is at 2024-07 → 2-month limit = {2024-07, 2024-06}
        // so 2024-06 is still available without re-advancing.
        val decryptRotation = buildKeyRotationService()
        val decryptCrypto = buildCryptoService(decryptRotation)
        decryptRotation.advanceToMonth("2024-07")

        val decrypted = decryptCrypto.decryptForMonth(encrypted, "2024-06", TEST_KEY_IDENTIFIER)
        assertArrayEquals(payload, decrypted)
    }

    @Test(expected = PushNotificationCryptoError.KeyNotFoundForMonth::class)
    fun `decryptForMonth throws for month outside the 2-month limit`() = runTest {
        // Encrypt with 2024-01
        val encryptRotation = buildKeyRotationService()
        encryptRotation.advanceToMonth("2024-01")
        val payload = "old payload".toByteArray(Charsets.UTF_8)
        val encrypted = encryptPayload(encryptRotation, payload)

        // Advance decrypt service to 2024-06 — 2024-01 is 5 months back, outside the 2-month limit
        val decryptRotation = buildKeyRotationService()
        val decryptCrypto = buildCryptoService(decryptRotation)
        decryptRotation.advanceToMonth("2024-06")

        decryptCrypto.decryptForMonth(encrypted, "2024-01", TEST_KEY_IDENTIFIER)
    }

    @Test(expected = IllegalArgumentException::class)
    fun `decryptForMonth rejects time_message_encrypted without yyyy-MM format`() = runTest {
        cryptoService.decryptForMonth(ByteArray(16), "2024-3", TEST_KEY_IDENTIFIER)
    }

    @Test
    fun `decrypt returns original payload from PNM1 encrypted fixture`() = runTest {
        keyRotationService.addGeneration()

        val payload = """{"ChannelId": "erp.task.activate", "Identifier": "Task.identifier.PrescriptionID"}"""
        val payloadBytes = payload.toByteArray(Charsets.UTF_8)

        val encrypted = encryptPayload(keyRotationService, payloadBytes)
        val decrypted = cryptoService.decryptForMonth(encrypted, "2023-10", TEST_KEY_IDENTIFIER)

        assertArrayEquals(payloadBytes, decrypted)
    }

    @Test
    fun `decryptForMonth works with target month`() = runTest {
        keyRotationService.advanceToMonth("2024-06")

        val payload = "encrypted message".toByteArray(Charsets.UTF_8)
        val encrypted = encryptPayload(keyRotationService, payload)

        //  decryptForMonth must advance keys internally
        val freshRotation = buildKeyRotationService()
        val freshCrypto = buildCryptoService(freshRotation)
        val decrypted = freshCrypto.decryptForMonth(encrypted, "2024-06", TEST_KEY_IDENTIFIER)

        assertArrayEquals(payload, decrypted)
    }

    @Test
    fun `multiple encrypt-decrypt cycles with different months`() = runTest {
        val months = listOf("2023-10", "2023-11", "2023-12", "2024-01")

        for (month in months) {
            val encryptRotation = buildKeyRotationService()
            encryptRotation.advanceToMonth(month)

            val payload = "payload for $month".toByteArray(Charsets.UTF_8)
            val encrypted = encryptPayload(encryptRotation, payload)

            val decryptRotation = buildKeyRotationService()
            val decryptCrypto = buildCryptoService(decryptRotation)
            val decrypted = decryptCrypto.decryptForMonth(encrypted, month, TEST_KEY_IDENTIFIER)

            assertEquals("payload for $month", decrypted.decodeToString())
        }
    }

    @Test
    fun `hex conversion round-trip`() = runTest {
        val original = byteArrayOf(0x0A, 0x1B, 0x2C, 0xFF.toByte(), 0x00)
        val hex = original.toHexString()
        val back = hex.hexToByteArray()
        assertArrayEquals(original, back)
    }

    @Test
    fun `hex conversion accepts uppercase hex`() = runTest {
        assertArrayEquals(byteArrayOf(0x0A, 0x1B), "0A1B".hexToByteArray())
    }

    @Test(expected = IllegalArgumentException::class)
    fun `hex conversion rejects odd length`() = runTest {
        "abc".hexToByteArray()
    }

    @Test(expected = IllegalArgumentException::class)
    fun `hex conversion rejects non-hex characters`() = runTest {
        "0g".hexToByteArray()
    }

    @Test(expected = IllegalArgumentException::class)
    fun `hex conversion rejects empty input`() = runTest {
        "".hexToByteArray()
    }

    @Test(expected = PushNotificationCryptoError.InvalidPNM1Prefix::class)
    fun `decrypt with invalid prefix throws`() = runTest {
        keyRotationService.addGeneration()
        val key = keyRotationService.getLatestGeneration()!!.encryptionKey.hexToByteArray()

        // Manually create ciphertext with wrong prefix but correct 1024-byte size
        val fakePayload = "XXXX".toByteArray() + ByteArray(1020) { 0x20 }

        cryptoService.decryptForMonth(encryptPlaintext(key, fakePayload), "2023-10", TEST_KEY_IDENTIFIER)
    }

    @Test
    fun `decrypt with tampered ciphertext throws AEADBadTagException`() = runTest {
        keyRotationService.addGeneration()
        val payload = "test payload".toByteArray(Charsets.UTF_8)
        val encrypted = encryptPayload(keyRotationService, payload).toMutableList()

        // Flip a byte in the ciphertext region (after IV, before tag)
        encrypted[encrypted.size / 2] = (encrypted[encrypted.size / 2].toInt() xor 0xFF).toByte()

        try {
            cryptoService.decryptForMonth(encrypted.toByteArray(), "2023-10", TEST_KEY_IDENTIFIER)
            throw AssertionError("Expected AEADBadTagException was not thrown")
        } catch (_: AEADBadTagException) {
            // expected — GCM tag validation rejected the tampered ciphertext
        }
    }

    @Test(expected = PushNotificationCryptoError.FutureMonthRejected::class)
    fun `decryptForMonth rejects month too far in the future`() = runTest {
        // currentMonthProvider fixed to "2024-06", so max allowed = "2024-08"
        val encryptRotation = buildKeyRotationService()
        val tooFarFuture = "2025-01"

        val decryptCrypto = buildCryptoService(encryptRotation, currentMonthProvider = { "2024-06" })

        decryptCrypto.decryptForMonth(ByteArray(100), tooFarFuture, TEST_KEY_IDENTIFIER)
    }

    @Test
    fun `decryptForMonth accepts month within the 2-month future window`() = runTest {
        // currentMonthProvider = "2024-06", max = "2024-08" → "2024-08" should be accepted
        val encryptRotation = buildKeyRotationService()
        encryptRotation.advanceToMonth("2024-08")
        val payload = "future payload".toByteArray(Charsets.UTF_8)
        val encrypted = encryptPayload(encryptRotation, payload)

        val decryptRotation = buildKeyRotationService()
        val decryptCrypto = buildCryptoService(decryptRotation, currentMonthProvider = { "2024-06" })
        val decrypted = decryptCrypto.decryptForMonth(encrypted, "2024-08", TEST_KEY_IDENTIFIER)
        assertArrayEquals(payload, decrypted)
    }

    @Test(expected = PushNotificationCryptoError.KeyNotFoundForMonth::class)
    fun `unknown key identifier has no chain and cannot derive a key`() = runTest {
        val freshRotation = buildKeyRotationService()
        val freshCrypto = buildCryptoService(freshRotation)
        freshCrypto.decryptForMonth(ByteArray(32), "2024-06", "unknown-key-id")
    }

    @Test
    fun `knownKeyIdentifiers exposes the registered key identifier`() = runTest {
        keyRotationService.addGeneration()
        assertEquals(setOf(TEST_KEY_IDENTIFIER), cryptoService.knownKeyIdentifiers())
    }
}
