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

import de.gematik.ti.erp.app.profile.repository.ProfileIdentifier
import de.gematik.ti.erp.app.profiles.model.ProfilesData
import kotlinx.coroutines.flow.Flow
import kotlinx.datetime.Instant

interface ProfileRepository {
    fun profiles(): Flow<List<ProfilesData.Profile>>
    fun activeProfile(): Flow<ProfilesData.Profile>
    fun getProfileById(profileId: ProfileIdentifier): Flow<ProfilesData.Profile>
    suspend fun isSsoTokenValid(profileId: ProfileIdentifier): Flow<Boolean>
    suspend fun createNewProfile(profileName: String)
    suspend fun activateProfile(profileId: ProfileIdentifier)
    suspend fun removeProfile(profileId: ProfileIdentifier, profileName: String)
    suspend fun saveInsuranceInformation(
        profileId: ProfileIdentifier,
        insurantName: String,
        insuranceIdentifier: String,
        organizationIdentifier: String,
        insuranceName: String
    )
    suspend fun updateProfileName(profileId: ProfileIdentifier, profileName: String)
    suspend fun updateProfileColor(profileId: ProfileIdentifier, color: ProfilesData.ProfileColorNames)
    suspend fun updateLastAuthenticated(profileId: ProfileIdentifier, lastAuthenticated: Instant)
    suspend fun saveAvatarFigure(profileId: ProfileIdentifier, avatar: ProfilesData.Avatar)
    suspend fun savePersonalizedProfileImage(profileId: ProfileIdentifier, profileImage: ByteArray)
    suspend fun clearPersonalizedProfileImage(profileId: ProfileIdentifier)
    suspend fun switchProfileToPKV(profileId: ProfileIdentifier): Boolean
    suspend fun switchProfileToGKV(profileId: ProfileIdentifier): Boolean
    suspend fun switchProfileToBUND(profileId: ProfileIdentifier): Boolean
    suspend fun checkIsProfilePKV(profileId: ProfileIdentifier): Boolean
    suspend fun getOrganizationIdentifier(profileId: ProfileIdentifier): Flow<String>
    suspend fun updateOrganizationIdentifier(iknr: String)
}
