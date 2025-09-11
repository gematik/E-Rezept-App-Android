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

package de.gematik.ti.erp.app.profiles.presentation

import app.cash.turbine.test
import de.gematik.ti.erp.app.mocks.PROFILE_ID
import de.gematik.ti.erp.app.mocks.profile.api.API_MOCK_PROFILE
import de.gematik.ti.erp.app.profiles.repository.ProfileRepository
import de.gematik.ti.erp.app.profiles.usecase.GetActiveProfileUseCase
import de.gematik.ti.erp.app.profiles.usecase.GetProfileByIdUseCase
import de.gematik.ti.erp.app.profiles.usecase.GetProfilesUseCase
import de.gematik.ti.erp.app.utils.uistate.UiState.Companion.isDataState
import de.gematik.ti.erp.app.utils.uistate.UiState.Companion.isErrorState
import de.gematik.ti.erp.app.utils.uistate.UiState.Companion.isLoadingState
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.emptyFlow
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

class GetProfileByIdControllerTest {

    private val profileRepository: ProfileRepository = mockk()
    private val dispatcher = StandardTestDispatcher()
    private val testScope = TestScope(dispatcher)

    private lateinit var controllerUnderTest: GetProfileByIdController
    private lateinit var getProfileByIdUseCase: GetProfileByIdUseCase
    private lateinit var getProfilesUseCase: GetProfilesUseCase
    private lateinit var getActiveProfileUseCase: GetActiveProfileUseCase

    @OptIn(ExperimentalCoroutinesApi::class)
    @Before
    fun setUp() {
        MockKAnnotations.init(this)
        Dispatchers.setMain(dispatcher)
        getProfileByIdUseCase = GetProfileByIdUseCase(profileRepository, dispatcher)
        getProfilesUseCase = GetProfilesUseCase(profileRepository, dispatcher)
        getActiveProfileUseCase = GetActiveProfileUseCase(profileRepository, dispatcher)
        controllerUnderTest = object : GetProfileByIdController(
            PROFILE_ID,
            getProfileByIdUseCase,
            getProfilesUseCase,
            getActiveProfileUseCase
        ) {}
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `selected profile is empty and screen in error state`() {
        every { profileRepository.getProfileById(PROFILE_ID) } returns emptyFlow()
        every { profileRepository.profiles() } returns flowOf(listOf())

        testScope.runTest {
            controllerUnderTest.combinedProfile.test {
                advanceUntilIdle()
                awaitItem()
                val result = awaitItem()
                assert(result.isErrorState)
            }
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `getProfiles is null and screen in error state`() {
        every { profileRepository.getProfileById(PROFILE_ID) } returns flowOf(API_MOCK_PROFILE)
        every { profileRepository.profiles() } returns emptyFlow()

        testScope.runTest {
            controllerUnderTest.combinedProfile.test {
                advanceUntilIdle()
                awaitItem()
                val result = awaitItem()
                assert(result.isErrorState)
            }
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `combined profile is loading and screen in loading state`() {
        every { profileRepository.getProfileById(PROFILE_ID) } returns flowOf(API_MOCK_PROFILE)
        every { profileRepository.profiles() } returns flowOf(listOf(API_MOCK_PROFILE))

        testScope.runTest {
            controllerUnderTest.combinedProfile.test {
                advanceUntilIdle()
                val result = awaitItem()
                awaitItem()
                assert(result.isLoadingState)
            }
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `selected profile is successfully loaded`() {
        every { profileRepository.getProfileById(PROFILE_ID) } returns flowOf(API_MOCK_PROFILE)
        every { profileRepository.profiles() } returns flowOf(listOf(API_MOCK_PROFILE))

        testScope.runTest {
            controllerUnderTest.combinedProfile.test {
                advanceUntilIdle()
                awaitItem()
                val result = awaitItem()
                assert(result.isDataState)
            }
        }
    }
}
