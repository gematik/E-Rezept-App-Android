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

package de.gematik.ti.erp.app.prescription.mapper

import de.gematik.ti.erp.app.diga.model.mapToDigaStatus
import de.gematik.ti.erp.app.task.model.Quantity
import de.gematik.ti.erp.app.task.model.Ratio
import de.gematik.ti.erp.app.prescription.model.ScannedTaskData
import de.gematik.ti.erp.app.prescription.model.SyncedTaskData
import de.gematik.ti.erp.app.prescription.model.SyncedTaskData.SyncedTask
import de.gematik.ti.erp.app.prescription.usecase.model.Prescription
import de.gematik.ti.erp.app.task.model.AddressErpModel
import de.gematik.ti.erp.app.task.model.InsuranceErpModel
import de.gematik.ti.erp.app.task.model.InsuranceErpModelCoverageType
import de.gematik.ti.erp.app.task.model.MedicationDispenseErpModel
import de.gematik.ti.erp.app.task.model.MedicationErpModel
import de.gematik.ti.erp.app.task.model.MedicationRequestErpModel
import de.gematik.ti.erp.app.task.model.OrganizationErpModel
import de.gematik.ti.erp.app.task.model.PatientErpModel
import de.gematik.ti.erp.app.task.model.PractitionerErpModel
import de.gematik.ti.erp.app.task.model.RatioErpModel
import de.gematik.ti.erp.app.task.model.TaskErpModel
import de.gematik.ti.erp.app.task.model.TaskStatusEnum
import kotlinx.datetime.Clock

fun TaskErpModel.Scanned.toPrescription() = Prescription.ScannedPrescription(
    taskId = taskId,
    scannedOn = scannedOn,
    name = name ?: "",
    index = index,
    redeemedOn = redeemedOn,
    communications = emptyList()
)

fun TaskErpModel.Synced.Prescription.toPrescription(): Prescription.SyncedAndDigaCombinedPrescription {
    val now = Clock.System.now()
    val taskExpiresOn = expiresOn
    val taskAcceptUntil = acceptUntil
    val taskLastMedicationDispense = lastMedicationDispense
    val dispenseDeviceRequest = medicationDispenses.firstOrNull()?.deviceRequest
    val state: SyncedTask.TaskState = when {
        taskExpiresOn != null && taskExpiresOn <= now && status != TaskStatusEnum.Completed ->
            SyncedTask.Expired(expiredOn = taskExpiresOn)

        status == TaskStatusEnum.Ready ->
            SyncedTask.Ready(
                expiresOn = taskExpiresOn ?: now,
                acceptUntil = taskAcceptUntil ?: now
            )

        status == TaskStatusEnum.Canceled -> SyncedTask.Deleted(lastModified = lastModified)
        status == TaskStatusEnum.InProgress && taskLastMedicationDispense != null ->
            SyncedTask.Provided(lastMedicationDispense = taskLastMedicationDispense)

        status == TaskStatusEnum.InProgress -> SyncedTask.InProgress(lastModified = lastModified)
        else -> SyncedTask.Other(state = SyncedTaskData.TaskStatus.Completed, lastModified = lastModified)
    }
    return Prescription.SyncedAndDigaCombinedPrescription(
        taskId = taskId,
        isIncomplete = isIncomplete,
        name = medicationName(),
        organization = practitioner?.name ?: organization?.name ?: "",
        authoredOn = authoredOn,
        redeemedOn = redeemedOn,
        expiresOn = expiresOn,
        acceptUntil = acceptUntil,
        state = state,
        isDiga = false,
        deviceRequestState = status.mapToDigaStatus(
            userActionState = null,
            sentOn = lastModified,
            isDeclined = dispenseDeviceRequest?.isDeclined ?: false,
            isRedeemed = dispenseDeviceRequest?.isRedeemed ?: false
        ),
        isNew = false,
        isArchived = false,
        isDirectAssignment = taskId.startsWith("169."),
        lastModified = lastModified,
        prescriptionChipInformation = Prescription.PrescriptionChipInformation(
            isSelfPayPrescription = insuranceInformation?.coverageType == InsuranceErpModelCoverageType.SEL,
            isPartOfMultiplePrescription = medicationRequest?.multiplePrescriptionInfo?.indicator ?: false,
            numerator = medicationRequest?.multiplePrescriptionInfo?.numbering?.numerator?.value,
            denominator = medicationRequest?.multiplePrescriptionInfo?.numbering?.denominator?.value,
            start = medicationRequest?.multiplePrescriptionInfo?.start
        )
    )
}

