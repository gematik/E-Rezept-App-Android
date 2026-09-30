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

package de.gematik.ti.erp.app.pushnotifications.domain.usecase

import de.gematik.ti.erp.app.profile.model.ProfilePushNotificationSettings
import de.gematik.ti.erp.app.profile.repository.ProfileIdentifier
import de.gematik.ti.erp.app.pushnotifications.domain.model.PushNotificationChannelMapper
import de.gematik.ti.erp.app.pushnotifications.domain.registration.PushRegistrationManager
import de.gematik.ti.erp.app.pushnotifications.repository.PusherRepository
import de.gematik.ti.erp.app.pushnotifications.storage.PushRegistrationStorage
import io.github.aakira.napier.Napier
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Registers the profile with all default channels.
 * Registration state is verified from the Fachdienst and local key material.
 */
class AcceptPushNotificationPermissionUseCase(
    private val registrationManager: PushRegistrationManager,
    private val registrationStorage: PushRegistrationStorage,
    private val pusherRepository: PusherRepository,
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO
) {
    suspend operator fun invoke(profileId: ProfileIdentifier): Result<ProfilePushNotificationSettings> =
        withContext(dispatcher) {
            try {
                registrationStorage.markPermissionPromptCompleted(profileId).getOrThrow()
                val enabledSettings = ProfilePushNotificationSettings()
                registrationManager.register(
                    profileId = profileId,
                    channels = PushNotificationChannelMapper.toPushChannels(enabledSettings)
                ).getOrThrow()

                val registeredPushKey = requireNotNull(
                    registrationStorage.load(profileId).getOrThrow()?.fcmToken
                ) {
                    "Push channels cannot be read before the device pusher is registered."
                }
                Result.success(
                    PushNotificationChannelMapper.toProfileSettings(
                        pusherRepository.getChannels(
                            pushKey = registeredPushKey,
                            profileId = profileId
                        ).getOrThrow()
                    )
                )
            } catch (cancellation: CancellationException) {
                throw cancellation
            } catch (error: Exception) {
                Napier.e(
                    "AcceptPushNotificationPermissionUseCase: initial push setup failed for profile=$profileId",
                    error
                )
                Result.failure(error)
            }
        }
}
