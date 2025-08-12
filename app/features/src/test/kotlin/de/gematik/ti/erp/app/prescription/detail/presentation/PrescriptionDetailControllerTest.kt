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

import de.gematik.ti.erp.app.api.ApiCallException
import de.gematik.ti.erp.app.featuretoggle.datasource.FeatureToggleDataStore
import de.gematik.ti.erp.app.fhir.model.json
import de.gematik.ti.erp.app.invoice.repository.InvoiceRepository
import de.gematik.ti.erp.app.medicationplan.repository.DefaultMedicationPlanRepository
import de.gematik.ti.erp.app.medicationplan.repository.MedicationPlanRepository
import de.gematik.ti.erp.app.medicationplan.usecase.GetMedicationScheduleByTaskIdUseCase
import de.gematik.ti.erp.app.mocks.prescription.api.API_ACTIVE_SCANNED_TASK
import de.gematik.ti.erp.app.mocks.prescription.api.API_ACTIVE_SYNCED_TASK
import de.gematik.ti.erp.app.mocks.profile.api.API_MOCK_PROFILE
import de.gematik.ti.erp.app.prescription.model.PrescriptionData
import de.gematik.ti.erp.app.prescription.repository.PrescriptionRepository
import de.gematik.ti.erp.app.prescription.usecase.DeletePrescriptionUseCase
import de.gematik.ti.erp.app.prescription.usecase.GetPrescriptionByTaskIdUseCase
import de.gematik.ti.erp.app.prescription.usecase.RedeemScannedTaskUseCase
import de.gematik.ti.erp.app.prescription.usecase.UpdateEuRedeemableStatusUseCase
import de.gematik.ti.erp.app.prescription.usecase.UpdateScannedTaskNameUseCase
import de.gematik.ti.erp.app.profiles.repository.ProfileRepository
import de.gematik.ti.erp.app.profiles.usecase.GetActiveProfileUseCase
import de.gematik.ti.erp.app.utils.uistate.UiState.Companion.isDataState
import de.gematik.ti.erp.app.utils.uistate.UiState.Companion.isEmptyState
import de.gematik.ti.erp.app.utils.uistate.UiState.Companion.isErrorState
import de.gematik.ti.erp.app.utils.uistate.UiState.Companion.isLoadingState
import io.mockk.MockKAnnotations
import io.mockk.clearMocks
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.spyk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.rules.TestWatcher
import java.net.HttpURLConnection

class PrescriptionDetailControllerTest : TestWatcher() {

    private val medicationPlanRepository: MedicationPlanRepository = mockk()
    private val prescriptionRepository: PrescriptionRepository = mockk()
    private val invoiceRepository: InvoiceRepository = mockk()
    private val featureToggleDataStore: FeatureToggleDataStore = mockk()
    private val defaultMedicationPlanRepository: DefaultMedicationPlanRepository = mockk()
    private val profileRepository: ProfileRepository = mockk()
    private val dispatcher = StandardTestDispatcher()
    private val testScope = TestScope(dispatcher)

    private lateinit var controllerUnderTest: PrescriptionDetailController
    private lateinit var getPrescriptionByTaskIdUseCase: GetPrescriptionByTaskIdUseCase
    private lateinit var redeemScannedTaskUseCase: RedeemScannedTaskUseCase
    private lateinit var deletePrescriptionUseCase: DeletePrescriptionUseCase
    private lateinit var updateScannedTaskNameUseCase: UpdateScannedTaskNameUseCase
    private lateinit var getActiveProfileUseCase: GetActiveProfileUseCase
    private lateinit var loadMedicationScheduleByTaskIdUseCase: GetMedicationScheduleByTaskIdUseCase
    private lateinit var updateEuRedeemableStatusUseCase: UpdateEuRedeemableStatusUseCase

