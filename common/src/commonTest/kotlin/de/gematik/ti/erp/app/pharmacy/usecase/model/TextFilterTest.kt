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

package de.gematik.ti.erp.app.pharmacy.usecase.model

import de.gematik.ti.erp.app.pharmacy.usecase.model.TextFilter.Companion.toSanitizedSearchText
import de.gematik.ti.erp.app.pharmacy.usecase.model.TextFilter.Companion.toTextFilter
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class TextFilterTest {

    @Test
    fun `toTextFilter - should split by space and filter empty strings`() {
        val input = "  Apotheke   am   Dom  "
        val expected = TextFilter(value = listOf("Apotheke", "am", "Dom"))
        assertEquals(expected, input.toTextFilter())
    }

    @Test
    fun `toTextFilter - should return empty list for empty string`() {
        val input = ""
        val expected = TextFilter(value = emptyList())
        assertEquals(expected, input.toTextFilter())
    }

    @Test
    fun `toTextFilter - should return empty list for blank string`() {
        val input = "   "
        val expected = TextFilter(value = emptyList())
        assertEquals(expected, input.toTextFilter())
    }

    @Test
    fun `toSanitizedSearchText - should join with comma and trim whitespace`() {
        val filter = TextFilter(value = listOf(" Apotheke", "am ", " Dom "))
        // joinToString() results in " Apotheke, am ,  Dom "
        // .trim() results in "Apotheke, am ,  Dom"
        val expected = "Apotheke, am ,  Dom"
        assertEquals(expected, filter.toSanitizedSearchText())
    }

    @Test
    fun `toSanitizedSearchText - should handle dots and quotes correctly`() {
        // currently sanitize() is NOT used in toSanitizedSearchText according to the code
        val filter = TextFilter(value = listOf("Apotheke.", "Dr. 'Test'"))
        val expected = "Apotheke., Dr. 'Test'"
        assertEquals(expected, filter.toSanitizedSearchText())
    }

    @Test
    fun `toSanitizedSearchText - should return null for empty list`() {
        val filter = TextFilter(value = emptyList())
        assertNull(filter.toSanitizedSearchText())
    }

    @Test
    fun `toSanitizedSearchText - should return null if empty string`() {
        val filter = TextFilter(value = listOf(" "))
        assertNull(filter.toSanitizedSearchText())
    }

    @Test
    fun `toSanitizedSearchText - should handle single word with whitespace`() {
        val filter = TextFilter(value = listOf("  Apotheke  "))
        assertEquals("Apotheke", filter.toSanitizedSearchText())
    }
}
