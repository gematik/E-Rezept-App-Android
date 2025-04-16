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

package de.gematik.ti.erp.app.medicationplan.usecase

import de.gematik.ti.erp.app.medicationplan.MEDICATION_REQUEST
import de.gematik.ti.erp.app.medicationplan.model.MedicationPlanDosageInstruction
import de.gematik.ti.erp.app.medicationplan.scannedTask
import de.gematik.ti.erp.app.medicationplan.syncedTask
import de.gematik.ti.erp.app.prescription.repository.PrescriptionRepository
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
    private val prescriptionRepository: PrescriptionRepository = mockk()
    private lateinit var useCase: GetDosageInstructionByTaskIdUseCase

    @BeforeTest
    fun setup() {
        useCase = GetDosageInstructionByTaskIdUseCase(
            repository = prescriptionRepository
        )
    }

    @Test
    fun `scanned prescription should return empty dosage instruction`() {
        coEvery { prescriptionRepository.loadScannedTaskByTaskId(any()) } returns flowOf(scannedTask)
        coEvery { prescriptionRepository.loadSyncedTaskByTaskId(any()) } returns emptyFlow()

        testScope.runTest {
            val dosageInstruction = useCase.invoke("taskId").first()
            assertEquals(MedicationPlanDosageInstruction.Empty, dosageInstruction)
        }
    }

    @Test
    fun `synced prescription should return empty dosage instruction`() {
        coEvery { prescriptionRepository.loadScannedTaskByTaskId(any()) } returns emptyFlow()
        coEvery { prescriptionRepository.loadSyncedTaskByTaskId(any()) } returns flowOf(
            syncedTask.copy(
                medicationRequest = MEDICATION_REQUEST.copy(dosageInstruction = null)
            )
        )

        testScope.runTest {
            val dosageInstruction = useCase.invoke("taskId").first()
            assertEquals(MedicationPlanDosageInstruction.Empty, dosageInstruction)
        }
    }

    @Test
    fun `synced prescription should return freetext dosage instruction`() {
        coEvery { prescriptionRepository.loadScannedTaskByTaskId(any()) } returns emptyFlow()
        coEvery { prescriptionRepository.loadSyncedTaskByTaskId(any()) } returns flowOf(
            syncedTask.copy(
                medicationRequest = MEDICATION_REQUEST.copy(dosageInstruction = "freetext")
            )
        )

        testScope.runTest {
            val dosageInstruction = useCase.invoke("taskId").first()
            assertEquals(MedicationPlanDosageInstruction.FreeText("freetext"), dosageInstruction)
        }
    }

    @Test
    fun `synced prescription should return structured dosage instruction`() {
        coEvery { prescriptionRepository.loadScannedTaskByTaskId(any()) } returns emptyFlow()
        coEvery { prescriptionRepository.loadSyncedTaskByTaskId(any()) } returns flowOf(
            syncedTask.copy(
                medicationRequest = MEDICATION_REQUEST.copy(dosageInstruction = "1-0-1-0")
            )
        )

        testScope.runTest {
            val dosageInstruction = useCase.invoke("taskId").first()
            assertEquals(
                MedicationPlanDosageInstruction.Structured(
                    text = "1-0-1-0",
                    interpretation = mapOf(
                        MedicationPlanDosageInstruction.DayTime.MORNING to "1",
                        MedicationPlanDosageInstruction.DayTime.EVENING to "1"
                    )
                ),
                dosageInstruction
            )
        }
    }
}
