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
import de.gematik.ti.erp.app.profile.model.ProfilePushNotificationType
import de.gematik.ti.erp.app.pushnotifications.domain.model.PushNotificationChannelMapper
import de.gematik.ti.erp.app.pushnotifications.domain.registration.PushRegistrationManager
import de.gematik.ti.erp.app.pushnotifications.repository.PusherRepository
import de.gematik.ti.erp.app.pushnotifications.storage.PushRegistrationData
import de.gematik.ti.erp.app.pushnotifications.storage.PushRegistrationStorage
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.coVerifyOrder
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertSame
import kotlin.test.assertTrue

class ProfilePushNotificationOperationsTest {
    @Test
    fun `accept operation returns enabled settings after registration`() = runTest {
        val manager = mockk<PushRegistrationManager>()
        val registrationStorage = mockk<PushRegistrationStorage>()
        val pusherRepository = mockk<PusherRepository>()
        val enabledSettings = ProfilePushNotificationSettings()
        coEvery {
            registrationStorage.markPermissionPromptCompleted("profile-id")
        } returns Result.success(Unit)
        coEvery {
            manager.register(
                "profile-id",
                null,
                PushNotificationChannelMapper.toPushChannels(enabledSettings)
            )
        } returns Result.success(Unit)
        coEvery { registrationStorage.load("profile-id") } returns Result.success(registration())
        coEvery { pusherRepository.getChannels("fcm-token", "profile-id") } returns
            Result.success(PushNotificationChannelMapper.toPushChannels(enabledSettings))

        val result = AcceptPushNotificationPermissionUseCase(
            manager,
            registrationStorage,
            pusherRepository
        )("profile-id")

        assertEquals(enabledSettings, result.getOrThrow())
        coVerifyOrder {
            registrationStorage.markPermissionPromptCompleted("profile-id")
            manager.register(
                "profile-id",
                null,
                PushNotificationChannelMapper.toPushChannels(enabledSettings)
            )
            registrationStorage.load("profile-id")
            pusherRepository.getChannels("fcm-token", "profile-id")
        }
    }

    @Test
    fun `accept operation does not return settings after registration failure`() = runTest {
        val manager = mockk<PushRegistrationManager>()
        val registrationStorage = mockk<PushRegistrationStorage>()
        val pusherRepository = mockk<PusherRepository>()
        val failure = IllegalStateException("remote failed")
        coEvery {
            registrationStorage.markPermissionPromptCompleted("profile-id")
        } returns Result.success(Unit)
        coEvery { manager.register(any(), any(), any()) } returns Result.failure(failure)

        val result = AcceptPushNotificationPermissionUseCase(
            manager,
            registrationStorage,
            pusherRepository
        )("profile-id")

        assertSame(failure, result.exceptionOrNull())
        coVerify(exactly = 0) { registrationStorage.load(any()) }
        coVerify(exactly = 0) { pusherRepository.getChannels(any(), any()) }
    }

    @Test
    fun `decline operation remembers prompt without registering`() = runTest {
        val registrationStorage = mockk<PushRegistrationStorage>()
        coEvery {
            registrationStorage.markPermissionPromptCompleted("profile-id")
        } returns Result.success(Unit)

        val result = DeclinePushNotificationPermissionUseCase(registrationStorage)("profile-id")

        assertTrue(result.isSuccess)
        coVerify(exactly = 1) {
            registrationStorage.markPermissionPromptCompleted("profile-id")
        }
    }

    @Test
    fun `toggle operation reads the verified remote channels`() = runTest {
        val registrationStorage = mockk<PushRegistrationStorage>()
        val pusherRepository = mockk<PusherRepository>()
        val registrationManager = mockk<PushRegistrationManager>()
        val currentSettings = ProfilePushNotificationSettings(newMessageEnabled = true)
        val updatedSettings = currentSettings.copy(newMessageEnabled = false)
        coEvery { registrationStorage.load("profile-id") } returns Result.success(registration())
        coEvery {
            pusherRepository.setChannels(
                "fcm-token",
                PushNotificationChannelMapper.toPushChannels(updatedSettings),
                "profile-id"
            )
        } returns Result.success(Unit)
        coEvery { pusherRepository.getChannels("fcm-token", "profile-id") } returns
            Result.success(PushNotificationChannelMapper.toPushChannels(updatedSettings))

        val result = UpdateProfilePushNotificationSettingUseCase(
            registrationStorage = registrationStorage,
            pusherRepository = pusherRepository,
            registrationManager = registrationManager
        )(
            profileId = "profile-id",
            currentSettings = currentSettings,
            notificationType = ProfilePushNotificationType.NEW_MESSAGE,
            enabled = false
        )

        assertEquals(updatedSettings, result.getOrThrow())
        coVerifyOrder {
            pusherRepository.setChannels(
                "fcm-token",
                PushNotificationChannelMapper.toPushChannels(updatedSettings),
                "profile-id"
            )
            pusherRepository.getChannels("fcm-token", "profile-id")
        }
    }

