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
package de.gematik.ti.erp.app.database.realm.v1.task.communication

import de.gematik.ti.erp.app.communication.model.CommunicationErpModel
import de.gematik.ti.erp.app.communication.model.CommunicationProfileV1
import de.gematik.ti.erp.app.database.api.CommunicationLocalDataSource
import de.gematik.ti.erp.app.database.realm.utils.queryFirst
import de.gematik.ti.erp.app.database.realm.utils.safeWrite
import de.gematik.ti.erp.app.database.realm.utils.toInstant
import de.gematik.ti.erp.app.database.realm.utils.toRealmInstant
import de.gematik.ti.erp.app.database.realm.v1.messages.mapper.CommunicationDatabaseMappers.toDatabaseModel
import de.gematik.ti.erp.app.database.realm.v1.task.entity.CommunicationEntityV1
import de.gematik.ti.erp.app.database.realm.v1.task.entity.ScannedTaskEntityV1
import de.gematik.ti.erp.app.database.realm.v1.task.entity.SyncedTaskEntityV1
import de.gematik.ti.erp.app.database.room.v2.task.mappers.toErpModel
import de.gematik.ti.erp.app.fhir.FhirCommunicationBundleErpModel
import de.gematik.ti.erp.app.fhir.communication.model.FhirDispenseCommunicationEntryErpModel
import de.gematik.ti.erp.app.fhir.communication.model.FhirReplyCommunicationEntryErpModel
import io.realm.kotlin.MutableRealm
import io.realm.kotlin.Realm
import io.realm.kotlin.ext.query
import io.realm.kotlin.query.Sort
import io.realm.kotlin.query.max
import io.realm.kotlin.types.RealmInstant
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.datetime.Clock

