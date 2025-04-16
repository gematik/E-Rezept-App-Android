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

package de.gematik.ti.erp.app.medicationplan.repository

import de.gematik.ti.erp.app.medicationplan.MEDICATION_SCHEDULE
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import kotlin.test.assertEquals

class MedicationPlanRepositoryTest {

    private val localDataSource: MedicationPlanLocalDataSource = mockk()
    lateinit var repository: MedicationPlanRepository

    @Before
    fun setup() {
        repository = MedicationPlanRepository(localDataSource)
    }

    @Test
    fun `load medicationSchedule`() {
        val medicationSchedule = MEDICATION_SCHEDULE
        coEvery { localDataSource.loadMedicationSchedule(any()) } returns flowOf(medicationSchedule)
        runTest {
            val result = repository.loadMedicationSchedule("taskId").first()
            assertEquals(medicationSchedule, result)
        }
    }

    @Test
    fun `update medicationSchedule`() = runTest {
        val newMedicationSchedule = MEDICATION_SCHEDULE
        coEvery { localDataSource.updateMedicationSchedule(newMedicationSchedule) } returns Unit

        repository.updateMedicationSchedule(newMedicationSchedule)
        coVerify(exactly = 1) { localDataSource.updateMedicationSchedule(newMedicationSchedule) }
    }

    @Test
    fun `load all medicationSchedules`() = runTest {
        coEvery { localDataSource.loadAllMedicationSchedules() } returns flowOf(listOf(MEDICATION_SCHEDULE))
        repository.loadAllMedicationSchedules()
        coVerify(exactly = 1) { localDataSource.loadAllMedicationSchedules() }
    }
}
