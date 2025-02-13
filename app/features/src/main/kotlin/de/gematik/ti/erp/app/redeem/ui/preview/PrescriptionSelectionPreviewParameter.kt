/*
 * Copyright 2024, gematik GmbH
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
 * In case of changes by gematik find details in the "Readme" file.
 *
 * See the Licence for the specific language governing permissions and limitations under the Licence.
 */

package de.gematik.ti.erp.app.redeem.ui.preview

import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import de.gematik.ti.erp.app.pharmacy.usecase.model.PharmacyUseCaseData
import kotlinx.datetime.Instant

data class PrescriptionSelectionPreview(
    val name: String,
    val orders: List<PharmacyUseCaseData.PrescriptionOrder>,
    val selectedOrders: List<PharmacyUseCaseData.PrescriptionOrder>,
    val onCheckedChange: (PharmacyUseCaseData.PrescriptionOrder, Boolean) -> Unit
)

class PrescriptionSelectionPreviewParameter : PreviewParameterProvider<PrescriptionSelectionPreview> {
    val time = Instant.fromEpochSeconds(1732060800)

    override val values: Sequence<PrescriptionSelectionPreview>
        get() = sequenceOf(
            PrescriptionSelectionPreview(
                name = "NoPrescriptionsSelected",
                orders = listOf(
                    PharmacyUseCaseData.PrescriptionOrder(
                        taskId = "1",
                        accessCode = "ABC123",
                        title = "Prescription for Cold Medicine",
                        isSelfPayerPrescription = false,
                        index = 1,
                        timestamp = time,
                        substitutionsAllowed = true,
                        isScanned = false
                    ),
                    PharmacyUseCaseData.PrescriptionOrder(
                        taskId = "2",
                        accessCode = "DEF456",
                        title = "Prescription for Pain Relief",
                        isSelfPayerPrescription = true,
                        index = 2,
                        timestamp = time,
                        substitutionsAllowed = false,
                        isScanned = true
                    )
                ),
                selectedOrders = emptyList(),
                onCheckedChange = { _, _ -> }
            ),
            PrescriptionSelectionPreview(
                name = "OnePrescriptionSelected",
                orders = listOf(
                    PharmacyUseCaseData.PrescriptionOrder(
                        taskId = "1",
                        accessCode = "ABC123",
                        title = "Prescription for Cold Medicine",
                        isSelfPayerPrescription = false,
                        index = 1,
                        timestamp = time,
                        substitutionsAllowed = true,
                        isScanned = false
                    ),
                    PharmacyUseCaseData.PrescriptionOrder(
                        taskId = "2",
                        accessCode = "DEF456",
                        title = "Prescription for Pain Relief",
                        isSelfPayerPrescription = true,
                        index = 2,
                        timestamp = time,
                        substitutionsAllowed = false,
                        isScanned = true
                    )
                ),
                selectedOrders = listOf(
                    PharmacyUseCaseData.PrescriptionOrder(
                        taskId = "1",
                        accessCode = "ABC123",
                        title = "Prescription for Cold Medicine",
                        isSelfPayerPrescription = false,
                        index = 1,
                        timestamp = time,
                        substitutionsAllowed = true,
                        isScanned = false
                    )
                ),
                onCheckedChange = { _, _ -> }
            )
        )
}
