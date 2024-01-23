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

import de.gematik.ti.erp.app.orders.mappers.toOrder
import de.gematik.ti.erp.app.orders.repository.CommunicationRepository
import de.gematik.ti.erp.app.orders.usecase.model.OrderUseCaseData
import de.gematik.ti.erp.app.prescription.mapper.toPrescription
import de.gematik.ti.erp.app.prescription.model.Communication
import kotlinx.coroutines.flow.first

suspend fun Communication.dispenseRequestCommunicationToOrder(
    communicationRepository: CommunicationRepository,
    withMedicationNames: Boolean,
    pharmacyName: String?
): OrderUseCaseData.Order {
    val taskIds = communicationRepository.taskIdsByOrder(orderId).first()
    val hasUnreadMessages = communicationRepository.hasUnreadPrescription(taskIds, orderId).first()
    val prescriptions = if (withMedicationNames) {
        taskIds.map {
            communicationRepository.loadSyncedByTaskId(it).first()?.toPrescription()
                ?: communicationRepository.loadScannedByTaskId(it).first()?.toPrescription()
        }
    } else {
        emptyList()
    }

    if (pharmacyName == null) {
        communicationRepository.downloadMissingPharmacy(recipient)
    }

    return toOrder(
        prescriptions = prescriptions,
        hasUnreadMessages = hasUnreadMessages,
        taskIds = taskIds,
        pharmacyName = pharmacyName
    )
}
