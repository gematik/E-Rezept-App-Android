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

package de.gematik.ti.erp.app.debugsettings.pkv.presentation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import de.gematik.ti.erp.app.base.presentation.GetActiveProfileController
import de.gematik.ti.erp.app.profiles.usecase.GetActiveProfileUseCase
import de.gematik.ti.erp.app.profiles.usecase.IsProfilePKVUseCase
import de.gematik.ti.erp.app.profiles.usecase.SwitchProfileToGKVUseCase
import de.gematik.ti.erp.app.profiles.usecase.SwitchProfileToPKVUseCase
import de.gematik.ti.erp.app.profiles.usecase.model.ProfilesUseCaseData
import de.gematik.ti.erp.app.utils.uistate.UiState
import de.gematik.ti.erp.app.utils.uistate.UiState.Companion.Data
import de.gematik.ti.erp.app.utils.uistate.UiState.Companion.Loading
import io.github.aakira.napier.Napier
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import org.kodein.di.compose.rememberInstance

@Suppress("ConstructorParameterNaming")
class DebugPkvController(
    private val getActiveProfileUseCase: GetActiveProfileUseCase,
    private val switchProfileToPKVUseCase: SwitchProfileToPKVUseCase,
    private val switchProfileToGKVUseCase: SwitchProfileToGKVUseCase,
    private val isProfilePKVUseCase: IsProfilePKVUseCase,
    private val _activeProfile: MutableStateFlow<UiState<ProfilesUseCaseData.Profile>> = MutableStateFlow(Loading())
) : GetActiveProfileController(
    getActiveProfileUseCase = getActiveProfileUseCase,
    onSuccess = { activeProfile, scope ->
        scope.launch {
            _activeProfile.value = Data(activeProfile)
        }
    },
    onFailure = { error, scope ->
        scope.launch {
            Napier.e { "error on dedug pkv, ${error.message}" }
        }
    }
) {
    private val _isProfilePkv: MutableStateFlow<UiState<Boolean>> = MutableStateFlow(Loading())
    val isProfilePKV: StateFlow<UiState<Boolean>> = _isProfilePkv

    fun checkIsProfilePkv() {
        controllerScope.launch {
            activeProfile.collectLatest {
                it.data?.let { profile ->
                    runCatching {
                        isProfilePKVUseCase.invoke(profile.id)
                    }.fold(
                        onSuccess = { result ->
                            _isProfilePkv.value = Data(result)
                        },
                        onFailure = {
                            Napier.e { "error on checking pkv, ${it.message}" }
                            _isProfilePkv.value = Data(false)
                        }
                    )
                }
            }
        }
    }

    fun switchToPkv() {
        controllerScope.launch {
            activeProfile.value.data?.let { profile ->
                runCatching {
                    switchProfileToPKVUseCase.invoke(profile.id)
                }.onSuccess { result ->
                    _isProfilePkv.value = Data(result)
                }.onFailure {
                    Napier.e { "error on changing to pkv, ${it.message}" }
                    _isProfilePkv.value = Data(false)
                }
            }
        }
    }

    fun switchToGkv() {
        controllerScope.launch {
            activeProfile.value.data?.let { profile ->
                runCatching {
                    switchProfileToGKVUseCase.invoke(profile.id)
                }.onSuccess { result ->
                    if (result) {
                        _isProfilePkv.value = Data(false)
                    } else {
                        _isProfilePkv.value = Data(true)
                    }
                }.onFailure {
                    Napier.e { "error on changing to pkv, ${it.message}" }
                    _isProfilePkv.value = Data(false)
                }
            }
        }
    }
}

@Composable
fun rememberDebugPkvController(): DebugPkvController {
    val getActiveProfileUseCase by rememberInstance<GetActiveProfileUseCase>()
    val switchProfileToPKVUseCase by rememberInstance<SwitchProfileToPKVUseCase>()
    val switchProfileToGKVUseCase by rememberInstance<SwitchProfileToGKVUseCase>()
    val isProfilePKVUseCase by rememberInstance<IsProfilePKVUseCase>()

    return remember {
        DebugPkvController(
            getActiveProfileUseCase = getActiveProfileUseCase,
            switchProfileToPKVUseCase = switchProfileToPKVUseCase,
            switchProfileToGKVUseCase = switchProfileToGKVUseCase,
            isProfilePKVUseCase = isProfilePKVUseCase
        )
    }
}
