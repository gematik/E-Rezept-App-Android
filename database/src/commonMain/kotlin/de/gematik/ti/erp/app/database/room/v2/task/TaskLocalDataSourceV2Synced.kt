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

package de.gematik.ti.erp.app.database.room.v2.task

import de.gematik.ti.erp.app.database.api.model.SaveTaskResult
import de.gematik.ti.erp.app.database.api.task.TaskLocalDataSourceSynced
import de.gematik.ti.erp.app.database.room.v2.task.insuranceinformation.InsuranceInformationDao
import de.gematik.ti.erp.app.database.room.v2.task.mappers.accidentInfoIdFor
import de.gematik.ti.erp.app.database.room.v2.task.mappers.deviceRequestIdFor
import de.gematik.ti.erp.app.database.room.v2.task.mappers.insuranceIdFor
import de.gematik.ti.erp.app.database.room.v2.task.mappers.medicationIdFor
import de.gematik.ti.erp.app.database.room.v2.task.mappers.medicationRequestIdFor
import de.gematik.ti.erp.app.database.room.v2.task.mappers.multiplePrescriptionIdFor
import de.gematik.ti.erp.app.database.room.v2.task.mappers.organizationIdFor
import de.gematik.ti.erp.app.database.room.v2.task.mappers.patientIdFor
import de.gematik.ti.erp.app.database.room.v2.task.mappers.practitionerIdFor
import de.gematik.ti.erp.app.database.room.v2.task.mappers.toIngredientEntities
import de.gematik.ti.erp.app.database.room.v2.task.mappers.toRoomEntities
import de.gematik.ti.erp.app.database.room.v2.task.mappers.toRoomEntity
import de.gematik.ti.erp.app.database.room.v2.task.medication.IngredientDao
import de.gematik.ti.erp.app.database.room.v2.task.medication.MedicationDispenseDao
import de.gematik.ti.erp.app.database.room.v2.task.organization.OrganizationDao
import de.gematik.ti.erp.app.database.room.v2.task.prescription.ErpTaskDao
import de.gematik.ti.erp.app.database.room.v2.task.prescription.ErpTaskWithRefsDao
import de.gematik.ti.erp.app.fhir.FhirMedicationDispenseErpModelCollection
import de.gematik.ti.erp.app.fhir.FhirTaskDataErpModel
import de.gematik.ti.erp.app.fhir.FhirTaskMetaDataErpModel
import de.gematik.ti.erp.app.fhir.prescription.model.FhirTaskStatusErpModel
import de.gematik.ti.erp.app.fhir.temporal.FhirTemporal
import de.gematik.ti.erp.app.profile.repository.ProfileIdentifier
import de.gematik.ti.erp.app.task.model.TaskStatusEnum
import kotlinx.coroutines.flow.Flow
import kotlinx.datetime.Instant
import kotlinx.serialization.json.JsonElement

