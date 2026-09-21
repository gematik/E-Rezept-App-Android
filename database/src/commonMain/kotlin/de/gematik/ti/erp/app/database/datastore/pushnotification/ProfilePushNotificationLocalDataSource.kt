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

import androidx.datastore.core.DataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class ProfilePushNotificationLocalDataSource(
    private val dataStore: DataStore<ProfilePushNotificationSchema>
) {

    fun notificationsForProfile(profileId: String): Flow<ProfilePushNotificationEntry> =
        dataStore.data.map { schema ->
            schema.entries[profileId] ?: ProfilePushNotificationEntry()
        }

    suspend fun saveToggle(profileId: String, notificationType: ProfilePushNotificationType, enabled: Boolean) {
        dataStore.updateData { schema ->
            val current = schema.entries[profileId] ?: ProfilePushNotificationEntry()
            val updated = when (notificationType) {
                ProfilePushNotificationType.NEW_PRESCRIPTION -> current.copy(newPrescription = enabled)
                ProfilePushNotificationType.NEW_MESSAGE -> current.copy(newMessage = enabled)
                ProfilePushNotificationType.STATUS_CHANGE -> current.copy(statusChange = enabled)
                ProfilePushNotificationType.NEW_INVOICE -> current.copy(newInvoice = enabled)
                ProfilePushNotificationType.EXTERNAL_ACCESS -> current.copy(externalAccess = enabled)
            }
            schema.copy(entries = schema.entries + (profileId to updated))
        }
    }
}
