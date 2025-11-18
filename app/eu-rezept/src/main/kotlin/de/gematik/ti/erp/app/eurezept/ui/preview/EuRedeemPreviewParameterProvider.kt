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
import de.gematik.ti.erp.app.eurezept.domain.model.Country

data class EuRedeemPreviewData(
    val selectedPrescriptions: List<String>,
    val selectedCountry: Country?,
    val isRedeemEnabled: Boolean
)

class EuRedeemPreviewParameterProvider : PreviewParameterProvider<EuRedeemPreviewData> {
    override val values: Sequence<EuRedeemPreviewData>
        get() = sequenceOf(
            // no prescriptions, no country
            EuRedeemPreviewData(
                selectedPrescriptions = emptyList(),
                selectedCountry = null,
                isRedeemEnabled = false
            ),
            // Only prescriptions selected
            EuRedeemPreviewData(
                selectedPrescriptions = listOf("🇪🇺 EU Medikament Prescription"),
                selectedCountry = null,
                isRedeemEnabled = false
            ),

            // Many prescriptions
            EuRedeemPreviewData(
                selectedPrescriptions = listOf(
                    "Ibuprofen 600",
                    "Paracetamol 500",
                    "Amoxicillin 250mg",
                    "Omeprazol 20mg",
                    "Metformin 500mg",
                    "Simvastatin 20mg"
                ),
                selectedCountry = Country(
                    name = "Österreich",
                    code = "at",
                    flagEmoji = "🇦🇹"
                ),
                isRedeemEnabled = true
            )
        )
}
