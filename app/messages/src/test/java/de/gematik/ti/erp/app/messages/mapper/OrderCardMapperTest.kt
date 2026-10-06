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

import de.gematik.ti.erp.app.communication.model.payload.CommunicationReplyLinkPayloadErpModel
import de.gematik.ti.erp.app.communication.model.payload.CommunicationReplyPayloadV1ErpModel
import de.gematik.ti.erp.app.communication.model.payload.CommunicationReplyPickupCodeDMCPayloadErpModel
import de.gematik.ti.erp.app.communication.model.payload.CommunicationReplyTextPayloadErpModel
import de.gematik.ti.erp.app.communication.model.payload.CommunicationSupplyOptionTypeErpModel
import de.gematik.ti.erp.app.communication.model.payload.CommunicationTypeErpModel
import de.gematik.ti.erp.app.communication.model.payload.DispenseRequestCommunicationPayloadV1ErpModel
import de.gematik.ti.erp.app.communication.model.payload.DispenseRequestCommunicationPayloadV3ErpModel
import de.gematik.ti.erp.app.communication.model.payload.DispenseRequestDeliveryPayloadErpModel
import de.gematik.ti.erp.app.communication.model.payload.DispenseRequestReservationPayloadErpModel
import de.gematik.ti.erp.app.communication.model.payload.DispenseRequestShipmentPayloadErpModel
import de.gematik.ti.erp.app.messages.domain.model.OrderUseCaseData
import kotlinx.datetime.Clock
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.test.assertIs

class OrderCardMapperTest {

    @Test
    fun `dispenseSupplyOption resolves correctly for all payload models`() {
        val v1 = DispenseRequestCommunicationPayloadV1ErpModel(
            supplyOptionsType = CommunicationSupplyOptionTypeErpModel.DELIVERY
        )
        val v3 = DispenseRequestCommunicationPayloadV3ErpModel(
            supplyOptionsType = CommunicationSupplyOptionTypeErpModel.SHIPMENT
        )
        val reservation = DispenseRequestReservationPayloadErpModel(
            supplyOptionsType = CommunicationSupplyOptionTypeErpModel.ON_PREMISE
        )
        val delivery = DispenseRequestDeliveryPayloadErpModel()
        val shipment = DispenseRequestShipmentPayloadErpModel()

        assertEquals(CommunicationSupplyOptionTypeErpModel.DELIVERY, v1.dispenseSupplyOption())
        assertEquals(CommunicationSupplyOptionTypeErpModel.SHIPMENT, v3.dispenseSupplyOption())
        assertEquals(CommunicationSupplyOptionTypeErpModel.ON_PREMISE, reservation.dispenseSupplyOption())
        assertEquals(CommunicationSupplyOptionTypeErpModel.DELIVERY, delivery.dispenseSupplyOption())
        assertEquals(CommunicationSupplyOptionTypeErpModel.SHIPMENT, shipment.dispenseSupplyOption())
        assertEquals(CommunicationSupplyOptionTypeErpModel.UNKNOWN, null.dispenseSupplyOption())
    }

    @Test
    fun `userVisibleText resolves text for DispenseRequestCommunicationPayloadV3ErpModel`() {
        val payloadWithText = DispenseRequestCommunicationPayloadV3ErpModel(
            text = "Please leave at front door"
        )
        val payloadWithBlankText = DispenseRequestCommunicationPayloadV3ErpModel(
            text = "   "
        )
        val payloadWithoutText = DispenseRequestCommunicationPayloadV3ErpModel(
            text = null
        )

        assertEquals("Please leave at front door", payloadWithText.userVisibleText())
        assertNull(payloadWithBlankText.userVisibleText())
        assertNull(payloadWithoutText.userVisibleText())
    }

    @Test
    fun `legacy v1 reply payload splits into CommResV3 renderable payloads`() {
        val payload = CommunicationReplyPayloadV1ErpModel(
            version = 1,
            communicationType = CommunicationTypeErpModel.Text,
            transactionID = "tx-1",
            infoText = "Bitte klingeln",
            url = "https://example.com",
            pickUpCodeDMC = "DMC-123"
        )

        val actual = payload.toCommResV3Payloads()

        assertEquals(3, actual.size)
        assertIs<CommunicationReplyTextPayloadErpModel>(actual[0])
        assertIs<CommunicationReplyLinkPayloadErpModel>(actual[1])
        assertIs<CommunicationReplyPickupCodeDMCPayloadErpModel>(actual[2])
        assertEquals("Bitte klingeln", (actual[0] as CommunicationReplyTextPayloadErpModel).text)
        assertEquals("", (actual[1] as CommunicationReplyLinkPayloadErpModel).text)
        assertEquals("DMC-123", (actual[2] as CommunicationReplyPickupCodeDMCPayloadErpModel).pickUpCodeDmc)
    }

    @Test
    fun `version 1 mapped link payload splits text from link for CommResV3`() {
        val payload = CommunicationReplyLinkPayloadErpModel(
            version = 1,
            communicationType = CommunicationTypeErpModel.Link,
            transactionID = "tx-2",
            text = "Bitte hier öffnen",
            url = "https://example.com"
        )

        val actual = payload.toCommResV3Payloads()

        assertEquals(2, actual.size)
        assertIs<CommunicationReplyTextPayloadErpModel>(actual[0])
        assertIs<CommunicationReplyLinkPayloadErpModel>(actual[1])
        assertEquals("Bitte hier öffnen", (actual[0] as CommunicationReplyTextPayloadErpModel).text)
        assertEquals("", (actual[1] as CommunicationReplyLinkPayloadErpModel).text)
    }

    @Test
    fun `empty pharmacy reply is kept in CommResV3 timeline`() {
        val time = Clock.System.now()
        val order = OrderUseCaseData.OrderDetail(
            orderId = "order-1",
            taskDetailedBundles = emptyList(),
            sentOn = time,
            pharmacy = OrderUseCaseData.Pharmacy(id = "pharmacy-1", name = "Test Pharmacy")
        )
        val emptyMessage = OrderUseCaseData.Message(
            communicationId = "message-1",
            sentOn = time,
            content = null,
            pickUpCodeDMC = null,
            pickUpCodeHR = null,
            link = null,
            consumed = false,
            prescriptions = emptyList()
        )

        val actual = order.toCommResV3OrderModel(messages = listOf(emptyMessage))

        assertEquals(2, actual.cards.size)
        assertTrue(actual.cards.any { it is OrderUseCaseData.OrderCardErpModel.PharmacyReplyCard })
    }
}
