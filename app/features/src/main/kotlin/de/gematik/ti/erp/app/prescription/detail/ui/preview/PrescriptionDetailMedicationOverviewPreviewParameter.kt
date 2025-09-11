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
import de.gematik.ti.erp.app.fhir.temporal.asFhirTemporal
import de.gematik.ti.erp.app.pkv.ui.preview.PkvMockData.medicationPzn
import de.gematik.ti.erp.app.prescription.model.PrescriptionData
import de.gematik.ti.erp.app.prescription.model.SyncedTaskData
import de.gematik.ti.erp.app.profiles.model.ProfilesData
import de.gematik.ti.erp.app.profiles.usecase.model.ProfileInsuranceInformation
import de.gematik.ti.erp.app.profiles.usecase.model.ProfilesUseCaseData
import de.gematik.ti.erp.app.utils.uistate.UiState
import kotlinx.datetime.Instant

class PrescriptionDetailMedicationOverviewPreviewParameter :
    PreviewParameterProvider<UiState<Pair<ProfilesUseCaseData.Profile, PrescriptionData.Prescription>>> {
    override val values = sequenceOf(
        UiState.Empty(),
        UiState.Loading(),
        UiState.Data(createPreviewPair(PrescriptionPreviewData.withDispenses()))
    )

    private fun createPreviewPair(previewData: PrescriptionPreviewData): Pair<ProfilesUseCaseData.Profile, PrescriptionData.Prescription> =
        ProfilesUseCaseData.Profile(
            id = "1",
            name = "Max Mustermann",
            insurance = ProfileInsuranceInformation(
                insuranceType = ProfilesUseCaseData.InsuranceType.GKV
            ),
            isActive = true,
            color = ProfilesData.ProfileColorNames.SPRING_GRAY,
            lastAuthenticated = null,
            ssoTokenScope = null,
            avatar = ProfilesData.Avatar.PersonalizedImage,
            image = null
        ) to PrescriptionData.Synced(task = previewData.syncedPrescription)
}

@Suppress("MagicNumber")
private data class PrescriptionPreviewData(
    val medication: SyncedTaskData.Medication,
    val syncedPrescription: SyncedTaskData.SyncedTask,
    val taskId: String
) {
    companion object {
        fun defaultPreview(): PrescriptionPreviewData {
            val mockMedication = medicationPzn
            val mockSyncedTask = SYNCED_TASK

            return PrescriptionPreviewData(
                medication = mockMedication,
                syncedPrescription = mockSyncedTask,
                taskId = "mockTaskId"
            )
        }

        fun withDispenses(): PrescriptionPreviewData {
            val default = defaultPreview()
            val dispense = SyncedTaskData.MedicationDispense(
                dispenseId = "1",
                patientIdentifier = "1234",
                medication = SyncedTaskData.Medication(
                    category = SyncedTaskData.MedicationCategory.AMVV,
                    medicationProfile = FhirTaskKbvMedicationProfileErpModel(
                        type = ErpMedicationProfileType.PZN,
                        version = ErpMedicationProfileVersion.V_110
                    ),
                    vaccine = false,
                    text = "Dispensed Medication",
                    form = "Capsule",
                    lotNumber = "654321",
                    expirationDate = null,
                    identifier = SyncedTaskData.Identifier(pzn = "333333", atc = "444444"),
                    normSizeCode = "N1",
                    amount = null,
                    manufacturingInstructions = null,
                    packaging = "Blister Pack",
                    ingredientMedications = emptyList(),
                    ingredients = emptyList()
                ),
                wasSubstituted = false,
                dosageInstruction = "Take twice daily",
                performer = "Pharmacist A",
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
                whenHandedOver = null
            )
            return default.copy(
                syncedPrescription = default.syncedPrescription.copy(
                    medicationDispenses = listOf(dispense)
                )
            )
        }
    }
}
