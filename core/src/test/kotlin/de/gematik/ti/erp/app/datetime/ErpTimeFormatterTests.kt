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

import de.gematik.ti.erp.app.datetime.ErpTimeFormatter.Style
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toInstant
import kotlinx.datetime.toLocalDateTime
import org.junit.Assert.assertThrows
import java.time.DateTimeException
import java.util.Locale
import kotlin.test.Test
import kotlin.test.assertEquals

val FIRST_OF_MAY = Instant.parse("2025-05-01T00:00:00Z")
val ELEVENTH_OF_NOVEMBER = Instant.parse("1991-11-11T11:11:11Z")
val TWELVE_THIRTY = LocalDateTime.parse("2012-06-01T12:30:00").toInstant(TimeZone.currentSystemDefault())

class ErpTimeFormatterTests {
    @Test
    fun `test german time formats for kotlinx Instant`() = with(ErpTimeFormatter(Locale.GERMANY)) {
        assertEquals("12:30", time(TWELVE_THIRTY)) // Style.SHORT
        assertEquals("12:30:00", time(TWELVE_THIRTY, style = Style.MEDIUM))
        assertEquals("12:30:00 MESZ", time(TWELVE_THIRTY, Style.LONG))
        assertEquals("12:30:00 Mitteleuropäische Sommerzeit", time(TWELVE_THIRTY, Style.FULL))
    }

    @Test
    fun `test german time formats for kotlinx LocalTime`() = with(ErpTimeFormatter(Locale.GERMANY)) {
        val lt = TWELVE_THIRTY.toLocalDateTime(timezone).time
        assertEquals("12:30", time(lt)) // Style.SHORT
        assertEquals("12:30:00", time(lt, style = Style.MEDIUM))

        assertThrows(DateTimeException::class.java) {
            time(lt, Style.LONG)
        }

        assertThrows(IllegalArgumentException::class.java) {
            time(null, ifNull = { throw IllegalArgumentException() })
        }

        assertEquals("", time(null))
        assertEquals("n/a", time(null, ifNull = { "n/a" }))
    }

    @Test
    fun `test UK time formats kotlinx Instant`() = with(ErpTimeFormatter(Locale.UK)) {
        assertEquals("12:30", time(TWELVE_THIRTY)) // Style.SHORT
        assertEquals("12:30:00", time(TWELVE_THIRTY, style = Style.MEDIUM))
        assertEquals("12:30:00 CEST", time(TWELVE_THIRTY, Style.LONG))
        assertEquals("12:30:00 Central European Summer Time", time(TWELVE_THIRTY, Style.FULL))
    }

    @Test
    fun `test US time formats kotlinx Instant`() = with(ErpTimeFormatter(Locale.US)) {
        assertEquals("12:30 PM", time(TWELVE_THIRTY)) // Style.SHORT
        assertEquals("12:30:00 PM", time(TWELVE_THIRTY, style = Style.MEDIUM))
        assertEquals("12:30:00 PM CEST", time(TWELVE_THIRTY, Style.LONG))
        assertEquals("12:30:00 PM Central European Summer Time", time(TWELVE_THIRTY, Style.FULL))
    }

    @Test
    fun `test german date formats kotlinx Instant`() = with(ErpTimeFormatter(Locale.GERMANY)) {
        assertEquals("01.05.25", date(FIRST_OF_MAY, style = Style.SHORT))
        assertEquals("01.05.2025", date(FIRST_OF_MAY)) // style = Style.MEDIUM
        assertEquals("1. Mai 2025", date(FIRST_OF_MAY, Style.LONG))
        assertEquals("Donnerstag, 1. Mai 2025", date(FIRST_OF_MAY, Style.FULL))
    }

    @Test
    fun `test UK date formats kotlinx Instant`() = with(ErpTimeFormatter(Locale.UK)) {
        assertEquals("01/05/2025", date(FIRST_OF_MAY, style = Style.SHORT))
        assertEquals("1 May 2025", date(FIRST_OF_MAY)) // style = Style.MEDIUM
        assertEquals("1 May 2025", date(FIRST_OF_MAY, Style.LONG))
        assertEquals("Thursday, 1 May 2025", date(FIRST_OF_MAY, Style.FULL))
    }

