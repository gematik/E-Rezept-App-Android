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

import de.gematik.ti.erp.app.prescription.model.PrescriptionData
import de.gematik.ti.erp.app.prescription.model.SyncedTaskData.SyncedTask
import de.gematik.ti.erp.app.prescription.repository.PrescriptionRepository
import de.gematik.ti.erp.app.redeem.model.PrescriptionReadinessResult
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOn

class GetReadyPrescriptionsByTaskIdsUseCase(
    private val repository: PrescriptionRepository,
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO
) {
    operator fun invoke(taskIds: List<String>): Flow<PrescriptionReadinessResult> =
        combine(
            repository.loadSyncedTasksByTaskIds(taskIds),
            repository.loadScannedTasksByTaskIds(taskIds)
        ) { syncedTasks, scannedTasks ->

            val readySyncedTasks: List<PrescriptionData.Prescription> = syncedTasks.mapNotNull { task ->
                PrescriptionData.Synced(task).takeIf { it.state is SyncedTask.Ready }
            }

            val notReadySyncedTasks: List<PrescriptionData.Prescription> = syncedTasks.mapNotNull { task ->
                PrescriptionData.Synced(task).takeIf { it.state !is SyncedTask.Ready }
            }

            // NOTE: State cannot be checked for scanned prescriptions, so they are always included in the result

            return@combine PrescriptionReadinessResult(
                // including the scanned tasks in the result if they are present
                readyPrescriptions = readySyncedTasks.toList() + scannedTasks.map { PrescriptionData.Scanned(it) },
                notReadyPrescriptions = notReadySyncedTasks
            )
        }
            .flowOn(dispatcher)
}
