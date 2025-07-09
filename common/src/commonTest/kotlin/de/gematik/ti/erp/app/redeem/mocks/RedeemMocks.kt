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

package de.gematik.ti.erp.app.redeem.mocks

import de.gematik.ti.erp.app.prescription.model.SyncedTaskData
import kotlinx.datetime.Instant

private const val MOCK_PRACTITIONER_NAME = "Dr. John Doe"

private val MOCK_ORGANIZATION = SyncedTaskData.Organization(
    name = "TestOrganization",
    address = SyncedTaskData.Address(
        line1 = "123 Main Street",
        line2 = "Apt 4",
        postalCode = "12345",
        city = "City"
    ),
    uniqueIdentifier = "org123",
    phone = "123-456-7890",
    mail = "info@testorg.com"
)

val MOCK_PRACTITIONER = SyncedTaskData.Practitioner(
    name = MOCK_PRACTITIONER_NAME,
    qualification = "",
    practitionerIdentifier = " "
)

private val MOCK_PATIENT = SyncedTaskData.Patient(
    name = "Jane",
    address = SyncedTaskData.Address(
        line1 = "",
        line2 = "",
        postalCode = "",
        city = ""
    ),
    birthdate = null,
    insuranceIdentifier = "ins123"
)

private val MOCK_MEDICATION_REQ = SyncedTaskData.MedicationRequest(
    null, null, null, SyncedTaskData.AccidentType.None,
    null, null, false, null,
    SyncedTaskData.MultiplePrescriptionInfo(false), 1, null, null, SyncedTaskData.AdditionalFee.None
)

private val VALID_DIGA_COVERAGE = SyncedTaskData.InsuranceInformation(
    name = "TestInsurance",
    status = "Active",
    identifierNumber = "identifier-for-insurance-provider",
    coverageType = SyncedTaskData.CoverageType.GKV
)

internal val INVALID_DIGA_COVERAGE = SyncedTaskData.InsuranceInformation(
    name = "TestInsurance",
    status = "Active",
    identifierNumber = null,
    coverageType = SyncedTaskData.CoverageType.GKV
)

internal val MOCK_SYNCED_TASK_DATA_DIGA = SyncedTaskData.SyncedTask(
    profileId = "testProfileId",
    taskId = "testId1",
    accessCode = "testAccessCode",
    lastModified = Instant.fromEpochSeconds(123456),
    organization = MOCK_ORGANIZATION,
    practitioner = MOCK_PRACTITIONER,
    patient = MOCK_PATIENT,
    insuranceInformation = VALID_DIGA_COVERAGE,
    expiresOn = Instant.fromEpochSeconds(123456),
    acceptUntil = Instant.fromEpochSeconds(123456),
    authoredOn = Instant.fromEpochSeconds(123456),
    status = SyncedTaskData.TaskStatus.Ready,
    isIncomplete = false,
    pvsIdentifier = "testPvsIdentifier",
    failureToReport = "testFailureToReport",
    medicationRequest = MOCK_MEDICATION_REQ,
    lastMedicationDispense = null,
    medicationDispenses = emptyList(),
    deviceRequest = null,
    communications = emptyList()
)
