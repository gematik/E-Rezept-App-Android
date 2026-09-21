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

package de.gematik.ti.erp.app.database.api

import de.gematik.ti.erp.app.medicationplan.model.MedicationScheduleDurationErpModel
import de.gematik.ti.erp.app.medicationplan.model.MedicationScheduleErpModel
import de.gematik.ti.erp.app.medicationplan.model.MedicationScheduleIntervalErpModel
import de.gematik.ti.erp.app.medicationplan.model.MedicationScheduleNotificationDosageErpModel
import de.gematik.ti.erp.app.medicationplan.model.MedicationScheduleNotificationErpModel
import kotlinx.coroutines.flow.Flow
import kotlinx.datetime.LocalTime

interface MedicationPlanLocalDataSource {
    fun getMedicationSchedule(taskId: String): Flow<MedicationScheduleErpModel?>

    fun getAllMedicationSchedules(): Flow<List<MedicationScheduleErpModel>>

    fun getMedicationScheduleNotification(notificationId: String): Flow<MedicationScheduleNotificationErpModel?>

    suspend fun deleteMedicationSchedule(taskId: String)

    suspend fun setOrCreateActiveMedicationSchedule(medicationScheduleErpModel: MedicationScheduleErpModel)

    suspend fun deactivateMedicationSchedule(taskId: String)

    suspend fun setMedicationScheduleDuration(taskId: String, medicationScheduleDurationErpModel: MedicationScheduleDurationErpModel)

    suspend fun setMedicationScheduleInterval(taskId: String, medicationScheduleIntervalErpModel: MedicationScheduleIntervalErpModel)

    suspend fun setOrCreateMedicationScheduleNotification(taskId: String, medicationScheduleNotificationErpModel: MedicationScheduleNotificationErpModel)

    suspend fun deleteMedicationScheduleNotification(medicationScheduleNotificationId: String)

    suspend fun setMedicationScheduleNotificationDosage(medicationScheduleNotificationId: String, dosage: MedicationScheduleNotificationDosageErpModel)

    suspend fun setMedicationScheduleNotificationTime(medicationScheduleNotificationId: String, time: LocalTime)

    suspend fun importMigratedSchedule(medicationScheduleErpModel: MedicationScheduleErpModel) {}
}
