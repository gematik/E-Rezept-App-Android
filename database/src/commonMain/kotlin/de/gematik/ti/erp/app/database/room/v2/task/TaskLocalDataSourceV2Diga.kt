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

import de.gematik.ti.erp.app.database.api.task.TaskLocalDataSourceDiga
import de.gematik.ti.erp.app.database.room.v2.task.mappers.toErpModel
import de.gematik.ti.erp.app.database.room.v2.task.prescription.ErpTaskDao
import de.gematik.ti.erp.app.database.room.v2.task.prescription.ErpTaskWithRefsDao
import de.gematik.ti.erp.app.diga.model.DigaStatus
import de.gematik.ti.erp.app.profile.repository.ProfileIdentifier
import de.gematik.ti.erp.app.task.model.TaskErpModel
import io.github.aakira.napier.Napier
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.datetime.Instant

internal class TaskLocalDataSourceV2Diga(
    taskDao: ErpTaskDao,
    taskWithRefsDao: ErpTaskWithRefsDao
) : TaskLocalDataSourceV2Base(taskDao, taskWithRefsDao), TaskLocalDataSourceDiga {

    override suspend fun setDigaAsNotNew(taskId: String) {
        // update the device request 'isNew' flag when present
        taskDao.setDeviceRequestIsNewFalseByTaskId(taskId)
    }

    override suspend fun updateDigaStatus(taskId: String, status: DigaStatus, lastModified: Instant?) {
        // Update lastModified on the task (if provided)
        lastModified?.let { taskDao.updateLastModified(taskId, it) }

        // Store the user action step in the dedicated column and status string as best-effort
        try {
            taskDao.updateDeviceRequestUserActionStateByTaskId(taskId, status.step)
        } catch (e: Exception) {
            Napier.e(e) { "Error updating device request userActionState for task $taskId" }
        }

        try {
            taskDao.updateDeviceRequestStatusByTaskId(taskId, status.step.toString())
        } catch (e: Exception) {
            Napier.e(e) { "Error updating device request status for task $taskId" }
        }

        // TODO: check if correct (dinesh)
        // If the status carries a sentOn timestamp, persist it into sentCommunicationOn (best-effort)
        if (status is DigaStatus.InProgress) {
            status.sentOn?.let { sentOn ->
                try {
                    taskDao.updateDeviceRequestSentCommunicationOnByTaskId(taskId, sentOn)
                } catch (e: Exception) {
                    Napier.e(e) { "Error updating device request sentCommunicationOn for task $taskId" }
                }
            }
        }
    }

    override suspend fun updateDigaArchiveStatus(taskId: String, lastModified: Instant, isArchive: Boolean) {
        // Update last modified and mark device request status string to indicate archive state
        taskDao.updateLastModified(taskId, lastModified)
        try {
            taskDao.updateDeviceRequestStatusByTaskId(taskId, if (isArchive) "archived" else "active")
        } catch (_: Exception) {
        }
    }

    override suspend fun updateDigaCommunicationSent(taskId: String, time: Instant) {
        // Persist communication time into sentCommunicationOn if available
        try {
            taskDao.updateDeviceRequestSentCommunicationOnByTaskId(taskId, time)
        } catch (_: Exception) {
        }
    }

    override fun loadDigaByTaskId(taskId: String): Flow<TaskErpModel.Synced.Diga?> =
        taskWithRefsDao.observeWithRefs(taskId).map { withRefs ->
            withRefs?.takeIf { it.deviceRequest?.pzn?.isNotEmpty() == true }?.let {
                runCatching { it.toErpModel() as? TaskErpModel.Synced.Diga }.getOrNull()
            }
        }

    override fun loadDigaListByProfileId(profileId: ProfileIdentifier): Flow<List<TaskErpModel.Synced.Diga>> =
        taskWithRefsDao.observeAllWithRefsByProfile(profileId).map { list ->
            list.filter { it.deviceRequest?.pzn?.isNotEmpty() == true }
                .mapNotNull { runCatching { it.toErpModel() as? TaskErpModel.Synced.Diga }.getOrNull() }
        }

    override fun loadArchiveDigaListByProfileId(profileId: ProfileIdentifier): Flow<List<TaskErpModel.Synced.Diga>> =
        taskWithRefsDao.observeAllWithRefsByProfile(profileId).map { list ->
            list.filter { it.deviceRequest?.pzn?.isNotEmpty() == true && it.deviceRequest.status == "archived" }
                .mapNotNull { runCatching { it.toErpModel() as? TaskErpModel.Synced.Diga }.getOrNull() }
        }
}
