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

package de.gematik.ti.erp.app.pharmacy.ui.preview

import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import de.gematik.ti.erp.app.pharmacy.usecase.model.PharmacyUseCaseData

data class PharmacyFilterSheetScreenPreviewData(
    val filter: PharmacyUseCaseData.Filter,
    val isNearbyFilter: Boolean,
    val navWithStartButton: Boolean,
    val isLoading: Boolean,
    val showDescriptions: Boolean,
    val selectedServiceCodes: Set<String>
)

class PharmacyFilterSheetScreenPreviewParameterProvider : PreviewParameterProvider<PharmacyFilterSheetScreenPreviewData> {
    override val values = sequenceOf(
        PharmacyFilterSheetScreenPreviewData(
            filter = PharmacyUseCaseData.Filter(
                nearBy = true,
                openNow = false,
                deliveryService = false,
                onlineService = true
            ),
            isNearbyFilter = true,
            navWithStartButton = true,
            isLoading = false,
            showDescriptions = false,
            selectedServiceCodes = emptySet()
        ),
        PharmacyFilterSheetScreenPreviewData(
            filter = PharmacyUseCaseData.Filter(
                nearBy = false,
                openNow = true,
                deliveryService = true,
                onlineService = false
            ),
            isNearbyFilter = false,
            navWithStartButton = false,
            isLoading = false,
            showDescriptions = true,
            selectedServiceCodes = setOf("impfung", "60")
        )
    )
}
