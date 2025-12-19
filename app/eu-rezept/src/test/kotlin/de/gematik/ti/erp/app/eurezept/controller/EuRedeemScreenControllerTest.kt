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
import de.gematik.ti.erp.app.eurezept.presentation.EuRedeemScreenController
import de.gematik.ti.erp.app.eurezept.ui.model.EuRedeemSelector.WAS_EU_REDEEM_INSTRUCTION_VIEWED
import de.gematik.ti.erp.app.idp.model.IdpData
import de.gematik.ti.erp.app.idp.repository.IdpRepository
import de.gematik.ti.erp.app.navigation.triggers.NavigationTriggerDataStore
import de.gematik.ti.erp.app.profiles.model.ProfilesData
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
    private lateinit var getActiveProfileUseCase: GetActiveProfileUseCase
    private lateinit var getProfileByIdUseCase: GetProfileByIdUseCase
    private lateinit var getProfilesUseCase: GetProfilesUseCase
    private lateinit var chooseAuthenticationDataUseCase: ChooseAuthenticationDataUseCase

    private lateinit var controller: EuRedeemScreenController

    @Before
    fun setup() {
        Dispatchers.setMain(dispatcher)

        observeNavigationTriggerUseCase = ObserveNavigationTriggerUseCase(navigationTriggerDataStore, dispatcher)
        getActiveProfileUseCase = GetActiveProfileUseCase(profileRepository, dispatcher)
        getProfileByIdUseCase = GetProfileByIdUseCase(profileRepository, dispatcher)
        getProfilesUseCase = GetProfilesUseCase(profileRepository, dispatcher)
        chooseAuthenticationDataUseCase = ChooseAuthenticationDataUseCase(profileRepository, idpRepository, dispatcher)

        coEvery {
            navigationTriggerDataStore.shouldNavigate(WAS_EU_REDEEM_INSTRUCTION_VIEWED.name)
        } returns flowOf(false)

        val mockProfileData = MockEuTestData.profileData
        coEvery { profileRepository.profiles() } returns flowOf(listOf(mockProfileData))
        coEvery { profileRepository.activeProfile() } returns flowOf(mockProfileData)
        coEvery { profileRepository.getProfileById(any()) } returns flowOf(mockProfileData)
        coEvery { profileRepository.updateLastAuthenticated(any(), any()) } returns Unit

        val mockAuthData = IdpData.AuthenticationData(
            singleSignOnTokenScope = mockk(relaxed = true)
        )
        coEvery { idpRepository.authenticationData(any()) } returns flowOf(mockAuthData)

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

        val mockInvalidProfileData = mockk<ProfilesData.Profile>(relaxed = true)
        coEvery { profileRepository.activeProfile() } returns flowOf(mockInvalidProfileData)

        testScope.runTest {
            var showInstructionsCalled = false
            var startRedemptionCalled = false

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
