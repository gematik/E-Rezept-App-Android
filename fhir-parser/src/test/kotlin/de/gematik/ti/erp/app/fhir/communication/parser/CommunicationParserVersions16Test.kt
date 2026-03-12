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

import de.gematik.ti.erp.app.data.communication_diga_dispense_1_6
import de.gematik.ti.erp.app.data.communication_diga_dispense__patient_1_6
import de.gematik.ti.erp.app.data.communication_diga_dispense_no_payload_1_6
import de.gematik.ti.erp.app.data.communication_reply_1_6
import de.gematik.ti.erp.app.fhir.communication.mocks.FhirCommunicationErpTestData
import de.gematik.ti.erp.app.fhir.communication.util.wrapCommunicationInBundle
import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Test

class CommunicationParserVersions16Test {

    private val communicationParser = CommunicationParser()
    private val json = Json { ignoreUnknownKeys = true }

    @Test
    fun `test parse reply 1_6 version`() {
        val communicationJson = wrapCommunicationInBundle(
            communication = json.parseToJsonElement(communication_reply_1_6)
        )
        val result = communicationParser.extract(communicationJson)
        assertEquals(FhirCommunicationErpTestData.replyCommunication_1_6_singleBundle, result)
    }

    @Test
    fun `test parse diga dispense no payload 1_6 version`() {
        val communicationJson = wrapCommunicationInBundle(
            communication = json.parseToJsonElement(communication_diga_dispense_no_payload_1_6)
        )
        val result = communicationParser.extract(communicationJson)
        assertEquals(FhirCommunicationErpTestData.dispense_1_6_singleBundle, result)
    }

    @Test
    fun `test parse patient dispense 1_6 version`() {
        val communicationJson = wrapCommunicationInBundle(
            communication = json.parseToJsonElement(communication_diga_dispense__patient_1_6)
        )
        val result = communicationParser.extract(communicationJson)
        assertEquals(FhirCommunicationErpTestData.patientDispense_1_6_singleBundle, result)
    }

    @Test
    fun `test parse diga dispense 1_6 version`() {
        val communicationJson = wrapCommunicationInBundle(
            communication = json.parseToJsonElement(communication_diga_dispense_1_6)
        )
        val result = communicationParser.extract(communicationJson)
        assertEquals(FhirCommunicationErpTestData.digaDispense_1_6_singleBundle, result)
    }
}
