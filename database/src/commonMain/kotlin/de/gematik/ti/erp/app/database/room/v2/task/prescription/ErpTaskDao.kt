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

package de.gematik.ti.erp.app.database.room.v2.task.prescription

import androidx.room.Dao
import androidx.room.Embedded
import androidx.room.Query
import androidx.room.Relation
import androidx.room.Transaction
import androidx.room.Upsert
import de.gematik.ti.erp.app.database.room.v2.task.accident.ErpAccidentInfoEntity
import de.gematik.ti.erp.app.database.room.v2.task.communication.ErpCommunicationEntity
import de.gematik.ti.erp.app.database.room.v2.task.diga.ErpTaskMedicationDeviceRequestEntity
import de.gematik.ti.erp.app.database.room.v2.task.insuranceinformation.ErpInsuranceInformationEntity
import de.gematik.ti.erp.app.database.room.v2.task.medication.ErpMedicationDispenseEntity
import de.gematik.ti.erp.app.database.room.v2.task.medication.ErpMedicationDispenseWithRefs
import de.gematik.ti.erp.app.database.room.v2.task.medication.ErpMedicationEntity
import de.gematik.ti.erp.app.database.room.v2.task.medication.ErpMedicationWithRefs
import de.gematik.ti.erp.app.database.room.v2.task.medicationrequest.ErpMedicationRequestEntity
import de.gematik.ti.erp.app.database.room.v2.task.medicationrequest.ErpMedicationRequestWithRefs
import de.gematik.ti.erp.app.database.room.v2.task.multipleprescription.ErpMultiplePrescriptionEntity
import de.gematik.ti.erp.app.database.room.v2.task.organization.ErpOrganizationEntity
import de.gematik.ti.erp.app.database.room.v2.task.patient.ErpPatientEntity
import de.gematik.ti.erp.app.database.room.v2.task.practitioner.ErpPractitionerEntity
import de.gematik.ti.erp.app.task.model.TaskStatusEnum
import kotlinx.coroutines.flow.Flow
import kotlinx.datetime.Instant

@Dao
interface ErpTaskDao {
    @Upsert
    suspend fun upsertDeviceRequests(items: List<ErpTaskMedicationDeviceRequestEntity>)

    @Upsert
    suspend fun upsertAccidentInfos(items: List<ErpAccidentInfoEntity>)

    @Upsert
    suspend fun upsertAll(items: List<ErpTaskEntity>)

    @Query("DELETE FROM tasks WHERE parentProfileId = :profileId")
    suspend fun deleteByProfileId(profileId: String)

    @Query("SELECT * FROM tasks WHERE parentProfileId = :profileId")
    suspend fun getByProfile(profileId: String): List<ErpTaskEntity>

    @Query("SELECT * FROM tasks WHERE taskId = :taskId LIMIT 1")
    suspend fun getByTaskId(taskId: String): ErpTaskEntity?

    @Query("DELETE FROM tasks WHERE taskId = :taskId")
    suspend fun deleteByTaskId(taskId: String)

    @Query("DELETE FROM tasks")
    suspend fun clearAll()

    // Observability helpers
    @Query("SELECT taskId FROM tasks")
    fun observeAllIds(): Flow<List<String>>

    @Query("SELECT taskId FROM tasks WHERE parentProfileId = :profileId")
    fun observeIdsByProfile(profileId: String): Flow<List<String>>

    @Query("SELECT MAX(lastModified) FROM tasks WHERE parentProfileId = :profileId")
    fun observeLatestModified(profileId: String): Flow<Instant?>

    @Query("UPDATE tasks SET status = :status, lastModified = COALESCE(:lastModified, lastModified) WHERE taskId = :taskId")
    suspend fun updateStatus(taskId: String, status: TaskStatusEnum, lastModified: Instant?)

    @Query("UPDATE tasks SET lastModified = COALESCE(:lastModified, lastModified) WHERE taskId = :taskId")
    suspend fun updateLastModified(taskId: String, lastModified: Instant?)

    @Query("UPDATE task_med_device_requests SET isNew = 0 WHERE deviceRequestId = (SELECT deviceRequestId FROM tasks WHERE taskId = :taskId LIMIT 1)")
    suspend fun setDeviceRequestIsNewFalseByTaskId(taskId: String)

    @Suppress("ktlint:standard:max-line-length", "MaxLineLength")
    @Query(
        "UPDATE task_med_device_requests " +
            "SET authoredOn = :time " +
            "WHERE deviceRequestId = (SELECT deviceRequestId FROM tasks WHERE taskId = :taskId LIMIT 1)"
    )
    suspend fun updateDeviceRequestAuthoredOnByTaskId(taskId: String, time: Instant)

    @Suppress("ktlint:standard:max-line-length", "MaxLineLength")
    @Query(
        "UPDATE task_med_device_requests " +
            "SET sentCommunicationOn = :time " +
            "WHERE deviceRequestId = (SELECT deviceRequestId FROM tasks WHERE taskId = :taskId LIMIT 1)"
    )
    suspend fun updateDeviceRequestSentCommunicationOnByTaskId(taskId: String, time: Instant)

