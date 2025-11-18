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

package de.gematik.ti.erp.app.medicationplan

import de.gematik.ti.erp.app.fhir.prescription.model.ErpMedicationProfileType
import de.gematik.ti.erp.app.fhir.prescription.model.ErpMedicationProfileVersion
import de.gematik.ti.erp.app.fhir.prescription.model.FhirTaskKbvMedicationProfileErpModel
import de.gematik.ti.erp.app.fhir.temporal.FhirTemporal
import de.gematik.ti.erp.app.fhir.temporal.toLocalDate
import de.gematik.ti.erp.app.medicationplan.model.MedicationNotificationMessage
import de.gematik.ti.erp.app.medicationplan.model.MedicationSchedule
import de.gematik.ti.erp.app.medicationplan.model.MedicationScheduleNotification
import de.gematik.ti.erp.app.medicationplan.model.MedicationScheduleNotificationDosage
import de.gematik.ti.erp.app.prescription.model.Quantity
import de.gematik.ti.erp.app.prescription.model.Ratio
import de.gematik.ti.erp.app.prescription.model.ScannedTaskData
import de.gematik.ti.erp.app.prescription.model.SyncedTaskData
import de.gematik.ti.erp.app.prescription.model.SyncedTaskData.MedicationRequest
import de.gematik.ti.erp.app.prescription.model.SyncedTaskData.Organization
import de.gematik.ti.erp.app.prescription.model.SyncedTaskData.Patient
import de.gematik.ti.erp.app.prescription.model.SyncedTaskData.Practitioner
import de.gematik.ti.erp.app.profiles.model.ProfilesData
import io.mockk.mockk
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalTime
import kotlin.time.Duration.Companion.days

val scannedTaskAmount = Ratio(Quantity("1", ""), Quantity("1", ""))
val syncedTaskAmount = Ratio(Quantity("1", ""), Quantity("1", ""))

val MEDICATION_SCHEDULE = MedicationSchedule(
    duration = de.gematik.ti.erp.app.medicationplan.model.MedicationScheduleDuration.Personalized(
        startDate = Instant.parse("2024-01-01T00:00:00Z").toLocalDate(),
        endDate = Instant.parse("2024-01-01T20:00:00Z").toLocalDate()
    ),
    interval = de.gematik.ti.erp.app.medicationplan.model.MedicationScheduleInterval.Daily,
    isActive = true,
    message = MedicationNotificationMessage("title", "body"),
    taskId = "taskId",
    profileId = "profileId",
    amount = scannedTaskAmount,
    notifications = listOf(
        MedicationScheduleNotification(
            time = LocalTime(8, 0),
            dosage = MedicationScheduleNotificationDosage("tablet", "1"),
            id = "1234"
        )
    )
)

val profile1 = ProfilesData.Profile(
    id = "PROFILE_ID1",
    color = ProfilesData.ProfileColorNames.PINK,
    avatar = ProfilesData.Avatar.Baby,
    image = null,
    name = "Erna Mustermann",
    insurantName = "Erna Mustermann",
    insuranceIdentifier = "AOK",
    insuranceType = ProfilesData.InsuranceType.GKV,
    isConsentDrawerShown = true,
    lastAuthenticated = mockk(),
    lastTaskSynced = mockk(),
    active = true,
    singleSignOnTokenScope = null
)

val profile2 = ProfilesData.Profile(
    id = "PROFILE_ID2",
    color = ProfilesData.ProfileColorNames.PINK,
    avatar = ProfilesData.Avatar.Baby,
    image = null,
    name = "Erna P",
    insurantName = "Erna P",
    insuranceIdentifier = "AOK",
    insuranceType = ProfilesData.InsuranceType.GKV,
    isConsentDrawerShown = true,
    lastAuthenticated = mockk(),
    lastTaskSynced = mockk(),
    active = true,
    singleSignOnTokenScope = null
)

