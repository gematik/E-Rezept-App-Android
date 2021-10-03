/*
 * Copyright (c) 2021 gematik GmbH
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
    private lateinit var useCase: SettingsUseCase

    @MockK
    private lateinit var safetynetUseCase: SafetynetUseCase

    @Before
    fun setup() {
        MockKAnnotations.init(this)
        every { safetynetUseCase.runSafetynetAttestation() } returns flow { emit(true) }
    }

    @Test
    fun `test showInsecureDevicePrompt - only show once`() = coroutineRule.testDispatcher.runBlockingTest {
        every { useCase.showInsecureDevicePrompt } returns flowOf(true)
        every { useCase.isNewUser } returns false
        viewModel = MainViewModel(useCase, safetynetUseCase)

        assertEquals(true, viewModel.showInsecureDevicePrompt.first())
        assertEquals(false, viewModel.showInsecureDevicePrompt.first())
    }

    @Test
    fun `test showInsecureDevicePrompt - device is secure`() = coroutineRule.testDispatcher.runBlockingTest {
        every { useCase.showInsecureDevicePrompt } returns flowOf(false)
        every { useCase.isNewUser } returns false

        viewModel = MainViewModel(useCase, safetynetUseCase)

        assertEquals(false, viewModel.showInsecureDevicePrompt.first())
        assertEquals(false, viewModel.showInsecureDevicePrompt.first())
    }
}
