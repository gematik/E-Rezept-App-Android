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

package de.gematik.ti.erp.app.pushnotifications.storage

import de.gematik.ti.erp.app.profile.repository.ProfileIdentifier

/**
 * Stores per-profile registration metadata and one-time prompt state.
 * Clearing registration preserves the prompt state; only derived keys are persisted separately.
 */
interface PushRegistrationStorage {
    suspend fun save(profileId: ProfileIdentifier, data: PushRegistrationData): Result<Unit>
    suspend fun load(profileId: ProfileIdentifier): Result<PushRegistrationData?>
    suspend fun loadAll(): Result<Map<ProfileIdentifier, PushRegistrationData>>
    suspend fun hasCompletedPermissionPrompt(profileId: ProfileIdentifier): Result<Boolean>
    suspend fun markPermissionPromptCompleted(profileId: ProfileIdentifier): Result<Unit>
    suspend fun clear(profileId: ProfileIdentifier): Result<Unit>
}

data class PushRegistrationData(
    val keyIdentifier: String,
    val timeIssCreated: String,
    val fcmToken: String? = null,
    val pendingFcmToken: String? = null
)
