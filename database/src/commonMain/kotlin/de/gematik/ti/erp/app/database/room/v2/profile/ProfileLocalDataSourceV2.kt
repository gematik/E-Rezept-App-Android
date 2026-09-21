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

package de.gematik.ti.erp.app.database.room.v2.profile

import de.gematik.ti.erp.app.database.api.ProfileLocalDataSource
import de.gematik.ti.erp.app.profile.model.Avatar
import de.gematik.ti.erp.app.profile.model.InsuranceType
import de.gematik.ti.erp.app.profile.model.ProfileColorNames
import de.gematik.ti.erp.app.profile.model.ProfileErpModel
import de.gematik.ti.erp.app.profile.repository.ProfileIdentifier
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.datetime.Instant

class ProfileLocalDataSourceV2(
    private val profileDao: ProfileDao
) : ProfileLocalDataSource {

    override fun loadProfiles(): Flow<List<ProfileErpModel>> =
        profileDao.getAllProfilesWithUserAuthentication()
            .map { entities ->
                entities.map { it.toProfileErpModel() }
            }

    override fun activeProfile(): Flow<ProfileErpModel?> =
        profileDao.getActiveProfileWithUserAuthentication()
            .map { it?.toProfileErpModel() }

    override fun getProfileById(profileId: String): Flow<ProfileErpModel?> =
        profileDao.getProfileWithUserAuthenticationById(profileId).map { it?.toProfileErpModel() }

    override suspend fun saveProfile(profile: ProfileErpModel) {
        profileDao.upsert(profile.toProfileEntity())
    }

    override suspend fun deleteProfile(profileId: String) {
        profileDao.delete(profileId)
    }

    override suspend fun activateProfile(profileId: String) {
        profileDao.activateProfileAtomic(profileId)
    }

    override suspend fun deactivateAllProfiles() {
        profileDao.deactivateAllProfiles()
    }

    override suspend fun updateProfileName(profileId: String, profileName: String) {
        profileDao.updateProfileName(profileId, profileName)
    }

    override suspend fun updateProfileColor(profileId: String, color: ProfileColorNames) {
        profileDao.updateProfileColor(profileId, color.name)
    }

    override suspend fun saveAvatarFigure(profileId: String, avatar: Avatar) {
        profileDao.updateAvatar(profileId, avatar.name)
    }

    override suspend fun savePersonalizedProfileImage(profileId: String, profileImage: ByteArray) {
        profileDao.updatePersonalizedImage(profileId, profileImage)
    }

    override suspend fun clearPersonalizedProfileImage(profileId: String) {
        profileDao.updatePersonalizedImage(profileId, null)
    }

    override suspend fun updateInsuranceInformation(
        profileId: String,
        insurantName: String,
        insuranceIdentifier: String,
        organizationIdentifier: String,
        insuranceName: String
    ) {
        profileDao.updateInsuranceInformation(
            profileId = profileId,
            insurantName = insurantName,
            insuranceIdentifier = insuranceIdentifier,
            organizationIdentifier = organizationIdentifier,
            insuranceName = insuranceName
        )
    }

    override suspend fun updateInsuranceType(profileId: String, insuranceType: InsuranceType) {
        profileDao.updateInsuranceType(profileId, insuranceType.name)
    }

    override suspend fun updateOrganizationIdentifier(profileId: String, organizationIdentifier: String) {
        profileDao.updateOrganizationIdentifier(profileId, organizationIdentifier)
    }

    override suspend fun updateLastAuthenticated(profileId: String, lastAuthenticated: Instant) {
        profileDao.updateLastAuthenticated(profileId, lastAuthenticated.toEpochMilliseconds())
    }

    override suspend fun updateLastTaskSynced(profileId: String, lastTaskSynced: Instant) {
        profileDao.updateLastAuthenticated(profileId, lastTaskSynced.toEpochMilliseconds())
    }

    override suspend fun wasProfileEverAuthenticated(profileId: ProfileIdentifier): Boolean = profileDao.wasProfileEverAuthenticated(profileId)
}
