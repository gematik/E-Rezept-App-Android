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

import android.graphics.Bitmap
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import de.gematik.ti.erp.app.base.Controller
import de.gematik.ti.erp.app.profile.model.Avatar
import de.gematik.ti.erp.app.profile.model.ProfileColorNames
import de.gematik.ti.erp.app.profile.model.ProfileErpModel
import de.gematik.ti.erp.app.profile.repository.ProfileIdentifier
import de.gematik.ti.erp.app.profiles.usecase.GetProfileByIdUseCase
import de.gematik.ti.erp.app.profiles.usecase.UpdateProfileAvatarUseCase
import de.gematik.ti.erp.app.profiles.usecase.UpdateProfileColorUseCase
import de.gematik.ti.erp.app.profiles.usecase.SavePersonalizedProfileImageUseCase
import de.gematik.ti.erp.app.profiles.usecase.ClearPersonalizedProfileImageUseCase
import de.gematik.ti.erp.app.utils.uistate.UiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.kodein.di.compose.rememberInstance

class ProfileImagePersonalizedImageScreenController(
    private val getProfileByIdUseCase: GetProfileByIdUseCase,
    private val updateProfileAvatarUseCase: UpdateProfileAvatarUseCase,
    private val updateProfileColorUseCase: UpdateProfileColorUseCase,
    private val savePersonalizedProfileImageUseCase: SavePersonalizedProfileImageUseCase,
    private val clearPersonalizedProfileImageUseCase: ClearPersonalizedProfileImageUseCase,
    private val profileId: ProfileIdentifier
) : Controller() {
    private val _profile: MutableStateFlow<UiState<ProfileErpModel>> = MutableStateFlow(UiState.Loading())
    val profile: StateFlow<UiState<ProfileErpModel>> = _profile

    init {
        _profile.update { UiState.Loading() }
        controllerScope.launch {
            try {
                getProfileByIdUseCase.invoke(profileId).collect { profile ->
                    _profile.update { UiState.Data(profile) }
                }
            } catch (_: Exception) {
                _profile.update { UiState.Error(error = IllegalArgumentException("ProfileId is null")) }
            }
        }
    }

    fun onSelectAvatar(avatar: Avatar) {
        controllerScope.launch {
            updateProfileAvatarUseCase(profileId, avatar)
        }
    }

    fun savePersonalizedProfileImage(image: Bitmap) {
        controllerScope.launch {
            savePersonalizedProfileImageUseCase(profileId, image)
            updateProfileAvatarUseCase(profileId, Avatar.PersonalizedImage)
        }
    }

    fun clearPersonalizedImage() {
        controllerScope.launch {
            clearPersonalizedProfileImageUseCase(profileId)
        }
    }

    fun updateAvatar(avatar: Avatar) {
        onSelectAvatar(avatar)
    }

    fun updateProfileColor(color: ProfileColorNames) {
        controllerScope.launch {
            updateProfileColorUseCase(profileId, color)
        }
    }

    fun updateProfileImageBitmap(bitmap: Bitmap) {
        savePersonalizedProfileImage(bitmap)
    }

    fun isSamsungDevice(): Boolean {
        return android.os.Build.MANUFACTURER.equals("Samsung", ignoreCase = true)
    }
}

@Composable
fun rememberProfileImagePersonalizedImageScreenController(
    profileId: ProfileIdentifier
): ProfileImagePersonalizedImageScreenController {
    val getProfileByIdUseCase by rememberInstance<GetProfileByIdUseCase>()
    val updateProfileAvatarUseCase by rememberInstance<UpdateProfileAvatarUseCase>()
    val updateProfileColorUseCase by rememberInstance<UpdateProfileColorUseCase>()
    val savePersonalizedProfileImageUseCase by rememberInstance<SavePersonalizedProfileImageUseCase>()
    val clearPersonalizedProfileImageUseCase by rememberInstance<ClearPersonalizedProfileImageUseCase>()
    return remember {
        ProfileImagePersonalizedImageScreenController(
            getProfileByIdUseCase = getProfileByIdUseCase,
            updateProfileAvatarUseCase = updateProfileAvatarUseCase,
            updateProfileColorUseCase = updateProfileColorUseCase,
            savePersonalizedProfileImageUseCase = savePersonalizedProfileImageUseCase,
            clearPersonalizedProfileImageUseCase = clearPersonalizedProfileImageUseCase,
            profileId = profileId
        )
    }
}
