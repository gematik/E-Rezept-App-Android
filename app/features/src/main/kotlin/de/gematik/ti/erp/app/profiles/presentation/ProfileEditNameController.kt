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
import de.gematik.ti.erp.app.base.Controller
import de.gematik.ti.erp.app.profile.repository.ProfileIdentifier
import de.gematik.ti.erp.app.profiles.model.ProfileCombinedData
import de.gematik.ti.erp.app.profiles.usecase.AddProfileUseCase
import de.gematik.ti.erp.app.profiles.usecase.GetProfilesUseCase
import de.gematik.ti.erp.app.profiles.usecase.GetSelectedProfileUseCase
import de.gematik.ti.erp.app.profiles.usecase.UpdateProfileUseCase
import de.gematik.ti.erp.app.utils.uistate.UiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import org.kodein.di.compose.rememberInstance

class ProfileEditNameController(
    private val profileId: ProfileIdentifier?,
    private val getSelectedProfileUseCase: GetSelectedProfileUseCase,
    private val getProfilesUseCase: GetProfilesUseCase,
    private val updateProfileUseCase: UpdateProfileUseCase,
    private val addProfileUseCase: AddProfileUseCase
) : Controller() {

    private val _combinedProfile = MutableStateFlow<UiState<ProfileCombinedData>>(UiState.Loading())
    val combinedProfile: StateFlow<UiState<ProfileCombinedData>> = _combinedProfile

    init {
        controllerScope.launch { load() }
    }

    private suspend fun load() {
        runCatching {
            val profiles = getProfilesUseCase().firstOrNull()
            val selectedProfile = profileId?.let {
                getSelectedProfileUseCase(profileId).firstOrNull()
            }
            selectedProfile to profiles
        }.fold(
            onSuccess = { (selectedProfile, profiles) ->
                _combinedProfile.value = UiState.Data(
                    ProfileCombinedData(
                        selectedProfile = selectedProfile,
                        profiles = profiles ?: emptyList()
                    )
                )
            },
            onFailure = {
                _combinedProfile.value = UiState.Error(it)
            }
        )
    }

    fun updateProfileName(name: String) {
        controllerScope.launch {
            _combinedProfile.value.data?.selectedProfile?.let {
                updateProfileUseCase(
                    modifier = UpdateProfileUseCase.Companion.ProfileModifier.Name(name),
                    id = it.id
                )
            }
        }
    }

    fun addNewProfile(name: String) {
        controllerScope.launch {
            addProfileUseCase(name)
        }
    }
}

@Composable
fun rememberProfileEditNameController(profileId: ProfileIdentifier?): ProfileEditNameController {
    val getSelectedProfileUseCase by rememberInstance<GetSelectedProfileUseCase>()
    val updateProfileUseCase by rememberInstance<UpdateProfileUseCase>()
    val getProfilesUseCase by rememberInstance<GetProfilesUseCase>()
    val addProfileUseCase by rememberInstance<AddProfileUseCase>()

    return remember(profileId) {
        ProfileEditNameController(
            profileId = profileId,
            getSelectedProfileUseCase = getSelectedProfileUseCase,
            getProfilesUseCase = getProfilesUseCase,
            updateProfileUseCase = updateProfileUseCase,
            addProfileUseCase = addProfileUseCase
        )
    }
}
