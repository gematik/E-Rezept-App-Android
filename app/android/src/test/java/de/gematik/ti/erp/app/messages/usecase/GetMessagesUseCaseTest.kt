/*
 * Copyright 2025, gematik GmbH
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
 * In case of changes by gematik find details in the "Readme" file.
 *
 * See the Licence for the specific language governing permissions and limitations under the Licence.
 */

package de.gematik.ti.erp.app.messages.usecase

import de.gematik.ti.erp.app.invoice.repository.InvoiceRepository
import de.gematik.ti.erp.app.messages.domain.model.OrderUseCaseData
import de.gematik.ti.erp.app.messages.domain.usecase.GetMessagesUseCase
import de.gematik.ti.erp.app.messages.mocks.MessageMocks.MOCK_DISP_REQ_COMMUNICATION_01
import de.gematik.ti.erp.app.messages.mocks.MessageMocks.MOCK_ORDER_01
import de.gematik.ti.erp.app.messages.mocks.MessageMocks.MOCK_PHARMACY_O1
import de.gematik.ti.erp.app.messages.mocks.MessageMocks.MOCK_PROFILE
import de.gematik.ti.erp.app.messages.mocks.MessageMocks.MOCK_TASK_ID_01
import de.gematik.ti.erp.app.messages.repository.CommunicationRepository
import de.gematik.ti.erp.app.profiles.repository.ProfileRepository
import io.mockk.coEvery
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import kotlin.test.assertEquals

@ExperimentalCoroutinesApi
class GetMessagesUseCaseTest {

    private val dispatcher = StandardTestDispatcher()

    private val communicationRepository: CommunicationRepository = mockk()

    private val profileRepository: ProfileRepository = mockk()

    private val invoiceRepository: InvoiceRepository = mockk()

    @InjectMockKs
    private lateinit var useCase: GetMessagesUseCase

    @Before
    fun setup() {
        coEvery { profileRepository.profiles() } returns flowOf(listOf(MOCK_PROFILE))

        coEvery {
            communicationRepository.loadDispReqCommunicationsByProfileId(any())
        } returns flowOf(listOf(MOCK_DISP_REQ_COMMUNICATION_01))

        coEvery {
            communicationRepository.loadPharmacies()
        } returns flowOf(listOf(MOCK_PHARMACY_O1))

        coEvery {
            communicationRepository.taskIdsByOrder(any())
        } returns flowOf(listOf(MOCK_TASK_ID_01))

        coEvery {
            communicationRepository.hasUnreadDispenseMessage(any(), any())
        } returns flowOf(true)

        coEvery {
            communicationRepository.hasUnreadRepliedMessages(any(), any())
        } returns flowOf(true)

        coEvery {
            communicationRepository.downloadMissingPharmacy(any())
        } returns Result.success(null)

        coEvery {
            communicationRepository.loadSyncedByTaskId(any())
        } returns flowOf(null)

        coEvery {
            communicationRepository.loadScannedByTaskId(any())
        } returns flowOf(null)

        coEvery {
            communicationRepository.loadRepliedCommunications(any(), any())
        } returns flowOf(emptyList())
        coEvery {
            communicationRepository.loadDispReqCommunications(any())
        } returns flowOf(emptyList())

        coEvery {
            invoiceRepository.getInvoiceTaskIdAndConsumedStatus(any())
        } returns flowOf(emptyList())

        coEvery { invoiceRepository.hasUnreadInvoiceMessages(any()) } returns flowOf(false)

        coEvery { invoiceRepository.invoiceByTaskId(any()) } returns flowOf(null)

        useCase = GetMessagesUseCase(
            communicationRepository = communicationRepository,
            profileRepository = profileRepository,
            invoiceRepository = invoiceRepository,
            dispatcher = dispatcher
        )
    }

    @Test
    fun `invoke should return list of orders`() = runTest(dispatcher) {
        val expectedOrders = listOf(MOCK_ORDER_01)

        val resultOrders: List<OrderUseCaseData.Order> = useCase()

        assertEquals(expectedOrders, resultOrders)
    }
}
