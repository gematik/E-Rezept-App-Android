/*
 * Copyright 2024, gematik GmbH
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

package de.gematik.ti.erp.app.fhir.prescription.parser.util

import de.gematik.ti.erp.app.fhir.model.kbvBundle_110_1
import de.gematik.ti.erp.app.fhir.prescription.model.original.FhirAuthor.Companion.findAuthorReferenceByType
import de.gematik.ti.erp.app.fhir.prescription.model.original.FhirAuthorBundle.Companion.getAuthorReferences
import de.gematik.ti.erp.app.fhir.prescription.util.ParserUtil
import kotlinx.serialization.json.Json
import org.junit.Test
import kotlin.test.assertEquals

class ParserUtilTest {

    @Test
    fun `test findValueByUrl to look for pvsId`() {
        val bundle = Json.parseToJsonElement(kbvBundle_110_1)
        val pvsId = ParserUtil.findValueByUrl(
            jsonElement = bundle,
            targetUrl = "https://fhir.kbv.de/NamingSystem/KBV_NS_FOR_Pruefnummer",
            mapKey = "system",
            mapValue = "value"
        )
        assert(pvsId == "Y/400/2107/36/999")
    }

    @Test
    fun `test find author reference`() {
        val bundle = Json.parseToJsonElement(kbvBundle_110_1)
        val practitionerId = bundle.getAuthorReferences().findAuthorReferenceByType("Practitioner")
        assertEquals("cb7558e2-0fdf-4107-93f6-07f13f39e067", practitionerId)
    }
}
