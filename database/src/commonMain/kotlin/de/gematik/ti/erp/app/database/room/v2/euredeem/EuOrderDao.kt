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

package de.gematik.ti.erp.app.database.room.v2.euredeem

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

@Dao
interface EuOrderDao {

    @Transaction
    @Query("SELECT * FROM eu_orders WHERE orderId = :orderId")
    fun observeEuOrder(orderId: String): Flow<EuOrderWithRelations?>

    @Transaction
    @Query("SELECT * FROM eu_orders ORDER BY lastModifiedAt DESC, createdAt DESC")
    fun observeAllEuOrders(): Flow<List<EuOrderWithRelations>>

    @Query("SELECT * FROM eu_access_codes WHERE profileId = :profileId AND countryCode = :countryCode")
    fun observeAccessCodes(profileId: String, countryCode: String): Flow<List<EuAccessCodeEntity>>

    @Transaction
    @Query("SELECT * FROM eu_orders WHERE profileId = :profileId AND countryCode = :countryCode")
    fun observeOrdersForProfileAndCountry(profileId: String, countryCode: String): Flow<List<EuOrderWithRelations>>

    @Transaction
    @Query("SELECT * FROM eu_orders WHERE profileId = :profileId")
    suspend fun getOrdersByProfileIdDirect(profileId: String): List<EuOrderWithRelations>

    @Query("SELECT * FROM eu_access_codes WHERE profileId = :profileId")
    suspend fun getAccessCodesByProfileId(profileId: String): List<EuAccessCodeEntity>

    @Query("DELETE FROM eu_access_codes WHERE profileId = :profileId")
    suspend fun deleteAccessCodeByProfileId(profileId: String)

    @Query("DELETE FROM eu_access_codes WHERE accessCode = :accessCode")
    suspend fun deleteAccessCode(accessCode: String)

    @Query("UPDATE eu_orders SET euAccessCodeCode = NULL WHERE euAccessCodeCode = :accessCode")
    suspend fun removeAccessCodeFromOrders(accessCode: String)

    @Upsert
    suspend fun upsertAccessCode(entity: EuAccessCodeEntity)

    @Upsert
    suspend fun upsertOrder(entity: EuOrderEntity)

    @Query("SELECT * FROM eu_orders WHERE orderId = :orderId")
    suspend fun getOrderById(orderId: String): EuOrderEntity?

    @Query("SELECT * FROM eu_access_codes WHERE accessCode = :accessCode")
    suspend fun getAccessCodeByCode(accessCode: String): EuAccessCodeEntity?

    @Query("SELECT * FROM eu_access_codes WHERE accessCode = :accessCode")
    fun observeAccessCodeByCode(accessCode: String): Flow<EuAccessCodeEntity?>

    @Upsert
    suspend fun insertEvents(entities: List<EuTaskEventEntity>)

    @Query("UPDATE eu_task_events SET isUnread = :isUnread WHERE id = :id")
    suspend fun updateEventUnreadStatus(id: String, isUnread: Boolean)
}
