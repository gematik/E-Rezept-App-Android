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

package de.gematik.ti.erp.app.pushnotifications.domain.model

import de.gematik.ti.erp.app.core.R
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNull

class IncomingPushNotificationMapperTest {
    private val mapper = IncomingPushNotificationMapper()

    @Test
    fun `complete encrypted data is parsed independently of Firebase classes`() {
        val result = mapper.parse(
            mapOf(
                IncomingPushNotificationMapper.KEY_CIPHERTEXT to "cipher",
                IncomingPushNotificationMapper.KEY_TIME_ENCRYPTED to "2026-07",
                IncomingPushNotificationMapper.KEY_IDENTIFIER to "key-id"
            )
        )

        assertEquals(
            IncomingPushNotification.Encrypted("cipher", "2026-07", "key-id"),
            result
        )
    }

    @Test
    fun `incomplete encrypted data is rejected instead of treated as plaintext`() {
        val result = mapper.parse(mapOf(IncomingPushNotificationMapper.KEY_CIPHERTEXT to "cipher"))

        assertIs<IncomingPushNotification.Rejected>(result)
        assertEquals("Encrypted push payload is incomplete.", result.reason)
    }

    @Test
    fun `message without encrypted data is rejected`() {
        val result = mapper.parse(emptyMap())

        assertIs<IncomingPushNotification.Rejected>(result)
        assertEquals("Encrypted push payload is missing.", result.reason)
    }

    @Test
    fun `blank encrypted field is rejected`() {
        val result = mapper.parse(
            mapOf(
                IncomingPushNotificationMapper.KEY_CIPHERTEXT to "",
                IncomingPushNotificationMapper.KEY_TIME_ENCRYPTED to "2026-07",
                IncomingPushNotificationMapper.KEY_IDENTIFIER to "key-id"
            )
        )

        assertIs<IncomingPushNotification.Rejected>(result)
        assertEquals("Encrypted push payload is incomplete.", result.reason)
    }

    @Test
    fun `erp task activate maps to new prescription banner`() {
        val content = mapper.mapDecrypted(
            PushNotificationPayload(
                channelId = IncomingPushNotificationMapper.CHANNEL_TASK_ACTIVATE,
                identifier = "task-123",
                identifierType = "TaskId",
                rawPayload = "{}"
            )
        )

        assertEquals(
            PushNotificationContent(
                title = R.string.push_notification_banner_activate_title,
                body = R.string.push_notification_banner_activate_body,
                groupKey = "task-123"
            ),
            content
        )
    }

    @Test
    fun `erp communication new maps to new pharmacy message banner`() {
        val content = mapper.mapDecrypted(
            PushNotificationPayload(
                channelId = IncomingPushNotificationMapper.CHANNEL_COMMUNICATION_NEW,
                identifier = "comm-123",
                identifierType = "CommunicationId",
                rawPayload = "{}"
            )
        )

        assertEquals(
            PushNotificationContent(
                title = R.string.push_notification_banner_communication_new_title,
                body = R.string.push_notification_banner_communication_new_body,
                groupKey = "comm-123"
            ),
            content
        )
    }

    @Test
    fun `erp task reject maps to rejected banner`() {
        val content = mapper.mapDecrypted(
            PushNotificationPayload(
                channelId = IncomingPushNotificationMapper.CHANNEL_TASK_REJECT,
                identifier = "task-123",
                identifierType = "TaskId",
                rawPayload = "{}"
            )
        )

        assertEquals(
            PushNotificationContent(
                title = R.string.push_notification_banner_reject_title,
                body = R.string.push_notification_banner_reject_body,
                groupKey = "task-123"
            ),
            content
        )
    }

    @Test
    fun `erp task accept maps to accepted and processing banner`() {
        val content = mapper.mapDecrypted(
            PushNotificationPayload(
                channelId = IncomingPushNotificationMapper.CHANNEL_TASK_ACCEPT,
                identifier = "task-123",
                identifierType = "TaskId",
                rawPayload = "{}"
            )
        )

        assertEquals(
            PushNotificationContent(
                title = R.string.push_notification_banner_accept_title,
                body = R.string.push_notification_banner_accept_body,
                groupKey = "task-123"
            ),
            content
        )
    }

    @Test
    fun `erp task abort maps to deleted banner`() {
        val content = mapper.mapDecrypted(
            PushNotificationPayload(
                channelId = IncomingPushNotificationMapper.CHANNEL_TASK_ABORT,
                identifier = "task-123",
                identifierType = "TaskId",
                rawPayload = "{}"
            )
        )

        assertEquals(
            PushNotificationContent(
                title = R.string.push_notification_banner_abort_title,
                body = R.string.push_notification_banner_abort_body,
                groupKey = "task-123"
            ),
            content
        )
    }

