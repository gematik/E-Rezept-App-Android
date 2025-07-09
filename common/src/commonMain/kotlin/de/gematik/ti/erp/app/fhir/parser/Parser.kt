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

package de.gematik.ti.erp.app.fhir.parser

import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive

private const val PathDelimiter = '.'

internal fun JsonElement.walk(path: List<String>): Iterator<JsonElement> =
    iterator {
        when (this@walk) {
            is JsonObject -> walk(this@walk, path)
            is JsonArray -> walk(this@walk, path)
            else -> {}
        }
    }

internal suspend fun SequenceScope<JsonElement>.walk(obj: JsonObject, path: List<String>) {
    val prefix = if (path.isNotEmpty()) path.first() else ""
    val suffix = if (path.isNotEmpty()) path.subList(1, path.size) else emptyList()

    if (prefix.isEmpty()) {
        // we are at the right element
        yield(obj)
    } else {
        when (val v = obj[prefix]) {
            is JsonObject -> walk(v, suffix)
            is JsonArray -> walk(v, suffix)
            is JsonPrimitive ->
                if (suffix.isEmpty()) {
                    // prefix matches primitive value and remaining path is empty
                    yield(v)
                }
            else -> {}
        }
    }
}

internal suspend fun SequenceScope<JsonElement>.walk(arr: JsonArray, path: List<String>) {
    arr.forEach {
        when (it) {
            is JsonObject -> walk(it, path)
            is JsonPrimitive -> yield(it)
            else -> {}
        }
    }
}

fun JsonElement.findAll(base: List<String>): Sequence<JsonElement> =
    walk(base)
        .asSequence()

fun JsonElement.findAll(base: String): Sequence<JsonElement> =
    findAll(splitPath(base))

fun Sequence<JsonElement>.findAll(base: List<String>): Sequence<JsonElement> {
    return asSequence().flatMap {
        it.findAll(base)
    }
}

fun Sequence<JsonElement>.findAll(base: String): Sequence<JsonElement> {
    val splitBase = splitPath(base)
    return asSequence().flatMap {
        it.findAll(splitBase)
    }
}

fun Sequence<JsonElement>.filterWith(relative: List<String>, matches: JsonComparator): Sequence<JsonElement> =
    filter {
        it.findAll(relative).any { el ->
            matches(el)
        }
    }

fun Sequence<JsonElement>.filterWith(relative: String, matches: JsonComparator): Sequence<JsonElement> =
    filterWith(splitPath(relative), matches)

private fun splitPath(path: String): List<String> {
    require(!path.startsWith('.')) { "A path can't start with a dot." }
    require(!path.endsWith('.')) { "A path can't end with a dot." }
    return path.split(PathDelimiter)
}
