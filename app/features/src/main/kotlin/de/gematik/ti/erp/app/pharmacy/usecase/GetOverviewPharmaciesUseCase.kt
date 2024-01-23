/*
 * Copyright (c) 2024 gematik GmbH
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

package de.gematik.ti.erp.app.pharmacy.usecase

import de.gematik.ti.erp.app.pharmacy.model.OverviewPharmacyData.OverviewPharmacy
import de.gematik.ti.erp.app.pharmacy.repository.PharmacyRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOn

private const val LAST_USED_PHARMACIES_COUNT = 5

class GetOverviewPharmaciesUseCase(
    private val repository: PharmacyRepository,
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO
) {
    operator fun invoke(): Flow<List<OverviewPharmacy>> {
        val result = combine(
            repository.loadOftenUsedPharmacies(),
            repository.loadFavoritePharmacies()
        ) { oftenUsedPharmacies, favouritePharmacies ->
            (oftenUsedPharmacies + favouritePharmacies).filter { mixedPharmacy ->
                val booleanResult = favouritePharmacies.any { it.telematikId == mixedPharmacy.telematikId }
                booleanResult
            }.distinctBy { it.telematikId }
                .take(LAST_USED_PHARMACIES_COUNT)
        }.flowOn(dispatcher)
        return result
    }
}
