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
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import de.gematik.ti.erp.app.Requirement
import de.gematik.ti.erp.app.orders.usecase.OrderUseCase
import de.gematik.ti.erp.app.prescription.ui.PrescriptionServiceState
import de.gematik.ti.erp.app.profiles.usecase.model.ProfilesUseCaseData
import de.gematik.ti.erp.app.settings.usecase.AcceptMLKitUseCase
import de.gematik.ti.erp.app.settings.usecase.AllowAnalyticsUseCase
import de.gematik.ti.erp.app.settings.usecase.GetCanStartToolTipsUseCase
import de.gematik.ti.erp.app.settings.usecase.GetMLKitAcceptedUseCase
import de.gematik.ti.erp.app.settings.usecase.GetOnboardingSucceededUseCase
import de.gematik.ti.erp.app.settings.usecase.GetScreenShotsAllowedUseCase
import de.gematik.ti.erp.app.settings.usecase.GetShowWelcomeDrawerUseCase
import de.gematik.ti.erp.app.settings.usecase.SavePasswordUseCase
import de.gematik.ti.erp.app.settings.usecase.SaveToolTippsShownUseCase
import de.gematik.ti.erp.app.settings.usecase.SaveWelcomeDrawerShownUseCase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.launch
import org.kodein.di.compose.rememberInstance

@Suppress("LongParameterList")
class MainScreenController(
    private val messageUseCase: OrderUseCase,
    private val saveToolTipsShownUseCase: SaveToolTippsShownUseCase,
    private val saveWelcomeDrawerShownUseCase: SaveWelcomeDrawerShownUseCase,
    private val acceptMLKitUseCase: AcceptMLKitUseCase,
    private val allowAnalyticsUseCase: AllowAnalyticsUseCase,
    private val savePasswordUseCase: SavePasswordUseCase,
    private val scope: CoroutineScope,
    getScreenShotsAllowedUseCase: GetScreenShotsAllowedUseCase,
    getOnboardingSucceededUseCase: GetOnboardingSucceededUseCase,
    getCanStartToolTipsUseCase: GetCanStartToolTipsUseCase,
    getShowWelcomeDrawerUseCase: GetShowWelcomeDrawerUseCase,
    getMLKitAcceptedUseCase: GetMLKitAcceptedUseCase
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

    private val screenshotsAllowed =
        getScreenShotsAllowedUseCase.invoke()

    val screenshotsState
        @Composable
        get() = screenshotsAllowed.collectAsStateWithLifecycle(false)

    val onboardingSucceeded = getOnboardingSucceededUseCase.invoke()

    private val canStartToolTips = getCanStartToolTipsUseCase.invoke()

    val canStartToolTipsState
        @Composable
        get() = canStartToolTips.collectAsStateWithLifecycle(false)

    fun toolTippsShown() = scope.launch {
        saveToolTipsShownUseCase()
    }

    private val showWelcomeDrawer =
        getShowWelcomeDrawerUseCase.invoke()

    val showWelcomeDrawerState
        @Composable
        get() = showWelcomeDrawer.collectAsStateWithLifecycle(false)

    fun welcomeDrawerShown() = scope.launch {
        saveWelcomeDrawerShownUseCase()
    }

    fun unreadOrders(profile: ProfilesUseCaseData.Profile) =
        messageUseCase.unreadOrders(profile)

    suspend fun onRefresh(event: PrescriptionServiceState) {
        _onRefreshEvent.emit(event)
    }

    fun onOrdered(hasError: Boolean) {
        orderedEvent = if (hasError) OrderedEvent.Error else OrderedEvent.Success
    }

    private val mlKitAccepted =
        getMLKitAcceptedUseCase.invoke()

    val mlKitAcceptedState
        @Composable
        get() = mlKitAccepted.collectAsStateWithLifecycle(false)

    fun acceptMLKit() = scope.launch {
        acceptMLKitUseCase()
    }

    @Requirement(
        "O.Purp_5#3",
        sourceSpecification = "BSI-eRp-ePA",
        rationale = "Enable usage analytics."
    )
    fun allowAnalytics(allow: Boolean) = scope.launch {
        allowAnalyticsUseCase(allow)
    }

    fun selectPasswordAsAuthenticationMode(password: String) = scope.launch {
        savePasswordUseCase.invoke(password)
    }
}

@Composable
fun rememberMainScreenController(): MainScreenController {
    val messageUseCase by rememberInstance<OrderUseCase>()
    val getScreenShotsAllowedUseCase by rememberInstance<GetScreenShotsAllowedUseCase>()
    val shouldShowOnboardingUseCase by rememberInstance<GetOnboardingSucceededUseCase>()
    val acceptMLKitUseCase by rememberInstance<AcceptMLKitUseCase>()
    val getMLKitAcceptedUseCase by rememberInstance<GetMLKitAcceptedUseCase>()
    val allowAnalyticsUseCase by rememberInstance<AllowAnalyticsUseCase>()
    val savePasswordUseCase by rememberInstance<SavePasswordUseCase>()
    val getShowToolTipsUseCase by rememberInstance<GetCanStartToolTipsUseCase>()
    val saveToolTipsShownUseCase by rememberInstance<SaveToolTippsShownUseCase>()
    val getShowWelcomeDrawerUseCase by rememberInstance<GetShowWelcomeDrawerUseCase>()
    val saveWelcomeDrawerShownUseCase by rememberInstance<SaveWelcomeDrawerShownUseCase>()
    val scope = rememberCoroutineScope()

    return remember {
        MainScreenController(
            messageUseCase = messageUseCase,
            getScreenShotsAllowedUseCase = getScreenShotsAllowedUseCase,
            getOnboardingSucceededUseCase = shouldShowOnboardingUseCase,
            acceptMLKitUseCase = acceptMLKitUseCase,
            getMLKitAcceptedUseCase = getMLKitAcceptedUseCase,
            allowAnalyticsUseCase = allowAnalyticsUseCase,
            savePasswordUseCase = savePasswordUseCase,
            getCanStartToolTipsUseCase = getShowToolTipsUseCase,
            saveToolTipsShownUseCase = saveToolTipsShownUseCase,
            getShowWelcomeDrawerUseCase = getShowWelcomeDrawerUseCase,
            saveWelcomeDrawerShownUseCase = saveWelcomeDrawerShownUseCase,
            scope = scope
        )
    }
}
