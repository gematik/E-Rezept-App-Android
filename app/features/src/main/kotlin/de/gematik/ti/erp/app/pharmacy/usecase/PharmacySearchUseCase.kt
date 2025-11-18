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

package de.gematik.ti.erp.app.pharmacy.usecase

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.PagingSource
import androidx.paging.PagingState
import de.gematik.ti.erp.app.Requirement
import de.gematik.ti.erp.app.pharmacy.repository.PharmacyRepository
import de.gematik.ti.erp.app.pharmacy.usecase.mapper.toModel
import de.gematik.ti.erp.app.pharmacy.usecase.model.PharmacyUseCaseData
import de.gematik.ti.erp.app.pharmacy.usecase.model.PharmacyUseCaseData.SearchData.Companion.toPharmacyFilter
import de.gematik.ti.erp.app.settings.model.SettingsData
import de.gematik.ti.erp.app.settings.repository.SettingsRepository
import io.github.aakira.napier.Napier
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn

class PharmacySearchUseCase(
    private val repository: PharmacyRepository,
    private val settingsRepository: SettingsRepository,
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO
) {

    private val fakePagingCount = 100

    /**
     * PagingSource that handles pharmacy search with loading, success, and error states
     */
    inner class PharmacyPagingSource(
        private val searchData: PharmacyUseCaseData.SearchData
    ) : PagingSource<String, PharmacyUseCaseData.Pharmacy>() {

        override fun getRefreshKey(state: PagingState<String, PharmacyUseCaseData.Pharmacy>): String? = null

        override suspend fun load(params: LoadParams<String>): LoadResult<String, PharmacyUseCaseData.Pharmacy> {
            return try {
                // Save search parameters to settings (database operation)
                settingsRepository.savePharmacySearch(
                    SettingsData.PharmacySearch(
                        name = searchData.name,
                        locationEnabled = searchData.locationMode !is PharmacyUseCaseData.LocationMode.Disabled,
                        deliveryService = searchData.filter.deliveryService,
                        onlineService = searchData.filter.onlineService,
                        openNow = searchData.filter.openNow
                    )
                )

                // Convert search data to pharmacy filter
                val filter = searchData.toPharmacyFilter()
                val locationMode = searchData.locationMode

                // Make direct repository call and transform results
                repository.searchPharmacies(filter)
                    .fold(
                        onSuccess = { fhirCollection ->
                            val pharmacies = fhirCollection.entries
                                .toModel(locationMode, fhirCollection.type)
                                .sortedBy { pharmacy -> pharmacy.distance }

                            LoadResult.Page(
                                data = pharmacies,
                                prevKey = null,
                                nextKey = null
                            )
                        },
                        onFailure = { exception ->
                            Napier.e(exception) { "Pharmacy search failed" }
                            LoadResult.Error(exception)
                        }
                    )
            } catch (exception: Exception) {
                Napier.e(exception) { "Pharmacy search failed with exception" }
                LoadResult.Error(exception)
            }
        }
    }

    @Requirement(
        "A_20285#3",
        sourceSpecification = "gemSpec_eRp_FdV",
        rationale = "pharmacy search based on search term and filter criteria set by the user."
    )
    operator fun invoke(
        searchData: PharmacyUseCaseData.SearchData
    ): Flow<PagingData<PharmacyUseCaseData.Pharmacy>> {
        return Pager(
            config = PagingConfig(
                pageSize = fakePagingCount,
                initialLoadSize = fakePagingCount,
                enablePlaceholders = false
            ),
            pagingSourceFactory = { PharmacyPagingSource(searchData) }
        ).flow.flowOn(dispatcher)
    }
}
