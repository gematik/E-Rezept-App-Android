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

import de.gematik.ti.erp.app.data.organization1AllPresent_v110_json
import de.gematik.ti.erp.app.data.organization1NoAddress_v110_json
import de.gematik.ti.erp.app.data.organization1NoEmail_v110_json
import de.gematik.ti.erp.app.data.organization1NoFax_v110_json
import de.gematik.ti.erp.app.data.organization1NoTelecom_v110_json
import de.gematik.ti.erp.app.data.organization1_v103_json
import de.gematik.ti.erp.app.data.organization2_v103_json
import de.gematik.ti.erp.app.data.organization2_v110_json
import de.gematik.ti.erp.app.data.patient1_v103_json
import de.gematik.ti.erp.app.fhir.prescription.mocks.FhirOrganizationErpTestData.erpOrganization1AllPresent_v110
import de.gematik.ti.erp.app.fhir.prescription.mocks.FhirOrganizationErpTestData.erpOrganization1NoAddress_v110
import de.gematik.ti.erp.app.fhir.prescription.mocks.FhirOrganizationErpTestData.erpOrganization1NoContact_v110
import de.gematik.ti.erp.app.fhir.prescription.mocks.FhirOrganizationErpTestData.erpOrganization1NoEmail_v110
import de.gematik.ti.erp.app.fhir.prescription.mocks.FhirOrganizationErpTestData.erpOrganization1NoFax_v110
import de.gematik.ti.erp.app.fhir.prescription.mocks.FhirOrganizationErpTestData.erpOrganization1_v103
import de.gematik.ti.erp.app.fhir.prescription.mocks.FhirOrganizationErpTestData.erpOrganization2_v103
import de.gematik.ti.erp.app.fhir.prescription.mocks.FhirOrganizationErpTestData.erpOrganization2_v110
import de.gematik.ti.erp.app.fhir.prescription.mocks.FhirOrganizationTestData.fhirOrganization1_v103
import de.gematik.ti.erp.app.fhir.prescription.mocks.FhirOrganizationTestData.fhirOrganization1AllPresent_v110
import de.gematik.ti.erp.app.fhir.prescription.mocks.FhirOrganizationTestData.fhirOrganization1NoAddress_v110
import de.gematik.ti.erp.app.fhir.prescription.mocks.FhirOrganizationTestData.fhirOrganization1NoEmail_v110
import de.gematik.ti.erp.app.fhir.prescription.mocks.FhirOrganizationTestData.fhirOrganization1NoFax_v110
import de.gematik.ti.erp.app.fhir.prescription.mocks.FhirOrganizationTestData.fhirOrganization1NoTelecom_v110
import de.gematik.ti.erp.app.fhir.prescription.mocks.FhirOrganizationTestData.fhirOrganization2_v103
import de.gematik.ti.erp.app.fhir.prescription.mocks.FhirOrganizationTestData.fhirOrganization2_v110
import de.gematik.ti.erp.app.fhir.prescription.model.original.FhirOrganization.Companion.getOrganization
import de.gematik.ti.erp.app.fhir.prescription.model.original.FhirOrganization.Companion.toErpModel
import kotlinx.serialization.json.Json
import org.junit.Test
import kotlin.test.assertEquals

class TaskEPrescriptionMedicalDataParserOrganizationTest {

    @Test
    fun `test parser for organization organization 1 version 1_0_3`() {
        val bundle = Json.parseToJsonElement(organization1_v103_json)
        val fhirModel = bundle.getOrganization()
        val erpModel = fhirModel?.toErpModel()
        assertEquals(fhirOrganization1_v103, fhirModel)
        assertEquals(erpOrganization1_v103, erpModel)
    }

    @Test
    fun `test parser for organization organization 2 version 1_0_3`() {
        val bundle = Json.parseToJsonElement(organization2_v103_json)
        val fhirModel = bundle.getOrganization()
        val erpModel = fhirModel?.toErpModel()
        assertEquals(fhirOrganization2_v103, fhirModel)
        assertEquals(erpOrganization2_v103, erpModel)
    }

    @Test
    fun `test parser for organization 1 version 1_1_0 all present`() {
        val bundleAllPresent = Json.parseToJsonElement(organization1AllPresent_v110_json)
        val fhirModelPresent = bundleAllPresent.getOrganization()
        val erpModelModelPresent = fhirModelPresent?.toErpModel()
        assertEquals(fhirOrganization1AllPresent_v110, fhirModelPresent)
        assertEquals(erpOrganization1AllPresent_v110, erpModelModelPresent)
    }

    @Test
    fun `test parser for organization 1 version 1_1_0 no address`() {
        val bundle = Json.parseToJsonElement(organization1NoAddress_v110_json)
        val fhirModel = bundle.getOrganization()
        val erpModel = fhirModel?.toErpModel()
        assertEquals(fhirOrganization1NoAddress_v110, fhirModel)
        assertEquals(erpOrganization1NoAddress_v110, erpModel)
    }

    @Test
    fun `test parser for organization 1 version 1_1_0 no email`() {
        val bundle = Json.parseToJsonElement(organization1NoEmail_v110_json)
        val fhirModel = bundle.getOrganization()
        val erpModel = fhirModel?.toErpModel()
        assertEquals(fhirOrganization1NoEmail_v110, fhirModel)
        assertEquals(erpOrganization1NoEmail_v110, erpModel)
    }

    @Test
    fun `test parser for organization 1 version 1_1_0 no fax`() {
        val bundle = Json.parseToJsonElement(organization1NoFax_v110_json)
        val fhirModel = bundle.getOrganization()
        val erpModel = fhirModel?.toErpModel()
        assertEquals(fhirOrganization1NoFax_v110, fhirModel)
        assertEquals(erpOrganization1NoFax_v110, erpModel)
    }

    @Test
    fun `test parser for organization 1 version 1_1_0 no telecom`() {
        val bundle = Json.parseToJsonElement(organization1NoTelecom_v110_json)
        val fhirModel = bundle.getOrganization()
        val erpModel = fhirModel?.toErpModel()
        assertEquals(fhirOrganization1NoTelecom_v110, fhirModel)
        assertEquals(erpOrganization1NoContact_v110, erpModel)
    }

    @Test
    fun `test parser for organization 2 version 1_1_0`() {
        val bundle = Json.parseToJsonElement(organization2_v110_json)
        val fhirModel = bundle.getOrganization()
        val erpModel = fhirModel?.toErpModel()
        assertEquals(fhirOrganization2_v110, fhirModel)
        assertEquals(erpOrganization2_v110, erpModel)
    }

    @Test
    fun `test parser for organization 1 version 1_1_0 with wrong profile`() {
        val bundle = Json.parseToJsonElement(patient1_v103_json)
        val fhirModel = bundle.getOrganization()
        val erpModel = fhirModel?.toErpModel()
        assertEquals(null, fhirModel)
        assertEquals(null, erpModel)
    }
}
