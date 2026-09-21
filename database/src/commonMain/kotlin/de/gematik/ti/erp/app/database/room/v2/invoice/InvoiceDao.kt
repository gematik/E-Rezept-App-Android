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

package de.gematik.ti.erp.app.database.room.v2.invoice

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

@Dao
interface InvoiceDao {
    @Upsert
    suspend fun upsertAll(items: List<InvoiceRoomEntity>)

    @Upsert
    suspend fun upsert(item: InvoiceRoomEntity)

    @Query("SELECT * FROM invoices WHERE profileId = :profileId")
    suspend fun getByProfile(profileId: String): List<InvoiceRoomEntity>

    @Query("SELECT * FROM invoices WHERE profileId = :profileId")
    fun observeByProfile(profileId: String): Flow<List<InvoiceRoomEntity>>

    @Query("SELECT * FROM invoices WHERE taskId = :taskId LIMIT 1")
    suspend fun getByTaskId(taskId: String): InvoiceRoomEntity?

    @Query("SELECT * FROM invoices WHERE taskId = :taskId LIMIT 1")
    fun observeByTaskId(taskId: String): Flow<InvoiceRoomEntity?>

    @Query("SELECT MAX(timestampEpochMillis) FROM invoices WHERE profileId = :profileId")
    fun latestInvoiceModifiedTimestamp(profileId: String): Flow<Long?>

    @Query("SELECT * FROM invoices WHERE consumed = 0")
    fun observeUnread(): Flow<List<InvoiceRoomEntity>>

    @Query("UPDATE invoices SET consumed = :consumed WHERE taskId = :taskId")
    suspend fun updateConsumedStatus(taskId: String, consumed: Boolean)

    @Query("SELECT COUNT(*) FROM invoices WHERE taskId IN (:taskIds) AND consumed = 0")
    fun countUnread(taskIds: List<String>): Flow<Int>

    @Query("DELETE FROM invoices WHERE taskId = :taskId")
    suspend fun deleteByTaskId(taskId: String)

    @Query("DELETE FROM invoices WHERE profileId = :profileId")
    suspend fun deleteByProfileId(profileId: String)

    @Query("DELETE FROM invoices")
    suspend fun clearAll()
}
