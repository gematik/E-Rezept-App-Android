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

import de.gematik.ti.erp.app.Requirement
import de.gematik.ti.erp.app.pushnotifications.domain.crypto.PushNotificationCryptoError
import de.gematik.ti.erp.app.pushnotifications.domain.crypto.PushNotificationCryptoService
import de.gematik.ti.erp.app.pushnotifications.domain.model.PushNotificationPayload
import org.json.JSONObject
import java.util.Base64

/**
 * Validates [keyIdentifier], Base64-decodes and AES-256-GCM decrypts the [ciphertext],
 * then parses the result into a [PushNotificationPayload].
 */
class DecryptPushNotificationUseCase(
    private val cryptoService: PushNotificationCryptoService
) {
    @Requirement(
        "A_27179#3",
        sourceSpecification = "gemF_PushNotification",
        rationale = "Rejects encrypted pushes whose key_identifier does not match the registered key chain before decrypting.",
        codeLines = 13
    )
    suspend operator fun invoke(
        ciphertext: String,
        timeMessageEncrypted: String,
        keyIdentifier: String
    ): Result<PushNotificationPayload> = runCatching {
        val expectedKeyId = cryptoService.keyIdentifier
        if (keyIdentifier != expectedKeyId) {
            throw PushNotificationCryptoError.KeyIdentifierMismatch(
                received = keyIdentifier,
                expected = expectedKeyId
            )
        }

        val cipherBytes = Base64.getDecoder().decode(ciphertext)
        val decryptedBytes = cryptoService.decryptForMonth(cipherBytes, timeMessageEncrypted)
        val payloadString = decryptedBytes.decodeToString()

        parsePayload(payloadString)
    }

    private fun parsePayload(json: String): PushNotificationPayload =
        runCatching {
            val obj = JSONObject(json)
            PushNotificationPayload(
                channelId = obj.optString(KEY_CHANNEL_ID).takeIf { it.isNotBlank() },
                identifier = obj.optString(KEY_IDENTIFIER).takeIf { it.isNotBlank() },
                identifierType = obj.optString(KEY_IDENTIFIER_TYPE).takeIf { it.isNotBlank() },
                rawPayload = json
            )
        }.getOrElse { PushNotificationPayload(rawPayload = json) }

    companion object {
        const val KEY_CHANNEL_ID = "ChannelId"
        const val KEY_IDENTIFIER = "Identifier"
        const val KEY_IDENTIFIER_TYPE = "IdentifierType"
    }
}
