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

package de.gematik.ti.erp.app.database.room.v2.task.mappers

import de.gematik.ti.erp.app.communication.model.CommunicationErpModel
import de.gematik.ti.erp.app.communication.model.toCommunicationProfile
import de.gematik.ti.erp.app.database.api.model.PrescriptionDataNotFoundException
import de.gematik.ti.erp.app.database.realm.v1.task.mappers.joinIngredientNames
import de.gematik.ti.erp.app.database.room.v2.task.accident.ErpAccidentInfoEntity
import de.gematik.ti.erp.app.database.room.v2.task.communication.ErpCommunicationEntity
import de.gematik.ti.erp.app.database.room.v2.task.diga.ErpTaskMedicationDeviceRequestEntity
import de.gematik.ti.erp.app.database.room.v2.task.insuranceinformation.ErpInsuranceInformationEntity
import de.gematik.ti.erp.app.database.room.v2.task.medication.ErpIdentifierEmbeddable
import de.gematik.ti.erp.app.database.room.v2.task.medication.ErpIngredientEntity
import de.gematik.ti.erp.app.database.room.v2.task.medication.ErpMedicationDispenseEntity
import de.gematik.ti.erp.app.database.room.v2.task.medication.ErpMedicationDispenseType
import de.gematik.ti.erp.app.database.room.v2.task.medication.ErpMedicationDispenseWithRefs
import de.gematik.ti.erp.app.database.room.v2.task.medication.ErpMedicationEntity
import de.gematik.ti.erp.app.database.room.v2.task.medication.ErpMedicationWithRefs
import de.gematik.ti.erp.app.database.room.v2.task.medication.ErpQuantityEmbeddable
import de.gematik.ti.erp.app.database.room.v2.task.medication.ErpRatioEmbeddable
import de.gematik.ti.erp.app.database.room.v2.task.medication.MedicationDispenseWithChildren
import de.gematik.ti.erp.app.database.room.v2.task.medicationrequest.ErpMedicationRequestEntity
import de.gematik.ti.erp.app.database.room.v2.task.medicationrequest.ErpMedicationRequestWithRefs
import de.gematik.ti.erp.app.database.room.v2.task.medicationrequest.ErpTeratogenicPrescriptionEmbeddable
import de.gematik.ti.erp.app.database.room.v2.task.multipleprescription.ErpMultiplePrescriptionEntity
import de.gematik.ti.erp.app.database.room.v2.task.organization.ErpOrganizationEntity
import de.gematik.ti.erp.app.database.room.v2.task.patient.ErpPatientEntity
import de.gematik.ti.erp.app.database.room.v2.task.practitioner.ErpPractitionerEntity
import de.gematik.ti.erp.app.database.room.v2.task.prescription.ErpTaskEntity
import de.gematik.ti.erp.app.database.room.v2.task.prescription.ErpTaskWithRefs
import de.gematik.ti.erp.app.database.room.v2.task.prescription.TaskTypeValues
import de.gematik.ti.erp.app.database.room.v2.task.util.AddressEmbeddable
import de.gematik.ti.erp.app.diga.model.DigaStatus
import de.gematik.ti.erp.app.diga.model.DigaStatusSteps
import de.gematik.ti.erp.app.fhir.dispense.model.DispensedEpaMedicationErpModel
import de.gematik.ti.erp.app.fhir.dispense.model.DispensedIngredientMedicationErpModel
import de.gematik.ti.erp.app.fhir.dispense.model.DispensedMedicationErpModel
import de.gematik.ti.erp.app.fhir.dispense.model.DispensedPznMedicationErpModel
import de.gematik.ti.erp.app.fhir.dispense.model.FhirDispenseDeviceRequestErpModel
import de.gematik.ti.erp.app.fhir.dispense.model.FhirDispensedCompoundingMedicationErpModel
import de.gematik.ti.erp.app.fhir.dispense.model.FhirDispensedFreeTextMedicationErpModel
import de.gematik.ti.erp.app.fhir.dispense.model.FhirMedicationDispenseErpModel
import de.gematik.ti.erp.app.fhir.prescription.model.FhirCoverageErpModel
import de.gematik.ti.erp.app.fhir.prescription.model.FhirMultiplePrescriptionInfoErpModel
import de.gematik.ti.erp.app.fhir.prescription.model.FhirTaskKbvDeviceRequestErpModel
import de.gematik.ti.erp.app.fhir.prescription.model.FhirTaskKbvMedicationErpModel
import de.gematik.ti.erp.app.fhir.prescription.model.FhirTaskKbvMedicationRequestErpModel
import de.gematik.ti.erp.app.fhir.prescription.model.FhirTaskKbvPatientErpModel
import de.gematik.ti.erp.app.fhir.prescription.model.FhirTaskKbvPractitionerErpModel
import de.gematik.ti.erp.app.fhir.prescription.model.FhirTaskOrganizationErpModel
import de.gematik.ti.erp.app.fhir.prescription.model.FhirTeratogenicPrescriptionErpModel
import de.gematik.ti.erp.app.fhir.prescription.model.RequestIntent
import de.gematik.ti.erp.app.fhir.support.FhirAccidentInformationErpModel
import de.gematik.ti.erp.app.fhir.support.FhirMedicationIngredientErpModel
import de.gematik.ti.erp.app.fhir.support.FhirRatioErpModel
import de.gematik.ti.erp.app.fhir.support.FhirTaskAccidentType
import de.gematik.ti.erp.app.fhir.support.FhirTaskKbvAddressErpModel
import de.gematik.ti.erp.app.fhir.temporal.FhirTemporal
import de.gematik.ti.erp.app.fhir.temporal.asFhirTemporal
import de.gematik.ti.erp.app.fhir.temporal.toLocalDate
import de.gematik.ti.erp.app.task.model.AccidentType
import de.gematik.ti.erp.app.task.model.AdditionalFeeErpModel
import de.gematik.ti.erp.app.task.model.AddressErpModel
import de.gematik.ti.erp.app.task.model.Identifier
import de.gematik.ti.erp.app.task.model.Ingredient
import de.gematik.ti.erp.app.task.model.InsuranceErpModel
import de.gematik.ti.erp.app.task.model.InsuranceErpModelCoverageType
import de.gematik.ti.erp.app.task.model.MedicationCategory
import de.gematik.ti.erp.app.task.model.MedicationDispenseErpModel
import de.gematik.ti.erp.app.task.model.MedicationErpModel
import de.gematik.ti.erp.app.task.model.MedicationRequestErpModel
import de.gematik.ti.erp.app.task.model.MultiplePrescriptionInfo
import de.gematik.ti.erp.app.task.model.OrganizationErpModel
import de.gematik.ti.erp.app.task.model.PatientErpModel
import de.gematik.ti.erp.app.task.model.PractitionerErpModel
import de.gematik.ti.erp.app.task.model.QuantityErpModel
import de.gematik.ti.erp.app.task.model.RatioErpModel
import de.gematik.ti.erp.app.task.model.TaskErpModel
import de.gematik.ti.erp.app.task.model.TeratogenicPrescriptionErpModel
import io.github.aakira.napier.Napier
import kotlinx.datetime.Clock.System
import kotlinx.datetime.Instant

