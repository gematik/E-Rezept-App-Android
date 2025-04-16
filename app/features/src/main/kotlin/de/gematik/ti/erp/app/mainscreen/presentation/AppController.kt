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

package de.gematik.ti.erp.app.mainscreen.presentation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import de.gematik.ti.erp.app.Requirement
import de.gematik.ti.erp.app.base.NetworkStatusTracker
import de.gematik.ti.erp.app.base.presentation.GetActiveProfileController
import de.gematik.ti.erp.app.core.complexAutoSaver
import de.gematik.ti.erp.app.messages.domain.usecase.GetUnreadMessagesCountUseCase
import de.gematik.ti.erp.app.prescription.usecase.GetDownloadResourcesDetailStateUseCase
import de.gematik.ti.erp.app.profiles.usecase.GetActiveProfileUseCase
import de.gematik.ti.erp.app.profiles.usecase.model.ProfilesUseCaseData
import de.gematik.ti.erp.app.settings.usecase.GetOnboardingSucceededUseCase
import de.gematik.ti.erp.app.settings.usecase.GetScreenShotsAllowedUseCase
import de.gematik.ti.erp.app.settings.usecase.SaveWelcomeDrawerShownUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import org.kodein.di.compose.rememberInstance

@Suppress("LongParameterList", "StaticFieldLeak", "ConstructorParameterNaming")
class AppController(
    networkStatusTracker: NetworkStatusTracker,
    getActiveProfileUseCase: GetActiveProfileUseCase,
    getScreenShotsAllowedUseCase: GetScreenShotsAllowedUseCase,
    getOnboardingSucceededUseCase: GetOnboardingSucceededUseCase,
    downloadResourcesStateUseCase: GetDownloadResourcesDetailStateUseCase,
    private val getUnreadMessagesCountUseCase: GetUnreadMessagesCountUseCase,
    private val saveWelcomeDrawerShownUseCase: SaveWelcomeDrawerShownUseCase,
    private val _unreadOrders: MutableStateFlow<Long> = MutableStateFlow(0L),
    private val _orderedEvent: MutableStateFlow<OrderedEvent?> = MutableStateFlow(null)
) : GetActiveProfileController(
    getActiveProfileUseCase = getActiveProfileUseCase,
    onSuccess = { profile, coroutineScope ->
        coroutineScope.launch {
            _unreadOrders.value = getUnreadMessagesCountUseCase.invoke(profile.id).first()
        }
    }
) {

    enum class OrderedEvent {
        Success,
        Error
    }

    val orderedEvent: StateFlow<OrderedEvent?> = _orderedEvent
    val unreadOrders: StateFlow<Long> = _unreadOrders

    val isNetworkConnected: StateFlow<Boolean> = networkStatusTracker.networkStatus
        .stateIn(
            scope = controllerScope,
            started = SharingStarted.WhileSubscribed(),
            initialValue = true
        )

    fun resetOrderedEvent() {
        _orderedEvent.value = null
    }

    private val isScreenshotsAllowed = getScreenShotsAllowedUseCase.invoke()

    @Requirement(
        "O.Resi_1#5",
        sourceSpecification = "BSI-eRp-ePA",
        rationale = "Screenshots are disabled by default"
    )
    val screenshotsState
        @Composable
        get() = isScreenshotsAllowed.collectAsStateWithLifecycle(false)

    val refreshState = downloadResourcesStateUseCase.invoke()

    val onboardingSucceeded = getOnboardingSucceededUseCase.invoke()

    fun welcomeDrawerShown() = controllerScope.launch {
        saveWelcomeDrawerShownUseCase()
    }

    suspend fun updateUnreadOrders(profile: ProfilesUseCaseData.Profile) {
        _unreadOrders.value = getUnreadMessagesCountUseCase.invoke(profile.id).first()
    }

    fun onOrdered(hasError: Boolean) {
        _orderedEvent.value = if (hasError) OrderedEvent.Error else OrderedEvent.Success
    }
}

@Composable
fun rememberAppController(): AppController {
    val getActiveProfileUseCase by rememberInstance<GetActiveProfileUseCase>()
    val appController by rememberInstance<AppController>()

    val activeProfile by getActiveProfileUseCase().collectAsStateWithLifecycle(null)

    // to have a singleton instance of AppController
    return rememberSaveable(activeProfile, saver = complexAutoSaver()) { appController }
}
