/*
 * Copyright (c) 2024 gematik GmbH
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
package de.gematik.ti.erp.app.settings.usecase

import de.gematik.ti.erp.app.settings.model.SettingsData
import de.gematik.ti.erp.app.settings.repository.SettingsRepository
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class GetMLKitAcceptedUseCaseTest {

    private lateinit var getMLKitAcceptedUseCase: GetMLKitAcceptedUseCase

    @MockK(relaxed = true)
    private lateinit var settingsRepository: SettingsRepository

    @Before
    fun setup() {
        MockKAnnotations.init(this)

        getMLKitAcceptedUseCase = GetMLKitAcceptedUseCase(settingsRepository)
    }

    @Test
    fun `get mlKit accepted should answer true`() = runTest {
        coEvery { settingsRepository.general } coAnswers {
            flowOf(
                SettingsData.General(
                    latestAppVersion = SettingsData.AppVersion(0, ""),
                    onboardingShownIn = SettingsData.AppVersion(0, ""),
                    welcomeDrawerShown = true,
                    mainScreenTooltipsShown = false,
                    zoomEnabled = false,
                    userHasAcceptedInsecureDevice = false,
                    userHasAcceptedIntegrityNotOk = false,
                    authenticationFails = 0,
                    mlKitAccepted = true,
                    trackingAllowed = false,
                    screenShotsAllowed = false
                )
            )
        }
        assertTrue { getMLKitAcceptedUseCase().first() }
    }

    @Test
    fun `get mlKit accepted should answer false`() = runTest {
        coEvery { settingsRepository.general } coAnswers {
            flowOf(
                SettingsData.General(
                    latestAppVersion = SettingsData.AppVersion(0, ""),
                    onboardingShownIn = SettingsData.AppVersion(0, ""),
                    welcomeDrawerShown = true,
                    mainScreenTooltipsShown = false,
                    zoomEnabled = false,
                    userHasAcceptedInsecureDevice = false,
                    userHasAcceptedIntegrityNotOk = false,
                    authenticationFails = 0,
                    mlKitAccepted = false,
                    trackingAllowed = false,
                    screenShotsAllowed = false
                )
            )
        }
        assertFalse { getMLKitAcceptedUseCase().first() }
    }
}
