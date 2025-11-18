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

import de.gematik.ti.erp.app.Requirement
import de.gematik.ti.erp.app.database.realm.utils.deleteAll
import de.gematik.ti.erp.app.database.realm.utils.queryFirst
import de.gematik.ti.erp.app.database.realm.utils.safeWrite
import de.gematik.ti.erp.app.database.realm.utils.toRealmInstant
import de.gematik.ti.erp.app.database.realm.utils.tryWrite
import de.gematik.ti.erp.app.database.realm.v1.ProfileEntityV1
import de.gematik.ti.erp.app.database.realm.v1.task.entity.CommunicationEntityV1
import de.gematik.ti.erp.app.database.realm.v1.task.entity.CommunicationProfileV1
import de.gematik.ti.erp.app.database.realm.v1.task.entity.ScannedTaskEntityV1
import de.gematik.ti.erp.app.database.realm.v1.task.entity.SyncedTaskEntityV1
import de.gematik.ti.erp.app.fhir.FhirCommunicationBundleErpModel
import de.gematik.ti.erp.app.fhir.communication.model.FhirDispenseCommunicationEntryErpModel
import de.gematik.ti.erp.app.fhir.communication.model.FhirReplyCommunicationEntryErpModel
import de.gematik.ti.erp.app.messages.mapper.CommunicationDatabaseMappers.toDatabaseModel
import de.gematik.ti.erp.app.prescription.model.ScannedTaskData
import de.gematik.ti.erp.app.prescription.model.SyncedTaskData
import de.gematik.ti.erp.app.profile.repository.ProfileIdentifier
import io.realm.kotlin.MutableRealm
import io.realm.kotlin.Realm
import io.realm.kotlin.ext.query
import io.realm.kotlin.query.Sort
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant

