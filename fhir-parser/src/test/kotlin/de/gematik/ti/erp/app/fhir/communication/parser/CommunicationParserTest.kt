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

package de.gematik.ti.erp.app.fhir.communication.parser

import de.gematik.ti.erp.app.data.DispenseCommTestBundleV1_4
import de.gematik.ti.erp.app.data.MixedBundleV1_2Json
import de.gematik.ti.erp.app.data.MixedBundleV1_4Json
import de.gematik.ti.erp.app.data.ReplyCommTestBundleV1_4
import de.gematik.ti.erp.app.data.SingleDispenseCommV1_2
import de.gematik.ti.erp.app.data.SingleDispenseCommV1_4
import de.gematik.ti.erp.app.data.SingleReplyCommV1_2
import de.gematik.ti.erp.app.data.SingleReplyCommV1_3
import de.gematik.ti.erp.app.data.SingleReplyCommV1_4
import de.gematik.ti.erp.app.fhir.communication.mocks.FhirCommunicationErpTestData.dispenseEntryV1_4Bundle
import de.gematik.ti.erp.app.fhir.communication.mocks.FhirCommunicationErpTestData.mixedBundleV1_2Model
import de.gematik.ti.erp.app.fhir.communication.mocks.FhirCommunicationErpTestData.mixedBundleV1_4Model
import de.gematik.ti.erp.app.fhir.communication.mocks.FhirCommunicationErpTestData.replyEntriesV1_4Bundle
import de.gematik.ti.erp.app.fhir.communication.mocks.FhirCommunicationErpTestData.singleDispenseV1_2Model
import de.gematik.ti.erp.app.fhir.communication.mocks.FhirCommunicationErpTestData.singleDispenseV1_4Model
import de.gematik.ti.erp.app.fhir.communication.mocks.FhirCommunicationErpTestData.singleReplyV1_2Model
import de.gematik.ti.erp.app.fhir.communication.mocks.FhirCommunicationErpTestData.singleReplyV1_3Model
import de.gematik.ti.erp.app.fhir.communication.mocks.FhirCommunicationErpTestData.singleReplyV1_4Model
import kotlinx.serialization.json.Json
import org.junit.Test
import kotlin.test.assertEquals

class CommunicationParserTest {

    private val communicationParser = CommunicationParser()
    private val json = Json { ignoreUnknownKeys = true }

    @Test
    fun `test parser for reply communication bundle v1_4`() {
        val bundle = json.parseToJsonElement(ReplyCommTestBundleV1_4)
        val result = communicationParser.extract(bundle)

        assertEquals(replyEntriesV1_4Bundle, result)
    }

    @Test
    fun `test parser for dispense communication bundle v1_4`() {
        val bundle = json.parseToJsonElement(DispenseCommTestBundleV1_4)
        val result = communicationParser.extract(bundle)

        assertEquals(dispenseEntryV1_4Bundle, result)
    }

    @Test
    fun `test parser with mixed content bundle with v1_4`() {
        val bundle = json.parseToJsonElement(MixedBundleV1_4Json)
        val result = communicationParser.extract(bundle)

        assertEquals(mixedBundleV1_4Model, result)
    }

    @Test
    fun `test parser with mixed content bundle with v1_2`() {
        val bundle = json.parseToJsonElement(MixedBundleV1_2Json)
        val result = communicationParser.extract(bundle)

        assertEquals(mixedBundleV1_2Model, result)
    }

    @Test
    fun `test parser with single reply communication v1_4`() {
        val communicationJson = json.parseToJsonElement(SingleReplyCommV1_4)
        val result = communicationParser.extract(communicationJson)

        assertEquals(singleReplyV1_4Model, result)
    }

    @Test
    fun `test parser with single reply communication v1_3`() {
        val communicationJson = json.parseToJsonElement(SingleReplyCommV1_3)
        val result = communicationParser.extract(communicationJson)

        assertEquals(singleReplyV1_3Model, result)
    }

    @Test
    fun `test parser with single reply communication v1_2`() {
        val communicationJson = json.parseToJsonElement(SingleReplyCommV1_2)
        val result = communicationParser.extract(communicationJson)

        assertEquals(singleReplyV1_2Model, result)
    }

    @Test
    fun `test parser with single dispense communication v1_4`() {
        val communicationJson = json.parseToJsonElement(SingleDispenseCommV1_4)
        val result = communicationParser.extract(communicationJson)

        assertEquals(singleDispenseV1_4Model, result)
    }

    @Test
    fun `test parser with single dispense communication v1_2`() {
        val communicationJson = json.parseToJsonElement(SingleDispenseCommV1_2)
        val result = communicationParser.extract(communicationJson)

        assertEquals(singleDispenseV1_2Model, result)
    }
}
