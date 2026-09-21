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

package de.gematik.ti.erp.app.database.datastore.pushnotification

import kotlinx.serialization.Serializable

const val PROFILE_PUSH_NOTIFICATION_DATA_SOURCE = "ProfilePushNotifications"
const val PROFILE_PUSH_NOTIFICATION_SCHEMA_VERSION = 1

enum class ProfilePushNotificationType {
    NEW_PRESCRIPTION,
    NEW_MESSAGE,
    STATUS_CHANGE,
    NEW_INVOICE,
    EXTERNAL_ACCESS
}

@Serializable
data class ProfilePushNotificationSchema(
    val version: Int = PROFILE_PUSH_NOTIFICATION_SCHEMA_VERSION,
    val entries: Map<String, ProfilePushNotificationEntry> = emptyMap()
)

@Serializable
data class ProfilePushNotificationEntry(
    val newPrescription: Boolean = false,
    val newMessage: Boolean = false,
    val statusChange: Boolean = false,
    val newInvoice: Boolean = false,
    val externalAccess: Boolean = false
)
