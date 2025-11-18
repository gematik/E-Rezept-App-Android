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

package de.gematik.ti.erp.app.prescription.detail.ui.preview

import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import de.gematik.ti.erp.app.fhir.dispense.model.FhirDispenseDeviceRequestErpModel
import de.gematik.ti.erp.app.fhir.prescription.model.ErpMedicationProfileType
import de.gematik.ti.erp.app.fhir.prescription.model.ErpMedicationProfileVersion
import de.gematik.ti.erp.app.fhir.prescription.model.FhirTaskKbvMedicationProfileErpModel
import de.gematik.ti.erp.app.fhir.temporal.FhirTemporal
import de.gematik.ti.erp.app.fhir.temporal.FhirTemporalSerializationType
import de.gematik.ti.erp.app.fhir.temporal.asFhirTemporal
import de.gematik.ti.erp.app.prescription.model.PrescriptionData
import de.gematik.ti.erp.app.prescription.model.Quantity
import de.gematik.ti.erp.app.prescription.model.Ratio
import de.gematik.ti.erp.app.prescription.model.SyncedTaskData
import kotlinx.datetime.Instant

data class PrescriptionDetailPreviewData(
    val name: String,
    val prescriptionDataMedication: PrescriptionData.Medication.Request,
    val syncedPrescription: PrescriptionData.Synced
)

class PrescriptionDetailPreviewParameterProvider : PreviewParameterProvider<PrescriptionDetailPreviewData> {
    override val values: Sequence<PrescriptionDetailPreviewData>
        get() = sequenceOf(
            PrescriptionDetailPreviewData(
                name = "original_prescription",
                prescriptionDataMedication = mockMedication,
                syncedPrescription = mockSyncedPrescription
            ),
            PrescriptionDetailPreviewData(
                name = "prescription_with_dispense",
                prescriptionDataMedication = mockMedicationWithDispense,
                syncedPrescription = mockSyncedPrescriptionWithDispense
            ),
            PrescriptionDetailPreviewData(
                name = "prescription_no_substitution",
                prescriptionDataMedication = mockMedicationNoSubstitution,
                syncedPrescription = mockSyncedPrescriptionNoSubstitution
            ),
            PrescriptionDetailPreviewData(
                name = "prescription_no_handover",
                prescriptionDataMedication = mockMedicationNoHandover,
                syncedPrescription = mockSyncedPrescriptionNoHandover
            ),
            PrescriptionDetailPreviewData(
                name = "prescription_vaccine",
                prescriptionDataMedication = mockMedicationVaccine,
                syncedPrescription = mockSyncedPrescriptionVaccine
            )
        )
}

// Base medication request
private val mockMedicationRequest = SyncedTaskData.MedicationRequest(
    medication = SyncedTaskData.Medication(
        category = SyncedTaskData.MedicationCategory.ARZNEI_UND_VERBAND_MITTEL,
        medicationProfile = FhirTaskKbvMedicationProfileErpModel(
            type = ErpMedicationProfileType.PZN,
            version = ErpMedicationProfileVersion.V_110
        ),
        vaccine = false,
        text = "Paracetamol 500mg",
        form = "Tablet",
        lotNumber = "Lot12345",
        expirationDate = null,
        identifier = SyncedTaskData.Identifier(
            pzn = "12345678",
            atc = "N02BE01",
            ask = "1234",
            snomed = "387517004"
        ),
        normSizeCode = "N2",
        amount = Ratio(
            numerator = Quantity(value = "5", unit = "oz"),
            denominator = null
        ),
        manufacturingInstructions = "Keep in a cool, dry place",
        packaging = "packing 123",
        ingredientMedications = emptyList(),
        ingredients = listOf(
            SyncedTaskData.Ingredient(
                text = "Paracetamol",
                form = "tablet",
                number = "001",
                amount = "500mg",
                strength = Ratio(
                    numerator = Quantity(value = "1", unit = "oz"),
                    denominator = null
                )
            )
        )
    ),
    authoredOn = FhirTemporal.Instant(
        value = Instant.parse("2024-01-01T10:00:00Z"),
        type = FhirTemporalSerializationType.FhirTemporalInstant
    ),
    dateOfAccident = null,
    accidentType = SyncedTaskData.AccidentType.None,
    location = null,
    emergencyFee = null,
    substitutionAllowed = true,
    dosageInstruction = "Take one tablet with water",
    multiplePrescriptionInfo = SyncedTaskData.MultiplePrescriptionInfo(),
    quantity = 30,
    note = "Bitte auf Anwendung schulen",
    bvg = false,
    additionalFee = SyncedTaskData.AdditionalFee.None
)
private val mockMedicationDispense = SyncedTaskData.MedicationDispense(
    dispenseId = "DISP123",
    patientIdentifier = "Patient123",
    medication = mockMedicationRequest.medication,
    wasSubstituted = false,
    dosageInstruction = "Take as prescribed",
    performer = "Test Pharmacy",
    deviceRequest = FhirDispenseDeviceRequestErpModel(
        deepLink = "",
        redeemCode = "xx12628491ß2242",
        declineCode = "001",
        note = "Error",
        referencePzn = "123456",
        display = "Diga App",
        status = "completed",
        modifiedDate = Instant.parse(input = "2024-08-01T10:00:00Z").asFhirTemporal()
    ),
    whenHandedOver = FhirTemporal.Instant(
        value = Instant.parse("2024-01-15T10:00:00Z"),
        type = FhirTemporalSerializationType.FhirTemporalInstant
    )
)

