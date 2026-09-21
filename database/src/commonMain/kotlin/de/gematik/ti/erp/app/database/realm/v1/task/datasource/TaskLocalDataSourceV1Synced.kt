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

import de.gematik.ti.erp.app.database.api.model.SaveTaskResult
import de.gematik.ti.erp.app.database.api.task.TaskLocalDataSourceSynced
import de.gematik.ti.erp.app.database.realm.utils.queryFirst
import de.gematik.ti.erp.app.database.realm.utils.safeWrite
import de.gematik.ti.erp.app.database.realm.utils.toInstant
import de.gematik.ti.erp.app.database.realm.utils.toRealmInstant
import de.gematik.ti.erp.app.database.realm.v1.task.entity.MedicationDispenseEntityV1
import de.gematik.ti.erp.app.database.realm.v1.task.entity.ScannedTaskEntityV1
import de.gematik.ti.erp.app.database.realm.v1.task.entity.SyncedTaskEntityV1
import de.gematik.ti.erp.app.database.realm.v1.task.entity.TaskStatusV1
import de.gematik.ti.erp.app.database.realm.v1.task.mappers.TaskDatabaseMappers.toDatabaseModel
import de.gematik.ti.erp.app.database.realm.v1.task.mappers.toTaskStatusV1
import de.gematik.ti.erp.app.fhir.FhirMedicationDispenseErpModelCollection
import de.gematik.ti.erp.app.fhir.FhirTaskDataErpModel
import de.gematik.ti.erp.app.fhir.FhirTaskMetaDataErpModel
import de.gematik.ti.erp.app.fhir.prescription.model.FhirTaskStatusErpModel
import de.gematik.ti.erp.app.fhir.temporal.FhirTemporal
import de.gematik.ti.erp.app.profile.repository.ProfileIdentifier
import io.realm.kotlin.Realm
import io.realm.kotlin.ext.query
import io.realm.kotlin.types.RealmInstant
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.sync.withLock
import kotlinx.datetime.Instant
import kotlinx.serialization.json.JsonElement

internal class TaskLocalDataSourceV1Synced(realm: Realm) : TaskLocalDataSourceV1Base(realm), TaskLocalDataSourceSynced {

    override fun getLatestTaskModifiedTimestamp(profileId: ProfileIdentifier): Flow<Instant?> {
        return realm.query<SyncedTaskEntityV1>("parent.id = $0", profileId)
            .max("lastModified", RealmInstant::class)
            .asFlow()
            .map {
                it?.toInstant()
            }
    }

    override suspend fun updateSyncedTaskStatus(
        taskId: String,
        status: FhirTaskStatusErpModel,
        lastModified: FhirTemporal?
    ) {
        realm.safeWrite {
            queryFirst<SyncedTaskEntityV1>("taskId = $0", taskId)?.let { task ->
                lastModified?.let {
                    task.lastModified = lastModified.toInstant().toRealmInstant()
                    task.status = status.toTaskStatusV1()
                }
            }
        }
    }

    override suspend fun markSyncedTaskAsIncomplete(
        taskId: String,
        error: Throwable,
        originalBundle: JsonElement
    ): Result<Unit> {
        return runCatching {
            mutex.withLock {
                realm.safeWrite {
                    findTask(taskId).apply {
                        isIncomplete = true
                        failureToReport = error.message ?: ""
                    }
                }
            }
        }
    }

    override suspend fun saveSyncedTaskMetaData(
        profileId: ProfileIdentifier,
        model: FhirTaskMetaDataErpModel
    ): Result<Unit> {
        return runCatching {
            mutex.withLock {
                realm.safeWrite {
                    val profileEntity = findProfile(profileId)
                    val taskEntity = findOrCreateTask(model.taskId, profileEntity)
                    updateTaskEntityWithMetaData(taskEntity, profileEntity, model)
                }
            }
        }
    }

    override suspend fun saveSyncedTaskKBVData(
        taskId: String,
        model: FhirTaskDataErpModel
    ): Result<SaveTaskResult> {
        return runCatching {
            mutex.withLock {
                realm.safeWrite {
                    val taskEntity = findTask(taskId)

                    with(taskEntity) {
                        pvsIdentifier = model.pvsId ?: ""
                        organization = model.organization?.toDatabaseModel()
                        patient = model.patient?.toDatabaseModel()
                        practitioner = model.practitioner?.toDatabaseModel()
                        insuranceInformation = model.coverage?.toDatabaseModel()
                        deviceRequest = model.deviceRequest?.toDatabaseModel(taskEntity.deviceRequest?.isNew == null)
                        medicationRequest = model.medicationRequest?.toDatabaseModel().apply {
                            model.medication?.toDatabaseModel()?.let {
                                this?.medication = it
                            }
                        }
                    }

                    queryFirst<ScannedTaskEntityV1>("taskId = $0", taskEntity.taskId)?.let { delete(it) }

                    SaveTaskResult(
                        isCompleted = taskEntity.status == TaskStatusV1.Completed,
                        lastModified = taskEntity.lastModified.toInstant(),
                        lastMedicationDispense = taskEntity.lastMedicationDispense?.toInstant()
                    )
                }
            }
        }
    }

    override suspend fun saveSyncedTaskMedicationDispense(
        taskId: String,
        dispenseCollection: FhirMedicationDispenseErpModelCollection
    ) {
        mutex.withLock {
            realm.safeWrite {
                dispenseCollection.dispensedMedications.forEach { dispensedMedication ->
                    if (query<MedicationDispenseEntityV1>("dispenseId = $0", dispensedMedication.dispenseId)
                        .count()
                        .find() == 0L
                    ) {
                        val taskEntity = findTask(taskId)
                        with(taskEntity) {
                            medicationDispenses += dispensedMedication.toDatabaseModel()
                        }
                    }
                }
            }
        }
    }
}
