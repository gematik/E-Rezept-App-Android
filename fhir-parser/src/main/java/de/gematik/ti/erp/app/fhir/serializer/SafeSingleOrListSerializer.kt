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

package de.gematik.ti.erp.app.fhir.serializer

import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.buildClassSerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonDecoder
import kotlinx.serialization.json.JsonEncoder
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonObject

/** Generic "single OR list" adapter that always gives you a List<T> */
class SafeSingleOrListSerializer<T>(
    private val elementSerializer: KSerializer<T>
) : KSerializer<List<T>> {

    override val descriptor: SerialDescriptor =
        buildClassSerialDescriptor("SingleOrList<${elementSerializer.descriptor.serialName}>")

    override fun deserialize(decoder: Decoder): List<T> {
        val json = decoder as? JsonDecoder
            ?: error("SingleOrListSerializer only works with JSON")
        val element = json.decodeJsonElement()

        return when (element) {
            is JsonArray -> element.map { json.json.decodeFromJsonElement(elementSerializer, it) }
            is JsonObject -> listOf(json.json.decodeFromJsonElement(elementSerializer, element))
            is JsonNull -> emptyList()
            else -> error("Expected object or array for SingleOrList, got: $element")
        }
    }

    override fun serialize(encoder: Encoder, value: List<T>) {
        val json = encoder as? JsonEncoder
            ?: error("SingleOrListSerializer only works with JSON")

        when (value.size) {
            0 -> json.encodeJsonElement(JsonArray(emptyList()))
            1 -> json.encodeSerializableValue(elementSerializer, value.first())
            else -> json.encodeJsonElement(
                JsonArray(value.map { json.json.encodeToJsonElement(elementSerializer, it) })
            )
        }
    }
}
