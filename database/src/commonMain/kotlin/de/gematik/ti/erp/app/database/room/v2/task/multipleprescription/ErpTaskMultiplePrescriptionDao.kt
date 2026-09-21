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

package de.gematik.ti.erp.app.database.room.v2.task.multipleprescription

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface ErpTaskMultiplePrescriptionDao {

    @Insert(onConflict = OnConflictStrategy.Companion.REPLACE)
    suspend fun upsert(entity: ErpMultiplePrescriptionEntity): Long

    @Insert(onConflict = OnConflictStrategy.Companion.REPLACE)
    suspend fun upsertAll(entities: List<ErpMultiplePrescriptionEntity>): List<Long>

    @Query("SELECT * FROM multiple_prescriptions WHERE multiplePrescriptionId = :id")
    suspend fun getById(id: Long): ErpMultiplePrescriptionEntity?

    @Query("SELECT * FROM multiple_prescriptions WHERE multiplePrescriptionId = :id")
    fun observeById(id: Long): Flow<ErpMultiplePrescriptionEntity?>

    @Query("SELECT * FROM multiple_prescriptions ORDER BY multiplePrescriptionId ASC LIMIT :limit OFFSET :offset")
    suspend fun list(limit: Int = 50, offset: Int = 0): List<ErpMultiplePrescriptionEntity>

    @Delete
    suspend fun delete(entity: ErpMultiplePrescriptionEntity)

    @Query("DELETE FROM multiple_prescriptions WHERE multiplePrescriptionId = :id")
    suspend fun deleteById(id: Long)

    @Query("DELETE FROM multiple_prescriptions")
    suspend fun clear()
}
