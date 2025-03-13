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

package de.gematik.ti.erp.app.prescription.ui.preview

import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import de.gematik.ti.erp.app.pharmacy.usecase.model.PharmacyUseCaseData
import de.gematik.ti.erp.app.prescription.ui.preview.OnlineRedeemPreferencesScreenPreviewData.PharmacyOrders
import de.gematik.ti.erp.app.prescription.ui.preview.OnlineRedeemPreferencesScreenPreviewData.emptyPharmacyOrders
import kotlinx.datetime.Instant

class OnlineRedeemPreferencesScreenPreviewParameterProvider : PreviewParameterProvider<List<PharmacyUseCaseData.PrescriptionInOrder>> {

    override val values: Sequence<List<PharmacyUseCaseData.PrescriptionInOrder>>
        get() = sequenceOf(
            PharmacyOrders,
            emptyPharmacyOrders
        )
}

object OnlineRedeemPreferencesScreenPreviewData {
    val time = Instant.parse("2021-11-25T15:20:00Z")

    val PharmacyOrders = listOf(
        PharmacyUseCaseData.PrescriptionInOrder(
            taskId = "1",
            accessCode = "ABC123",
            title = "Prescription 1",
            isSelfPayerPrescription = false,
            index = 1,
            timestamp = time,
            substitutionsAllowed = true,
            isScanned = false
        ),
        PharmacyUseCaseData.PrescriptionInOrder(
            taskId = "2",
            accessCode = "XYZ456",
            title = "Prescription 2",
            isSelfPayerPrescription = true,
            index = 2,
            timestamp = time,
            substitutionsAllowed = false,
            isScanned = true
        ),
        PharmacyUseCaseData.PrescriptionInOrder(
            taskId = "2",
            accessCode = "XYZ456",
            title = "Prescription 2",
            isSelfPayerPrescription = true,
            index = 2,
            timestamp = time,
            substitutionsAllowed = false,
            isScanned = true
        )
    )
    val emptyPharmacyOrders = emptyList<PharmacyUseCaseData.PrescriptionInOrder>()
}
