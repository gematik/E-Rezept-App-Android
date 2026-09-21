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

package de.gematik.ti.erp.app.task.model

import de.gematik.ti.erp.app.fhir.prescription.model.FhirTaskKbvMedicationProfileErpModel
import de.gematik.ti.erp.app.fhir.temporal.FhirTemporal
import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonContentPolymorphicSerializer
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.jsonObject

/**
 * copied [SyncedTaskData.Medication]
 */
@Serializable
data class MedicationErpModel(
    val category: MedicationCategory = MedicationCategory.UNKNOWN,
    val medicationProfile: FhirTaskKbvMedicationProfileErpModel? = null,
    val isVaccine: Boolean = false,
    val text: String = "",
    val form: String? = null,
    val lotNumber: String? = null,
    val expirationDate: FhirTemporal? = null, // todo maybe change?
    val identifier: Identifier = Identifier(),
    val normSizeCode: String? = null,
    val amount: RatioErpModel? = null,
    val manufacturingInstructions: String? = null,
    val packaging: String? = null,
    val ingredientMedications: List<MedicationErpModel?> = emptyList(),
    val ingredients: List<Ingredient> = emptyList()
) {
    fun name() = text.ifEmpty {
        joinIngredientNames(ingredients)
    }

    private fun joinIngredientNames(ingredients: List<Ingredient>) =
        ingredients.joinToString(", ") { ingredient ->
            ingredient.text
        }
}

/**
 * copied [SyncedTaskData.MedicationCategory]
 */
@Serializable
enum class MedicationCategory {
    ARZNEI_UND_VERBAND_MITTEL,
    BTM,
    AMVV,
    SONSTIGES,
    UNKNOWN
}

/**
 * copied [SyncedTaskData.Identifier]
 */
@Serializable
data class Identifier(
    val pzn: String? = null,
    val atc: String? = null,
    val ask: String? = null,
    val snomed: String? = null
)

/**
 * copied [SyncedTaskData.Ingredient]
 */
@Serializable
data class Ingredient(
    val text: String = "",
    val form: String? = null,
    val number: String? = null,
    val amount: String? = null,
    val strength: RatioErpModel? = null
)

// todo implement this concept again, source [PrescriptionData]
@Serializable(with = MedicationInterfaceSerializer::class)
@SerialName("MedicationInterface")
sealed interface Medication {
    @Serializable
    @SerialName("Request")
    class Request(val medicationRequest: MedicationRequestErpModel) : Medication

    @Serializable
    @SerialName("Dispense")
    class Dispense(val medicationDispense: MedicationDispenseErpModel) : Medication
}

object MedicationInterfaceSerializer : JsonContentPolymorphicSerializer<Medication>(Medication::class) {
    override fun selectDeserializer(element: JsonElement): KSerializer<out Medication> = when {
        "medicationRequest" in element.jsonObject -> Medication.Request.serializer()
        else -> Medication.Dispense.serializer()
    }
}
