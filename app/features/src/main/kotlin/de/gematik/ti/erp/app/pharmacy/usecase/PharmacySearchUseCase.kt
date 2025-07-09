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
import de.gematik.ti.erp.app.fhir.pharmacy.type.PharmacyVzdService
import de.gematik.ti.erp.app.pharmacy.repository.PharmacyRepository
import de.gematik.ti.erp.app.pharmacy.usecase.mapper.PharmacyInitialResultsPerPage
import de.gematik.ti.erp.app.pharmacy.usecase.mapper.PharmacyNextResultsPerPage
import de.gematik.ti.erp.app.pharmacy.usecase.mapper.toModel
import de.gematik.ti.erp.app.pharmacy.usecase.model.PharmacyUseCaseData
import de.gematik.ti.erp.app.pharmacy.usecase.model.PharmacyUseCaseData.SearchData.Companion.toPharmacyFilter
import de.gematik.ti.erp.app.settings.model.SettingsData
import de.gematik.ti.erp.app.settings.repository.SettingsRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlin.math.max
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

class PharmacySearchUseCase(
    private val repository: PharmacyRepository,
    private val settingsRepository: SettingsRepository,
    private val dispatchers: CoroutineDispatcher = Dispatchers.IO
) {
    data class PharmacyPagingKey(val bundleId: String, val offset: Int)

    private val isApoVzd: Boolean by lazy {
        repository.getSelectedVzdPharmacyBackend() == PharmacyVzdService.APOVZD
    }

    @Requirement(
        "A_20285#3",
        sourceSpecification = "gemSpec_eRp_FdV",
        rationale = "pharmacy search paging based on search term and filter criteria set by the user."
    )
    inner class PharmacyPagingSource(searchData: PharmacyUseCaseData.SearchData) :
        PagingSource<PharmacyPagingKey, PharmacyUseCaseData.Pharmacy>() {

        private val filter = searchData.toPharmacyFilter()

        private val locationMode = searchData.locationMode

        override fun getRefreshKey(
            state: PagingState<PharmacyPagingKey, PharmacyUseCaseData.Pharmacy>
        ): PharmacyPagingKey? = null

        @OptIn(ExperimentalUuidApi::class)
        override suspend fun load(
            params: LoadParams<PharmacyPagingKey>
        ): LoadResult<PharmacyPagingKey, PharmacyUseCaseData.Pharmacy> {
            val count = params.loadSize

            return when (params) {
                is LoadParams.Refresh -> {
                    repository.searchPharmacies(filter)
                        .map {
                            LoadResult.Page(
                                data = it.entries.toModel(locationMode, it.type).sortedBy { pharmacy -> pharmacy.distance },
                                nextKey = when {
                                    (it.total == PharmacyInitialResultsPerPage) && isApoVzd -> PharmacyPagingKey(
                                        it.id ?: Uuid.random().toString(),
                                        it.total
                                    )
                                    else -> null
                                },
                                prevKey = null
                            )
                        }.getOrElse { LoadResult.Error(it) }
                }

                is LoadParams.Append, is LoadParams.Prepend -> {
                    params.key?.let { key ->
                        repository.searchPharmaciesByBundle(key.bundleId, offset = key.offset, count = count).map {
                            val nextKey = if (it.total == count) {
                                PharmacyPagingKey(
                                    key.bundleId,
                                    key.offset + it.total
                                )
                            } else {
                                null
                            }
                            val prevKey = if (key.offset == 0) null else key.copy(offset = max(0, key.offset - count))

                            LoadResult.Page(
                                data = it.entries.toModel(locationMode, it.type).sortedBy { pharmacy -> pharmacy.distance },
                                nextKey = nextKey,
                                prevKey = prevKey,
                                itemsBefore = if (prevKey != null) count else 0,
                                itemsAfter = if (nextKey != null) count else 0
                            )
                        }.getOrElse { LoadResult.Error(it) }
                    } ?: LoadResult.Error(NullPointerException("key is null"))
                }
            }
        }
    }

    @Requirement(
        "A_20285#4",
        sourceSpecification = "gemSpec_eRp_FdV",
        rationale = "pharmacy search based on search term and filter criteria set by the user."
    )
    suspend operator fun invoke(
        searchData: PharmacyUseCaseData.SearchData
    ): Flow<PagingData<PharmacyUseCaseData.Pharmacy>> {
        settingsRepository.savePharmacySearch(
            SettingsData.PharmacySearch(
                name = searchData.name,
                locationEnabled = searchData.locationMode !is PharmacyUseCaseData.LocationMode.Disabled,
                deliveryService = searchData.filter.deliveryService,
                onlineService = searchData.filter.onlineService,
                openNow = searchData.filter.openNow
            )
        )

        return Pager(
            PagingConfig(
                pageSize = PharmacyNextResultsPerPage,
                initialLoadSize = PharmacyInitialResultsPerPage,
                maxSize = PharmacyInitialResultsPerPage * 2
            ),
            pagingSourceFactory = { PharmacyPagingSource(searchData) }
        ).flow.flowOn(dispatchers)
    }
}
