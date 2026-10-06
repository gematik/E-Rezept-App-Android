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

package de.gematik.ti.erp.app.fhir.communication.parser

import de.gematik.ti.erp.app.communication.model.payload.CommunicationDeliveryStatusErpModel
import de.gematik.ti.erp.app.communication.model.payload.CommunicationReplyDeliveryStatusPayloadErpModel
import de.gematik.ti.erp.app.communication.model.payload.CommunicationReplyPickupCodeHRPayloadErpModel
import de.gematik.ti.erp.app.data.dispense_request_delivery_full_payload_v1
import de.gematik.ti.erp.app.data.dispense_request_delivery_full_payload_v3
import de.gematik.ti.erp.app.data.dispense_request_delivery_minimal_payload_v1
import de.gematik.ti.erp.app.data.dispense_request_delivery_minimal_payload_v3
import de.gematik.ti.erp.app.data.dispense_request_reservation_full_payload_v3
import de.gematik.ti.erp.app.data.dispense_request_reservation_minimal_payload_v3
import de.gematik.ti.erp.app.data.dispense_request_shipment_full_payload_v3
import de.gematik.ti.erp.app.data.dispense_request_shipment_minimal_payload_v3
import de.gematik.ti.erp.app.data.info_request_full_payload_v3
import de.gematik.ti.erp.app.data.info_request_minimal_payload_v3
import de.gematik.ti.erp.app.data.reply_delivery_status_full_payload_v3
import de.gematik.ti.erp.app.data.reply_delivery_status_minimal_payload_v3
import de.gematik.ti.erp.app.data.reply_full_payload_v1
import de.gematik.ti.erp.app.data.reply_link_full_payload_v3
import de.gematik.ti.erp.app.data.reply_minimal_payload_v1
import de.gematik.ti.erp.app.data.reply_payment_info_full_payload_v3
import de.gematik.ti.erp.app.data.reply_pickup_code_hr_and_link_payload_v1
import de.gematik.ti.erp.app.data.reply_without_supply_options_type_payload_v1
import de.gematik.ti.erp.app.data.reply_payment_info_minimal_payload_v3
import de.gematik.ti.erp.app.data.reply_pickup_code_dmc_full_payload_v3
import de.gematik.ti.erp.app.data.reply_pickup_code_dmc_minimal_payload_v3
import de.gematik.ti.erp.app.data.reply_pickup_code_full_payload_v3
import de.gematik.ti.erp.app.data.reply_pickup_code_minimal_payload_v3
import de.gematik.ti.erp.app.data.reply_reservation_status_immediately_full_payload_v3
import de.gematik.ti.erp.app.data.reply_reservation_status_next_day_full_payload_v3
import de.gematik.ti.erp.app.data.reply_text_full_payload_v3
import de.gematik.ti.erp.app.fhir.communication.mocks.FhirCommunicationPayloadTestData
import org.junit.Test
import kotlin.test.assertEquals

class CommunicationPayloadParserTest {

    @Test
    fun `dispense_request_delivery_full_payload_v1 parses payload`() {
        val expected = FhirCommunicationPayloadTestData.dispenseRequestDeliveryFullPayloadV1
        val actual = CommunicationPayloadParser.extract(dispense_request_delivery_full_payload_v1, isRequest = true)
        assertEquals(expected, actual)
    }

    @Test
    fun `dispense_request_delivery_minimal_payload_v1 parses payload`() {
        val expected = FhirCommunicationPayloadTestData.dispenseRequestDeliveryMinimalPayloadV1
        val actual = CommunicationPayloadParser.extract(dispense_request_delivery_minimal_payload_v1, isRequest = true)
        assertEquals(expected, actual)
    }

    @Test
    fun `reply_full_payload_v1 parses payload`() {
        val expected = FhirCommunicationPayloadTestData.communicationReplyFullPayloadV1
        val actual = CommunicationPayloadParser.extract(reply_full_payload_v1)
        assertEquals(expected, actual)
    }

