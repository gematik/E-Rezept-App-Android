/*
 * Copyright (c) 2022 gematik GmbH
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

package de.gematik.ti.erp.app.communication.usecase

import de.gematik.ti.erp.app.communication.repository.CommunicationRepository
import de.gematik.ti.erp.app.communication.usecase.model.CommunicationUseCaseData
import de.gematik.ti.erp.app.communication.usecase.model.CommunicationUseCaseData.Communication.SupplyOption
import de.gematik.ti.erp.app.prescription.repository.CommunicationProfile.Reply
import de.gematik.ti.erp.app.prescription.repository.CommunicationSupplyOption
import de.gematik.ti.erp.app.prescription.repository.PrescriptionRepository
import de.gematik.ti.erp.app.prescription.repository.SimpleCommunicationWithPharmacy
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map

class CommunicationUseCase(
    private val communicationRepository: CommunicationRepository,
    private val prescriptionRepository: PrescriptionRepository,
) {
    fun pharmacyCommunications(): Flow<List<CommunicationUseCaseData.Communication>> =
        communicationRepository.communications().map {
            it.mapNotNull { it as? SimpleCommunicationWithPharmacy }
        }.combine(prescriptionRepository.tasks()) { communications, tasks ->
            val medications = tasks.associate { it.taskId to it.medicationText }

            communications.map {
                mapSimpleCommunication(it, medications[it.basedOnTaskWithId])
            }
        }

    //
    // mapper
    //

    private fun mapSimpleCommunication(com: SimpleCommunicationWithPharmacy, medicationName: String?) =
        CommunicationUseCaseData.Communication(
            name = medicationName ?: com.basedOnTaskWithId,
            sender = if (com.profile == Reply) com.telematicsId else com.userId,
            recipient = if (com.profile == Reply) com.userId else com.telematicsId,
            infoText = com.payload?.infoText,
            supplyOption = when (com.payload?.supplyOptionsType) {
                CommunicationSupplyOption.OnPremise -> SupplyOption.OnPremise
                CommunicationSupplyOption.Shipment -> SupplyOption.Shipment
                CommunicationSupplyOption.Delivery -> SupplyOption.Delivery
                else -> null
            },
            pickUpCode = (com.payload?.pickUpCodeHR ?: com.payload?.pickUpCodeDMC)?.takeIf { it.isNotBlank() },
            url = com.payload?.url?.takeIf { it.isNotBlank() },
            sent = com.sent
        )
}
