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

package de.gematik.ti.erp.app.database.room.v2.pharmacy

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import de.gematik.ti.erp.app.database.room.v2.task.util.InstantConverter
import kotlinx.datetime.Instant

@Entity(
    tableName = "pharmacies",
    indices = [
        Index("name"),
        Index("city"),
        Index("zip"),
        Index("isFavourite"),
        Index("isOftenUsed"),
        Index("id", unique = true),
        Index(value = ["latitude", "longitude"]) // handy for bounding-box queries
    ]
)
@TypeConverters(InstantConverter::class)
data class PharmacyEntity(
    @PrimaryKey val id: String,

    val name: String,
    val lineAddress: String,
    val city: String,
    val zip: String,

    val latitude: Double,
    val longitude: Double,

    val phone: String?,
    val fax: String?,
    val email: String?,
    val web: String?,
    val imagePath: String?, // local path or URL

    @ColumnInfo(defaultValue = "0")
    val countUsage: Int = 0,

    @ColumnInfo(defaultValue = "false") // SQLite boolean: 0/1
    val isFavourite: Boolean = false,

    @ColumnInfo(defaultValue = "false") // SQLite boolean: 0/1
    val isOftenUsed: Boolean = false,

    val created: Instant,
    val lastUsed: Instant? // nullable until first use
)

fun PharmacyEntity.toSingleLineAddress(): String =
    listOf(
        listOf(lineAddress).filter(String::isNotBlank).joinToString(" "),
        listOf(zip, city).filter(String::isNotBlank).joinToString(" ")
    ).filter(String::isNotBlank)
        .joinToString(", ")
