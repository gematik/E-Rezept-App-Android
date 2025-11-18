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

package de.gematik.ti.erp.app.pharmacy.presentation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.paging.cachedIn
import androidx.paging.filter
import androidx.paging.map
import de.gematik.ti.erp.app.Requirement
import de.gematik.ti.erp.app.base.Controller
import de.gematik.ti.erp.app.pharmacy.presentation.FilterType.Companion.getUpdatedFilter
import de.gematik.ti.erp.app.pharmacy.usecase.PharmacySearchUseCase
import de.gematik.ti.erp.app.pharmacy.usecase.model.PharmacyUseCaseData
import de.gematik.ti.erp.app.pharmacy.usecase.model.PharmacyUseCaseData.Coordinates
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.flow.onEach
import org.kodein.di.compose.rememberInstance

/**
 * Controller for the pharmacy search list
 * @param pharmacyFilter filter for the pharmacy filter
 * @param coordinates coordinates for the latitude and longitude
 */
class PharmacySearchListController(
    pharmacyFilter: PharmacyUseCaseData.Filter,
    coordinates: Coordinates?,
    searchTerm: String,
    private val pharmacySearchUseCase: PharmacySearchUseCase
) : Controller() {

    private val searchTerm = MutableStateFlow(searchTerm)

    private val defaultSearch = PharmacyUseCaseData.SearchData(
        name = searchTerm,
        filter = pharmacyFilter,
        locationMode = if (pharmacyFilter.nearBy) {
            coordinates?.let {
                PharmacyUseCaseData.LocationMode.Enabled(it)
            } ?: PharmacyUseCaseData.LocationMode.Disabled
        } else {
            PharmacyUseCaseData.LocationMode.Disabled
        }
    )

    private val searchParams = MutableStateFlow(defaultSearch)

    fun onSearchTerm(value: String) {
        searchTerm.value = value
        searchParams.value = searchParams.value.copy(name = value)
    }

    fun onFilter(
        filterType: FilterType,
        block: (FilterType) -> Unit
    ) {
        searchParams.value =
            if (filterType == FilterType.NEARBY && searchParams.value.filter.nearBy) {
                searchParams.value.copy(
                    name = searchTerm.value,
                    locationMode = PharmacyUseCaseData.LocationMode.Disabled,
                    filter = filterType.getUpdatedFilter(searchParams.value.filter)
                )
            } else {
                searchParams.value.copy(
                    name = searchTerm.value,
                    filter = filterType.getUpdatedFilter(searchParams.value.filter)
                )
            }
        block(filterType)
    }

    @Requirement(
        "A_20285#2",
        sourceSpecification = "gemSpec_eRp_FdV",
        rationale = "pharmacy state based on search params and filters"
    )
    @OptIn(ExperimentalCoroutinesApi::class)
    val pharmacies by lazy {
        searchParams.onEach { }.flatMapLatest { searchParams ->
            pharmacySearchUseCase.invoke(searchParams)
                .mapNotNull { pagingData ->
                    pagingData.map { it.location(searchParams.locationMode) }
                        .filter { it.deliveryService(searchParams.filter.deliveryService) }
                        .filter { it.onlineService(searchParams.filter.onlineService) }
                        .filter { it.isOpenNow(searchParams.filter.openNow) }
                }.cachedIn(controllerScope)
        }
    }

    val searchParamState
        @Composable
        get() = searchParams.collectAsStateWithLifecycle()
}

@Composable
fun rememberPharmacySearchListController(
    filter: PharmacyUseCaseData.Filter = PharmacyUseCaseData.Filter(),
    coordinates: Coordinates? = null,
    searchTerm: String = WILDCARD
): PharmacySearchListController {
    val searchUseCase by rememberInstance<PharmacySearchUseCase>()

    return remember(filter, coordinates, searchTerm) {
        PharmacySearchListController(
            pharmacyFilter = filter,
            coordinates = coordinates,
            searchTerm = searchTerm,
            pharmacySearchUseCase = searchUseCase
        )
    }
}
