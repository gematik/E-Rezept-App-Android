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

import de.gematik.ti.erp.app.pushnotifications.domain.crypto.PushKeyChainManager
import de.gematik.ti.erp.app.pushnotifications.domain.model.PushNotificationChannelMapper
import de.gematik.ti.erp.app.pushnotifications.domain.registration.PushRegistrationManager
import de.gematik.ti.erp.app.pushnotifications.model.Pusher
import de.gematik.ti.erp.app.pushnotifications.provider.FcmTokenProvider
import de.gematik.ti.erp.app.pushnotifications.provider.PushApplicationIdProvider
import de.gematik.ti.erp.app.pushnotifications.repository.PusherRepository
import de.gematik.ti.erp.app.pushnotifications.storage.PushRegistrationData
import de.gematik.ti.erp.app.pushnotifications.storage.PushRegistrationStorage
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.coVerifyOrder
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.test.runTest
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class SyncProfilePushNotificationStateUseCaseTest {
    private val registrationStorage = mockk<PushRegistrationStorage>()
    private val pusherRepository = mockk<PusherRepository>()
    private val fcmTokenProvider = mockk<FcmTokenProvider>()
    private val applicationIdProvider = mockk<PushApplicationIdProvider>()
    private val keyChainManager = mockk<PushKeyChainManager>()
    private val registrationManager = mockk<PushRegistrationManager>()
    private val channels = PushNotificationChannelMapper.toPushChannels(
        de.gematik.ti.erp.app.profile.model.ProfilePushNotificationSettings()
    )

    @Test
    fun `missing local registration requires registration without a remote lookup`() = runTest {
        commonSetup(currentToken = "fcm-token")
        coEvery { registrationStorage.load("profile-id") } returns Result.success(null)
        coEvery {
            registrationStorage.hasCompletedPermissionPrompt("profile-id")
        } returns Result.success(false)

        assertEquals(
            ProfilePushNotificationState.RegistrationRequired,
            useCase()("profile-id").getOrThrow()
        )
        coVerify(exactly = 0) { pusherRepository.getPushers(any()) }
        coVerify(exactly = 0) { fcmTokenProvider.getToken() }
    }

    @Test
    fun `completed prompt without registration returns disabled without remote lookup`() = runTest {
        commonSetup(currentToken = "fcm-token")
        coEvery { registrationStorage.load("profile-id") } returns Result.success(null)
        coEvery {
            registrationStorage.hasCompletedPermissionPrompt("profile-id")
        } returns Result.success(true)

        assertEquals(
            ProfilePushNotificationState.Disabled,
            useCase()("profile-id").getOrThrow()
        )
        coVerify(exactly = 0) { pusherRepository.getPushers(any()) }
        coVerify(exactly = 0) { fcmTokenProvider.getToken() }
    }

    @Test
    fun `matching local key and remote pusher loads channels`() = runTest {
        commonSetup(currentToken = "fcm-token")
        coEvery { registrationStorage.load("profile-id") } returns Result.success(registration("fcm-token"))
        every { keyChainManager.isInitialized("key-id") } returns true
        coEvery { pusherRepository.getChannels("fcm-token", "profile-id") } returns Result.success(channels)

        val result = useCase()("profile-id").getOrThrow()

        assertEquals(
            ProfilePushNotificationState.Registered(
                PushNotificationChannelMapper.toProfileSettings(channels)
            ),
            result
        )
    }

    @Test
    fun `remote pusher mismatch clears registration without repeating prompt`() = runTest {
        commonSetup(currentToken = "fcm-token", remoteToken = "other-token")
        coEvery { registrationStorage.load("profile-id") } returns Result.success(registration("fcm-token"))
        coEvery {
            registrationStorage.markPermissionPromptCompleted("profile-id")
        } returns Result.success(Unit)
        coEvery {
            registrationStorage.hasCompletedPermissionPrompt("profile-id")
        } returns Result.success(true)
        every { keyChainManager.isInitialized("key-id") } returns true
        coEvery { registrationManager.clear("profile-id") } returns Result.success(Unit)

        assertEquals(
            ProfilePushNotificationState.Disabled,
            useCase()("profile-id").getOrThrow()
        )
        coVerifyOrder {
            registrationStorage.markPermissionPromptCompleted("profile-id")
            registrationManager.clear("profile-id")
        }
    }

    @Test
    fun `token drift preserves old channels while registering current token`() = runTest {
        commonSetup(currentToken = "new-token", remoteToken = "old-token")
        coEvery { registrationStorage.load("profile-id") } returns Result.success(registration("old-token"))
        every { keyChainManager.isInitialized("key-id") } returns true
        coEvery { pusherRepository.getChannels("old-token", "profile-id") } returns Result.success(channels)
        coEvery { registrationManager.register("profile-id", "new-token", channels) } returns Result.success(Unit)
        coEvery { pusherRepository.getChannels("new-token", "profile-id") } returns Result.success(channels)

        useCase()("profile-id").getOrThrow()

        coVerify(exactly = 1) { registrationManager.register("profile-id", "new-token", channels) }
        coVerify(exactly = 1) { pusherRepository.getChannels("new-token", "profile-id") }
    }

    @Test
    fun `cancellation is rethrown`() = runTest {
        val cancellation = CancellationException("cancelled")
        coEvery { keyChainManager.restoreFromStorage() } throws cancellation

        val thrown = assertFailsWith<CancellationException> {
            useCase()("profile-id")
        }

        assertEquals(cancellation.message, thrown.message)
    }

    private fun commonSetup(currentToken: String, remoteToken: String = currentToken) {
        coEvery { fcmTokenProvider.getToken() } returns currentToken
        every { applicationIdProvider.getPushApplicationId() } returns "app-id"
        coEvery { pusherRepository.getPushers("profile-id") } returns
            Result.success(listOf(Pusher(remoteToken, appId = "app-id")))
        coEvery { keyChainManager.restoreFromStorage() } returns Unit
    }

    private fun useCase() = SyncProfilePushNotificationStateUseCase(
        registrationStorage = registrationStorage,
        pusherRepository = pusherRepository,
        fcmTokenProvider = fcmTokenProvider,
        pushApplicationIdProvider = applicationIdProvider,
        keyChainManager = keyChainManager,
        registrationManager = registrationManager
    )

    private fun registration(token: String) = PushRegistrationData(
        keyIdentifier = "key-id",
        timeIssCreated = "2026-07",
        fcmToken = token
    )
}
