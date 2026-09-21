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

package de.gematik.ti.erp.app.messages.ui.preview

import de.gematik.ti.erp.app.task.model.InsuranceErpModel
import de.gematik.ti.erp.app.task.model.InsuranceErpModelCoverageType
import de.gematik.ti.erp.app.task.model.OrganizationErpModel
import de.gematik.ti.erp.app.task.model.PatientErpModel
import de.gematik.ti.erp.app.task.model.PractitionerErpModel
import de.gematik.ti.erp.app.task.model.TaskErpModel
import de.gematik.ti.erp.app.task.model.TaskStatusEnum
import kotlinx.datetime.Instant

object MessagePreviewMocks {

    internal const val MOCK_TASK_ID_01 = "123-001"
    private const val MOCK_PRACTITIONER_NAME = "Dr. John Doe"
    private const val MESSAGE_TIMESTAMP = "2025-01-01T10:00:00Z"

    private val MOCK_PRACTITIONER = PractitionerErpModel(
        name = MOCK_PRACTITIONER_NAME,
        qualification = "",
        practitionerIdentifier = " ",
        dentistIdentifier = null,
        telematikId = null
    )

    private val MOCK_ORGANIZATION = OrganizationErpModel(
        name = "TestOrganization",
        address = null,
        phone = "123-456-7890",
        mail = "info@testorg.com"
    )

    private val MOCK_PATIENT = PatientErpModel(
        name = "Jane",
        address = null,
        dateOfBirth = null,
        insuranceIdentifier = "ins123"
    )

    val MOCK_PRESCRIPTION_01 = TaskErpModel.Synced.Prescription(
        profileId = "testProfileId",
        name = "Rezept_01",
        taskId = MOCK_TASK_ID_01,
        accessCode = "testAccessCode",
        lastModified = Instant.fromEpochSeconds(123456),
        organization = MOCK_ORGANIZATION,
        practitioner = MOCK_PRACTITIONER,
        patient = MOCK_PATIENT,
        insuranceInformation = InsuranceErpModel(
            name = "TestInsurance",
            status = "Active",
            coverageType = InsuranceErpModelCoverageType.GKV
        ),
        expiresOn = Instant.fromEpochSeconds(123456),
        acceptUntil = Instant.fromEpochSeconds(123456),
        authoredOn = Instant.fromEpochSeconds(123456),
        status = TaskStatusEnum.Ready,
        isIncomplete = false,
        pvsIdentifier = "testPvsIdentifier",
        failureToReport = "testFailureToReport",
        medicationRequest = null,
        lastMedicationDispense = null,
        medicationDispenses = emptyList(),
        isEuRedeemable = true,
        isEuRedeemableByPatientAuthorization = true
    )

    val MOCK_PRESCRIPTION_02 = MOCK_PRESCRIPTION_01.copy(
        taskId = "456-002",
        name = "Rezept_02"
    )

    val MOCK_PRESCRIPTION_03 = MOCK_PRESCRIPTION_01.copy(
        taskId = "789-003",
        name = "Rezept_03"
    )

    internal val MOCK_SYNCED_TASK_DATA_01 = MOCK_PRESCRIPTION_01
}
