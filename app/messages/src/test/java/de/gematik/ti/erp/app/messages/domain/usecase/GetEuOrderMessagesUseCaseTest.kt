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

package de.gematik.ti.erp.app.messages.domain.usecase

import app.cash.turbine.test
import de.gematik.ti.erp.app.eurezept.model.EuEventType
import de.gematik.ti.erp.app.eurezept.model.EuOrder
import de.gematik.ti.erp.app.eurezept.model.EuTaskEvent
import de.gematik.ti.erp.app.eurezept.repository.EuRepository
import de.gematik.ti.erp.app.messages.mappers.EuOrderToMessagesMapper
import de.gematik.ti.erp.app.messages.ui.model.EuOrderMessageUiModel
import de.gematik.ti.erp.app.mocks.order.model.MOCK_SYNCED_TASK_DATA_01_NEW
import de.gematik.ti.erp.app.prescription.repository.PrescriptionRepository
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlinx.datetime.Instant
import org.junit.After
import org.junit.Before
import kotlin.test.Test
import kotlin.test.assertEquals

@OptIn(ExperimentalCoroutinesApi::class)
class GetEuOrderMessagesUseCaseTest {

    private val euRepository = mockk<EuRepository>()
    private val prescriptionRepository = mockk<PrescriptionRepository>(relaxed = true)
    private val mapper = mockk<EuOrderToMessagesMapper>()
    private val testDispatcher = StandardTestDispatcher()
    private val testScope = TestScope(testDispatcher)
    private val orderId = "order-1"

    private val useCase = GetEuOrderMessagesUseCase(
        euRepository = euRepository,
        prescriptionRepository = prescriptionRepository,
        mapper = mapper,
        dispatcher = testDispatcher
    )

    private fun event(
        id: String,
        taskId: String,
        type: EuEventType,
        time: Instant
    ) = EuTaskEvent(
        id = id,
        type = type,
        taskId = taskId,
        createdAt = time,
        isUnread = true
    )

    private fun order(
        events: List<EuTaskEvent>
    ) = EuOrder(
        orderId = orderId,
        countryCode = "BE",
        createdAt = Instant.parse("2025-11-25T15:00:00Z"),
        lastModifiedAt = null,
        profileId = "PROFILE-1",
        euAccessCode = null,
        events = events,
        relatedTaskIds = emptyList()
    )

    @Before
    fun setup() {
        MockKAnnotations.init(this)
        Dispatchers.setMain(testDispatcher)
        every { prescriptionRepository.getTask(any()) } returns MOCK_SYNCED_TASK_DATA_01_NEW
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `invoke maps only events inside thread window`() = testScope.runTest {
        val threadStart = Instant.parse("2025-11-25T15:00:00Z")
        val inWindowTime = Instant.parse("2025-11-25T15:00:10Z")
        val outsideWindowTime = Instant.parse("2025-11-25T16:00:00Z")
        val threadEnd = Instant.parse("2025-11-25T15:00:30Z")

        val inWindowEvent = event(
            id = "E-IN",
            taskId = "T1",
            type = EuEventType.TASK_ADDED,
            time = inWindowTime
        )

        val outsideWindowEvent = event(
            id = "E-OUT",
            taskId = "T2",
            type = EuEventType.TASK_REMOVED,
            time = outsideWindowTime
        )

        val euOrder = order(listOf(inWindowEvent, outsideWindowEvent))

        every { euRepository.observeEuOrder(orderId) } returns flowOf(euOrder)

        val expectedMessages = listOf(mockk<EuOrderMessageUiModel>())
        val capturedEvents = mutableListOf<List<EuTaskEvent>>()

        every {
            mapper.map(
                order = any(),
                threadEvents = capture(capturedEvents),
                mappedTaskIdsToNames = any()
            )
        } returns expectedMessages

        advanceUntilIdle()

        useCase.invoke(
            orderId = orderId,
            threadStart = threadStart,
            threadEnd = threadEnd
        ).test {
            val item = awaitItem()
            assertEquals(expectedMessages, item)
            assertEquals(1, capturedEvents.size)
            assertEquals(listOf(inWindowEvent), capturedEvents.first())
            awaitComplete()
        }
    }

    @Test
    fun `invoke returns empty list when order is null`() = testScope.runTest {
        val orderId = "order-null"

        every { euRepository.observeEuOrder(orderId) } returns flowOf(null)

        useCase(
            orderId = orderId,
            threadStart = Instant.parse("2025-11-25T15:00:00Z"),
            threadEnd = Instant.parse("2025-11-25T15:10:00Z")
        ).test {
            val item = awaitItem()
            assertEquals(emptyList(), item)
            awaitComplete()
        }
    }
}
