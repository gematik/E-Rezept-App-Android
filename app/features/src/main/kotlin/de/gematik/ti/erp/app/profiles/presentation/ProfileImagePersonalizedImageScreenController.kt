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
import android.os.Build
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import de.gematik.ti.erp.app.base.Controller
import de.gematik.ti.erp.app.profiles.model.ProfilesData
import de.gematik.ti.erp.app.profiles.model.ProfilesData.Avatar.PersonalizedImage
import de.gematik.ti.erp.app.profiles.repository.ProfileIdentifier
import de.gematik.ti.erp.app.profiles.usecase.GetProfilesUseCase
import de.gematik.ti.erp.app.profiles.usecase.UpdateProfileUseCase
import de.gematik.ti.erp.app.profiles.usecase.UpdateProfileUseCase.Companion.ProfileModifier.Avatar
import de.gematik.ti.erp.app.profiles.usecase.UpdateProfileUseCase.Companion.ProfileModifier.Color
import de.gematik.ti.erp.app.profiles.usecase.UpdateProfileUseCase.Companion.ProfileModifier.Image
import de.gematik.ti.erp.app.profiles.usecase.model.ProfilesUseCaseData
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import org.kodein.di.compose.rememberInstance

/**
 * Controller for [ProfileImageCameraScreen] and [ProfileImageEmojiScreen]
 * If those screens become bigger, then we can split this
 */
class ProfileImagePersonalizedImageScreenController(
    private val profileId: ProfileIdentifier,
    private val updateProfileUseCase: UpdateProfileUseCase,
    private val getProfilesUseCase: GetProfilesUseCase
) : Controller() {

    private val _profile: MutableStateFlow<ProfilesUseCaseData.Profile?> = MutableStateFlow(null)
    val profile: StateFlow<ProfilesUseCaseData.Profile?> = _profile

    init {
        controllerScope.launch {
            getProfilesUseCase().firstOrNull()?.let {
                _profile.value = it.firstOrNull { profile -> profile.id == profileId }
            }
        }
    }

    fun updateProfileImageBitmap(bitmap: Bitmap) {
        controllerScope.launch {
            updateProfileUseCase(modifier = Image(bitmap), id = profileId)
            updateProfileUseCase(modifier = Avatar(value = PersonalizedImage), id = profileId)
        }
    }

    fun updateProfileColor(color: ProfilesData.ProfileColorNames) {
        controllerScope.launch {
            _profile.value = _profile.value?.copy(color = color)
            updateProfileUseCase(modifier = Color(color), id = profileId)
        }
    }

    // need this as memojis are not supported on Samsung devices
    fun isSamsungDevice(): Boolean {
        return Build.MANUFACTURER.equals("Samsung", ignoreCase = true)
    }
}

@Composable
fun rememberProfileImagePersonalizedImageScreenController(
    profileId: ProfileIdentifier
): ProfileImagePersonalizedImageScreenController {
    val updateProfileUseCase by rememberInstance<UpdateProfileUseCase>()
    val getProfilesUseCase by rememberInstance<GetProfilesUseCase>()
    return remember {
        ProfileImagePersonalizedImageScreenController(
            profileId = profileId,
            updateProfileUseCase = updateProfileUseCase,
            getProfilesUseCase = getProfilesUseCase
        )
    }
}
