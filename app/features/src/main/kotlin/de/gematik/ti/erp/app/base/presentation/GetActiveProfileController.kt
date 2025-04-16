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

package de.gematik.ti.erp.app.base.presentation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
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

open class GetActiveProfileController(
    private val getActiveProfileUseCase: GetActiveProfileUseCase,
    val onSuccess: ((ProfilesUseCaseData.Profile, CoroutineScope) -> Unit)? = null,
    val onFailure: ((Throwable, CoroutineScope) -> Unit)? = null
) : Controller() {
    private val _activeProfile = MutableStateFlow<UiState<ProfilesUseCaseData.Profile>>(UiState.Loading())
    private val _isRedemptionAllowed = MutableStateFlow(false)

    protected val onRefreshProfileAction = ComposableEvent<Boolean>()

    val activeProfile: StateFlow<UiState<ProfilesUseCaseData.Profile>> = _activeProfile
    val isRedemptionAllowed: StateFlow<Boolean> = _isRedemptionAllowed

    init {
        initActiveProfile()
    }

    fun refreshActiveProfile() {
        initActiveProfile()
    }

    private fun initActiveProfile() {
        controllerScope.launch {
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
                    onSuccess?.invoke(profile, controllerScope)
                    onRefreshProfileAction.trigger(false)
                },
                onFailure = {
                    _activeProfile.value = UiState.Error(it)
                    onFailure?.invoke(it, controllerScope)
                    onRefreshProfileAction.trigger(false)
                }
            )
            onRefreshProfileAction.trigger(false)
        }
    }
}

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
