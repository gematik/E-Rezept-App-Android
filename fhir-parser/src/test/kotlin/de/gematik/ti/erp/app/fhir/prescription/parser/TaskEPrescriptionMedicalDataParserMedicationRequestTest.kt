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

import de.gematik.ti.erp.app.data.medicationRequestJson_vers_1_0_2
import de.gematik.ti.erp.app.data.medicationRequestJson_vers_1_1_0
import de.gematik.ti.erp.app.data.medicationRequestJson_vers_1_1_0_with_accident
import de.gematik.ti.erp.app.data.medicationRequestJson_vers_1_2
import de.gematik.ti.erp.app.data.medicationRequestJson_vers_1_3
import de.gematik.ti.erp.app.fhir.prescription.mocks.FhirMedicationRequestErpTestData.erpMedicationRequestModel12
import de.gematik.ti.erp.app.fhir.prescription.mocks.FhirMedicationRequestErpTestData.erpMedicationRequestModelV102
import de.gematik.ti.erp.app.fhir.prescription.mocks.FhirMedicationRequestErpTestData.erpMedicationRequestModelV110
import de.gematik.ti.erp.app.fhir.prescription.mocks.FhirMedicationRequestErpTestData.erpMedicationRequestModelV13
import de.gematik.ti.erp.app.fhir.prescription.mocks.FhirMedicationRequestErpTestData.erpMedicationRequestWithAccidentInfoModelV110
import de.gematik.ti.erp.app.fhir.prescription.mocks.FhirMedicationRequestTestData.fhirMedicationRequestModel12
import de.gematik.ti.erp.app.fhir.prescription.mocks.FhirMedicationRequestTestData.fhirMedicationRequestModelV102
import de.gematik.ti.erp.app.fhir.prescription.mocks.FhirMedicationRequestTestData.fhirMedicationRequestModelV110
import de.gematik.ti.erp.app.fhir.prescription.mocks.FhirMedicationRequestTestData.fhirMedicationRequestModelV13
import de.gematik.ti.erp.app.fhir.prescription.mocks.FhirMedicationRequestTestData.fhirMedicationRequestWithAccidentModelV110
import de.gematik.ti.erp.app.fhir.prescription.model.original.FhirMedicationRequest.Companion.getMedicationRequest
import de.gematik.ti.erp.app.fhir.prescription.model.original.FhirMedicationRequest.Companion.toErpModel
import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Test

class TaskEPrescriptionMedicalDataParserMedicationRequestTest {

    @Test
    fun `test parser for medication request 102`() {
        val bundle = Json.parseToJsonElement(medicationRequestJson_vers_1_0_2)
        val fhirModel = bundle.getMedicationRequest()
        val erpModel = fhirModel?.toErpModel()
        assertEquals(fhirMedicationRequestModelV102, fhirModel)
        assertEquals(erpMedicationRequestModelV102, erpModel)
    }

    @Test
    fun `test parser for medication request 110`() {
        val bundle = Json.parseToJsonElement(medicationRequestJson_vers_1_1_0)
        val fhirModel = bundle.getMedicationRequest()
        val erpModel = fhirModel?.toErpModel()
        assertEquals(fhirMedicationRequestModelV110, fhirModel)
        assertEquals(erpMedicationRequestModelV110, erpModel)
    }

    @Test
    fun `test parser for medication request 110 with accident`() {
        val bundle = Json.parseToJsonElement(medicationRequestJson_vers_1_1_0_with_accident)
        val fhirModel = bundle.getMedicationRequest()
        val erpModel = fhirModel?.toErpModel()
        assertEquals(fhirMedicationRequestWithAccidentModelV110, fhirModel)
        assertEquals(erpMedicationRequestWithAccidentInfoModelV110, erpModel)
    }

    @Test
    fun `test parser for medication request 1_2`() {
        val bundle = Json.parseToJsonElement(medicationRequestJson_vers_1_2)
        val fhirModel = bundle.getMedicationRequest()
        val erpModel = fhirModel?.toErpModel()
        assertEquals(fhirMedicationRequestModel12, fhirModel)
        assertEquals(erpMedicationRequestModel12, erpModel)
    }

    @Test
    fun `test parser for medication request 1_3`() {
        val bundle = Json.parseToJsonElement(medicationRequestJson_vers_1_3)
        val fhirModel = bundle.getMedicationRequest()
        val erpModel = fhirModel?.toErpModel()
        assertEquals(fhirMedicationRequestModelV13, fhirModel)
        assertEquals(erpMedicationRequestModelV13, erpModel)
    }
}