    @Test
    fun `test US date formats kotlinx Instant`() = with(ErpTimeFormatter(Locale.US)) {
        assertEquals("5/1/25", date(FIRST_OF_MAY, style = Style.SHORT))
        assertEquals("11/11/91", date(ELEVENTH_OF_NOVEMBER, style = Style.SHORT))
        assertEquals("May 1, 2025", date(FIRST_OF_MAY))
        assertEquals("May 1, 2025", date(FIRST_OF_MAY, Style.LONG))
        assertEquals("June 1, 2012", date(TWELVE_THIRTY, Style.LONG))
        assertEquals("Jun 1, 2012", date(TWELVE_THIRTY)) // Style.MEDIUM
        assertEquals("November 11, 1991", date(ELEVENTH_OF_NOVEMBER, style = Style.LONG))
        assertEquals("Thursday, May 1, 2025", date(FIRST_OF_MAY, Style.FULL))
        assertEquals("Friday, June 1, 2012", date(TWELVE_THIRTY, Style.FULL))
        assertEquals("6/1/12", date(TWELVE_THIRTY, Style.SHORT))
    }

    @Test
    fun `test german timestamp formats kotlinx Instant`() = with(ErpTimeFormatter(Locale.GERMANY)) {
        assertEquals("01.06.12, 12:30", timestamp(TWELVE_THIRTY)) // Style.SHORT
        assertEquals("01.06.2012, 12:30:00", timestamp(TWELVE_THIRTY, style = Style.MEDIUM))
        assertEquals("1. Juni 2012 um 12:30:00 MESZ", timestamp(TWELVE_THIRTY, Style.LONG))
        assertEquals("Freitag, 1. Juni 2012 um 12:30:00 Mitteleuropäische Sommerzeit", timestamp(TWELVE_THIRTY, Style.FULL))
    }

    @Test
    fun `test US timestamp formats kotlinx Instant`() = with(ErpTimeFormatter(Locale.US)) {
        assertEquals("6/1/12, 12:30 PM", timestamp(TWELVE_THIRTY)) // Style.SHORT
        assertEquals("Jun 1, 2012, 12:30:00 PM", timestamp(TWELVE_THIRTY, style = Style.MEDIUM))
        assertEquals("June 1, 2012 at 12:30:00 PM CEST", timestamp(TWELVE_THIRTY, Style.LONG))
        assertEquals("Friday, June 1, 2012 at 12:30:00 PM Central European Summer Time", timestamp(TWELVE_THIRTY, Style.FULL))
    }

    @Test
    fun `test some french formats kotlinx Instant`() = with(ErpTimeFormatter(Locale.FRENCH)) {
        assertEquals("01/06/2012 12:30", timestamp(TWELVE_THIRTY)) // Style.SHORT
        assertEquals("1 juin 2012, 12:30:00", timestamp(TWELVE_THIRTY, style = Style.MEDIUM))
        assertEquals("12:30:00 heure d’été d’Europe centrale", time(TWELVE_THIRTY, Style.FULL))
        assertEquals("1 juin 2012 à 12:30:00 CEST", timestamp(TWELVE_THIRTY, Style.LONG))
    }

    @Test
    fun `test some arabic formats kotlinx Instant`() = with(ErpTimeFormatter(Locale.forLanguageTag("ar"))) {
        assertEquals("12:30 م", time(TWELVE_THIRTY, Style.SHORT))
        assertEquals("1\u200F/6\u200F/2012", date(TWELVE_THIRTY, Style.SHORT))
        assertEquals("1\u200F/6\u200F/2012, 12:30 م", timestamp(TWELVE_THIRTY))
        assertEquals("01\u200F/06\u200F/2012, 12:30:00 م", timestamp(TWELVE_THIRTY, style = Style.MEDIUM)) // MEDIUM
        assertEquals("12:30:00 م توقيت وسط أوروبا الصيفي", time(TWELVE_THIRTY, Style.FULL))
        assertEquals("1 يونيو 2012 في 12:30:00 م CEST", timestamp(TWELVE_THIRTY, Style.LONG))
    }
}

enum class ErpLanguageCode(val code: String) {
    DE("de"), // default language
    AR("ar"),
    BG("bg"),
    CS("cs"),
    DA("da"),
    EN("en"),
    FR("fr"),
    IW("iw"),
    IT("it"),
    NL("nl"),
    PL("pl"),
    RO("ro"),
    RU("ru"),
    TR("tr"),
    UK("uk"),
    ES("es"),
    GA("ga");
}
