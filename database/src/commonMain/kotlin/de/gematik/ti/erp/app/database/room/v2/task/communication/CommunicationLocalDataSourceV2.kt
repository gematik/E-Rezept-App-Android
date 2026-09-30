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

import de.gematik.ti.erp.app.communication.model.CommunicationErpModel
import de.gematik.ti.erp.app.communication.model.CommunicationProfileV1
import de.gematik.ti.erp.app.database.api.CommunicationLocalDataSource
import de.gematik.ti.erp.app.database.room.v2.task.mappers.toErpCommunicationEntity
import de.gematik.ti.erp.app.database.room.v2.task.mappers.toErpModel
import de.gematik.ti.erp.app.fhir.FhirCommunicationBundleErpModel
import de.gematik.ti.erp.app.fhir.communication.model.FhirDispenseCommunicationEntryErpModel
import de.gematik.ti.erp.app.fhir.communication.model.FhirReplyCommunicationEntryErpModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.datetime.Clock.System
import kotlinx.datetime.Instant

class CommunicationLocalDataSourceV2(
    private val dao: CommunicationDao
) : CommunicationLocalDataSource {

    override suspend fun saveLocalCommunication(taskId: String, pharmacyId: String, transactionId: String) {
        // TODO CommResV3 CleanUp of Migration: DB Insurance is the wrong name here, should be insurant or profileId
        val insuranceId = dao.getInsuranceIdByTaskId(taskId)
        val task = dao.getTaskByTaskId(taskId) ?: return
        val profileId = task.parentProfileId ?: return
        val entity = ErpCommunicationEntity(
            communicationId = transactionId,
            orderId = "",
            taskId = taskId,
            profileId = profileId,
            telematikId = pharmacyId,
            kvnr = "",
            consumed = false,
            payload = null,
            profile = CommunicationProfileV1.ErxCommunicationDispReq,
            insuranceId = insuranceId,
            timeStamp = System.now()
        )
        dao.upsertAll(listOf(entity))
    }

    override suspend fun saveCommunications(communicationModels: List<CommunicationErpModel>): Int {
        if (communicationModels.isEmpty()) return 0
        val mapped = communicationModels.map { communication ->
            communication.toErpCommunicationEntity()
        }
        dao.upsertAll(mapped)
        return mapped.size
    }

    override suspend fun saveCommunications(entities: FhirCommunicationBundleErpModel): Int {
        if (entities.messages.isEmpty()) return 0
        val mapped = entities.messages.mapNotNull { message ->
            val taskId = message.taskId ?: return@mapNotNull null
            val task = dao.getTaskByTaskId(taskId) ?: return@mapNotNull null
            val insuranceId = dao.getInsuranceIdByTaskId(taskId) ?: return@mapNotNull null
            val profileId = task.parentProfileId ?: return@mapNotNull null
            when (message) {
                is FhirReplyCommunicationEntryErpModel -> {
                    val telematikId = message.sender?.identifier ?: ""
                    var orderId = message.orderId?.ifEmpty { null }
                    if (orderId == null) {
                        // Check if orderId is in the list we received
                        orderId = entities.messages.firstOrNull {
                            it is FhirDispenseCommunicationEntryErpModel &&
                                it.taskId == taskId &&
                                !it.orderId.isNullOrEmpty()
                        }?.orderId
                    }
                    if (orderId == null) {
                        orderId = dao.getOrderIdByTaskIdAndProfile(taskId, CommunicationProfileV1.ErxCommunicationDispReq)
                    }
                    if (orderId == null) {
                        orderId = dao.getOrderIdByTaskIdAndTelematikId(taskId, telematikId)
                    }
                    if (orderId == null) {
                        // No real OrderID could be resolved - this happens e.g. when the
                        // corresponding dispense-request was created (and later removed server-side)
                        // on a different device before this device ever synced. Fall back to a stable,
                        // deterministic synthetic orderId built from taskId+telematikId (the same key
                        // already used by getOrderIdByTaskIdAndTelematikId above) so that all replies
                        // for the same task from the same pharmacy keep being grouped into one openable
                        // order, instead of being persisted with a blank orderId that breaks order lookups.
                        orderId = syntheticOrderId(taskId, telematikId)
                    }
                    message.toErpCommunicationEntity(task, profileId, orderId, insuranceId)
                }

                is FhirDispenseCommunicationEntryErpModel -> {
                    message.toErpCommunicationEntity(task, profileId, insuranceId)
                }
            }
        }
        dao.upsertAll(mapped)
        return mapped.size
    }

    override fun loadDispReqCommunications(orderId: String): Flow<List<CommunicationErpModel>> {
        return dao.observeByOrderAndProfile(orderId, CommunicationProfileV1.ErxCommunicationDispReq)
            .map { list -> list.map { it.toErpModel() } }
    }

    override fun loadDispReqCommunicationsByProfileId(profileId: String): Flow<List<CommunicationErpModel>> =
        dao.observeByInsuranceAndProfileSorted(profileId, CommunicationProfileV1.ErxCommunicationDispReq)
            .map { list -> list.map { it.toErpModel() } }

    override fun loadDispReqCommunicationsByTaskId(taskId: String): Flow<List<CommunicationErpModel>> =
        dao.observeByTaskIdAndProfile(taskId, CommunicationProfileV1.ErxCommunicationDispReq)
            .map { list -> list.map { it.toErpModel() } }

    override fun loadRepliedCommunications(taskIds: List<String>, telematikId: String?): Flow<List<CommunicationErpModel>> {
        if (taskIds.isEmpty()) return flowOf(emptyList())
        val flow = if (telematikId.isNullOrEmpty()) {
            dao.observeRepliesForTaskIds(taskIds, CommunicationProfileV1.ErxCommunicationReply)
        } else {
            dao.observeRepliesForTaskIdsFromSender(taskIds, telematikId, CommunicationProfileV1.ErxCommunicationReply)
        }
        return flow.map { entities ->
            entities.map { it.toErpModel() }
        }
    }

    override fun loadRepliedCommunications(orderId: String?): Flow<List<CommunicationErpModel>> {
        if (orderId.isNullOrEmpty()) return flowOf(emptyList())
        return dao
            .observeRepliesForOrderIdFromSender(orderId, CommunicationProfileV1.ErxCommunicationReply)
            .map { entities -> entities.map { it.toErpModel() } }
    }

    override fun loadRepliedCommunications(orderId: String, telematikId: String): Flow<List<CommunicationErpModel>> {
        return loadRepliedCommunications(orderId = orderId)
    }

    override fun loadRepliedCommunicationsByProfileId(profileId: String): Flow<List<CommunicationErpModel>> =
        dao.observeByInsuranceAndProfileSorted(profileId, CommunicationProfileV1.ErxCommunicationReply)
            .map { list -> list.map { it.toErpModel() } }

    override fun loadAllRepliedCommunications(taskIds: List<String>): Flow<List<CommunicationErpModel>> {
        if (taskIds.isEmpty()) return flowOf(emptyList())
        return dao.observeRepliesForTaskIds(taskIds, CommunicationProfileV1.ErxCommunicationReply)
            .map { list -> list.map { it.toErpModel() } }
    }

    override fun hasUnreadDispenseMessage(taskIds: List<String>, orderId: String): Flow<Boolean> =
        if (taskIds.isEmpty()) flowOf(false) else dao.observeUnreadCountByOrderAndTaskIds(taskIds, orderId).map { it > 0 }

    override fun hasUnreadDispenseMessage(profileId: String): Flow<Boolean> =
        dao.observeUnreadCountByInsurance(profileId).map { it > 0 }

    override fun unreadMessagesCount(): Flow<Long> =
        dao.observeAll().map { list ->
            val dispReq = list.filter { it.profile == CommunicationProfileV1.ErxCommunicationDispReq }
            val replies = list.filter { it.profile == CommunicationProfileV1.ErxCommunicationReply }
            val unreadDispReqCount = dispReq
                .groupBy { it.orderId }
                .count { (_, dispReqs) -> dispReqs.any { !it.consumed } }
                .toLong()
            val uniqueUnconsumedReplies = replies
                .filter { !it.consumed }
                .distinctBy { it.taskId to it.payload }
                .size.toLong()
            unreadDispReqCount + uniqueUnconsumedReplies
        }

    override fun getAllUnreadMessages(): Flow<List<CommunicationErpModel>> =
        dao.observeUnread().map { list ->
            val dispReqMessages = list.filter { it.profile == CommunicationProfileV1.ErxCommunicationDispReq }
            val uniqueDispReqOrders = dispReqMessages.distinctBy { it.orderId }
            val replyMessages = list.filter { it.profile == CommunicationProfileV1.ErxCommunicationReply }
            val uniqueReplies = replyMessages.distinctBy { Triple(it.taskId, it.payload, it.telematikId) }
            (uniqueDispReqOrders + uniqueReplies).map { it.toErpModel() }
        }

    override fun unreadPrescriptionsInAllOrders(profileId: String): Flow<Long> =
        dao.observeUnreadCountByInsurance(profileId)

    override fun taskIdsByOrder(orderId: String): Flow<List<String>> =
        dao.observeDistinctTaskIdsByOrder(orderId)

    override fun getProfileIdByOrderId(orderId: String): Flow<String?> =
        dao.observeParentProfileIdByOrder(orderId)

    override fun getProfileIdByTaskId(taskId: String): Flow<String?> =
        dao.observeParentProfileIdByTaskId(taskId)

    override suspend fun setCommunicationStatus(communicationId: String, consumed: Boolean) {
        val entity = dao.getById(communicationId) ?: return

        // Find duplicates in Kotlin to avoid SQLite TypeConverter issues when comparing complex payloads
        val duplicates = dao.getByTaskId(entity.taskId).filter {
            it.orderId == entity.orderId &&
                it.telematikId == entity.telematikId &&
                it.recipient == entity.recipient &&
                it.payload == entity.payload
        }

        if (duplicates.isNotEmpty()) {
            duplicates.forEach { duplicate ->
                dao.updateConsumedById(duplicate.communicationId, consumed)
            }
        } else {
            // Fallback just in case
            dao.updateConsumedById(communicationId, consumed)
        }
    }

    override suspend fun updatePharmacyName(communicationId: String, pharmacyName: String) {
        dao.updatePharmacyName(communicationId, pharmacyName)
    }

    override fun latestCommunicationTimestamp(profileId: String): Flow<Instant?> =
        dao.observeMaxTimestampByInsurance(profileId)

    override fun hasUnreadRepliedMessages(taskIds: List<String>, telematikId: String?): Flow<Boolean> {
        if (taskIds.isEmpty()) return flowOf(false)
        val flow = if (telematikId.isNullOrEmpty()) {
            dao.observeUnreadRepliesCount(taskIds, CommunicationProfileV1.ErxCommunicationReply)
        } else {
            dao.observeUnreadRepliesCountFromSender(taskIds, telematikId, CommunicationProfileV1.ErxCommunicationReply)
        }
        return flow.map { it > 0 }
    }

    private companion object {
        /**
         * Deterministic synthetic orderId used when no real OrderID can be resolved for a
         * reply (e.g. the dispense-request was created on another device and is unknown/removed here).
         * Built from the same taskId+telematikId key already used by [CommunicationDao.getOrderIdByTaskIdAndTelematikId]
         * so replies for the same task from the same pharmacy keep being grouped into one openable order.
         */
        fun syntheticOrderId(taskId: String, telematikId: String): String = "$taskId:$telematikId"
    }
}
