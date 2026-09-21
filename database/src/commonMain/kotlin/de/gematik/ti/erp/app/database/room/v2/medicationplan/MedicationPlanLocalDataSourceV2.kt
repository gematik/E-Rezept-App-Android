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

import de.gematik.ti.erp.app.database.api.MedicationPlanLocalDataSource
import de.gematik.ti.erp.app.medicationplan.model.MedicationScheduleDurationErpModel
import de.gematik.ti.erp.app.medicationplan.model.MedicationScheduleErpModel
import de.gematik.ti.erp.app.medicationplan.model.MedicationScheduleIntervalErpModel
import de.gematik.ti.erp.app.medicationplan.model.MedicationScheduleNotificationDosageErpModel
import de.gematik.ti.erp.app.medicationplan.model.MedicationScheduleNotificationErpModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.datetime.LocalTime

class MedicationPlanLocalDataSourceV2(
    private val dao: MedicationPlanDao
) : MedicationPlanLocalDataSource {
    override fun getMedicationSchedule(taskId: String): Flow<MedicationScheduleErpModel?> {
        return dao.getMedicationSchedule(taskId).map { it?.toErpModel() }
    }

    override fun getAllMedicationSchedules(): Flow<List<MedicationScheduleErpModel>> {
        return dao.getAllMedicationSchedules().map { list -> list.map { it.toErpModel() } }
    }

    override fun getMedicationScheduleNotification(notificationId: String): Flow<MedicationScheduleNotificationErpModel?> {
        return dao.getMedicationScheduleNotification(notificationId).map { it?.toErpModel() }
    }

    override suspend fun deleteMedicationSchedule(taskId: String) {
        dao.deleteMedicationSchedule(taskId)
    }

    override suspend fun setOrCreateActiveMedicationSchedule(medicationScheduleErpModel: MedicationScheduleErpModel) {
        val entity = medicationScheduleErpModel.toMedicationScheduleEntity()
        val notifications = medicationScheduleErpModel.notifications.map {
            it.toMedicationScheduleNotificationEntity(medicationScheduleErpModel.taskId)
        }
        dao.setOrCreateActiveMedicationScheduleWithNotifications(entity, notifications)
    }

    override suspend fun deactivateMedicationSchedule(taskId: String) {
        dao.deactivateMedicationSchedule(taskId)
    }

    override suspend fun setMedicationScheduleDuration(
        taskId: String,
        medicationScheduleDurationErpModel: MedicationScheduleDurationErpModel
    ) {
        dao.setMedicationScheduleDuration(taskId, medicationScheduleDurationErpModel.toMedicationScheduleDurationEntity())
    }

    override suspend fun setMedicationScheduleInterval(
        taskId: String,
        medicationScheduleIntervalErpModel: MedicationScheduleIntervalErpModel
    ) {
        dao.setMedicationScheduleInterval(taskId, medicationScheduleIntervalErpModel.toMedicationScheduleIntervalEntity())
    }

    override suspend fun setOrCreateMedicationScheduleNotification(
        taskId: String,
        medicationScheduleNotificationErpModel: MedicationScheduleNotificationErpModel
    ) {
        dao.setOrCreateMedicationScheduleNotification(
            medicationScheduleNotificationErpModel.toMedicationScheduleNotificationEntity(taskId)
        )
    }

    override suspend fun deleteMedicationScheduleNotification(medicationScheduleNotificationId: String) {
        dao.deleteMedicationScheduleNotification(medicationScheduleNotificationId)
    }

    override suspend fun setMedicationScheduleNotificationDosage(
        medicationScheduleNotificationId: String,
        dosage: MedicationScheduleNotificationDosageErpModel
    ) {
        dao.setMedicationScheduleNotificationDosage(medicationScheduleNotificationId, dosage.toMedicationScheduleNotificationDosageEntity())
    }

    override suspend fun setMedicationScheduleNotificationTime(medicationScheduleNotificationId: String, time: LocalTime) {
        dao.setMedicationScheduleNotificationTime(medicationScheduleNotificationId, time)
    }

    override suspend fun importMigratedSchedule(medicationScheduleErpModel: MedicationScheduleErpModel) {
        val entity = medicationScheduleErpModel.toMedicationScheduleEntity()
        val notifications = medicationScheduleErpModel.notifications.map {
            it.toMedicationScheduleNotificationEntity(medicationScheduleErpModel.taskId)
        }
        dao.insertMedicationSchedule(entity)
        dao.insertMedicationNotifications(notifications)
    }
}
