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
import de.gematik.ti.erp.app.communication.model.payload.CommunicationReplyDeliveryStatusPayloadErpModel
import de.gematik.ti.erp.app.communication.model.payload.CommunicationReplyLinkPayloadErpModel
import de.gematik.ti.erp.app.communication.model.payload.CommunicationReplyPayloadV1ErpModel
import de.gematik.ti.erp.app.communication.model.payload.CommunicationReplyPickupCodeDMCPayloadErpModel
import de.gematik.ti.erp.app.communication.model.payload.CommunicationReplyPickupCodeHRPayloadErpModel
import de.gematik.ti.erp.app.communication.model.payload.CommunicationReplyReservationStatusPayloadErpModel
import de.gematik.ti.erp.app.communication.model.payload.CommunicationReplyTextPayloadErpModel
import de.gematik.ti.erp.app.communication.model.payload.CommunicationSupplyOptionTypeErpModel
import de.gematik.ti.erp.app.communication.model.payload.CommunicationTypeErpModel
import de.gematik.ti.erp.app.messages.domain.model.OrderUseCaseData
import kotlinx.datetime.Clock
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class CommunicationMapperTest {

    private fun buildModel(payload: de.gematik.ti.erp.app.communication.model.payload.CommunicationPayloadErpModel? = null) =
        CommunicationErpModel(
            communicationId = "comm-1",
            orderId = "order-1",
            taskId = "task-1",
            senderTelematikId = "sender-1",
            consumed = false,
            payload = payload,
            profile = CommunicationErpModel.CommunicationProfile.ErxCommunicationReply,
            recipient = "recipient-1",
            profileId = "profile-1",
            timeStamp = Clock.System.now()
        )

    @Test
    fun nullPayload_mapsToDefaultMessage() {
        val message = buildModel(null).toMessage(listOf("task-1"), true)

        assertEquals("comm-1", message.communicationId)
        assertNull(message.content)
        assertNull(message.pickUpCodeHR)
        assertNull(message.pickUpCodeDMC)
        assertNull(message.link)
    }

    @Test
    fun v1Payload_mapsFieldsCorrectly() {
        val payload = CommunicationReplyPayloadV1ErpModel(
            version = 1,
            supplyOptionsType = CommunicationSupplyOptionTypeErpModel.ON_PREMISE,
            infoText = "Your medication is ready",
            url = "https://example.com",
            pickUpCodeHR = "HR123",
            pickUpCodeDMC = "DMC456"
        )

        val message = buildModel(payload).toMessage(listOf("task-1"), true)

        assertEquals("Your medication is ready", message.content)
        assertEquals("https://example.com", message.link)
        assertEquals("HR123", message.pickUpCodeHR)
        assertEquals("DMC456", message.pickUpCodeDMC)
    }

    @Test
    fun v3TextPayload_mapsContent() {
        val payload = CommunicationReplyTextPayloadErpModel(
            transactionID = "tx-1",
            text = "Medication ready for pickup"
        )

        val message = buildModel(payload).toMessage(listOf("task-1"), true)

        assertEquals("Medication ready for pickup", message.content)
        assertNull(message.link)
        assertNull(message.pickUpCodeHR)
        assertNull(message.pickUpCodeDMC)
    }

    @Test
    fun v3LinkPayload_mapsContentAndLink() {
        val payload = CommunicationReplyLinkPayloadErpModel(
            transactionID = "tx-2",
            text = "Track your parcel",
            url = "https://tracking.example.com"
        )

        val message = buildModel(payload).toMessage(listOf("task-1"), true)

        assertEquals("Track your parcel", message.content)
        assertEquals("https://tracking.example.com", message.link)
    }

    @Test
    fun v3PickupCodeHRPayload_mapsContentAndHR() {
        val payload = CommunicationReplyPickupCodeHRPayloadErpModel(
            transactionID = "tx-3",
            text = "Use this code",
            pickUpCode = "CODE-123"
        )

        val message = buildModel(payload).toMessage(listOf("task-1"), true)

        assertEquals("Use this code", message.content)
        assertEquals("CODE-123", message.pickUpCodeHR)
        assertNull(message.pickUpCodeDMC)
    }

    @Test
    fun v3PickupCodeDMCPayload_mapsContentAndDMC() {
        val payload = CommunicationReplyPickupCodeDMCPayloadErpModel(
            transactionID = "tx-4",
            text = "Scan this code",
            pickUpCodeDmc = "DMC-CODE"
        )

        val message = buildModel(payload).toMessage(listOf("task-1"), true)

        assertEquals("Scan this code", message.content)
        assertEquals("DMC-CODE", message.pickUpCodeDMC)
        assertNull(message.pickUpCodeHR)
    }

    @Test
    fun v3ReservationStatusPayload_mapsContent() {
        val payload = CommunicationReplyReservationStatusPayloadErpModel(
            transactionID = "tx-5",
            readyForCollection = de.gematik.ti.erp.app.communication.model.payload.CommunicationAvailabilityResponseErpModel.Immediately
        )

        val message = buildModel(payload).toMessage(listOf("task-1"), true)

        assertEquals("Immediately", message.content)
    }

    @Test
    fun v3DeliveryStatusPayload_mapsContent() {
        val payload = CommunicationReplyDeliveryStatusPayloadErpModel(
            transactionID = "tx-6",
            deliveryStatus = de.gematik.ti.erp.app.communication.model.payload.CommunicationDeliveryStatusErpModel.InTransport,
            text = "Out for delivery"
        )

        val message = buildModel(payload).toMessage(listOf("task-1"), true)

        assertEquals("Out for delivery", message.content)
    }

    @Test
    fun `legacy v1 reply payload is expanded for CommResV3 message timeline`() {
        val payload = CommunicationReplyPayloadV1ErpModel(
            version = 1,
            communicationType = CommunicationTypeErpModel.Text,
            transactionID = "tx-7",
            infoText = "Ihre Bestellung ist bereit",
            url = "https://example.com/tracking",
            pickUpCodeHR = "HR-123"
        )

        val orderDetail = OrderUseCaseData.OrderDetail(
            orderId = "order-1",
            taskDetailedBundles = emptyList(),
            sentOn = Clock.System.now(),
            pharmacy = OrderUseCaseData.Pharmacy(id = "pharmacy-1", name = "Test Pharmacy")
        )

        val message = listOf(buildModel(payload)).toMessageList(orderDetail).single()

        assertEquals(3, message.payloads.size)
        assertTrue(message.payloads.any { it is CommunicationReplyTextPayloadErpModel })
        assertTrue(message.payloads.any { it is CommunicationReplyLinkPayloadErpModel })
        assertTrue(message.payloads.any { it is CommunicationReplyPickupCodeHRPayloadErpModel })
    }

    // Reproduces the MultiReply scenario: three prescriptions redeemed together, each answered with its own
    // unique reply text (Test_Communications KBV 1_3_2 -> MultiReply -> Reply_Unique).
    private fun buildReply(taskId: String, payload: de.gematik.ti.erp.app.communication.model.payload.CommunicationPayloadErpModel?) =
        CommunicationErpModel(
            communicationId = "comm-$taskId",
            orderId = "order-1",
            taskId = taskId,
            senderTelematikId = "sender-1",
            consumed = false,
            payload = payload,
            profile = CommunicationErpModel.CommunicationProfile.ErxCommunicationReply,
            recipient = "recipient-1",
            profileId = "profile-1",
            timeStamp = Clock.System.now()
        )

    private fun buildOrderDetail() = OrderUseCaseData.OrderDetail(
        orderId = "order-1",
        taskDetailedBundles = listOf("task-1", "task-2", "task-3").map {
            OrderUseCaseData.TaskDetailedBundle(prescription = null)
        },
        sentOn = Clock.System.now(),
        pharmacy = OrderUseCaseData.Pharmacy(id = "recipient-1", name = "Adelheid Ulmendorfer")
    )

    @Test
    fun multiReply_uniquePayloadsAreKeptAsSeparateMessages() {
        val replies = listOf(
            buildReply("task-1", CommunicationReplyTextPayloadErpModel(transactionID = "", text = "Medikament A ist fertig")),
            buildReply("task-2", CommunicationReplyTextPayloadErpModel(transactionID = "", text = "Medikament B ist fertig")),
            buildReply("task-3", CommunicationReplyTextPayloadErpModel(transactionID = "", text = "Medikament C ist fertig"))
        )

        val messages = replies.toMessageList(buildOrderDetail())

        assertEquals(3, messages.size)
        assertEquals(
            setOf("Medikament A ist fertig", "Medikament B ist fertig", "Medikament C ist fertig"),
            messages.map { it.content }.toSet()
        )
    }

    @Test
    fun multiReply_nullPayloadsAreNotMergedIntoASingleEmptyMessage() {
        // Simulates all three unique replies failing to be parsed into a payload (e.g. unsupported format),
        // which previously collapsed all of them into a single empty, grouped message via `null == null`.
        val replies = listOf(
            buildReply("task-1", null),
            buildReply("task-2", null),
            buildReply("task-3", null)
        )

        val messages = replies.toMessageList(buildOrderDetail())

        assertEquals(3, messages.size)
        messages.forEach { assertNull(it.content) }
        assertEquals(setOf("comm-task-1", "comm-task-2", "comm-task-3"), messages.map { it.communicationId }.toSet())
    }

    @Test
    fun multiReply_mixOfNullAndUniquePayloadsProducesThreeDistinctMessages() {
        val replies = listOf(
            buildReply("task-1", CommunicationReplyTextPayloadErpModel(transactionID = "", text = "Medikament A ist fertig")),
            buildReply("task-2", null),
            buildReply("task-3", CommunicationReplyTextPayloadErpModel(transactionID = "", text = "Medikament C ist fertig"))
        )

        val messages = replies.toMessageList(buildOrderDetail())

        assertEquals(3, messages.size)
        assertTrue(messages.any { it.content == "Medikament A ist fertig" })
        assertTrue(messages.any { it.content == "Medikament C ist fertig" })
        assertTrue(messages.any { it.content == null })
    }

    @Test
    fun multiReply_samePayloadOnDifferentTaskIdsIsMergedIntoOneMessage() {
        // Same order, same reply text sent to all three redeemed prescriptions (e.g. pharmacy broadcasts one
        // status update to every task). Since the payload is identical, the replies represent the same
        // logical message and must be merged into a single message listing all the involved task-ids.
        val sharedPayload = CommunicationReplyTextPayloadErpModel(transactionID = "", text = "Alle Medikamente sind fertig zur Abholung.")
        val replies = listOf(
            buildReply("task-1", sharedPayload),
            buildReply("task-2", sharedPayload),
            buildReply("task-3", sharedPayload)
        )

        val messages = replies.toMessageList(buildOrderDetail())

        assertEquals(1, messages.size)
        assertEquals("Alle Medikamente sind fertig zur Abholung.", messages.first().content)
        assertEquals(setOf("task-1", "task-2", "task-3"), messages.first().taskIds.toSet())
    }

    @Test
    fun multiReply_nullPayloadOnDifferentTaskIdsIsKeptAsSeparateMessages() {
        // Null-payload replies carry no information that lets us safely correlate them across tasks, so each
        // must remain its own message tied to just its own task-id, even within the same order.
        val replies = listOf(
            buildReply("task-1", null),
            buildReply("task-2", null),
            buildReply("task-3", null)
        )

        val messages = replies.toMessageList(buildOrderDetail())

        assertEquals(3, messages.size)
        assertEquals(setOf("comm-task-1", "comm-task-2", "comm-task-3"), messages.map { it.communicationId }.toSet())
        assertEquals(setOf(listOf("task-1"), listOf("task-2"), listOf("task-3")), messages.map { it.taskIds }.toSet())
    }

    @Test
    fun multiReply_literalDuplicateDeliveryToSameTaskIsDeduplicated() {
        // A genuine technical duplicate: the very same reply content delivered twice to the same task. This is
        // the only case that should still be collapsed into a single message.
        val payload = CommunicationReplyTextPayloadErpModel(transactionID = "", text = "Ihr Medikament ist fertig.")
        val replies = listOf(
            buildReply("task-1", payload).copy(communicationId = "comm-task-1-a"),
            buildReply("task-1", payload).copy(communicationId = "comm-task-1-b")
        )

        val messages = replies.toMessageList(buildOrderDetail())

        assertEquals(1, messages.size)
        assertEquals("Ihr Medikament ist fertig.", messages.first().content)
    }

    @Test
    fun multiReply_samePayloadWithDifferentTransactionIdsOnDifferentTaskIdsIsMergedIntoOneMessage() {
        val replies = listOf(
            buildReply("task-1", CommunicationReplyTextPayloadErpModel(transactionID = "tx-1", text = "Alle Medikamente sind fertig zur Abholung.")),
            buildReply("task-2", CommunicationReplyTextPayloadErpModel(transactionID = "tx-2", text = "Alle Medikamente sind fertig zur Abholung.")),
            buildReply("task-3", CommunicationReplyTextPayloadErpModel(transactionID = "tx-3", text = "Alle Medikamente sind fertig zur Abholung."))
        )

        val messages = replies.toMessageList(buildOrderDetail())

        assertEquals(1, messages.size)
        assertEquals("Alle Medikamente sind fertig zur Abholung.", messages.first().content)
        assertEquals(setOf("task-1", "task-2", "task-3"), messages.first().taskIds.toSet())
    }
}
