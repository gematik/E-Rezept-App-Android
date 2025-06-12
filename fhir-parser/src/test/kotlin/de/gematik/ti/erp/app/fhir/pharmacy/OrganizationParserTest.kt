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

package de.gematik.ti.erp.app.fhir.pharmacy

import de.gematik.ti.erp.app.data.fhirVzdOrganizationBundle
import de.gematik.ti.erp.app.fhir.common.model.erp.FhirInstitutionTelematikId
import de.gematik.ti.erp.app.fhir.pharmacy.parser.OrganizationParser
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import org.junit.Test
import kotlin.test.assertEquals

class OrganizationParserTest {

    private val parser = OrganizationParser()

    @Test
    fun `parse fhirVzd bundle for pharmacy bundle`() = runTest {
        val bundle = Json.parseToJsonElement(fhirVzdOrganizationBundle)
        val result = parser.extract(bundle)
        assertEquals(FhirInstitutionTelematikId(id = "8-01-0000000134"), result)
    }
}
