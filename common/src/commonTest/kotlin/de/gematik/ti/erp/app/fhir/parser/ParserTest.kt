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

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertIs

class ParserTest {
    private val jsonBundle: JsonElement
        get() = Json.decodeFromString(testBundle)

    @Test
    fun `find the name of the patient resource matching the given profile `() {
        val result = jsonBundle
            .findAll("entry.resource.entry.resource")
            .filterWith(
                "meta.profile",
                stringValue("https://fhir.kbv.de/StructureDefinition/KBV_PR_FOR_Patient|1.0.3")
            )
            .findAll("name")
            .toList()

        assertEquals(1, result.size)
        assertIs<JsonObject>(result.first())
        assertEquals("Graf Freiherr von Schaumberg", (result.first() as JsonObject)["family"]!!.containedString())
        assertEquals("Karl-Friederich", (result.first() as JsonObject)["given"]!!.containedString())
    }

    @Test
    fun `find all resources within the bundle`() {
        val result = jsonBundle
            .findAll("entry.resource")
            .filterWith(
                "meta.profile",
                stringValue("https://fhir.kbv.de/StructureDefinition/KBV_PR_ERP_Bundle|1.0.2")
            )
            .findAll("entry.resource.meta.profile")
            .toList()

        assertEquals(7, result.size)
        assertEquals(
            "https://fhir.kbv.de/StructureDefinition/KBV_PR_ERP_Composition|1.0.2",
            result[0].containedString()
        )
        assertEquals(
            "https://fhir.kbv.de/StructureDefinition/KBV_PR_ERP_Prescription|1.0.2",
            result[1].containedString()
        )
        assertEquals(
            "https://fhir.kbv.de/StructureDefinition/KBV_PR_ERP_Medication_PZN|1.0.2",
            result[2].containedString()
        )
        assertEquals(
            "https://fhir.kbv.de/StructureDefinition/KBV_PR_FOR_Patient|1.0.3",
            result[3].containedString()
        )
        assertEquals(
            "https://fhir.kbv.de/StructureDefinition/KBV_PR_FOR_Practitioner|1.0.3",
            result[4].containedString()
        )
        assertEquals(
            "https://fhir.kbv.de/StructureDefinition/KBV_PR_FOR_Organization|1.0.3",
            result[5].containedString()
        )
        assertEquals(
            "https://fhir.kbv.de/StructureDefinition/KBV_PR_FOR_Coverage|1.0.3",
            result[6].containedString()
        )
    }

    @Test
    fun `find entry to primary resource`() {
        val result = jsonBundle
            .findAll("")
            .toList()

        assertEquals(1, result.size)
        assertEquals(
            "collection",
            (result.first() as JsonObject)["type"]!!.containedString()
        )
    }

    @Test
    fun `base path with trailing dot throws exception`() {
        assertFails {
            jsonBundle
                .findAll("entry.")
                .toList()
        }
    }

    @Test
    fun `base path with dots throws exception`() {
        assertFails {
            jsonBundle
                .findAll("..")
                .toList()
        }
        assertFails {
            jsonBundle
                .findAll(".")
                .toList()
        }
    }

    @Test
    fun `base path leading dot throws exception`() {
        assertFails {
            jsonBundle
                .findAll(".entry")
                .toList()
        }
    }
}