    @Test
    fun `toggle operation does not update state after remote failure`() = runTest {
        val registrationStorage = mockk<PushRegistrationStorage>()
        val pusherRepository = mockk<PusherRepository>()
        val registrationManager = mockk<PushRegistrationManager>()
        val failure = IllegalStateException("remote failed")
        coEvery { registrationStorage.load("profile-id") } returns Result.success(registration())
        coEvery { pusherRepository.setChannels(any(), any(), any()) } returns Result.failure(failure)

        val result = UpdateProfilePushNotificationSettingUseCase(
            registrationStorage = registrationStorage,
            pusherRepository = pusherRepository,
            registrationManager = registrationManager
        )(
            "profile-id",
            ProfilePushNotificationSettings(),
            ProfilePushNotificationType.NEW_MESSAGE,
            false
        )

        assertSame(failure, result.exceptionOrNull())
    }

    @Test
    fun `disabling the final setting deregisters without updating channels`() = runTest {
        val registrationStorage = mockk<PushRegistrationStorage>()
        val pusherRepository = mockk<PusherRepository>()
        val registrationManager = mockk<PushRegistrationManager>()
        val currentSettings = disabledSettings.copy(newMessageEnabled = true)
        coEvery {
            registrationStorage.markPermissionPromptCompleted("profile-id")
        } returns Result.success(Unit)
        coEvery { registrationManager.clear("profile-id") } returns Result.success(Unit)

        val result = UpdateProfilePushNotificationSettingUseCase(
            registrationStorage = registrationStorage,
            pusherRepository = pusherRepository,
            registrationManager = registrationManager
        )(
            profileId = "profile-id",
            currentSettings = currentSettings,
            notificationType = ProfilePushNotificationType.NEW_MESSAGE,
            enabled = false
        )

        assertEquals(disabledSettings, result.getOrThrow())
        coVerifyOrder {
            registrationStorage.markPermissionPromptCompleted("profile-id")
            registrationManager.clear("profile-id")
        }
        coVerify(exactly = 0) { pusherRepository.setChannels(any(), any(), any()) }
        coVerify(exactly = 0) { pusherRepository.getChannels(any(), any()) }
    }

    @Test
    fun `enabling after deregistration registers selected channels and reads them back`() = runTest {
        val registrationStorage = mockk<PushRegistrationStorage>()
        val pusherRepository = mockk<PusherRepository>()
        val registrationManager = mockk<PushRegistrationManager>()
        val updatedSettings = disabledSettings.copy(newMessageEnabled = true)
        val channels = PushNotificationChannelMapper.toPushChannels(updatedSettings)
        coEvery { registrationStorage.load("profile-id") } returnsMany listOf(
            Result.success(null),
            Result.success(registration())
        )
        coEvery {
            registrationManager.register(
                profileId = "profile-id",
                channels = channels
            )
        } returns Result.success(Unit)
        coEvery { pusherRepository.getChannels("fcm-token", "profile-id") } returns
            Result.success(channels)

        val result = UpdateProfilePushNotificationSettingUseCase(
            registrationStorage = registrationStorage,
            pusherRepository = pusherRepository,
            registrationManager = registrationManager
        )(
            profileId = "profile-id",
            currentSettings = disabledSettings,
            notificationType = ProfilePushNotificationType.NEW_MESSAGE,
            enabled = true
        )

        assertEquals(updatedSettings, result.getOrThrow())
        coVerify(exactly = 1) {
            registrationManager.register(
                profileId = "profile-id",
                channels = channels
            )
        }
        coVerify(exactly = 0) { pusherRepository.setChannels(any(), any(), any()) }
        coVerify(exactly = 1) { pusherRepository.getChannels("fcm-token", "profile-id") }
    }

    private val disabledSettings = ProfilePushNotificationSettings(
        newPrescriptionEnabled = false,
        newMessageEnabled = false,
        statusChangeEnabled = false,
        newInvoiceEnabled = false,
        externalAccessEnabled = false
    )

    private fun registration() = PushRegistrationData(
        keyIdentifier = "key-id",
        timeIssCreated = "2026-07",
        fcmToken = "fcm-token"
    )
}
