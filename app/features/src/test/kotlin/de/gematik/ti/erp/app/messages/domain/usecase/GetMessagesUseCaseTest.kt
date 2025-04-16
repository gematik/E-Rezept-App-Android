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

package de.gematik.ti.erp.app.messages.domain.usecase

import de.gematik.ti.erp.app.invoice.repository.InvoiceRepository
import de.gematik.ti.erp.app.messages.domain.model.OrderUseCaseData.Order
import de.gematik.ti.erp.app.messages.domain.model.OrderUseCaseData.Pharmacy
import de.gematik.ti.erp.app.messages.repository.CommunicationRepository
import de.gematik.ti.erp.app.mocks.order.model.CACHED_PHARMACY
import de.gematik.ti.erp.app.mocks.order.model.COMMUNICATION_DATA
import de.gematik.ti.erp.app.mocks.order.model.communicationDataReply
import de.gematik.ti.erp.app.mocks.prescription.api.API_ACTIVE_SCANNED_TASK
import de.gematik.ti.erp.app.mocks.prescription.api.API_ACTIVE_SYNCED_TASK_STRUCTURED_DOSAGE
import de.gematik.ti.erp.app.mocks.profile.api.API_MOCK_PROFILE
import de.gematik.ti.erp.app.messages.model.CommunicationProfile
import de.gematik.ti.erp.app.messages.model.LastMessage
import de.gematik.ti.erp.app.messages.model.LastMessageDetails
import de.gematik.ti.erp.app.prescription.model.SyncedTaskData.SyncedTask.Ready
import de.gematik.ti.erp.app.prescription.model.SyncedTaskData.TaskStateSerializationType
import de.gematik.ti.erp.app.prescription.usecase.model.Prescription.PrescriptionChipInformation
import de.gematik.ti.erp.app.prescription.usecase.model.Prescription.ScannedPrescription
import de.gematik.ti.erp.app.prescription.usecase.model.Prescription.SyncedPrescription
import de.gematik.ti.erp.app.profiles.repository.ProfileRepository
import io.mockk.clearMocks
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.Instant
import org.junit.After
import org.junit.Before
import org.junit.Test
import kotlin.test.assertEquals

class GetMessagesUseCaseTest {
    private val communicationRepository: CommunicationRepository = mockk()
    private val invoiceRepository: InvoiceRepository = mockk()
    private val profileRepository: ProfileRepository = mockk()
    private val dispatcher = StandardTestDispatcher()
    private val testScope = TestScope(dispatcher)

    private lateinit var usecaseUnderTest: GetMessagesUseCase

