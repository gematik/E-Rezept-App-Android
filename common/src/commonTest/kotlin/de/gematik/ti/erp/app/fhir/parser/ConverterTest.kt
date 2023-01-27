/*
 * Copyright (c) 2023 gematik GmbH
 * 
 * Licensed under the EUPL, Version 1.2 or – as soon they will be approved by
 * the European Commission - subsequent versions of the EUPL (the Licence);
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at:
 * 
 *     https://joinup.ec.europa.eu/software/page/eupl
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the Licence is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Licence for the specific language governing permissions and
 * limitations under the Licence.
 * 
 */

package de.gematik.ti.erp.app.fhir.parser

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.jsonObject
import kotlin.test.Test
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.Year
import java.time.YearMonth
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertIs
import kotlin.test.assertNull

class ConverterTest {
    private val fhirInstant = listOf(
        "2015-02-07T13:28:17+02:00",
        "2015-02-07T13:28:17+00:00",
        "2015-02-07T13:28:17.243+00:00",
        "2022-01-13T15:44:15.816+00:00"
    )

    private val fhirLocalDate = listOf(
        "2015-02-03",
        "2011-03-12"
    )

    private val fhirYearMonth = listOf(
        "2015-02",
        "1999-01"
    )

    private val fhirYear = listOf(
        "2015",
        "1999"
    )

    private val fhirTime = listOf(
        "13:28:00",
        "13:28:17"
    )

    @Test
    fun `convert dates expecting type`() {
        fhirInstant.forEach {
            assertIs<Instant>(JsonPrimitive(it).asTemporalAccessor())
        }
        fhirLocalDate.forEach {
            assertIs<LocalDate>(JsonPrimitive(it).asTemporalAccessor())
        }
        fhirYearMonth.forEach {
            assertIs<YearMonth>(JsonPrimitive(it).asTemporalAccessor())
        }
        fhirYear.forEach {
            assertIs<Year>(JsonPrimitive(it).asTemporalAccessor())
        }
        fhirTime.forEach {
            assertIs<LocalTime>(JsonPrimitive(it).asTemporalAccessor())
        }
    }

    @Test
    fun `convert dates to string`() {
        assertEquals("2022-01-13T15:44:15.816Z", Instant.parse("2022-01-13T15:44:15.816+00:00").asFormattedString())
        assertEquals("2015-02-07T11:28:17Z", Instant.parse("2015-02-07T13:28:17+02:00").asFormattedString())
        assertEquals("2015-02-07T11:28:17", LocalDateTime.parse("2015-02-07T11:28:17").asFormattedString())
        assertEquals("2015-02-03", LocalDate.parse("2015-02-03").asFormattedString())
        assertEquals("2015-02", YearMonth.parse("2015-02").asFormattedString())
        assertEquals("2015", Year.parse("2015").asFormattedString())
        assertEquals("13:28:00", LocalTime.parse("13:28:00").asFormattedString())

        assertEquals(Instant.parse("2022-01-13T15:44:15.816+00:00"), "2022-01-13T15:44:15.816Z".asTemporalAccessor())
    }

    @Test
    fun `contained primitive - string`() {
        val a = Json.parseToJsonElement("""{ "foo": "bar" }""")
        val b = Json.parseToJsonElement("""{ "foo": [ "bar" ] }""")

        assertEquals("bar", a.containedString("foo"))
        assertEquals("bar", b.containedString("foo"))

        assertEquals("bar", a.jsonObject["foo"]!!.containedString())
        assertEquals("bar", b.jsonObject["foo"]!!.containedString())
    }

    @Test
    fun `contained primitive - int`() {
        val a = Json.parseToJsonElement("""{ "foo": 1 }""")
        val b = Json.parseToJsonElement("""{ "foo": [ 1 ] }""")

        assertEquals(1, a.containedInt("foo"))
        assertEquals(1, b.containedInt("foo"))

        assertEquals(1, a.jsonObject["foo"]!!.containedInt())
        assertEquals(1, b.jsonObject["foo"]!!.containedInt())
    }

    @Test
    fun `contained primitive - double`() {
        val a = Json.parseToJsonElement("""{ "foo": 1.0 }""")
        val b = Json.parseToJsonElement("""{ "foo": [ 1.0 ] }""")

        assertEquals(1.0, a.containedDouble("foo"))
        assertEquals(1.0, b.containedDouble("foo"))

        assertEquals(1.0, a.jsonObject["foo"]!!.containedDouble())
        assertEquals(1.0, b.jsonObject["foo"]!!.containedDouble())
    }