    @Test
    fun `external access channels map to accessed banner`() {
        listOf(
            IncomingPushNotificationMapper.CHANNEL_TASK_VERTRETER,
            IncomingPushNotificationMapper.CHANNEL_EU_PRESCRIPTION_GET
        ).forEach { channelId ->
            val content = mapper.mapDecrypted(
                PushNotificationPayload(
                    channelId = channelId,
                    identifier = "task-123",
                    identifierType = "TaskId",
                    rawPayload = "{}"
                )
            )

            assertEquals(
                PushNotificationContent(
                    title = R.string.push_notification_banner_accessed_title,
                    body = R.string.push_notification_banner_accessed_body,
                    groupKey = "task-123"
                ),
                content
            )
        }
    }

    @Test
    fun `erp chargeitem update maps to invoice updated banner`() {
        val content = mapper.mapDecrypted(
            PushNotificationPayload(
                channelId = IncomingPushNotificationMapper.CHANNEL_CHARGEITEM_UPDATE,
                identifier = "invoice-123",
                identifierType = "ChargeItemId",
                rawPayload = "{}"
            )
        )

        assertEquals(
            PushNotificationContent(
                title = R.string.push_notification_banner_chargeitem_update_title,
                body = R.string.push_notification_banner_chargeitem_update_body,
                groupKey = "invoice-123"
            ),
            content
        )
    }

    @Test
    fun `erp chargeitem create maps to new invoice banner`() {
        val content = mapper.mapDecrypted(
            PushNotificationPayload(
                channelId = IncomingPushNotificationMapper.CHANNEL_CHARGEITEM_CREATE,
                identifier = "invoice-123",
                identifierType = "ChargeItemId",
                rawPayload = "{}"
            )
        )

        assertEquals(
            PushNotificationContent(
                title = R.string.push_notification_banner_chargeitem_create_title,
                body = R.string.push_notification_banner_chargeitem_create_body,
                groupKey = "invoice-123"
            ),
            content
        )
    }

    @Test
    fun `dispense and redeem channels map to dispense banner`() {
        listOf(
            IncomingPushNotificationMapper.CHANNEL_TASK_DISPENSE,
            IncomingPushNotificationMapper.CHANNEL_EU_PRESCRIPTION_REDEEM
        ).forEach { channelId ->
            val content = mapper.mapDecrypted(
                PushNotificationPayload(
                    channelId = channelId,
                    identifier = "task-123",
                    identifierType = "TaskId",
                    rawPayload = "{}"
                )
            )

            assertEquals(
                PushNotificationContent(
                    title = R.string.push_notification_banner_dispense_title,
                    body = R.string.push_notification_banner_dispense_body,
                    groupKey = "task-123"
                ),
                content
            )
        }
    }

    @Test
    fun `close channels are no-op and return null`() {
        listOf(
            IncomingPushNotificationMapper.CHANNEL_TASK_CLOSE,
            IncomingPushNotificationMapper.CHANNEL_EU_PRESCRIPTION_CLOSE
        ).forEach { channelId ->
            val content = mapper.mapDecrypted(
                PushNotificationPayload(
                    channelId = channelId,
                    identifier = "task-123",
                    identifierType = "TaskId",
                    rawPayload = "{}"
                )
            )

            assertNull(content)
        }
    }

    @Test
    fun `unknown channel maps to generic banner`() {
        val content = mapper.mapDecrypted(
            PushNotificationPayload(
                channelId = "erp.unknown.event",
                identifier = "task-123",
                identifierType = "TaskId",
                rawPayload = "{}"
            )
        )

        assertEquals(
            PushNotificationContent(
                title = R.string.push_notification_banner_generic_title,
                body = R.string.push_notification_banner_generic_body,
                groupKey = "task-123"
            ),
            content
        )
    }

    @Test
    fun `groupKey falls back to channelId when identifier is absent or blank`() {
        val withNullIdentifier = mapper.mapDecrypted(
            PushNotificationPayload(
                channelId = IncomingPushNotificationMapper.CHANNEL_TASK_ACTIVATE,
                identifier = null,
                rawPayload = "{}"
            )
        )
        assertEquals(IncomingPushNotificationMapper.CHANNEL_TASK_ACTIVATE, withNullIdentifier?.groupKey)

        val withBlankIdentifier = mapper.mapDecrypted(
            PushNotificationPayload(
                channelId = IncomingPushNotificationMapper.CHANNEL_TASK_ACTIVATE,
                identifier = "   ",
                rawPayload = "{}"
            )
        )
        assertEquals(IncomingPushNotificationMapper.CHANNEL_TASK_ACTIVATE, withBlankIdentifier?.groupKey)
    }

    @Test
    fun `fallback content is generic with no groupKey`() {
        assertEquals(
            PushNotificationContent(
                title = R.string.push_notification_banner_generic_title,
                body = R.string.push_notification_banner_generic_body,
                groupKey = null
            ),
            mapper.fallback()
        )
    }
}
