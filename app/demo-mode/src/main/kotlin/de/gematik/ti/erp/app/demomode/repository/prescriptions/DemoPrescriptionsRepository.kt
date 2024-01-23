/*
 * Copyright (c) 2024 gematik GmbH
 * 
 * Licensed under the EUPL, Version 1.2 or – as soon they will be approved by
 * the European Commission - subsequent versions of the EUPL (the Licence);
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at:
 * 
 *     https://joinup.ec.europa.eu/software/page/eupl
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the Licence is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Licence for the specific language governing permissions and
 * limitations under the Licence.
 * 
 */

package de.gematik.ti.erp.app.demomode.repository.prescriptions

import de.gematik.ti.erp.app.demomode.datasource.DemoModeDataSource
import de.gematik.ti.erp.app.demomode.datasource.INDEX_OUT_OF_BOUNDS
import de.gematik.ti.erp.app.demomode.model.DemoModeSentCommunicationJson
import de.gematik.ti.erp.app.demomode.model.toDemoModeProfileLinkedCommunication
import de.gematik.ti.erp.app.prescription.model.ScannedTaskData.ScannedTask
import de.gematik.ti.erp.app.prescription.model.SyncedTaskData.SyncedTask
import de.gematik.ti.erp.app.prescription.repository.PrescriptionRepository
import de.gematik.ti.erp.app.profiles.repository.ProfileIdentifier
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.flow.updateAndGet
import kotlinx.coroutines.withContext
import kotlinx.datetime.Instant
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.decodeFromJsonElement

class DemoPrescriptionsRepository(
    private val dataSource: DemoModeDataSource,
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO
) : PrescriptionRepository {
    override suspend fun saveScannedTasks(
        profileId: ProfileIdentifier,
        tasks: List<ScannedTask>
    ) {
        withContext(dispatcher) {
            dataSource.scannedTasks.value = dataSource.scannedTasks.updateAndGet {
                val scannedList = it.toMutableList()
                scannedList.addAll(tasks)
                scannedList
            }
        }
    }

    override fun scannedTasks(profileId: ProfileIdentifier): Flow<List<ScannedTask>> = dataSource.scannedTasks

    override fun syncedTasks(profileId: ProfileIdentifier): Flow<List<SyncedTask>> =
        dataSource.syncedTasks.mapNotNull { taskList ->
            taskList.filter { it.profileId == profileId }.sortedBy { it.lastModified }
        }.flowOn(dispatcher)


    override suspend fun redeemPrescription(
        profileId: ProfileIdentifier,
        communication: JsonElement,
        accessCode: String?
    ): Result<Unit> =
        withContext(dispatcher) {
            val decodedCommunication = Json
                .decodeFromJsonElement<DemoModeSentCommunicationJson>(communication)
                .toDemoModeProfileLinkedCommunication(profileId)
            dataSource.communications.value = dataSource.communications.updateAndGet { communications ->
                communications.add(decodedCommunication)
                communications
            }
            Result.success(Unit)
        }

    override suspend fun deleteTaskByTaskId(profileId: ProfileIdentifier, taskId: String): Result<Unit> =
        withContext(dispatcher) {
            dataSource.syncedTasks.value = dataSource.syncedTasks.updateAndGet { syncedList ->
                syncedList.removeIf { it.taskId == taskId && it.profileId == profileId }
                syncedList
            }
            dataSource.scannedTasks.value = dataSource.scannedTasks.updateAndGet {
                val scannedList = it.toMutableList()
                scannedList.removeIf { scannedItem -> scannedItem.taskId == taskId && scannedItem.profileId == profileId }
                scannedList
            }
            Result.success(Unit)
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
}
