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
package de.gematik.ti.erp.app.pharmacy.mapper

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import de.gematik.ti.erp.app.core.R
import de.gematik.ti.erp.app.fhir.temporal.FhirTemporal
import de.gematik.ti.erp.app.fhir.temporal.FhirTemporal.Instant
import de.gematik.ti.erp.app.fhir.temporal.FhirTemporal.LocalDate
import de.gematik.ti.erp.app.fhir.temporal.FhirTemporal.LocalDateTime
import de.gematik.ti.erp.app.fhir.temporal.FhirTemporal.LocalTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

/**
 * Converts a [FhirTemporal] instance into a human-readable, localized date/time string.
 *
 * This function handles all supported FHIR temporal representations:
 *  - [Instant]: converted to the given [zone] before formatting.
 *  - [LocalDateTime]: formatted as `dd.MM.yyyy, HH:mm Uhr`.
 *  - [LocalDate]: formatted as `dd.MM.yyyy`.
 *  - [LocalTime]: formatted as `HH:mm Uhr`.
 *
 * Examples:
 * ```
 * Instant.parse("2025-07-15T18:00:00Z") → "15.07.2025, 20:00 Uhr"  (Berlin in UTC+2)
 * LocalDate(2025, 7, 15)                → "15.07.2025"
 * LocalTime(20, 0)                      → "20:00 Uhr"
 * ```
 *
 * If no temporal value is present, an empty string is returned.
 *
 * @param zone the [TimeZone] used for converting [Instant] or [LocalDateTime] values.
 *              Defaults to [TimeZone.currentSystemDefault].
 * @return a German-style formatted date/time string suitable for UI display.
 */
@Composable
internal fun FhirTemporal.toFhirReadableDateTime(zone: TimeZone = TimeZone.currentSystemDefault()): String? {
    val (datePart, timePart) = when (this) {
        is Instant -> {
            val ldt = value.toLocalDateTime(zone)
            val time = "%02d:%02d".format(ldt.hour, ldt.minute)
            val formattedTime = stringResource(R.string.message_list_time_state_hours, time)
            "%02d.%02d.%04d".format(ldt.dayOfMonth, ldt.monthNumber, ldt.year) to formattedTime
        }

        is LocalDate -> {
            "%02d.%02d.%04d".format(value.dayOfMonth, value.monthNumber, value.year) to null
        }

        is LocalDateTime -> {
            val time = "%02d:%02d".format(value.hour, value.minute)
            val formattedTime = stringResource(R.string.message_list_time_state_hours, time)
            "%02d.%02d.%04d".format(value.dayOfMonth, value.monthNumber, value.year) to formattedTime
        }

        is LocalTime -> {
            val time = "%02d:%02d".format(value.hour, value.minute)
            val formattedTime = stringResource(R.string.message_list_time_state_hours, time)
            null to formattedTime
        }

        else -> {
            null to null
        }
    }

    return when {
        datePart != null && timePart != null -> "$datePart, $timePart"
        datePart != null -> datePart
        timePart != null -> timePart
        else -> null
    }
}
