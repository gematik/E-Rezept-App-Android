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

import de.gematik.ti.erp.app.data.digaDentistJson
import de.gematik.ti.erp.app.data.digaHospitalWithAddressJson
import de.gematik.ti.erp.app.data.digaInjuryJson
import de.gematik.ti.erp.app.data.digaOccupationalDiseaseJson
import de.gematik.ti.erp.app.data.digaSelJson
import de.gematik.ti.erp.app.data.digaSimple
import de.gematik.ti.erp.app.data.digaWithAccident2Json
import de.gematik.ti.erp.app.data.digaWithAccident3Json
import de.gematik.ti.erp.app.data.digaWithAccidentJson
import de.gematik.ti.erp.app.fhir.prescription.mocks.FhirDeviceRequestErpTestData.fhirTaskKbvDeviceRequestErpModelTinnitus
import de.gematik.ti.erp.app.fhir.prescription.mocks.FhirDeviceRequestErpTestData.fhirTaskKbvDeviceRequestErpModelType2
import de.gematik.ti.erp.app.fhir.prescription.mocks.FhirDeviceRequestErpTestData.fhirTaskKbvDeviceRequestErpModelType3
import de.gematik.ti.erp.app.fhir.prescription.mocks.FhirDeviceRequestErpTestData.fhirTaskKbvDeviceRequestErpModelWithAccident
import de.gematik.ti.erp.app.fhir.prescription.mocks.FhirDeviceRequestErpTestData.fhirTaskKbvDeviceRequestErpModelWithDentist
import de.gematik.ti.erp.app.fhir.prescription.mocks.FhirDeviceRequestErpTestData.fhirTaskKbvDeviceRequestErpModelWithInjury
import de.gematik.ti.erp.app.fhir.prescription.mocks.FhirDeviceRequestErpTestData.fhirTaskKbvDeviceRequestErpModelWithOccupationalDisease
import de.gematik.ti.erp.app.fhir.prescription.mocks.FhirDeviceRequestErpTestData.fhirTaskKbvDeviceRequestErpModelWithoutAccident
import de.gematik.ti.erp.app.fhir.prescription.mocks.FhirDeviceRequestErpTestData.fhirTaskKbvDeviceRequestErpModelWithoutSelfUse
import de.gematik.ti.erp.app.fhir.prescription.mocks.FhirDeviceRequestTestData.fhirDeviceRequestModelAccident3
import de.gematik.ti.erp.app.fhir.prescription.mocks.FhirDeviceRequestTestData.fhirDeviceRequestModelTinnitus
import de.gematik.ti.erp.app.fhir.prescription.mocks.FhirDeviceRequestTestData.fhirDeviceRequestModelWithAccident2
import de.gematik.ti.erp.app.fhir.prescription.mocks.FhirDeviceRequestTestData.fhirDeviceRequestModelWithAccidentInfo
import de.gematik.ti.erp.app.fhir.prescription.mocks.FhirDeviceRequestTestData.fhirDeviceRequestModelWithDentist
import de.gematik.ti.erp.app.fhir.prescription.mocks.FhirDeviceRequestTestData.fhirDeviceRequestModelWithInjury
import de.gematik.ti.erp.app.fhir.prescription.mocks.FhirDeviceRequestTestData.fhirDeviceRequestModelWithOccupationalDisease
import de.gematik.ti.erp.app.fhir.prescription.mocks.FhirDeviceRequestTestData.fhirDeviceRequestModelWithoutAccidentInfo
import de.gematik.ti.erp.app.fhir.prescription.mocks.FhirDeviceRequestTestData.fhirDeviceRequestModelWithoutSelfUse
import de.gematik.ti.erp.app.fhir.prescription.model.original.FhirDeviceRequestModel.Companion.getDeviceRequest
import de.gematik.ti.erp.app.fhir.prescription.model.original.FhirDeviceRequestModel.Companion.toErpModel
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals

class TaskKbvParserDeviceRequestTest {

