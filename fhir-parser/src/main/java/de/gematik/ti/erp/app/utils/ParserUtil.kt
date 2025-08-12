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

package de.gematik.ti.erp.app.utils

import de.gematik.ti.erp.app.fhir.temporal.FhirTemporal
import de.gematik.ti.erp.app.fhir.temporal.Year
import de.gematik.ti.erp.app.fhir.temporal.YearMonth
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.LocalTime
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonPrimitive

object ParserUtil {
    /**
     * Recursively searches a JsonElement for a specific URL key and returns its associated value.
     *
     * @param jsonElement The JSON element to search.
     * @param targetUrl The URL string to find.
     * @param mapKey The key to search for the URL string.
     * @param mapValue The key to search for the value associated with the URL string.
     * @return The value associated with the target URL or null if not found.
     */
    fun findValueByUrl(
        jsonElement: JsonElement,
        targetUrl: String,
        mapKey: String,
        mapValue: String
    ): String? {
        val targetParts = targetUrl.splitToParts() // Cache once

        return when (jsonElement) {
            is JsonObject -> {
                // 1. Direct match check
                jsonElement[mapKey]?.jsonPrimitive?.content?.splitToParts()
                    ?.takeIf { it == targetParts }
                    ?.let { return jsonElement[mapValue]?.jsonPrimitive?.content }

                // 2. Priority search for relevant fields
                jsonElement["extension"]?.let {
                    findValueByUrl(it, targetUrl, mapKey, mapValue)
                }?.let { return it }

                jsonElement["identifier"]?.let {
                    findValueByUrl(it, targetUrl, mapKey, mapValue)
                }?.let { return it }

                // 3. General property search (lazy evaluation via sequence)
                sequence<String?> {
                    for ((_, value) in jsonElement) {
                        yield(findValueByUrl(value, targetUrl, mapKey, mapValue))
                    }
                }.firstOrNull { it != null }
            }

            is JsonArray -> {
                // Search each element lazily using sequence
                sequence<String?> {
                    for (element in jsonElement) {
                        yield(findValueByUrl(element, targetUrl, mapKey, mapValue))
                    }
                }.filterNotNull().firstOrNull()
            }

            else -> null
        }
    }

    private fun String.splitToParts(): List<String> = buildList {
        var start = 0
        for (i in indices) {
            if (this@splitToParts[i] in "/-|.:") {
                if (start < i) add(this@splitToParts.substring(start, i))
                start = i + 1
            }
        }
        if (start < length) add(this@splitToParts.substring(start))
    }

    fun String.asFhirTemporal(): FhirTemporal {
        val parsers = listOf<(String) -> FhirTemporal>(
            { FhirTemporal.Instant(Instant.parse(it)) },
            { FhirTemporal.LocalDateTime(LocalDateTime.parse(it)) },
            { FhirTemporal.LocalDate(LocalDate.parse(it)) },
            { FhirTemporal.YearMonth(YearMonth.parse(it)) },
            { FhirTemporal.Year(Year.parse(it)) },
            { FhirTemporal.LocalTime(LocalTime.parse(it)) }
        )

        parsers.forEach { parser ->
            runCatching { return parser(this) }
        }

        error("Couldn't parse `$this`")
    }

    fun String.asNullableFhirTemporal(): FhirTemporal? {
        val parsers = listOf<(String) -> FhirTemporal>(
            { FhirTemporal.Instant(Instant.parse(it)) },
            { FhirTemporal.LocalDateTime(LocalDateTime.parse(it)) },
            { FhirTemporal.LocalDate(LocalDate.parse(it)) },
            { FhirTemporal.YearMonth(YearMonth.parse(it)) },
            { FhirTemporal.Year(Year.parse(it)) },
            { FhirTemporal.LocalTime(LocalTime.parse(it)) }
        )

        for (parser in parsers) {
            val result = runCatching { parser(this) }.getOrNull()
            if (result != null) {
                return result
            }
        }
        return null
    }

    fun String.asFhirInstant(): FhirTemporal.Instant =
        FhirTemporal.Instant(Instant.parse(this))
}
