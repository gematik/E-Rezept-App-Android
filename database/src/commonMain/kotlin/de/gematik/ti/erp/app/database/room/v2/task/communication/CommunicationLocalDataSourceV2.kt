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
import de.gematik.ti.erp.app.communication.model.payload.CommunicationPayloadErpModel
import de.gematik.ti.erp.app.database.api.CommunicationLocalDataSource
import de.gematik.ti.erp.app.database.room.v2.task.mappers.toErpCommunicationEntity
import de.gematik.ti.erp.app.database.room.v2.task.mappers.toErpModel
import de.gematik.ti.erp.app.fhir.FhirCommunicationBundleErpModel
import de.gematik.ti.erp.app.fhir.communication.model.FhirCommunicationEntryErpModel
import de.gematik.ti.erp.app.fhir.communication.model.FhirDispenseCommunicationEntryErpModel
import de.gematik.ti.erp.app.fhir.communication.model.FhirReplyCommunicationEntryErpModel
import de.gematik.ti.erp.app.fhir.communication.parser.CommunicationPayloadParser
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.datetime.Clock.System
import kotlinx.datetime.Instant

class CommunicationLocalDataSourceV2(
    private val dao: CommunicationDao
) : CommunicationLocalDataSource {

    override suspend fun saveLocalCommunication(taskId: String, pharmacyId: String, transactionId: String) {
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

    /**
     * Persists all communications of a synced FHIR bundle.
     *
     * Every communication has to be linked to an order (via `orderId`) and, where possible, to a task, because
     * the messages UI loads conversations by order. The server does not always send an OrderID (e.g. for replies
     * of some pharmacy systems), so the ids are resolved here with the following fallbacks:
     *
     * - Dispense requests (sent by the user): see [toDispenseRequestEntity].
     * - Replies (sent by the pharmacy): see [toReplyEntity] and [resolveReplyOrderId].
     *
     * @return the number of persisted communications
     */
    override suspend fun saveCommunications(entities: FhirCommunicationBundleErpModel): Int {
        if (entities.messages.isEmpty()) return 0
        val batch = SyncBatch(entities.messages)
        val mapped = entities.messages.map { message ->
            when (message) {
                is FhirReplyCommunicationEntryErpModel -> message.toReplyEntity(batch)
                is FhirDispenseCommunicationEntryErpModel -> message.toDispenseRequestEntity(batch)
            }
        }
        dao.upsertAll(mapped)
        return mapped.size
    }

    /**
     * Maps a pharmacy reply to its entity.
     *
     * 1. The order is resolved first from the OrderID or the transactionID ([resolveOrderIdByTransactionId]).
     * 2. The task is then resolved, using that order if the reply does not reference a task itself.
     * 3. If no order is known yet, the task based fallbacks of [resolveReplyOrderId] are used.
     */
    private suspend fun FhirReplyCommunicationEntryErpModel.toReplyEntity(batch: SyncBatch): ErpCommunicationEntity {
        val payload = payload?.let { CommunicationPayloadParser.extract(it, isRequest = false) }
        val knownOrderId = orderId?.ifEmpty { null } ?: resolveOrderIdByTransactionId(payload?.transactionID, batch)
        val task = resolveTaskContext(resolveTaskId(knownOrderId, batch))
        val telematikId = sender?.identifier.orEmpty()
        val orderId = knownOrderId ?: resolveReplyOrderId(task.taskId, telematikId, batch)

        return toEntity(
            task = task,
            orderId = orderId,
            profile = CommunicationProfileV1.ErxCommunicationReply,
            payload = payload
        )
    }

    /**
     * Maps a dispense request to its entity. Dispense requests are created by the app and always carry
     * their OrderID, so only the task may need to be resolved (via that order).
     */
    private suspend fun FhirDispenseCommunicationEntryErpModel.toDispenseRequestEntity(batch: SyncBatch): ErpCommunicationEntity =
        toEntity(
            task = resolveTaskContext(resolveTaskId(orderId, batch)),
            orderId = orderId.orEmpty(),
            profile = CommunicationProfileV1.ErxCommunicationDispReq,
            payload = payload?.let { CommunicationPayloadParser.extract(it, isRequest = true) }
        )

    /**
     * Resolves the order of a reply that has no OrderID through its [transactionId].
     *
     * Dispense requests are sent with `transactionID = orderId`, so a reply carrying the same transactionID
     * belongs to that order. The current sync batch is checked first, then the dispense requests already stored.
     * Blank transactionIDs are ignored, since they would match every other communication without one.
     *
     * @return the orderId of the matching dispense request, or null if there is none
     */
    private suspend fun resolveOrderIdByTransactionId(transactionId: String?, batch: SyncBatch): String? {
        if (transactionId.isNullOrBlank()) return null
        return batch.dispenseRequestOrderIdsByTransactionId[transactionId]
            ?: dao.getOrderIdByTransactionId(transactionId, CommunicationProfileV1.ErxCommunicationDispReq)
    }

    /**
     * Resolves the order of a reply that could not be linked by OrderID or transactionID, in this order:
     *
     * 1. a dispense request for the same task in the current sync batch
     * 2. a stored dispense request for the same task
     * 3. any stored communication for the same task and pharmacy
     * 4. [syntheticOrderId] - happens e.g. when the dispense request was created (and later removed
     *    server-side) on a different device before this device ever synced. The synthetic id is stable, so
     *    all replies for the same task from the same pharmacy are still grouped into one openable order,
     *    instead of being persisted with a blank orderId that breaks order lookups.
     */
    private suspend fun resolveReplyOrderId(taskId: String, telematikId: String, batch: SyncBatch): String {
        if (taskId.isEmpty()) return syntheticOrderId(taskId, telematikId)
        return batch.dispenseRequestOrderIdForTask(taskId)
            ?: dao.getOrderIdByTaskIdAndProfile(taskId, CommunicationProfileV1.ErxCommunicationDispReq)
            ?: dao.getOrderIdByTaskIdAndTelematikId(taskId, telematikId)
            ?: syntheticOrderId(taskId, telematikId)
    }

    /**
     * Returns the taskId of this communication. If it references no task, the task is taken from another
     * communication of [orderId] - first from the current sync batch, then from the database.
     *
     * @return the taskId, or an empty string if it cannot be resolved
     */
    private suspend fun FhirCommunicationEntryErpModel.resolveTaskId(orderId: String?, batch: SyncBatch): String =
        taskId
            ?: orderId?.let { batch.taskIdForOrder(it) ?: dao.getTaskIdByOrderId(it) }
            ?: ""

    /**
     * Loads the profile and insurance the communication of [taskId] belongs to.
     * Without a task (empty [taskId]) both stay unknown.
     */
    private suspend fun resolveTaskContext(taskId: String): TaskContext {
        if (taskId.isEmpty()) return TaskContext(taskId = taskId, profileId = "", insuranceId = null)
        val insuranceId = dao.getInsuranceIdByTaskId(taskId)
        val profileId = dao.getTaskByTaskId(taskId)?.parentProfileId ?: insuranceId ?: ""
        return TaskContext(taskId = taskId, profileId = profileId, insuranceId = insuranceId)
    }

    private fun FhirCommunicationEntryErpModel.toEntity(
        task: TaskContext,
        orderId: String,
        profile: CommunicationProfileV1,
        payload: CommunicationPayloadErpModel?
    ) = ErpCommunicationEntity(
        communicationId = id,
        orderId = orderId,
        taskId = task.taskId,
        profileId = task.profileId,
        telematikId = sender?.identifier.orEmpty(),
        kvnr = recipient?.identifier.orEmpty(),
        consumed = false,
        payload = payload,
        profile = profile,
        recipient = recipient?.identifier.orEmpty(),
        insuranceId = task.insuranceId,
        timeStamp = sent?.value ?: System.now(),
        pharmacyName = pharmacyName
    )

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

    /** The task a communication belongs to, together with the profile and insurance resolved from it. */
    private data class TaskContext(
        val taskId: String,
        val profileId: String,
        val insuranceId: String?
    )

    /**
     * The communications of one sync. Communications of the same order often arrive together, so the batch
     * is searched before the database when resolving order and task ids.
     */
    private class SyncBatch(private val messages: List<FhirCommunicationEntryErpModel>) {

        /** OrderIds of the dispense requests in this batch, keyed by their (non blank) payload transactionID. */
        val dispenseRequestOrderIdsByTransactionId: Map<String, String> by lazy {
            messages
                .filterIsInstance<FhirDispenseCommunicationEntryErpModel>()
                .mapNotNull { dispenseRequest ->
                    val orderId = dispenseRequest.orderId?.ifEmpty { null } ?: return@mapNotNull null
                    val transactionId = dispenseRequest.payload
                        ?.let { CommunicationPayloadParser.extract(it, isRequest = true)?.transactionID }
                        ?.takeUnless { it.isBlank() }
                        ?: return@mapNotNull null
                    transactionId to orderId
                }
                .toMap()
        }

        fun taskIdForOrder(orderId: String): String? =
            messages.firstOrNull { it.orderId == orderId && !it.taskId.isNullOrEmpty() }?.taskId

        fun dispenseRequestOrderIdForTask(taskId: String): String? =
            messages.firstOrNull {
                it is FhirDispenseCommunicationEntryErpModel && it.taskId == taskId && !it.orderId.isNullOrEmpty()
            }?.orderId
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
