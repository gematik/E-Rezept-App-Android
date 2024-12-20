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

package de.gematik.ti.erp.app.redeem.presentation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.State
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import de.gematik.ti.erp.app.base.presentation.GetActiveProfileController
import de.gematik.ti.erp.app.pharmacy.model.PharmacyScreenData
import de.gematik.ti.erp.app.pharmacy.usecase.GetOrderStateUseCase
import de.gematik.ti.erp.app.pharmacy.usecase.GetShippingContactValidationUseCase
import de.gematik.ti.erp.app.pharmacy.usecase.SaveShippingContactUseCase
import de.gematik.ti.erp.app.pharmacy.usecase.ShippingContactState
import de.gematik.ti.erp.app.pharmacy.usecase.model.PharmacyUseCaseData
import de.gematik.ti.erp.app.profiles.usecase.GetActiveProfileUseCase
import io.github.aakira.napier.Napier
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

abstract class OnlineRedeemGraphController(
    getActiveProfileUseCase: GetActiveProfileUseCase
) : GetActiveProfileController(getActiveProfileUseCase) {
    abstract val singleTaskId: MutableStateFlow<String>

    abstract fun onResetPrescriptionSelection()

    abstract fun validateAndGetShippingContactState(
        contact: PharmacyUseCaseData.ShippingContact?,
        selectedOrderOption: PharmacyScreenData.OrderOption?
    ): ShippingContactState?

    abstract fun saveShippingContact(contact: PharmacyUseCaseData.ShippingContact)

    @Composable
    abstract fun redeemableOrderState(): State<List<PharmacyUseCaseData.PrescriptionOrder>>

    @Composable
    abstract fun selectedOrderState(): State<PharmacyUseCaseData.OrderState>

    abstract fun onPrescriptionSelectionChanged(order: PharmacyUseCaseData.PrescriptionOrder, select: Boolean)
    abstract fun saveSingleTaskId(taskId: String)
    abstract suspend fun deselectPrescriptions(taskId: String)
}

@Stable
class DefaultOnlineRedeemGraphController(
    getActiveProfileUseCase: GetActiveProfileUseCase,
    private val getOrderStateUseCase: GetOrderStateUseCase,
    private val getShippingContactValidationUseCase: GetShippingContactValidationUseCase,
    private val saveShippingContactUseCase: SaveShippingContactUseCase
) : OnlineRedeemGraphController(getActiveProfileUseCase) {

    override val singleTaskId by lazy { MutableStateFlow("") }

    private val orders by lazy { getOrderStateUseCase() }

    private val unselectedPrescriptionTaskIds: MutableStateFlow<List<String>> by lazy {
        MutableStateFlow(emptyList())
    }

    private val prescriptionOrders by lazy {
        orders.map {
            it.prescriptionOrders
        }
    }

    private val selectedOrders by lazy {
        Napier.d { "--- selected orders changed ---" }
        combine(
            unselectedPrescriptionTaskIds,
            orders
        ) { unSelectedPrescriptions, orderState ->
            orderState.copy(
                prescriptionOrders = orderState.prescriptionOrders.filter { it.taskId !in unSelectedPrescriptions }
            )
        }
    }

    override fun onPrescriptionSelectionChanged(order: PharmacyUseCaseData.PrescriptionOrder, select: Boolean) {
        if (!select) {
            unselectedPrescriptionTaskIds.update { it + order.taskId }
        } else {
            unselectedPrescriptionTaskIds.update { it - order.taskId }
        }
    }

    override fun saveSingleTaskId(taskId: String) {
        singleTaskId.value = taskId
    }

    override suspend fun deselectPrescriptions(taskId: String) {
        if (taskId != singleTaskId.value) {
            selectedOrders.first().prescriptionOrders.forEach {
                if (it.taskId != taskId) {
                    onPrescriptionSelectionChanged(it, false)
                }
            }
        }
        saveSingleTaskId(taskId)
    }

    override fun onResetPrescriptionSelection() {
        unselectedPrescriptionTaskIds.value = emptyList()
        singleTaskId.value = ""
    }

    override fun validateAndGetShippingContactState(
        contact: PharmacyUseCaseData.ShippingContact?,
        selectedOrderOption: PharmacyScreenData.OrderOption?
    ): ShippingContactState? = contact?.let { getShippingContactValidationUseCase(it, selectedOrderOption) }

    override fun saveShippingContact(contact: PharmacyUseCaseData.ShippingContact) {
        controllerScope.launch {
            saveShippingContactUseCase(contact)
        }
    }

    // all orders that are available for redeeming
    @Composable
    override fun redeemableOrderState() = prescriptionOrders.collectAsStateWithLifecycle(emptyList())

    // all orders that are selected for redeeming
    @Composable
    override fun selectedOrderState() =
        selectedOrders.collectAsStateWithLifecycle(PharmacyUseCaseData.OrderState.Empty)
}
