/*
 * Copyright (c) 2024 gematik GmbH
 * 
 * Licensed under the EUPL, Version 1.2 or – as soon they will be approved by
 * the European Commission - subsequent versions of the EUPL (the Licence);
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at:
 * 
 *     https://joinup.ec.europa.eu/software/page/eupl
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the Licence is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Licence for the specific language governing permissions and
 * limitations under the Licence.
 * 
 */
@file:Suppress("TooManyFunctions")

package de.gematik.ti.erp.app.profiles.presentation

import android.graphics.Bitmap
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import de.gematik.ti.erp.app.Requirement
import de.gematik.ti.erp.app.profiles.model.ProfilesData
import de.gematik.ti.erp.app.profiles.repository.ProfileIdentifier
import de.gematik.ti.erp.app.profiles.usecase.AddProfileUseCase
import de.gematik.ti.erp.app.profiles.usecase.DecryptAccessTokenUseCase
import de.gematik.ti.erp.app.profiles.usecase.DeleteProfileUseCase
import de.gematik.ti.erp.app.profiles.usecase.GetActiveProfileUseCase
import de.gematik.ti.erp.app.profiles.usecase.GetProfilesUseCase
import de.gematik.ti.erp.app.profiles.usecase.LogoutProfileUseCase
import de.gematik.ti.erp.app.profiles.usecase.ProfilesWithPairedDevicesUseCase
import de.gematik.ti.erp.app.profiles.usecase.ResetProfileUseCase
import de.gematik.ti.erp.app.profiles.usecase.SwitchActiveProfileUseCase
import de.gematik.ti.erp.app.profiles.usecase.SwitchProfileToPKVUseCase
import de.gematik.ti.erp.app.profiles.usecase.UpdateProfileUseCase
import de.gematik.ti.erp.app.profiles.usecase.UpdateProfileUseCase.Companion.ProfileModifier
import de.gematik.ti.erp.app.profiles.usecase.model.PairedDevice
import de.gematik.ti.erp.app.profiles.usecase.model.ProfileInsuranceInformation
import de.gematik.ti.erp.app.profiles.usecase.model.ProfilesUseCaseData
import de.gematik.ti.erp.app.profiles.usecase.model.ProfilesUseCaseData.Profile
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import org.kodein.di.compose.rememberInstance