internal fun TaskErpModel.Synced.Diga.toPrescription(): Prescription.SyncedAndDigaCombinedPrescription {
    val now = Clock.System.now()
    val taskExpiresOn = expiresOn
    val taskAcceptUntil = acceptUntil
    val dispenseDeviceRequest = medicationDispenses.firstOrNull()?.deviceRequest
    val sentOn = deviceRequest?.sentOn?.toInstant()
    val state: SyncedTask.TaskState = when {
        taskExpiresOn != null && taskExpiresOn <= now && status != TaskStatusEnum.Completed ->
            SyncedTask.Expired(expiredOn = taskExpiresOn)

        status == TaskStatusEnum.Ready ->
            SyncedTask.Ready(
                expiresOn = taskExpiresOn ?: now,
                acceptUntil = taskAcceptUntil ?: now
            )

        status == TaskStatusEnum.Canceled -> SyncedTask.Deleted(lastModified = lastModified)
        status == TaskStatusEnum.InProgress && sentOn != null ->
            SyncedTask.Pending(sentOn = sentOn, toTelematikId = "")

        status == TaskStatusEnum.InProgress -> SyncedTask.InProgress(lastModified = lastModified)
        else -> SyncedTask.Other(state = SyncedTaskData.TaskStatus.Completed, lastModified = lastModified)
    }
    return Prescription.SyncedAndDigaCombinedPrescription(
        taskId = taskId,
        isIncomplete = false,
        name = deviceRequest?.appName,
        organization = practitioner?.name ?: organization?.name ?: "",
        authoredOn = authoredOn,
        redeemedOn = redeemedOn,
        expiresOn = expiresOn,
        acceptUntil = acceptUntil,
        state = state,
        isDiga = true,
        deviceRequestState = status.mapToDigaStatus(
            userActionState = deviceRequest?.userActionState,
            sentOn = sentOn,
            isDeclined = dispenseDeviceRequest?.isDeclined ?: false,
            isRedeemed = dispenseDeviceRequest?.isRedeemed ?: false
        ),
        isNew = deviceRequest?.isNew ?: false,
        isArchived = deviceRequest?.isArchived ?: false,
        isDirectAssignment = false,
        lastModified = lastModified,
        prescriptionChipInformation = Prescription.PrescriptionChipInformation(
            isSelfPayPrescription = false,
            isPartOfMultiplePrescription = false,
            numerator = null,
            denominator = null,
            start = null
        )
    )
}

fun TaskErpModel.toLegacyPrescription(): Prescription =
    when (this) {
        is TaskErpModel.Scanned -> this.toPrescription()
        is TaskErpModel.Synced.Diga -> this.toPrescription()
        is TaskErpModel.Synced.Prescription -> this.toPrescription()
    }

// Bridge functions: TaskErpModel → legacy SyncedTaskData / ScannedTaskData
// Needed by use cases that still return PrescriptionData (detail screens not yet migrated).

fun TaskErpModel.Scanned.toScannedTask() = ScannedTaskData.ScannedTask(
    profileId = profileId,
    taskId = taskId,
    index = index,
    name = name ?: "",
    accessCode = accessCode,
    scannedOn = scannedOn,
    redeemedOn = redeemedOn,
    communications = emptyList()
)

fun TaskErpModel.Synced.Prescription.toSyncedTask() = SyncedTask(
    profileId = profileId,
    taskId = taskId,
    accessCode = accessCode,
    lastModified = lastModified,
    isEuRedeemable = isEuRedeemable,
    isEuRedeemableByPatientAuthorization = isEuRedeemableByPatientAuthorization,
    organization = organization.toOrganization(),
    practitioner = practitioner.toPractitioner(),
    patient = patient.toPatient(),
    insuranceInformation = insuranceInformation.toInsuranceInformation(),
    expiresOn = expiresOn,
    acceptUntil = acceptUntil,
    authoredOn = authoredOn,
    status = try {
        SyncedTaskData.TaskStatus.valueOf(status.name)
    } catch (e: IllegalArgumentException) {
        SyncedTaskData.TaskStatus.Other
    },
    isIncomplete = isIncomplete,
    pvsIdentifier = pvsIdentifier,
    failureToReport = failureToReport,
    medicationRequest = medicationRequest.toMedicationRequest(),
    lastMedicationDispense = lastMedicationDispense,
    medicationDispenses = medicationDispenses.map { it.toMedicationDispense() },
    deviceRequest = null,
    communications = emptyList()
)

