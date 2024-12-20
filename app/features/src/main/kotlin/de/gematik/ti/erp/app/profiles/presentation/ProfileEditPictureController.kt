/*
 * Copyright 2024, gematik GmbH
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

package de.gematik.ti.erp.app.profiles.presentation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import de.gematik.ti.erp.app.base.Controller
import de.gematik.ti.erp.app.profiles.model.ProfilesData
import de.gematik.ti.erp.app.profiles.repository.ProfileIdentifier
import de.gematik.ti.erp.app.profiles.usecase.GetSelectedProfileUseCase
import de.gematik.ti.erp.app.profiles.usecase.UpdateProfileUseCase
import de.gematik.ti.erp.app.profiles.usecase.UpdateProfileUseCase.Companion.ProfileModifier
import de.gematik.ti.erp.app.profiles.usecase.model.ProfilesUseCaseData
import de.gematik.ti.erp.app.utils.uistate.UiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import org.kodein.di.compose.rememberInstance

class ProfileEditPictureController(
    private val profileId: ProfileIdentifier?,
    private val getSelectedProfileUseCase: GetSelectedProfileUseCase,
    private val updateProfileUseCase: UpdateProfileUseCase
) : Controller() {

    private val _profile = MutableStateFlow<UiState<ProfilesUseCaseData.Profile>>(UiState.Loading())
    val profile: StateFlow<UiState<ProfilesUseCaseData.Profile>> = _profile

    init {
        loadSelectedProfile(profileId)
    }

    private fun loadSelectedProfile(profileId: ProfileIdentifier?) {
        controllerScope.launch {
            runCatching {
                profileId?.let {
                    getSelectedProfileUseCase(profileId).first()
                } ?: run {
                    throw IllegalArgumentException("ProfileId is null")
                }
            }.fold(
                onSuccess = {
                    _profile.value = UiState.Data(it)
                },
                onFailure = {
                    UiState.Error<Throwable>(it)
                }
            )
        }
    }

    fun updateProfileColor(color: ProfilesData.ProfileColorNames) {
        controllerScope.launch {
            profile.value.data?.let {
                updateProfileUseCase(
                    modifier = ProfileModifier.Color(color),
                    id = it.id
                )
            }
        }
    }

    fun updateAvatar(avatar: ProfilesData.Avatar) {
        controllerScope.launch {
            profile.value.data?.let {
                updateProfileUseCase(
                    modifier = ProfileModifier.Avatar(avatar),
                    id = it.id
                )
            }
        }
    }

    fun clearPersonalizedImage() {
        controllerScope.launch {
            profile.value.data?.let {
                updateProfileUseCase(
                    modifier = ProfileModifier.ClearImage,
                    id = it.id
                )
            }
        }
    }
}

@Composable
fun rememberProfileEditPictureController(profileId: ProfileIdentifier?): ProfileEditPictureController {
    val getSelectedProfileUseCase by rememberInstance<GetSelectedProfileUseCase>()
    val updateProfileUseCase by rememberInstance<UpdateProfileUseCase>()

    return remember(profileId) {
        ProfileEditPictureController(
            profileId = profileId,
            getSelectedProfileUseCase = getSelectedProfileUseCase,
            updateProfileUseCase = updateProfileUseCase
        )
    }
}
