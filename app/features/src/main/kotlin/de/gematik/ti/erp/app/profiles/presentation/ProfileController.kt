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
import de.gematik.ti.erp.app.profile.model.InsuranceType
import de.gematik.ti.erp.app.profile.model.ProfileColorNames
import de.gematik.ti.erp.app.profile.model.ProfileErpModel
import de.gematik.ti.erp.app.profile.model.ProfileImageDataErpModel
import de.gematik.ti.erp.app.profile.model.ProfileInsuranceDataErpModel
import de.gematik.ti.erp.app.profile.repository.ProfileIdentifier
import de.gematik.ti.erp.app.profiles.usecase.GetActiveProfileUseCase
import de.gematik.ti.erp.app.profiles.usecase.GetProfilesUseCase
import de.gematik.ti.erp.app.profiles.usecase.SwitchActiveProfileUseCase
import de.gematik.ti.erp.app.profiles.usecase.UpdateProfileNameUseCase
import de.gematik.ti.erp.app.profiles.usecase.UpdateProfileColorUseCase
import de.gematik.ti.erp.app.profiles.usecase.UpdateProfileAvatarUseCase
import de.gematik.ti.erp.app.profiles.usecase.SavePersonalizedProfileImageUseCase
import de.gematik.ti.erp.app.profiles.usecase.ClearPersonalizedProfileImageUseCase
import de.gematik.ti.erp.app.userauthentication.model.UserAuthenticationErpModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import org.kodein.di.compose.rememberInstance
import de.gematik.ti.erp.app.profile.model.Avatar as ErpAvatar

class ProfileController(
    private val getActiveProfileUseCase: GetActiveProfileUseCase,
    private val getProfilesUseCase: GetProfilesUseCase,
    private val switchActiveProfileUseCase: SwitchActiveProfileUseCase,
    private val updateProfileNameUseCase: UpdateProfileNameUseCase,
    private val updateProfileColorUseCase: UpdateProfileColorUseCase,
    private val updateProfileAvatarUseCase: UpdateProfileAvatarUseCase,
    private val savePersonalizedProfileImageUseCase: SavePersonalizedProfileImageUseCase,
    private val clearPersonalizedProfileImageUseCase: ClearPersonalizedProfileImageUseCase
) : Controller() {

    private val _profile: MutableStateFlow<ProfileErpModel?> = MutableStateFlow(null)
    val profile: StateFlow<ProfileErpModel?> = _profile

    private val _profiles by lazy {
        getProfilesUseCase().stateIn(controllerScope, SharingStarted.Eagerly, null)
    }

    private val activeProfile by lazy {
        getActiveProfileUseCase().stateIn(controllerScope, SharingStarted.Lazily, DEFAULT_EMPTY_PROFILE)
    }

    @Composable
    fun getProfilesState() = _profiles.collectAsStateWithLifecycle()

    @Composable
    fun getActiveProfileState() = activeProfile.collectAsStateWithLifecycle()

    fun switchActiveProfile(id: ProfileIdentifier) {
        controllerScope.launch {
            switchActiveProfileUseCase(id)
        }
    }

    fun updateProfileColor(profile: ProfileErpModel, color: ProfileColorNames) {
        controllerScope.launch {
            updateProfileColorUseCase(profile.id, color)
        }
    }

    fun savePersonalizedProfileImage(profileId: ProfileIdentifier, image: Bitmap) {
        controllerScope.launch {
            savePersonalizedProfileImageUseCase(profileId, image)
        }
    }

    fun updateProfileName(profileId: ProfileIdentifier, name: String) {
        controllerScope.launch {
            updateProfileNameUseCase(profileId, name)
        }
    }

    fun saveAvatarFigure(profileId: ProfileIdentifier, avatar: ErpAvatar) {
        controllerScope.launch {
            updateProfileAvatarUseCase(profileId, avatar)
        }
    }

    fun clearPersonalizedImage(profileId: ProfileIdentifier) {
        controllerScope.launch {
            clearPersonalizedProfileImageUseCase(profileId)
        }
    }

    companion object {
        val DEFAULT_EMPTY_PROFILE = ProfileErpModel(
            id = "no-id",
            name = "no-name",
            active = false,
            profileImageData = ProfileImageDataErpModel(
                color = ProfileColorNames.SPRING_GRAY,
                avatar = ErpAvatar.PersonalizedImage,
                image = null
            ),
            insuranceData = ProfileInsuranceDataErpModel(
                insuranceType = InsuranceType.NONE,
                insurantName = null,
                insuranceIdentifier = null,
                organizationIdentifier = null,
                insuranceName = null
            ),
            lastAuthenticated = null,
            userAuthentication = UserAuthenticationErpModel.NotInitialized,
            isNewlyCreated = false,
            isConsentDrawerShown = true,
            lastAuditEventSynced = null,
            lastTaskSynced = null
        )
    }
}

@Composable
fun rememberProfileController(): ProfileController {
    val getActiveProfileUseCase by rememberInstance<GetActiveProfileUseCase>()
    val getProfilesUseCase by rememberInstance<GetProfilesUseCase>()
    val switchActiveProfileUseCase by rememberInstance<SwitchActiveProfileUseCase>()
    val updateProfileNameUseCase by rememberInstance<UpdateProfileNameUseCase>()
    val updateProfileColorUseCase by rememberInstance<UpdateProfileColorUseCase>()
    val updateProfileAvatarUseCase by rememberInstance<UpdateProfileAvatarUseCase>()
    val savePersonalizedProfileImageUseCase by rememberInstance<SavePersonalizedProfileImageUseCase>()
    val clearPersonalizedProfileImageUseCase by rememberInstance<ClearPersonalizedProfileImageUseCase>()

    return remember {
        ProfileController(
            getActiveProfileUseCase = getActiveProfileUseCase,
            getProfilesUseCase = getProfilesUseCase,
            switchActiveProfileUseCase = switchActiveProfileUseCase,
            updateProfileNameUseCase = updateProfileNameUseCase,
            updateProfileColorUseCase = updateProfileColorUseCase,
            updateProfileAvatarUseCase = updateProfileAvatarUseCase,
            savePersonalizedProfileImageUseCase = savePersonalizedProfileImageUseCase,
            clearPersonalizedProfileImageUseCase = clearPersonalizedProfileImageUseCase
        )
    }
}
