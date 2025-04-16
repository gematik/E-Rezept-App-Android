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

import de.gematik.ti.erp.app.data.insuranceInformation110Json
import de.gematik.ti.erp.app.data.insuranceInformationJson
import de.gematik.ti.erp.app.fhir.prescription.mocks.FhirCoverageErpTestData.expectedErpCoverageModel110
import de.gematik.ti.erp.app.fhir.prescription.mocks.FhirCoverageErpTestData.expectedErpCoverageModelV103
import de.gematik.ti.erp.app.fhir.prescription.mocks.FhirCoverageTestData.expectedFhirCoverageModelV103
import de.gematik.ti.erp.app.fhir.prescription.mocks.FhirCoverageTestData.expectedFhirCoverageModelV110
import de.gematik.ti.erp.app.fhir.prescription.model.original.FhirCoverageModel.Companion.getCoverage
import de.gematik.ti.erp.app.fhir.prescription.model.original.FhirCoverageModel.Companion.toErpModel
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import org.junit.Test
import kotlin.test.assertEquals

class TaskKbvParserCoverageTest {

    @Test
    fun `test parser for coverage 103`() = runTest {
        val coverage = Json.parseToJsonElement(insuranceInformationJson)
        val fhirCoverageModel = coverage.getCoverage()
        val erpModel = fhirCoverageModel?.toErpModel()
        assertEquals(expectedFhirCoverageModelV103, fhirCoverageModel)
        assertEquals(expectedErpCoverageModelV103, erpModel)
    }

    @Test
    fun `test parser for coverage 110`() = runTest {
        val coverage = Json.parseToJsonElement(insuranceInformation110Json)
        val fhirCoverageModel = coverage.getCoverage()
        val erpModel = fhirCoverageModel?.toErpModel()
        assertEquals(expectedFhirCoverageModelV110, fhirCoverageModel)
        assertEquals(expectedErpCoverageModel110, erpModel)
    }
}
