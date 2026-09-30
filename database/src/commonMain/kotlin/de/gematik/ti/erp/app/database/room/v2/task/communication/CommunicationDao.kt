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

package de.gematik.ti.erp.app.database.room.v2.task.communication

import androidx.room.Dao
import androidx.room.Query
import androidx.room.TypeConverters
import androidx.room.Upsert
import de.gematik.ti.erp.app.communication.model.CommunicationProfileV1
import de.gematik.ti.erp.app.database.room.v2.task.prescription.ErpTaskEntity
import de.gematik.ti.erp.app.database.room.v2.task.util.CommunicationPayloadConverter
import de.gematik.ti.erp.app.database.room.v2.task.util.CommunicationProfileConverter
import de.gematik.ti.erp.app.database.room.v2.task.util.InstantConverter
import kotlinx.coroutines.flow.Flow
import kotlinx.datetime.Instant

@Dao
@TypeConverters(InstantConverter::class, CommunicationProfileConverter::class, CommunicationPayloadConverter::class)
interface CommunicationDao {
    @Upsert
    suspend fun upsertAll(items: List<ErpCommunicationEntity>)

    @Query("DELETE FROM communications WHERE profileId = :profileId")
    suspend fun deleteByProfileId(profileId: String)

    @Query("SELECT * FROM communications WHERE taskId = :taskId")
    suspend fun getByTaskId(taskId: String): List<ErpCommunicationEntity>

    @Query("SELECT * FROM communications WHERE communicationId = :id LIMIT 1")
    suspend fun getById(id: String): ErpCommunicationEntity?

    @Query("SELECT orderId FROM communications WHERE taskId = :taskId AND telematikId = :telematikId AND orderId != '' LIMIT 1")
    suspend fun getOrderIdByTaskIdAndTelematikId(taskId: String, telematikId: String): String?

    @Query("SELECT orderId FROM communications WHERE taskId = :taskId AND profile = :profile AND orderId != '' LIMIT 1")
    suspend fun getOrderIdByTaskIdAndProfile(taskId: String, profile: CommunicationProfileV1): String?

    // Observe by order and profile (e.g., ErxCommunicationDispReq)
    @Query("SELECT * FROM communications WHERE orderId = :orderId AND profile = :profile")
    fun observeByOrderAndProfile(orderId: String, profile: CommunicationProfileV1): Flow<List<ErpCommunicationEntity>>

    @Query("SELECT * FROM communications WHERE taskId = :taskId AND profile = :profile")
    fun observeByTaskIdAndProfile(taskId: String, profile: CommunicationProfileV1): Flow<List<ErpCommunicationEntity>>

    // Observe by insurance/profile sorted by timestamp desc
    // TODO CommResV3 CleanUp of Migration: DB Insurance is the wrong name here, should be insurant or profileId
    @Query(
        """
        SELECT * FROM communications
        WHERE insuranceId = :insuranceId AND profile = :profile
        ORDER BY timeStamp DESC
        """
    )
    fun observeByInsuranceAndProfileSorted(insuranceId: String, profile: CommunicationProfileV1): Flow<List<ErpCommunicationEntity>>

    // Observe replies for multiple taskIds (IN clause), optionally filter by sender
    @Query(
        """
        SELECT * FROM communications
        WHERE taskId IN (:taskIds)
          AND profile = :profile
        ORDER BY timeStamp DESC
        """
    )
    fun observeRepliesForTaskIds(taskIds: List<String>, profile: CommunicationProfileV1): Flow<List<ErpCommunicationEntity>>

    @Query(
        """
        SELECT * FROM communications
        WHERE taskId IN (:taskIds)
          AND profile = :profile
          AND telematikId = :sender
        ORDER BY timeStamp DESC
        """
    )
    fun observeRepliesForTaskIdsFromSender(taskIds: List<String>, sender: String, profile: CommunicationProfileV1): Flow<List<ErpCommunicationEntity>>

    @Query(
        """
        SELECT * FROM communications
        WHERE orderId IN (:orderId)
          AND profile = :profile
        ORDER BY timeStamp DESC
        """
    )
    fun observeRepliesForOrderIdFromSender(orderId: String, profile: CommunicationProfileV1): Flow<List<ErpCommunicationEntity>>

