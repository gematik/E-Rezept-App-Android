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

import de.gematik.ti.erp.app.utils.CoroutineTestRule
import de.gematik.ti.erp.app.utils.TEST_SCANNED_TASK_GROUPS
import de.gematik.ti.erp.app.utils.TEST_SCANNED_TASK_GROUP_1
import de.gematik.ti.erp.app.utils.TEST_SCANNED_TASK_GROUP_2
import de.gematik.ti.erp.app.utils.TEST_SYNCED_TASK_GROUPS
import de.gematik.ti.erp.app.utils.TEST_SYNCED_TASK_GROUP_1
import de.gematik.ti.erp.app.utils.TEST_SYNCED_TASK_GROUP_2
import de.gematik.ti.erp.app.utils.TEST_SYNCED_TASK_GROUP_3
import de.gematik.ti.erp.app.utils.testScannedTasks
import de.gematik.ti.erp.app.utils.testSyncedTasks
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

@ExperimentalCoroutinesApi
class PrescriptionUseCaseTest {
    // necessary for mockk
    abstract class TestPrescriptionUseCase : PrescriptionUseCase

    @get:Rule
    val coroutineRule = CoroutineTestRule()

    @MockK(relaxed = true)
    lateinit var useCase: TestPrescriptionUseCase

    @Before
    fun setup() {
        MockKAnnotations.init(this)

        every { useCase.syncedTasks() } answers { flowOf(testSyncedTasks()) }
        every { useCase.scannedTasks() } answers { flowOf(testScannedTasks()) }

        every { useCase.syncedRecipes() } answers { callOriginal() }
        every { useCase.scannedRecipes() } answers { callOriginal() }
    }

    @Test
    fun `syncedRecipes - should return synchronized tasks in form of recipes sorted by authoredOn and grouped by organization`() =
        coroutineRule.testDispatcher.runBlockingTest {
            useCase.syncedRecipes().toCollection(mutableListOf()).first().let {
                assertEquals(TEST_SYNCED_TASK_GROUPS, it.size)
                assertArrayEquals(
                    TEST_SYNCED_TASK_GROUP_1,
                    it[0].prescriptions.map { it.taskId }.toTypedArray()
                )
                assertArrayEquals(
                    TEST_SYNCED_TASK_GROUP_2,
                    it[1].prescriptions.map { it.taskId }.toTypedArray()
                )
                assertArrayEquals(
                    TEST_SYNCED_TASK_GROUP_3,
                    it[2].prescriptions.map { it.taskId }.toTypedArray()
                )
            }
        }

    @Test
    fun `scannedRecipes - should return scanned tasks in form of recipes sorted by scannedOn and grouped by scanSessionEnd`() =
        coroutineRule.testDispatcher.runBlockingTest {
            useCase.scannedRecipes().toCollection(mutableListOf()).first().let {
                assertEquals(TEST_SCANNED_TASK_GROUPS, it.size)
                assertArrayEquals(
                    TEST_SCANNED_TASK_GROUP_1,
                    it[0].prescriptions.map { it.taskId }.toTypedArray()
                )
                assertArrayEquals(
                    TEST_SCANNED_TASK_GROUP_2,
                    it[1].prescriptions.map { it.taskId }.toTypedArray()
                )
            }
        }
}
