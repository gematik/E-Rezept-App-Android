/*
 * Copyright (c) 2023 gematik GmbH
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

package de.gematik.ti.erp.app.orders.usecase

import de.gematik.ti.erp.app.CoroutineTestRule
import de.gematik.ti.erp.app.orders.usecase.model.OrderUseCaseData
import de.gematik.ti.erp.app.prescription.model.SyncedTaskData
import org.junit.Rule
import java.time.Instant
import kotlin.test.Test
import kotlin.test.assertEquals

class OrderUseCaseTest {
    @get:Rule
    val coroutineRule = CoroutineTestRule()

    @Test
    fun `communication to message - normal`() {
        val communication = SyncedTaskData.Communication(
            taskId = "",
            orderId = "",
            communicationId = "CID123456",
            profile = SyncedTaskData.CommunicationProfile.ErxCommunicationReply,
            sentOn = Instant.ofEpochMilli(123456),
            sender = "ABC123456",
            recipient = "ABC654321",
            payload = """
            {
                "version": 1,
                "info_text": "Hi!",
                "supplyOptionsType": "shipment", 
                "url": "https://example.org" 
            }
            """.trimIndent(),
            consumed = false
        )
        val expected = OrderUseCaseData.Message(
            communicationId = "CID123456",
            sentOn = Instant.ofEpochMilli(123456),
            message = "Hi!",
            code = null,
            link = "https://example.org",
            consumed = false
        )
        assertEquals(expected, communication.toMessage())
    }

    @Test
    fun `communication to message - payload partially empty`() {
        val communication = SyncedTaskData.Communication(
            taskId = "",
            orderId = "",
            communicationId = "CID123456",
            profile = SyncedTaskData.CommunicationProfile.ErxCommunicationReply,
            sentOn = Instant.ofEpochMilli(123456),
            sender = "ABC123456",
            recipient = "ABC654321",
            payload = """{ "version": 1, "supplyOptionsType": "shipment", "url": "    ", "pickUpCodeHR": "" }""",
            consumed = false
        )
        val expected = OrderUseCaseData.Message(
            communicationId = "CID123456",
            sentOn = Instant.ofEpochMilli(123456),
            message = null,
            code = null,
            link = null,
            consumed = false
        )
        assertEquals(expected, communication.toMessage())
    }

    @Test
    fun `communication to message - payload broken`() {
        val communication = SyncedTaskData.Communication(
            taskId = "",
            orderId = "",
            communicationId = "CID123456",
            profile = SyncedTaskData.CommunicationProfile.ErxCommunicationReply,
            sentOn = Instant.ofEpochMilli(123456),
            sender = "ABC123456",
            recipient = "ABC654321",
            payload = """{   - """,
            consumed = false
        )
        val expected = OrderUseCaseData.Message(
            communicationId = "CID123456",
            sentOn = Instant.ofEpochMilli(123456),
            message = null,
            code = null,
            link = null,
            consumed = false
        )
        assertEquals(expected, communication.toMessage())
    }

    @Test
    fun `communication to message - invalid url`() {
        val communication = SyncedTaskData.Communication(
            taskId = "",
            orderId = "",
            communicationId = "CID123456",
            profile = SyncedTaskData.CommunicationProfile.ErxCommunicationReply,
            sentOn = Instant.ofEpochMilli(123456),
            sender = "ABC123456",
            recipient = "ABC654321",
            payload = """{ "version": 1, "supplyOptionsType": "shipment", "url": "ftp://example.org" }""",
            consumed = false
        )
        val expected = OrderUseCaseData.Message(
            communicationId = "CID123456",
            sentOn = Instant.ofEpochMilli(123456),
            message = null,
            code = null,
            link = null,
            consumed = false
        )
        assertEquals(expected, communication.toMessage())
    }
}
