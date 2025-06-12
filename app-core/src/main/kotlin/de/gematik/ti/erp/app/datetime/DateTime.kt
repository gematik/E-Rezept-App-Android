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

package de.gematik.ti.erp.app.datetime

import de.gematik.ti.erp.app.fhir.parser.Year
import de.gematik.ti.erp.app.fhir.parser.YearMonth
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toJavaLocalDate
import kotlinx.datetime.toJavaLocalDateTime
import kotlinx.datetime.toJavaLocalTime
import kotlinx.datetime.toLocalDateTime
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

val dateTimeShortFormatter: DateTimeFormatter = DateTimeFormatter.ofLocalizedDate(FormatStyle.SHORT)

val dateTimeHHMMFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("HH:mm")

fun dateTimeShortText(instant: Instant): String =
    instant.toLocalDateTime(TimeZone.currentSystemDefault())
        .toJavaLocalDateTime()
        .format(dateTimeShortFormatter)

val dateTimeMediumFormatter: DateTimeFormatter = DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM)

fun dateTimeMediumText(instant: Instant, zone: TimeZone = TimeZone.currentSystemDefault()): String =
    instant.toLocalDateTime(zone)
        .toJavaLocalDateTime()
        .format(dateTimeMediumFormatter)

private val YearMonthPattern = DateTimeFormatter.ofPattern("MMMM yyyy")
private val MonthPattern = DateTimeFormatter.ofPattern("yyyy")

fun temporalText(temporal: de.gematik.ti.erp.app.utils.FhirTemporal, timeZone: TimeZone = TimeZone.UTC): String =
    when (temporal) {
        is de.gematik.ti.erp.app.utils.FhirTemporal.Instant -> temporal.value.toLocalDateTime(timeZone).toJavaLocalDateTime()
            .format(DateTimeFormatter.ofLocalizedDateTime(FormatStyle.MEDIUM))

        is de.gematik.ti.erp.app.utils.FhirTemporal.LocalDate -> temporal.value.toJavaLocalDate()
            .format(DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM))

        is de.gematik.ti.erp.app.utils.FhirTemporal.LocalDateTime -> temporal.value.toJavaLocalDateTime()
            .format(DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM))

        is de.gematik.ti.erp.app.utils.FhirTemporal.LocalTime -> temporal.value.toJavaLocalTime()
            .format(DateTimeFormatter.ofLocalizedTime(FormatStyle.MEDIUM))

        is de.gematik.ti.erp.app.utils.FhirTemporal.Year -> temporal.value.toJavaYear().format(MonthPattern)

        is de.gematik.ti.erp.app.utils.FhirTemporal.YearMonth -> temporal.value.toJavaYearMonth().format(YearMonthPattern)

        else -> "n.a."
    }

private fun YearMonth.toJavaYearMonth(): java.time.YearMonth = java.time.YearMonth.of(this.year, this.monthNumber)

private fun Year.toJavaYear(): java.time.Year = java.time.Year.of(this.year)

object DateTimeUtils {
    val dateFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy")
    val timeFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("HH:mm")
}
