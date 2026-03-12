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
import de.gematik.ti.erp.app.consent.repository.ConsentRepository
import de.gematik.ti.erp.app.consent.usecase.GetConsentUseCase
import de.gematik.ti.erp.app.consent.usecase.GrantConsentUseCase
import de.gematik.ti.erp.app.eurezept.model.MockEuTestData
import de.gematik.ti.erp.app.eurezept.model.MockEuTestData.MOCK_PROFILE_ID
import de.gematik.ti.erp.app.eurezept.model.MockEuTestData.mockInactiveConsent
import de.gematik.ti.erp.app.eurezept.presentation.EuConsentScreenController
import de.gematik.ti.erp.app.fhir.consent.model.ConsentCategory
import de.gematik.ti.erp.app.fhir.constant.consent.ConsentConstants
import de.gematik.ti.erp.app.idp.model.IdpData
import de.gematik.ti.erp.app.idp.repository.IdpRepository
import de.gematik.ti.erp.app.profiles.repository.ProfileRepository
import de.gematik.ti.erp.app.profiles.usecase.GetActiveProfileUseCase
import de.gematik.ti.erp.app.profiles.usecase.GetProfileByIdUseCase
import de.gematik.ti.erp.app.profiles.usecase.GetProfilesUseCase
import de.gematik.ti.erp.app.settings.repository.ConsentVersionRepository
import io.mockk.coEvery
import io.mockk.coVerify
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
import kotlin.test.assertFalse
import kotlin.test.assertNull

@OptIn(ExperimentalCoroutinesApi::class)
class EuConsentScreenControllerTest {

    private val dispatcher = StandardTestDispatcher()
    private val testScope = TestScope(dispatcher)

    private val consentRepository: ConsentRepository = mockk()
    private val profileRepository: ProfileRepository = mockk()
    private val idpRepository: IdpRepository = mockk()
    private val consentVersionRepository: ConsentVersionRepository = mockk()

    private val networkStatusTracker: NetworkStatusTracker = mockk(relaxed = true)
    private val biometricAuthenticator: BiometricAuthenticator = mockk(relaxed = true)

    private lateinit var getEuPrescriptionConsentUseCase: GetConsentUseCase
    private val grantEuPrescriptionConsentUseCase: GrantConsentUseCase = mockk() // Mock instead of real
    private lateinit var getProfilesUseCase: GetProfilesUseCase
    private lateinit var getActiveProfileUseCase: GetActiveProfileUseCase
    private lateinit var getProfileByIdUseCase: GetProfileByIdUseCase
    private lateinit var chooseAuthenticationDataUseCase: ChooseAuthenticationDataUseCase

    private lateinit var controller: EuConsentScreenController

    private fun buildController() = EuConsentScreenController(
        getEuPrescriptionConsentUseCase = getEuPrescriptionConsentUseCase,
        grantEuPrescriptionConsentUseCase = grantEuPrescriptionConsentUseCase,
        getProfilesUseCase = getProfilesUseCase,
        getActiveProfileUseCase = getActiveProfileUseCase,
        chooseAuthenticationDataUseCase = chooseAuthenticationDataUseCase,
        networkStatusTracker = networkStatusTracker,
        biometricAuthenticator = biometricAuthenticator,
        getProfileByIdUseCase = getProfileByIdUseCase
    )