    @OptIn(ExperimentalCoroutinesApi::class)
    @Before
    fun setUp() {
        Dispatchers.setMain(dispatcher)
        MockKAnnotations.init(this)

        getPrescriptionByTaskIdUseCase = GetPrescriptionByTaskIdUseCase(
            repository = prescriptionRepository,
            dispatcher = dispatcher
        )
        loadMedicationScheduleByTaskIdUseCase = spyk(
            GetMedicationScheduleByTaskIdUseCase(
                medicationPlanRepository = defaultMedicationPlanRepository
            )
        )
        redeemScannedTaskUseCase = spyk(
            RedeemScannedTaskUseCase(
                repository = prescriptionRepository,
                dispatcher = dispatcher
            )
        )
        deletePrescriptionUseCase = DeletePrescriptionUseCase(
            prescriptionRepository = prescriptionRepository,
            invoiceRepository = invoiceRepository,
            medicationPlanRepository = medicationPlanRepository,
            dispatcher = dispatcher
        )
        updateScannedTaskNameUseCase = spyk(
            UpdateScannedTaskNameUseCase(
                repository = prescriptionRepository,
                dispatcher = dispatcher
            )
        )
        getActiveProfileUseCase = spyk(
            GetActiveProfileUseCase(
                repository = profileRepository,
                dispatcher = dispatcher
            )
        )
        every { defaultMedicationPlanRepository.getMedicationSchedule(any()) } returns flowOf(null)
        updateEuRedeemableStatusUseCase = spyk(
            UpdateEuRedeemableStatusUseCase(
                repository = prescriptionRepository,
                dispatcher = dispatcher
            )
        )
        every { featureToggleDataStore.isFeatureEnabled(any()) } returns flowOf(true)
        controllerUnderTest = PrescriptionDetailController(
            getActiveProfileUseCase = getActiveProfileUseCase,
            taskId = "taskId",
            redeemScannedTaskUseCase = redeemScannedTaskUseCase,
            deletePrescriptionUseCase = deletePrescriptionUseCase,
            loadMedicationScheduleByTaskIdUseCase = loadMedicationScheduleByTaskIdUseCase,
            getPrescriptionByTaskIdUseCase = getPrescriptionByTaskIdUseCase,
            updateScannedTaskNameUseCase = updateScannedTaskNameUseCase,
            updateEuRedeemableStatusUseCase = updateEuRedeemableStatusUseCase
        )
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @After
    fun tearDown() {
        Dispatchers.resetMain()
        clearMocks(
            invoiceRepository,
            prescriptionRepository,
            profileRepository
        )
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `active profile is loaded and prescription is empty screen in error state`() {
        every { profileRepository.activeProfile() } returns flowOf(API_MOCK_PROFILE)
        every { prescriptionRepository.loadSyncedTaskByTaskId(any()) } returns emptyFlow()
        every { prescriptionRepository.loadScannedTaskByTaskId(any()) } returns emptyFlow()

        testScope.runTest {
            advanceUntilIdle()
            val prescription = controllerUnderTest.profilePrescription.first()
            assert(prescription.isErrorState)
        }
    }

    @Test
    fun `profilePrescription is loading and screen in loading state`() {
        every { profileRepository.activeProfile() } returns flowOf(API_MOCK_PROFILE)
        every { prescriptionRepository.loadSyncedTaskByTaskId(any()) } returns emptyFlow()
        every { prescriptionRepository.loadScannedTaskByTaskId(any()) } returns emptyFlow()

        testScope.runTest {
            val prescription = controllerUnderTest.profilePrescription.first()
            assert(prescription.isLoadingState)
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `prescription is empty and screen in empty state`() {
        every { profileRepository.activeProfile() } returns flowOf(API_MOCK_PROFILE)
        every { prescriptionRepository.loadSyncedTaskByTaskId(any()) } returns flowOf()
        every { prescriptionRepository.loadScannedTaskByTaskId(any()) } returns flowOf()

        testScope.runTest {
            advanceUntilIdle()
            val prescription = controllerUnderTest.profilePrescription.first()
            assert(prescription.isEmptyState)
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `a synced prescription is loaded and screen is in data state with a synced prescription`() {
        every { profileRepository.activeProfile() } returns flowOf(API_MOCK_PROFILE)
        every { prescriptionRepository.loadSyncedTaskByTaskId(any()) } returns flowOf(API_ACTIVE_SYNCED_TASK)
        every { prescriptionRepository.loadScannedTaskByTaskId(any()) } returns flowOf()

        testScope.runTest {
            advanceUntilIdle()
            val prescription = controllerUnderTest.profilePrescription.first()
            assert(prescription.isDataState)
            assert(prescription.data?.second is PrescriptionData.Synced)
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `a scanned prescription is loaded and screen is in data state with a scanned prescription`() {
        every { profileRepository.activeProfile() } returns flowOf(API_MOCK_PROFILE)
        every { prescriptionRepository.loadSyncedTaskByTaskId(any()) } returns flowOf()
        every { prescriptionRepository.loadScannedTaskByTaskId(any()) } returns flowOf(API_ACTIVE_SCANNED_TASK)

        testScope.runTest {
            advanceUntilIdle()
            val prescription = controllerUnderTest.profilePrescription.first()
            assert(prescription.isDataState)
            assert(prescription.data?.second is PrescriptionData.Scanned)
        }
    }

    @Test
    fun `redeem scanned task (false) should invoke the redeemScannedTaskUseCase and prescriptionRepository`() {
        coEvery { prescriptionRepository.updateRedeemedOn(any(), any()) } returns Unit
        testScope.runTest {
            controllerUnderTest.redeemScannedTask("taskId", false)
        }
        coVerify(exactly = 1) {
            redeemScannedTaskUseCase.invoke("taskId", false)
        }
        coVerify(exactly = 1) {
            prescriptionRepository.updateRedeemedOn(
                "taskId",
                null
            )
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `redeem scanned task (true) should invoke the redeemScannedTaskUseCase`() {
        coEvery { prescriptionRepository.updateRedeemedOn(any(), any()) } returns Unit
        testScope.runTest {
            advanceUntilIdle()
            controllerUnderTest.redeemScannedTask("taskId", true)
        }
        coVerify(exactly = 1) {
            redeemScannedTaskUseCase.invoke("taskId", true)
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `Delete prescription should invoke the deletePrescriptionUseCase and remove data in the repository`() {
        coEvery {
            prescriptionRepository.deleteRemoteTaskById(any(), any())
        } returns Result.success(json.parseToJsonElement("{}"))
        coEvery { prescriptionRepository.deleteLocalTaskById(any()) } returns Unit
        coEvery { medicationPlanRepository.deleteMedicationSchedule(any()) } returns Unit
        coEvery { invoiceRepository.deleteRemoteInvoiceById(any(), any()) } returns Result.success(Unit)
        coEvery { prescriptionRepository.wasProfileEverAuthenticated(any()) } returns true

        testScope.runTest {
            advanceUntilIdle()
            controllerUnderTest.deletePrescription("profileId", "taskId")
        }
        coVerify(exactly = 1) {
            deletePrescriptionUseCase.invoke(profileId = "profileId", taskId = "taskId", deleteLocallyOnly = false)
        }
        coVerify(exactly = 1) { prescriptionRepository.deleteRemoteTaskById("profileId", "taskId") }
        coVerify(exactly = 1) { prescriptionRepository.deleteLocalTaskById("taskId") }
        coVerify(exactly = 1) { invoiceRepository.deleteRemoteInvoiceById("taskId", "profileId") }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `Delete prescription on a unauthenticated profile should only remove local prescription`() {
        coEvery {
            prescriptionRepository.deleteRemoteTaskById(any(), any())
        } returns Result.success(json.parseToJsonElement("{}"))
        coEvery { prescriptionRepository.deleteLocalTaskById(any()) } returns Unit
        coEvery { invoiceRepository.deleteLocalInvoiceById(any()) } returns Unit
        coEvery { medicationPlanRepository.deleteMedicationSchedule(any()) } returns Unit
        coEvery { invoiceRepository.deleteRemoteInvoiceById(any(), any()) } returns Result.success(Unit)
        coEvery { prescriptionRepository.wasProfileEverAuthenticated(any()) } returns false

        testScope.runTest {
            advanceUntilIdle()
            controllerUnderTest.deletePrescription("profileId", "taskId")
        }
        coVerify(exactly = 1) {
            deletePrescriptionUseCase.invoke(profileId = "profileId", taskId = "taskId", deleteLocallyOnly = false)
        }
        coVerify(exactly = 0) { prescriptionRepository.deleteRemoteTaskById("profileId", "taskId") }
        coVerify(exactly = 1) { prescriptionRepository.deleteLocalTaskById("taskId") }
        coVerify(exactly = 0) { invoiceRepository.deleteRemoteInvoiceById("taskId", "profileId") }
        coVerify(exactly = 1) { invoiceRepository.deleteLocalInvoiceById("taskId") }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `Delete prescription locally should only remove local prescription and invoices`() {
        coEvery { prescriptionRepository.deleteLocalTaskById(any()) } returns Unit
        coEvery { invoiceRepository.deleteLocalInvoiceById(any()) } returns Unit
        coEvery { medicationPlanRepository.deleteMedicationSchedule(any()) } returns Unit
        coEvery { invoiceRepository.deleteRemoteInvoiceById(any(), any()) } returns Result.success(Unit)
        coEvery { prescriptionRepository.wasProfileEverAuthenticated(any()) } returns true

        testScope.runTest {
            advanceUntilIdle()
            controllerUnderTest.deletePrescriptionFromLocal("profileId", "taskId")
        }
        coVerify(exactly = 1) {
            deletePrescriptionUseCase.invoke(profileId = "profileId", taskId = "taskId", deleteLocallyOnly = false)
        }
        coVerify(exactly = 0) { prescriptionRepository.deleteRemoteTaskById("profileId", "taskId") }
        coVerify(exactly = 1) { prescriptionRepository.deleteLocalTaskById("taskId") }
        coVerify(exactly = 0) { invoiceRepository.deleteRemoteInvoiceById("taskId", "profileId") }
        coVerify(exactly = 1) { invoiceRepository.deleteLocalInvoiceById("taskId") }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `Delete prescription but deleteRemoteTask fails`() {
        coEvery {
            prescriptionRepository.deleteRemoteTaskById(any(), any())
        } returns Result.failure(
            ApiCallException(
                message = "Error executing safe api call",
                response = retrofit2.Response.error<Any>(HttpURLConnection.HTTP_GONE, "Error executing safe api call".toResponseBody(null))
            )
        )
        coEvery { prescriptionRepository.deleteLocalTaskById(any()) } returns Unit
        coEvery { invoiceRepository.deleteLocalInvoiceById(any()) } returns Unit
        coEvery { medicationPlanRepository.deleteMedicationSchedule(any()) } returns Unit
        coEvery { invoiceRepository.deleteRemoteInvoiceById(any(), any()) } returns Result.success(Unit)
        coEvery { prescriptionRepository.wasProfileEverAuthenticated(any()) } returns true

        testScope.runTest {
            advanceUntilIdle()
            controllerUnderTest.deletePrescription("profileId", "taskId")
        }
        coVerify(exactly = 1) {
            deletePrescriptionUseCase.invoke(profileId = "profileId", taskId = "taskId", deleteLocallyOnly = false)
        }
        coVerify(exactly = 1) { prescriptionRepository.deleteRemoteTaskById("profileId", "taskId") }
        coVerify(exactly = 1) { prescriptionRepository.deleteLocalTaskById("taskId") }
        coVerify(exactly = 0) { invoiceRepository.deleteRemoteInvoiceById("taskId", "profileId") }
        coVerify(exactly = 1) { invoiceRepository.deleteLocalInvoiceById("taskId") }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `Delete prescription but deleteRemoteInvoice fails`() {
        coEvery {
            prescriptionRepository.deleteRemoteTaskById(any(), any())
        } returns Result.success(json.parseToJsonElement("{}"))
        coEvery { prescriptionRepository.deleteLocalTaskById(any()) } returns Unit
        coEvery { invoiceRepository.deleteLocalInvoiceById(any()) } returns Unit
        coEvery { medicationPlanRepository.deleteMedicationSchedule(any()) } returns Unit
        coEvery { invoiceRepository.deleteRemoteInvoiceById(any(), any()) } returns Result.failure(
            ApiCallException(
                message = "Error executing safe api call",
                response = retrofit2.Response.error<Any>(HttpURLConnection.HTTP_GONE, "Error executing safe api call".toResponseBody(null))
            )
        )
        coEvery { prescriptionRepository.wasProfileEverAuthenticated(any()) } returns true

        testScope.runTest {
            controllerUnderTest.deletePrescription("profileId", "taskId")
            advanceUntilIdle()
        }
        coVerify(exactly = 1) {
            deletePrescriptionUseCase.invoke(profileId = "profileId", taskId = "taskId", deleteLocallyOnly = false)
        }
        coVerify(exactly = 1) { prescriptionRepository.deleteRemoteTaskById("profileId", "taskId") }
        coVerify(exactly = 1) { prescriptionRepository.deleteLocalTaskById("taskId") }
        coVerify(exactly = 1) { invoiceRepository.deleteRemoteInvoiceById("taskId", "profileId") }
        coVerify(exactly = 1) { invoiceRepository.deleteLocalInvoiceById("taskId") }
    }

    @Test
    fun `Update scanned task name should only invoke the useCase and repository`() {
        coEvery { prescriptionRepository.updateScannedTaskName(any(), any()) } returns Unit

        testScope.runTest {
            controllerUnderTest.updateScannedTaskName("taskId", "newName")
        }
        coVerify(exactly = 1) {
            updateScannedTaskNameUseCase.invoke(taskId = "taskId", name = "newName")
        }
        coVerify(exactly = 1) { prescriptionRepository.updateScannedTaskName("taskId", "newName") }
    }
}
