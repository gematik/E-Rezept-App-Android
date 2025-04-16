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

package de.gematik.ti.erp.app.redeem.presentation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.State
import androidx.compose.runtime.collectAsState
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import de.gematik.ti.erp.app.base.presentation.GetActiveProfileController
import de.gematik.ti.erp.app.pharmacy.model.PharmacyScreenData
import de.gematik.ti.erp.app.pharmacy.usecase.GetOrderStateUseCase
import de.gematik.ti.erp.app.pharmacy.usecase.GetShippingContactValidationUseCase
import de.gematik.ti.erp.app.pharmacy.usecase.SaveShippingContactUseCase
import de.gematik.ti.erp.app.pharmacy.usecase.ShippingContactState
import de.gematik.ti.erp.app.pharmacy.usecase.model.PharmacyUseCaseData
import de.gematik.ti.erp.app.pharmacy.usecase.model.PharmacyUseCaseData.OrderState
import de.gematik.ti.erp.app.pharmacy.usecase.model.PharmacyUseCaseData.PrescriptionInOrder
import de.gematik.ti.erp.app.profiles.usecase.GetActiveProfileUseCase
import de.gematik.ti.erp.app.redeem.model.RedeemPrescriptionSelectionState
import de.gematik.ti.erp.app.redeem.model.RedeemPrescriptionSelectionState.ListOfPrescriptions
import de.gematik.ti.erp.app.redeem.model.RedeemPrescriptionSelectionState.NotInFinalRedeemStep
import de.gematik.ti.erp.app.redeem.model.RedeemPrescriptionSelectionState.SinglePrescription
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

abstract class OnlineRedeemGraphController(
    getActiveProfileUseCase: GetActiveProfileUseCase
) : GetActiveProfileController(getActiveProfileUseCase) {

    abstract fun setPrescriptionSelectionState(taskId: String?)

    abstract fun refresh()

    abstract fun onResetPrescriptionSelection()

    abstract fun validateAndGetShippingContactState(
        contact: PharmacyUseCaseData.ShippingContact?,
        selectedOrderOption: PharmacyScreenData.OrderOption?
    ): ShippingContactState?

    abstract fun saveShippingContact(contact: PharmacyUseCaseData.ShippingContact)

    abstract fun onPrescriptionSelectionChanged(prescriptionInOrder: PrescriptionInOrder, select: Boolean)

    abstract fun deselectInvalidPrescriptions(taskIds: List<String>)

    abstract fun updatePrescriptionSelectionFailureFlag()

    abstract val selectedOrderState: State<OrderState>
        @Composable get

    abstract val redeemableOrderState: State<List<PrescriptionInOrder>>
        @Composable get
}

