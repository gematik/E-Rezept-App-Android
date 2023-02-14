/*
 * Copyright (c) 2023 gematik GmbH
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

package de.gematik.ti.erp.app.db

import io.realm.kotlin.types.RealmInstant
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.UtcOffset
import kotlinx.datetime.asTimeZone
import kotlinx.datetime.toInstant
import kotlinx.datetime.toLocalDateTime
import kotlin.test.Test
import kotlin.test.assertEquals

class RealmInstantConverterTest {
    @Test
    fun `RealmInstant to LocalDateTime`() {
        val dt = RealmInstant.from(123456, 123456789).toLocalDateTime().toInstant(TimeZone.UTC)
        assertEquals(123456, dt.epochSeconds)
        assertEquals(123456789, dt.nanosecondsOfSecond)
    }

    @Test
    fun `LocalDateTime to RealmInstant`() {
        val ri = Instant.fromEpochSeconds(123456, 123456789).toRealmInstant()
        assertEquals(123456, ri.epochSeconds)
        assertEquals(123456789, ri.nanosecondsOfSecond)
    }

    @Test
    fun `RealmInstant to Instant`() {
        val dt = RealmInstant.from(123456, 123456789).toInstant()
        assertEquals(123456, dt.epochSeconds)
        assertEquals(123456789, dt.nanosecondsOfSecond)
    }

    @Test
    fun `Instant to RealmInstant`() {
        val ri = Instant.fromEpochSeconds(123456, 123456789).toRealmInstant()
        assertEquals(123456, ri.epochSeconds)
        assertEquals(123456789, ri.nanosecondsOfSecond)
    }

    @Test
    fun `Convert with offset`() {
        val dtPlus2 = Instant.parse("2022-02-04T14:05:10+02:00")
        val timestampAtUTC = dtPlus2.epochSeconds

        val realmInstantAtUTC = dtPlus2.toLocalDateTime(UtcOffset(hours = 2).asTimeZone()).toRealmInstant(
            UtcOffset(hours = 2).asTimeZone()
        )
        assertEquals(timestampAtUTC, realmInstantAtUTC.epochSeconds)

        val localDateTimeAtPlus2 = realmInstantAtUTC.toLocalDateTime(UtcOffset(hours = 2).asTimeZone())
        assertEquals(LocalDateTime.parse("2022-02-04T14:05:10"), localDateTimeAtPlus2)
    }
}