// ─────────────────────────────────────────────────────────────────────────────
// Sub-model helpers: Room entity → domain model
// ─────────────────────────────────────────────────────────────────────────────

internal fun ErpAccidentInfoEntity.toErpModel(): FhirAccidentInformationErpModel =
    FhirAccidentInformationErpModel(
        type = FhirTaskAccidentType.entries.find { it.name == accidentType } ?: when (accidentType) {
            AccidentType.Unfall.name -> FhirTaskAccidentType.Accident
            AccidentType.Arbeitsunfall.name -> FhirTaskAccidentType.WorkAccident
            AccidentType.Berufskrankheit.name -> FhirTaskAccidentType.OccupationalDisease
            else -> FhirTaskAccidentType.None
        },
        date = dateOfAccident.toLocalDate().asFhirTemporal(),
        location = workPlaceIdentifier
    )

private fun Int.toDigaStatus(sentOn: Instant?): DigaStatus = when (this) {
    DigaStatusSteps.Ready.step -> DigaStatus.Ready
    DigaStatusSteps.InProgress.step -> DigaStatus.InProgress(sentOn)
    DigaStatusSteps.CompletedWithRejection.step -> DigaStatus.CompletedWithRejection(sentOn)
    DigaStatusSteps.CompletedSuccessfully.step -> DigaStatus.CompletedSuccessfully
    DigaStatusSteps.DownloadDigaApp.step -> DigaStatus.DownloadDigaApp
    DigaStatusSteps.OpenAppWithRedeemCode.step -> DigaStatus.OpenAppWithRedeemCode
    DigaStatusSteps.ReadyForSelfArchiveDiga.step -> DigaStatus.ReadyForSelfArchiveDiga
    DigaStatusSteps.SelfArchiveDiga.step -> DigaStatus.SelfArchiveDiga
    else -> DigaStatus.WrappedTaskStatus("Unknown step $this")
}

internal fun ErpTaskMedicationDeviceRequestEntity.toErpModel(
    accidentInfo: ErpAccidentInfoEntity?,
    lastModified: Instant?
): FhirTaskKbvDeviceRequestErpModel = FhirTaskKbvDeviceRequestErpModel(
    id = deviceRequestId,
    intent = RequestIntent.fromCode(intent),
    status = status,
    pzn = pzn,
    appName = appName,
    accident = accidentInfo?.toErpModel(),
    isSelfUse = isSelfUse,
    authoredOn = authoredOn.asFhirTemporal(),
    sentOn = sentCommunicationOn?.asFhirTemporal() ?: lastModified?.asFhirTemporal(),
    userActionState = userActionState?.toDigaStatus(sentCommunicationOn),
    isNew = isNew,
    isArchived = status.equals("archived", ignoreCase = true) ||
        userActionState == DigaStatusSteps.SelfArchiveDiga.step
)

internal fun ErpPatientEntity.toErpModel(): PatientErpModel = PatientErpModel(
    name = name,
    address = address?.toErpModel(),
    dateOfBirth = dob?.toFhirTemporalOrNull(),
    insuranceIdentifier = insuranceIdentifier
)

internal fun ErpPractitionerEntity.toErpModel(): PractitionerErpModel =
    PractitionerErpModel(
        name = name,
        qualification = qualification,
        practitionerIdentifier = practitionerIdentifier,
        dentistIdentifier = dentistIdentifier,
        telematikId = telematikId
    )

internal fun ErpOrganizationEntity.toErpModel(): OrganizationErpModel =
    OrganizationErpModel(
        name = name,
        address = address?.toErpModel(),
        uniqueIdentifier = bsnr,
        phone = phone,
        mail = mail
    )

internal fun ErpInsuranceInformationEntity.toErpModel(): InsuranceErpModel =
    InsuranceErpModel(
        name = name,
        status = status,
        identifierNumber = identifierNumber,
        coverageType = InsuranceErpModelCoverageType.mapTo(coverageType)
    )

internal fun ErpMedicationDispenseWithRefs.toErpModel(): MedicationDispenseErpModel =
    dispense.toErpModel().copy(
        medication = medications.firstOrNull()?.toErpModel(dispense) ?: MedicationErpModel(
            ingredients = ingredients.map { it.toErpModel() }
        )
    )

internal fun MedicationDispenseWithChildren.toErpModel(): MedicationDispenseErpModel =
    medication.toErpModel().copy(
        medication = MedicationErpModel(
            ingredients = ingredients.map { it.toErpModel() }
        )
    )