class PrescriptionLocalDataSource(
    private val realm: Realm
) {
    suspend fun saveScannedTasks(
        profileId: ProfileIdentifier,
        tasks: List<ScannedTaskData.ScannedTask>,
        medicationString: String
    ) {
        realm.safeWrite {
            queryFirst<ProfileEntityV1>("id = $0", profileId)?.let { profile ->
                val numberOfUnnamedScannedTasks = query<ScannedTaskEntityV1>(
                    "parent = $0 AND name CONTAINS $1",
                    profile,
                    medicationString
                ).count().find().toInt()

                tasks.forEachIndexed { idx, task ->
                    if (query<ProfileEntityV1>(
                            "syncedTasks.taskId = $0 OR scannedTasks.taskId = $0",
                            task.taskId
                        ).count().find() == 0L
                    ) {
                        profile.scannedTasks += copyToRealm(
                            ScannedTaskEntityV1().apply {
                                this.index = task.index + 1
                                this.name = task.name.ifEmpty { "$medicationString ${numberOfUnnamedScannedTasks + idx + 1}" }
                                this.parent = profile
                                this.taskId = task.taskId
                                this.accessCode = task.accessCode
                                this.scannedOn = Clock.System.now().toRealmInstant()
                                this.redeemedOn = task.redeemedOn?.toRealmInstant()
                            }
                        )
                    }
                }
            }
        }
    }

    fun loadSyncedTasks(profileId: ProfileIdentifier): Flow<List<SyncedTaskData.SyncedTask>> =
        realm.query<SyncedTaskEntityV1>("parent.id = $0", profileId)
            .asFlow()
            .map { syncedTasks ->
                syncedTasks.list.map { syncedTask ->
                    syncedTask.toSyncedTask()
                }
            }

    fun loadSyncedTasksByTaskIds(taskIds: List<String>): Flow<List<SyncedTaskData.SyncedTask>> =
        combine(taskIds.map { loadSyncedTaskByTaskId(it) }) { tasks ->
            tasks.filterNotNull().toList()
        }

    fun loadScannedTasksByTaskIds(taskIds: List<String>): Flow<List<ScannedTaskData.ScannedTask>> =
        combine(taskIds.map { loadScannedTaskByTaskId(it) }) { tasks ->
            tasks.filterNotNull().toList()
        }

    fun loadSyncedTaskByTaskId(taskId: String): Flow<SyncedTaskData.SyncedTask?> =
        realm.query<SyncedTaskEntityV1>("taskId = $0", taskId)
            .first()
            .asFlow()
            .map { syncedTask ->
                syncedTask.obj?.toSyncedTask()
            }

    fun loadScannedTaskByTaskId(taskId: String): Flow<ScannedTaskData.ScannedTask?> =
        realm.query<ScannedTaskEntityV1>("taskId = $0", taskId)
            .first()
            .asFlow()
            .map { scannedTask ->
                scannedTask.obj?.toScannedTask()
            }

    suspend fun saveCommunications(communicationModel: FhirCommunicationBundleErpModel): Int =
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

    private fun MutableRealm.saveCommunicationToDatabase(communication: CommunicationEntityV1): Int {
        val syncedTask = queryFirst<SyncedTaskEntityV1>("taskId = $0", communication.taskId) ?: return 0
        communication.parent = syncedTask
        syncedTask.communications += copyToRealm(communication)
        return 1
    }

    // ToDo: The Pharmacy-ID should be saved as recipient, and this should then be migrated correctly
    suspend fun saveLocalCommunication(taskId: String, pharmacyId: String, transactionId: String) {
        realm.tryWrite {
            val entity = CommunicationEntityV1().apply {
                this.profile = CommunicationProfileV1.ErxCommunicationDispReq
                this.taskId = taskId
                this.communicationId = transactionId
                this.sentOn = Clock.System.now().toRealmInstant()
                this.sender = pharmacyId
                this.consumed = false
            }

            queryFirst<ScannedTaskEntityV1>("taskId = $0", taskId)?.let { scannedTask ->
                scannedTask.communications += copyToRealm(entity)
            }
        }
    }

    fun loadScannedTasks(profileId: ProfileIdentifier): Flow<List<ScannedTaskData.ScannedTask>> =
        realm.query<ScannedTaskEntityV1>("parent.id = $0", profileId)
            .sort("scannedOn", Sort.DESCENDING)
            .sort("index", Sort.ASCENDING)
            .asFlow()
            .map { scannedTasks ->
                scannedTasks.list.map { task ->
                    task.toScannedTask()
                }
            }

    @Requirement(
        "A_19229-01#2",
        sourceSpecification = "gemSpec_eRp_FdV",
        rationale = "User can delete a locally stored prescription and all its linked resources."
    )
    suspend fun deleteTask(taskId: String) {
        realm.tryWrite<Unit> {
            queryFirst<ScannedTaskEntityV1>("taskId = $0", taskId)?.let { deleteAll(it) }
            queryFirst<SyncedTaskEntityV1>("taskId = $0", taskId)?.let { deleteAll(it) }
        }
    }

    suspend fun updateRedeemedOn(taskId: String, timestamp: Instant?) {
        realm.tryWrite<Unit> {
            queryFirst<ScannedTaskEntityV1>("taskId = $0", taskId)?.apply {
                this.redeemedOn = timestamp?.toRealmInstant()
            }
        }
    }

    suspend fun updateScannedTaskName(taskId: String, name: String) {
        realm.tryWrite<Unit> {
            queryFirst<ScannedTaskEntityV1>("taskId = $0", taskId)?.apply {
                this.name = name
            }
        }
    }

    fun loadTaskIds(): Flow<List<String>> =
        combine(
            realm.query<SyncedTaskEntityV1>().asFlow(),
            realm.query<ScannedTaskEntityV1>().asFlow()
        ) { syncedTasks, scannedTasks ->
            syncedTasks.list.map { it.taskId } + scannedTasks.list.map { it.taskId }
        }

    fun loadAllTaskIds(profileId: ProfileIdentifier): Flow<List<String>> =
        combine(
            realm.query<SyncedTaskEntityV1>("parent.id = $0", profileId).asFlow(),
            realm.query<ScannedTaskEntityV1>("parent.id = $0", profileId).asFlow()
        ) { syncedTasks, scannedTasks ->
            syncedTasks.list.map { it.taskId } + scannedTasks.list.map { it.taskId }
        }

    fun wasProfileEverAuthenticated(profileId: ProfileIdentifier): Boolean =
        realm.queryFirst<ProfileEntityV1>("id = $0", profileId)?.let { profile ->
            profile.lastAuthenticated != null
        } ?: false

    suspend fun redeemScannedTasks(taskIds: List<String>) {
        realm.tryWrite {
            taskIds.forEach { taskId ->
                queryFirst<ScannedTaskEntityV1>("taskId = $0", taskId)?.let {
                    it.redeemedOn = Clock.System.now().toRealmInstant()
                }
            }
        }
    }
}
