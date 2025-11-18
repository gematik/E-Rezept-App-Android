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

package de.gematik.ti.erp.app.fhir.pharmacy.model

import de.gematik.ti.erp.app.fhir.temporal.FhirTemporal
import de.gematik.ti.erp.app.fhir.temporal.FhirTemporalSerializationType.FhirTemporalLocalDateTime
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.LocalTime
import org.junit.Test
import kotlin.test.assertEquals
import kotlinx.datetime.Instant as KInstant

class NotAvailablePeriodErpModelTest {

    private val availableTime = OpeningHoursErpModel(
        openingTime = mapOf(
            DayOfWeek.MONDAY to listOf(OpeningTimeErpModel(LocalTime(9, 0), LocalTime(18, 0))),
            DayOfWeek.TUESDAY to listOf(OpeningTimeErpModel(LocalTime(9, 0), LocalTime(18, 0))),
            DayOfWeek.WEDNESDAY to listOf(OpeningTimeErpModel(LocalTime(9, 0), LocalTime(18, 0))),
            DayOfWeek.THURSDAY to listOf(OpeningTimeErpModel(LocalTime(9, 0), LocalTime(18, 0))),
            DayOfWeek.FRIDAY to listOf(OpeningTimeErpModel(LocalTime(9, 0), LocalTime(18, 0)))
        )
    )

    @Test
    fun `not available period is only date, so cannot check overlap`() = runTest {
        val notAvailablePeriod = NotAvailablePeriodErpModel(
            description = "Holiday",
            period = FhirPharmacyErpModelPeriod(
                start = FhirTemporal.LocalDate(LocalDate(2025, 10, 6)), // Monday
                end = FhirTemporal.LocalDate(LocalDate(2025, 10, 8)) // Wednesday
            )
        )

        val result = notAvailablePeriod.overlapsWithAvailableTime(availableTime)
        assertEquals(true, result)
    }

    @Test
    fun `not available period on weekend (no overlap)`() = runTest {
        val notAvailablePeriod = NotAvailablePeriodErpModel(
            description = "Weekend closure",
            period = FhirPharmacyErpModelPeriod(
                start = FhirTemporal.LocalDate(LocalDate(2025, 10, 11)), // Saturday
                end = FhirTemporal.LocalDate(LocalDate(2025, 10, 12)) // Sunday
            )
        )

        val result = notAvailablePeriod.overlapsWithAvailableTime(availableTime)
        assertEquals(false, result)
    }

    @Test
    fun `overlapsWithAvailableTime should handle all-day opening times`() = runTest {
        // Create mock available time - pharmacy open all day Monday
        val availableTime = OpeningHoursErpModel(
            openingTime = mapOf(
                DayOfWeek.MONDAY to listOf(OpeningTimeErpModel(null, null)) // All day open
            )
        )

        val notAvailablePeriod = NotAvailablePeriodErpModel(
            description = "Emergency closure",
            period = FhirPharmacyErpModelPeriod(
                start = FhirTemporal.LocalDate(LocalDate(2025, 10, 6)), // Monday
                end = FhirTemporal.LocalDate(LocalDate(2025, 10, 6)) // Monday
            )
        )

        val result = notAvailablePeriod.overlapsWithAvailableTime(availableTime)
        assertEquals(true, result)
    }

    @Test
    fun `overlapsWithAvailableTime should handle instant temporal types`() = runTest {
        val availableTime = OpeningHoursErpModel(
            openingTime = mapOf(
                DayOfWeek.TUESDAY to listOf(OpeningTimeErpModel(LocalTime(8, 0), LocalTime(20, 0)))
            )
        )

        val notAvailablePeriod = NotAvailablePeriodErpModel(
            description = "System maintenance",
            period = FhirPharmacyErpModelPeriod(
                start = FhirTemporal.Instant(KInstant.parse("2025-10-07T10:00:00Z")), // Tuesday 10:00
                end = FhirTemporal.Instant(KInstant.parse("2025-10-07T15:00:00Z")) // Tuesday 15:00
            )
        )

        val result = notAvailablePeriod.overlapsWithAvailableTime(availableTime)
        assertEquals(true, result)
    }

