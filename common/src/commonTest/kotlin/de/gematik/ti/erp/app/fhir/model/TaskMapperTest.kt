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

package de.gematik.ti.erp.app.fhir.model

import kotlinx.serialization.json.Json
import org.junit.Test
import java.time.Instant
import java.time.LocalDate
import kotlin.test.assertEquals

class TaskMapperTest {

    @Test
    fun `extract task version 1_1_1`() {
        val taskJson = Json.parseToJsonElement(taskJson_vers_1_1_1)
        extractTask(
            taskJson,
            process = { taskId, accessCode, lastModified, expiresOn, acceptUntil, authoredOn, status ->
                assertEquals("160.000.000.029.982.30", taskId)
                assertEquals("dd23212d35d14ccde351f9a1077f3d9508dcb8629882627ec16a22ea86144290", accessCode)
                assertEquals(Instant.parse("2022-06-09T11:57:37.923Z"), lastModified)
                assertEquals(LocalDate.parse("2022-09-09"), expiresOn)
                assertEquals(LocalDate.parse("2022-07-07"), acceptUntil)
                assertEquals(Instant.parse("2022-06-09T11:50:23.223Z"), authoredOn)
                assertEquals(TaskStatus.Completed, status)
            }
        )
    }

    @Test
    fun `extract task version 1_2`() {
        val taskJson = Json.parseToJsonElement(taskJson_vers_1_2)
        extractTask(
            taskJson,
            process = { taskId, accessCode, lastModified, expiresOn, acceptUntil, authoredOn, status ->
                assertEquals("160.000.033.491.280.78", taskId)
                assertEquals("777bea0e13cc9c42ceec14aec3ddee2263325dc2c6c699db115f58fe423607ea", accessCode)
                assertEquals(Instant.parse("2022-03-18T15:27:00Z"), lastModified)
                assertEquals(LocalDate.parse("2022-06-02"), expiresOn)
                assertEquals(LocalDate.parse("2022-04-02"), acceptUntil)
                assertEquals(Instant.parse("2022-03-18T15:26:00Z"), authoredOn)
                assertEquals(TaskStatus.Ready, status)
            }
        )
    }
}
