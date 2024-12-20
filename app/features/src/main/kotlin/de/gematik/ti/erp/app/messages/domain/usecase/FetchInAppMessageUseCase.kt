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
import de.gematik.ti.erp.app.messages.domain.repository.InAppLocalMessageRepository
import de.gematik.ti.erp.app.prescription.model.CommunicationProfile
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.datetime.Instant

class FetchInAppMessageUseCase(
    private val inAppMessageRepository: InAppMessageRepository,
    private val localMessageRepository: InAppLocalMessageRepository,
    private val messageResources: InAppMessageResources

) {
    suspend operator fun invoke(): Flow<List<InAppMessage>> {
        val showWelcomeMessage = inAppMessageRepository.showWelcomeMessage.first()
        val inAppMessageEntities = inAppMessageRepository.inAppMessages.first()
        val internalMessages = localMessageRepository.getInternalMessages()
        return internalMessages.map {
            it.drop(if (showWelcomeMessage) 1 else 0)
                .map { inAppMessage ->
                    InAppMessage(
                        id = inAppMessage.id,
                        from = messageResources.messageFrom,
                        timestamp = Instant.parse(inAppMessage.timestamp.toString()),
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
