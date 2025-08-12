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
import de.gematik.ti.erp.app.medicationplan.usecase.DeactivateMedicationScheduleUseCase
import de.gematik.ti.erp.app.medicationplan.usecase.DeleteMedicationScheduleNotificationUseCase
import de.gematik.ti.erp.app.medicationplan.usecase.DeleteMedicationScheduleUseCase
import de.gematik.ti.erp.app.medicationplan.usecase.GetMedicationScheduleByTaskIdUseCase
import de.gematik.ti.erp.app.medicationplan.usecase.ScheduleMedicationScheduleUseCase
import de.gematik.ti.erp.app.medicationplan.usecase.SetMedicationScheduleNotificationDosageUseCase
import de.gematik.ti.erp.app.medicationplan.usecase.SetMedicationScheduleNotificationTimeUseCase
import de.gematik.ti.erp.app.medicationplan.usecase.SetOrCreateActiveMedicationScheduleUseCase
import de.gematik.ti.erp.app.medicationplan.usecase.SetOrCreateMedicationScheduleNotificationUseCase
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
import org.junit.After
import org.junit.Before

class MedicationPlanScheduleDurationAndIntervalScreenDetailScreenControllerTest {
    private val now = Instant.parse("2024-01-01T12:00:00Z")
    private val profileRepository: ProfileRepository = mockk()
    private val medicationPlanRepository: MedicationPlanRepository = mockk()
    private val prescriptionRepository: PrescriptionRepository = mockk()
    private val getPrescriptionByTaskIdUseCase: GetPrescriptionByTaskIdUseCase = mockk()
    private val medicationPlanNotificationScheduler: MedicationPlanNotificationScheduler = mockk()
    private val dispatcher = StandardTestDispatcher()
    private val testScope = TestScope(dispatcher)
    private lateinit var controllerUnderTest: MedicationPlanScheduleDetailScreenController
    private lateinit var getActiveProfileUseCase: GetActiveProfileUseCase
    private lateinit var getMedicationScheduleByTaskIdUseCase: GetMedicationScheduleByTaskIdUseCase
    private lateinit var setOrCreateActiveMedicationScheduleUseCase: SetOrCreateActiveMedicationScheduleUseCase
    private lateinit var setOrCreateMedicationScheduleNotificationUseCase: SetOrCreateMedicationScheduleNotificationUseCase
    private lateinit var deactivateMedicationScheduleUseCase: DeactivateMedicationScheduleUseCase
    private lateinit var deleteMedicationScheduleNotificationUseCase: DeleteMedicationScheduleNotificationUseCase
    private lateinit var setMedicationScheduleNotificationDosageUseCase: SetMedicationScheduleNotificationDosageUseCase
    private lateinit var setMedicationScheduleNotificationTimeUseCase: SetMedicationScheduleNotificationTimeUseCase
    private lateinit var scheduleMedicationScheduleUseCase: ScheduleMedicationScheduleUseCase
    private lateinit var deleteMedicationScheduleUseCase: DeleteMedicationScheduleUseCase

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
        setOrCreateActiveMedicationScheduleUseCase = SetOrCreateActiveMedicationScheduleUseCase(
            medicationPlanRepository
        )
        setOrCreateMedicationScheduleNotificationUseCase = SetOrCreateMedicationScheduleNotificationUseCase(
            medicationPlanRepository
        )
        deactivateMedicationScheduleUseCase = DeactivateMedicationScheduleUseCase(
            medicationPlanRepository
        )
        deleteMedicationScheduleNotificationUseCase = DeleteMedicationScheduleNotificationUseCase(
            medicationPlanRepository
        )
        setMedicationScheduleNotificationDosageUseCase = SetMedicationScheduleNotificationDosageUseCase(
            medicationPlanRepository
        )
        setMedicationScheduleNotificationTimeUseCase = SetMedicationScheduleNotificationTimeUseCase(
            medicationPlanRepository
        )
        deleteMedicationScheduleUseCase = DeleteMedicationScheduleUseCase(
            medicationPlanRepository
        )

