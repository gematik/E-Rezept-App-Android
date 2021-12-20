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
import de.gematik.ti.erp.app.prescription.usecase.PrescriptionUseCase
import de.gematik.ti.erp.app.utils.CoroutineTestRule
import de.gematik.ti.erp.app.utils.validScannedCode
import de.gematik.ti.erp.app.utils.validScannedCode2
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.every
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runBlockingTest
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test

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
    fun `test addScannedCode with three tasks and one duplicated - should return true`() =
        coroutineRule.testDispatcher.runBlockingTest {
            coEvery { useCase.getAllTasksWithTaskIdOnly() } returns mutableListOf("234fabe0964598efd23f34dd23e122b2323344ea8e8934dae23e2a9a934513bc")
            val codeHasUniqueUrls = viewModel.addScannedCode(validScannedCode)
            assertTrue("codeHasUniqueUrls", codeHasUniqueUrls)
        }

    @Test
    fun `test addScannedCode with one task duplicated - should return false`() =
        coroutineRule.testDispatcher.runBlockingTest {
            coEvery { useCase.getAllTasksWithTaskIdOnly() } returns mutableListOf("234fabe0964598efd23f34dd23e122b2323344ea8e8934dae23e2a9a934513bc")
            val codeHasUniqueUrls = viewModel.addScannedCode(validScannedCode2)
            assertFalse("codeHasUniqueUrls", codeHasUniqueUrls)
        }
}