private fun OrganizationErpModel?.toOrganization() = SyncedTaskData.Organization(
    name = this?.name,
    address = this?.address?.toAddress(),
    uniqueIdentifier = this?.uniqueIdentifier,
    phone = this?.phone,
    mail = this?.mail
)

private fun PractitionerErpModel?.toPractitioner() = SyncedTaskData.Practitioner(
    name = this?.name,
    qualification = this?.qualification,
    practitionerIdentifier = this?.practitionerIdentifier
)

private fun PatientErpModel?.toPatient() = SyncedTaskData.Patient(
    name = this?.name,
    address = this?.address?.toAddress(),
    birthdate = this?.dateOfBirth,
    insuranceIdentifier = this?.insuranceIdentifier
)

private fun InsuranceErpModel?.toInsuranceInformation() = SyncedTaskData.InsuranceInformation(
    name = this?.name,
    status = this?.status,
    identifierNumber = this?.identifierNumber,
    coverageType = try {
        SyncedTaskData.CoverageType.valueOf(this?.coverageType?.name ?: InsuranceErpModelCoverageType.UNKNOWN.name)
    } catch (e: IllegalArgumentException) {
        SyncedTaskData.CoverageType.UNKNOWN
    }
)

fun MedicationRequestErpModel?.toMedicationRequest(): SyncedTaskData.MedicationRequest {
    if (this == null) {
        return SyncedTaskData.MedicationRequest(
            substitutionAllowed = false,
            multiplePrescriptionInfo = SyncedTaskData.MultiplePrescriptionInfo(),
            note = null
        )
    }
    return SyncedTaskData.MedicationRequest(
        medication = medication?.toMedication(),
        authoredOn = authoredOn,
        dateOfAccident = dateOfAccident,
        accidentType = try {
            SyncedTaskData.AccidentType.valueOf(accidentType.name)
        } catch (e: IllegalArgumentException) {
            SyncedTaskData.AccidentType.None
        },
        location = location,
        emergencyFee = emergencyFee,
        substitutionAllowed = substitutionAllowed,
        dosageInstruction = dosageInstruction,
        multiplePrescriptionInfo = SyncedTaskData.MultiplePrescriptionInfo(
            indicator = multiplePrescriptionInfo.indicator,
            numbering = multiplePrescriptionInfo.numbering?.toRatio(),
            start = multiplePrescriptionInfo.start,
            end = multiplePrescriptionInfo.end
        ),
        quantity = quantity,
        note = note,
        bvg = bvg,
        additionalFee = SyncedTaskData.AdditionalFee.valueOf(additionalFee.value)
    )
}

fun MedicationDispenseErpModel.toMedicationDispense() = SyncedTaskData.MedicationDispense(
    dispenseId = dispenseId,
    patientIdentifier = patientIdentifier,
    medication = medication?.toMedication(),
    deviceRequest = deviceRequest,
    wasSubstituted = wasSubstituted,
    dosageInstruction = dosageInstruction,
    performer = performer,
    whenHandedOver = whenHandedOver
)

private fun MedicationErpModel.toMedication(): SyncedTaskData.Medication = SyncedTaskData.Medication(
    category = try {
        SyncedTaskData.MedicationCategory.valueOf(category.name)
    } catch (e: IllegalArgumentException) {
        SyncedTaskData.MedicationCategory.UNKNOWN
    },
    medicationProfile = medicationProfile,
    vaccine = isVaccine,
    text = text,
    form = form,
    lotNumber = lotNumber,
    expirationDate = expirationDate,
    identifier = SyncedTaskData.Identifier(pzn = identifier.pzn, atc = identifier.atc, ask = identifier.ask, snomed = identifier.snomed),
    normSizeCode = normSizeCode,
    amount = amount?.toRatio(),
    manufacturingInstructions = manufacturingInstructions,
    packaging = packaging,
    ingredientMedications = ingredientMedications.map { it?.toMedication() },
    ingredients = ingredients.map { ingredient ->
        SyncedTaskData.Ingredient(
            text = ingredient.text,
            form = ingredient.form,
            number = ingredient.number,
            amount = ingredient.amount,
            strength = ingredient.strength?.toRatio()
        )
    }
)

