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

package de.gematik.ti.erp.app.profiles.repository

import de.gematik.ti.erp.app.database.api.ProfileLocalDataSource
import de.gematik.ti.erp.app.database.api.UserAuthenticationLocalDataSource
import de.gematik.ti.erp.app.profile.model.Avatar
import de.gematik.ti.erp.app.profile.model.ProfileColorNames
import de.gematik.ti.erp.app.profile.model.ProfileErpModel
import de.gematik.ti.erp.app.profile.model.ProfileImageDataErpModel
import de.gematik.ti.erp.app.profile.model.ProfileInsuranceDataErpModel
import de.gematik.ti.erp.app.profile.repository.ProfileIdentifier
import de.gematik.ti.erp.app.userauthentication.model.UserAuthenticationErpModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.datetime.Instant
import de.gematik.ti.erp.app.profile.model.InsuranceType as ErpInsuranceType

class DefaultProfilesRepository(
    private val profileLocalDataSource: ProfileLocalDataSource,
    private val userAuthenticationLocalDataSource: UserAuthenticationLocalDataSource,
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO
) : ProfileRepository {
    private val lock = Mutex()

    override fun profiles(): Flow<List<ProfileErpModel>> =
        profileLocalDataSource.loadProfiles()
            .map { it.withRecoveredActiveProfile() }
            .flowOn(dispatcher)

    override fun activeProfile(): Flow<ProfileErpModel> =
        profiles()
            .mapNotNull { profiles -> profiles.firstOrNull { it.active } }
            .flowOn(dispatcher)

    private fun List<ProfileErpModel>.withRecoveredActiveProfile(): List<ProfileErpModel> {
        val hasActiveProfile = any { it.active }
        return if (size == 1 && !hasActiveProfile) {
            map { it.copy(active = true) }
        } else {
            this
        }
    }

    override suspend fun createNewProfile(profileName: String) {
        val randomColor = ProfileColorNames.entries.toTypedArray().random()
        val newProfile = ProfileErpModel(
            id = java.util.UUID.randomUUID().toString(),
            name = profileName,
            active = true,
            isNewlyCreated = true,
            profileImageData = ProfileImageDataErpModel(
                color = randomColor,
                avatar = Avatar.PersonalizedImage,
                image = null
            ),
            insuranceData = ProfileInsuranceDataErpModel(
                insurantName = null,
                insuranceIdentifier = null,
                insuranceName = null,
                insuranceType = ErpInsuranceType.NONE,
                organizationIdentifier = null
            ),
            isConsentDrawerShown = false,
            lastAuthenticated = null,
            lastAuditEventSynced = null,
            lastTaskSynced = null,
            userAuthentication = UserAuthenticationErpModel.NotInitialized
        )
        profileLocalDataSource.deactivateAllProfiles()
        profileLocalDataSource.saveProfile(newProfile)
        userAuthenticationLocalDataSource.saveUserAuthenticationForProfile(
            newProfile.id,
            newProfile.userAuthentication
        )
    }

    override suspend fun activateProfile(profileId: ProfileIdentifier) {
        profileLocalDataSource.activateProfile(profileId)
    }

    override suspend fun removeProfile(profileId: ProfileIdentifier, profileName: String) {
        lock.withLock {
            val profiles = profileLocalDataSource.loadProfiles().first()

            if (profiles.size == 1) {
                createNewProfile(profileName)
            }

            val profileToDelete = profiles.find { it.id == profileId }
            if (profileToDelete?.active == true && profiles.size > 1) {
                val nextProfile = profiles.find { it.id != profileId }
                nextProfile?.let {
                    profileLocalDataSource.activateProfile(it.id)
                }
            }

            profileLocalDataSource.deleteProfile(profileId)
        }
    }

    override suspend fun saveInsuranceInformation(
        profileId: ProfileIdentifier,
        insurantName: String,
        insuranceIdentifier: String,
        organizationIdentifier: String,
        insuranceName: String
    ) {
        lock.withLock {
            val allProfiles = profileLocalDataSource.loadProfiles().first()

            val existingProfile = allProfiles.find {
                it.insuranceData.insuranceIdentifier == insuranceIdentifier && it.id != profileId
            }
            if (existingProfile != null) {
                throw KVNRAlreadyAssignedException(
                    "KVNR already assigned to another profile",
                    false,
                    existingProfile.name,
                    existingProfile.insuranceData.insuranceIdentifier!!
                )
            }

            val currentProfile = allProfiles.find { it.id == profileId }
            if (currentProfile?.insuranceData?.insuranceIdentifier != null &&
                currentProfile.insuranceData.insuranceIdentifier != insuranceIdentifier
            ) {
                throw KVNRAlreadyAssignedException(
                    "Profile already assigned to another KVNR",
                    true,
                    profileId,
                    currentProfile.insuranceData.insuranceIdentifier!!
                )
            }

            profileLocalDataSource.updateInsuranceInformation(
                profileId,
                insurantName,
                insuranceIdentifier,
                organizationIdentifier,
                insuranceName
            )
        }
    }

    override suspend fun updateProfileName(profileId: ProfileIdentifier, profileName: String) {
        profileLocalDataSource.updateProfileName(profileId, profileName)
    }

    override suspend fun updateProfileColor(profileId: ProfileIdentifier, color: ProfileColorNames) {
        profileLocalDataSource.updateProfileColor(profileId, color)
    }

    override suspend fun updateLastAuthenticated(profileId: ProfileIdentifier, lastAuthenticated: Instant) {
        profileLocalDataSource.updateLastAuthenticated(profileId, lastAuthenticated)
    }

    override suspend fun updateLastTaskSynced(profileId: ProfileIdentifier, lastTaskSynced: Instant) {
        profileLocalDataSource.updateLastTaskSynced(profileId, lastTaskSynced)
    }

    override suspend fun saveAvatarFigure(profileId: ProfileIdentifier, avatar: Avatar) {
        profileLocalDataSource.saveAvatarFigure(profileId, avatar)
    }

    override suspend fun savePersonalizedProfileImage(profileId: ProfileIdentifier, profileImage: ByteArray) {
        profileLocalDataSource.savePersonalizedProfileImage(profileId, profileImage)
    }

    override suspend fun clearPersonalizedProfileImage(profileId: ProfileIdentifier) {
        profileLocalDataSource.clearPersonalizedProfileImage(profileId)
    }

    override suspend fun switchProfileToPKV(profileId: ProfileIdentifier) {
        profileLocalDataSource.updateInsuranceType(profileId, ErpInsuranceType.PKV)
    }

    override suspend fun switchProfileToGKV(profileId: ProfileIdentifier) {
        profileLocalDataSource.updateInsuranceType(profileId, ErpInsuranceType.GKV)
    }

    override suspend fun switchProfileToBUND(profileId: ProfileIdentifier) {
        profileLocalDataSource.updateInsuranceType(profileId, ErpInsuranceType.BUND)
    }

    override suspend fun checkIsProfilePKV(profileId: ProfileIdentifier): Boolean =
        getProfileById(profileId).first().isPkv()

    override fun getProfileById(profileId: ProfileIdentifier): Flow<ProfileErpModel> =
        profileLocalDataSource.getProfileById(profileId)
            .mapNotNull { it }
            .flowOn(dispatcher)

    override suspend fun isSsoTokenValid(profileId: ProfileIdentifier): Flow<Boolean> =
        profileLocalDataSource.getProfileById(profileId)
            .mapNotNull { it }
            .map { it.isSSOTokenValid() }
            .flowOn(dispatcher)

    override suspend fun getOrganizationIdentifier(profileId: ProfileIdentifier): Flow<String> =
        profileLocalDataSource.getProfileById(profileId)
            .mapNotNull { it?.insuranceData?.organizationIdentifier ?: "" }
            .flowOn(dispatcher)

    override suspend fun updateOrganizationIdentifier(iknr: String) {
        val activeProfile = activeProfile().first()
        profileLocalDataSource.updateOrganizationIdentifier(activeProfile.id, iknr)
    }

    override suspend fun wasProfileEverAuthenticated(profileId: ProfileIdentifier): Boolean =
        profileLocalDataSource.wasProfileEverAuthenticated(profileId)
}

class KVNRAlreadyAssignedException(
    message: String,
    val isActiveProfile: Boolean,
    val inProfile: String,
    val insuranceIdentifier: String
) : IllegalStateException(message)
