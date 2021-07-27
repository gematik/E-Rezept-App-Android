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

package de.gematik.ti.erp.app.prescription.ui

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import de.gematik.ti.erp.app.db.entities.Task
import de.gematik.ti.erp.app.prescription.usecase.PrescriptionUseCase
import de.gematik.ti.erp.app.utils.CoroutineTestRule
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.every
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runBlockingTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import java.time.OffsetDateTime

@ExperimentalCoroutinesApi
class ScanPrescriptionViewModelTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    @get:Rule
    val coroutineRule = CoroutineTestRule()

    private lateinit var viewModel: ScanPrescriptionViewModel

    @MockK
    private lateinit var useCase: PrescriptionUseCase

    @MockK
    private lateinit var validator: TwoDCodeValidator

    @MockK
    private lateinit var scanner: TwoDCodeScanner

    @MockK
    private lateinit var processor: TwoDCodeProcessor

    private val batch = TwoDCodeScanner.Batch(
        matrixCodes = listOf(),
        cameraSize = android.util.Size(0, 0),
        cameraRotation = 0,
        averageScanTime = 250
    )

    private val scannedCode = ScannedCode(
        "{\n" +
            "  \"urls\": [\n" +
            "    \"Task/234fabe0964598efd23f34dd23e122b2323344ea8e8934dae23e2a9a934513bc/\$accept?ac=777bea0e13cc9c42ceec14aec3ddee2263325dc2c6c699db115f58fe423607ea\",\n" +
            "    \"Task/2aef43b8c5e8f2d3d7aef64598b3c40e1d9e348f75d62fd39fe4a7bc5c923de8/\$accept?ac=0936cfa582b447144b71ac89eb7bb83a77c67c99d4054f91ee3703acf5d6a629\",\n" +
            "    \"Task/5e78f21cd6abc35edf4f1726c3d451ea2736d547a263f45726bc13a47e65d189/\$accept?ac=d3e6092ae3af14b5225e2ddbe5a4f59b3939a907d6fdd5ce6a760ca71f45d8e5\"\n" +
            "  ]\n" +
            "}",
        OffsetDateTime.now()
    )

    private val validScannedCode = ValidScannedCode(
        scannedCode,
        mutableListOf(
            "Task/234fabe0964598efd23f34dd23e122b2323344ea8e8934dae23e2a9a934513bc/\$accept?ac=777bea0e13cc9c42ceec14aec3ddee2263325dc2c6c699db115f58fe423607ea",
            "Task/2aef43b8c5e8f2d3d7aef64598b3c40e1d9e348f75d62fd39fe4a7bc5c923de8/\$accept?ac=0936cfa582b447144b71ac89eb7bb83a77c67c99d4054f91ee3703acf5d6a629",
            "Task/5e78f21cd6abc35edf4f1726c3d451ea2736d547a263f45726bc13a47e65d189/\$accept?ac=d3e6092ae3af14b5225e2ddbe5a4f59b3939a907d6fdd5ce6a760ca71f45d8e5"
        )
    )

    private val validScannedCode2 = ValidScannedCode(
        scannedCode,
        mutableListOf(
            "Task/234fabe0964598efd23f34dd23e122b2323344ea8e8934dae23e2a9a934513bc/\$accept?ac=777bea0e13cc9c42ceec14aec3ddee2263325dc2c6c699db115f58fe423607ea",
        )
    )

    @Before
    fun setup() {
        MockKAnnotations.init(this)

        every { scanner.defaultBatch } returns batch
        every { scanner.batch } returns MutableStateFlow(batch)

        viewModel = ScanPrescriptionViewModel(
            useCase,
            scanner,
            processor,
            validator,
            coroutineRule.testDispatchProvider
        )
    }

    @Test
    fun `test addScannedCode with three tasks and one duplicated - should return true`() = coroutineRule.testDispatcher.runBlockingTest {
        coEvery { useCase.getAllTasksWithTaskIdOnly() } returns mutableListOf("234fabe0964598efd23f34dd23e122b2323344ea8e8934dae23e2a9a934513bc")
        val codeHasUniqueUrls = viewModel.addScannedCode(validScannedCode)
        assertTrue("codeHasUniqueUrls", codeHasUniqueUrls)
    }

    @Test
    fun `test addScannedCode with one task duplicated - should return false`() = coroutineRule.testDispatcher.runBlockingTest {
        coEvery { useCase.getAllTasksWithTaskIdOnly() } returns mutableListOf("234fabe0964598efd23f34dd23e122b2323344ea8e8934dae23e2a9a934513bc")
        val codeHasUniqueUrls = viewModel.addScannedCode(validScannedCode2)
        assertFalse("codeHasUniqueUrls", codeHasUniqueUrls)
    }

    @Test
    fun `test saveToDatabase() with three tasks`() = coroutineRule.testDispatcher.runBlockingTest {
        coEvery { useCase.getAllTasksWithTaskIdOnly() } returns mutableListOf()
        viewModel.addScannedCode(validScannedCode)

        val capTasks = mutableListOf<List<Task>>()
        coEvery { useCase.saveScannedTasks(capture(capTasks)) } coAnswers { }

        viewModel.saveToDatabase()

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
