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

package de.gematik.ti.erp.app.pharmacy.ui

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import de.gematik.ti.erp.app.common.usecase.HintUseCase
import de.gematik.ti.erp.app.pharmacy.usecase.PharmacySearchUseCase
import de.gematik.ti.erp.app.utils.CoroutineTestRule
import de.gematik.ti.erp.app.utils.listOfUIPrescriptions
import de.gematik.ti.erp.app.utils.testUIPrescription
import io.mockk.every
import io.mockk.mockk
import junit.framework.Assert.assertFalse
import junit.framework.Assert.assertTrue
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runBlockingTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@ExperimentalCoroutinesApi
class PharmacySearchViewModelTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    @get:Rule
    val coroutineRule = CoroutineTestRule()

    private lateinit var viewModel: PharmacySearchViewModel
    private lateinit var useCase: PharmacySearchUseCase
    private lateinit var hintUseCase: HintUseCase

    @Before
    fun setUp() {
        useCase = mockk()
        hintUseCase = mockk()
        viewModel = PharmacySearchViewModel(mockk(), useCase, hintUseCase, coroutineRule.testDispatchProvider)
    }

    @Test
    fun `tests fetching of orders from db - list is not empty`() =
        coroutineRule.testDispatcher.runBlockingTest {
            every { useCase.prescriptionDetailsForOrdering(any()) } answers {
                flowOf(
                    listOfUIPrescriptions()
                )
            }
            viewModel.fetchSelectedOrders(listOf("")).collect {
                assertTrue(it.isNotEmpty())
            }
        }

    @Test
    fun `tests fetching of orders from db - element is not selected`() =
        coroutineRule.testDispatcher.runBlockingTest {
            val uiPrescriptionOrder = testUIPrescription()
            every { useCase.prescriptionDetailsForOrdering(any()) } answers {
                flowOf(
                    listOf(
                        uiPrescriptionOrder
                    )
                )
            }
            viewModel.fetchSelectedOrders(listOf("")).collect {
                assertFalse(it.first().selected)
            }
        }

    @Test
    fun `tests fetching of orders from db - element is selected`() =
        coroutineRule.testDispatcher.runBlockingTest {
            val uiPrescriptionOrder = testUIPrescription()
            uiPrescriptionOrder.selected = false
            viewModel.toggleOrder(uiPrescriptionOrder)
            every { useCase.prescriptionDetailsForOrdering(any()) } answers {
                flowOf(
                    listOf(
                        uiPrescriptionOrder
                    )
                )
            }
            viewModel.fetchSelectedOrders(listOf("")).collect {
                assertTrue(it.first().selected)
            }
        }

    @Test
    fun `tests toggling order - adds order to list of orders`() {
        val uiPrescriptionOrder = testUIPrescription()
        uiPrescriptionOrder.selected = false
        val result = viewModel.toggleOrder(uiPrescriptionOrder)
        assertTrue(result)
    }

    @Test
    fun `tests toggling order - removes order from list of orders`() {
        val uiPrescriptionOrder = testUIPrescription()
        uiPrescriptionOrder.selected = true
        val result = viewModel.toggleOrder(uiPrescriptionOrder)
        assertFalse(result)
    }

    @Test
    fun `tests fabState enabled - should be false`() {
        val uiPrescriptionOrder = testUIPrescription()
        viewModel.toggleOrder(uiPrescriptionOrder)
        val result = viewModel.uiState.fabState
        assertFalse(result)
    }

    @Test
    fun `tests fabState enabled - should be true`() {
        val uiPrescriptionOrder = testUIPrescription()
        uiPrescriptionOrder.selected = false
        viewModel.toggleOrder(uiPrescriptionOrder)
        val result = viewModel.uiState.fabState
        assertTrue(result)
    }
}