internal fun ErpMedicationDispenseEntity.toErpModel(): MedicationDispenseErpModel = MedicationDispenseErpModel(
    dispenseId = dispenseId,
    patientIdentifier = patientIdentifier,
    wasSubstituted = substitutionAllowed,
    dosageInstruction = dosageInstruction,
    performer = performer,
    whenHandedOver = handedOverOn.toLocalDate().asFhirTemporal(),
    deviceRequest = FhirDispenseDeviceRequestErpModel(
        deepLink = deepLink,
        declineCode = declineCode,
        redeemCode = redeemCode,
        referencePzn = pzn,
        modifiedDate = modifiedDate?.asFhirTemporal(),
        note = note,
        display = display,
        status = status
    ),
    pharmacyName = pharmacyName,
    euCountryCode = euCountryCode
)

internal fun ErpMedicationWithRefs.toErpModel(dispense: ErpMedicationDispenseEntity? = null): MedicationErpModel =
    medication.toErpModel(dispense).copy(
        ingredients = ingredients.map { it.toErpModel() }
    )

internal fun ErpIngredientEntity.toErpModel(): Ingredient =
    Ingredient(
        text = text,
        form = form,
        amount = amount,
        number = number,
        strength = strength.toErpModel()
    )

internal fun ErpMedicationEntity.toErpModel(dispense: ErpMedicationDispenseEntity?): MedicationErpModel =
    MedicationErpModel(
        category = runCatching { MedicationCategory.valueOf(medicationCategory) }.getOrDefault(MedicationCategory.UNKNOWN),
        medicationProfile = medicationProfile,
        isVaccine = vaccine,
        text = text,
        form = form?.ifEmpty { null },
        normSizeCode = normSizeCode.ifEmpty { null },
        manufacturingInstructions = manufacturingInstructions.ifEmpty { null },
        packaging = packaging.ifEmpty { null },
        lotNumber = dispense?.lotNumber,
        expirationDate = dispense?.expirationDate?.asFhirTemporal(),
        amount = amount.toErpModel(),
        identifier = Identifier(
            pzn = identifier?.pzn ?: pzn,
            ask = identifier?.ask,
            atc = identifier?.atc,
            snomed = identifier?.snomed
        )
    )

private fun ErpRatioEmbeddable?.toErpModel(): RatioErpModel? = this?.let {
    RatioErpModel(
        numerator = QuantityErpModel(
            value = it.numerator.value,
            unit = it.numerator.unit
        ),
        denominator = QuantityErpModel(
            value = it.denominator.value,
            unit = it.denominator.unit
        )
    )
}

internal fun ErpMultiplePrescriptionEntity.toErpModel(): MultiplePrescriptionInfo =
    MultiplePrescriptionInfo(
        indicator = indicator,
        numbering = numbering.toErpModel() ?: RatioErpModel(
            numerator = QuantityErpModel("", ""),
            denominator = QuantityErpModel("", "")
        ),
        start = start,
        end = end
    )

internal fun ErpTeratogenicPrescriptionEmbeddable.toErpModel(): TeratogenicPrescriptionErpModel =
    TeratogenicPrescriptionErpModel(
        offLabel = offLabel ?: false,
        gebaerfaehigeFrau = gebaerfaehigeFrau ?: false,
        einhaltungSicherheitsmassnahmen = einhaltungSicherheitsmassnahmen ?: false,
        aushaendigungInformationsmaterialien = aushaendigungInformationsmaterialien ?: false,
        erklaerungSachkenntnis = erklaerungSachkenntnis ?: false
    )

internal fun ErpMedicationRequestWithRefs.toErpModel(
    multiplePrescription: ErpMultiplePrescriptionEntity?
): MedicationRequestErpModel =
    medicationRequest.toErpModel(
        medication = medication,
        multiplePrescription = multiplePrescription
    )

internal fun ErpMedicationRequestEntity.toErpModel(
    medication: ErpMedicationWithRefs?,
    multiplePrescription: ErpMultiplePrescriptionEntity?
): MedicationRequestErpModel =
    MedicationRequestErpModel(
        medication = medication?.toErpModel(),
        authoredOn = authoredOn?.toFhirTemporalOrNull(),
        dateOfAccident = dateOfAccident?.toFhirTemporalOrNull()?.toInstant(),
        accidentType = runCatching { AccidentType.valueOf(accidentType) }.getOrDefault(AccidentType.None),
        location = location,
        emergencyFee = emergencyFee,
        substitutionAllowed = substitutionAllowed,
        dosageInstruction = dosageInstruction,
        multiplePrescriptionInfo = multiplePrescription?.toErpModel() ?: MultiplePrescriptionInfo(),
        quantity = quantity,
        note = note,
        bvg = bvg,
        additionalFee = AdditionalFeeErpModel.valueOf(additionalFee),
        teratogenicPrescription = teratogenicPrescription?.toErpModel()
    )

// ─────────────────────────────────────────────────────────────────────────────
// Main mapping: ErpTaskWithRefs → TaskErpModel
// ─────────────────────────────────────────────────────────────────────────────