private val mockMedicationDispenseNoHandover = mockMedicationDispense.copy(
    whenHandedOver = null
)

private val mockMedicationRequestWithDispense = mockMedicationRequest

private val mockMedicationRequestNoSubstitution = mockMedicationRequest.copy(
    substitutionAllowed = false
)

private val mockMedicationRequestNoHandover = mockMedicationRequest

private val mockMedicationRequestVaccine = mockMedicationRequest.copy(
    medication = mockMedicationRequest.medication?.copy(
        vaccine = true,
        text = "COVID-19 Vaccine",
        category = SyncedTaskData.MedicationCategory.SONSTIGES
    )
)

// Base synced task
private val mockSyncedTask = SyncedTaskData.SyncedTask(
    profileId = "Profile123",
    taskId = "Task123",
    accessCode = "Access123",
    lastModified = Instant.fromEpochSeconds(1680000000),
    organization = SyncedTaskData.Organization(
        name = "Test Pharmacy",
        address = SyncedTaskData.Address(
            line1 = "123 Health St",
            line2 = "",
            postalCode = "12345",
            city = "MedCity"
        ),
        uniqueIdentifier = "ORG123"
    ),
    practitioner = SyncedTaskData.Practitioner(
        name = "Dr. Smith",
        qualification = "MD",
        practitionerIdentifier = "PRAC456"
    ),
    patient = SyncedTaskData.Patient(
        name = "John Doe",
        address = SyncedTaskData.Address(
            line1 = "456 Patient St",
            line2 = "",
            postalCode = "67890",
            city = "PatientCity"
        ),
        birthdate = null,
        insuranceIdentifier = "INS789"
    ),
    insuranceInformation = SyncedTaskData.InsuranceInformation(
        name = "Test Insurance",
        status = "Active",
        coverageType = SyncedTaskData.CoverageType.GKV
    ),
    expiresOn = Instant.fromEpochSeconds(1690000000),
    acceptUntil = Instant.fromEpochSeconds(1685000000),
    authoredOn = Instant.fromEpochSeconds(1680000000),
    status = SyncedTaskData.TaskStatus.Ready,
    isIncomplete = false,
    pvsIdentifier = "PVS123",
    failureToReport = "",
    medicationRequest = mockMedicationRequest,
    lastMedicationDispense = null,
    medicationDispenses = emptyList(),
    communications = emptyList(),
    isEuRedeemable = false,
    isEuRedeemableByPatientAuthorization = false
)

// prescription data objects
private val mockMedicationWithDispense = PrescriptionData.Medication.Request(mockMedicationRequestWithDispense)
private val mockMedicationNoSubstitution = PrescriptionData.Medication.Request(mockMedicationRequestNoSubstitution)
private val mockMedicationNoHandover = PrescriptionData.Medication.Request(mockMedicationRequestNoHandover)
private val mockMedicationVaccine = PrescriptionData.Medication.Request(mockMedicationRequestVaccine)
private val mockMedication = PrescriptionData.Medication.Request(mockMedicationRequest)

// synced task variations
private val mockSyncedTaskWithDispense = mockSyncedTask.copy(
    medicationRequest = mockMedicationRequestWithDispense,
    lastMedicationDispense = Instant.parse("2024-01-15T10:00:00Z"),
    medicationDispenses = listOf(mockMedicationDispense)
)

private val mockSyncedTaskNoSubstitution = mockSyncedTask.copy(
    medicationRequest = mockMedicationRequestNoSubstitution
)

private val mockSyncedTaskNoHandover = mockSyncedTask.copy(
    medicationRequest = mockMedicationRequestNoHandover,
    lastMedicationDispense = null,
    medicationDispenses = listOf(mockMedicationDispenseNoHandover)
)

private val mockSyncedTaskVaccine = mockSyncedTask.copy(
    medicationRequest = mockMedicationRequestVaccine
)

// synced prescription objects
private val mockSyncedPrescription = PrescriptionData.Synced(mockSyncedTask)
private val mockSyncedPrescriptionWithDispense = PrescriptionData.Synced(mockSyncedTaskWithDispense)
private val mockSyncedPrescriptionNoSubstitution = PrescriptionData.Synced(mockSyncedTaskNoSubstitution)
private val mockSyncedPrescriptionNoHandover = PrescriptionData.Synced(mockSyncedTaskNoHandover)
private val mockSyncedPrescriptionVaccine = PrescriptionData.Synced(mockSyncedTaskVaccine)
