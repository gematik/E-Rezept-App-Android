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

package de.gematik.ti.erp.app.database.realm.v1.task.datasource

import de.gematik.ti.erp.app.database.api.task.TaskLocalDataSourceCommon
import de.gematik.ti.erp.app.database.realm.utils.deleteAll
import de.gematik.ti.erp.app.database.realm.utils.queryFirst
import de.gematik.ti.erp.app.database.realm.utils.safeWrite
import de.gematik.ti.erp.app.database.realm.v1.messages.mapper.CommunicationDatabaseMappers.toDatabaseModel
import de.gematik.ti.erp.app.database.realm.v1.task.entity.CommunicationEntityV1
import de.gematik.ti.erp.app.database.realm.v1.task.entity.ScannedTaskEntityV1
import de.gematik.ti.erp.app.database.realm.v1.task.entity.SyncedTaskEntityV1
import de.gematik.ti.erp.app.fhir.FhirCommunicationBundleErpModel
import de.gematik.ti.erp.app.fhir.communication.model.FhirDispenseCommunicationEntryErpModel
import de.gematik.ti.erp.app.fhir.communication.model.FhirReplyCommunicationEntryErpModel
import de.gematik.ti.erp.app.profile.repository.ProfileIdentifier
import de.gematik.ti.erp.app.task.model.TaskErpModel
import io.realm.kotlin.MutableRealm
import io.realm.kotlin.Realm
import io.realm.kotlin.ext.query
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map

internal class TaskLocalDataSourceV1Common(realm: Realm) : TaskLocalDataSourceV1Base(realm), TaskLocalDataSourceCommon {

    override fun loadTaskListByProfileId(profileId: ProfileIdentifier): Flow<List<TaskErpModel>> {
        val syncedTasks = loadSyncedTasksByProfileId(profileId)
        val scannedTasks = loadScannedTasksByProfileId(profileId)
        return combine(syncedTasks, scannedTasks) { synced: List<TaskErpModel>, scanned: List<TaskErpModel> ->
            (synced + scanned).distinctBy { it.taskId }
        }
    }

    override fun loadTaskListByTaskIdList(taskIds: List<String>): Flow<List<TaskErpModel>> {
        if (taskIds.isEmpty()) return flowOf(emptyList())

        val syncedFlows = taskIds.map { id -> loadSyncedTaskByTaskId(id) }
        val scannedFlows = taskIds.map { id -> loadScannedTaskByTaskId(id) }

        return combine(syncedFlows + scannedFlows) { results: Array<TaskErpModel?> ->
            results.distinctBy { it?.taskId }.filterNotNull()
        }
    }

    override fun loadTaskByTaskId(taskId: String): Flow<TaskErpModel?> {
        val syncedTask = loadSyncedTaskByTaskId(taskId)
        val scannedTask = loadScannedTaskByTaskId(taskId)
        return combine(syncedTask, scannedTask) { synced, scanned ->
            synced ?: scanned
        }
    }

    override suspend fun deleteTaskByTaskId(taskId: String) {
        realm.safeWrite<Unit> {
            queryFirst<ScannedTaskEntityV1>("taskId = $0", taskId)?.let { deleteAll(it) }
            queryFirst<SyncedTaskEntityV1>("taskId = $0", taskId)?.let { deleteAll(it) }
        }
    }

    override fun loadATaskIdStringList(): Flow<List<String>> {
        return combine(
            realm.query<SyncedTaskEntityV1>().asFlow(),
            realm.query<ScannedTaskEntityV1>().asFlow()
        ) { syncedTasks, scannedTasks ->
            (syncedTasks.list.map { it.taskId } + scannedTasks.list.map { it.taskId })
                .distinctBy { it }
        }
    }

    override fun loadTaskIdStringListByProfileId(profileId: ProfileIdentifier): Flow<List<String>> {
        val syncedIds = realm.query<SyncedTaskEntityV1>("parent.id = $0", profileId)
            .asFlow()
            .map { results -> results.list.map { it.taskId } }
        val scannedIds = realm.query<ScannedTaskEntityV1>("parent.id = $0", profileId)
            .asFlow()
            .map { results -> results.list.map { it.taskId } }
        return combine(syncedIds, scannedIds) { a, b ->
            (a + b).distinct()
        }
    }

    override suspend fun saveCommunications(communicationModel: FhirCommunicationBundleErpModel): Int =
        realm.safeWrite {
            communicationModel.messages.sumOf { message ->
                saveCommunicationToDatabase(
                    when (message) {
                        is FhirReplyCommunicationEntryErpModel -> message.toDatabaseModel()
                        is FhirDispenseCommunicationEntryErpModel -> message.toDatabaseModel()
                    }
                )
            }
        }

    override suspend fun deleteTasksByProfileId(profileId: ProfileIdentifier) {
        realm.safeWrite {
            query<SyncedTaskEntityV1>("parent.id = $0", profileId).find().forEach {
                deleteAll(it)
            }
            query<ScannedTaskEntityV1>("parent.id = $0", profileId).find().forEach {
                deleteAll(it)
            }
        }
    }

    override suspend fun deleteCommunicationsByProfileId(profileId: ProfileIdentifier) {
        realm.safeWrite {
            query<CommunicationEntityV1>("parent.parent.id = $0", profileId).find().forEach {
                delete(it)
            }
        }
    }

    override suspend fun deleteInvoicesByProfileId(profileId: ProfileIdentifier) {
        // Realm V1 schema handles invoices (dispenses) as nested objects in SyncedTaskEntityV1,
        // so they are deleted when tasks are deleted.
    }

    private fun MutableRealm.saveCommunicationToDatabase(communication: CommunicationEntityV1): Int {
        val syncedTask = queryFirst<SyncedTaskEntityV1>("taskId = $0", communication.taskId) ?: return 0
        communication.parent = syncedTask
        syncedTask.communications += copyToRealm(communication)
        return 1
    }
}