internal fun ErpTaskWithRefs.toErpModel(): TaskErpModel {
    val profileId = task.parentProfileId ?: throw PrescriptionDataNotFoundException("parentProfileId missing for task ${task.taskId}")
    val deviceRequestErp = deviceRequest?.toErpModel(accidentInfo, task.lastModified)
    return when (task.taskType) {
        TaskTypeValues.SCANNED -> TaskErpModel.Scanned(
            profileId = profileId,
            taskId = task.taskId,
            accessCode = task.accessCode,
            name = task.name,
            redeemedOn = task.redeemedOn,
            isEuRedeemable = task.isEuRedeemable,
            scannedOn = task.scannedOn
                ?: throw PrescriptionDataNotFoundException("scannedOn missing for scanned task ${task.taskId}"),
            index = task.index,
            communications = communications.map { it.toErpModel() }
        )

        TaskTypeValues.PRESCRIPTION -> TaskErpModel.Synced.Prescription(
            profileId = profileId,
            taskId = task.taskId,
            accessCode = task.accessCode,
            name = medicationRequest?.name(),
            isEuRedeemable = task.isEuRedeemable,
            isEuRedeemableByPatientAuthorization = task.isEuRedeemableByPatientAuthorization,
            lastModified = task.lastModified
                ?: throw PrescriptionDataNotFoundException("lastModified missing for prescription task ${task.taskId}"),
            lastMedicationDispense = task.lastMedicationDispense,
            expiresOn = task.expiresOn,
            acceptUntil = task.acceptUntil,
            authoredOn = task.authoredOn
                ?: throw PrescriptionDataNotFoundException("authoredOn missing for prescription task ${task.taskId}"),
            status = task.status,
            isIncomplete = task.isIncomplete,
            pvsIdentifier = task.pvsIdentifier,
            failureToReport = task.failureToReport,
            organization = organization?.toErpModel(),
            practitioner = practitioner?.toErpModel(),
            patient = patient?.toErpModel(),
            insuranceInformation = insuranceInformation?.toErpModel(),
            medicationRequest = medicationRequest?.toErpModel(multiplePrescription),
            medicationDispenses = medicationDispenses.map { it.toErpModel() },
            communications = communications.map { it.toErpModel() }
        )

        TaskTypeValues.DIGA -> TaskErpModel.Synced.Diga(
            profileId = profileId,
            taskId = task.taskId,
            accessCode = task.accessCode,
            isEuRedeemable = task.isEuRedeemable,
            isEuRedeemableByPatientAuthorization = task.isEuRedeemableByPatientAuthorization,
            lastModified = task.lastModified ?: throw PrescriptionDataNotFoundException("lastModified missing for diga task ${task.taskId}"),
            name = deviceRequestErp?.appName,
            expiresOn = task.expiresOn,
            acceptUntil = task.acceptUntil,
            authoredOn = task.authoredOn
                ?: throw PrescriptionDataNotFoundException("authoredOn missing for diga task ${task.taskId}"),
            status = task.status,
            isIncomplete = task.isIncomplete,
            pvsIdentifier = task.pvsIdentifier,
            failureToReport = task.failureToReport,
            organization = organization?.toErpModel(),
            practitioner = practitioner?.toErpModel(),
            patient = patient?.toErpModel(),
            insuranceInformation = insuranceInformation?.toErpModel(),
            deviceRequest = deviceRequestErp,
            medicationDispenses = medicationDispenses.map { it.toErpModel() },
            communications = communications.map { it.toErpModel() }
        )

        else -> throw PrescriptionDataNotFoundException("Unknown taskType '${task.taskType}' for task ${task.taskId}")
    }
}

internal fun ErpMedicationWithRefs.name(): String? =
    medication.text.ifBlank {
        joinIngredientNames(ingredients.map { it.text })
    }.ifBlank { null }

internal fun ErpMedicationRequestWithRefs.name(): String? = medication?.name()

// ─────────────────────────────────────────────────────────────────────────────
// Sub-model helpers: domain model → Room entity
// ─────────────────────────────────────────────────────────────────────────────

internal fun PatientErpModel.toRoomEntity(patientId: String, taskId: String): ErpPatientEntity =
    ErpPatientEntity(
        patientId = patientId,
        taskId = taskId,
        name = name,
        dob = dateOfBirth?.formattedString(),
        insuranceIdentifier = insuranceIdentifier,
        address = address?.toEmbeddable()
    )

internal fun PractitionerErpModel.toRoomEntity(practitionerId: String, taskId: String): ErpPractitionerEntity =
    ErpPractitionerEntity(
        practitionerId = practitionerId,
        taskId = taskId,
        name = name,
        qualification = qualification,
        practitionerIdentifier = practitionerIdentifier,
        dentistIdentifier = dentistIdentifier,
        telematikId = telematikId
    )

internal fun OrganizationErpModel.toRoomEntity(organizationId: String, taskId: String): ErpOrganizationEntity =
    ErpOrganizationEntity(
        organizationId = organizationId,
        taskId = taskId,
        name = name,
        bsnr = uniqueIdentifier,
        phone = phone,
        mail = mail,
        address = address?.toEmbeddable()
    )

internal fun InsuranceErpModel.toRoomEntity(insuranceInformationId: String, taskId: String): ErpInsuranceInformationEntity =
    ErpInsuranceInformationEntity(
        insuranceInformationId = insuranceInformationId,
        taskId = taskId,
        name = name,
        status = status,
        identifierNumber = identifierNumber,
        coverageType = coverageType.name
    )

// ─────────────────────────────────────────────────────────────────────────────
// Main mapping: TaskErpModel → Room entity bundle
// ─────────────────────────────────────────────────────────────────────────────

/**
 * All Room entities representing a single [TaskErpModel].
 * Persist child entities first, then the task entity.
 */
data class TaskRoomEntityBundle(
    val task: ErpTaskEntity,
    val patient: ErpPatientEntity?,
    val practitioner: ErpPractitionerEntity?,
    val organization: ErpOrganizationEntity?,
    val insuranceInformation: ErpInsuranceInformationEntity?
)

internal fun TaskErpModel.toRoomEntityBundle(): TaskRoomEntityBundle =
    when (this) {
        is TaskErpModel.Scanned -> toRoomEntityBundle()
        is TaskErpModel.Synced.Prescription -> toRoomEntityBundle()
        is TaskErpModel.Synced.Diga -> toRoomEntityBundle()
    }

private fun TaskErpModel.Scanned.toRoomEntityBundle(): TaskRoomEntityBundle {
    val task = ErpTaskEntity(
        taskId = taskId,
        taskType = TaskTypeValues.SCANNED,
        parentProfileId = profileId,
        accessCode = accessCode,
        name = name,
        redeemedOn = redeemedOn,
        isEuRedeemable = isEuRedeemable,
        scannedOn = scannedOn,
        index = index
    )
    return TaskRoomEntityBundle(task = task, patient = null, practitioner = null, organization = null, insuranceInformation = null)
}

