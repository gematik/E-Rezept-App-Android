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

@file:Suppress("SpreadOperator")

package de.gematik.ti.erp.app.messages.repository

import de.gematik.ti.erp.app.database.realm.utils.queryFirst
import de.gematik.ti.erp.app.database.realm.utils.toInstant
import de.gematik.ti.erp.app.database.realm.v1.task.entity.CommunicationEntityV1
import de.gematik.ti.erp.app.database.realm.v1.task.entity.CommunicationProfileV1
import de.gematik.ti.erp.app.messages.model.Communication
import de.gematik.ti.erp.app.messages.model.CommunicationProfile
import de.gematik.ti.erp.app.prescription.repository.toCommunication
import de.gematik.ti.erp.app.profile.repository.ProfileIdentifier
import de.gematik.ti.erp.app.profiles.mapper.toProfileData
import de.gematik.ti.erp.app.profiles.model.ProfilesData
import io.realm.kotlin.Realm
import io.realm.kotlin.ext.query
import io.realm.kotlin.query.Sort
import io.realm.kotlin.query.max
import io.realm.kotlin.types.RealmInstant
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapNotNull

class CommunicationLocalDataSource(
    private val realm: Realm
) {

    fun loadDispReqCommunications(
        orderId: String
    ): Flow<List<Communication>> =
        realm.query<CommunicationEntityV1>(
            "orderId = $0 && _profile = $1",
            orderId,
            CommunicationProfile.ErxCommunicationDispReq.toEntityValue()
        )
            .asFlow()
            .map { communication ->
                communication.list.mapNotNull {
                    it.toCommunication()
                }
            }

    fun loadDispReqCommunicationsByProfileId(
        profileId: ProfileIdentifier
    ): Flow<List<Communication>> =
        realm.query<CommunicationEntityV1>(
            "parent.parent.id = $0 && _profile = $1",
            profileId,
            CommunicationProfile.ErxCommunicationDispReq.toEntityValue()
        )
            .sort("sentOn", Sort.DESCENDING)
            .asFlow()
            .map { communications ->
                communications.list.mapNotNull {
                    it.toCommunication()
                }
            }

    fun loadRepliedCommunications(
        taskIds: List<String>,
        telematikId: String? = null // To handle the optional case
    ): Flow<List<Communication>> {
        var query = realm.query<CommunicationEntityV1>(
            orQuerySubstring("parent.taskId", taskIds.size),
            *taskIds.toTypedArray()
        )
            .query("_profile = $0", CommunicationProfile.ErxCommunicationReply.toEntityValue())
            .sort("sentOn", Sort.DESCENDING)

        if (!telematikId.isNullOrEmpty()) {
            query = query.query("sender = $0", telematikId)
        }

        return query.asFlow().map { results ->
            val communications = results.list
                .mapNotNull { it.toCommunication() }
            val repliedMessages = mutableListOf<Communication>()
            // Group communications by their payload.
            val groupedCommunications = communications.groupBy { it.payload }

            groupedCommunications.forEach { communication ->
                // Extract distinct task IDs from the group.
                val repliedTaskIds = communication.value.distinctBy { it.taskId }.map(Communication::taskId)
                repliedMessages.addAll(
                    communication.value.distinctBy { it.payload }.map {
                        // Determine if the count of task IDs matches the provided list size.
                        val isTaskIdCountMatching = (repliedTaskIds.size == taskIds.size)
                        // Create a copy of the communication with additional information.
                        it.copy(taskIds = repliedTaskIds, isTaskIdCountMatching = isTaskIdCountMatching)
                    }
                )
            }
            repliedMessages
        }
    }

    fun loadAllRepliedCommunications(
        taskIds: List<String>
    ): Flow<List<Communication>> {
        val query = realm.query<CommunicationEntityV1>(
            orQuerySubstring("parent.taskId", taskIds.size),
            *taskIds.toTypedArray()
        )
            .query("_profile = $0", CommunicationProfile.ErxCommunicationReply.toEntityValue())
            .sort("sentOn", Sort.DESCENDING)

        return query.asFlow().map { results ->
            results.list.mapNotNull { it.toCommunication() }
        }
    }

    fun hasUnreadDispenseMessage(taskIds: List<String>, orderId: String): Flow<Boolean> =
        realm.query<CommunicationEntityV1>(
            orQuerySubstring("parent.taskId", taskIds.size),
            *taskIds.toTypedArray()
        )
            .query("consumed = false && orderId = $0", orderId)
            .count()
            .asFlow()
            .map { it > 0 }

    fun hasUnreadDispenseMessage(profileId: ProfileIdentifier): Flow<Boolean> =
        realm.query<CommunicationEntityV1>("consumed = false && parent.parent.id = $0", profileId)
            .count()
            .asFlow()
            .map { it > 0 }

    /**
     * [consumed] which refers to if the order message was read is checked to be false.
     * @return [flow<Long>] of the count of the unread messages
     */

    fun unreadMessagesCount(): Flow<Long> =
        realm.query<CommunicationEntityV1>()
            .asFlow()
            .map { results ->
                val messages = results.list

                // Filter messages by profile type
                val dispReqMessages = messages.filter { it.profile == CommunicationProfileV1.ErxCommunicationDispReq }
                val replyMessages = messages.filter { it.profile == CommunicationProfileV1.ErxCommunicationReply }

                // Count unread dispensing requests grouped by orderId
                val unreadDispReqCount = dispReqMessages
                    .groupBy { it.orderId }
                    .count { (_, dispReqs) ->
                        dispReqs.any { !it.consumed }
                    }.toLong()

                // Count unique unconsumed replies using taskId and payload as unique identifiers
                val uniqueUnconsumedReplies = replyMessages
                    .filter { !it.consumed }
                    .distinctBy { Pair(it.taskId, it.payload) }
                    .size
                    .toLong()

                // Combine both counts to get the total unread message count
                unreadDispReqCount + uniqueUnconsumedReplies
            }

    fun getAllUnreadMessages(): Flow<List<Communication>> =
        realm.query<CommunicationEntityV1>("consumed = false")
            .asFlow()
            .map { results ->
                val messages = results.list

                val dispReqMessages = messages.filter { it.profile == CommunicationProfileV1.ErxCommunicationDispReq }
                val uniqueDispReqOrders = dispReqMessages.distinctBy { it.orderId }

                val replyMessages = messages.filter { it.profile == CommunicationProfileV1.ErxCommunicationReply }
                val uniqueReplies = replyMessages.distinctBy { it.taskId to it.payload to it.sender }

                (uniqueDispReqOrders + uniqueReplies).mapNotNull { it.toCommunication() }
            }

    fun unreadPrescriptionsInAllOrders(profileId: ProfileIdentifier): Flow<Long> =
        realm.query<CommunicationEntityV1>("consumed = false && parent.parent.id = $0", profileId)
            .count()
            .asFlow()

    private fun orQuerySubstring(field: String, count: Int): String =
        (0 until count)
            .map { "$field = $$it" }
            .joinToString(" || ")

    fun taskIdsByOrder(orderId: String): Flow<List<String>> =
        realm.query<CommunicationEntityV1>(
            "orderId = $0",
            orderId
        ).distinct("taskId")
            .asFlow()
            .map { result ->
                val taskIds = result.list.map { it.taskId }
                taskIds
            }

    fun getProfileByOrderId(orderId: String): Flow<ProfilesData.Profile> =
        realm.query<CommunicationEntityV1>("orderId = $0", orderId)
            .first()
            .asFlow()
            .mapNotNull {
                it.obj?.parent?.parent?.toProfileData()
            }

    suspend fun setCommunicationStatus(communicationId: String, consumed: Boolean) {
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

    fun latestCommunicationTimestamp(profileId: ProfileIdentifier) =
        realm.query<CommunicationEntityV1>("parent.parent.id = $0", profileId)
            .max<RealmInstant>("sentOn")
            .asFlow()
            .map { it?.toInstant() }

    fun hasUnreadRepliedMessages(taskIds: List<String>, telematikId: String?) =
        realm.query<CommunicationEntityV1>(
            orQuerySubstring("parent.taskId", taskIds.size),
            *taskIds.toTypedArray()
        )
            .query("consumed = false")
            .query("_profile = $0", CommunicationProfile.ErxCommunicationReply.toEntityValue())
            .query("sender = $0", telematikId)
            .count()
            .asFlow()
            .map { it > 0 }
}
