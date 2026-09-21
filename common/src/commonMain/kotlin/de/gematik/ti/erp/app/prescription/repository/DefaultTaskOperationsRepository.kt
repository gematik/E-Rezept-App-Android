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

package de.gematik.ti.erp.app.prescription.repository

import de.gematik.ti.erp.app.database.api.task.TaskLocalDataSource
import de.gematik.ti.erp.app.prescription.remote.PrescriptionRemoteDataSource
import de.gematik.ti.erp.app.profile.repository.ProfileIdentifier
import de.gematik.ti.erp.app.task.model.TaskErpModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.datetime.Instant
import kotlinx.serialization.json.JsonElement

class DefaultTaskOperationsRepository(
    private val localDataSource: TaskLocalDataSource,
    private val remoteDataSource: PrescriptionRemoteDataSource
) : TaskOperationsRepository {

    /**
     * Saves all scanned tasks. It doesn't matter if they already exist.
     */
    override suspend fun saveScannedTaskList(
        profileId: ProfileIdentifier,
        tasks: List<TaskErpModel.Scanned>,
        medicationString: String
    ) {
        localDataSource.saveScannedTaskList(profileId, tasks, medicationString)
    }

    override fun loadScannedTaskListByProfileId(profileId: ProfileIdentifier): Flow<List<TaskErpModel.Scanned>> =
        localDataSource.loadTaskListByProfileId(profileId).map { it.filterIsInstance<TaskErpModel.Scanned>() }

    override fun loadSyncedTaskListByProfileId(profileId: ProfileIdentifier): Flow<List<TaskErpModel.Synced.Prescription>> =
        localDataSource.loadTaskListByProfileId(profileId).map { it.filterIsInstance<TaskErpModel.Synced.Prescription>() }

    override fun loadDigaTaskListByProfileId(profileId: ProfileIdentifier): Flow<List<TaskErpModel.Synced.Diga>> =
        localDataSource.loadTaskListByProfileId(profileId).map { it.filterIsInstance<TaskErpModel.Synced.Diga>() }

    override fun loadTaskByTaskId(taskId: String): Flow<TaskErpModel?> =
        localDataSource.loadTaskByTaskId(taskId)

    override suspend fun redeem(
        profileId: ProfileIdentifier,
        communication: JsonElement,
        accessCode: String
    ): Result<JsonElement> = remoteDataSource.communicate(profileId, communication, accessCode)

    override suspend fun deleteRemoteTaskById(
        profileId: ProfileIdentifier,
        taskId: String
    ): Result<JsonElement?> =
        remoteDataSource.deleteTask(profileId, taskId).map { it }

    override suspend fun updateScannedTaskRedeemedOn(taskId: String, timestamp: Instant?) =
        localDataSource.updateScannedTaskRedeemedOn(taskId, timestamp)

    override suspend fun updateScannedTaskName(taskId: String, name: String) =
        localDataSource.updateScannedTaskName(taskId, name)

    override fun loadSyncedTaskByTaskId(taskId: String): Flow<TaskErpModel.Synced.Prescription?> =
        localDataSource.loadTaskByTaskId(taskId).map { it as? TaskErpModel.Synced.Prescription }

    override fun loadDigaTaskByTaskId(taskId: String): Flow<TaskErpModel.Synced.Diga?> =
        localDataSource.loadTaskByTaskId(taskId).map { it as? TaskErpModel.Synced.Diga }

    override fun loadSyncedTasksByTaskIds(taskIds: List<String>): Flow<List<TaskErpModel.Synced.Prescription>> =
        localDataSource.loadTaskListByTaskIdList(taskIds).map {
            it.filterIsInstance<TaskErpModel.Synced.Prescription>()
        }

    override fun loadScannedTasksByTaskIds(taskIds: List<String>): Flow<List<TaskErpModel.Scanned>> =
        localDataSource.loadTaskListByTaskIdList(taskIds).map {
            it.filterIsInstance<TaskErpModel.Scanned>()
        }

    override fun loadScannedTaskByTaskId(taskId: String): Flow<TaskErpModel.Scanned?> =
        loadTaskByTaskId(taskId).map { it as? TaskErpModel.Scanned }

    override fun loadTaskIds(): Flow<List<String>> = localDataSource.loadATaskIdStringList()

    override suspend fun deleteTaskByTaskIdOnlyInLocalDatabase(taskId: String) {
        localDataSource.deleteTaskByTaskId(taskId)
    }

    override suspend fun redeemScannedTaskListByTaskIdList(taskIds: List<String>) {
        localDataSource.redeemScannedTaskListByTaskIdList(taskIds)
    }

    override fun loadTaskIdStringListByProfileId(profileId: ProfileIdentifier): Flow<List<String>> =
        localDataSource.loadTaskIdStringListByProfileId(profileId)

    override suspend fun deleteTasksByProfileId(profileId: ProfileIdentifier) {
        localDataSource.deleteTasksByProfileId(profileId)
    }

    override suspend fun deleteCommunicationsByProfileId(profileId: ProfileIdentifier) {
        localDataSource.deleteCommunicationsByProfileId(profileId)
    }

    override suspend fun deleteInvoicesByProfileId(profileId: ProfileIdentifier) {
        localDataSource.deleteInvoicesByProfileId(profileId)
    }

    // loadATaskIdStringList(profileId)
}
