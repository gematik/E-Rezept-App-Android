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

package de.gematik.ti.erp.app.database.room.v2.task.util

import de.gematik.ti.erp.app.communication.model.payload.CommunicationReplyLinkPayloadErpModel
import de.gematik.ti.erp.app.communication.model.payload.CommunicationReplyPayloadV1ErpModel
import de.gematik.ti.erp.app.communication.model.payload.CommunicationReplyPickupCodeHRPayloadErpModel
import de.gematik.ti.erp.app.communication.model.payload.CommunicationReplyTextPayloadErpModel
import de.gematik.ti.erp.app.communication.model.payload.CommunicationSupplyOptionTypeErpModel
import de.gematik.ti.erp.app.communication.model.payload.DispenseRequestDeliveryPayloadErpModel
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

class CommunicationPayloadConverterTest {

    private val converter = CommunicationPayloadConverter()

    @Test
    fun nullPayload_convertsToAndFromNull() {
        assertNull(converter.fromPayload(null))
        assertNull(converter.toPayload(null))
    }

    @Test
    fun invalidJson_returnsNull() {
        assertNull(converter.toPayload("not a valid json"))
        assertNull(converter.toPayload(""))
    }

    @Test
    fun legacyV1DerivedPickupCodeHrPayload_roundTripsWithoutLosingCodeOrText() {
        // Real-world regression: CommunicationReplyPickupCodeHRPayloadErpModel (and the DMC variant) is
        // created by mapV1ToV3ReplyModel() with "version" hardcoded to 1 when it originates from a legacy
        // V1 backend reply that carried only a single field (see CommunicationPayloadParser.kt). Before the
        // toPayload() fast-path fix, every read re-ran the full parser, which dispatched purely on that
        // "version" field, misread this already-structured "type"-tagged JSON as a raw V1 payload, and
        // silently produced an all-null CommunicationReplyPayloadV1ErpModel (SafeJson ignores the unknown
        // "type"/"pickupCodeHR" keys instead of failing) - wiping out the pickup code and text on every
        // re-read from the DB. This must not happen anymore.
        val payload = CommunicationReplyPickupCodeHRPayloadErpModel(
            version = 1,
            transactionID = "",
            pickUpCode = "T01__R01",
            text = "A unique message!"
        )

        val serialized = converter.fromPayload(payload)
        assertNotNull(serialized)

        val deserialized = converter.toPayload(serialized)
        assertEquals(payload, deserialized)
    }

    @Test
    fun legacyRawV1Payload_stillParsesViaFallback() {
        // DBs created before the CommResV3 refactor (Room migration 11->12) may still contain the original,
        // un-parsed backend payload string in this column - migrations copy the raw column value across
        // schema versions without transforming it. toPayload() must still be able to make sense of it via
        // the CommunicationPayloadParser fallback, not just the fast-path plain deserialize.
        val legacyRawPayload = """
            {
            "version":1,
            "supplyOptionsType":"onPremise",
            "info_text":"Ready for pickup",
            "pickUpCodeHR":"HR123"
            }
        """.trimIndent()

        val deserialized = converter.toPayload(legacyRawPayload)

        assertNotNull(deserialized)
        check(deserialized is de.gematik.ti.erp.app.communication.model.payload.CommunicationReplyPickupCodeHRPayloadErpModel)
        assertEquals("HR123", deserialized.pickUpCode)
        assertEquals("Ready for pickup", deserialized.text)
    }

    @Test
    fun v1ReplyPayload_roundTripsCorrectly() {
        val payload = CommunicationReplyPayloadV1ErpModel(
            version = 1,
            supplyOptionsType = CommunicationSupplyOptionTypeErpModel.ON_PREMISE,
            infoText = "Ready for pickup",
            url = "https://pharmacy.de",
            pickUpCodeHR = "HR123",
            pickUpCodeDMC = "DMC456"
        )

        val serialized = converter.fromPayload(payload)
        assertNotNull(serialized)

        val deserialized = converter.toPayload(serialized)
        assertEquals(payload, deserialized)
    }

    @Test
    fun v1ReplyPayload_withBlankPickUpCodeHR_roundTripsWithoutLosingDMCOrUrl() {
        // Reproduces a real backend combination (KBV Test_Communications case "05"): pickUpCodeHR is sent
        // as a blank string (present but empty) alongside a real DMC code and a real url. This must survive
        // a DB round-trip (serialize -> deserialize -> re-parsed via CommunicationPayloadParser) without
        // silently dropping the DMC code, i.e. it must not collapse into a Link-only payload.
        val payload = CommunicationReplyPayloadV1ErpModel(
            version = 1,
            supplyOptionsType = CommunicationSupplyOptionTypeErpModel.ON_PREMISE,
            infoText = "05 Info/Para + HRcode/noPara + DMC/Para + URL/Para",
            url = "https://www.example.com/forest/33",
            pickUpCodeHR = "",
            pickUpCodeDMC = "Test_05___Rezept_02___abcdefg12345"
        )

        val serialized = converter.fromPayload(payload)
        assertNotNull(serialized)

        val deserialized = converter.toPayload(serialized)
        assertEquals(payload, deserialized)
    }

    @Test
    fun v3TextReplyPayload_roundTripsCorrectly() {
        val payload = CommunicationReplyTextPayloadErpModel(
            transactionID = "tx-123",
            text = "Your medication is ready"
        )

        val serialized = converter.fromPayload(payload)
        assertNotNull(serialized)

        val deserialized = converter.toPayload(serialized)
        assertEquals(payload, deserialized)
    }

    @Test
    fun v3LinkReplyPayload_roundTripsCorrectly() {
        val payload = CommunicationReplyLinkPayloadErpModel(
            transactionID = "tx-456",
            text = "Track your order here",
            url = "https://tracking.pharmacy.de/123"
        )

        val serialized = converter.fromPayload(payload)
        assertNotNull(serialized)

        val deserialized = converter.toPayload(serialized)
        assertEquals(payload, deserialized)
    }

    @Test
    fun v3DeliveryRequestPayload_roundTripsCorrectly() {
        val payload = DispenseRequestDeliveryPayloadErpModel(
            transactionID = "tx-789",
            firstname = "Max",
            lastname = "Mustermann",
            address = "Musterstrasse 1",
            postcode = "12345",
            city = "Berlin",
            country = "DE",
            phone = "+49123456789",
            text = "Delivery instructions"
        )

        val serialized = converter.fromPayload(payload)
        assertNotNull(serialized)

        val deserialized = converter.toPayload(serialized)
        assertEquals(payload, deserialized)
    }
}
