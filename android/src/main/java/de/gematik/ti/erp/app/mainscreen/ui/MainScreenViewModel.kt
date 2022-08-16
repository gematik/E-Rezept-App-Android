/*
 * Copyright (c) 2022 gematik GmbH
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

package de.gematik.ti.erp.app.mainscreen.ui

import androidx.lifecycle.viewModelScope
import de.gematik.ti.erp.app.DispatchProvider
import androidx.lifecycle.ViewModel
import de.gematik.ti.erp.app.orders.usecase.OrderUseCase
import de.gematik.ti.erp.app.pharmacy.ui.model.PharmacyScreenData
import de.gematik.ti.erp.app.prescription.ui.PrescriptionServiceState
import de.gematik.ti.erp.app.profiles.repository.ProfileIdentifier
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.onEach

/**
 * Event used to indicate an action that should be visible to the user on main screen.
 */
sealed class ActionEvent {
    data class ReturnFromPharmacyOrder(val successfullyOrdered: PharmacyScreenData.OrderOption) : ActionEvent()
}

class MainScreenViewModel(
    private val messageUseCase: OrderUseCase,
    private val dispatchers: DispatchProvider
) : ViewModel() {

    private val _onRefreshEvent = MutableSharedFlow<PrescriptionServiceState>()
    val onRefreshEvent: Flow<PrescriptionServiceState>
        get() = _onRefreshEvent

    private val _onActionEvent = MutableStateFlow<ActionEvent?>(null)
    val onActionEvent: Flow<ActionEvent>
        get() = _onActionEvent.filterNotNull().onEach { _onActionEvent.value = null }

    fun unreadMessagesAvailable(profileIdentifier: ProfileIdentifier) =
        messageUseCase.unreadCommunicationsAvailable(profileIdentifier)

    suspend fun onRefresh(event: PrescriptionServiceState) {
        _onRefreshEvent.emit(event)
    }

    fun onAction(event: ActionEvent) {
        viewModelScope.launch(dispatchers.Default) {
            _onActionEvent.emit(event)
        }
    }
}
