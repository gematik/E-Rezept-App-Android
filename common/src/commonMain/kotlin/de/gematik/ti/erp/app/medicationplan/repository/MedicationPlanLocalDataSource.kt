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

package de.gematik.ti.erp.app.medicationplan.repository

import de.gematik.ti.erp.app.database.realm.utils.queryFirst
import de.gematik.ti.erp.app.database.realm.v1.medicationplan.MedicationScheduleEntityV1
import de.gematik.ti.erp.app.database.realm.v1.medicationplan.MedicationScheduleNotificationEntityV1
import de.gematik.ti.erp.app.medicationplan.model.MedicationScheduleNotificationDosage
import de.gematik.ti.erp.app.medicationplan.model.MedicationScheduleNotification
import de.gematik.ti.erp.app.medicationplan.model.MedicationSchedule
import de.gematik.ti.erp.app.medicationplan.model.MedicationScheduleDuration
import de.gematik.ti.erp.app.medicationplan.model.MedicationScheduleInterval
import de.gematik.ti.erp.app.medicationplan.model.toMedicationSchedule
import de.gematik.ti.erp.app.medicationplan.model.toMedicationScheduleDurationEntityV1
import de.gematik.ti.erp.app.medicationplan.model.toMedicationScheduleEntityV1
import de.gematik.ti.erp.app.medicationplan.model.toMedicationScheduleIntervalEntityV1
import de.gematik.ti.erp.app.medicationplan.model.toMedicationScheduleNotificationDosageEntityV1
import de.gematik.ti.erp.app.medicationplan.model.toMedicationScheduleNotificationEntityV1
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

class MedicationPlanLocalDataSource(
    private val realm: Realm,
    private val dispatchers: CoroutineDispatcher = Dispatchers.IO
) {
    fun getMedicationSchedule(taskId: String): Flow<MedicationSchedule?> =
        realm.query<MedicationScheduleEntityV1>("taskId = $0", taskId).asFlow().map {
            it.list.firstOrNull()?.toMedicationSchedule()
        }.flowOn(dispatchers)

    fun getAllMedicationSchedules(): Flow<List<MedicationSchedule>> =
        realm.query<MedicationScheduleEntityV1>().asFlow().map {
            it.list.map { schedule ->
                schedule.toMedicationSchedule()
            }
        }.flowOn(dispatchers)

    suspend fun deleteMedicationSchedule(taskId: String) {
        withContext(dispatchers) {
            realm.write {
                queryFirst<MedicationScheduleEntityV1>("taskId = $0", taskId)?.let { schedule ->
                    delete(schedule)
                }
            }
        }
    }

    suspend fun setOrCreateActiveMedicationSchedule(medicationSchedule: MedicationSchedule) {
        withContext(dispatchers) {
            realm.write {
                queryFirst<MedicationScheduleEntityV1>("taskId = $0", medicationSchedule.taskId)?.let {
                    it.isActive = true
                }
                    ?: copyToRealm(medicationSchedule.copy(isActive = true).toMedicationScheduleEntityV1(), UpdatePolicy.ALL)
            }
        }
    }

    suspend fun deactivateMedicationSchedule(taskId: String) {
        withContext(dispatchers) {
            realm.write {
                queryFirst<MedicationScheduleEntityV1>("taskId = $0", taskId)?.let {
                    it.isActive = false
                }
            }
        }
    }

    suspend fun setMedicationScheduleDuration(taskId: String, medicationScheduleDuration: MedicationScheduleDuration) {
        withContext(dispatchers) {
            realm.write {
                queryFirst<MedicationScheduleEntityV1>("taskId = $0", taskId)?.let {
                    it.duration = copyToRealm(medicationScheduleDuration.toMedicationScheduleDurationEntityV1())
                }
            }
        }
    }

    suspend fun setMedicationScheduleInterval(taskId: String, medicationScheduleInterval: MedicationScheduleInterval) {
        withContext(dispatchers) {
            realm.write {
                queryFirst<MedicationScheduleEntityV1>("taskId = $0", taskId)?.let {
                    it.interval = copyToRealm(medicationScheduleInterval.toMedicationScheduleIntervalEntityV1())
                }
            }
        }
    }

    suspend fun setOrCreateMedicationScheduleNotification(taskId: String, medicationScheduleNotification: MedicationScheduleNotification) {
        withContext(dispatchers) {
            realm.write {
                queryFirst<MedicationScheduleEntityV1>("taskId = $0", taskId)?.let { schedule ->
                    val existingNotification = schedule.notifications.find { it.id == medicationScheduleNotification.id }
                    val newNotification = copyToRealm(medicationScheduleNotification.toMedicationScheduleNotificationEntityV1(), UpdatePolicy.ALL)
                    if (existingNotification == null) {
                        schedule.notifications.add(
                            newNotification
                        )
                    }
                }
            }
        }
    }

    suspend fun deleteMedicationScheduleNotification(medicationScheduleNotificationId: String) {
        withContext(dispatchers) {
            realm.write {
                queryFirst<MedicationScheduleNotificationEntityV1>("id = $0", medicationScheduleNotificationId)?.let { notification ->
                    delete(notification)
                }
            }
        }
    }

    suspend fun setMedicationScheduleNotificationDosage(medicationScheduleNotificationId: String, dosage: MedicationScheduleNotificationDosage) {
        withContext(dispatchers) {
            realm.write {
                queryFirst<MedicationScheduleNotificationEntityV1>("id = $0", medicationScheduleNotificationId)?.let { notification ->
                    notification.dosage = copyToRealm(dosage.toMedicationScheduleNotificationDosageEntityV1())
                }
            }
        }
    }

    suspend fun setMedicationScheduleNotificationTime(medicationScheduleNotificationId: String, time: LocalTime) {
        withContext(dispatchers) {
            realm.write {
                queryFirst<MedicationScheduleNotificationEntityV1>("id = $0", medicationScheduleNotificationId)?.let { notification ->
                    notification.time = time.toString()
                }
            }
        }
    }
}