    @Test
    fun `reply_minimal_payload_v1 parses payload`() {
        val expected = FhirCommunicationPayloadTestData.communicationReplyMinimalPayloadV1
        val actual = CommunicationPayloadParser.extract(reply_minimal_payload_v1)
        assertEquals(expected, actual)
    }

    @Test
    fun `reply_pickup_code_hr_and_link_payload_v1 keeps both pickUpCodeHR and url instead of dropping one`() {
        val expected = FhirCommunicationPayloadTestData.communicationReplyPickupCodeHrAndLinkPayloadV1
        val actual = CommunicationPayloadParser.extract(reply_pickup_code_hr_and_link_payload_v1)
        assertEquals(expected, actual)
    }

    @Test
    fun `reply_without_supply_options_type_payload_v1 parses payload even when supplyOptionsType is missing`() {
        // Real-world pharmacy replies do not always include supplyOptionsType (it is only mandatory for
        // requests, not replies, per the KBV spec). This must not crash with a MissingFieldException.
        val expected = FhirCommunicationPayloadTestData.communicationReplyWithoutSupplyOptionsTypePayloadV1
        val actual = CommunicationPayloadParser.extract(reply_without_supply_options_type_payload_v1)
        assertEquals(expected, actual)
    }

    @Test
    fun `dispense_request_delivery_full_payload_v3 parses payload`() {
        val expected = FhirCommunicationPayloadTestData.dispenseRequestDeliveryFullPayloadV3
        val actual = CommunicationPayloadParser.extract(dispense_request_delivery_full_payload_v3, isRequest = true)
        assertEquals(expected, actual)
    }

    @Test
    fun `dispense_request_delivery_minimal_payload_v3 parses payload`() {
        val expected = FhirCommunicationPayloadTestData.dispenseRequestDeliveryMinimalPayloadV3
        val actual = CommunicationPayloadParser.extract(dispense_request_delivery_minimal_payload_v3, isRequest = true)
        assertEquals(expected, actual)
    }

    @Test
    fun `dispense_request_shipment_full_payload_v3 parses payload`() {
        val expected = FhirCommunicationPayloadTestData.dispenseRequestShipmentFullPayloadV3
        val actual = CommunicationPayloadParser.extract(dispense_request_shipment_full_payload_v3, isRequest = true)
        assertEquals(expected, actual)
    }

    @Test
    fun `dispense_request_shipment_minimal_payload_v3 parses payload`() {
        val expected = FhirCommunicationPayloadTestData.dispenseRequestShipmentMinimalPayloadV3
        val actual = CommunicationPayloadParser.extract(dispense_request_shipment_minimal_payload_v3, isRequest = true)
        assertEquals(expected, actual)
    }

    @Test
    fun `dispense_request_reservation_full_payload_v3 parses payload`() {
        val expected = FhirCommunicationPayloadTestData.dispenseRequestReservationFullPayloadV3
        val actual = CommunicationPayloadParser.extract(dispense_request_reservation_full_payload_v3, isRequest = true)
        assertEquals(expected, actual)
    }

    @Test
    fun `dispense_request_reservation_minimal_payload_v3 parses payload`() {
        val expected = FhirCommunicationPayloadTestData.dispenseRequestReservationMinimalPayloadV3
        val actual = CommunicationPayloadParser.extract(dispense_request_reservation_minimal_payload_v3, isRequest = true)
        assertEquals(expected, actual)
    }

    @Test
    fun `info_request_full_payload_v3 parses payload`() {
        val expected = FhirCommunicationPayloadTestData.infoRequestFullPayloadV3
        val actual = CommunicationPayloadParser.extract(info_request_full_payload_v3, isRequest = true)
        assertEquals(expected, actual)
    }

    @Test
    fun `info_request_minimal_payload_v3 parses payload`() {
        val expected = FhirCommunicationPayloadTestData.infoRequestMinimalPayloadV3
        val actual = CommunicationPayloadParser.extract(info_request_minimal_payload_v3, isRequest = true)
        assertEquals(expected, actual)
    }

