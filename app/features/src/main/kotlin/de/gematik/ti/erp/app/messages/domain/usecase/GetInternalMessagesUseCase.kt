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

package de.gematik.ti.erp.app.messages.domain.usecase

import de.gematik.ti.erp.app.messages.domain.repository.ChangeLogLocalDataSource
import de.gematik.ti.erp.app.messages.mapper.toInAppMessage
import de.gematik.ti.erp.app.messages.repository.InternalMessagesRepository
import de.gematik.ti.erp.app.messages.model.InAppMessage
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/*
    Get the InternalMessages out of the database. If the AppLanguage changed it will re-download the messages
 */
class GetInternalMessagesUseCase(
    private val internalMessagesRepository: InternalMessagesRepository,
    private val changeLogLocalDataSource: ChangeLogLocalDataSource
) {
    operator fun invoke(currentAppLanguage: String): Flow<List<InAppMessage>> =
        internalMessagesRepository.getInternalMessages().map { list ->
            list.map { internalMessage ->
                if (currentAppLanguage == internalMessage.languageCode) {
                    internalMessage.toInAppMessage()
                } else {
                    val updatedLanguageInternalMessage = changeLogLocalDataSource.getInternalMessageInCurrentLanguage(internalMessage)
                    updatedLanguageInternalMessage?.let { internalMessagesRepository.updateInternalMessage(it) }
                    updatedLanguageInternalMessage?.toInAppMessage() ?: internalMessage.toInAppMessage()
                }
            }
        }
}
