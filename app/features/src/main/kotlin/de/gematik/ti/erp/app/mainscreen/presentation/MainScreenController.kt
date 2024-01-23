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

package de.gematik.ti.erp.app.mainscreen.presentation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import de.gematik.ti.erp.app.attestation.usecase.IntegrityUseCase
import de.gematik.ti.erp.app.orders.usecase.OrderUseCase
import de.gematik.ti.erp.app.prescription.ui.PrescriptionServiceState
import de.gematik.ti.erp.app.profiles.repository.ProfileIdentifier
import de.gematik.ti.erp.app.profiles.usecase.model.ProfilesUseCaseData
import de.gematik.ti.erp.app.settings.usecase.SettingsUseCase
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import org.kodein.di.compose.rememberInstance

class MainScreenController(
    private val integrityUseCase: IntegrityUseCase,
    private val messageUseCase: OrderUseCase,
    private val settingsUseCase: SettingsUseCase
) {

    enum class OrderedEvent {
        Success,
        Error
    }

    private val _onRefreshEvent = MutableSharedFlow<PrescriptionServiceState>()
    val onRefreshEvent: Flow<PrescriptionServiceState>
        get() = _onRefreshEvent

    var orderedEvent: OrderedEvent? by mutableStateOf(null)
        private set

    fun resetOrderedEvent() {
        orderedEvent = null
    }

    fun hasUnreadPrescriptionAvailable(profileIdentifier: ProfileIdentifier) =
        messageUseCase.unreadPrescriptionAvailable(profileIdentifier)

    fun unreadOrders(profile: ProfilesUseCaseData.Profile) =
        messageUseCase.unreadOrders(profile)

    fun unreadPrescriptionsInAllOrders(profileIdentifier: ProfileIdentifier) =
        messageUseCase.unreadPrescriptionsInAllOrders(profileIdentifier)

    suspend fun onRefresh(event: PrescriptionServiceState) {
        _onRefreshEvent.emit(event)
    }

    fun onOrdered(hasError: Boolean) {
        orderedEvent = if (hasError) OrderedEvent.Error else OrderedEvent.Success
    }

    fun checkDeviceIntegrity() = integrityUseCase.runIntegrityAttestation().map {
        !it && !settingsUseCase.general.first().userHasAcceptedInsecureDevice
    }
}

@Composable
fun rememberMainScreenController(): MainScreenController {
    val integrityUseCase by rememberInstance<IntegrityUseCase>()
    val messageUseCase by rememberInstance<OrderUseCase>()
    val settingsUseCase by rememberInstance<SettingsUseCase>()

    return remember {
        MainScreenController(
            integrityUseCase = integrityUseCase,
            messageUseCase = messageUseCase,
            settingsUseCase = settingsUseCase
        )
    }
}
