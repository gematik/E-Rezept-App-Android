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

@file:Suppress("TooManyFunctions")

package de.gematik.ti.erp.app.profiles.presentation

import android.graphics.Bitmap
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import de.gematik.ti.erp.app.base.Controller
import de.gematik.ti.erp.app.profiles.model.ProfilesData
import de.gematik.ti.erp.app.profiles.repository.ProfileIdentifier
import de.gematik.ti.erp.app.profiles.usecase.GetActiveProfileUseCase
import de.gematik.ti.erp.app.profiles.usecase.GetProfilesUseCase
import de.gematik.ti.erp.app.profiles.usecase.SwitchActiveProfileUseCase
import de.gematik.ti.erp.app.profiles.usecase.UpdateProfileUseCase
import de.gematik.ti.erp.app.profiles.usecase.UpdateProfileUseCase.Companion.ProfileModifier
import de.gematik.ti.erp.app.profiles.usecase.model.ProfileInsuranceInformation
import de.gematik.ti.erp.app.profiles.usecase.model.ProfilesUseCaseData
import de.gematik.ti.erp.app.profiles.usecase.model.ProfilesUseCaseData.Profile
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import org.kodein.di.compose.rememberInstance

class ProfileController(
    private val getActiveProfileUseCase: GetActiveProfileUseCase,
    private val getProfilesUseCase: GetProfilesUseCase,
    private val switchActiveProfileUseCase: SwitchActiveProfileUseCase,
    private val updateProfileUseCase: UpdateProfileUseCase
) : Controller() {

    private val _profile: MutableStateFlow<Profile?> = MutableStateFlow(null)
    val profile: StateFlow<Profile?> = _profile

    private val _profiles by lazy {
        getProfilesUseCase().stateIn(controllerScope, SharingStarted.Eagerly, null)
    }

    private val activeProfile by lazy {
        getActiveProfileUseCase().stateIn(controllerScope, SharingStarted.Lazily, DEFAULT_EMPTY_PROFILE)
    }

    // todo: replace getProfilesState with getProfilesState2
    @Composable
    fun getProfilesState2() = _profiles.collectAsStateWithLifecycle()

    @Composable
    fun getActiveProfileState() = activeProfile.collectAsStateWithLifecycle()

    fun switchActiveProfile(id: ProfileIdentifier) {
        controllerScope.launch {
            switchActiveProfileUseCase(id)
        }
    }

    fun updateProfileColor(profile: Profile, color: ProfilesData.ProfileColorNames) {
        controllerScope.launch {
            updateProfileUseCase(modifier = ProfileModifier.Color(color), id = profile.id)
        }
    }

    fun savePersonalizedProfileImage(profileId: ProfileIdentifier, image: Bitmap) {
        controllerScope.launch {
            updateProfileUseCase(modifier = ProfileModifier.Image(image), id = profileId)
        }
    }

    fun updateProfileName(profileId: ProfileIdentifier, name: String) {
        controllerScope.launch {
            updateProfileUseCase(modifier = ProfileModifier.Name(name), id = profileId)
        }
    }

    fun saveAvatarFigure(profileId: ProfileIdentifier, avatar: ProfilesData.Avatar) {
        controllerScope.launch {
            updateProfileUseCase(modifier = ProfileModifier.Avatar(avatar), id = profileId)
        }
    }

    fun clearPersonalizedImage(profileId: ProfileIdentifier) {
        controllerScope.launch {
            updateProfileUseCase(modifier = ProfileModifier.ClearImage, id = profileId)
        }
    }

    companion object {
        val DEFAULT_EMPTY_PROFILE = Profile(
            id = "no-id",
            name = "no-name",
            insurance = ProfileInsuranceInformation(
                insuranceType = ProfilesUseCaseData.InsuranceType.NONE
            ),
            isActive = false,
            color = ProfilesData.ProfileColorNames.SPRING_GRAY,
            lastAuthenticated = null,
            ssoTokenScope = null,
            avatar = ProfilesData.Avatar.PersonalizedImage
        )
    }
}

@Composable
fun rememberProfileController(): ProfileController {
    val getActiveProfileUseCase by rememberInstance<GetActiveProfileUseCase>()
    val getProfilesUseCase by rememberInstance<GetProfilesUseCase>()
    val switchActiveProfileUseCase by rememberInstance<SwitchActiveProfileUseCase>()
    val updateProfileUseCase by rememberInstance<UpdateProfileUseCase>()

    return remember {
        ProfileController(
            getActiveProfileUseCase = getActiveProfileUseCase,
            getProfilesUseCase = getProfilesUseCase,
            switchActiveProfileUseCase = switchActiveProfileUseCase,
            updateProfileUseCase = updateProfileUseCase
        )
    }
}
