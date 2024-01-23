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

package de.gematik.ti.erp.app.profiles.repository

import de.gematik.ti.erp.app.profiles.model.ProfilesData
import kotlinx.coroutines.flow.Flow
import kotlinx.datetime.Instant

interface ProfileRepository {
    fun profiles(): Flow<List<ProfilesData.Profile>>
    fun activeProfile(): Flow<ProfilesData.Profile>
    suspend fun saveProfile(profileName: String, activate: Boolean)
    suspend fun activateProfile(profileId: ProfileIdentifier)
    suspend fun removeProfile(profileId: ProfileIdentifier)
    suspend fun saveInsuranceInformation(
        profileId: ProfileIdentifier,
        insurantName: String,
        insuranceIdentifier: String,
        insuranceName: String
    )
    suspend fun updateProfileName(profileId: ProfileIdentifier, profileName: String)
    suspend fun updateProfileColor(profileId: ProfileIdentifier, color: ProfilesData.ProfileColorNames)
    suspend fun updateLastAuthenticated(profileId: ProfileIdentifier, lastAuthenticated: Instant)
    suspend fun saveAvatarFigure(profileId: ProfileIdentifier, avatar: ProfilesData.Avatar)
    suspend fun savePersonalizedProfileImage(profileId: ProfileIdentifier, profileImage: ByteArray)
    suspend fun clearPersonalizedProfileImage(profileId: ProfileIdentifier)
    suspend fun switchProfileToPKV(profileId: ProfileIdentifier)
    suspend fun checkIsProfilePKV(profileId: ProfileIdentifier): Boolean
}
