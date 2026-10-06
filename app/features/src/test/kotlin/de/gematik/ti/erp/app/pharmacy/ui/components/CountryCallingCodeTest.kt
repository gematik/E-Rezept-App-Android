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

package de.gematik.ti.erp.app.pharmacy.ui.components

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class CountryCallingCodeTest {

    @Test
    fun `getAvailableCountryCallingCodes puts DE, AT, CH first`() {
        val codes = getAvailableCountryCallingCodes()
        assertTrue(codes.isNotEmpty())
        assertEquals("DE", codes[0].countryCode)
        assertEquals(49, codes[0].callingCode)
        assertEquals("AT", codes[1].countryCode)
        assertEquals(43, codes[1].callingCode)
        assertEquals("CH", codes[2].countryCode)
        assertEquals(41, codes[2].callingCode)
    }

    @Test
    fun `parsePhoneToCallingCodeAndNationalNumber splits german phone number`() {
        val allCodes = getAvailableCountryCallingCodes()
        val (code, national) = parsePhoneToCallingCodeAndNationalNumber("+491701234567", allCodes)
        assertEquals(49, code.callingCode)
        assertEquals("1701234567", national)
    }

    @Test
    fun `parsePhoneToCallingCodeAndNationalNumber splits austrian phone number`() {
        val allCodes = getAvailableCountryCallingCodes()
        val (code, national) = parsePhoneToCallingCodeAndNationalNumber("+43660123456", allCodes)
        assertEquals(43, code.callingCode)
        assertEquals("660123456", national)
    }

    @Test
    fun `parsePhoneToCallingCodeAndNationalNumber handles only calling code`() {
        val allCodes = getAvailableCountryCallingCodes()
        val (code, national) = parsePhoneToCallingCodeAndNationalNumber("+49", allCodes)
        assertEquals(49, code.callingCode)
        assertEquals("", national)
    }

    @Test
    fun `parsePhoneToCallingCodeAndNationalNumber handles leading zero without plus`() {
        val allCodes = getAvailableCountryCallingCodes()
        val (code, national) = parsePhoneToCallingCodeAndNationalNumber("01701234567", allCodes)
        assertEquals(49, code.callingCode)
        assertEquals("1701234567", national)
    }

    @Test
    fun `parsePhoneToCallingCodeAndNationalNumber handles empty string`() {
        val allCodes = getAvailableCountryCallingCodes()
        val (code, national) = parsePhoneToCallingCodeAndNationalNumber("", allCodes, "AT")
        assertEquals(43, code.callingCode)
        assertEquals("", national)
    }

    @Test
    fun `parsePhoneToCallingCodeAndNationalNumber uses defaultCountryCode for local number`() {
        val allCodes = getAvailableCountryCallingCodes()
        val (code, national) = parsePhoneToCallingCodeAndNationalNumber("0660123456", allCodes, "AT")
        assertEquals(43, code.callingCode)
        assertEquals("660123456", national)
    }

    @Test
    fun `parsePhoneToCallingCodeAndNationalNumber handles swiss calling code only`() {
        val allCodes = getAvailableCountryCallingCodes()
        val (code, national) = parsePhoneToCallingCodeAndNationalNumber("+41", allCodes, "CH")
        assertEquals(41, code.callingCode)
        assertEquals("", national)
    }
}
