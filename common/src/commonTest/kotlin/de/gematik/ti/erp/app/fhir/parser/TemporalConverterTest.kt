/*
 * Copyright (c) 2024 gematik GmbH
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

package de.gematik.ti.erp.app.fhir.parser

import de.gematik.ti.erp.app.utils.FhirTemporal
import de.gematik.ti.erp.app.utils.toFhirTemporal
import kotlin.test.Test
import kotlin.test.assertEquals

class TemporalConverterTest {

    @Test
    fun `convert dates to string`() {
        assertEquals("2022-01-13T15:44:15.816Z", "2022-01-13T15:44:15.816+00:00".toFhirTemporal().formattedString())
        assertEquals("2022-01-13T15:44:15.816Z", "2022-01-13T15:44:15.816Z".toFhirTemporal().formattedString())
        assertEquals("2022-01-13T15:44:00Z", "2022-01-13T15:44:00+00:00".toFhirTemporal().formattedString())
        assertEquals("2015-02-07T11:28:17Z", "2015-02-07T13:28:17+02:00".toFhirTemporal().formattedString())
        assertEquals("2015-02-07T15:28:17Z", "2015-02-07T13:28:17-02:00".toFhirTemporal().formattedString())

        assertEquals("2015-02-07T11:28:17", "2015-02-07T11:28:17".toFhirTemporal().formattedString())
        assertEquals("2015-02-07T11:28", "2015-02-07T11:28:00".toFhirTemporal().formattedString())
        assertEquals("2015-02-07T11:28:00.123", "2015-02-07T11:28:00.123".toFhirTemporal().formattedString())

        assertEquals("2015-02-03", "2015-02-03".toFhirTemporal().formattedString())
        assertEquals("2015-02", "2015-02".toFhirTemporal().formattedString())
        assertEquals("2015", "2015".toFhirTemporal().formattedString())

        assertEquals("13:28:05", "13:28:05".toFhirTemporal().formattedString())
        assertEquals("13:00", "13:00:00".toFhirTemporal().formattedString())
        assertEquals("13:28", "13:28:00".toFhirTemporal().formattedString())

        assertEquals(
            kotlinx.datetime.Instant.parse("2022-01-13T15:44:15.816+00:00"),
            ("2022-01-13T15:44:15.816Z".toFhirTemporal() as FhirTemporal.Instant).value
        )
    }
}
