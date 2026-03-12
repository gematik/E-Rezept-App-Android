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

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity(
    tableName = "medication",
    foreignKeys = [
        ForeignKey( // ingredient
            entity = ErpIngredientEntity::class,
            parentColumns = ["ingredientId"],
            childColumns = ["ingredientId"],
            onUpdate = ForeignKey.Companion.CASCADE,
            onDelete = ForeignKey.Companion.SET_NULL
        ),
        ForeignKey( // ratio
            entity = ErpRatioEntity::class,
            parentColumns = ["ratioId"],
            childColumns = ["ratioId"],
            onUpdate = ForeignKey.Companion.CASCADE,
            onDelete = ForeignKey.Companion.SET_NULL
        )
    ]
)
data class ErpMedicationEntity(
    @PrimaryKey
    val medicationId: String,

    val ingredientId: String,
    val ratioId: String,
    /** Free-text display for the medication */
    val text: String,

    /** Drug/medication category (aka "drugCategory" on iOS) */
    val medicationCategory: String,

    /** Dosage form (aka "dosageForm" on iOS) */
    val form: String,

    /** Whether this is a vaccine (aka "isVaccine" on iOS) */
    val vaccine: Boolean,

    /** Manufacturing/compounding instructions */
    val manufacturingInstructions: String,

    /** Packaging description */
    val packaging: String,

    /** Norm size code */
    val normSizeCode: String,

    /** PZN; on Android this may appear under ingredients, so it can be absent here */
    val pzn: String? = null
)
