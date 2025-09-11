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
import de.gematik.ti.erp.app.data.medicationCompoundingJson_vers_1_3
import de.gematik.ti.erp.app.fhir.prescription.mocks.FhirMedicationErpTestData.erpMedicationCompoundingMedicationV102
import de.gematik.ti.erp.app.fhir.prescription.mocks.FhirMedicationErpTestData.erpMedicationCompoundingMedicationV110
import de.gematik.ti.erp.app.fhir.prescription.mocks.FhirMedicationErpTestData.erpMedicationCompoundingMedicationV13
import de.gematik.ti.erp.app.fhir.prescription.mocks.FhirMedicationTestData.fhirMedicationCompoundingModelV102
import de.gematik.ti.erp.app.fhir.prescription.mocks.FhirMedicationTestData.fhirMedicationCompoundingModelV110
import de.gematik.ti.erp.app.fhir.prescription.mocks.FhirMedicationTestData.fhirMedicationCompoundingModelV13
import de.gematik.ti.erp.app.fhir.prescription.model.original.FhirMedication.Companion.getMedication
import de.gematik.ti.erp.app.fhir.prescription.model.original.FhirMedication.Companion.toErpModel
import kotlinx.serialization.json.Json
import org.junit.Test
import kotlin.test.assertEquals

class TaskEPrescriptionMedicalDataParserMedicationCompoundingTest {

    @Test
    fun `test parser for compounding 13`() {
        val bundle = Json.parseToJsonElement(medicationCompoundingJson_vers_1_3)
        val fhirModel = bundle.getMedication()
        val erpModel = fhirModel?.toErpModel()
        assertEquals(fhirMedicationCompoundingModelV13, fhirModel)
        assertEquals(erpMedicationCompoundingMedicationV13, erpModel)
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
}
