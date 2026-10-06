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

package de.gematik.ti.erp.app.messages.mapper

import de.gematik.ti.erp.app.communication.model.CommunicationErpModel
import de.gematik.ti.erp.app.communication.model.payload.CommunicationAvailabilityResponseErpModel
import de.gematik.ti.erp.app.communication.model.payload.CommunicationDeliveryStatusErpModel
import de.gematik.ti.erp.app.communication.model.payload.CommunicationReplyDeliveryStatusPayloadErpModel
import de.gematik.ti.erp.app.communication.model.payload.CommunicationReplyPaymentInfoPayloadErpModel
import de.gematik.ti.erp.app.communication.model.payload.CommunicationReplyReservationStatusPayloadErpModel
import de.gematik.ti.erp.app.communication.model.payload.CommunicationSupplyOptionTypeErpModel
import de.gematik.ti.erp.app.core.R
import de.gematik.ti.erp.app.messages.domain.model.MessagesStringProvider
import de.gematik.ti.erp.app.messages.domain.model.OrderUseCaseData
import de.gematik.ti.erp.app.messages.model.InAppMessageStatus
import de.gematik.ti.erp.app.messages.model.LastMessage
import de.gematik.ti.erp.app.messages.model.LastMessageDetails
import de.gematik.ti.erp.app.mocks.DATE_2024_01_01
import de.gematik.ti.erp.app.task.model.TaskErpModel
import de.gematik.ti.erp.app.task.model.TaskStateErpModel
import de.gematik.ti.erp.app.task.model.TaskStatusEnum
import io.mockk.every
import io.mockk.mockk
import org.junit.Test
import kotlin.test.assertEquals

class OrderToInAppMessageMapperTest {
    private val mapper = OrderToInAppMessageMapper(
        object : MessagesStringProvider {
            override fun getString(resourceId: Int, vararg args: Any): String = "message"
        }
    )

    @Test
    fun `maps reservation status next day to ready tomorrow`() {
        val inAppMessage = mapper.map(
            order(latestCommunicationMessage = reservationStatusMessage("NextDay"))
        )

        assertEquals(InAppMessageStatus.READY_TOMORROW, inAppMessage.orderStatus)
    }

    @Test
    fun `maps reservation status immediately to ready for pickup`() {
        val inAppMessage = mapper.map(
            order(latestCommunicationMessage = reservationStatusMessage("Immediately"))
        )

        assertEquals(InAppMessageStatus.READY_FOR_PICKUP, inAppMessage.orderStatus)
    }

    @Test
    fun `maps replies without reservation status to pending`() {
        val inAppMessage = mapper.map(
            order(
                latestCommunicationMessage = LastMessage(
                    lastMessageDetails = LastMessageDetails(
                        content = "Ihre Bestellung wird bearbeitet",
                        pickUpCodeDMC = null,
                        pickUpCodeHR = null,
                        link = null
                    ),
                    profile = CommunicationErpModel.CommunicationProfile.ErxCommunicationReply
                )
            )
        )

        assertEquals(InAppMessageStatus.PENDING, inAppMessage.orderStatus)
    }

    @Test
    fun `maps in progress task to pending when message profile is disp req`() {
        val mockedTask = mockk<TaskErpModel.Synced>()
        every { mockedTask.status } returns TaskStatusEnum.InProgress
        every { mockedTask.taskId } returns "123"
        every { mockedTask.state() } returns mockk<TaskStateErpModel.InProgress>()

        val inAppMessage = mapper.map(
            order(
                latestCommunicationMessage = LastMessage(
                    lastMessageDetails = LastMessageDetails(
                        content = "Dispense Request content",
                        pickUpCodeDMC = null,
                        pickUpCodeHR = null,
                        link = null
                    ),
                    profile = CommunicationErpModel.CommunicationProfile.ErxCommunicationDispReq
                )
            ).copy(prescriptions = listOf(mockedTask))
        )

        assertEquals(InAppMessageStatus.PENDING, inAppMessage.orderStatus)
    }

