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
 */

package de.gematik.ti.erp.app.pushnotifications.domain.usecase

import de.gematik.ti.erp.app.profile.model.ProfilePushNotificationSettings
import de.gematik.ti.erp.app.profile.repository.ProfileIdentifier
import de.gematik.ti.erp.app.pushnotifications.domain.crypto.PushKeyChainManager
import de.gematik.ti.erp.app.pushnotifications.domain.model.PushNotificationChannelMapper
import de.gematik.ti.erp.app.pushnotifications.domain.registration.PushRegistrationManager
import de.gematik.ti.erp.app.pushnotifications.model.PushChannel
import de.gematik.ti.erp.app.pushnotifications.provider.FcmTokenProvider
import de.gematik.ti.erp.app.pushnotifications.provider.PushApplicationIdProvider
import de.gematik.ti.erp.app.pushnotifications.repository.PusherRepository
import de.gematik.ti.erp.app.pushnotifications.storage.PushRegistrationStorage
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

sealed interface ProfilePushNotificationState {
    data object RegistrationRequired : ProfilePushNotificationState
    data object Disabled : ProfilePushNotificationState
    data class Registered(
        val settings: ProfilePushNotificationSettings
    ) : ProfilePushNotificationState
}

/**
 * A registration is usable only when its local metadata, key chain, and matching remote pusher exist.
 * Token rotation re-registers the current FCM token while preserving the remote channel settings.
 */
class SyncProfilePushNotificationStateUseCase(
    private val registrationStorage: PushRegistrationStorage,
    private val pusherRepository: PusherRepository,
    private val fcmTokenProvider: FcmTokenProvider,
    private val pushApplicationIdProvider: PushApplicationIdProvider,
    private val keyChainManager: PushKeyChainManager,
    private val registrationManager: PushRegistrationManager,
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO
) {
    suspend operator fun invoke(profileId: ProfileIdentifier): Result<ProfilePushNotificationState> =
        withContext(dispatcher) {
            try {
                Result.success(syncState(profileId))
            } catch (cancellation: CancellationException) {
                throw cancellation
            } catch (error: Exception) {
                Result.failure(error)
            }
        }

    private suspend fun syncState(profileId: ProfileIdentifier): ProfilePushNotificationState {
        val registration = loadUsableRegistration(profileId)
            ?: return stateWithoutRegistration(profileId)

        if (!hasMatchingRemotePusher(profileId, registration.pushKey)) {
            registrationStorage.markPermissionPromptCompleted(profileId).getOrThrow()
            registrationManager.clear(profileId).getOrThrow()
            return stateWithoutRegistration(profileId)
        }

        val currentChannels = syncChannelsWithCurrentToken(
            profileId = profileId,
            registration = registration
        )

        return ProfilePushNotificationState.Registered(
            settings = PushNotificationChannelMapper.toProfileSettings(currentChannels)
        )
    }

    private suspend fun stateWithoutRegistration(
        profileId: ProfileIdentifier
    ): ProfilePushNotificationState =
        if (registrationStorage.hasCompletedPermissionPrompt(profileId).getOrThrow()) {
            ProfilePushNotificationState.Disabled
        } else {
            ProfilePushNotificationState.RegistrationRequired
        }

    private suspend fun loadUsableRegistration(
        profileId: ProfileIdentifier
    ): UsablePushRegistration? {
        keyChainManager.restoreFromStorage()
        val registration = registrationStorage.load(profileId).getOrThrow() ?: return null
        val registeredPushKey = registration.fcmToken ?: return null

        return if (keyChainManager.isInitialized(registration.keyIdentifier)) {
            UsablePushRegistration(
                pushKey = registeredPushKey,
                hasPendingFcmToken = registration.pendingFcmToken != null
            )
        } else {
            null
        }
    }

    private suspend fun hasMatchingRemotePusher(
        profileId: ProfileIdentifier,
        registeredPushKey: String
    ): Boolean {
        val applicationId = pushApplicationIdProvider.getPushApplicationId()

        return pusherRepository.getPushers(profileId).getOrThrow().any { remotePusher ->
            remotePusher.pushKey == registeredPushKey && remotePusher.appId == applicationId
        }
    }

    private suspend fun syncChannelsWithCurrentToken(
        profileId: ProfileIdentifier,
        registration: UsablePushRegistration
    ): List<PushChannel> {
        val registeredChannels = pusherRepository.getChannels(
            pushKey = registration.pushKey,
            profileId = profileId
        ).getOrThrow()
        val currentFcmToken = fcmTokenProvider.getToken()

        if (registration.pushKey == currentFcmToken && !registration.hasPendingFcmToken) {
            return registeredChannels
        }

        registrationManager.register(
            profileId = profileId,
            pushKeyOverride = currentFcmToken,
            channels = registeredChannels
        ).getOrThrow()

        return pusherRepository.getChannels(
            pushKey = currentFcmToken,
            profileId = profileId
        ).getOrThrow()
    }

    private data class UsablePushRegistration(
        val pushKey: String,
        val hasPendingFcmToken: Boolean
    )
}
