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
import de.gematik.ti.erp.app.fhir.temporal.FhirTemporal
import de.gematik.ti.erp.app.fhir.temporal.FhirTemporalSerializationType
import de.gematik.ti.erp.app.fhir.temporal.asFhirTemporal
import de.gematik.ti.erp.app.prescription.detail.ui.model.PrescriptionMedicationUiModel
import de.gematik.ti.erp.app.task.model.MedicationDispenseErpModel
import de.gematik.ti.erp.app.task.model.TaskErpModel
import kotlinx.datetime.Instant

data class PrescriptionDetailPreviewData(
    val name: String,
    val prescriptionMedication: PrescriptionMedicationUiModel,
    val syncedPrescription: TaskErpModel.Synced.Prescription
)

class PrescriptionDetailPreviewParameterProvider : PreviewParameterProvider<PrescriptionDetailPreviewData> {
    override val values: Sequence<PrescriptionDetailPreviewData>
        get() = sequenceOf(
            PrescriptionDetailPreviewData(
                name = "original_prescription",
                prescriptionMedication = mockMedication,
                syncedPrescription = SYNCED_TASK
            ),
            PrescriptionDetailPreviewData(
                name = "prescription_with_dispense",
                prescriptionMedication = mockMedicationWithDispense,
                syncedPrescription = SYNCED_TASK.copy(
                    lastMedicationDispense = Instant.parse("2024-01-15T10:00:00Z"),
                    medicationDispenses = listOf(mockDispense)
                )
            ),
            PrescriptionDetailPreviewData(
                name = "prescription_no_substitution",
                prescriptionMedication = mockMedicationNoSubstitution,
                syncedPrescription = SYNCED_TASK.copy(
                    medicationRequest = SYNCED_TASK.medicationRequest?.copy(substitutionAllowed = false)
                )
            ),
            PrescriptionDetailPreviewData(
                name = "prescription_no_handover",
                prescriptionMedication = mockMedicationNoHandover,
                syncedPrescription = SYNCED_TASK.copy(
                    medicationDispenses = listOf(mockDispense.copy(whenHandedOver = null))
                )
            ),
            PrescriptionDetailPreviewData(
                name = "prescription_vaccine",
                prescriptionMedication = mockMedicationVaccine,
                syncedPrescription = SYNCED_TASK.copy(
                    medicationRequest = SYNCED_TASK.medicationRequest?.copy(
                        medication = SYNCED_TASK.medicationRequest?.medication?.copy(
                            isVaccine = true,
                            text = "COVID-19 Vaccine"
                        )
                    )
                )
            )
        )
}

private val mockDispense = MedicationDispenseErpModel(
    dispenseId = "DISP123",
    patientIdentifier = "Patient123",
    medication = SYNCED_TASK.medicationRequest?.medication,
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
    ),
    euCountryCode = "DE"
)

private val mockMedication = PrescriptionMedicationUiModel.Request(SYNCED_TASK.medicationRequest)
private val mockMedicationWithDispense = PrescriptionMedicationUiModel.Dispense(mockDispense)
private val mockMedicationNoSubstitution = PrescriptionMedicationUiModel.Request(
    SYNCED_TASK.medicationRequest?.copy(substitutionAllowed = false)
)
private val mockMedicationNoHandover = PrescriptionMedicationUiModel.Request(SYNCED_TASK.medicationRequest)
private val mockMedicationVaccine = PrescriptionMedicationUiModel.Request(
    SYNCED_TASK.medicationRequest?.copy(
        medication = SYNCED_TASK.medicationRequest?.medication?.copy(
            isVaccine = true,
            text = "COVID-19 Vaccine"
        )
    )
)
