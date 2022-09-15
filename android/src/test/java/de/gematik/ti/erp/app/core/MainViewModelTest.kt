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

package de.gematik.ti.erp.app.core

import de.gematik.ti.erp.app.attestation.usecase.SafetynetUseCase
import de.gematik.ti.erp.app.idp.usecase.IdpUseCase
import de.gematik.ti.erp.app.prescription.usecase.PrescriptionUseCase
import de.gematik.ti.erp.app.profiles.usecase.ProfilesUseCase
import de.gematik.ti.erp.app.settings.usecase.SettingsUseCase
import de.gematik.ti.erp.app.utils.CoroutineTestRule
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runBlockingTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class MainViewModelTest {
    @get:Rule
    val coroutineRule = CoroutineTestRule()

    private lateinit var viewModel: MainViewModel

    @MockK
    private lateinit var settingsUseCase: SettingsUseCase

    @MockK
    private lateinit var prescriptionUseCase: PrescriptionUseCase

    @MockK
    private lateinit var profilesUseCase: ProfilesUseCase

    @MockK(relaxed = true)
    private lateinit var idpUseCase: IdpUseCase

    @MockK
    private lateinit var safetynetUseCase: SafetynetUseCase

    @Before
    fun setup() {
        MockKAnnotations.init(this)
        every { safetynetUseCase.runSafetynetAttestation() } returns flow { emit(true) }
    }

    @Test
    fun `test showInsecureDevicePrompt - only show once`() = coroutineRule.testDispatcher.runBlockingTest {
        every { settingsUseCase.showDataTermsUpdate } returns flowOf(false)
        every { settingsUseCase.showInsecureDevicePrompt } returns flowOf(true)
        every { settingsUseCase.isNewUser } returns false
        every { profilesUseCase.isProfileSetupCompleted() } returns flowOf(true)
        viewModel = MainViewModel(settingsUseCase, safetynetUseCase, profilesUseCase)

        assertEquals(true, viewModel.showInsecureDevicePrompt.first())
        assertEquals(false, viewModel.showInsecureDevicePrompt.first())
    }

    @Test
    fun `test showInsecureDevicePrompt - device is secure`() = coroutineRule.testDispatcher.runBlockingTest {
        every { settingsUseCase.showDataTermsUpdate } returns flowOf(false)
        every { settingsUseCase.showInsecureDevicePrompt } returns flowOf(false)
        every { settingsUseCase.isNewUser } returns false
        every { profilesUseCase.isProfileSetupCompleted() } returns flowOf(true)

        viewModel = MainViewModel(settingsUseCase, safetynetUseCase, profilesUseCase)

        assertEquals(false, viewModel.showInsecureDevicePrompt.first())
        assertEquals(false, viewModel.showInsecureDevicePrompt.first())
    }

    @Test
    fun `test showDataTermsUpdate - dataTerms updates should be shown`() = coroutineRule.testDispatcher.runBlockingTest {
        every { settingsUseCase.showDataTermsUpdate } returns flowOf(true)
        every { settingsUseCase.showInsecureDevicePrompt } returns flowOf(false)
        every { settingsUseCase.isNewUser } returns false
        every { profilesUseCase.isProfileSetupCompleted() } returns flowOf(true)

        viewModel = MainViewModel(settingsUseCase, safetynetUseCase, profilesUseCase)

        assertEquals(true, viewModel.showDataTermsUpdate.first())
    }

    @Test
    fun `test showDataTermsUpdate - dataTerms updates should not be shown`() = coroutineRule.testDispatcher.runBlockingTest {
        every { settingsUseCase.showDataTermsUpdate } returns flowOf(false)
        every { settingsUseCase.showInsecureDevicePrompt } returns flowOf(false)
        every { settingsUseCase.isNewUser } returns false
        every { profilesUseCase.isProfileSetupCompleted() } returns flowOf(true)

        viewModel = MainViewModel(settingsUseCase, safetynetUseCase, profilesUseCase)

        assertEquals(false, viewModel.showDataTermsUpdate.first())
    }
}
