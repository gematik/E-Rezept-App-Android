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

import de.gematik.ti.erp.app.data.patientJson_vers_1_0_2
import de.gematik.ti.erp.app.data.patientJson_vers_1_0_2_with_incomplete_birthDate
import de.gematik.ti.erp.app.data.patientJson_vers_1_1_0
import de.gematik.ti.erp.app.fhir.prescription.mocks.FhirPatientErpTestData.erpPatient103
import de.gematik.ti.erp.app.fhir.prescription.mocks.FhirPatientErpTestData.erpPatient103IncompleteBirth
import de.gematik.ti.erp.app.fhir.prescription.mocks.FhirPatientErpTestData.erpPatient110
import de.gematik.ti.erp.app.fhir.prescription.mocks.FhirPatientTestData.fhirPatient103
import de.gematik.ti.erp.app.fhir.prescription.mocks.FhirPatientTestData.fhirPatient103IncompleteBirth
import de.gematik.ti.erp.app.fhir.prescription.mocks.FhirPatientTestData.fhirPatient110
import de.gematik.ti.erp.app.fhir.prescription.model.erp.KbvBundleVersion
import de.gematik.ti.erp.app.fhir.prescription.model.original.FhirPatient.Companion.getPatient
import de.gematik.ti.erp.app.fhir.prescription.model.original.FhirPatient.Companion.toErpModel
import kotlinx.serialization.json.Json
import org.junit.Test
import kotlin.test.assertEquals

class TaskEPrescriptionMedicalDataParserPatientTest {

    @Test
    fun `test parser for patient version 1_0_3`() {
        val bundle = Json.parseToJsonElement(patientJson_vers_1_0_2)
        val fhirModel = bundle.getPatient()
        val erpModel = fhirModel?.toErpModel(KbvBundleVersion.V_1_0_3)
        assertEquals(fhirPatient103, fhirModel)
        assertEquals(erpPatient103, erpModel)
    }

    @Test
    fun `test parser for patient version 1_0_3 with incomplete_birthDate`() {
        val bundle = Json.parseToJsonElement(patientJson_vers_1_0_2_with_incomplete_birthDate)
        val fhirModel = bundle.getPatient()
        val erpModel = fhirModel?.toErpModel(KbvBundleVersion.V_1_0_3)
        assertEquals(fhirPatient103IncompleteBirth, fhirModel)
        assertEquals(erpPatient103IncompleteBirth, erpModel)
    }

    @Test
    fun `test parser for patient version 1_1_0`() {
        val bundle = Json.parseToJsonElement(patientJson_vers_1_1_0)
        val fhirModel = bundle.getPatient()
        val erpModel = fhirModel?.toErpModel(KbvBundleVersion.V_1_1_0)
        assertEquals(fhirPatient110, fhirModel)
        assertEquals(erpPatient110, erpModel)
    }
}
