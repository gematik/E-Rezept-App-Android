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

import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.Year
import java.time.YearMonth
import java.time.ZoneId
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.time.temporal.TemporalAccessor

val dateTimeShortFormatter: DateTimeFormatter = DateTimeFormatter.ofLocalizedDate(FormatStyle.SHORT)

fun dateTimeShortText(instant: Instant): String =
    LocalDateTime
        .ofInstant(instant, ZoneId.systemDefault())
        .format(dateTimeShortFormatter)

val dateTimeMediumFormatter: DateTimeFormatter = DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM)

fun dateTimeMediumText(instant: Instant, zoneId: ZoneId = ZoneOffset.UTC): String =
    LocalDateTime
        .ofInstant(instant, zoneId)
        .format(dateTimeMediumFormatter)

private val YearMonthPattern = DateTimeFormatter.ofPattern("MMMM yyyy")
private val MonthPattern = DateTimeFormatter.ofPattern("yyyy")

fun temporalText(temporalAccessor: TemporalAccessor, zoneId: ZoneId = ZoneOffset.UTC): String =
    when (temporalAccessor) {
        is Instant ->
            LocalDateTime
                .ofInstant(temporalAccessor, zoneId)
                .format(DateTimeFormatter.ofLocalizedDateTime(FormatStyle.MEDIUM))
        is LocalDate ->
            temporalAccessor
                .format(DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM))
        is YearMonth ->
            temporalAccessor
                .format(YearMonthPattern)
        is Year ->
            temporalAccessor
                .format(MonthPattern)
        is LocalTime ->
            temporalAccessor
                .format(DateTimeFormatter.ofLocalizedTime(FormatStyle.MEDIUM))
        else -> "n.a."
    }
