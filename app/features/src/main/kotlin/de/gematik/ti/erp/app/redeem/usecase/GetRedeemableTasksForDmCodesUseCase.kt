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

package de.gematik.ti.erp.app.redeem.usecase

import de.gematik.ti.erp.app.pharmacy.model.PrescriptionInOrderErpModel
import de.gematik.ti.erp.app.pharmacy.mapper.toPrescriptionInOrder
import de.gematik.ti.erp.app.prescription.repository.TaskOperationsRepository
import de.gematik.ti.erp.app.profile.repository.ProfileIdentifier
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.mapNotNull

class GetRedeemableTasksForDmCodesUseCase(
    private val taskOperationsRepository: TaskOperationsRepository,
    private val dispatchers: CoroutineDispatcher = Dispatchers.IO
) {
    operator fun invoke(
        profileId: ProfileIdentifier

    ): Flow<List<PrescriptionInOrderErpModel>> =
        combine(
            taskOperationsRepository.loadSyncedTaskListByProfileId(profileId).mapNotNull { tasks ->
                tasks.filter {
                    it.redeemState().isRedeemable()
                }.sortedByDescending { it.authoredOn }
                    .map {
                        it.toPrescriptionInOrder()
                    }
            },
            taskOperationsRepository.loadScannedTaskListByProfileId(profileId).mapNotNull { tasks ->
                tasks.filter {
                    it.isRedeemable()
                }.sortedByDescending { it.scannedOn }
                    .map {
                        it.toPrescriptionInOrder()
                    }
            }
        ) { syncedTasks, scannedTasks ->
            val prescriptionOrderList = mutableListOf<PrescriptionInOrderErpModel>()
            prescriptionOrderList.addAll(scannedTasks)
            prescriptionOrderList.addAll(syncedTasks)
            prescriptionOrderList
        }.flowOn(dispatchers)
}
