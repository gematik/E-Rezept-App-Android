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

package de.gematik.ti.erp.app.fhir.parser

import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonArrayBuilder
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonObjectBuilder
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.JsonTransformingSerializer
import kotlinx.serialization.json.addJsonArray
import kotlinx.serialization.json.addJsonObject
import kotlinx.serialization.json.buildJsonArray
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.putJsonArray
import kotlinx.serialization.json.putJsonObject

/**
 * Returns the original JSON without the values set to [transform].
 */
internal fun JsonElement.transformValues(transform: (JsonPrimitive) -> JsonPrimitive): JsonElement =
    when (val element = this) {
        is JsonObject -> {
            buildJsonObject {
                element.entries.forEach {
                    walkTree(it.key, it.value, transform)
                }
            }
        }
        is JsonArray -> {
            buildJsonArray {
                element.forEach {
                    walkTree(it, transform)
                }
            }
        }
        else -> JsonNull
    }

private fun JsonObjectBuilder.walkTree(key: String, element: JsonElement, transform: (JsonPrimitive) -> JsonPrimitive) {
    when (element) {
        is JsonObject ->
            putJsonObject(key) {
                element.entries.forEach {
                    walkTree(it.key, it.value, transform)
                }
            }
        is JsonArray ->
            putJsonArray(key) {
                element.forEach {
                    walkTree(it, transform)
                }
            }
        is JsonPrimitive ->
            put(key, transform(element))
        else -> error("Unknown element $element at $key")
    }
}

private fun JsonArrayBuilder.walkTree(element: JsonElement, transform: (JsonPrimitive) -> JsonPrimitive) {
    when (element) {
        is JsonObject ->
            addJsonObject {
                element.entries.forEach {
                    walkTree(it.key, it.value, transform)
                }
            }
        is JsonArray ->
            addJsonArray {
                element.forEach {
                    walkTree(it, transform)
                }
            }
        is JsonPrimitive ->
            add(transform(element))
        else -> error("Unknown element $element")
    }
}

object JsonPrimitiveAsNullSerializer : JsonTransformingSerializer<JsonElement>(JsonElement.serializer()) {
    override fun transformSerialize(element: JsonElement): JsonElement =
        element.transformValues(transform = { JsonNull })
}