    @Suppress("ktlint:standard:max-line-length", "MaxLineLength")
    @Query(
        "UPDATE task_med_device_requests " +
            "SET status = :status " +
            "WHERE deviceRequestId = (SELECT deviceRequestId FROM tasks WHERE taskId = :taskId LIMIT 1)"
    )
    suspend fun updateDeviceRequestStatusByTaskId(taskId: String, status: String)

    @Suppress("ktlint:standard:max-line-length", "MaxLineLength")
    @Query(
        "UPDATE task_med_device_requests " +
            "SET userActionState = :state " +
            "WHERE deviceRequestId = (SELECT deviceRequestId FROM tasks WHERE taskId = :taskId LIMIT 1)"
    )
    suspend fun updateDeviceRequestUserActionStateByTaskId(taskId: String, state: Int?)

    @Upsert
    suspend fun upsertPatients(items: List<ErpPatientEntity>)

    @Upsert
    suspend fun upsertPractitioners(items: List<ErpPractitionerEntity>)

    @Upsert
    suspend fun upsertMedications(items: List<ErpMedicationEntity>)

    @Upsert
    suspend fun upsertMedicationRequest(items: List<ErpMedicationRequestEntity>)

    @Upsert
    suspend fun upsertMultiplePrescriptions(items: List<ErpMultiplePrescriptionEntity>)

    @Query("UPDATE tasks SET redeemedOn = :redeemedOn WHERE taskId IN (:taskIds)")
    suspend fun markAsRedeemed(taskIds: List<String>, redeemedOn: Instant)

    @Query("UPDATE tasks SET redeemedOn = :redeemedOn WHERE taskId = :taskId")
    suspend fun updateRedeemedOn(taskId: String, redeemedOn: Instant?)

    @Query("UPDATE tasks SET name = :name WHERE taskId = :taskId")
    suspend fun updateName(taskId: String, name: String)

    @Query("SELECT insuranceIdentifier FROM tasks INNER JOIN patient ON tasks.taskId = patient.taskId WHERE tasks.taskId = :taskId")
    suspend fun getInsurantIdByTaskId(taskId: String): String?

    @Query("UPDATE tasks SET isIncomplete = 1, failureToReport = :error WHERE taskId = :taskId")
    suspend fun markAsIncomplete(taskId: String, error: String)
}

data class ErpTaskWithRefs(
    @Embedded val task: ErpTaskEntity,

    @Relation(
        parentColumn = "taskId",
        entityColumn = "taskId"
    )
    val organization: ErpOrganizationEntity?,

    @Relation(
        parentColumn = "taskId",
        entityColumn = "taskId"
    )
    val practitioner: ErpPractitionerEntity?,

    @Relation(
        parentColumn = "taskId",
        entityColumn = "taskId"
    )
    val patient: ErpPatientEntity?,

    @Relation(
        parentColumn = "taskId",
        entityColumn = "taskId"
    )
    val insuranceInformation: ErpInsuranceInformationEntity?,

    @Relation(
        entity = ErpMedicationEntity::class,
        parentColumn = "taskId",
        entityColumn = "taskId"
    )
    val medication: ErpMedicationWithRefs?,

    @Relation(
        parentColumn = "taskId",
        entityColumn = "taskId"
    )
    val deviceRequest: ErpTaskMedicationDeviceRequestEntity?,

    @Relation(
        parentColumn = "taskId",
        entityColumn = "taskId"
    )
    val multiplePrescription: ErpMultiplePrescriptionEntity?,

    @Relation(
        parentColumn = "taskId",
        entityColumn = "taskId"
    )
    val accidentInfo: ErpAccidentInfoEntity?,

    @Relation(
        entity = ErpMedicationRequestEntity::class,
        parentColumn = "taskId",
        entityColumn = "taskId"
    )
    val medicationRequest: ErpMedicationRequestWithRefs?,

    @Relation(
        entity = ErpMedicationDispenseEntity::class,
        parentColumn = "taskId",
        entityColumn = "taskId"
    )
    val medicationDispenses: List<ErpMedicationDispenseWithRefs>,

    @Relation(
        parentColumn = "taskId",
        entityColumn = "taskId"
    )
    val communications: List<ErpCommunicationEntity>
)

@Dao
interface ErpTaskWithRefsDao {
    @Transaction
    @Query("SELECT * FROM tasks WHERE taskId = :taskId LIMIT 1")
    fun observeWithRefs(taskId: String): Flow<ErpTaskWithRefs?>

    @Transaction
    @Query("SELECT * FROM tasks WHERE parentProfileId = :profileId")
    fun observeAllWithRefsByProfile(profileId: String): Flow<List<ErpTaskWithRefs>>

    @Transaction
    @Query("SELECT * FROM tasks WHERE taskId IN (:taskIds)")
    fun observeAllWithRefsByTaskIds(taskIds: List<String>): Flow<List<ErpTaskWithRefs>>

    @Transaction
    @Query("SELECT * FROM tasks WHERE parentProfileId = :profileId AND taskType = 'Scanned'")
    fun observeScannedByProfile(profileId: String): Flow<List<ErpTaskWithRefs>>

    @Transaction
    @Query("SELECT * FROM tasks WHERE taskId IN (:taskIds) AND taskType = 'Scanned'")
    fun observeScannedByTaskIds(taskIds: List<String>): Flow<List<ErpTaskWithRefs>>
}
