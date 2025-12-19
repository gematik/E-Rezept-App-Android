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
import de.gematik.ti.erp.app.prescription.mapper.toPrescription
import de.gematik.ti.erp.app.prescription.usecase.model.Prescription
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn

suspend fun Communication.dispenseRequestCommunicationToOrder(
    communicationRepository: CommunicationRepository,
    invoiceRepository: InvoiceRepository,
    withMedicationNames: Boolean,
    dispatcher: CoroutineDispatcher = Dispatchers.IO
): Flow<Pair<List<Prescription?>, Boolean>> = flow {
    val taskIds: List<String> = communicationRepository.taskIdsByOrder(orderId).first()
    val hasUnreadDispenseMessage = communicationRepository.hasUnreadDispenseMessage(taskIds, orderId).first()
    val hasUnreadMessages: Boolean = communicationRepository.hasUnreadRepliedMessages(taskIds = taskIds, telematikId = recipient).first()
    val hasUnreadInvoices: Boolean = invoiceRepository.hasUnreadInvoiceMessages(taskIds).first()

    val prescriptions = if (withMedicationNames) {
        taskIds.map {
            communicationRepository.loadSyncedByTaskId(it).first()?.toPrescription()
                ?: communicationRepository.loadScannedByTaskId(it).first()?.toPrescription()
        }
    } else {
        emptyList()
    }

    emit(Pair(prescriptions, hasUnreadDispenseMessage || hasUnreadMessages || hasUnreadInvoices))
}.flowOn(dispatcher)
