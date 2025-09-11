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

package de.gematik.ti.erp.app.fhir.pkv.parser

import de.gematik.ti.erp.app.data.TestDataGenerator
import de.gematik.ti.erp.app.data.pkvChargeItemBundleEntry
import de.gematik.ti.erp.app.data.pkvChargeItemBundle_1_4_Entry
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.Instant
import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals

class ChargeItemBundleEntryParserTest {
    private val parser = ChargeItemBundleEntryParser()
    private val generator = TestDataGenerator()

    @Test
    fun `test task id extraction from entry bundle on first call`() = runTest {
        val bundle = Json.parseToJsonElement(pkvChargeItemBundleEntry)
        val result = parser.extract(bundle)
        assertEquals(2, result?.bundleTotal)
        assertEquals(
            listOf("200.086.824.605.539.20", "200.086.824.605.539.20"),
            result?.chargeItemEntries?.map { it.taskId }
        )
    }

    @Test
    fun `test task id extraction from entry bundle on first call version 1_4`() = runTest {
        val bundle = generator.createEntryBundleForChargeItems(
            resource = Json.parseToJsonElement(pkvChargeItemBundle_1_4_Entry),
            fullUrl = "http://hapi.fhir.org/baseR4/ChargeItem/abc825bc-bc30-45f8-b109-1b343fff5c45",
            id = "200e3c55-b154-4335-a0ec-65addd39a3b6",
            lastUpdated = Instant.parse("2021-09-02T11:38:42.557Z")
        )
        val result = parser.extract(bundle)
        assertEquals(1, result?.bundleTotal)
        assertEquals(
            listOf("200.000.000.000.000.01"),
            result?.chargeItemEntries?.map { it.taskId }
        )
    }
}
