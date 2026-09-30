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

import androidx.annotation.StringRes
import de.gematik.ti.erp.app.core.R

sealed interface IncomingPushNotification {
    data class Encrypted(
        val ciphertext: String,
        val timeMessageEncrypted: String,
        val keyIdentifier: String
    ) : IncomingPushNotification

    data class Rejected(val reason: String) : IncomingPushNotification
}

data class PushNotificationContent(
    @StringRes val title: Int,
    @StringRes val body: Int,
    val groupKey: String? = null
)

/** Maps FCM data and decrypted payloads to domain notification models. */
class IncomingPushNotificationMapper {
    fun parse(data: Map<String, String>): IncomingPushNotification {
        val ciphertext = data[KEY_CIPHERTEXT]
        val timeEncrypted = data[KEY_TIME_ENCRYPTED]
        val keyIdentifier = data[KEY_IDENTIFIER]

        return when {
            ciphertext == null && timeEncrypted == null && keyIdentifier == null ->
                IncomingPushNotification.Rejected("Encrypted push payload is missing.")
            ciphertext.isNullOrBlank() || timeEncrypted.isNullOrBlank() || keyIdentifier.isNullOrBlank() ->
                IncomingPushNotification.Rejected("Encrypted push payload is incomplete.")
            else -> IncomingPushNotification.Encrypted(ciphertext, timeEncrypted, keyIdentifier)
        }
    }

    /**
     * Maps a decrypted Fachdienst [payload] to localized notification content.
     * Returns null for no-op channels (`erp.task.close` and `erp.eu.prescription.close`).
     */
    fun mapDecrypted(payload: PushNotificationPayload): PushNotificationContent? {
        val channelId = payload.channelId
        if (channelId in NO_OP_CHANNEL_IDS) {
            return null
        }

        val groupKey = payload.identifier?.takeIf { it.isNotBlank() } ?: channelId

        val (title, body) = when (channelId) {
            CHANNEL_TASK_ACTIVATE ->
                R.string.push_notification_banner_activate_title to R.string.push_notification_banner_activate_body
            CHANNEL_COMMUNICATION_NEW ->
                R.string.push_notification_banner_communication_new_title to R.string.push_notification_banner_communication_new_body
            CHANNEL_TASK_REJECT ->
                R.string.push_notification_banner_reject_title to R.string.push_notification_banner_reject_body
            CHANNEL_TASK_ACCEPT ->
                R.string.push_notification_banner_accept_title to R.string.push_notification_banner_accept_body
            CHANNEL_TASK_ABORT ->
                R.string.push_notification_banner_abort_title to R.string.push_notification_banner_abort_body
            CHANNEL_TASK_VERTRETER, CHANNEL_EU_PRESCRIPTION_GET ->
                R.string.push_notification_banner_accessed_title to R.string.push_notification_banner_accessed_body
            CHANNEL_CHARGEITEM_UPDATE ->
                R.string.push_notification_banner_chargeitem_update_title to R.string.push_notification_banner_chargeitem_update_body
            CHANNEL_CHARGEITEM_CREATE ->
                R.string.push_notification_banner_chargeitem_create_title to R.string.push_notification_banner_chargeitem_create_body
            CHANNEL_TASK_DISPENSE, CHANNEL_EU_PRESCRIPTION_REDEEM ->
                R.string.push_notification_banner_dispense_title to R.string.push_notification_banner_dispense_body
            else ->
                R.string.push_notification_banner_generic_title to R.string.push_notification_banner_generic_body
        }

        return PushNotificationContent(
            title = title,
            body = body,
            groupKey = groupKey
        )
    }

    fun fallback(): PushNotificationContent = PushNotificationContent(
        title = R.string.push_notification_banner_generic_title,
        body = R.string.push_notification_banner_generic_body,
        groupKey = null
    )

    companion object {
        const val KEY_CIPHERTEXT = "ciphertext"
        const val KEY_TIME_ENCRYPTED = "time_message_encrypted"
        const val KEY_IDENTIFIER = "key_identifier"

        const val CHANNEL_TASK_ACTIVATE = "erp.task.activate"
        const val CHANNEL_COMMUNICATION_NEW = "erp.communication.new"
        const val CHANNEL_TASK_REJECT = "erp.task.reject"
        const val CHANNEL_TASK_ACCEPT = "erp.task.accept"
        const val CHANNEL_TASK_ABORT = "erp.task.abort"
        const val CHANNEL_TASK_VERTRETER = "erp.task.vertreter"
        const val CHANNEL_EU_PRESCRIPTION_GET = "erp.eu.prescription.get"
        const val CHANNEL_CHARGEITEM_UPDATE = "erp.chargeitem.update"
        const val CHANNEL_CHARGEITEM_CREATE = "erp.chargeitem.create"
        const val CHANNEL_TASK_DISPENSE = "erp.task.dispense"
        const val CHANNEL_EU_PRESCRIPTION_REDEEM = "erp.eu.prescription.redeem"
        const val CHANNEL_TASK_CLOSE = "erp.task.close"
        const val CHANNEL_EU_PRESCRIPTION_CLOSE = "erp.eu.prescription.close"

        val NO_OP_CHANNEL_IDS = setOf(
            CHANNEL_TASK_CLOSE,
            CHANNEL_EU_PRESCRIPTION_CLOSE
        )
    }
}