    @Test
    fun `maps provided task to ready for pickup`() {
        val mockedTask = mockk<TaskErpModel.Synced>()
        val mockedState = mockk<TaskStateErpModel.Provided>()
        every { mockedTask.status } returns TaskStatusEnum.InProgress
        every { mockedTask.taskId } returns "123"
        every { mockedTask.state() } returns mockedState

        val inAppMessage = mapper.map(
            order(
                latestCommunicationMessage = LastMessage(
                    lastMessageDetails = LastMessageDetails(
                        content = "Dispense Request content",
                        pickUpCodeDMC = null,
                        pickUpCodeHR = null,
                        link = null
                    ),
                    profile = CommunicationErpModel.CommunicationProfile.ErxCommunicationDispReq
                )
            ).copy(prescriptions = listOf(mockedTask))
        )

        assertEquals(InAppMessageStatus.READY_FOR_PICKUP, inAppMessage.orderStatus)
    }

    @Test
    fun `special rule 1 - nextDay with ON_PREMISE maps to READY_TOMORROW`() {
        val mockedTask = mockk<TaskErpModel.Synced>()
        every { mockedTask.status } returns TaskStatusEnum.InProgress
        every { mockedTask.taskId } returns "123"
        every { mockedTask.state() } returns mockk<TaskStateErpModel.InProgress>()

        val payload = CommunicationReplyReservationStatusPayloadErpModel(
            readyForCollection = CommunicationAvailabilityResponseErpModel.NextDay
        )
        val inAppMessage = mapper.map(
            order(
                latestCommunicationMessage = LastMessage(
                    lastMessageDetails = LastMessageDetails("NextDay", null, null, null),
                    profile = CommunicationErpModel.CommunicationProfile.ErxCommunicationReply,
                    payload = payload
                )
            ).copy(
                prescriptions = listOf(mockedTask),
                supplyOption = CommunicationSupplyOptionTypeErpModel.ON_PREMISE
            )
        )

        assertEquals(InAppMessageStatus.READY_TOMORROW, inAppMessage.orderStatus)
    }

    @Test
    fun `special rule 1 - nextDay with DELIVERY maps to PENDING`() {
        val mockedTask = mockk<TaskErpModel.Synced>()
        every { mockedTask.status } returns TaskStatusEnum.InProgress
        every { mockedTask.taskId } returns "123"
        every { mockedTask.state() } returns mockk<TaskStateErpModel.InProgress>()

        val payload = CommunicationReplyReservationStatusPayloadErpModel(
            readyForCollection = CommunicationAvailabilityResponseErpModel.NextDay
        )
        val inAppMessage = mapper.map(
            order(
                latestCommunicationMessage = LastMessage(
                    lastMessageDetails = LastMessageDetails("NextDay", null, null, null),
                    profile = CommunicationErpModel.CommunicationProfile.ErxCommunicationReply,
                    payload = payload
                )
            ).copy(
                prescriptions = listOf(mockedTask),
                supplyOption = CommunicationSupplyOptionTypeErpModel.DELIVERY
            )
        )

        assertEquals(InAppMessageStatus.PENDING, inAppMessage.orderStatus)
    }

    @Test
    fun `special rule 2 - immediately with ON_PREMISE maps to READY_FOR_PICKUP`() {
        val mockedTask = mockk<TaskErpModel.Synced>()
        every { mockedTask.status } returns TaskStatusEnum.InProgress
        every { mockedTask.taskId } returns "123"
        every { mockedTask.state() } returns mockk<TaskStateErpModel.InProgress>()

        val payload = CommunicationReplyReservationStatusPayloadErpModel(
            readyForCollection = CommunicationAvailabilityResponseErpModel.Immediately
        )
        val inAppMessage = mapper.map(
            order(
                latestCommunicationMessage = LastMessage(
                    lastMessageDetails = LastMessageDetails("Immediately", null, null, null),
                    profile = CommunicationErpModel.CommunicationProfile.ErxCommunicationReply,
                    payload = payload
                )
            ).copy(
                prescriptions = listOf(mockedTask),
                supplyOption = CommunicationSupplyOptionTypeErpModel.ON_PREMISE
            )
        )

        assertEquals(InAppMessageStatus.READY_FOR_PICKUP, inAppMessage.orderStatus)
    }