private fun TaskErpModel.Synced.Prescription.toRoomEntityBundle(): TaskRoomEntityBundle {
    val patientId = patientIdFor(taskId)
    val practitionerId = practitionerIdFor(taskId)
    val organizationId = organizationIdFor(taskId)
    val insuranceId = insuranceIdFor(taskId)

    val patientEntity = patient?.toRoomEntity(patientId, taskId)
    val practitionerEntity = practitioner?.toRoomEntity(practitionerId, taskId)
    val organizationEntity = organization?.toRoomEntity(organizationId, taskId)
    val insuranceEntity = insuranceInformation?.toRoomEntity(insuranceId, taskId)

    val task = ErpTaskEntity(
        taskId = taskId,
        taskType = TaskTypeValues.PRESCRIPTION,
        parentProfileId = profileId,
        accessCode = accessCode,
        name = name,
        redeemedOn = redeemedOn,
        isEuRedeemable = isEuRedeemable,
        isEuRedeemableByPatientAuthorization = isEuRedeemableByPatientAuthorization,
        lastModified = lastModified,
        lastMedicationDispense = lastMedicationDispense,
        expiresOn = expiresOn,
        acceptUntil = acceptUntil,
        authoredOn = authoredOn,
        status = status,
        isIncomplete = isIncomplete,
        pvsIdentifier = pvsIdentifier,
        failureToReport = failureToReport,
        patientId = patientEntity?.patientId,
        practitionerId = practitionerEntity?.practitionerId,
        organizationId = organizationEntity?.organizationId,
        insuranceInformationId = insuranceEntity?.insuranceInformationId
    )
    return TaskRoomEntityBundle(task, patientEntity, practitionerEntity, organizationEntity, insuranceEntity)
}

private fun TaskErpModel.Synced.Diga.toRoomEntityBundle(): TaskRoomEntityBundle {
    val patientId = patientIdFor(taskId)
    val practitionerId = practitionerIdFor(taskId)
    val organizationId = organizationIdFor(taskId)
    val insuranceId = insuranceIdFor(taskId)

    val patientEntity = patient?.toRoomEntity(patientId, taskId)
    val practitionerEntity = practitioner?.toRoomEntity(practitionerId, taskId)
    val organizationEntity = organization?.toRoomEntity(organizationId, taskId)
    val insuranceEntity = insuranceInformation?.toRoomEntity(insuranceId, taskId)

    val task = ErpTaskEntity(
        taskId = taskId,
        taskType = TaskTypeValues.DIGA,
        parentProfileId = profileId,
        accessCode = accessCode,
        name = name,
        redeemedOn = redeemedOn,
        isEuRedeemable = isEuRedeemable,
        isEuRedeemableByPatientAuthorization = isEuRedeemableByPatientAuthorization,
        lastModified = lastModified,
        expiresOn = expiresOn,
        acceptUntil = acceptUntil,
        authoredOn = authoredOn,
        status = status,
        isIncomplete = isIncomplete,
        pvsIdentifier = pvsIdentifier,
        failureToReport = failureToReport,
        patientId = patientEntity?.patientId,
        practitionerId = practitionerEntity?.practitionerId,
        organizationId = organizationEntity?.organizationId,
        insuranceInformationId = insuranceEntity?.insuranceInformationId
    )
    return TaskRoomEntityBundle(task, patientEntity, practitionerEntity, organizationEntity, insuranceEntity)
}

// ─────────────────────────────────────────────────────────────────────────────
// Private helpers
// ─────────────────────────────────────────────────────────────────────────────

/** Convert [AddressErpModel] → [AddressEmbeddable] for persistence. */
private fun AddressErpModel.toEmbeddable(): AddressEmbeddable =
    AddressEmbeddable(
        line1 = line1.ifEmpty { null },
        line2 = line2.ifEmpty { null },
        postalCode = postalCode.ifEmpty { null },
        city = city.ifEmpty { null },
        additionalAddressInformation = additionalAddressInformation
    )

/** Convert [AddressEmbeddable] → [AddressErpModel]; returns null when all fields are absent. */
internal fun AddressEmbeddable.toErpModel(): AddressErpModel? {
    if (line1 == null && line2 == null && postalCode == null && city == null) return null
    return AddressErpModel(
        line1 = line1.orEmpty(),
        line2 = line2.orEmpty(),
        postalCode = postalCode.orEmpty(),
        additionalAddressInformation = additionalAddressInformation,
        city = city.orEmpty()
    )
}

@Suppress("DEPRECATION")
private fun FhirTaskAccidentType.toAccidentTypeName(): String = when (this) {
    FhirTaskAccidentType.Accident -> AccidentType.Unfall.name
    FhirTaskAccidentType.WorkAccident -> AccidentType.Arbeitsunfall.name
    FhirTaskAccidentType.OccupationalDisease -> AccidentType.Berufskrankheit.name
    FhirTaskAccidentType.None -> AccidentType.None.name
}

private fun String.toFhirTemporalOrNull(): FhirTemporal? = runCatching {
    asFhirTemporal()
}.onFailure {
    Napier.e { "Failed to parse FhirTemporal from '$this': ${it.message}" }
}.getOrNull()

internal fun MedicationErpModel.toRoomEntity(medicationId: String, taskId: String?, parentDispenseId: String?): ErpMedicationEntity =
    ErpMedicationEntity(
        medicationId = medicationId,
        taskId = taskId,
        ratioId = null,
        parentDispenseId = parentDispenseId,
        text = text ?: "",
        medicationCategory = category.name,
        form = form,
        vaccine = isVaccine,
        manufacturingInstructions = manufacturingInstructions ?: "",
        packaging = packaging ?: "",
        normSizeCode = normSizeCode ?: "",
        amount = amount.toRoomEntity(),
        identifier = ErpIdentifierEmbeddable(
            pzn = identifier.pzn,
            ask = identifier.ask,
            atc = identifier.atc,
            snomed = identifier.snomed
        ),
        pzn = identifier.pzn
    )

internal fun Ingredient.toRoomEntity(
    ingredientId: String,
    medicationId: String,
    ingredientNumber: String
): ErpIngredientEntity =
    ErpIngredientEntity(
        ingredientId = ingredientId,
        medicationId = medicationId,
        text = text ?: "",
        form = form,
        amount = amount,
        number = ingredientNumber,
        strength = strength?.toRoomEntity()
    )

