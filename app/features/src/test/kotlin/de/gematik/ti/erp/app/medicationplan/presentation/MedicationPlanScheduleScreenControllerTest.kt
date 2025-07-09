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

package de.gematik.ti.erp.app.medicationplan.presentation

import app.cash.turbine.test
import de.gematik.ti.erp.app.medicationplan.model.DateEvent
import de.gematik.ti.erp.app.medicationplan.model.MedicationDosage
import de.gematik.ti.erp.app.medicationplan.model.MedicationNotification
import de.gematik.ti.erp.app.medicationplan.model.MedicationPlanDosageInstruction
import de.gematik.ti.erp.app.medicationplan.model.toMedicationSchedule
import de.gematik.ti.erp.app.medicationplan.repository.MedicationPlanRepository
import de.gematik.ti.erp.app.medicationplan.usecase.GetDosageInstructionByTaskIdUseCase
import de.gematik.ti.erp.app.medicationplan.usecase.LoadMedicationScheduleByTaskIdUseCase
import de.gematik.ti.erp.app.medicationplan.usecase.PlanMedicationScheduleUseCase
import de.gematik.ti.erp.app.medicationplan.usecase.ScheduleReminderWorker
import de.gematik.ti.erp.app.mocks.prescription.api.API_ACTIVE_SCANNED_TASK
import de.gematik.ti.erp.app.mocks.prescription.api.API_ACTIVE_SYNCED_TASK
import de.gematik.ti.erp.app.mocks.prescription.api.API_ACTIVE_SYNCED_TASK_STRUCTURED_DOSAGE
import de.gematik.ti.erp.app.prescription.model.PrescriptionData
import de.gematik.ti.erp.app.prescription.repository.PrescriptionRepository
import de.gematik.ti.erp.app.prescription.usecase.GetPrescriptionByTaskIdUseCase
import de.gematik.ti.erp.app.profiles.repository.ProfileRepository
import de.gematik.ti.erp.app.profiles.usecase.GetActiveProfileUseCase
import de.gematik.ti.erp.app.utils.toLocalDate
import de.gematik.ti.erp.app.utils.uistate.UiState
import de.gematik.ti.erp.app.utils.uistate.UiState.Companion.isDataState
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
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalTime
import org.junit.After
import org.junit.Before
import org.junit.Test
import java.util.UUID
import kotlin.test.assertEquals
import kotlin.time.Duration.Companion.days

class MedicationPlanScheduleScreenControllerTest {

    private val now = Instant.parse("2024-01-01T12:00:00Z")
    private val profileRepository: ProfileRepository = mockk()
    private val medicationPlanRepository: MedicationPlanRepository = mockk()
    private val prescriptionRepository: PrescriptionRepository = mockk()
    private val getPrescriptionByTaskIdUseCase: GetPrescriptionByTaskIdUseCase = mockk()
    private val scheduleReminderWorker: ScheduleReminderWorker = mockk()
    private val dispatcher = StandardTestDispatcher()
    private val testScope = TestScope(dispatcher)
    private lateinit var controllerUnderTest: MedicationPlanScheduleScreenController
    private lateinit var getActiveProfileUseCase: GetActiveProfileUseCase
    private lateinit var getDosageInstructionByTaskIdUseCase: GetDosageInstructionByTaskIdUseCase
    private lateinit var loadMedicationScheduleByTaskIdUseCase: LoadMedicationScheduleByTaskIdUseCase
    private lateinit var planMedicationScheduleUseCase: PlanMedicationScheduleUseCase