    @Before
    fun setup() {
        coEvery {
            profileRepository.profiles()
        } returns flowOf(listOf(API_MOCK_PROFILE))
        // loads one pharmacy which was used for redeeming a prescription
        coEvery { communicationRepository.loadPharmacies() } returns flowOf(listOf(CACHED_PHARMACY.copy(telematikId = "telematik-id-1")))

        // has two orders with one task each
        coEvery { communicationRepository.hasUnreadDispenseMessage(listOf("task-id-1"), "order-id-1") } returns flowOf(true)
        coEvery { communicationRepository.hasUnreadDispenseMessage(listOf("task-id-2"), "order-id-2") } returns flowOf(false)

        coEvery { communicationRepository.hasUnreadRepliedMessages(any(), any()) } returns flowOf(false)
        coEvery { communicationRepository.downloadMissingPharmacy("telematik-id-1") } returns Result.success(null)

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
                    orderId = "order-id-1",
                    taskIds = listOf("task-id-1")
                ),
                COMMUNICATION_DATA.copy(
                    communicationId = "communication-id-2",
                    taskId = "task-id-2",
                    orderId = "order-id-2",
                    taskIds = listOf("task-id-2")
                )
            )
        )
        // no replied messages
        coEvery { communicationRepository.loadRepliedCommunications(any(), any()) } returns flowOf(emptyList())

        // every order returns a communication specific to the order and task
        coEvery { communicationRepository.loadDispReqCommunications("order-id-1") } returns flowOf(
            listOf(
                COMMUNICATION_DATA.copy(
                    communicationId = "communication-id-1",
                    taskId = "task-id-1",
                    orderId = "order-id-1",
                    taskIds = listOf("task-id-1")
                )
            )
        )
        coEvery { communicationRepository.loadDispReqCommunications("order-id-2") } returns flowOf(
            listOf(
                COMMUNICATION_DATA.copy(
                    communicationId = "communication-id-2",
                    taskId = "task-id-2",
                    orderId = "order-id-2",
                    taskIds = listOf("task-id-2")
                )
            )
        )
        coEvery { invoiceRepository.hasUnreadInvoiceMessages(any()) } returns flowOf(false)
        usecaseUnderTest = GetMessagesUseCase(communicationRepository, invoiceRepository, profileRepository, dispatcher)
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
            val result = usecaseUnderTest.invoke()
            println(result)
            assert(result.isNotEmpty())
            assertEquals(ORDERS_WITH_ONLY_REQUEST_COMMUNICATIONS, result)
        }
    }

    @Test
    fun `request and reply messages are available`() {
        coEvery { communicationRepository.hasUnreadRepliedMessages(listOf("task-id-1"), "recipient") } returns flowOf(true)
        coEvery { communicationRepository.hasUnreadRepliedMessages(listOf("task-id-2"), "recipient") } returns flowOf(true)

        // replied messages are present for both tasks in different orders
        coEvery { communicationRepository.loadRepliedCommunications(listOf("task-id-1"), "recipient") } returns flowOf(
            listOf(
                communicationDataReply(
                    taskId = "task-id-1",
                    telematikId = "telematik-id-1",
                    taskIds = listOf("task-id-1"),
                    communicationId = "communication-id-1-reply"
                )
            )
        )
        coEvery { communicationRepository.loadRepliedCommunications(listOf("task-id-2"), "recipient") } returns flowOf(
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
            val result = usecaseUnderTest.invoke()
            assert(result.isNotEmpty())
            assertEquals(ORDERS_WITH_REQUEST_AND_REPLY, result)
        }
    }

    companion object {
        private val ORDERS_WITH_ONLY_REQUEST_COMMUNICATIONS = listOf(
            Order(
                orderId = "order-id-1",
                sentOn = Instant.parse("2024-01-01T10:00:00Z"),
                prescriptions = listOf(
                    SyncedPrescription(
                        taskId = "task-id-1",
                        name = "Medication",
                        redeemedOn = null,
                        expiresOn = Instant.parse("3024-01-01T10:00:00Z"),
                        state = Ready(
                            type = TaskStateSerializationType.Ready,
                            expiresOn = Instant.parse("3024-01-01T10:00:00Z"),
                            acceptUntil = Instant.parse("3024-01-01T10:00:00Z")
                        ),
                        isIncomplete = false,
                        organization = "Dr. Max Mustermann",
                        authoredOn = Instant.parse("2024-01-01T10:00:00Z"),
                        acceptUntil = Instant.parse("3024-01-01T10:00:00Z"),
                        isDirectAssignment = false,
                        prescriptionChipInformation = PrescriptionChipInformation(
                            isSelfPayPrescription = false,
                            isPartOfMultiplePrescription = false,
                            numerator = null,
                            denominator = null,
                            start = null
                        )
                    )
                ),
                pharmacy = Pharmacy(id = "recipient", name = ""),
                hasUnreadMessages = true,
                latestCommunicationMessage = LastMessage(
                    lastMessageDetails = LastMessageDetails(
                        content = "",
                        pickUpCodeDMC = null,
                        pickUpCodeHR = null,
                        link = null
                    ),
                    profile = CommunicationProfile.ErxCommunicationDispReq
                )
            ),
            Order(
                orderId = "order-id-2",
                sentOn = Instant.parse("2024-01-01T10:00:00Z"),
                prescriptions = listOf(
                    ScannedPrescription(
                        taskId = "task-id-2",
                        name = "Scanned Task",
                        redeemedOn = null,
                        scannedOn = Instant.parse("2024-01-01T10:00:00Z"),
                        index = 0,
                        communications = emptyList()
                    )
                ),
                pharmacy = Pharmacy(id = "recipient", name = ""),
                hasUnreadMessages = false,
                latestCommunicationMessage = LastMessage(
                    lastMessageDetails = LastMessageDetails(
                        content = "",
                        pickUpCodeDMC = null,
                        pickUpCodeHR = null,
                        link = null
                    ),
                    profile = CommunicationProfile.ErxCommunicationDispReq
                )
            )
        )
    }

    private val ORDERS_WITH_REQUEST_AND_REPLY = listOf(
        Order(
            orderId = "order-id-1",
            prescriptions = listOf(
                SyncedPrescription(
                    taskId = "task-id-1",
                    name = "Medication",
                    redeemedOn = null,
                    expiresOn = Instant.parse("3024-01-01T10:00:00Z"),
                    state = Ready(
                        type = TaskStateSerializationType.Ready,
                        expiresOn = Instant.parse("3024-01-01T10:00:00Z"),
                        acceptUntil = Instant.parse("3024-01-01T10:00:00Z")
                    ),
                    isIncomplete = false,
                    organization = "Dr. Max Mustermann",
                    authoredOn = Instant.parse("2024-01-01T10:00:00Z"),
                    acceptUntil = Instant.parse("3024-01-01T10:00:00Z"),
                    isDirectAssignment = false,
                    prescriptionChipInformation = PrescriptionChipInformation(
                        isSelfPayPrescription = false,
                        isPartOfMultiplePrescription = false,
                        numerator = null,
                        denominator = null,
                        start = null
                    )
                )
            ),
            sentOn = Instant.parse("3023-12-31T10:00:00Z"),
            pharmacy = Pharmacy(id = "recipient", name = ""),
            hasUnreadMessages = true,
            latestCommunicationMessage = LastMessage(
                lastMessageDetails = LastMessageDetails(
                    content = null,
                    pickUpCodeDMC = null,
                    pickUpCodeHR = null,
                    link = null
                ),
                profile = CommunicationProfile.ErxCommunicationReply
            )
        ),
        Order(
            orderId = "order-id-2",
            prescriptions = listOf(
                ScannedPrescription(
                    taskId = "task-id-2",
                    name = "Scanned Task",
                    redeemedOn = null,
                    scannedOn = Instant.parse("2024-01-01T10:00:00Z"),
                    index = 0,
                    communications = emptyList()
                )
            ),
            sentOn = Instant.parse("3023-12-31T10:00:00Z"),
            pharmacy = Pharmacy(id = "recipient", name = ""),
            hasUnreadMessages = true,
            latestCommunicationMessage = LastMessage(
                lastMessageDetails = LastMessageDetails(
                    content = null,
                    pickUpCodeDMC = null,
                    pickUpCodeHR = null,
                    link = null
                ),
                profile = CommunicationProfile.ErxCommunicationReply
            )
        )
    )
}
