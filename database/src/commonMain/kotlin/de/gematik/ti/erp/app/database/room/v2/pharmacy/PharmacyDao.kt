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

package de.gematik.ti.erp.app.database.room.v2.pharmacy

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.TypeConverters
import de.gematik.ti.erp.app.database.room.v2.task.util.InstantConverter
import kotlinx.coroutines.flow.Flow
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant

@Dao
@TypeConverters(InstantConverter::class)
interface PharmacyDao {

    // Insert or replace (idempotent upsert by primary key)
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(entity: PharmacyEntity)

    // Fetch by telematikId
    @Query("SELECT * FROM pharmacies WHERE id = :id")
    suspend fun getPharmacyById(id: String): PharmacyEntity?

    @Query(
        """
            SELECT EXISTS(      
            SELECT 1
            FROM pharmacies
            WHERE id = :id
              AND isFavourite = true
        )
     """
    )
    fun observeIsFavourite(id: String): Flow<Boolean>

    @Query(
        """
            SELECT EXISTS(      
            SELECT 1
            FROM pharmacies
            WHERE id = :id
              AND isOftenUsed = true
        )
     """
    )
    fun observeIsOftenUsed(id: String): Flow<Boolean>

    @Query(
        """
    SELECT * FROM pharmacies
"""
    )
    fun observePharmacy(): Flow<List<PharmacyEntity>>

    // Mark as used: bump counter + update lastUsed
    @Query(
        """
        UPDATE pharmacies
        SET countUsage = countUsage + 1,
            lastUsed   = :now
        WHERE id = :telematikId
    """
    )
    suspend fun markUsed(telematikId: String, now: Instant = Clock.System.now()): Int

    @Query("DELETE FROM pharmacies WHERE id = :id")
    suspend fun deleteById(id: String)
}
