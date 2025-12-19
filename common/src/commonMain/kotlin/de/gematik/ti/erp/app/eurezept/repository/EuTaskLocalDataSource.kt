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

package de.gematik.ti.erp.app.eurezept.repository

import de.gematik.ti.erp.app.database.realm.utils.queryFirst
import de.gematik.ti.erp.app.database.realm.utils.safeWrite
import de.gematik.ti.erp.app.database.realm.utils.toInstant
import de.gematik.ti.erp.app.database.realm.utils.toRealmInstant
import de.gematik.ti.erp.app.database.realm.v1.euredeem.EuAccessCodeEntityV1
import de.gematik.ti.erp.app.database.realm.v1.euredeem.EuOrderEntityV1
import de.gematik.ti.erp.app.database.realm.v1.euredeem.EuTaskEventLogEntityV1
import de.gematik.ti.erp.app.eurezept.model.EuAccessCode
import de.gematik.ti.erp.app.eurezept.model.EuEventType
import de.gematik.ti.erp.app.eurezept.model.EuOrder
import de.gematik.ti.erp.app.eurezept.model.toEuAccessCode
import de.gematik.ti.erp.app.eurezept.model.toEuOrderEntityV1
import de.gematik.ti.erp.app.eurezept.model.toModel
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

class EuTaskLocalDataSource(
    private val realm: Realm,
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO
) {
    fun observeEuOrder(orderId: String): Flow<EuOrder?> =
        realm.query<EuOrderEntityV1>("orderId == $0", orderId)
            .sort("lastModifiedAt", Sort.DESCENDING)
            .first()
            .asFlow()
            .map { it.obj }
            .map { it?.toModel() }

    fun observeAllEuOrders(): Flow<List<EuOrder>> =
        realm.query<EuOrderEntityV1>()
            .sort("lastModifiedAt", Sort.DESCENDING)
            .asFlow()
            .map { result ->
                result.list
                    .sortedByDescending { it.createdAt }
                    .map { it.toModel() }
            }
            .flowOn(dispatcher)

    fun getLatestEuAccessCodeByProfileIdAndCountry(profileId: ProfileIdentifier, countryCode: String): Flow<EuAccessCode?> =
        realm.query<EuAccessCodeEntityV1>("profileId = $0 AND countryCode = $1", profileId, countryCode).asFlow().map {
            it.list.maxByOrNull { it.validUntil }?.toEuAccessCode()
        }.flowOn(dispatcher)

    fun getOrdersForProfileCountryAndTasks(
        profileId: ProfileIdentifier,
        countryCode: String,
        taskIds: List<String>
    ): Flow<List<EuOrder>> {
        val hasTasks = taskIds.isNotEmpty()
        return realm.query<EuOrderEntityV1>(
            buildString {
                append("profileId = $0 AND countryCode = $1")
                // TODO: Does it make sense to filter by taskIds?, can we have two orders for the same profileId/countryCode?
                if (hasTasks) {
                    append(" AND (")
                    append(taskIdQueryFragment(count = taskIds.size))
                    append(")")
                }
            },
            // Bind parameters: profileId, countryCode, and all taskIds
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

    suspend fun deleteEuAccessCodeByProfileId(profileId: ProfileIdentifier) {
        try {
            withContext(dispatcher) {
                realm.safeWrite {
                    val accessCodeEntity = queryFirst<EuAccessCodeEntityV1>("profileId = $0", profileId)
                    if (accessCodeEntity != null) {
                        // delete the access code object
                        val code = accessCodeEntity.accessCode
                        delete(accessCodeEntity)

                        // delete the access code from all orders that have it.
                        val orders = query<EuOrderEntityV1>("euAccessCode.accessCode == $0", code).find()
                        orders.forEach { order -> order.euAccessCode = null }
                    }
                }
            }
        } catch (e: Exception) {
            Napier.e(tag = "eu-order", message = "Error deleting EuAccessCode for $profileId", throwable = e)
        }
    }

    suspend fun saveEuOrder(euOrder: EuOrder, eventType: EuEventType) {
        when (eventType) {
            EuEventType.ACCESS_CODE_CREATED -> saveAsNewOrder(euOrder)
            EuEventType.ACCESS_CODE_RECREATED -> updateExistingOrder(euOrder)
            else -> {
                // Ignored
            }
        }
    }

    suspend fun markEventsAsRead(eventIds: List<String>) {
        realm.safeWrite {
            eventIds.forEach { id ->
                val event = query<EuTaskEventLogEntityV1>("id == $0", id).first().find()
                if (event != null) {
                    event.isUnread = false
                }
            }
        }
    }

    private suspend fun saveAsNewOrder(euOrder: EuOrder) {
        withContext(dispatcher) {
            realm.safeWrite {
                // 1. Remove old access code for this profile/country
                queryFirst<EuAccessCodeEntityV1>(
                    "profileId = $0 AND countryCode = $1",
                    euOrder.profileId,
                    euOrder.countryCode
                )?.let { delete(it) }

                // 2. Persist new order
                copyToRealm(
                    euOrder.toEuOrderEntityV1(EuEventType.ACCESS_CODE_CREATED),
                    UpdatePolicy.ALL
                )
            }
        }
    }

    private suspend fun updateExistingOrder(euOrder: EuOrder) {
        withContext(dispatcher) {
            realm.safeWrite {
                // 1. Fetch existing order
                val existingOrder = queryFirst<EuOrderEntityV1>(
                    "orderId = $0 AND profileId = $1",
                    euOrder.orderId,
                    euOrder.profileId
                ) ?: throw IllegalStateException(
                    "Cannot update order. No existing order found for orderId=${euOrder.orderId}"
                )

                // 2. Delete existing access code based on profileId and countryCode
                queryFirst<EuAccessCodeEntityV1>("profileId = $0 AND countryCode = $1", euOrder.profileId, euOrder.countryCode)
                    ?.let { delete(it) }

                // 3. Create order with new access code
                val entityV1 = euOrder.toEuOrderEntityV1(EuEventType.ACCESS_CODE_RECREATED)

                // 4. Update fields of the order
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

    // Updating events on all valid orders
    suspend fun addEventToValidOrders(
        profileId: ProfileIdentifier,
        taskIds: List<String>,
        eventType: EuEventType
    ) {
        try {
            withContext(dispatcher) {
                realm.safeWrite {
                    // 1. Find all orders that are present for this profileId
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
                            // --- update relatedTaskIds based on event type ---
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
                                    // do nothing for created/recreated/redeemed/etc.
                                }
                            }

                            // write back to realm
                            relatedTaskIds.apply {
                                clear()
                                addAll(updatedTaskIds)
                            }

                            // --- append new task event logs ---
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

    fun getEuAccessCode(accessCode: String): Flow<EuAccessCode?> =
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
