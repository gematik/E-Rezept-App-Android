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

import de.gematik.ti.erp.app.communication.model.CommunicationErpModel
import de.gematik.ti.erp.app.invoice.repository.InvoiceRepository
import de.gematik.ti.erp.app.messages.domain.model.OrderUseCaseData
import de.gematik.ti.erp.app.messages.mapper.toOrderDetail
import de.gematik.ti.erp.app.messages.repository.CommunicationRepository
import de.gematik.ti.erp.app.pharmacy.repository.PharmacyRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map

class GetDispenseMessagesByTaskIdUseCase(
    private val communicationRepository: CommunicationRepository,
    private val pharmacyRepository: PharmacyRepository,
    private val invoiceRepository: InvoiceRepository,
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO
) {
    operator fun invoke(taskId: String): Flow<OrderUseCaseData.OrderDetail?> {
        return communicationRepository.loadDispReqCommunicationsByTaskId(taskId)
            .map { communications ->
                communications.firstOrNull()?.dispenseRequestCommunicationToOrder(
                    taskId = taskId,
                    withMedicationNames = true,
                    pharmacyName = communications.firstOrNull()?.pharmacyName
                )
            }
            .flowOn(dispatcher)
    }

    private suspend fun CommunicationErpModel.dispenseRequestCommunicationToOrder(
        taskId: String,
        withMedicationNames: Boolean,
        pharmacyName: String?
    ): OrderUseCaseData.OrderDetail {
        val invoice = invoiceRepository.invoiceByTaskId(taskId).first()
        val bundle = OrderUseCaseData.TaskDetailedBundle(
            invoiceInfo = OrderUseCaseData.InvoiceInfo(
                hasInvoice = invoice != null,
                invoiceSentOn = invoice?.timestamp
            ),
            prescription = when {
                withMedicationNames -> loadPrescription(taskId)
                else -> null
            }
        )
        val resolvedPharmacyName = pharmacyName?.takeIf { it.isNotBlank() } ?: resolvePharmacyName(
            pharmacyRepository = pharmacyRepository,
            communicationRepository = communicationRepository,
            communicationId = communicationId,
            telematikId = recipient
        )

        return toOrderDetail(
            taskDetailedBundles = listOf(bundle),
            pharmacyName = resolvedPharmacyName
        )
    }

    private suspend fun loadPrescription(taskId: String) =
        communicationRepository.loadSyncedByTaskId(taskId).first()
            ?: communicationRepository.loadScannedByTaskId(taskId).first()
}
