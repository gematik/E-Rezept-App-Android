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
import de.gematik.ti.erp.app.profile.model.Avatar
import de.gematik.ti.erp.app.profile.model.InsuranceType
import de.gematik.ti.erp.app.profile.model.ProfileColorNames
import de.gematik.ti.erp.app.profile.model.ProfileErpModel
import de.gematik.ti.erp.app.profile.model.ProfileImageDataErpModel
import de.gematik.ti.erp.app.profile.model.ProfileInsuranceDataErpModel
import de.gematik.ti.erp.app.task.model.Identifier
import de.gematik.ti.erp.app.task.model.MedicationCategory
import de.gematik.ti.erp.app.task.model.MedicationDispenseErpModel
import de.gematik.ti.erp.app.task.model.MedicationErpModel
import de.gematik.ti.erp.app.task.model.TaskErpModel
import de.gematik.ti.erp.app.userauthentication.model.UserAuthenticationErpModel
import de.gematik.ti.erp.app.utils.uistate.UiState
import kotlinx.datetime.Instant

class PrescriptionDetailMedicationOverviewPreviewParameter :
    PreviewParameterProvider<UiState<Pair<ProfileErpModel, TaskErpModel>>> {
    override val values = sequenceOf(
        UiState.Empty(),
        UiState.Loading(),
        UiState.Data(createPreviewPair(PrescriptionPreviewData.withDispenses()))
    )

    private fun createPreviewPair(previewData: PrescriptionPreviewData): Pair<ProfileErpModel, TaskErpModel> =
        ProfileErpModel(
            id = "1",
            name = "Max Mustermann",
            insuranceData = ProfileInsuranceDataErpModel(
                insurantName = "Max Mustermann",
                insuranceIdentifier = "1234567890",
                insuranceName = "Muster AG",
                insuranceType = InsuranceType.GKV,
                organizationIdentifier = null
            ),
            active = true,
            profileImageData = ProfileImageDataErpModel(
                color = ProfileColorNames.SPRING_GRAY,
                avatar = Avatar.Baby,
                image = null
            ),
            lastAuthenticated = null,
            lastTaskSynced = null,
            lastAuditEventSynced = null,
            isConsentDrawerShown = true,
            isNewlyCreated = false,
            userAuthentication = UserAuthenticationErpModel.NotInitialized
        ) to previewData.syncedPrescription
}

@Suppress("MagicNumber")
private data class PrescriptionPreviewData(
    val syncedPrescription: TaskErpModel.Synced.Prescription,
    val taskId: String
) {
    companion object {
        fun defaultPreview(): PrescriptionPreviewData {
            val mockSyncedTask = SYNCED_TASK

            return PrescriptionPreviewData(
                syncedPrescription = mockSyncedTask,
                taskId = "mockTaskId"
            )
        }

        fun withDispenses(): PrescriptionPreviewData {
            val default = defaultPreview()
            val dispense = MedicationDispenseErpModel(
                dispenseId = "1",
                patientIdentifier = "1234",
                medication = MedicationErpModel(
                    category = MedicationCategory.AMVV,
                    medicationProfile = FhirTaskKbvMedicationProfileErpModel(
                        type = ErpMedicationProfileType.PZN,
                        version = ErpMedicationProfileVersion.V_110
                    ),
                    isVaccine = false,
                    text = "Dispensed Medication",
                    form = "Capsule",
                    lotNumber = "654321",
                    expirationDate = null,
                    identifier = Identifier(pzn = "333333", atc = "444444"),
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
                whenHandedOver = null,
                euCountryCode = "DE"
            )
            return default.copy(
                syncedPrescription = default.syncedPrescription.copy(
                    medicationDispenses = listOf(dispense)
                )
            )
        }
    }
}
