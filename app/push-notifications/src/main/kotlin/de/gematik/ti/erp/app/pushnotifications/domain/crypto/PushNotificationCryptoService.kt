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

import de.gematik.ti.erp.app.Requirement
import de.gematik.ti.erp.app.pushnotifications.domain.crypto.PushNotificationKeyRotationService.Companion.incrementMonth
import de.gematik.ti.erp.app.pushnotifications.domain.crypto.PushNotificationKeyRotationService.Companion.requireYearMonth
import de.gematik.ti.erp.app.pushnotifications.domain.model.PushNotificationKeyGeneration
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import javax.crypto.Cipher
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.SecretKeySpec

interface PushNotificationCryptoService {
    val keyIdentifier: String

    suspend fun decrypt(combined: ByteArray): ByteArray

    suspend fun decryptForMonth(combined: ByteArray, timeMessageEncrypted: String): ByteArray
}

class DefaultPushNotificationCryptoService(
    private val keyRotationService: PushNotificationKeyRotationService,
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO,
    private val currentMonthProvider: () -> String = {
        val now = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
        "${now.year}-${now.monthNumber.toString().padStart(2, '0')}"
    }
) : PushNotificationCryptoService {

    override val keyIdentifier: String
        get() = keyRotationService.keyIdentifier

    @Requirement(
        "A_27181#1",
        sourceSpecification = "gemF_PushNotification",
        rationale = "Decrypts the AES/GCM content and strips the PNM1 padding.",
        codeLines = 7
    )
    override suspend fun decrypt(combined: ByteArray): ByteArray = withContext(dispatcher) {
        val generation = keyRotationService.getLatestGeneration()
            ?: throw PushNotificationCryptoError.NoKeyAvailable()
        decryptWithGeneration(combined, generation)
    }

    @Requirement(
        "A_27179#2",
        "A_27181#2",
        sourceSpecification = "gemF_PushNotification",
        rationale = "Derives keys to the time_message_encrypted month for A_27179 and decrypts the content for A_27181.",
        codeLines = 25
    )
    override suspend fun decryptForMonth(
        combined: ByteArray,
        timeMessageEncrypted: String
    ): ByteArray = withContext(dispatcher) {
        val validTimeMessageEncrypted = requireYearMonth(timeMessageEncrypted)
        val currentMonth = requireYearMonth(currentMonthProvider())
        var maxAllowed = currentMonth
        repeat(PushNotificationKeyRotationService.MAX_FUTURE_MONTHS_ALLOWED) { maxAllowed = incrementMonth(maxAllowed) }
        if (validTimeMessageEncrypted > maxAllowed) {
            throw PushNotificationCryptoError.FutureMonthRejected(validTimeMessageEncrypted, maxAllowed)
        }

        val generation = keyRotationService.getGenerationForMonth(validTimeMessageEncrypted)
            ?: run {
                // Only attempt to advance if the month is not already past the head.
                val latest = keyRotationService.getLatestGeneration()
                if (latest != null && validTimeMessageEncrypted < latest.month) {
                    throw PushNotificationCryptoError.KeyNotFoundForMonth(validTimeMessageEncrypted)
                }
                keyRotationService.advanceToMonth(validTimeMessageEncrypted)
                keyRotationService.getGenerationForMonth(validTimeMessageEncrypted)
                    ?: throw PushNotificationCryptoError.KeyNotFoundForMonth(validTimeMessageEncrypted)
            }
        decryptWithGeneration(combined, generation)
    }

    private fun decryptWithGeneration(
        combined: ByteArray,
        generation: PushNotificationKeyGeneration
    ): ByteArray {
        val key = SecretKeySpec(generation.encryptionKey.hexToByteArray(), "AES")

        if (combined.size <= GCM_IV_LENGTH) throw PushNotificationCryptoError.InvalidPayloadTooShort()

        val iv = combined.copyOfRange(0, GCM_IV_LENGTH)
        val ciphertext = combined.copyOfRange(GCM_IV_LENGTH, combined.size)

        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        cipher.init(Cipher.DECRYPT_MODE, key, GCMParameterSpec(GCM_TAG_BITS, iv))
        val decrypted = cipher.doFinal(ciphertext)

        if (decrypted.size < PREFIX_SIZE + LENGTH_FIELD_SIZE) throw PushNotificationCryptoError.InvalidPayloadTooShort()
        if (!decrypted.copyOfRange(0, PREFIX_SIZE).contentEquals(PNM1_PREFIX)) throw PushNotificationCryptoError.InvalidPNM1Prefix()

        val paddingLength = ((decrypted[PREFIX_SIZE].toInt() and 0xFF) shl 8) or
            (decrypted[PREFIX_SIZE + 1].toInt() and 0xFF)
        val payloadStart = PREFIX_SIZE + LENGTH_FIELD_SIZE + paddingLength
        if (payloadStart > decrypted.size) throw PushNotificationCryptoError.InvalidPayloadLength()

        return decrypted.copyOfRange(payloadStart, decrypted.size)
    }

    private companion object {
        val PNM1_PREFIX = "PNM1".toByteArray(Charsets.UTF_8)
        const val PREFIX_SIZE = 4 // "PNM1" marker
        const val LENGTH_FIELD_SIZE = 2 // big-endian padding length field
        const val GCM_IV_LENGTH = 12 // standard AES-GCM IV size
        const val GCM_TAG_BITS = 128 // AES-GCM authentication tag length
    }
}
