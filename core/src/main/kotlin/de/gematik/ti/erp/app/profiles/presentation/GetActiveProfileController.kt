/*
 * Copyright (Change Date see Readme), gematik GmbH
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
 * In case of changes by gematik GmbH find details in the "Readme" file.
 *
 * See the Licence for the specific language governing permissions and limitations under the Licence.
 *
 * *******
 *
 * For additional notes and disclaimer from gematik and in case of changes by gematik find details in the "Readme" file.
 */

package de.gematik.ti.erp.app.profiles.presentation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.lifecycle.viewModelScope
import de.gematik.ti.erp.app.base.Controller
import de.gematik.ti.erp.app.profiles.usecase.GetActiveProfileUseCase
import de.gematik.ti.erp.app.profiles.usecase.model.ProfilesUseCaseData
import de.gematik.ti.erp.app.utils.compose.ComposableEvent
import de.gematik.ti.erp.app.utils.uistate.UiState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import org.kodein.di.compose.rememberInstance

/**
 * Controller responsible for managing and providing the active profile state.
 *
 * This controller fetches the active profile from the database using [GetActiveProfileUseCase],
 * exposes its state as a [StateFlow], and provides a mechanism to refresh the profile.
 * It also exposes a flag indicating if redemption is allowed for the current profile.
 *
 * @property getActiveProfileUseCase Use case to retrieve the active profile.
 * @property onSuccess Optional callback invoked with the profile and coroutine scope on successful fetch.
 * @property onFailure Optional callback invoked with the error and coroutine scope on failure.
 */
open class GetActiveProfileController(
    private val getActiveProfileUseCase: GetActiveProfileUseCase,
    val onSuccess: ((ProfilesUseCaseData.Profile, CoroutineScope) -> Unit)? = null,
    val onFailure: ((Throwable, CoroutineScope) -> Unit)? = null
) : Controller() {
    /**
     * Holds the current UI state of the active profile.
     */
    private val _activeProfile = MutableStateFlow<UiState<ProfilesUseCaseData.Profile>>(UiState.Loading())

    /**
     * Indicates if redemption is allowed for the current profile.
     */
    private val _isRedemptionAllowed = MutableStateFlow(false)

    /**
     * Event to notify UI about the refresh state of the profile in the database.
     *
     * This event emits `true` when a refresh is in progress and `false` when it is complete.
     * UI can use this to show or hide loading indicators.
     */
    protected val isProfileRefreshingEvent = ComposableEvent<Boolean>()

    /**
     * Public state flow exposing the current UI state of the active profile.
     */
    val activeProfile: StateFlow<UiState<ProfilesUseCaseData.Profile>> = _activeProfile

    /**
     * Public state flow indicating if redemption is allowed for the current profile.
     */
    val isRedemptionAllowed: StateFlow<Boolean> = _isRedemptionAllowed

    init {
        viewModelScope.launch {
            initActiveProfile()
        }
    }

    /**
     * Refreshes the active profile by re-fetching it from the database.
     */
    fun refreshActiveProfile() {
        viewModelScope.launch {
            initActiveProfile()
        }
    }

    /**
     * Initializes or refreshes the active profile state from the database.
     *
     * On success, updates the profile state and redemption flag, and triggers [onSuccess].
     * On failure, updates the error state and triggers [onFailure].
     * Always triggers [isProfileRefreshingEvent] with `false` when done.
     */
    protected suspend fun initActiveProfile() {
        runCatching {
            getActiveProfileUseCase.invoke()
                .distinctUntilChanged { old, new ->
                    old.isSSOTokenValid() == new.isSSOTokenValid() ||
                        old.isActive == new.isActive ||
                        old.lastAuthenticated == new.lastAuthenticated ||
                        old.color == new.color ||
                        old.avatar == new.avatar ||
                        old.image.contentEquals(new.image) ||
                        old.ssoTokenScope?.token == new.ssoTokenScope?.token ||
                        old.ssoTokenScope?.token?.token == new.ssoTokenScope?.token?.token
                }
        }.fold(
            onSuccess = { profileFlow ->
                val profile = profileFlow.first()
                _isRedemptionAllowed.value = profile.isRedemptionAllowed()
                _activeProfile.value = UiState.Data(profile)
                onSuccess?.invoke(profile, viewModelScope)
                // informs the ui that is does not need to show that the profile refresh is in progress
                isProfileRefreshingEvent.trigger(false)
            },
            onFailure = {
                _activeProfile.value = UiState.Error(it)
                onFailure?.invoke(it, viewModelScope)
                // informs the ui that is does not need to show that the profile refresh is in progress
                isProfileRefreshingEvent.trigger(false)
            }
        )
    }
}

/**
 * Remembers and provides an instance of [GetActiveProfileController] for use in Compose.
 *
 * @param onSuccess Optional callback invoked with the profile and coroutine scope on successful fetch.
 * @param onFailure Optional callback invoked with the error and coroutine scope on failure.
 * @return Remembered [GetActiveProfileController] instance.
 */
@Composable
fun rememberGetActiveProfileController(
    onSuccess: ((ProfilesUseCaseData.Profile, CoroutineScope) -> Unit)? = null,
    onFailure: ((Throwable, CoroutineScope) -> Unit)? = null
): GetActiveProfileController {
    val getActiveProfileUseCase by rememberInstance<GetActiveProfileUseCase>()
    return remember {
        GetActiveProfileController(
            getActiveProfileUseCase = getActiveProfileUseCase,
            onSuccess = onSuccess,
            onFailure = onFailure
        )
    }
}