    @Before
    fun setup() {
        Dispatchers.setMain(dispatcher)

        val mockProfileData = MockEuTestData.profileData.copy(
            id = MOCK_PROFILE_ID,
            insuranceIdentifier = "X123456789" // Add insurance identifier for consent tests
        )
        coEvery { consentVersionRepository.getConsentVersion() } returns ConsentConstants.ErpCharge.DEFAULT
        coEvery { profileRepository.profiles() } returns flowOf(listOf(mockProfileData))
        coEvery { profileRepository.activeProfile() } returns flowOf(mockProfileData)
        coEvery { profileRepository.getProfileById(any()) } returns flowOf(mockProfileData)
        coEvery { profileRepository.updateLastAuthenticated(any(), any()) } returns Unit
        coEvery { profileRepository.isSsoTokenValid(any()) } returns flowOf(true)

        getEuPrescriptionConsentUseCase = GetConsentUseCase(consentRepository, Dispatchers.Unconfined)
        // grantEuPrescriptionConsentUseCase is now mocked, no need to create real instance
        getProfilesUseCase = GetProfilesUseCase(profileRepository, Dispatchers.Unconfined)
        getActiveProfileUseCase = GetActiveProfileUseCase(profileRepository, Dispatchers.Unconfined)
        getProfileByIdUseCase = GetProfileByIdUseCase(profileRepository, Dispatchers.Unconfined)
        chooseAuthenticationDataUseCase = ChooseAuthenticationDataUseCase(profileRepository, idpRepository, Dispatchers.Unconfined)

        val mockAuthData = IdpData.AuthenticationData(
            singleSignOnTokenScope = mockk(relaxed = true)
        )
        coEvery { idpRepository.authenticationData(any()) } returns flowOf(mockAuthData)

        coEvery {
            consentRepository.getConsent(
                profileId = MOCK_PROFILE_ID,
                category = ConsentCategory.EUCONSENT.code
            )
        } returns Result.success(mockInactiveConsent)

        coEvery {
            consentRepository.grantConsent(
                profileId = MOCK_PROFILE_ID,
                consent = any()
            )
        } returns Result.success(Unit)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `load consent data successfully with inactive consent`() = testScope.runTest {
        controller = buildController()
        advanceUntilIdle()

        val state = controller.consentViewState.value

        assertEquals(mockInactiveConsent, state.data?.consentData)
        assertFalse(state.data?.isGrantingConsent ?: true)
        assertNull(state.data?.grantConsentError)

        coVerify(exactly = 1) {
            consentRepository.getConsent(
                profileId = MOCK_PROFILE_ID,
                category = ConsentCategory.EUCONSENT.code
            )
        }
    }

    @Test
    fun `load consent data fails with error`() {
        val testException = RuntimeException("Network error")
        coEvery {
            consentRepository.getConsent(
                profileId = MOCK_PROFILE_ID,
                category = ConsentCategory.EUCONSENT.code
            )
        } returns Result.failure(testException)

        testScope.runTest {
            controller = buildController()
            advanceUntilIdle()

            val state = controller.consentViewState.value

            assertEquals(testException, state.error)

            coVerify(exactly = 1) {
                consentRepository.getConsent(
                    profileId = MOCK_PROFILE_ID,
                    category = ConsentCategory.EUCONSENT.code
                )
            }
        }
    }

    @Test
    fun `onConsentAccepted grants consent successfully`() = testScope.runTest {
        // Mock the use case to return success
        coEvery {
            grantEuPrescriptionConsentUseCase.invoke(any(), any())
        } returns Result.success(Unit)

        // Build controller and wait for initialization
        controller = buildController()
        advanceUntilIdle()

        // Call onConsentAccepted
        controller.onConsentAccepted()
        advanceUntilIdle()

        // Verify the use case was called
        coVerify(exactly = 1) {
            grantEuPrescriptionConsentUseCase.invoke(any(), ConsentCategory.EUCONSENT)
        }
    }

    @Test
    fun `onConsentAccepted handles backend error`() = testScope.runTest {
        // Mock the use case to return failure
        val testException = RuntimeException("Backend error")
        coEvery {
            grantEuPrescriptionConsentUseCase.invoke(any(), any())
        } returns Result.failure(testException)

        // Build controller and wait for initialization
        controller = buildController()
        advanceUntilIdle()

        // Call onConsentAccepted
        controller.onConsentAccepted()
        advanceUntilIdle()

        // Verify error is propagated to state
        val state = controller.consentViewState.value
        assertEquals(testException, state.error)

        // Verify the use case was called
        coVerify(exactly = 1) {
            grantEuPrescriptionConsentUseCase.invoke(any(), ConsentCategory.EUCONSENT)
        }
    }
}
