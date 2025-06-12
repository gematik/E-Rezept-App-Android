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

package de.gematik.ti.erp.app.fhir.prescription.parser

import de.gematik.ti.erp.app.data.kbvBundle_110_1
import de.gematik.ti.erp.app.data.kbvBundle_110_2
import de.gematik.ti.erp.app.data.kbvBundle_110_3
import de.gematik.ti.erp.app.data.kbvBundle_device_request_1_4
import de.gematik.ti.erp.app.fhir.prescription.mocks.FhirTaskDataErpTestData.fhirTaskDataErpModel
import de.gematik.ti.erp.app.fhir.prescription.mocks.FhirTaskDataErpTestData.fhirTaskDataErpModelV1_1_DeviceRequest
import de.gematik.ti.erp.app.fhir.prescription.mocks.FhirTaskDataErpTestData.fhirTaskDataErpModelV2
import de.gematik.ti.erp.app.fhir.prescription.mocks.FhirTaskDataErpTestData.fhirTaskDataErpModelV2WithDifferentFullUrl
import kotlinx.serialization.json.Json
import org.junit.Test
import kotlin.test.assertEquals

class TaskEPrescriptionMedicalDataParserTest {

    val parser = TaskEPrescriptionMedicalDataParser()

    @Test
    fun `test parser for kbv_bundle for version 110`() {
        val bundle = Json.parseToJsonElement(kbvBundle_110_1)
        val result = parser.extract(bundle)
        assertEquals(fhirTaskDataErpModel, result)
    }

    @Test
    fun `test parser for kbv_bundle for version_110 with different resources`() {
        val bundle = Json.parseToJsonElement(kbvBundle_110_2)
        val result = parser.extract(bundle)
        assertEquals(fhirTaskDataErpModelV2, result)
    }

    @Test
    fun `test parser for kbv_bundle for version 110 with different fullUrl`() {
        val bundle = Json.parseToJsonElement(kbvBundle_110_3)
        val result = parser.extract(bundle)
        assertEquals(fhirTaskDataErpModelV2WithDifferentFullUrl, result)
    }

    @Test
    fun `test parser for kbv_bundle for device requests`() {
        val bundle = Json.parseToJsonElement(kbvBundle_device_request_1_4)
        val result = parser.extract(bundle)
        assertEquals(fhirTaskDataErpModelV1_1_DeviceRequest, result)
    }
}
