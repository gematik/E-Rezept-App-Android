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

import de.gematik.ti.erp.app.data.practitioner1_v103_json
import de.gematik.ti.erp.app.data.practitioner1_v110_json
import de.gematik.ti.erp.app.data.practitioner2_v103_json
import de.gematik.ti.erp.app.data.practitioner2_v110_json
import de.gematik.ti.erp.app.data.practitioner3_v12_json
import de.gematik.ti.erp.app.data.practitioner4_zanr_v12_json
import de.gematik.ti.erp.app.fhir.prescription.mocks.FhirPractitionerErpTestData.erpPractitioner1_v103
import de.gematik.ti.erp.app.fhir.prescription.mocks.FhirPractitionerErpTestData.erpPractitioner1_v110
import de.gematik.ti.erp.app.fhir.prescription.mocks.FhirPractitionerErpTestData.erpPractitioner2_v103
import de.gematik.ti.erp.app.fhir.prescription.mocks.FhirPractitionerErpTestData.erpPractitioner2_v110
import de.gematik.ti.erp.app.fhir.prescription.mocks.FhirPractitionerErpTestData.erpPractitioner3_v12
import de.gematik.ti.erp.app.fhir.prescription.mocks.FhirPractitionerErpTestData.erpPractitioner4_v12
import de.gematik.ti.erp.app.fhir.prescription.mocks.FhirPractitionerTestData.fhirPractitioner1_v103
import de.gematik.ti.erp.app.fhir.prescription.mocks.FhirPractitionerTestData.fhirPractitioner1_v110
import de.gematik.ti.erp.app.fhir.prescription.mocks.FhirPractitionerTestData.fhirPractitioner2_v103
import de.gematik.ti.erp.app.fhir.prescription.mocks.FhirPractitionerTestData.fhirPractitioner2_v110
import de.gematik.ti.erp.app.fhir.prescription.mocks.FhirPractitionerTestData.fhirPractitioner3_v12
import de.gematik.ti.erp.app.fhir.prescription.mocks.FhirPractitionerTestData.fhirPractitioner4_v12
import de.gematik.ti.erp.app.fhir.prescription.model.original.FhirPractitioner.Companion.getPractitioner
import de.gematik.ti.erp.app.fhir.prescription.model.original.FhirPractitioner.Companion.toErpModel
import kotlinx.serialization.json.Json
import org.junit.Test
import kotlin.test.assertEquals

class TaskEPrescriptionMedicalDataParserPractitionerTest {

    @Test
    fun `test parser for practitioner 1 version 1_0_3`() {
        val bundle = Json.parseToJsonElement(practitioner1_v103_json)
        val fhirModel = bundle.getPractitioner()
        val erpModel = fhirModel?.toErpModel()
        assertEquals(fhirPractitioner1_v103, fhirModel)
        assertEquals(erpPractitioner1_v103, erpModel)
    }

    @Test
    fun `test parser for practitioner 2 version 1_0_3`() {
        val bundle = Json.parseToJsonElement(practitioner2_v103_json)
        val fhirModel = bundle.getPractitioner()
        val erpModel = fhirModel?.toErpModel()
        assertEquals(fhirPractitioner2_v103, fhirModel)
        assertEquals(erpPractitioner2_v103, erpModel)
    }

    @Test
    fun `test parser for practitioner 1 version 1_1_0`() {
        val bundle = Json.parseToJsonElement(practitioner1_v110_json)
        val fhirModel = bundle.getPractitioner()
        val erpModel = fhirModel?.toErpModel()
        assertEquals(fhirPractitioner1_v110, fhirModel)
        assertEquals(erpPractitioner1_v110, erpModel)
    }

    @Test
    fun `test parser for practitioner 2 version 1_1_0`() {
        val bundle = Json.parseToJsonElement(practitioner2_v110_json)
        val fhirModel = bundle.getPractitioner()
        val erpModel = fhirModel?.toErpModel()
        assertEquals(fhirPractitioner2_v110, fhirModel)
        assertEquals(erpPractitioner2_v110, erpModel)
    }

    @Test
    fun `test parser for practitioner 3 version 1_2`() {
        val bundle = Json.parseToJsonElement(practitioner3_v12_json)
        val fhirModel = bundle.getPractitioner()
        val erpModel = fhirModel?.toErpModel()
        assertEquals(fhirPractitioner3_v12, fhirModel)
        assertEquals(erpPractitioner3_v12, erpModel)
    }

    @Test
    fun `test parser for practitioner 4 with zanr and telematik-id version 1_2`() {
        val bundle = Json.parseToJsonElement(practitioner4_zanr_v12_json)
        val fhirModel = bundle.getPractitioner()
        val erpModel = fhirModel?.toErpModel()
        assertEquals(fhirPractitioner4_v12, fhirModel)
        assertEquals(erpPractitioner4_v12, erpModel)
    }
}
