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
package de.gematik.ti.erp.app.database.realm.v1.task.mappers

import de.gematik.ti.erp.app.database.api.model.PrescriptionDataNotFoundException
import de.gematik.ti.erp.app.database.realm.utils.toInstant
import de.gematik.ti.erp.app.database.realm.v1.AddressEntityV1
import de.gematik.ti.erp.app.database.realm.v1.task.entity.AccidentTypeV1
import de.gematik.ti.erp.app.database.realm.v1.task.entity.CoverageTypeV1
import de.gematik.ti.erp.app.database.realm.v1.task.entity.DeviceRequestDispenseEntityV1
import de.gematik.ti.erp.app.database.realm.v1.task.entity.IdentifierEntityV1
import de.gematik.ti.erp.app.database.realm.v1.task.entity.IngredientEntityV1
import de.gematik.ti.erp.app.database.realm.v1.task.entity.InsuranceInformationEntityV1
import de.gematik.ti.erp.app.database.realm.v1.task.entity.MedicationCategoryV1
import de.gematik.ti.erp.app.database.realm.v1.task.entity.MedicationDispenseEntityV1
import de.gematik.ti.erp.app.database.realm.v1.task.entity.MedicationEntityV1
import de.gematik.ti.erp.app.database.realm.v1.task.entity.MedicationRequestEntityV1
import de.gematik.ti.erp.app.database.realm.v1.task.entity.MultiplePrescriptionInfoEntityV1
import de.gematik.ti.erp.app.database.realm.v1.task.entity.OrganizationEntityV1
import de.gematik.ti.erp.app.database.realm.v1.task.entity.PatientEntityV1
import de.gematik.ti.erp.app.database.realm.v1.task.entity.PractitionerEntityV1
import de.gematik.ti.erp.app.database.realm.v1.task.entity.QuantityEntityV1
import de.gematik.ti.erp.app.database.realm.v1.task.entity.RatioEntityV1
import de.gematik.ti.erp.app.database.realm.v1.task.entity.ScannedTaskEntityV1
import de.gematik.ti.erp.app.database.realm.v1.task.entity.SyncedTaskEntityV1
import de.gematik.ti.erp.app.database.realm.v1.task.entity.TaskStatusV1
import de.gematik.ti.erp.app.database.realm.v1.task.entity.TeratogenicPrescriptionEntityV1
import de.gematik.ti.erp.app.database.realm.v1.task.mappers.ErpDigaMappers.toErpModel
import de.gematik.ti.erp.app.database.room.v2.task.mappers.toErpModel
import de.gematik.ti.erp.app.fhir.dispense.model.FhirDispenseDeviceRequestErpModel
import de.gematik.ti.erp.app.fhir.prescription.model.FhirTaskStatusErpModel
import de.gematik.ti.erp.app.fhir.temporal.asFhirTemporal
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
import de.gematik.ti.erp.app.task.model.TaskStatusEnum
import de.gematik.ti.erp.app.task.model.TeratogenicPrescriptionErpModel
import io.github.aakira.napier.Napier
import kotlinx.datetime.Clock

internal fun AddressEntityV1.toErpModel(): AddressErpModel {
    return AddressErpModel(
        line1 = this.line1,
        line2 = this.line2,
        postalCode = this.postalCode,
        additionalAddressInformation = this.additionalAddressInformation,
        city = this.city
    )
}

internal fun InsuranceInformationEntityV1.toErpModel(): InsuranceErpModel {
    return InsuranceErpModel(
        name = this.name,
        status = this.statusCode,
        identifierNumber = this.identifierNumber,
        coverageType = this.coverageType.toCoverageTypeEnum()
    )
}

internal fun PractitionerEntityV1.toErpModel(): PractitionerErpModel {
    return PractitionerErpModel(
        name = this.name,
        qualification = this.qualification,
        practitionerIdentifier = this.practitionerIdentifier,
        dentistIdentifier = this.dentistIdentifier,
        telematikId = this.telematikId
    )
}

internal fun PatientEntityV1.toErpModel(): PatientErpModel {
    return PatientErpModel(
        name = this.name,
        dateOfBirth = this.dateOfBirth,
        insuranceIdentifier = this.insuranceIdentifier,
        address = this.address?.toErpModel()
    )
}

internal fun OrganizationEntityV1.toErpModel(): OrganizationErpModel {
    return OrganizationErpModel(
        name = this.name,
        address = this.address?.toErpModel(),
        uniqueIdentifier = this.uniqueIdentifier,
        phone = this.phone,
        mail = this.mail
    )
}

internal fun DeviceRequestDispenseEntityV1.toErpModel() = FhirDispenseDeviceRequestErpModel(
    deepLink = deepLink,
    redeemCode = redeemCode,
    declineCode = declineCode,
    modifiedDate = modifiedDate?.toInstant()?.asFhirTemporal(),
    note = note,
    referencePzn = referencePzn,
    display = display,
    status = status
)

