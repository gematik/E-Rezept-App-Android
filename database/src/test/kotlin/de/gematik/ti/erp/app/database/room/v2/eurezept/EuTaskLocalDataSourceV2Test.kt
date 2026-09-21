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
 */

package de.gematik.ti.erp.app.database.room.v2.eurezept

import de.gematik.ti.erp.app.database.api.eurezept.EuTaskLocalDataSource
import de.gematik.ti.erp.app.database.room.v2.euredeem.EuAccessCodeEntity
import de.gematik.ti.erp.app.database.room.v2.euredeem.EuOrderDao
import de.gematik.ti.erp.app.database.room.v2.euredeem.EuOrderEntity
import de.gematik.ti.erp.app.database.room.v2.euredeem.EuOrderWithRelations
import de.gematik.ti.erp.app.database.room.v2.euredeem.EuTaskEventEntity
import de.gematik.ti.erp.app.eurezept.model.EuAccessCodeErpModel
import de.gematik.ti.erp.app.eurezept.model.EuEventType
import de.gematik.ti.erp.app.eurezept.model.EuOrderErpModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.Clock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue
import kotlin.time.Duration.Companion.hours

@OptIn(ExperimentalCoroutinesApi::class)
class EuTaskLocalDataSourceV2Test {

    private class FakeEuOrderDao : EuOrderDao {
        val orders = LinkedHashMap<String, EuOrderEntity>()
        val accessCodes = LinkedHashMap<String, EuAccessCodeEntity>()
        val events = LinkedHashMap<String, EuTaskEventEntity>()

        private val flowOrders = MutableStateFlow<List<EuOrderWithRelations>>(emptyList())
        private val flowAccessCodes = MutableStateFlow<List<EuAccessCodeEntity>>(emptyList())

        private fun emit() {
            flowOrders.value = orders.values.map { order ->
                EuOrderWithRelations(
                    order = order,
                    accessCode = order.euAccessCodeCode?.let { accessCodes[it] },
                    events = this@FakeEuOrderDao.events.values.filter { it.orderId == order.orderId }
                )
            }
            flowAccessCodes.value = accessCodes.values.toList()
        }

        override fun observeEuOrder(orderId: String): Flow<EuOrderWithRelations?> =
            flowOrders.map { list -> list.firstOrNull { it.order.orderId == orderId } }

        override fun observeAllEuOrders(): Flow<List<EuOrderWithRelations>> = flowOrders

        override fun observeOrdersForProfileAndCountry(profileId: String, countryCode: String): Flow<List<EuOrderWithRelations>> =
            flowOrders.map { list -> list.filter { it.order.profileId == profileId && it.order.countryCode == countryCode } }

        override fun observeAccessCodes(profileId: String, countryCode: String): Flow<List<EuAccessCodeEntity>> =
            flowAccessCodes.map { list -> list.filter { it.profileId == profileId && it.countryCode == countryCode } }

        override fun observeAccessCodeByCode(accessCode: String): Flow<EuAccessCodeEntity?> =
            flowAccessCodes.map { list -> list.firstOrNull { it.accessCode == accessCode } }

        override suspend fun getOrderById(orderId: String): EuOrderEntity? = orders[orderId]
        override suspend fun getAccessCodeByCode(accessCode: String): EuAccessCodeEntity? = accessCodes[accessCode]

        override suspend fun getOrdersByProfileIdDirect(profileId: String): List<EuOrderWithRelations> =
            orders.values.filter { it.profileId == profileId }.map { order ->
                EuOrderWithRelations(
                    order = order,
                    accessCode = order.euAccessCodeCode?.let { accessCodes[it] },
                    events = this@FakeEuOrderDao.events.values.filter { it.orderId == order.orderId }
                )
            }

        override suspend fun getAccessCodesByProfileId(profileId: String): List<EuAccessCodeEntity> =
            accessCodes.values.filter { it.profileId == profileId }

