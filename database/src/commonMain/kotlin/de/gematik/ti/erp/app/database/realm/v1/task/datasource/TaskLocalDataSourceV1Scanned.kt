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

import de.gematik.ti.erp.app.communication.model.CommunicationProfileV1
import de.gematik.ti.erp.app.database.api.task.TaskLocalDataSourceScanned
import de.gematik.ti.erp.app.database.realm.utils.queryFirst
import de.gematik.ti.erp.app.database.realm.utils.safeWrite
import de.gematik.ti.erp.app.database.realm.utils.toRealmInstant
import de.gematik.ti.erp.app.database.realm.v1.profile.ProfileEntityV1
import de.gematik.ti.erp.app.database.realm.v1.task.entity.CommunicationEntityV1
import de.gematik.ti.erp.app.database.realm.v1.task.entity.ScannedTaskEntityV1
import de.gematik.ti.erp.app.profile.repository.ProfileIdentifier
import de.gematik.ti.erp.app.task.model.TaskErpModel
import io.realm.kotlin.Realm
import io.realm.kotlin.ext.query
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant

internal class TaskLocalDataSourceV1Scanned(realm: Realm) : TaskLocalDataSourceV1Base(realm), TaskLocalDataSourceScanned {

    override suspend fun redeemScannedTaskListByTaskIdList(taskIds: List<String>) {
        realm.safeWrite {
            taskIds.forEach { taskId ->
                queryFirst<ScannedTaskEntityV1>("taskId = $0", taskId)?.let {
                    it.redeemedOn = Clock.System.now().toRealmInstant()
                }
            }
        }
    }

    override suspend fun updateScannedTaskRedeemedOn(taskId: String, timestamp: Instant?) {
        realm.safeWrite<Unit> {
            queryFirst<ScannedTaskEntityV1>("taskId = $0", taskId)?.apply {
                this.redeemedOn = timestamp?.toRealmInstant()
            }
        }
    }

    override suspend fun saveScannedTaskList(
        profileId: ProfileIdentifier,
        tasks: List<TaskErpModel.Scanned>,
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
                                this.name = task.name?.ifEmpty { "$medicationString ${numberOfUnnamedScannedTasks + idx + 1}" }
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

    override suspend fun updateScannedTaskName(taskId: String, name: String) {
        realm.safeWrite<Unit> {
            queryFirst<ScannedTaskEntityV1>("taskId = $0", taskId)?.apply {
                this.name = name
            }
        }
    }

    override fun loadScannedPrescriptionListByProfileId(profileId: ProfileIdentifier): Flow<List<TaskErpModel.Scanned>> {
        return loadScannedTasksByProfileId(profileId).map { list ->
            list.filterIsInstance<TaskErpModel.Scanned>()
        }
    }

    override fun loadScannedTaskListByTaskIdList(taskIds: List<String>): Flow<List<TaskErpModel.Scanned>> {
        return TaskLocalDataSourceV1Common(realm).loadTaskListByTaskIdList(taskIds).map { list ->
            list.filterIsInstance<TaskErpModel.Scanned>()
        }
    }

    override fun loadScannedPrescriptionByTaskId(taskId: String): Flow<TaskErpModel.Scanned?> {
        return loadScannedTaskByTaskId(taskId).map { it as? TaskErpModel.Scanned }
    }

    override suspend fun saveCommunicationForScannedTask(taskId: String, pharmacyId: String, transactionId: String) {
        realm.safeWrite {
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
}
