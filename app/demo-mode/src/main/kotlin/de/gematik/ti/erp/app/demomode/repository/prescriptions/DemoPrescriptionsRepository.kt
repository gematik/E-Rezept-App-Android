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

package de.gematik.ti.erp.app.demomode.repository.prescriptions

import de.gematik.ti.erp.app.demomode.datasource.DemoModeDataSource
import de.gematik.ti.erp.app.demomode.datasource.INDEX_OUT_OF_BOUNDS
import de.gematik.ti.erp.app.demomode.model.DemoModeSentCommunicationJson
import de.gematik.ti.erp.app.demomode.model.emptyDemoModeProfileLinkedCommunication
import de.gematik.ti.erp.app.demomode.model.toDemoModeProfileLinkedCommunication
import de.gematik.ti.erp.app.prescription.model.ScannedTaskData.ScannedTask
import de.gematik.ti.erp.app.prescription.model.SyncedTaskData
import de.gematik.ti.erp.app.prescription.model.SyncedTaskData.SyncedTask
import de.gematik.ti.erp.app.prescription.repository.PrescriptionRepository
import de.gematik.ti.erp.app.profiles.repository.ProfileIdentifier
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.flow.updateAndGet
import kotlinx.coroutines.withContext
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.decodeFromJsonElement

class DemoPrescriptionsRepository(
    private val dataSource: DemoModeDataSource,
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO
) : PrescriptionRepository {
    override suspend fun saveScannedTasks(
        profileId: ProfileIdentifier,
        tasks: List<ScannedTask>,
        medicationString: String
    ) {
        val updatedTasksWithNames = tasks.mapIndexed { index, scannedTask ->
            scannedTask.copy(name = "$medicationString ${index + 1}")
        }
        withContext(dispatcher) {
            dataSource.scannedTasks.value = dataSource.scannedTasks.updateAndGet {
                val scannedList = it.toMutableList()
                scannedList.addAll(updatedTasksWithNames)
                scannedList
            }
        }
    }

    override fun scannedTasks(profileId: ProfileIdentifier): Flow<List<ScannedTask>> = dataSource.scannedTasks
        .map { list -> list.filter { it.profileId == profileId } }

    override fun syncedTasks(profileId: ProfileIdentifier): Flow<List<SyncedTask>> =
        dataSource.syncedTasks.mapNotNull { taskList ->
            taskList.filter { it.profileId == profileId }.sortedBy { it.lastModified }
        }.flowOn(dispatcher)

    override suspend fun redeem(
        profileId: ProfileIdentifier,
        communication: JsonElement,
        accessCode: String
    ): Result<JsonElement> =
        withContext(dispatcher) {
            val decodedCommunication = runCatching {
                Json
                    .decodeFromJsonElement<DemoModeSentCommunicationJson>(communication)
                    .toDemoModeProfileLinkedCommunication(profileId)
            }.getOrElse {
                emptyDemoModeProfileLinkedCommunication(profileId)
            }
            dataSource.communications.value = dataSource.communications.updateAndGet { communications ->
                communications.add(decodedCommunication)
                communications
            }
            // change the status of the prescription to in progress
            dataSource.syncedTasks.value = dataSource.syncedTasks.updateAndGet { syncedList ->
                val index = syncedList.indexOfFirst { profileId == it.profileId && it.taskId == decodedCommunication.taskId }
                if (index != INDEX_OUT_OF_BOUNDS) {
                    val updatedItem = syncedList[index].copy(
                        status = SyncedTaskData.TaskStatus.InProgress,
                        lastModified = Clock.System.now()
                    )
                    syncedList[index] = updatedItem
                }
                syncedList
            }
            dataSource.scannedTasks.value = dataSource.scannedTasks.updateAndGet { scannedList ->
                val index = scannedList.indexOfFirst { profileId == it.profileId && it.taskId == decodedCommunication.taskId }
                if (index != INDEX_OUT_OF_BOUNDS) {
                    val updatedItem = scannedList[index].copy(
                        redeemedOn = Clock.System.now()
                    )
                    scannedList[index] = updatedItem
                }
                scannedList
            }
            Result.success(JsonPrimitive(true)) // sending some random json response
        }

    override suspend fun deleteRemoteTaskById(
        profileId: ProfileIdentifier,
        taskId: String
    ): Result<JsonElement?> =
        withContext(dispatcher) {
            dataSource.syncedTasks.value = dataSource.syncedTasks.updateAndGet { syncedList ->
                syncedList.removeIf { it.taskId == taskId && it.profileId == profileId }
                syncedList
            }
            dataSource.scannedTasks.value = dataSource.scannedTasks.updateAndGet {
                val scannedList = it.toMutableList()
                scannedList
                    .removeIf { scannedItem -> scannedItem.taskId == taskId && scannedItem.profileId == profileId }
                scannedList
            }
            Result.success(null)
        }

    // used only for scanned
    override suspend fun updateRedeemedOn(taskId: String, timestamp: Instant?) {
        withContext(dispatcher) {
            dataSource.scannedTasks.value = dataSource.scannedTasks.updateAndGet {
                val scannedList = it.toMutableList()
                val index = scannedList.indexOfFirst { item -> item.taskId == taskId }
                if (index != INDEX_OUT_OF_BOUNDS) {
                    scannedList[index] = scannedList[index].copy(redeemedOn = timestamp)
                }
                scannedList
            }
        }
    }

    override suspend fun updateScannedTaskName(taskId: String, name: String) {
        withContext(dispatcher) {
            dataSource.scannedTasks.value = dataSource.scannedTasks.updateAndGet {
                val scannedList = it.toMutableList()
                val index = scannedList.indexOfFirst { item -> item.taskId == taskId }
                if (index != INDEX_OUT_OF_BOUNDS) {
                    scannedList[index] = scannedList[index].copy(name = name)
                }
                scannedList
            }
        }
    }

    override fun loadSyncedTaskByTaskId(taskId: String) =
        dataSource.syncedTasks.mapNotNull { list ->
            list.find { it.taskId == taskId }
        }.flowOn(dispatcher)

    override fun loadSyncedTasksByTaskIds(taskIds: List<String>): Flow<List<SyncedTask>> =
        dataSource.syncedTasks
            .map { list -> list.filter { task -> task.taskId in taskIds } }
            .flowOn(dispatcher)

    override fun loadScannedTasksByTaskIds(taskIds: List<String>): Flow<List<ScannedTask>> {
        return dataSource.scannedTasks.map { list ->
            list.filter { task -> task.taskId in taskIds }
        }.flowOn(dispatcher)
    }

    override fun loadScannedTaskByTaskId(taskId: String) =
        dataSource.scannedTasks.mapNotNull { list ->
            list.find { it.taskId == taskId }
        }.flowOn(dispatcher)

    override fun loadTaskIds() =
        combine(
            dataSource.scannedTasks,
            dataSource.syncedTasks
        ) { scannedTasks, syncedTasks ->
            scannedTasks.mapNotNull { it.taskId }.plus(
                syncedTasks.mapNotNull { it.taskId }
            )
        }.flowOn(dispatcher)

    override suspend fun deleteLocalTaskById(taskId: String) {
        // do nothing
    }

    override suspend fun wasProfileEverAuthenticated(profileId: ProfileIdentifier): Boolean {
        return true
    }

    override suspend fun redeemScannedTasks(taskIds: List<String>) {
        // do nothing
    }

    override fun loadAllTaskIds(profileId: ProfileIdentifier): Flow<List<String>> = loadTaskIds()
}
