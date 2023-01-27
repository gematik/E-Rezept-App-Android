/*
 * Copyright (c) 2023 gematik GmbH
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

package de.gematik.ti.erp.app.utils

import de.gematik.ti.erp.app.fhir.parser.asTemporalAccessor
import org.junit.Test
import java.util.Locale
import kotlin.test.assertEquals

class DateTimeTest {
    @Test
    fun `format temporal accessor`() {
        Locale.setDefault(Locale.GERMAN)

        assertEquals("07.02.2015, 11:28:17", temporalText("2015-02-07T13:28:17+02:00".asTemporalAccessor()!!))
        assertEquals("07.02.2015", temporalText("2015-02-07".asTemporalAccessor()!!))
        assertEquals("Februar 2015", temporalText("2015-02".asTemporalAccessor()!!))
        assertEquals("2015", temporalText("2015".asTemporalAccessor()!!))
        assertEquals("11:28:17", temporalText("11:28:17".asTemporalAccessor()!!))
    }
}
