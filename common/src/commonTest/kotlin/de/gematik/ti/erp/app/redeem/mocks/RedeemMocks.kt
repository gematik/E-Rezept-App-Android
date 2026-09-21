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

import de.gematik.ti.erp.app.task.model.InsuranceErpModel
import de.gematik.ti.erp.app.task.model.InsuranceErpModelCoverageType
import de.gematik.ti.erp.app.task.model.OrganizationErpModel
import de.gematik.ti.erp.app.task.model.PatientErpModel
import de.gematik.ti.erp.app.task.model.PractitionerErpModel
import de.gematik.ti.erp.app.task.model.TaskErpModel
import de.gematik.ti.erp.app.task.model.TaskStatusEnum
import kotlinx.datetime.Instant

private val MOCK_LAST_MODIFIED = Instant.fromEpochSeconds(123456)
private val MOCK_FUTURE_INSTANT = Instant.parse("2099-01-01T00:00:00Z")
private val MOCK_CURRENT_TIME = Instant.parse("2021-01-01T00:00:00Z")

internal val MOCK_SYNCED_TASK_DATA_DIGA = TaskErpModel.Synced.Prescription(
    profileId = "testProfileId",
    name = null,
    taskId = "testId1",
    accessCode = "testAccessCode",
    isEuRedeemable = false,
    isEuRedeemableByPatientAuthorization = false,
    lastModified = MOCK_LAST_MODIFIED,
    organization = OrganizationErpModel(
        name = "TestOrganization",
        address = null,
        uniqueIdentifier = "org123",
        phone = "123-456-7890",
        mail = "info@testorg.com"
    ),
    practitioner = PractitionerErpModel(
        name = "Dr. John Doe",
        qualification = "",
        practitionerIdentifier = " ",
        dentistIdentifier = null,
        telematikId = null
    ),
    patient = PatientErpModel(
        name = "Jane",
        address = null,
        dateOfBirth = null,
        insuranceIdentifier = "ins123"
    ),
    insuranceInformation = InsuranceErpModel(
        name = "TestInsurance",
        status = "Active",
        identifierNumber = "identifier-for-insurance-provider",
        coverageType = InsuranceErpModelCoverageType.GKV
    ),
    expiresOn = MOCK_FUTURE_INSTANT,
    acceptUntil = MOCK_FUTURE_INSTANT,
    authoredOn = MOCK_LAST_MODIFIED,
    status = TaskStatusEnum.Ready,
    isIncomplete = false,
    pvsIdentifier = "testPvsIdentifier",
    failureToReport = "testFailureToReport",
    currentTime = MOCK_CURRENT_TIME,
    medicationRequest = null,
    medicationDispenses = emptyList(),
    lastMedicationDispense = null
)
