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

import de.gematik.ti.erp.app.profile.repository.ProfileIdentifier
import de.gematik.ti.erp.app.profiles.model.ProfileCombinedData
import de.gematik.ti.erp.app.profiles.usecase.GetActiveProfileUseCase
import de.gematik.ti.erp.app.profiles.usecase.GetProfileByIdUseCase
import de.gematik.ti.erp.app.profiles.usecase.GetProfilesUseCase
import de.gematik.ti.erp.app.profiles.usecase.model.ProfilesUseCaseData
import de.gematik.ti.erp.app.utils.uistate.UiState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEmpty
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
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
    private val _combinedProfile =
        MutableStateFlow<UiState<ProfileCombinedData>>(UiState.Companion.Loading())
    val combinedProfile: StateFlow<UiState<ProfileCombinedData>> = _combinedProfile

    init {
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
                started = SharingStarted.Companion.Eagerly,
                initialValue = false
            )
    }

    private suspend fun initCombinedProfile() {
        try {
            _combinedProfile.update { UiState.Loading() }
            selectedProfileId?.let {
                combine(
                    getProfilesUseCase.invoke(),
                    getProfileByIdUseCase(selectedProfileId)
                ) { profileList, selectedProfile ->
                    onSelectedProfileSuccess?.invoke(selectedProfile, controllerScope)
                    ProfileCombinedData(
                        selectedProfile = selectedProfile,
                        profiles = profileList
                    )
                }.onEmpty { throw IllegalStateException("profileList- or selectedProfileFlow is empty") }
                    .collect { combinedData ->
                        _combinedProfile.update {
                            UiState.Data(
                                combinedData
                            )
                        }
                    }
            }
                ?: getProfilesUseCase.invoke()
                    .onEmpty { throw IllegalStateException("profileListFlow is empty") }
                    .collect {
                            profileList ->
                        _combinedProfile.update {
                            UiState.Data(
                                ProfileCombinedData(
                                    selectedProfile = null,
                                    profiles = profileList
                                )
                            )
                        }
                    }
        } catch (e: Exception) {
            onSelectedProfileFailure?.invoke(e, controllerScope)
            _combinedProfile.update { UiState.Error(e) }
        }
    }
}
