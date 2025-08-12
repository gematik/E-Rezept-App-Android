/*
 * Copyright (Change Date see Readme), gematik GmbH
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
 * In case of changes by gematik GmbH find details in the "Readme" file.
 *
 * See the Licence for the specific language governing permissions and limitations under the Licence.
 *
 * *******
 *
 * For additional notes and disclaimer from gematik and in case of changes by gematik find details in the "Readme" file.
 */

package de.gematik.ti.erp.app.digas.presentation

import app.cash.turbine.test
import de.gematik.ti.erp.app.digas.domain.usecase.FetchInsuranceListUseCase
import de.gematik.ti.erp.app.digas.ui.preview.mockInsuranceUiModelList
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Test
import kotlin.test.assertEquals

@OptIn(ExperimentalCoroutinesApi::class)
class InsuranceSearchListControllerTest {

    private val fetchInsuranceListUseCase: FetchInsuranceListUseCase = mockk()
    private val testDispatcher = StandardTestDispatcher()
    private val testScope = TestScope(testDispatcher)

    private lateinit var insuranceController: InsuranceSearchListController

    @Before
    fun setUp() {
        MockKAnnotations.init(this)
        Dispatchers.setMain(testDispatcher)

        coEvery { fetchInsuranceListUseCase(any()) } returns mockInsuranceUiModelList

        insuranceController = InsuranceSearchListController(
            fetchInsuranceListUseCase = fetchInsuranceListUseCase
        )
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun shouldReturnFullListWhenSearchTermIsBlank() = testScope.runTest {
        // When
        insuranceController.onSearchFieldValueChange("")
        advanceUntilIdle()

        // Then
        insuranceController.healthInsuranceList.test {
            val result = awaitItem()
            result.data?.let {
                assertEquals<Any>(mockInsuranceUiModelList, it)
            }
        }
    }
}
