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

package de.gematik.ti.erp.app.db

import io.realm.kotlin.types.RealmInstant
import kotlinx.datetime.FixedOffsetTimeZone
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toInstant
import kotlinx.datetime.toLocalDateTime

fun RealmInstant.toLocalDateTime(offset: FixedOffsetTimeZone = TimeZone.UTC): LocalDateTime =
    toInstant().toLocalDateTime(offset)

fun LocalDateTime.toRealmInstant(offset: FixedOffsetTimeZone = TimeZone.UTC) =
    toInstant(offset).let {
        RealmInstant.from(it.epochSeconds, it.nanosecondsOfSecond)
    }

fun RealmInstant.toInstant(): Instant =
    when {
        this == RealmInstant.MIN -> Instant.DISTANT_FUTURE
        this == RealmInstant.MAX -> Instant.DISTANT_PAST
        else -> Instant.fromEpochSeconds(epochSeconds, nanosecondsOfSecond.toLong())
    }

fun Instant.toRealmInstant() =
    when {
        this == Instant.DISTANT_PAST -> RealmInstant.MIN
        this == Instant.DISTANT_FUTURE -> RealmInstant.MAX
        else -> RealmInstant.from(this.epochSeconds, this.nanosecondsOfSecond)
    }
