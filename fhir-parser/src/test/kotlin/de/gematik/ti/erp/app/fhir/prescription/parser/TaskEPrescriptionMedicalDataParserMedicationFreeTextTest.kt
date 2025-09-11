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

import de.gematik.ti.erp.app.data.medicationFreetextJson_vers_1_0_2
import de.gematik.ti.erp.app.data.medicationFreetextJson_vers_1_1_0
import de.gematik.ti.erp.app.data.medicationFreetextJson_vers_1_3
import de.gematik.ti.erp.app.fhir.prescription.mocks.FhirMedicationErpTestData.erpMedicationFreeTextModelV102
import de.gematik.ti.erp.app.fhir.prescription.mocks.FhirMedicationErpTestData.erpMedicationFreeTextModelV110
import de.gematik.ti.erp.app.fhir.prescription.mocks.FhirMedicationErpTestData.erpMedicationFreeTextModelV13
import de.gematik.ti.erp.app.fhir.prescription.mocks.FhirMedicationTestData.fhirMedicationFreeTextModelV102
import de.gematik.ti.erp.app.fhir.prescription.mocks.FhirMedicationTestData.fhirMedicationFreeTextModelV110
import de.gematik.ti.erp.app.fhir.prescription.mocks.FhirMedicationTestData.fhirMedicationFreeTextModelV13
import de.gematik.ti.erp.app.fhir.prescription.model.original.FhirMedication.Companion.getMedication
import de.gematik.ti.erp.app.fhir.prescription.model.original.FhirMedication.Companion.toErpModel
import kotlinx.serialization.json.Json
import org.junit.Test
import kotlin.test.assertEquals

class TaskEPrescriptionMedicalDataParserMedicationFreeTextTest {

    @Test
    fun `test parser for freetext 13`() {
        val bundle = Json.parseToJsonElement(medicationFreetextJson_vers_1_3)
        val fhirModel = bundle.getMedication()
        val erpModel = fhirModel?.toErpModel()
        assertEquals(fhirMedicationFreeTextModelV13, fhirModel)
        assertEquals(erpMedicationFreeTextModelV13, erpModel)
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
