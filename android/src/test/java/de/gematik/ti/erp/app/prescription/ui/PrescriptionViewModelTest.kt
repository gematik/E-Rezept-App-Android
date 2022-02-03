/*
 * Copyright (c) 2022 gematik GmbH
 * 
 * Licensed under the EUPL, Version 1.2 or – as soon they will be approved by
 * the European Commission - subsequent versions of the EUPL (the Licence);
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at:
 * 
 *     https://joinup.ec.europa.eu/software/page/eupl
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the Licence is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Licence for the specific language governing permissions and
 * limitations under the Licence.
 * 
 */

package de.gematik.ti.erp.app.prescription.ui

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import de.gematik.ti.erp.app.cardwall.usecase.AuthenticationUseCase
import de.gematik.ti.erp.app.common.usecase.HintUseCase
import de.gematik.ti.erp.app.common.usecase.model.PrescriptionScreenHintDefineSecurity
import de.gematik.ti.erp.app.common.usecase.model.PrescriptionScreenHintDemoModeActivated
import de.gematik.ti.erp.app.common.usecase.model.PrescriptionScreenHintTryDemoMode
import de.gematik.ti.erp.app.db.entities.Settings
import de.gematik.ti.erp.app.db.entities.SettingsAuthenticationMethod
import de.gematik.ti.erp.app.demo.usecase.DemoUseCase
import de.gematik.ti.erp.app.prescription.usecase.PrescriptionUseCase
import de.gematik.ti.erp.app.profiles.usecase.ProfilesUseCase
import de.gematik.ti.erp.app.settings.usecase.SettingsUseCase
import de.gematik.ti.erp.app.utils.CoroutineTestRule
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.impl.annotations.RelaxedMockK
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runBlockingTest
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@ExperimentalCoroutinesApi
class PrescriptionViewModelTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    @get:Rule
    val coroutineRule = CoroutineTestRule()

    @MockK
    private lateinit var viewModel: PrescriptionViewModel

    @RelaxedMockK
    private lateinit var prescriptionUseCase: PrescriptionUseCase

    @RelaxedMockK
    private lateinit var profilesUseCase: ProfilesUseCase

    @MockK
    private lateinit var demoUseCase: DemoUseCase

    @MockK
    private lateinit var hintUseCase: HintUseCase

    @MockK
    private lateinit var settingsUseCase: SettingsUseCase

    @MockK
    private lateinit var authenticationUseCase: AuthenticationUseCase

    @Before
    fun setup() {
        MockKAnnotations.init(this)

        every { prescriptionUseCase.syncedRecipes() } answers { flowOf(listOf()) }
        every { prescriptionUseCase.scannedRecipes() } answers { flowOf(listOf()) }
        every { prescriptionUseCase.redeemedPrescriptions() } answers { flowOf(listOf()) }

        every { demoUseCase.demoModeActive } answers { MutableStateFlow(false) }
        every { demoUseCase.isDemoModeActive } answers { false }
        every { demoUseCase.demoModeHasBeenSeen } answers { false }
        every { hintUseCase.cancelledHints } returns flowOf(setOf())
        every { hintUseCase.isHintCanceled(PrescriptionScreenHintTryDemoMode) } answers { false }
        every { hintUseCase.isHintCanceled(PrescriptionScreenHintDemoModeActivated) } answers { false }
    }

    private fun instantiateViewModel() {
        viewModel = PrescriptionViewModel(
            prescriptionUseCase = prescriptionUseCase,
            profilesUseCase = profilesUseCase,
            settingsUseCase = settingsUseCase,
            demoUseCase = demoUseCase,
            dispatchProvider = coroutineRule.testDispatchProvider,
            hintUseCase = hintUseCase,
            authenticationUseCase = authenticationUseCase
        )
    }

    @Test
    fun `if demo is inactive and no security setting is selected - app security hint should be visible`() =
        coroutineRule.testDispatcher.runBlockingTest {
            every { settingsUseCase.settings } answers {
                flowOf(
                    Settings(
                        authenticationMethod = SettingsAuthenticationMethod.Unspecified,
                        authenticationFails = 0,
                        zoomEnabled = false
                    )
                )
            }
            every { demoUseCase.authTokenReceived } answers { MutableStateFlow(false) }

            instantiateViewModel()

            viewModel.screenState().first().hints.let { hints ->
                assertTrue(
                    hints.first() == PrescriptionScreenHintDefineSecurity
                )
            }
        }

    @Test
    fun `if demo is active and no security setting is selected - welcome to demoMode should be at first place`() =
        coroutineRule.testDispatcher.runBlockingTest {
            every { settingsUseCase.settings } answers {
                flowOf(
                    Settings(
                        authenticationMethod = SettingsAuthenticationMethod.Unspecified,
                        authenticationFails = 0,
                        zoomEnabled = false
                    )
                )
            }

            every { demoUseCase.isDemoModeActive } answers { true }
            every { demoUseCase.demoModeActive } answers { MutableStateFlow(true) }
            every { demoUseCase.authTokenReceived } answers { MutableStateFlow(false) }

            instantiateViewModel()

            viewModel.screenState().first().hints.let { hints ->
                assertTrue(
                    hints.first() == PrescriptionScreenHintDemoModeActivated
                )
            }
        }

    @Test
    fun `if demo is inactive and some auth is selected - app auth hint should be gone and link to demo mode should be in first place`() =
        coroutineRule.testDispatcher.runBlockingTest {
            every { settingsUseCase.settings } answers {
                flowOf(
                    Settings(
                        authenticationMethod = SettingsAuthenticationMethod.None,
                        authenticationFails = 0,
                        zoomEnabled = false
                    )
                )
            }

            instantiateViewModel()

            viewModel.screenState().first().hints.let { hints ->
                assertTrue(
                    hints.first() == PrescriptionScreenHintTryDemoMode
                )

                assertNull(
                    hints.find { it == PrescriptionScreenHintDefineSecurity }
                )
            }
        }

    @Test
    fun `if demo mode was already activated  - demo hint is not available anymore`() =
        coroutineRule.testDispatcher.runBlockingTest {
            every { settingsUseCase.settings } answers {
                flowOf(
                    Settings(
                        authenticationMethod = SettingsAuthenticationMethod.None,
                        authenticationFails = 0,
                        zoomEnabled = false
                    )
                )
            }
            every { demoUseCase.demoModeHasBeenSeen } answers { true }

            instantiateViewModel()

            assertNull(
                viewModel.screenState()
                    .first().hints.find { it == PrescriptionScreenHintTryDemoMode }
            )
        }

    @Test
    fun `if demo mode was canceled  - demo hint is not available anymore`() =
        coroutineRule.testDispatcher.runBlockingTest {
            every { settingsUseCase.settings } answers {
                flowOf(
                    Settings(
                        authenticationMethod = SettingsAuthenticationMethod.None,
                        authenticationFails = 0,
                        zoomEnabled = false
                    )
                )
            }

            every { hintUseCase.cancelledHints } returns flowOf(
                setOf(
                    PrescriptionScreenHintTryDemoMode
                )
            )
            every { hintUseCase.isHintCanceled(PrescriptionScreenHintTryDemoMode) } answers { true }

            instantiateViewModel()

            assertNull(
                viewModel.screenState()
                    .first().hints.find { it == PrescriptionScreenHintTryDemoMode }
            )
        }

    @Test
    fun `if demo mode is active and welcome hint has been canceled  - welcome to demo hint is not available anymore`() =
        coroutineRule.testDispatcher.runBlockingTest {
            every { settingsUseCase.settings } answers {
                flowOf(
                    Settings(
                        authenticationMethod = SettingsAuthenticationMethod.None,
                        authenticationFails = 0,
                        zoomEnabled = false
                    )
                )
            }

            every { demoUseCase.demoModeActive } answers { MutableStateFlow(true) }
            every { hintUseCase.cancelledHints } returns flowOf(
                setOf(
                    PrescriptionScreenHintDemoModeActivated
                )
            )

            instantiateViewModel()

            assertNull(
                viewModel.screenState()
                    .first().hints.find { it == PrescriptionScreenHintDemoModeActivated }
            )
        }
}
