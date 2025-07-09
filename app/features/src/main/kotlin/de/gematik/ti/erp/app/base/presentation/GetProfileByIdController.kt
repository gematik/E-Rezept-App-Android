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

package de.gematik.ti.erp.app.base.presentation

import de.gematik.ti.erp.app.profiles.model.ProfileCombinedData
import de.gematik.ti.erp.app.profiles.repository.ProfileIdentifier
import de.gematik.ti.erp.app.profiles.usecase.GetActiveProfileUseCase
import de.gematik.ti.erp.app.profiles.usecase.GetProfileByIdUseCase
import de.gematik.ti.erp.app.profiles.usecase.GetProfilesUseCase
import de.gematik.ti.erp.app.profiles.usecase.model.ProfilesUseCaseData
import de.gematik.ti.erp.app.utils.letNotNullOnCondition
import de.gematik.ti.erp.app.utils.uistate.UiState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

abstract class GetProfileByIdController(
    private val selectedProfileId: ProfileIdentifier? = null,
    private val getProfileByIdUseCase: GetProfileByIdUseCase,
    private val getProfilesUseCase: GetProfilesUseCase,
    getActiveProfileUseCase: GetActiveProfileUseCase,
    protected open val onSelectedProfileSuccess: ((ProfilesUseCaseData.Profile, CoroutineScope) -> Unit)? = null,
    protected open val onSelectedProfileFailure: ((Throwable, CoroutineScope) -> Unit)? = null,
    protected open val onActiveProfileSuccess: ((ProfilesUseCaseData.Profile, CoroutineScope) -> Unit)? = null,
    protected open val onActiveProfileFailure: ((Throwable, CoroutineScope) -> Unit)? = null
) : GetActiveProfileController(
    getActiveProfileUseCase = getActiveProfileUseCase,
    onSuccess = onActiveProfileSuccess,
    onFailure = onActiveProfileFailure
) {
    private val _combinedProfile = MutableStateFlow<UiState<ProfileCombinedData>>(UiState.Loading())
    val combinedProfile: StateFlow<UiState<ProfileCombinedData>> = _combinedProfile

    init {
        controllerScope.launch {
            initCombinedProfile()
        }
    }

    fun refreshCombinedProfile() {
        controllerScope.launch {
            initCombinedProfile()
        }
    }

    val isSsoTokenValidForSelectedProfile: StateFlow<Boolean> by lazy {
        combinedProfile.map { it.data?.selectedProfile?.ssoTokenScope }
            .distinctUntilChanged()
            .map { it?.token?.isValid() ?: false }
            .stateIn(
                scope = controllerScope,
                started = SharingStarted.Eagerly,
                initialValue = false
            )
    }

    private suspend fun initCombinedProfile() {
        runCatching {
            _combinedProfile.value = UiState.Loading()

            val profiles = getProfilesUseCase().first()
            val selectedProfile = selectedProfileId?.let { getProfileByIdUseCase(selectedProfileId).first() }
            selectedProfile to profiles
        }.fold(
            onSuccess = { (selectedProfile, profiles) ->
                selectedProfile?.let { onSelectedProfileSuccess?.invoke(it, controllerScope) }
                letNotNullOnCondition(
                    first = selectedProfile,
                    condition = { profiles.isEmpty().not() }
                ) { profile ->
                    _combinedProfile.value = UiState.Data(
                        ProfileCombinedData(
                            selectedProfile = profile,
                            profiles = profiles
                        )
                    )
                } ?: run {
                    _combinedProfile.value = UiState.Data(
                        ProfileCombinedData(
                            selectedProfile = null,
                            profiles = profiles
                        )
                    )
                }
            },
            onFailure = {
                onSelectedProfileFailure?.invoke(it, controllerScope)
                _combinedProfile.value = UiState.Error(it)
            }
        )
    }
}
