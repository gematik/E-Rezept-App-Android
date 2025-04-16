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

package de.gematik.ti.erp.app.demomode.repository.pharmacy

import de.gematik.ti.erp.app.demomode.datasource.DemoModeDataSource
import de.gematik.ti.erp.app.demomode.datasource.INDEX_OUT_OF_BOUNDS
import de.gematik.ti.erp.app.pharmacy.model.OverviewPharmacyData
import de.gematik.ti.erp.app.pharmacy.repository.datasource.local.FavouritePharmacyLocalDataSource
import de.gematik.ti.erp.app.pharmacy.usecase.model.PharmacyUseCaseData
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.flow.updateAndGet
import kotlinx.coroutines.withContext
import kotlinx.datetime.Clock

class DemoFavouritePharmacyLocalDataSource(
    private val dataSource: DemoModeDataSource,
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO
) : FavouritePharmacyLocalDataSource {

    override suspend fun deleteFavoritePharmacy(favoritePharmacy: PharmacyUseCaseData.Pharmacy) {
        withContext(dispatcher) {
            dataSource.favoritePharmacies.value = dataSource.favoritePharmacies.updateAndGet {
                val pharmacies = it.toMutableList()
                pharmacies.removeIf { item -> item.telematikId == favoritePharmacy.telematikId }
                pharmacies
            }
        }
    }

    override fun loadFavoritePharmacies(): Flow<List<OverviewPharmacyData.OverviewPharmacy>> =
        dataSource.favoritePharmacies

    override suspend fun markPharmacyAsFavourite(pharmacy: PharmacyUseCaseData.Pharmacy) {
        withContext(dispatcher) {
            dataSource.favoritePharmacies.value = dataSource.favoritePharmacies.updateAndGet {
                val favoritePharmacies = it.toMutableList()
                favoritePharmacies
                    .indexOfFirst { existingPharmacy -> existingPharmacy.telematikId == pharmacy.telematikId }
                    .takeIf { index -> index != INDEX_OUT_OF_BOUNDS }?.let { index ->
                        favoritePharmacies[index] = favoritePharmacies[index].copy(lastUsed = Clock.System.now())
                        favoritePharmacies
                    } ?: run {
                    val overviewPharmacy = OverviewPharmacyData.OverviewPharmacy(
                        lastUsed = Clock.System.now(),
                        usageCount = 1,
                        isFavorite = true,
                        telematikId = pharmacy.telematikId,
                        pharmacyName = pharmacy.name,
                        address = pharmacy.address ?: "---"
                    )
                    favoritePharmacies.add(overviewPharmacy)
                    favoritePharmacies
                }
            }
        }
    }

    override fun isPharmacyInFavorites(pharmacy: PharmacyUseCaseData.Pharmacy): Flow<Boolean> =
        dataSource.favoritePharmacies.mapNotNull {
            val favoritePharmacy = it.find { it.telematikId == pharmacy.telematikId }
            favoritePharmacy != null
        }.flowOn(dispatcher)
}
