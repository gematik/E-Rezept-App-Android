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

import de.gematik.ti.erp.app.data.organizationAllPresentJson
import de.gematik.ti.erp.app.data.organizationJson
import de.gematik.ti.erp.app.data.organizationNoAddressJson
import de.gematik.ti.erp.app.data.organizationNoEmailJson
import de.gematik.ti.erp.app.data.organizationNoFaxJson
import de.gematik.ti.erp.app.data.organizationNoTelecomJson
import de.gematik.ti.erp.app.data.patientJson_vers_1_0_2
import de.gematik.ti.erp.app.fhir.prescription.mocks.FhirOrganizationErpTestData.erpOrganizationAllPresent2
import de.gematik.ti.erp.app.fhir.prescription.mocks.FhirOrganizationErpTestData.erpOrganizationNoAddress
import de.gematik.ti.erp.app.fhir.prescription.mocks.FhirOrganizationErpTestData.erpOrganizationNoContact
import de.gematik.ti.erp.app.fhir.prescription.mocks.FhirOrganizationErpTestData.erpOrganizationNoEmail
import de.gematik.ti.erp.app.fhir.prescription.mocks.FhirOrganizationErpTestData.erpOrganizationNoStreet
import de.gematik.ti.erp.app.fhir.prescription.mocks.FhirOrganizationErpTestData.erpTaskOrganizationAllPresent1
import de.gematik.ti.erp.app.fhir.prescription.mocks.FhirOrganizationTestData.fhirOrganizationAllPresent1
import de.gematik.ti.erp.app.fhir.prescription.mocks.FhirOrganizationTestData.fhirOrganizationAllPresent2
import de.gematik.ti.erp.app.fhir.prescription.mocks.FhirOrganizationTestData.fhirOrganizationNoAddress
import de.gematik.ti.erp.app.fhir.prescription.mocks.FhirOrganizationTestData.organizationNoEmail
import de.gematik.ti.erp.app.fhir.prescription.mocks.FhirOrganizationTestData.organizationNoFax
import de.gematik.ti.erp.app.fhir.prescription.mocks.FhirOrganizationTestData.organizationNoTelecom
import de.gematik.ti.erp.app.fhir.prescription.model.original.FhirOrganization.Companion.getOrganization
import de.gematik.ti.erp.app.fhir.prescription.model.original.FhirOrganization.Companion.toErpModel
import kotlinx.serialization.json.Json
import org.junit.Test
import kotlin.test.assertEquals

class TaskEPrescriptionMedicalDataParserOrganizationTest {

    @Test
    fun `test parser for organization all present`() {
        // bundle 1
        val bundle = Json.parseToJsonElement(organizationJson)
        val fhirModel = bundle.getOrganization()
        val erpModel = fhirModel?.toErpModel()
        assertEquals(fhirOrganizationAllPresent1, fhirModel)
        assertEquals(erpTaskOrganizationAllPresent1, erpModel)

        // bundle 2
        val bundleAllPresent = Json.parseToJsonElement(organizationAllPresentJson)
        val fhirModelPresent = bundleAllPresent.getOrganization()
        val erpModelModelPresent = fhirModelPresent?.toErpModel()
        assertEquals(fhirOrganizationAllPresent2, fhirModelPresent)
        assertEquals(erpOrganizationAllPresent2, erpModelModelPresent)
    }

    @Test
    fun `test parser for organization no address`() {
        val bundle = Json.parseToJsonElement(organizationNoAddressJson)
        val fhirModel = bundle.getOrganization()
        val erpModel = fhirModel?.toErpModel()
        assertEquals(fhirOrganizationNoAddress, fhirModel)
        assertEquals(erpOrganizationNoAddress, erpModel)
    }

    @Test
    fun `test parser for organization no email`() {
        val bundle = Json.parseToJsonElement(organizationNoEmailJson)
        val fhirModel = bundle.getOrganization()
        val erpModel = fhirModel?.toErpModel()
        assertEquals(organizationNoEmail, fhirModel)
        assertEquals(erpOrganizationNoEmail, erpModel)
    }

    @Test
    fun `test parser for organization no fax`() {
        val bundle = Json.parseToJsonElement(organizationNoFaxJson)
        val fhirModel = bundle.getOrganization()
        val erpModel = fhirModel?.toErpModel()
        assertEquals(organizationNoFax, fhirModel)
        assertEquals(erpOrganizationNoStreet, erpModel)
    }

    @Test
    fun `test parser for organization no telecom`() {
        val bundle = Json.parseToJsonElement(organizationNoTelecomJson)
        val fhirModel = bundle.getOrganization()
        val erpModel = fhirModel?.toErpModel()
        assertEquals(organizationNoTelecom, fhirModel)
        assertEquals(erpOrganizationNoContact, erpModel)
    }

    @Test
    fun `test parser for organization with wrong profile`() {
        val bundle = Json.parseToJsonElement(patientJson_vers_1_0_2)
        val fhirModel = bundle.getOrganization()
        val erpModel = fhirModel?.toErpModel()
        assertEquals(null, fhirModel)
        assertEquals(null, erpModel)
    }
}
