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

import de.gematik.ti.erp.app.authentication.presentation.BiometricAuthenticator
import de.gematik.ti.erp.app.authentication.usecase.ChooseAuthenticationDataUseCase
import de.gematik.ti.erp.app.base.NetworkStatusTracker
import de.gematik.ti.erp.app.eurezept.domain.usecase.GetCountryLocaleRedemptionCodeUseCase
import de.gematik.ti.erp.app.eurezept.model.MockEuTestData.mockCountrySpecificLabels
import de.gematik.ti.erp.app.eurezept.model.MockEuTestData.mockEuAccessCode
import de.gematik.ti.erp.app.eurezept.model.MockEuTestData.mockEuRedemptionDetails
import de.gematik.ti.erp.app.eurezept.model.MockEuTestData.mockTtsLocale
import de.gematik.ti.erp.app.eurezept.presentation.EuRedemptionCodeController
import de.gematik.ti.erp.app.eurezept.util.TextToSpeechManager
import de.gematik.ti.erp.app.idp.repository.IdpRepository
import de.gematik.ti.erp.app.profiles.repository.ProfileRepository
import de.gematik.ti.erp.app.profiles.usecase.GetActiveProfileUseCase
import de.gematik.ti.erp.app.profiles.usecase.GetProfileByIdUseCase
import de.gematik.ti.erp.app.profiles.usecase.GetProfilesUseCase
import io.mockk.coEvery
import io.mockk.mockk
import io.mockk.verify
import junit.framework.TestCase.assertFalse
import junit.framework.TestCase.assertTrue
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
import kotlin.test.Test
import kotlin.test.assertEquals

@OptIn(ExperimentalCoroutinesApi::class)
class EuRedemptionCodeControllerTest {

    private val textToSpeechManager: TextToSpeechManager = mockk(relaxed = true)
    private val getCountryLocaleRedemptionCodeUseCase: GetCountryLocaleRedemptionCodeUseCase = mockk()

    private val profileRepository: ProfileRepository = mockk()
    private val idpRepository: IdpRepository = mockk()

    private lateinit var getActiveProfileUseCase: GetActiveProfileUseCase
    private lateinit var getProfileByIdUseCase: GetProfileByIdUseCase
    private lateinit var getProfilesUseCase: GetProfilesUseCase
    private lateinit var chooseAuthenticationDataUseCase: ChooseAuthenticationDataUseCase

    private val networkStatusTracker: NetworkStatusTracker = mockk()
    private val biometricAuthenticator: BiometricAuthenticator = mockk(relaxed = true)

    private val dispatcher = StandardTestDispatcher()
    private val testScope = TestScope(dispatcher)

    private val mockCountryCode = "DE"
    private lateinit var controller: EuRedemptionCodeController

