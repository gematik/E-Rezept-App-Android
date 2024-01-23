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

import kotlinx.datetime.Clock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.time.Duration.Companion.days
import kotlin.time.Duration.Companion.minutes

class SentOrCompletedTest {
    @Test
    fun `just now - completed`() {
        val now = Clock.System.now()
        val lastModified = now - 4.minutes

        val result = sentOrCompleted(lastModified = lastModified, now = now, completed = true)

        assertEquals(SentOrCompletedPhrase.RedeemedJustNow, result)
    }

    @Test
    fun `just now - not completed`() {
        val now = Clock.System.now()
        val lastModified = now - 4.minutes

        val result = sentOrCompleted(lastModified = lastModified, now = now, completed = false)

        assertEquals(SentOrCompletedPhrase.SentJustNow, result)
    }

    @Test
    fun `redeemed minutes ago - completed`() {
        val now = Clock.System.now()
        val lastModified = now - 30.minutes

        val result = sentOrCompleted(lastModified = lastModified, now = now, completed = true)

        assertEquals(SentOrCompletedPhrase.RedeemedMinutesAgo(30), result)
    }

    @Test
    fun `redeemed minutes ago - not completed`() {
        val now = Clock.System.now()
        val lastModified = now - 30.minutes

        val result = sentOrCompleted(lastModified = lastModified, now = now, completed = false)

        assertEquals(SentOrCompletedPhrase.SentMinutesAgo(30), result)
    }

    @Test
    fun `redeemed hours ago - completed`() {
        val now = Clock.System.now()
        val lastModified = now - 120.minutes

        val result = sentOrCompleted(lastModified = lastModified, now = now, completed = true)

        assertEquals(SentOrCompletedPhrase.RedeemedHoursAgo(lastModified), result)
    }

    @Test
    fun `redeemed hours ago - not completed`() {
        val now = Clock.System.now()
        val lastModified = now - 120.minutes

        val result = sentOrCompleted(lastModified = lastModified, now = now, completed = false)

        assertEquals(SentOrCompletedPhrase.SentHoursAgo(lastModified), result)
    }

    @Test
    fun `redeemed on - completed`() {
        val now = Clock.System.now()
        val lastModified = now - 1.days

        val result = sentOrCompleted(lastModified = lastModified, now = now, completed = true)

        assertEquals(SentOrCompletedPhrase.RedeemedOn(lastModified), result)
    }

    @Test
    fun `redeemed on - not completed`() {
        val now = Clock.System.now()
        val lastModified = now - 1.days

        val result = sentOrCompleted(lastModified = lastModified, now = now, completed = false)

        assertEquals(SentOrCompletedPhrase.SentOn(lastModified), result)
    }
}
