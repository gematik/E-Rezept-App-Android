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

import de.gematik.ti.erp.app.invoice.repository.InvoiceRepository
import de.gematik.ti.erp.app.orders.mocks.OrderMocks.MOCK_DISP_REPLY_COMMUNICATION_01
import de.gematik.ti.erp.app.orders.mocks.OrderMocks.MOCK_DISP_REPLY_COMMUNICATION_02
import de.gematik.ti.erp.app.orders.mocks.OrderMocks.MOCK_INVOICE_01
import de.gematik.ti.erp.app.orders.mocks.OrderMocks.MOCK_INVOICE_02
import de.gematik.ti.erp.app.orders.mocks.OrderMocks.MOCK_MESSAGE_01
import de.gematik.ti.erp.app.orders.mocks.OrderMocks.MOCK_MESSAGE_02
import de.gematik.ti.erp.app.orders.mocks.OrderMocks.MOCK_ORDER_ID
import de.gematik.ti.erp.app.orders.mocks.OrderMocks.MOCK_TASK_ID_01
import de.gematik.ti.erp.app.orders.mocks.OrderMocks.MOCK_TASK_ID_02
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
class GetRepliedMessagesUseCaseTest {

    private val dispatcher = StandardTestDispatcher()

    private val communicationRepository: CommunicationRepository = mockk()
    private val invoiceRepository: InvoiceRepository = mockk()

    @InjectMockKs
    private lateinit var useCase: GetRepliedMessagesUseCase

    @Before
    fun setup() {
        coEvery {
            communicationRepository.taskIdsByOrder(any())
        } returns flowOf(listOf(MOCK_TASK_ID_01, MOCK_TASK_ID_02))

        coEvery {
            communicationRepository.loadRepliedCommunications(any())
        } returns flowOf(listOf(MOCK_DISP_REPLY_COMMUNICATION_01, MOCK_DISP_REPLY_COMMUNICATION_02))

        coEvery {
            invoiceRepository.invoiceById(MOCK_TASK_ID_01)
        } returns flowOf(MOCK_INVOICE_01)

        coEvery {
            invoiceRepository.invoiceById(MOCK_TASK_ID_02)
        } returns flowOf(MOCK_INVOICE_02)

        useCase = GetRepliedMessagesUseCase(
            communicationRepository = communicationRepository,
            invoiceRepository = invoiceRepository,
            dispatcher = dispatcher
        )
    }

    @Test
    fun `invoke should return list of replied messages`() = runTest(dispatcher) {
        val expectedRepliedMessages = listOf(MOCK_MESSAGE_01, MOCK_MESSAGE_02)

        val resultRepliedMessages: Flow<List<OrderUseCaseData.Message>> =
            useCase(MOCK_ORDER_ID)

        assertEquals(expectedRepliedMessages, resultRepliedMessages.first())
    }
}
