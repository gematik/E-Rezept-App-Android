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
import de.gematik.ti.erp.app.eurezept.domain.usecase.GetEuPrescriptionsUseCase
import de.gematik.ti.erp.app.eurezept.domain.usecase.ToggleIsEuRedeemableByPatientAuthorizationUseCase
import de.gematik.ti.erp.app.eurezept.model.MockEuTestData.MOCK_MEDICATION_NAME_1
import de.gematik.ti.erp.app.eurezept.model.MockEuTestData.MOCK_PROFILE_ID
import de.gematik.ti.erp.app.eurezept.model.MockEuTestData.MOCK_READY_EU_SYNCED_TASK
import de.gematik.ti.erp.app.eurezept.model.MockEuTestData.PRECRIPTION_ID_1
import de.gematik.ti.erp.app.eurezept.model.MockEuTestData.PRECRIPTION_ID_2
import de.gematik.ti.erp.app.eurezept.model.MockEuTestData.mockEuPrescriptions
import de.gematik.ti.erp.app.eurezept.model.MockEuTestData.mockValidSsoTokenScope
import de.gematik.ti.erp.app.eurezept.model.MockEuTestData.profileData
import de.gematik.ti.erp.app.eurezept.presentation.EuPrescriptionSelectionController
import de.gematik.ti.erp.app.eurezept.repository.EuRepository
import de.gematik.ti.erp.app.fhir.constant.prescription.euredeem.FhirEuRedeemAccessCodeRequestConstants.FhirEuRedeemAccessCodeRequestMeta
import de.gematik.ti.erp.app.fhir.constant.prescription.euredeem.FhirEuRedeemAccessCodeResponseConstants.FhirEuRedeemAccessCodeResponseMeta
import de.gematik.ti.erp.app.fhir.constant.prescription.euredeem.FhirTaskEuPatchInputModelConstants.FhirTaskEuPatchMeta
import de.gematik.ti.erp.app.idp.model.IdpData
import de.gematik.ti.erp.app.idp.repository.IdpRepository
import de.gematik.ti.erp.app.prescription.repository.PrescriptionRepository
import de.gematik.ti.erp.app.profiles.repository.ProfileRepository
import de.gematik.ti.erp.app.profiles.usecase.GetActiveProfileUseCase
import de.gematik.ti.erp.app.profiles.usecase.GetProfileByIdUseCase
import de.gematik.ti.erp.app.profiles.usecase.GetProfilesUseCase
import de.gematik.ti.erp.app.settings.repository.EuVersionRepository
import de.gematik.ti.erp.app.utils.uistate.UiState.Companion.isEmptyState
import de.gematik.ti.erp.app.utils.uistate.UiState.Companion.isErrorState
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
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class EuPrescriptionSelectionControllerTest {

    private val dispatcher = StandardTestDispatcher()
    private val testScope = TestScope(dispatcher)

    private val prescriptionRepository: PrescriptionRepository = mockk()
    private val profileRepository: ProfileRepository = mockk()
    private val euRepository: EuRepository = mockk()

    private val euVersionRepository: EuVersionRepository = mockk()
    private val idpRepository: IdpRepository = mockk()

    private val networkStatusTracker: NetworkStatusTracker = mockk()
    private val biometricAuthenticator: BiometricAuthenticator = mockk(relaxed = true)

    private lateinit var getEuPrescriptionsUseCase: GetEuPrescriptionsUseCase
    private lateinit var toggleIsEuRedeemableByPatientAuthorizationUseCase: ToggleIsEuRedeemableByPatientAuthorizationUseCase
    private lateinit var getProfileByIdUseCase: GetProfileByIdUseCase
    private lateinit var getProfilesUseCase: GetProfilesUseCase
    private lateinit var getActiveProfileUseCase: GetActiveProfileUseCase
    private lateinit var chooseAuthenticationDataUseCase: ChooseAuthenticationDataUseCase

    private lateinit var controller: EuPrescriptionSelectionController

    @Before
    fun setup() {
        Dispatchers.setMain(dispatcher)

        every { networkStatusTracker.networkStatus } returns flowOf(true)

        coEvery { euVersionRepository.getEuRedeemAccessCodeRequestMeta() } returns FhirEuRedeemAccessCodeRequestMeta.V_1_0
        coEvery { euVersionRepository.getEuRedeemAccessCodeResponseMeta() } returns FhirEuRedeemAccessCodeResponseMeta.V_1_0
        coEvery { euVersionRepository.getEuPatchMeta() } returns FhirTaskEuPatchMeta.V_1_0

        getEuPrescriptionsUseCase = GetEuPrescriptionsUseCase(
            prescriptionRepository = prescriptionRepository,
            profileRepository = profileRepository,
            dispatcher = dispatcher
        )

        toggleIsEuRedeemableByPatientAuthorizationUseCase = ToggleIsEuRedeemableByPatientAuthorizationUseCase(
            euRepository = euRepository,
            euVersionRepository = euVersionRepository,
            dispatcher = dispatcher
        )

        getProfileByIdUseCase = GetProfileByIdUseCase(profileRepository, dispatcher)
        getProfilesUseCase = GetProfilesUseCase(profileRepository, dispatcher)
        getActiveProfileUseCase = GetActiveProfileUseCase(profileRepository, dispatcher)
        chooseAuthenticationDataUseCase = ChooseAuthenticationDataUseCase(profileRepository, idpRepository, dispatcher)

        val mockProfileData = profileData.copy(id = MOCK_PROFILE_ID)
        coEvery { profileRepository.activeProfile() } returns flowOf(mockProfileData)
        coEvery { profileRepository.profiles() } returns flowOf(listOf(mockProfileData))
        coEvery { profileRepository.getProfileById(any()) } returns flowOf(mockProfileData)
        coEvery { profileRepository.updateLastAuthenticated(any(), any()) } returns Unit
        coEvery { profileRepository.isSsoTokenValid(any()) } returns flowOf(true)

        coEvery { prescriptionRepository.syncedTasks(MOCK_PROFILE_ID) } returns flowOf(listOf(MOCK_READY_EU_SYNCED_TASK))
        coEvery { prescriptionRepository.scannedTasks(MOCK_PROFILE_ID) } returns flowOf(emptyList())

        coEvery {
            euRepository.toggleIsEuRedeemableByPatientAuthorization(
                taskId = PRECRIPTION_ID_1,
                profileId = MOCK_PROFILE_ID,
                metadata = FhirTaskEuPatchMeta.V_1_0,
                isEuRedeemableByPatientAuthorization = true
            )
        } returns Result.success(Unit)

        coEvery {
            euRepository.toggleIsEuRedeemableByPatientAuthorization(
                taskId = PRECRIPTION_ID_2,
                profileId = MOCK_PROFILE_ID,
                metadata = FhirTaskEuPatchMeta.V_1_0,
                isEuRedeemableByPatientAuthorization = false
            )
        } returns Result.success(Unit)

        val mockAuthData = IdpData.AuthenticationData(
            singleSignOnTokenScope = mockValidSsoTokenScope
        )
        coEvery { idpRepository.authenticationData(any()) } returns flowOf(mockAuthData)

        controller = EuPrescriptionSelectionController(
            getEuPrescriptionsUseCase = getEuPrescriptionsUseCase,
            toggleIsEuRedeemableByPatientAuthorizationUseCase = toggleIsEuRedeemableByPatientAuthorizationUseCase,
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
    fun `load EU prescriptions successfully with valid profile`() = testScope.runTest {
        advanceUntilIdle()

        val finalState = controller.uiState.value
        assertEquals(1, finalState.data?.size)

        val euPrescription = finalState.data?.get(0)
        assertEquals(MOCK_READY_EU_SYNCED_TASK.taskId, euPrescription?.id)
        assertEquals(MOCK_MEDICATION_NAME_1, euPrescription?.name)

        coVerify(exactly = 1) { prescriptionRepository.syncedTasks(MOCK_PROFILE_ID) }
        coVerify(exactly = 1) { prescriptionRepository.scannedTasks(MOCK_PROFILE_ID) }
    }

    @Test
    fun `show empty state when no prescriptions available`() {
        coEvery { prescriptionRepository.syncedTasks(MOCK_PROFILE_ID) } returns flowOf(emptyList())
        coEvery { prescriptionRepository.scannedTasks(MOCK_PROFILE_ID) } returns flowOf(emptyList())

        testScope.runTest {
            advanceUntilIdle()
            controller.getEuPrescriptions()

            val finalState = controller.uiState.value
            assertTrue(finalState.isEmptyState)
        }
    }

    @Test
    fun `show error state on exception when loading prescriptions`() {
        val testException = RuntimeException()
        coEvery { prescriptionRepository.syncedTasks(MOCK_PROFILE_ID) } throws testException

        testScope.runTest {
            advanceUntilIdle()
            controller.getEuPrescriptions()

            val finalState = controller.uiState.value
            assertTrue(finalState.isErrorState)
        }
    }

    @Test
    fun `toggle prescription selection marks as loading then clears on success`() {
        testScope.runTest {
            advanceUntilIdle()

            controller.togglePrescriptionSelection(mockEuPrescriptions[0])
            advanceUntilIdle()

            coVerify(exactly = 1) {
                euRepository.toggleIsEuRedeemableByPatientAuthorization(
                    taskId = PRECRIPTION_ID_1,
                    profileId = MOCK_PROFILE_ID,
                    metadata = FhirTaskEuPatchMeta.V_1_0,
                    isEuRedeemableByPatientAuthorization = true
                )
            }
        }
    }

    @Test
    fun `toggle prescription selection marks as error on failure`() {
        val testException = RuntimeException()
        coEvery {
            euRepository.toggleIsEuRedeemableByPatientAuthorization(
                taskId = PRECRIPTION_ID_1,
                profileId = MOCK_PROFILE_ID,
                metadata = FhirTaskEuPatchMeta.V_1_0,
                isEuRedeemableByPatientAuthorization = true
            )
        } returns Result.failure(testException)

        testScope.runTest {
            advanceUntilIdle()

            controller.togglePrescriptionSelection(mockEuPrescriptions[0])
            advanceUntilIdle()

            coVerify(exactly = 1) {
                euRepository.toggleIsEuRedeemableByPatientAuthorization(
                    taskId = PRECRIPTION_ID_1,
                    profileId = MOCK_PROFILE_ID,
                    metadata = FhirTaskEuPatchMeta.V_1_0,
                    isEuRedeemableByPatientAuthorization = true
                )
            }
        }
    }

    @Test
    fun `toggle prescription unmarks when already marked`() {
        testScope.runTest {
            advanceUntilIdle()

            // prescription 2 already marked, toggling should unmark it
            controller.togglePrescriptionSelection(mockEuPrescriptions[1])
            advanceUntilIdle()

            coVerify(exactly = 1) {
                euRepository.toggleIsEuRedeemableByPatientAuthorization(
                    taskId = PRECRIPTION_ID_2,
                    profileId = MOCK_PROFILE_ID,
                    metadata = FhirTaskEuPatchMeta.V_1_0,
                    isEuRedeemableByPatientAuthorization = false
                )
            }
        }
    }
}
