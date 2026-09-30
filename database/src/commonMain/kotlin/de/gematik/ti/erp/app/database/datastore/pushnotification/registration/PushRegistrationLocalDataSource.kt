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
 */

package de.gematik.ti.erp.app.database.datastore.pushnotification.registration

import androidx.datastore.core.DataStore
import kotlinx.coroutines.flow.first

class PushRegistrationLocalDataSource(
    private val dataStore: DataStore<PushRegistrationEntitySchema>
) {
    suspend fun save(profileId: String, registration: PushRegistrationEntity) {
        require(registration.profileId == profileId) {
            "Push registration profileId does not match its storage key"
        }
        dataStore.updateData { current ->
            current.copy(registrations = current.registrations + (profileId to registration))
        }
    }

    suspend fun load(profileId: String): PushRegistrationEntity? =
        dataStore.data.first().registrations[profileId]

    suspend fun loadAll(): Map<String, PushRegistrationEntity> =
        dataStore.data.first().registrations

    suspend fun hasCompletedPermissionPrompt(profileId: String): Boolean =
        profileId in dataStore.data.first().completedPermissionPromptProfiles

    suspend fun markPermissionPromptCompleted(profileId: String) {
        dataStore.updateData { current ->
            val completedProfiles = current.completedPermissionPromptProfiles + profileId
            current.copy(
                completedPermissionPromptProfiles = completedProfiles
            )
        }
    }

    suspend fun clear(profileId: String) {
        dataStore.updateData { current ->
            current.copy(registrations = current.registrations - profileId)
        }
    }
}
