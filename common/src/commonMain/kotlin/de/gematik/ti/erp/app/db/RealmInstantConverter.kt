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
import java.time.ZoneOffset

fun RealmInstant.toLocalDateTime(offset: ZoneOffset = ZoneOffset.UTC): LocalDateTime =
    LocalDateTime.ofEpochSecond(epochSeconds, nanosecondsOfSecond, offset)

fun LocalDateTime.toRealmInstant(offset: ZoneOffset = ZoneOffset.UTC) =
    RealmInstant.from(toEpochSecond(offset), toLocalTime().nano)

fun RealmInstant.toInstant(): Instant =
    when {
        this == RealmInstant.MIN -> Instant.MIN
        this == RealmInstant.MAX -> Instant.MAX
        else -> Instant.ofEpochSecond(epochSeconds, nanosecondsOfSecond.toLong())
    }

fun Instant.toRealmInstant() =
    when {
        this == Instant.MIN -> RealmInstant.MIN
        this == Instant.MAX -> RealmInstant.MAX
        else -> RealmInstant.from(epochSecond, nano)
    }
