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

package de.gematik.ti.erp.app.database.realm.v1.eurezept

import de.gematik.ti.erp.app.database.api.eurezept.EuTaskLocalDataSource
import de.gematik.ti.erp.app.database.realm.utils.queryFirst
import de.gematik.ti.erp.app.database.realm.utils.safeWrite
import de.gematik.ti.erp.app.database.realm.utils.toInstant
import de.gematik.ti.erp.app.database.realm.utils.toRealmInstant
import de.gematik.ti.erp.app.database.realm.v1.euredeem.EuAccessCodeEntityV1
import de.gematik.ti.erp.app.database.realm.v1.euredeem.EuOrderEntityV1
import de.gematik.ti.erp.app.database.realm.v1.euredeem.EuTaskEventLogEntityV1
import de.gematik.ti.erp.app.eurezept.model.EuAccessCodeErpModel
import de.gematik.ti.erp.app.eurezept.model.EuEventType
import de.gematik.ti.erp.app.eurezept.model.EuOrderErpModel
import de.gematik.ti.erp.app.profile.repository.ProfileIdentifier
import io.github.aakira.napier.Napier
import io.realm.kotlin.Realm
import io.realm.kotlin.UpdatePolicy
import io.realm.kotlin.ext.query
import io.realm.kotlin.ext.toRealmList
import io.realm.kotlin.query.Sort
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import java.util.UUID

