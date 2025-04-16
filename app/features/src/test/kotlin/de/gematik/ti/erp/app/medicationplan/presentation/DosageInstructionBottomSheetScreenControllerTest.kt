/*
 * Copyright 2025, gematik GmbH
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
 * In case of changes by gematik find details in the "Readme" file.
 *
 * See the Licence for the specific language governing permissions and limitations under the Licence.
 */

package de.gematik.ti.erp.app.medicationplan.presentation

import de.gematik.ti.erp.app.medicationplan.model.MedicationPlanDosageInstruction

import de.gematik.ti.erp.app.medicationplan.usecase.GetDosageInstructionByTaskIdUseCase
import de.gematik.ti.erp.app.mocks.prescription.api.API_ACTIVE_SCANNED_TASK
import de.gematik.ti.erp.app.mocks.prescription.api.API_ACTIVE_SYNCED_TASK
import de.gematik.ti.erp.app.mocks.prescription.api.API_ACTIVE_SYNCED_TASK_STRUCTURED_DOSAGE

import de.gematik.ti.erp.app.prescription.repository.PrescriptionRepository
import de.gematik.ti.erp.app.utils.uistate.UiState.Companion.isDataState
import de.gematik.ti.erp.app.utils.uistate.UiState.Companion.isErrorState
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
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

@OptIn(ExperimentalCoroutinesApi::class)
class DosageInstructionBottomSheetScreenControllerTest {
    private val prescriptionRepository: PrescriptionRepository = mockk()
    private val dispatcher = StandardTestDispatcher()
    private val testScope = TestScope(dispatcher)

    private lateinit var controllerUnderTest: DosageInstructionBottomSheetScreenController
    private lateinit var getDosageInstructionByTaskIdUseCase: GetDosageInstructionByTaskIdUseCase

    @Before
    fun setUp() {
        MockKAnnotations.init(this)
        Dispatchers.setMain(dispatcher)
        getDosageInstructionByTaskIdUseCase = GetDosageInstructionByTaskIdUseCase(prescriptionRepository, dispatcher)

        controllerUnderTest = object : DosageInstructionBottomSheetScreenController(
            getDosageInstructionByTaskIdUseCase = getDosageInstructionByTaskIdUseCase,
            taskId = "taskId"
        ) {}
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `test getDosageInstruction empty`() {
        coEvery {
            prescriptionRepository
                .loadSyncedTaskByTaskId(any())
        } returns flowOf()
        coEvery { prescriptionRepository.loadScannedTaskByTaskId(any()) } returns flowOf()
        runTest {
            testScope.runTest {
                advanceUntilIdle()
                val dosageInstruction = controllerUnderTest.dosageInstruction.first()
                assert(dosageInstruction.isErrorState)
            }
        }
    }

    @Test
    fun `test getDosageInstruction loading`() {
        coEvery {
            prescriptionRepository
                .loadSyncedTaskByTaskId(any())
        } returns flowOf()
        coEvery { prescriptionRepository.loadScannedTaskByTaskId(any()) } returns flowOf()
        runTest {
            testScope.runTest {
                val dosageInstruction = controllerUnderTest.dosageInstruction.first()
                assert(dosageInstruction.isLoading)
            }
        }
    }

    @Test
    fun `test getDosageInstruction empty data for scanned prescription`() {
        coEvery {
            prescriptionRepository
                .loadSyncedTaskByTaskId(any())
        } returns flowOf()
        coEvery { prescriptionRepository.loadScannedTaskByTaskId(any()) } returns flowOf(API_ACTIVE_SCANNED_TASK)
        runTest {
            testScope.runTest {
                advanceUntilIdle()
                val dosageInstruction = controllerUnderTest.dosageInstruction.first()
                assert(dosageInstruction.isDataState)
                assert(dosageInstruction.data == MedicationPlanDosageInstruction.Empty)
            }
        }
    }

    @Test
    fun `test getDosageInstruction data for synced prescription`() {
        coEvery {
            prescriptionRepository
                .loadScannedTaskByTaskId(any())
        } returns flowOf()
        coEvery { prescriptionRepository.loadSyncedTaskByTaskId(any()) } returns flowOf(API_ACTIVE_SYNCED_TASK)
        runTest {
            testScope.runTest {
                advanceUntilIdle()
                val dosageInstruction = controllerUnderTest.dosageInstruction.first()
                assert(dosageInstruction.isDataState)
                assert(dosageInstruction.data == MedicationPlanDosageInstruction.FreeText("Dosage"))
            }
        }
    }

    @Test
    fun `test structured getDosageInstruction data for synced prescription`() {
        coEvery {
            prescriptionRepository
                .loadScannedTaskByTaskId(any())
        } returns flowOf()
        coEvery { prescriptionRepository.loadSyncedTaskByTaskId(any()) } returns
            flowOf(API_ACTIVE_SYNCED_TASK_STRUCTURED_DOSAGE)
        runTest {
            testScope.runTest {
                advanceUntilIdle()
                val dosageInstruction = controllerUnderTest.dosageInstruction.first()
                assert(dosageInstruction.isDataState)
                assert(
                    dosageInstruction.data == MedicationPlanDosageInstruction.Structured(
                        text = "1-0-1-0",
                        interpretation = mapOf(
                            MedicationPlanDosageInstruction.DayTime.MORNING to "1",
                            MedicationPlanDosageInstruction.DayTime.EVENING to "1"
                        )
                    )
                )
            }
        }
    }
}
