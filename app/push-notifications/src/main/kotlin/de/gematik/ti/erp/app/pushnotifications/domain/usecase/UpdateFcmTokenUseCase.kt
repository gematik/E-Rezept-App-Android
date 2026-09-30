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

import de.gematik.ti.erp.app.Requirement
import de.gematik.ti.erp.app.idp.usecase.IdpUseCase
import de.gematik.ti.erp.app.profile.repository.ProfileIdentifier
import de.gematik.ti.erp.app.pushnotifications.domain.registration.PushRegistrationManager
import de.gematik.ti.erp.app.pushnotifications.provider.FcmTokenProvider
import de.gematik.ti.erp.app.pushnotifications.repository.PusherRepository
import de.gematik.ti.erp.app.pushnotifications.storage.PushRegistrationData
import de.gematik.ti.erp.app.pushnotifications.storage.PushRegistrationStorage
import io.github.aakira.napier.Napier
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Re-registers each authenticated profile after FCM token rotation.
 * For profiles that cannot authenticate, stores the new token for the next registration attempt.
 */
class UpdateFcmTokenUseCase(
    private val registrationManager: PushRegistrationManager,
    private val registrationStorage: PushRegistrationStorage,
    private val idpUseCase: IdpUseCase,
    private val pusherRepository: PusherRepository,
    private val fcmTokenProvider: FcmTokenProvider,
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO
) {
    @Requirement(
        "A_27171#2",
        sourceSpecification = "gemF_PushNotification",
        rationale = "After application startup, obtains the current provider pushkey when locally consented " +
            "registrations exist.",
        codeLines = 17
    )
    suspend fun syncAtAppStart(): Result<Unit> = withContext(dispatcher) {
        try {
            val registrations = registrationStorage.loadAll().getOrThrow()
            if (registrations.isEmpty()) return@withContext Result.success(Unit)

            updateRegistrations(
                registrations = registrations,
                newToken = fcmTokenProvider.getToken()
            )
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Napier.e("UpdateFcmTokenUseCase: app-start FCM token synchronization failed", e)
            Result.failure(e)
        }
    }

    suspend operator fun invoke(newToken: String): Result<Unit> = withContext(dispatcher) {
        try {
            val registrations = registrationStorage.loadAll().getOrThrow()
            updateRegistrations(registrations, newToken)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Napier.e("UpdateFcmTokenUseCase: failed to update FCM token", e)
            Result.failure(e)
        }
    }

    private suspend fun updateRegistrations(
        registrations: Map<ProfileIdentifier, PushRegistrationData>,
        newToken: String
    ): Result<Unit> {
        var firstFailure: Throwable? = null

        registrations.forEach { (profileId, registration) ->
            try {
                updateProfileToken(profileId, registration, newToken)
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                if (firstFailure == null) firstFailure = e
                Napier.e("UpdateFcmTokenUseCase: failed to synchronize profile=$profileId", e)
            }
        }

        return firstFailure?.let { Result.failure(it) } ?: Result.success(Unit)
    }

    private suspend fun updateProfileToken(
        profileId: ProfileIdentifier,
        registration: PushRegistrationData,
        newToken: String
    ) {
        @Requirement(
            "A_27171#3",
            sourceSpecification = "gemF_PushNotification",
            rationale = "Compares the registered pushkey with the provider pushkey and starts re-registration " +
                "with the new pushkey only when synchronization is required.",
            codeLines = 27
        )
        val registeredPushKey = requireNotNull(registration.fcmToken) {
            "Cannot rotate a pusher without its previously registered push key."
        }

        if (registeredPushKey == newToken && registration.pendingFcmToken == null) return

        if (!canAuthenticate(profileId)) {
            Napier.w(
                "UpdateFcmTokenUseCase: no bearer token for profile=$profileId — " +
                    "FCM token update deferred until next login."
            )
            registrationStorage.save(profileId, registration.copy(pendingFcmToken = newToken)).getOrThrow()
            return
        }

        Napier.d { "UpdateFcmTokenUseCase: FCM token rotated — re-registering profile=$profileId" }
        val channels = pusherRepository.getChannels(
            pushKey = registeredPushKey,
            profileId = profileId
        ).getOrThrow()
        registrationManager.register(
            profileId = profileId,
            pushKeyOverride = newToken,
            channels = channels
        ).getOrThrow()
    }

    private suspend fun canAuthenticate(profileId: ProfileIdentifier): Boolean = try {
        idpUseCase.loadAccessToken(profileId = profileId, refresh = false)
        true
    } catch (e: CancellationException) {
        throw e
    } catch (_: Exception) {
        false
    }
}
