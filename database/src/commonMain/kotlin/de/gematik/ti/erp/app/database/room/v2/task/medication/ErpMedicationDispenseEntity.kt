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

package de.gematik.ti.erp.app.database.room.v2.task.medication

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import androidx.room.TypeConverter
import androidx.room.TypeConverters
import de.gematik.ti.erp.app.database.room.v2.task.util.InstantConverter
import kotlinx.datetime.Instant

@Suppress("EnumEntryNameCase")
enum class ErpMedicationDispenseType {
    Epa,
    Ingredient,
    Diga,
    Freetext,
    Compounding,
    Other
}

// ---------- Converters ----------
class EnumConverters {
    @TypeConverter
    fun fromDispenseType(v: ErpMedicationDispenseType?): String? = v?.name

    @TypeConverter
    fun toDispenseType(s: String?): ErpMedicationDispenseType =
        runCatching { ErpMedicationDispenseType.valueOf(s ?: "") }.getOrElse { ErpMedicationDispenseType.Other }
}

@Entity(
    tableName = "medication_dispense",
    foreignKeys = [
        ForeignKey(
            entity = ErpIngredientEntity::class,
            parentColumns = ["ingredientId"],
            childColumns = ["ingredientId"],
            onUpdate = ForeignKey.CASCADE,
            onDelete = ForeignKey.SET_NULL
        ),
        ForeignKey(
            entity = ErpMedicationEntity::class,
            parentColumns = ["medicationId"],
            childColumns = ["medicationId"],
            onUpdate = ForeignKey.CASCADE,
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = ErpMedicationDispenseEntity::class,
            parentColumns = ["dispenseId"],
            childColumns = ["childId"],
            onDelete = ForeignKey.CASCADE
        )

    ],
    indices = [Index("medicationId")]
)
@TypeConverters(InstantConverter::class, EnumConverters::class)
data class ErpMedicationDispenseEntity(
    // Primary key
    @PrimaryKey val dispenseId: String,
    val childId: String?, // ID of the parent entity (e.g., Task or MedicationDispenseE)
    val medicationId: String,
    val taskId: String,
    val ingredientId: String?,

    // Core attributes
    val patientIdentifier: String, // KVNR
    val substitutionAllowed: Boolean,
    val dosageInstruction: String,
    val performer: String, // Telematik-ID
    val handedOverOn: Instant, // DateTime -> Instant
    val text: String,
    val euCountryCode: String? = null,
    val pharmacyName: String? = null,
    // Classification
    val medicationDispenseType: ErpMedicationDispenseType,

    // Medication facets
    val form: String?,
    @Embedded(prefix = "amount_")
    val amount: ErpRatioEmbeddable, // embedded ratio (num_*/den_* columns)
    val isVaccine: Boolean,
    val lotNumber: String,
    val expirationDate: Instant?,
    val pzn: String,
    val deepLink: String? = null,
    val redeemCode: String? = null,
    val declineCode: String? = null,
    val note: String? = null,
    val modifiedDate: Instant? = null,
    val display: String? = null,
    val status: String? = null
)
