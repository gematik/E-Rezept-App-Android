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

package de.gematik.ti.erp.app.prescription.detail.presentation

import app.cash.turbine.test
import de.gematik.ti.erp.app.api.ApiCallException
import de.gematik.ti.erp.app.authentication.presentation.BiometricAuthenticator
import de.gematik.ti.erp.app.authentication.usecase.ChooseAuthenticationDataUseCase
import de.gematik.ti.erp.app.base.NetworkStatusTracker
import de.gematik.ti.erp.app.base.usecase.IsFeatureToggleEnabledUseCase
import de.gematik.ti.erp.app.database.datastore.featuretoggle.FeatureToggleLocalDataSource
import de.gematik.ti.erp.app.fhir.model.json
import de.gematik.ti.erp.app.idp.repository.IdpRepository
import de.gematik.ti.erp.app.invoice.repository.InvoiceRepository
import de.gematik.ti.erp.app.medicationplan.repository.DefaultMedicationPlanRepository
import de.gematik.ti.erp.app.medicationplan.repository.MedicationPlanRepository
import de.gematik.ti.erp.app.medicationplan.usecase.GetMedicationScheduleByTaskIdUseCase
import de.gematik.ti.erp.app.mocks.PROFILE_ID
import de.gematik.ti.erp.app.mocks.prescription.api.API_ACTIVE_SCANNED_TASK
import de.gematik.ti.erp.app.mocks.prescription.api.API_ACTIVE_SYNCED_TASK
import de.gematik.ti.erp.app.mocks.profile.api.API_MOCK_PROFILE
import de.gematik.ti.erp.app.prescription.repository.TaskOperationsRepository
import de.gematik.ti.erp.app.prescription.usecase.DeletePrescriptionUseCase
import de.gematik.ti.erp.app.prescription.usecase.GetPrescriptionByTaskIdUseCase
import de.gematik.ti.erp.app.prescription.usecase.RedeemScannedTaskUseCase
import de.gematik.ti.erp.app.prescription.usecase.UpdateScannedTaskNameUseCase
import de.gematik.ti.erp.app.profiles.repository.ProfileRepository
import de.gematik.ti.erp.app.profiles.usecase.GetActiveProfileUseCase
import de.gematik.ti.erp.app.profiles.usecase.GetProfileByIdUseCase
import de.gematik.ti.erp.app.profiles.usecase.GetProfilesUseCase
import de.gematik.ti.erp.app.task.model.TaskErpModel
import de.gematik.ti.erp.app.userauthentication.model.SingleSignOnTokenErpModel
import de.gematik.ti.erp.app.userauthentication.model.UserAuthenticationErpModel
import de.gematik.ti.erp.app.utils.uistate.UiState
import de.gematik.ti.erp.app.utils.uistate.UiState.Companion.isEmptyState
import de.gematik.ti.erp.app.utils.uistate.UiState.Companion.isErrorState
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.spyk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlinx.datetime.Clock
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.rules.TestWatcher
import java.net.HttpURLConnection
import kotlin.test.assertEquals
import kotlin.time.Duration.Companion.hours

class PrescriptionDetailControllerTest : TestWatcher() {

    private val medicationPlanRepository: MedicationPlanRepository = mockk()
    private val taskOperationsRepository: TaskOperationsRepository = mockk()
    private val invoiceRepository: InvoiceRepository = mockk()
    private val featureToggleLocalDataSource: FeatureToggleLocalDataSource = mockk()
    private val defaultMedicationPlanRepository: DefaultMedicationPlanRepository = mockk()
    private val profileRepository: ProfileRepository = mockk()
    private val idpRepository: IdpRepository = mockk()
    private val networkStatusTracker: NetworkStatusTracker = mockk()
    private val biometricAuthenticator: BiometricAuthenticator = mockk()
    private val dispatcher = StandardTestDispatcher()
    private val testScope = TestScope(dispatcher)

