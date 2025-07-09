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

package de.gematik.ti.erp.app.medicationplan.ui.preview.mocks

import de.gematik.ti.erp.app.medicationplan.ui.preview.medicationPlanPreviewCurrentTime
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
    vaccine = true,
    text = "Medication",
    form = "AEO",
    lotNumber = "123456",
    expirationDate = de.gematik.ti.erp.app.utils.FhirTemporal.Instant(Clock.System.now().plus(30.days)),
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
    ingredientMedications = emptyList(),
    ingredients = emptyList()
)

private val MEDICATION_10_TAB = MEDICATION.copy(
    form = "TAB",
    amount = Ratio(
        numerator = Quantity(
            value = "10",
            unit = "TAB"
        ),
        denominator = null
    )
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

internal var MEDICATION_REQUEST_DOSAGE_STRUCTURED_AMOUNT_20 = MEDICATION_REQUEST.copy(
    medication = MEDICATION_10_TAB,
    dosageInstruction = "1-0-1-0"
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

val SYNCED_TASK = SyncedTaskData.SyncedTask(
    profileId = "PROFILE_ID",
    taskId = "active-synced-task-id-1",
    accessCode = "1234",
    lastModified = medicationPlanPreviewCurrentTime,
    organization = ORGANIZATION,
    practitioner = PRACTITIONER,
    patient = PATIENT,
    insuranceInformation = INSURANCE_INFO,
    expiresOn = medicationPlanPreviewCurrentTime.plus(30.days),
    acceptUntil = medicationPlanPreviewCurrentTime.plus(27.days),
    authoredOn = medicationPlanPreviewCurrentTime,
    status = SyncedTaskData.TaskStatus.Ready,
    isIncomplete = false,
    pvsIdentifier = "pvsIdentifier",
    failureToReport = "failureToReport",
    medicationRequest = MEDICATION_REQUEST,
    medicationDispenses = emptyList(),
    lastMedicationDispense = null,
    communications = emptyList()
)

val SYNCED_TASK_STRUCTURED_DOSAGE = SYNCED_TASK.copy(
    medicationRequest = MEDICATION_REQUEST_DOSAGE_STRUCTURED_AMOUNT_20
)
