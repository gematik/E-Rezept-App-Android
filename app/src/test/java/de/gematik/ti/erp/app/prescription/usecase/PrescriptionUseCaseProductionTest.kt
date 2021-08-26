/*
 * Copyright (c) 2021 gematik GmbH
 * 
 * Licensed under the EUPL, Version 1.2 or – as soon they will be approved by
 * the European Commission - subsequent versions of the EUPL (the Licence);
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at:
 * 
 *     https://joinup.ec.europa.eu/software/page/eupl
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the Licence is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Licence for the specific language governing permissions and
 * limitations under the Licence.
 * 
 */

package de.gematik.ti.erp.app.prescription.usecase

import de.gematik.ti.erp.app.idp.usecase.IdpUseCase
import de.gematik.ti.erp.app.prescription.repository.Mapper
import de.gematik.ti.erp.app.prescription.repository.PrescriptionRepository
import de.gematik.ti.erp.app.utils.CoroutineTestRule
import de.gematik.ti.erp.app.utils.TEST_TASK_GROUP_SCANNED
import de.gematik.ti.erp.app.utils.TEST_TASK_GROUP_SYNCED
import de.gematik.ti.erp.app.utils.testTasks
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.toCollection
import kotlinx.coroutines.test.runBlockingTest
import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import java.time.OffsetDateTime

@ExperimentalCoroutinesApi
class PrescriptionUseCaseProductionTest {

    private lateinit var useCase: PrescriptionUseCaseProduction

    @MockK
    lateinit var repo: PrescriptionRepository

    @MockK
    lateinit var idpUseCase: IdpUseCase

    @MockK
    lateinit var mapper: Mapper

    @get:Rule
    val coroutineRule = CoroutineTestRule()

    @Before
    fun setup() {
        MockKAnnotations.init(this)

        useCase = PrescriptionUseCaseProduction(repo, mapper)

        every { repo.tasks() } answers { flowOf(testTasks()) }
        every { repo.syncedTasksWithoutBundle() } answers { flowOf(testTasks().filter { it.scannedOn == null }) }
        every { repo.scannedTasksWithoutBundle() } answers { flowOf(testTasks().filter { it.scannedOn != null }) }
    }

    @Test
    fun `tasks - should return every task`() =
        coroutineRule.testDispatcher.runBlockingTest {
            useCase.tasks().toCollection(mutableListOf()).first().let {
                val expectedTasks = (TEST_TASK_GROUP_SCANNED + TEST_TASK_GROUP_SYNCED).sortedArray()
                assertEquals(expectedTasks.size, it.size)
                assertArrayEquals(expectedTasks, it.map { it.taskId }.sorted().toTypedArray())
            }
        }

    @Test
    fun `syncedTasks - should only return synced tasks`() =
        coroutineRule.testDispatcher.runBlockingTest {
            useCase.syncedTasks().toCollection(mutableListOf()).first().let {
                val expectedTasks = TEST_TASK_GROUP_SYNCED.sortedArray()
                assertEquals(expectedTasks.size, it.size)
                assertArrayEquals(expectedTasks, it.map { it.taskId }.sorted().toTypedArray())
            }
        }

    @Test
    fun `scannedTasks - should only return scanned tasks`() =
        coroutineRule.testDispatcher.runBlockingTest {
            useCase.scannedTasks().toCollection(mutableListOf()).first().let {
                val expectedTasks = TEST_TASK_GROUP_SCANNED.sortedArray()
                assertEquals(expectedTasks.size, it.size)
                assertArrayEquals(expectedTasks, it.map { it.taskId }.sorted().toTypedArray())
            }
        }

    @Test
    fun `edit scanPrescriptionsName`() =
        coroutineRule.testDispatcher.runBlockingTest {
            val scanSessionEnd = OffsetDateTime.now()
            every { repo.updateScanSessionName(null, scanSessionEnd) } answers {}
            useCase.editScannedPrescriptionsName("", scanSessionEnd)
            useCase.editScannedPrescriptionsName("     ", scanSessionEnd)
            every { repo.updateScanSessionName("Dr. Test", scanSessionEnd) } answers {}
            useCase.editScannedPrescriptionsName(" Dr. Test  ", scanSessionEnd)
        }
}
