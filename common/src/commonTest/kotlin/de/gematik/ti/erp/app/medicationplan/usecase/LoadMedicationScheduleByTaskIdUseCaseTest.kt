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

import de.gematik.ti.erp.app.medicationplan.MEDICATION_SCHEDULE
import de.gematik.ti.erp.app.medicationplan.repository.MedicationPlanLocalDataSource
import de.gematik.ti.erp.app.medicationplan.repository.MedicationPlanRepository
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runTest
import kotlin.test.assertEquals
import kotlin.test.BeforeTest
import kotlin.test.Test

class LoadMedicationScheduleByTaskIdUseCaseTest {
    private val dispatcher = StandardTestDispatcher()
    private val medicationPlanLocalDataSource: MedicationPlanLocalDataSource = mockk()
    private lateinit var medicationPlanRepository: MedicationPlanRepository
    private lateinit var useCase: LoadMedicationScheduleByTaskIdUseCase

    @BeforeTest
    fun setup() {
        medicationPlanRepository = MedicationPlanRepository(
            localDataSource = medicationPlanLocalDataSource
        )
        useCase = LoadMedicationScheduleByTaskIdUseCase(
            medicationPlanRepository = medicationPlanRepository
        )
        coEvery { medicationPlanRepository.loadMedicationSchedule(any()) } returns flowOf(MEDICATION_SCHEDULE)
    }

    @Test
    fun `get medication plans for profile`() {
        runTest(dispatcher) {
            val medicationScheduleByTaskId = useCase.invoke("TaskId").first()
            assertEquals(MEDICATION_SCHEDULE, medicationScheduleByTaskId)
        }
    }
}
