/*
 * Copyright (c) 2021 gematik GmbH
 * 
 * Licensed under the EUPL, Version 1.2 or – as soon they will be approved by
 * the European Commission - subsequent versions of the EUPL (the Licence);
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at:
 * 
 *     https://joinup.ec.europa.eu/software/page/eupl
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the Licence is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Licence for the specific language governing permissions and
 * limitations under the Licence.
 * 
 */

package de.gematik.ti.erp.app.orderhealthcard.usecase

import org.junit.Test
import kotlin.test.assertEquals

private val contacts = """
    Kassenname;eGK und PIN Rufnummer;eGK und PIN eMail;eGK und PIN URL;PIN Rufnummer;PIN eMail;PIN URL
    Krankenkasse A;001422345336789;kk@example.com;https://www.krankenkasse.kk/;;;
    Krankenkasse B;;kk@example.com;https://www.krankenkasse.kk/;0098765423124;;
""".trimIndent()

class HealthCardOrderUseCaseTest {

    @Test
    fun `test loadHealthInsuranceContactsFromCSV() with expected data`() {
        loadHealthInsuranceContactsFromCSV(contacts.byteInputStream()).let {
            assertEquals("Krankenkasse A", it[0].name)
            assertEquals("kk@example.com", it[0].healthCardAndPinMail)
            assertEquals("001422345336789", it[0].healthCardAndPinPhone)
            assertEquals("https://www.krankenkasse.kk/", it[0].healthCardAndPinUrl)
            assertEquals(null, it[0].pinMail)
            assertEquals(null, it[0].pinPhone)
            assertEquals(null, it[0].pinUrl)

            assertEquals("Krankenkasse B", it[1].name)
            assertEquals("kk@example.com", it[1].healthCardAndPinMail)
            assertEquals(null, it[1].healthCardAndPinPhone)
            assertEquals("https://www.krankenkasse.kk/", it[1].healthCardAndPinUrl)
            assertEquals(null, it[1].pinMail)
            assertEquals("0098765423124", it[1].pinPhone)
            assertEquals(null, it[1].pinUrl)
        }
    }
}