        override suspend fun deleteAccessCodeByProfileId(profileId: String) {
            val toDelete = accessCodes.values.filter { it.profileId == profileId }.map { it.accessCode }
            toDelete.forEach { accessCodes.remove(it) }
            emit()
        }

        override suspend fun upsertOrder(order: EuOrderEntity) {
            orders[order.orderId] = order
            emit()
        }

        override suspend fun upsertAccessCode(accessCode: EuAccessCodeEntity) {
            accessCodes[accessCode.accessCode] = accessCode
            emit()
        }

        override suspend fun deleteAccessCode(accessCode: String) {
            accessCodes.remove(accessCode)
            emit()
        }

        override suspend fun removeAccessCodeFromOrders(accessCode: String) {
            orders.values.forEach { order ->
                if (order.euAccessCodeCode == accessCode) {
                    orders[order.orderId] = order.copy(euAccessCodeCode = null)
                }
            }
            emit()
        }

        override suspend fun insertEvents(entities: List<EuTaskEventEntity>) {
            entities.forEach { events[it.id] = it }
            emit()
        }

        override suspend fun updateEventUnreadStatus(id: String, isUnread: Boolean) {
            events[id]?.let {
                events[id] = it.copy(isUnread = isUnread)
            }
            emit()
        }
    }

    private val testDispatcher = StandardTestDispatcher()
    private val fakeDao = FakeEuOrderDao()
    private val sut: EuTaskLocalDataSource = EuTaskLocalDataSourceV2(fakeDao, testDispatcher)
    private val now = Clock.System.now()

    @Test
    fun observeEuOrder_returnsMappedModel() = runTest(testDispatcher) {
        val orderEntity = EuOrderEntity(
            orderId = "order1",
            countryCode = "IT",
            createdAt = now,
            lastModifiedAt = null,
            profileId = "profile1",
            euAccessCodeCode = null,
            relatedTaskIds = listOf("task1")
        )
        fakeDao.upsertOrder(orderEntity)

        val observed = sut.observeEuOrder("order1").first()
        assertNotNull(observed)
        assertEquals("order1", observed.orderId)
        assertEquals("IT", observed.countryCode)
        assertNull(observed.euAccessCode)
        assertEquals(listOf("task1"), observed.relatedTaskIds)
    }

    @Test
    fun observeAllEuOrders_returnsMappedModels() = runTest(testDispatcher) {
        val orderEntity1 = EuOrderEntity(
            orderId = "order1",
            countryCode = "IT",
            createdAt = now,
            lastModifiedAt = null,
            profileId = "profile1",
            euAccessCodeCode = null,
            relatedTaskIds = listOf("task1")
        )
        val orderEntity2 = EuOrderEntity(
            orderId = "order2",
            countryCode = "FR",
            createdAt = now,
            lastModifiedAt = null,
            profileId = "profile1",
            euAccessCodeCode = null,
            relatedTaskIds = listOf("task2")
        )
        fakeDao.upsertOrder(orderEntity1)
        fakeDao.upsertOrder(orderEntity2)

        val observedList = sut.observeAllEuOrders().first()
        assertEquals(2, observedList.size)
    }

