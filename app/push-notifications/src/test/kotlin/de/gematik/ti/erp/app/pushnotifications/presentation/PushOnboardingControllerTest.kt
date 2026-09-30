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

import de.gematik.ti.erp.app.profile.model.ProfilePushNotificationSettings
import de.gematik.ti.erp.app.pushnotifications.domain.usecase.AcceptPushNotificationPermissionUseCase
import de.gematik.ti.erp.app.pushnotifications.domain.usecase.DeclinePushNotificationPermissionUseCase
import de.gematik.ti.erp.app.pushnotifications.domain.usecase.ShouldShowPushPermissionPromptUseCase
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import junit.framework.TestCase.assertFalse
import junit.framework.TestCase.assertTrue
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Test
import kotlin.test.assertNotNull

@OptIn(ExperimentalCoroutinesApi::class)
class PushOnboardingControllerTest {
    private val dispatcher = StandardTestDispatcher()
    private val testScope = TestScope(dispatcher)
    private val shouldPresent = mockk<ShouldShowPushPermissionPromptUseCase>()
    private val acceptPermission = mockk<AcceptPushNotificationPermissionUseCase>()
    private val declinePermission = mockk<DeclinePushNotificationPermissionUseCase>()

    @Before
    fun setUp() {
        Dispatchers.setMain(dispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun controller() = PushOnboardingController(
        profileId = "profile-id",
        shouldShowPushPermissionPromptUseCase = shouldPresent,
        acceptPushNotificationPermissionUseCase = acceptPermission,
        declinePushNotificationPermissionUseCase = declinePermission
    )

    @Test
    fun `checkFirstLogin displays consent dialog when required and runs only once`() = testScope.runTest {
        coEvery { shouldPresent("profile-id") } returns true
        val controller = controller()

        controller.checkFirstLogin()
        controller.checkFirstLogin()
        advanceUntilIdle()

        assertTrue(controller.showConsentDialog.value)
        coVerify(exactly = 1) { shouldPresent("profile-id") }
    }

    @Test
    fun `accepting registers all channels and triggers enable notifications event`() = testScope.runTest {
        coEvery { acceptPermission("profile-id") } returns
            Result.success(ProfilePushNotificationSettings())
        val controller = controller()

        controller.onConsentAccepted()
        advanceUntilIdle()

        coVerify(exactly = 1) { acceptPermission("profile-id") }
        assertNotNull(controller.showEnableNotificationsEvent.payload)
    }

    @Test
    fun `declining records decision and hides dialog without registering`() = testScope.runTest {
        coEvery { shouldPresent("profile-id") } returns true
        coEvery { declinePermission("profile-id") } returns Result.success(Unit)
        val controller = controller()

        controller.checkFirstLogin()
        advanceUntilIdle()
        controller.onConsentDeclined()
        advanceUntilIdle()

        assertFalse(controller.showConsentDialog.value)
        coVerify(exactly = 1) { declinePermission("profile-id") }
        coVerify(exactly = 0) { acceptPermission(any()) }
    }
}
