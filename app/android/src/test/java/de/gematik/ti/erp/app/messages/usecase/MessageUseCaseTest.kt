/*
 * Copyright 2024, gematik GmbH
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

package de.gematik.ti.erp.app.messages.usecase

import de.gematik.ti.erp.app.CoroutineTestRule
import de.gematik.ti.erp.app.messages.mappers.toMessage
import de.gematik.ti.erp.app.messages.domain.model.OrderUseCaseData
import de.gematik.ti.erp.app.prescription.model.Communication
import de.gematik.ti.erp.app.prescription.model.CommunicationProfile
import kotlinx.datetime.Instant
import org.junit.Rule
import org.junit.Test
import kotlin.test.assertEquals

class MessageUseCaseTest {
    @get:Rule
    val coroutineRule = CoroutineTestRule()

    @Test
    fun `communication to message - normal`() {
        val communication = Communication(
            taskId = "",
            orderId = "",
            communicationId = "CID123456",
            profile = CommunicationProfile.ErxCommunicationReply,
            sentOn = Instant.fromEpochSeconds(123456),
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
            sentOn = Instant.fromEpochSeconds(123456),
            message = "Hi!",
            pickUpCodeDMC = null,
            pickUpCodeHR = null,
            link = "https://example.org",
            consumed = false,
            prescriptions = emptyList(),
            taskIds = emptyList(),
            isTaskIdCountMatching = false
        )
        assertEquals(expected, communication.toMessage())
    }

    @Test
    fun `communication to message - payload partially empty`() {
        val communication = Communication(
            taskId = "",
            orderId = "",
            communicationId = "CID123456",
            profile = CommunicationProfile.ErxCommunicationReply,
            sentOn = Instant.fromEpochSeconds(123456),
            sender = "ABC123456",
            recipient = "ABC654321",
            payload = """{ "version": 1, "supplyOptionsType": "shipment", "url": "    ", "pickUpCodeHR": "" }""",
            consumed = false
        )
        val expected = OrderUseCaseData.Message(
            communicationId = "CID123456",
            sentOn = Instant.fromEpochSeconds(123456),
            message = null,
            pickUpCodeDMC = null,
            pickUpCodeHR = null,
            link = null,
            consumed = false,
            prescriptions = emptyList(),
            taskIds = emptyList(),
            isTaskIdCountMatching = false

        )
        assertEquals(expected, communication.toMessage())
    }

    @Test
    fun `communication to message - payload broken`() {
        val communication = Communication(
            taskId = "",
            orderId = "",
            communicationId = "CID123456",
            profile = CommunicationProfile.ErxCommunicationReply,
            sentOn = Instant.fromEpochSeconds(123456),
            sender = "ABC123456",
            recipient = "ABC654321",
            payload = """{   - """,
            consumed = false
        )
        val expected = OrderUseCaseData.Message(
            communicationId = "CID123456",
            sentOn = Instant.fromEpochSeconds(123456),
            message = null,
            pickUpCodeDMC = null,
            pickUpCodeHR = null,
            link = null,
            consumed = false,
            prescriptions = emptyList(),
            taskIds = emptyList(),
            isTaskIdCountMatching = false
        )
        assertEquals(expected, communication.toMessage())
    }

    @Test
    fun `communication to message - invalid url`() {
        val communication = Communication(
            taskId = "",
            orderId = "",
            communicationId = "CID123456",
            profile = CommunicationProfile.ErxCommunicationReply,
            sentOn = Instant.fromEpochSeconds(123456),
            sender = "ABC123456",
            recipient = "ABC654321",
            payload = """{ "version": 1, "supplyOptionsType": "shipment", "url": "ftp://example.org" }""",
            consumed = false
        )
        val expected = OrderUseCaseData.Message(
            communicationId = "CID123456",
            sentOn = Instant.fromEpochSeconds(123456),
            message = null,
            pickUpCodeDMC = null,
            pickUpCodeHR = null,
            link = null,
            consumed = false,
            prescriptions = emptyList(),
            taskIds = emptyList(),
            isTaskIdCountMatching = false
        )
        assertEquals(expected, communication.toMessage())
    }
}
