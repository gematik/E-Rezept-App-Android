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

import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.buildClassSerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

internal object SafeFhirAddressLineSerializer : KSerializer<Map<String, String>> {
    override val descriptor: SerialDescriptor =
        buildClassSerialDescriptor("SafeFhirAddressLineSerializer")

    override fun serialize(encoder: Encoder, value: Map<String, String>) {
        // Convert the map back to JSON format
        val jsonObject = JsonObject(value.mapValues { JsonPrimitive(it.value) })
        encoder.encodeSerializableValue(JsonObject.serializer(), jsonObject)
    }

    /**
     * Deserializes a FHIR `_line` field into a `Map<String, String>`, where keys are extracted
     * from extension URLs (e.g., `houseNumber`, `streetName`) and values are their respective `valueString`.
     *
     * This method ensures:
     * - If `_line` is **missing**, an **empty map** is returned.
     * - If `_line` exists but has no extensions, an **empty map** is returned.
     * - If `_line` contains extensions, their URLs are mapped to simplified keys using regex.
     * - If a URL does not match known patterns, the **full URL** is used as the key.
     *
     * ### 📥 Example JSON Input:
     * ```json
     * "_line": [
     *   {
     *     "extension": [
     *       {
     *         "url": "http://hl7.org/fhir/StructureDefinition/iso21090-ADXP-houseNumber",
     *         "valueString": "155"
     *       },
     *       {
     *         "url": "http://hl7.org/fhir/StructureDefinition/iso21090-ADXP-streetName",
     *         "valueString": "Siegburger Str."
     *       }
     *     ]
     *   }
     * ]
     * ```
     *
     * ### 📤 Output (Deserialized Map):
     * ```kotlin
     * {
     *   "houseNumber" to "155",
     *   "streetName" to "Siegburger Str."
     * }
     * ```
     *
     * ### 📥 Example JSON (Missing `_line`):
     * ```json
     * {
     *   "type": "both",
     *   "city": "Köln",
     *   "postalCode": "51105",
     *   "country": "D"
     * }
     * ```
     *
     * ### 📤 Output (Empty Map):
     * ```kotlin
     * {}
     * ```
     *
     * @param decoder The [Decoder] that reads the input JSON.
     * @return A map where the **keys** are extracted field names (e.g., `houseNumber`, `streetName`),
     * and the **values** are their corresponding `valueString` values.
     */
    override fun deserialize(decoder: Decoder): Map<String, String> {
        val jsonElement = decoder.decodeSerializableValue(JsonElement.serializer())

        // Extract `_line` array safely
        val extensionArray = jsonElement.jsonArrayOrNull ?: return emptyMap()

        return extensionArray.flatMap { element ->
            val extensions = element.jsonObject["extension"]?.jsonArrayOrNull ?: return@flatMap emptyList()

            extensions.mapNotNull { ext ->
                val url = ext.jsonObject["url"]?.jsonPrimitive?.content ?: return@mapNotNull null
                val value = ext.jsonObject["valueString"]?.jsonPrimitive?.content ?: return@mapNotNull null

                val key = extractKeyFromUrl(url)
                key to value
            }
        }.toMap()
    }

    /**
     * Extracts a simplified key from the given FHIR extension URL.
     * Uses regex to map known address fields (e.g., `houseNumber`, `streetName`).
     * If no match is found, returns the full URL.
     */
    private fun extractKeyFromUrl(url: String): String {
        val regex = Regex("iso21090-ADXP-(\\w+)")
        return regex.find(url)?.groupValues?.get(1) ?: url // Default to full URL if no match
    }

    // Helper extension to handle missing or null arrays
    private val JsonElement.jsonArrayOrNull: JsonArray?
        get() = this as? JsonArray
}
