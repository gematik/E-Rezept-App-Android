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

import de.gematik.ti.erp.app.medicationplan.alarm.MedicationPlanNotificationScheduler
import de.gematik.ti.erp.app.medicationplan.repository.MedicationPlanRepository
import de.gematik.ti.erp.app.medicationplan.usecase.GetMedicationScheduleByTaskIdUseCase
import de.gematik.ti.erp.app.medicationplan.usecase.ScheduleMedicationScheduleUseCase
import de.gematik.ti.erp.app.medicationplan.usecase.SetMedicationScheduleDurationUseCase
import de.gematik.ti.erp.app.medicationplan.usecase.SetMedicationScheduleIntervalUseCase
import de.gematik.ti.erp.app.prescription.repository.PrescriptionRepository
import de.gematik.ti.erp.app.prescription.usecase.GetPrescriptionByTaskIdUseCase
import de.gematik.ti.erp.app.profiles.repository.ProfileRepository
import de.gematik.ti.erp.app.profiles.usecase.GetActiveProfileUseCase
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.mockk
import io.mockk.spyk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import kotlinx.datetime.Instant
import kotlinx.datetime.plus
import org.junit.After
import org.junit.Before

class MedicationPlanScheduleDurationAndIntervalScreenDurationAndIntervalScreenControllerTest {
    private val now = Instant.parse("2024-01-01T12:00:00Z")
    private val profileRepository: ProfileRepository = mockk()
    private val medicationPlanRepository: MedicationPlanRepository = mockk()
    private val prescriptionRepository: PrescriptionRepository = mockk()
    private val getPrescriptionByTaskIdUseCase: GetPrescriptionByTaskIdUseCase = mockk()
    private val medicationPlanNotificationScheduler: MedicationPlanNotificationScheduler = mockk()
    private val dispatcher = StandardTestDispatcher()
    private val testScope = TestScope(dispatcher)
    private lateinit var controllerUnderTest: MedicationPlanScheduleDurationAndIntervalScreenController
    private lateinit var getActiveProfileUseCase: GetActiveProfileUseCase
    private lateinit var getMedicationScheduleByTaskIdUseCase: GetMedicationScheduleByTaskIdUseCase
    private lateinit var setMedicationScheduleIntervalUseCase: SetMedicationScheduleIntervalUseCase
    private lateinit var setMedicationScheduleDurationUseCase: SetMedicationScheduleDurationUseCase
    private lateinit var scheduleMedicationScheduleUseCase: ScheduleMedicationScheduleUseCase

