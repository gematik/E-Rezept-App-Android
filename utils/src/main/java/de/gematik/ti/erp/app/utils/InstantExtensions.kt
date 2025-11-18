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

package de.gematik.ti.erp.app.utils

import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

@OptIn(kotlin.time.ExperimentalTime::class)
fun Instant?.format(pattern: String = dateFormat): String {
    if (this == null) return ""

    val zone = TimeZone.currentSystemDefault()
    val localDateTime = this.toLocalDateTime(zone)

    return pattern
        .replace("dd", localDateTime.dayOfMonth.toString().padStart(2, '0'))
        .replace("MM", localDateTime.monthNumber.toString().padStart(2, '0'))
        .replace("yyyy", localDateTime.year.toString())
}

/**
 * Converts an Instant to a timestamp string in the format "gtYYYY-MM-DDTHH:MM:SSZ".
 * The seconds are truncated to the nearest second.
 * Required for calls to the Fachdienst API, which expects timestamps in this format.
 *
 * @return A formatted timestamp string or null if the Instant is null.
 */
@OptIn(kotlin.time.ExperimentalTime::class)
fun Instant?.toFachdienstTimestampString(): String? = this?.let {
    val seconds = it.epochSeconds
    val truncated = Instant.fromEpochSeconds(seconds)
    // toString() yields e.g. "2025-06-12T13:14:15Z"
    "gt$truncated"
}

private const val dateFormat = "dd.MM.yyyy"
