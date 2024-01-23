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
import de.gematik.ti.erp.app.orders.mocks.OrderMocks.MOCK_DISP_REQ_COMMUNICATION_01
import de.gematik.ti.erp.app.orders.mocks.OrderMocks.MOCK_DISP_REQ_COMMUNICATION_02
import de.gematik.ti.erp.app.orders.mocks.OrderMocks.MOCK_INVOICE_01
import de.gematik.ti.erp.app.orders.mocks.OrderMocks.MOCK_INVOICE_02
import de.gematik.ti.erp.app.orders.mocks.OrderMocks.MOCK_ORDER_DETAIL
import de.gematik.ti.erp.app.orders.mocks.OrderMocks.MOCK_ORDER_ID
import de.gematik.ti.erp.app.orders.mocks.OrderMocks.MOCK_PHARMACY_O1
import de.gematik.ti.erp.app.orders.mocks.OrderMocks.MOCK_PHARMACY_O2
import de.gematik.ti.erp.app.orders.mocks.OrderMocks.MOCK_SYNCED_TASK_DATA_01
import de.gematik.ti.erp.app.orders.mocks.OrderMocks.MOCK_SYNCED_TASK_DATA_02
import de.gematik.ti.erp.app.orders.mocks.OrderMocks.MOCK_TASK_ID_01
import de.gematik.ti.erp.app.orders.mocks.OrderMocks.MOCK_TASK_ID_02
import de.gematik.ti.erp.app.orders.repository.CommunicationRepository
import io.mockk.coEvery
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import kotlin.test.assertEquals

@ExperimentalCoroutinesApi
class GetOrdersUsingOrderIdUseCaseTest {

    private val dispatcher = StandardTestDispatcher()

    private val communicationRepository: CommunicationRepository = mockk()
    private val invoiceRepository: InvoiceRepository = mockk()

    @InjectMockKs
    private lateinit var useCase: GetOrderUsingOrderIdUseCase

    @Before
    fun setup() {
        coEvery {
            communicationRepository.loadDispReqCommunications(any())
        } returns flowOf(listOf(MOCK_DISP_REQ_COMMUNICATION_01, MOCK_DISP_REQ_COMMUNICATION_02))

        coEvery {
            communicationRepository.loadPharmacies()
        } returns flowOf(listOf(MOCK_PHARMACY_O1, MOCK_PHARMACY_O2))

        coEvery {
            communicationRepository.taskIdsByOrder(any())
        } returns flowOf(listOf(MOCK_TASK_ID_01, MOCK_TASK_ID_02))

        coEvery {
            communicationRepository.hasUnreadPrescription(any(), any())
        } returns flowOf(true)

        coEvery {
            communicationRepository.loadSyncedByTaskId(MOCK_TASK_ID_01)
        } returns flowOf(MOCK_SYNCED_TASK_DATA_01)

        coEvery {
            communicationRepository.loadSyncedByTaskId(MOCK_TASK_ID_02)
        } returns flowOf(MOCK_SYNCED_TASK_DATA_02)

        coEvery {
            communicationRepository.downloadMissingPharmacy(any())
        } returns Unit

        coEvery { invoiceRepository.invoiceById(MOCK_TASK_ID_01) } returns flowOf(MOCK_INVOICE_01)

        coEvery { invoiceRepository.invoiceById(MOCK_TASK_ID_02) } returns flowOf(MOCK_INVOICE_02)

        useCase = GetOrderUsingOrderIdUseCase(
            communicationRepository = communicationRepository,
            invoiceRepository = invoiceRepository,
            dispatcher = dispatcher
        )
    }

    @Test
    fun `invoke should return order detail`() = runTest(dispatcher) {
        val expectedOrderDetail = MOCK_ORDER_DETAIL

        val resultOrderDetail = useCase(MOCK_ORDER_ID)

        assertEquals(expectedOrderDetail, resultOrderDetail.first())
    }
}
