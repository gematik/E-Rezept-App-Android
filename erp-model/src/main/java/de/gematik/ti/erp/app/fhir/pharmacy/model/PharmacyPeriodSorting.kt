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
import io.github.aakira.napier.Napier
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atStartOfDayIn
import kotlinx.datetime.atTime
import kotlinx.datetime.toInstant

/**
 * Shared stable sorting for period-based metadata (special closing times, special opening times)
 * by their period.start temporal value.
 *
 * Ordering rules:
 *  1. Date/DateTime based temporals (Instant, LocalDateTime, LocalDate, YearMonth, Year) ordered by their instant millis
 *  2. Time-only temporals (LocalTime) ordered after all date/date-time values, relative among themselves by a fixed anchor date
 *  3. Null start values ordered last (stable secondary ordering by description to avoid flakiness)
 */

private const val CATEGORY_DATETIME = 0
private const val CATEGORY_LOCALTIME = 1
private const val CATEGORY_NULL = 2

private const val ANCHOR_YEAR = 1970
private const val ANCHOR_MONTH = 1
private const val ANCHOR_DAY = 1
private val LOCALTIME_ANCHOR_DATE = LocalDate(ANCHOR_YEAR, ANCHOR_MONTH, ANCHOR_DAY)

// Tag for logging
private const val SORT_TAG = "PeriodMetadataSort"

private data class SortKey(
    val category: Int,
    val epochMillis: Long,
    val descriptionFallback: String
)

/**
 * Builds a deterministic [SortKey] out of a nullable [FhirTemporal].
 *
 * Mapping rules:
 *  - Instant, LocalDateTime, LocalDate, YearMonth, Year -> CATEGORY_DATETIME (0) ordered by their epoch start
 *    (YearMonth & Year anchored to first day 00:00 of their scope).
 *  - LocalTime -> CATEGORY_LOCALTIME (1) anchored to the fixed date 1970-01-01 to keep ordering stable across days.
 *  - null or failure -> CATEGORY_NULL (2) and pushed to the end via Long.MAX_VALUE epoch.
 *
 * Failure handling: Any exception during conversion is logged (Napier) and a fallback
 * key placing the element at the end (null category) is returned.
 *
 * @param timeZone Time zone used for converting date based temporals to an [kotlinx.datetime.Instant].
 * @return A [SortKey] used by the comparator for multi-criteria ordering.
 */
@OptIn(kotlin.time.ExperimentalTime::class)
private fun FhirTemporal?.toSortKey(timeZone: TimeZone): SortKey = try {
    when (this) {
        is FhirTemporal.Instant -> SortKey(CATEGORY_DATETIME, value.toEpochMilliseconds(), "")
        is FhirTemporal.LocalDateTime -> SortKey(CATEGORY_DATETIME, value.toInstant(timeZone).toEpochMilliseconds(), "")
        is FhirTemporal.LocalDate -> SortKey(CATEGORY_DATETIME, value.atStartOfDayIn(timeZone).toEpochMilliseconds(), "")
        is FhirTemporal.LocalTime -> {
            val epoch = LOCALTIME_ANCHOR_DATE.atTime(value).toInstant(timeZone).toEpochMilliseconds()
            SortKey(CATEGORY_LOCALTIME, epoch, "")
        }

        is FhirTemporal.YearMonth -> {
            val firstDay = LocalDate(this.value.year, this.value.monthNumber, 1)
            SortKey(CATEGORY_DATETIME, firstDay.atStartOfDayIn(timeZone).toEpochMilliseconds(), "")
        }

        is FhirTemporal.Year -> {
            val firstDay = LocalDate(this.value.year, 1, 1)
            SortKey(CATEGORY_DATETIME, firstDay.atStartOfDayIn(timeZone).toEpochMilliseconds(), "")
        }

        null -> SortKey(CATEGORY_NULL, Long.MAX_VALUE, "")
    }
} catch (e: Exception) {
    Napier.e(e, tag = SORT_TAG) { "Failed to derive sort key for temporal '$this'. Using fallback. StackTrace: ${e.stackTraceToString()}" }
    SortKey(CATEGORY_NULL, Long.MAX_VALUE, "")
}

/**
 * Utility converting a difference in [Long] to comparator tri-state (-1, 0, 1).
 */
private fun Long.sign(): Int = when {
    this < 0 -> -1
    this > 0 -> 1
    else -> 0
}

/**
 * Creates a comparator for any metadata type that has a period start and description.
 *
 * Ordering dimensions (in priority order):
 *  1. Temporal category (date/dateTime < localTime < null/invalid)
 *  2. Epoch milliseconds derived from normalized temporal representation
 *  3. Description (alphabetical) as deterministic tiebreaker
 */
private fun <T> periodStartComparator(
    periodStart: (T) -> FhirTemporal?,
    description: (T) -> String?
): Comparator<T> = Comparator { o1, o2 ->
    val tz = TimeZone.currentSystemDefault()
    val k1 = periodStart(o1).toSortKey(tz)
    val k2 = periodStart(o2).toSortKey(tz)

    // Primary: category
    if (k1.category != k2.category) return@Comparator k1.category - k2.category

    // Secondary: epoch
    if (k1.epochMillis != k2.epochMillis) return@Comparator (k1.epochMillis - k2.epochMillis).sign()

    // Tertiary: description to keep deterministic
    val d1 = description(o1).orEmpty()
    val d2 = description(o2).orEmpty()
    d1.compareTo(d2)
}

/**
 * Returns a new list sorted by the start temporal of each not-available period.
 * Uses description as tertiary tiebreaker for deterministic ordering.
 * The original list remains unmodified.
 */
@JvmName("sortedByPeriodStartNotAvailable")
fun List<NotAvailablePeriodMetadata>.sortedByPeriodStart(): List<NotAvailablePeriodMetadata> =
    this.sortedWith(
        periodStartComparator(
            periodStart = { it.erpModel.period.start },
            description = { it.erpModel.description }
        )
    )

/**
 * Returns a new list sorted by the start temporal of each special opening time period.
 * Uses description (qualifier) as tertiary tiebreaker for deterministic ordering.
 * The original list remains unmodified.
 */
@JvmName("sortedByPeriodStartSpecialOpening")
fun List<SpecialOpeningTimeMetadata>.sortedByPeriodStart(): List<SpecialOpeningTimeMetadata> =
    this.sortedWith(
        periodStartComparator(
            periodStart = { it.erpModel.period?.start },
            description = { it.erpModel.description }
        )
    )
