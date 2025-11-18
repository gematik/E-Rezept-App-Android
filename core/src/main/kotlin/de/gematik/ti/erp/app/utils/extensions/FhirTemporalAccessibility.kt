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

package de.gematik.ti.erp.app.utils.extensions

import android.content.Context
import android.text.format.DateFormat
import de.gematik.ti.erp.app.fhir.temporal.FhirTemporal
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atStartOfDayIn
import kotlinx.datetime.toInstant
import java.util.Date

/**
 * Converts FhirTemporal to TalkBack string.
 * Keep consideration of user's locale and date/time preferences.
 */
fun FhirTemporal.toAccessibleString(
    context: Context,
    zone: TimeZone = TimeZone.currentSystemDefault()
): String = when (this) {
    is FhirTemporal.Instant -> {
        val date = Date(value.toEpochMilliseconds())
        "${DateFormat.getLongDateFormat(context).format(date)}, ${DateFormat.getTimeFormat(context).format(date)}"
    }
    is FhirTemporal.LocalDateTime -> {
        val date = Date(value.toInstant(zone).toEpochMilliseconds())
        "${DateFormat.getLongDateFormat(context).format(date)}, ${DateFormat.getTimeFormat(context).format(date)}"
    }
    is FhirTemporal.LocalDate -> {
        val date = Date(value.atStartOfDayIn(zone).toEpochMilliseconds())
        DateFormat.getLongDateFormat(context).format(date)
    }
    else -> toString()
}
