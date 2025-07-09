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

package de.gematik.ti.erp.app.fhir.prescription.parser

import de.gematik.ti.erp.app.data.taskMetadataBundleKbvBundle
import de.gematik.ti.erp.app.data.taskMetadataBundleKbvWithDigaBundle
import de.gematik.ti.erp.app.utils.isNotNullOrEmpty
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class TaskBundleSeparationParserTest {

    val parser = TaskBundleSeparationParser()

    @Test
    fun `parse task meta_data_bundle with kbv_bundle`() = runTest {
        val bundle = Json.parseToJsonElement(taskMetadataBundleKbvBundle) as? JsonArray
        requireNotNull(bundle) { "Parsed JSON is not a JsonArray" }

        assertEquals(9, bundle.size, "Expected 9 elements in the bundle array")

        assertTrue(
            bundle.all { element ->
                val payload = parser.extract(element)
                (payload != null) && (payload.taskBundle.toString().isNotNullOrEmpty()) && (payload.kbvBundle.toString().isNotNullOrEmpty())
            },
            "All elements must contain a valid taskBundle and kbvBundle"
        )
    }

    @Test
    fun `parse device request task meta_data_bundle with kbv_bundle`() = runTest {
        val bundle = Json.parseToJsonElement(taskMetadataBundleKbvWithDigaBundle) as? JsonArray
        requireNotNull(bundle) { "Parsed JSON is not a JsonArray" }
        assertTrue(
            bundle.all { element ->
                val payload = parser.extract(element)
                (payload != null) && (payload.taskBundle.toString().isNotNullOrEmpty()) && (payload.kbvBundle.toString().isNotNullOrEmpty())
            },
            "All elements must contain a valid taskBundle and kbvBundle"
        )
    }
}
