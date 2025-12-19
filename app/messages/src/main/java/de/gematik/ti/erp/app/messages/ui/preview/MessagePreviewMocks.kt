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

import de.gematik.ti.erp.app.diga.model.DigaStatus
import de.gematik.ti.erp.app.fhir.prescription.model.ErpMedicationProfileType
import de.gematik.ti.erp.app.fhir.prescription.model.ErpMedicationProfileVersion
import de.gematik.ti.erp.app.fhir.prescription.model.FhirTaskKbvMedicationProfileErpModel
import de.gematik.ti.erp.app.fhir.temporal.FhirTemporal
import de.gematik.ti.erp.app.prescription.model.Quantity
import de.gematik.ti.erp.app.prescription.model.Ratio
import de.gematik.ti.erp.app.prescription.model.SyncedTaskData
import de.gematik.ti.erp.app.prescription.model.SyncedTaskData.Medication
import de.gematik.ti.erp.app.prescription.usecase.model.Prescription
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlin.time.Duration.Companion.days

object MessagePreviewMocks {

    internal const val MOCK_TASK_ID_01 = "123-001"
    internal const val MOCK_TASK_ID_02 = "456-002"
    private const val MOCK_PRACTITIONER_NAME = "Dr. John Doe"
    private const val MESSAGE_TIMESTAMP = "2025-01-01T10:00:00Z"

    private val MOCK_PRACTITIONER = SyncedTaskData.Practitioner(
        name = MOCK_PRACTITIONER_NAME,
        qualification = "",
        practitionerIdentifier = " "
    )

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

    private val MEDICATION_10_TAB = Medication(
        category = SyncedTaskData.MedicationCategory.entries[0],
        medicationProfile = FhirTaskKbvMedicationProfileErpModel(
            type = ErpMedicationProfileType.PZN,
            version = ErpMedicationProfileVersion.V_110
        ),
        vaccine = true,
        text = "Gematidolor 100mg",
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

    private val MOCK_MEDICATION_REQ = SyncedTaskData.MedicationRequest(
        MEDICATION_10_TAB, null, null, SyncedTaskData.AccidentType.None,
        null, null, false, null,
        SyncedTaskData.MultiplePrescriptionInfo(false), 1, null, null, SyncedTaskData.AdditionalFee.None
    )

    private val MOCK_CHIP_INFO = Prescription.PrescriptionChipInformation(
        isPartOfMultiplePrescription = false,
        numerator = null,
        denominator = null,
        start = null
    )

    val MOCK_PRESCRIPTION_01 = Prescription.SyncedPrescription(
        taskId = MOCK_TASK_ID_01,
        name = "Rezept_01",
        redeemedOn = null,
        expiresOn = Instant.fromEpochSeconds(123456),
        state = SyncedTaskData.SyncedTask.Expired(
            expiredOn = Instant.fromEpochSeconds(123456)
        ),
        isIncomplete = false,
        organization = MOCK_PRACTITIONER_NAME,
        authoredOn = Instant.fromEpochSeconds(123456),
        acceptUntil = Instant.fromEpochSeconds(123456),
        isDirectAssignment = false,
        prescriptionChipInformation = MOCK_CHIP_INFO,
        deviceRequestState = DigaStatus.Ready,
        lastModified = Instant.fromEpochSeconds(123456)
    )

    val MOCK_PRESCRIPTION_02 = Prescription.SyncedPrescription(
        taskId = MOCK_TASK_ID_02,
        name = "Rezept_02",
        redeemedOn = null,
        expiresOn = Instant.fromEpochSeconds(123456),
        state = SyncedTaskData.SyncedTask.Expired(
            expiredOn = Instant.fromEpochSeconds(123456)
        ),
        isIncomplete = false,
        organization = MOCK_PRACTITIONER_NAME,
        authoredOn = Instant.fromEpochSeconds(123456),
        acceptUntil = Instant.fromEpochSeconds(123456),
        isDirectAssignment = false,
        prescriptionChipInformation = MOCK_CHIP_INFO,
        deviceRequestState = DigaStatus.Ready,
        lastModified = Instant.fromEpochSeconds(123456)
    )

    val MOCK_PRESCRIPTION_03 = Prescription.SyncedPrescription(
        taskId = MOCK_TASK_ID_02,
        name = "Rezept_03",
        redeemedOn = null,
        expiresOn = Instant.fromEpochSeconds(123456),
        state = SyncedTaskData.SyncedTask.Expired(
            expiredOn = Instant.fromEpochSeconds(123456)
        ),
        isIncomplete = false,
        organization = MOCK_PRACTITIONER_NAME,
        authoredOn = Instant.fromEpochSeconds(123456),
        acceptUntil = Instant.fromEpochSeconds(123456),
        isDirectAssignment = false,
        prescriptionChipInformation = MOCK_CHIP_INFO,
        deviceRequestState = DigaStatus.Ready,
        lastModified = Instant.fromEpochSeconds(123456)
    )

    internal val MOCK_SYNCED_TASK_DATA_01 = SyncedTaskData.SyncedTask(
        profileId = "testProfileId",
        taskId = "1.0.1.2.3.4.5.6.7",
        accessCode = "testAccessCode",
        lastModified = Instant.parse(MESSAGE_TIMESTAMP),
        organization = MOCK_ORGANIZATION,
        practitioner = MOCK_PRACTITIONER,
        patient = MOCK_PATIENT,
        insuranceInformation = SyncedTaskData.InsuranceInformation(
            name = "TestInsurance",
            status = "Active",
            coverageType = SyncedTaskData.CoverageType.GKV
        ),
        expiresOn = Instant.parse(MESSAGE_TIMESTAMP),
        acceptUntil = Instant.parse(MESSAGE_TIMESTAMP),
        authoredOn = Instant.parse(MESSAGE_TIMESTAMP),
        status = SyncedTaskData.TaskStatus.Ready,
        isIncomplete = false,
        pvsIdentifier = "testPvsIdentifier",
        failureToReport = "testFailureToReport",
        medicationRequest = MOCK_MEDICATION_REQ,
        lastMedicationDispense = null,
        medicationDispenses = emptyList(),
        communications = emptyList(),
        isEuRedeemable = true,
        isEuRedeemableByPatientAuthorization = true
    )
}
