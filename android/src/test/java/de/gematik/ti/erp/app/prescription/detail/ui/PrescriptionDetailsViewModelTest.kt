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

package de.gematik.ti.erp.app.prescription.detail.ui

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import de.gematik.ti.erp.app.prescription.usecase.PrescriptionUseCase
import de.gematik.ti.erp.app.utils.CoroutineTestRule
import de.gematik.ti.erp.app.utils.detailPrescriptionScanned
import io.mockk.coEvery
import io.mockk.mockk
import junit.framework.Assert.assertEquals
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@ExperimentalCoroutinesApi
class PrescriptionDetailsViewModelTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    @get:Rule
    val coroutineRule = CoroutineTestRule()

    private lateinit var viewModel: PrescriptionDetailsViewModel
    private lateinit var useCase: PrescriptionUseCase

    @Before
    fun setup() {
        useCase = mockk()
        viewModel =
            PrescriptionDetailsViewModel(useCase, coroutineRule.testDispatchProvider)
    }

    @Test
    fun `test loading task`() = runTest {
        val expected = detailPrescriptionScanned()
        coEvery { useCase.generatePrescriptionDetails(any()) } returns expected
        coEvery { useCase.unRedeemMorePossible(any(), any()) } returns true

        assertEquals(expected, viewModel.detailedPrescription(expected.taskId))
    }
}
