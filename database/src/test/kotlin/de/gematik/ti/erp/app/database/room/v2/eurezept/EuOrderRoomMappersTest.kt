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
 */

package de.gematik.ti.erp.app.database.room.v2.eurezept

import de.gematik.ti.erp.app.database.room.v2.euredeem.EuAccessCodeEntity
import de.gematik.ti.erp.app.database.room.v2.euredeem.EuOrderEntity
import de.gematik.ti.erp.app.database.room.v2.euredeem.EuOrderWithRelations
import de.gematik.ti.erp.app.database.room.v2.euredeem.EuTaskEventEntity
import de.gematik.ti.erp.app.eurezept.model.EuAccessCodeErpModel
import de.gematik.ti.erp.app.eurezept.model.EuEventType
import de.gematik.ti.erp.app.eurezept.model.EuOrderErpModel
import de.gematik.ti.erp.app.eurezept.model.EuTaskEventErpModel
import kotlinx.datetime.Clock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue
import kotlin.time.Duration.Companion.minutes

class EuOrderRoomMappersTest {

    private val now = Clock.System.now()

    @Test
    fun testEuAccessCodeErpModelToEntityAndBack() {
        val model = EuAccessCodeErpModel(
            accessCode = "CODE123",
            countryCode = "DE",
            validUntil = now,
            createdAt = now,
            profileIdentifier = "profileId1"
        )

        val entity = model.toEntity()
        assertEquals("CODE123", entity.accessCode)
        assertEquals("DE", entity.countryCode)
        assertEquals(now, entity.validUntil)
        assertEquals(now, entity.createdAt)
        assertEquals("profileId1", entity.profileId)

        val backToModel = entity.toModel()
        assertEquals(model, backToModel)
    }

    @Test
    fun testEuOrderErpModelToEntity() {
        val model = EuOrderErpModel(
            orderId = "orderId1",
            countryCode = "DE",
            createdAt = now,
            lastModifiedAt = null,
            profileId = "profileId1",
            euAccessCode = EuAccessCodeErpModel("CODE123", "DE", now, now, "profileId1"),
            events = emptyList(),
            relatedTaskIds = listOf("task1", "task2")
        )

        val entity = model.toEntity(now)
        assertEquals("orderId1", entity.orderId)
        assertEquals("DE", entity.countryCode)
        assertEquals(now, entity.createdAt)
        assertEquals(now, entity.lastModifiedAt)
        assertEquals("profileId1", entity.profileId)
        assertEquals(listOf("task1", "task2"), entity.relatedTaskIds)
    }

    @Test
    fun testEuTaskEventErpModelToEntityAndBack() {
        val model = EuTaskEventErpModel(
            id = "eventId1",
            type = EuEventType.ACCESS_CODE_CREATED,
            taskId = "taskId1",
            createdAt = now,
            isUnread = true
        )

        val entity = model.toEntity("orderId1")
        assertEquals("eventId1", entity.id)
        assertEquals("orderId1", entity.orderId)
        assertEquals("ACCESS_CODE_CREATED", entity.type)
        assertEquals("taskId1", entity.taskId)
        assertEquals(now, entity.createdAt)
        assertTrue(entity.isUnread)

        val backToModel = entity.toModel()
        assertEquals(model, backToModel)
    }

    @Test
    fun testEuTaskEventEntityWithUnknownTypeMapsToUnknown() {
        val entity = EuTaskEventEntity(
            id = "eventId2",
            orderId = "orderId1",
            type = "SOMETHING_STRANGE",
            taskId = "taskId2",
            createdAt = now,
            isUnread = false
        )

        val model = entity.toModel()
        assertEquals(EuEventType.UNKNOWN, model.type)
    }

    @Test
    fun testEuOrderWithRelationsToModel() {
        val orderEntity = EuOrderEntity(
            orderId = "orderId1",
            countryCode = "DE",
            createdAt = now,
            lastModifiedAt = now,
            profileId = "profileId1",
            euAccessCodeCode = "CODE123",
            relatedTaskIds = listOf("task1")
        )

        val accessCodeEntity = EuAccessCodeEntity(
            accessCode = "CODE123",
            countryCode = "DE",
            validUntil = now,
            createdAt = now,
            profileId = "profileId1"
        )

        val eventEntity = EuTaskEventEntity(
            id = "eventId1",
            orderId = "orderId1",
            type = "ACCESS_CODE_CREATED",
            taskId = "task1",
            createdAt = now,
            isUnread = true
        )

        val withRelations = EuOrderWithRelations(
            order = orderEntity,
            accessCode = accessCodeEntity,
            events = listOf(eventEntity)
        )

        val model = withRelations.toModel()
        assertEquals("orderId1", model.orderId)
        assertEquals("DE", model.countryCode)
        assertEquals(now, model.createdAt)
        assertEquals(now, model.lastModifiedAt)
        assertEquals("profileId1", model.profileId)
        assertNotNull(model.euAccessCode)
        assertEquals("CODE123", model.euAccessCode?.accessCode)
        assertEquals(1, model.events.size)
        assertEquals("eventId1", model.events[0].id)
        assertEquals(EuEventType.ACCESS_CODE_CREATED, model.events[0].type)
        assertEquals(listOf("task1"), model.relatedTaskIds)
    }

    @Test
    fun testEuOrderWithRelationsToModel_sortsEventsChronologically() {
        val orderEntity = EuOrderEntity(
            orderId = "orderId2",
            countryCode = "DE",
            createdAt = now,
            lastModifiedAt = now,
            profileId = "profileId1",
            euAccessCodeCode = null,
            relatedTaskIds = listOf("taskA", "taskB")
        )

        val newerEvent = EuTaskEventEntity(
            id = "event-new",
            orderId = "orderId2",
            type = "TASK_ADDED",
            taskId = "taskB",
            createdAt = now + 1.minutes,
            isUnread = true
        )
        val olderEvent = EuTaskEventEntity(
            id = "event-old",
            orderId = "orderId2",
            type = "ACCESS_CODE_CREATED",
            taskId = "taskA",
            createdAt = now,
            isUnread = true
        )

        val withRelations = EuOrderWithRelations(
            order = orderEntity,
            accessCode = null,
            events = listOf(newerEvent, olderEvent)
        )

        val model = withRelations.toModel()
        assertEquals(listOf("event-old", "event-new"), model.events.map { it.id })
    }
}
