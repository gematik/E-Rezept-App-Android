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

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonPrimitive
import kotlin.test.Test
import kotlin.test.assertEquals

class FormatterTest {

    @Test
    fun `json object`() {
        val input = """
            {
                "test1": "someValue",
                "test2": [ 1, 2, 3, 4 ],
                "test3": {
                    "test4": "someValue"
                }
            }
        """.trimIndent()

        val expected = """
             {
                "test1": null,
                "test2": [ null, null, null, null ],
                "test3": {
                    "test4": null
                }
            }
        """.trimIndent().replace("\\s+".toRegex(), "")

        assertEquals(
            expected,
            Json.encodeToString(JsonPrimitiveAsNullSerializer, Json.parseToJsonElement(input))
        )
    }

    @Test
    fun `json array`() {
        val input = """
            [
                { "test1": "someValue" },
                { "test2": "someValue" },
                { "test3": "someValue" },
                1
            ]
        """.trimIndent()

        val expected = """
             [
                { "test1": null },
                { "test2": null },
                { "test3": null },
                null
            ]
        """.trimIndent().replace("\\s+".toRegex(), "")

        assertEquals(
            expected,
            Json.encodeToString(JsonPrimitiveAsNullSerializer, Json.parseToJsonElement(input))
        )
    }

    @Test
    fun `json primitive`() {
        val input = """
            123456
        """.trimIndent()

        val expected = """
             null
        """.trimIndent().replace("\\s+".toRegex(), "")

        assertEquals(
            expected,
            Json.encodeToString(JsonPrimitiveAsNullSerializer, Json.parseToJsonElement(input))
        )
    }

    @Test
    fun `transform all string values with another string`() {
        val input = """
            {
                "test1": "someValue",
                "test2": [ 1, 2, 3, 4 ],
                "test3": {
                    "test4": "someValue"
                }
            }
        """.trimIndent()

        val expected = """
            {
                "test1": "otherValue",
                "test2": [ 1, 2, 3, 4 ],
                "test3": {
                    "test4": "otherValue"
                }
            }
        """.trimIndent()

        assertEquals(
            Json.parseToJsonElement(expected),
            Json.parseToJsonElement(input).transformValues {
                if (it.isString) {
                    JsonPrimitive("otherValue")
                } else {
                    it
                }
            }
        )
    }
}
