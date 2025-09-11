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

import de.gematik.ti.erp.app.data.coverage1_v103_json
import de.gematik.ti.erp.app.data.coverage1_v110_json
import de.gematik.ti.erp.app.data.coverage2_v103_json
import de.gematik.ti.erp.app.data.coverage2_v110_json
import de.gematik.ti.erp.app.data.coverage3_v12_json
import de.gematik.ti.erp.app.fhir.prescription.mocks.FhirCoverageErpTestData.coverage_AokNordost
import de.gematik.ti.erp.app.fhir.prescription.mocks.FhirCoverageErpTestData.erpCoverage1_v103
import de.gematik.ti.erp.app.fhir.prescription.mocks.FhirCoverageErpTestData.erpCoverage1_v110
import de.gematik.ti.erp.app.fhir.prescription.mocks.FhirCoverageErpTestData.erpCoverage2_v103
import de.gematik.ti.erp.app.fhir.prescription.mocks.FhirCoverageErpTestData.erpCoverage2_v110
import de.gematik.ti.erp.app.fhir.prescription.mocks.FhirCoverageTestData.fhirCoverage1_v103
import de.gematik.ti.erp.app.fhir.prescription.mocks.FhirCoverageTestData.fhirCoverage1_v110
import de.gematik.ti.erp.app.fhir.prescription.mocks.FhirCoverageTestData.fhirCoverage2_v103
import de.gematik.ti.erp.app.fhir.prescription.mocks.FhirCoverageTestData.fhirCoverage2_v110
import de.gematik.ti.erp.app.fhir.prescription.mocks.FhirCoverageTestData.fhir_coverage_AokNordost
import de.gematik.ti.erp.app.fhir.prescription.model.original.FhirCoverageModel.Companion.getCoverage
import de.gematik.ti.erp.app.fhir.prescription.model.original.FhirCoverageModel.Companion.toErpModel
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import org.junit.Test
import kotlin.test.assertEquals

class TaskEPrescriptionMedicalDataParserCoverageTest {

    @Test
    fun `test parser for coverage 1 version 103`() = runTest {
        val coverage = Json.parseToJsonElement(coverage1_v103_json)
        val fhirCoverageModel = coverage.getCoverage()
        val erpModel = fhirCoverageModel?.toErpModel()
        assertEquals(fhirCoverage1_v103, fhirCoverageModel)
        assertEquals(erpCoverage1_v103, erpModel)
    }

    @Test
    fun `test parser for coverage 2 version 103`() = runTest {
        val coverage = Json.parseToJsonElement(coverage2_v103_json)
        val fhirCoverageModel = coverage.getCoverage()
        val erpModel = fhirCoverageModel?.toErpModel()
        assertEquals(fhirCoverage2_v103, fhirCoverageModel)
        assertEquals(erpCoverage2_v103, erpModel)
    }

    @Test
    fun `test parser for coverage 1 version 110`() = runTest {
        val coverage = Json.parseToJsonElement(coverage1_v110_json)
        val fhirCoverageModel = coverage.getCoverage()
        val erpModel = fhirCoverageModel?.toErpModel()
        assertEquals(fhirCoverage1_v110, fhirCoverageModel)
        assertEquals(erpCoverage1_v110, erpModel)
    }

    @Test
    fun `test parser for coverage 2 version 110`() = runTest {
        val coverage = Json.parseToJsonElement(coverage2_v110_json)
        val fhirCoverageModel = coverage.getCoverage()
        val erpModel = fhirCoverageModel?.toErpModel()
        assertEquals(fhirCoverage2_v110, fhirCoverageModel)
        assertEquals(erpCoverage2_v110, erpModel)
    }

    @Test
    fun `test parser for coverage 3 version 120`() = runTest {
        val coverage = Json.parseToJsonElement(coverage3_v12_json)
        val fhirCoverageModel = coverage.getCoverage()
        val erpModel = fhirCoverageModel?.toErpModel()
        assertEquals(fhir_coverage_AokNordost, fhirCoverageModel)
        assertEquals(coverage_AokNordost, erpModel)
    }
}
