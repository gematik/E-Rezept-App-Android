/*
 * Copyright (c) 2022 gematik GmbH
 * 
 * Licensed under the EUPL, Version 1.2 or – as soon they will be approved by
 * the European Commission - subsequent versions of the EUPL (the Licence);
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at:
 * 
 *     https://joinup.ec.europa.eu/software/page/eupl
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the Licence is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Licence for the specific language governing permissions and
 * limitations under the Licence.
 * 
 */

@file:Suppress("SpreadOperator")

package de.gematik.ti.erp.app.orders.repository

import de.gematik.ti.erp.app.db.entities.v1.task.CommunicationEntityV1
import de.gematik.ti.erp.app.db.queryFirst
import de.gematik.ti.erp.app.db.toInstant
import de.gematik.ti.erp.app.prescription.model.SyncedTaskData
import de.gematik.ti.erp.app.prescription.repository.toCommunication
import de.gematik.ti.erp.app.profiles.repository.ProfileIdentifier
import io.realm.kotlin.Realm
import io.realm.kotlin.ext.query
import io.realm.kotlin.query.Sort
import io.realm.kotlin.query.max
import io.realm.kotlin.types.RealmInstant
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class CommunicationLocalDataSource(
    private val realm: Realm
) {

    fun loadDispReqCommunications(
        orderId: String
    ): Flow<List<SyncedTaskData.Communication>> =
        realm.query<CommunicationEntityV1>(
            "orderId = $0 && _profile = $1",
            orderId,
            SyncedTaskData.CommunicationProfile.ErxCommunicationDispReq.toEntityValue()
        )
            .asFlow()
            .map { communication ->
                communication.list.mapNotNull {
                    it.toCommunication()
                }
            }

    fun loadFirstDispReqCommunications(
        profileId: ProfileIdentifier
    ): Flow<List<SyncedTaskData.Communication>> =
        realm.query<CommunicationEntityV1>(
            "parent.parent.id = $0 && _profile = $1",
            profileId,
            SyncedTaskData.CommunicationProfile.ErxCommunicationDispReq.toEntityValue()
        )
            .sort("sentOn", Sort.DESCENDING)
            .distinct("orderId")
            .asFlow()
            .map { communications ->
                communications.list.mapNotNull {
                    it.toCommunication()
                }
            }

    fun loadRepliedCommunications(
        taskIds: List<String>
    ): Flow<List<SyncedTaskData.Communication>> =
        realm.query<CommunicationEntityV1>(
            orQuerySubstring("parent.taskId", taskIds.size),
            *taskIds.toTypedArray()
        )
            .query("_profile = $0", SyncedTaskData.CommunicationProfile.ErxCommunicationReply.toEntityValue())
            .sort("sentOn", Sort.DESCENDING)
            .distinct("payload")
            .asFlow()
            .map { communications ->
                communications.list.mapNotNull {
                    it.toCommunication()
                }
            }

    fun hasUnreadMessages(taskIds: List<String>, orderId: String): Flow<Boolean> =
        realm.query<CommunicationEntityV1>(
            orQuerySubstring("parent.taskId", taskIds.size),
            *taskIds.toTypedArray()
        )
            .query("consumed = false && orderId = $0", orderId)
            .count()
            .asFlow()
            .map { it > 0 }

    fun hasUnreadMessages(profileId: ProfileIdentifier): Flow<Boolean> =
        realm.query<CommunicationEntityV1>("consumed = false && parent.parent.id = $0", profileId)
            .count()
            .asFlow()
            .map { it > 0 }

    private fun orQuerySubstring(field: String, count: Int): String =
        (0 until count)
            .map { "$field = $$it" }
            .joinToString(" || ")

    fun taskIdsByOrder(orderId: String): Flow<List<String>> =
        realm.query<CommunicationEntityV1>(
            "orderId = $0 && _profile = $1",
            orderId,
            SyncedTaskData.CommunicationProfile.ErxCommunicationDispReq.toEntityValue()
        )
            .asFlow()
            .map { result ->
                result.list.map { it.taskId }
            }

    suspend fun setCommunicationStatus(communicationId: String, consumed: Boolean) {
        realm.write<Unit> {
            queryFirst<CommunicationEntityV1>("communicationId = $0", communicationId)?.apply {
                this.consumed = consumed
            }
        }
    }

    fun latestCommunicationTimestamp(profileId: ProfileIdentifier) =
        realm.query<CommunicationEntityV1>("parent.parent.id = $0", profileId)
            .max<RealmInstant>("sentOn")
            .asFlow()
            .map {
                it?.toInstant()
            }
}
