/*
 * Copyright (c) 2022 gematik GmbH
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
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class ComparatorTest {
    @Test
    fun `stringValue test`() {
        assertTrue(stringValue("test123").invoke(Json.parseToJsonElement("test123")))
        assertTrue(stringValue("test123", ignoreCase = true).invoke(Json.parseToJsonElement("TEST123")))

        assertFalse(stringValue("test123").invoke(Json.parseToJsonElement("null")))
        assertFalse(stringValue("test123").invoke(Json.parseToJsonElement("{}")))
    }

    @Test
    fun `regexValue test`() {
        assertTrue(regexValue("""test\d+""".toRegex()).invoke(Json.parseToJsonElement("test123")))
        assertTrue(regexValue("""test\d+""".toRegex()).invoke(Json.parseToJsonElement("test12345678")))

        assertFalse(regexValue("""test\d+""".toRegex()).invoke(Json.parseToJsonElement("null")))
        assertFalse(regexValue("""test\d+""".toRegex()).invoke(Json.parseToJsonElement("{}")))
    }

    @Test
    fun `rangeValue floating point test`() {
        assertTrue(rangeValue(0f..1f, String::toFloatOrNull).invoke(Json.parseToJsonElement("0.0004")))
        assertTrue(rangeValue(0f..1f, String::toFloatOrNull).invoke(Json.parseToJsonElement("0.0000")))
        assertTrue(rangeValue(0f..1f, String::toFloatOrNull).invoke(Json.parseToJsonElement("1.0000")))

        assertFalse(rangeValue(0f..1f, String::toFloatOrNull).invoke(Json.parseToJsonElement("-1.0000")))
        assertFalse(rangeValue(0f..1f, String::toFloatOrNull).invoke(Json.parseToJsonElement("5")))
    }

    private fun toInt10(s: String) = s.toIntOrNull()

    @Test
    fun `rangeValue integer test`() {
        assertTrue(rangeValue(-44..66, ::toInt10).invoke(Json.parseToJsonElement("0")))
        assertTrue(rangeValue(-44..66, ::toInt10).invoke(Json.parseToJsonElement("66")))
        assertTrue(rangeValue(-44..66, ::toInt10).invoke(Json.parseToJsonElement("-44")))
        assertTrue(rangeValue(-44..66, ::toInt10).invoke(Json.parseToJsonElement("-9")))

        assertFalse(rangeValue(-44..66, ::toInt10).invoke(Json.parseToJsonElement("0.5")))
        assertFalse(rangeValue(-44..66, ::toInt10).invoke(Json.parseToJsonElement("1000")))
    }

    @Test
    fun `profileValue - profile with version`() {
        assertTrue(
            profileValue("https://base.profile/PROFILE", "1.0.1")
                .invoke(JsonPrimitive("https://base.profile/PROFILE|1.0.1"))
        )
        assertTrue(
            profileValue("https://base.profile/PROFILE", "1.0.1", "1.0.2", "1.0.3")
                .invoke(JsonPrimitive("https://base.profile/PROFILE|1.0.3"))
        )

        assertFalse(
            profileValue("https://base.profile/PROFILE", "1.0.1", "1.0.2", "1.0.3")
                .invoke(JsonPrimitive("https://base.profile/PROFILE|1.0.7"))
        )
        assertFalse(
            profileValue("https://base.profile/PROFILE")
                .invoke(JsonPrimitive("https://base.profile/PROFILE|1.0.7"))
        )
    }

    @Test
    fun `profileValue - profile without version`() {
        assertTrue(
            profileValue("https://base.profile/PROFILE")
                .invoke(JsonPrimitive("https://base.profile/PROFILE"))
        )

        assertFalse(
            profileValue("https://base.profile/PROFILE", "1.0.1", "1.0.2", "1.0.3")
                .invoke(JsonPrimitive("https://base.profile/PROFILE"))
        )
        assertFalse(
            profileValue("https://base.profile/PROFILE", "")
                .invoke(JsonPrimitive("https://base.profile/PROFILE"))
        )
    }

    @Test
    fun `or comparator test`() {
        assertTrue(
            or(
                stringValue("https://base.profile/PROFILE|1.0.3"),
                profileValue("https://base.profile/PROFILE", "1.0.1", "1.0.2", "1.0.3")
            ).invoke(JsonPrimitive("https://base.profile/PROFILE|1.0.3"))
        )

        assertTrue(
            or(
                stringValue("https://base.profile/PROFILE"),
                profileValue("https://base.profile/PROFILE", "1.0.1", "1.0.2", "1.0.3")
            ).invoke(JsonPrimitive("https://base.profile/PROFILE|1.0.3"))
        )

        assertFalse(
            or(
                stringValue("https://base.profile/"),
                profileValue("https://base.profile/PROFILE")
            ).invoke(JsonPrimitive("https://base.profile/PROFILE|1.0.3"))
        )
    }

    @Test
    fun `not comparator test`() {
        assertFalse(
            not(stringValue("https://base.profile/PROFILE|1.0.3"))
                .invoke(JsonPrimitive("https://base.profile/PROFILE|1.0.3"))
        )

        assertTrue(
            not(stringValue("abc"))
                .invoke(JsonPrimitive("abcd"))
        )
    }
}
