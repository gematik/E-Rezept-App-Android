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

import io.github.aakira.napier.Napier
import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

// Custom serializer for extracting Task ID from FHIR basedOn reference

object TaskIdSerializer : KSerializer<String?> {

    override val descriptor: SerialDescriptor =
        PrimitiveSerialDescriptor("TaskId", PrimitiveKind.STRING)

    override fun serialize(encoder: Encoder, value: String?) {
        value?.let { encoder.encodeString(it) } ?: encoder.encodeNull()
    }

    override fun deserialize(decoder: Decoder): String? {
        return try {
            val jsonElement = decoder.decodeSerializableValue(JsonElement.serializer())

            if (jsonElement !is JsonArray || jsonElement.isEmpty()) {
                return null
            }

            val reference = jsonElement.firstOrNull()
                ?.jsonObject
                ?.get("reference")
                ?.jsonPrimitive
                ?.content ?: return null

            if (!reference.startsWith("Task/")) {
                return null
            }

            val afterTask = reference.substringAfter("Task/")

            val dollarIndex = if (afterTask.contains("$")) afterTask.indexOf("$") else afterTask.length
            val slashIndex = if (afterTask.contains("/")) afterTask.indexOf("/") else afterTask.length

            val endIndex = minOf(dollarIndex, slashIndex)

            afterTask.substring(0, endIndex)
        } catch (e: Exception) {
            Napier.e("Error extracting task ID: ${e.message}")
            null
        }
    }
}
