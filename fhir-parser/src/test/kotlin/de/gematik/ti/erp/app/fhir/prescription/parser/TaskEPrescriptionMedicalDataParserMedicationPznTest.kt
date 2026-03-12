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

import de.gematik.ti.erp.app.data.medicationPznJson_vers_1_0_2
import de.gematik.ti.erp.app.data.medicationPznJson_vers_1_1_0
import de.gematik.ti.erp.app.data.medicationPznJson_vers_1_2
import de.gematik.ti.erp.app.data.medicationPznJson_vers_1_3
import de.gematik.ti.erp.app.data.medicationPznWithAmountJson_vers_1_2
import de.gematik.ti.erp.app.data.medicationPzn_vers_1_6_noStrengthCode
import de.gematik.ti.erp.app.data.medicationPzn_vers_1_6_simple
import de.gematik.ti.erp.app.data.medicationPzn_vers_1_6_sumatripanmedication
import de.gematik.ti.erp.app.data.medicationPzn_vers_1_6_withStrengthCode
import de.gematik.ti.erp.app.fhir.prescription.mocks.FhirMedicationErpTestData.erpMedicationPznModelV102
import de.gematik.ti.erp.app.fhir.prescription.mocks.FhirMedicationErpTestData.erpMedicationPznModelV110
import de.gematik.ti.erp.app.fhir.prescription.mocks.FhirMedicationErpTestData.erpMedicationPznModelV12
import de.gematik.ti.erp.app.fhir.prescription.mocks.FhirMedicationErpTestData.erpMedicationPznModelV13
import de.gematik.ti.erp.app.fhir.prescription.mocks.FhirMedicationErpTestData.erpMedicationPznWithAmountModelV12
import de.gematik.ti.erp.app.fhir.prescription.mocks.FhirMedicationTestData.fhirMedicationPznModelV102
import de.gematik.ti.erp.app.fhir.prescription.mocks.FhirMedicationTestData.fhirMedicationPznModelV110
import de.gematik.ti.erp.app.fhir.prescription.mocks.FhirMedicationTestData.fhirMedicationPznModelV12
import de.gematik.ti.erp.app.fhir.prescription.mocks.FhirMedicationTestData.fhirMedicationPznWithAmountModelV12
import de.gematik.ti.erp.app.fhir.prescription.mocks.FhirMedicationTestData.medicationPznModelV13
import de.gematik.ti.erp.app.fhir.prescription.model.original.FhirMedication.Companion.getMedication
import de.gematik.ti.erp.app.fhir.prescription.model.original.FhirMedication.Companion.toErpModel
import kotlinx.serialization.json.Json
import org.junit.Test
import kotlin.test.assertEquals

class TaskEPrescriptionMedicalDataParserMedicationPznTest {

    @Test
    fun `test parser for medication pzn 16_sumatripanmedication`() {
        val bundle = Json.parseToJsonElement(medicationPzn_vers_1_6_sumatripanmedication)
        val fhirModel = bundle.getMedication()
        val erpModel = fhirModel?.toErpModel()

        // the url to find the ratio is different from version 1.6
        assertEquals("20", fhirModel?.amountRatio?.numerator?.value)
        assertEquals("St", fhirModel?.amountRatio?.numerator?.unit)
        assertEquals("20", erpModel?.amount?.numerator?.value)
        assertEquals("St", erpModel?.amount?.numerator?.unit)
    }

    @Test
    fun `test parser for medication pzn 16_noStrengthCode`() {
        val bundle = Json.parseToJsonElement(medicationPzn_vers_1_6_noStrengthCode)
        val fhirModel = bundle.getMedication()
        val erpModel = fhirModel?.toErpModel()

        // FHIR Model
        assertEquals(
            "Infusion bestehend aus 85mg Doxorubicin aufgeloest zur Verabreichung in 250ml 5-%iger (50 mg/ml) Glucose-Infusionsloesung",
            fhirModel?.code?.text
        )
        assertEquals("L01DB01", fhirModel?.getIdentifiers("http://fhir.de/CodeSystem/bfarm/atc"))

        // ERP Model
        assertEquals(
            "Infusion bestehend aus 85mg Doxorubicin aufgeloest zur Verabreichung in 250ml 5-%iger (50 mg/ml) Glucose-Infusionsloesung",
            erpModel?.text
        )
        assertEquals("L01DB01", erpModel?.identifier?.atc)
    }

    @Test
    fun `test parser for medication pzn 16_withStrengthCode`() {
        val bundle = Json.parseToJsonElement(medicationPzn_vers_1_6_withStrengthCode)
        val fhirModel = bundle.getMedication()
        val erpModel = fhirModel?.toErpModel()

        // FHIR Model
        assertEquals("08585997", fhirModel?.getIdentifiers("http://fhir.de/CodeSystem/ifa/pzn"))
        assertEquals("Prospan® Hustensaft 100ml N1", fhirModel?.code?.text)

        // ERP Model
        assertEquals("08585997", erpModel?.identifier?.pzn)
        assertEquals("Prospan® Hustensaft 100ml N1", erpModel?.text)
    }

    @Test
    fun `test parser for medication pzn 16_simple`() {
        val bundle = Json.parseToJsonElement(medicationPzn_vers_1_6_simple)
        val fhirModel = bundle.getMedication()
        val erpModel = fhirModel?.toErpModel()

        // FHIR Model
        assertEquals("06313728", fhirModel?.getIdentifiers("http://fhir.de/CodeSystem/ifa/pzn"))
        assertEquals("Simple Medication Text", fhirModel?.code?.text)
        assertEquals("1234567890", fhirModel?.batch?.lotNumber)

        // ERP Model
        assertEquals("06313728", erpModel?.identifier?.pzn)
        assertEquals("Simple Medication Text", erpModel?.text)
        assertEquals("1234567890", erpModel?.lotNumber)
    }

    @Test
    fun `test parser for medication pzn 13`() {
        val bundle = Json.parseToJsonElement(medicationPznJson_vers_1_3)
        val fhirModel = bundle.getMedication()
        val erpModel = fhirModel?.toErpModel()
        assertEquals(medicationPznModelV13, fhirModel)
        assertEquals(erpMedicationPznModelV13, erpModel)
    }

    @Test
    fun `test parser for medication pzn 12`() {
        val bundle = Json.parseToJsonElement(medicationPznJson_vers_1_2)
        val fhirModel = bundle.getMedication()
        val erpModel = fhirModel?.toErpModel()
        assertEquals(fhirMedicationPznModelV12, fhirModel)
        assertEquals(erpMedicationPznModelV12, erpModel)
    }

    @Test
    fun `test parser for medication pzn_with_amount 12`() {
        val bundle = Json.parseToJsonElement(medicationPznWithAmountJson_vers_1_2)
        val fhirModel = bundle.getMedication()
        val erpModel = fhirModel?.toErpModel()
        assertEquals(fhirMedicationPznWithAmountModelV12, fhirModel)
        assertEquals(erpMedicationPznWithAmountModelV12, erpModel)
    }

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
}