internal fun MedicationDispenseEntityV1.toErpModel() = MedicationDispenseErpModel(
    dispenseId = dispenseId,
    patientIdentifier = patientIdentifier,
    medication = medication?.toErpModel(),
    deviceRequest = deviceRequest?.toErpModel(),
    wasSubstituted = wasSubstituted,
    dosageInstruction = dosageInstruction,
    performer = performer,
    whenHandedOver = handedOverOn,
    pharmacyName = pharmacyName,
    euCountryCode = euCountryCode
)

internal fun SyncedTaskEntityV1.toErpModel(): TaskErpModel {
    return when {
        this.deviceRequest != null -> this.toDigaErpModel()
        else -> this.toPrescriptionErpModel()
    }
}

internal fun QuantityEntityV1.toErpModel() = QuantityErpModel(
    value = value,
    unit = unit
)

internal fun RatioEntityV1.toErpModel() = RatioErpModel(
    numerator = numerator?.toErpModel(),
    denominator = denominator?.toErpModel()
)

internal fun IdentifierEntityV1.toErpModel() = Identifier(
    pzn = pzn,
    atc = atc,
    ask = ask,
    snomed = snomed
)

internal fun IngredientEntityV1.toErpModel() = Ingredient(
    text = text,
    form = form,
    number = number,
    amount = amount,
    strength = strength?.toErpModel()
)

internal fun MedicationEntityV1.toErpModel(): MedicationErpModel = MedicationErpModel(
    category = medicationCategory.toMedicationCategoryEnum(),
    medicationProfile = null, // V1 doesn't have the full profile
    isVaccine = vaccine,
    text = text,
    form = form,
    lotNumber = lotNumber,
    expirationDate = expirationDate,
    identifier = identifier?.toErpModel() ?: Identifier(),
    normSizeCode = normSizeCode,
    amount = amount?.toErpModel(),
    manufacturingInstructions = manufacturingInstructions,
    packaging = packaging,
    ingredientMedications = ingredientMedications.map { it.toErpModel() },
    ingredients = ingredients.map { it.toErpModel() }
)

internal fun MultiplePrescriptionInfoEntityV1.toErpModel() = MultiplePrescriptionInfo(
    indicator = indicator,
    numbering = numbering?.toErpModel(),
    start = start?.toInstant(),
    end = end?.toInstant()
)

internal fun MedicationRequestEntityV1.toErpModel() = MedicationRequestErpModel(
    medication = medication?.toErpModel(),
    authoredOn = authoredOn,
    dateOfAccident = dateOfAccident?.toInstant(),
    accidentType = accidentType.toAccidentTypeEnum(),
    location = location,
    emergencyFee = emergencyFee,
    substitutionAllowed = substitutionAllowed,
    dosageInstruction = dosageInstruction,
    multiplePrescriptionInfo = multiplePrescriptionInfo?.toErpModel() ?: MultiplePrescriptionInfo(),
    quantity = quantity,
    note = note,
    bvg = bvg,
    additionalFee = AdditionalFeeErpModel.valueOf(additionalFee),
    teratogenicPrescription = teratogenicPrescription?.toErpModel()
)

internal fun TeratogenicPrescriptionEntityV1.toErpModel() = TeratogenicPrescriptionErpModel(
    offLabel = offLabel,
    gebaerfaehigeFrau = gebaerfaehigeFrau,
    einhaltungSicherheitsmassnahmen = einhaltungSicherheitsmassnahmen,
    aushaendigungInformationsmaterialien = aushaendigungInformationsmaterialien,
    erklaerungSachkenntnis = erklaerungSachkenntnis
)

internal fun MedicationCategoryV1.toMedicationCategoryEnum() = when (this) {
    MedicationCategoryV1.ARZNEI_UND_VERBAND_MITTEL -> MedicationCategory.ARZNEI_UND_VERBAND_MITTEL
    MedicationCategoryV1.BTM -> MedicationCategory.BTM
    MedicationCategoryV1.AMVV -> MedicationCategory.AMVV
    MedicationCategoryV1.SONSTIGES -> MedicationCategory.SONSTIGES
    MedicationCategoryV1.UNKNOWN -> MedicationCategory.UNKNOWN
}

internal fun AccidentTypeV1.toAccidentTypeEnum() = when (this) {
    AccidentTypeV1.Unfall -> AccidentType.Unfall
    AccidentTypeV1.Arbeitsunfall -> AccidentType.Arbeitsunfall
    AccidentTypeV1.Berufskrankheit -> AccidentType.Berufskrankheit
    AccidentTypeV1.None -> AccidentType.None
}

internal fun FhirTaskStatusErpModel.toTaskStatusV1(): TaskStatusV1 = try {
    TaskStatusV1.valueOf(this.name)
} catch (e: Exception) {
    Napier.e { "Error converting FhirTaskStatusErpModel $name to TaskStatusV1: ${e.message}" }
    TaskStatusV1.Other
}

