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

package de.gematik.ti.erp.app.messages.usecase

import de.gematik.ti.erp.app.invoice.repository.InvoiceRepository
import de.gematik.ti.erp.app.messages.domain.usecase.GetMessageUsingOrderIdUseCase
import de.gematik.ti.erp.app.messages.repository.CommunicationRepository
import de.gematik.ti.erp.app.mocks.messages.model.MessageMocks.MOCK_DISP_REPLY_COMMUNICATION_01_ERP
import de.gematik.ti.erp.app.mocks.messages.model.MessageMocks.MOCK_DISP_REPLY_COMMUNICATION_02_ERP
import de.gematik.ti.erp.app.mocks.messages.model.MessageMocks.MOCK_DISP_REQ_COMMUNICATION_01_ERP
import de.gematik.ti.erp.app.mocks.messages.model.MessageMocks.MOCK_DISP_REQ_COMMUNICATION_02_ERP
import de.gematik.ti.erp.app.mocks.messages.model.MessageMocks.MOCK_INVOICE_01
import de.gematik.ti.erp.app.mocks.messages.model.MessageMocks.MOCK_INVOICE_02
import de.gematik.ti.erp.app.mocks.messages.model.MessageMocks.MOCK_ORDER_DETAIL
import de.gematik.ti.erp.app.mocks.messages.model.MessageMocks.MOCK_ORDER_ID
import de.gematik.ti.erp.app.mocks.messages.model.MessageMocks.MOCK_PHARMACY_O1
import de.gematik.ti.erp.app.mocks.messages.model.MessageMocks.MOCK_SYNCED_TASK_DATA_01
import de.gematik.ti.erp.app.mocks.messages.model.MessageMocks.MOCK_SYNCED_TASK_DATA_02
import de.gematik.ti.erp.app.mocks.messages.model.MessageMocks.MOCK_TASK_ID_01
import de.gematik.ti.erp.app.mocks.messages.model.MessageMocks.MOCK_TASK_ID_02
import de.gematik.ti.erp.app.pharmacy.repository.PharmacyRepository
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
import kotlin.test.assertNull

@ExperimentalCoroutinesApi
class GetMessageUsingOrderIdUseCaseTest {

    private val dispatcher = StandardTestDispatcher()

    private val communicationRepository: CommunicationRepository = mockk()
    private val invoiceRepository: InvoiceRepository = mockk()
    private val pharmacyRepository: PharmacyRepository = mockk()

    @InjectMockKs
    private lateinit var useCase: GetMessageUsingOrderIdUseCase

    @Before
    fun setup() {
        coEvery {
            communicationRepository.loadDispReqCommunications(any())
        } returns flowOf(listOf(MOCK_DISP_REQ_COMMUNICATION_01_ERP, MOCK_DISP_REQ_COMMUNICATION_02_ERP))
        coEvery { communicationRepository.taskIdsByOrder(any()) } returns flowOf(listOf(MOCK_TASK_ID_01, MOCK_TASK_ID_02))
        coEvery { communicationRepository.hasUnreadDispenseMessage(any(), any()) } returns flowOf(false)
        coEvery { communicationRepository.hasUnreadRepliedMessages(any(), any()) } returns flowOf(false)
        coEvery { communicationRepository.loadSyncedByTaskId(MOCK_TASK_ID_01) } returns flowOf(MOCK_SYNCED_TASK_DATA_01)
        coEvery { communicationRepository.loadSyncedByTaskId(MOCK_TASK_ID_02) } returns flowOf(MOCK_SYNCED_TASK_DATA_02)
        coEvery { invoiceRepository.invoiceByTaskId(MOCK_TASK_ID_01) } returns flowOf(MOCK_INVOICE_01)
        coEvery { invoiceRepository.invoiceByTaskId(MOCK_TASK_ID_02) } returns flowOf(MOCK_INVOICE_02)
        coEvery { pharmacyRepository.searchPharmacyByTelematikId(any()) } returns Result.failure(Exception())
        useCase = GetMessageUsingOrderIdUseCase(
            communicationRepository = communicationRepository,
            invoiceRepository = invoiceRepository,
            pharmacyRepository = pharmacyRepository,
            dispatcher = dispatcher
        )
    }

    @Test
    fun `invoke should return order detail`() = runTest(dispatcher) {
        val expectedOrderDetail = MOCK_ORDER_DETAIL

        val resultOrderDetail = useCase(MOCK_ORDER_ID).first()

        assertEquals(expectedOrderDetail, resultOrderDetail)
    }

    // Regression test for the "orphaned reply" scenario: the dispense-request was created and
    // redeemed on a different device and is not known locally (e.g. already removed server-side by
    // the time this device synced), but replies for that order did sync down. The order must still
    // be openable, built entirely from the reply-side data.
    @Test
    fun `invoke falls back to reply communications when no dispense-request communication is known locally`() =
        runTest(dispatcher) {
            coEvery { communicationRepository.loadDispReqCommunications(MOCK_ORDER_ID) } returns flowOf(emptyList())
            coEvery {
                communicationRepository.loadRepliedCommunications(MOCK_ORDER_ID)
            } returns flowOf(listOf(MOCK_DISP_REPLY_COMMUNICATION_01_ERP, MOCK_DISP_REPLY_COMMUNICATION_02_ERP))

            val result = useCase(MOCK_ORDER_ID).first()

            assertEquals(MOCK_ORDER_ID, result?.orderId)
            // Replies expose the pharmacy via `senderTelematikId`, not `recipient` - this must be used
            // when resolving the pharmacy identity for a reply-only order.
            assertEquals(MOCK_DISP_REPLY_COMMUNICATION_01_ERP.senderTelematikId, result?.pharmacy?.id)
            assertEquals(MOCK_PHARMACY_O1.name, result?.pharmacy?.name)
            assertEquals(
                listOf(MOCK_SYNCED_TASK_DATA_01, MOCK_SYNCED_TASK_DATA_02),
                result?.taskDetailedBundles?.map { it.prescription }
            )
        }

    @Test
    fun `invoke returns null when neither dispense-request nor reply communications are known locally`() =
        runTest(dispatcher) {
            coEvery { communicationRepository.loadDispReqCommunications(MOCK_ORDER_ID) } returns flowOf(emptyList())
            coEvery { communicationRepository.loadRepliedCommunications(MOCK_ORDER_ID) } returns flowOf(emptyList())

            val result = useCase(MOCK_ORDER_ID).first()

            assertNull(result)
        }
}
