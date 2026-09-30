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

package de.gematik.ti.erp.app.debugsettings.pushnotifications.usecase

import de.gematik.ti.erp.app.pushnotifications.domain.crypto.PushKeyChainAdvancer
import de.gematik.ti.erp.app.pushnotifications.domain.crypto.PushNotificationCryptoError
import de.gematik.ti.erp.app.pushnotifications.domain.crypto.hexToByteArray
import de.gematik.ti.erp.app.pushnotifications.domain.model.PushNotificationKeyGeneration
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.security.SecureRandom
import javax.crypto.Cipher
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.SecretKeySpec

/**
 * Simulates Fachdienst-side encryption for debug pushes.
 *
 * PNM1 plaintext format (1024 bytes total before encryption):
 * - "PNM1" (4 bytes)
 * - 2 bytes: length of space padding (big-endian)
 * - N spaces (0x20)
 * - actual payload
 */
class EncryptDebugPushNotificationPayloadUseCase(
    private val keyChainAdvancer: PushKeyChainAdvancer,
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO,
    private val secureRandom: SecureRandom = SecureRandom()
) {
    suspend operator fun invoke(payload: ByteArray): EncryptedDebugPushNotificationPayload =
        withContext(dispatcher) {
            val keyId = keyChainAdvancer.knownKeyIdentifiers().firstOrNull()
                ?: throw PushNotificationCryptoError.NoKeyAvailable()
            val generation = keyChainAdvancer.getLatestGeneration(keyId)
                ?: throw PushNotificationCryptoError.NoKeyAvailable()
            val key = SecretKeySpec(generation.encryptionKey.hexToByteArray(), "AES")

            val paddingLength = MAX_PAYLOAD_SIZE - payload.size
            if (paddingLength < 0) throw PushNotificationCryptoError.InvalidPayloadLength()

            val lengthBytes = byteArrayOf(
                (paddingLength shr 8).toByte(),
                (paddingLength and 0xFF).toByte()
            )
            val plaintext = PNM1_PREFIX + lengthBytes + ByteArray(paddingLength) { 0x20 } + payload

            val iv = ByteArray(GCM_IV_LENGTH).also(secureRandom::nextBytes)
            val cipher = Cipher.getInstance("AES/GCM/NoPadding")
            cipher.init(Cipher.ENCRYPT_MODE, key, GCMParameterSpec(GCM_TAG_BITS, iv))

            EncryptedDebugPushNotificationPayload(
                ciphertext = iv + cipher.doFinal(plaintext),
                keyGeneration = generation
            )
        }

    private companion object {
        val PNM1_PREFIX = "PNM1".toByteArray(Charsets.UTF_8)
        const val PADDED_SIZE = 1024
        const val PREFIX_SIZE = 4
        const val LENGTH_FIELD_SIZE = 2

        // PNM1 plaintext is exactly 1024 bytes:
        // "PNM1" (4) + padding length field (2) + padding + payload.
        // So max payload size is 1024 - 4 - 2 = 1018 bytes.
        const val MAX_PAYLOAD_SIZE = PADDED_SIZE - PREFIX_SIZE - LENGTH_FIELD_SIZE
        const val GCM_IV_LENGTH = 12
        const val GCM_TAG_BITS = 128
    }
}

data class EncryptedDebugPushNotificationPayload(
    val ciphertext: ByteArray,
    val keyGeneration: PushNotificationKeyGeneration
)
