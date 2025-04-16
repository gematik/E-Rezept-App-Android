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

package de.gematik.ti.erp.app.consent.model

import de.gematik.ti.erp.app.fhir.model.ResourceBasePath
import de.gematik.ti.erp.app.fhir.parser.contained
import de.gematik.ti.erp.app.fhir.parser.containedString
import de.gematik.ti.erp.app.navigation.json
import org.junit.Test
import java.io.File
import kotlin.test.assertEquals

class ConsentMapperTest {

    private val expectedInsuranceId = "X123456789"
    private val consent by lazy { File("$ResourceBasePath/fhir/pkv/pkv1_2/consent.json").readText() }

    @Test
    fun createConsent() {
        val consent = createConsent(expectedInsuranceId)

        val profileString = consent.contained("meta")
            .contained("profile")
            .containedString()

        val insuranceId = consent.contained("patient")
            .contained("identifier").contained("value").containedString()

        assertEquals("https://gematik.de/fhir/erpchrg/StructureDefinition/GEM_ERPCHRG_PR_Consent|1.0", profileString)
        assertEquals(expectedInsuranceId, insuranceId)
    }

    @Test
    fun extractConsentBundle() {
        val consent = json.parseToJsonElement(consent)
        val consentType = extractConsent(consent)
        assertEquals(ConsentType.Charge, consentType)
    }

    @Test
    fun `extractConsentBundle - should return null`() {
        val consent = json.parseToJsonElement("""{}""")
        val consentType = extractConsent(consent)
        assertEquals(null, consentType)
    }
}