val medicationSchedule1 = MedicationSchedule(
    duration = de.gematik.ti.erp.app.medicationplan.model.MedicationScheduleDuration.Personalized(
        startDate = Instant.parse("2024-01-01T08:00:00Z").toLocalDate(),
        endDate = Instant.parse("2024-01-01T20:00:00Z").toLocalDate()
    ),
    interval = de.gematik.ti.erp.app.medicationplan.model.MedicationScheduleInterval.Daily,
    isActive = true,
    message = MedicationNotificationMessage("title", "body"),
    taskId = "taskId",
    profileId = "PROFILE_ID1",
    amount = scannedTaskAmount,
    notifications = listOf(
        MedicationScheduleNotification(id = "1", time = LocalTime(8, 0), dosage = MedicationScheduleNotificationDosage("Dosis", "1")),
        MedicationScheduleNotification(id = "2", time = LocalTime(12, 0), dosage = MedicationScheduleNotificationDosage("Dosis", "1"))
    )
)

val medicationSchedule2 = MedicationSchedule(
    duration = de.gematik.ti.erp.app.medicationplan.model.MedicationScheduleDuration.Personalized(
        startDate = Instant.parse("2024-01-01T08:00:00Z").toLocalDate(),
        endDate = Instant.parse("2024-01-01T20:00:00Z").toLocalDate()
    ),
    interval = de.gematik.ti.erp.app.medicationplan.model.MedicationScheduleInterval.Daily,
    isActive = true,
    message = MedicationNotificationMessage("title", "body"),
    taskId = "taskId",
    profileId = "PROFILE_ID2",
    amount = syncedTaskAmount,
    notifications = listOf(
        MedicationScheduleNotification(id = "3", time = LocalTime(10, 5), dosage = MedicationScheduleNotificationDosage("Dosis", "1")),
        MedicationScheduleNotification(id = "4", time = LocalTime(18, 0), dosage = MedicationScheduleNotificationDosage("Dosis", "1"))
    )
)

private val MEDICATION = SyncedTaskData.Medication(
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
    ingredientMedications = emptyList(),
    ingredients = emptyList(),
    normSizeCode = "KA",
    amount = Ratio(
        numerator = Quantity(
            value = "1",
            unit = "oz"
        ),
        denominator = null
    ),
    manufacturingInstructions = null,
    packaging = null
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

internal val ADDRESS = SyncedTaskData.Address(
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

internal val ORGANIZATION = Organization(
    name = "Praxis Dr. Mustermann",
    address = ADDRESS,
    uniqueIdentifier = "1234567890",
    phone = "0123456789",
    mail = "mustermann@praxis.de"
)

val scannedTask = ScannedTaskData.ScannedTask(
    profileId = "PROFILE_ID",
    taskId = "active-scanned-task-id-1",
    index = 0,
    name = "Scanned Task",
    accessCode = "1234",
    scannedOn = Instant.parse("2024-01-01T08:00:00Z"),
    redeemedOn = null,
    communications = emptyList()
)

val syncedTask = SyncedTaskData.SyncedTask(
    profileId = "PROFILE_ID",
    taskId = "active-synced-task-id-1",
    accessCode = "1234",
    lastModified = Instant.parse("2024-01-01T08:00:00Z"),
    organization = ORGANIZATION,
    practitioner = PRACTITIONER,
    patient = PATIENT,
    insuranceInformation = INSURANCE_INFO,
    expiresOn = Instant.parse("2024-01-01T08:00:00Z"),
    acceptUntil = Instant.parse("2024-01-01T08:00:00Z"),
    authoredOn = Instant.parse("2024-01-01T08:00:00Z"),
    status = SyncedTaskData.TaskStatus.Ready, // to be active prescription
    isIncomplete = false,
    pvsIdentifier = "pvsIdentifier",
    failureToReport = "failureToReport",
    medicationRequest = MEDICATION_REQUEST,
    medicationDispenses = emptyList(),
    lastMedicationDispense = null,
    communications = emptyList(),
    isEuRedeemable = false,
    isEuRedeemableByPatientAuthorization = false
)
