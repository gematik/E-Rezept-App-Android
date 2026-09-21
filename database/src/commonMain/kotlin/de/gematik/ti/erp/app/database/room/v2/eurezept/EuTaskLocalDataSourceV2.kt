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

package de.gematik.ti.erp.app.database.room.v2.eurezept

import de.gematik.ti.erp.app.database.api.eurezept.EuTaskLocalDataSource
import de.gematik.ti.erp.app.database.room.v2.euredeem.EuOrderDao
import de.gematik.ti.erp.app.database.room.v2.euredeem.EuTaskEventEntity
import de.gematik.ti.erp.app.eurezept.model.EuAccessCodeErpModel
import de.gematik.ti.erp.app.eurezept.model.EuEventType
import de.gematik.ti.erp.app.eurezept.model.EuOrderErpModel
import de.gematik.ti.erp.app.profile.repository.ProfileIdentifier
import io.github.aakira.napier.Napier
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import kotlinx.datetime.Clock
import java.util.UUID

class EuTaskLocalDataSourceV2(
    private val dao: EuOrderDao,
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO
) : EuTaskLocalDataSource {

    override fun observeEuOrder(orderId: String): Flow<EuOrderErpModel?> =
        dao.observeEuOrder(orderId)
            .map { it?.toModel() }
            .flowOn(dispatcher)

    override fun observeAllEuOrders(): Flow<List<EuOrderErpModel>> =
        dao.observeAllEuOrders()
            .map { list -> list.map { it.toModel() } }
            .flowOn(dispatcher)

    override fun getLatestEuAccessCodeByProfileIdAndCountry(
        profileId: ProfileIdentifier,
        countryCode: String
    ): Flow<EuAccessCodeErpModel?> =
        dao.observeAccessCodes(profileId, countryCode)
            .map { list -> list.maxByOrNull { it.validUntil }?.toModel() }
            .flowOn(dispatcher)

    override fun getOrdersForProfileCountryAndTasks(
        profileId: ProfileIdentifier,
        countryCode: String,
        taskIds: List<String>
    ): Flow<List<EuOrderErpModel>> =
        dao.observeOrdersForProfileAndCountry(profileId, countryCode)
            .map { list ->
                val filtered = if (taskIds.isNotEmpty()) {
                    list.filter { item ->
                        item.order.relatedTaskIds.any { tid -> taskIds.contains(tid) }
                    }
                } else {
                    list
                }
                filtered.sortedBy { it.order.createdAt }.map { it.toModel() }
            }
            .flowOn(dispatcher)

    override suspend fun deleteEuAccessCodeByProfileId(profileId: ProfileIdentifier) {
        try {
            withContext(dispatcher) {
                val accessCode = dao.getAccessCodesByProfileId(profileId).firstOrNull()
                if (accessCode != null) {
                    dao.removeAccessCodeFromOrders(accessCode.accessCode)
                    dao.deleteAccessCode(accessCode.accessCode)
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
        withContext(dispatcher) {
            eventIds.forEach { id ->
                dao.updateEventUnreadStatus(id, false)
            }
        }
    }

    private suspend fun saveAsNewOrder(euOrder: EuOrderErpModel) {
        withContext(dispatcher) {
            val oldCodes = dao.getAccessCodesByProfileId(euOrder.profileId)
                .filter { it.countryCode == euOrder.countryCode }
            oldCodes.forEach { ac ->
                dao.removeAccessCodeFromOrders(ac.accessCode)
                dao.deleteAccessCode(ac.accessCode)
            }

            euOrder.euAccessCode?.let {
                dao.upsertAccessCode(it.toEntity())
            }

            val now = Clock.System.now()
            dao.upsertOrder(euOrder.toEntity(lastModifiedAt = now))

            val initialEvents = euOrder.relatedTaskIds.map { tid ->
                EuTaskEventEntity(
                    id = UUID.randomUUID().toString(),
                    orderId = euOrder.orderId,
                    type = EuEventType.ACCESS_CODE_CREATED.name,
                    taskId = tid,
                    createdAt = now,
                    isUnread = true
                )
            }
            dao.insertEvents(initialEvents)
        }
    }

    private suspend fun updateExistingOrder(euOrder: EuOrderErpModel) {
        withContext(dispatcher) {
            val existingOrderWithRelations = dao.getOrdersByProfileIdDirect(euOrder.profileId)
                .firstOrNull { it.order.orderId == euOrder.orderId }
                ?: throw IllegalStateException(
                    "Cannot update order. No existing order found for orderId=${euOrder.orderId}"
                )

            val existingEvents = existingOrderWithRelations.events

            val oldCodes = dao.getAccessCodesByProfileId(euOrder.profileId)
                .filter { it.countryCode == euOrder.countryCode }
            oldCodes.forEach { ac ->
                dao.removeAccessCodeFromOrders(ac.accessCode)
                dao.deleteAccessCode(ac.accessCode)
            }

            euOrder.euAccessCode?.let {
                dao.upsertAccessCode(it.toEntity())
            }

            val now = Clock.System.now()
            val updatedOrder = euOrder.toEntity(lastModifiedAt = now).copy(
                createdAt = euOrder.createdAt
            )
            dao.upsertOrder(updatedOrder)

            val newEvents = euOrder.relatedTaskIds.map { tid ->
                EuTaskEventEntity(
                    id = UUID.randomUUID().toString(),
                    orderId = euOrder.orderId,
                    type = EuEventType.ACCESS_CODE_RECREATED.name,
                    taskId = tid,
                    createdAt = now,
                    isUnread = true
                )
            }
            dao.insertEvents(existingEvents + newEvents)
        }
    }

    override suspend fun addEventToValidOrders(
        profileId: ProfileIdentifier,
        taskIds: List<String>,
        eventType: EuEventType
    ) {
        try {
            withContext(dispatcher) {
                val now = Clock.System.now()
                val allOrders = dao.getOrdersByProfileIdDirect(profileId)
                val validOrders = allOrders
                    .filter { item ->
                        val validUntil = item.accessCode?.validUntil
                        validUntil != null && validUntil >= now
                    }

                if (validOrders.isEmpty()) {
                    Napier.d(tag = "eu-order", message = "No valid orders found for profile=$profileId → ignoring event")
                    return@withContext
                }

                validOrders.forEach { item ->
                    val updatedTaskIds = item.order.relatedTaskIds.toMutableList()
                    val existingEvents = item.events
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
                        else -> {}
                    }

                    val updatedOrder = item.order.copy(
                        relatedTaskIds = updatedTaskIds,
                        lastModifiedAt = now
                    )
                    dao.upsertOrder(updatedOrder)

                    val eventEntities = taskIds.map { tid ->
                        EuTaskEventEntity(
                            id = UUID.randomUUID().toString(),
                            orderId = item.order.orderId,
                            type = eventType.name,
                            taskId = tid,
                            createdAt = now,
                            isUnread = true
                        )
                    }
                    dao.insertEvents(existingEvents + eventEntities)
                }
            }
        } catch (e: Exception) {
            Napier.e(tag = "eu-order", message = "Error adding event to latest order", throwable = e)
        }
    }

    override suspend fun addRedeemedEventIfValidOrderExists(
        profileId: ProfileIdentifier,
        countryCode: String,
        taskId: String
    ) {
        try {
            withContext(dispatcher) {
                val now = Clock.System.now()
                val allOrders = dao.getOrdersByProfileIdDirect(profileId)
                val validOrder = allOrders
                    .filter { it.order.countryCode == countryCode }
                    .filter { item ->
                        val validUntil = item.accessCode?.validUntil
                        validUntil != null && validUntil >= now
                    }
                    .maxByOrNull { it.order.lastModifiedAt ?: it.order.createdAt }

                if (validOrder == null) {
                    Napier.d(
                        tag = "eu-order",
                        message = "No valid order found for profile=$profileId, country=$countryCode → skipping TASK_REDEEMED event"
                    )
                    return@withContext
                }

                val newEvent = EuTaskEventEntity(
                    id = UUID.randomUUID().toString(),
                    orderId = validOrder.order.orderId,
                    type = EuEventType.TASK_REDEEMED.name,
                    taskId = taskId,
                    createdAt = now,
                    isUnread = true
                )

                dao.insertEvents(listOf(newEvent))

                Napier.d(
                    tag = "eu-order",
                    message = "Added TASK_REDEEMED event for taskId=$taskId on orderId=${validOrder.order.orderId}"
                )
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
        dao.observeAccessCodeByCode(accessCode)
            .map { it?.toModel() }
            .flowOn(dispatcher)

    override suspend fun importMigratedOrder(euOrder: EuOrderErpModel) {
        withContext(dispatcher) {
            euOrder.euAccessCode?.let {
                dao.upsertAccessCode(it.toEntity())
            }
            dao.upsertOrder(euOrder.toEntity(lastModifiedAt = euOrder.lastModifiedAt))
            dao.insertEvents(euOrder.events.map { it.toEntity(euOrder.orderId) })
        }
    }
}
