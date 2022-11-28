/*
 * Copyright (c) 2022 gematik GmbH
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

package de.gematik.ti.erp.app.utils.compose

import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.stringResource
import de.gematik.ti.erp.app.R
import java.time.Duration
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

enum class TimeDiff {
    FewMinutes,
    Today,
    Other
}

private const val FewMinutes = 5L // 5 minutes
private const val Today = 1440L / 2L // 12 hours

private fun timeDiff(diffMinutes: Long): TimeDiff =
    when {
        diffMinutes < FewMinutes -> TimeDiff.FewMinutes
        diffMinutes < Today -> TimeDiff.Today
        else -> TimeDiff.Other
    }

typealias TimeDescriptionFormatter = (diff: TimeDiff, localDt: LocalDateTime, duration: Duration) -> String

@Composable
fun timeDescription(
    instant: Instant,
    formatter: TimeDescriptionFormatter = TimeDescriptionDefaults.formatter()
): State<String> {
    LocalConfiguration.current

    val dt by rememberUpdatedState(instant)
    val fmt by rememberUpdatedState(formatter)
    val timeString = remember(dt, fmt) {
        val duration = Duration.between(dt, Instant.now())
        val diffMinutes = duration.toMinutes()
        val localDt = LocalDateTime.ofInstant(dt, ZoneId.systemDefault())
        mutableStateOf(fmt(timeDiff(diffMinutes = diffMinutes), localDt, duration))
    }
    return timeString
}

object TimeDescriptionDefaults {

    @Composable
    fun formatter(): TimeDescriptionFormatter {
        val fewMinutes = stringResource(R.string.time_description_few_minutes)
        val today = stringResource(R.string.time_description_today)

        return remember {
            val hours = DateTimeFormatter.ofPattern("HH:mm")
            val other = DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM)

            val fmt: TimeDescriptionFormatter = { diff, localDt, _ ->
                when (diff) {
                    TimeDiff.FewMinutes -> fewMinutes
                    TimeDiff.Today -> today.format(hours.format(localDt))
                    TimeDiff.Other -> other.format(localDt)
                }
            }
            fmt
        }
    }
}
