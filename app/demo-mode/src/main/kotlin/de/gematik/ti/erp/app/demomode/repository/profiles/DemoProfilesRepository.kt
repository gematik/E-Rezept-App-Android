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
package de.gematik.ti.erp.app.demomode.repository.profiles

import de.gematik.ti.erp.app.demomode.datasource.DemoModeDataSource
import de.gematik.ti.erp.app.demomode.datasource.INDEX_OUT_OF_BOUNDS
import de.gematik.ti.erp.app.demomode.datasource.data.DemoProfileInfo.create
import de.gematik.ti.erp.app.demomode.model.DemoModeProfile
import de.gematik.ti.erp.app.demomode.model.toProfile
import de.gematik.ti.erp.app.demomode.model.toProfiles
import de.gematik.ti.erp.app.demomode.repository.profiles.DemoProfilesRepository.ImageActions.Add
import de.gematik.ti.erp.app.demomode.repository.profiles.DemoProfilesRepository.ImageActions.NoAction
import de.gematik.ti.erp.app.demomode.repository.profiles.DemoProfilesRepository.ImageActions.Remove
import de.gematik.ti.erp.app.profiles.model.ProfilesData
import de.gematik.ti.erp.app.profiles.repository.ProfileIdentifier
import de.gematik.ti.erp.app.profiles.repository.ProfileRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.flow.updateAndGet
import kotlinx.coroutines.withContext
import kotlinx.datetime.Instant
import java.util.UUID

class DemoProfilesRepository(
    private val dataSource: DemoModeDataSource,
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO
) : ProfileRepository {

    private fun demoModeProfiles(): MutableStateFlow<MutableList<DemoModeProfile>> = dataSource.profiles
    override fun profiles(): Flow<List<ProfilesData.Profile>> = demoModeProfiles()
        .mapNotNull(MutableList<DemoModeProfile>::toProfiles)

    override fun activeProfile() = demoModeProfiles().mapNotNull {
        it.find { profile -> profile.active }?.toProfile()
    }

    override suspend fun saveProfile(profileName: String, activate: Boolean) {
        withContext(dispatcher) {
            dataSource.profiles.value = dataSource.profiles
                .updateAndGet { profileList ->
                    val profiles = profileList.deactivateAllProfiles()
                    profiles.add(profileName.create())
                    profiles
                }
        }
    }

    override suspend fun activateProfile(profileId: ProfileIdentifier) {
        withContext(dispatcher) {
            dataSource.profiles.value = dataSource.profiles
                .updateAndGet { profileList ->
                    val profiles = profileList.deactivateAllProfiles()
                    val updatedProfiles = profiles.replace(profileId = profileId, activate = true)
                    updatedProfiles
                }
        }
    }

    override suspend fun removeProfile(profileId: ProfileIdentifier) {
        withContext(dispatcher) {
            dataSource.profiles.value = dataSource.profiles
                .updateAndGet { profiles ->
                    profiles.removeIf { profile -> profile.id == profileId }
                    profiles
                }
        }
    }

    override suspend fun saveInsuranceInformation(
        profileId: ProfileIdentifier,
        insurantName: String,
        insuranceIdentifier: String,
        insuranceName: String
    ) {
        // Not used in demo mode
    }

    override suspend fun updateProfileName(profileId: ProfileIdentifier, profileName: String) {
        withContext(dispatcher) {
            dataSource.profiles.value = dataSource.profiles
                .updateAndGet {
                    it.replace(profileId = profileId, name = profileName)
                }
                .updateUUIDForChangeVisibility()
        }
    }

    override suspend fun updateProfileColor(profileId: ProfileIdentifier, color: ProfilesData.ProfileColorNames) {
        withContext(dispatcher) {
            dataSource.profiles.value = dataSource.profiles
                .updateAndGet { profileList ->
                    val updatedList = profileList.replace(profileId = profileId, color = color)
                    updatedList
                }
                .updateUUIDForChangeVisibility()
        }
    }

    override suspend fun updateLastAuthenticated(profileId: ProfileIdentifier, lastAuthenticated: Instant) {
        withContext(dispatcher) {
            dataSource.profiles.value = dataSource.profiles
                .updateAndGet { it.replace(profileId = profileId, lastAuthenticated = lastAuthenticated) }
                .updateUUIDForChangeVisibility()
        }
    }

    override suspend fun saveAvatarFigure(profileId: ProfileIdentifier, avatar: ProfilesData.Avatar) {
        withContext(dispatcher) {
            dataSource.profiles.value = dataSource.profiles
                .updateAndGet { it.replace(profileId = profileId, avatar = avatar) }
                .updateUUIDForChangeVisibility()
        }
    }

    override suspend fun savePersonalizedProfileImage(profileId: ProfileIdentifier, profileImage: ByteArray) {
        withContext(dispatcher) {
            dataSource.profiles.value = dataSource.profiles
                .updateAndGet { it.replace(profileId = profileId, profileImage = profileImage, imageAction = Add) }
                .updateUUIDForChangeVisibility()
        }
    }

    override suspend fun clearPersonalizedProfileImage(profileId: ProfileIdentifier) {
        withContext(dispatcher) {
            dataSource.profiles.value = dataSource.profiles
                .updateAndGet { it.replace(profileId = profileId, profileImage = null, imageAction = Remove) }
                .updateUUIDForChangeVisibility()
        }
    }

    override suspend fun switchProfileToPKV(profileId: ProfileIdentifier) {
        // Not for demo mode, will come later
    }

    override suspend fun checkIsProfilePKV(profileId: ProfileIdentifier): Boolean = false

    private fun MutableList<DemoModeProfile>.index(profileId: ProfileIdentifier) =
        indexOfFirst { profile -> profile.id == profileId }
            .takeIf { it != INDEX_OUT_OF_BOUNDS }

    private fun MutableList<DemoModeProfile>.replace(
        profileId: ProfileIdentifier,
        activate: Boolean? = null,
        name: String? = null,
        color: ProfilesData.ProfileColorNames? = null,
        lastAuthenticated: Instant? = null,
        avatar: ProfilesData.Avatar? = null,
        profileImage: ByteArray? = null,
        imageAction: ImageActions = NoAction
    ): MutableList<DemoModeProfile> =
        index(profileId)?.let { index ->
            val existingProfile = this[index]
            this[index] = this[index].copy(
                demoModeId = existingProfile.demoModeId,
                active = activate ?: existingProfile.active,
                name = name ?: existingProfile.name,
                color = color ?: existingProfile.color,
                lastAuthenticated = lastAuthenticated,
                avatar = avatar ?: existingProfile.avatar,
                personalizedImage = when (imageAction) {
                    Add -> profileImage
                    Remove -> null
                    NoAction -> existingProfile.personalizedImage
                }
            )
            this
        } ?: this

    private fun List<DemoModeProfile>.deactivateAllProfiles() =
        mapNotNull {
            it.copy(active = false)
        }.toMutableList()

    private fun MutableList<DemoModeProfile>.updateUUIDForChangeVisibility() =
        map { it.copy(demoModeId = UUID.randomUUID()) }.toMutableList()

    enum class ImageActions {
        Add, Remove, NoAction
    }
}
