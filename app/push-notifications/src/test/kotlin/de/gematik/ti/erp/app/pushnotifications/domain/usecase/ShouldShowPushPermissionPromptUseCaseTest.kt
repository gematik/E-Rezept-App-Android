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

import de.gematik.ti.erp.app.pushnotifications.storage.PushRegistrationStorage
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class ShouldShowPushPermissionPromptUseCaseTest {
    private val dispatcher = StandardTestDispatcher()
    private val registrationStorage = mockk<PushRegistrationStorage>()

    private fun useCase() = ShouldShowPushPermissionPromptUseCase(registrationStorage, dispatcher)

    @Test
    fun `presents prompt when the profile has never answered it`() = runTest(dispatcher) {
        coEvery { registrationStorage.hasCompletedPermissionPrompt("profile-id") } returns
            Result.success(false)

        assertTrue(useCase()("profile-id"))
    }

    @Test
    fun `does not present prompt when the profile already answered it`() = runTest(dispatcher) {
        coEvery { registrationStorage.hasCompletedPermissionPrompt("profile-id") } returns
            Result.success(true)

        assertFalse(useCase()("profile-id"))
    }

    @Test
    fun `presents prompt when the completion state cannot be read`() = runTest(dispatcher) {
        coEvery { registrationStorage.hasCompletedPermissionPrompt("profile-id") } returns
            Result.failure(IllegalStateException("storage unavailable"))

        assertTrue(useCase()("profile-id"))
    }
}
