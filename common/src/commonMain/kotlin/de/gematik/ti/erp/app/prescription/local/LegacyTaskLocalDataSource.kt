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

package de.gematik.ti.erp.app.prescription.local

import de.gematik.ti.erp.app.database.realm.v1.task.entity.IdentifierEntityV1
import de.gematik.ti.erp.app.database.realm.v1.task.entity.IngredientEntityV1
import de.gematik.ti.erp.app.database.realm.v1.task.entity.MedicationCategoryV1
import de.gematik.ti.erp.app.database.realm.v1.task.entity.MedicationEntityV1
import de.gematik.ti.erp.app.database.realm.v1.task.entity.RatioEntityV1
import de.gematik.ti.erp.app.fhir.prescription.model.FhirTaskKbvMedicationProfileErpModel
import de.gematik.ti.erp.app.prescription.model.SyncedTaskData
import de.gematik.ti.erp.app.task.model.Quantity
import de.gematik.ti.erp.app.task.model.Ratio
import de.gematik.ti.erp.app.utils.isNotNullOrEmpty
import io.realm.kotlin.types.RealmList

@Deprecated(
    message = "Use TaskLocalDataSource instead",
    level = DeprecationLevel.WARNING
)
@Suppress("CyclomaticComplexMethod")
fun MedicationEntityV1?.toMedication(): SyncedTaskData.Medication? =
    this?.let { medication ->
        SyncedTaskData.Medication(
            identifier = medication.identifier?.toIdentifier() ?: SyncedTaskData.Identifier(),
            category = medication.medicationCategory.toMedicationCategory(),
            medicationProfile = FhirTaskKbvMedicationProfileErpModel.restoreTaskKbvMedicationProfileErpModel(
                typeString = medication.medicationProfileType,
                versionString = medication.medicationProfileVersion
            ),
            vaccine = medication.vaccine,
            text = if (medication.text.isNotNullOrEmpty()) medication.text else medication.identifier?.pzn ?: "",
            form = medication.form,
            normSizeCode = medication.normSizeCode,
            amount = medication.amount.toRatio(),
            manufacturingInstructions = medication.manufacturingInstructions,
            packaging = medication.packaging,
            ingredients = medication.ingredients.toIngredients(),
            lotNumber = medication.lotNumber,
            ingredientMedications = medication.ingredientMedications.map {
                it.toMedication()
            },
            expirationDate = medication.expirationDate
        )
    }

fun IdentifierEntityV1.toIdentifier(): SyncedTaskData.Identifier = SyncedTaskData.Identifier(
    pzn = this.pzn,
    atc = this.atc,
    ask = this.ask,
    snomed = this.snomed
)

fun RatioEntityV1?.toRatio(): Ratio? = this?.let {
    Ratio(
        numerator = it.numerator?.let { quantity ->
            Quantity(
                value = quantity.value,
                unit = quantity.unit
            )
        },
        denominator = it.denominator?.let { quantity ->
            Quantity(
                value = quantity.value,
                unit = quantity.unit
            )
        }
    )
}

private fun RealmList<IngredientEntityV1>.toIngredients(): List<SyncedTaskData.Ingredient> =
    this.map {
        SyncedTaskData.Ingredient(
            text = it.text,
            form = it.form,
            number = it.number,
            amount = it.amount,
            strength = it.strength.toRatio()
        )
    }

private fun MedicationCategoryV1?.toMedicationCategory(): SyncedTaskData.MedicationCategory =
    when (this) {
        MedicationCategoryV1.ARZNEI_UND_VERBAND_MITTEL -> SyncedTaskData.MedicationCategory.ARZNEI_UND_VERBAND_MITTEL
        MedicationCategoryV1.BTM -> SyncedTaskData.MedicationCategory.BTM
        MedicationCategoryV1.AMVV -> SyncedTaskData.MedicationCategory.AMVV
        MedicationCategoryV1.SONSTIGES -> SyncedTaskData.MedicationCategory.SONSTIGES
        else -> SyncedTaskData.MedicationCategory.UNKNOWN
    }