internal fun QuantityErpModel?.toRoomEntity(): ErpQuantityEmbeddable = ErpQuantityEmbeddable(
    value = this?.value,
    unit = this?.unit
)

internal fun RatioErpModel?.toRoomEntity(): ErpRatioEmbeddable = ErpRatioEmbeddable(
    numerator = this?.numerator.toRoomEntity(),
    denominator = this?.denominator.toRoomEntity()
)

internal fun MedicationRequestErpModel.toRoomEntity(
    medicationRequestId: String,
    taskId: String,
    medicationId: String? = null
): ErpMedicationRequestEntity =
    ErpMedicationRequestEntity(
        medicationRequestId = medicationRequestId,
        taskId = taskId,
        emergencyFee = emergencyFee,
        quantity = quantity,
        note = note,
        location = location,
        authoredOn = authoredOn?.formattedString(),
        substitutionAllowed = substitutionAllowed,
        dosageInstruction = dosageInstruction,
        bvg = bvg ?: false,
        additionalFee = additionalFee.name,
        dateOfAccident = dateOfAccident?.toString(),
        accidentType = accidentType.name,
        medicationId = medicationId
    )

internal fun MultiplePrescriptionInfo.toRoomEntity(
    multiplePrescriptionId: String,
    taskId: String
): ErpMultiplePrescriptionEntity =
    ErpMultiplePrescriptionEntity(
        multiplePrescriptionId = multiplePrescriptionId,
        taskId = taskId,
        indicator = indicator,
        numbering = numbering.toRoomEntity(),
        start = start ?: Instant.fromEpochMilliseconds(0),
        end = end ?: Instant.fromEpochMilliseconds(0)
    )

internal fun MedicationDispenseErpModel.toRoomEntity(
    taskId: String,
    medicationId: String
): ErpMedicationDispenseEntity = ErpMedicationDispenseEntity(
    dispenseId = dispenseId ?: taskId,
    childId = null,
    medicationId = medicationId,
    taskId = taskId,
    ingredientId = null,
    patientIdentifier = patientIdentifier,
    substitutionAllowed = wasSubstituted,
    dosageInstruction = dosageInstruction ?: "",
    performer = performer,
    handedOverOn = whenHandedOver?.toInstant() ?: System.now(),
    text = medication?.text ?: deviceRequest?.display ?: "",
    medicationDispenseType = ErpMedicationDispenseType.Other,
    form = medication?.form,
    amount = medication?.amount.toRoomEntity(),
    isVaccine = medication?.isVaccine ?: false,
    lotNumber = medication?.lotNumber ?: "",
    expirationDate = medication?.expirationDate?.toInstant(),
    pzn = medication?.identifier?.pzn ?: deviceRequest?.referencePzn ?: "",
    deepLink = deviceRequest?.deepLink,
    redeemCode = deviceRequest?.redeemCode,
    declineCode = deviceRequest?.declineCode,
    note = deviceRequest?.note,
    modifiedDate = deviceRequest?.modifiedDate?.toInstant(),
    display = deviceRequest?.display,
    status = deviceRequest?.status
)

/** Deterministic child-entity IDs derived from the task ID. */
internal fun patientIdFor(taskId: String) = "patient_$taskId"
internal fun practitionerIdFor(taskId: String) = "practitioner_$taskId"
internal fun organizationIdFor(taskId: String) = "organization_$taskId"
internal fun insuranceIdFor(taskId: String) = "insurance_$taskId"
internal fun medicationRequestIdFor(taskId: String) = "medication_request_$taskId"
internal fun multiplePrescriptionIdFor(taskId: String) = "multiple_prescription_$taskId"
internal fun medicationIdFor(taskId: String) = "medication_$taskId"
internal fun deviceRequestIdFor(taskId: String) = "device_request_$taskId"
internal fun accidentInfoIdFor(taskId: String) = "accident_$taskId"

/**
 * Deterministic ID for the [ErpMedicationEntity] that belongs to a single dispensed medication.
 * Mirrors the pattern used by [medicationIdFor] but scoped per dispense entry.
 */
internal fun dispensesMedicationIdFor(taskId: String, dispenseId: String) = "dispense_medication_${taskId}_$dispenseId"

// ─────────────────────────────────────────────────────────────────────────────
// FHIR model → Room entity (used when saving synced prescription medical data)
// ─────────────────────────────────────────────────────────────────────────────

internal fun FhirTaskOrganizationErpModel.toRoomEntity(organizationId: String, taskId: String): ErpOrganizationEntity =
    ErpOrganizationEntity(
        organizationId = organizationId,
        taskId = taskId,
        name = name,
        bsnr = bsnr,
        phone = phone,
        mail = email,
        address = address?.toEmbeddable()
    )

internal fun FhirTaskKbvPatientErpModel.toRoomEntity(patientId: String, taskId: String): ErpPatientEntity =
    ErpPatientEntity(
        patientId = patientId,
        taskId = taskId,
        name = name,
        dob = birthDate?.formattedString(),
        insuranceIdentifier = insuranceInformation,
        address = address?.toEmbeddable()
    )

internal fun FhirTaskKbvPractitionerErpModel.toRoomEntity(practitionerId: String, taskId: String): ErpPractitionerEntity =
    ErpPractitionerEntity(
        practitionerId = practitionerId,
        taskId = taskId,
        name = name,
        qualification = qualification,
        practitionerIdentifier = lanr,
        dentistIdentifier = zanr,
        telematikId = telematikId
    )

internal fun FhirCoverageErpModel.toRoomEntity(insuranceId: String, taskId: String): ErpInsuranceInformationEntity =
    ErpInsuranceInformationEntity(
        insuranceInformationId = insuranceId,
        taskId = taskId,
        name = name,
        status = statusCode,
        identifierNumber = healthInsuranceIdentifierForDiga,
        coverageType = coverageType ?: "UNKNOWN"
    )

