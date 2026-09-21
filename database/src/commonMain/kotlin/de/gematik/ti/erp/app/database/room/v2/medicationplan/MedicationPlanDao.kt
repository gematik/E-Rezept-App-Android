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

package de.gematik.ti.erp.app.database.room.v2.medicationplan

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow
import kotlinx.datetime.LocalTime

@Dao
interface MedicationPlanDao {
    @Transaction
    @Query("SELECT * FROM medicationPlan WHERE taskId = :taskId LIMIT 1")
    fun getMedicationSchedule(taskId: String): Flow<MedicationScheduleWithNotifications?>

    @Transaction
    @Query("SELECT * FROM medicationPlan")
    fun getAllMedicationSchedules(): Flow<List<MedicationScheduleWithNotifications>>

    @Query("SELECT * FROM medicationNotification WHERE id = :notificationId LIMIT 1")
    fun getMedicationScheduleNotification(notificationId: String): Flow<MedicationScheduleNotificationEntity?>

    @Query("DELETE FROM medicationPlan WHERE taskId = :taskId")
    suspend fun deleteMedicationScheduleEntity(taskId: String)

    @Query("DELETE FROM medicationNotification WHERE taskId = :taskId")
    suspend fun deleteMedicationNotifications(taskId: String)

    @Transaction
    suspend fun deleteMedicationSchedule(taskId: String) {
        deleteMedicationNotifications(taskId)
        deleteMedicationScheduleEntity(taskId)
    }

    @Query("SELECT * FROM medicationPlan WHERE taskId = :taskId LIMIT 1")
    suspend fun getMedicationScheduleEntitySync(taskId: String): MedicationScheduleEntity?

    @Upsert
    suspend fun insertMedicationSchedule(entity: MedicationScheduleEntity)

    @Query("UPDATE medicationPlan SET isActive = 1 WHERE taskId = :taskId")
    suspend fun activateMedicationSchedule(taskId: String)

    @Upsert
    suspend fun insertMedicationNotifications(notifications: List<MedicationScheduleNotificationEntity>)

    @Transaction
    suspend fun setOrCreateActiveMedicationScheduleWithNotifications(
        entity: MedicationScheduleEntity,
        notifications: List<MedicationScheduleNotificationEntity>
    ) {
        val existing = getMedicationScheduleEntitySync(entity.taskId)
        if (existing != null) {
            activateMedicationSchedule(entity.taskId)
        } else {
            insertMedicationSchedule(entity.apply { isActive = true })
            insertMedicationNotifications(notifications)
        }
    }

    @Query("UPDATE medicationPlan SET isActive = 0 WHERE taskId = :taskId")
    suspend fun deactivateMedicationSchedule(taskId: String)

    @Transaction
    suspend fun setMedicationScheduleDuration(
        taskId: String,
        medicationScheduleDurationEntity: MedicationScheduleDurationEntity
    ) {
        getMedicationScheduleEntitySync(taskId)?.let { schedule ->
            insertMedicationSchedule(schedule.copy(duration = medicationScheduleDurationEntity))
        }
    }

    @Transaction
    suspend fun setMedicationScheduleInterval(
        taskId: String,
        medicationScheduleIntervalEntity: MedicationScheduleIntervalEntity
    ) {
        getMedicationScheduleEntitySync(taskId)?.let { schedule ->
            insertMedicationSchedule(schedule.copy(interval = medicationScheduleIntervalEntity))
        }
    }

    @Upsert
    suspend fun setOrCreateMedicationScheduleNotification(
        medicationScheduleNotificationEntity: MedicationScheduleNotificationEntity
    )

    @Query("DELETE FROM medicationNotification WHERE id = :medicationScheduleNotificationId")
    suspend fun deleteMedicationScheduleNotification(medicationScheduleNotificationId: String)

    @Query("SELECT * FROM medicationNotification WHERE id = :notificationId LIMIT 1")
    suspend fun getMedicationScheduleNotificationSync(notificationId: String): MedicationScheduleNotificationEntity?

    @Transaction
    suspend fun setMedicationScheduleNotificationDosage(
        medicationScheduleNotificationId: String,
        dosage: MedicationScheduleNotificationDosageEntity
    ) {
        getMedicationScheduleNotificationSync(medicationScheduleNotificationId)?.let { notification ->
            setOrCreateMedicationScheduleNotification(notification.copy(dosage = dosage))
        }
    }

    @Transaction
    suspend fun setMedicationScheduleNotificationTime(
        medicationScheduleNotificationId: String,
        time: LocalTime
    ) {
        getMedicationScheduleNotificationSync(medicationScheduleNotificationId)?.let { notification ->
            setOrCreateMedicationScheduleNotification(notification.copy(time = time))
        }
    }
}
