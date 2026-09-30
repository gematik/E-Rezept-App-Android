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

package de.gematik.ti.erp.app.pushnotifications.presentation

import de.gematik.ti.erp.app.authentication.presentation.BiometricAuthenticator
import de.gematik.ti.erp.app.authentication.usecase.ChooseAuthenticationDataUseCase
import de.gematik.ti.erp.app.base.NetworkStatusTracker
import de.gematik.ti.erp.app.profile.model.ProfileErpModel
import de.gematik.ti.erp.app.profile.model.ProfilePushNotificationSettings
import de.gematik.ti.erp.app.profile.model.ProfilePushNotificationType
import de.gematik.ti.erp.app.profiles.usecase.GetActiveProfileUseCase
import de.gematik.ti.erp.app.profiles.usecase.GetProfileByIdUseCase
import de.gematik.ti.erp.app.profiles.usecase.GetProfilesUseCase
import de.gematik.ti.erp.app.pushnotifications.domain.usecase.AcceptPushNotificationPermissionUseCase
import de.gematik.ti.erp.app.pushnotifications.domain.usecase.DeclinePushNotificationPermissionUseCase
import de.gematik.ti.erp.app.pushnotifications.domain.usecase.ProfilePushNotificationState
import de.gematik.ti.erp.app.pushnotifications.domain.usecase.SyncProfilePushNotificationStateUseCase
import de.gematik.ti.erp.app.pushnotifications.domain.usecase.UpdateProfilePushNotificationSettingUseCase
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Test
import kotlin.test.assertEquals

@OptIn(ExperimentalCoroutinesApi::class)
class ProfilePushNotificationsControllerTest {
    private val dispatcher = StandardTestDispatcher()
    private val testScope = TestScope(dispatcher)
    private val settings = ProfilePushNotificationSettings(newMessageEnabled = true)
    private val acceptPermission = mockk<AcceptPushNotificationPermissionUseCase>()
    private val declinePermission = mockk<DeclinePushNotificationPermissionUseCase>()
    private val updateSetting = mockk<UpdateProfilePushNotificationSettingUseCase>()
    private val syncProfileState = mockk<SyncProfilePushNotificationStateUseCase>()
    private val profile = mockk<ProfileErpModel>(relaxed = true)

