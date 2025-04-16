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
import de.gematik.ti.erp.app.pharmacy.repository.datasource.local.OftenUsedPharmacyLocalDataSource
import de.gematik.ti.erp.app.pharmacy.usecase.model.PharmacyUseCaseData
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.updateAndGet
import kotlinx.coroutines.withContext
import kotlinx.datetime.Clock

class DemoOftenUsePharmacyLocalDataSource(
    private val dataSource: DemoModeDataSource,
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO
) : OftenUsedPharmacyLocalDataSource {
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

    override suspend fun markPharmacyAsOftenUsed(pharmacy: PharmacyUseCaseData.Pharmacy) {
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
}
