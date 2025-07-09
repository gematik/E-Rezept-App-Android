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

import de.gematik.ti.erp.app.data.medicationCompoundingJson_vers_1_0_2
import de.gematik.ti.erp.app.data.medicationCompoundingJson_vers_1_1_0
import de.gematik.ti.erp.app.data.medicationFreetextJson_vers_1_0_2
import de.gematik.ti.erp.app.data.medicationFreetextJson_vers_1_1_0
import de.gematik.ti.erp.app.data.medicationIngredientJson_vers_1_0_2
import de.gematik.ti.erp.app.data.medicationIngredientJson_vers_1_1_0
import de.gematik.ti.erp.app.data.medicationPznJson_vers_1_0_2
import de.gematik.ti.erp.app.data.medicationPznJson_vers_1_1_0
import de.gematik.ti.erp.app.fhir.prescription.mocks.FhirMedicationErpTestData.erpMedicationCompoundingMedicationV102
import de.gematik.ti.erp.app.fhir.prescription.mocks.FhirMedicationErpTestData.erpMedicationCompoundingMedicationV110
import de.gematik.ti.erp.app.fhir.prescription.mocks.FhirMedicationErpTestData.erpMedicationFreeTextModelV102
import de.gematik.ti.erp.app.fhir.prescription.mocks.FhirMedicationErpTestData.erpMedicationFreeTextModelV110
import de.gematik.ti.erp.app.fhir.prescription.mocks.FhirMedicationErpTestData.erpMedicationIngredientModelV102
import de.gematik.ti.erp.app.fhir.prescription.mocks.FhirMedicationErpTestData.erpMedicationIngredientModelV110
import de.gematik.ti.erp.app.fhir.prescription.mocks.FhirMedicationErpTestData.erpMedicationPznModelV102
import de.gematik.ti.erp.app.fhir.prescription.mocks.FhirMedicationErpTestData.erpMedicationPznModelV110
import de.gematik.ti.erp.app.fhir.prescription.mocks.FhirMedicationTestData.fhirMedicationCompoundingModelV102
import de.gematik.ti.erp.app.fhir.prescription.mocks.FhirMedicationTestData.fhirMedicationCompoundingModelV110
import de.gematik.ti.erp.app.fhir.prescription.mocks.FhirMedicationTestData.fhirMedicationFreeTextModelV102
import de.gematik.ti.erp.app.fhir.prescription.mocks.FhirMedicationTestData.fhirMedicationFreeTextModelV110
import de.gematik.ti.erp.app.fhir.prescription.mocks.FhirMedicationTestData.fhirMedicationPznModelV102
import de.gematik.ti.erp.app.fhir.prescription.mocks.FhirMedicationTestData.fhirMedicationPznModelV110
import de.gematik.ti.erp.app.fhir.prescription.mocks.FhirMedicationTestData.medicationIngredientModelV102
import de.gematik.ti.erp.app.fhir.prescription.mocks.FhirMedicationTestData.medicationIngredientModelV110
import de.gematik.ti.erp.app.fhir.prescription.model.original.FhirMedication.Companion.getMedication
import de.gematik.ti.erp.app.fhir.prescription.model.original.FhirMedication.Companion.toErpModel
import kotlinx.serialization.json.Json
import org.junit.Test
import kotlin.test.assertEquals

class TaskEPrescriptionMedicalDataParserMedicationTest {

    @Test
    fun `test parser for medication pzn 102`() {
        val bundle = Json.parseToJsonElement(medicationPznJson_vers_1_0_2)
        val fhirModel = bundle.getMedication()
        val erpModel = fhirModel?.toErpModel()
        assertEquals(fhirMedicationPznModelV102, fhirModel)
        assertEquals(erpMedicationPznModelV102, erpModel)
    }

    @Test
    fun `test parser for pzn 110`() {
        val bundle = Json.parseToJsonElement(medicationPznJson_vers_1_1_0)
        val fhirModel = bundle.getMedication()
        val erpModel = fhirModel?.toErpModel()
        assertEquals(fhirMedicationPznModelV110, fhirModel)
        assertEquals(erpMedicationPznModelV110, erpModel)
    }

    @Test
    fun `test parser for ingredient 102`() {
        val bundle = Json.parseToJsonElement(medicationIngredientJson_vers_1_0_2)
        val fhirModel = bundle.getMedication()
        val erpModel = fhirModel?.toErpModel()
        assertEquals(medicationIngredientModelV102, fhirModel)
        assertEquals(erpMedicationIngredientModelV102, erpModel)
    }

    @Test
    fun `test parser for ingredient 110`() {
        val bundle = Json.parseToJsonElement(medicationIngredientJson_vers_1_1_0)
        val fhirModel = bundle.getMedication()
        val erpModel = fhirModel?.toErpModel()
        assertEquals(medicationIngredientModelV110, fhirModel)
        assertEquals(erpMedicationIngredientModelV110, erpModel)
    }

    @Test
    fun `test parser for compounding 102`() {
        val bundle = Json.parseToJsonElement(medicationCompoundingJson_vers_1_0_2)
        val fhirModel = bundle.getMedication()
        val erpModel = fhirModel?.toErpModel()
        assertEquals(fhirMedicationCompoundingModelV102, fhirModel)
        assertEquals(erpMedicationCompoundingMedicationV102, erpModel)
    }

    @Test
    fun `test parser for compounding 110`() {
        val bundle = Json.parseToJsonElement(medicationCompoundingJson_vers_1_1_0)
        val fhirModel = bundle.getMedication()
        val erpModel = fhirModel?.toErpModel()
        assertEquals(fhirMedicationCompoundingModelV110, fhirModel)
        assertEquals(erpMedicationCompoundingMedicationV110, erpModel)
    }

    @Test
    fun `test parser for freetext 102`() {
        val bundle = Json.parseToJsonElement(medicationFreetextJson_vers_1_0_2)
        val fhirModel = bundle.getMedication()
        val erpModel = fhirModel?.toErpModel()
        assertEquals(fhirMedicationFreeTextModelV102, fhirModel)
        assertEquals(erpMedicationFreeTextModelV102, erpModel)
    }

    @Test
    fun `test parser for freetext 110`() {
        val bundle = Json.parseToJsonElement(medicationFreetextJson_vers_1_1_0)
        val fhirModel = bundle.getMedication()
        val erpModel = fhirModel?.toErpModel()
        assertEquals(fhirMedicationFreeTextModelV110, fhirModel)
        assertEquals(erpMedicationFreeTextModelV110, erpModel)
    }
}
