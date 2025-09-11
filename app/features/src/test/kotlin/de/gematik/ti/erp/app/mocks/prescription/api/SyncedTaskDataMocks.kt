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
import de.gematik.ti.erp.app.prescription.model.Quantity
import de.gematik.ti.erp.app.prescription.model.Ratio
import de.gematik.ti.erp.app.prescription.model.SyncedTaskData
import de.gematik.ti.erp.app.prescription.model.SyncedTaskData.Medication
import de.gematik.ti.erp.app.prescription.model.SyncedTaskData.MedicationRequest
import de.gematik.ti.erp.app.prescription.model.SyncedTaskData.Organization
import de.gematik.ti.erp.app.prescription.model.SyncedTaskData.Patient
import de.gematik.ti.erp.app.prescription.model.SyncedTaskData.Practitioner
import kotlinx.datetime.Clock
import kotlin.time.Duration.Companion.days

private val ADDRESS = SyncedTaskData.Address(
    line1 = "Hauptstraße 1",
    line2 = "12345 Musterstadt",
    postalCode = "12345",
    city = "Musterstadt"
)

internal val PATIENT = Patient(
    name = "Erna Mustermann",
    address = ADDRESS,
    birthdate = null,
    insuranceIdentifier = "AOK"
)

private val MEDICATION = Medication(
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

private val MEDICATION_10_TAB = Medication(
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

internal var MEDICATION_REQUEST = MedicationRequest(
    medication = MEDICATION,
    dateOfAccident = null,
    location = "Location",
    emergencyFee = true,
    dosageInstruction = "Dosage",
    multiplePrescriptionInfo = SyncedTaskData.MultiplePrescriptionInfo(),
    note = "Note",
    substitutionAllowed = true
)

internal var MEDICATION_REQUEST_DOSAGE_STRUCTURED_AMOUNT_10 = MedicationRequest(
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

internal val PRACTITIONER = Practitioner(
    name = "Dr. Max Mustermann",
    qualification = "Arzt",
    practitionerIdentifier = "1234567890"
)

internal val INSURANCE_INFO = SyncedTaskData.InsuranceInformation(
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

val API_ACTIVE_SYNCED_TASK = SyncedTaskData.SyncedTask(
    profileId = PROFILE_ID,
    taskId = "active-synced-task-id-1",
    accessCode = "1234",
    lastModified = DATE_2024_01_01,
    organization = ORGANIZATION,
    practitioner = PRACTITIONER,
    patient = PATIENT,
    insuranceInformation = INSURANCE_INFO,
    expiresOn = DATE_3024_01_01,
    acceptUntil = DATE_3024_01_01,
    authoredOn = DATE_2024_01_01,
    status = SyncedTaskData.TaskStatus.Ready, // to be active prescription
    isIncomplete = false,
    pvsIdentifier = "pvsIdentifier",
    failureToReport = "failureToReport",
    medicationRequest = MEDICATION_REQUEST,
    medicationDispenses = emptyList(),
    lastMedicationDispense = null,
    communications = emptyList(),
    isEuRedeemable = false
)

val API_ACTIVE_SYNCED_TASK_STRUCTURED_DOSAGE = SyncedTaskData.SyncedTask(
    profileId = PROFILE_ID,
    taskId = "active-synced-task-id-1",
    accessCode = "1234",
    lastModified = DATE_2024_01_01,
    organization = ORGANIZATION,
    practitioner = PRACTITIONER,
    patient = PATIENT,
    insuranceInformation = INSURANCE_INFO,
    expiresOn = DATE_3024_01_01,
    acceptUntil = DATE_3024_01_01,
    authoredOn = DATE_2024_01_01,
    status = SyncedTaskData.TaskStatus.Ready, // to be active prescription
    isIncomplete = false,
    pvsIdentifier = "pvsIdentifier",
    failureToReport = "failureToReport",
    medicationRequest = MEDICATION_REQUEST_DOSAGE_STRUCTURED_AMOUNT_10,
    medicationDispenses = emptyList(),
    lastMedicationDispense = null,
    communications = emptyList(),
    isEuRedeemable = false
)

val API_ARCHIVE_SYNCED_TASK = SyncedTaskData.SyncedTask(
    profileId = PROFILE_ID,
    taskId = "archive-synced-task-id-1",
    accessCode = "1234",
    lastModified = DATE_2024_01_01,
    organization = ORGANIZATION,
    practitioner = PRACTITIONER,
    patient = PATIENT,
    insuranceInformation = INSURANCE_INFO,
    expiresOn = DATE_3024_01_01,
    acceptUntil = DATE_2024_01_01,
    authoredOn = DATE_2024_01_01,
    status = SyncedTaskData.TaskStatus.Completed,
    isIncomplete = false,
    pvsIdentifier = "pvsIdentifier",
    failureToReport = "failureToReport",
    medicationRequest = MEDICATION_REQUEST,
    medicationDispenses = emptyList(),
    lastMedicationDispense = null,
    communications = emptyList(),
    isEuRedeemable = false
)