internal class TaskLocalDataSourceV2Synced(
    taskDao: ErpTaskDao,
    taskWithRefsDao: ErpTaskWithRefsDao,
    private val organizationDao: OrganizationDao,
    private val insuranceInformationDao: InsuranceInformationDao,
    private val ingredientDao: IngredientDao,
    private val medicationDispenseDao: MedicationDispenseDao
) : TaskLocalDataSourceV2Base(taskDao, taskWithRefsDao), TaskLocalDataSourceSynced {

    override fun getLatestTaskModifiedTimestamp(profileId: ProfileIdentifier): Flow<Instant?> =
        taskDao.observeLatestModified(profileId)

    override suspend fun updateSyncedTaskStatus(
        taskId: String,
        status: FhirTaskStatusErpModel,
        lastModified: FhirTemporal?
    ) {
        lastModified ?: return
        val roomStatus = runCatching { TaskStatusEnum.valueOf(status.name) }.getOrDefault(TaskStatusEnum.Other)
        taskDao.updateStatus(taskId, roomStatus, lastModified.toInstant())
    }

    override suspend fun markSyncedTaskAsIncomplete(
        taskId: String,
        error: Throwable,
        originalBundle: JsonElement // TODO: Save the original bundle when there is a parsing error
    ): Result<Unit> = runCatching {
        requireNotNull(taskDao.getByTaskId(taskId)) { "Task $taskId not found" }
        taskDao.markAsIncomplete(
            taskId = taskId,
            error = error.message ?: ""
        )
    }

    override suspend fun saveSyncedTaskMetaData(
        profileId: ProfileIdentifier,
        model: FhirTaskMetaDataErpModel
    ): Result<Unit> = runCatching {
        val existing = taskDao.getByTaskId(model.taskId)
        taskDao.upsertAll(listOf(buildMetaDataEntity(existing, profileId, model)))
    }

    override suspend fun saveSyncedTaskKBVData(
        taskId: String,
        model: FhirTaskDataErpModel
    ): Result<SaveTaskResult> = runCatching {
        model.patient?.let { taskDao.upsertPatients(listOf(it.toRoomEntity(patientIdFor(taskId), taskId))) }
        model.practitioner?.let { taskDao.upsertPractitioners(listOf(it.toRoomEntity(practitionerIdFor(taskId), taskId))) }
        model.organization?.let { organizationDao.upsertAll(listOf(it.toRoomEntity(organizationIdFor(taskId), taskId))) }
        model.coverage?.let { insuranceInformationDao.upsertAll(listOf(it.toRoomEntity(insuranceIdFor(taskId), taskId))) }
        model.medication?.let { med ->
            val medId = medicationIdFor(taskId)
            taskDao.upsertMedications(listOf(med.toRoomEntity(medId, taskId)))
            val ingredientEntities = med.toIngredientEntities(medId)
            if (ingredientEntities.isNotEmpty()) {
                ingredientDao.upsertAll(ingredientEntities)
            }
        }
        model.medicationRequest?.let { medicationRequest ->
            val medicationId = medicationIdFor(taskId)
            val medicationRequestId = medicationRequestIdFor(taskId)
            taskDao.upsertMedicationRequest(
                listOf(medicationRequest.toRoomEntity(medicationRequestId, taskId, medicationId))
            )
            medicationRequest.multiplePrescriptionInfo?.let { multiplePrescription ->
                val mpId = multiplePrescriptionIdFor(taskId)
                taskDao.upsertMultiplePrescriptions(listOf(multiplePrescription.toRoomEntity(mpId, taskId)))
            }
        }
        model.deviceRequest?.let { deviceRequest ->
            val accidentInfoId = deviceRequest.accident?.let { accident ->
                val accidentId = accidentInfoIdFor(taskId)
                taskDao.upsertAccidentInfos(listOf(accident.toRoomEntity(accidentId, taskId)))
                accidentId
            }
            val deviceRequestId = deviceRequestIdFor(taskId)
            taskDao.upsertDeviceRequests(
                listOf(deviceRequest.toRoomEntity(deviceRequestId, taskId, accidentInfoId))
            )
        }

        val existing = requireNotNull(taskDao.getByTaskId(taskId)) {
            "Task $taskId not found; saveSyncedPrescriptionMetaData must be called first"
        }
        val mpId = if (model.medicationRequest?.multiplePrescriptionInfo?.indicator == true) multiplePrescriptionIdFor(taskId) else null
        val deviceRequestId = if (model.deviceRequest != null) deviceRequestIdFor(taskId) else null
        val updated = applyMedicalData(existing, taskId, model, mpId, deviceRequestId)
        taskDao.upsertAll(listOf(updated))

        SaveTaskResult(
            isCompleted = updated.status == TaskStatusEnum.Completed,
            lastModified = requireNotNull(updated.lastModified) { "lastModified missing for task $taskId" },
            lastMedicationDispense = updated.lastMedicationDispense
        )
    }

    override suspend fun saveSyncedTaskMedicationDispense(
        taskId: String,
        dispenseCollection: FhirMedicationDispenseErpModelCollection
    ) {
        val incomingDispenses = dispenseCollection.dispensedMedications
        val existingDispenseIds = medicationDispenseDao
            .getByTaskId(taskId)
            .map { it.dispenseId }
            .toSet()

        val roomData = incomingDispenses
            .filter { it.dispenseId !in existingDispenseIds }
            .map { it.toRoomEntities(taskId) }

        val medicationEntities = roomData.flatMap { it.first }
        val dispenseEntities = roomData.map { it.second }

        if (medicationEntities.isNotEmpty()) {
            taskDao.upsertMedications(medicationEntities)
        }

        if (dispenseEntities.isNotEmpty()) {
            medicationDispenseDao.upsertAll(dispenseEntities)
        }
    }
}
