/*
 * Copyright (c) 2024 gematik GmbH
 * 
 * Licensed under the EUPL, Version 1.2 or – as soon they will be approved by
 * the European Commission - subsequent versions of the EUPL (the Licence);
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at:
 * 
 *     https://joinup.ec.europa.eu/software/page/eupl
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the Licence is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Licence for the specific language governing permissions and
 * limitations under the Licence.
 * 
 */

@file:Suppress("ktlint:max-line-length")

package de.gematik.ti.erp.app.fhir.model

import de.gematik.ti.erp.app.fhir.parser.contained
import de.gematik.ti.erp.app.fhir.parser.containedString
import de.gematik.ti.erp.app.utils.FhirTemporal
import kotlinx.datetime.Instant
import kotlinx.serialization.json.Json
import org.junit.Test
import java.io.File
import kotlin.test.assertEquals

private const val JsonSymbols = "\"{}[]:"
private const val JsonSymbolsEscaped = "\\\"{}[]:"

private val testBundle by lazy { File("$ResourceBasePath/communications_bundle.json").readText() }
private val testBundleVersion12 by lazy { File("$ResourceBasePath/communications_bundle_version_1_2.json").readText() }

class CommunicationMapperTest {
    @Test
    fun `create disp req communication`() {
        val json = createCommunicationDispenseRequest(
            orderId = "orderId$JsonSymbols",
            taskId = "taskId$JsonSymbols",
            accessCode = "accessCode$JsonSymbols",
            recipientTID = "recipientTID$JsonSymbols",
            payload = CommunicationPayload(
                version = 1,
                supplyOptionsType = "onPremise",
                name = "Anton Miller",
                address = listOf("Some Street", "1234", JsonSymbols),
                hint = "Oh no",
                phone = "132342546547"
            )
        )

        val orderId = json
            .contained("identifier")
            .containedString("value")

        assertEquals("orderId$JsonSymbols", orderId)

        val reference = json
            .contained("basedOn")
            .containedString("reference")

        assertEquals("Task/taskId$JsonSymbols/\$accept?ac=accessCode$JsonSymbols", reference)

        val recipientTID = json
            .contained("recipient")
            .contained("identifier")
            .containedString("value")

        assertEquals("recipientTID$JsonSymbols", recipientTID)

        val payload = json
            .contained("payload")
            .containedString("contentString")

        @Suppress("MaxLineLength")
        assertEquals(
            "{\"version\":1,\"supplyOptionsType\":\"onPremise\",\"name\":\"Anton Miller\",\"address\":[\"Some Street\",\"1234\",\"$JsonSymbolsEscaped\"],\"hint\":\"Oh no\",\"phone\":\"132342546547\"}",
            payload
        )
    }

    @Suppress("LongParameterList")
    private class Communication(
        val taskId: String,
        val communicationId: String,
        val orderId: String?,
        val profile: CommunicationProfile,
        val sentOn: Instant,
        val sender: String,
        val recipient: String,
        val payload: String?
    )

    @Suppress("MaxLineLength")
    private val communications = mapOf(
        0 to Communication(
            taskId = "160.000.000.030.926.11",
            communicationId = "01eb8d02-199b-3080-fe9e-ef29caeda984",
            orderId = null,
            profile = CommunicationProfile.ErxCommunicationReply,
            sentOn = Instant.parse("2022-07-06T15:02:03.984+00:00"),
            sender = "3-TEST-TID",
            recipient = "X110535768",
            payload = "{\"version\":\"1\" , \"supplyOptionsType\":\"shipment\" , \"info_text\":\"11 Info\\/Para + HRcode\\/Para + DMC\\/noPara + URL\\/noPara\" , \"pickUpCodeHR\":\"T11__R03\" , \"pickUpCodeDMC\":\"\" , \"url\":\"\"}"
        ),
        3 to Communication(
            taskId = "160.000.000.030.926.11",
            communicationId = "01eb8d01-9a8d-99b8-9277-24b66fb07635",
            orderId = null,
            profile = CommunicationProfile.ErxCommunicationDispReq,
            sentOn = Instant.parse("2022-07-06T14:26:32.387+00:00"),
            sender = "X110535768",
            recipient = "3-TEST-TID",
            payload = "{\"version\":\"1\",\"supplyOptionsType\":\"shipment\",\"name\":\"Prinzessin Lars Graf Freiherr von Schinder\",\"address\":[\"Siegburger Str. 155\",\"\",\"51105 Köln\"],\"hint\":\"\",\"phone\":\"01711111111\"}"
        )
    )

    private val communicationsVersion12 = mapOf(
        0 to Communication(
            taskId = "160.000.033.491.280.78",
            communicationId = "7977a4ab-97a9-4d95-afb3-6c4c1e2ac596",
            orderId = null,
            profile = CommunicationProfile.ErxCommunicationReply,
            sentOn = Instant.parse("2020-04-29T11:46:30.128Z"),
            sender = "3-SMC-B-Testkarte-883110000123465",
            recipient = "X234567890",
            payload = "Eisern"
        )
    )

    @Test
    fun `parse communications`() {
        var index = 0

        extractCommunications(
            Json.parseToJsonElement(testBundle)
        ) { taskId, communicationId, orderId, profile, sentOn, sender, recipient, payload ->
            communications[index]?.let { com ->
                assertEquals(com.taskId, taskId)
                assertEquals(com.communicationId, communicationId)
                assertEquals(com.orderId, orderId)
                assertEquals(com.profile, profile)
                assertEquals(FhirTemporal.Instant(com.sentOn), sentOn)
                assertEquals(com.sender, sender)
                assertEquals(com.recipient, recipient)
                assertEquals(com.payload, payload)
            }

            index++
        }

        assertEquals(15, index)
    }

    @Test
    fun `parse communications version 1_2`() {
        extractCommunications(
            Json.parseToJsonElement(testBundleVersion12)
        ) { taskId, communicationId, orderId, profile, sentOn, sender, recipient, payload ->
            communicationsVersion12[0]?.let { com ->
                assertEquals(com.taskId, taskId)
                assertEquals(com.communicationId, communicationId)
                assertEquals(com.orderId, orderId)
                assertEquals(com.profile, profile)
                assertEquals(sentOn.toInstant(), com.sentOn)
                assertEquals(com.sender, sender)
                assertEquals(com.recipient, recipient)
                assertEquals(com.payload, payload)
            }
        }
    }
}
