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

package de.gematik.ti.erp.app.fhir.prescription.parser

import de.gematik.ti.erp.app.data.kbvBundle1_gkv_v110_json
import de.gematik.ti.erp.app.data.kbvBundle1_incomplete_v102_json
import de.gematik.ti.erp.app.data.kbvBundle1_incomplete_v110_json
import de.gematik.ti.erp.app.data.kbvBundle1_v102_json
import de.gematik.ti.erp.app.data.kbvBundle2_pkv_v110_json
import de.gematik.ti.erp.app.data.kbvBundle3_gkv_v110_json
import de.gematik.ti.erp.app.data.kbvBundle4_pkv_v110_json
import de.gematik.ti.erp.app.data.kbvBundle5_gkv_v110_json
import de.gematik.ti.erp.app.data.kbvBundle6_gkv_v110_json
import de.gematik.ti.erp.app.data.kbvBundle_device_request_1_4
import de.gematik.ti.erp.app.fhir.prescription.mocks.FhirTaskDataErpTestData.fhirKbvBundle1_incomplete_v102
import de.gematik.ti.erp.app.fhir.prescription.mocks.FhirTaskDataErpTestData.fhirKbvBundle1_incomplete_v102_missingFields
import de.gematik.ti.erp.app.fhir.prescription.mocks.FhirTaskDataErpTestData.fhirKbvBundle1_incomplete_v110
import de.gematik.ti.erp.app.fhir.prescription.mocks.FhirTaskDataErpTestData.fhirKbvBundle1_incomplete_v110_missingFields
import de.gematik.ti.erp.app.fhir.prescription.mocks.FhirTaskDataErpTestData.fhirKbvBundle1_v102
import de.gematik.ti.erp.app.fhir.prescription.mocks.FhirTaskDataErpTestData.fhirKbvBundle1_v102_missingFields
import de.gematik.ti.erp.app.fhir.prescription.mocks.FhirTaskDataErpTestData.fhirKbvBundle1_v110
import de.gematik.ti.erp.app.fhir.prescription.mocks.FhirTaskDataErpTestData.fhirKbvBundle1_v110_missingFields
import de.gematik.ti.erp.app.fhir.prescription.mocks.FhirTaskDataErpTestData.fhirTaskDataErpModelV1_1_DeviceRequest
import de.gematik.ti.erp.app.fhir.prescription.mocks.FhirTaskDataErpTestData.fhirKbvBundle2_v110
import de.gematik.ti.erp.app.fhir.prescription.mocks.FhirTaskDataErpTestData.fhirKbvBundle2_v110_missingFields
import de.gematik.ti.erp.app.fhir.prescription.mocks.FhirTaskDataErpTestData.fhirKbvBundle3_v110
import de.gematik.ti.erp.app.fhir.prescription.mocks.FhirTaskDataErpTestData.fhirKbvBundle3_v110_missingFields
import de.gematik.ti.erp.app.fhir.prescription.mocks.FhirTaskDataErpTestData.fhirKbvBundle4_v110
import de.gematik.ti.erp.app.fhir.prescription.mocks.FhirTaskDataErpTestData.fhirKbvBundle4_v110_missingFields
import de.gematik.ti.erp.app.fhir.prescription.mocks.FhirTaskDataErpTestData.fhirKbvBundle5_v110
import de.gematik.ti.erp.app.fhir.prescription.mocks.FhirTaskDataErpTestData.fhirKbvBundle5_v110_missingFields
import de.gematik.ti.erp.app.fhir.prescription.mocks.FhirTaskDataErpTestData.fhirKbvBundle6_v110
import de.gematik.ti.erp.app.fhir.prescription.mocks.FhirTaskDataErpTestData.fhirKbvBundle6_v110_missingFields
import kotlinx.serialization.json.Json
import org.junit.Test
import kotlin.test.assertEquals

class TaskEPrescriptionMedicalDataParserTest {

    val parser = TaskEPrescriptionMedicalDataParser()

