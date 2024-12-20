/*
 * Copyright 2024, gematik GmbH
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

package de.gematik.ti.erp.app.order.messge.presentation

import de.gematik.ti.erp.app.changelogs.InAppMessageRepository
import de.gematik.ti.erp.app.fhir.model.PharmacyServices
import de.gematik.ti.erp.app.info.BuildConfigInformation
import de.gematik.ti.erp.app.invoice.repository.InvoiceRepository
import de.gematik.ti.erp.app.messages.domain.model.InAppMessageResources
import de.gematik.ti.erp.app.messages.domain.repository.InAppLocalMessageRepository
import de.gematik.ti.erp.app.messages.domain.usecase.FetchInAppMessageUseCase
import de.gematik.ti.erp.app.messages.domain.usecase.FetchWelcomeMessageUseCase
import de.gematik.ti.erp.app.messages.domain.usecase.GetMessageUsingOrderIdUseCase
import de.gematik.ti.erp.app.messages.domain.usecase.GetProfileByOrderIdUseCase
import de.gematik.ti.erp.app.messages.domain.usecase.GetRepliedMessagesUseCase
import de.gematik.ti.erp.app.messages.domain.usecase.SetInternalMessageAsReadUseCase
import de.gematik.ti.erp.app.messages.domain.usecase.UpdateCommunicationByCommunicationIdUseCase
import de.gematik.ti.erp.app.messages.domain.usecase.UpdateCommunicationByOrderIdAndCommunicationIdUseCase
import de.gematik.ti.erp.app.messages.domain.usecase.UpdateInvoicesByOrderIdAndTaskIdUseCase
import de.gematik.ti.erp.app.messages.presentation.MessageDetailController
import de.gematik.ti.erp.app.messages.repository.CommunicationRepository
import de.gematik.ti.erp.app.mocks.order.model.COMMUNICATION_DATA
import de.gematik.ti.erp.app.mocks.order.model.COMMUNICATION_ID
import de.gematik.ti.erp.app.mocks.order.model.MOCK_MESSAGE
import de.gematik.ti.erp.app.mocks.order.model.MOCK_PROFILE
import de.gematik.ti.erp.app.mocks.order.model.ORDER_DETAIL
import de.gematik.ti.erp.app.mocks.order.model.ORDER_ID
import de.gematik.ti.erp.app.mocks.order.model.TASK_ID
import de.gematik.ti.erp.app.mocks.order.model.WELCOME_MESSAGE_GET_MESSAGE_TAG
import de.gematik.ti.erp.app.mocks.order.model.WELCOME_MESSAGE_ID
import de.gematik.ti.erp.app.mocks.order.model.WELCOME_MESSAGE_TAG
import de.gematik.ti.erp.app.mocks.order.model.WELCOME_MESSAGE_TEXT
import de.gematik.ti.erp.app.mocks.order.model.WELCOME_MESSAGE_TIMESTAMP
import de.gematik.ti.erp.app.mocks.order.model.WELCOME_MESSAGE_VERSION
import de.gematik.ti.erp.app.mocks.order.model.inAppMessage
import de.gematik.ti.erp.app.mocks.order.model.internalEntity
import de.gematik.ti.erp.app.mocks.order.model.welcomeMessage
import de.gematik.ti.erp.app.mocks.pharmacy.model.PHARMACY_DATA
import de.gematik.ti.erp.app.mocks.pharmacy.model.PHARMACY_DATA_FHIR
import de.gematik.ti.erp.app.pharmacy.repository.PharmacyRepository
import de.gematik.ti.erp.app.pharmacy.usecase.GetPharmacyByTelematikIdUseCase
import de.gematik.ti.erp.app.profiles.repository.ProfileRepository
import de.gematik.ti.erp.app.profiles.usecase.GetActiveProfileUseCase
import de.gematik.ti.erp.app.utils.uistate.UiState.Companion.isDataState
import de.gematik.ti.erp.app.utils.uistate.UiState.Companion.isEmptyState
import de.gematik.ti.erp.app.utils.uistate.UiState.Companion.isErrorState
import de.gematik.ti.erp.app.utils.uistate.UiState.Companion.isLoadingState
import io.mockk.MockKAnnotations
import io.mockk.Runs
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.spyk
import junit.framework.TestCase.assertEquals
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import org.junit.After
import org.junit.Before
import org.junit.Test
import kotlin.test.assertNotNull

class MessageDetailControllerTest {
    private val profileRepository: ProfileRepository = mockk()
    private val communicationRepository: CommunicationRepository = mockk()
    private val invoiceRepository: InvoiceRepository = mockk()
    private val inAppMessageRepository: InAppMessageRepository = mockk()
    private val messageResources: InAppMessageResources = mockk()
    private val localMessageRepository: InAppLocalMessageRepository = mockk()
    private val buildConfigInformation: BuildConfigInformation = mockk()
    private val pharmacyRepository: PharmacyRepository = mockk()
    private val dispatcher = StandardTestDispatcher()
    private val testScope = TestScope(dispatcher)

    private lateinit var controllerUnderTest: MessageDetailController
    private val clock = mockk<Clock>()

    private lateinit var getActiveProfileUseCase: GetActiveProfileUseCase
    private lateinit var getRepliedMessagesUseCase: GetRepliedMessagesUseCase
    private lateinit var getMessageUsingOrderIdUseCase: GetMessageUsingOrderIdUseCase
    private lateinit var fetchInAppMessageUseCase: FetchInAppMessageUseCase
    private lateinit var fetchWelcomeMessageUseCase: FetchWelcomeMessageUseCase
    private lateinit var setInternalMessageIsReadUseCase: SetInternalMessageAsReadUseCase
    private lateinit var updateCommunicationByCommunicationIdUseCase: UpdateCommunicationByCommunicationIdUseCase
    private lateinit var updateCommunicationByOrderIdAndCommunicationIdUseCase: UpdateCommunicationByOrderIdAndCommunicationIdUseCase
    private lateinit var updateInvoicesByOrderIdAndTaskIdUseCase: UpdateInvoicesByOrderIdAndTaskIdUseCase
    private lateinit var getPharmacyByTelematikIdUseCase: GetPharmacyByTelematikIdUseCase
    private lateinit var getProfileByOrderIdUseCase: GetProfileByOrderIdUseCase

    @OptIn(ExperimentalCoroutinesApi::class)
    @Before
    fun setUp() {
        Dispatchers.setMain(dispatcher)
        MockKAnnotations.init(this)

        getActiveProfileUseCase = GetActiveProfileUseCase(
            repository = profileRepository,
            dispatcher = dispatcher
        )
        setInternalMessageIsReadUseCase = SetInternalMessageAsReadUseCase(
            inAppMessageRepository = inAppMessageRepository

        )
        fetchInAppMessageUseCase = FetchInAppMessageUseCase(
            inAppMessageRepository = inAppMessageRepository,
            localMessageRepository = localMessageRepository,
            messageResources = messageResources
        )
        getActiveProfileUseCase = GetActiveProfileUseCase(
            repository = profileRepository,
            dispatcher = dispatcher
        )

        getRepliedMessagesUseCase = GetRepliedMessagesUseCase(
            communicationRepository = communicationRepository,
            dispatcher = dispatcher
        )
        getMessageUsingOrderIdUseCase = GetMessageUsingOrderIdUseCase(
            communicationRepository = communicationRepository,
            invoiceRepository = invoiceRepository,
            dispatcher = dispatcher
        )
        updateCommunicationByCommunicationIdUseCase = UpdateCommunicationByCommunicationIdUseCase(
            repository = communicationRepository,
            dispatcher = dispatcher
        )
        updateCommunicationByOrderIdAndCommunicationIdUseCase = UpdateCommunicationByOrderIdAndCommunicationIdUseCase(
            repository = communicationRepository,
            dispatcher = dispatcher
        )
        updateInvoicesByOrderIdAndTaskIdUseCase = UpdateInvoicesByOrderIdAndTaskIdUseCase(
            communicationRepository = communicationRepository,
            invoiceRepository = invoiceRepository,
            dispatcher = dispatcher
        )
        getPharmacyByTelematikIdUseCase = GetPharmacyByTelematikIdUseCase(
            repository = pharmacyRepository,
            dispatchers = dispatcher
        )
        getProfileByOrderIdUseCase = GetProfileByOrderIdUseCase(
            communicationRepository = communicationRepository,
            dispatcher = dispatcher
        )
        fetchWelcomeMessageUseCase = spyk(FetchWelcomeMessageUseCase(inAppMessageRepository, buildConfigInformation, messageResources))
        fetchInAppMessageUseCase = spyk(FetchInAppMessageUseCase(inAppMessageRepository, localMessageRepository, messageResources))
        controllerUnderTest = MessageDetailController(
            orderId = ORDER_ID,
            isLocalMessage = false,
            getRepliedMessagesUseCase = getRepliedMessagesUseCase,
            getMessageUsingOrderIdUseCase = getMessageUsingOrderIdUseCase,
            fetchInAppMessageUseCase = fetchInAppMessageUseCase,
            fetchWelcomeMessageUseCase = fetchWelcomeMessageUseCase,
            setInternalMessageIsReadUseCase = setInternalMessageIsReadUseCase,
            updateCommunicationByCommunicationIdUseCase = updateCommunicationByCommunicationIdUseCase,
            updateCommunicationByOrderIdAndCommunicationIdUseCase = updateCommunicationByOrderIdAndCommunicationIdUseCase,
            updateInvoicesByOrderIdAndTaskIdUseCase = updateInvoicesByOrderIdAndTaskIdUseCase,
            getPharmacyByTelematikIdUseCase = getPharmacyByTelematikIdUseCase,
            getProfileByOrderIdUseCase = getProfileByOrderIdUseCase
        )

        every { clock.now() } returns Instant.parse(WELCOME_MESSAGE_TIMESTAMP)
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `when orderId is not provided`() {
        coEvery { communicationRepository.profileByOrderId(ORDER_ID) } returns flowOf(MOCK_PROFILE)

        testScope.runTest {
            controllerUnderTest.init()
            advanceUntilIdle()

            val messagesResult = controllerUnderTest.messages.first()
            val orderStateResult = controllerUnderTest.order.first()
            val pharmacyStateResult = controllerUnderTest.pharmacy.first()

            assert(messagesResult.isLoadingState)
            assert(orderStateResult.isErrorState)
            assert(pharmacyStateResult.isEmptyState)
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `when orderId is provided and returns data successfully`() {
        val testMessages = listOf(MOCK_MESSAGE)

        every { getRepliedMessagesUseCase(ORDER_ID, "") } returns flowOf(testMessages)
        every { getMessageUsingOrderIdUseCase(ORDER_ID) } returns flowOf(ORDER_DETAIL)
        coEvery { pharmacyRepository.searchPharmacyByTelematikId(any()) } returns Result.success(
            PharmacyServices(listOf(PHARMACY_DATA_FHIR), "", 1)
        )

        every { communicationRepository.taskIdsByOrder(ORDER_ID) } returns flowOf(listOf(TASK_ID))
        every { communicationRepository.loadRepliedCommunications(listOf(TASK_ID), any()) } returns flowOf(
            listOf(COMMUNICATION_DATA)
        )
        every { communicationRepository.loadDispReqCommunications(ORDER_ID) } returns flowOf(
            listOf(
                COMMUNICATION_DATA
            )
        )
        every { communicationRepository.loadPharmacies() } returns flowOf(emptyList())
        every { communicationRepository.hasUnreadDispenseMessage(any(), any()) } returns flowOf(false)
        every { invoiceRepository.invoiceByTaskId(TASK_ID) } returns flowOf(null)
        every { invoiceRepository.invoiceByTaskId(eq(TASK_ID)) } returns flowOf(null)
        every { communicationRepository.loadSyncedByTaskId(TASK_ID) } returns flowOf(null)
        every { communicationRepository.loadScannedByTaskId(TASK_ID) } returns flowOf(null)
        coEvery { communicationRepository.downloadMissingPharmacy(any()) } returns Result.success(null)
        coEvery { communicationRepository.profileByOrderId(ORDER_ID) } returns flowOf(MOCK_PROFILE)

        testScope.runTest {
            controllerUnderTest.init()
            advanceUntilIdle()

            val messagesResult = controllerUnderTest.messages.first()
            val orderStateResult = controllerUnderTest.order.first()
            val pharmacyStateResult = controllerUnderTest.pharmacy.first()
            val actualMessages = messagesResult.data

            assertEquals(testMessages, actualMessages)
            assert(orderStateResult.isDataState)
            assertEquals(ORDER_DETAIL, orderStateResult.data)
            assert(pharmacyStateResult.isDataState)
            assertEquals(PHARMACY_DATA, pharmacyStateResult.data)
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `when orderId is provided but messages and order data are not found`() {
        every { getRepliedMessagesUseCase(ORDER_ID, "") } returns flowOf(emptyList())
        every { getMessageUsingOrderIdUseCase(ORDER_ID) } returns flowOf(null)
        coEvery { pharmacyRepository.searchPharmacyByTelematikId(any()) } returns Result.success(
            PharmacyServices(emptyList(), "", 0)
        )

        every { communicationRepository.taskIdsByOrder(ORDER_ID) } returns flowOf(emptyList())
        every { communicationRepository.loadRepliedCommunications(emptyList(), "") } returns flowOf(emptyList())
        every { communicationRepository.loadDispReqCommunications(ORDER_ID) } returns flowOf(emptyList())
        every { communicationRepository.loadPharmacies() } returns flowOf(emptyList())
        every { communicationRepository.hasUnreadDispenseMessage(any(), any()) } returns flowOf(false)
        every { invoiceRepository.invoiceByTaskId(any()) } returns flowOf(null)
        every { communicationRepository.loadSyncedByTaskId(any()) } returns flowOf(null)
        every { communicationRepository.loadScannedByTaskId(any()) } returns flowOf(null)
        coEvery { communicationRepository.downloadMissingPharmacy(any()) } returns Result.success(null)
        coEvery { communicationRepository.setCommunicationStatus(any(), any()) } returns Unit
        coEvery { communicationRepository.profileByOrderId(ORDER_ID) } returns flowOf(MOCK_PROFILE)

        testScope.runTest {
            controllerUnderTest.init()
            advanceUntilIdle()

            val messagesResult = controllerUnderTest.messages.first()
            val orderStateResult = controllerUnderTest.order.first()
            val pharmacyStateResult = controllerUnderTest.pharmacy.first()

            messagesResult.data?.let { assert(it.isEmpty()) }
            assert(orderStateResult.isEmptyState)
            assert(pharmacyStateResult.isLoadingState)
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `when orderId is provided and use cases throw errors`() {
        every { getRepliedMessagesUseCase(ORDER_ID, "") } throws IllegalArgumentException("Messages error")
        every { getMessageUsingOrderIdUseCase(ORDER_ID) } throws IllegalArgumentException("Order error")
        coEvery {
            pharmacyRepository.searchPharmacyByTelematikId(any())
        } throws IllegalArgumentException("Pharmacy error")
        coEvery { communicationRepository.profileByOrderId(ORDER_ID) } returns flowOf(MOCK_PROFILE)

        testScope.runTest {
            controllerUnderTest.init()
            advanceUntilIdle()

            val orderStateResult = controllerUnderTest.order.first()
            val pharmacyStateResult = controllerUnderTest.pharmacy.first()

            assert(orderStateResult.isErrorState)
            assert(pharmacyStateResult.isErrorState)
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `fetch inapp messages from local database`() {
        val mockMessages = inAppMessage
        coEvery { inAppMessageRepository.showWelcomeMessage } returns flowOf(false)
        coEvery { fetchInAppMessageUseCase.invoke() } returns flowOf(mockMessages)
        every { getMessageUsingOrderIdUseCase(ORDER_ID) } returns flowOf(null)
        coEvery { inAppMessageRepository.setInternalMessageAsRead() } returns Unit
        coEvery { inAppMessageRepository.inAppMessages } returns flowOf(internalEntity)

        controllerUnderTest = MessageDetailController(
            orderId = ORDER_ID,
            isLocalMessage = true,
            getRepliedMessagesUseCase = getRepliedMessagesUseCase,
            getMessageUsingOrderIdUseCase = getMessageUsingOrderIdUseCase,
            fetchInAppMessageUseCase = fetchInAppMessageUseCase,
            fetchWelcomeMessageUseCase = fetchWelcomeMessageUseCase,
            setInternalMessageIsReadUseCase = setInternalMessageIsReadUseCase,
            updateCommunicationByCommunicationIdUseCase = updateCommunicationByCommunicationIdUseCase,
            updateCommunicationByOrderIdAndCommunicationIdUseCase = updateCommunicationByOrderIdAndCommunicationIdUseCase,
            updateInvoicesByOrderIdAndTaskIdUseCase = updateInvoicesByOrderIdAndTaskIdUseCase,
            getPharmacyByTelematikIdUseCase = getPharmacyByTelematikIdUseCase,
            getProfileByOrderIdUseCase = getProfileByOrderIdUseCase
        )

        testScope.runTest {
            controllerUnderTest.init()
            advanceUntilIdle()
            val messageList = controllerUnderTest._localMessages.value

            assertNotNull(messageList)
            assertEquals(messageList, inAppMessage)
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `fetch inapp messages from local database return empty list`() {
        every { getMessageUsingOrderIdUseCase(ORDER_ID) } returns flowOf(null)
        coEvery { inAppMessageRepository.setInternalMessageAsRead() } returns Unit
        coEvery { buildConfigInformation.versionName() } returns (WELCOME_MESSAGE_VERSION)
        coEvery { inAppMessageRepository.showWelcomeMessage } returns flowOf(false)
        coEvery { localMessageRepository.getInternalMessages() } returns flowOf(emptyList())
        coEvery { inAppMessageRepository.inAppMessages } returns flowOf(emptyList())

        controllerUnderTest = MessageDetailController(
            orderId = ORDER_ID,
            isLocalMessage = true,
            getRepliedMessagesUseCase = getRepliedMessagesUseCase,
            getMessageUsingOrderIdUseCase = getMessageUsingOrderIdUseCase,
            fetchInAppMessageUseCase = fetchInAppMessageUseCase,
            fetchWelcomeMessageUseCase = fetchWelcomeMessageUseCase,
            setInternalMessageIsReadUseCase = setInternalMessageIsReadUseCase,
            updateCommunicationByCommunicationIdUseCase = updateCommunicationByCommunicationIdUseCase,
            updateCommunicationByOrderIdAndCommunicationIdUseCase = updateCommunicationByOrderIdAndCommunicationIdUseCase,
            updateInvoicesByOrderIdAndTaskIdUseCase = updateInvoicesByOrderIdAndTaskIdUseCase,
            getPharmacyByTelematikIdUseCase = getPharmacyByTelematikIdUseCase,
            getProfileByOrderIdUseCase = getProfileByOrderIdUseCase
        )

        testScope.runTest {
            controllerUnderTest.init()
            advanceUntilIdle()
            val messageList = controllerUnderTest._localMessages.value
            assertNotNull(messageList)
            kotlin.test.assertEquals(true, messageList.isEmpty())
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `fetch inapp messages from local database return empty list with welcome message`() {
        every { getMessageUsingOrderIdUseCase(ORDER_ID) } returns flowOf(null)
        coEvery { fetchWelcomeMessageUseCase.invoke() } returns flowOf(welcomeMessage)
        coEvery { inAppMessageRepository.setInternalMessageAsRead() } returns Unit
        coEvery { buildConfigInformation.versionName() } returns (WELCOME_MESSAGE_VERSION)
        coEvery { inAppMessageRepository.showWelcomeMessage } returns flowOf(true)
        coEvery { localMessageRepository.getInternalMessages() } returns flowOf(emptyList())
        coEvery { messageResources.messageFrom } returns (WELCOME_MESSAGE_ID)
        coEvery { messageResources.welcomeMessage } returns (WELCOME_MESSAGE_TEXT)
        coEvery { messageResources.welcomeMessageTag } returns (WELCOME_MESSAGE_TAG)
        coEvery { messageResources.getMessageTag(any()) } returns (WELCOME_MESSAGE_GET_MESSAGE_TAG)
        coEvery { inAppMessageRepository.inAppMessages } returns flowOf(emptyList())
        coEvery { fetchWelcomeMessageUseCase.getCurrentTimeAsString() } returns (WELCOME_MESSAGE_VERSION)

        controllerUnderTest = MessageDetailController(
            orderId = ORDER_ID,
            isLocalMessage = true,
            getRepliedMessagesUseCase = getRepliedMessagesUseCase,
            getMessageUsingOrderIdUseCase = getMessageUsingOrderIdUseCase,
            fetchInAppMessageUseCase = fetchInAppMessageUseCase,
            fetchWelcomeMessageUseCase = fetchWelcomeMessageUseCase,
            setInternalMessageIsReadUseCase = setInternalMessageIsReadUseCase,
            updateCommunicationByCommunicationIdUseCase = updateCommunicationByCommunicationIdUseCase,
            updateCommunicationByOrderIdAndCommunicationIdUseCase = updateCommunicationByOrderIdAndCommunicationIdUseCase,
            updateInvoicesByOrderIdAndTaskIdUseCase = updateInvoicesByOrderIdAndTaskIdUseCase,
            getPharmacyByTelematikIdUseCase = getPharmacyByTelematikIdUseCase,
            getProfileByOrderIdUseCase = getProfileByOrderIdUseCase
        )

        testScope.runTest {
            controllerUnderTest.init()
            advanceUntilIdle()
            val messageList = controllerUnderTest._localMessages.value
            assertNotNull(messageList)
            assertEquals(messageList, listOf(welcomeMessage))
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `consumeAllMessages updates communication statuses correctly`() {
        val mockMessages = listOf(MOCK_MESSAGE)
        coEvery { inAppMessageRepository.showWelcomeMessage } returns flowOf(false)
        every { getRepliedMessagesUseCase(ORDER_ID, "") } returns flowOf(mockMessages)
        every { getMessageUsingOrderIdUseCase(ORDER_ID) } returns flowOf(ORDER_DETAIL)
        coEvery { pharmacyRepository.searchPharmacyByTelematikId(any()) } returns Result.success(
            PharmacyServices(emptyList(), "", 0)
        )

        every { communicationRepository.taskIdsByOrder(ORDER_ID) } returns flowOf(listOf(TASK_ID))
        every { communicationRepository.loadRepliedCommunications(listOf(TASK_ID), any()) } returns flowOf(
            listOf(
                COMMUNICATION_DATA
            )
        )
        every { communicationRepository.loadDispReqCommunications(ORDER_ID) } returns flowOf(
            listOf(
                COMMUNICATION_DATA
            )
        )
        every { communicationRepository.loadPharmacies() } returns flowOf(emptyList())
        every { communicationRepository.hasUnreadDispenseMessage(any(), any()) } returns flowOf(false)
        every { invoiceRepository.invoiceByTaskId(TASK_ID) } returns flowOf(null)
        coEvery { invoiceRepository.updateInvoiceCommunicationStatus(TASK_ID, true) } just Runs
        every { communicationRepository.loadSyncedByTaskId(TASK_ID) } returns flowOf(null)
        every { communicationRepository.loadScannedByTaskId(TASK_ID) } returns flowOf(null)
        coEvery { communicationRepository.downloadMissingPharmacy(any()) } returns Result.success(null)
        coEvery { communicationRepository.setCommunicationStatus(any(), any()) } just Runs
        coEvery { communicationRepository.profileByOrderId(ORDER_ID) } returns flowOf(MOCK_PROFILE)

        val updateCommunicationByOrderIdAndCommunicationIdUseCase = spyk(
            UpdateCommunicationByOrderIdAndCommunicationIdUseCase(communicationRepository, dispatcher)
        )
        val updateCommunicationByCommunicationIdUseCase = spyk(
            UpdateCommunicationByCommunicationIdUseCase(communicationRepository, dispatcher)
        )

        controllerUnderTest = MessageDetailController(
            orderId = ORDER_ID,
            isLocalMessage = false,
            getRepliedMessagesUseCase = getRepliedMessagesUseCase,
            getMessageUsingOrderIdUseCase = getMessageUsingOrderIdUseCase,
            fetchInAppMessageUseCase = fetchInAppMessageUseCase,
            fetchWelcomeMessageUseCase = fetchWelcomeMessageUseCase,
            setInternalMessageIsReadUseCase = setInternalMessageIsReadUseCase,
            updateCommunicationByCommunicationIdUseCase = updateCommunicationByCommunicationIdUseCase,
            updateCommunicationByOrderIdAndCommunicationIdUseCase = updateCommunicationByOrderIdAndCommunicationIdUseCase,
            updateInvoicesByOrderIdAndTaskIdUseCase = updateInvoicesByOrderIdAndTaskIdUseCase,
            getPharmacyByTelematikIdUseCase = getPharmacyByTelematikIdUseCase,
            getProfileByOrderIdUseCase = getProfileByOrderIdUseCase
        )

        testScope.runTest {
            controllerUnderTest.init()
            advanceUntilIdle()

            controllerUnderTest.consumeAllMessages {}
            advanceUntilIdle()

            coVerify(exactly = 1) { updateCommunicationByOrderIdAndCommunicationIdUseCase(ORDER_ID) }
            coVerify(exactly = 1) { updateCommunicationByCommunicationIdUseCase(COMMUNICATION_ID) }
        }
    }
}

interface Clock {
    fun now(): Instant
}

class SystemClock : Clock {
    override fun now(): Instant = Clock.System.now()
}
