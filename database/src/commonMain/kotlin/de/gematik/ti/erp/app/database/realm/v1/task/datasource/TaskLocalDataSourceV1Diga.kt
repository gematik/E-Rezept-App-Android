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

import de.gematik.ti.erp.app.database.api.task.TaskLocalDataSourceDiga
import de.gematik.ti.erp.app.database.realm.utils.queryFirst
import de.gematik.ti.erp.app.database.realm.utils.safeWrite
import de.gematik.ti.erp.app.database.realm.utils.toRealmInstant
import de.gematik.ti.erp.app.database.realm.v1.task.entity.SyncedTaskEntityV1
import de.gematik.ti.erp.app.database.realm.v1.task.mappers.toDigaErpModel
import de.gematik.ti.erp.app.diga.model.DigaStatus
import de.gematik.ti.erp.app.profile.repository.ProfileIdentifier
import de.gematik.ti.erp.app.task.model.TaskErpModel
import io.realm.kotlin.Realm
import io.realm.kotlin.ext.query
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.datetime.Instant

internal class TaskLocalDataSourceV1Diga(realm: Realm) : TaskLocalDataSourceV1Base(realm), TaskLocalDataSourceDiga {
    override suspend fun setDigaAsNotNew(taskId: String) {
        realm.safeWrite {
            queryFirst<SyncedTaskEntityV1>("taskId = $0", taskId)?.let { task ->
                task.deviceRequest?.isNew = false
            }
        }
    }

    override suspend fun updateDigaStatus(taskId: String, status: DigaStatus, lastModified: Instant?) {
        realm.safeWrite {
            queryFirst<SyncedTaskEntityV1>("taskId = $0", taskId)?.let { task ->
                lastModified?.let {
                    task.deviceRequest?.userActionState = status.step
                    // timestamp if its going to in progress
                    if (status is DigaStatus.InProgress) {
                        task.deviceRequest?.sentCommunicationOn = status.sentOn?.toRealmInstant()
                    }
                    task.lastModified = lastModified.toRealmInstant()
                    task.deviceRequest?.isArchived = status == DigaStatus.SelfArchiveDiga
                }
            }
        }
    }

    override suspend fun updateDigaArchiveStatus(taskId: String, lastModified: Instant, isArchive: Boolean) {
        realm.safeWrite {
            queryFirst<SyncedTaskEntityV1>("taskId = $0", taskId)?.let { task ->
                task.deviceRequest?.isArchived = isArchive
                task.lastModified = lastModified.toRealmInstant()
            }
        }
    }

    override suspend fun updateDigaCommunicationSent(taskId: String, time: Instant) {
        realm.safeWrite {
            queryFirst<SyncedTaskEntityV1>("taskId = $0", taskId)?.let { task ->
                task.deviceRequest?.sentCommunicationOn = time.toRealmInstant()
            }
        }
    }

    override fun loadDigaByTaskId(taskId: String): Flow<TaskErpModel.Synced.Diga?> {
        return realm.query<SyncedTaskEntityV1>("taskId == $0 AND deviceRequest.pzn != $1  ", taskId, "")
            .first()
            .asFlow()
            .map { syncedTask ->
                syncedTask.obj?.toDigaErpModel()
            }
    }

    override fun loadDigaListByProfileId(profileId: ProfileIdentifier): Flow<List<TaskErpModel.Synced.Diga>> {
        return realm.query<SyncedTaskEntityV1>("parent.id == $0 AND deviceRequest.pzn != $1 ", profileId, "")
            .asFlow()
            .map { syncedTasks ->
                syncedTasks.list.map { syncedTask ->
                    syncedTask.toDigaErpModel()
                }
            }
    }

    override fun loadArchiveDigaListByProfileId(profileId: ProfileIdentifier): Flow<List<TaskErpModel.Synced.Diga>> {
        return realm.query<SyncedTaskEntityV1>("parent.id == $0 AND deviceRequest.isArchived == $1", profileId, true)
            .asFlow()
            .map { syncedTasks ->
                syncedTasks.list.map { syncedTask ->
                    syncedTask.toDigaErpModel()
                }
            }
    }
}
