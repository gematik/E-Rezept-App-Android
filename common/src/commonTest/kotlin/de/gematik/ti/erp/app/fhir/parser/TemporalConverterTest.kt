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

package de.gematik.ti.erp.app.fhir.parser

import de.gematik.ti.erp.app.navigation.fromNavigationString
import de.gematik.ti.erp.app.navigation.toNavigationString
import de.gematik.ti.erp.app.utils.FhirTemporal
import de.gematik.ti.erp.app.utils.toFhirTemporal
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.LocalTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toInstant
import org.junit.Test
import kotlin.test.assertEquals

class TemporalConverterTest {

    @Test
    fun `serialize FhirTemporal Instant correctly`() {
        val localDateTimeInstant: Instant = LocalDateTime(2023, 12, 25, 14, 30, 45).toInstant(TimeZone.UTC)
        val expectedValue = FhirTemporal.Instant(localDateTimeInstant)
        assertEquals(expectedValue, expectedValue.getActualValue())
    }

    @Test
    fun `serialize FhirTemporal LocalDateTime correctly`() {
        val localDateTime = LocalDateTime(2023, 12, 25, 14, 30, 45)
        val expectedValue = FhirTemporal.LocalDateTime(localDateTime)
        assertEquals(expectedValue, expectedValue.getActualValue())
    }

    @Test
    fun `serialize FhirTemporal LocalDate correctly`() {
        val localDate = LocalDate(2023, 12, 25)
        val expectedValue = FhirTemporal.LocalDate(localDate)
        assertEquals(expectedValue, expectedValue.getActualValue())
    }

    @Test
    fun `serialize FhirTemporal LocalTime correctly`() {
        val localTime = LocalTime(14, 30, 45)
        val expectedValue = FhirTemporal.LocalTime(localTime)
        assertEquals(expectedValue, expectedValue.getActualValue())
    }

    @Test
    fun `serialize FhirTemporal YearMonth correctly`() {
        val yearMonth = YearMonth(year = 2023, monthNumber = 12)
        val expectedValue = FhirTemporal.YearMonth(yearMonth)
        assertEquals(expectedValue, expectedValue.getActualValue())
    }

    @Test
    fun `serialize FhirTemporal Year correctly`() {
        val year = Year(year = 2023)
        val expectedValue = FhirTemporal.Year(year)
        assertEquals(expectedValue, expectedValue.getActualValue())
    }

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
            Instant.parse("2022-01-13T15:44:15.816+00:00"),
            ("2022-01-13T15:44:15.816Z".toFhirTemporal() as FhirTemporal.Instant).value
        )
        assertEquals("07.02.2015", "2015-02-07T11:28:17".toFhirTemporal().toFormattedDate())
        assertEquals("03.02.2015", "2015-02-03".toFhirTemporal().toFormattedDate())
    }

    private fun FhirTemporal.getActualValue(): FhirTemporal {
        val encodedString = this.toNavigationString()
        return fromNavigationString<FhirTemporal>(encodedString)
    }
}
