/*
 * Copyright 2024, gematik GmbH
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

import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.contentOrNull

typealias JsonComparator = (value: JsonElement) -> Boolean

internal class OrComparator(private val comparators: List<JsonComparator>) : JsonComparator {
    override fun invoke(value: JsonElement): Boolean =
        comparators.any {
            it(value)
        }

    override fun toString(): String {
        return "OrComparator(${comparators.joinToString()})"
    }
}

fun or(vararg comparator: JsonComparator): JsonComparator =
    OrComparator(comparator.toList())

internal class NotComparator(private val comparator: JsonComparator) : JsonComparator {
    override fun invoke(value: JsonElement): Boolean =
        !comparator(value)

    override fun toString(): String {
        return "NotComparator($comparator)"
    }
}

fun not(comparator: JsonComparator): JsonComparator =
    NotComparator(comparator)

internal class RegexJsonComparator(private val regex: Regex) : JsonComparator {
    override fun invoke(value: JsonElement): Boolean =
        if (value is JsonPrimitive) {
            value.contentOrNull?.matches(regex) ?: false
        } else {
            false
        }

    override fun toString(): String {
        return "RegexJsonComparator($regex)"
    }
}

fun regexValue(regex: Regex): JsonComparator =
    RegexJsonComparator(regex)

internal class StringJsonComparator(private val otherValue: String, private val ignoreCase: Boolean) : JsonComparator {
    override fun invoke(value: JsonElement): Boolean =
        value is JsonPrimitive && value.contentOrNull.equals(otherValue, ignoreCase)

    override fun toString(): String {
        return "StringJsonComparator($otherValue)"
    }
}

fun stringValue(value: String, ignoreCase: Boolean = false): JsonComparator =
    StringJsonComparator(value, ignoreCase)

internal class RangeJsonComparator<T : Comparable<T>>(
    private val range: ClosedRange<T>,
    private val converter: (String) -> T?
) : JsonComparator {
    override fun invoke(value: JsonElement): Boolean =
        (value as? JsonPrimitive)
            ?.contentOrNull
            ?.let { converter(it) }
            ?.let { it in range }
            ?: false

    override fun toString(): String {
        return "RangeJsonComparator($range)"
    }
}

public fun <T : Comparable<T>> rangeValue(range: ClosedRange<T>, converter: (String) -> T?): JsonComparator =
    RangeJsonComparator(range, converter)

internal class ProfileStringComparator(
    private val base: String,
    private val versions: Array<out String>
) : JsonComparator {
    override fun invoke(value: JsonElement): Boolean =
        (value as? JsonPrimitive)
            ?.contentOrNull
            ?.let {
                val path = it.split('|', limit = 2)
                when {
                    path.size == 2 && versions.isNotEmpty() -> {
                        val matchesBasePath = path[0] == base
                        val matchesVersion = versions.any { v -> path[1] == v }
                        matchesBasePath && matchesVersion
                    }
                    path.size == 1 && versions.isEmpty() -> {
                        path[0] == base
                    }
                    else -> false
                }
            }
            ?: false

    override fun toString(): String {
        return "ProfileStringComparator($base|(${versions.joinToString("|")}))"
    }
}

fun profileValue(base: String, vararg versions: String): JsonComparator =
    ProfileStringComparator(base, versions)

fun JsonElement.isProfileValue(base: String, vararg versions: String) =
    profileValue(base, *versions).invoke(this)
