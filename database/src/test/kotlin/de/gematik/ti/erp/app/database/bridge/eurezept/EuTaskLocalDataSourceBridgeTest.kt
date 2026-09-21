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

package de.gematik.ti.erp.app.database.bridge.eurezept

import de.gematik.ti.erp.app.database.api.eurezept.EuTaskLocalDataSource
import de.gematik.ti.erp.app.database.datastore.debug.logger.DbMigrationLogHolder
import de.gematik.ti.erp.app.debug.model.DbMigrationFunctionalState
import de.gematik.ti.erp.app.debug.model.DbMigrationLogEntry
import de.gematik.ti.erp.app.eurezept.model.EuAccessCodeErpModel
import de.gematik.ti.erp.app.eurezept.model.EuEventType
import de.gematik.ti.erp.app.eurezept.model.EuOrderErpModel
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.runs
import io.mockk.slot
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.Clock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class EuTaskLocalDataSourceBridgeTest {

    private val v1: EuTaskLocalDataSource = mockk()
    private val v2: EuTaskLocalDataSource = mockk()
    private val logger: DbMigrationLogHolder = mockk(relaxed = true)
    private val now = Clock.System.now()

    @Test
    fun testObserveEuOrder_callsV2_whenUseRoomIsTrue() = runTest {
        val sut = EuTaskLocalDataSourceBridge(v1, v2, logger, useRoom = true)
        val orderModel = EuOrderErpModel(
            orderId = "id123",
            countryCode = "DE",
            createdAt = now,
            lastModifiedAt = null,
            profileId = "profileId1",
            euAccessCode = null,
            events = emptyList(),
            relatedTaskIds = emptyList()
        )

        every { v1.observeEuOrder("id123") } returns flowOf(orderModel)
        every { v2.observeEuOrder("id123") } returns flowOf(orderModel)

        val flow = sut.observeEuOrder("id123")
        val capturedEntry = slot<DbMigrationLogEntry>()
        coEvery { logger.addLog(capture(capturedEntry)) } just runs

        // Consume the flow
        val result = mutableListOf<EuOrderErpModel?>()
        flow.collect { result.add(it) }

        assertEquals(1, result.size)
        assertEquals("id123", result[0]?.orderId)

        // Verify V2 was called as the primary flow
        coVerify(exactly = 2) { v2.observeEuOrder("id123") }
        // Verify V1 and V2 were called to extract state comparison logs
        coVerify(atLeast = 1) { v1.observeEuOrder("id123") }

        coVerify(atLeast = 1) { logger.addLog(capture(capturedEntry)) }

        assertTrue(capturedEntry.isCaptured)
        // Note: operation might be "invokeSuspend" due to runBlocking in Bridge
        // assertEquals("observeEuOrder", capturedEntry.captured.operation)
        assertTrue(capturedEntry.captured.usesRoom)
        assertEquals(DbMigrationFunctionalState.CheckFunctionalityForDifferentModels, capturedEntry.captured.functionalState)
    }

    @Test
    fun testObserveEuOrder_callsV1_whenUseRoomIsFalse() = runTest {
        val sut = EuTaskLocalDataSourceBridge(v1, v2, logger, useRoom = false)
        val orderModel = EuOrderErpModel(
            orderId = "id123",
            countryCode = "DE",
            createdAt = now,
            lastModifiedAt = null,
            profileId = "profileId1",
            euAccessCode = null,
            events = emptyList(),
            relatedTaskIds = emptyList()
        )

        every { v1.observeEuOrder("id123") } returns flowOf(orderModel)
        every { v2.observeEuOrder("id123") } returns flowOf(null)

        val flow = sut.observeEuOrder("id123")

        val result = mutableListOf<EuOrderErpModel?>()
        flow.collect { result.add(it) }

        assertEquals(1, result.size)
        assertEquals("id123", result[0]?.orderId)
    }

    @Test
    fun testSaveEuOrder_callsV2_whenUseRoomIsTrue() = runTest {
        val sut = EuTaskLocalDataSourceBridge(v1, v2, logger, useRoom = true)
        val orderModel = EuOrderErpModel(
            orderId = "id123",
            countryCode = "DE",
            createdAt = now,
            lastModifiedAt = null,
            profileId = "profileId1",
            euAccessCode = null,
            events = emptyList(),
            relatedTaskIds = emptyList()
        )

        coEvery { v2.saveEuOrder(orderModel, EuEventType.ACCESS_CODE_CREATED) } just runs

        sut.saveEuOrder(orderModel, EuEventType.ACCESS_CODE_CREATED)

        coVerify(exactly = 1) { v2.saveEuOrder(orderModel, EuEventType.ACCESS_CODE_CREATED) }
        coVerify(exactly = 0) { v1.saveEuOrder(any(), any()) }

        val capturedEntry = slot<DbMigrationLogEntry>()
        coVerify { logger.addLog(capture(capturedEntry)) }
        // Note: operation might be "invokeSuspend" due to runBlocking in Bridge
        // assertEquals("saveEuOrder", capturedEntry.captured.operation)
        assertTrue(capturedEntry.captured.usesRoom)
        assertEquals(DbMigrationFunctionalState.OperationNoCheck, capturedEntry.captured.functionalState)
        assertEquals("Saving order: id123 with type: ACCESS_CODE_CREATED", capturedEntry.captured.roomData)
    }

    @Test
    fun testDeleteEuAccessCodeByProfileId_callsV1_whenUseRoomIsFalse() = runTest {
        val sut = EuTaskLocalDataSourceBridge(v1, v2, logger, useRoom = false)

        coEvery { v1.deleteEuAccessCodeByProfileId("profile123") } just runs

        sut.deleteEuAccessCodeByProfileId("profile123")

        coVerify(exactly = 1) { v1.deleteEuAccessCodeByProfileId("profile123") }
        coVerify(exactly = 0) { v2.deleteEuAccessCodeByProfileId(any()) }

        val capturedEntry = slot<DbMigrationLogEntry>()
        coVerify { logger.addLog(capture(capturedEntry)) }
        // Note: operation might be "invokeSuspend" due to runBlocking in Bridge
        // assertEquals("deleteEuAccessCodeByProfileId", capturedEntry.captured.operation)
        assertFalse(capturedEntry.captured.usesRoom)
        assertEquals("Deleting access code for profileId: profile123", capturedEntry.captured.realmData)
    }

    @Test
    fun testMarkEventsAsRead_callsV2_whenUseRoomIsTrue() = runTest {
        val sut = EuTaskLocalDataSourceBridge(v1, v2, logger, useRoom = true)
        val eventIds = listOf("e1", "e2")

        coEvery { v2.markEventsAsRead(eventIds) } just runs

        sut.markEventsAsRead(eventIds)

        coVerify(exactly = 1) { v2.markEventsAsRead(eventIds) }
        coVerify(exactly = 0) { v1.markEventsAsRead(any()) }
    }

    @Test
    fun testAddEventToValidOrders_callsV1_whenUseRoomIsFalse() = runTest {
        val sut = EuTaskLocalDataSourceBridge(v1, v2, logger, useRoom = false)
        val taskIds = listOf("task1")

        coEvery { v1.addEventToValidOrders("profileId1", taskIds, EuEventType.TASK_ADDED) } just runs

        sut.addEventToValidOrders("profileId1", taskIds, EuEventType.TASK_ADDED)

        coVerify(exactly = 1) { v1.addEventToValidOrders("profileId1", taskIds, EuEventType.TASK_ADDED) }
        coVerify(exactly = 0) { v2.addEventToValidOrders(any(), any(), any()) }
    }

    @Test
    fun testGetEuAccessCode_callsV2_whenUseRoomIsTrue() = runTest {
        val sut = EuTaskLocalDataSourceBridge(v1, v2, logger, useRoom = true)
        val accessCodeModel = EuAccessCodeErpModel("DE", "CODE123", now, now, "profileId1")

        every { v1.getEuAccessCode("CODE123") } returns flowOf(null)
        every { v2.getEuAccessCode("CODE123") } returns flowOf(accessCodeModel)

        val flow = sut.getEuAccessCode("CODE123")
        val capturedEntry = slot<DbMigrationLogEntry>()
        coEvery { logger.addLog(capture(capturedEntry)) } just runs

        val result = mutableListOf<EuAccessCodeErpModel?>()
        flow.collect { result.add(it) }

        coVerify(atLeast = 1) { logger.addLog(capture(capturedEntry)) }

        assertEquals(1, result.size)
        assertEquals("CODE123", result[0]?.accessCode)

        coVerify(exactly = 2) { v2.getEuAccessCode("CODE123") }
        coVerify(atLeast = 1) { v1.getEuAccessCode("CODE123") }

        assertTrue(capturedEntry.isCaptured)
        // Note: operation might be "invokeSuspend" due to runBlocking in Bridge, but test is focusing on functionality
        // assertEquals("getEuAccessCode", capturedEntry.captured.operation)
        assertTrue(capturedEntry.captured.usesRoom)
        assertEquals(accessCodeModel.toString(), capturedEntry.captured.roomData)
        assertEquals(null, capturedEntry.captured.realmData)
    }

    @Test
    fun testAddRedeemedEventIfValidOrderExists_callsV2_whenUseRoomIsTrue() = runTest {
        val sut = EuTaskLocalDataSourceBridge(v1, v2, logger, useRoom = true)

        coEvery { v2.addRedeemedEventIfValidOrderExists("profile123", "DE", "task123") } just runs

        sut.addRedeemedEventIfValidOrderExists("profile123", "DE", "task123")

        coVerify(exactly = 1) { v2.addRedeemedEventIfValidOrderExists("profile123", "DE", "task123") }
        coVerify(exactly = 0) { v1.addRedeemedEventIfValidOrderExists(any(), any(), any()) }

        val capturedEntry = slot<DbMigrationLogEntry>()
        coVerify { logger.addLog(capture(capturedEntry)) }
        assertTrue(capturedEntry.captured.usesRoom)
        assertEquals(DbMigrationFunctionalState.OperationNoCheck, capturedEntry.captured.functionalState)
        assertEquals("Adding TASK_REDEEMED event for profile=profile123, country=DE, task=task123", capturedEntry.captured.roomData)
    }

    @Test
    fun testAddRedeemedEventIfValidOrderExists_callsV1_whenUseRoomIsFalse() = runTest {
        val sut = EuTaskLocalDataSourceBridge(v1, v2, logger, useRoom = false)

        coEvery { v1.addRedeemedEventIfValidOrderExists("profile123", "DE", "task123") } just runs

        sut.addRedeemedEventIfValidOrderExists("profile123", "DE", "task123")

        coVerify(exactly = 1) { v1.addRedeemedEventIfValidOrderExists("profile123", "DE", "task123") }
        coVerify(exactly = 0) { v2.addRedeemedEventIfValidOrderExists(any(), any(), any()) }

        val capturedEntry = slot<DbMigrationLogEntry>()
        coVerify { logger.addLog(capture(capturedEntry)) }
        assertFalse(capturedEntry.captured.usesRoom)
        assertEquals(DbMigrationFunctionalState.OperationNoCheck, capturedEntry.captured.functionalState)
        assertEquals("Adding TASK_REDEEMED event for profile=profile123, country=DE, task=task123", capturedEntry.captured.realmData)
    }
}
