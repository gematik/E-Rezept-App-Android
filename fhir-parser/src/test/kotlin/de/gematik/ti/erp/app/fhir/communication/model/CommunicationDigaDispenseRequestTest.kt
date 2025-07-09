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

package de.gematik.ti.erp.app.fhir.communication.model

import de.gematik.ti.erp.app.data.communication_diga_dispense_1_4
import de.gematik.ti.erp.app.fhir.communication.DigaDispenseRequestBuilder
import de.gematik.ti.erp.app.fhir.communication.model.CommunicationDigaDispenseRequest.Companion.toJson
import kotlinx.datetime.Instant
import kotlinx.serialization.json.Json
import org.junit.Test
import kotlin.test.assertEquals

class CommunicationDigaDispenseRequestTest {

    private val generator = DigaDispenseRequestBuilder()

    @Test
    fun `test generating diga request bundle`() {
        val model = generator.build(
            orderId = "order-id-1",
            telematikId = "telematik-id-1",
            kvnrNumber = "X11054359845",
            taskId = "160.000.006.394.157.15",
            accessCode = "8cc887c16681517e2db71078f367d4446c156bde743e15c2440722ec0835f406",
            sent = Instant.parse("2025-04-14T13:37:00Z")
        )

        val communicationJsonElement = model.toJson()

        val expectedCommunicationJsonElement = Json.parseToJsonElement(communication_diga_dispense_1_4)

        assertEquals(expectedCommunicationJsonElement, communicationJsonElement)
    }
}
