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
import de.gematik.ti.erp.app.messages.domain.usecase.GetMessagesUseCase
import de.gematik.ti.erp.app.messages.repository.CommunicationRepository
import de.gematik.ti.erp.app.mocks.order.model.COMMUNICATION_DATA
import de.gematik.ti.erp.app.mocks.order.model.communicationDataReply
import de.gematik.ti.erp.app.mocks.prescription.api.API_ACTIVE_SCANNED_TASK
import de.gematik.ti.erp.app.mocks.prescription.api.API_ACTIVE_SYNCED_TASK_STRUCTURED_DOSAGE
import de.gematik.ti.erp.app.mocks.profile.api.API_MOCK_PROFILE
import de.gematik.ti.erp.app.pharmacy.repository.PharmacyRepository
import de.gematik.ti.erp.app.profiles.repository.ProfileRepository
import io.mockk.clearMocks
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Test
import kotlin.test.assertEquals

class GetMessagesUseCaseTest {
    private val communicationRepository: CommunicationRepository = mockk()
    private val invoiceRepository: InvoiceRepository = mockk()
    private val profileRepository: ProfileRepository = mockk()
    private val pharmacyRepository: PharmacyRepository = mockk()
    private val dispatcher = StandardTestDispatcher()
    private val testScope = TestScope(dispatcher)

    private lateinit var usecaseUnderTest: GetMessagesUseCase

    @Before
    fun setup() {
        coEvery {
            profileRepository.profiles()
        } returns flowOf(listOf(API_MOCK_PROFILE))

        // has two orders with one task each
        coEvery { communicationRepository.hasUnreadDispenseMessage(listOf("task-id-1"), "order-id-1") } returns flowOf(true)
        coEvery { communicationRepository.hasUnreadDispenseMessage(listOf("task-id-2"), "order-id-2") } returns flowOf(false)

        coEvery { communicationRepository.hasUnreadRepliedMessages(any(), any()) } returns flowOf(false)

        // the first task returns a synced task, the second a scanned task
        coEvery { communicationRepository.loadSyncedByTaskId("task-id-1") } returns flowOf(API_ACTIVE_SYNCED_TASK_STRUCTURED_DOSAGE.copy(taskId = "task-id-1"))
        coEvery { communicationRepository.loadSyncedByTaskId("task-id-2") } returns flowOf(null)

        coEvery { communicationRepository.loadScannedByTaskId("task-id-1") } returns flowOf(null)
        coEvery { communicationRepository.loadScannedByTaskId("task-id-2") } returns flowOf(API_ACTIVE_SCANNED_TASK.copy(taskId = "task-id-2"))

        // the first order has one task, the second order has one task
        coEvery { communicationRepository.taskIdsByOrder("order-id-1") } returns flowOf(listOf("task-id-1"))
        coEvery { communicationRepository.taskIdsByOrder("order-id-2") } returns flowOf(listOf("task-id-2"))

        coEvery { invoiceRepository.invoiceByTaskId(any()) } returns flowOf(null)

        // the first order has one communication, the second order has one communication
        coEvery { communicationRepository.loadDispReqCommunicationsByProfileId(any()) } returns flowOf(
            listOf(
                COMMUNICATION_DATA.copy(
                    communicationId = "communication-id-1",
                    taskId = "task-id-1",
                    orderId = "order-id-1"
                ),
                COMMUNICATION_DATA.copy(
                    communicationId = "communication-id-2",
                    taskId = "task-id-2",
                    orderId = "order-id-2"
                )
            )
        )
        // no replied messages
        coEvery { communicationRepository.loadAllRepliedCommunications(any<List<String>>()) } returns flowOf(emptyList())
        coEvery { communicationRepository.loadRepliedCommunicationsByProfileId(any()) } returns flowOf(emptyList())

        // every order returns a communication specific to the order and task
        coEvery { communicationRepository.loadDispReqCommunications("order-id-1") } returns flowOf(
            listOf(
                COMMUNICATION_DATA.copy(
                    communicationId = "communication-id-1",
                    taskId = "task-id-1",
                    orderId = "order-id-1"
                )
            )
        )
        coEvery { communicationRepository.loadDispReqCommunications("order-id-2") } returns flowOf(
            listOf(
                COMMUNICATION_DATA.copy(
                    communicationId = "communication-id-2",
                    taskId = "task-id-2",
                    orderId = "order-id-2"
                )
            )
        )
        coEvery { invoiceRepository.hasUnreadInvoiceMessages(any()) } returns flowOf(false)

        usecaseUnderTest = GetMessagesUseCase(
            communicationRepository,
            invoiceRepository,
            profileRepository,
            pharmacyRepository,
            dispatcher
        )
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @After
    fun tearDown() {
        Dispatchers.resetMain()
        clearMocks(communicationRepository, invoiceRepository, profileRepository)
    }

    @Test
    fun `only request messages are available`() {
        testScope.runTest {
            val result = usecaseUnderTest.observe().first()
            assert(result.isNotEmpty())
            val expected = listOf(
                COMMUNICATION_DATA.copy(
                    communicationId = "communication-id-1",
                    taskId = "task-id-1",
                    orderId = "order-id-1"
                ),
                COMMUNICATION_DATA.copy(
                    communicationId = "communication-id-2",
                    taskId = "task-id-2",
                    orderId = "order-id-2"
                )
            ).sortedByDescending { it.timeStamp }.distinctBy { it.orderId }
            assertEquals(expected, result)
        }
    }

    @Test
    fun `request and reply messages are available`() {
        coEvery { communicationRepository.hasUnreadRepliedMessages(listOf("task-id-1"), "recipient") } returns flowOf(true)
        coEvery { communicationRepository.hasUnreadRepliedMessages(listOf("task-id-2"), "recipient") } returns flowOf(true)

        // replied messages are present for both tasks in different orders
        coEvery { communicationRepository.loadAllRepliedCommunications(listOf("task-id-1")) } returns flowOf(
            listOf(
                communicationDataReply(
                    taskId = "task-id-1",
                    telematikId = "telematik-id-1",
                    taskIds = listOf("task-id-1"),
                    communicationId = "communication-id-1-reply"
                )
            )
        )
        coEvery { communicationRepository.loadAllRepliedCommunications(listOf("task-id-2")) } returns flowOf(
            listOf(
                communicationDataReply(
                    taskId = "task-id-2",
                    telematikId = "telematik-id-1",
                    taskIds = listOf("task-id-2"),
                    communicationId = "communication-id-2-reply"
                )
            )
        )
        testScope.runTest {
            val result = usecaseUnderTest.observe().first()
            assert(result.isNotEmpty())
            val expected = listOf(
                COMMUNICATION_DATA.copy(
                    communicationId = "communication-id-1",
                    taskId = "task-id-1",
                    orderId = "order-id-1"
                ),
                COMMUNICATION_DATA.copy(
                    communicationId = "communication-id-2",
                    taskId = "task-id-2",
                    orderId = "order-id-2"
                )
            ).sortedByDescending { it.timeStamp }.distinctBy { it.orderId }
            assertEquals(expected, result)
        }
    }
}
