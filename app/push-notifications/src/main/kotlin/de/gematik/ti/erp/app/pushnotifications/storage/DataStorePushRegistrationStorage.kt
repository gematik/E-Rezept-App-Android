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

package de.gematik.ti.erp.app.pushnotifications.storage

import de.gematik.ti.erp.app.database.datastore.pushnotification.registration.PushRegistrationEntity
import de.gematik.ti.erp.app.database.datastore.pushnotification.registration.PushRegistrationLocalDataSource
import de.gematik.ti.erp.app.profile.repository.ProfileIdentifier

class DataStorePushRegistrationStorage(
    private val localDataSource: PushRegistrationLocalDataSource
) : PushRegistrationStorage {
    override suspend fun save(profileId: ProfileIdentifier, data: PushRegistrationData): Result<Unit> =
        safeStorageCall("PushRegistrationStorage.save failed for profile=$profileId") {
            localDataSource.save(profileId, data.toEntity(profileId))
        }

    override suspend fun load(profileId: ProfileIdentifier): Result<PushRegistrationData?> =
        safeStorageCall("PushRegistrationStorage.load failed for profile=$profileId") {
            localDataSource.load(profileId)?.toDomain()
        }

    override suspend fun loadAll(): Result<Map<ProfileIdentifier, PushRegistrationData>> =
        safeStorageCall("PushRegistrationStorage.loadAll failed") {
            localDataSource.loadAll().mapValues { (_, entity) -> entity.toDomain() }
        }

    override suspend fun hasCompletedPermissionPrompt(
        profileId: ProfileIdentifier
    ): Result<Boolean> = safeStorageCall(
        "PushRegistrationStorage.hasCompletedPermissionPrompt failed for profile=$profileId"
    ) {
        localDataSource.hasCompletedPermissionPrompt(profileId)
    }

    override suspend fun markPermissionPromptCompleted(
        profileId: ProfileIdentifier
    ): Result<Unit> = safeStorageCall(
        "PushRegistrationStorage.markPermissionPromptCompleted failed for profile=$profileId"
    ) {
        localDataSource.markPermissionPromptCompleted(profileId)
    }

    override suspend fun clear(profileId: ProfileIdentifier): Result<Unit> =
        safeStorageCall("PushRegistrationStorage.clear failed for profile=$profileId") {
            localDataSource.clear(profileId)
        }
}

private fun PushRegistrationData.toEntity(profileId: ProfileIdentifier) = PushRegistrationEntity(
    profileId = profileId,
    keyIdentifier = keyIdentifier,
    timeIssCreated = timeIssCreated,
    fcmToken = fcmToken,
    pendingFcmToken = pendingFcmToken
)

private fun PushRegistrationEntity.toDomain() = PushRegistrationData(
    keyIdentifier = keyIdentifier,
    timeIssCreated = timeIssCreated,
    fcmToken = fcmToken,
    pendingFcmToken = pendingFcmToken
)
