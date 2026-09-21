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

package de.gematik.ti.erp.app.prescription.usecase

import de.gematik.ti.erp.app.prescription.mapper.filterActiveDigaTasks
import de.gematik.ti.erp.app.prescription.mapper.filterActivePrescriptionTasks
import de.gematik.ti.erp.app.prescription.mapper.flatMapToBaseErpModel
import de.gematik.ti.erp.app.prescription.mapper.groupByHospitalsOrDoctors
import de.gematik.ti.erp.app.prescription.mapper.sortByExpiredDateAndAuthoredDate
import de.gematik.ti.erp.app.prescription.repository.TaskOperationsRepository
import de.gematik.ti.erp.app.profile.repository.ProfileIdentifier
import de.gematik.ti.erp.app.task.model.TaskErpModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOn

/**
 * The prescription [repository] obtains the active
 * scanned and synced prescriptions and sorts them
 * by the authored or scanned date in a descending order and then
 * by name.
 *
 */
class GetActivePrescriptionsUseCase(
    private val repository: TaskOperationsRepository,
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO
) {
    operator fun invoke(id: ProfileIdentifier): Flow<List<TaskErpModel>> = combine(
        repository.loadScannedTaskListByProfileId(id),
        repository.loadSyncedTaskListByProfileId(id),
        repository.loadDigaTaskListByProfileId(id)
    ) { scannedTasks, syncedTasks, digas ->
        val scannedPrescriptions = scannedTasks
            .filterActivePrescriptionTasks()
            .sortedBy { it.scannedOn }

        val activeSyncedTasks = syncedTasks.filterActivePrescriptionTasks()

        val activeDigas = digas.filterActiveDigaTasks()

        val syncedPrescriptionsAndDigas = buildList<TaskErpModel.Synced> {
            addAll(activeDigas)
            addAll(activeSyncedTasks)
        }.sortByExpiredDateAndAuthoredDate()
            .groupByHospitalsOrDoctors()
            .flatMapToBaseErpModel()

        val syncedTaskIds = syncedPrescriptionsAndDigas.map { it.taskId }.toSet()
        val filteredScannedPrescriptions = scannedPrescriptions.filter { it.taskId !in syncedTaskIds }

        (filteredScannedPrescriptions + syncedPrescriptionsAndDigas).sortActives()
    }.flowOn(dispatcher)

    companion object {

        private fun List<TaskErpModel>.sortActives() =
            sortedWith(compareByDescending<TaskErpModel> { it.startedOn }.thenBy { it.name })
    }
}
