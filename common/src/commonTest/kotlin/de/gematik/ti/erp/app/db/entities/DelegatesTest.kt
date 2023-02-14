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

package de.gematik.ti.erp.app.db.entities

import de.gematik.ti.erp.app.fhir.parser.FhirTemporal
import de.gematik.ti.erp.app.fhir.parser.Year
import de.gematik.ti.erp.app.fhir.parser.asFhirTemporal
import kotlinx.datetime.Instant
import kotlin.random.Random
import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals
import kotlin.test.assertFails
import org.bouncycastle.util.encoders.Base64

private object Clazz {
    enum class EnumA {
        A, B, C
    }

    object Clazz {
        enum class EnumB {
            A, B, C
        }
    }
}

class DelegatesTest {
    @Test
    fun `name of enum is delegated`() {
        val container = object {
            var backingProp: String = ""
            var prop: Clazz.EnumA by enumName(::backingProp)
        }

        container.prop = Clazz.EnumA.A
        assertEquals("A", container.backingProp)
        assertEquals(Clazz.EnumA.A, container.prop)

        container.prop = Clazz.EnumA.B
        assertEquals("B", container.backingProp)
        assertEquals(Clazz.EnumA.B, container.prop)

        container.prop = Clazz.EnumA.C
        assertEquals("C", container.backingProp)
        assertEquals(Clazz.EnumA.C, container.prop)
    }

    @Test
    fun `name of enum is delegated from inner class`() {
        val container = object {
            var backingProp: String = ""
            var prop: Clazz.Clazz.EnumB by enumName(::backingProp)
        }

        container.prop = Clazz.Clazz.EnumB.A
        assertEquals("A", container.backingProp)
        assertEquals(Clazz.Clazz.EnumB.A, container.prop)

        container.prop = Clazz.Clazz.EnumB.B
        assertEquals("B", container.backingProp)
        assertEquals(Clazz.Clazz.EnumB.B, container.prop)

        container.prop = Clazz.Clazz.EnumB.C
        assertEquals("C", container.backingProp)
        assertEquals(Clazz.Clazz.EnumB.C, container.prop)
    }

    @Test
    fun `name of enum is delegated from backing property`() {
        val container = object {
            var backingProp: String = ""
            var prop: Clazz.Clazz.EnumB by enumName(::backingProp)
        }

        container.backingProp = "A"
        assertEquals("A", container.backingProp)
        assertEquals(Clazz.Clazz.EnumB.A, container.prop)

        container.backingProp = "B"
        assertEquals("B", container.backingProp)
        assertEquals(Clazz.Clazz.EnumB.B, container.prop)

        container.backingProp = "C"
        assertEquals("C", container.backingProp)
        assertEquals(Clazz.Clazz.EnumB.C, container.prop)
    }

    @Test
    fun `name of enum is delegated from backing property - backing property contains invalid name`() {
        val container = object {
            var backingProp: String = ""
            var prop: Clazz.Clazz.EnumB by enumName(::backingProp)
        }

        container.backingProp = "ABC"
        assertFails {
            container.prop
        }
    }

    @Test
    fun `name of enum is delegated from backing property - backing property contains invalid name - returns default`() {
        val container = object {
            var backingProp: String = ""
            var prop: Clazz.Clazz.EnumB by enumName(::backingProp, Clazz.Clazz.EnumB.B)
        }

        container.backingProp = "ABC"
        assertEquals(Clazz.Clazz.EnumB.B, container.prop)
        assertEquals("ABC", container.backingProp)
    }

    @Test
    fun `transform byte array to base64 and back again`() {
        val origBacking = ByteArray(512).apply {
            Random.nextBytes(this)
        }

        val container = object {
            var backingProp: String = Base64.toBase64String(origBacking)
            var prop: ByteArray by byteArrayBase64(::backingProp)
        }

        assertContentEquals(origBacking, container.prop)

        val orig = ByteArray(512).apply {
            Random.nextBytes(this)
        }
        container.prop = orig.clone()

        assertContentEquals(orig, container.prop)
    }

    @Test
    fun `date time parsing`() {
        val container = object {
            var backingProp: String? = "2015-02-07T13:28:17.243+00:00"
            var prop: FhirTemporal? by temporalAccessorNullable(::backingProp)
        }

        assertEquals(Instant.parse("2015-02-07T13:28:17.243+00:00"), (container.prop as FhirTemporal.Instant).value)

        container.prop = Year.parse("2023").asFhirTemporal()

        assertEquals("2023", container.backingProp)
        assertEquals(Year.parse("2023"), (container.prop as FhirTemporal.Year).value)
    }
}
