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

package de.gematik.ti.erp.app.medicationplan.usecase

import de.gematik.ti.erp.app.medicationplan.MEDICATION_REQUEST
import de.gematik.ti.erp.app.medicationplan.model.MedicationPlanDosageInstructionErpModel
import de.gematik.ti.erp.app.medicationplan.scannedTask
import de.gematik.ti.erp.app.medicationplan.syncedTask
import de.gematik.ti.erp.app.prescription.repository.TaskOperationsRepository
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

class GetDosageInstructionByTaskIdUseCaseTest {
    private val dispatcher = StandardTestDispatcher()
    private val testScope = TestScope(dispatcher)
    private val taskOperationsRepository: TaskOperationsRepository = mockk()
    private lateinit var useCase: GetDosageInstructionByTaskIdUseCase

    @BeforeTest
    fun setup() {
        useCase = GetDosageInstructionByTaskIdUseCase(
            repository = taskOperationsRepository
        )
    }

    @Test
    fun `scanned prescription should return empty dosage instruction`() {
        coEvery { taskOperationsRepository.loadScannedTaskByTaskId(any()) } returns flowOf(scannedTask)
        coEvery { taskOperationsRepository.loadSyncedTaskByTaskId(any()) } returns emptyFlow()

        testScope.runTest {
            val dosageInstruction = useCase.invoke("taskId").first()
            assertEquals(MedicationPlanDosageInstructionErpModel.Empty, dosageInstruction)
        }
    }

    @Test
    fun `synced prescription should return empty dosage instruction`() {
        coEvery { taskOperationsRepository.loadScannedTaskByTaskId(any()) } returns emptyFlow()
        coEvery { taskOperationsRepository.loadSyncedTaskByTaskId(any()) } returns flowOf(
            syncedTask.copy(
                medicationRequest = MEDICATION_REQUEST.copy(dosageInstruction = null)
            )
        )

        testScope.runTest {
            val dosageInstruction = useCase.invoke("taskId").first()
            assertEquals(MedicationPlanDosageInstructionErpModel.Empty, dosageInstruction)
        }
    }

    @Test
    fun `synced prescription should return freetext dosage instruction`() {
        coEvery { taskOperationsRepository.loadScannedTaskByTaskId(any()) } returns emptyFlow()
        coEvery { taskOperationsRepository.loadSyncedTaskByTaskId(any()) } returns flowOf(
            syncedTask.copy(
                medicationRequest = MEDICATION_REQUEST.copy(dosageInstruction = "freetext")
            )
        )

        testScope.runTest {
            val dosageInstruction = useCase.invoke("taskId").first()
            assertEquals(MedicationPlanDosageInstructionErpModel.FreeText("freetext"), dosageInstruction)
        }
    }

    @Test
    fun `synced prescription should return structured dosage instruction`() {
        coEvery { taskOperationsRepository.loadScannedTaskByTaskId(any()) } returns emptyFlow()
        coEvery { taskOperationsRepository.loadSyncedTaskByTaskId(any()) } returns flowOf(
            syncedTask.copy(
                medicationRequest = MEDICATION_REQUEST.copy(dosageInstruction = "1-0-1-0")
            )
        )

        testScope.runTest {
            val dosageInstruction = useCase.invoke("taskId").first()
            assertEquals(
                MedicationPlanDosageInstructionErpModel.Structured(
                    text = "1-0-1-0",
                    interpretation = mapOf(
                        MedicationPlanDosageInstructionErpModel.DayTime.MORNING to "1",
                        MedicationPlanDosageInstructionErpModel.DayTime.EVENING to "1"
                    )
                ),
                dosageInstruction
            )
        }
    }
}
