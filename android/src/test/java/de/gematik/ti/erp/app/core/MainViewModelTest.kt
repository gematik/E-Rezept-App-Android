/*
 * Copyright (c) 2023 gematik GmbH
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

package de.gematik.ti.erp.app.core

import de.gematik.ti.erp.app.CoroutineTestRule
import de.gematik.ti.erp.app.attestation.usecase.IntegrityUseCase
import de.gematik.ti.erp.app.settings.model.SettingsData
import de.gematik.ti.erp.app.settings.model.SettingsData.AppVersion
import de.gematik.ti.erp.app.settings.model.SettingsData.AuthenticationMode
import de.gematik.ti.erp.app.settings.usecase.SettingsUseCase
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import java.time.Instant

@OptIn(ExperimentalCoroutinesApi::class)
class MainViewModelTest {
    @get:Rule
    val coroutineRule = CoroutineTestRule()

    private lateinit var viewModel: MainViewModel

    @MockK
    private lateinit var settingsUseCase: SettingsUseCase

    @MockK
    private lateinit var integrityUseCase: IntegrityUseCase

    @Before
    fun setup() {
        MockKAnnotations.init(this)
        every { integrityUseCase.runIntegrityAttestation() } returns flow { emit(true) }
        every { settingsUseCase.general } returns flowOf(
            SettingsData.General(
                latestAppVersion = AppVersion(code = 1, name = "Test"),
                onboardingShownIn = null,
                dataProtectionVersionAcceptedOn = Instant.now(),
                zoomEnabled = false,
                userHasAcceptedInsecureDevice = false,
                authenticationFails = 0,
                welcomeDrawerShown = false,
                mainScreenTooltipsShown = false,
                mlKitAccepted = false
            )
        )
        every { settingsUseCase.authenticationMode } returns flowOf(AuthenticationMode.Unspecified)
    }

    @Test
    fun `test showInsecureDevicePrompt - only show once`() = runTest {
        every { settingsUseCase.showDataTermsUpdate } returns flowOf(false)
        every { settingsUseCase.showInsecureDevicePrompt } returns flowOf(true)
        every { settingsUseCase.showOnboarding } returns flowOf(false)
        every { settingsUseCase.showWelcomeDrawer } returns flowOf(false)
        every { settingsUseCase.showMainScreenTooltip } returns flowOf(false)

        viewModel = MainViewModel(integrityUseCase, settingsUseCase)

        assertEquals(true, viewModel.showInsecureDevicePrompt.first())
        assertEquals(false, viewModel.showInsecureDevicePrompt.first())
    }

    @Test
    fun `test showInsecureDevicePrompt - device is secure`() = runTest {
        every { settingsUseCase.showDataTermsUpdate } returns flowOf(false)
        every { settingsUseCase.showInsecureDevicePrompt } returns flowOf(false)
        every { settingsUseCase.showOnboarding } returns flowOf(false)
        every { settingsUseCase.showWelcomeDrawer } returns flowOf(false)
        every { settingsUseCase.showMainScreenTooltip } returns flowOf(false)

        viewModel = MainViewModel(integrityUseCase, settingsUseCase)

        assertEquals(false, viewModel.showInsecureDevicePrompt.first())
        assertEquals(false, viewModel.showInsecureDevicePrompt.first())
    }

    @Test
    fun `test showDataTermsUpdate - dataTerms updates should be shown`() = runTest {
        every { settingsUseCase.showDataTermsUpdate } returns flowOf(true)
        every { settingsUseCase.showInsecureDevicePrompt } returns flowOf(false)
        every { settingsUseCase.showOnboarding } returns flowOf(false)
        every { settingsUseCase.showWelcomeDrawer } returns flowOf(false)
        every { settingsUseCase.showMainScreenTooltip } returns flowOf(false)

        viewModel = MainViewModel(integrityUseCase, settingsUseCase)

        assertEquals(true, viewModel.showDataTermsUpdate.first())
    }

    @Test
    fun `test showDataTermsUpdate - dataTerms updates should not be shown`() = runTest {
        every { settingsUseCase.showDataTermsUpdate } returns flowOf(false)
        every { settingsUseCase.showInsecureDevicePrompt } returns flowOf(false)
        every { settingsUseCase.showOnboarding } returns flowOf(false)
        every { settingsUseCase.showWelcomeDrawer } returns flowOf(false)
        every { settingsUseCase.showMainScreenTooltip } returns flowOf(false)

        viewModel = MainViewModel(integrityUseCase, settingsUseCase)

        assertEquals(false, viewModel.showDataTermsUpdate.first())
    }
}