class ProfileController(
    private val addProfileUseCase: AddProfileUseCase,
    private val decryptAccessTokenUseCase: DecryptAccessTokenUseCase,
    private val deleteProfileUseCase: DeleteProfileUseCase,
    private val getActiveProfileUseCase: GetActiveProfileUseCase,
    private val getProfilesUseCase: GetProfilesUseCase,
    private val resetProfileUseCase: ResetProfileUseCase,
    private val switchActiveProfileUseCase: SwitchActiveProfileUseCase,
    private val updateProfileUseCase: UpdateProfileUseCase,
    private val logoutProfileUseCase: LogoutProfileUseCase,
    private val switchProfileToPKVUseCase: SwitchProfileToPKVUseCase,
    private val profilesWithPairedDevicesUseCase: ProfilesWithPairedDevicesUseCase,
    private val scope: CoroutineScope
) {

    private val profiles by lazy {
        getProfilesUseCase().stateIn(scope, SharingStarted.Lazily, listOf(DEFAULT_EMPTY_PROFILE))
    }

    private val activeProfile by lazy {
        getActiveProfileUseCase().stateIn(scope, SharingStarted.Lazily, DEFAULT_EMPTY_PROFILE)
    }

    @Composable
    fun decryptedAccessToken(profile: Profile) =
        decryptAccessTokenUseCase(profile.id).collectAsStateWithLifecycle(null)

    fun pairedDevices(profileId: ProfileIdentifier) =
        profilesWithPairedDevicesUseCase.pairedDevices(profileId)

    suspend fun deletePairedDevice(profileId: ProfileIdentifier, device: PairedDevice) =
        profilesWithPairedDevicesUseCase.deletePairedDevices(profileId, device)

    @Requirement(
        "O.Tokn_6#2",
        sourceSpecification = "BSI-eRp-ePA",
        rationale = "invalidate config and token "
    )
    fun logout(profile: Profile) {
        scope.launch {
            logoutProfileUseCase(profile.id)
        }
    }

    fun addProfile(profileName: String) {
        scope.launch {
            addProfileUseCase(profileName)
        }
    }

    fun removeProfile(profile: Profile, profileName: String?) {
        scope.launch {
            when (profileName != null) {
                true -> resetProfileUseCase(profile, profileName)
                false -> deleteProfileUseCase(profile)
            }
        }
    }

    @Composable
    fun getProfilesState() = profiles.collectAsStateWithLifecycle()

    @Composable
    fun getActiveProfileState() = activeProfile.collectAsStateWithLifecycle()

    fun switchActiveProfile(id: ProfileIdentifier) {
        scope.launch {
            switchActiveProfileUseCase(id)
        }
    }

    fun switchToPrivateInsurance(profileId: ProfileIdentifier) {
        scope.launch {
            switchProfileToPKVUseCase(profileId)
        }
    }

    fun updateProfileColor(profile: Profile, color: ProfilesData.ProfileColorNames) {
        scope.launch {
            updateProfileUseCase(modifier = ProfileModifier.Color(color), id = profile.id)
        }
    }

    fun savePersonalizedProfileImage(profileId: ProfileIdentifier, image: Bitmap) {
        scope.launch {
            updateProfileUseCase(modifier = ProfileModifier.Image(image), id = profileId)
        }
    }

    fun updateProfileName(profileId: ProfileIdentifier, name: String) {
        scope.launch {
            updateProfileUseCase(modifier = ProfileModifier.Name(name), id = profileId)
        }
    }

    fun saveAvatarFigure(profileId: ProfileIdentifier, avatar: ProfilesData.Avatar) {
        scope.launch {
            updateProfileUseCase(modifier = ProfileModifier.Avatar(avatar), id = profileId)
        }
    }

    fun clearPersonalizedImage(profileId: ProfileIdentifier) {
        scope.launch {
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
            active = false,
            color = ProfilesData.ProfileColorNames.SPRING_GRAY,
            lastAuthenticated = null,
            ssoTokenScope = null,
            avatar = ProfilesData.Avatar.PersonalizedImage
        )
    }
}

@Composable
fun rememberProfileController(): ProfileController {
    val addProfileUseCase by rememberInstance<AddProfileUseCase>()
    val decryptAccessTokenUseCase by rememberInstance<DecryptAccessTokenUseCase>()
    val deleteProfileUseCase by rememberInstance<DeleteProfileUseCase>()
    val getActiveProfileUseCase by rememberInstance<GetActiveProfileUseCase>()
    val getProfilesUseCase by rememberInstance<GetProfilesUseCase>()
    val resetProfileUseCase by rememberInstance<ResetProfileUseCase>()
    val switchActiveProfileUseCase by rememberInstance<SwitchActiveProfileUseCase>()
    val updateProfileUseCase by rememberInstance<UpdateProfileUseCase>()
    val logoutProfileUseCase by rememberInstance<LogoutProfileUseCase>()
    val switchProfileToPKVUseCase by rememberInstance<SwitchProfileToPKVUseCase>()
    val profilesWithPairedDevicesUseCase by rememberInstance<ProfilesWithPairedDevicesUseCase>()
    val scope = rememberCoroutineScope()

    return remember {
        ProfileController(
            addProfileUseCase = addProfileUseCase,
            decryptAccessTokenUseCase = decryptAccessTokenUseCase,
            deleteProfileUseCase = deleteProfileUseCase,
            getActiveProfileUseCase = getActiveProfileUseCase,
            getProfilesUseCase = getProfilesUseCase,
            resetProfileUseCase = resetProfileUseCase,
            switchActiveProfileUseCase = switchActiveProfileUseCase,
            updateProfileUseCase = updateProfileUseCase,
            logoutProfileUseCase = logoutProfileUseCase,
            switchProfileToPKVUseCase = switchProfileToPKVUseCase,
            profilesWithPairedDevicesUseCase = profilesWithPairedDevicesUseCase,
            scope = scope
        )
    }
}
