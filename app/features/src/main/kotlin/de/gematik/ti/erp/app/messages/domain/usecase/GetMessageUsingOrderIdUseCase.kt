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

import de.gematik.ti.erp.app.invoice.model.InvoiceData
import de.gematik.ti.erp.app.invoice.repository.InvoiceRepository
import de.gematik.ti.erp.app.messages.domain.model.OrderUseCaseData
import de.gematik.ti.erp.app.messages.mappers.toOrderDetail
import de.gematik.ti.erp.app.messages.repository.CommunicationRepository
import de.gematik.ti.erp.app.prescription.mapper.toPrescription
import de.gematik.ti.erp.app.messages.model.Communication
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOn

class GetMessageUsingOrderIdUseCase(
    private val communicationRepository: CommunicationRepository,
    private val invoiceRepository: InvoiceRepository,
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO
) {
    operator fun invoke(orderId: String): Flow<OrderUseCaseData.OrderDetail?> =
        combine(
            communicationRepository.loadDispReqCommunications(orderId),
            communicationRepository.loadPharmacies()
        ) { communications, pharmacies ->
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
        val taskDetailedBundles = taskIds.map {
            val invoice: InvoiceData.PKVInvoiceRecord? = invoiceRepository.invoiceByTaskId(it).first()
            OrderUseCaseData.TaskDetailedBundle(
                invoiceInfo = OrderUseCaseData.InvoiceInfo(
                    hasInvoice = invoice != null,
                    invoiceSentOn = invoice?.timestamp
                ),
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
            taskDetailedBundles = taskDetailedBundles,
            pharmacyName = pharmacyName
        )
    }

    private suspend fun loadPrescription(taskId: String) =
        communicationRepository.loadSyncedByTaskId(taskId).first()?.toPrescription()
            ?: communicationRepository.loadScannedByTaskId(taskId).first()?.toPrescription()
}
