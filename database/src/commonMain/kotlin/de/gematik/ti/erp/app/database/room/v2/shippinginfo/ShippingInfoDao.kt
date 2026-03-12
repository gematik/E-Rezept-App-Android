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

package de.gematik.ti.erp.app.database.room.v2.shippinginfo

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface ShippingInfoDao {

    // Insert or replace a shipping info record
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(entity: ShippingInfoEntity): Long

    // Bulk upsert
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(entities: List<ShippingInfoEntity>)

    // Fetch by primary key
    @Query("SELECT * FROM shipping_info WHERE id = :id")
    suspend fun getById(id: String): ShippingInfoEntity?

    // Observe a single record
    @Query("SELECT * FROM shipping_info WHERE id = :id")
    fun observeById(id: String): Flow<ShippingInfoEntity?>

    // Simple finders
    @Query("SELECT * FROM shipping_info WHERE mail = :mail")
    suspend fun findByEmail(mail: String): List<ShippingInfoEntity>

    @Query("SELECT * FROM shipping_info WHERE phone = :phone")
    suspend fun findByPhone(phone: String): List<ShippingInfoEntity>

    // Optional: lightweight search by city / zip
    @Query(
        """
        SELECT * FROM shipping_info
        WHERE (:city IS NULL OR city = :city)
          AND (:zip  IS NULL OR zip  = :zip)
    """
    )
    suspend fun search(city: String?, zip: String?): List<ShippingInfoEntity>

    // Delete
    @Delete
    suspend fun delete(entity: ShippingInfoEntity)

    @Query("DELETE FROM shipping_info WHERE id = :id")
    suspend fun deleteById(id: String)
}
