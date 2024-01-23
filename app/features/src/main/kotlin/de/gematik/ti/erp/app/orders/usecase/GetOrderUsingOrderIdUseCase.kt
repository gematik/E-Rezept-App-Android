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
import de.gematik.ti.erp.app.orders.mappers.toOrderDetail
import de.gematik.ti.erp.app.orders.repository.CommunicationRepository
import de.gematik.ti.erp.app.orders.usecase.model.OrderUseCaseData
import de.gematik.ti.erp.app.prescription.mapper.toPrescription
import de.gematik.ti.erp.app.prescription.model.Communication
import io.github.aakira.napier.Napier
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOn

class GetOrderUsingOrderIdUseCase(
    private val communicationRepository: CommunicationRepository,
    private val invoiceRepository: InvoiceRepository,
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO
) {
    operator fun invoke(orderId: String): Flow<OrderUseCaseData.OrderDetail?> =
        combine(
            communicationRepository.loadDispReqCommunications(orderId),
            communicationRepository.loadPharmacies()
        ) { communications, pharmacies ->
            Napier.d("communications are $communications \npharmacies are $pharmacies ")
            communications.firstOrNull()?.let { communication ->
                communication.dispenseRequestCommunicationToOrder(
                    communicationRepository = communicationRepository,
                    withMedicationNames = true,
                    pharmacyName = pharmacies.find { it.telematikId == communication.recipient }?.name
                )
            }
        }.flowOn(dispatcher)

    private suspend fun Communication.dispenseRequestCommunicationToOrder(
        communicationRepository: CommunicationRepository,
        withMedicationNames: Boolean,
        pharmacyName: String?
    ): OrderUseCaseData.OrderDetail {
        val taskIds = communicationRepository.taskIdsByOrder(orderId).first()
        val hasUnreadMessages = communicationRepository.hasUnreadPrescription(taskIds, orderId).first()

        val taskDetailedBundles = taskIds.map {
            val invoice: InvoiceData.PKVInvoice? = invoiceRepository.invoiceById(it).first()
            OrderUseCaseData.TaskDetailedBundle(
                taskId = it,
                hasInvoice = invoice != null,
                prescription = when {
                    withMedicationNames -> loadPrescription(it)
                    else -> null
                }
            )
        }
        if (pharmacyName == null) {
            communicationRepository.downloadMissingPharmacy(recipient)
        }

        return toOrderDetail(
            hasUnreadMessages = hasUnreadMessages,
            taskDetailedBundles = taskDetailedBundles,
            pharmacyName = pharmacyName
        )
    }

    private suspend fun loadPrescription(taskId: String) =
        communicationRepository.loadSyncedByTaskId(taskId).first()?.toPrescription()
            ?: communicationRepository.loadScannedByTaskId(taskId).first()?.toPrescription()
}
