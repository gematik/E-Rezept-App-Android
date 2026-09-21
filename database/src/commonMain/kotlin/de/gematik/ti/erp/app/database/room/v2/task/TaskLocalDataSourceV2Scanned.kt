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

package de.gematik.ti.erp.app.database.room.v2.task

import de.gematik.ti.erp.app.communication.model.CommunicationProfileV1.ErxCommunicationDispReq
import de.gematik.ti.erp.app.database.api.task.TaskLocalDataSourceScanned
import de.gematik.ti.erp.app.database.room.v2.task.communication.CommunicationDao
import de.gematik.ti.erp.app.database.room.v2.task.communication.ErpCommunicationEntity
import de.gematik.ti.erp.app.database.room.v2.task.mappers.toErpModel
import de.gematik.ti.erp.app.database.room.v2.task.prescription.ErpTaskDao
import de.gematik.ti.erp.app.database.room.v2.task.prescription.ErpTaskWithRefsDao
import de.gematik.ti.erp.app.database.room.v2.task.prescription.TaskTypeValues
import de.gematik.ti.erp.app.profile.repository.ProfileIdentifier
import de.gematik.ti.erp.app.task.model.TaskErpModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant

internal class TaskLocalDataSourceV2Scanned(
    taskDao: ErpTaskDao,
    taskWithRefsDao: ErpTaskWithRefsDao,
    private val communicationDao: CommunicationDao
) : TaskLocalDataSourceV2Base(taskDao, taskWithRefsDao), TaskLocalDataSourceScanned {

    override suspend fun redeemScannedTaskListByTaskIdList(taskIds: List<String>) {
        if (taskIds.isEmpty()) return
        taskDao.markAsRedeemed(taskIds, Clock.System.now())
    }

    override suspend fun updateScannedTaskRedeemedOn(taskId: String, timestamp: Instant?) {
        taskDao.updateRedeemedOn(taskId, timestamp)
    }

    override suspend fun saveScannedTaskList(
        profileId: ProfileIdentifier,
        tasks: List<TaskErpModel.Scanned>,
        medicationString: String
    ) {
        val newTasks = tasks.filter { taskDao.getByTaskId(it.taskId) == null }
        if (newTasks.isEmpty()) return
        val unnamedCount = taskDao.getByProfile(profileId).count {
            it.taskType == TaskTypeValues.SCANNED && it.name?.startsWith(medicationString) == true
        }
        val entities = newTasks.mapIndexed { idx, task ->
            buildScannedEntity(task, profileId, "$medicationString ${unnamedCount + idx + 1}")
        }
        taskDao.upsertAll(entities)
    }

    override suspend fun updateScannedTaskName(taskId: String, name: String) {
        taskDao.updateName(taskId, name)
    }

    override fun loadScannedPrescriptionListByProfileId(profileId: ProfileIdentifier): Flow<List<TaskErpModel.Scanned>> =
        taskWithRefsDao.observeScannedByProfile(profileId).map { list ->
            list.mapNotNull { runCatching { it.toErpModel() as? TaskErpModel.Scanned }.getOrNull() }
        }

    override fun loadScannedTaskListByTaskIdList(taskIds: List<String>): Flow<List<TaskErpModel.Scanned>> {
        if (taskIds.isEmpty()) return flowOf(emptyList())
        return taskWithRefsDao.observeScannedByTaskIds(taskIds).map { list ->
            list.mapNotNull { runCatching { it.toErpModel() as? TaskErpModel.Scanned }.getOrNull() }
        }
    }

    override fun loadScannedPrescriptionByTaskId(taskId: String): Flow<TaskErpModel.Scanned?> =
        taskWithRefsDao.observeWithRefs(taskId).map {
            it?.let { runCatching { it.toErpModel() as? TaskErpModel.Scanned }.getOrNull() }
        }

    override suspend fun saveCommunicationForScannedTask(taskId: String, pharmacyId: String, transactionId: String) {
        val task = taskDao.getByTaskId(taskId) ?: return
        communicationDao.upsertAll(
            listOf(
                ErpCommunicationEntity(
                    communicationId = transactionId,
                    taskId = taskId,
                    orderId = "",
                    profileId = task.parentProfileId ?: "",
                    telematikId = pharmacyId,
                    kvnr = "",
                    consumed = false,
                    payload = "",
                    profile = ErxCommunicationDispReq,
                    insuranceId = null,
                    timeStamp = Clock.System.now()
                )
            )
        )
    }
}
