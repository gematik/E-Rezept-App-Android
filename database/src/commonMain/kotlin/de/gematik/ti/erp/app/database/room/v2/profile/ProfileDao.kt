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

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

@Dao
interface ProfileDao {
    @Upsert
    suspend fun upsert(profile: ProfileEntity)

    @Upsert
    suspend fun upsertAll(items: List<ProfileEntity>)

    @Query("SELECT * FROM profiles WHERE identifier = :profileId")
    fun getProfileById(profileId: String): Flow<ProfileWithUserAuthenticationEntity?>

    @Transaction
    @Query("SELECT * FROM profiles")
    fun getAllProfilesWithUserAuthentication(): Flow<List<ProfileWithUserAuthenticationEntity>>

    @Transaction
    @Query("SELECT * FROM profiles WHERE identifier = :profileId")
    fun getProfileWithUserAuthenticationById(profileId: String): Flow<ProfileWithUserAuthenticationEntity?>

    @Transaction
    @Query("SELECT * FROM profiles WHERE active = 1 LIMIT 1")
    fun getActiveProfileWithUserAuthentication(): Flow<ProfileWithUserAuthenticationEntity?>

    @Query("DELETE FROM profiles WHERE identifier = :profileId")
    suspend fun delete(profileId: String)

    @Query("UPDATE profiles SET active = 0")
    suspend fun deactivateAllProfiles()

    @Query("UPDATE profiles SET active = 1 WHERE identifier = :profileId")
    suspend fun activateProfile(profileId: String)

    @Transaction
    suspend fun activateProfileAtomic(profileId: String) {
        deactivateAllProfiles()
        activateProfile(profileId)
    }

    @Query("UPDATE profiles SET name = :profileName, isNew = 0 WHERE identifier = :profileId")
    suspend fun updateProfileName(profileId: String, profileName: String)

    @Query("UPDATE profiles SET color = :color WHERE identifier = :profileId")
    suspend fun updateProfileColor(profileId: String, color: String)

    @Query("UPDATE profiles SET avatar = :avatar WHERE identifier = :profileId")
    suspend fun updateAvatar(profileId: String, avatar: String)

    @Query("UPDATE profiles SET image = :profileImage WHERE identifier = :profileId")
    suspend fun updatePersonalizedImage(profileId: String, profileImage: ByteArray?)

    @Query("UPDATE profiles SET lastAuthenticated = :lastAuthenticated WHERE identifier = :profileId")
    suspend fun updateLastAuthenticated(profileId: String, lastAuthenticated: Long)

    @Query("UPDATE profiles SET lastTaskSynced = :lastTaskSynced WHERE identifier = :profileId")
    suspend fun updateLastTaskSynced(profileId: String, lastTaskSynced: Long)

    @Query(
        """
        UPDATE profiles 
        SET insurantName = :insurantName,
            insuranceId = :insuranceIdentifier,
            insuranceName = CASE WHEN :insuranceName = '' THEN NULL ELSE :insuranceName END,
            organizationId = :organizationIdentifier,
            name = CASE WHEN isNew = 1 THEN :insurantName ELSE name END
        WHERE identifier = :profileId
        """
    )
    suspend fun updateInsuranceInformation(
        profileId: String,
        insurantName: String,
        insuranceIdentifier: String,
        organizationIdentifier: String,
        insuranceName: String
    )

    @Query("UPDATE profiles SET insuranceType = :insuranceType WHERE identifier = :profileId")
    suspend fun updateInsuranceType(profileId: String, insuranceType: String)

    @Query("UPDATE profiles SET organizationId = :organizationIdentifier WHERE identifier = :profileId")
    suspend fun updateOrganizationIdentifier(profileId: String, organizationIdentifier: String)

    @Query("SELECT EXISTS(SELECT 1 FROM profiles WHERE identifier = :profileId AND lastAuthenticated IS NOT NULL)")
    suspend fun wasProfileEverAuthenticated(profileId: String): Boolean
}
