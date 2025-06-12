/*
 * Copyright 2025, gematik GmbH
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
 * In case of changes by gematik find details in the "Readme" file.
 *
 * See the Licence for the specific language governing permissions and limitations under the Licence.
 */

package de.gematik.ti.erp.app.fhir.serializer

import de.gematik.ti.erp.app.fhir.dispense.model.original.FhirCodeableIngredient
import de.gematik.ti.erp.app.fhir.dispense.model.original.FhirDispenseIngredient
import de.gematik.ti.erp.app.fhir.dispense.model.original.FhirReferenceIngredient
import de.gematik.ti.erp.app.fhir.error.fhirSerializationError
import io.github.aakira.napier.Napier
import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerializationException
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonDecoder
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonEncoder
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.serializer

/**
 * Custom serializer for a heterogeneous list of `FhirDispenseIngredient` elements.
 *
 * This serializer is designed to safely handle polymorphic deserialization of FHIR `ingredient` fields
 * that may contain either:
 * - `itemReference` → [FhirReferenceIngredient]
 * - `itemCodeableConcept` → [FhirCodeableIngredient]
 *
 * During deserialization, the serializer inspects each element of the JSON array and determines
 * which concrete class it should deserialize into, based on the presence of keys.
 *
 * During serialization, it re-encodes the polymorphic types into their corresponding JSON representations.
 *
 * Example FHIR input structures:
 * ```json
 * {
 *   "itemReference": { "reference": "#some-id" }
 * }
 * {
 *   "itemCodeableConcept": {
 *     "coding": [{ "code": "value" }]
 *   }
 * }
 * ```
 *
 * This avoids using Kotlinx’s built-in `PolymorphicSerializer` and a global `SerializersModule`,
 * which would otherwise require global registration and might conflict with other serializers in use.
 *
 * @see FhirReferenceIngredient
 * @see FhirCodeableIngredient
 */
internal object SafeFhirIngredientListSerializer : KSerializer<List<FhirDispenseIngredient>> {

    override val descriptor: SerialDescriptor =
        ListSerializer(JsonElement.serializer()).descriptor

    /**
     * Deserializes a JSON array of ingredient elements into a list of [FhirDispenseIngredient].
     * Each element is matched to its type based on whether it has an `itemReference` or `itemCodeableConcept` field.
     *
     * @throws SerializationException if the decoder is not a [JsonDecoder]
     */
    override fun deserialize(decoder: Decoder): List<FhirDispenseIngredient> {
        val input = decoder as? JsonDecoder
            ?: fhirSerializationError("Expected JsonDecoder for FhirDispenseIngredient")

        val jsonArray = input.decodeJsonElement().jsonArray
        val result = mutableListOf<FhirDispenseIngredient>()

        for (element in jsonArray) {
            val obj = element.jsonObject
            val ingredient = when {
                "itemReference" in obj -> input.json.decodeFromJsonElement<FhirReferenceIngredient>(serializer(), element)
                "itemCodeableConcept" in obj -> input.json.decodeFromJsonElement<FhirCodeableIngredient>(serializer(), element)
                else -> {
                    Napier.e { "Unexpected json element $element present to FhirDispenseIngredient. Returning null" }
                    null
                }
            }

            ingredient?.let { result.add(it) }
        }

        return result
    }

    /**
     * Serializes a list of [FhirDispenseIngredient] into a JSON array.
     *
     * @throws SerializationException if the encoder is not a [JsonEncoder]
     */
    override fun serialize(encoder: Encoder, value: List<FhirDispenseIngredient>) {
        val output = encoder as? JsonEncoder
            ?: fhirSerializationError("Expected JsonEncoder for FhirDispenseIngredient")

        val jsonList = value.map {
            when (it) {
                is FhirReferenceIngredient -> output.json.encodeToJsonElement(serializer(), it)
                is FhirCodeableIngredient -> output.json.encodeToJsonElement(serializer(), it)
            }
        }

        output.encodeJsonElement(JsonArray(jsonList))
    }
}
