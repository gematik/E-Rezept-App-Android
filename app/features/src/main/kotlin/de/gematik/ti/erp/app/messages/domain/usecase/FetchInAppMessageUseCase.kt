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

import de.gematik.ti.erp.app.changelogs.InAppMessageRepository
import de.gematik.ti.erp.app.messages.domain.model.InAppMessage
import de.gematik.ti.erp.app.messages.domain.model.InAppMessageResources
import de.gematik.ti.erp.app.messages.domain.model.getTimeState
import de.gematik.ti.erp.app.messages.domain.repository.InAppLocalMessageRepository
import de.gematik.ti.erp.app.prescription.model.CommunicationProfile
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.datetime.Instant

/**
 * Use case for fetching and transforming in-app messages to display in the UI.
 *
 * This use case combines internal and external message sources to provide a unified
 * flow of in-app messages, applying transformations to enrich the data.
 *
 * @param inAppMessageRepository Repository for retrieving in-app message configurations and data.
 * @param localMessageRepository Repository for managing local/internal in-app messages.
 * @param messageResources Resources used for enriching message content (e.g., tags, sender details).
 *
 * * ### Testing Note:
 *  * - When `showWelcomeMessage` is **false**, the application can display changelog messages on a fresh installation.
 *  *   This behavior can be used to validate the handling of changelog messages during testing scenarios.
 */
class FetchInAppMessageUseCase(
    private val inAppMessageRepository: InAppMessageRepository,
    private val localMessageRepository: InAppLocalMessageRepository,
    private val messageResources: InAppMessageResources
) {
    /**
     * Invokes the use case to fetch and process in-app messages.
     *
     * The use case performs the following steps:
     * - Checks if a welcome message should be shown using [inAppMessageRepository.showWelcomeMessage].
     * - Retrieves external in-app messages from [inAppMessageRepository.inAppMessages].
     * - Combines the above with internal messages fetched from [localMessageRepository.getInternalMessages].
     * - Drops the welcome message if it should not be shown.
     * - Maps the messages to [InAppMessage] objects, enriching them with:
     *   - A formatted sender name from [messageResources.messageFrom].
     *   - A formatted tag using [messageResources.getMessageTag].
     *   - Unread status based on external message data.
     *
     * @return A [Flow] of lists of [InAppMessage] objects for display in the UI.
     */
    suspend operator fun invoke(): Flow<List<InAppMessage>> {
        // Check if the welcome message should be displayed
        val showWelcomeMessage = inAppMessageRepository.showWelcomeMessage.first()

        // Fetch external in-app messages
        val inAppMessageEntities = inAppMessageRepository.inAppMessages.first()

        // Fetch internal in-app messages from local storage
        val internalMessages = localMessageRepository.getInternalMessages()

        // Process and transform internal messages
        return internalMessages.map {
            // Optionally drop the welcome message based on the flag
            it.drop(if (showWelcomeMessage) 1 else 0)
                .map { inAppMessage ->
                    // Map the raw message data to a fully constructed InAppMessage object
                    InAppMessage(
                        id = inAppMessage.id,
                        from = messageResources.messageFrom,
                        timeState = getTimeState(Instant.parse(inAppMessage.timeState.timestamp.toString())),
                        text = inAppMessage.text,
                        tag = messageResources.getMessageTag(inAppMessage.version),
                        version = inAppMessage.version,
                        isUnread = inAppMessageEntities.find { it.id == inAppMessage.id }?.isUnRead ?: false,
                        lastMessage = null,
                        messageProfile = CommunicationProfile.InApp,
                        prescriptionsCount = 0
                    )
                }
        }
    }
}
