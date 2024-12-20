/*
 * Copyright 2024, gematik GmbH
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

package de.gematik.ti.erp.app.prescription.ui.model

import kotlinx.datetime.Instant

sealed interface SentOrCompletedPhrase {
    data object RedeemedJustNow : SentOrCompletedPhrase
    data object SentJustNow : SentOrCompletedPhrase
    data object ProvidedJustNow : SentOrCompletedPhrase

    data class RedeemedMinutesAgo(val minutes: Long) : SentOrCompletedPhrase
    data class SentMinutesAgo(val minutes: Long) : SentOrCompletedPhrase
    data class ProvidedMinutesAgo(val minutes: Long) : SentOrCompletedPhrase

    data class RedeemedHoursAgo(val on: Instant) : SentOrCompletedPhrase
    data class SentHoursAgo(val on: Instant) : SentOrCompletedPhrase
    data class ProvidedHoursAgo(val on: Instant) : SentOrCompletedPhrase

    data class RedeemedOn(val on: Instant) : SentOrCompletedPhrase
    data class SentOn(val on: Instant) : SentOrCompletedPhrase
    data class ProvidedOn(val on: Instant) : SentOrCompletedPhrase
}

private const val JustNowMinutes = 5L
private const val MinutesAgo = 60L

fun sentOrCompleted(
    lastModified: Instant,
    now: Instant,
    completed:
        Boolean = false,
    provided: Boolean = false
): SentOrCompletedPhrase {
    val dayDifference = (now - lastModified).inWholeDays
    val minDifference = (now - lastModified).inWholeMinutes
    return when {
        minDifference < JustNowMinutes -> if (completed) {
            SentOrCompletedPhrase.RedeemedJustNow
        } else if (provided) {
            SentOrCompletedPhrase.ProvidedJustNow
        } else {
            SentOrCompletedPhrase.SentJustNow
        }
        minDifference < MinutesAgo -> if (completed) {
            SentOrCompletedPhrase.RedeemedMinutesAgo(minDifference)
        } else if (provided) {
            SentOrCompletedPhrase.ProvidedMinutesAgo(minDifference)
        } else {
            SentOrCompletedPhrase.SentMinutesAgo(minDifference)
        }

        dayDifference <= 0L -> if (completed) {
            SentOrCompletedPhrase.RedeemedHoursAgo(lastModified)
        } else if (provided) {
            SentOrCompletedPhrase.ProvidedHoursAgo(lastModified)
        } else {
            SentOrCompletedPhrase.SentHoursAgo(lastModified)
        }

        else -> if (completed) {
            SentOrCompletedPhrase.RedeemedOn(lastModified)
        } else if (provided) {
            SentOrCompletedPhrase.ProvidedOn(lastModified)
        } else {
            SentOrCompletedPhrase.SentOn(lastModified)
        }
    }
}