        controllerUnderTest = MedicationPlanScheduleDetailScreenController(
            getPrescriptionByTaskIdUseCase = getPrescriptionByTaskIdUseCase,
            getMedicationScheduleByTaskIdUseCase = getMedicationScheduleByTaskIdUseCase,
            setOrCreateActiveMedicationScheduleUseCase = setOrCreateActiveMedicationScheduleUseCase,
            setOrCreateMedicationScheduleNotificationUseCase = setOrCreateMedicationScheduleNotificationUseCase,
            deactivateMedicationScheduleUseCase = deactivateMedicationScheduleUseCase,
            deleteMedicationScheduleNotificationUseCase = deleteMedicationScheduleNotificationUseCase,
            setMedicationScheduleNotificationDosageUseCase = setMedicationScheduleNotificationDosageUseCase,
            setMedicationScheduleNotificationTimeUseCase = setMedicationScheduleNotificationTimeUseCase,
            scheduleMedicationScheduleUseCase = scheduleMedicationScheduleUseCase,
            deleteMedicationScheduleUseCase = deleteMedicationScheduleUseCase,
            taskId = "taskId"

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
        fun `test initial state with scanned prescription`() {
            coEvery { prescriptionRepository.loadScannedTaskByTaskId(any()) } returns flowOf(API_ACTIVE_SCANNED_TASK)
            coEvery { prescriptionRepository.loadSyncedTaskByTaskId(any()) } returns emptyFlow()
            coEvery { medicationPlanRepository.getMedicationSchedule(any()) } returns flowOf(null)
            coEvery { getPrescriptionByTaskIdUseCase(any()) } returns
                flowOf(PrescriptionData.Scanned(API_ACTIVE_SCANNED_TASK))

            testScope.runTest {
                controllerUnderTest.medicationSchedule.test {
                    val loading = awaitItem()
                    assertEquals(UiState.Loading(), loading)
                    advanceUntilIdle()
                    val data = awaitItem()
                    assertEquals(
                        UiState.Data(
                            PrescriptionData.Scanned(
                                API_ACTIVE_SCANNED_TASK
                            ).toMedicationSchedule(now)
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
            coEvery { medicationPlanRepository.getMedicationSchedule(any()) } returns flowOf(null)

            coEvery { getPrescriptionByTaskIdUseCase(any()) } returns flowOf(expectedPrescription)
            val expectedMedicationSchedule = expectedPrescription.toMedicationSchedule(now)

            testScope.runTest {
                controllerUnderTest.medicationSchedule.test {
                    val loading = awaitItem()
                    assertEquals(UiState.Loading(), loading)
                    advanceUntilIdle()
                    val data = awaitItem()
                    assertEquals(
                        UiState.Data(
                            expectedMedicationSchedule
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
            coEvery { medicationPlanRepository.getMedicationSchedule(any()) } returns flowOf(medicationSchedule)
            coEvery { getPrescriptionByTaskIdUseCase(any()) } returns flowOf(expectedPrescription)

            testScope.runTest {
                controllerUnderTest.medicationSchedule.test {
                    val loading = awaitItem()
                    assertEquals(UiState.Loading(), loading)
                    advanceUntilIdle()
                    val data = awaitItem()
                    assertEquals(
                        UiState.Data(
                            medicationSchedule
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
            coEvery { medicationPlanRepository.getMedicationSchedule(any()) } returns flowOf(null)
            coEvery { getPrescriptionByTaskIdUseCase(any()) } returns emptyFlow()

            testScope.runTest {
                controllerUnderTest.medicationSchedule.test {
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
            coEvery { medicationPlanRepository.getMedicationSchedule(any()) } returns flowOf(null)
            coEvery { getPrescriptionByTaskIdUseCase(any()) } returns flowOf(expectedPrescription)
            coEvery { medicationPlanRepository.setOrCreateMedicationScheduleNotification(any(), any()) } returns Unit
            val uuid = UUID.randomUUID().toString()
            val expectedNotification = MedicationScheduleNotification(
                id = uuid,
                dosage = MedicationScheduleNotificationDosage("", ""),
                time = LocalTime(12, 0)
            )

            testScope.runTest {
                controllerUnderTest.medicationSchedule.test {
                    awaitItem()
                    advanceUntilIdle()
                    awaitItem()
                    controllerUnderTest.addNewMedicationNotification(
                        dosage = MedicationScheduleNotificationDosage("", ""),
                        time = LocalTime(12, 0),
                        uuid = uuid
                    )
                    val updatedSchedule = awaitItem()
                    assertEquals(expectedNotification, updatedSchedule.data?.notifications?.last())
                }
            }
        }

        @OptIn(ExperimentalCoroutinesApi::class)
        @Test
        fun `remove notification`() {
            val expectedPrescription = PrescriptionData.Synced(task = API_ACTIVE_SYNCED_TASK_STRUCTURED_DOSAGE, now = now)

            coEvery { prescriptionRepository.loadScannedTaskByTaskId(any()) } returns emptyFlow()
            coEvery { prescriptionRepository.loadSyncedTaskByTaskId(any()) } returns flowOf(expectedPrescription.task)
            coEvery { medicationPlanRepository.getMedicationSchedule(any()) } returns flowOf(null)
            coEvery { getPrescriptionByTaskIdUseCase(any()) } returns flowOf(expectedPrescription)
            coEvery { medicationPlanRepository.deleteMedicationScheduleNotification(any()) } returns Unit
            testScope.runTest {
                controllerUnderTest.medicationSchedule.test {
                    awaitItem()
                    advanceUntilIdle()
                    val item = awaitItem()
                    val notifications = item.data!!.notifications
                    assertEquals(2, notifications.size)
                    val notificationToDelete = notifications[0]
                    controllerUnderTest.removeMedicationNotification(notificationToDelete)
                    val updatedSchedule = awaitItem()
                    assertEquals(1, updatedSchedule.data?.notifications?.size)
                }
            }
        }

        @OptIn(ExperimentalCoroutinesApi::class)
        @Test
        fun `test modify notification time`() {
            val expectedPrescription = PrescriptionData.Synced(task = API_ACTIVE_SYNCED_TASK_STRUCTURED_DOSAGE, now = now)

            coEvery { prescriptionRepository.loadScannedTaskByTaskId(any()) } returns emptyFlow()
            coEvery { prescriptionRepository.loadSyncedTaskByTaskId(any()) } returns flowOf(expectedPrescription.task)
            coEvery { medicationPlanRepository.getMedicationSchedule(any()) } returns flowOf(null)
            coEvery { getPrescriptionByTaskIdUseCase(any()) } returns flowOf(expectedPrescription)
            coEvery { medicationPlanRepository.setMedicationScheduleNotificationTime(any(), any()) } returns Unit

            val newTime = LocalTime(14, 0)

            testScope.runTest {
                controllerUnderTest.medicationSchedule.test {
                    awaitItem()
                    advanceUntilIdle()
                    val item = awaitItem()
                    val notifications = item.data!!.notifications
                    assertEquals(2, notifications.size)
                    val notificationToModify = notifications[0]
                    assertEquals(LocalTime(8, 0), notificationToModify.time)
                    controllerUnderTest.changeMedicationNotificationTime(notification = notificationToModify, time = newTime)
                    val updatedSchedule = awaitItem()
                    updatedSchedule.data?.notifications?.get(0)?.let { assertEquals(newTime, it.time) }
                }
            }
        }

        @OptIn(ExperimentalCoroutinesApi::class)
        @Test
        fun `test modify notification dosage`() {
            val expectedPrescription = PrescriptionData.Synced(task = API_ACTIVE_SYNCED_TASK_STRUCTURED_DOSAGE, now = now)

            coEvery { prescriptionRepository.loadScannedTaskByTaskId(any()) } returns emptyFlow()
            coEvery { prescriptionRepository.loadSyncedTaskByTaskId(any()) } returns flowOf(expectedPrescription.task)
            coEvery { medicationPlanRepository.getMedicationSchedule(any()) } returns flowOf(null)
            coEvery { getPrescriptionByTaskIdUseCase(any()) } returns flowOf(expectedPrescription)
            coEvery { medicationPlanRepository.setMedicationScheduleNotificationDosage(any(), any()) } returns Unit

            val newMediationDosage = MedicationScheduleNotificationDosage("Tabletten", "2")

            testScope.runTest {
                controllerUnderTest.medicationSchedule.test {
                    awaitItem()
                    advanceUntilIdle()
                    val item = awaitItem()
                    val notifications = item.data!!.notifications
                    assertEquals(2, notifications.size)
                    val notificationToModify = notifications[0]
                    assertEquals(MedicationScheduleNotificationDosage("TAB", "1"), notificationToModify.dosage)
                    controllerUnderTest.changeMedicationNotificationDosage(notification = notificationToModify, dosage = newMediationDosage)
                    val updatedSchedule = awaitItem()
                    updatedSchedule.data?.notifications?.get(0)?.let { assertEquals(newMediationDosage, it.dosage) }
                }
            }
        }

        @OptIn(ExperimentalCoroutinesApi::class)
        @Test
        fun `test activate schedule`() {
            coEvery { prescriptionRepository.loadScannedTaskByTaskId(any()) } returns flowOf(API_ACTIVE_SCANNED_TASK)
            coEvery { prescriptionRepository.loadSyncedTaskByTaskId(any()) } returns emptyFlow()
            coEvery { medicationPlanRepository.getMedicationSchedule(any()) } returns flowOf(null)
            coEvery { getPrescriptionByTaskIdUseCase(any()) } returns
                flowOf(PrescriptionData.Scanned(API_ACTIVE_SCANNED_TASK))
            coEvery { medicationPlanRepository.setOrCreateActiveMedicationSchedule(any()) } returns Unit

            testScope.runTest {
                controllerUnderTest.medicationSchedule.test {
                    awaitItem()
                    advanceUntilIdle()
                    val item = awaitItem()
                    assert(item.isDataState)
                    assertEquals(false, item.data!!.isActive)
                    controllerUnderTest.activateSchedule()
                    val updatedSchedule = awaitItem()
                    assertEquals(true, updatedSchedule.data?.isActive)
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
            coEvery { medicationPlanRepository.getMedicationSchedule(any()) } returns flowOf(activeMedicationSchedule)
            coEvery { getPrescriptionByTaskIdUseCase(any()) } returns
                flowOf(prescription)
            coEvery { medicationPlanRepository.deactivateMedicationSchedule(any()) } returns Unit

            testScope.runTest {
                controllerUnderTest.medicationSchedule.test {
                    awaitItem()
                    advanceUntilIdle()
                    val item = awaitItem()
                    assert(item.isDataState)
                    assertEquals(true, item.data!!.isActive)
                    controllerUnderTest.deactivateSchedule()
                    val updatedSchedule = awaitItem()
                    assertEquals(false, updatedSchedule.data?.isActive)
                }
            }
        }

     */
}