internal fun SyncedTaskEntityV1.toPrescriptionErpModel(): TaskErpModel.Synced.Prescription {
    val profileId = this.profileIdOrThrow()
    return TaskErpModel.Synced.Prescription(
        profileId = profileId,
        name = this.resolveName(),
        taskId = this.taskId,
        accessCode = this.accessCode,
        isEuRedeemable = this.isEuRedeemableByProperties,
        lastModified = this.lastModified.toInstant(),
        isEuRedeemableByPatientAuthorization = this.isEuRedeemableByPatientAuthorization,
        organization = this.organization?.toErpModel(),
        practitioner = this.practitioner?.toErpModel(),
        patient = this.patient?.toErpModel(),
        insuranceInformation = this.insuranceInformation?.toErpModel(),
        expiresOn = this.expiresOn?.toInstant(),
        acceptUntil = this.acceptUntil?.toInstant(),
        authoredOn = this.authoredOn.toInstant(),
        status = this.status.toTaskStatusEnum(),
        isIncomplete = this.isIncomplete,
        pvsIdentifier = this.pvsIdentifier,
        failureToReport = this.failureToReport,
        currentTime = Clock.System.now(),
        medicationRequest = this.medicationRequest?.toErpModel() ?: this.medication?.let {
            // fallback for metadata only tasks
            MedicationRequestErpModel(
                medication = it.toErpModel(),
                authoredOn = null,
                dateOfAccident = null,
                accidentType = AccidentType.None,
                location = null,
                emergencyFee = null,
                substitutionAllowed = false,
                dosageInstruction = null,
                multiplePrescriptionInfo = MultiplePrescriptionInfo(),
                quantity = 0,
                note = null,
                bvg = false,
                additionalFee = AdditionalFeeErpModel.None
            )
        },
        medicationDispenses = this.medicationDispenses.map { it.toErpModel() },
        lastMedicationDispense = this.lastMedicationDispense?.toInstant(),
        communications = this.communications.map { it.toErpModel(profileId) }
    )
}

internal fun SyncedTaskEntityV1.toDigaErpModel(): TaskErpModel.Synced.Diga {
    val profileId = this.profileIdOrThrow()
    val deviceRequestErp = this.deviceRequest?.toErpModel(this.lastModified.toInstant())
    return TaskErpModel.Synced.Diga(
        profileId = profileId,
        taskId = this.taskId,
        accessCode = this.accessCode,
        isEuRedeemable = this.isEuRedeemableByProperties,
        isEuRedeemableByPatientAuthorization = this.isEuRedeemableByPatientAuthorization,
        lastModified = this.lastModified.toInstant(),
        name = deviceRequestErp?.appName,
        // lastMedicationDispense = this.lastMedicationDispense?.toInstant(), // todo : do we not need this?
        expiresOn = this.expiresOn?.toInstant(),
        acceptUntil = this.acceptUntil?.toInstant(),
        authoredOn = this.authoredOn.toInstant(),
        isIncomplete = this.isIncomplete,
        pvsIdentifier = this.pvsIdentifier,
        failureToReport = this.failureToReport,
        organization = this.organization?.toErpModel(),
        practitioner = this.practitioner?.toErpModel(),
        patient = this.patient?.toErpModel(),
        insuranceInformation = this.insuranceInformation?.toErpModel(),
        deviceRequest = deviceRequestErp,
        medicationDispenses = this.medicationDispenses.map { it.toErpModel() },
        status = this.status.toTaskStatusEnum(),
        communications = this.communications.map { it.toErpModel(profileId) }
    )
}

fun joinIngredientNames(ingredients: List<String>?) = ingredients?.joinToString(", ") { it } ?: ""

private fun TaskStatusV1.toTaskStatusEnum(): TaskStatusEnum = try {
    TaskStatusEnum.valueOf(this.name)
} catch (e: Exception) {
    Napier.e { "Error converting TaskStatusV1 $name to TaskStatusEnum: ${e.message}" }
    TaskStatusEnum.Other
}

private fun CoverageTypeV1.toCoverageTypeEnum(): InsuranceErpModelCoverageType = try {
    InsuranceErpModelCoverageType.valueOf(name)
} catch (e: Exception) {
    Napier.e { "Error converting coverageType $name to enum: ${e.message}" }
    InsuranceErpModelCoverageType.UNKNOWN
}

internal fun ScannedTaskEntityV1.toErpModel(): TaskErpModel {
    val profileId = this.parent?.id ?: throw PrescriptionDataNotFoundException("ProfileEntity for scanned ${this.taskId} not found in realm database")
    return TaskErpModel.Scanned(
        profileId = profileId,
        taskId = this.taskId,
        redeemedOn = this.redeemedOn?.toInstant(),
        accessCode = this.accessCode,
        name = this.name,
        index = this.index,
        scannedOn = this.scannedOn.toInstant(),
        isEuRedeemable = false,
        communications = this.communications.map { it.toErpModel(profileId) }
    )
}

private fun SyncedTaskEntityV1.profileIdOrThrow(): String =
    this.parent?.id ?: throw PrescriptionDataNotFoundException("ProfileEntity for synced ${this.taskId} not found in realm database")

private fun SyncedTaskEntityV1.resolveName(): String? {
    val medicationText = medicationRequest?.medication?.text ?: medication?.text
    return if (medicationText.isNullOrBlank()) {
        val ingredients = (medicationRequest?.medication?.ingredients ?: medication?.ingredients)?.map { it.text }
        joinIngredientNames(ingredients)
    } else {
        medicationText
    }
}
