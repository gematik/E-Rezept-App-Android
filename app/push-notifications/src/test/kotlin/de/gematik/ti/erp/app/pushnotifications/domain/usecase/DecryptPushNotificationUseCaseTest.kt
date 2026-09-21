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

package de.gematik.ti.erp.app.pushnotifications.domain.usecase

import de.gematik.ti.erp.app.pushnotifications.domain.crypto.PushNotificationCryptoError
import de.gematik.ti.erp.app.pushnotifications.domain.crypto.PushNotificationCryptoService
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.Base64

class DecryptPushNotificationUseCaseTest {

    private class FakeCryptoService(
        val keyId: String = CORRECT_KEY_ID,
        private val decryptResult: () -> ByteArray = { DEFAULT_PAYLOAD.toByteArray() }
    ) : PushNotificationCryptoService {
        var decryptForMonthCalls = 0

        override val keyIdentifier: String get() = keyId
        override suspend fun decrypt(combined: ByteArray): ByteArray = decryptResult()
        override suspend fun decryptForMonth(combined: ByteArray, timeMessageEncrypted: String): ByteArray {
            decryptForMonthCalls++
            return decryptResult()
        }
    }

    private fun buildUseCase(fake: FakeCryptoService) =
        DecryptPushNotificationUseCase(cryptoService = fake)

    companion object {
        private const val CORRECT_KEY_ID = "correct-key-id"
        private const val TEST_TIME = "2026-05"
        private val DEFAULT_PAYLOAD =
            """{"${DecryptPushNotificationUseCase.KEY_CHANNEL_ID}":"erp.task.activate",
                |"${DecryptPushNotificationUseCase.KEY_IDENTIFIER}":"160.000.000.000.001",
                |"${DecryptPushNotificationUseCase.KEY_IDENTIFIER_TYPE}":"TaskId"}
            """.trimMargin()
    }

    private val dummyCiphertext = Base64.getEncoder().encodeToString(ByteArray(16))

    @Test
    fun `success - full payload is parsed into PushNotificationPayload`() = runTest {
        val fake = FakeCryptoService()
        val result = buildUseCase(fake).invoke(
            ciphertext = dummyCiphertext,
            timeMessageEncrypted = TEST_TIME,
            keyIdentifier = CORRECT_KEY_ID
        )

        assertTrue(result.isSuccess)
        val payload = result.getOrThrow()
        assertEquals("erp.task.activate", payload.channelId)
        assertEquals("160.000.000.000.001", payload.identifier)
        assertEquals("TaskId", payload.identifierType)
    }

    @Test
    fun `success - rawPayload contains original decrypted JSON string`() = runTest {
        val json =
            """{"${DecryptPushNotificationUseCase.KEY_CHANNEL_ID}":"erp.task.activate",
                |"${DecryptPushNotificationUseCase.KEY_IDENTIFIER}":"abc",
                |"${DecryptPushNotificationUseCase.KEY_IDENTIFIER_TYPE}":"TaskId"}
            """.trimMargin()
        val fake = FakeCryptoService(decryptResult = { json.toByteArray() })
        val result = buildUseCase(fake).invoke(
            ciphertext = dummyCiphertext,
            timeMessageEncrypted = TEST_TIME,
            keyIdentifier = CORRECT_KEY_ID
        )

        assertEquals(json, result.getOrThrow().rawPayload)
    }

    @Test
    fun `key_identifier mismatch returns failure with KeyIdentifierMismatch error`() = runTest {
        val fake = FakeCryptoService(keyId = CORRECT_KEY_ID)
        val result = buildUseCase(fake).invoke(
            ciphertext = dummyCiphertext,
            timeMessageEncrypted = TEST_TIME,
            keyIdentifier = "wrong-key-id"
        )

        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is PushNotificationCryptoError.KeyIdentifierMismatch)
        assertEquals(0, fake.decryptForMonthCalls)
    }

    @Test
    fun `decryptForMonth throwing PushNotificationCryptoError propagates as failure`() = runTest {
        val fake = FakeCryptoService(
            decryptResult = { throw PushNotificationCryptoError.NoKeyAvailable() }
        )
        val result = buildUseCase(fake).invoke(
            ciphertext = dummyCiphertext,
            timeMessageEncrypted = TEST_TIME,
            keyIdentifier = CORRECT_KEY_ID
        )

        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is PushNotificationCryptoError.NoKeyAvailable)
    }

    @Test
    fun `non-JSON decrypted payload returns payload with null fields and raw string preserved`() = runTest {
        val fake = FakeCryptoService(decryptResult = { "not-valid-json".toByteArray() })
        val result = buildUseCase(fake).invoke(
            ciphertext = dummyCiphertext,
            timeMessageEncrypted = TEST_TIME,
            keyIdentifier = CORRECT_KEY_ID
        )

        assertTrue(result.isSuccess)
        val payload = result.getOrThrow()
        assertNull(payload.channelId)
        assertNull(payload.identifier)
        assertNull(payload.identifierType)
        assertEquals("not-valid-json", payload.rawPayload)
    }

    @Test
    fun `JSON with missing optional fields returns payload with null for those fields`() = runTest {
        val fake = FakeCryptoService(
            decryptResult = { """{"${DecryptPushNotificationUseCase.KEY_CHANNEL_ID}":"erp.task.activate"}""".toByteArray() }
        )
        val result = buildUseCase(fake).invoke(
            ciphertext = dummyCiphertext,
            timeMessageEncrypted = TEST_TIME,
            keyIdentifier = CORRECT_KEY_ID
        )

        assertTrue(result.isSuccess)
        val payload = result.getOrThrow()
        assertEquals("erp.task.activate", payload.channelId)
        assertNull(payload.identifier)
        assertNull(payload.identifierType)
    }

    @Test
    fun `invalid Base64 ciphertext returns failure`() = runTest {
        val fake = FakeCryptoService()
        val result = buildUseCase(fake).invoke(
            ciphertext = "not-valid-base64!!!",
            timeMessageEncrypted = TEST_TIME,
            keyIdentifier = CORRECT_KEY_ID
        )

        assertTrue(result.isFailure)
    }
}
