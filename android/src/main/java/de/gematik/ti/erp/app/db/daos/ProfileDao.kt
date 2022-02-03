/*
 * Copyright (c) 2022 gematik GmbH
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

package de.gematik.ti.erp.app.db.daos

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import de.gematik.ti.erp.app.db.entities.ProfileEntity
import de.gematik.ti.erp.app.db.entities.ProfileColorNames
import kotlinx.coroutines.flow.Flow
import java.time.Instant
import java.time.OffsetDateTime

@Dao
interface ProfileDao {

    @Query("SELECT * FROM profiles")
    fun getAllProfilesFlow(): Flow<List<ProfileEntity>>

    @Query("SELECT * FROM profiles")
    suspend fun getAllProfiles(): List<ProfileEntity>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertProfile(profile: ProfileEntity)

    @Query("SELECT COUNT(*) FROM profiles WHERE name = :profileName")
    suspend fun countProfilesWithName(profileName: String): Int

    @Query("DELETE FROM profiles WHERE name = :profileName")
    suspend fun removeProfileByName(profileName: String)

    @Query("UPDATE profiles SET name = :profileName WHERE id = :profileId")
    suspend fun updateProfileName(profileId: Int, profileName: String)

    @Query("UPDATE profiles SET name = :updatedName WHERE name = :currentName")
    suspend fun updateProfileName(currentName: String, updatedName: String)

    @Query("SELECT * FROM profiles WHERE id = :profileId")
    fun loadProfile(profileId: Int): Flow<ProfileEntity?>

    @Query("UPDATE profiles SET color = :color WHERE name = :profileName")
    suspend fun updateProfileColor(profileName: String, color: ProfileColorNames)

    @Query("UPDATE profiles SET lastAuthenticated = :lastAuthenticated WHERE name = :profileName")
    suspend fun updateLastAuthenticated(lastAuthenticated: Instant, profileName: String)

    @Query("SELECT lastAuthenticated FROM profiles WHERE id = :profileId")
    fun getLastAuthenticated(profileId: Int): Flow<Instant>

    @Query("UPDATE profiles SET lastAuditEventSynced = :lastAuditEventSynced WHERE name = :profileName")
    suspend fun updateAuditEventSynced(lastAuditEventSynced: OffsetDateTime, profileName: String)

    @Query("SELECT lastAuditEventSynced FROM profiles WHERE name = :profileName")
    suspend fun getLastAuditEventSynced(profileName: String): OffsetDateTime?

    @Query("UPDATE profiles SET lastTaskSynced = :lastSynced WHERE name = :profileName")
    suspend fun updateLastTaskSynced(profileName: String, lastSynced: Instant?)

    @Query("SELECT lastTaskSynced FROM profiles WHERE name = :profileName")
    suspend fun getLastTaskSynced(profileName: String): Instant?

    @Query("UPDATE profiles SET insurantName = :insurantName, insuranceIdentifier = :insuranceIdentifier, insuranceName = :insuranceName WHERE name = :profileName")
    suspend fun setInsuranceInformation(profileName: String, insurantName: String, insuranceIdentifier: String, insuranceName: String)
}