    @OptIn(ExperimentalCoroutinesApi::class)
    @Before
    fun setUp() {
        MockKAnnotations.init(this)
        every { medicationPlanNotificationScheduler.scheduleMedicationSchedule(any()) } returns Unit
        Dispatchers.setMain(dispatcher)

        getActiveProfileUseCase = GetActiveProfileUseCase(profileRepository, dispatcher)
        scheduleMedicationScheduleUseCase = spyk(
            ScheduleMedicationScheduleUseCase(medicationPlanNotificationScheduler)
        )
        getMedicationScheduleByTaskIdUseCase = GetMedicationScheduleByTaskIdUseCase(
            medicationPlanRepository
        )
        setMedicationScheduleIntervalUseCase = SetMedicationScheduleIntervalUseCase(
            medicationPlanRepository
        )
        setMedicationScheduleDurationUseCase = SetMedicationScheduleDurationUseCase(medicationPlanRepository)

        controllerUnderTest = MedicationPlanScheduleDurationAndIntervalScreenController(
            getPrescriptionByTaskIdUseCase = getPrescriptionByTaskIdUseCase,
            getMedicationScheduleByTaskIdUseCase = getMedicationScheduleByTaskIdUseCase,
            setMedicationScheduleIntervalUseCase = setMedicationScheduleIntervalUseCase,
            setMedicationScheduleDurationUseCase = setMedicationScheduleDurationUseCase,
            scheduleMedicationScheduleUseCase = scheduleMedicationScheduleUseCase,
            taskId = "taskId",
            now = now
        )
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }
/*
    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `test change duration to personalized and then back to endless again`() {
        val prescription = PrescriptionData.Scanned(API_ACTIVE_SCANNED_TASK)
        coEvery { prescriptionRepository.loadScannedTaskByTaskId(any()) } returns flowOf(prescription.task)
        coEvery { prescriptionRepository.loadSyncedTaskByTaskId(any()) } returns emptyFlow()
        coEvery { medicationPlanRepository.getMedicationSchedule(any()) } returns flowOf(null)
        coEvery { getPrescriptionByTaskIdUseCase(any()) } returns
            flowOf(prescription)
        val newStartDate = LocalDate(2024, 10, 22)
        val newEndDate = LocalDate(2025, 10, 22)
        coEvery { medicationPlanRepository.setMedicationScheduleDuration(any(), any()) } returns Unit

        testScope.runTest {
            advanceUntilIdle()
            controllerUnderTest.medicationSchedule.test {
                val item = awaitItem()
                assert(item.isDataState)
                assertEquals(now.toLocalDate(), item.data!!.duration.startDate)
                controllerUnderTest.setMedicationScheduleDurationToPersonalized(startDate = newStartDate, endDate = newEndDate)
                var updatedSchedule = awaitItem()
                assertEquals(newStartDate, updatedSchedule.data?.duration?.startDate)
                assertEquals(newEndDate, updatedSchedule.data?.duration?.endDate)
                controllerUnderTest.changeMedicationScheduleDurationPersonalizedStartDate(startDate = now.toLocalDate())
                updatedSchedule = awaitItem()
                assertEquals(now.toLocalDate(), updatedSchedule.data?.duration?.startDate)
                controllerUnderTest.changeMedicationScheduleDurationPersonalizedEndDate(now.toLocalDate())
                updatedSchedule = awaitItem()
                assertEquals(now.toLocalDate(), updatedSchedule.data?.duration?.endDate)
                controllerUnderTest.setMedicationScheduleDurationToEndless()
                updatedSchedule = awaitItem()
                assert(updatedSchedule.data?.duration is MedicationScheduleDuration.Endless)
            }
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `test calculate individual date range for scanned prescription`() {
        coEvery { prescriptionRepository.loadScannedTaskByTaskId(any()) } returns flowOf(API_ACTIVE_SCANNED_TASK)
        coEvery { prescriptionRepository.loadSyncedTaskByTaskId(any()) } returns emptyFlow()
        val prescription = PrescriptionData.Scanned(API_ACTIVE_SCANNED_TASK)
        coEvery { medicationPlanRepository.getMedicationSchedule(any()) } returns flowOf(null)
        coEvery { getPrescriptionByTaskIdUseCase(any()) } returns
            flowOf(prescription)
        coEvery { medicationPlanRepository.setMedicationScheduleDuration(any(), any()) } returns Unit
        val currentDate = now.toLocalDate()

        testScope.runTest {
            advanceUntilIdle()
            controllerUnderTest.medicationSchedule.test {
                awaitItem()
                val item = awaitItem()
                assert(item.isDataState)
                assertEquals(currentDate, item.data!!.duration.startDate)
                assertEquals(now.plus(10.days).toLocalDate(), item.data!!.duration.endDate)
                controllerUnderTest.setMedicationScheduleDurationToEndOfPack()
                val updatedSchedule = awaitItem()
                assert(updatedSchedule.data?.duration is MedicationScheduleDuration.EndOfPack)
                assertEquals(now.toLocalDate(), updatedSchedule.data?.duration?.endDate)
            }
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `test calculate individual date range for synced pieceable prescription with structured dosage and amount`() {
        val prescription = PrescriptionData.Synced(API_ACTIVE_SYNCED_TASK_STRUCTURED_DOSAGE)
        coEvery { prescriptionRepository.loadScannedTaskByTaskId(any()) } returns emptyFlow()
        coEvery { prescriptionRepository.loadSyncedTaskByTaskId(any()) } returns
            flowOf(prescription.task)
        coEvery { medicationPlanRepository.getMedicationSchedule(any()) } returns
            flowOf(prescription.toMedicationSchedule(now))
        coEvery { getPrescriptionByTaskIdUseCase(any()) } returns
            flowOf(prescription)
        coEvery { medicationPlanRepository.setMedicationScheduleDuration(any(), any()) } returns Unit
        val currentDate = now.toLocalDate()

        testScope.runTest {
            advanceUntilIdle()
            controllerUnderTest.medicationSchedule.test {
                awaitItem()
                val item = awaitItem()
                assert(item.isDataState)
                assertEquals(currentDate, item.data!!.duration.startDate)
                assertEquals(now.toLocalDate(), item.data!!.duration.endDate)
                controllerUnderTest.setMedicationScheduleDurationToEndOfPack()
                val updatedSchedule = awaitItem()
                assert(updatedSchedule.data?.duration is MedicationScheduleDuration.EndOfPack)
                assertEquals(now.toLocalDate().plus(DatePeriod(days = 5)), updatedSchedule.data?.duration?.endDate)
            }
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `test set interval to every two days and then back to daily`() {
        val prescription = PrescriptionData.Synced(API_ACTIVE_SYNCED_TASK_STRUCTURED_DOSAGE)
        coEvery { prescriptionRepository.loadScannedTaskByTaskId(any()) } returns emptyFlow()
        coEvery { prescriptionRepository.loadSyncedTaskByTaskId(any()) } returns
            flowOf(prescription.task)
        coEvery { medicationPlanRepository.getMedicationSchedule(any()) } returns
            flowOf(prescription.toMedicationSchedule(now))
        coEvery { getPrescriptionByTaskIdUseCase(any()) } returns
            flowOf(prescription)
        coEvery { medicationPlanRepository.setMedicationScheduleInterval(any(), any()) } returns Unit

        testScope.runTest {
            advanceUntilIdle()
            controllerUnderTest.medicationSchedule.test {
                awaitItem()
                val item = awaitItem()
                assert(item.isDataState)
                assert(item.data?.interval is MedicationScheduleInterval.Daily)
                controllerUnderTest.setMedicationScheduleIntervalToEveryToDays()
                var updatedSchedule = awaitItem()
                assert(updatedSchedule.data?.interval is MedicationScheduleInterval.EveryTwoDays)
                controllerUnderTest.setMedicationScheduleIntervalToDaily()
                updatedSchedule = awaitItem()
                assert(updatedSchedule.data?.interval is MedicationScheduleInterval.Daily)
            }
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `test set interval to every monday and then select it again to remove monday`() {
        val prescription = PrescriptionData.Synced(API_ACTIVE_SYNCED_TASK_STRUCTURED_DOSAGE)
        coEvery { prescriptionRepository.loadScannedTaskByTaskId(any()) } returns emptyFlow()
        coEvery { prescriptionRepository.loadSyncedTaskByTaskId(any()) } returns
            flowOf(prescription.task)
        coEvery { medicationPlanRepository.getMedicationSchedule(any()) } returns
            flowOf(prescription.toMedicationSchedule(now))
        coEvery { getPrescriptionByTaskIdUseCase(any()) } returns
            flowOf(prescription)
        coEvery { medicationPlanRepository.setMedicationScheduleInterval(any(), any()) } returns Unit

        testScope.runTest {
            advanceUntilIdle()
            controllerUnderTest.medicationSchedule.test {
                awaitItem()
                val item = awaitItem()
                assert(item.isDataState)
                assert(item.data?.interval is MedicationScheduleInterval.Daily)
                controllerUnderTest.selectDayOfWeekAndSetMedicationScheduleIntervalToPersonalized(DayOfWeek.MONDAY)
                var updatedSchedule = awaitItem()
                assert(updatedSchedule.data?.interval is MedicationScheduleInterval.Personalized)
                val selectedDays = updatedSchedule.data?.interval as MedicationScheduleInterval.Personalized
                assert(selectedDays.selectedDays.contains(DayOfWeek.MONDAY))
                controllerUnderTest.selectDayOfWeekAndSetMedicationScheduleIntervalToPersonalized(DayOfWeek.MONDAY)
                updatedSchedule = awaitItem()
                assert(updatedSchedule.data?.interval is MedicationScheduleInterval.Daily)
            }
        }
    }

 */
}