private fun AddressErpModel.toAddress() = SyncedTaskData.Address(
    line1 = line1,
    line2 = line2,
    postalCode = postalCode,
    city = city
)

private fun RatioErpModel.toRatio() = Ratio(
    numerator = numerator?.let { Quantity(it.value, it.unit) },
    denominator = denominator?.let { Quantity(it.value, it.unit) }
)

/* fun ScannedTask.toPrescription() = Prescription.ScannedPrescription(
    taskId = taskId,
    scannedOn = scannedOn,
    name = name,
    index = index,
    redeemedOn = redeemedOn,
    communications = communications
)

fun SyncedTask.toPrescription(): Prescription.SyncedAndDigaCombinedPrescription {
    val dispenseDeviceRequest = medicationDispenses.firstOrNull()?.deviceRequest

    return Prescription.SyncedAndDigaCombinedPrescription(
        taskId = taskId,
        isIncomplete = isIncomplete,
        name = deviceRequest?.appName ?: medicationName(),
        organization = practitioner.name ?: organization.name ?: "",
        authoredOn = authoredOn,
        redeemedOn = redeemedOn(),
        expiresOn = expiresOn,
        acceptUntil = acceptUntil,
        state = state(),
        isDiga = deviceRequest != null,
        deviceRequestState = status.mapToDigaStatus(
            userActionState = deviceRequest?.userActionState,
            sentOn = deviceRequest?.sentOn?.toInstant() ?: lastModified,
            isDeclined = dispenseDeviceRequest?.isDeclined ?: false,
            isRedeemed = dispenseDeviceRequest?.isRedeemed ?: false
        ),
        isNew = deviceRequest?.isNew ?: false,
        isArchived = deviceRequest?.isArchived ?: false,
        isDirectAssignment = isDirectAssignment(),
        lastModified = lastModified,
        prescriptionChipInformation = Prescription.PrescriptionChipInformation(
            isSelfPayPrescription = insuranceInformation
                .coverageType == SyncedTaskData.CoverageType.SEL,
            isPartOfMultiplePrescription = medicationRequest
                .multiplePrescriptionInfo.indicator,
            numerator = medicationRequest.multiplePrescriptionInfo
                .numbering?.numerator?.value,
            denominator = medicationRequest.multiplePrescriptionInfo
                .numbering?.denominator?.value,
            start = medicationRequest.multiplePrescriptionInfo.start
        )
    )
} */

// @JvmName("filterSyncedNonActiveTasks") fun List<SyncedTask>.filterNonActiveTasks() = filter { !it.isActive() }

// fun List<TaskErpModel.Synced.Prescription>.sortByExpiredDateAndAuthoredDate() = sortedWith(compareBy<TaskErpModel.Synced.Prescription> { it.expiresOn }.thenBy { it.authoredOn })

// fun List<TaskErpModel.Synced.Prescription>.groupByHospitalsOrDoctors() = groupBy { it.practitioner?.name ?: it.organization?.name }

/*fun Map<String?, List<TaskErpModel.Synced.Prescription>>.flatMapToPrescriptions() =
    flatMap { (_, tasks) -> tasks.map(TaskErpModel.Synced.Prescription::toPrescription) }*/

/*
@JvmName("flatMapSyncedToPrescriptions")
fun Map<String?, List<TaskErpModel.Synced>>.flatMapToPrescriptions(): List<Prescription.SyncedAndDigaCombinedPrescription> =
    flatMap { (_, tasks) ->
        tasks.map { task ->
            when (task) {
                is TaskErpModel.Synced.Prescription -> task.toPrescription()
                is TaskErpModel.Synced.Diga -> task.toPrescription()
            }
        }
    }*/

val TaskErpModel.uuid: String
    get() = "$taskId-$name-${hashCode()}"

fun TaskErpModel.redeemedOrExpiredOn(): kotlinx.datetime.Instant =
    when (this) {
        is TaskErpModel.Scanned -> requireNotNull(redeemedOn) { "Scanned prescription needs a redeemed timestamp" }
        is TaskErpModel.Synced.Prescription -> redeemedOn() ?: expiresOn ?: authoredOn
        is TaskErpModel.Synced.Diga -> redeemedOn() ?: expiresOn ?: authoredOn
    }