    @Test
    fun `test parser for simple diga`() = runTest {
        val diga = Json.parseToJsonElement(digaSimple)
        val fhirDigaModel = diga.getDeviceRequest()
        val erpModel = fhirDigaModel?.toErpModel()
        assertEquals(fhirDeviceRequestModelWithoutAccidentInfo, fhirDigaModel)
        assertEquals(fhirTaskKbvDeviceRequestErpModelWithoutAccident, erpModel)
    }

    @Test
    fun `test parser for diga with accident`() = runTest {
        val diga = Json.parseToJsonElement(digaWithAccidentJson)
        val fhirDigaModel = diga.getDeviceRequest()
        val erpModel = fhirDigaModel?.toErpModel()
        assertEquals(fhirDeviceRequestModelWithAccidentInfo, fhirDigaModel)
        assertEquals(fhirTaskKbvDeviceRequestErpModelWithAccident, erpModel)
    }

    @Test
    fun `test parser for diga with accident type II`() = runTest {
        val diga = Json.parseToJsonElement(digaWithAccident2Json)
        val fhirDigaModel = diga.getDeviceRequest()
        val erpModel = fhirDigaModel?.toErpModel()
        assertEquals(fhirDeviceRequestModelWithAccident2, fhirDigaModel)
        assertEquals(fhirTaskKbvDeviceRequestErpModelType2, erpModel)
    }

    @Test
    fun `test parser for diga with accident type III`() = runTest {
        val diga = Json.parseToJsonElement(digaWithAccident3Json)
        val fhirDigaModel = diga.getDeviceRequest()
        val erpModel = fhirDigaModel?.toErpModel()
        assertEquals(fhirDeviceRequestModelAccident3, fhirDigaModel)
        assertEquals(fhirTaskKbvDeviceRequestErpModelType3, erpModel)
    }

    @Test
    fun `test parser for diga with occupational disease`() = runTest {
        val diga = Json.parseToJsonElement(digaOccupationalDiseaseJson)
        val fhirDigaModel = diga.getDeviceRequest()
        val erpModel = fhirDigaModel?.toErpModel()
        assertEquals(fhirDeviceRequestModelWithOccupationalDisease, fhirDigaModel)
        assertEquals(fhirTaskKbvDeviceRequestErpModelWithOccupationalDisease, erpModel)
    }

    @Test
    fun `test parser for diga with hospital address`() = runTest {
        val diga = Json.parseToJsonElement(digaHospitalWithAddressJson)
        val fhirDigaModel = diga.getDeviceRequest()
        val erpModel = fhirDigaModel?.toErpModel()
        assertEquals(fhirDeviceRequestModelTinnitus, fhirDigaModel)
        assertEquals(fhirTaskKbvDeviceRequestErpModelTinnitus, erpModel)
    }

    @Test
    fun `test parser for diga without selfUse`() = runTest {
        val diga = Json.parseToJsonElement(digaSelJson)
        val fhirDigaModel = diga.getDeviceRequest()
        val erpModel = fhirDigaModel?.toErpModel()
        assertEquals(fhirDeviceRequestModelWithoutSelfUse, fhirDigaModel)
        assertEquals(fhirTaskKbvDeviceRequestErpModelWithoutSelfUse, erpModel)
    }

    @Test
    fun `test parser for diga with injury`() = runTest {
        val diga = Json.parseToJsonElement(digaInjuryJson)
        val fhirDigaModel = diga.getDeviceRequest()
        val erpModel = fhirDigaModel?.toErpModel()
        assertEquals(fhirDeviceRequestModelWithInjury, fhirDigaModel)
        assertEquals(fhirTaskKbvDeviceRequestErpModelWithInjury, erpModel)
    }

    @Test
    fun `test parser for diga with dentist`() = runTest {
        val diga = Json.parseToJsonElement(digaDentistJson)
        val fhirDigaModel = diga.getDeviceRequest()
        val erpModel = fhirDigaModel?.toErpModel()
        assertEquals(fhirDeviceRequestModelWithDentist, fhirDigaModel)
        assertEquals(fhirTaskKbvDeviceRequestErpModelWithDentist, erpModel)
    }
}
