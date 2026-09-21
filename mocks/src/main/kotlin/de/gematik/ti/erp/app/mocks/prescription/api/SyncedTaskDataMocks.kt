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

package de.gematik.ti.erp.app.mocks.prescription.api

import de.gematik.ti.erp.app.fhir.prescription.model.ErpMedicationProfileType
import de.gematik.ti.erp.app.fhir.prescription.model.ErpMedicationProfileVersion
import de.gematik.ti.erp.app.fhir.prescription.model.FhirTaskKbvMedicationProfileErpModel
import de.gematik.ti.erp.app.fhir.temporal.FhirTemporal
import de.gematik.ti.erp.app.mocks.DATE_2024_01_01
import de.gematik.ti.erp.app.mocks.DATE_3024_01_01
import de.gematik.ti.erp.app.mocks.PROFILE_ID
import de.gematik.ti.erp.app.task.model.Quantity
import de.gematik.ti.erp.app.task.model.Ratio
import de.gematik.ti.erp.app.prescription.model.SyncedTaskData
import de.gematik.ti.erp.app.prescription.model.SyncedTaskData.Medication
import de.gematik.ti.erp.app.prescription.model.SyncedTaskData.MedicationRequest
import de.gematik.ti.erp.app.prescription.model.SyncedTaskData.Organization
import de.gematik.ti.erp.app.prescription.model.SyncedTaskData.Patient
import de.gematik.ti.erp.app.prescription.model.SyncedTaskData.Practitioner
import de.gematik.ti.erp.app.task.model.AccidentType
import de.gematik.ti.erp.app.task.model.AdditionalFeeErpModel
import de.gematik.ti.erp.app.task.model.AddressErpModel
import de.gematik.ti.erp.app.task.model.Identifier
import de.gematik.ti.erp.app.task.model.InsuranceErpModel
import de.gematik.ti.erp.app.task.model.InsuranceErpModelCoverageType
import de.gematik.ti.erp.app.task.model.MedicationCategory
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
import kotlinx.datetime.Clock
import kotlin.time.Duration.Companion.days

val ADDRESS = SyncedTaskData.Address(
    line1 = "Hauptstraße 1",
    line2 = "12345 Musterstadt",
    postalCode = "12345",
    city = "Musterstadt"
)

val PATIENT = Patient(
    name = "Erna Mustermann",
    address = ADDRESS,
    birthdate = null,
    insuranceIdentifier = "AOK"
)

val MEDICATION = Medication(
    category = SyncedTaskData.MedicationCategory.entries[0],
    medicationProfile = FhirTaskKbvMedicationProfileErpModel(
        type = ErpMedicationProfileType.PZN,
        version = ErpMedicationProfileVersion.V_110
    ),
    vaccine = true,
    text = "Medication",
    form = "AEO",
    lotNumber = "123456",
    expirationDate = FhirTemporal.Instant(Clock.System.now().plus(30.days)),
    identifier = SyncedTaskData.Identifier("1234567890"),
    normSizeCode = "KA",
    amount = Ratio(
        numerator = Quantity(
            value = "1",
            unit = "oz"
        ),
        denominator = null
    ),
    manufacturingInstructions = null,
    packaging = null,
    ingredients = emptyList(),
    ingredientMedications = emptyList()
)

val MEDICATION_10_TAB = Medication(
    category = SyncedTaskData.MedicationCategory.entries[0],
    medicationProfile = FhirTaskKbvMedicationProfileErpModel(
        type = ErpMedicationProfileType.PZN,
        version = ErpMedicationProfileVersion.V_110
    ),
    vaccine = true,
    text = "Medication",
    form = "TAB",
    lotNumber = "123456",
    expirationDate = FhirTemporal.Instant(Clock.System.now().plus(30.days)),
    identifier = SyncedTaskData.Identifier("1234567890"),
    normSizeCode = "KA",
    amount = Ratio(
        numerator = Quantity(
            value = "10",
            unit = "TAB"
        ),
        denominator = null
    ),
    manufacturingInstructions = null,
    packaging = null,
    ingredients = emptyList(),
    ingredientMedications = emptyList()
)

var MEDICATION_REQUEST = MedicationRequest(
    medication = MEDICATION,
    dateOfAccident = null,
    location = "Location",
    emergencyFee = true,
    dosageInstruction = "Dosage",
    multiplePrescriptionInfo = SyncedTaskData.MultiplePrescriptionInfo(),
    note = "Note",
    substitutionAllowed = true
)

var MEDICATION_REQUEST_DOSAGE_STRUCTURED_AMOUNT_10 = MedicationRequest(
    medication = MEDICATION_10_TAB,
    dateOfAccident = null,
    location = "Location",
    emergencyFee = true,
    dosageInstruction = "1-0-1-0",
    multiplePrescriptionInfo = SyncedTaskData.MultiplePrescriptionInfo(),
    note = "Note",
    substitutionAllowed = true,
    quantity = 1
)

val PRACTITIONER = Practitioner(
    name = "Dr. Max Mustermann",
    qualification = "Arzt",
    practitionerIdentifier = "1234567890"
)

val INSURANCE_INFO = SyncedTaskData.InsuranceInformation(
    name = "AOK",
    status = "status",
    coverageType = SyncedTaskData.CoverageType.GKV
)

