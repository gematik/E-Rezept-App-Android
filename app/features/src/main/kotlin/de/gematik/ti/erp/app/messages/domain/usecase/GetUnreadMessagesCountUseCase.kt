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

import de.gematik.ti.erp.app.invoice.repository.InvoiceRepository
import de.gematik.ti.erp.app.messages.model.Communication
import de.gematik.ti.erp.app.messages.repository.CommunicationRepository
import de.gematik.ti.erp.app.messages.repository.InternalMessagesRepository
import de.gematik.ti.erp.app.profile.repository.ProfileIdentifier
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.onEmpty

class GetUnreadMessagesCountUseCase(
    private val communicationRepository: CommunicationRepository,
    private val internalMessagesRepository: InternalMessagesRepository,
    private val invoiceRepository: InvoiceRepository,
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO
) {
    operator fun invoke(profileId: ProfileIdentifier): Flow<Long> {
        return combine(
            communicationRepository.unreadMessagesCount(),
            getUnreadInvoiceCount(profileId),
            internalMessagesRepository.getUnreadInternalMessagesCount()
        ) { unreadMessagesCount, unreadInvoiceCount, unreadInternalMessagesCount ->
            val totalCount = unreadMessagesCount + unreadInvoiceCount + unreadInternalMessagesCount
            totalCount
        }.flowOn(dispatcher)
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    private fun getUnreadInvoiceCount(profileId: ProfileIdentifier): Flow<Long> {
        return invoiceRepository.getInvoiceTaskIdAndConsumedStatus(profileId)
            .flatMapLatest { invoiceStatusList ->
                if (invoiceStatusList.isEmpty()) {
                    return@flatMapLatest flowOf(0L)
                }

                val taskIdConsumedMap = invoiceStatusList.associate { it.taskId to it.consumed }

                val requestCommunicationFlow = communicationRepository.loadDispReqCommunicationsByProfileId(profileId)
                val replyCommunicationFlow = communicationRepository.loadRepliedCommunications(
                    invoiceStatusList.map { it.taskId },
                    ""
                )

                combine(
                    requestCommunicationFlow,
                    replyCommunicationFlow
                ) { requestFlow, replyFlow ->
                    val unreadRequestTaskIds = filterUnreadTaskIds(requestFlow, taskIdConsumedMap)
                    val unreadReplyTaskIds = filterUnreadTaskIds(replyFlow, taskIdConsumedMap)

                    // Combine both request and reply task IDs and remove duplicates
                    if ((unreadRequestTaskIds + unreadReplyTaskIds).toSet().isNotEmpty()) 1 else 0L
                }
            }
            .onEmpty {
                emit(0L)
            }
    }

    private fun filterUnreadTaskIds(
        communicationFlow: List<Communication>,
        taskIdConsumedMap: Map<String, Boolean>
    ): List<String> {
        return communicationFlow
            .filter { it.taskId in taskIdConsumedMap && taskIdConsumedMap[it.taskId] == false }
            .map { it.taskId }
    }
}
