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

import androidx.annotation.RestrictTo
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import de.gematik.ti.erp.app.pharmacy.ui.model.PharmacyScreenData
import de.gematik.ti.erp.app.pharmacy.usecase.GetOrderStateUseCase
import de.gematik.ti.erp.app.pharmacy.usecase.PharmacySearchUseCase
import de.gematik.ti.erp.app.pharmacy.usecase.model.PharmacyUseCaseData
import de.gematik.ti.erp.app.profiles.presentation.ProfilesController.Companion.DEFAULT_EMPTY_PROFILE
import de.gematik.ti.erp.app.profiles.usecase.GetActiveProfileUseCase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.kodein.di.compose.rememberInstance

@Stable
class PharmacyOrderController(
    private val getActiveProfileUseCase: GetActiveProfileUseCase,
    private val pharmacySearchUseCase: PharmacySearchUseCase,
    private val getOrderStateUseCase: GetOrderStateUseCase,
    private val scope: CoroutineScope
) {
    private val activeProfile by lazy {
        getActiveProfileUseCase().stateIn(scope, SharingStarted.WhileSubscribed(0, 0), DEFAULT_EMPTY_PROFILE)
    }

    private val isDirectRedeemEnabled = activeProfile.mapNotNull { it.lastAuthenticated == null }

    private val orders by lazy { getOrderStateUseCase() }

    @OptIn(ExperimentalCoroutinesApi::class)
    private val hasRedeemableOrders by lazy {
        orders.map { it.orders.isNotEmpty() }.mapLatest { return@mapLatest it }
    }

    private val prescription by lazy { orders.map { it.orders } }

    private val unSelectedPrescriptions: MutableStateFlow<List<String>> = MutableStateFlow(emptyList())

    private val updatedOrders =
        combine(
            unSelectedPrescriptions,
            orders
        ) { unSelectedPrescriptions, prescriptionOrder ->
            prescriptionOrder.copy(
                orders = prescriptionOrder.orders.filter { it.taskId !in unSelectedPrescriptions }
            )
        }

    val activeProfileState
        @Composable
        get() = activeProfile.collectAsStateWithLifecycle()

    val isDirectRedeemEnabledState
        @Composable
        get() = isDirectRedeemEnabled.collectAsStateWithLifecycle(false)

    val hasRedeemableOrdersState
        @Composable
        get() = hasRedeemableOrders.collectAsStateWithLifecycle(false)

    val orderState
        @Composable
        get() = updatedOrders.collectAsStateWithLifecycle(PharmacyUseCaseData.OrderState.Empty)

    val prescriptionsState
        @Composable
        get() = prescription.collectAsStateWithLifecycle(emptyList())

    var selectedPharmacy: PharmacyUseCaseData.Pharmacy? by mutableStateOf(null)
        private set

    var selectedOrderOption: PharmacyScreenData.OrderOption? by mutableStateOf(null)
        private set

    fun onSelectPharmacy(pharmacy: PharmacyUseCaseData.Pharmacy, orderOption: PharmacyScreenData.OrderOption) {
        selectedPharmacy = pharmacy
        selectedOrderOption = orderOption
    }

    fun onSelectPrescription(order: PharmacyUseCaseData.PrescriptionOrder) {
        unSelectedPrescriptions.update { it - order.taskId }
    }

    fun onDeselectPrescription(order: PharmacyUseCaseData.PrescriptionOrder) {
        unSelectedPrescriptions.update { it + order.taskId }
    }

    fun onSaveContact(contact: PharmacyUseCaseData.ShippingContact) {
        scope.launch {
            pharmacySearchUseCase.saveShippingContact(contact)
        }
    }

    fun onResetPharmacySelection() {
        selectedPharmacy = null
        selectedOrderOption = null
    }

    fun onResetPrescriptionSelection() {
        unSelectedPrescriptions.value = emptyList()
    }

    @RestrictTo(RestrictTo.Scope.TESTS)
    val updatedOrdersForTest = updatedOrders
}

@Composable
fun rememberPharmacyOrderController(): PharmacyOrderController {
    val getActiveProfileUseCase by rememberInstance<GetActiveProfileUseCase>()
    val pharmacySearchUseCase by rememberInstance<PharmacySearchUseCase>()
    val getOrderStateUseCase by rememberInstance<GetOrderStateUseCase>()
    val scope = rememberCoroutineScope()

    return remember {
        PharmacyOrderController(
            getActiveProfileUseCase = getActiveProfileUseCase,
            pharmacySearchUseCase = pharmacySearchUseCase,
            getOrderStateUseCase = getOrderStateUseCase,
            scope = scope
        )
    }
}