internal fun FhirTeratogenicPrescriptionErpModel.toRoomEmbeddable(): ErpTeratogenicPrescriptionEmbeddable =
    ErpTeratogenicPrescriptionEmbeddable(
        offLabel = offLabel,
        gebaerfaehigeFrau = gebaerfaehigeFrau,
        einhaltungSicherheitsmassnahmen = einhaltungSicherheitsmassnahmen,
        aushaendigungInformationsmaterialien = aushaendigungInformationsmaterialien,
        erklaerungSachkenntnis = erklaerungSachkenntnis
    )

internal fun FhirTaskKbvMedicationRequestErpModel.toRoomEntity(
    medicationRequestId: String,
    taskId: String,
    medicationId: String? = null
): ErpMedicationRequestEntity =
    ErpMedicationRequestEntity(
        medicationRequestId = medicationRequestId,
        taskId = taskId,
        emergencyFee = emergencyFee,
        quantity = quantity,
        note = note,
        location = location,
        authoredOn = authoredOn?.formattedString(),
        substitutionAllowed = substitutionAllowed,
        dosageInstruction = dosageInstruction,
        bvg = isSer,
        additionalFee = additionalFee,
        dateOfAccident = dateOfAccident?.formattedString(),
        accidentType = accidentType.toAccidentTypeName(),
        medicationId = medicationId,
        teratogenicPrescription = teratogenicPrescription?.toRoomEmbeddable()
    )

internal fun FhirMultiplePrescriptionInfoErpModel.toRoomEntity(
    multiplePrescriptionId: String,
    taskId: String
): ErpMultiplePrescriptionEntity =
    ErpMultiplePrescriptionEntity(
        multiplePrescriptionId = multiplePrescriptionId,
        taskId = taskId,
        indicator = indicator,
        numbering = numbering?.toRoomEntity(),
        start = start?.toInstant() ?: Instant.fromEpochMilliseconds(0),
        end = end?.toInstant() ?: Instant.fromEpochMilliseconds(0)
    )

internal fun FhirAccidentInformationErpModel.toRoomEntity(accidentInfoId: String, taskId: String): ErpAccidentInfoEntity = ErpAccidentInfoEntity(
    accidentInfoId = accidentInfoId,
    taskId = taskId,
    dateOfAccident = date?.toInstant() ?: System.now(),
    accidentType = type?.toAccidentTypeName() ?: AccidentType.None.name,
    workPlaceIdentifier = location ?: ""
)

internal fun FhirTaskKbvDeviceRequestErpModel.toRoomEntity(
    deviceRequestId: String,
    taskId: String,
    accidentInfoId: String?
): ErpTaskMedicationDeviceRequestEntity = ErpTaskMedicationDeviceRequestEntity(
    deviceRequestId = deviceRequestId,
    taskId = taskId,
    accidentInfoId = accidentInfoId,
    intent = intent.code,
    status = status,
    pzn = pzn ?: "",
    appName = appName ?: "",
    isSelfUse = isSelfUse,
    isNew = isNew,
    userActionState = userActionState?.step,
    sentCommunicationOn = sentOn?.toInstant(),
    authoredOn = authoredOn?.toInstant() ?: System.now()
)

internal fun FhirTaskKbvMedicationErpModel.toRoomEntity(medicationId: String, taskId: String? = null): ErpMedicationEntity =
    ErpMedicationEntity(
        medicationId = medicationId,
        taskId = taskId,
        ratioId = null,
        text = text ?: "",
        medicationCategory = medicationCategory.name,
        form = form,
        vaccine = isVaccine,
        medicationProfile = medicationProfile,
        amount = amount?.toRoomEntity(),
        identifier = identifier.let {
            ErpIdentifierEmbeddable(
                pzn = it.pzn,
                ask = it.ask,
                atc = it.atc,
                snomed = it.snomed
            )
        },
        manufacturingInstructions = compoundingInstructions ?: "",
        packaging = compoundingPackaging ?: "",
        normSizeCode = normSizeCode ?: ""
    )

internal fun ingredientIdFor(medicationId: String, index: Int): String = "$medicationId-ingredient-$index"

internal fun FhirMedicationIngredientErpModel.toRoomEntity(
    ingredientId: String,
    medicationId: String,
    ingredientNumber: String?
): ErpIngredientEntity =
    ErpIngredientEntity(
        ingredientId = ingredientId,
        medicationId = medicationId,
        text = text ?: "",
        form = form,
        amount = amount,
        number = ingredientNumber ?: "",
        strength = strengthRatio?.toRoomEntity()
    )

internal fun FhirTaskKbvMedicationErpModel.toIngredientEntities(
    medicationId: String
): List<ErpIngredientEntity> =
    ingredients.mapIndexed { index, ingredient ->
        ingredient.toRoomEntity(
            ingredientId = ingredientIdFor(medicationId, index),
            medicationId = medicationId,
            ingredientNumber = this.ingredientNumber
        )
    }

private fun FhirTaskKbvAddressErpModel.toEmbeddable(): AddressEmbeddable =
    AddressEmbeddable(
        line1 = streetName?.ifEmpty { null },
        line2 = houseNumber?.ifEmpty { null },
        postalCode = postalCode?.ifEmpty { null },
        city = city?.ifEmpty { null },
        additionalAddressInformation = additionalAddressInformation
    )

// ─────────────────────────────────────────────────────────────────────────────
// MedicationDispense → Room entities
// ─────────────────────────────────────────────────────────────────────────────

/**
 * Maps a [FhirMedicationDispenseErpModel] to a pair of:
 * - [ErpMedicationEntity] representing the dispensed medication (1-1 relation, first entry wins per FHIR spec)
 * - [ErpMedicationDispenseEntity] referencing that medication
 *
 * @param taskId      the parent task ID used to build deterministic child IDs
 */
