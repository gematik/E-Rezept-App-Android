/*
 * Copyright 2025, gematik GmbH
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
 * In case of changes by gematik find details in the "Readme" file.
 *
 * See the Licence for the specific language governing permissions and limitations under the Licence.
 */

@file:Suppress("ktlint:max-line-length")

package de.gematik.ti.erp.app.fhir.model

import de.gematik.ti.erp.app.fhir.communication.model.CommunicationPayload
import de.gematik.ti.erp.app.fhir.parser.contained
import de.gematik.ti.erp.app.fhir.parser.containedString
import de.gematik.ti.erp.app.utils.FhirTemporal
import kotlinx.datetime.Instant
import kotlinx.serialization.json.Json
import java.io.File
import kotlin.test.Test
import kotlin.test.assertEquals

private const val JsonSymbols = "\"{}[]:"
private const val JsonSymbolsEscaped = "\\\"{}[]:"

// TODO: remove Version 1.2 after 30.Jun.2025
private val testBundleVersion12 by lazy { File("$ResourceBasePath/communications_reply_bundle_version_1_2.json").readText() }
private val testBundleVersion13 by lazy { File("$ResourceBasePath/communications_reply_bundle_version_1_3.json").readText() }
private val testBundleVersion14 by lazy { File("$ResourceBasePath/communications_reply_bundle_version_1_4.json").readText() }

class FhirVzdCommunicationMapperTest {
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

    private val communicationsVersion13 = mapOf(
        0 to Communication(
            taskId = "160.000.226.545.733.51",
            communicationId = "01ebc980-ae10-41f0-5a9f-c8ad61141a66",
            orderId = null,
            profile = CommunicationProfile.ErxCommunicationReply,
            sentOn = Instant.parse("2024-08-14T11:14:38.230Z"),
            sender = "3-01.2.2023001.16.103",
            recipient = "X110432693",
            payload = "Eisern"
        ),
        1 to Communication(
            taskId = "160.000.226.545.733.51",
            communicationId = "01ebc980-c555-9bf8-66b2-0d434e302916",
            orderId = null,
            profile = CommunicationProfile.ErxCommunicationReply,
            sentOn = Instant.parse("2024-08-14T11:21:08.651Z"),
            sender = "3-01.2.2023001.16.103",
            recipient = "X110432693",
            payload = "Eisern"
        ),
        2 to Communication(
            taskId = "160.000.226.545.733.51",
            communicationId = "01ebc980-cb72-d730-762e-dd08075f568a",
            orderId = null,
            profile = CommunicationProfile.ErxCommunicationReply,
            sentOn = Instant.parse("2024-08-14T11:22:51.230Z"),
            sender = "3-01.2.2023001.16.103",
            recipient = "X110432693",
            payload = "Eisern"
        )
    )

    private val communicationsVersion14 = mapOf(
        0 to Communication(
            taskId = "160.000.226.545.733.51",
            communicationId = "01ebc980-ae10-41f0-5a9f-c8ad61141a66",
            orderId = null,
            profile = CommunicationProfile.ErxCommunicationReply,
            sentOn = Instant.parse("2024-08-14T11:14:38.230Z"),
            sender = "3-01.2.2023001.16.103",
            recipient = "X110432693",
            payload = "Eisern"
        )
    )

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

    @Test
    fun `parse communications version 1_3`() {
        var index = 0

        extractCommunications(
            Json.parseToJsonElement(testBundleVersion13)
        ) { taskId, communicationId, orderId, profile, sentOn, sender, recipient, payload ->
            communicationsVersion13[index]?.let { com ->
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

        assertEquals(3, index)
    }

    @Test
    fun `parse communications version 1_4`() {
        var index = 0
        extractCommunications(
            Json.parseToJsonElement(testBundleVersion14)
        ) { taskId, communicationId, orderId, profile, sentOn, sender, recipient, payload ->
            communicationsVersion14[index]?.let { com ->
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

        assertEquals(3, index)
    }
}
