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

package de.gematik.ti.erp.app.medicationplan.presentation

import app.cash.turbine.test
import de.gematik.ti.erp.app.medicationplan.repository.DefaultMedicationPlanRepository
import de.gematik.ti.erp.app.medicationplan.usecase.GetActiveProfileWithSchedulesUseCase
import de.gematik.ti.erp.app.mocks.profile.api.API_MOCK_PROFILE
import de.gematik.ti.erp.app.profiles.repository.ProfileRepository
import de.gematik.ti.erp.app.utils.uistate.UiState
import io.mockk.MockKAnnotations
import io.mockk.coEvery
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
import kotlinx.datetime.Instant
import org.junit.After
import org.junit.Before
import org.junit.Test
import kotlin.test.assertEquals

class MedicationScheduleNotificationSuccessScreenControllerTest {

    private val now = Instant.parse("2024-01-01T12:00:00Z")
    private val profileRepository: ProfileRepository = mockk()
    private val defaultMedicationPlanRepository: DefaultMedicationPlanRepository = mockk()
    private val dispatcher = StandardTestDispatcher()
    private val testScope = TestScope(dispatcher)
    private lateinit var controllerUnderTest: MedicationPlanNotificationScreenController
    private lateinit var loadProfilesWithSchedulesUseCase: GetActiveProfileWithSchedulesUseCase

    @OptIn(ExperimentalCoroutinesApi::class)
    @Before
    fun setUp() {
        MockKAnnotations.init(this)
        Dispatchers.setMain(dispatcher)

        loadProfilesWithSchedulesUseCase = GetActiveProfileWithSchedulesUseCase(
            medicationPlanRepository = defaultMedicationPlanRepository,
            profileRepository = profileRepository,
            dispatcher = dispatcher
        )

        controllerUnderTest = MedicationPlanNotificationScreenController(
            loadProfilesWithSchedulesUseCase = loadProfilesWithSchedulesUseCase,
            now = now
        )
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `test loading state`() {
        coEvery { profileRepository.profiles() } returns flowOf(listOf(API_MOCK_PROFILE))
        coEvery { defaultMedicationPlanRepository.getAllMedicationSchedules() } returns flowOf(listOf())

        testScope.runTest {
            controllerUnderTest.profilesWithSchedules.test {
                val loading = awaitItem()
                assertEquals(UiState.Loading(), loading)
            }
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `test empty state`() {
        coEvery { profileRepository.profiles() } returns flowOf(listOf())
        coEvery { defaultMedicationPlanRepository.getAllMedicationSchedules() } returns flowOf(listOf())

        testScope.runTest {
            controllerUnderTest.profilesWithSchedules.test {
                val loading = awaitItem()
                assertEquals(UiState.Loading(), loading)
                advanceUntilIdle()
                val empty = awaitItem()
                assertEquals(UiState.Empty(), empty)
            }
        }
    }
}