    @Test
    fun `overlapsWithAvailableTime should handle datetime temporal types`() = runTest {
        val availableTime = OpeningHoursErpModel(
            openingTime = mapOf(
                DayOfWeek.WEDNESDAY to listOf(OpeningTimeErpModel(LocalTime(9, 0), LocalTime(17, 0)))
            )
        )

        val notAvailablePeriod = NotAvailablePeriodErpModel(
            description = "Staff meeting",
            period = FhirPharmacyErpModelPeriod(
                start = FhirTemporal.LocalDateTime(
                    LocalDateTime(2025, 10, 8, 12, 0), // Wednesday 12:00
                    FhirTemporalLocalDateTime
                ),
                end = FhirTemporal.LocalDateTime(
                    LocalDateTime(2025, 10, 8, 14, 0), // Wednesday 14:00
                    FhirTemporalLocalDateTime
                )
            )
        )

        val result = notAvailablePeriod.overlapsWithAvailableTime(availableTime)
        assertEquals(true, result)
    }

    @Test
    fun `isInPast should return true for past periods`() = runTest {
        // Test case 1: FhirPharmacyErpModelPeriod that started yesterday
        val pastPeriod = NotAvailablePeriodErpModel(
            description = "Past maintenance",
            period = FhirPharmacyErpModelPeriod(
                start = FhirTemporal.LocalDate(LocalDate(2025, 10, 1)), // Past date
                end = FhirTemporal.LocalDate(LocalDate(2025, 10, 1))
            )
        )

        val result1 = pastPeriod.isInPast()
        assertEquals(true, result1)

        // Test case 2: FhirPharmacyErpModelPeriod starting today (should be considered as started)
        val todayPeriod = NotAvailablePeriodErpModel(
            description = "Today's closure",
            period = FhirPharmacyErpModelPeriod(
                start = FhirTemporal.LocalDate(LocalDate(2025, 10, 2)), // Today
                end = FhirTemporal.LocalDate(LocalDate(2025, 10, 2))
            )
        )

        val result2 = todayPeriod.isInPast()
        assertEquals(true, result2)

        // Test case 3: Future period
        val futurePeriod = NotAvailablePeriodErpModel(
            description = "Future maintenance",
            period = FhirPharmacyErpModelPeriod(
                start = FhirTemporal.LocalDate(LocalDate(2025, 12, 25)), // Future date
                end = FhirTemporal.LocalDate(LocalDate(2025, 12, 26))
            )
        )

        val result3 = futurePeriod.isInPast()
        assertEquals(false, result3)
    }

    @Test
    fun `isInPast should handle instant temporal types`() = runTest {
        val pastInstantPeriod = NotAvailablePeriodErpModel(
            description = "Past instant closure",
            period = FhirPharmacyErpModelPeriod(
                start = FhirTemporal.Instant(KInstant.parse("2025-09-30T10:00:00Z")), // Past instant
                end = FhirTemporal.Instant(KInstant.parse("2025-09-30T15:00:00Z"))
            )
        )

        val result = pastInstantPeriod.isInPast()
        assertEquals(true, result)
    }

    @Test
    fun `isInPast should return false for null end period`() = runTest {
        val nullEndPeriod = NotAvailablePeriodErpModel(
            description = "No end time",
            period = FhirPharmacyErpModelPeriod(
                start = FhirTemporal.LocalDate(LocalDate(2025, 10, 5)),
                end = null
            )
        )

        val result = nullEndPeriod.isInPast()
        assertEquals(false, result)
    }

    @Test
    fun `overlapsWithAvailableTime should handle YearMonth and Year temporal types`() = runTest {
        val availableTime = OpeningHoursErpModel(
            openingTime = mapOf(
                DayOfWeek.MONDAY to listOf(OpeningTimeErpModel(LocalTime(9, 0), LocalTime(17, 0)))
            )
        )

        // Test YearMonth temporal type
        val yearMonthPeriod = NotAvailablePeriodErpModel(
            description = "Monthly closure",
            period = FhirPharmacyErpModelPeriod(
                start = FhirTemporal.YearMonth(
                    de.gematik.ti.erp.app.fhir.temporal.YearMonth(2025, 10)
                ),
                end = FhirTemporal.YearMonth(
                    de.gematik.ti.erp.app.fhir.temporal.YearMonth(2025, 10)
                )
            )
        )

        val result1 = yearMonthPeriod.overlapsWithAvailableTime(availableTime)
        assertEquals(true, result1)

        // Test Year temporal type
        val yearPeriod = NotAvailablePeriodErpModel(
            description = "Yearly closure",
            period = FhirPharmacyErpModelPeriod(
                start = FhirTemporal.Year(
                    de.gematik.ti.erp.app.fhir.temporal.Year(2025)
                ),
                end = FhirTemporal.Year(
                    de.gematik.ti.erp.app.fhir.temporal.Year(2025)
                )
            )
        )

        val result2 = yearPeriod.overlapsWithAvailableTime(availableTime)
        assertEquals(true, result2)
    }
}
