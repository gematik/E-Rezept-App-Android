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

import android.content.Context
import app.cash.turbine.test
import de.gematik.ti.erp.app.eurezept.model.EuEventType
import de.gematik.ti.erp.app.eurezept.model.EuOrder
import de.gematik.ti.erp.app.eurezept.model.EuTaskEvent
import de.gematik.ti.erp.app.eurezept.repository.EuRepository
import de.gematik.ti.erp.app.messages.mappers.EuOrderToMessagesMapper
import de.gematik.ti.erp.app.messages.model.CommunicationProfile
import de.gematik.ti.erp.app.messages.model.InAppMessage
import de.gematik.ti.erp.app.messages.model.LastMessage
import de.gematik.ti.erp.app.messages.model.LastMessageDetails
import de.gematik.ti.erp.app.messages.ui.model.EuOrderMessageUiModel
import de.gematik.ti.erp.app.prescription.repository.PrescriptionRepository
import de.gematik.ti.erp.app.timestate.TimeState
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.Instant
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.time.Duration.Companion.milliseconds

class GetLatestEuOrderMessageAsInAppMessageUseCaseTest {
    private val euRepository = mockk<EuRepository>()
    private val prescriptionRepository = mockk<PrescriptionRepository>(relaxed = true)
    private val mapper = mockk<EuOrderToMessagesMapper>()
    private val dispatcher = StandardTestDispatcher()
    private val context = mockk<Context>(relaxed = true)
    private val testCoroutineScope = TestScope(dispatcher)
    private val useCase = GetLatestEuOrderMessageAsInAppMessageUseCase(
        euRepository = euRepository,
        prescriptionRepository = prescriptionRepository,
        mapper = mapper,
        context = context,
        dispatcher = dispatcher
    )

    private fun event(
        time: Instant
    ) = EuTaskEvent(
        id = "EV_B",
        type = EuEventType.TASK_ADDED,
        taskId = "T1",
        createdAt = time,
        isUnread = true
    )

    private fun order(
        id: String,
        created: Instant,
        modified: Instant?
    ) = EuOrder(
        orderId = id,
        countryCode = "BE",
        createdAt = created,
        lastModifiedAt = modified,
        profileId = "P1",
        euAccessCode = null,
        events = emptyList(),
        relatedTaskIds = listOf("T1")
    )

    @Test
    fun `usecase returns latest message for latest updated order`() = testCoroutineScope.runTest {
        val fixedNow = Instant.parse("2025-11-26T14:27:14.308457Z")

        // Order A (older)
        val orderA = order(
            id = "A",
            created = fixedNow.minus(10_000.milliseconds),
            modified = fixedNow.minus(5_000.milliseconds)
        )

        // Order B (newer)
        val orderB = order(
            id = "B",
            created = fixedNow.minus(2_000.milliseconds),
            modified = fixedNow.minus(1_000.milliseconds)
        )

        // This is the event inside B
        val eventB = event(fixedNow.minus(500.milliseconds))

        // Attach events
        val latestOrder = orderB.copy(events = listOf(eventB))

        every { euRepository.observeAllEuOrders() } returns flowOf(listOf(orderA, latestOrder))

        val expectedUiModel = mockk<EuOrderMessageUiModel>(relaxed = true)

        every {
            mapper.map(
                order = latestOrder,
                threadEvents = listOf(eventB),
                mappedTaskIdsToNames = any()
            )
        } returns listOf(expectedUiModel)

        // Execute
        useCase().test {
            val item = awaitItem().first()
            assertEquals(message.messageProfile, item.messageProfile)
            assertEquals(message.threadStart, item.threadStart)
            assertEquals(message.threadEnd, item.threadEnd)
            assertEquals(message.isUnread, item.isUnread)
            cancelAndIgnoreRemainingEvents()
        }
    }

    companion object {
        private val message = InAppMessage(
            id = "",
            from = "",
            text = "",
            timeState = TimeState.ShowDate(
                timestamp = Instant.parse("2025-11-26T14:27:14.308457Z")
            ),
            prescriptionsCount = 0,
            tag = "",
            isUnread = true,
            lastMessage = LastMessage(
                profile = CommunicationProfile.EuOrder,
                lastMessageDetails = LastMessageDetails(
                    content = "last message",
                    pickUpCodeDMC = null,
                    pickUpCodeHR = null,
                    link = null
                )
            ),
            messageProfile = CommunicationProfile.EuOrder,
            version = "",
            threadOrderId = "",
            threadStart = Instant.parse("2025-11-26T14:27:13.808457Z"),
            threadEnd = Instant.parse("2025-12-03T14:27:13.808457Z")
        )
    }
}
