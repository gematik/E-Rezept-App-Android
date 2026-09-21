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

import de.gematik.ti.erp.app.profile.model.Avatar
import de.gematik.ti.erp.app.profile.model.ProfileColorNames
import de.gematik.ti.erp.app.profile.model.ProfileErpModel
import de.gematik.ti.erp.app.profile.repository.ProfileIdentifier
import kotlinx.coroutines.flow.Flow
import kotlinx.datetime.Instant

interface ProfileRepository {
    fun profiles(): Flow<List<ProfileErpModel>>
    fun activeProfile(): Flow<ProfileErpModel>
    fun getProfileById(profileId: ProfileIdentifier): Flow<ProfileErpModel>
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
    suspend fun updateProfileColor(profileId: ProfileIdentifier, color: ProfileColorNames)
    suspend fun updateLastAuthenticated(profileId: ProfileIdentifier, lastAuthenticated: Instant)
    suspend fun updateLastTaskSynced(profileId: ProfileIdentifier, lastTaskSynced: Instant)
    suspend fun saveAvatarFigure(profileId: ProfileIdentifier, avatar: Avatar)
    suspend fun savePersonalizedProfileImage(profileId: ProfileIdentifier, profileImage: ByteArray)
    suspend fun clearPersonalizedProfileImage(profileId: ProfileIdentifier)
    suspend fun switchProfileToPKV(profileId: ProfileIdentifier)
    suspend fun switchProfileToGKV(profileId: ProfileIdentifier)
    suspend fun switchProfileToBUND(profileId: ProfileIdentifier)
    suspend fun checkIsProfilePKV(profileId: ProfileIdentifier): Boolean
    suspend fun getOrganizationIdentifier(profileId: ProfileIdentifier): Flow<String>
    suspend fun updateOrganizationIdentifier(iknr: String)
    suspend fun wasProfileEverAuthenticated(profileId: ProfileIdentifier): Boolean
}