internal val ORGANIZATION = Organization(
    name = "Praxis Dr. Mustermann",
    address = ADDRESS,
    uniqueIdentifier = "1234567890",
    phone = "0123456789",
    mail = "mustermann@praxis.de"
)

val API_ACTIVE_SYNCED_TASK = TaskErpModel.Synced.Prescription(
    profileId = PROFILE_ID,
    name = MEDICATION.name(),
    taskId = "active-synced-task-id-1",
    accessCode = "1234",
    isEuRedeemable = false,
    lastModified = DATE_2024_01_01,
    isEuRedeemableByPatientAuthorization = false,
    organization = OrganizationErpModel(
        name = ORGANIZATION.name,
        address = AddressErpModel(line1 = ADDRESS.line1, line2 = ADDRESS.line2, postalCode = ADDRESS.postalCode, city = ADDRESS.city),
        uniqueIdentifier = ORGANIZATION.uniqueIdentifier,
        phone = ORGANIZATION.phone,
        mail = ORGANIZATION.mail
    ),
    practitioner = PractitionerErpModel(
        name = PRACTITIONER.name,
        qualification = PRACTITIONER.qualification,
        practitionerIdentifier = PRACTITIONER.practitionerIdentifier,
        dentistIdentifier = null,
        telematikId = null
    ),
    patient = PatientErpModel(
        name = PATIENT.name,
        address = AddressErpModel(line1 = ADDRESS.line1, line2 = ADDRESS.line2, postalCode = ADDRESS.postalCode, city = ADDRESS.city),
        dateOfBirth = PATIENT.birthdate,
        insuranceIdentifier = PATIENT.insuranceIdentifier
    ),
    insuranceInformation = InsuranceErpModel(
        name = INSURANCE_INFO.name,
        status = INSURANCE_INFO.status,
        identifierNumber = INSURANCE_INFO.identifierNumber,
        coverageType = InsuranceErpModelCoverageType.GKV
    ),
    expiresOn = DATE_3024_01_01,
    acceptUntil = DATE_3024_01_01,
    authoredOn = DATE_2024_01_01,
    status = TaskStatusEnum.Ready,
    isIncomplete = false,
    pvsIdentifier = "pvsIdentifier",
    failureToReport = "failureToReport",
    medicationRequest = MedicationRequestErpModel(
        medication = MedicationErpModel(
            category = MedicationCategory.ARZNEI_UND_VERBAND_MITTEL,
            medicationProfile = FhirTaskKbvMedicationProfileErpModel(
                type = ErpMedicationProfileType.PZN,
                version = ErpMedicationProfileVersion.V_110
            ),
            isVaccine = true,
            text = "Medication",
            form = "AEO",
            lotNumber = "123456",
            expirationDate = FhirTemporal.Instant(Clock.System.now().plus(30.days)),
            identifier = Identifier(pzn = "1234567890"),
            normSizeCode = "KA",
            amount = RatioErpModel(numerator = QuantityErpModel("1", "oz"), denominator = null),
            manufacturingInstructions = null,
            packaging = null,
            ingredientMedications = emptyList(),
            ingredients = emptyList()
        ),
        authoredOn = null,
        dateOfAccident = null,
        accidentType = AccidentType.None,
        location = "Location",
        emergencyFee = true,
        substitutionAllowed = true,
        dosageInstruction = "Dosage",
        multiplePrescriptionInfo = MultiplePrescriptionInfo(),
        quantity = 0,
        note = "Note",
        bvg = null,
        additionalFee = AdditionalFeeErpModel.None
    ),
    medicationDispenses = emptyList(),
    lastMedicationDispense = null
)

val API_ACTIVE_SYNCED_TASK_STRUCTURED_DOSAGE = API_ACTIVE_SYNCED_TASK.copy(
    medicationRequest = API_ACTIVE_SYNCED_TASK.medicationRequest?.copy(
        medication = API_ACTIVE_SYNCED_TASK.medicationRequest!!.medication?.copy(
            text = "Medication",
            form = "TAB",
            amount = RatioErpModel(numerator = QuantityErpModel("10", "TAB"), denominator = null)
        ),
        dosageInstruction = "1-0-1-0",
        quantity = 1
    )
)

val API_ARCHIVE_SYNCED_TASK = API_ACTIVE_SYNCED_TASK.copy(
    taskId = "archive-synced-task-id-1",
    acceptUntil = DATE_2024_01_01,
    status = TaskStatusEnum.Completed
    // redeemedOn = DATE_2024_01_01
)

val API_ACTIVE_DIGA_TASK = TaskErpModel.Synced.Diga(
    profileId = PROFILE_ID,
    name = "Diga Task",
    taskId = "active-diga-task-id-1",
    accessCode = "access-code-diga",
    isEuRedeemable = false,
    lastModified = DATE_3024_01_01,
    isEuRedeemableByPatientAuthorization = false,
    organization = null,
    practitioner = null,
    patient = null,
    insuranceInformation = null,
    expiresOn = DATE_3024_01_01,
    acceptUntil = DATE_3024_01_01,
    authoredOn = DATE_3024_01_01,
    status = TaskStatusEnum.Ready,
    isIncomplete = false,
    pvsIdentifier = "",
    failureToReport = ""
)

val API_ARCHIVE_DIGA_TASK = API_ACTIVE_DIGA_TASK.copy(
    taskId = "archive-diga-task-id-1",
    acceptUntil = DATE_2024_01_01,
    status = TaskStatusEnum.Completed
)
