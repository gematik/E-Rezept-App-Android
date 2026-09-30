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
import de.gematik.ti.erp.app.pushnotifications.domain.model.PushNotificationPayload
import java.util.Base64
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

@Serializable
private data class SerializedPushNotificationPayload(
    @SerialName("ChannelId") val channelId: String? = null,
    @SerialName("Identifier") val identifier: String? = null,
    @SerialName("IdentifierType") val identifierType: String? = null
)

/**
 * Validates [keyIdentifier], Base64-decodes and AES-256-GCM decrypts the [ciphertext],
 * then parses the result into a [PushNotificationPayload].
 */
class DecryptPushNotificationUseCase(
    private val cryptoService: PushNotificationCryptoService,
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO
) {
    suspend operator fun invoke(
        ciphertext: String,
        timeMessageEncrypted: String,
        keyIdentifier: String
    ): Result<PushNotificationPayload> = withContext(dispatcher) {
        try {
            val known = cryptoService.knownKeyIdentifiers()
            if (keyIdentifier !in known) {
                throw PushNotificationCryptoError.UnknownKeyIdentifier(received = keyIdentifier, known = known)
            }

            val cipherBytes = Base64.getDecoder().decode(ciphertext)
            val decryptedBytes = cryptoService.decryptForMonth(cipherBytes, timeMessageEncrypted, keyIdentifier)
            val payloadString = decryptedBytes.decodeToString()

            Result.success(parsePayload(payloadString))
        } catch (cancellation: CancellationException) {
            throw cancellation
        } catch (error: Exception) {
            Result.failure(error)
        }
    }

    private fun parsePayload(json: String): PushNotificationPayload =
        runCatching {
            val payload = payloadJson.decodeFromString<SerializedPushNotificationPayload>(json)
            PushNotificationPayload(
                channelId = payload.channelId?.takeIf { it.isNotBlank() },
                identifier = payload.identifier?.takeIf { it.isNotBlank() },
                identifierType = payload.identifierType?.takeIf { it.isNotBlank() },
                rawPayload = json
            )
        }.getOrElse { PushNotificationPayload(rawPayload = json) }

    private companion object {
        val payloadJson = Json {
            ignoreUnknownKeys = true
        }
    }
}
