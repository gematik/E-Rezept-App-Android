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

package de.gematik.ti.erp.app.eurezept.controller

import android.content.Context
import de.gematik.ti.erp.app.authentication.presentation.BiometricAuthenticator
import de.gematik.ti.erp.app.authentication.usecase.ChooseAuthenticationDataUseCase
import de.gematik.ti.erp.app.base.NetworkStatusTracker
import de.gematik.ti.erp.app.base.usecase.TriggerNavigationUseCase
import de.gematik.ti.erp.app.eurezept.domain.model.Country
import de.gematik.ti.erp.app.eurezept.domain.usecase.GetPrescriptionPhrasesUseCase
import de.gematik.ti.erp.app.eurezept.model.MockEuTestData
import de.gematik.ti.erp.app.eurezept.model.MockEuTestData.mockCountryPhrases
import de.gematik.ti.erp.app.eurezept.model.MockEuTestData.mockSupportedCountries
import de.gematik.ti.erp.app.eurezept.presentation.EuInstructionController
import de.gematik.ti.erp.app.eurezept.ui.model.EuRedeemSelector.WAS_EU_REDEEM_INSTRUCTION_VIEWED
import de.gematik.ti.erp.app.idp.model.IdpData
import de.gematik.ti.erp.app.idp.repository.IdpRepository
import de.gematik.ti.erp.app.localization.CountryCode
import de.gematik.ti.erp.app.localization.GetSupportedCountriesFromXmlUseCase
import de.gematik.ti.erp.app.navigation.triggers.NavigationTriggerDataStore
import de.gematik.ti.erp.app.profiles.repository.ProfileRepository
import de.gematik.ti.erp.app.profiles.usecase.GetActiveProfileUseCase
import de.gematik.ti.erp.app.profiles.usecase.GetProfileByIdUseCase
import de.gematik.ti.erp.app.profiles.usecase.GetProfilesUseCase
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
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
import org.junit.After
import org.junit.Before
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

@OptIn(ExperimentalCoroutinesApi::class)
class EuInstructionControllerTest {

    private val dispatcher = StandardTestDispatcher()
    private val testScope = TestScope(dispatcher)

    private val profileRepository: ProfileRepository = mockk()
    private val idpRepository: IdpRepository = mockk()

    private val navigationTriggerDataStore: NavigationTriggerDataStore = mockk()
    private val getSupportedCountriesFromXmlUseCase: GetSupportedCountriesFromXmlUseCase = mockk()
    private val context: Context = mockk()
    private val networkStatusTracker: NetworkStatusTracker = mockk(relaxed = true)
    private val biometricAuthenticator: BiometricAuthenticator = mockk(relaxed = true)

    private lateinit var triggerNavigationUseCase: TriggerNavigationUseCase
    private lateinit var getPrescriptionPhrasesUseCase: GetPrescriptionPhrasesUseCase
    private lateinit var getActiveProfileUseCase: GetActiveProfileUseCase
    private lateinit var getProfileByIdUseCase: GetProfileByIdUseCase
    private lateinit var getProfilesUseCase: GetProfilesUseCase
    private lateinit var chooseAuthenticationDataUseCase: ChooseAuthenticationDataUseCase

    private lateinit var controller: EuInstructionController

    @Before
    fun setup() {
        Dispatchers.setMain(dispatcher)

        triggerNavigationUseCase = TriggerNavigationUseCase(navigationTriggerDataStore, dispatcher)
        getPrescriptionPhrasesUseCase = mockk()
        getActiveProfileUseCase = GetActiveProfileUseCase(profileRepository, dispatcher)
        getProfileByIdUseCase = GetProfileByIdUseCase(profileRepository, dispatcher)
        getProfilesUseCase = GetProfilesUseCase(profileRepository, dispatcher)
        chooseAuthenticationDataUseCase = ChooseAuthenticationDataUseCase(profileRepository, idpRepository, dispatcher)

        val mockProfileData = MockEuTestData.profileData

        coEvery { profileRepository.profiles() } returns flowOf(listOf(mockProfileData))
        coEvery { profileRepository.activeProfile() } returns flowOf(mockProfileData)
        coEvery { profileRepository.getProfileById(any()) } returns flowOf(mockProfileData)
        coEvery { profileRepository.updateLastAuthenticated(any(), any()) } returns Unit

        val mockAuthData = IdpData.AuthenticationData(
            singleSignOnTokenScope = MockEuTestData.mockValidSsoTokenScope
        )
        coEvery { idpRepository.authenticationData(any()) } returns flowOf(mockAuthData)

        every { getSupportedCountriesFromXmlUseCase.invoke() } returns mockSupportedCountries
        every { getPrescriptionPhrasesUseCase.invoke(any()) } returns mockCountryPhrases
        controller = EuInstructionController(
            getSupportedCountriesFromXmlUseCase = getSupportedCountriesFromXmlUseCase,
            getPrescriptionPhrasesUseCase = getPrescriptionPhrasesUseCase,
            triggerNavigationUseCase = triggerNavigationUseCase,
            getProfileByIdUseCase = getProfileByIdUseCase,
            getProfilesUseCase = getProfilesUseCase,
            getActiveProfileUseCase = getActiveProfileUseCase,
            chooseAuthenticationDataUseCase = chooseAuthenticationDataUseCase,
            networkStatusTracker = networkStatusTracker,
            biometricAuthenticator = biometricAuthenticator
        )
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `markInstructionsViewed calls triggerNavigationUseCase with correct parameter`() {
        coEvery { navigationTriggerDataStore.triggerNavigation(any()) } returns Unit

        testScope.runTest {
            controller.markInstructionsViewed()
            advanceUntilIdle()

            coVerify(exactly = 1) {
                navigationTriggerDataStore.triggerNavigation(WAS_EU_REDEEM_INSTRUCTION_VIEWED.name)
            }
        }
    }

    @Test
    fun `findSupportedCountryByCode returns correct country code for valid code`() = testScope.runTest {
        val result = controller.findSupportedCountryByCode(CountryCode.DE.code)

        assertNotNull(result)
        assertEquals(CountryCode.DE, result)
    }

    @Test
    fun `findSupportedCountryByCode returns null for null and blank code`() = testScope.runTest {
        val resultNull = controller.findSupportedCountryByCode(null)
        val resultBlank = controller.findSupportedCountryByCode("   ")

        assertNull(resultNull)
        assertNull(resultBlank)
    }

    @Test
    fun `findSupportedCountryByCode returns null for unsupported country code`() = testScope.runTest {
        val result = controller.findSupportedCountryByCode("US")

        assertNull(result)
    }

    @Test
    fun `getPhrasesForCountry returns UK phrases as fallback when country is null`() {
        testScope.runTest {
            val result = controller.getPhrasesForCountry(null)

            assertEquals(mockCountryPhrases, result)
        }
    }

    @Test
    fun `getPhrasesForCountry returns UK phrases as fallback when country code is unsupported`() {
        testScope.runTest {
            val unsupportedCountry = Country("United States", "US", "🇺🇸")
            val result = controller.getPhrasesForCountry(unsupportedCountry)

            assertEquals(mockCountryPhrases, result)
        }
    }

    @Test
    fun `getPhrasesForCountry returns correct phrases for supported country`() {
        testScope.runTest {
            val supportedCountry = Country("Germany", "DE", "🇩🇪")
            val result = controller.getPhrasesForCountry(supportedCountry)

            assertEquals(mockCountryPhrases, result)
        }
    }
}
