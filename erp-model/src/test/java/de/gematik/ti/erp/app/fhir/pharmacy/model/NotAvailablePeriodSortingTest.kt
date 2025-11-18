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
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.LocalTime
import kotlin.test.Test
import kotlin.test.assertEquals

class NotAvailablePeriodSortingTest {

    private fun meta(
        description: String,
        start: FhirTemporal? = null,
        end: FhirTemporal? = null
    ): NotAvailablePeriodMetadata = NotAvailablePeriodMetadata(
        erpModel = NotAvailablePeriodErpModel(
            description = description,
            period = FhirPharmacyErpModelPeriod(start = start, end = end)
        ),
        hasOverlap = false,
        isActive = false,
        isInPast = false
    )

    @Test
    fun `sorts heterogenous temporal types with defined precedence`() {
        val unsorted = listOf(
            meta("LocalTime 15:00", FhirTemporal.LocalTime(LocalTime(15, 0, 0))),
            meta("Year 2024", FhirTemporal.Year(de.gematik.ti.erp.app.fhir.temporal.Year(2024))),
            meta("Null B", null),
            meta("Date 2025-01-10", FhirTemporal.LocalDate(LocalDate(2025, 1, 10))),
            meta("Instant early", FhirTemporal.Instant(Instant.parse("2025-01-09T05:00:00Z"))),
            meta("LocalTime 08:00", FhirTemporal.LocalTime(LocalTime(8, 0, 0))),
            meta("YearMonth 2024-06", FhirTemporal.YearMonth(de.gematik.ti.erp.app.fhir.temporal.YearMonth(2024, 6))),
            meta("Null A", null),
            meta("DateTime 2025-01-09T10", FhirTemporal.LocalDateTime(LocalDateTime(2025, 1, 9, 10, 0, 0)))
        )

        val sorted = unsorted.sortedByPeriodStart().map { it.erpModel.description }

        val expectedOrder = listOf(
            "Year 2024", // Year -> category 0 earliest
            "YearMonth 2024-06", // YearMonth
            "Instant early", // Instant
            "DateTime 2025-01-09T10", // LocalDateTime same day as instant but later time
            "Date 2025-01-10", // LocalDate after previous day entries
            "LocalTime 08:00", // LocalTime (category 1)
            "LocalTime 15:00", // LocalTime (category 1)
            "Null A", // Null (category 2) alphabetical
            "Null B" // Null (category 2)
        )

        assertEquals(expectedOrder, sorted)
    }

    @Test
    fun `ties on same epoch resolved by description alphabetically`() {
        val date1 = meta("Date tie Z", FhirTemporal.LocalDate(LocalDate(2025, 2, 1)))
        val date2 = meta("Date tie A", FhirTemporal.LocalDate(LocalDate(2025, 2, 1)))
        val sorted = listOf(date1, date2).shuffled().sortedByPeriodStart().map { it.erpModel.description }
        assertEquals(listOf("Date tie A", "Date tie Z"), sorted)
    }

    @Test
    fun `null starts remain last even after multiple sorts (stability)`() {
        val withNulls = listOf(
            meta("A", FhirTemporal.LocalDate(LocalDate(2025, 1, 1))),
            meta("Null 1", null),
            meta("Null 2", null),
            meta("B", FhirTemporal.LocalDate(LocalDate(2025, 1, 2)))
        )
        val first = withNulls.sortedByPeriodStart()
        val second = first.sortedByPeriodStart() // re-sort
        assertEquals(first.map { it.erpModel.description }, second.map { it.erpModel.description })
        // explicit end check
        assertEquals(listOf("Null 1", "Null 2"), second.takeLast(2).map { it.erpModel.description })
    }
}
