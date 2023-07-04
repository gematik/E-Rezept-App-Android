/*
 * Copyright (c) 2023 gematik GmbH
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

package de.gematik.ti.erp.app.profiles.ui

import android.graphics.Bitmap
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import androidx.paging.PagingData
import de.gematik.ti.erp.app.profiles.model.ProfilesData
import de.gematik.ti.erp.app.profiles.repository.ProfileIdentifier
import de.gematik.ti.erp.app.profiles.usecase.ProfileAvatarUseCase
import de.gematik.ti.erp.app.profiles.usecase.ProfilesUseCase
import de.gematik.ti.erp.app.profiles.usecase.ProfilesWithPairedDevicesUseCase
import de.gematik.ti.erp.app.profiles.usecase.model.ProfilesUseCaseData
import de.gematik.ti.erp.app.protocol.model.AuditEventData
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.kodein.di.compose.rememberInstance

class ProfilesController(
    private val profilesUseCase: ProfilesUseCase,
    private val profilesWithPairedDevicesUseCase: ProfilesWithPairedDevicesUseCase,
    private val profileAvatarUseCase: ProfileAvatarUseCase
) : ProfileBridge {
    private val profilesFlow = profilesUseCase.profiles.map { ProfilesStateData.ProfilesState(it) }

    val profilesState
        @Composable
        get() = profilesFlow.collectAsState(ProfilesStateData.defaultProfilesState)

    fun pairedDevices(profileId: ProfileIdentifier) =
        profilesWithPairedDevicesUseCase.pairedDevices(profileId)

    // tag::DeletePairedDevicesViewModel[]
    suspend fun deletePairedDevice(profileId: ProfileIdentifier, device: ProfilesUseCaseData.PairedDevice) =
        profilesWithPairedDevicesUseCase.deletePairedDevices(profileId, device)

    // end::DeletePairedDevicesViewModel[]
    fun decryptedAccessToken(profile: ProfilesUseCaseData.Profile) =
        profilesUseCase.decryptedAccessToken(profile.id)

    suspend fun logout(profile: ProfilesUseCaseData.Profile) {
        profilesUseCase.logout(profile)
    }

    suspend fun addProfile(profileName: String) {
        profilesUseCase.addProfile(profileName, activate = true)
    }

    suspend fun removeProfile(profile: ProfilesUseCaseData.Profile, newProfileName: String?) {
        if (newProfileName != null) {
            profilesUseCase.removeAndSaveProfile(profile, newProfileName)
        } else {
            profilesUseCase.removeProfile(profile)
        }
    }

    fun loadAuditEventsForProfile(profileId: ProfileIdentifier): Flow<PagingData<AuditEventData.AuditEvent>> =
        profilesUseCase.auditEvents(profileId)

    override suspend fun switchActiveProfile(profile: ProfilesUseCaseData.Profile) {
        profilesUseCase.switchActiveProfile(profile)
    }

    override val profiles: Flow<List<ProfilesUseCaseData.Profile>> =
        profilesUseCase.profiles

    override suspend fun switchProfileToPKV(profileId: ProfileIdentifier) {
        profilesUseCase.switchProfileToPKV(profileId)
    }

    suspend fun updateProfileColor(profile: ProfilesUseCaseData.Profile, color: ProfilesData.ProfileColorNames) {
        profilesUseCase.updateProfileColor(profile, color)
    }

    suspend fun savePersonalizedProfileImage(profileId: ProfileIdentifier, profileImage: Bitmap) {
        profileAvatarUseCase.savePersonalizedProfileImage(profileId, profileImage)
    }

    suspend fun updateProfileName(profileId: ProfileIdentifier, newName: String) {
        profilesUseCase.updateProfileName(profileId, newName)
    }

    suspend fun saveAvatarFigure(profileId: ProfileIdentifier, avatarFigure: ProfilesData.AvatarFigure) {
        profileAvatarUseCase.saveAvatarFigure(profileId, avatarFigure)
    }

    suspend fun clearPersonalizedImage(profileId: ProfileIdentifier) {
        profileAvatarUseCase.clearPersonalizedImage(profileId)
    }
}

@Composable
fun rememberProfilesController(): ProfilesController {
    val profilesUseCase by rememberInstance<ProfilesUseCase>()
    val profilesWithPairedDevicesUseCase by rememberInstance<ProfilesWithPairedDevicesUseCase>()
    val profileAvatarUseCase by rememberInstance<ProfileAvatarUseCase>()
    return remember {
        ProfilesController(
            profilesUseCase,
            profilesWithPairedDevicesUseCase,
            profileAvatarUseCase
        )
    }
}

object ProfilesStateData {
    @Immutable
    data class ProfilesState(
        val profiles: List<ProfilesUseCaseData.Profile>
    ) {
        fun activeProfile() = profiles.find { it.active }!!
        fun profileById(profileId: String) = profiles.find { it.id == profileId }
        fun containsProfileWithName(name: String) = profiles.any {
            it.name.equals(name.trim(), true)
        }
    }

    val defaultProfilesState = ProfilesState(
        profiles = listOf()
    )

    val defaultProfile = ProfilesUseCaseData.Profile(
        id = "",
        name = "",
        insuranceInformation = ProfilesUseCaseData.ProfileInsuranceInformation(
            insuranceType = ProfilesUseCaseData.InsuranceType.NONE
        ),
        active = false,
        color = ProfilesData.ProfileColorNames.SPRING_GRAY,
        lastAuthenticated = null,
        ssoTokenScope = null,
        avatarFigure = ProfilesData.AvatarFigure.PersonalizedImage
    )
}