    @OptIn(ExperimentalCoroutinesApi::class)
    @Before
    fun setUp() {
        MockKAnnotations.init(this)
        every { scheduleReminderWorker.schedule() } returns Unit
        Dispatchers.setMain(dispatcher)
        getDosageInstructionByTaskIdUseCase = GetDosageInstructionByTaskIdUseCase(prescriptionRepository, dispatcher)
        getActiveProfileUseCase = GetActiveProfileUseCase(profileRepository, dispatcher)
        planMedicationScheduleUseCase = spyk(
            PlanMedicationScheduleUseCase(scheduleReminderWorker, medicationPlanRepository, dispatcher)
        )
        loadMedicationScheduleByTaskIdUseCase = LoadMedicationScheduleByTaskIdUseCase(
            medicationPlanRepository,
            dispatcher
        )

        controllerUnderTest = object : MedicationPlanScheduleScreenController(
            getPrescriptionByTaskIdUseCase = getPrescriptionByTaskIdUseCase,
            getDosageInstructionByTaskIdUseCase = getDosageInstructionByTaskIdUseCase,
            planMedicationScheduleUseCase = planMedicationScheduleUseCase,
            loadMedicationScheduleByTaskIdUseCase = loadMedicationScheduleByTaskIdUseCase,
            taskId = "taskId",
            now = now
        ) {}
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `test initial state with scanned prescription`() {
        coEvery { prescriptionRepository.loadScannedTaskByTaskId(any()) } returns flowOf(API_ACTIVE_SCANNED_TASK)
        coEvery { prescriptionRepository.loadSyncedTaskByTaskId(any()) } returns emptyFlow()
        coEvery { medicationPlanRepository.loadMedicationSchedule(any()) } returns flowOf(null)
        coEvery { getPrescriptionByTaskIdUseCase(any()) } returns
            flowOf(PrescriptionData.Scanned(API_ACTIVE_SCANNED_TASK))

        testScope.runTest {
            controllerUnderTest.prescriptionSchedule.test {
                val loading = awaitItem()
                assertEquals(UiState.Loading(), loading)
                advanceUntilIdle()
                val data = awaitItem()
                assertEquals(
                    UiState.Data(
                        PrescriptionSchedule(
                            prescription = PrescriptionData.Scanned(API_ACTIVE_SCANNED_TASK),
                            dosageInstruction = MedicationPlanDosageInstruction.Empty,
                            medicationSchedule = PrescriptionData.Scanned(
                                API_ACTIVE_SCANNED_TASK
                            ).toMedicationSchedule(now)
                        )
                    ),
                    data
                )
            }
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `test initial state with synced prescription`() {
        val expectedPrescription = PrescriptionData.Synced(task = API_ACTIVE_SYNCED_TASK, now = now)

        coEvery { prescriptionRepository.loadScannedTaskByTaskId(any()) } returns emptyFlow()
        coEvery { prescriptionRepository.loadSyncedTaskByTaskId(any()) } returns flowOf(expectedPrescription.task)
        coEvery { medicationPlanRepository.loadMedicationSchedule(any()) } returns flowOf(null)

        coEvery { getPrescriptionByTaskIdUseCase(any()) } returns flowOf(expectedPrescription)
        val expectedMedicationSchedule = expectedPrescription.toMedicationSchedule(now)
        val expectedDosageInstruction = MedicationPlanDosageInstruction.FreeText("Dosage")

        testScope.runTest {
            controllerUnderTest.prescriptionSchedule.test {
                val loading = awaitItem()
                assertEquals(UiState.Loading(), loading)
                advanceUntilIdle()
                val data = awaitItem()
                assertEquals(
                    UiState.Data(
                        PrescriptionSchedule(
                            prescription = expectedPrescription,
                            dosageInstruction = expectedDosageInstruction,
                            medicationSchedule = expectedMedicationSchedule
                        )
                    ),
                    data
                )
            }
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `test initial state with synced prescription and medication schedule`() {
        val expectedPrescription = PrescriptionData.Synced(task = API_ACTIVE_SYNCED_TASK, now = now)
        val medicationSchedule = expectedPrescription.toMedicationSchedule(now)

        coEvery { prescriptionRepository.loadScannedTaskByTaskId(any()) } returns emptyFlow()
        coEvery { prescriptionRepository.loadSyncedTaskByTaskId(any()) } returns flowOf(expectedPrescription.task)
        coEvery { medicationPlanRepository.loadMedicationSchedule(any()) } returns flowOf(medicationSchedule)

        coEvery { getPrescriptionByTaskIdUseCase(any()) } returns flowOf(expectedPrescription)
        val expectedDosageInstruction = MedicationPlanDosageInstruction.FreeText("Dosage")

        testScope.runTest {
            controllerUnderTest.prescriptionSchedule.test {
                val loading = awaitItem()
                assertEquals(UiState.Loading(), loading)
                advanceUntilIdle()
                val data = awaitItem()
                assertEquals(
                    UiState.Data(
                        PrescriptionSchedule(
                            prescription = expectedPrescription,
                            dosageInstruction = expectedDosageInstruction,
                            medicationSchedule = medicationSchedule
                        )
                    ),
                    data
                )
            }
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `test initial state with empty prescription should be error state`() {
        coEvery { prescriptionRepository.loadScannedTaskByTaskId(any()) } returns emptyFlow()
        coEvery { prescriptionRepository.loadSyncedTaskByTaskId(any()) } returns emptyFlow()
        coEvery { medicationPlanRepository.loadMedicationSchedule(any()) } returns flowOf(null)
        coEvery { getPrescriptionByTaskIdUseCase(any()) } returns emptyFlow()

        testScope.runTest {
            controllerUnderTest.prescriptionSchedule.test {
                advanceUntilIdle()
                val state = awaitItem()
                assert(state.isErrorState)
            }
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `test add new time slot with scanned`() {
        val expectedPrescription = PrescriptionData.Synced(task = API_ACTIVE_SYNCED_TASK, now = now)

        coEvery { prescriptionRepository.loadScannedTaskByTaskId(any()) } returns emptyFlow()
        coEvery { prescriptionRepository.loadSyncedTaskByTaskId(any()) } returns flowOf(expectedPrescription.task)
        coEvery { medicationPlanRepository.loadMedicationSchedule(any()) } returns flowOf(null)
        coEvery { getPrescriptionByTaskIdUseCase(any()) } returns flowOf(expectedPrescription)
        coEvery { medicationPlanRepository.updateMedicationSchedule(any()) } returns Unit
        val uuid = UUID.randomUUID().toString()
        val expectedNotification = MedicationNotification(
            id = uuid,
            dosage = MedicationDosage("", ""),
            time = LocalTime(12, 0)
        )

        testScope.runTest {
            controllerUnderTest.prescriptionSchedule.test {
                awaitItem()
                advanceUntilIdle()
                awaitItem()
                controllerUnderTest.addNewTimeSlot(
                    dosage = MedicationDosage("", ""),
                    time = LocalTime(12, 0),
                    uuid = uuid
                )
            }
            advanceUntilIdle()
            coVerify {
                planMedicationScheduleUseCase(
                    withArg { medicationSchedule ->
                        assertEquals(
                            expectedNotification,
                            medicationSchedule.notifications.last() // Check the last added notification
                        )
                    }
                )
            }
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `remove notification`() {
        val expectedPrescription = PrescriptionData.Synced(task = API_ACTIVE_SYNCED_TASK_STRUCTURED_DOSAGE, now = now)

        coEvery { prescriptionRepository.loadScannedTaskByTaskId(any()) } returns emptyFlow()
        coEvery { prescriptionRepository.loadSyncedTaskByTaskId(any()) } returns flowOf(expectedPrescription.task)
        coEvery { medicationPlanRepository.loadMedicationSchedule(any()) } returns flowOf(null)
        coEvery { getPrescriptionByTaskIdUseCase(any()) } returns flowOf(expectedPrescription)
        coEvery { medicationPlanRepository.updateMedicationSchedule(any()) } returns Unit

        testScope.runTest {
            controllerUnderTest.prescriptionSchedule.test {
                awaitItem()
                advanceUntilIdle()
                val item = awaitItem()
                val notifications = item.data!!.medicationSchedule.notifications
                assertEquals(2, notifications.size)
                val notificationToDelete = notifications[0]
                controllerUnderTest.removeNotification(notificationToDelete)
            }
            advanceUntilIdle()
            coVerify {
                planMedicationScheduleUseCase(
                    withArg { medicationSchedule ->
                        assert(medicationSchedule.notifications.size == 1)
                    }
                )
            }
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `test modify notification time`() {
        val expectedPrescription = PrescriptionData.Synced(task = API_ACTIVE_SYNCED_TASK_STRUCTURED_DOSAGE, now = now)

        coEvery { prescriptionRepository.loadScannedTaskByTaskId(any()) } returns emptyFlow()
        coEvery { prescriptionRepository.loadSyncedTaskByTaskId(any()) } returns flowOf(expectedPrescription.task)
        coEvery { medicationPlanRepository.loadMedicationSchedule(any()) } returns flowOf(null)
        coEvery { getPrescriptionByTaskIdUseCase(any()) } returns flowOf(expectedPrescription)
        coEvery { medicationPlanRepository.updateMedicationSchedule(any()) } returns Unit
        val newTime = LocalTime(14, 0)

        testScope.runTest {
            controllerUnderTest.prescriptionSchedule.test {
                awaitItem()
                advanceUntilIdle()
                val item = awaitItem()
                val notifications = item.data!!.medicationSchedule.notifications
                assertEquals(2, notifications.size)
                val notificationToModify = notifications[0]
                assertEquals(LocalTime(8, 0), notificationToModify.time)
                controllerUnderTest.modifyNotificationTime(notification = notificationToModify, time = newTime)
            }
            advanceUntilIdle()
            coVerify {
                planMedicationScheduleUseCase(
                    withArg { medicationSchedule ->
                        assertEquals(newTime, medicationSchedule.notifications[0].time)
                    }
                )
            }
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `test modify notification dosage`() {
        val expectedPrescription = PrescriptionData.Synced(task = API_ACTIVE_SYNCED_TASK_STRUCTURED_DOSAGE, now = now)

        coEvery { prescriptionRepository.loadScannedTaskByTaskId(any()) } returns emptyFlow()
        coEvery { prescriptionRepository.loadSyncedTaskByTaskId(any()) } returns flowOf(expectedPrescription.task)
        coEvery { medicationPlanRepository.loadMedicationSchedule(any()) } returns flowOf(null)
        coEvery { getPrescriptionByTaskIdUseCase(any()) } returns flowOf(expectedPrescription)
        coEvery { medicationPlanRepository.updateMedicationSchedule(any()) } returns Unit
        val newMediationDosage = MedicationDosage("Tabletten", "2")

        testScope.runTest {
            controllerUnderTest.prescriptionSchedule.test {
                awaitItem()
                advanceUntilIdle()
                val item = awaitItem()
                val notifications = item.data!!.medicationSchedule.notifications
                assertEquals(2, notifications.size)
                val notificationToModify = notifications[0]
                assertEquals(MedicationDosage("TAB", "1"), notificationToModify.dosage)
                controllerUnderTest.modifyDosage(notification = notificationToModify, dosage = newMediationDosage)
            }
            advanceUntilIdle()
            coVerify {
                planMedicationScheduleUseCase(
                    withArg { medicationSchedule ->
                        assertEquals(newMediationDosage, medicationSchedule.notifications[0].dosage)
                    }
                )
            }
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `test activate schedule`() {
        coEvery { prescriptionRepository.loadScannedTaskByTaskId(any()) } returns flowOf(API_ACTIVE_SCANNED_TASK)
        coEvery { prescriptionRepository.loadSyncedTaskByTaskId(any()) } returns emptyFlow()
        coEvery { medicationPlanRepository.loadMedicationSchedule(any()) } returns flowOf(null)
        coEvery { getPrescriptionByTaskIdUseCase(any()) } returns
            flowOf(PrescriptionData.Scanned(API_ACTIVE_SCANNED_TASK))
        coEvery { medicationPlanRepository.updateMedicationSchedule(any()) } returns Unit

        testScope.runTest {
            controllerUnderTest.prescriptionSchedule.test {
                awaitItem()
                advanceUntilIdle()
                val item = awaitItem()
                assert(item.isDataState)
                assertEquals(false, item.data!!.medicationSchedule.isActive)
                controllerUnderTest.activateSchedule()
            }
            advanceUntilIdle()
            coVerify {
                planMedicationScheduleUseCase(
                    withArg { medicationSchedule ->
                        assert(medicationSchedule.isActive)
                    }
                )
            }
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `test deActivate schedule`() {
        coEvery { prescriptionRepository.loadScannedTaskByTaskId(any()) } returns flowOf(API_ACTIVE_SCANNED_TASK)
        coEvery { prescriptionRepository.loadSyncedTaskByTaskId(any()) } returns emptyFlow()
        val prescription = PrescriptionData.Scanned(API_ACTIVE_SCANNED_TASK)
        val activeMedicationSchedule = prescription.toMedicationSchedule().copy(isActive = true)
        coEvery { medicationPlanRepository.loadMedicationSchedule(any()) } returns flowOf(activeMedicationSchedule)
        coEvery { getPrescriptionByTaskIdUseCase(any()) } returns
            flowOf(prescription)
        coEvery { medicationPlanRepository.updateMedicationSchedule(any()) } returns Unit

        testScope.runTest {
            controllerUnderTest.prescriptionSchedule.test {
                awaitItem()
                advanceUntilIdle()
                val item = awaitItem()
                assert(item.isDataState)
                assertEquals(true, item.data!!.medicationSchedule.isActive)
                controllerUnderTest.deactivateSchedule()
            }
            advanceUntilIdle()
            coVerify {
                planMedicationScheduleUseCase(
                    withArg { medicationSchedule ->
                        assert(!medicationSchedule.isActive)
                    }
                )
            }
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `test change scheduled date with date event start date`() {
        val prescription = PrescriptionData.Scanned(API_ACTIVE_SCANNED_TASK)
        coEvery { prescriptionRepository.loadScannedTaskByTaskId(any()) } returns flowOf(prescription.task)
        coEvery { prescriptionRepository.loadSyncedTaskByTaskId(any()) } returns emptyFlow()
        coEvery { medicationPlanRepository.loadMedicationSchedule(any()) } returns flowOf(null)
        coEvery { getPrescriptionByTaskIdUseCase(any()) } returns
            flowOf(prescription)
        coEvery { medicationPlanRepository.updateMedicationSchedule(any()) } returns Unit
        val newDate = LocalDate(2024, 10, 22)

        testScope.runTest {
            controllerUnderTest.prescriptionSchedule.test {
                awaitItem()
                advanceUntilIdle()
                val item = awaitItem()
                assert(item.isDataState)
                assertEquals(now.toLocalDate(), item.data!!.medicationSchedule.start)
                controllerUnderTest.changeScheduledDate(DateEvent.StartDate(newDate))
            }
            advanceUntilIdle()
            coVerify {
                planMedicationScheduleUseCase(
                    withArg { medicationSchedule ->
                        assertEquals(newDate, medicationSchedule.start)
                    }
                )
            }
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `test change scheduled date with date event end date`() {
        coEvery { prescriptionRepository.loadScannedTaskByTaskId(any()) } returns flowOf(API_ACTIVE_SCANNED_TASK)
        coEvery { prescriptionRepository.loadSyncedTaskByTaskId(any()) } returns emptyFlow()
        val prescription = PrescriptionData.Scanned(API_ACTIVE_SCANNED_TASK)
        coEvery { medicationPlanRepository.loadMedicationSchedule(any()) } returns flowOf(null)
        coEvery { getPrescriptionByTaskIdUseCase(any()) } returns
            flowOf(prescription)
        coEvery { medicationPlanRepository.updateMedicationSchedule(any()) } returns Unit
        val newDate = LocalDate(2025, 10, 22)

        testScope.runTest {
            controllerUnderTest.prescriptionSchedule.test {
                awaitItem()
                advanceUntilIdle()
                val item = awaitItem()
                assert(item.isDataState)
                // default endDate of scanned is 10 days after
                assertEquals(now.plus(10.days).toLocalDate(), item.data!!.medicationSchedule.end)
                controllerUnderTest.changeScheduledDate(DateEvent.EndDate(newDate))
            }
            advanceUntilIdle()
            coVerify {
                planMedicationScheduleUseCase(
                    withArg { medicationSchedule ->
                        assertEquals(newDate, medicationSchedule.end)
                    }
                )
            }
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `test save endless date range`() {
        coEvery { prescriptionRepository.loadScannedTaskByTaskId(any()) } returns flowOf(API_ACTIVE_SCANNED_TASK)
        coEvery { prescriptionRepository.loadSyncedTaskByTaskId(any()) } returns emptyFlow()
        val prescription = PrescriptionData.Scanned(API_ACTIVE_SCANNED_TASK)
        coEvery { medicationPlanRepository.loadMedicationSchedule(any()) } returns flowOf(null)
        coEvery { getPrescriptionByTaskIdUseCase(any()) } returns
            flowOf(prescription)
        coEvery { medicationPlanRepository.updateMedicationSchedule(any()) } returns Unit

        val currentDate = now.toLocalDate()

        testScope.runTest {
            controllerUnderTest.prescriptionSchedule.test {
                awaitItem()
                advanceUntilIdle()
                val item = awaitItem()
                assert(item.isDataState)
                assertEquals(currentDate, item.data!!.medicationSchedule.start)
                assertEquals(now.plus(10.days).toLocalDate(), item.data!!.medicationSchedule.end)
                controllerUnderTest.saveEndlessDateRange(currentDate)
            }
            advanceUntilIdle()
            coVerify {
                planMedicationScheduleUseCase(
                    withArg { medicationSchedule ->
                        assertEquals(currentDate, medicationSchedule.start)
                        assertEquals(de.gematik.ti.erp.app.utils.maxLocalDate(), medicationSchedule.end)
                    }
                )
            }
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `test calculate individual date range for scanned prescription`() {
        coEvery { prescriptionRepository.loadScannedTaskByTaskId(any()) } returns flowOf(API_ACTIVE_SCANNED_TASK)
        coEvery { prescriptionRepository.loadSyncedTaskByTaskId(any()) } returns emptyFlow()
        val prescription = PrescriptionData.Scanned(API_ACTIVE_SCANNED_TASK)
        coEvery { medicationPlanRepository.loadMedicationSchedule(any()) } returns flowOf(null)
        coEvery { getPrescriptionByTaskIdUseCase(any()) } returns
            flowOf(prescription)
        coEvery { medicationPlanRepository.updateMedicationSchedule(any()) } returns Unit

        val currentDate = now.toLocalDate()

        testScope.runTest {
            controllerUnderTest.prescriptionSchedule.test {
                awaitItem()
                advanceUntilIdle()
                val item = awaitItem()
                assert(item.isDataState)
                assertEquals(currentDate, item.data!!.medicationSchedule.start)
                assertEquals(now.plus(10.days).toLocalDate(), item.data!!.medicationSchedule.end)
                controllerUnderTest.calculateIndividualDateRange()
            }
            advanceUntilIdle()
            coVerify {
                planMedicationScheduleUseCase(
                    withArg { medicationSchedule ->
                        assertEquals(currentDate, medicationSchedule.start)
                        assertEquals(currentDate, medicationSchedule.end)
                    }
                )
            }
        }
    }

    /*
    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `test calculate individual date range for synced pieceable prescription with structured dosage and amount`() {
        val prescription = PrescriptionData.Synced(API_ACTIVE_SYNCED_TASK_STRUCTURED_DOSAGE)
        coEvery { prescriptionRepository.loadScannedTaskByTaskId(any()) } returns emptyFlow()
        coEvery { prescriptionRepository.loadSyncedTaskByTaskId(any()) } returns
            flowOf(prescription.task)
        coEvery { medicationPlanRepository.loadMedicationSchedule(any()) } returns
            flowOf(prescription.toMedicationSchedule(now))
        coEvery { getPrescriptionByTaskIdUseCase(any()) } returns
            flowOf(prescription)
        coEvery { medicationPlanRepository.updateMedicationSchedule(any()) } returns Unit

        val currentDate = now.toLocalDate()

        testScope.runTest {
            controllerUnderTest.prescriptionSchedule.test {
                awaitItem()
                advanceUntilIdle()
                val item = awaitItem()
                assert(item.isDataState)
                assertEquals(currentDate, item.data!!.medicationSchedule.start)
                assertEquals(now.plus(5.days).toLocalDate(), item.data!!.medicationSchedule.end)
                controllerUnderTest.calculateIndividualDateRange()
            }
            advanceUntilIdle()
            coVerify {
                planMedicationScheduleUseCase(
                    withArg { medicationSchedule ->
                        assertEquals(currentDate, medicationSchedule.start)
                        assertEquals(now.plus(5.days).toLocalDate(), medicationSchedule.end)
                    }
                )
            }
        }
    }
     */
}