    @Test
    fun `reply_text_full_payload_v3 parses payload`() {
        val expected = FhirCommunicationPayloadTestData.replyTextFullPayloadV3
        val actual = CommunicationPayloadParser.extract(reply_text_full_payload_v3)
        assertEquals(expected, actual)
    }

    @Test
    fun `reply_link_full_payload_v3 parses payload`() {
        val expected = FhirCommunicationPayloadTestData.replyLinkFullPayloadV3
        val actual = CommunicationPayloadParser.extract(reply_link_full_payload_v3)
        assertEquals(expected, actual)
    }

    @Test
    fun `reply_reservation_status_immediately_full_payload_v3 parses payload`() {
        val expected = FhirCommunicationPayloadTestData.replyReservationStatusImmediatelyFullPayloadV3
        val actual = CommunicationPayloadParser.extract(reply_reservation_status_immediately_full_payload_v3)
        assertEquals(expected, actual)
    }

    @Test
    fun `reply_reservation_status_next_day_full_payload_v3 parses payload`() {
        val expected = FhirCommunicationPayloadTestData.replyReservationStatusNextDayFullPayloadV3
        val actual = CommunicationPayloadParser.extract(reply_reservation_status_next_day_full_payload_v3)
        assertEquals(expected, actual)
    }

    @Test
    fun `reply_pickup_code_full_payload_v3 parses payload`() {
        val expected = FhirCommunicationPayloadTestData.replyPickupCodeFullPayloadV3
        val actual = CommunicationPayloadParser.extract(reply_pickup_code_full_payload_v3)
        assertEquals(expected, actual)
    }

    @Test
    fun `reply_pickup_code_minimal_payload_v3 parses payload`() {
        val expected = FhirCommunicationPayloadTestData.replyPickupCodeMinimalPayloadV3
        val actual = CommunicationPayloadParser.extract(reply_pickup_code_minimal_payload_v3)
        assertEquals(expected, actual)
    }

    @Test
    fun `reply_pickup_code_dmc_full_payload_v3 parses payload`() {
        val expected = FhirCommunicationPayloadTestData.replyPickupCodeDmcFullPayloadV3
        val actual = CommunicationPayloadParser.extract(reply_pickup_code_dmc_full_payload_v3)
        assertEquals(expected, actual)
    }

    @Test
    fun `reply_pickup_code_dmc_minimal_payload_v3 parses payload`() {
        val expected = FhirCommunicationPayloadTestData.replyPickupCodeDmcMinimalPayloadV3
        val actual = CommunicationPayloadParser.extract(reply_pickup_code_dmc_minimal_payload_v3)
        assertEquals(expected, actual)
    }

    @Test
    fun `reply_delivery_status_full_payload_v3 parses payload`() {
        val expected = FhirCommunicationPayloadTestData.replyDeliveryStatusFullPayloadV3
        val actual = CommunicationPayloadParser.extract(reply_delivery_status_full_payload_v3)
        assertEquals(expected, actual)
    }

    @Test
    fun `reply_delivery_status_minimal_payload_v3 parses payload`() {
        val expected = FhirCommunicationPayloadTestData.replyDeliveryStatusMinimalPayloadV3
        val actual = CommunicationPayloadParser.extract(reply_delivery_status_minimal_payload_v3)
        assertEquals(expected, actual)
    }

    @Test
    fun `reply_payment_info_full_payload_v3 parses payload`() {
        val expected = FhirCommunicationPayloadTestData.replyPaymentInfoFullPayloadV3
        val actual = CommunicationPayloadParser.extract(reply_payment_info_full_payload_v3)
        assertEquals(expected, actual)
    }

