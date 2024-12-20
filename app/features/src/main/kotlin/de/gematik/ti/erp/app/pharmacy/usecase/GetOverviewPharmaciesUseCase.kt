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
    operator fun invoke(): Flow<List<OverviewPharmacy>> = combine(
        repository.loadFavoritePharmacies(),
        repository.loadOftenUsedPharmacies()
    ) { favouritePharmacies, oftenUsedPharmacies ->
        val favourites = favouritePharmacies
            .sortByLastUsed()

        val oftenUsedOnes = oftenUsedPharmacies
            .notInFavourites(favouritePharmacies)
            .sortByLastUsed()

        (favourites + oftenUsedOnes)
            .distinctBy { it.telematikId }
            .take(LAST_USED_PHARMACIES_COUNT)
    }.flowOn(dispatcher)

    companion object {

        fun List<OverviewPharmacy>.sortByLastUsed() = sortedByDescending { it.lastUsed }

        fun List<OverviewPharmacy>.notInFavourites(
            favourites: List<OverviewPharmacy>
        ) = filter { it.telematikId !in favourites.map { fav -> fav.telematikId } }
    }
}
