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

package de.gematik.ti.erp.app.pharmacy.presentation

import androidx.compose.runtime.Composable
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import de.gematik.ti.erp.app.base.SharedController
import de.gematik.ti.erp.app.pharmacy.usecase.GetOrderStateUseCase
import de.gematik.ti.erp.app.pharmacy.usecase.GetOverviewPharmaciesUseCase
import de.gematik.ti.erp.app.pharmacy.usecase.model.PharmacyUseCaseData
import de.gematik.ti.erp.app.profiles.usecase.GetActiveProfileUseCase
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.updateAndGet
import kotlinx.coroutines.launch

class PharmacyGraphController(
    private val getActiveProfileUseCase: GetActiveProfileUseCase,
    private val getOverviewPharmaciesUseCase: GetOverviewPharmaciesUseCase,
    private val getOrderStateUseCase: GetOrderStateUseCase
) : SharedController() {

    private val activeProfile by lazy {
        getActiveProfileUseCase()
            .stateIn(controllerScope, SharingStarted.WhileSubscribed(0, 0), null)
    }

    private val orders by lazy { getOrderStateUseCase() }

    @OptIn(ExperimentalCoroutinesApi::class)
    private val hasRedeemableOrders by lazy {
        orders.map { it.orders.isNotEmpty() }.mapLatest { return@mapLatest it }
    }

    private val filter = MutableStateFlow(PharmacyUseCaseData.Filter())

    private val isDirectRedeemEnabled = activeProfile
        .mapNotNull { (it?.lastAuthenticated == null) }

    private val favouritePharmacies by lazy {
        getOverviewPharmaciesUseCase().stateIn(controllerScope, SharingStarted.Lazily, emptyList())
    }

    init {
        init()
    }

    fun init() {
        controllerScope.launch {
            filter.value = PharmacyUseCaseData.Filter()
        }
    }

    fun updateIsDirectRedeemEnabledOnFilter() {
        controllerScope.launch {
            combine(hasRedeemableOrders, isDirectRedeemEnabled) { hasOrders, isEnabled ->
                if (hasOrders && isEnabled) {
                    updateFilter(type = FilterType.DIRECT_REDEEM)
                }
            }
        }
    }

    fun updateFilter(type: FilterType) {
        when (type) {
            FilterType.NEARBY -> filter.value = filter.updateAndGet { it.copy(nearBy = !it.nearBy) }
            FilterType.OPEN_NOW -> filter.value = filter.updateAndGet { it.copy(openNow = !it.openNow) }
            FilterType.DELIVERY_SERVICE ->
                filter.value =
                    filter.updateAndGet { it.copy(deliveryService = !it.deliveryService) }

            FilterType.ONLINE_SERVICE ->
                filter.value =
                    filter.updateAndGet { it.copy(onlineService = !it.onlineService) }

            FilterType.DIRECT_REDEEM -> filter.value = filter.updateAndGet { it.copy(directRedeem = !it.directRedeem) }
        }
    }

    val filterState
        @Composable
        get() = filter.collectAsStateWithLifecycle(PharmacyUseCaseData.Filter())

    val favouritePharmaciesState
        @Composable
        get() = favouritePharmacies.collectAsStateWithLifecycle(emptyList())

    enum class FilterType {
        NEARBY,
        OPEN_NOW,
        DELIVERY_SERVICE,
        ONLINE_SERVICE,
        DIRECT_REDEEM
    }
}
