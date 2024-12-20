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

import de.gematik.ti.erp.app.pharmacy.repository.PharmacyRepository
import de.gematik.ti.erp.app.pharmacy.usecase.mapper.PharmacyInitialResultsPerPage
import de.gematik.ti.erp.app.pharmacy.usecase.mapper.toModel
import de.gematik.ti.erp.app.pharmacy.usecase.model.PharmacyUseCaseData
import de.gematik.ti.erp.app.settings.model.SettingsData
import de.gematik.ti.erp.app.settings.repository.SettingsRepository
import io.github.aakira.napier.Napier
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

const val PharmacyMapNextResultsPerPage = 50
private const val PharmacyMapMaxResults = 120
private const val DefaultRadiusInMeter = 999 * 1000.0

class PharmacyMapsUseCase(
    private val repository: PharmacyRepository,
    private val settingsRepository: SettingsRepository,
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO
) {
    @Suppress("MagicNumber")
    suspend operator fun invoke(
        searchData: PharmacyUseCaseData.MapsSearchData,
        forcedRadius: Double? = null
    ): List<PharmacyUseCaseData.Pharmacy> =
        withContext(dispatcher) {
            try {
                settingsRepository.savePharmacySearch(
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
                        val radiusInKm = (forcedRadius?.toInt() ?: locationMode.radiusInMeter.toInt()) / 1000
                        val loc = locationMode.coordinates
                        filterMap += "near" to "${loc.latitude}|${loc.longitude}|$radiusInKm|km"
                    } else if (locationMode is PharmacyUseCaseData.LocationMode.Disabled &&
                        searchData.coordinates != null
                    ) {
                        val radiusInKm = (forcedRadius?.toInt() ?: DefaultRadiusInMeter.toInt()) / 1000
                        val loc = searchData.coordinates
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
                    val pharmacies = initialResult.pharmacies.toModel().toMutableList()

                    var offset = initialResult.bundleResultCount
                    loop@ while (true) {
                        val result = repository.searchPharmaciesByBundle(
                            bundleId = initialResult.bundleId,
                            offset = offset,
                            count = PharmacyMapNextResultsPerPage
                        ).getOrThrow()

                        if (result.bundleResultCount < PharmacyMapNextResultsPerPage ||
                            offset > PharmacyMapMaxResults
                        ) {
                            break@loop
                        }

                        pharmacies += result.pharmacies.toModel()
                        offset += result.bundleResultCount
                    }

                    pharmacies
                } else {
                    initialResult.pharmacies.toModel()
                }
            } catch (e: Throwable) {
                Napier.e { "silent exception ${e.stackTraceToString()}" }
                emptyList()
            }
        }
}