    // Unread checks/counts
    @Query(
        """
        SELECT COUNT(*) FROM communications
        WHERE taskId IN (:taskIds)
          AND orderId = :orderId
          AND consumed = 0
        """
    )
    fun observeUnreadCountByOrderAndTaskIds(taskIds: List<String>, orderId: String): Flow<Long>

    @Query("SELECT COUNT(*) FROM communications WHERE consumed = 0 AND insuranceId = :insuranceId")
    fun observeUnreadCountByInsurance(insuranceId: String): Flow<Long>

    // Stream all and unread
    @Query("SELECT * FROM communications")
    fun observeAll(): Flow<List<ErpCommunicationEntity>>

    @Query("SELECT * FROM communications WHERE consumed = 0")
    fun observeUnread(): Flow<List<ErpCommunicationEntity>>

    // Distinct taskIds by order
    @Query("SELECT DISTINCT taskId FROM communications WHERE orderId = :orderId")
    fun observeDistinctTaskIdsByOrder(orderId: String): Flow<List<String>>

    // Parent profile id by order
    @Query(
        """
        SELECT t.parentProfileId 
        FROM tasks t 
        JOIN communications c ON t.taskId = c.taskId 
        WHERE c.orderId = :orderId 
        LIMIT 1
    """
    )
    fun observeParentProfileIdByOrder(orderId: String): Flow<String?>

    @Query("SELECT parentProfileId FROM tasks WHERE taskId = :taskId LIMIT 1")
    fun observeParentProfileIdByTaskId(taskId: String): Flow<String?>

    // Update consumed for a group (no recipient column in Room v2 schema)
    @Query(
        """
        UPDATE communications
        SET consumed = :consumed
        WHERE orderId = :orderId
          AND taskId = :taskId
          AND telematikId = :sender
          AND recipient = :recipient
        """
    )
    suspend fun updateConsumedForGroup(
        orderId: String,
        taskId: String,
        sender: String,
        recipient: String,
        consumed: Boolean
    ): Int

    @Query("UPDATE communications SET consumed = :consumed WHERE communicationId = :communicationId")
    suspend fun updateConsumedById(communicationId: String, consumed: Boolean): Int

    @Query("UPDATE communications SET pharmacyName = :pharmacyName WHERE communicationId = :communicationId")
    suspend fun updatePharmacyName(communicationId: String, pharmacyName: String): Int

    // Latest timestamp for an insurance/profile
    @Query("SELECT MAX(timeStamp) FROM communications WHERE insuranceId = :insuranceId")
    fun observeMaxTimestampByInsurance(insuranceId: String): Flow<Instant?>

    // TODO CommResV3 CleanUp of Migration: DB Insurance is the wrong name here, should be insurant or profileId
    @Query("SELECT parentProfileId FROM tasks WHERE taskId = :taskId LIMIT 1")
    suspend fun getInsuranceIdByTaskId(taskId: String): String?

    @Query("SELECT * FROM tasks WHERE taskId = :taskId LIMIT 1")
    suspend fun getTaskByTaskId(taskId: String): ErpTaskEntity?

    /*
    @Query("SELECT p.insuranceIdentifier FROM tasks t INNER JOIN patient p ON t.patientId = p.patientId WHERE t.taskId = :taskId")
    suspend fun getInsurantIdByTaskId(taskId: String): String?


     */
    // Unread replies for multiple taskIds (optionally by sender)
    @Query(
        """
        SELECT COUNT(*) FROM communications
        WHERE taskId IN (:taskIds)
          AND profile = :profile
          AND consumed = 0
        """
    )
    fun observeUnreadRepliesCount(taskIds: List<String>, profile: CommunicationProfileV1): Flow<Long>

    @Query(
        """
        SELECT COUNT(*) FROM communications
        WHERE taskId IN (:taskIds)
          AND profile = :profile
          AND telematikId = :sender
          AND consumed = 0
        """
    )
    fun observeUnreadRepliesCountFromSender(taskIds: List<String>, sender: String, profile: CommunicationProfileV1): Flow<Long>
}
