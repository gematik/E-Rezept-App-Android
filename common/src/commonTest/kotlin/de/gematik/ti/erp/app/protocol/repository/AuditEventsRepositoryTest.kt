/*
 * Copyright (c) 2024 gematik GmbH
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

package de.gematik.ti.erp.app.protocol.repository

import de.gematik.ti.erp.app.CoroutineTestRule
import de.gematik.ti.erp.app.fhir.model.ResourceBasePath
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.Instant
import kotlinx.serialization.json.Json
import org.junit.Rule
import java.io.File
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

private val testBundle by lazy {
    File("$ResourceBasePath/audit_events_bundle.json").readText()
}
private val testAuditEventVersion12 by lazy {
    File("$ResourceBasePath/audit_events_bundle_version_1_2.json").readText()
}

@get:Rule
val coroutineRule = CoroutineTestRule()

class AuditEventsRepositoryTest {

    @MockK
    lateinit var remoteDataSource: AuditEventRemoteDataSource
    private lateinit var auditEventsRepository: AuditEventsRepository
    private class AuditEvent(
        val id: String,
        val taskId: String?,
        val description: String,
        val timestamp: Instant
    )

    private val events = mapOf(
        0 to AuditEvent(
            id = "01eb7f56-6820-a140-abdb-34aa9f2ab6ea",
            taskId = null,
            description = "Zacharias Zebra hat eine Liste mit Medikament-Informationen heruntergeladen.",
            timestamp = Instant.parse("2022-01-13T15:44:15.816+00:00")
        ),
        2 to AuditEvent(
            id = "01eb7f56-75dc-6850-9729-d94c0839ab3b",
            taskId = "169.000.000.000.026.84",
            description = "Praxis Rainer Graf d' AgóstinoTEST-ONLY hat das Rezept" +
                " mit der ID 169.000.000.000.026.84 eingestellt.",
            timestamp = Instant.parse("2022-01-13T15:48:06.226+00:00")
        ),
        7 to AuditEvent(
            id = "01eb7f56-862a-e830-e470-120f0137c54e",
            taskId = "169.000.000.000.026.84",
            description = "Zacharias Zebra hat das Rezept mit der ID 169.000.000.000.026.84 heruntergeladen.",
            timestamp = Instant.parse("2022-01-13T15:52:39.806+00:00")
        )
    )

    private val auditEventsVersion12 = mapOf(
        0 to AuditEvent(
            id = "9361863d-fec0-4ba9-8776-7905cf1b0cfa",
            taskId = "160.123.456.789.123.58",
            description = "Praxis Dr. Müller, Bahnhofstr. 78 hat ein E-Rezept 160.123.456.789.123.58 eingestellt",
            timestamp = Instant.parse("2022-04-27T08:04:27.434Z")
        )
    )

    @BeforeTest
    fun setUp() {
        MockKAnnotations.init(this)
        auditEventsRepository = AuditEventsRepository(remoteDataSource)
    }

    @Test
    fun `parse audit events`() {
        val auditEventsJson = Json.parseToJsonElement(testBundle)
        coEvery { remoteDataSource.getAuditEvents(any(), any(), any()) } coAnswers {
            Result.success(
                auditEventsJson
            )
        }
        runTest {
            val auditEventMappingResult = auditEventsRepository
                .downloadAuditEvents("", null, null).getOrThrow()
            assertEquals(50, auditEventMappingResult.auditEvents.size)

            assertEquals(events[0]?.id, auditEventMappingResult.auditEvents[0].auditId)
            assertEquals(events[0]?.taskId, auditEventMappingResult.auditEvents[0].taskId)
            assertEquals(events[0]?.description, auditEventMappingResult.auditEvents[0].description)
            assertEquals(events[0]?.timestamp, auditEventMappingResult.auditEvents[0].timestamp)

            assertEquals(events[2]?.id, auditEventMappingResult.auditEvents[2].auditId)
            assertEquals(events[2]?.taskId, auditEventMappingResult.auditEvents[2].taskId)
            assertEquals(events[2]?.description, auditEventMappingResult.auditEvents[2].description)
            assertEquals(events[2]?.timestamp, auditEventMappingResult.auditEvents[2].timestamp)

            assertEquals(events[7]?.id, auditEventMappingResult.auditEvents[7].auditId)
            assertEquals(events[7]?.taskId, auditEventMappingResult.auditEvents[7].taskId)
            assertEquals(events[7]?.description, auditEventMappingResult.auditEvents[7].description)
            assertEquals(events[7]?.timestamp, auditEventMappingResult.auditEvents[7].timestamp)
        }
    }

    @Test
    fun `parse audit events version 1_2`() {
        val auditEventsJson = Json.parseToJsonElement(testAuditEventVersion12)
        coEvery { remoteDataSource.getAuditEvents(any(), any(), any()) } coAnswers {
            Result.success(auditEventsJson)
        }

        runTest {
            val auditEventMappingResult = auditEventsRepository
                .downloadAuditEvents("", null, null).getOrThrow()

            assertEquals(1, auditEventMappingResult.auditEvents.size)

            assertEquals(auditEventsVersion12[0]?.id, auditEventMappingResult.auditEvents[0].auditId)
            assertEquals(auditEventsVersion12[0]?.taskId, auditEventMappingResult.auditEvents[0].taskId)
            assertEquals(auditEventsVersion12[0]?.description, auditEventMappingResult.auditEvents[0].description)
            assertEquals(auditEventsVersion12[0]?.timestamp, auditEventMappingResult.auditEvents[0].timestamp)
        }
    }
}
