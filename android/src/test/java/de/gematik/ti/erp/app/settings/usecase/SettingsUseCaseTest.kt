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

package de.gematik.ti.erp.app.settings.usecase

import de.gematik.ti.erp.app.settings.model.SettingsData
import de.gematik.ti.erp.app.settings.model.SettingsData.AppVersion
import de.gematik.ti.erp.app.settings.repository.SettingsRepository
import io.mockk.MockKAnnotations
import io.mockk.coVerify
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import java.time.Instant
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

@OptIn(ExperimentalCoroutinesApi::class)
class SettingsUseCaseTest {
    private lateinit var settings: SettingsUseCase

    @MockK(relaxed = true)
    private lateinit var settingsRepository: SettingsRepository

    @BeforeTest
    fun setup() {
        MockKAnnotations.init(this)

        initSettings()
    }

    private fun initSettings() {
        settings = SettingsUseCase(
            context = mockk(),
            settingsRepository = settingsRepository
        )
    }

    @Test
    fun `accept onboarding`() = runTest {
        val now = Instant.now()
        settings.onboardingSucceeded(
            authenticationMode = SettingsData.AuthenticationMode.Unspecified,
            "Profil 1",
            now = now
        )

        coVerify(exactly = 1) {
            settingsRepository.saveOnboardingSucceededData(
                SettingsData.AuthenticationMode.Unspecified,
                "Profil 1",
                now
            )
        }
    }

    @Test
    fun `show data terms update - data accepted in the past`() = runTest {
        every { settingsRepository.general } returns flowOf(
            SettingsData.General(
                latestAppVersion = AppVersion(code = 1, name = "Test"),
                onboardingShownIn = null,
                dataProtectionVersionAcceptedOn = DATA_PROTECTION_LAST_UPDATED.minusSeconds(1000),
                zoomEnabled = false,
                userHasAcceptedInsecureDevice = false,
                authenticationFails = 0,
                welcomeDrawerShown = false
            )
        )

        initSettings()

        assertEquals(true, settings.showDataTermsUpdate.first())
    }

    @Test
    fun `show data terms update - data already accepted`() = runTest {
        every { settingsRepository.general } returns flowOf(
            SettingsData.General(
                latestAppVersion = AppVersion(code = 1, name = "Test"),
                onboardingShownIn = null,
                dataProtectionVersionAcceptedOn = DATA_PROTECTION_LAST_UPDATED.plusSeconds(1000),
                zoomEnabled = false,
                userHasAcceptedInsecureDevice = false,
                authenticationFails = 0,
                welcomeDrawerShown = false
            )
        )

        initSettings()

        assertEquals(false, settings.showDataTermsUpdate.first())
    }

    @Test
    fun `show welcome drawer`() = runTest {
        every { settingsRepository.general } returns flowOf(
            SettingsData.General(
                latestAppVersion = AppVersion(code = 1, name = "Test"),
                onboardingShownIn = null,
                dataProtectionVersionAcceptedOn = DATA_PROTECTION_LAST_UPDATED.plusSeconds(1000),
                zoomEnabled = false,
                userHasAcceptedInsecureDevice = false,
                authenticationFails = 0,
                welcomeDrawerShown = false
            )
        )
        initSettings()

        assertEquals(true, settings.showWelcomeDrawer.first())
    }

    @Test
    fun `don't show welcome drawer`() = runTest {
        every { settingsRepository.general } returns flowOf(
            SettingsData.General(
                latestAppVersion = AppVersion(code = 1, name = "Test"),
                onboardingShownIn = null,
                dataProtectionVersionAcceptedOn = DATA_PROTECTION_LAST_UPDATED.plusSeconds(1000),
                zoomEnabled = false,
                userHasAcceptedInsecureDevice = false,
                authenticationFails = 0,
                welcomeDrawerShown = true
            )
        )
        initSettings()

        assertEquals(false, settings.showWelcomeDrawer.first())
    }
}
