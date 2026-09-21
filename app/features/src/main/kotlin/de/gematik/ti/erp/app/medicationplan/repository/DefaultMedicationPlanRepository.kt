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

import de.gematik.ti.erp.app.database.api.MedicationPlanLocalDataSource
import de.gematik.ti.erp.app.medicationplan.alarm.MedicationPlanNotificationScheduler
import de.gematik.ti.erp.app.medicationplan.model.MedicationScheduleErpModel
import de.gematik.ti.erp.app.medicationplan.model.MedicationScheduleDurationErpModel
import de.gematik.ti.erp.app.medicationplan.model.MedicationScheduleIntervalErpModel
import de.gematik.ti.erp.app.medicationplan.model.MedicationScheduleNotificationErpModel
import de.gematik.ti.erp.app.medicationplan.model.MedicationScheduleNotificationDosageErpModel
import de.gematik.ti.erp.app.profile.repository.ProfileIdentifier
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.datetime.LocalTime

class DefaultMedicationPlanRepository(
    private val localDataSource: MedicationPlanLocalDataSource,
    private val scheduler: MedicationPlanNotificationScheduler
) : MedicationPlanRepository {
    override fun getMedicationSchedule(taskId: String): Flow<MedicationScheduleErpModel?> {
        return localDataSource.getMedicationSchedule(taskId = taskId)
    }

    override fun getAllMedicationSchedules(): Flow<List<MedicationScheduleErpModel>> {
        return localDataSource.getAllMedicationSchedules()
    }

    override suspend fun deleteMedicationSchedule(taskId: String) {
        scheduler.cancelNotification(taskId)
        return localDataSource.deleteMedicationSchedule(taskId = taskId)
    }

    override suspend fun deleteAllMedicationSchedulesForProfile(profileIdentifier: ProfileIdentifier) {
        val filteredSchedules = localDataSource.getAllMedicationSchedules().firstOrNull()?.let {
            it.filter { schedule -> schedule.profileId == profileIdentifier }
        }
        filteredSchedules?.forEach {
            scheduler.cancelNotification(it.taskId)
            localDataSource.deleteMedicationSchedule(taskId = it.taskId)
        }
    }

    override suspend fun setOrCreateActiveMedicationSchedule(medicationScheduleErpModel: MedicationScheduleErpModel) {
        return localDataSource.setOrCreateActiveMedicationSchedule(medicationScheduleErpModel = medicationScheduleErpModel)
    }

    override suspend fun deactivateMedicationSchedule(taskId: String) {
        return localDataSource.deactivateMedicationSchedule(taskId = taskId)
    }

    override suspend fun setMedicationScheduleDuration(taskId: String, medicationScheduleDurationErpModel: MedicationScheduleDurationErpModel) {
        return localDataSource.setMedicationScheduleDuration(taskId = taskId, medicationScheduleDurationErpModel = medicationScheduleDurationErpModel)
    }

    override suspend fun setMedicationScheduleInterval(taskId: String, medicationScheduleIntervalErpModel: MedicationScheduleIntervalErpModel) {
        return localDataSource.setMedicationScheduleInterval(taskId = taskId, medicationScheduleIntervalErpModel = medicationScheduleIntervalErpModel)
    }

    override suspend fun setOrCreateMedicationScheduleNotification(
        taskId: String,
        medicationScheduleNotificationErpModel: MedicationScheduleNotificationErpModel
    ) {
        return localDataSource.setOrCreateMedicationScheduleNotification(
            taskId = taskId,
            medicationScheduleNotificationErpModel = medicationScheduleNotificationErpModel
        )
    }

    override suspend fun deleteMedicationScheduleNotification(medicationScheduleNotificationId: String) {
        return localDataSource.deleteMedicationScheduleNotification(medicationScheduleNotificationId = medicationScheduleNotificationId)
    }

    override suspend fun setMedicationScheduleNotificationDosage(
        medicationScheduleNotificationId: String,
        dosage: MedicationScheduleNotificationDosageErpModel
    ) {
        return localDataSource.setMedicationScheduleNotificationDosage(medicationScheduleNotificationId = medicationScheduleNotificationId, dosage = dosage)
    }

    override suspend fun setMedicationScheduleNotificationTime(medicationScheduleNotificationId: String, time: LocalTime) {
        return localDataSource.setMedicationScheduleNotificationTime(medicationScheduleNotificationId = medicationScheduleNotificationId, time = time)
    }
}
