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

package de.gematik.ti.erp.app.messages.domain.usecase

import androidx.annotation.VisibleForTesting
import de.gematik.ti.erp.app.changelogs.InAppMessageRepository
import de.gematik.ti.erp.app.db.entities.v1.changelogs.InAppMessageEntity
import de.gematik.ti.erp.app.info.BuildConfigInformation
import de.gematik.ti.erp.app.messages.domain.model.InAppMessage
import de.gematik.ti.erp.app.messages.domain.model.InAppMessageResources
import de.gematik.ti.erp.app.prescription.model.CommunicationProfile
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

@OptIn(ExperimentalCoroutinesApi::class)
class FetchWelcomeMessageUseCase(
    private val inAppMessageRepository: InAppMessageRepository,
    private val buildConfigInformation: BuildConfigInformation,
    private val messageResources: InAppMessageResources
) {

    operator fun invoke(): Flow<InAppMessage?> =
        inAppMessageRepository.showWelcomeMessage.flatMapLatest { showWelcomeMessage ->
            when {
                showWelcomeMessage -> generateWelcomeMessageFlow()
                else -> flowOf(null)
            }
        }

    private fun generateWelcomeMessageFlow(): Flow<InAppMessage?> = inAppMessageRepository.inAppMessages.flatMapLatest { inAppMessages ->
        flow {
            val versionWithoutRC = buildConfigInformation.versionName().substringBefore("-")
            emit(createWelcomeMessage(versionWithoutRC, inAppMessages))
        }
    }

    private fun createWelcomeMessage(version: String, inAppMessagesEntity: List<InAppMessageEntity>): InAppMessage? =
        inAppMessagesEntity.firstOrNull()?.let {
            InAppMessage(
                id = it.id,
                from = messageResources.messageFrom,
                text = messageResources.welcomeMessage,
                timestamp = Instant.parse(getCurrentTimeAsString()),
                tag = messageResources.welcomeMessageTag,
                isUnread = it.isUnRead,
                lastMessage = null,
                messageProfile = CommunicationProfile.InApp,
                version = version
            )
        }

    @VisibleForTesting
    fun getCurrentTimeAsString(): String {
        val currentInstant = Clock.System.now()
        val dateTime = currentInstant.toLocalDateTime(TimeZone.UTC)
        return dateTime.toString().substring(0, TIMESTAMP_SUBSTRING_LENGTH) + "Z"
    }

    companion object {
        const val TIMESTAMP_SUBSTRING_LENGTH = 19
    }
}
