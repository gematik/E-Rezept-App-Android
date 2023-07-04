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

package de.gematik.ti.erp.app.pharmacy.usecase

import de.gematik.ti.erp.app.DispatchProvider
import de.gematik.ti.erp.app.pharmacy.repository.PharmacyRepository
import de.gematik.ti.erp.app.pharmacy.usecase.model.PharmacyUseCaseData
import de.gematik.ti.erp.app.settings.model.SettingsData
import de.gematik.ti.erp.app.settings.usecase.SettingsUseCase
import kotlinx.coroutines.withContext

const val PharmacyMapNextResultsPerPage = 50
private const val PharmacyMapMaxResults = 120

class PharmacyMapsUseCase(
    private val repository: PharmacyRepository,
    private val settingsUseCase: SettingsUseCase,
    private val dispatchers: DispatchProvider
) {
    suspend fun searchPharmacies(
        searchData: PharmacyUseCaseData.SearchData
    ): List<PharmacyUseCaseData.Pharmacy> =
        withContext(dispatchers.IO) {
            settingsUseCase.savePharmacySearch(
                SettingsData.PharmacySearch(
                    name = searchData.name,
                    locationEnabled = searchData.locationMode !is PharmacyUseCaseData.LocationMode.Disabled,
                    deliveryService = searchData.filter.deliveryService,
                    onlineService = searchData.filter.onlineService,
                    openNow = searchData.filter.openNow
                )
            )

            val names = searchData.name.split(" ").filter { it.isNotEmpty() }
            val locationMode = searchData.locationMode
            val filter = run {
                val filterMap = mutableMapOf<String, String>()
                if (locationMode is PharmacyUseCaseData.LocationMode.Enabled) {
                    @Suppress("MagicNumber")
                    val radiusInKm = locationMode.radiusInMeter.toInt() / 1000
                    val loc = locationMode.location
                    filterMap += "near" to "${loc.latitude}|${loc.longitude}|$radiusInKm|km"
                }
                if (searchData.filter.onlineService) {
                    filterMap += "type" to "mobl"
                }
                filterMap
            }

            val initialResult = repository.searchPharmacies(
                names = names,
                filter = filter
            ).getOrThrow()

            if (initialResult.bundleResultCount == PharmacyInitialResultsPerPage) {
                val pharmacies = initialResult.pharmacies.mapToUseCasePharmacies().toMutableList()

                var offset = initialResult.bundleResultCount
                loop@ while (true) {
                    val result = repository.searchPharmaciesByBundle(
                        bundleId = initialResult.bundleId,
                        offset = offset,
                        count = PharmacyMapNextResultsPerPage
                    ).getOrThrow()

                    if (result.bundleResultCount < PharmacyMapNextResultsPerPage || offset > PharmacyMapMaxResults) {
                        break@loop
                    }

                    pharmacies += result.pharmacies.mapToUseCasePharmacies()
                    offset += result.bundleResultCount
                }

                pharmacies
            } else {
                initialResult.pharmacies.mapToUseCasePharmacies()
            }
        }
}