    private lateinit var controllerUnderTest: PrescriptionDetailController
    private lateinit var getPrescriptionByTaskIdUseCase: GetPrescriptionByTaskIdUseCase
    private lateinit var redeemScannedTaskUseCase: RedeemScannedTaskUseCase
    private lateinit var deletePrescriptionUseCase: DeletePrescriptionUseCase
    private lateinit var updateScannedTaskNameUseCase: UpdateScannedTaskNameUseCase
    private lateinit var getActiveProfileUseCase: GetActiveProfileUseCase
    private lateinit var loadMedicationScheduleByTaskIdUseCase: GetMedicationScheduleByTaskIdUseCase
    private lateinit var isFeatureToggleEnabledUseCase: IsFeatureToggleEnabledUseCase
    private lateinit var getProfilesUseCase: GetProfilesUseCase
    private lateinit var getProfileByIdUseCase: GetProfileByIdUseCase
    private lateinit var chooseAuthenticationDataUseCase: ChooseAuthenticationDataUseCase

    @OptIn(ExperimentalCoroutinesApi::class)
    @Before
    fun setUp() {
        Dispatchers.setMain(dispatcher)
        MockKAnnotations.init(this)

        every { profileRepository.activeProfile() } returns flowOf(API_MOCK_PROFILE)
        every { defaultMedicationPlanRepository.getMedicationSchedule(any()) } returns flowOf(null)
        every { featureToggleLocalDataSource.isFeatureEnabled(any()) } returns flowOf(true)
        every { networkStatusTracker.networkStatus } returns flowOf(true)
        every { taskOperationsRepository.loadSyncedTaskByTaskId(any()) } returns flowOf(null)
        every { taskOperationsRepository.loadScannedTaskByTaskId(any()) } returns flowOf(null)
        every { taskOperationsRepository.loadDigaTaskByTaskId(any()) } returns flowOf(null)

        getPrescriptionByTaskIdUseCase = GetPrescriptionByTaskIdUseCase(
            repository = taskOperationsRepository,
            dispatcher = dispatcher
        )
        loadMedicationScheduleByTaskIdUseCase = GetMedicationScheduleByTaskIdUseCase(
            medicationPlanRepository = defaultMedicationPlanRepository
        )
        redeemScannedTaskUseCase = spyk(
            RedeemScannedTaskUseCase(
                repository = taskOperationsRepository,
                dispatcher = dispatcher
            )
        )
        deletePrescriptionUseCase = spyk(
            DeletePrescriptionUseCase(
                taskOperationsRepository = taskOperationsRepository,
                invoiceRepository = invoiceRepository,
                medicationPlanRepository = medicationPlanRepository,
                profileRepository = profileRepository,
                dispatcher = dispatcher
            )
        )
        updateScannedTaskNameUseCase = spyk(
            UpdateScannedTaskNameUseCase(
                repository = taskOperationsRepository,
                dispatcher = dispatcher
            )
        )
        getActiveProfileUseCase = GetActiveProfileUseCase(
            repository = profileRepository,
            dispatcher = dispatcher
        )
        isFeatureToggleEnabledUseCase = IsFeatureToggleEnabledUseCase(
            featureToggleLocalDataSource = featureToggleLocalDataSource
        )
        getProfilesUseCase = GetProfilesUseCase(
            repository = profileRepository,
            dispatcher = dispatcher
        )
        getProfileByIdUseCase = GetProfileByIdUseCase(
            repository = profileRepository,
            dispatcher = dispatcher
        )
        chooseAuthenticationDataUseCase = ChooseAuthenticationDataUseCase(
            profileRepository = profileRepository,
            idpRepository = idpRepository,
            dispatcher = dispatcher
        )

        controllerUnderTest = PrescriptionDetailController(
            getProfileByIdUseCase = getProfileByIdUseCase,
            getProfilesUseCase = getProfilesUseCase,
            getActiveProfileUseCase = getActiveProfileUseCase,
            chooseAuthenticationDataUseCase = chooseAuthenticationDataUseCase,
            networkStatusTracker = networkStatusTracker,
            biometricAuthenticator = biometricAuthenticator,
            taskId = "taskId",
            redeemScannedTaskUseCase = redeemScannedTaskUseCase,
            deletePrescriptionUseCase = deletePrescriptionUseCase,
            loadMedicationScheduleByTaskIdUseCase = loadMedicationScheduleByTaskIdUseCase,
            getPrescriptionByTaskIdUseCase = getPrescriptionByTaskIdUseCase,
            updateScannedTaskNameUseCase = updateScannedTaskNameUseCase,
            isFeatureToggleEnabledUseCase = isFeatureToggleEnabledUseCase
        )
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `active profile is loaded and prescription is empty screen in error state`() = testScope.runTest {
        every { taskOperationsRepository.loadSyncedTaskByTaskId(any()) } returns flow { throw Exception("Error") }
        every { taskOperationsRepository.loadDigaTaskByTaskId(any()) } returns flowOf()
        every { taskOperationsRepository.loadScannedTaskByTaskId(any()) } returns flowOf()

        advanceUntilIdle()
        controllerUnderTest.profilePrescription.test {
            assertEquals(UiState.Loading(), awaitItem())
            val state = awaitItem()
            assertEquals(true, state.isErrorState)
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `profilePrescription is loading and screen in loading state`() = testScope.runTest {
        every { taskOperationsRepository.loadSyncedTaskByTaskId(any()) } returns emptyFlow()
        every { taskOperationsRepository.loadDigaTaskByTaskId(any()) } returns emptyFlow()
        every { taskOperationsRepository.loadScannedTaskByTaskId(any()) } returns emptyFlow()

        advanceUntilIdle()
        controllerUnderTest.profilePrescription.test {
            assertEquals(UiState.Loading(), awaitItem())
            val state = awaitItem()
            assertEquals(true, state.isEmptyState)
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `prescription is empty and screen in empty state`() = testScope.runTest {
        every { taskOperationsRepository.loadDigaTaskByTaskId(any()) } returns flowOf(null)
        every { taskOperationsRepository.loadSyncedTaskByTaskId(any()) } returns flowOf(null)
        every { taskOperationsRepository.loadScannedTaskByTaskId(any()) } returns flowOf(null)

        advanceUntilIdle()
        controllerUnderTest.profilePrescription.test {
            assertEquals(UiState.Loading(), awaitItem())
            val state = awaitItem()
            assertEquals(true, state.isEmptyState)
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `a synced prescription is loaded and screen is in data state with a synced prescription`() = testScope.runTest {
        every { taskOperationsRepository.loadSyncedTaskByTaskId(any()) } returns flowOf(API_ACTIVE_SYNCED_TASK)
        every { taskOperationsRepository.loadScannedTaskByTaskId(any()) } returns flowOf()
        every { taskOperationsRepository.loadDigaTaskByTaskId(any()) } returns flowOf()

        advanceUntilIdle()
        controllerUnderTest.profilePrescription.test {
            assertEquals(UiState.Loading(), awaitItem())
            val state = awaitItem()
            assertEquals(API_ACTIVE_SYNCED_TASK, state.data?.second)
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `a scanned prescription is loaded and screen is in data state with a scanned prescription`() = testScope.runTest {
        every { taskOperationsRepository.loadSyncedTaskByTaskId(any()) } returns flowOf()
        every { taskOperationsRepository.loadScannedTaskByTaskId(any()) } returns flowOf(API_ACTIVE_SCANNED_TASK)
        every { taskOperationsRepository.loadDigaTaskByTaskId(any()) } returns flowOf()

        advanceUntilIdle()
        controllerUnderTest.profilePrescription.test {
            assertEquals(UiState.Loading(), awaitItem())
            val state = awaitItem()
            assertEquals(true, state.data?.second is TaskErpModel.Scanned)
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `redeem scanned task (false) should invoke the redeemScannedTaskUseCase and taskOperationsRepository`() = testScope.runTest {
        coEvery { taskOperationsRepository.updateScannedTaskRedeemedOn(any(), any()) } returns Unit
        controllerUnderTest.redeemScannedTask("taskId", false)
        advanceUntilIdle()

        coVerify(exactly = 1) {
            redeemScannedTaskUseCase.invoke("taskId", false)
        }
        coVerify(exactly = 1) {
            taskOperationsRepository.updateScannedTaskRedeemedOn(
                "taskId",
                null
            )
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `redeem scanned task (true) should invoke the redeemScannedTaskUseCase`() = testScope.runTest {
        coEvery { taskOperationsRepository.updateScannedTaskRedeemedOn(any(), any()) } returns Unit
        controllerUnderTest.redeemScannedTask("taskId", true)
        advanceUntilIdle()

        coVerify(exactly = 1) {
            redeemScannedTaskUseCase.invoke("taskId", true)
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `Delete prescription should invoke the deletePrescriptionUseCase and remove data in the repository`() = testScope.runTest {
        val profileWithToken = API_MOCK_PROFILE.copy(
            userAuthentication = UserAuthenticationErpModel.External(
                singleSignOnTokenErpModel = SingleSignOnTokenErpModel(
                    token = "dummy",
                    expiresOn = Clock.System.now().plus(2.hours),
                    validOn = Clock.System.now().minus(1.hours)
                ),
                externalAuthenticatorId = "0001",
                externalAuthenticatorName = "Authenticator"
            )
        )
        every { profileRepository.activeProfile() } returns flowOf(profileWithToken)

        coEvery {
            taskOperationsRepository.deleteRemoteTaskById(any(), any())
        } returns Result.success(json.parseToJsonElement("{}"))
        coEvery { taskOperationsRepository.deleteTaskByTaskIdOnlyInLocalDatabase(any()) } returns Unit
        coEvery { medicationPlanRepository.deleteMedicationSchedule(any()) } returns Unit
        coEvery { invoiceRepository.deleteRemoteInvoiceById(any(), any()) } returns Result.success(Unit)
        coEvery { profileRepository.wasProfileEverAuthenticated(any()) } returns true

        advanceUntilIdle()
        controllerUnderTest.deletePrescription(true)
        advanceUntilIdle()

        coVerify(exactly = 1) {
            deletePrescriptionUseCase.invoke(profileId = PROFILE_ID, taskId = "taskId", deleteLocallyOnly = false)
        }
        coVerify(exactly = 1) { taskOperationsRepository.deleteRemoteTaskById(PROFILE_ID, "taskId") }
        coVerify(exactly = 1) { taskOperationsRepository.deleteTaskByTaskIdOnlyInLocalDatabase("taskId") }
        coVerify(exactly = 1) { invoiceRepository.deleteRemoteInvoiceById("taskId", PROFILE_ID) }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `Delete prescription on a unauthenticated profile should only remove local prescription`() = testScope.runTest {
        coEvery {
            taskOperationsRepository.deleteRemoteTaskById(any(), any())
        } returns Result.success(json.parseToJsonElement("{}"))
        coEvery { taskOperationsRepository.deleteTaskByTaskIdOnlyInLocalDatabase(any()) } returns Unit
        coEvery { invoiceRepository.deleteLocalInvoiceById(any()) } returns Unit
        coEvery { medicationPlanRepository.deleteMedicationSchedule(any()) } returns Unit
        coEvery { invoiceRepository.deleteRemoteInvoiceById(any(), any()) } returns Result.success(Unit)
        coEvery { profileRepository.wasProfileEverAuthenticated(any()) } returns false

        advanceUntilIdle()
        controllerUnderTest.deletePrescription(true)
        advanceUntilIdle()

        coVerify(exactly = 1) {
            deletePrescriptionUseCase.invoke(profileId = PROFILE_ID, taskId = "taskId", deleteLocallyOnly = false)
        }
        coVerify(exactly = 0) { taskOperationsRepository.deleteRemoteTaskById(PROFILE_ID, "taskId") }
        coVerify(exactly = 1) { taskOperationsRepository.deleteTaskByTaskIdOnlyInLocalDatabase("taskId") }
        coVerify(exactly = 0) { invoiceRepository.deleteRemoteInvoiceById("taskId", PROFILE_ID) }
        coVerify(exactly = 1) { invoiceRepository.deleteLocalInvoiceById("taskId") }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `Delete scanned prescription should delete locally without requiring authentication`() = testScope.runTest {
        every { taskOperationsRepository.loadSyncedTaskByTaskId(any()) } returns flowOf()
        every { taskOperationsRepository.loadScannedTaskByTaskId(any()) } returns flowOf(API_ACTIVE_SCANNED_TASK)
        every { taskOperationsRepository.loadDigaTaskByTaskId(any()) } returns flowOf()
        coEvery { taskOperationsRepository.deleteTaskByTaskIdOnlyInLocalDatabase(any()) } returns Unit
        coEvery { invoiceRepository.deleteLocalInvoiceById(any()) } returns Unit
        coEvery { medicationPlanRepository.deleteMedicationSchedule(any()) } returns Unit
        coEvery { profileRepository.wasProfileEverAuthenticated(any()) } returns true

        controllerUnderTest.profilePrescription.test {
            awaitItem() // Loading
            awaitItem() // Data
        }

        controllerUnderTest.deletePrescription()
        advanceUntilIdle()

        coVerify(exactly = 1) {
            deletePrescriptionUseCase.invoke(profileId = PROFILE_ID, taskId = "taskId", deleteLocallyOnly = true)
        }
        coVerify(exactly = 0) { taskOperationsRepository.deleteRemoteTaskById(PROFILE_ID, "taskId") }
        coVerify(exactly = 1) { taskOperationsRepository.deleteTaskByTaskIdOnlyInLocalDatabase("taskId") }
        coVerify(exactly = 0) { invoiceRepository.deleteRemoteInvoiceById("taskId", PROFILE_ID) }
        coVerify(exactly = 1) { invoiceRepository.deleteLocalInvoiceById("taskId") }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `Delete prescription locally should only remove local prescription and invoices`() = testScope.runTest {
        coEvery { taskOperationsRepository.deleteTaskByTaskIdOnlyInLocalDatabase(any()) } returns Unit
        coEvery { invoiceRepository.deleteLocalInvoiceById(any()) } returns Unit
        coEvery { medicationPlanRepository.deleteMedicationSchedule(any()) } returns Unit
        coEvery { invoiceRepository.deleteRemoteInvoiceById(any(), any()) } returns Result.success(Unit)
        coEvery { profileRepository.wasProfileEverAuthenticated(any()) } returns true

        advanceUntilIdle()
        controllerUnderTest.deletePrescriptionFromLocal()
        advanceUntilIdle()

        coVerify(exactly = 1) {
            deletePrescriptionUseCase.invoke(profileId = PROFILE_ID, taskId = "taskId", deleteLocallyOnly = true)
        }
        coVerify(exactly = 0) { taskOperationsRepository.deleteRemoteTaskById(PROFILE_ID, "taskId") }
        coVerify(exactly = 1) { taskOperationsRepository.deleteTaskByTaskIdOnlyInLocalDatabase("taskId") }
        coVerify(exactly = 0) { invoiceRepository.deleteRemoteInvoiceById("taskId", PROFILE_ID) }
        coVerify(exactly = 1) { invoiceRepository.deleteLocalInvoiceById("taskId") }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `Delete prescription but deleteRemoteTask fails`() = testScope.runTest {
        coEvery {
            taskOperationsRepository.deleteRemoteTaskById(any(), any())
        } returns Result.failure(
            ApiCallException(
                message = "Error executing safe api call",
                response = retrofit2.Response.error<Any>(HttpURLConnection.HTTP_GONE, "Error executing safe api call".toResponseBody(null))
            )
        )
        coEvery { taskOperationsRepository.deleteTaskByTaskIdOnlyInLocalDatabase(any()) } returns Unit
        coEvery { invoiceRepository.deleteLocalInvoiceById(any()) } returns Unit
        coEvery { medicationPlanRepository.deleteMedicationSchedule(any()) } returns Unit
        coEvery { invoiceRepository.deleteRemoteInvoiceById(any(), any()) } returns Result.success(Unit)
        coEvery { profileRepository.wasProfileEverAuthenticated(any()) } returns true

        advanceUntilIdle()
        controllerUnderTest.deletePrescription(true)
        advanceUntilIdle()

        coVerify(exactly = 1) {
            deletePrescriptionUseCase.invoke(profileId = PROFILE_ID, taskId = "taskId", deleteLocallyOnly = false)
        }
        coVerify(exactly = 1) { taskOperationsRepository.deleteRemoteTaskById(PROFILE_ID, "taskId") }
        coVerify(exactly = 1) { taskOperationsRepository.deleteTaskByTaskIdOnlyInLocalDatabase("taskId") }
        coVerify(exactly = 0) { invoiceRepository.deleteRemoteInvoiceById("taskId", PROFILE_ID) }
        coVerify(exactly = 1) { invoiceRepository.deleteLocalInvoiceById("taskId") }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `Delete prescription but deleteRemoteInvoice fails`() = testScope.runTest {
        coEvery {
            taskOperationsRepository.deleteRemoteTaskById(any(), any())
        } returns Result.success(json.parseToJsonElement("{}"))
        coEvery { taskOperationsRepository.deleteTaskByTaskIdOnlyInLocalDatabase(any()) } returns Unit
        coEvery { invoiceRepository.deleteLocalInvoiceById(any()) } returns Unit
        coEvery { medicationPlanRepository.deleteMedicationSchedule(any()) } returns Unit
        coEvery { invoiceRepository.deleteRemoteInvoiceById(any(), any()) } returns Result.failure(
            ApiCallException(
                message = "Error executing safe api call",
                response = retrofit2.Response.error<Any>(HttpURLConnection.HTTP_GONE, "Error executing safe api call".toResponseBody(null))
            )
        )
        coEvery { profileRepository.wasProfileEverAuthenticated(any()) } returns true

        advanceUntilIdle()
        controllerUnderTest.deletePrescription(true)
        advanceUntilIdle()

        coVerify(exactly = 1) {
            deletePrescriptionUseCase.invoke(profileId = PROFILE_ID, taskId = "taskId", deleteLocallyOnly = false)
        }
        coVerify(exactly = 1) { taskOperationsRepository.deleteRemoteTaskById(PROFILE_ID, "taskId") }
        coVerify(exactly = 1) { taskOperationsRepository.deleteTaskByTaskIdOnlyInLocalDatabase("taskId") }
        coVerify(exactly = 1) { invoiceRepository.deleteRemoteInvoiceById("taskId", PROFILE_ID) }
        coVerify(exactly = 1) { invoiceRepository.deleteLocalInvoiceById("taskId") }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `Update scanned task name should only invoke the useCase and repository`() = testScope.runTest {
        coEvery { taskOperationsRepository.updateScannedTaskName(any(), any()) } returns Unit

        advanceUntilIdle()
        controllerUnderTest.updateScannedTaskName("taskId", "newName")
        advanceUntilIdle()

        coVerify(exactly = 1) {
            updateScannedTaskNameUseCase.invoke(taskId = "taskId", name = "newName")
        }
        coVerify(exactly = 1) { taskOperationsRepository.updateScannedTaskName("taskId", "newName") }
    }
}
