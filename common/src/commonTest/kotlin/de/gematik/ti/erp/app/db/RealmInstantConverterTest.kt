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

package de.gematik.ti.erp.app.db

import io.realm.kotlin.types.RealmInstant
import java.time.Instant
import java.time.LocalDateTime
import java.time.OffsetDateTime
import java.time.ZoneOffset
import kotlin.test.Test
import kotlin.test.assertEquals

class RealmInstantConverterTest {
    @Test
    fun `RealmInstant to LocalDateTime`() {
        val dt = RealmInstant.from(123456, 123456789).toLocalDateTime()
        assertEquals(123456, dt.toEpochSecond(ZoneOffset.UTC))
        assertEquals(123456789, dt.nano)
    }

    @Test
    fun `LocalDateTime to RealmInstant`() {
        val ri = LocalDateTime.ofEpochSecond(123456, 123456789, ZoneOffset.UTC).toRealmInstant()
        assertEquals(123456, ri.epochSeconds)
        assertEquals(123456789, ri.nanosecondsOfSecond)
    }

    @Test
    fun `RealmInstant to Instant`() {
        val dt = RealmInstant.from(123456, 123456789).toInstant()
        assertEquals(123456, dt.epochSecond)
        assertEquals(123456789, dt.nano)
    }

    @Test
    fun `Instant to RealmInstant`() {
        val ri = Instant.ofEpochSecond(123456, 123456789).toRealmInstant()
        assertEquals(123456, ri.epochSeconds)
        assertEquals(123456789, ri.nanosecondsOfSecond)
    }

    @Test
    fun `Convert with offset`() {
        val dtPlus2 = OffsetDateTime.parse("2022-02-04T14:05:10+02:00")
        val dtUTC = OffsetDateTime.parse("2022-02-04T12:05:10+00:00")
        val timestampAtUTC = dtPlus2.toEpochSecond()

        assertEquals(dtUTC, dtPlus2.withOffsetSameInstant(ZoneOffset.UTC))

        val realmInstantAtUTC = dtPlus2.toLocalDateTime().toRealmInstant(ZoneOffset.ofHours(2))
        assertEquals(timestampAtUTC, realmInstantAtUTC.epochSeconds)

        val localDateTimeAtPlus2 = realmInstantAtUTC.toLocalDateTime(ZoneOffset.ofHours(2))
        assertEquals(LocalDateTime.parse("2022-02-04T14:05:10"), localDateTimeAtPlus2)
    }
}
