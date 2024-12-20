/*
 * Copyright 2024, gematik GmbH
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
 * In case of changes by gematik find details in the "Readme" file.
 *
 * See the Licence for the specific language governing permissions and limitations under the Licence.
 */

package de.gematik.ti.erp.app.settings.usecase

import de.gematik.ti.erp.app.settings.model.SettingsData
import de.gematik.ti.erp.app.settings.repository.DefaultSettingsRepository
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

class GetCanStartToolTipsUseCaseTest {

    private lateinit var getCanStartToolTipsUseCase: GetCanStartToolTipsUseCase

    @MockK(relaxed = true)
    private lateinit var settingsRepository: DefaultSettingsRepository

    @Before
    fun setup() {
        MockKAnnotations.init(this)

        getCanStartToolTipsUseCase = GetCanStartToolTipsUseCase(settingsRepository)
    }

    @Test
    fun `can start tooltips should answer true`() = runTest {
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
                    mlKitAccepted = false,
                    trackingAllowed = false,
                    screenShotsAllowed = false
                )
            )
        }
        assertTrue { getCanStartToolTipsUseCase().first() }
    }

    @Test
    fun `can start tooltips should answer false (welcome drawer not shown)`() = runTest {
        coEvery { settingsRepository.general } coAnswers {
            flowOf(
                SettingsData.General(
                    latestAppVersion = SettingsData.AppVersion(0, ""),
                    onboardingShownIn = SettingsData.AppVersion(0, ""),
                    welcomeDrawerShown = false,
                    mainScreenTooltipsShown = false,
                    zoomEnabled = false,
                    userHasAcceptedInsecureDevice = false,
                    userHasAcceptedIntegrityNotOk = false,
                    mlKitAccepted = false,
                    trackingAllowed = false,
                    screenShotsAllowed = false
                )
            )
        }
        assertFalse { getCanStartToolTipsUseCase().first() }
    }

    @Test
    fun `can start tooltips should answer false (onboarding not shown)`() = runTest {
        coEvery { settingsRepository.general } coAnswers {
            flowOf(
                SettingsData.General(
                    latestAppVersion = SettingsData.AppVersion(0, ""),
                    onboardingShownIn = null,
                    welcomeDrawerShown = true,
                    mainScreenTooltipsShown = false,
                    zoomEnabled = false,
                    userHasAcceptedInsecureDevice = false,
                    userHasAcceptedIntegrityNotOk = false,
                    mlKitAccepted = false,
                    trackingAllowed = false,
                    screenShotsAllowed = false
                )
            )
        }
        assertFalse { getCanStartToolTipsUseCase().first() }
    }

    @Test
    fun `can start tooltips should answer false (tool tips already shown)`() = runTest {
        coEvery { settingsRepository.general } coAnswers {
            flowOf(
                SettingsData.General(
                    latestAppVersion = SettingsData.AppVersion(0, ""),
                    onboardingShownIn = null,
                    welcomeDrawerShown = true,
                    mainScreenTooltipsShown = true,
                    zoomEnabled = false,
                    userHasAcceptedInsecureDevice = false,
                    userHasAcceptedIntegrityNotOk = false,
                    mlKitAccepted = false,
                    trackingAllowed = false,
                    screenShotsAllowed = false
                )
            )
        }
        assertFalse { getCanStartToolTipsUseCase().first() }
    }
}
