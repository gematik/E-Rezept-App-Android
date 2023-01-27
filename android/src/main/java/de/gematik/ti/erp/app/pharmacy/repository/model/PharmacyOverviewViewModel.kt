/*
 * Copyright (c) 2023 gematik GmbH
 * 
 * Licensed under the EUPL, Version 1.2 or – as soon they will be approved by
 * the European Commission - subsequent versions of the EUPL (the Licence);
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at:
 * 
 *     https://joinup.ec.europa.eu/software/page/eupl
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the Licence is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Licence for the specific language governing permissions and
 * limitations under the Licence.
 * 
 */

package de.gematik.ti.erp.app.pharmacy.repository.model

import androidx.lifecycle.ViewModel
import de.gematik.ti.erp.app.pharmacy.model.OverviewPharmacyData
import de.gematik.ti.erp.app.pharmacy.usecase.PharmacyOverviewUseCase
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOf

class PharmacyOverviewViewModel(
    private val pharmacyUseCase: PharmacyOverviewUseCase
) : ViewModel() {
    fun pharmacyOverviewState() = combine(
        pharmacyUseCase.favoritePharmacies(),
        pharmacyUseCase.oftenUsedPharmacies()
    ) { favorites, oftenUsed ->
        favorites + oftenUsed.filterNot { oftenUsedPharmacy ->
            favorites.any {
                it.telematikId == oftenUsedPharmacy.telematikId
            }
        }
    }

    suspend fun deleteOverviewPharmacy(overviewPharmacy: OverviewPharmacyData.OverviewPharmacy) {
        pharmacyUseCase.deleteOverviewPharmacy(overviewPharmacy)
    }

    suspend fun findPharmacyByTelematikIdState(
        telematikId: String
    ) = flowOf(pharmacyUseCase.searchPharmacyByTelematikId(telematikId))
}
