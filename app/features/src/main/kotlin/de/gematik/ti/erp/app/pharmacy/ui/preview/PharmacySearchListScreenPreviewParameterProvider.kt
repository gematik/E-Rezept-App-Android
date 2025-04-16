/*
 * Copyright 2025, gematik GmbH
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

package de.gematik.ti.erp.app.pharmacy.ui.preview

import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import androidx.paging.PagingData
import de.gematik.ti.erp.app.pharmacy.usecase.model.PharmacyUseCaseData
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

data class PharmacySearchListScreenPreviewData(
    val filter: PharmacyUseCaseData.Filter,
    val searchTerm: String,
    val isLoading: Boolean,
    val pagingData: Flow<PagingData<PharmacyUseCaseData.Pharmacy>>
)

class PharmacySearchListScreenPreviewParameterProvider : PreviewParameterProvider<PharmacySearchListScreenPreviewData> {
    override val values = sequenceOf(
        PharmacySearchListScreenPreviewData(
            filter = PharmacyUseCaseData.Filter(
                openNow = true,
                deliveryService = true,
                nearBy = true
            ),
            searchTerm = "Apotheke",
            isLoading = false,
            pagingData = flowOf(PagingData.empty())
        ),
        PharmacySearchListScreenPreviewData(
            filter = PharmacyUseCaseData.Filter(),
            searchTerm = "Loading",
            isLoading = true,
            pagingData = flowOf(PagingData.empty())
        ),
        PharmacySearchListScreenPreviewData(
            filter = PharmacyUseCaseData.Filter(
                onlineService = true
            ),
            searchTerm = "",
            isLoading = true,
            pagingData = flowOf(PagingData.empty())
        )
    )
}
