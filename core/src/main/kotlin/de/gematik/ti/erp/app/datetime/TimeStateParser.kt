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

import androidx.annotation.StringRes
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.buildAnnotatedString
import de.gematik.ti.erp.app.core.R
import de.gematik.ti.erp.app.timestate.TimeState

/**
 * Parses the given [TimeState] into a user-friendly string representation.
 *
 * @param timeState The [TimeState] indicating how the timestamp should be displayed:
 * - [TimeState.SentNow]: Represents a timestamp within 5 minutes of the current time.
 * - [TimeState.ShowTime]: Represents a timestamp from today but not within 5 minutes.
 * - [TimeState.ShowDate]: Represents a timestamp from another day.
 *
 * @return A string that represents the parsed [timeState] for display:
 * - "Just now" for [TimeState.SentNow].
 * - A formatted time (e.g., "14:45") for [TimeState.ShowTime].
 * - A formatted date (e.g., "10.01.2025") for [TimeState.ShowDate].
 *
 * ### Examples:
 * - For `MessageTimeState.SentNow`: Returns `"Just now"`.
 * - For `MessageTimeState.ShowTime`: Returns `"14:45"` (localized time format).
 * - For `MessageTimeState.ShowDate`: Returns `"10.01.2025"` (localized date format).
 *
 * @see TimeState for the possible states and their usage.
 */
@Composable
fun timeStateParser(
    timeState: TimeState
): String {
    return when (timeState) {
        is TimeState.SentNow -> stringResource(R.string.time_description_few_minutes)
        is TimeState.ShowTime -> {
            val time = rememberErpTimeFormatter().time(timeState.timestamp)
            annotatedStringResource(R.string.message_list_time_state_hours, time).toString()
        }

        is TimeState.ShowDate -> rememberErpTimeFormatter().date(timeState.timestamp)
    }
}

@Composable
fun annotatedStringResource(@StringRes id: Int, vararg args: String): AnnotatedString {
    val raw = stringResource(id)
    return buildAnnotatedString {
        val parts = raw.split("%s")
        parts.forEachIndexed { index, part ->
            append(part)
            if (index < args.size) {
                append(args[index])
            }
        }
    }
}
