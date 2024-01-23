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

package de.gematik.ti.erp.app.prescription.ui.model

import kotlinx.datetime.Instant

sealed interface SentOrCompletedPhrase {
    object RedeemedJustNow : SentOrCompletedPhrase
    object SentJustNow : SentOrCompletedPhrase

    data class RedeemedMinutesAgo(val minutes: Long) : SentOrCompletedPhrase
    data class SentMinutesAgo(val minutes: Long) : SentOrCompletedPhrase

    data class RedeemedHoursAgo(val on: Instant) : SentOrCompletedPhrase
    data class SentHoursAgo(val on: Instant) : SentOrCompletedPhrase

    data class RedeemedOn(val on: Instant) : SentOrCompletedPhrase
    data class SentOn(val on: Instant) : SentOrCompletedPhrase
}

private const val JustNowMinutes = 5L
private const val MinutesAgo = 60L

fun sentOrCompleted(lastModified: Instant, now: Instant, completed: Boolean = false): SentOrCompletedPhrase {
    val dayDifference = (now - lastModified).inWholeDays
    val minDifference = (now - lastModified).inWholeMinutes
    return when {
        minDifference < JustNowMinutes -> if (completed) {
            SentOrCompletedPhrase.RedeemedJustNow
        } else {
            SentOrCompletedPhrase.SentJustNow
        }

        minDifference < MinutesAgo -> if (completed) {
            SentOrCompletedPhrase.RedeemedMinutesAgo(minDifference)
        } else {
            SentOrCompletedPhrase.SentMinutesAgo(minDifference)
        }

        dayDifference <= 0L -> if (completed) {
            SentOrCompletedPhrase.RedeemedHoursAgo(lastModified)
        } else {
            SentOrCompletedPhrase.SentHoursAgo(lastModified)
        }

        else -> if (completed) {
            SentOrCompletedPhrase.RedeemedOn(lastModified)
        } else {
            SentOrCompletedPhrase.SentOn(lastModified)
        }
    }
}
