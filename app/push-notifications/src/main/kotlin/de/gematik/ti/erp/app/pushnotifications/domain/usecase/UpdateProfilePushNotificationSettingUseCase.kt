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

package de.gematik.ti.erp.app.pushnotifications.domain.usecase

import de.gematik.ti.erp.app.profile.model.ProfilePushNotificationSettings
import de.gematik.ti.erp.app.profile.model.ProfilePushNotificationType
import de.gematik.ti.erp.app.profile.repository.ProfileIdentifier
import de.gematik.ti.erp.app.pushnotifications.domain.model.PushNotificationChannelMapper
import de.gematik.ti.erp.app.pushnotifications.domain.model.hasEnabledPushNotificationSetting
import de.gematik.ti.erp.app.pushnotifications.domain.registration.PushRegistrationManager
import de.gematik.ti.erp.app.pushnotifications.repository.PusherRepository
import de.gematik.ti.erp.app.pushnotifications.storage.PushRegistrationStorage
import io.github.aakira.napier.Napier
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Synchronizes the complete remote channel configuration.
 *
 * Disabling the final setting deregisters the pusher and clears its local key material. Enabling a
 * setting without an existing registration creates a new pusher before reading back its channels.
 */
class UpdateProfilePushNotificationSettingUseCase(
    private val registrationStorage: PushRegistrationStorage,
    private val pusherRepository: PusherRepository,
    private val registrationManager: PushRegistrationManager,
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO
) {
    suspend operator fun invoke(
        profileId: ProfileIdentifier,
        currentSettings: ProfilePushNotificationSettings,
        notificationType: ProfilePushNotificationType,
        enabled: Boolean
    ): Result<ProfilePushNotificationSettings> = withContext(dispatcher) {
        try {
            val updatedSettings = currentSettings.withToggle(notificationType, enabled)
            if (!updatedSettings.hasEnabledPushNotificationSetting()) {
                registrationStorage.markPermissionPromptCompleted(profileId).getOrThrow()
                registrationManager.clear(profileId).getOrThrow()
                return@withContext Result.success(updatedSettings)
            }

            val registeredPushKey = registrationStorage.load(profileId).getOrThrow()?.fcmToken
            val pushKey = if (registeredPushKey == null) {
                register(profileId, updatedSettings)
            } else {
                pusherRepository.setChannels(
                    pushKey = registeredPushKey,
                    channels = PushNotificationChannelMapper.toPushChannels(updatedSettings),
                    profileId = profileId
                ).getOrThrow()
                registeredPushKey
            }

            Result.success(
                PushNotificationChannelMapper.toProfileSettings(
                    pusherRepository.getChannels(pushKey = pushKey, profileId = profileId).getOrThrow()
                )
            )
        } catch (cancellation: CancellationException) {
            throw cancellation
        } catch (error: Exception) {
            Napier.e(
                "UpdateProfilePushNotificationSettingUseCase: update failed for profile=$profileId",
                error
            )
            Result.failure(error)
        }
    }

    private suspend fun register(
        profileId: ProfileIdentifier,
        settings: ProfilePushNotificationSettings
    ): String {
        registrationManager.register(
            profileId = profileId,
            channels = PushNotificationChannelMapper.toPushChannels(settings)
        ).getOrThrow()
        return requireNotNull(registrationStorage.load(profileId).getOrThrow()?.fcmToken) {
            "Push registration completed without a persisted FCM token."
        }
    }
}

private fun ProfilePushNotificationSettings.withToggle(
    type: ProfilePushNotificationType,
    enabled: Boolean
): ProfilePushNotificationSettings = when (type) {
    ProfilePushNotificationType.NEW_PRESCRIPTION -> copy(newPrescriptionEnabled = enabled)
    ProfilePushNotificationType.NEW_MESSAGE -> copy(newMessageEnabled = enabled)
    ProfilePushNotificationType.STATUS_CHANGE -> copy(statusChangeEnabled = enabled)
    ProfilePushNotificationType.NEW_INVOICE -> copy(newInvoiceEnabled = enabled)
    ProfilePushNotificationType.EXTERNAL_ACCESS -> copy(externalAccessEnabled = enabled)
}
