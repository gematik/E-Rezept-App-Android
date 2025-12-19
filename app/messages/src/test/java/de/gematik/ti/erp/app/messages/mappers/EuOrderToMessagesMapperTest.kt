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

package de.gematik.ti.erp.app.messages.mappers

import android.content.Context
import de.gematik.ti.erp.app.core.R
import de.gematik.ti.erp.app.datetime.ErpTimeFormatter
import de.gematik.ti.erp.app.eurezept.model.EuAccessCode
import de.gematik.ti.erp.app.eurezept.model.EuEventType
import de.gematik.ti.erp.app.eurezept.model.EuOrder
import de.gematik.ti.erp.app.eurezept.model.EuTaskEvent
import de.gematik.ti.erp.app.messages.ui.model.EuOrderMessageUiModel
import io.mockk.every
import io.mockk.mockk
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertFalse
import junit.framework.TestCase.assertTrue
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlin.test.Test
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds

class EuOrderToMessagesMapperTest {

    private val context = mockk<Context>(relaxed = true)

    private val mapper = EuOrderToMessagesMapper(context, ErpTimeFormatter())

    private fun mockString(id: Int, result: String) {
        every { context.getString(id, *anyVararg()) } returns result
        every { context.getString(id) } returns result
    }

    private fun event(
        id: String,
        taskId: String,
        type: EuEventType,
        time: Instant
    ) = EuTaskEvent(
        id = id,
        taskId = taskId,
        type = type,
        isUnread = true,
        createdAt = time
    )

    private fun accessCode(
        code: String,
        country: String,
        validSeconds: Long = 60
    ) = EuAccessCode(
        accessCode = code,
        countryCode = country,
        validUntil = Clock.System.now().plus(validSeconds.seconds),
        createdAt = Clock.System.now(),
        profileIdentifier = "PID"
    )

    private fun order(
        id: String,
        country: String = "BE",
        code: EuAccessCode,
        events: List<EuTaskEvent>,
        tasks: List<String>,
        ts: Instant
    ) = EuOrder(
        orderId = id,
        countryCode = country,
        createdAt = ts,
        lastModifiedAt = null,
        profileId = "PID",
        euAccessCode = code,
        events = events,
        relatedTaskIds = tasks
    )

    @Test
    fun `map returns UI for ACCESS_CODE_CREATED event`() {
        mockString(R.string.eu_messages_code_created_message_title, "CreatedTitle")
        mockString(R.string.eu_messages_code_created_message_body, "CreatedBody")
        mockString(R.string.eu_messages_code_created_but_new_generated_message_body, "Regenerated Body")

        val now = Clock.System.now()

        val ev = event("E1", "T1", EuEventType.ACCESS_CODE_CREATED, now)
        val ord = order(
            id = "O1",
            country = "PL",
            tasks = listOf("T1"),
            code = accessCode("A1B2C3", "PL"),
            events = listOf(ev),
            ts = now
        )

        val ui = mapper.map(order = ord, threadEvents = emptyList(), mappedTaskIdsToNames = { listOf("names") })

        assertEquals(1, ui.size)
        assertEquals("E1", ui[0].id)
        assert(ui[0].title.contains("CreatedTitle"))
        assertEquals("CreatedBody", ui[0].description)
    }

    @Test
    fun `map returns UI for ACCESS_CODE_REGENERATED event`() {
        mockString(R.string.eu_messages_code_created_again_message_title, "RegeneratedTitle")
        mockString(R.string.eu_messages_code_created_but_new_generated_message_body, "RegeneratedDescription")
        mockString(R.string.eu_messages_code_created_message_body, "RegeneratedDescription New")

        val now = Clock.System.now()

        val ev = event("R1", "T1", EuEventType.ACCESS_CODE_RECREATED, now)
        val ord = order(
            id = "O2",
            country = "BE",
            tasks = listOf("T1"),
            code = accessCode("XYZ123", "BE"),
            events = listOf(ev),
            ts = now
        )

        val ui = mapper.map(
            order = ord,
            threadEvents = emptyList(),
            mappedTaskIdsToNames = { listOf("names") }
        )

        assertEquals(1, ui.size)
        assertEquals("R1", ui[0].id)
        assertEquals("RegeneratedTitle", ui[0].title)
        assertEquals("RegeneratedDescription New", ui[0].description)
    }

