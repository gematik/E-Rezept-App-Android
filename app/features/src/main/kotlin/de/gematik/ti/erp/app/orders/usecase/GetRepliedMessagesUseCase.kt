/*
 * Copyright (c) 2024 gematik GmbH
 * 
 * Licensed under the EUPL, Version 1.2 or – as soon they will be approved by
 * the European Commission - subsequent versions of the EUPL (the Licence);
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at:
 * 
 *     https://joinup.ec.europa.eu/software/page/eupl
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the Licence is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Licence for the specific language governing permissions and
 * limitations under the Licence.
 * 
 */

package de.gematik.ti.erp.app.orders.usecase

import de.gematik.ti.erp.app.invoice.model.InvoiceData
import de.gematik.ti.erp.app.invoice.repository.InvoiceRepository
import de.gematik.ti.erp.app.orders.mappers.toMessage
import de.gematik.ti.erp.app.orders.repository.CommunicationRepository
import de.gematik.ti.erp.app.orders.usecase.model.OrderUseCaseData
import io.github.aakira.napier.Napier
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map

class GetRepliedMessagesUseCase(
    private val communicationRepository: CommunicationRepository,
    private val invoiceRepository: InvoiceRepository,
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO
) {
    operator fun invoke(orderId: String): Flow<List<OrderUseCaseData.Message>> =
        communicationRepository.taskIdsByOrder(orderId).flatMapLatest { taskIds ->
            Napier.d("taskIds is $taskIds")
            communicationRepository.loadRepliedCommunications(taskIds = taskIds)
                .map { communications ->
                    Napier.d("communications is $communications") // Getting empty list if No reply message
                    communications.map {
                        val invoice: InvoiceData.PKVInvoice? = invoiceRepository.invoiceById(
                            it.taskId
                        ).first() // Loading chargeItem with TaskId
                        Napier.d("invoice is $invoice")
                        it.toMessage(invoice != null)
                    }
                }
        }.flowOn(dispatcher)
}
