/*
 * Copyright (c) 2021 gematik GmbH
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
import de.gematik.ti.erp.app.db.entities.Communication
import de.gematik.ti.erp.app.db.entities.CommunicationProfile
import kotlinx.coroutines.flow.Flow

@Dao
interface CommunicationDao {

    @Query("SELECT * FROM communications WHERE profile = :profile AND profileName = :userProfile")
    fun getAllCommunications(
        profile: CommunicationProfile,
        userProfile: String
    ): Flow<List<Communication>>

    @Query("SELECT * FROM communications WHERE profile = :profile AND consumed = :consumed AND profileName = :userProfile")
    fun getAllUnreadCommunications(
        profile: CommunicationProfile,
        consumed: Boolean = false,
        userProfile: String
    ): Flow<List<Communication>>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertMultipleCommunications(vararg communication: Communication)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertCommunication(communication: Communication)

    @Query("UPDATE communications SET consumed = :consumed WHERE communicationId = :communicationId")
    suspend fun updateCommunication(communicationId: String, consumed: Boolean)
}