    @Test
    fun `contained primitive - string - nullable`() {
        val a = Json.parseToJsonElement("""{ "foo": [] }""")
        val b = Json.parseToJsonElement("""{ "foo": {} }""")

        assertNull(a.containedStringOrNull("foo"))
        assertNull(b.containedStringOrNull("foo"))

        assertNull(a.jsonObject["foo"]!!.containedStringOrNull())
        assertNull(b.jsonObject["foo"]!!.containedStringOrNull())

        assertFails { a.containedString("foo") }
        assertFails { b.containedString("foo") }

        assertFails { a.jsonObject["foo"]!!.containedString() }
        assertFails { b.jsonObject["foo"]!!.containedString() }
    }

    @Test
    fun `contained primitive - int - nullable`() {
        val a = Json.parseToJsonElement("""{ "foo": [] }""")
        val b = Json.parseToJsonElement("""{ "foo": {} }""")

        assertNull(a.containedIntOrNull("foo"))
        assertNull(b.containedIntOrNull("foo"))

        assertNull(a.jsonObject["foo"]!!.containedIntOrNull())
        assertNull(b.jsonObject["foo"]!!.containedIntOrNull())

        assertFails { a.containedInt("foo") }
        assertFails { b.containedInt("foo") }

        assertFails { a.jsonObject["foo"]!!.containedInt() }
        assertFails { b.jsonObject["foo"]!!.containedInt() }
    }

    @Test
    fun `contained primitive - double - nullable`() {
        val a = Json.parseToJsonElement("""{ "foo": [] }""")
        val b = Json.parseToJsonElement("""{ "foo": {} }""")

        assertNull(a.containedDoubleOrNull("foo"))
        assertNull(b.containedDoubleOrNull("foo"))

        assertNull(a.jsonObject["foo"]!!.containedDoubleOrNull())
        assertNull(b.jsonObject["foo"]!!.containedDoubleOrNull())

        assertFails { a.containedDouble("foo") }
        assertFails { b.containedDouble("foo") }

        assertFails { a.jsonObject["foo"]!!.containedDouble() }
        assertFails { b.jsonObject["foo"]!!.containedDouble() }
    }

    @Test
    fun `contained object`() {
        val a = Json.parseToJsonElement("""{ "foo": { "bar": "baz" } }""")
        val b = Json.parseToJsonElement("""{ "foo": [ { "bar": "baz" } ] }""")

        val expected = Json.parseToJsonElement("""{ "bar": "baz" }""").toString()

        assertEquals(expected, a.jsonObject["foo"]!!.containedObject().toString())
        assertEquals(expected, b.jsonObject["foo"]!!.containedObject().toString())
    }

    @Test
    fun `contained object - nullable`() {
        val a = Json.parseToJsonElement("""{ "foo": true }""")
        val b = Json.parseToJsonElement("""{ "foo": [] }""")

        assertNull(a.jsonObject["foo"]!!.containedObjectOrNull())
        assertNull(b.jsonObject["foo"]!!.containedObjectOrNull())

        assertFails { a.jsonObject["foo"]!!.containedObject() }
        assertFails { b.jsonObject["foo"]!!.containedObject() }
    }

    @Test
    fun `contained array`() {
        val a = Json.parseToJsonElement("""{ "foo": [ [ { "bar": "baz" } ] ] }""")
        val b = Json.parseToJsonElement("""{ "foo": [ { "bar": "baz" } ] }""")

        val expected = Json.parseToJsonElement("""[ { "bar": "baz" } ]""").toString()

        assertEquals(expected, a.containedArray("foo").toString())
        assertEquals(expected, b.containedArray("foo").toString())

        assertEquals(expected, a.jsonObject["foo"]!!.containedArray().toString())
        assertEquals(expected, b.jsonObject["foo"]!!.containedArray().toString())
    }

    @Test
    fun `contained array - nullable`() {
        val a = Json.parseToJsonElement("""{ "foo": {} }""")
        val b = Json.parseToJsonElement("""{ "foo": true }""")

        assertNull(a.containedArrayOrNull("foo"))
        assertNull(b.containedArrayOrNull("foo"))

        assertFails { a.jsonObject["foo"]!!.containedArray() }
        assertFails { b.jsonObject["foo"]!!.containedArray() }
    }
}
