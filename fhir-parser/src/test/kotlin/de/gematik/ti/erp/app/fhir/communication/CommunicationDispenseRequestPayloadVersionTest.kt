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

package de.gematik.ti.erp.app.fhir.communication

import de.gematik.ti.erp.app.communication.model.payload.CommunicationPayloadErpModel
import de.gematik.ti.erp.app.communication.model.payload.CommunicationSupplyOptionTypeErpModel
import de.gematik.ti.erp.app.communication.model.payload.DispenseRequestCommunicationPayloadV1ErpModel
import de.gematik.ti.erp.app.communication.model.payload.DispenseRequestCommunicationPayloadV3ErpModel
import de.gematik.ti.erp.app.fhir.constant.SafeJson
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNull

class CommunicationDispenseRequestPayloadVersionTest {

    private val v1Payload = DispenseRequestCommunicationPayloadV1ErpModel(
        supplyOptionsType = CommunicationSupplyOptionTypeErpModel.SHIPMENT,
        name = "Erika Mustermann",
        address = listOf("Musterstr. 1", "", "10117", "Berlin"),
        phone = "004916094858168",
        hint = "Bitte klingeln"
    )

    private val v3Payload = DispenseRequestCommunicationPayloadV3ErpModel(
        supplyOptionsType = CommunicationSupplyOptionTypeErpModel.SHIPMENT,
        firstname = "Erika",
        lastname = "Mustermann",
        address = "Musterstr. 1",
        postcode = "10117",
        city = "Berlin",
        country = "DE",
        hint = "Bitte klingeln",
        phone = "004916094858168"
    )

    private fun payloadJsonOf(version: String, payload: CommunicationPayloadErpModel) =
        CommunicationDispenseRequest.createCommunicationDispenseRequest(
            orderId = "order-id-1",
            taskId = "160.000.006.394.157.15",
            accessCode = "access-code",
            recipientId = "telematik-id-1",
            communicationPayloadVersion = version,
            payloadContent = payload,
            flowTypeCode = "160",
            flowTypeDisplay = "Muster 16 (Apothekenpflichtige Arzneimittel)"
        )
            .jsonObject["payload"]!!
            .jsonArray[0]
            .jsonObject["contentString"]!!
            .jsonPrimitive.content
            .let { Json.parseToJsonElement(it).jsonObject }

    @Test
    fun `payload version 1 serializes the v1 model`() {
        val payloadJson = payloadJsonOf("1", v1Payload)

        assertEquals("1", payloadJson["version"]?.jsonPrimitive?.content)
        assertEquals("Erika Mustermann", payloadJson["name"]?.jsonPrimitive?.content)
        assertEquals("shipment", payloadJson["supplyOptionsType"]?.jsonPrimitive?.content)
        // v3-only fields must not be present
        assertNull(payloadJson["firstname"])
        assertNull(payloadJson["postcode"])
        assertNull(payloadJson["communicationType"])
    }

    @Test
    fun `payload version 3 serializes the v3 model`() {
        val payloadJson = payloadJsonOf("3", v3Payload)

        assertEquals("3", payloadJson["version"]?.jsonPrimitive?.content)
        assertEquals("order", payloadJson["communicationType"]?.jsonPrimitive?.content)
        assertEquals("shipment", payloadJson["supplyOptionsType"]?.jsonPrimitive?.content)
        assertEquals("Erika", payloadJson["firstname"]?.jsonPrimitive?.content)
        assertEquals("Mustermann", payloadJson["lastname"]?.jsonPrimitive?.content)
        assertEquals("Musterstr. 1", payloadJson["address"]?.jsonPrimitive?.content)
        assertEquals("10117", payloadJson["postcode"]?.jsonPrimitive?.content)
        assertEquals("Berlin", payloadJson["city"]?.jsonPrimitive?.content)
        assertEquals("DE", payloadJson["country"]?.jsonPrimitive?.content)
        // v1-only field must not be present
        assertNull(payloadJson["name"])
    }

    @Test
    fun `mismatched payload model for version 3 throws`() {
        assertFailsWith<IllegalArgumentException> { payloadJsonOf("3", v1Payload) }
    }

    @Test
    fun `mismatched payload model for version 1 throws`() {
        assertFailsWith<IllegalArgumentException> { payloadJsonOf("1", v3Payload) }
    }

    @Test
    fun `v3 model round trips through json`() {
        val json = SafeJson.value.encodeToString(DispenseRequestCommunicationPayloadV3ErpModel.serializer(), v3Payload)
        val decoded = SafeJson.value.decodeFromString(DispenseRequestCommunicationPayloadV3ErpModel.serializer(), json)

        assertEquals(v3Payload, decoded)
    }
}