    @Test
    fun `test parser for kbv_bundle 1 for version 1_0_2`() {
        val bundle = Json.parseToJsonElement(kbvBundle1_v102_json)
        val result = parser.extract(bundle)
        assertEquals(fhirKbvBundle1_v102, result)
        val missingFields = result?.getMissingProperties()
        assertEquals(fhirKbvBundle1_v102_missingFields, missingFields)
    }

    @Test
    fun `test parser for kbv_bundle 1 incomplete for version 1_0_2`() {
        val bundle = Json.parseToJsonElement(kbvBundle1_incomplete_v102_json)
        val result = parser.extract(bundle)
        assertEquals(fhirKbvBundle1_incomplete_v102, result)
        val missingFields = result?.getMissingProperties()
        assertEquals(fhirKbvBundle1_incomplete_v102_missingFields, missingFields)
    }

    @Test
    fun `test parser for kbv_bundle 1 for version 1_1_0`() {
        val bundle = Json.parseToJsonElement(kbvBundle1_gkv_v110_json)
        val result = parser.extract(bundle)
        assertEquals(fhirKbvBundle1_v110, result)
        val missingFields = result?.getMissingProperties()
        assertEquals(fhirKbvBundle1_v110_missingFields, missingFields)
    }

    @Test
    fun `test parser for kbv_bundle 1 incomplete for version 1_1_0`() {
        val bundle = Json.parseToJsonElement(kbvBundle1_incomplete_v110_json)
        val result = parser.extract(bundle)
        assertEquals(fhirKbvBundle1_incomplete_v110, result)
        val missingFields = result?.getMissingProperties()
        assertEquals(fhirKbvBundle1_incomplete_v110_missingFields, missingFields)
    }

    @Test
    fun `test parser for kbv_bundle 2 pkv for version 1_1_0`() {
        val bundle = Json.parseToJsonElement(kbvBundle2_pkv_v110_json)
        val result = parser.extract(bundle)
        assertEquals(fhirKbvBundle2_v110, result)
        val missingFields = result?.getMissingProperties()
        assertEquals(fhirKbvBundle2_v110_missingFields, missingFields)
    }

    @Test
    fun `test parser for kbv_bundle 3 gkv for version 1_1_0`() {
        val bundle = Json.parseToJsonElement(kbvBundle3_gkv_v110_json)
        val result = parser.extract(bundle)
        assertEquals(fhirKbvBundle3_v110, result)
        val missingFields = result?.getMissingProperties()
        assertEquals(fhirKbvBundle3_v110_missingFields, missingFields)
    }

    @Test
    fun `test parser for kbv_bundle 4 pkv for version 1_1_0`() {
        val bundle = Json.parseToJsonElement(kbvBundle4_pkv_v110_json)
        val result = parser.extract(bundle)
        assertEquals(fhirKbvBundle4_v110, result)
        val missingFields = result?.getMissingProperties()
        assertEquals(fhirKbvBundle4_v110_missingFields, missingFields)
    }

    @Test
    fun `test parser for kbv_bundle 5 gkv for version 1_1_0`() {
        val bundle = Json.parseToJsonElement(kbvBundle5_gkv_v110_json)
        val result = parser.extract(bundle)
        assertEquals(fhirKbvBundle5_v110, result)
        val missingFields = result?.getMissingProperties()
        assertEquals(fhirKbvBundle5_v110_missingFields, missingFields)
    }

    @Test
    fun `test parser for kbv_bundle 6 gkv for version 1_1_0`() {
        val bundle = Json.parseToJsonElement(kbvBundle6_gkv_v110_json)
        val result = parser.extract(bundle)
        assertEquals(fhirKbvBundle6_v110, result)
        val missingFields = result?.getMissingProperties()
        assertEquals(fhirKbvBundle6_v110_missingFields, missingFields)
    }

    @Test
    fun `test parser for kbv_bundle for device requests`() {
        val bundle = Json.parseToJsonElement(kbvBundle_device_request_1_4)
        val result = parser.extract(bundle)
        assertEquals(fhirTaskDataErpModelV1_1_DeviceRequest, result)
    }
}
