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

package de.gematik.ti.erp.app.fhir.model

import kotlinx.serialization.json.Json
import org.junit.Test
import java.time.Instant
import java.time.LocalDate
import kotlin.test.assertEquals

class TaskMapperTest {

    @Test
    fun `extract task`() {
        val taskJson = Json.parseToJsonElement(taskJson)
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
}
