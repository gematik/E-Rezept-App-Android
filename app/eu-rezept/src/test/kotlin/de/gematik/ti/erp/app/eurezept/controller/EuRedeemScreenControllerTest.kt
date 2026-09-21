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
package de.gematik.ti.erp.app.eurezept.controller

import de.gematik.ti.erp.app.authentication.presentation.BiometricAuthenticator
import de.gematik.ti.erp.app.authentication.usecase.ChooseAuthenticationDataUseCase
import de.gematik.ti.erp.app.base.NetworkStatusTracker
import de.gematik.ti.erp.app.base.usecase.ObserveNavigationTriggerUseCase
import de.gematik.ti.erp.app.eurezept.model.MockEuTestData
import de.gematik.ti.erp.app.eurezept.model.MockEuTestData.mockInvalidProfileMock
import de.gematik.ti.erp.app.eurezept.model.MockEuTestData.mockValidProfileMock
import de.gematik.ti.erp.app.eurezept.presentation.EuRedeemScreenController
import de.gematik.ti.erp.app.eurezept.ui.model.EuRedeemSelector.WAS_EU_REDEEM_INSTRUCTION_VIEWED
import de.gematik.ti.erp.app.idp.repository.IdpRepository
import de.gematik.ti.erp.app.navigation.triggers.NavigationTriggerDataStore
import de.gematik.ti.erp.app.profiles.repository.ProfileRepository
import de.gematik.ti.erp.app.profiles.usecase.GetActiveProfileUseCase
import de.gematik.ti.erp.app.profiles.usecase.GetProfileByIdUseCase
import de.gematik.ti.erp.app.profiles.usecase.GetProfilesUseCase
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class EuRedeemScreenControllerTest {

    private val dispatcher = StandardTestDispatcher()
    private val testScope = TestScope(dispatcher)

    private val navigationTriggerDataStore: NavigationTriggerDataStore = mockk()
    private val profileRepository: ProfileRepository = mockk()
    private val idpRepository: IdpRepository = mockk()

    private val networkStatusTracker: NetworkStatusTracker = mockk(relaxed = true)
    private val biometricAuthenticator: BiometricAuthenticator = mockk(relaxed = true)

    private lateinit var observeNavigationTriggerUseCase: ObserveNavigationTriggerUseCase
    private lateinit var chooseAuthenticationDataUseCase: ChooseAuthenticationDataUseCase

    private val getActiveProfileUseCase: GetActiveProfileUseCase = mockk()
    private val getProfileByIdUseCase: GetProfileByIdUseCase = mockk()
    private val getProfilesUseCase: GetProfilesUseCase = mockk()

    private lateinit var controller: EuRedeemScreenController

    private val mockValidProfile = mockValidProfileMock
    private val mockInvalidProfile = mockInvalidProfileMock

    @Before
    fun setup() {
        Dispatchers.setMain(dispatcher)

        observeNavigationTriggerUseCase = ObserveNavigationTriggerUseCase(navigationTriggerDataStore, dispatcher)
        chooseAuthenticationDataUseCase = ChooseAuthenticationDataUseCase(profileRepository, idpRepository, dispatcher)

        coEvery {
            navigationTriggerDataStore.shouldNavigate(WAS_EU_REDEEM_INSTRUCTION_VIEWED.name)
        } returns flowOf(false)

        coEvery { getActiveProfileUseCase.invoke() } returns flowOf(mockValidProfile)
        coEvery { getProfilesUseCase.invoke() } returns flowOf(listOf(mockValidProfile))
        coEvery { getProfileByIdUseCase.invoke(any()) } returns flowOf(mockValidProfile)
        coEvery { profileRepository.getProfileById(any()) } returns flowOf(mockValidProfile)

        val mockAuthData = MockEuTestData.mockValidUserAuthentication
        coEvery { idpRepository.getUserAuthentication(any()) } returns flowOf(mockAuthData)
        coEvery { profileRepository.updateLastAuthenticated(any(), any()) } returns Unit
        controller = EuRedeemScreenController(
            getProfileByIdUseCase = getProfileByIdUseCase,
            getProfilesUseCase = getProfilesUseCase,
            getActiveProfileUseCase = getActiveProfileUseCase,
            chooseAuthenticationDataUseCase = chooseAuthenticationDataUseCase,
            observeNavigationTriggerUseCase = observeNavigationTriggerUseCase,
            networkStatusTracker = networkStatusTracker,
            biometricAuthenticator = biometricAuthenticator
        )
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `handleRedeemAction calls onShowInstructions when instructions not viewed`() = testScope.runTest {
        var showInstructionsCalled = false
        var startRedemptionCalled = false

        controller.handleRedeemAction(
            onStartRedemption = { startRedemptionCalled = true },
            onShowInstructions = { showInstructionsCalled = true }
        )

        advanceUntilIdle()

        assertTrue(showInstructionsCalled, "onShowInstructions should be called")
        assertFalse(startRedemptionCalled, "onStartRedemption should not be called")

        coVerify(exactly = 1) {
            navigationTriggerDataStore.shouldNavigate(WAS_EU_REDEEM_INSTRUCTION_VIEWED.name)
        }
    }

    @Test
    fun `handleRedeemAction calls onStartRedemption when instructions viewed and SSO token valid`() {
        coEvery {
            navigationTriggerDataStore.shouldNavigate(WAS_EU_REDEEM_INSTRUCTION_VIEWED.name)
        } returns flowOf(true)

        testScope.runTest {
            var showInstructionsCalled = false
            var startRedemptionCalled = false

            advanceUntilIdle() // let activeProfile settle to Data(mockValidProfile)

            controller.handleRedeemAction(
                onStartRedemption = { startRedemptionCalled = true },
                onShowInstructions = { showInstructionsCalled = true }
            )

            advanceUntilIdle()

            assertFalse(showInstructionsCalled, "onShowInstructions should not be called")
            assertTrue(startRedemptionCalled, "onStartRedemption should be called")

            coVerify(exactly = 1) {
                navigationTriggerDataStore.shouldNavigate(WAS_EU_REDEEM_INSTRUCTION_VIEWED.name)
            }
        }
    }

    @Test
    fun `handleRedeemAction triggers authentication when instructions viewed and SSO token invalid`() {
        coEvery {
            navigationTriggerDataStore.shouldNavigate(WAS_EU_REDEEM_INSTRUCTION_VIEWED.name)
        } returns flowOf(true)

        // Recreate controller with invalid profile
        coEvery { getActiveProfileUseCase.invoke() } returns flowOf(mockInvalidProfile)
        coEvery { getProfilesUseCase.invoke() } returns flowOf(listOf(mockInvalidProfile))
        coEvery { getProfileByIdUseCase.invoke(any()) } returns flowOf(mockInvalidProfile)
        coEvery { profileRepository.getProfileById(any()) } returns flowOf(mockInvalidProfile)

        controller = EuRedeemScreenController(
            getProfileByIdUseCase = getProfileByIdUseCase,
            getProfilesUseCase = getProfilesUseCase,
            getActiveProfileUseCase = getActiveProfileUseCase,
            chooseAuthenticationDataUseCase = chooseAuthenticationDataUseCase,
            observeNavigationTriggerUseCase = observeNavigationTriggerUseCase,
            networkStatusTracker = networkStatusTracker,
            biometricAuthenticator = biometricAuthenticator
        )

        testScope.runTest {
            var showInstructionsCalled = false
            var startRedemptionCalled = false

            advanceUntilIdle() // let activeProfile settle to Data(mockInvalidProfile)

            controller.handleRedeemAction(
                onStartRedemption = { startRedemptionCalled = true },
                onShowInstructions = { showInstructionsCalled = true }
            )

            advanceUntilIdle()

            assertFalse(showInstructionsCalled, "onShowInstructions should not be called")
            assertFalse(startRedemptionCalled, "onStartRedemption should not be called")

            coVerify(exactly = 1) {
                navigationTriggerDataStore.shouldNavigate(WAS_EU_REDEEM_INSTRUCTION_VIEWED.name)
            }
        }
    }

    @Test
    fun `wasRedeemInstructionNotViewed returns true when instruction not viewed`() = testScope.runTest {
        val result = controller.wasRedeemInstructionNotViewed()

        assertTrue(result, "Should return true when instruction not viewed")

        coVerify(exactly = 1) {
            navigationTriggerDataStore.shouldNavigate(WAS_EU_REDEEM_INSTRUCTION_VIEWED.name)
        }
    }

    @Test
    fun `wasRedeemInstructionNotViewed returns false when instruction viewed`() {
        coEvery {
            navigationTriggerDataStore.shouldNavigate(WAS_EU_REDEEM_INSTRUCTION_VIEWED.name)
        } returns flowOf(true)

        testScope.runTest {
            val result = controller.wasRedeemInstructionNotViewed()

            assertFalse(result, "Should return false when instruction viewed")

            coVerify(exactly = 1) {
                navigationTriggerDataStore.shouldNavigate(WAS_EU_REDEEM_INSTRUCTION_VIEWED.name)
            }
        }
    }
}
