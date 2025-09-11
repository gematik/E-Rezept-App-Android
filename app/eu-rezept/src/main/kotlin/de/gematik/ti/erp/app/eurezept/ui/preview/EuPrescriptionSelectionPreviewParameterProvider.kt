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

package de.gematik.ti.erp.app.eurezept.ui.preview

import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import de.gematik.ti.erp.app.eurezept.domin.model.EuAvailabilityInfo
import de.gematik.ti.erp.app.eurezept.domin.model.EuPrescription
import de.gematik.ti.erp.app.prescription.model.PrescriptionType
import kotlinx.datetime.Instant

data class EuPrescriptionSelectionPreviewData(
    val prescriptions: List<EuPrescription>,
    val selectedPrescriptionIds: Set<String>,
    val selectAll: Boolean,
    val profileName: String = "EU-Profile",
    val getAvailabilityInfo: (EuPrescription) -> EuAvailabilityInfo
)

class EuPrescriptionSelectionPreviewParameterProvider : PreviewParameterProvider<EuPrescriptionSelectionPreviewData> {

    private val fullErrorCasePrescriptions = listOf(
        EuPrescription(
            id = "1",
            name = "🇪🇺 EU Medikament Prescription",
            type = PrescriptionType.EuRezeptTask,
            expiryDate = Instant.parse("2024-07-01T10:00:00Z")
        ),
        EuPrescription(
            id = "2",
            name = "Acaimoum",
            type = PrescriptionType.ScannedTask
        ),
        EuPrescription(
            id = "3",
            name = "MeinMedikament",
            type = PrescriptionType.SyncedTask
        ),
        EuPrescription(
            id = "4",
            name = "Wirkstoff Ibu",
            type = PrescriptionType.SyncedTask
        ),
        EuPrescription(
            id = "5",
            name = "Gescanntes Medikament 3",
            type = PrescriptionType.ScannedTask
        ),
        EuPrescription(
            id = "6",
            name = "Benzos",
            type = PrescriptionType.SyncedTask
        )
    )

    private val getAvailabilityInfoWithAllErrors: (EuPrescription) -> EuAvailabilityInfo = { prescription ->
        when {
            prescription.type == PrescriptionType.EuRezeptTask && prescription.id in listOf("1", "2") -> {
                EuAvailabilityInfo(
                    isAvailable = true,
                    expiryDate = prescription.expiryDate
                )
            }

            prescription.type == PrescriptionType.EuRezeptTask && prescription.id == "3" -> {
                EuAvailabilityInfo(
                    isAvailable = false,
                    reason = "Nicht im EU Ausland einlösbar"
                )
            }

            prescription.type == PrescriptionType.ScannedTask -> {
                EuAvailabilityInfo(
                    isAvailable = false,
                    reason = "Gescannte Rezepte können nicht im Ausland eingelöst werden"
                )
            }

            prescription.name == "MeinMedikament" -> {
                EuAvailabilityInfo(
                    isAvailable = false,
                    reason = "Freitextverordnungen können nicht im Ausland eingelöst werden"
                )
            }

            prescription.name == "Wirkstoff Ibu" -> {
                EuAvailabilityInfo(
                    isAvailable = false,
                    reason = "Wirkstoffverordnungen können nicht im Ausland eingelöst werden"
                )
            }

            prescription.name == "Benzos" -> {
                EuAvailabilityInfo(
                    isAvailable = false,
                    reason = "Betäubungsmittel können nicht im Ausland eingelöst werden"
                )
            }

            else -> {
                EuAvailabilityInfo(
                    isAvailable = false,
                    reason = "Nicht im EU Ausland einlösbar"
                )
            }
        }
    }

    override val values: Sequence<EuPrescriptionSelectionPreviewData>
        get() = sequenceOf(
            // Full error cases - no selection
            EuPrescriptionSelectionPreviewData(
                prescriptions = fullErrorCasePrescriptions,
                selectedPrescriptionIds = emptySet(),
                selectAll = false,
                getAvailabilityInfo = getAvailabilityInfoWithAllErrors
            ),

            // Full error cases - only available selected
            EuPrescriptionSelectionPreviewData(
                prescriptions = fullErrorCasePrescriptions,
                selectedPrescriptionIds = setOf("1"),
                selectAll = true,
                getAvailabilityInfo = getAvailabilityInfoWithAllErrors
            )
        )
}