    @Before
    fun setup() {
        Dispatchers.setMain(dispatcher)

        getActiveProfileUseCase = GetActiveProfileUseCase(profileRepository, dispatcher)
        getProfileByIdUseCase = GetProfileByIdUseCase(profileRepository, dispatcher)
        getProfilesUseCase = GetProfilesUseCase(profileRepository, dispatcher)
        chooseAuthenticationDataUseCase = ChooseAuthenticationDataUseCase(profileRepository, idpRepository, dispatcher)

        val mockProfileData = mockk<de.gematik.ti.erp.app.profiles.model.ProfilesData.Profile>(relaxed = true)
        coEvery { profileRepository.activeProfile() } returns flowOf(mockProfileData)
        coEvery { profileRepository.getProfileById(any()) } returns flowOf(mockProfileData)
        coEvery { profileRepository.profiles() } returns flowOf(listOf(mockProfileData))
        coEvery { profileRepository.updateLastAuthenticated(any(), any()) } returns Unit

        val mockAuthData = mockk<de.gematik.ti.erp.app.idp.model.IdpData.AuthenticationData>(relaxed = true)
        coEvery { idpRepository.authenticationData(any()) } returns flowOf(mockAuthData)

        coEvery { networkStatusTracker.networkStatus } returns flowOf(true)

        coEvery { getCountryLocaleRedemptionCodeUseCase(mockCountryCode) } returns mockCountrySpecificLabels
        coEvery { getCountryLocaleRedemptionCodeUseCase.getLocaleForTTS(mockCountryCode) } returns mockTtsLocale

        controller = EuRedemptionCodeController(
            textToSpeechManager = textToSpeechManager,
            selectedCountryCode = mockCountryCode,
            getCountryLocaleRedemptionCodeUseCase = getCountryLocaleRedemptionCodeUseCase,
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
    fun `controller initializes with correct country specific labels`() = testScope.runTest {
        advanceUntilIdle()

        val labels = controller.countrySpecificLabels.value
        assertEquals(mockCountrySpecificLabels.codeLabel, labels.codeLabel)
        assertEquals(mockCountrySpecificLabels.insuranceNumberLabel, labels.insuranceNumberLabel)

        verify { textToSpeechManager.initialize() }
    }

    @Test
    fun `qr code visibility toggles correctly`() = testScope.runTest {
        advanceUntilIdle()

        assertFalse(controller.isQrCodeVisible.value)

        controller.toggleQrCodeView()
        assertTrue(controller.isQrCodeVisible.value)

        controller.toggleQrCodeView()
        assertFalse(controller.isQrCodeVisible.value)
    }

    @Test
    fun `onPlayInsuranceNumberAudio plays insurance number with correct TTS settings`() = testScope.runTest {
        advanceUntilIdle()

        val testRedemptionDetails = mockEuRedemptionDetails

        controller.onPlayInsuranceNumberAudio(testRedemptionDetails)

        verify {
            textToSpeechManager.speakWithLocale(
                text = mockCountrySpecificLabels.insuranceNumberLabel,
                locale = mockTtsLocale,
                speechRate = 1.0f,
                utteranceId = "insurance_label",
                addPauses = false,
                spellOutCharacters = false
            )
        }

        verify {
            textToSpeechManager.speakQueuedWithLocale(
                text = mockEuRedemptionDetails.insuranceNumber,
                locale = mockTtsLocale,
                speechRate = 1.0f,
                utteranceId = "insurance_number",
                addPauses = true,
                spellOutCharacters = true
            )
        }
    }

    @Test
    fun `onPlayInsuranceNumberAudio does nothing when codeData is null`() = testScope.runTest {
        advanceUntilIdle()

        controller.onPlayInsuranceNumberAudio(null)

        verify(exactly = 0) {
            textToSpeechManager.speakWithLocale(any(), any(), any(), any(), any(), any())
        }
        verify(exactly = 0) {
            textToSpeechManager.speakQueuedWithLocale(any(), any(), any(), any(), any(), any())
        }
    }

    @Test
    fun `onPlayCodeAudio plays access code with correct TTS settings`() = testScope.runTest {
        advanceUntilIdle()

        val testRedemptionDetails = mockEuRedemptionDetails

        controller.onPlayCodeAudio(testRedemptionDetails)

        verify {
            textToSpeechManager.speakWithLocale(
                text = mockCountrySpecificLabels.codeLabel,
                locale = mockTtsLocale,
                speechRate = 1.0f,
                utteranceId = "code_label",
                addPauses = false,
                spellOutCharacters = false
            )
        }

        verify {
            textToSpeechManager.speakQueuedWithLocale(
                text = mockEuAccessCode.accessCode,
                locale = mockTtsLocale,
                speechRate = 1.0f,
                utteranceId = "redemption_code",
                addPauses = true,
                spellOutCharacters = true
            )
        }
    }

    @Test
    fun `onPlayCodeAudio does nothing when codeData is null`() = testScope.runTest {
        advanceUntilIdle()

        controller.onPlayCodeAudio(null)

        verify(exactly = 0) {
            textToSpeechManager.speakWithLocale(any(), any(), any(), any(), any(), any())
        }
        verify(exactly = 0) {
            textToSpeechManager.speakQueuedWithLocale(any(), any(), any(), any(), any(), any())
        }
    }
}
