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

package de.gematik.ti.erp.app.fhir.model

import de.gematik.ti.erp.app.fhir.temporal.asFhirTemporal
import de.gematik.ti.erp.app.fhir.temporal.toFhirTemporal
import de.gematik.ti.erp.app.task.model.TaskStatus
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals

class TaskDataMapperTest {

    @Test
    fun `extract task version 1_2`() {
        val taskJson = Json.parseToJsonElement(taskJson_vers_1_2)
        extractTask(
            taskJson,
            process = { taskId, accessCode, lastModified, expiresOn,
                acceptUntil, authoredOn, status, lastMedicationDispense ->

                assertEquals("160.000.033.491.280.78", taskId)
                assertEquals("777bea0e13cc9c42ceec14aec3ddee2263325dc2c6c699db115f58fe423607ea", accessCode)
                assertEquals(Instant.parse("2022-03-18T15:29:00Z"), lastModified.toInstant())
                assertEquals(LocalDate.parse("2022-06-02").asFhirTemporal(), expiresOn)
                assertEquals(LocalDate.parse("2022-04-02").asFhirTemporal(), acceptUntil)
                assertEquals(Instant.parse("2022-03-18T15:26:00Z"), authoredOn.toInstant())
                assertEquals(TaskStatus.Completed, status)
                assertEquals(null, lastMedicationDispense)
            }
        )
    }

    @Test
    fun `extract task version 1_3`() {
        val taskJson = Json.parseToJsonElement(taskJson_vers_1_3)
        extractTask(
            taskJson,
            process = { taskId, accessCode, lastModified, expiresOn, acceptUntil,
                authoredOn, status, lastMedicationDispense ->
                assertEquals("160.123.456.789.123.61", taskId)
                assertEquals("777bea0e13cc9c42ceec14aec3ddee2263325dc2c6c699db115f58fe423607bl", accessCode)
                assertEquals(Instant.parse("2020-03-02T08:45:05+00:00"), lastModified.toInstant())
                assertEquals(LocalDate.parse("2020-06-02").asFhirTemporal(), expiresOn)
                assertEquals(LocalDate.parse("2020-04-01").asFhirTemporal(), acceptUntil)
                assertEquals(Instant.parse("2020-03-02T08:25:05+00:00"), authoredOn.toInstant())
                assertEquals(TaskStatus.InProgress, status)
                assertEquals(Instant.parse("2020-04-01T15:37:17Z"), lastMedicationDispense?.toInstant())
            }
        )
    }

    @Test
    fun `extract task id and current status from bundle version 1_2`() {
        val taskJson = Json.parseToJsonElement(task_bundle_version_1_2)
        val (bundleTotal, taskdata) = extractActualTaskData(taskJson)
        assertEquals(1, bundleTotal)

        val taskId = taskdata[0].taskId
        val status = taskdata[0].status
        val lastModified = taskdata[0].lastModified

        assertEquals("160.000.033.491.280.78", taskId)
        assertEquals(TaskStatus.Ready, status)
        assertEquals("2022-03-18T15:27:00Z".toFhirTemporal(), lastModified)
    }

    @Test
    fun `extract task id's and current status version 1_3`() {
        val taskJson = Json.parseToJsonElement(task_bundle_version_1_3)
        val (bundleTotal, taskdata) = extractActualTaskData(taskJson)
        assertEquals(3, bundleTotal)
        assertEquals("160.123.456.789.123.58", taskdata[0].taskId)
        assertEquals("160.123.456.789.123.78", taskdata[1].taskId)
        assertEquals("160.123.456.789.123.61", taskdata[2].taskId)
    }
}