    @Before
    fun setUp() {
        Dispatchers.setMain(dispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `authentication synchronizes profile push state`() = testScope.runTest {
        coEvery { syncProfileState("profile-id") } returns
            Result.success(ProfilePushNotificationState.Registered(settings))
        val controller = controller()

        controller.onAuthenticationStateChanged(true)
        advanceUntilIdle()

        assertEquals(ProfilePushNotificationsUiState.Ready(settings), controller.uiState.value)
        coVerify(exactly = 1) { syncProfileState("profile-id") }
    }

    @Test
    fun `missing registration exposes registration state`() = testScope.runTest {
        coEvery { syncProfileState("profile-id") } returns
            Result.success(ProfilePushNotificationState.RegistrationRequired)
        val controller = controller()

        controller.onAuthenticationStateChanged(true)
        advanceUntilIdle()

        assertEquals(ProfilePushNotificationsUiState.RegistrationRequired, controller.uiState.value)
    }

    @Test
    fun `completed prompt allows a toggle to register directly`() = testScope.runTest {
        coEvery { syncProfileState("profile-id") } returns
            Result.success(ProfilePushNotificationState.Disabled)
        coEvery {
            updateSetting(
                "profile-id",
                any(),
                ProfilePushNotificationType.NEW_MESSAGE,
                true
            )
        } returns Result.success(settings)
        val controller = controller()

        controller.onAuthenticationStateChanged(true)
        advanceUntilIdle()
        assertEquals(ProfilePushNotificationsUiState.Disabled, controller.uiState.value)

        controller.onToggle(ProfilePushNotificationType.NEW_MESSAGE, true)
        advanceUntilIdle()

        assertEquals(ProfilePushNotificationsUiState.Ready(settings), controller.uiState.value)
    }

    @Test
    fun `declining prompt records decision and leaves all toggles off`() = testScope.runTest {
        coEvery { declinePermission("profile-id") } returns Result.success(Unit)
        val controller = controller()

        controller.onPermissionPromptDecision(false)
        advanceUntilIdle()

        assertEquals(ProfilePushNotificationsUiState.Disabled, controller.uiState.value)
        coVerify(exactly = 1) { declinePermission("profile-id") }
        coVerify(exactly = 0) { acceptPermission(any()) }
    }

    @Test
    fun `initial setup exposes loading then ready`() = testScope.runTest {
        val completion = CompletableDeferred<Result<ProfilePushNotificationSettings>>()
        coEvery { acceptPermission("profile-id") } coAnswers { completion.await() }
        val controller = controller()

        controller.onPermissionPromptDecision(true)
        runCurrent()
        assertEquals(ProfilePushNotificationsUiState.Loading(null), controller.uiState.value)

        completion.complete(Result.success(settings))
        advanceUntilIdle()
        assertEquals(ProfilePushNotificationsUiState.Ready(settings), controller.uiState.value)
    }

    @Test
    fun `retry repeats failed operation and clears error after success`() = testScope.runTest {
        coEvery { acceptPermission("profile-id") } returnsMany listOf(
            Result.failure(IllegalStateException("failed")),
            Result.success(settings)
        )
        val controller = controller()

        controller.onPermissionPromptDecision(true)
        advanceUntilIdle()
        assertEquals(
            ProfilePushNotificationsUiState.Error(previousSettings = null, isInitialSetup = true),
            controller.uiState.value
        )

        controller.retryPushNotificationSync()
        advanceUntilIdle()

        assertEquals(ProfilePushNotificationsUiState.Ready(settings), controller.uiState.value)
        coVerify(exactly = 2) { acceptPermission("profile-id") }
    }

    @Test
    fun `toggle failure retains the previous ready settings`() = testScope.runTest {
        coEvery { syncProfileState("profile-id") } returns
            Result.success(ProfilePushNotificationState.Registered(settings))
        coEvery { updateSetting(any(), any(), any(), any()) } returns
            Result.failure(IllegalStateException("remote failed"))
        val controller = controller()

        controller.onAuthenticationStateChanged(true)
        advanceUntilIdle()
        controller.onToggle(ProfilePushNotificationType.NEW_MESSAGE, false)
        advanceUntilIdle()

        assertEquals(
            ProfilePushNotificationsUiState.Error(previousSettings = settings, isInitialSetup = false),
            controller.uiState.value
        )
    }

    private fun controller(): ProfilePushNotificationsController {
        val getProfiles = mockk<GetProfilesUseCase>()
        val getProfileById = mockk<GetProfileByIdUseCase>()
        val getActiveProfile = mockk<GetActiveProfileUseCase>()
        every { getProfiles() } returns flowOf(listOf(profile))
        every { getProfileById("profile-id") } returns flowOf(profile)
        every { getActiveProfile() } returns flowOf(profile)

        return ProfilePushNotificationsController(
            profileId = "profile-id",
            getProfileByIdUseCase = getProfileById,
            getProfilesUseCase = getProfiles,
            getActiveProfileUseCase = getActiveProfile,
            chooseAuthenticationDataUseCase = mockk<ChooseAuthenticationDataUseCase>(),
            biometricAuthenticator = mockk<BiometricAuthenticator>(relaxed = true),
            networkStatusTracker = mockk<NetworkStatusTracker>(relaxed = true),
            acceptPushNotificationPermissionUseCase = acceptPermission,
            declinePushNotificationPermissionUseCase = declinePermission,
            updateProfilePushNotificationSettingUseCase = updateSetting,
            syncProfilePushNotificationStateUseCase = syncProfileState
        )
    }
}
