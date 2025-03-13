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

package de.gematik.ti.erp.app.fhir.prescription.parser

import de.gematik.ti.erp.app.fhir.model.practitionerJson
import de.gematik.ti.erp.app.fhir.model.practitionerJson110
import de.gematik.ti.erp.app.fhir.prescription.mocks.FhirPractitionerErpTestData.erpPractitioner103
import de.gematik.ti.erp.app.fhir.prescription.mocks.FhirPractitionerErpTestData.erpPractitioner110
import de.gematik.ti.erp.app.fhir.prescription.mocks.FhirPractitionerTestData.fhirPractitioner103
import de.gematik.ti.erp.app.fhir.prescription.mocks.FhirPractitionerTestData.fhirPractitioner110
import de.gematik.ti.erp.app.fhir.prescription.model.original.FhirPractitioner.Companion.getPractitioner
import de.gematik.ti.erp.app.fhir.prescription.model.original.FhirPractitioner.Companion.toErpModel
import kotlinx.serialization.json.Json
import org.junit.Test
import kotlin.test.assertEquals

class TaskKbvParserPractitionerTest {

    @Test
    fun `test parser for practitioner version 1_0_3`() {
        val bundle = Json.parseToJsonElement(practitionerJson)
        val fhirModel = bundle.getPractitioner()
        val erpModel = fhirModel?.toErpModel()
        assertEquals(fhirPractitioner103, fhirModel?.first)
        assertEquals(erpPractitioner103, erpModel)
    }

    @Test
    fun `test parser for practitioner version 1_1_0`() {
        val bundle = Json.parseToJsonElement(practitionerJson110)
        val fhirModel = bundle.getPractitioner()
        val erpModel = fhirModel?.toErpModel()
        assertEquals(fhirPractitioner110, fhirModel?.first)
        assertEquals(erpPractitioner110, erpModel)
    }
}
