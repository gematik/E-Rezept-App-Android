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

import android.os.Build
import de.gematik.ti.erp.app.fhir.parser.FhirTemporal
import de.gematik.ti.erp.app.fhir.parser.toJavaYear
import de.gematik.ti.erp.app.fhir.parser.toJavaYearMonth
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toJavaLocalDate
import kotlinx.datetime.toJavaLocalDateTime
import kotlinx.datetime.toJavaLocalTime
import kotlinx.datetime.toLocalDateTime
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.Year
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

val dateTimeShortFormatter: DateTimeFormatter = DateTimeFormatter.ofLocalizedDate(FormatStyle.SHORT)

fun dateTimeShortText(instant: Instant): String =
    instant.toLocalDateTime(TimeZone.currentSystemDefault())
        .toJavaLocalDateTime()
        .format(dateTimeShortFormatter)

val dateTimeMediumFormatter: DateTimeFormatter = DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM)

fun dateTimeMediumText(instant: Instant, zone: TimeZone = TimeZone.currentSystemDefault()): String =
    instant.toLocalDateTime(zone)
        .toJavaLocalDateTime()
        .format(dateTimeMediumFormatter)

fun dateIsoText(instant: Instant, zone: TimeZone = TimeZone.currentSystemDefault()): String =
    instant.toLocalDateTime(zone)
        .toJavaLocalDateTime()
        .format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))

private val YearMonthPattern = DateTimeFormatter.ofPattern("MMMM yyyy")
private val MonthPattern = DateTimeFormatter.ofPattern("yyyy")

fun temporalText(temporal: FhirTemporal, timeZone: TimeZone = TimeZone.UTC): String =
    when (temporal) {
        is FhirTemporal.Instant -> temporal.value.toLocalDateTime(timeZone).toJavaLocalDateTime()
            .format(DateTimeFormatter.ofLocalizedDateTime(FormatStyle.MEDIUM))

        is FhirTemporal.LocalDate -> temporal.value.toJavaLocalDate()
            .format(DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM))

        is FhirTemporal.LocalDateTime -> temporal.value.toJavaLocalDateTime()
            .format(DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM))

        is FhirTemporal.LocalTime -> temporal.value.toJavaLocalTime()
            .format(DateTimeFormatter.ofLocalizedTime(FormatStyle.MEDIUM))

        is FhirTemporal.Year -> if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            temporal.value.toJavaYear().format(MonthPattern)
        } else {
            error("VERSION.SDK_INT < O")
        }

        is FhirTemporal.YearMonth -> if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            temporal.value.toJavaYearMonth().format(YearMonthPattern)
        } else {
            error("VERSION.SDK_INT < O")
        }

        else -> "n.a."
    }
