/*
 * Copyright 2025, gematik GmbH
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

import de.gematik.ti.erp.app.fhir.constant.SafeJson
import de.gematik.ti.erp.app.messages.domain.model.InternalMessageResources
import de.gematik.ti.erp.app.messages.mapper.toInternalMessage
import de.gematik.ti.erp.app.messages.model.ChangeLogMessage
import de.gematik.ti.erp.app.messages.model.CommunicationProfile
import de.gematik.ti.erp.app.messages.model.InternalMessage
import de.gematik.ti.erp.app.timestate.TimeState
import de.gematik.ti.erp.app.timestate.getTimeState
import io.github.aakira.napier.Napier
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant

private const val IN_APP_MESSAGE_FILE_NAME = "internal_messages.json"
private const val IN_APP_MESSAGE_FOLDER_NAME = "lproj"
const val WELCOME_MESSAGE_ID = "0"

class ChangeLogLocalDataSource(
    private val messageResources: InternalMessageResources
) {
    fun getChangeLogsAsInternalMessage(): List<InternalMessage> =
        try {
            val language = messageResources.language
            val filePath = "$language.$IN_APP_MESSAGE_FOLDER_NAME/$IN_APP_MESSAGE_FILE_NAME" // Construct path to file in assets
            if (messageResources.assets.list("$language.$IN_APP_MESSAGE_FOLDER_NAME")?.contains(IN_APP_MESSAGE_FILE_NAME) == true) {
                getChangeLogsFromAssets(filePath = filePath, messageResources = messageResources)
            } else { // show german message if device language is not supported
                getChangeLogsFromAssets(filePath = "de.$IN_APP_MESSAGE_FOLDER_NAME/$IN_APP_MESSAGE_FILE_NAME", messageResources = messageResources)
            }
        } catch (e: Exception) {
            Napier.e("Error reading internal messages: ${e.stackTraceToString()}")
            emptyList<InternalMessage>()
        }

    fun createWelcomeMessage(
        currentVersion: String,
        time: TimeState = getTimeState(Clock.System.now()),
        isUnread: Boolean = true
    ) =
        InternalMessage(
            id = WELCOME_MESSAGE_ID,
            version = currentVersion,
            time = time,
            sender = messageResources.messageFrom,
            tag = messageResources.welcomeMessageTag,
            text = messageResources.welcomeMessage,
            isUnread = isUnread,
            messageProfile = CommunicationProfile.InApp,
            languageCode = messageResources.language
        )

    fun getInternalMessageInCurrentLanguage(internalMessage: InternalMessage): InternalMessage? =
        when {
            internalMessage.id == WELCOME_MESSAGE_ID -> createWelcomeMessage(
                internalMessage.version,
                internalMessage.time,
                internalMessage.isUnread
            )
            else -> getChangeLogsAsInternalMessage().find { it.id == internalMessage.id }
        }
}

private fun getChangeLogsFromAssets(
    filePath: String,
    messageResources: InternalMessageResources
): List<InternalMessage> {
    val jsonString = messageResources.assets.open(filePath).bufferedReader().use { it.readText() }
    val message: List<ChangeLogMessage> = SafeJson.value.decodeFromString<List<ChangeLogMessage>>(jsonString)
    return message.map {
        it.toInternalMessage(
            changeLogSender = messageResources.messageFrom,
            changeLogTag = messageResources.getMessageTag(it.version),
            changeLogTimeStamp = it.timestamp?.let { time -> Instant.parse(time) } ?: Clock.System.now(),
            language = messageResources.language
        )
    }
}
