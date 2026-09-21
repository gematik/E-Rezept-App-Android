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

package de.gematik.ti.erp.app.demomode.repository.diga

import de.gematik.ti.erp.app.demomode.datasource.DemoModeDataSource
import de.gematik.ti.erp.app.demomode.datasource.INDEX_OUT_OF_BOUNDS
import de.gematik.ti.erp.app.diga.model.DigaStatus
import de.gematik.ti.erp.app.diga.repository.DigaRepository
import de.gematik.ti.erp.app.fhir.temporal.FhirTemporal
import de.gematik.ti.erp.app.profile.repository.ProfileIdentifier
import de.gematik.ti.erp.app.task.model.TaskErpModel
import de.gematik.ti.erp.app.task.model.TaskStatusEnum
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.updateAndGet
import kotlinx.coroutines.withContext
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant

class DemoDigaRepository(
    private val dataSource: DemoModeDataSource,
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO
) : DigaRepository {

    /**
     * Atomically finds the Diga synced task with [taskId], applies [transform] to it,
     * and writes back the modified list on [dispatcher].
     */
    private suspend fun updateTask(
        taskId: String,
        transform: TaskErpModel.Synced.Diga.() -> TaskErpModel.Synced.Diga
    ) {
        withContext(dispatcher) {
            dataSource.syncedTasks.value = dataSource.syncedTasks.updateAndGet { list ->
                val idx = list.indexOfFirst { it.taskId == taskId }
                if (idx != INDEX_OUT_OF_BOUNDS) {
                    val task = list[idx]
                    if (task is TaskErpModel.Synced.Diga) {
                        list[idx] = task.transform()
                    }
                }
                list
            }
        }
    }

    override suspend fun updateDigaAsSeen(taskId: String) {
        updateTask(taskId) {
            copy(deviceRequest = deviceRequest?.copy(isNew = false))
        }
    }

    override suspend fun updateDigaStatus(taskId: String, status: DigaStatus, lastModified: Instant?) {
        updateTask(taskId) {
            copy(
                deviceRequest = deviceRequest?.copy(userActionState = status),
                lastModified = lastModified ?: Clock.System.now()
            )
        }
    }

    override suspend fun updateDigaCommunicationSent(taskId: String, time: Instant) {
        updateTask(taskId) {
            copy(deviceRequest = deviceRequest?.copy(sentOn = FhirTemporal.Instant(time)))
        }
    }

    override fun loadDigaByTaskId(taskId: String): Flow<TaskErpModel.Synced.Diga?> {
        return dataSource.syncedTasks.map {
            it.filterIsInstance<TaskErpModel.Synced.Diga>().firstOrNull { diga -> diga.taskId == taskId }
        }.flowOn(dispatcher)
    }

    override suspend fun updateArchiveStatus(taskId: String, lastModified: Instant, setArchiveStatus: Boolean) {
        updateTask(taskId) {
            copy(
                deviceRequest = deviceRequest?.copy(isArchived = setArchiveStatus),
                status = if (setArchiveStatus) TaskStatusEnum.Completed else status,
                lastModified = lastModified
            )
        }
    }

    override suspend fun loadDigasByProfileId(profileId: ProfileIdentifier): Flow<List<TaskErpModel.Synced.Diga>> {
        return dataSource.syncedTasks.map {
            it.filterIsInstance<TaskErpModel.Synced.Diga>().filter { diga -> diga.profileId == profileId }
        }.flowOn(dispatcher)
    }

    override fun loadArchiveDigasByProfileId(profileId: ProfileIdentifier): Flow<List<TaskErpModel.Synced.Diga>> {
        return dataSource.syncedTasks.map {
            it.filterIsInstance<TaskErpModel.Synced.Diga>()
                .filter { diga -> diga.profileId == profileId && diga.deviceRequest?.isArchived == true }
        }.flowOn(dispatcher)
    }
}
