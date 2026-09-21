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

import de.gematik.ti.erp.app.database.api.model.PrescriptionDataNotFoundException
import de.gematik.ti.erp.app.database.realm.utils.queryFirst
import de.gematik.ti.erp.app.database.realm.utils.toRealmInstant
import de.gematik.ti.erp.app.database.realm.v1.profile.ProfileEntityV1
import de.gematik.ti.erp.app.database.realm.v1.task.entity.ScannedTaskEntityV1
import de.gematik.ti.erp.app.database.realm.v1.task.entity.SyncedTaskEntityV1
import de.gematik.ti.erp.app.database.realm.v1.task.mappers.toErpModel
import de.gematik.ti.erp.app.database.realm.v1.task.mappers.toTaskStatusV1
import de.gematik.ti.erp.app.fhir.FhirTaskMetaDataErpModel
import de.gematik.ti.erp.app.profile.repository.ProfileIdentifier
import de.gematik.ti.erp.app.task.model.TaskErpModel
import io.realm.kotlin.MutableRealm
import io.realm.kotlin.Realm
import io.realm.kotlin.ext.query
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.sync.Mutex
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atStartOfDayIn

internal abstract class TaskLocalDataSourceV1Base(protected val realm: Realm) {

    protected val mutex = Mutex()

    protected fun MutableRealm.findProfile(profileId: ProfileIdentifier): ProfileEntityV1 {
        return queryFirst<ProfileEntityV1>("id = $0", profileId)
            ?: throw PrescriptionDataNotFoundException("ProfileEntity with id $profileId not found in database")
    }

    protected fun MutableRealm.findTask(taskId: String): SyncedTaskEntityV1 {
        return queryFirst<SyncedTaskEntityV1>("taskId = $0", taskId)
            ?: throw PrescriptionDataNotFoundException("SyncedTaskEntity with taskId $taskId not found in database")
    }

    protected fun MutableRealm.findOrCreateTask(taskId: String, profileEntity: ProfileEntityV1): SyncedTaskEntityV1 {
        return queryFirst<SyncedTaskEntityV1>("taskId = $0", taskId) ?: copyToRealm(SyncedTaskEntityV1())
            .also { profileEntity.syncedTasks += it }
    }

    protected fun loadSyncedTasksByProfileId(profileId: ProfileIdentifier): Flow<List<TaskErpModel>> =
        realm.query<SyncedTaskEntityV1>("parent.id = $0", profileId)
            .asFlow()
            .map { syncedTasks ->
                syncedTasks.list.map { syncedTask ->
                    syncedTask.toErpModel()
                }
            }

    protected fun loadScannedTasksByProfileId(profileId: ProfileIdentifier): Flow<List<TaskErpModel>> =
        realm.query<ScannedTaskEntityV1>("parent.id = $0", profileId)
            .asFlow()
            .map { syncedTasks ->
                syncedTasks.list.map { syncedTask ->
                    syncedTask.toErpModel()
                }
            }

    protected fun loadSyncedTaskByTaskId(taskId: String): Flow<TaskErpModel?> =
        realm.query<SyncedTaskEntityV1>("taskId = $0", taskId)
            .first()
            .asFlow()
            .map { syncedTask ->
                syncedTask.obj?.toErpModel()
            }

    protected fun loadScannedTaskByTaskId(taskId: String): Flow<TaskErpModel?> =
        realm.query<ScannedTaskEntityV1>("taskId = $0", taskId)
            .first()
            .asFlow()
            .map { scannedTask ->
                scannedTask.obj?.toErpModel()
            }

    protected fun MutableRealm.updateTaskEntityWithMetaData(
        taskEntity: SyncedTaskEntityV1,
        profileEntity: ProfileEntityV1,
        model: FhirTaskMetaDataErpModel
    ) {
        with(taskEntity) {
            parent = profileEntity
            taskId = model.taskId
            accessCode = model.accessCode
            lastModified = model.lastModified.value.toRealmInstant()
            status = model.status.toTaskStatusV1()
            expiresOn = model.expiresOn?.value?.atStartOfDayIn(TimeZone.UTC)?.toRealmInstant()
            lastMedicationDispense = model.lastMedicationDispense?.toInstant()?.toRealmInstant()
            acceptUntil = model.acceptUntil?.value?.atStartOfDayIn(TimeZone.UTC)?.toRealmInstant()
            authoredOn = model.authoredOn.value.toRealmInstant()
            isEuRedeemableByProperties = model.isEuRedeemableByProperties
            isEuRedeemableByPatientAuthorization = model.isEuRedeemableByPatientAuthorization
        }
    }
}
