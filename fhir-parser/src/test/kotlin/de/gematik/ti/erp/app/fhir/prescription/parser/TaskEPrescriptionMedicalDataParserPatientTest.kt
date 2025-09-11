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

import de.gematik.ti.erp.app.data.patient1_incomplete_birth_date_v103_json
import de.gematik.ti.erp.app.data.patient1_v103_json
import de.gematik.ti.erp.app.data.patient1_v110_json
import de.gematik.ti.erp.app.data.patient2_v103_json
import de.gematik.ti.erp.app.data.patient2_v110_json
import de.gematik.ti.erp.app.data.patient3_pkv_v110_json
import de.gematik.ti.erp.app.data.patient4_v12_json
import de.gematik.ti.erp.app.data.patient5_v12_json
import de.gematik.ti.erp.app.fhir.prescription.mocks.FhirPatientErpTestData.erpPatient1IncompleteBirth_v103
import de.gematik.ti.erp.app.fhir.prescription.mocks.FhirPatientErpTestData.erpPatient1_v103
import de.gematik.ti.erp.app.fhir.prescription.mocks.FhirPatientErpTestData.erpPatient1_v110
import de.gematik.ti.erp.app.fhir.prescription.mocks.FhirPatientErpTestData.erpPatient2_v103
import de.gematik.ti.erp.app.fhir.prescription.mocks.FhirPatientErpTestData.erpPatient2_v110
import de.gematik.ti.erp.app.fhir.prescription.mocks.FhirPatientErpTestData.erpPatient3_v110
import de.gematik.ti.erp.app.fhir.prescription.mocks.FhirPatientErpTestData.erpPatient4_v12
import de.gematik.ti.erp.app.fhir.prescription.mocks.FhirPatientErpTestData.erpPatient5_v12
import de.gematik.ti.erp.app.fhir.prescription.mocks.FhirPatientTestData.fhirPatient1IncompleteBirth_v103
import de.gematik.ti.erp.app.fhir.prescription.mocks.FhirPatientTestData.fhirPatient1_v103
import de.gematik.ti.erp.app.fhir.prescription.mocks.FhirPatientTestData.fhirPatient1_v110
import de.gematik.ti.erp.app.fhir.prescription.mocks.FhirPatientTestData.fhirPatient2_v103
import de.gematik.ti.erp.app.fhir.prescription.mocks.FhirPatientTestData.fhirPatient2_v110
import de.gematik.ti.erp.app.fhir.prescription.mocks.FhirPatientTestData.fhirPatient3_v110
import de.gematik.ti.erp.app.fhir.prescription.mocks.FhirPatientTestData.fhirPatient4_v12
import de.gematik.ti.erp.app.fhir.prescription.mocks.FhirPatientTestData.fhirPatient5_v12
import de.gematik.ti.erp.app.fhir.prescription.model.original.FhirPatient.Companion.getPatient
import de.gematik.ti.erp.app.fhir.prescription.model.original.FhirPatient.Companion.toErpModel
import de.gematik.ti.erp.app.fhir.prescription.model.original.KbvBundleVersion
import kotlinx.serialization.json.Json
import org.junit.Test
import kotlin.test.assertEquals

class TaskEPrescriptionMedicalDataParserPatientTest {

    @Test
    fun `test parser for patient 1 version 1_0_3`() {
        val bundle = Json.parseToJsonElement(patient1_v103_json)
        val fhirModel = bundle.getPatient()
        val erpModel = fhirModel?.toErpModel(KbvBundleVersion.V_1_0_3)
        assertEquals(fhirPatient1_v103, fhirModel)
        assertEquals(erpPatient1_v103, erpModel)
    }

    @Test
    fun `test parser for patient 2 version 1_0_3`() {
        val bundle = Json.parseToJsonElement(patient2_v103_json)
        val fhirModel = bundle.getPatient()
        val erpModel = fhirModel?.toErpModel(KbvBundleVersion.V_1_0_3)
        assertEquals(fhirPatient2_v103, fhirModel)
        assertEquals(erpPatient2_v103, erpModel)
    }

    @Test
    fun `test parser for patient 1 version 1_0_3 with incomplete_birthDate`() {
        val bundle = Json.parseToJsonElement(patient1_incomplete_birth_date_v103_json)
        val fhirModel = bundle.getPatient()
        val erpModel = fhirModel?.toErpModel(KbvBundleVersion.V_1_0_3)
        assertEquals(fhirPatient1IncompleteBirth_v103, fhirModel)
        assertEquals(erpPatient1IncompleteBirth_v103, erpModel)
    }

    @Test
    fun `test parser for patient 1 version 1_1_0`() {
        val bundle = Json.parseToJsonElement(patient1_v110_json)
        val fhirModel = bundle.getPatient()
        val erpModel = fhirModel?.toErpModel(KbvBundleVersion.V_1_1_0)
        assertEquals(fhirPatient1_v110, fhirModel)
        assertEquals(erpPatient1_v110, erpModel)
    }

    @Test
    fun `test parser for patient 2 version 1_1_0`() {
        val bundle = Json.parseToJsonElement(patient2_v110_json)
        val fhirModel = bundle.getPatient()
        val erpModel = fhirModel?.toErpModel(KbvBundleVersion.V_1_1_0)
        assertEquals(fhirPatient2_v110, fhirModel)
        assertEquals(erpPatient2_v110, erpModel)
    }

    @Test
    fun `test parser for patient 3 pkv version 1_1_0`() {
        val bundle = Json.parseToJsonElement(patient3_pkv_v110_json)
        val fhirModel = bundle.getPatient()
        val erpModel = fhirModel?.toErpModel(KbvBundleVersion.V_1_1_0)
        assertEquals(fhirPatient3_v110, fhirModel)
        assertEquals(erpPatient3_v110, erpModel)
    }

    @Test
    fun `test parser for patient 4 version 1_2`() {
        val bundle = Json.parseToJsonElement(patient4_v12_json)
        val fhirModel = bundle.getPatient()
        val erpModel = fhirModel?.toErpModel(KbvBundleVersion.V_1_2)
        assertEquals(fhirPatient4_v12, fhirModel)
        assertEquals(erpPatient4_v12, erpModel)
    }

    @Test
    fun `test parser for patient 5 version 1_3`() {
        val bundle = Json.parseToJsonElement(patient5_v12_json)
        val fhirModel = bundle.getPatient()
        val erpModel = fhirModel?.toErpModel(KbvBundleVersion.V_1_3)
        assertEquals(fhirPatient5_v12, fhirModel)
        assertEquals(erpPatient5_v12, erpModel)
    }
}
