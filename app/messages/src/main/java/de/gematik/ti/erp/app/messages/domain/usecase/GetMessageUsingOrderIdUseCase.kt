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
import de.gematik.ti.erp.app.invoice.model.PKVInvoiceErpModel
import de.gematik.ti.erp.app.invoice.repository.InvoiceRepository
import de.gematik.ti.erp.app.messages.domain.model.OrderUseCaseData
import de.gematik.ti.erp.app.messages.mapper.toOrderDetail
import de.gematik.ti.erp.app.messages.repository.CommunicationRepository
import de.gematik.ti.erp.app.pharmacy.repository.PharmacyRepository
import de.gematik.ti.erp.app.task.model.TaskErpModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.datetime.Clock

class GetMessageUsingOrderIdUseCase(
    private val communicationRepository: CommunicationRepository,
    private val pharmacyRepository: PharmacyRepository,
    private val invoiceRepository: InvoiceRepository,
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO
) {
    @OptIn(ExperimentalCoroutinesApi::class)
    operator fun invoke(orderId: String): Flow<OrderUseCaseData.OrderDetail?> {
        return communicationRepository.loadDispReqCommunications(orderId)
            .map { communications ->
                communications.firstOrNull()?.dispenseRequestCommunicationToOrder(
                    communicationRepository = communicationRepository,
                    withMedicationNames = true,
                    pharmacyName = communications.firstOrNull()?.pharmacyName
                )
            }
            .flatMapLatest { dispenseRequestOrderDetail ->
                if (dispenseRequestOrderDetail != null) {
                    flowOf(dispenseRequestOrderDetail)
                } else {
                    // No dispense-request communication is known locally for this order - this happens
                    // e.g. when the redemption itself was performed on a different device and the
                    // dispense-request was already removed server-side by the time this device synced.
                    // Fall back to building the OrderDetail purely from the reply side so the order can
                    // still be opened.
                    communicationRepository.loadRepliedCommunications(orderId)
                        .map { replies ->
                            replies.firstOrNull()?.replyCommunicationToOrder(
                                communicationRepository = communicationRepository,
                                withMedicationNames = true,
                                pharmacyName = replies.firstOrNull()?.pharmacyName
                            )
                        }
                }
            }
            .flowOn(dispatcher)
    }

    private suspend fun CommunicationErpModel.dispenseRequestCommunicationToOrder(
        communicationRepository: CommunicationRepository,
        withMedicationNames: Boolean,
        pharmacyName: String?
    ): OrderUseCaseData.OrderDetail {
        val taskDetailedBundles = taskDetailedBundlesByOrder(orderId, withMedicationNames)
        val resolvedPharmacyName = pharmacyName?.takeIf { it.isNotBlank() } ?: resolvePharmacyName(
            pharmacyRepository = pharmacyRepository,
            communicationRepository = communicationRepository,
            communicationId = communicationId,
            telematikId = recipient
        )

        return toOrderDetail(
            taskDetailedBundles = taskDetailedBundles,
            pharmacyName = resolvedPharmacyName
        )
    }

    private suspend fun CommunicationErpModel.replyCommunicationToOrder(
        communicationRepository: CommunicationRepository,
        withMedicationNames: Boolean,
        pharmacyName: String?
    ): OrderUseCaseData.OrderDetail {
        val taskDetailedBundles = taskDetailedBundlesByOrder(orderId, withMedicationNames)
        val resolvedPharmacyName = pharmacyName?.takeIf { it.isNotBlank() } ?: resolvePharmacyName(
            pharmacyRepository = pharmacyRepository,
            communicationRepository = communicationRepository,
            communicationId = communicationId,
            // The reply's `recipient` is the patient, not the pharmacy - the pharmacy is the sender.
            telematikId = senderTelematikId
        )

        return OrderUseCaseData.OrderDetail(
            orderId = orderId,
            taskDetailedBundles = taskDetailedBundles,
            sentOn = timeStamp ?: Clock.System.now(),
            pharmacy = OrderUseCaseData.Pharmacy(name = resolvedPharmacyName ?: "", id = senderTelematikId)
        )
    }

    private suspend fun taskDetailedBundlesByOrder(
        orderId: String,
        withMedicationNames: Boolean
    ): List<OrderUseCaseData.TaskDetailedBundle> {
        val taskIds = communicationRepository.taskIdsByOrder(orderId).first()
        return taskIds.map {
            val invoice: PKVInvoiceErpModel? = invoiceRepository.invoiceByTaskId(it).first()
            OrderUseCaseData.TaskDetailedBundle(
                invoiceInfo = OrderUseCaseData.InvoiceInfo(
                    hasInvoice = invoice != null,
                    invoiceSentOn = invoice?.timestamp,
                    medicationName = invoice?.medicationRequest?.medication?.name()
                ),
                prescription = when {
                    withMedicationNames -> loadPrescription(it)
                    else -> null
                }
            )
        }
    }

    private suspend fun loadPrescription(taskId: String): TaskErpModel? =
        communicationRepository.loadSyncedByTaskId(taskId).first()
            ?: communicationRepository.loadScannedByTaskId(taskId).first()
}
