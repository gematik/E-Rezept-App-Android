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

package de.gematik.ti.erp.app.profiles.presentation

import de.gematik.ti.erp.app.authentication.presentation.BiometricAuthenticator
import de.gematik.ti.erp.app.authentication.usecase.ChooseAuthenticationDataUseCase
import de.gematik.ti.erp.app.base.NetworkStatusTracker
import de.gematik.ti.erp.app.idp.repository.IdpRepository
import de.gematik.ti.erp.app.medicationplan.repository.MedicationPlanRepository
import de.gematik.ti.erp.app.mocks.PROFILE_ID
import de.gematik.ti.erp.app.mocks.profile.api.API_MOCK_PROFILE
import de.gematik.ti.erp.app.profiles.repository.ProfileRepository
import de.gematik.ti.erp.app.profiles.usecase.AddProfileUseCase
import de.gematik.ti.erp.app.profiles.usecase.DeleteProfileUseCase
import de.gematik.ti.erp.app.profiles.usecase.GetActiveProfileUseCase
import de.gematik.ti.erp.app.profiles.usecase.GetProfileByIdUseCase
import de.gematik.ti.erp.app.profiles.usecase.GetProfilesUseCase
import de.gematik.ti.erp.app.profiles.usecase.LogoutProfileUseCase
import de.gematik.ti.erp.app.profiles.usecase.SwitchActiveProfileUseCase
import de.gematik.ti.erp.app.profiles.usecase.UpdateProfileUseCase
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.spyk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.emptyFlow
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

class ProfileScreenControllerTest {
    private val medicationPlanRepository: MedicationPlanRepository = mockk()
    private val profileRepository: ProfileRepository = mockk()
    private val idpRepository: IdpRepository = mockk()
    private val dispatcher = StandardTestDispatcher()
    private val testScope = TestScope(dispatcher)
    private val biometricAuthenticator = mockk<BiometricAuthenticator>()
    private lateinit var controllerUnderTest: ProfileScreenController

    // tested by CombineSelectedProfileWithAllProfilesControllerTest
    private lateinit var getProfileByIdUseCase: GetProfileByIdUseCase

    // tested by CombineSelectedProfileWithAllProfilesControllerTest
    private lateinit var getProfilesUseCase: GetProfilesUseCase
    private lateinit var addProfileUseCase: AddProfileUseCase
    private lateinit var deleteProfileUseCase: DeleteProfileUseCase
    private lateinit var logoutProfileUseCase: LogoutProfileUseCase
    private lateinit var switchActiveProfileUseCase: SwitchActiveProfileUseCase
    private lateinit var updateProfileUseCase: UpdateProfileUseCase
    private lateinit var getActiveProfileUseCase: GetActiveProfileUseCase
    private lateinit var chooseAuthenticationDataUseCase: ChooseAuthenticationDataUseCase

    private val networkStatusTracker = mockk<NetworkStatusTracker>()