class EuTaskLocalDataSourceV1(
    private val realm: Realm,
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO
) : EuTaskLocalDataSource {

    override fun observeEuOrder(orderId: String): Flow<EuOrderErpModel?> =
        realm.query<EuOrderEntityV1>("orderId == $0", orderId)
            .sort("lastModifiedAt", Sort.DESCENDING)
            .first()
            .asFlow()
            .map { it.obj }
            .map { it?.toModel() }

    override fun observeAllEuOrders(): Flow<List<EuOrderErpModel>> =
        realm.query<EuOrderEntityV1>()
            .sort("lastModifiedAt", Sort.DESCENDING)
            .asFlow()
            .map { result ->
                result.list
                    .sortedByDescending { it.createdAt }
                    .map { it.toModel() }
            }
            .flowOn(dispatcher)

    override fun getLatestEuAccessCodeByProfileIdAndCountry(
        profileId: ProfileIdentifier,
        countryCode: String
    ): Flow<EuAccessCodeErpModel?> =
        realm.query<EuAccessCodeEntityV1>("profileId = $0 AND countryCode = $1", profileId, countryCode)
            .asFlow()
            .map { it.list.maxByOrNull { item -> item.validUntil }?.toEuAccessCode() }
            .flowOn(dispatcher)

    override fun getOrdersForProfileCountryAndTasks(
        profileId: ProfileIdentifier,
        countryCode: String,
        taskIds: List<String>
    ): Flow<List<EuOrderErpModel>> {
        val hasTasks = taskIds.isNotEmpty()
        return realm.query<EuOrderEntityV1>(
            buildString {
                append("profileId = $0 AND countryCode = $1")
                if (hasTasks) {
                    append(" AND (")
                    append(taskIdQueryFragment(count = taskIds.size))
                    append(")")
                }
            },
            profileId,
            countryCode,
            *taskIds.toTypedArray()
        )
            .asFlow()
            .map { result ->
                result.list
                    .sortedBy { it.createdAt }
                    .map { it.toModel() }
            }
            .flowOn(dispatcher)
    }

    override suspend fun deleteEuAccessCodeByProfileId(profileId: ProfileIdentifier) {
        try {
            withContext(dispatcher) {
                realm.safeWrite {
                    val accessCodeEntity = queryFirst<EuAccessCodeEntityV1>("profileId = $0", profileId)
                    if (accessCodeEntity != null) {
                        val code = accessCodeEntity.accessCode
                        delete(accessCodeEntity)

                        val orders = query<EuOrderEntityV1>("euAccessCode.accessCode == $0", code).find()
                        orders.forEach { order -> order.euAccessCode = null }
                    }
                }
            }
        } catch (e: Exception) {
            Napier.e(tag = "eu-order", message = "Error deleting EuAccessCode for $profileId", throwable = e)
        }
    }

    override suspend fun saveEuOrder(euOrder: EuOrderErpModel, eventType: EuEventType) {
        when (eventType) {
            EuEventType.ACCESS_CODE_CREATED -> saveAsNewOrder(euOrder)
            EuEventType.ACCESS_CODE_RECREATED -> updateExistingOrder(euOrder)
            else -> {
                // Ignored
            }
        }
    }

    override suspend fun markEventsAsRead(eventIds: List<String>) {
        realm.safeWrite {
            eventIds.forEach { id ->
                val event = query<EuTaskEventLogEntityV1>("id == $0", id).first().find()
                if (event != null) {
                    event.isUnread = false
                }
            }
        }
    }

    private suspend fun saveAsNewOrder(euOrder: EuOrderErpModel) {
        withContext(dispatcher) {
            realm.safeWrite {
                queryFirst<EuAccessCodeEntityV1>(
                    "profileId = $0 AND countryCode = $1",
                    euOrder.profileId,
                    euOrder.countryCode
                )?.let { delete(it) }

                copyToRealm(
                    euOrder.toEuOrderEntityV1(EuEventType.ACCESS_CODE_CREATED),
                    UpdatePolicy.ALL
                )
            }
        }
    }

    private suspend fun updateExistingOrder(euOrder: EuOrderErpModel) {
        withContext(dispatcher) {
            realm.safeWrite {
                val existingOrder = queryFirst<EuOrderEntityV1>(
                    "orderId = $0 AND profileId = $1",
                    euOrder.orderId,
                    euOrder.profileId
                ) ?: throw IllegalStateException(
                    "Cannot update order. No existing order found for orderId=${euOrder.orderId}"
                )

                queryFirst<EuAccessCodeEntityV1>("profileId = $0 AND countryCode = $1", euOrder.profileId, euOrder.countryCode)
                    ?.let { delete(it) }

                val entityV1 = euOrder.toEuOrderEntityV1(EuEventType.ACCESS_CODE_RECREATED)

                existingOrder.apply {
                    val tasksEvents = taskEvents.toMutableList()
                    Napier.d(tag = "eu-order", message = "Existing events: ${taskEvents.size}")
                    Napier.d(tag = "eu-order", message = "New events: ${entityV1.taskEvents.size}")
                    tasksEvents.addAll(entityV1.taskEvents)

                    taskEvents = tasksEvents.toRealmList()
                    createdAt = euOrder.createdAt.toRealmInstant()
                    countryCode = euOrder.countryCode
                    euAccessCode = entityV1.euAccessCode
                    relatedTaskIds.apply {
                        clear()
                        addAll(euOrder.relatedTaskIds)
                    }
                }
            }
        }
    }

    override suspend fun addEventToValidOrders(
        profileId: ProfileIdentifier,
        taskIds: List<String>,
        eventType: EuEventType
    ) {
        try {
            withContext(dispatcher) {
                realm.safeWrite {
                    val validOrders = query<EuOrderEntityV1>(
                        "profileId == $0",
                        profileId
                    ).find()
                        .sortedByDescending { it.lastModifiedAt }
                        .filter { it.hasValidAccessCode() }

                    if (validOrders.isEmpty()) {
                        Napier.d(tag = "eu-order", message = "No valid orders found for profile=$profileId → ignoring event")
                        return@safeWrite
                    }

                    validOrders.forEach { order ->
                        val entityV1 = order.toModel().toEuOrderEntityV1(
                            eventType = eventType,
                            affectedTaskIds = taskIds
                        )

                        order.apply {
                            val updatedTaskIds = relatedTaskIds.toMutableList()

                            when (eventType) {
                                EuEventType.TASK_ADDED -> {
                                    taskIds.forEach { tid ->
                                        if (!updatedTaskIds.contains(tid)) {
                                            updatedTaskIds.add(tid)
                                        }
                                    }
                                }

                                EuEventType.TASK_REMOVED -> {
                                    updatedTaskIds.removeAll(taskIds.toSet())
                                }

                                else -> {
                                    // do nothing
                                }
                            }

                            relatedTaskIds.apply {
                                clear()
                                addAll(updatedTaskIds)
                            }

                            val existingEvents = taskEvents.toMutableList()
                            existingEvents.addAll(entityV1.taskEvents)

                            taskEvents = existingEvents.toRealmList()
                        }
                    }
                }
            }
        } catch (e: Exception) {
            Napier.e(tag = "eu-order", message = "Error adding event to latest order", throwable = e)
        }
    }

    /**
     * Checks if there is a valid [EuOrderEntityV1] for the given [profileId] and [countryCode]
     * (i.e. the order has a non-null, non-expired access code), and if so appends a
     * [EuTaskEventLogEntityV1] with [EuEventType.TASK_REDEEMED] for the supplied [taskId].
     */
    override suspend fun addRedeemedEventIfValidOrderExists(
        profileId: ProfileIdentifier,
        countryCode: String,
        taskId: String
    ) {
        try {
            withContext(dispatcher) {
                realm.safeWrite {
                    val order = query<EuOrderEntityV1>(
                        "profileId == $0 AND countryCode == $1",
                        profileId,
                        countryCode
                    ).find()
                        .filter { it.hasValidAccessCode() }
                        .maxByOrNull { it.lastModifiedAt ?: it.createdAt }

                    if (order == null) {
                        Napier.d(
                            tag = "eu-order",
                            message = "No valid order found for profile=$profileId, country=$countryCode → skipping TASK_REDEEMED event"
                        )
                        return@safeWrite
                    }

                    val newEvent = EuTaskEventLogEntityV1().apply {
                        id = UUID.randomUUID().toString()
                        this.orderId = order.orderId
                        this.taskId = taskId
                        this.event = EuEventType.TASK_REDEEMED.name
                        this.createdAt = Clock.System.now().toRealmInstant()
                        this.isUnread = true
                    }

                    val updatedEvents = order.taskEvents.toMutableList()
                    updatedEvents.add(newEvent)
                    order.taskEvents = updatedEvents.toRealmList()

                    Napier.d(
                        tag = "eu-order",
                        message = "Added TASK_REDEEMED event for taskId=$taskId on orderId=${order.orderId}"
                    )
                }
            }
        } catch (e: Exception) {
            Napier.e(
                tag = "eu-order",
                message = "Error adding TASK_REDEEMED event for profile=$profileId, country=$countryCode, task=$taskId",
                throwable = e
            )
        }
    }

    override fun getEuAccessCode(accessCode: String): Flow<EuAccessCodeErpModel?> =
        realm.query<EuAccessCodeEntityV1>("accessCode = $0", accessCode)
            .first()
            .asFlow()
            .map { it.obj?.toEuAccessCode() }.flowOn(dispatcher)

    private fun taskIdQueryFragment(startIndex: Int = 2, count: Int): String =
        (0 until count)
            .joinToString(" OR ") { idx ->
                "ANY relatedTaskIds = $${startIndex + idx}"
            }

    private fun EuOrderEntityV1.hasValidAccessCode(now: Instant = Clock.System.now()): Boolean =
        euAccessCode?.validUntil?.toInstant()?.let { it >= now } ?: false
}
