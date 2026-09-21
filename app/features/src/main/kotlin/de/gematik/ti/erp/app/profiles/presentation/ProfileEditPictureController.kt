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
import de.gematik.ti.erp.app.profile.model.Avatar
import de.gematik.ti.erp.app.profile.model.ProfileColorNames
import de.gematik.ti.erp.app.profile.model.ProfileErpModel
import de.gematik.ti.erp.app.profile.repository.ProfileIdentifier
import de.gematik.ti.erp.app.profiles.usecase.GetProfileByIdUseCase
import de.gematik.ti.erp.app.profiles.usecase.UpdateProfileColorUseCase
import de.gematik.ti.erp.app.profiles.usecase.UpdateProfileAvatarUseCase
import de.gematik.ti.erp.app.profiles.usecase.ClearPersonalizedProfileImageUseCase
import de.gematik.ti.erp.app.utils.uistate.UiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import org.kodein.di.compose.rememberInstance

class ProfileEditPictureController(
    private val profileId: ProfileIdentifier?,
    private val getProfileByIdUseCase: GetProfileByIdUseCase,
    private val updateProfileColorUseCase: UpdateProfileColorUseCase,
    private val updateProfileAvatarUseCase: UpdateProfileAvatarUseCase,
    private val clearPersonalizedProfileImageUseCase: ClearPersonalizedProfileImageUseCase
) : Controller() {

    private val _profile = MutableStateFlow<UiState<ProfileErpModel>>(UiState.Loading())
    val profile: StateFlow<UiState<ProfileErpModel>> = _profile

    init {
        loadSelectedProfile(profileId)
    }

    private fun loadSelectedProfile(profileId: ProfileIdentifier?) {
        controllerScope.launch {
            run {
                try {
                    profileId?.let {
                        getProfileByIdUseCase(profileId).collect {
                            _profile.value = UiState.Data(it)
                        }
                    } ?: throw IllegalArgumentException("ProfileId is null")
                } catch (e: Exception) {
                    _profile.value = UiState.Error(e)
                }
            }
        }
    }

    fun updateProfileColor(color: ProfileColorNames) {
        controllerScope.launch {
            profile.value.data?.let {
                updateProfileColorUseCase(it.id, color)
            }
        }
    }

    fun updateAvatar(avatar: Avatar) {
        controllerScope.launch {
            profile.value.data?.let {
                updateProfileAvatarUseCase(it.id, avatar)
            }
        }
    }

    fun clearPersonalizedImage() {
        controllerScope.launch {
            profile.value.data?.let {
                clearPersonalizedProfileImageUseCase(it.id)
            }
        }
    }
}

@Composable
fun rememberProfileEditPictureController(profileId: ProfileIdentifier?): ProfileEditPictureController {
    val getProfileByIdUseCase by rememberInstance<GetProfileByIdUseCase>()
    val updateProfileColorUseCase by rememberInstance<UpdateProfileColorUseCase>()
    val updateProfileAvatarUseCase by rememberInstance<UpdateProfileAvatarUseCase>()
    val clearPersonalizedProfileImageUseCase by rememberInstance<ClearPersonalizedProfileImageUseCase>()

    return remember(profileId) {
        ProfileEditPictureController(
            profileId = profileId,
            getProfileByIdUseCase = getProfileByIdUseCase,
            updateProfileColorUseCase = updateProfileColorUseCase,
            updateProfileAvatarUseCase = updateProfileAvatarUseCase,
            clearPersonalizedProfileImageUseCase = clearPersonalizedProfileImageUseCase
        )
    }
}