    @Test
    fun `special rule 2 - immediately with SHIPMENT maps to READY_FOR_PICKUP`() {
        val mockedTask = mockk<TaskErpModel.Synced>()
        every { mockedTask.status } returns TaskStatusEnum.InProgress
        every { mockedTask.taskId } returns "123"
        every { mockedTask.state() } returns mockk<TaskStateErpModel.InProgress>()

        val payload = CommunicationReplyReservationStatusPayloadErpModel(
            readyForCollection = CommunicationAvailabilityResponseErpModel.Immediately
        )
        val inAppMessage = mapper.map(
            order(
                latestCommunicationMessage = LastMessage(
                    lastMessageDetails = LastMessageDetails("Immediately", null, null, null),
                    profile = CommunicationErpModel.CommunicationProfile.ErxCommunicationReply,
                    payload = payload
                )
            ).copy(
                prescriptions = listOf(mockedTask),
                supplyOption = CommunicationSupplyOptionTypeErpModel.SHIPMENT
            )
        )

        assertEquals(InAppMessageStatus.READY_FOR_PICKUP, inAppMessage.orderStatus)
    }

    @Test
    fun `special rule 3 - delivery incident maps to NOT_AVAILABLE`() {
        val mockedTask = mockk<TaskErpModel.Synced>()
        every { mockedTask.status } returns TaskStatusEnum.InProgress
        every { mockedTask.taskId } returns "123"
        every { mockedTask.state() } returns mockk<TaskStateErpModel.InProgress>()

        val payload = CommunicationReplyDeliveryStatusPayloadErpModel(
            deliveryStatus = CommunicationDeliveryStatusErpModel.Incident
        )
        val inAppMessage = mapper.map(
            order(
                latestCommunicationMessage = LastMessage(
                    lastMessageDetails = LastMessageDetails("Incident", null, null, null),
                    profile = CommunicationErpModel.CommunicationProfile.ErxCommunicationReply,
                    payload = payload
                )
            ).copy(
                prescriptions = listOf(mockedTask),
                supplyOption = CommunicationSupplyOptionTypeErpModel.SHIPMENT
            )
        )

        assertEquals(InAppMessageStatus.NOT_AVAILABLE, inAppMessage.orderStatus)
    }

    @Test
    fun `special rule with masked payload - last message is payment but latestReservationPayload is set`() {
        val mockedTask = mockk<TaskErpModel.Synced>()
        every { mockedTask.status } returns TaskStatusEnum.InProgress
        every { mockedTask.taskId } returns "123"
        every { mockedTask.state() } returns mockk<TaskStateErpModel.InProgress>()

        val payload = CommunicationReplyReservationStatusPayloadErpModel(
            readyForCollection = CommunicationAvailabilityResponseErpModel.Immediately
        )
        val inAppMessage = mapper.map(
            order(
                latestCommunicationMessage = LastMessage(
                    lastMessageDetails = LastMessageDetails("Payment done", null, null, null),
                    profile = CommunicationErpModel.CommunicationProfile.ErxCommunicationReply,
                    payload = CommunicationReplyPaymentInfoPayloadErpModel(),
                    latestReservationPayload = payload
                )
            ).copy(
                prescriptions = listOf(mockedTask),
                supplyOption = CommunicationSupplyOptionTypeErpModel.ON_PREMISE
            )
        )

        assertEquals(InAppMessageStatus.READY_FOR_PICKUP, inAppMessage.orderStatus)
    }

    @Test
    fun `special rule 1 - nextDay with null supplyOption maps to READY_TOMORROW`() {
        val mockedTask = mockk<TaskErpModel.Synced>()
        every { mockedTask.status } returns TaskStatusEnum.InProgress
        every { mockedTask.taskId } returns "123"
        every { mockedTask.state() } returns mockk<TaskStateErpModel.InProgress>()

        val payload = CommunicationReplyReservationStatusPayloadErpModel(
            readyForCollection = CommunicationAvailabilityResponseErpModel.NextDay
        )
        val inAppMessage = mapper.map(
            order(
                latestCommunicationMessage = LastMessage(
                    lastMessageDetails = LastMessageDetails("NextDay", null, null, null),
                    profile = CommunicationErpModel.CommunicationProfile.ErxCommunicationReply,
                    payload = payload
                )
            ).copy(
                prescriptions = listOf(mockedTask),
                supplyOption = null
            )
        )

        assertEquals(InAppMessageStatus.READY_TOMORROW, inAppMessage.orderStatus)
    }

