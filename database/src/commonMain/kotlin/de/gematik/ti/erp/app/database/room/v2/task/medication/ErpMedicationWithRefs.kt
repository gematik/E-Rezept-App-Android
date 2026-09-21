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

import androidx.room.Dao
import androidx.room.Embedded
import androidx.room.Query
import androidx.room.Relation
import androidx.room.Transaction
import kotlinx.coroutines.flow.Flow

data class ErpMedicationWithRefs(
    @Embedded
    val medication: ErpMedicationEntity,

    @Relation(
        parentColumn = "ratioId",
        entityColumn = "ratioId"
    )
    val ratio: ErpRatioEntity?,

    @Relation(
        entity = ErpIngredientEntity::class,
        parentColumn = "medicationId",
        entityColumn = "medicationId"
    )
    val ingredients: List<ErpIngredientEntity>,

    @Relation(
        parentColumn = "medicationId",
        entityColumn = "medicationId"
    )
    val dispenses: List<ErpMedicationDispenseEntity>
)

data class ErpMedicationDispenseWithRefs(
    @Embedded
    val dispense: ErpMedicationDispenseEntity,

    @Relation(
        entity = ErpMedicationEntity::class,
        parentColumn = "dispenseId",
        entityColumn = "parentDispenseId"
    )
    val medications: List<ErpMedicationWithRefs>,

    @Relation(
        entity = ErpIngredientEntity::class,
        parentColumn = "dispenseId",
        entityColumn = "dispenseId"
    )
    val ingredients: List<ErpIngredientEntity>
)

@Dao
interface ErpMedicationWithRefsDao {
    @Transaction
    @Query("SELECT * FROM medication WHERE medicationId = :medicationId LIMIT 1")
    fun observeById(medicationId: String): Flow<ErpMedicationWithRefs?>

    @Transaction
    @Query("SELECT * FROM medication WHERE medicationId IN (:medicationIds)")
    fun observeByIds(medicationIds: List<String>): Flow<List<ErpMedicationWithRefs>>

    @Transaction
    @Query("SELECT * FROM medication")
    fun observeAll(): Flow<List<ErpMedicationWithRefs>>
}
