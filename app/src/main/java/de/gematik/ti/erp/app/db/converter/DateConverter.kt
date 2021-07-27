/*
 * Copyright (c) 2021 gematik GmbH
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

package de.gematik.ti.erp.app.db.converter

import androidx.room.TypeConverter
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter

class DateConverter {
    private val offsetDateTimeFormatter = DateTimeFormatter.ISO_OFFSET_DATE_TIME

    @TypeConverter
    fun toInstant(value: Long?): Instant? {
        return value?.let {
            Instant.ofEpochMilli(it)
        }
    }

    @TypeConverter
    fun fromInstant(timestamp: Instant?): Long? {
        return timestamp?.toEpochMilli()
    }

    @TypeConverter
    fun toOffsetDateTime(value: String?): OffsetDateTime? {
        return value?.let {
            OffsetDateTime.parse(it)
        }
    }

    @TypeConverter
    fun fromOffsetDateTime(date: OffsetDateTime?): String? {
        return date?.format(offsetDateTimeFormatter)
    }

    @TypeConverter
    fun toLocalDate(value: String?): LocalDate? {
        return value?.let {
            LocalDate.parse(it)
        }
    }

    @TypeConverter
    fun fromLocalDate(date: LocalDate?): String? {
        return date?.format(DateTimeFormatter.ISO_DATE)
    }

    @TypeConverter
    fun toLocalDateTime(value: String?): LocalDateTime? {
        return value?.let {
            LocalDateTime.parse(it)
        }
    }

    @TypeConverter
    fun fromLocalDateTime(date: LocalDateTime?): String? {
        return date?.format(DateTimeFormatter.ISO_DATE_TIME)
    }
}
