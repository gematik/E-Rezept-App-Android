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

package de.gematik.ti.erp.app.database.realm.v1.medicationplan

import de.gematik.ti.erp.app.database.api.MedicationPlanLocalDataSource
import de.gematik.ti.erp.app.database.realm.utils.queryFirst
import de.gematik.ti.erp.app.medicationplan.model.MedicationScheduleDurationErpModel
import de.gematik.ti.erp.app.medicationplan.model.MedicationScheduleErpModel
import de.gematik.ti.erp.app.medicationplan.model.MedicationScheduleIntervalErpModel
import de.gematik.ti.erp.app.medicationplan.model.MedicationScheduleNotificationDosageErpModel
import de.gematik.ti.erp.app.medicationplan.model.MedicationScheduleNotificationErpModel
import io.realm.kotlin.Realm
import io.realm.kotlin.UpdatePolicy
import io.realm.kotlin.ext.query
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import kotlinx.datetime.LocalTime

class MedicationPlanLocalDataSourceV1(
    private val realm: Realm,
    private val dispatchers: CoroutineDispatcher = Dispatchers.IO
) : MedicationPlanLocalDataSource {
    override fun getMedicationSchedule(taskId: String): Flow<MedicationScheduleErpModel?> =
        realm.query<MedicationScheduleEntityV1>("taskId = $0", taskId).asFlow().map {
            it.list.firstOrNull()?.toMedicationSchedule()
        }.flowOn(dispatchers)

    override fun getAllMedicationSchedules(): Flow<List<MedicationScheduleErpModel>> =
        realm.query<MedicationScheduleEntityV1>().asFlow().map {
            it.list.map { schedule ->
                schedule.toMedicationSchedule()
            }
        }.flowOn(dispatchers)

    override fun getMedicationScheduleNotification(notificationId: String): Flow<MedicationScheduleNotificationErpModel?> =
        realm.query<MedicationScheduleNotificationEntityV1>("id = $0", notificationId).asFlow().map {
            it.list.firstOrNull()?.toMedicationScheduleNotification()
        }.flowOn(dispatchers)

    override suspend fun deleteMedicationSchedule(taskId: String) {
        withContext(dispatchers) {
            realm.write {
                queryFirst<MedicationScheduleEntityV1>("taskId = $0", taskId)?.let { schedule ->
                    delete(schedule)
                }
            }
        }
    }

    override suspend fun setOrCreateActiveMedicationSchedule(medicationScheduleErpModel: MedicationScheduleErpModel) {
        withContext(dispatchers) {
            realm.write {
                queryFirst<MedicationScheduleEntityV1>(
                    "taskId = $0",
                    medicationScheduleErpModel.taskId
                )?.let {
                    it.isActive = true
                }
                    ?: copyToRealm(
                        medicationScheduleErpModel.copy(isActive = true)
                            .toMedicationScheduleEntityV1(),
                        UpdatePolicy.ALL
                    )
            }
        }
    }

    override suspend fun deactivateMedicationSchedule(taskId: String) {
        withContext(dispatchers) {
            realm.write {
                queryFirst<MedicationScheduleEntityV1>("taskId = $0", taskId)?.let {
                    it.isActive = false
                }
            }
        }
    }

    override suspend fun setMedicationScheduleDuration(taskId: String, medicationScheduleDurationErpModel: MedicationScheduleDurationErpModel) {
        withContext(dispatchers) {
            realm.write {
                queryFirst<MedicationScheduleEntityV1>("taskId = $0", taskId)?.let {
                    it.duration =
                        copyToRealm(medicationScheduleDurationErpModel.toMedicationScheduleDurationEntityV1())
                }
            }
        }
    }

    override suspend fun setMedicationScheduleInterval(taskId: String, medicationScheduleIntervalErpModel: MedicationScheduleIntervalErpModel) {
        withContext(dispatchers) {
            realm.write {
                queryFirst<MedicationScheduleEntityV1>("taskId = $0", taskId)?.let {
                    it.interval =
                        copyToRealm(medicationScheduleIntervalErpModel.toMedicationScheduleIntervalEntityV1())
                }
            }
        }
    }

    override suspend fun setOrCreateMedicationScheduleNotification(
        taskId: String,
        medicationScheduleNotificationErpModel: MedicationScheduleNotificationErpModel
    ) {
        withContext(dispatchers) {
            realm.write {
                queryFirst<MedicationScheduleEntityV1>("taskId = $0", taskId)?.let { schedule ->
                    val existingNotification =
                        schedule.notifications.find { it.id == medicationScheduleNotificationErpModel.id }
                    val newNotification = copyToRealm(
                        medicationScheduleNotificationErpModel.toMedicationScheduleNotificationEntityV1(),
                        UpdatePolicy.ALL
                    )
                    if (existingNotification == null) {
                        schedule.notifications.add(
                            newNotification
                        )
                    }
                }
            }
        }
    }

    override suspend fun deleteMedicationScheduleNotification(medicationScheduleNotificationId: String) {
        withContext(dispatchers) {
            realm.write {
                queryFirst<MedicationScheduleNotificationEntityV1>(
                    "id = $0",
                    medicationScheduleNotificationId
                )?.let { notification ->
                    delete(notification)
                }
            }
        }
    }

    override suspend fun setMedicationScheduleNotificationDosage(
        medicationScheduleNotificationId: String,
        dosage: MedicationScheduleNotificationDosageErpModel
    ) {
        withContext(dispatchers) {
            realm.write {
                queryFirst<MedicationScheduleNotificationEntityV1>(
                    "id = $0",
                    medicationScheduleNotificationId
                )?.let { notification ->
                    notification.dosage =
                        copyToRealm(dosage.toMedicationScheduleNotificationDosageEntityV1())
                }
            }
        }
    }

    override suspend fun setMedicationScheduleNotificationTime(medicationScheduleNotificationId: String, time: LocalTime) {
        withContext(dispatchers) {
            realm.write {
                queryFirst<MedicationScheduleNotificationEntityV1>(
                    "id = $0",
                    medicationScheduleNotificationId
                )?.let { notification ->
                    notification.time = time.toString()
                }
            }
        }
    }
}
