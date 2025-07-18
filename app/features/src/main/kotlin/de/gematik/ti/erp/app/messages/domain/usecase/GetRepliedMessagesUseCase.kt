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

import de.gematik.ti.erp.app.messages.domain.model.OrderUseCaseData
import de.gematik.ti.erp.app.messages.mappers.toMessage
import de.gematik.ti.erp.app.messages.repository.CommunicationRepository
import de.gematik.ti.erp.app.messages.model.Communication
import io.github.aakira.napier.Napier
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map

class GetRepliedMessagesUseCase(
    private val communicationRepository: CommunicationRepository,
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO
) {
    @OptIn(ExperimentalCoroutinesApi::class)
    operator fun invoke(orderId: String, telematikId: String): Flow<List<OrderUseCaseData.Message>> =
        communicationRepository.taskIdsByOrder(orderId).flatMapLatest { taskIds ->
            communicationRepository.loadRepliedCommunications(taskIds = taskIds, telematikId = telematikId)
                .map { communications ->
                    Napier.d { "GetRepliedMessagesUseCase: communications: $communications" }
                    communications.map(Communication::toMessage)
                }
        }.flowOn(dispatcher)
}
