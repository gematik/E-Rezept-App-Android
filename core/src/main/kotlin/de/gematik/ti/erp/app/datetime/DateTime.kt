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

package de.gematik.ti.erp.app.datetime

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalConfiguration
import de.gematik.ti.erp.app.fhir.temporal.FhirTemporal
import de.gematik.ti.erp.app.fhir.temporal.Year
import de.gematik.ti.erp.app.fhir.temporal.YearMonth
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toJavaLocalDate
import kotlinx.datetime.toJavaLocalDateTime
import kotlinx.datetime.toJavaLocalTime
import kotlinx.datetime.toKotlinTimeZone
import kotlinx.datetime.toLocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.util.Locale

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

        is FhirTemporal.Year -> temporal.value.toJavaYear().format(MonthPattern)

        is FhirTemporal.YearMonth -> temporal.value.toJavaYearMonth().format(YearMonthPattern)
    }

private fun YearMonth.toJavaYearMonth(): java.time.YearMonth = java.time.YearMonth.of(this.year, this.monthNumber)

private fun Year.toJavaYear(): java.time.Year = java.time.Year.of(this.year)

/**
 * Formatting dates and times based on locale and time zone. All default values reflect the usage within the Erp app:
 * - time formatting defaults to [Style.SHORT] cause we don't need seconds, never ever.
 * - date formatting defaults to [Style.MEDIUM] cause we always need four digit years
 *
 * @property locale The locale used for formatting.
 * @property zoneId The time zone used for formatting. Defaults to the system's default time zone.
 */
open class ErpTimeFormatter(val locale: Locale, val zoneId: ZoneId = ZoneId.systemDefault()) {

    /**
     * Styles for (localized) time, date or timestamp formatter.
     */
    enum class Style(javaStyle: FormatStyle) {
        /**
         * Short text style, typically numeric. Examples:
         * - 12:30 PM
         * - 6/1/12
         * - 6/1/12, 12:30 PM
         */
        SHORT(FormatStyle.SHORT),

        /**
         * Medium text style, with some detail. Examples
         * - 12:30:00 PM
         * - Jun 1, 2012
         * - Jun 1, 2012, 12:30:00 PM
         */
        MEDIUM(FormatStyle.MEDIUM),

        /**
         * Long text style, with lots of detail. Examples:
         * - 12:30:00 PM CEST
         * - June 1, 2012
         * - June 1, 2012 at 12:30:00 PM CEST
         */
        LONG(FormatStyle.LONG),

        /**
         * Full text style, with the most detail. Examples:
         * - 12:30:00 PM Central European Summer Time
         * - Friday, June 1, 2012
         * - Friday, June 1, 2012 at 12:30:00 PM Central European Summer Time'
         */
        FULL(FormatStyle.FULL);

        /**
         * Ready to use date formatter for this style and [Locale.getDefault].
         */
        internal val dateFormatter: DateTimeFormatter by lazy {
            DateTimeFormatter.ofLocalizedDate(javaStyle)
        }

        /**
         * Ready to use time formatter for this style and [Locale.getDefault].
         */
        internal val timeFormatter: DateTimeFormatter by lazy {
            DateTimeFormatter.ofLocalizedTime(javaStyle)
        }

        /**
         * Ready to use date-time formatter for this style and [Locale.getDefault].
         */
        internal val timestampFormatter: DateTimeFormatter by lazy {
            DateTimeFormatter.ofLocalizedDateTime(javaStyle)
        }
    }

    val timezone = zoneId.toKotlinTimeZone()

    fun Instant.toJavaLocalDateTime() = this.toLocalDateTime(timezone).toJavaLocalDateTime()

    /**
     * Creates a time string with the given style using [locale] and [zoneId]
     *
     * @param instant The `Instant` to be formatted.
     * @param style The formatting style. Defaults to [Style.SHORT]
     * @return a string representation of the time in [instant]
     */
    fun time(instant: Instant, style: Style = Style.SHORT): String {
        val ldt = instant.toJavaLocalDateTime().atZone(zoneId)
        return style.timeFormatter.withLocale(locale).format(ldt)
    }

    /**
     * Creates a time string with the given style using [locale] and [zoneId]
     *
     * @param localTime The time to be formatted.
     * @param style The formatting style. Defaults to [Style.SHORT]. Wont work with [Style.LONG] and [Style.FULL]
     * @param ifNull a function that can return a string to be used if [localTime] or throw an exception. Defaults to ""
     * @return a string representation of the time of [localTime]
     * @throws [java.time.DateTimeException] if [Style.LONG] or [Style.FULL] is used, cause there is now zone info in [localTime]
     */
    fun time(
        localTime: LocalTime?,
        style: Style = Style.SHORT,
        ifNull: () -> String = { "" }
    ): String {
        return if (localTime == null) {
            ifNull()
        } else {
            style.timeFormatter.withLocale(locale).format(localTime.toJavaLocalTime())
        }
    }

    /**
     * Creates a date string with the given style using [locale] and [zoneId]
     *
     * @param instant The `Instant` to be formatted.
     * @param style The formatting style. Defaults to [Style.MEDIUM] which differs from the default in [time]!
     * @return a string representation of the date in [instant]
     */
    fun date(instant: Instant, style: Style = Style.MEDIUM): String {
        val ldt = instant.toJavaLocalDateTime().atZone(zoneId)
        return style.dateFormatter.withLocale(locale).format(ldt)
    }

    /**
     * Creates a date-time string with the given style using [locale] and [zoneId]
     *
     * @param instant The `Instant` to be formatted.
     * @param style The formatting style. Defaults to [Style.SHORT]
     * @return a string representaion of the date and time in [instant]
     */
    fun timestamp(instant: Instant, style: Style = Style.SHORT): String {
        val ldt = instant.toJavaLocalDateTime().atZone(zoneId)
        return style.timestampFormatter.withLocale(locale).format(ldt)
    }

    /**
     * Companion object providing a default instance of [ErpTimeFormatter] using
     * the system's default locale and zoneId.
     */
    companion object Default : ErpTimeFormatter(Locale.getDefault())
}

@Composable
fun rememberErpTimeFormatter(): ErpTimeFormatter {
    val appLocale = LocalConfiguration.current.locales[0]

    // avoid recreating unless the locale changes
    return remember(appLocale) {
        ErpTimeFormatter(locale = appLocale)
    }
}
