/*
 * Copyright (c) 2022 gematik GmbH
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

import de.gematik.ti.erp.app.db.entities.Task
import de.gematik.ti.erp.app.prescription.repository.Mapper
import de.gematik.ti.erp.app.prescription.repository.PrescriptionRepository
import de.gematik.ti.erp.app.profiles.usecase.ProfilesUseCase
import de.gematik.ti.erp.app.utils.CoroutineTestRule
import de.gematik.ti.erp.app.utils.TEST_TASK_GROUP_SCANNED
import de.gematik.ti.erp.app.utils.TEST_TASK_GROUP_SYNCED
import de.gematik.ti.erp.app.utils.testTasks
import de.gematik.ti.erp.app.utils.validScannedCode
import io.mockk.MockKAnnotations
import io.mockk.coEvery
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
import kotlinx.coroutines.flow.flow

@ExperimentalCoroutinesApi
class PrescriptionUseCaseProductionTest {

    private lateinit var useCase: PrescriptionUseCaseProduction

    @MockK
    lateinit var repo: PrescriptionRepository

    @MockK
    lateinit var mapper: Mapper

    @MockK
    lateinit var profilesUseCase: ProfilesUseCase

    @get:Rule
    val coroutineRule = CoroutineTestRule()

    @Before
    fun setup() {
        MockKAnnotations.init(this)

        useCase = PrescriptionUseCaseProduction(repo, mapper, profilesUseCase)

        every { repo.tasks(any()) } answers { flowOf(testTasks()) }
        every { repo.syncedTasksWithoutBundle(any()) } answers { flowOf(testTasks().filter { it.scannedOn == null }) }
        every { repo.scannedTasksWithoutBundle(any()) } answers { flowOf(testTasks().filter { it.scannedOn != null }) }
        every { profilesUseCase.activeProfileName() } returns flow { emit("Tester") }
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

    @Test
    fun `test saveToDatabase() with three tasks`() = coroutineRule.testDispatcher.runBlockingTest {
        val capTasks = mutableListOf<List<Task>>()
        coEvery { useCase.saveScannedTasks(capture(capTasks)) } coAnswers { }
        useCase.mapScannedCodeToTask(listOf(validScannedCode))

        val tasks = capTasks.first()

        assertEquals(
            "234fabe0964598efd23f34dd23e122b2323344ea8e8934dae23e2a9a934513bc",
            tasks[0].taskId
        )
        assertEquals(
            "2aef43b8c5e8f2d3d7aef64598b3c40e1d9e348f75d62fd39fe4a7bc5c923de8",
            tasks[1].taskId
        )
        assertEquals(
            "5e78f21cd6abc35edf4f1726c3d451ea2736d547a263f45726bc13a47e65d189",
            tasks[2].taskId
        )

        assertEquals(
            "777bea0e13cc9c42ceec14aec3ddee2263325dc2c6c699db115f58fe423607ea",
            tasks[0].accessCode
        )
        assertEquals(
            "0936cfa582b447144b71ac89eb7bb83a77c67c99d4054f91ee3703acf5d6a629",
            tasks[1].accessCode
        )
        assertEquals(
            "d3e6092ae3af14b5225e2ddbe5a4f59b3939a907d6fdd5ce6a760ca71f45d8e5",
            tasks[2].accessCode
        )
    }
}
