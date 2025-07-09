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

import de.gematik.ti.erp.app.data.task_bundle_vers_1_2
import de.gematik.ti.erp.app.data.task_bundle_vers_1_3
import de.gematik.ti.erp.app.fhir.prescription.mocks.FhirTaskEntryParserResultTestData.taskEntryV_1_2
import de.gematik.ti.erp.app.fhir.prescription.mocks.FhirTaskEntryParserResultTestData.taskEntryV_1_3
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import org.junit.Test
import kotlin.test.assertEquals

class TaskEntryParserTest {

    val parser = TaskEntryParser()

    @Test
    fun `test parser for task bundle version 1_2`() = runTest {
        val bundle = Json.parseToJsonElement(task_bundle_vers_1_2)
        val result = parser.extract(bundle)
        assertEquals(taskEntryV_1_2, result)
    }

    @Test
    fun `test parser for task bundle version 1_3`() = runTest {
        val bundle = Json.parseToJsonElement(task_bundle_vers_1_3)
        val result = parser.extract(bundle)
        assertEquals(taskEntryV_1_3, result)
    }
}
