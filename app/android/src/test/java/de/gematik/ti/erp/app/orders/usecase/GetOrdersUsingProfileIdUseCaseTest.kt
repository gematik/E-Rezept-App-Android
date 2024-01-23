/*
 * Copyright (c) 2024 gematik GmbH
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

package de.gematik.ti.erp.app.orders.usecase

import de.gematik.ti.erp.app.orders.mocks.OrderMocks.MOCK_DISP_REQ_COMMUNICATION_01
import de.gematik.ti.erp.app.orders.mocks.OrderMocks.MOCK_ORDER_01
import de.gematik.ti.erp.app.orders.mocks.OrderMocks.MOCK_PHARMACY_O1
import de.gematik.ti.erp.app.orders.mocks.OrderMocks.MOCK_PROFILE_IDENTIFIER
import de.gematik.ti.erp.app.orders.mocks.OrderMocks.MOCK_TASK_ID_01
import de.gematik.ti.erp.app.orders.repository.CommunicationRepository
import de.gematik.ti.erp.app.orders.usecase.model.OrderUseCaseData
import io.mockk.coEvery
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import kotlin.test.assertEquals

@ExperimentalCoroutinesApi
class GetOrdersUsingProfileIdUseCaseTest {

    private val dispatcher = StandardTestDispatcher()

    private val communicationRepository: CommunicationRepository = mockk()

    @InjectMockKs
    private lateinit var useCase: GetOrdersUsingProfileIdUseCase

    @Before
    fun setup() {
        coEvery {
            communicationRepository.loadFirstDispReqCommunications(any())
        } returns flowOf(listOf(MOCK_DISP_REQ_COMMUNICATION_01))

        coEvery {
            communicationRepository.loadPharmacies()
        } returns flowOf(listOf(MOCK_PHARMACY_O1))

        coEvery {
            communicationRepository.taskIdsByOrder(any())
        } returns flowOf(listOf(MOCK_TASK_ID_01))

        coEvery {
            communicationRepository.hasUnreadPrescription(any(), any())
        } returns flowOf(true)

        coEvery {
            communicationRepository.downloadMissingPharmacy(any())
        } returns Unit

        useCase = GetOrdersUsingProfileIdUseCase(
            repository = communicationRepository,
            dispatcher = dispatcher
        )
    }

    @Test
    fun `invoke should return list of orders`() = runTest(dispatcher) {
        val expectedOrders = listOf(MOCK_ORDER_01)

        val resultOrders: Flow<List<OrderUseCaseData.Order>> = useCase(MOCK_PROFILE_IDENTIFIER)

        assertEquals(expectedOrders, resultOrders.first())
    }
}
