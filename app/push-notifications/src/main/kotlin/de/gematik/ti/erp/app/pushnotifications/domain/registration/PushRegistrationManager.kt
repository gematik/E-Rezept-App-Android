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

package de.gematik.ti.erp.app.pushnotifications.domain.registration

import android.os.Build
import de.gematik.ti.erp.app.Requirement
import de.gematik.ti.erp.app.profile.repository.ProfileIdentifier
import de.gematik.ti.erp.app.pushnotifications.domain.crypto.PushKeyChainManager
import de.gematik.ti.erp.app.pushnotifications.domain.formatPushNotificationYearMonth
import de.gematik.ti.erp.app.pushnotifications.model.PushChannel
import de.gematik.ti.erp.app.pushnotifications.provider.FcmTokenProvider
import de.gematik.ti.erp.app.pushnotifications.repository.PusherRepository
import de.gematik.ti.erp.app.pushnotifications.storage.PushRegistrationData
import de.gematik.ti.erp.app.pushnotifications.storage.PushRegistrationStorage
import io.github.aakira.napier.Napier
import kotlinx.coroutines.CancellationException
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import java.security.SecureRandom
import java.util.UUID

/** Manages device registration, local key initialization, and channel synchronization per profile. */
@Suppress("LongParameterList")
class PushRegistrationManager(
    private val repository: PusherRepository,
    private val fcmTokenProvider: FcmTokenProvider,
    private val keyChainManager: PushKeyChainManager,
    private val registrationStorage: PushRegistrationStorage,
    private val deviceNameProvider: () -> String = {
        val manufacturer = Build.MANUFACTURER
        val model = Build.MODEL
        if (model.startsWith(manufacturer, ignoreCase = true)) {
            model.capitalized()
        } else {
            "${manufacturer.capitalized()} $model"
        }
    },
    private val currentMonthProvider: () -> String = {
        val now = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
        formatPushNotificationYearMonth(now.year, now.monthNumber)
    },
    private val initialSharedSecretGenerator: () -> ByteArray = {
        ByteArray(ISS_BYTE_COUNT).also { SecureRandom().nextBytes(it) }
    },
    private val keyIdentifierProvider: () -> String = { UUID.randomUUID().toString() }
) {
    @Requirement(
        "A_27172",
        sourceSpecification = "gemF_PushNotification",
        rationale = "Performs encrypted FdV registration by obtaining the pushkey, creating the ISS, registering " +
            "the device, initializing and persisting the key chain, and discarding the ISS after derivation.",
        codeLines = 79
    )
    @Requirement(
        "A_27176",
        sourceSpecification = "gemF_PushNotification",
        rationale = "After successful Fachdienst registration, initializes and derives the initial key generation " +
            "for time_iss_created.",
        codeLines = 79
    )
    suspend fun register(
        profileId: ProfileIdentifier,
        pushKeyOverride: String? = null,
        channels: List<PushChannel>
    ): Result<Unit> = try {
        // Restore first because restoration removes registrations that have no persisted key material.
        keyChainManager.restoreFromStorage()

        val existingRegistration = registrationStorage.load(profileId).getOrThrow()
        val fcmToken = pushKeyOverride
            ?: existingRegistration?.pendingFcmToken
            ?: fcmTokenProvider.getToken()
        Napier.d {
            "PushRegistrationManager: obtained FCM token " +
                "(${fcmToken.take(FCM_TOKEN_LOG_PREFIX_LENGTH)}...)"
        }

        val issBytes = initialSharedSecretGenerator()
        require(issBytes.size == ISS_BYTE_COUNT) { "ISS must contain exactly $ISS_BYTE_COUNT bytes." }
        val issHex = issBytes.joinToString("") { "%02x".format(it) }
        issBytes.fill(0)
        val keyIdentifier = keyIdentifierProvider()
        val timeIssCreated = currentMonthProvider()

        Napier.d {
            "PushRegistrationManager: starting registration profile=$profileId " +
                "keyIdentifier=$keyIdentifier timeIssCreated=$timeIssCreated"
        }

        @Requirement(
            "A_27171#4",
            sourceSpecification = "gemF_PushNotification",
            rationale = "When the provider pushkey changed, deletes the old pushkey successfully before registering " +
                "the new pushkey.",
            codeLines = 15
        )
        val oldFcmToken = existingRegistration?.fcmToken
        if (oldFcmToken != null && oldFcmToken != fcmToken) {
            Napier.d { "PushRegistrationManager: FCM token changed — deregistering old pushkey first" }
            repository.deregister(pushKey = oldFcmToken, profileId = profileId).getOrThrow()
        }

        repository.registerDevice(
            pushKey = fcmToken,
            iss = issHex,
            keyIdentifier = keyIdentifier,
            timeIssCreated = timeIssCreated,
            deviceName = deviceNameProvider(),
            profileId = profileId
        ).getOrThrow()
        Napier.d { "PushRegistrationManager: Fachdienst registration successful" }

        // Remove the superseded chain before replacing its registration metadata.
        existingRegistration?.keyIdentifier
            ?.takeIf { it != keyIdentifier }
            ?.let { keyChainManager.clear(it).getOrThrow() }

        registrationStorage.save(
            profileId,
            PushRegistrationData(
                keyIdentifier = keyIdentifier,
                timeIssCreated = timeIssCreated,
                fcmToken = fcmToken
            )
        ).getOrThrow()

        keyChainManager.initializeAndAdvance(
            iss = issHex,
            keyIdentifier = keyIdentifier,
            timeIssCreated = timeIssCreated
        )

        repository.setChannels(
            pushKey = fcmToken,
            channels = channels,
            profileId = profileId
        ).getOrThrow()
        Napier.d { "PushRegistrationManager: registration complete for profile=$profileId" }
        Result.success(Unit)
    } catch (cancellation: CancellationException) {
        throw cancellation
    } catch (error: Exception) {
        Napier.e("PushRegistrationManager: registration failed for profile=$profileId", error)
        Result.failure(error)
    }

    /**
     * Performs best-effort remote deregistration, then clears the profile's local push data.
     * The stored FCM token is preferred because the current Firebase token may already have
     * rotated by the time a profile is logged out or deleted.
     */
    suspend fun clear(profileId: ProfileIdentifier): Result<Unit> = try {
        val registration = registrationStorage.load(profileId).getOrNull()
        deregisterBestEffort(profileId, registration)
        clearKeyChain(registration)
        registrationStorage.clear(profileId).getOrThrow()
        Result.success(Unit)
    } catch (cancellation: CancellationException) {
        throw cancellation
    } catch (error: Exception) {
        Napier.e("PushRegistrationManager: unexpected cleanup error for profile=$profileId", error)
        Result.failure(error)
    }

    private suspend fun deregisterBestEffort(
        profileId: ProfileIdentifier,
        registration: PushRegistrationData?
    ) {
        try {
            val fcmToken = registration?.fcmToken ?: fcmTokenProvider.getToken()
            repository.deregister(pushKey = fcmToken, profileId = profileId).getOrThrow()
        } catch (cancellation: CancellationException) {
            throw cancellation
        } catch (error: Exception) {
            Napier.w(
                "PushRegistrationManager: Fachdienst deregistration failed for profile=$profileId " +
                    "(proceeding with local clear): ${error.message}"
            )
        }
    }

    private suspend fun clearKeyChain(registration: PushRegistrationData?) {
        registration?.keyIdentifier?.let { keyIdentifier ->
            keyChainManager.clear(keyIdentifier).onFailure { error ->
                Napier.e("PushRegistrationManager: failed to clear key chain $keyIdentifier", error)
            }
        }
    }

    private companion object {
        const val ISS_BYTE_COUNT = 32
        const val FCM_TOKEN_LOG_PREFIX_LENGTH = 8
    }
}

private fun String.capitalized() = replaceFirstChar { it.uppercase() }
