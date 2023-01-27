/*
 * Copyright (c) 2023 gematik GmbH
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

package de.gematik.ti.erp.app.prescription.repository

import de.gematik.ti.erp.app.db.entities.deleteAll
import de.gematik.ti.erp.app.db.entities.v1.ProfileEntityV1
import de.gematik.ti.erp.app.db.entities.v1.task.CommunicationEntityV1
import de.gematik.ti.erp.app.db.entities.v1.task.CommunicationProfileV1
import de.gematik.ti.erp.app.db.entities.v1.task.ScannedTaskEntityV1
import de.gematik.ti.erp.app.db.entities.v1.task.SyncedTaskEntityV1
import de.gematik.ti.erp.app.db.queryFirst
import de.gematik.ti.erp.app.db.toRealmInstant
import de.gematik.ti.erp.app.db.tryWrite
import de.gematik.ti.erp.app.fhir.model.CommunicationProfile
import de.gematik.ti.erp.app.fhir.model.extractCommunications
import de.gematik.ti.erp.app.prescription.model.ScannedTaskData
import de.gematik.ti.erp.app.prescription.model.SyncedTaskData
import de.gematik.ti.erp.app.profiles.repository.ProfileIdentifier
import io.realm.kotlin.Realm
import io.realm.kotlin.ext.query
import io.realm.kotlin.query.Sort
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.serialization.json.JsonElement
import java.time.Instant

class LocalDataSource(
    private val realm: Realm
) {
    suspend fun saveScannedTasks(profileId: ProfileIdentifier, tasks: List<ScannedTaskData.ScannedTask>) {
        realm.tryWrite<Unit> {
            queryFirst<ProfileEntityV1>("id = $0", profileId)?.let { profile ->
                tasks.forEach { task ->
                    if (query<ProfileEntityV1>(
                            "syncedTasks.taskId = $0 OR scannedTasks.taskId = $0",
                            task.taskId
                        ).count().find() == 0L
                    ) {
                        profile.scannedTasks += copyToRealm(
                            ScannedTaskEntityV1().apply {
                                this.parent = profile
                                this.taskId = task.taskId
                                this.accessCode = task.accessCode
                                this.scannedOn = task.scannedOn.toRealmInstant()
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

    suspend fun saveCommunications(communications: JsonElement): Int =
        realm.tryWrite {
            val totalCommunicationsInBundle =
                extractCommunications(communications) {
                        taskId, communicationId, orderId, profile, sentOn, sender, recipient, payload ->

                    val entity = CommunicationEntityV1().apply {
                        this.profile = when (profile) {
                            CommunicationProfile.ErxCommunicationDispReq ->
                                CommunicationProfileV1.ErxCommunicationDispReq

                            CommunicationProfile.ErxCommunicationReply ->
                                CommunicationProfileV1.ErxCommunicationReply
                        }
                        this.taskId = taskId
                        this.communicationId = communicationId
                        this.orderId = orderId ?: ""
                        this.sentOn = sentOn.toRealmInstant()
                        this.sender = sender
                        this.recipient = recipient
                        this.payload = payload
                        this.consumed = false
                    }

                    queryFirst<SyncedTaskEntityV1>("taskId = $0", taskId)?.let { syncedTask ->
                        entity.parent = syncedTask
                        syncedTask.communications += copyToRealm(entity)
                    }
                }

            totalCommunicationsInBundle
        }

    fun loadScannedTasks(profileId: ProfileIdentifier): Flow<List<ScannedTaskData.ScannedTask>> =
        realm.query<ScannedTaskEntityV1>("parent.id = $0", profileId)
            .sort("scannedOn", Sort.DESCENDING)
            .asFlow()
            .map { scannedTasks ->
                scannedTasks.list.map { task ->
                    task.toScannedTask()
                }
            }

    suspend fun deleteTask(taskId: String) {
        realm.tryWrite<Unit> {
            queryFirst<ScannedTaskEntityV1>("taskId = $0", taskId)?.let { delete(it) }
            queryFirst<SyncedTaskEntityV1>("taskId = $0", taskId)?.let {
                deleteAll(it)
            }
        }
    }

    suspend fun updateRedeemedOn(taskId: String, timestamp: Instant?) {
        realm.tryWrite<Unit> {
            queryFirst<ScannedTaskEntityV1>("taskId = $0", taskId)?.apply {
                this.redeemedOn = timestamp?.toRealmInstant()
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
}
