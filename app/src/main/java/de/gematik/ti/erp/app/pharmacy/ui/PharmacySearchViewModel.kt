/*
 * Copyright (c) 2021 gematik GmbH
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

package de.gematik.ti.erp.app.pharmacy.ui

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import androidx.paging.filter
import androidx.paging.insertHeaderItem
import androidx.paging.map
import dagger.hilt.android.lifecycle.HiltViewModel
import de.gematik.ti.erp.app.DispatchProvider
import de.gematik.ti.erp.app.api.Result
import de.gematik.ti.erp.app.common.usecase.HintUseCase
import de.gematik.ti.erp.app.common.usecase.model.PharmacyScreenHintEnableLocation
import de.gematik.ti.erp.app.pharmacy.repository.model.DeliveryPharmacyService
import de.gematik.ti.erp.app.pharmacy.repository.model.Location
import de.gematik.ti.erp.app.pharmacy.repository.model.isOpenAt
import de.gematik.ti.erp.app.pharmacy.usecase.PharmacySearchUseCase
import de.gematik.ti.erp.app.pharmacy.usecase.model.PharmacyUseCaseData
import de.gematik.ti.erp.app.pharmacy.usecase.model.UIPrescriptionOrder
import de.gematik.ti.erp.app.prescription.repository.RemoteRedeemOption
import javax.inject.Inject
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.async
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.shareIn
import kotlinx.coroutines.launch
import java.time.OffsetDateTime

sealed class PharmacySearchUi {
    class Pharmacy(val pharmacy: PharmacyUseCaseData.Pharmacy) : PharmacySearchUi()
    object LocationHint : PharmacySearchUi()
}

@HiltViewModel
class PharmacySearchViewModel @Inject constructor(
    private val useCase: PharmacySearchUseCase,
    private val hintUseCase: HintUseCase,
    private val dispatcher: DispatchProvider
) : ViewModel() {
    private val searchChannel = Channel<PharmacyUseCaseData.SearchData>()
    private var searchState = MutableStateFlow<PharmacyUseCaseData.SearchData?>(null)

    val defaultState
        get() = PharmacyUseCaseData.State(
            search = searchState.value,
            !hintUseCase.isHintCanceled(PharmacyScreenHintEnableLocation)
        )

    @OptIn(ExperimentalCoroutinesApi::class)
    val pharmacySearchFlow: Flow<PagingData<PharmacySearchUi>> =
        searchChannel
            .receiveAsFlow()
            .onEach {
                // if we receive an empty list as the first page and the last searchPagingItems state was already populated with results,
                // the continues loading won't work; this short timeout is an ugly workaround to this issue
                delay(100)
                searchState.value = it

                if (it.location != null) {
                    cancelLocationHint()
                }
            }
            .flatMapLatest { searchData ->
                useCase.searchPharmacies(searchData)
                    .map { pagingData ->
                        if (searchData.location != null) {
                            pagingData.map {
                                it.copy(
                                    distance = it.location?.minus(searchData.location)
                                )
                            }
                        } else {
                            pagingData
                        }.filter { pharmacy ->
                            if (searchData.filter.deliveryService) {
                                when {
                                    searchData.filter.deliveryService && pharmacy.provides.any { it is DeliveryPharmacyService } -> true
                                    else -> false
                                }
                            } else {
                                true
                            }
                        }.filter {
                            if (searchData.filter.openNow) {
                                when {
                                    it.openingHours == null -> false
                                    it.openingHours.isOpenAt(OffsetDateTime.now()) -> true
                                    else -> false
                                }
                            } else {
                                true
                            }
                        }.map<PharmacyUseCaseData.Pharmacy, PharmacySearchUi> {
                            PharmacySearchUi.Pharmacy(it)
                        }.let {
                            if (defaultState.showLocationHint) {
                                it.insertHeaderItem(item = PharmacySearchUi.LocationHint)
                            } else {
                                it
                            }
                        }
                    }
                    .cachedIn(viewModelScope)
            }.shareIn(
                viewModelScope,
                SharingStarted.WhileSubscribed(),
                1
            )

    suspend fun searchPharmacies(name: String, filter: PharmacyUseCaseData.Filter, location: Location?) {
        searchChannel.send(PharmacyUseCaseData.SearchData(name, filter, location))
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    fun screenState(): Flow<PharmacyUseCaseData.State> =
        combine(hintUseCase.cancelledHints, searchState) { cancelledHints, search ->
            PharmacyUseCaseData.State(
                search = search,
                PharmacyScreenHintEnableLocation !in cancelledHints
            )
        }

    fun cancelLocationHint() {
        hintUseCase.cancelHint(PharmacyScreenHintEnableLocation)
    }

    @Immutable
    data class RedeemUIState(
        val loading: Boolean = false,
        val success: Boolean = false,
        val error: Boolean = false,
        val fabState: Boolean = false
    )

    var uiState by mutableStateOf(RedeemUIState())
    private val orders = mutableSetOf<UIPrescriptionOrder>()

    fun fetchSelectedOrders(taskIds: String): Flow<List<UIPrescriptionOrder>> {
        return useCase.prescriptionDetailsForOrdering(*taskIds.split(",").toTypedArray())
            .onStart { updateUIState(loading = true, fabState = false) }
            .onCompletion { updateUIState(loading = false, fabState = orders.isNotEmpty()) }
            .map {
                it.map { order ->
                    order.selected = orders.contains(order)
                    order
                }
            }
    }

    fun toggleOrder(order: UIPrescriptionOrder): Boolean {
        order.selected = !order.selected
        if (order.selected) {
            orders.add(order)
        } else {
            orders.remove(order)
        }
        updateUIState(fabState = orders.isNotEmpty())
        return order.selected
    }

    fun triggerOrderInPharmacy(telematikId: String, redeemOption: RemoteRedeemOption) {
        viewModelScope.launch(dispatcher.io()) {
            updateUIState(loading = true, fabState = false)
            var uploadStatus = false
            for (order in orders) {
                val deferred = async {
                    useCase.redeemPrescription(
                        redeemOption,
                        order,
                        telematikId,
                    )
                }
                uploadStatus = deferred.await() is Result.Success
            }
            updateUIState(loading = false, fabState = true)
            if (uploadStatus) {
                updateUIState(success = true, fabState = false)
            } else {
                updateUIState(error = true, fabState = true)
            }
        }
    }

    private fun updateUIState(
        loading: Boolean = false,
        success: Boolean = false,
        error: Boolean = false,
        fabState: Boolean = false
    ) {
        uiState =
            uiState.copy(loading = loading, success = success, error = error, fabState = fabState)
    }
}
