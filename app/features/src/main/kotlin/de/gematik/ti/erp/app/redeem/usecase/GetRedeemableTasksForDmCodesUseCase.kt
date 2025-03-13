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

import de.gematik.ti.erp.app.pharmacy.mapper.toOrder
import de.gematik.ti.erp.app.pharmacy.usecase.model.PharmacyUseCaseData
import de.gematik.ti.erp.app.prescription.repository.PrescriptionRepository
import de.gematik.ti.erp.app.profiles.repository.ProfileIdentifier
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.mapNotNull

class GetRedeemableTasksForDmCodesUseCase(
    private val prescriptionRepository: PrescriptionRepository,
    private val dispatchers: CoroutineDispatcher = Dispatchers.IO
) {
    operator fun invoke(
        profileId: ProfileIdentifier

    ): Flow<List<PharmacyUseCaseData.PrescriptionInOrder>> =
        combine(
            prescriptionRepository.syncedTasks(profileId).mapNotNull { tasks ->
                tasks.filter {
                    it.redeemState().isRedeemable()
                }.sortedByDescending { it.authoredOn }
                    .map {
                        it.toOrder()
                    }
            },
            prescriptionRepository.scannedTasks(profileId).mapNotNull { tasks ->
                tasks.filter {
                    it.isRedeemable()
                }.sortedByDescending { it.scannedOn }
                    .map {
                        it.toOrder()
                    }
            }
        ) { syncedTasks, scannedTasks ->
            val prescriptionOrderList = mutableListOf<PharmacyUseCaseData.PrescriptionInOrder>()
            prescriptionOrderList.addAll(scannedTasks)
            prescriptionOrderList.addAll(syncedTasks)
            prescriptionOrderList
        }.flowOn(dispatchers)
}
