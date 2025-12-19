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

import android.content.Context
import de.gematik.ti.erp.app.eurezept.repository.EuRepository
import de.gematik.ti.erp.app.invoice.model.InvoiceData
import de.gematik.ti.erp.app.invoice.repository.InvoiceRepository
import de.gematik.ti.erp.app.messages.domain.usecase.GetLatestEuOrderMessageAsInAppMessageUseCase
import de.gematik.ti.erp.app.messages.domain.usecase.GetUnreadMessagesCountUseCase
import de.gematik.ti.erp.app.messages.mappers.EuOrderToMessagesMapper
import de.gematik.ti.erp.app.messages.repository.CommunicationRepository
import de.gematik.ti.erp.app.messages.repository.InternalMessagesRepository
import de.gematik.ti.erp.app.prescription.repository.PrescriptionRepository
import de.gematik.ti.erp.app.profile.repository.ProfileIdentifier
import io.mockk.coEvery
import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.single
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import kotlin.test.assertEquals

@ExperimentalCoroutinesApi
class GetUnreadMessagesCountUseCaseTest {

    private val dispatcher = StandardTestDispatcher()

    private val communicationRepository: CommunicationRepository = mockk()
    private val internalMessagesRepository: InternalMessagesRepository = mockk()
    private val invoiceRepository: InvoiceRepository = mockk()

    val euRepository: EuRepository = mockk()
    val prescriptionRepository: PrescriptionRepository = mockk()
    val mapper: EuOrderToMessagesMapper = mockk()

    private val mockContext = mockk<Context>()

    private lateinit var getLatestEuOrderMessageAsInAppMessageUseCase: GetLatestEuOrderMessageAsInAppMessageUseCase

    private val profileId: ProfileIdentifier = "testProfileId"

    @InjectMockKs
    private lateinit var useCase: GetUnreadMessagesCountUseCase

    @Before
    fun setup() {
        coEvery { communicationRepository.unreadMessagesCount() } returns flowOf(COUNTER_NUMBER)
        coEvery {
            invoiceRepository.getInvoiceTaskIdAndConsumedStatus(any())
        } returns flowOf(listOf(InvoiceData.InvoiceStatus(taskId = "taskId1", consumed = false)))
        coEvery { communicationRepository.loadDispReqCommunicationsByProfileId(any()) } returns flowOf(emptyList())
        coEvery { communicationRepository.loadRepliedCommunications(any(), any()) } returns flowOf(emptyList())
        coEvery { internalMessagesRepository.getUnreadInternalMessagesCount() } returns flowOf(0L)
        every { euRepository.observeEuOrder(any()) } returns flowOf(null)
        every { euRepository.observeAllEuOrders() } returns flowOf(emptyList())
        every { mockContext.resources.configuration.locales[0].language } returns "de"

        getLatestEuOrderMessageAsInAppMessageUseCase = GetLatestEuOrderMessageAsInAppMessageUseCase(
            mockContext,
            euRepository,
            prescriptionRepository,
            mapper
        )

        useCase = GetUnreadMessagesCountUseCase(
            communicationRepository,
            internalMessagesRepository,
            invoiceRepository,
            getLatestEuOrderMessageAsInAppMessageUseCase,
            dispatcher
        )
    }

    @Test
    fun `invoke should return flow of unread orders`() = runTest(dispatcher) {
        val expectedUnreadCount = 15L

        val resultOrders = useCase(profileId).single()

        assertEquals(expectedUnreadCount, resultOrders)
    }

    companion object {
        private const val COUNTER_NUMBER = 15L
    }
}