@Stable
class DefaultOnlineRedeemGraphController(
    getActiveProfileUseCase: GetActiveProfileUseCase,
    private val getOrderStateUseCase: GetOrderStateUseCase,
    private val getShippingContactValidationUseCase: GetShippingContactValidationUseCase,
    private val saveShippingContactUseCase: SaveShippingContactUseCase
) : OnlineRedeemGraphController(getActiveProfileUseCase) {

    @OptIn(ExperimentalCoroutinesApi::class)
    private val orderState: Flow<OrderState> by lazy {
        refreshTrigger.flatMapLatest {
            getOrderStateUseCase()
        }
    }

    private val refreshTrigger = MutableStateFlow(Unit) // Trigger for refreshing the flow

    private val prescriptionSelectionState: MutableStateFlow<RedeemPrescriptionSelectionState> by lazy {
        MutableStateFlow(NotInFinalRedeemStep)
    }

    // list of tasks that could be in the multiple-order but have been deselected
    private val deselectedTaskIdsFromOrder: MutableStateFlow<List<String>> by lazy {
        MutableStateFlow(emptyList())
    }

    // list of tasks that are modified on a single prescription order
    private val selectedTaskIdsFromOrder: MutableStateFlow<List<String>> by lazy {
        MutableStateFlow(emptyList())
    }

    // flag to maintain the failure state when the user navigates from PrescriptionSelection back to OrderOverview
    private val prescriptionSelectionFailureFlag = MutableStateFlow(false)

    // used for HowToRedeem, OnlineRedeemPreferences, PrescriptionSelection screens
    private val possiblePrescriptionsInOrders by lazy { orderState.map { it.prescriptionsInOrder } }

    // list of orders that are selected for redeeming
    private val selectedOrderStateFlow: Flow<OrderState>
        get() =
            combine(
                deselectedTaskIdsFromOrder,
                selectedTaskIdsFromOrder,
                prescriptionSelectionState,
                orderState
            ) { deselectedTaskIds, selectedTaskIds, selectionState, orderState ->
                when (selectionState) {
                    is SinglePrescription -> orderState.withSinglePrescription(selectedTaskIds)
                    else -> orderState.withMultiplePrescriptions(deselectedTaskIds)
                }
            }

    // for a list of prescriptions other than the invalid ones all other prescriptions are kept
    private fun OrderState.withMultiplePrescriptions(deselectedTaskIds: List<String>): OrderState {
        val prescriptionsInOrder = prescriptionsInOrder.filterNot { it.taskId in deselectedTaskIds }
        val updatedState = copy(prescriptionsInOrder = prescriptionsInOrder)
        return updatedState
    }

    // for single prescriptions only the selected prescription will be kept if its a valid prescription
    private fun OrderState.withSinglePrescription(selectedTaskIds: List<String>): OrderState {
        val prescriptionsInOrder = prescriptionsInOrder.filter { it.taskId in selectedTaskIds }
        val updatedState = copy(prescriptionsInOrder = prescriptionsInOrder)
        return updatedState
    }

    private fun updateSelectedPrescriptionsBasedOnSelectionFlag(taskId: String) {
        when {
            // the flag is set from PrescriptionSelection, when it is not set and there are no selected task-ids in the orders
            // we can use the task-id to start the selectedTaskIdsFromOrder
            !prescriptionSelectionFailureFlag.value && selectedTaskIdsFromOrder.value.isEmpty() -> {
                selectedTaskIdsFromOrder.value = mutableListOf(taskId)
            }

            // if the flag is set, the selected task-ids are under user control and we do not change them
            prescriptionSelectionFailureFlag.value -> prescriptionSelectionFailureFlag.value = false
        }
    }

    // updated from Prescription selection when all orders are deselected
    override fun updatePrescriptionSelectionFailureFlag() {
        prescriptionSelectionFailureFlag.value = true
    }

    // this state decides which prescriptions can be added or removed from the order,
    // happens only from OrderOverview
    override fun setPrescriptionSelectionState(taskId: String?) {
        controllerScope.launch {
            when (taskId.isNullOrEmpty()) {
                true -> prescriptionSelectionState.value = ListOfPrescriptions
                else -> {
                    updateSelectedPrescriptionsBasedOnSelectionFlag(taskId)
                    prescriptionSelectionState.value = SinglePrescription(taskId)
                }
            }
        }
    }

    override fun refresh() {
        refreshTrigger.value = Unit
    }

    // on the Prescription selection, the user interaction changes the prescriptions that are in the order
    override fun onPrescriptionSelectionChanged(prescriptionInOrder: PrescriptionInOrder, select: Boolean) {
        val taskId = prescriptionInOrder.taskId
        val state = prescriptionSelectionState.value
        when (state) {
            is SinglePrescription -> selectedTaskIdsFromOrder.update { if (select) it + taskId else it - taskId }
            else -> deselectedTaskIdsFromOrder.update { if (select) it - taskId else it + taskId }
        }
    }

    // all the tasks that have been deemed invalid are now deselected from the order
    // and we make a call the database to refresh the order state
    override fun deselectInvalidPrescriptions(taskIds: List<String>) {
        val taskIdSet = taskIds.toSet()
        listOf(deselectedTaskIdsFromOrder, selectedTaskIdsFromOrder).forEach { stateFlow ->
            stateFlow.update { it - taskIdSet }
        }
    }

    // every task is now removed from the order making it a complete invalid order
    // and we make call to the database to refresh the order state
    override fun onResetPrescriptionSelection() {
        listOf(deselectedTaskIdsFromOrder, selectedTaskIdsFromOrder).forEach { it.value = emptyList() }
        prescriptionSelectionState.value = NotInFinalRedeemStep
        prescriptionSelectionFailureFlag.value = false
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

    override val selectedOrderState
        @Composable
        get() = selectedOrderStateFlow.collectAsState(OrderState.Empty)

    // all orders that are available for redeeming
    override val redeemableOrderState
        @Composable
        get() = possiblePrescriptionsInOrders.collectAsStateWithLifecycle(emptyList())
}
