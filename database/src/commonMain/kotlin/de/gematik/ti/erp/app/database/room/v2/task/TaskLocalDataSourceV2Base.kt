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

import de.gematik.ti.erp.app.database.room.v2.task.mappers.insuranceIdFor
import de.gematik.ti.erp.app.database.room.v2.task.mappers.medicationIdFor
import de.gematik.ti.erp.app.database.room.v2.task.mappers.medicationRequestIdFor
import de.gematik.ti.erp.app.database.room.v2.task.mappers.organizationIdFor
import de.gematik.ti.erp.app.database.room.v2.task.mappers.patientIdFor
import de.gematik.ti.erp.app.database.room.v2.task.mappers.practitionerIdFor
import de.gematik.ti.erp.app.database.room.v2.task.prescription.ErpTaskDao
import de.gematik.ti.erp.app.database.room.v2.task.prescription.ErpTaskEntity
import de.gematik.ti.erp.app.database.room.v2.task.prescription.ErpTaskWithRefsDao
import de.gematik.ti.erp.app.database.room.v2.task.prescription.TaskTypeValues
import de.gematik.ti.erp.app.fhir.FhirTaskDataErpModel
import de.gematik.ti.erp.app.fhir.FhirTaskMetaDataErpModel
import de.gematik.ti.erp.app.profile.repository.ProfileIdentifier
import de.gematik.ti.erp.app.task.model.TaskErpModel
import de.gematik.ti.erp.app.task.model.TaskStatusEnum

internal abstract class TaskLocalDataSourceV2Base(
    protected val taskDao: ErpTaskDao,
    protected val taskWithRefsDao: ErpTaskWithRefsDao
) {

    protected fun buildScannedEntity(
        task: TaskErpModel.Scanned,
        profileId: ProfileIdentifier,
        autoName: String
    ): ErpTaskEntity = ErpTaskEntity(
        taskId = task.taskId,
        taskType = TaskTypeValues.SCANNED,
        parentProfileId = profileId,
        accessCode = task.accessCode,
        name = task.name?.ifEmpty { autoName },
        redeemedOn = task.redeemedOn,
        isEuRedeemable = task.isEuRedeemable,
        scannedOn = task.scannedOn,
        index = task.index
    )

    protected fun buildMetaDataEntity(
        existing: ErpTaskEntity?,
        profileId: ProfileIdentifier,
        model: FhirTaskMetaDataErpModel
    ): ErpTaskEntity {
        val status = runCatching { TaskStatusEnum.valueOf(model.status.name) }.getOrDefault(TaskStatusEnum.Other)
        return ErpTaskEntity(
            taskId = model.taskId,
            taskType = existing?.taskType?.takeIf { it != TaskTypeValues.SCANNED } ?: TaskTypeValues.PRESCRIPTION,
            parentProfileId = profileId,
            accessCode = model.accessCode,
            name = existing?.name,
            redeemedOn = existing?.redeemedOn,
            isEuRedeemable = model.isEuRedeemableByProperties,
            isEuRedeemableByPatientAuthorization = model.isEuRedeemableByPatientAuthorization,
            lastModified = model.lastModified.toInstant(),
            expiresOn = model.expiresOn?.toInstant(),
            acceptUntil = model.acceptUntil?.toInstant(),
            authoredOn = model.authoredOn.toInstant(),
            status = status,
            lastMedicationDispense = model.lastMedicationDispense?.toInstant(),
            organizationId = existing?.organizationId,
            practitionerId = existing?.practitionerId,
            patientId = existing?.patientId,
            insuranceInformationId = existing?.insuranceInformationId,
            medicationId = existing?.medicationId,
            medicationRequestId = existing?.medicationRequestId,
            accidentInfoId = existing?.accidentInfoId,
            deviceRequestId = existing?.deviceRequestId,
            multiplePrescriptionId = existing?.multiplePrescriptionId,
            scannedOn = existing?.scannedOn,
            index = existing?.index ?: 0,
            isIncomplete = existing?.isIncomplete ?: false,
            pvsIdentifier = existing?.pvsIdentifier ?: "",
            failureToReport = existing?.failureToReport ?: ""
        )
    }

    protected fun applyMedicalData(
        existing: ErpTaskEntity,
        taskId: String,
        model: FhirTaskDataErpModel,
        multiplePrescriptionId: String? = null,
        deviceRequestId: String? = null
    ): ErpTaskEntity = existing.copy(
        taskType = if (model.deviceRequest != null) TaskTypeValues.DIGA else TaskTypeValues.PRESCRIPTION,
        pvsIdentifier = model.pvsId ?: "",
        patientId = model.patient?.let { patientIdFor(taskId) },
        practitionerId = model.practitioner?.let { practitionerIdFor(taskId) },
        organizationId = model.organization?.let { organizationIdFor(taskId) },
        insuranceInformationId = model.coverage?.let { insuranceIdFor(taskId) },
        medicationId = model.medication?.let { medicationIdFor(taskId) },
        medicationRequestId = model.medicationRequest?.let { medicationRequestIdFor(taskId) },
        multiplePrescriptionId = multiplePrescriptionId,
        deviceRequestId = deviceRequestId
    )
}