    @OptIn(ExperimentalCoroutinesApi::class)
    @Before
    fun setUp() {
        Dispatchers.setMain(dispatcher)
        MockKAnnotations.init(this)
        getProfileByIdUseCase = GetProfileByIdUseCase(profileRepository, dispatcher)
        getProfilesUseCase = GetProfilesUseCase(profileRepository, dispatcher)
        addProfileUseCase = spyk(AddProfileUseCase(profileRepository, dispatcher))
        deleteProfileUseCase = spyk(DeleteProfileUseCase(profileRepository, idpRepository, medicationPlanRepository, dispatcher))
        logoutProfileUseCase = spyk(LogoutProfileUseCase(idpRepository, dispatcher))
        switchActiveProfileUseCase = spyk(SwitchActiveProfileUseCase(profileRepository, dispatcher))
        updateProfileUseCase = spyk(UpdateProfileUseCase(profileRepository, dispatcher))
        getActiveProfileUseCase = spyk(GetActiveProfileUseCase(profileRepository, dispatcher))
        chooseAuthenticationDataUseCase = spyk(ChooseAuthenticationDataUseCase(profileRepository, idpRepository, dispatcher))
        every { networkStatusTracker.networkStatus } returns flowOf(true)
        controllerUnderTest = ProfileScreenController(
            profileId = PROFILE_ID,
            getProfileByIdUseCase = getProfileByIdUseCase,
            getProfilesUseCase = getProfilesUseCase,
            addProfileUseCase = addProfileUseCase,
            deleteProfileUseCase = deleteProfileUseCase,
            logoutProfileUseCase = logoutProfileUseCase,
            switchActiveProfileUseCase = switchActiveProfileUseCase,
            updateProfileUseCase = updateProfileUseCase,
            getActiveProfileUseCase = getActiveProfileUseCase,
            chooseAuthenticationDataUseCase = chooseAuthenticationDataUseCase,
            biometricAuthenticator = biometricAuthenticator,
            networkStatusTracker = networkStatusTracker
        )
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `adding a profile should invoke the addProfileUseCase and create a new profile in the repository`() {
        every { profileRepository.getProfileById(any()) } returns emptyFlow()
        coEvery { profileRepository.createNewProfile(any()) } returns Unit

        testScope.runTest {
            advanceUntilIdle()
            controllerUnderTest.addNewProfile("newProfile")
        }
        coVerify(exactly = 1) { addProfileUseCase.invoke("newProfile") }
        coVerify(exactly = 1) { profileRepository.createNewProfile("newProfile") }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `switch a profile should invoke the switchActiveProfileUseCase and activate the profile in the repository`() {
        coEvery { profileRepository.activateProfile("profileId") } returns Unit
        testScope.runTest {
            advanceUntilIdle()
            controllerUnderTest.switchActiveProfile("profileId")
        }
        coVerify(exactly = 1) { switchActiveProfileUseCase.invoke("profileId") }
        coVerify(exactly = 1) { profileRepository.activateProfile("profileId") }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `logout profile should invoke the logoutProfileUseCase and invalidate idp data in the repository`() {
        coEvery { idpRepository.invalidate(any()) } returns Unit

        testScope.runTest {
            advanceUntilIdle()
            controllerUnderTest.logout("profileId")
        }
        coVerify(exactly = 1) { logoutProfileUseCase.invoke("profileId") }
        coVerify(exactly = 1) { idpRepository.invalidate("profileId") }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `update profile name should invoke the updateProfileUseCase and update data in the repository`() {
        every { profileRepository.getProfileById(any()) } returns flowOf(API_MOCK_PROFILE)
        every { profileRepository.profiles() } returns flowOf(listOf(API_MOCK_PROFILE))
        coEvery { profileRepository.updateProfileName(any(), any()) } returns Unit

        val profileId = API_MOCK_PROFILE.id

        testScope.runTest {
            advanceUntilIdle()
            controllerUnderTest.updateProfileName("newName")
        }
        coVerify(exactly = 1) {
            updateProfileUseCase.invoke(
                modifier = UpdateProfileUseCase.Companion.ProfileModifier.Name("newName"),
                id = profileId
            )
        }
        coVerify(exactly = 1) { profileRepository.updateProfileName(profileId, "newName") }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `Delete profile should invoke the deleteProfileUseCase, invalidate and remove the data in the repositories`() {
        coEvery { profileRepository.removeProfile(any(), any()) } returns Unit
        coEvery { medicationPlanRepository.deleteAllMedicationSchedulesForProfile(any()) } returns Unit
        coEvery { idpRepository.invalidateDecryptedAccessToken(any()) } returns Unit

        testScope.runTest {
            advanceUntilIdle()
            controllerUnderTest.deleteProfile("profileId", "name")
        }
        coVerify(exactly = 1) {
            deleteProfileUseCase.invoke("profileId", "name")
        }
        coVerify(exactly = 1) { idpRepository.invalidateDecryptedAccessToken("profileId") }
        coVerify(exactly = 1) { profileRepository.removeProfile("profileId", "name") }
    }
}
