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

package de.gematik.ti.erp.app.utils

import de.gematik.ti.erp.app.fhir.parser.Year
import de.gematik.ti.erp.app.fhir.parser.YearMonth
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.LocalTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atStartOfDayIn
import kotlinx.datetime.toInstant
import kotlinx.datetime.toJavaLocalDateTime
import kotlinx.datetime.toLocalDateTime
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.contentOrNull
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

/**
 * The Fhir documentation mentions the following formats:
 *
 * instant YYYY-MM-DDThh:mm:ss.sss+zz:zz
 * datetime YYYY, YYYY-MM, YYYY-MM-DD or YYYY-MM-DDThh:mm:ss+zz:zz
 * date YYYY, YYYY-MM, or YYYY-MM-DD
 * time hh:mm:ss
 *
 */

// keep the regex - but we can't be sure about all the different patterns
// val FhirInstantRegex = """(\d\d\d\d-\d\d-\d\d)T(\d\d:\d\d:\d\d)(\.\d\d\d)?(([+-]\d\d:\d\d)|Z)""".toRegex()
// val FhirLocalDateTimeRegex = """(\d\d\d\d-\d\d-\d\d)T(\d\d:\d\d(:\d\d)?)(\.\d\d\d)?""".toRegex()
// val FhirLocalDateRegex = """(\d\d\d\d-\d\d-\d\d)""".toRegex()
val FhirYearMonthRegex = """(?<year>\d\d\d\d)-(?<month>\d\d)""".toRegex()
val FhirYearRegex = """(?<year>\d\d\d\d)""".toRegex()
// val FhirLocalTimeRegex = """(\d\d:\d\d(:\d\d)?)""".toRegex()

sealed interface FhirTemporal {
    @JvmInline
    value class Instant(val value: kotlinx.datetime.Instant) : FhirTemporal

    @JvmInline
    value class LocalDateTime(val value: kotlinx.datetime.LocalDateTime) : FhirTemporal

    @JvmInline
    value class LocalDate(val value: kotlinx.datetime.LocalDate) : FhirTemporal

    @JvmInline
    value class YearMonth(val value: de.gematik.ti.erp.app.fhir.parser.YearMonth) : FhirTemporal

    @JvmInline
    value class Year(val value: de.gematik.ti.erp.app.fhir.parser.Year) : FhirTemporal

    @JvmInline
    value class LocalTime(val value: kotlinx.datetime.LocalTime) : FhirTemporal

    fun formattedString(): String =
        when (this) {
            is Instant -> this.value.toString()
            is LocalDate -> this.value.toString()
            is LocalDateTime -> this.value.toString()
            is LocalTime -> this.value.toString()
            is Year -> this.value.toString()
            is YearMonth -> this.value.toString()
        }

    fun toInstant(timeZone: TimeZone = TimeZone.currentSystemDefault()): kotlinx.datetime.Instant =
        when (this) {
            is Instant -> this.value
            is LocalDate -> this.value.atStartOfDayIn(timeZone)
            is LocalDateTime -> this.value.toInstant(timeZone)
            is LocalTime, is Year, is YearMonth -> error("invalid format")
        }

    fun toFormattedDate(): String? = this.toInstant(TimeZone.currentSystemDefault())
        .toFormattedDate()
}

fun Instant.asFhirTemporal(): FhirTemporal.Instant {
    val desiredFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
    val updatedInstant = toLocalDateTime(TimeZone.currentSystemDefault())
        .toJavaLocalDateTime().format(desiredFormatter)
        .toInstant()
    return FhirTemporal.Instant(updatedInstant)
}

fun Instant.toFormattedDateTime(): String? = this.toLocalDateTime(TimeZone.currentSystemDefault())
    .toJavaLocalDateTime().format(DateTimeFormatter.ofLocalizedDateTime(FormatStyle.SHORT, FormatStyle.SHORT))
fun Instant.toStartOfDayInUTC(): Instant {
    val currentLocalDateTime = this.toLocalDateTime(TimeZone.currentSystemDefault())
    return currentLocalDateTime.date.atStartOfDayIn(TimeZone.UTC)
}
fun Instant.toFormattedDate(): String? = this.toLocalDateTime(TimeZone.currentSystemDefault())
    .toJavaLocalDateTime().format(DateTimeFormatter.ofLocalizedDateTime(FormatStyle.SHORT))
fun LocalDateTime.asFhirTemporal() = FhirTemporal.LocalDateTime(this)
fun LocalDate.asFhirTemporal() = FhirTemporal.LocalDate(this)
fun YearMonth.asFhirTemporal() = FhirTemporal.YearMonth(this)
fun Year.asFhirTemporal() = FhirTemporal.Year(this)
fun LocalTime.asFhirTemporal() = FhirTemporal.LocalTime(this)

@Suppress("ReturnCount")
fun String.toFhirTemporal(): FhirTemporal {
    // going from the most specific to the least

    try {
        return FhirTemporal.Instant(Instant.parse(this))
    } catch (_: IllegalArgumentException) {
    }
    try {
        return FhirTemporal.LocalDateTime(LocalDateTime.parse(this))
    } catch (_: IllegalArgumentException) {
    }
    try {
        return FhirTemporal.LocalDate(LocalDate.parse(this))
    } catch (_: IllegalArgumentException) {
    }
    try {
        return FhirTemporal.YearMonth(YearMonth.parse(this))
    } catch (_: IllegalArgumentException) {
    }
    try {
        return FhirTemporal.Year(Year.parse(this))
    } catch (_: IllegalArgumentException) {
    }
    try {
        return FhirTemporal.LocalTime(LocalTime.parse(this))
    } catch (_: IllegalArgumentException) {
    }

    error("Couldn't parse `$this`")
}

fun JsonPrimitive.toFhirTemporal() =
    this.contentOrNull?.toFhirTemporal()

fun JsonPrimitive.asFhirLocalTime(): FhirTemporal.LocalTime? =
    this.contentOrNull?.let {
        FhirTemporal.LocalTime(LocalTime.parse(it))
    }

fun JsonPrimitive.asLocalDateTime(): FhirTemporal.LocalDateTime? =
    this.contentOrNull?.let {
        FhirTemporal.LocalDateTime(LocalDateTime.parse(it))
    }

fun JsonPrimitive.asFhirLocalDate(): FhirTemporal.LocalDate? =
    this.contentOrNull?.let {
        FhirTemporal.LocalDate(LocalDate.parse(it))
    }

fun JsonPrimitive.asFhirInstant(): FhirTemporal.Instant? =
    this.contentOrNull?.let {
        FhirTemporal.Instant(Instant.parse(it))
    }
