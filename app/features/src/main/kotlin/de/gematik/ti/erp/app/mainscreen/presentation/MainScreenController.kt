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
import de.gematik.ti.erp.app.consent.usecase.SaveGrantConsentDrawerShownUseCase
import de.gematik.ti.erp.app.consent.usecase.ShowGrantConsentUseCase
import de.gematik.ti.erp.app.orders.usecase.GetUnreadOrdersUseCase
import de.gematik.ti.erp.app.prescription.ui.PrescriptionServiceState
import de.gematik.ti.erp.app.profiles.repository.ProfileIdentifier
import de.gematik.ti.erp.app.profiles.usecase.model.ProfilesUseCaseData
import de.gematik.ti.erp.app.settings.usecase.GetCanStartToolTipsUseCase
import de.gematik.ti.erp.app.settings.usecase.GetMLKitAcceptedUseCase
import de.gematik.ti.erp.app.settings.usecase.GetOnboardingSucceededUseCase
import de.gematik.ti.erp.app.settings.usecase.GetScreenShotsAllowedUseCase
import de.gematik.ti.erp.app.settings.usecase.GetShowWelcomeDrawerUseCase
import de.gematik.ti.erp.app.settings.usecase.SaveToolTippsShownUseCase
import de.gematik.ti.erp.app.settings.usecase.SaveWelcomeDrawerShownUseCase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.launch
import org.kodein.di.compose.rememberInstance

@Suppress("LongParameterList")
class MainScreenController(
    private val getUnreadOrdersUseCase: GetUnreadOrdersUseCase,
    private val saveToolTipsShownUseCase: SaveToolTippsShownUseCase,
    private val saveWelcomeDrawerShownUseCase: SaveWelcomeDrawerShownUseCase,
    private val saveGrantConsentDrawerShownUseCase: SaveGrantConsentDrawerShownUseCase,
    private val getScreenShotsAllowedUseCase: GetScreenShotsAllowedUseCase,
    private val getOnboardingSucceededUseCase: GetOnboardingSucceededUseCase,
    private val getCanStartToolTipsUseCase: GetCanStartToolTipsUseCase,
    private val getShowWelcomeDrawerUseCase: GetShowWelcomeDrawerUseCase,
    private val showGrantConsentUseCase: ShowGrantConsentUseCase,
    private val getMLKitAcceptedUseCase: GetMLKitAcceptedUseCase,
    private val scope: CoroutineScope
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

    fun toolTipsShown() = scope.launch {
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

    private val showGiveConsentDrawer =
        showGrantConsentUseCase.invoke()

    val showGiveConsentDrawerState
        @Composable
        get() = showGiveConsentDrawer.collectAsStateWithLifecycle(false)

    fun giveConsentDrawerShown(profileId: ProfileIdentifier) = scope.launch {
        saveGrantConsentDrawerShownUseCase(profileId)
    }

    fun updateUnreadOrders(profile: ProfilesUseCaseData.Profile) = getUnreadOrdersUseCase.invoke(profile.id)

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
}

@Composable
fun rememberMainScreenController(): MainScreenController {
    val getUnreadOrdersUseCase by rememberInstance<GetUnreadOrdersUseCase>()
    val getScreenShotsAllowedUseCase by rememberInstance<GetScreenShotsAllowedUseCase>()
    val shouldShowOnboardingUseCase by rememberInstance<GetOnboardingSucceededUseCase>()
    val getMLKitAcceptedUseCase by rememberInstance<GetMLKitAcceptedUseCase>()
    val getShowToolTipsUseCase by rememberInstance<GetCanStartToolTipsUseCase>()
    val saveToolTipsShownUseCase by rememberInstance<SaveToolTippsShownUseCase>()
    val getShowWelcomeDrawerUseCase by rememberInstance<GetShowWelcomeDrawerUseCase>()
    val saveWelcomeDrawerShownUseCase by rememberInstance<SaveWelcomeDrawerShownUseCase>()
    val getShowGiveConsentDrawerUseCase by rememberInstance<ShowGrantConsentUseCase>()
    val saveGrantConsentDrawerShownUseCase by rememberInstance<SaveGrantConsentDrawerShownUseCase>()
    val scope = rememberCoroutineScope()

    return remember {
        MainScreenController(
            getUnreadOrdersUseCase = getUnreadOrdersUseCase,
            getScreenShotsAllowedUseCase = getScreenShotsAllowedUseCase,
            getOnboardingSucceededUseCase = shouldShowOnboardingUseCase,
            getMLKitAcceptedUseCase = getMLKitAcceptedUseCase,
            getCanStartToolTipsUseCase = getShowToolTipsUseCase,
            saveToolTipsShownUseCase = saveToolTipsShownUseCase,
            getShowWelcomeDrawerUseCase = getShowWelcomeDrawerUseCase,
            showGrantConsentUseCase = getShowGiveConsentDrawerUseCase,
            saveGrantConsentDrawerShownUseCase = saveGrantConsentDrawerShownUseCase,
            saveWelcomeDrawerShownUseCase = saveWelcomeDrawerShownUseCase,
            scope = scope
        )
    }
}
