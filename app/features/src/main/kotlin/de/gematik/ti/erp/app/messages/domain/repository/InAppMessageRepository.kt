/*
 * Copyright 2024, gematik GmbH
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
 * In case of changes by gematik find details in the "Readme" file.
 *
 * See the Licence for the specific language governing permissions and limitations under the Licence.
 */

package de.gematik.ti.erp.app.messages.domain.repository

import de.gematik.ti.erp.app.messages.domain.model.InAppMessage
import de.gematik.ti.erp.app.messages.domain.model.InAppMessageResources
import de.gematik.ti.erp.app.messages.domain.model.LocalInAppJsonMessage
import de.gematik.ti.erp.app.messages.mappers.toInAppMessage
import io.github.aakira.napier.Napier
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.serialization.json.Json

private const val IN_APP_MESSAGE_FILE_NAME = "internal_messages.json"
private const val IN_APP_MESSAGE_FOLDER_NAME = "lproj"

class InAppLocalMessageRepository(
    private val messageResources: InAppMessageResources
) {
    fun getInternalMessages(): Flow<List<InAppMessage>> = flow {
        emit(
            try {
                val language = messageResources.language
                val filePath = "$language.$IN_APP_MESSAGE_FOLDER_NAME/$IN_APP_MESSAGE_FILE_NAME" // Construct path to file in assets
                if (messageResources.assets.list("$language.$IN_APP_MESSAGE_FOLDER_NAME")?.contains(IN_APP_MESSAGE_FILE_NAME) == true) {
                    getInAppMessageFromAssets(filePath = filePath, messageResources = messageResources)
                } else { // show german message if device language is not supported
                    getInAppMessageFromAssets(filePath = "de.$IN_APP_MESSAGE_FOLDER_NAME/$IN_APP_MESSAGE_FILE_NAME", messageResources = messageResources)
                }
            } catch (e: Exception) {
                Napier.e("Error reading internal messages: ${e.stackTraceToString()}")
                emptyList<InAppMessage>()
            }

        )
    }
}

private fun getInAppMessageFromAssets(
    filePath: String,
    messageResources: InAppMessageResources
): List<InAppMessage> {
    val jsonString = messageResources.assets.open(filePath).bufferedReader().use { it.readText() }
    val message: List<LocalInAppJsonMessage> = Json.decodeFromString<List<LocalInAppJsonMessage>>(jsonString)
    return message.map {
        it.toInAppMessage(
            messageResources.messageFrom,
            messageResources.getMessageTag(it.version),
            it.timestamp?.let { time -> Instant.parse(time) } ?: Clock.System.now()
        )
    }
}
