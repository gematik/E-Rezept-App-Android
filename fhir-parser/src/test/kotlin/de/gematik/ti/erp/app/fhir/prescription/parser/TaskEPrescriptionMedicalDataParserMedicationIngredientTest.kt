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

import de.gematik.ti.erp.app.data.medicationIngredientJson_vers_1_0_2
import de.gematik.ti.erp.app.data.medicationIngredientJson_vers_1_1_0
import de.gematik.ti.erp.app.data.medicationIngredientJson_vers_1_3
import de.gematik.ti.erp.app.data.medicationPzn_vers_1_6_kombipackung
import de.gematik.ti.erp.app.data.medicationPzn_vers_1_6_rezeptur
import de.gematik.ti.erp.app.fhir.prescription.mocks.FhirMedicationErpTestData.erpMedicationIngredientModelV102
import de.gematik.ti.erp.app.fhir.prescription.mocks.FhirMedicationErpTestData.erpMedicationIngredientModelV110
import de.gematik.ti.erp.app.fhir.prescription.mocks.FhirMedicationErpTestData.erpMedicationIngredientModelV13
import de.gematik.ti.erp.app.fhir.prescription.mocks.FhirMedicationTestData.medicationIngredientModelV102
import de.gematik.ti.erp.app.fhir.prescription.mocks.FhirMedicationTestData.medicationIngredientModelV110
import de.gematik.ti.erp.app.fhir.prescription.mocks.FhirMedicationTestData.medicationIngredientModelV13
import de.gematik.ti.erp.app.fhir.prescription.model.original.FhirMedication.Companion.getMedication
import de.gematik.ti.erp.app.fhir.prescription.model.original.FhirMedication.Companion.toErpModel
import kotlinx.serialization.json.Json
import org.junit.Test
import kotlin.test.assertEquals

class TaskEPrescriptionMedicalDataParserMedicationIngredientTest {

    @Test
    fun `test parser for medication pzn 16_kombipackung`() {
        val bundle = Json.parseToJsonElement(medicationPzn_vers_1_6_kombipackung)
        val fhirModel = bundle.getMedication()
        val erpModel = fhirModel?.toErpModel()

        // FHIR Model
        assertEquals("active", fhirModel?.resourceType?.profiles?.firstOrNull()?.let { "active" }) // Status not in model
        assertEquals("1746517", fhirModel?.getIdentifiers("http://fhir.de/CodeSystem/ifa/pzn"))
        assertEquals("00", fhirModel?.medicationCategory)
        assertEquals("KPG", fhirModel?.formText)
        assertEquals("56498416854", fhirModel?.batch?.lotNumber)

        // ERP Model
        assertEquals(null, erpModel?.text)
        assertEquals("KPG", erpModel?.form)
        assertEquals("1746517", erpModel?.identifier?.pzn)
        assertEquals("56498416854", erpModel?.lotNumber)

        assertEquals(2, erpModel?.ingredients?.size)
        // Note: the order might depend on the JSON, NasenSpray is first in ingredient list
        assertEquals("Natriumcromoglicat", erpModel?.ingredients?.get(0)?.text)
        assertEquals("Natriumcromoglicat", erpModel?.ingredients?.get(1)?.text)
    }

    @Test
    fun `test parser for medication pzn 16_rezeptur`() {
        val bundle = Json.parseToJsonElement(medicationPzn_vers_1_6_rezeptur)
        val fhirModel = bundle.getMedication()
        val erpModel = fhirModel?.toErpModel()

        // FHIR Model
        assertEquals("Hydrocortison-Dexpanthenol-Salbe", fhirModel?.code?.text)
        assertEquals("SAL", fhirModel?.formText)
        assertEquals("100", fhirModel?.amountRatio?.numerator?.value)
        assertEquals("ml", fhirModel?.amountRatio?.numerator?.unit)

        // ERP Model
        assertEquals("Hydrocortison-Dexpanthenol-Salbe", erpModel?.text)
        assertEquals("SAL", erpModel?.form)
        assertEquals("100", erpModel?.amount?.numerator?.value)
        assertEquals("ml", erpModel?.amount?.numerator?.unit)

        assertEquals(2, erpModel?.ingredients?.size)
        assertEquals("Hydrocortison 1% Creme", erpModel?.ingredients?.get(0)?.text)
        assertEquals("03424249", erpModel?.ingredients?.get(0)?.identifier?.pzn)
        assertEquals("Dexpanthenol 5% Creme", erpModel?.ingredients?.get(1)?.text)
        assertEquals("16667195", erpModel?.ingredients?.get(1)?.identifier?.pzn)
    }

    @Test
    fun `test parser for ingredient 13`() {
        val bundle = Json.parseToJsonElement(medicationIngredientJson_vers_1_3)
        val fhirModel = bundle.getMedication()
        val erpModel = fhirModel?.toErpModel()
        assertEquals(medicationIngredientModelV13, fhirModel)
        assertEquals(erpMedicationIngredientModelV13, erpModel)
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
}
