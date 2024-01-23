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

package de.gematik.ti.erp.app.demomode.repository.pharmacy

import de.gematik.ti.erp.app.demomode.datasource.DemoModeDataSource
import de.gematik.ti.erp.app.demomode.datasource.INDEX_OUT_OF_BOUNDS
import de.gematik.ti.erp.app.pharmacy.model.OverviewPharmacyData
import de.gematik.ti.erp.app.pharmacy.repository.PharmacyLocalDataSource
import de.gematik.ti.erp.app.pharmacy.usecase.model.PharmacyUseCaseData
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.flow.updateAndGet
import kotlinx.coroutines.withContext
import kotlinx.datetime.Clock

class DemoPharmacyLocalDataSource(
    private val dataSource: DemoModeDataSource,
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO
) : PharmacyLocalDataSource {
    override suspend fun deleteOverviewPharmacy(overviewPharmacy: OverviewPharmacyData.OverviewPharmacy) {
        withContext(dispatcher) {
            dataSource.oftenUsedPharmacies.value = dataSource.oftenUsedPharmacies.updateAndGet {
                val pharmacies = it.toMutableList()
                pharmacies.removeIf { item -> item.telematikId == overviewPharmacy.telematikId }
                pharmacies
            }
            dataSource.favoritePharmacies.value = dataSource.favoritePharmacies.updateAndGet {
                val pharmacies = it.toMutableList()
                pharmacies.removeIf { item -> item.telematikId == overviewPharmacy.telematikId }
                pharmacies
            }
        }
    }

    override fun loadOftenUsedPharmacies(): Flow<List<OverviewPharmacyData.OverviewPharmacy>> =
        dataSource.oftenUsedPharmacies

    override suspend fun saveOrUpdateOftenUsedPharmacy(pharmacy: PharmacyUseCaseData.Pharmacy) {
        withContext(dispatcher) {
            dataSource.oftenUsedPharmacies.value = dataSource.oftenUsedPharmacies.updateAndGet {
                val oftenUsedPharmacies = it.toMutableList()
                oftenUsedPharmacies.indexOfFirst { item -> item.telematikId == pharmacy.telematikId }
                    .takeIf { index -> index != INDEX_OUT_OF_BOUNDS }
                    ?.let { index ->
                        oftenUsedPharmacies[index] = oftenUsedPharmacies[index].copy(
                            lastUsed = Clock.System.now(),
                            usageCount = oftenUsedPharmacies[index].usageCount + 1
                        )
                        oftenUsedPharmacies
                    } ?: run {
                    val isFavourite = dataSource.favoritePharmacies.value
                        .find { item -> item.telematikId == pharmacy.telematikId } != null
                    val overviewPharmacy = OverviewPharmacyData.OverviewPharmacy(
                        lastUsed = Clock.System.now(),
                        usageCount = 1,
                        isFavorite = isFavourite,
                        telematikId = pharmacy.telematikId,
                        pharmacyName = pharmacy.name,
                        address = pharmacy.address ?: "---"
                    )
                    oftenUsedPharmacies.add(overviewPharmacy)
                    oftenUsedPharmacies
                }
            }
        }
    }

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

    override suspend fun saveOrUpdateFavoritePharmacy(pharmacy: PharmacyUseCaseData.Pharmacy) {
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

    override suspend fun markAsRedeemed(taskId: String) {
        withContext(dispatcher) {
            dataSource.scannedTasks.value = dataSource.scannedTasks.updateAndGet {
                val scannedTasks = it.toMutableList()
                val index = scannedTasks.indexOfFirst { item -> item.taskId == taskId }
                scannedTasks[index] = scannedTasks[index].copy(redeemedOn = Clock.System.now())
                scannedTasks
            }
        }
    }
}