    @Test
    fun saveEuOrder_ACCESS_CODE_CREATED_cleansOldCodes_savesNewOrderAndAccessCode() = runTest(testDispatcher) {
        // Seed an existing access code and link it to an order for same profile & country
        val oldAccessCode = EuAccessCodeEntity(
            accessCode = "OLD_CODE",
            countryCode = "IT",
            validUntil = now + 1.hours,
            createdAt = now - 1.hours,
            profileId = "profile1"
        )
        val oldOrder = EuOrderEntity(
            orderId = "order_old",
            countryCode = "IT",
            createdAt = now - 1.hours,
            lastModifiedAt = null,
            profileId = "profile1",
            euAccessCodeCode = "OLD_CODE",
            relatedTaskIds = listOf("task_old")
        )
        fakeDao.upsertAccessCode(oldAccessCode)
        fakeDao.upsertOrder(oldOrder)

        // Create new EuOrderErpModel
        val newOrder = EuOrderErpModel(
            orderId = "order_new",
            countryCode = "IT",
            createdAt = now,
            lastModifiedAt = null,
            profileId = "profile1",
            euAccessCode = EuAccessCodeErpModel(
                accessCode = "NEW_CODE",
                countryCode = "IT",
                validUntil = now + 2.hours,
                createdAt = now,
                profileIdentifier = "profile1"
            ),
            events = emptyList(),
            relatedTaskIds = listOf("task_new")
        )

        sut.saveEuOrder(newOrder, EuEventType.ACCESS_CODE_CREATED)

        // Verify old access code is removed
        assertNull(fakeDao.accessCodes["OLD_CODE"])
        // Verify old order access code reference was wiped
        assertEquals(null, fakeDao.orders["order_old"]?.euAccessCodeCode)

        // Verify new access code and order are saved
        assertNotNull(fakeDao.accessCodes["NEW_CODE"])
        assertNotNull(fakeDao.orders["order_new"])
        assertEquals("NEW_CODE", fakeDao.orders["order_new"]?.euAccessCodeCode)

        // Verify initial events were created
        val createdEvents = fakeDao.events.values.filter { it.orderId == "order_new" }
        assertEquals(1, createdEvents.size)
        assertTrue(createdEvents.any { it.type == "ACCESS_CODE_CREATED" && it.taskId == "task_new" })
    }

    @Test
    fun saveEuOrder_ACCESS_CODE_RECREATED_updatesExistingOrder() = runTest(testDispatcher) {
        // Seed order
        val existingOrder = EuOrderEntity(
            orderId = "order1",
            countryCode = "IT",
            createdAt = now - 1.hours,
            lastModifiedAt = null,
            profileId = "profile1",
            euAccessCodeCode = null,
            relatedTaskIds = listOf("task1")
        )
        fakeDao.upsertOrder(existingOrder)

        val updatedOrderModel = EuOrderErpModel(
            orderId = "order1",
            countryCode = "IT",
            createdAt = now - 1.hours,
            lastModifiedAt = null,
            profileId = "profile1",
            euAccessCode = EuAccessCodeErpModel(
                accessCode = "RECREATED_CODE",
                countryCode = "IT",
                validUntil = now + 2.hours,
                createdAt = now,
                profileIdentifier = "profile1"
            ),
            events = emptyList(),
            relatedTaskIds = listOf("task1", "task2")
        )

        sut.saveEuOrder(updatedOrderModel, EuEventType.ACCESS_CODE_RECREATED)

        // Verify updated order fields
        val after = fakeDao.orders["order1"]
        assertNotNull(after)
        assertEquals("RECREATED_CODE", after.euAccessCodeCode)
        assertEquals(listOf("task1", "task2"), after.relatedTaskIds)

        // Verify newly added events
        val recreatedEvents = fakeDao.events.values.filter { it.orderId == "order1" }
        assertEquals(2, recreatedEvents.size)
        assertTrue(recreatedEvents.all { it.type == "ACCESS_CODE_RECREATED" })
    }

    @Test
    fun deleteEuAccessCodeByProfileId_matchesRealmBehavior_deletesOnlyOneAccessCode() = runTest(testDispatcher) {
        val accessCode1 = EuAccessCodeEntity("CODE_1", "IT", now + 2.hours, now, "profile1")
        val accessCode2 = EuAccessCodeEntity("CODE_2", "FR", now + 2.hours, now, "profile1")
        val order1 = EuOrderEntity("order1", "IT", now, null, "profile1", "CODE_1", listOf("task1"))
        val order2 = EuOrderEntity("order2", "FR", now, null, "profile1", "CODE_2", listOf("task2"))

        fakeDao.upsertAccessCode(accessCode1)
        fakeDao.upsertAccessCode(accessCode2)
        fakeDao.upsertOrder(order1)
        fakeDao.upsertOrder(order2)

        sut.deleteEuAccessCodeByProfileId("profile1")

        assertNull(fakeDao.accessCodes["CODE_1"])
        assertNotNull(fakeDao.accessCodes["CODE_2"])
        assertNull(fakeDao.orders["order1"]?.euAccessCodeCode)
        assertEquals("CODE_2", fakeDao.orders["order2"]?.euAccessCodeCode)
    }