    @Test
    fun `reply_payment_info_minimal_payload_v3 parses payload`() {
        val expected = FhirCommunicationPayloadTestData.replyPaymentInfoMinimalPayloadV3
        val actual = CommunicationPayloadParser.extract(reply_payment_info_minimal_payload_v3)
        assertEquals(expected, actual)
    }

    @Test
    fun `parses custom pickupCodeHR payload with variant type`() {
        val json = """
            {
              "version": 3,
              "text": "Some Text to state your request",
              "transactionID": "ABCD-EFGH-IJKL-MNOP",
              "communicationType": "pickupCodeHR",
              "pickupCodeHR": "0815"
            }
        """.trimIndent()
        val actual = CommunicationPayloadParser.extract(json)
        kotlin.test.assertNotNull(actual)
        assertEquals("0815", (actual as? CommunicationReplyPickupCodeHRPayloadErpModel)?.pickUpCode)
    }

    @Test
    fun `parses custom deliveryStatus payload with position and ETA`() {
        val json = """
            {
              "version": 3,
              "text": "Some Text to state your request",
              "transactionID": "ABCD-EFGH-IJKL-MNOP",
              "communicationType": "deliveryStatus",
              "deliveryStatus": "inTransport",
              "inTransportPosition": {
                "long": 13.387595793605172,
                "lat": 52.522529939635795
              },
              "inTransportETA": {
                "from": 1735736400,
                "to": 1735741800
              }
            }
        """.trimIndent()
        val actual = CommunicationPayloadParser.extract(json)
        kotlin.test.assertNotNull(actual)
        assertEquals(CommunicationDeliveryStatusErpModel.InTransport, (actual as? CommunicationReplyDeliveryStatusPayloadErpModel)?.deliveryStatus)
    }

    @Test
    fun `parses exact user deliveryStatus payload`() {
        val json = """
            {
              "version": 3,
              "text": "Some Text to state your request",
              "transactionID": "ABCD-EFGH-IJKL-MNOP",
              "communicationType": "deliveryStatus",
              "deliveryStatus": "inTransport",
              "inTransportPosition": {
                "long": 13.387595793605172,
                "lat": 52.522529939635795
              },
              "inTransportETA": {
                "from": 1735736400,
                "to": 1735741800
              }
            }
        """.trimIndent()
        val actual = CommunicationPayloadParser.extract(json)
        kotlin.test.assertNotNull(actual)
        val deliveryPayload = actual as? CommunicationReplyDeliveryStatusPayloadErpModel
        kotlin.test.assertNotNull(deliveryPayload)
        assertEquals("ABCD-EFGH-IJKL-MNOP", deliveryPayload.transactionID)
        assertEquals(CommunicationDeliveryStatusErpModel.InTransport, deliveryPayload.deliveryStatus)
        assertEquals(13.387595793605172, deliveryPayload.inTransportPosition?.longitude)
        assertEquals(52.522529939635795, deliveryPayload.inTransportPosition?.latitude)
        assertEquals(1735736400L, deliveryPayload.inTransportETA?.from)
        assertEquals(1735741800L, deliveryPayload.inTransportETA?.to)
        assertEquals("Some Text to state your request", deliveryPayload.text)
    }

    @Test
    fun `parses exact user pickupCodeHR payload`() {
        val json = """
            {
              "version": 3,
              "text": "Some Text to state your request",
              "transactionID": "ABCD-EFGH-IJKL-MNOP",
              "communicationType": "pickupCodeHR",
              "pickupCodeHR": "0815"
            }
        """.trimIndent()
        val actual = CommunicationPayloadParser.extract(json)
        kotlin.test.assertNotNull(actual)
        val pickupPayload = actual as? CommunicationReplyPickupCodeHRPayloadErpModel
        kotlin.test.assertNotNull(pickupPayload)
        assertEquals("ABCD-EFGH-IJKL-MNOP", pickupPayload.transactionID)
        assertEquals("0815", pickupPayload.pickUpCode)
        assertEquals("Some Text to state your request", pickupPayload.text)
    }
}