internal fun FhirMedicationDispenseErpModel.toRoomEntities(
    taskId: String
): Pair<List<ErpMedicationEntity>, ErpMedicationDispenseEntity> {
    val baseMedicationId = dispensesMedicationIdFor(taskId, dispenseId)
    val dispensedMed = dispensedMedication.firstOrNull()

    val medicationEntities = if (dispensedMedication.isEmpty()) {
        // Create a pseudo-medication for DiGAs if no explicit medication is present
        listOf(
            ErpMedicationEntity(
                medicationId = baseMedicationId,
                taskId = taskId,
                ratioId = null,
                parentDispenseId = dispenseId,
                text = dispensedDeviceRequest?.display ?: "",
                medicationCategory = MedicationCategory.UNKNOWN.name,
                form = "",
                vaccine = false,
                medicationProfile = null,
                manufacturingInstructions = "",
                packaging = "",
                normSizeCode = "",
                amount = null,
                identifier = null,
                pzn = dispensedDeviceRequest?.referencePzn ?: ""
            )
        )
    } else {
        dispensedMedication.mapIndexed { index, med ->
            med.toMedicationEntity(
                medicationId = if (index == 0) baseMedicationId else "${baseMedicationId}_$index",
                taskId = taskId,
                parentDispenseId = dispenseId
            )
        }
    }

    val dispenseEntity = ErpMedicationDispenseEntity(
        dispenseId = dispenseId,
        childId = null,
        medicationId = baseMedicationId,
        taskId = taskId,
        ingredientId = null,
        patientIdentifier = kvnrNumber,
        substitutionAllowed = substitutionAllowed,
        dosageInstruction = dosageInstruction ?: "",
        performer = telematikId ?: "",
        handedOverOn = handedOver?.toInstant() ?: System.now(),
        text = dispensedMed?.text ?: dispensedDeviceRequest?.display ?: "",
        medicationDispenseType = dispensedMed?.toDispenseType() ?: ErpMedicationDispenseType.Other,
        form = dispensedMed?.form,
        amount = dispensedMed?.amount.toRoomEntity(),
        isVaccine = dispensedMed?.isVaccine ?: false,
        lotNumber = dispensedMed?.lotNumber ?: "",
        expirationDate = dispensedMed?.expirationDate?.toInstant(),
        pzn = dispensedMed?.toPzn() ?: dispensedDeviceRequest?.referencePzn ?: "",
        deepLink = dispensedDeviceRequest?.deepLink,
        redeemCode = dispensedDeviceRequest?.redeemCode,
        declineCode = dispensedDeviceRequest?.declineCode,
        note = dispensedDeviceRequest?.note,
        modifiedDate = dispensedDeviceRequest?.modifiedDate?.toInstant(),
        display = dispensedDeviceRequest?.display,
        status = dispensedDeviceRequest?.status,
        pharmacyName = pharmacyName,
        euCountryCode = euCountryCode
    )
    return medicationEntities to dispenseEntity
}

/**
 * Maps a [DispensedMedicationErpModel] subtype to an [ErpMedicationEntity] used as the
 * medication record for a dispense row.  The [medicationId] is a deterministic key built
 * via [dispensesMedicationIdFor].
 */
private fun DispensedMedicationErpModel.toMedicationEntity(medicationId: String, taskId: String, parentDispenseId: String): ErpMedicationEntity =
    ErpMedicationEntity(
        medicationId = medicationId,
        taskId = taskId,
        ratioId = null,
        parentDispenseId = parentDispenseId,
        text = text ?: "",
        medicationCategory = category ?: "",
        form = form,
        vaccine = isVaccine ?: false,
        medicationProfile = null,
        manufacturingInstructions = when (this) {
            is FhirDispensedCompoundingMedicationErpModel -> contextualData.manufacturingInstructions ?: ""
            is DispensedEpaMedicationErpModel -> contextualData.manufacturingInstructions ?: ""
            else -> ""
        },
        packaging = when (this) {
            is FhirDispensedCompoundingMedicationErpModel -> contextualData.packaging ?: ""
            is DispensedEpaMedicationErpModel -> contextualData.packaging ?: ""
            else -> ""
        },
        normSizeCode = when (this) {
            is DispensedPznMedicationErpModel -> contextualData.normSizeCode ?: ""
            is DispensedEpaMedicationErpModel -> contextualData.normSizeCode ?: ""
            is DispensedIngredientMedicationErpModel -> contextualData.normSizeCode ?: ""
            else -> ""
        },
        amount = amount?.toRoomEntity(),
        identifier = null,
        pzn = this.toPzn()
    )

private fun FhirRatioErpModel?.toRoomEntity(): ErpRatioEmbeddable = ErpRatioEmbeddable(
    numerator = ErpQuantityEmbeddable(
        value = this?.numerator?.value,
        unit = this?.numerator?.unit
    ),
    denominator = ErpQuantityEmbeddable(
        value = this?.denominator?.value,
        unit = this?.denominator?.unit
    )
)

private fun DispensedMedicationErpModel.toPzn(): String = identifier?.pzn ?: ""

private fun DispensedMedicationErpModel.toDispenseType(): ErpMedicationDispenseType = when (this) {
    is DispensedEpaMedicationErpModel -> ErpMedicationDispenseType.Epa
    is DispensedIngredientMedicationErpModel -> ErpMedicationDispenseType.Ingredient
    is FhirDispensedCompoundingMedicationErpModel -> ErpMedicationDispenseType.Compounding
    is FhirDispensedFreeTextMedicationErpModel -> ErpMedicationDispenseType.Freetext
    is DispensedPznMedicationErpModel -> ErpMedicationDispenseType.Other
}

fun ErpCommunicationEntity.toErpModel(): CommunicationErpModel {
    return CommunicationErpModel(
        communicationId = communicationId,
        orderId = orderId,
        taskId = taskId,
        senderTelematikId = telematikId,
        consumed = consumed,
        payload = payload,
        profile = profile.toCommunicationProfile(),
        recipient = recipient,
        profileId = insuranceId,
        timeStamp = timeStamp,
        pharmacyName = pharmacyName
    )
}