    @Test
    fun markEventsAsRead_updatesEventStatus() = runTest(testDispatcher) {
        val eventEntity = EuTaskEventEntity(
            id = "event1",
            orderId = "order1",
            type = "ACCESS_CODE_CREATED",
            taskId = "task1",
            createdAt = now,
            isUnread = true
        )
        fakeDao.insertEvents(listOf(eventEntity))

        assertTrue(fakeDao.events["event1"]!!.isUnread)

        sut.markEventsAsRead(listOf("event1"))

        assertFalse(fakeDao.events["event1"]!!.isUnread)
    }

    @Test
    fun addEventToValidOrders_createsEventsAndUpdatesTasksOnValidOrdersOnly() = runTest(testDispatcher) {
        // Seed one valid order (validUntil in the future) and one expired order (validUntil in the past)
        val validAccessCode = EuAccessCodeEntity("VALID_CODE", "IT", now + 1.hours, now, "profile1")
        val expiredAccessCode = EuAccessCodeEntity("EXPIRED_CODE", "IT", now - 1.hours, now, "profile1")

        fakeDao.upsertAccessCode(validAccessCode)
        fakeDao.upsertAccessCode(expiredAccessCode)

        val validOrder = EuOrderEntity("order_valid", "IT", now, null, "profile1", "VALID_CODE", listOf("task1"))
        val expiredOrder = EuOrderEntity("order_expired", "IT", now, null, "profile1", "EXPIRED_CODE", listOf("task1"))

        fakeDao.upsertOrder(validOrder)
        fakeDao.upsertOrder(expiredOrder)

        sut.addEventToValidOrders("profile1", listOf("task2"), EuEventType.TASK_ADDED)

        // Valid order should have task2 added and modified
        val updatedValidOrder = fakeDao.orders["order_valid"]!!
        assertEquals(listOf("task1", "task2"), updatedValidOrder.relatedTaskIds)
        assertNotNull(updatedValidOrder.lastModifiedAt)

        // Expired order should be unchanged
        val updatedExpiredOrder = fakeDao.orders["order_expired"]!!
        assertEquals(listOf("task1"), updatedExpiredOrder.relatedTaskIds)
        assertNull(updatedExpiredOrder.lastModifiedAt)

        // Event entity should only be added for order_valid
        val addedEvents = fakeDao.events.values.filter { it.taskId == "task2" }
        assertEquals(1, addedEvents.size)
        assertTrue(addedEvents.any { it.orderId == "order_valid" && it.type == "TASK_ADDED" })
    }

    @Test
    fun addRedeemedEventIfValidOrderExists_happyPath_appendsTaskRedeemedEvent() = runTest(testDispatcher) {
        val accessCode = EuAccessCodeEntity("VALID_CODE", "IT", now + 1.hours, now, "profile1")
        fakeDao.upsertAccessCode(accessCode)
        val order = EuOrderEntity("order1", "IT", now, null, "profile1", "VALID_CODE", listOf("task1"))
        fakeDao.upsertOrder(order)

        sut.addRedeemedEventIfValidOrderExists("profile1", "IT", "task_redeemed_id")

        val addedEvents = fakeDao.events.values.filter { it.taskId == "task_redeemed_id" }
        assertEquals(1, addedEvents.size)
        val event = addedEvents.first()
        assertEquals("TASK_REDEEMED", event.type)
        assertEquals("order1", event.orderId)
        assertTrue(event.isUnread)
    }

