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

package de.gematik.ti.erp.app.settings.usecase

import de.gematik.ti.erp.app.profile.model.ProfileErpModel
import de.gematik.ti.erp.app.profiles.repository.ProfileRepository
import de.gematik.ti.erp.app.settings.model.AppVersionErpModel
import de.gematik.ti.erp.app.settings.model.SettingsErpModel
import de.gematik.ti.erp.app.settings.model.ThemeMode
import de.gematik.ti.erp.app.settings.repository.DefaultSettingsRepository
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class GetOnboardingSucceededUseCaseTest {

    private lateinit var getOnboardingSucceededUseCase: GetOnboardingSucceededUseCase

    @MockK(relaxed = true)
    private lateinit var settingsRepository: DefaultSettingsRepository

    @MockK(relaxed = true)
    private lateinit var profileRepository: ProfileRepository

    @Before
    fun setup() {
        MockKAnnotations.init(this)
        every { profileRepository.profiles() } returns flowOf(emptyList())

        getOnboardingSucceededUseCase = GetOnboardingSucceededUseCase(settingsRepository, profileRepository)
    }

    @Test
    fun `get onboarding succeeded should answer true`() = runTest {
        coEvery { settingsRepository.loadSettings() } coAnswers {
            flowOf(
                SettingsErpModel(
                    latestAppVersion = AppVersionErpModel("", 0),
                    onboardingShownIn = AppVersionErpModel("", 0),
                    welcomeDrawerShown = true,
                    theme = ThemeMode.SYSTEM,
                    zoomEnabled = false,
                    userHasAcceptedInsecureDevice = false,
                    userHasAcceptedIntegrityNotOk = false,
                    trackingAllowed = false,
                    screenShotsAllowed = false
                )
            )
        }
        val activeProfile = mockk<ProfileErpModel>(relaxed = true)
        every { activeProfile.active } returns true
        every { profileRepository.profiles() } returns flowOf(listOf(activeProfile))
        assertTrue { getOnboardingSucceededUseCase() }
    }

    @Test
    fun `onboarding shown without an active profile should answer false`() = runTest {
        coEvery { settingsRepository.loadSettings() } returns flowOf(
            SettingsErpModel(
                latestAppVersion = AppVersionErpModel("", 0),
                onboardingShownIn = AppVersionErpModel("", 0),
                welcomeDrawerShown = true,
                theme = ThemeMode.SYSTEM,
                zoomEnabled = false,
                userHasAcceptedInsecureDevice = false,
                userHasAcceptedIntegrityNotOk = false,
                trackingAllowed = false,
                screenShotsAllowed = false
            )
        )
        every { profileRepository.profiles() } returns flowOf(emptyList())
        assertFalse { getOnboardingSucceededUseCase() }
    }

    @Test
    fun `get onboarding succeeded answer false`() = runTest {
        coEvery { settingsRepository.loadSettings() } coAnswers {
            flowOf(
                SettingsErpModel(
                    latestAppVersion = AppVersionErpModel("", 0),
                    onboardingShownIn = null,
                    welcomeDrawerShown = true,
                    theme = ThemeMode.SYSTEM,
                    zoomEnabled = false,
                    userHasAcceptedInsecureDevice = false,
                    userHasAcceptedIntegrityNotOk = false,
                    trackingAllowed = false,
                    screenShotsAllowed = false
                )
            )
        }
        assertFalse { getOnboardingSucceededUseCase() }
    }
}
