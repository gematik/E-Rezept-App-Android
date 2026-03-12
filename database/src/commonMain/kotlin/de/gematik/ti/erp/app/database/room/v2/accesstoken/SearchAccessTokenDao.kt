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

package de.gematik.ti.erp.app.database.room.v2.accesstoken

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import kotlinx.datetime.Instant

@Dao
interface SearchAccessTokenDao {

    // Get the current token (null if not stored yet)
    @Query("SELECT * FROM search_access_token WHERE id = :id LIMIT 1")
    suspend fun get(id: String = SINGLETON_ID): SearchAccessTokenEntity?

    // Observe token changes as a Flow
    @Query("SELECT * FROM search_access_token WHERE id = :id LIMIT 1")
    fun observe(id: String = SINGLETON_ID): Flow<SearchAccessTokenEntity?>

    // Insert or replace the singleton record
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(entity: SearchAccessTokenEntity)

    // Update just the token string and timestamp
    @Query(
        """
        UPDATE search_access_token
        SET accessToken = :accessToken,
            lastUpdate = :lastUpdate
        WHERE id = :id
    """
    )
    suspend fun updateToken(
        accessToken: String,
        lastUpdate: Instant,
        id: String = SINGLETON_ID
    ): Int

    // Clear table (for logout / reset)
    @Query("DELETE FROM search_access_token")
    suspend fun clear()
}
