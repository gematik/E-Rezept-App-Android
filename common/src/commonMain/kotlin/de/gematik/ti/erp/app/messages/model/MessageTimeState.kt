/*
 * Copyright 2025, gematik GmbH
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
 * In case of changes by gematik find details in the "Readme" file.
 *
 * See the Licence for the specific language governing permissions and limitations under the Licence.
 */

package de.gematik.ti.erp.app.messages.model

import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlinx.serialization.Serializable
import kotlin.time.Duration.Companion.minutes

@Serializable
sealed class MessageTimeState(open val timestamp: Instant) {
    data class SentNow(override val timestamp: Instant) : MessageTimeState(timestamp)
    data class ShowTime(override val timestamp: Instant) : MessageTimeState(timestamp) // e.g., "14:45"
    data class ShowDate(override val timestamp: Instant) : MessageTimeState(timestamp) // e.g., "10.01.2025"
}

/**
 * Determines the display state of a given timestamp based on its relation to the current time.
 *
 * @param timestamp The `Instant` representing the time to be evaluated.
 * @return A [MessageTimeState] that represents how the timestamp should be displayed:
 * - [MessageTimeState.SentNow]: If the timestamp is within 5 minutes (past or future) from the current time.
 * - [MessageTimeState.ShowTime]: If the timestamp falls on today's date but is not within 5 minutes of the current time.
 * - [MessageTimeState.ShowDate]: If the timestamp is on a date other than today.
 *
 * ### Examples:
 * - If the timestamp is within 5 minutes of now:
 *     Returns `MessageTimeState.SentNow`.
 * - If the timestamp is from earlier today:
 *     Returns `MessageTimeState.ShowTime` (e.g., displayed as `14:45`).
 * - If the timestamp is from a previous or future date:
 *     Returns `MessageTimeState.ShowDate` (e.g., displayed as `10.01.2025`).
 *
 * @see MessageTimeState for the possible states and their representations.
 */
fun getTimeState(timestamp: Instant): MessageTimeState {
    val now = Clock.System.now()
    val timeZone = TimeZone.currentSystemDefault()
    val currentDate = now.toLocalDateTime(timeZone)
    val messageDate = timestamp.toLocalDateTime(timeZone)

    return when {
        timestamp in (now - 5.minutes)..(now + 5.minutes) -> MessageTimeState.SentNow(timestamp)
        messageDate.date == currentDate.date -> MessageTimeState.ShowTime(timestamp)
        else -> MessageTimeState.ShowDate(timestamp)
    }
}
