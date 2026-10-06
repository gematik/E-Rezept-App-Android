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

import app.cash.turbine.test
import de.gematik.ti.erp.app.communication.model.payload.CommunicationReplyTextPayloadErpModel
import de.gematik.ti.erp.app.invoice.repository.InvoiceRepository
import de.gematik.ti.erp.app.messages.domain.model.MessagesStringProvider
import de.gematik.ti.erp.app.messages.domain.usecase.GetExternalInAppMessagesUseCase
import de.gematik.ti.erp.app.messages.domain.usecase.GetMessagesUseCase
import de.gematik.ti.erp.app.messages.mapper.OrderToInAppMessageMapper
import de.gematik.ti.erp.app.messages.model.InAppMessageStatus
import de.gematik.ti.erp.app.messages.repository.CommunicationRepository
import de.gematik.ti.erp.app.mocks.DATE_2024_01_01
import de.gematik.ti.erp.app.mocks.DATE_3023_12_31
import de.gematik.ti.erp.app.mocks.order.model.COMMUNICATION_DATA
import de.gematik.ti.erp.app.mocks.order.model.TASK_ID
import de.gematik.ti.erp.app.mocks.order.model.communicationDataReply
import de.gematik.ti.erp.app.mocks.profile.api.API_MOCK_PROFILE
import de.gematik.ti.erp.app.pharmacy.repository.PharmacyRepository
import de.gematik.ti.erp.app.profiles.repository.ProfileRepository
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Test
import kotlin.test.assertEquals

class GetExternalInAppMessagesUseCaseTest {
    private val communicationRepository: CommunicationRepository = mockk()
    private val invoiceRepository: InvoiceRepository = mockk()
    private val profileRepository: ProfileRepository = mockk()
    private val pharmacyRepository: PharmacyRepository = mockk()
    private val dispatcher = StandardTestDispatcher()
    private val testScope = TestScope(dispatcher)

    private val dispReqCommunication = COMMUNICATION_DATA.copy(
        communicationId = "disp-1",
        orderId = "order-1",
        taskId = TASK_ID,
        profileId = API_MOCK_PROFILE.id,
        timeStamp = DATE_2024_01_01
    )

    private val dispReqByProfileFlow = MutableStateFlow(listOf(dispReqCommunication))
    private val repliesByProfileFlow = MutableStateFlow(emptyList<de.gematik.ti.erp.app.communication.model.CommunicationErpModel>())
    private val repliesByTaskFlow = MutableStateFlow(emptyList<de.gematik.ti.erp.app.communication.model.CommunicationErpModel>())

    private val useCase = GetExternalInAppMessagesUseCase(
        getMessagesUseCase = GetMessagesUseCase(
            communicationRepository = communicationRepository,
            invoiceRepository = invoiceRepository,
            profileRepository = profileRepository,
            pharmacyRepository = pharmacyRepository,
            dispatcher = dispatcher
        ),
        orderToInAppMessageMapper = OrderToInAppMessageMapper(
            object : MessagesStringProvider {
                override fun getString(resourceId: Int, vararg args: Any): String = "message"
            }
        ),
        dispatcher = dispatcher
    )

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `emits updated order status when reply arrives without screen restart`() = testScope.runTest {
        every { profileRepository.profiles() } returns flowOf(listOf(API_MOCK_PROFILE))
        every { communicationRepository.loadDispReqCommunicationsByProfileId(API_MOCK_PROFILE.id) } returns dispReqByProfileFlow
        every { communicationRepository.loadRepliedCommunicationsByProfileId(API_MOCK_PROFILE.id) } returns repliesByProfileFlow
        every { communicationRepository.taskIdsByOrder("order-1") } returns flowOf(listOf(TASK_ID))
        every { communicationRepository.loadSyncedByTaskId(TASK_ID) } returns flowOf(null)
        every { communicationRepository.loadScannedByTaskId(TASK_ID) } returns flowOf(null)
        every { communicationRepository.hasUnreadDispenseMessage(listOf(TASK_ID), "order-1") } returns flowOf(false)
        coEvery { communicationRepository.hasUnreadRepliedMessages(listOf(TASK_ID), dispReqCommunication.recipient) } returns flowOf(false)
        every { invoiceRepository.hasUnreadInvoiceMessages(listOf(TASK_ID)) } returns flowOf(false)
        every { invoiceRepository.invoiceByTaskId(TASK_ID) } returns flowOf(null)
        every { communicationRepository.loadDispReqCommunications("order-1") } returns flowOf(listOf(dispReqCommunication))
        every { communicationRepository.loadAllRepliedCommunications(listOf(TASK_ID)) } returns repliesByTaskFlow
        every { communicationRepository.loadDispReqCommunicationsByTaskId(TASK_ID) } returns flowOf(listOf(dispReqCommunication))
        coEvery { pharmacyRepository.findLocalPharmacyByTelematikId(any()) } returns null
        coEvery { pharmacyRepository.searchPharmacyByTelematikId(any()) } returns Result.failure(Exception("not found"))

        useCase.invoke().test {
            assertEquals(InAppMessageStatus.ORDERED, awaitItem().single().orderStatus)

            repliesByTaskFlow.value = listOf(
                communicationDataReply(
                    taskId = TASK_ID,
                    telematikId = dispReqCommunication.recipient,
                    communicationId = "reply-1",
                    date = DATE_3023_12_31
                ).copy(
                    payload = CommunicationReplyTextPayloadErpModel(
                        text = "Ihre Bestellung wird bearbeitet"
                    )
                )
            )

            runCurrent()

            var updatedStatus = awaitItem().single().orderStatus
            if (updatedStatus != InAppMessageStatus.PENDING) {
                updatedStatus = awaitItem().single().orderStatus
            }

            assertEquals(InAppMessageStatus.PENDING, updatedStatus)
            cancelAndIgnoreRemainingEvents()
        }
    }
}
