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

package de.gematik.ti.erp.app.messages.domain.usecase

import de.gematik.ti.erp.app.messages.repository.CommunicationRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.withContext

class UpdateCommunicationConsumedStatusUseCase(
    private val repository: CommunicationRepository,
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO
) {

    suspend operator fun invoke(identifier: CommunicationIdentifier) {
        withContext(dispatcher) {
            when (identifier) {
                is CommunicationIdentifier.Communication -> {
                    repository.setCommunicationStatus(identifier.id, true)
                }

                is CommunicationIdentifier.Order -> {
                    repository.taskIdsByOrder(identifier.id)
                        .firstOrNull()
                        ?.let { taskIds ->
                            repository.loadAllRepliedCommunications(taskIds)
                                .firstOrNull()?.forEach {
                                    repository.setCommunicationStatus(it.communicationId, consumed = true)
                                }
                        }
                    repository.loadDispReqCommunications(identifier.id)
                        .firstOrNull()?.forEach {
                            repository.setCommunicationStatus(it.communicationId, consumed = true)
                        }
                }
            }
        }
    }

    companion object {
        sealed interface CommunicationIdentifier {
            data class Communication(val id: String) : CommunicationIdentifier
            data class Order(val id: String) : CommunicationIdentifier
        }
    }
}