    @Test
    fun `special rule 1 - nextDay with UNKNOWN supplyOption maps to READY_TOMORROW`() {
        val mockedTask = mockk<TaskErpModel.Synced>()
        every { mockedTask.status } returns TaskStatusEnum.InProgress
        every { mockedTask.taskId } returns "123"
        every { mockedTask.state() } returns mockk<TaskStateErpModel.InProgress>()

        val payload = CommunicationReplyReservationStatusPayloadErpModel(
            readyForCollection = CommunicationAvailabilityResponseErpModel.NextDay
        )
        val inAppMessage = mapper.map(
            order(
                latestCommunicationMessage = LastMessage(
                    lastMessageDetails = LastMessageDetails("NextDay", null, null, null),
                    profile = CommunicationErpModel.CommunicationProfile.ErxCommunicationReply,
                    payload = payload
                )
            ).copy(
                prescriptions = listOf(mockedTask),
                supplyOption = CommunicationSupplyOptionTypeErpModel.UNKNOWN
            )
        )

        assertEquals(InAppMessageStatus.READY_TOMORROW, inAppMessage.orderStatus)
    }

    private fun order(latestCommunicationMessage: LastMessage?) = OrderUseCaseData.Order(
        orderId = "order-1",
        prescriptions = emptyList(),
        sentOn = DATE_2024_01_01,
        pharmacy = OrderUseCaseData.Pharmacy(
            id = "pharmacy-id",
            name = "Test Pharmacy"
        ),
        hasUnreadMessages = false,
        latestCommunicationMessage = latestCommunicationMessage
    )

    @Test
    fun `maps message text for NextDay reservation payload`() {
        val customMapper = OrderToInAppMessageMapper(
            object : MessagesStringProvider {
                override fun getString(resourceId: Int, vararg args: Any): String = "resourceId:$resourceId"
            }
        )
        val payload = CommunicationReplyReservationStatusPayloadErpModel(
            readyForCollection = CommunicationAvailabilityResponseErpModel.NextDay
        )
        val inAppMessage = customMapper.map(
            order(
                latestCommunicationMessage = LastMessage(
                    lastMessageDetails = LastMessageDetails("NextDay", null, null, null),
                    profile = CommunicationErpModel.CommunicationProfile.ErxCommunicationReply,
                    payload = payload
                )
            )
        )

        assertEquals("resourceId:${R.string.message_card_reservation_state_nextday_am_title}", inAppMessage.text)
    }

    @Test
    fun `maps message text for Immediately reservation payload`() {
        val customMapper = OrderToInAppMessageMapper(
            object : MessagesStringProvider {
                override fun getString(resourceId: Int, vararg args: Any): String = "resourceId:$resourceId"
            }
        )
        val payload = CommunicationReplyReservationStatusPayloadErpModel(
            readyForCollection = CommunicationAvailabilityResponseErpModel.Immediately
        )
        val inAppMessage = customMapper.map(
            order(
                latestCommunicationMessage = LastMessage(
                    lastMessageDetails = LastMessageDetails("Immediately", null, null, null),
                    profile = CommunicationErpModel.CommunicationProfile.ErxCommunicationReply,
                    payload = payload
                )
            )
        )

        assertEquals("resourceId:${R.string.message_card_pickupcode_title}", inAppMessage.text)
    }

    private fun reservationStatusMessage(content: String) = LastMessage(
        lastMessageDetails = LastMessageDetails(
            content = content,
            pickUpCodeDMC = null,
            pickUpCodeHR = null,
            link = null
        ),
        profile = CommunicationErpModel.CommunicationProfile.ErxCommunicationReply
    )
}