    @Test
    fun addRedeemedEventIfValidOrderExists_noOrderForProfileAndCountry_noEventAdded() = runTest(testDispatcher) {
        val accessCode = EuAccessCodeEntity("VALID_CODE", "FR", now + 1.hours, now, "profile1")
        fakeDao.upsertAccessCode(accessCode)
        val order = EuOrderEntity("order1", "FR", now, null, "profile1", "VALID_CODE", listOf("task1"))
        fakeDao.upsertOrder(order)

        // Requesting for country IT, but order is for FR
        sut.addRedeemedEventIfValidOrderExists("profile1", "IT", "task_redeemed_id")

        val addedEvents = fakeDao.events.values.filter { it.taskId == "task_redeemed_id" }
        assertEquals(0, addedEvents.size)
    }

    @Test
    fun addRedeemedEventIfValidOrderExists_expiredAccessCode_noEventAdded() = runTest(testDispatcher) {
        val accessCode = EuAccessCodeEntity("EXPIRED_CODE", "IT", now - 1.hours, now, "profile1")
        fakeDao.upsertAccessCode(accessCode)
        val order = EuOrderEntity("order1", "IT", now, null, "profile1", "EXPIRED_CODE", listOf("task1"))
        fakeDao.upsertOrder(order)

        sut.addRedeemedEventIfValidOrderExists("profile1", "IT", "task_redeemed_id")

        val addedEvents = fakeDao.events.values.filter { it.taskId == "task_redeemed_id" }
        assertEquals(0, addedEvents.size)
    }

    @Test
    fun addRedeemedEventIfValidOrderExists_noAccessCode_noEventAdded() = runTest(testDispatcher) {
        val order = EuOrderEntity("order1", "IT", now, null, "profile1", null, listOf("task1"))
        fakeDao.upsertOrder(order)

        sut.addRedeemedEventIfValidOrderExists("profile1", "IT", "task_redeemed_id")

        val addedEvents = fakeDao.events.values.filter { it.taskId == "task_redeemed_id" }
        assertEquals(0, addedEvents.size)
    }

    @Test
    fun addRedeemedEventIfValidOrderExists_multipleOrders_picksMostRecent() = runTest(testDispatcher) {
        val olderAccessCode = EuAccessCodeEntity("CODE_OLD", "IT", now + 24.hours, now, "profile1")
        val newerAccessCode = EuAccessCodeEntity("CODE_NEW", "IT", now + 24.hours, now, "profile1")
        fakeDao.upsertAccessCode(olderAccessCode)
        fakeDao.upsertAccessCode(newerAccessCode)

        val olderOrder = EuOrderEntity("order_old", "IT", now - 2.hours, now - 2.hours, "profile1", "CODE_OLD", listOf("task1"))
        val newerOrder = EuOrderEntity("order_new", "IT", now - 1.hours, now - 1.hours, "profile1", "CODE_NEW", listOf("task1"))
        fakeDao.upsertOrder(olderOrder)
        fakeDao.upsertOrder(newerOrder)

        sut.addRedeemedEventIfValidOrderExists("profile1", "IT", "task_redeemed_id")

        val addedEvents = fakeDao.events.values.filter { it.taskId == "task_redeemed_id" }
        assertEquals(1, addedEvents.size)
        assertEquals("order_new", addedEvents.first().orderId)
    }

    @Test
    fun addRedeemedEventIfValidOrderExists_accumulatesEvents() = runTest(testDispatcher) {
        val accessCode = EuAccessCodeEntity("VALID_CODE", "IT", now + 1.hours, now, "profile1")
        fakeDao.upsertAccessCode(accessCode)
        val order = EuOrderEntity("order1", "IT", now, null, "profile1", "VALID_CODE", listOf("task1"))
        fakeDao.upsertOrder(order)

        sut.addRedeemedEventIfValidOrderExists("profile1", "IT", "task-1")
        sut.addRedeemedEventIfValidOrderExists("profile1", "IT", "task-2")

        val addedEvents = fakeDao.events.values.filter { it.orderId == "order1" && it.type == "TASK_REDEEMED" }
        assertEquals(2, addedEvents.size)
        assertTrue(addedEvents.any { it.taskId == "task-1" })
        assertTrue(addedEvents.any { it.taskId == "task-2" })
    }
}
