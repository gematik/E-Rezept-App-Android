/*
 * Copyright 2024, gematik GmbH
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

package de.gematik.ti.erp.app.redeem.usecase

import de.gematik.ti.erp.app.pharmacy.usecase.model.PharmacyUseCaseData
import de.gematik.ti.erp.app.redeem.model.DMCode
import de.gematik.ti.erp.app.utils.createDMPayload
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine

class GetDMCodesForLocalRedeemUseCase {
    companion object {
        const val MaxTasks = 3
        const val Four = 4
    }

    operator fun invoke(
        prescriptionOrders: Flow<List<PharmacyUseCaseData.PrescriptionInOrder>>,
        showSingleCodes: Flow<Boolean>
    ): Flow<List<DMCode>> = combine(prescriptionOrders, showSingleCodes) { orders, showSingleCode ->

        val maxTasks =
            when {
                showSingleCode -> 1
                orders.size == Four -> 2 // split to 2x2 codes
                else -> MaxTasks
            }

        orders
            .map { prescription ->
                prescription to "Task/${prescription.taskId}/\$accept?ac=${prescription.accessCode}"
            }
            .windowed(maxTasks, maxTasks, partialWindows = true)
            .map { codes ->
                val prescriptions = codes.map { it.first }
                val urls = codes.map { it.second }
                val json = createDMPayload(urls)
                DMCode(
                    payload = json,
                    nrOfCodes = urls.size,
                    name = prescriptions.mapNotNull { it.title }.joinToString { it },
                    selfPayerPrescriptionNames = prescriptions.filter { it.isSelfPayerPrescription }.map { it.title },
                    containsScanned = prescriptions.any { it.isScanned }
                )
            }
    }
}