    @Test
    fun `map returns TWO UI models for two regenerated events 2 minutes apart`() {
        mockString(R.string.eu_messages_code_created_again_message_title, "RegeneratedTitle")
        mockString(R.string.eu_messages_code_created_but_new_generated_message_body, "RegeneratedDescription")
        mockString(R.string.eu_messages_code_created_message_body, "RegeneratedDescription New")

        val now = Clock.System.now()

        // Event 1 (at now)
        val ev1 = event(
            id = "R1",
            taskId = "T1",
            type = EuEventType.ACCESS_CODE_RECREATED,
            time = now
        )

        // Event 2 (2 minutes later → must NOT merge)
        val ev2 = event(
            id = "R2",
            taskId = "T2",
            type = EuEventType.ACCESS_CODE_RECREATED,
            time = now.plus(2.minutes)
        )

        val ord = order(
            id = "O1",
            country = "BE",
            tasks = listOf("T1", "T2"),
            code = accessCode("XYZ123", "BE"),
            events = listOf(ev1, ev2),
            ts = now
        )

        val ui = mapper.map(
            order = ord,
            threadEvents = emptyList(),
            mappedTaskIdsToNames = { listOf("names") }
        )

        // They must NOT be merged → expect 2
        assertEquals(2, ui.size)

        // latest event becomes the first item in the list
        assertEquals("R2", ui[0].id)
        assertEquals("R1", ui[1].id)

        assertEquals("RegeneratedTitle", ui[0].title)
        assertEquals("RegeneratedTitle", ui[1].title)
        assertEquals("RegeneratedDescription New", ui[0].description)
        assertEquals("RegeneratedDescription", ui[1].description)
    }

    @Test
    fun `map returns UI model for TASK_ADDED event`() {
        mockString(R.string.eu_messages_prescription_added_title, "TaskAddedTitle")
        mockString(R.string.eu_messages_prescription_added_body, "TaskAddedDescription")

        val now = Clock.System.now()

        val ev = event(
            id = "ADD1",
            taskId = "T123",
            type = EuEventType.TASK_ADDED,
            time = now
        )

        val ord = order(
            id = "ORDER1",
            country = "BE",
            tasks = listOf("T123"),
            code = accessCode("ABC123", "BE"),
            events = listOf(ev),
            ts = now
        )

        val ui = mapper.map(
            order = ord,
            threadEvents = emptyList(),
            mappedTaskIdsToNames = { listOf("Painkiller") } // any example value
        )

        // Expect exactly 1 UI model
        assertEquals(1, ui.size)

        val item = ui[0] as EuOrderMessageUiModel.TaskAdded

        // event identity
        assertEquals("ADD1", item.id)
        assertEquals("ORDER1", item.orderId)

        // access code propagation
        assertEquals("ABC123", item.accessCode)

        // timestamps
        assertEquals(ev.createdAt, item.timestamp)

        // merged task IDs
        assertEquals(listOf("T123"), item.taskIds)

        // prescription names (you passed one)
        assertEquals(listOf("Painkiller"), item.prescriptionNames)

        // flags & state
        assertEquals("BE", item.countryCode)
        assertEquals(true, item.isUnread)
        assertEquals(false, item.showButtons)

        // title + description
        assertEquals("TaskAddedTitle", item.title)
        assertEquals("TaskAddedDescription", item.description)
    }

    @Test
    fun `map returns UI model for TASK_REMOVED event`() {
        mockString(R.string.eu_messages_prescription_removed_title, "TaskRemovedTitle")
        mockString(R.string.eu_messages_prescription_removed_body, "TaskRemovedDescription")

        val now = Clock.System.now()

        val ev = event(
            id = "REM1",
            taskId = "T999",
            type = EuEventType.TASK_REMOVED,
            time = now
        )

        val ord = order(
            id = "ORDER2",
            country = "BE",
            tasks = listOf("T999"),
            code = accessCode("XYZ789", "BE"),
            events = listOf(ev),
            ts = now
        )

        val ui = mapper.map(
            order = ord,
            threadEvents = emptyList(),
            mappedTaskIdsToNames = { listOf("Painkiller X") }
        )

        // Expect exactly 1 UI model
        assertEquals(1, ui.size)

        val item = ui[0] as EuOrderMessageUiModel.TaskRemoved

        // Core identity
        assertEquals("REM1", item.id)
        assertEquals("ORDER2", item.orderId)

        // Access code propagation
        assertEquals("XYZ789", item.accessCode)

        // Timestamp + formatted time string
        assertEquals(ev.createdAt, item.timestamp)

        // Task IDs (merged list)
        assertEquals(listOf("T999"), item.taskIds)

        // Prescription names
        assertEquals(listOf("Painkiller X"), item.prescriptionNames)

        // Flags/state
        assertEquals("BE", item.countryCode)
        assertTrue(item.isUnread)
        assertFalse(item.showButtons)

        // Title + description
        assertEquals("TaskRemovedTitle", item.title)
        assertEquals("TaskRemovedDescription", item.description)
    }
}