internal class CommunicationLocalDataSourceV1(
    private val realm: Realm
) : CommunicationLocalDataSource {

    override suspend fun saveLocalCommunication(taskId: String, pharmacyId: String, transactionId: String) {
        realm.write<Unit> {
            val entity = CommunicationEntityV1().apply {
                this.profile = CommunicationProfileV1.ErxCommunicationDispReq
                this.taskId = taskId
                this.communicationId = transactionId
                this.sentOn = Clock.System.now().toRealmInstant()
                this.sender = pharmacyId
                this.consumed = false
            }
            queryFirst<ScannedTaskEntityV1>("taskId = $0", taskId)?.let { scannedTask ->
                scannedTask.communications += copyToRealm(entity)
            }
        }
    }

    override suspend fun saveCommunications(communicationModel: FhirCommunicationBundleErpModel): Int {
        return realm.safeWrite {
            communicationModel.messages.sumOf { message ->
                saveCommunicationToDatabase(
                    when (message) {
                        is FhirReplyCommunicationEntryErpModel -> message.toDatabaseModel()
                        is FhirDispenseCommunicationEntryErpModel -> message.toDatabaseModel()
                    }
                )
            }
        }
    }

    override suspend fun saveCommunications(communicationModels: List<CommunicationErpModel>): Int {
        return realm.safeWrite {
            communicationModels.sumOf { message ->
                saveCommunicationToDatabase(message.toDatabaseModel()) ?: 0
            }
        }
    }

    private fun MutableRealm.saveCommunicationToDatabase(communication: CommunicationEntityV1): Int {
        val syncedTask = queryFirst<SyncedTaskEntityV1>("taskId = $0", communication.taskId) ?: return 0
        communication.parent = syncedTask
        syncedTask.communications += copyToRealm(communication)
        return 1
    }

    override fun loadDispReqCommunications(orderId: String): Flow<List<CommunicationErpModel>> {
        return realm.query<CommunicationEntityV1>(
            "orderId = $0 && _profile = $1",
            orderId,
            CommunicationProfileV1.ErxCommunicationDispReq.toString()
        )
            .asFlow()
            .map { communication -> communication.list.map { it.toErpModel() } }
    }

    override fun loadDispReqCommunicationsByProfileId(profileId: String): Flow<List<CommunicationErpModel>> =
        realm.query<CommunicationEntityV1>(
            "parent.parent.id = $0 && _profile = $1",
            profileId,
            CommunicationProfileV1.ErxCommunicationDispReq.toString()
        )
            .sort("sentOn", Sort.DESCENDING)
            .asFlow()
            .map { communications -> communications.list.map { it.toErpModel() } }

    override fun loadRepliedCommunications(orderId: String?): Flow<List<CommunicationErpModel>> {
        return realm.query<CommunicationEntityV1>(
            "orderId = $0 && _profile = $1",
            orderId,
            CommunicationProfileV1.ErxCommunicationReply.toString()
        )
            .asFlow()
            .map { communication -> communication.list.map { it.toErpModel() } }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    override fun loadRepliedCommunications(orderId: String, telematikId: String): Flow<List<CommunicationErpModel>> {
        return taskIdsByOrder(orderId).flatMapLatest { taskIds ->
            loadRepliedCommunications(taskIds = taskIds, telematikId = telematikId)
        }
    }

    override fun loadRepliedCommunicationsByProfileId(profileId: String): Flow<List<CommunicationErpModel>> =
        realm.query<CommunicationEntityV1>(
            "parent.parent.id = $0 && _profile = $1",
            profileId,
            CommunicationProfileV1.ErxCommunicationReply.toString()
        )
            .sort("sentOn", Sort.DESCENDING)
            .asFlow()
            .map { communications -> communications.list.map { it.toErpModel() } }

    override fun loadDispReqCommunicationsByTaskId(taskId: String): Flow<List<CommunicationErpModel>> =
        realm.query<CommunicationEntityV1>(
            "taskId = $0 && _profile = $1",
            taskId,
            CommunicationProfileV1.ErxCommunicationDispReq.toString()
        )
            .asFlow()
            .map { communications -> communications.list.map { it.toErpModel() } }

    override fun loadRepliedCommunications(taskIds: List<String>, telematikId: String?): Flow<List<CommunicationErpModel>> {
        if (taskIds.isEmpty()) return flowOf(emptyList())

        var q = realm.query<CommunicationEntityV1>(orQuerySubstring("parent.taskId", taskIds.size), *taskIds.toTypedArray())
            .query("_profile = $0", CommunicationProfileV1.ErxCommunicationReply.toString())
            .sort("sentOn", Sort.DESCENDING)
        if (!telematikId.isNullOrEmpty()) {
            q = q.query("sender = $0", telematikId)
        }
        return q.asFlow().map { results ->
            results.list.map { it.toErpModel() }
        }
    }

    override fun loadAllRepliedCommunications(taskIds: List<String>): Flow<List<CommunicationErpModel>> {
        if (taskIds.isEmpty()) return flowOf(emptyList())

        val q = realm.query<CommunicationEntityV1>(orQuerySubstring("parent.taskId", taskIds.size), *taskIds.toTypedArray())
            .query("_profile = $0", CommunicationProfileV1.ErxCommunicationReply.toString())
            .sort("sentOn", Sort.DESCENDING)
        return q.asFlow().map { results -> results.list.map { it.toErpModel() } }
    }

    override fun hasUnreadDispenseMessage(taskIds: List<String>, orderId: String): Flow<Boolean> =
        if (taskIds.isEmpty()) {
            flowOf(false)
        } else {
            realm.query<CommunicationEntityV1>(orQuerySubstring("parent.taskId", taskIds.size), *taskIds.toTypedArray())
                .query("consumed = false && orderId = $0", orderId)
                .count()
                .asFlow()
                .map { it > 0 }
        }

    override fun hasUnreadDispenseMessage(profileId: String): Flow<Boolean> =
        realm.query<CommunicationEntityV1>("consumed = false && parent.parent.id = $0", profileId)
            .count()
            .asFlow()
            .map { it > 0 }

    override fun unreadMessagesCount(): Flow<Long> =
        realm.query<CommunicationEntityV1>("consumed = false")
            .asFlow()
            .map { results ->
                val messages = results.list
                val unreadDispReqCount = messages
                    .filter { it._profile == CommunicationProfileV1.ErxCommunicationDispReq.toString() }
                    .distinctBy { it.orderId }
                    .size.toLong()
                val uniqueUnconsumedReplies = messages
                    .filter { it._profile == CommunicationProfileV1.ErxCommunicationReply.toString() }
                    .distinctBy { Pair(it.taskId, it.payload) }
                    .size.toLong()
                unreadDispReqCount + uniqueUnconsumedReplies
            }

    override fun getAllUnreadMessages(): Flow<List<CommunicationErpModel>> =
        realm.query<CommunicationEntityV1>("consumed = false")
            .asFlow()
            .map { results ->
                val messages = results.list
                val dispReqMessages = messages.filter { it._profile == CommunicationProfileV1.ErxCommunicationDispReq.toString() }
                val uniqueDispReqOrders = dispReqMessages.distinctBy { it.orderId }
                val replyMessages = messages.filter { it._profile == CommunicationProfileV1.ErxCommunicationReply.toString() }
                val uniqueReplies = replyMessages.distinctBy { Triple(it.taskId, it.payload, it.sender) }
                (uniqueDispReqOrders + uniqueReplies).map { it.toErpModel() }
            }

    override fun unreadPrescriptionsInAllOrders(profileId: String): Flow<Long> =
        realm.query<CommunicationEntityV1>("consumed = false && parent.parent.id = $0", profileId)
            .count()
            .asFlow()

    private fun orQuerySubstring(field: String, count: Int): String =
        if (count == 0) {
            "FALSEPREDICATE"
        } else {
            (0 until count).joinToString(" || ") { "$field = $$it" }
        }

    override fun taskIdsByOrder(orderId: String): Flow<List<String>> =
        realm.query<CommunicationEntityV1>("orderId = $0", orderId)
            .distinct("taskId")
            .asFlow()
            .map { result -> result.list.map { it.taskId } }

    override fun getProfileIdByOrderId(orderId: String): Flow<String?> =
        realm.query<CommunicationEntityV1>("orderId = $0", orderId)
            .first()
            .asFlow()
            .map { it.obj?.parent?.parent?.id }

    override fun getProfileIdByTaskId(taskId: String): Flow<String?> =
        realm.query<CommunicationEntityV1>("taskId = $0", taskId)
            .first()
            .asFlow()
            .map { it.obj?.parent?.parent?.id }

    override suspend fun setCommunicationStatus(communicationId: String, consumed: Boolean) {
        realm.write<Unit> {
            val originalCommunication = queryFirst<CommunicationEntityV1>("communicationId = $0", communicationId)
            originalCommunication?.let { communication ->
                val communicationsToUpdate = query<CommunicationEntityV1>(
                    "orderId = $0 && taskId = $1 && payload = $2 && sender = $3 && recipient = $4",
                    communication.orderId,
                    communication.taskId,
                    communication.payload,
                    communication.sender,
                    communication.recipient
                ).find()
                communicationsToUpdate.forEach { it.consumed = consumed }
            }
        }
    }

    override suspend fun updatePharmacyName(communicationId: String, pharmacyName: String) {
        realm.write<Unit> {
            queryFirst<CommunicationEntityV1>("communicationId = $0", communicationId)?.apply {
                this.pharmacyName = pharmacyName
            }
        }
    }

    override fun latestCommunicationTimestamp(profileId: String) =
        realm.query<CommunicationEntityV1>("parent.parent.id = $0", profileId)
            .max<RealmInstant>("sentOn")
            .asFlow()
            .map { it?.toInstant() }

    override fun hasUnreadRepliedMessages(taskIds: List<String>, telematikId: String?): Flow<Boolean> {
        if (taskIds.isEmpty()) return flowOf(false)

        var q = realm.query<CommunicationEntityV1>(orQuerySubstring("parent.taskId", taskIds.size), *taskIds.toTypedArray())
            .query("consumed = false")
            .query("_profile = $0", CommunicationProfileV1.ErxCommunicationReply.toString())

        if (!telematikId.isNullOrEmpty()) {
            q = q.query("sender = $0", telematikId)
        }

        return q.count()
            .asFlow()
            .map { it > 0 }
    }
}
