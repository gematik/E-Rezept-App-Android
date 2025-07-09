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

package de.gematik.ti.erp.app.order.messge.presentation

import android.app.Application
import com.google.mlkit.common.model.RemoteModelManager
import de.gematik.ti.erp.app.fhir.common.model.erp.FhirPharmacyErpModelCollection
import de.gematik.ti.erp.app.fhir.pharmacy.type.PharmacyVzdService
import de.gematik.ti.erp.app.info.BuildConfigInformation
import de.gematik.ti.erp.app.invoice.repository.InvoiceRepository
import de.gematik.ti.erp.app.messages.domain.model.InternalMessageResources
import de.gematik.ti.erp.app.messages.domain.repository.ChangeLogLocalDataSource
import de.gematik.ti.erp.app.messages.domain.usecase.GetInternalMessagesUseCase
import de.gematik.ti.erp.app.messages.domain.usecase.GetMessageUsingOrderIdUseCase
import de.gematik.ti.erp.app.messages.domain.usecase.GetProfileByOrderIdUseCase
import de.gematik.ti.erp.app.messages.domain.usecase.GetRepliedMessagesUseCase
import de.gematik.ti.erp.app.messages.domain.usecase.SetInternalMessagesAsReadUseCase
import de.gematik.ti.erp.app.messages.domain.usecase.UpdateCommunicationConsumedStatusUseCase
import de.gematik.ti.erp.app.messages.domain.usecase.UpdateCommunicationConsumedStatusUseCase.Companion.CommunicationIdentifier
import de.gematik.ti.erp.app.messages.domain.usecase.UpdateInvoicesByOrderIdAndTaskIdUseCase
import de.gematik.ti.erp.app.messages.mapper.toInAppMessage
import de.gematik.ti.erp.app.messages.presentation.MessageDetailController
import de.gematik.ti.erp.app.messages.repository.CachedPharmacy
import de.gematik.ti.erp.app.messages.repository.CommunicationRepository
import de.gematik.ti.erp.app.messages.repository.InternalMessagesRepository
import de.gematik.ti.erp.app.mocks.order.model.COMMUNICATION_DATA_WITH_TASK_ID
import de.gematik.ti.erp.app.mocks.order.model.COMMUNICATION_ID
import de.gematik.ti.erp.app.mocks.order.model.IN_APP_MESSAGE_TEXT
import de.gematik.ti.erp.app.mocks.order.model.MOCK_MESSAGE
import de.gematik.ti.erp.app.mocks.order.model.MOCK_PROFILE
import de.gematik.ti.erp.app.mocks.order.model.MOCK_SYNCED_TASK_DATA_01_NEW
import de.gematik.ti.erp.app.mocks.order.model.ORDER_DETAIL
import de.gematik.ti.erp.app.mocks.order.model.ORDER_ID
import de.gematik.ti.erp.app.mocks.order.model.TASK_ID
import de.gematik.ti.erp.app.mocks.order.model.TELEMATIK_ID
import de.gematik.ti.erp.app.mocks.order.model.WELCOME_MESSAGE_GET_MESSAGE_TAG
import de.gematik.ti.erp.app.mocks.order.model.WELCOME_MESSAGE_ID
import de.gematik.ti.erp.app.mocks.order.model.WELCOME_MESSAGE_LANG
import de.gematik.ti.erp.app.mocks.order.model.WELCOME_MESSAGE_TAG
import de.gematik.ti.erp.app.mocks.order.model.WELCOME_MESSAGE_TEXT
import de.gematik.ti.erp.app.mocks.order.model.WELCOME_MESSAGE_TIMESTAMP
import de.gematik.ti.erp.app.mocks.order.model.WELCOME_MESSAGE_VERSION
import de.gematik.ti.erp.app.mocks.order.model.welcomeMessage
import de.gematik.ti.erp.app.mocks.pharmacy.model.PHARMACY_DATA
import de.gematik.ti.erp.app.mocks.pharmacy.model.PHARMACY_DATA_FHIR
import de.gematik.ti.erp.app.pharmacy.repository.PharmacyRepository
import de.gematik.ti.erp.app.pharmacy.usecase.GetPharmacyByTelematikIdUseCase
import de.gematik.ti.erp.app.profiles.repository.ProfileRepository
import de.gematik.ti.erp.app.profiles.usecase.GetActiveProfileUseCase
import de.gematik.ti.erp.app.translation.domain.model.LanguageDownloadState
import de.gematik.ti.erp.app.translation.repository.TranslationRepository
import de.gematik.ti.erp.app.translation.usecase.DownloadLanguageModelUseCase
import de.gematik.ti.erp.app.translation.usecase.GetTranslationConsentUseCase
import de.gematik.ti.erp.app.translation.usecase.IsTargetLanguageSetUseCase
import de.gematik.ti.erp.app.translation.usecase.ToggleTranslationConsentUseCase
import de.gematik.ti.erp.app.translation.usecase.TranslateTextUseCase
import de.gematik.ti.erp.app.utils.uistate.UiState.Companion.isDataState
import de.gematik.ti.erp.app.utils.uistate.UiState.Companion.isEmptyState
import de.gematik.ti.erp.app.utils.uistate.UiState.Companion.isErrorState
import de.gematik.ti.erp.app.utils.uistate.UiState.Companion.isLoadingState
import io.mockk.MockKAnnotations
import io.mockk.Runs
import io.mockk.clearMocks
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.spyk
import junit.framework.TestCase.assertEquals
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
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
import kotlin.test.fail

class MessageDetailControllerTest {
    private val profileRepository: ProfileRepository = mockk()
    private val communicationRepository: CommunicationRepository = mockk()
    private val invoiceRepository: InvoiceRepository = mockk()
    private val internalMessagesRepository: InternalMessagesRepository = mockk()
    private val messageResources: InternalMessageResources = mockk()
    private val localMessageRepository: ChangeLogLocalDataSource = mockk()
    private val buildConfigInformation: BuildConfigInformation = mockk()
    private val pharmacyRepository: PharmacyRepository = mockk()
    private val changeLogLocalDataSource: ChangeLogLocalDataSource = mockk()
    private val translationRepository: TranslationRepository = mockk()
    private val dispatcher = StandardTestDispatcher()
    private val testScope = TestScope(dispatcher)
    private val mockContext = mockk<Application>()

    private lateinit var controllerUnderTest: MessageDetailController
    private lateinit var isLocalMessageControllerUnderTest: MessageDetailController
    private val clock = mockk<Clock>()

    private lateinit var getActiveProfileUseCase: GetActiveProfileUseCase
    private lateinit var getRepliedMessagesUseCase: GetRepliedMessagesUseCase
    private lateinit var getMessageUsingOrderIdUseCase: GetMessageUsingOrderIdUseCase
    private lateinit var getInternalMessagesUseCase: GetInternalMessagesUseCase
    private lateinit var setInternalMessageIsReadUseCase: SetInternalMessagesAsReadUseCase
    private lateinit var updateCommunicationConsumedStatusUseCase: UpdateCommunicationConsumedStatusUseCase
    private lateinit var updateInvoicesByOrderIdAndTaskIdUseCase: UpdateInvoicesByOrderIdAndTaskIdUseCase
    private lateinit var getPharmacyByTelematikIdUseCase: GetPharmacyByTelematikIdUseCase
    private lateinit var getProfileByOrderIdUseCase: GetProfileByOrderIdUseCase
    private lateinit var getTranslationConsentUseCase: GetTranslationConsentUseCase
    private lateinit var translateTextUseCase: TranslateTextUseCase
    private lateinit var isTargetLanguageSetUseCase: IsTargetLanguageSetUseCase
    private lateinit var toggleTranslationConsentUseCase: ToggleTranslationConsentUseCase
    private lateinit var downloadLanguageModelUseCase: DownloadLanguageModelUseCase

    @OptIn(ExperimentalCoroutinesApi::class)
    @Before
    fun setUp() {
        Dispatchers.setMain(dispatcher)
        MockKAnnotations.init(this)

        getActiveProfileUseCase = GetActiveProfileUseCase(
            repository = profileRepository,
            dispatcher = dispatcher
        )
        setInternalMessageIsReadUseCase = SetInternalMessagesAsReadUseCase(
            internalMessagesRepository = internalMessagesRepository

        )
        getInternalMessagesUseCase = GetInternalMessagesUseCase(
            internalMessagesRepository = internalMessagesRepository,
            changeLogLocalDataSource = changeLogLocalDataSource
        )

        getRepliedMessagesUseCase = GetRepliedMessagesUseCase(
            communicationRepository = communicationRepository,
            dispatcher = dispatcher
        )
        getMessageUsingOrderIdUseCase = GetMessageUsingOrderIdUseCase(
            communicationRepository = communicationRepository,
            invoiceRepository = invoiceRepository,
            pharmacyRepository = pharmacyRepository,
            dispatcher = dispatcher
        )
        updateCommunicationConsumedStatusUseCase = spyk(
            UpdateCommunicationConsumedStatusUseCase(
                repository = communicationRepository,
                dispatcher = dispatcher
            )
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
        getInternalMessagesUseCase = spyk(GetInternalMessagesUseCase(internalMessagesRepository, changeLogLocalDataSource))

        translateTextUseCase = TranslateTextUseCase(
            remoteModelManager = mockk<RemoteModelManager>(relaxed = true),
            repository = translationRepository
        )

        getTranslationConsentUseCase = GetTranslationConsentUseCase(repository = translationRepository)

        isTargetLanguageSetUseCase = IsTargetLanguageSetUseCase(repository = translationRepository)

        toggleTranslationConsentUseCase = ToggleTranslationConsentUseCase(
            repository = translationRepository,
            dispatcher = dispatcher
        )

        downloadLanguageModelUseCase = DownloadLanguageModelUseCase(
            repository = translationRepository,
            dispatcher = dispatcher
        )

        every { mockContext.getString(any()) } returns IN_APP_MESSAGE_TEXT
        every { mockContext.resources.configuration.locales[0].language } returns "de"
        every { clock.now() } returns Instant.parse(WELCOME_MESSAGE_TIMESTAMP)
        every { communicationRepository.hasUnreadDispenseMessage(any(), any()) } returns flowOf(false)
        every { invoiceRepository.invoiceByTaskId(TASK_ID) } returns flowOf(null)
        every { communicationRepository.taskIdsByOrder(ORDER_ID) } returns flowOf(listOf(TASK_ID))
        every { communicationRepository.loadScannedByTaskId(TASK_ID) } returns flowOf(null)
        coEvery { communicationRepository.profileByOrderId(ORDER_ID) } returns flowOf(MOCK_PROFILE)
        every { pharmacyRepository.loadCachedPharmacies() } returns flowOf(listOf())
        coEvery { pharmacyRepository.savePharmacyToCache(any()) } returns Unit
        coEvery { pharmacyRepository.searchPharmacyByTelematikId(any()) } returns Result.failure(Exception())
        coEvery { translationRepository.getTargetLanguageTag() } returns flowOf("de")
        coEvery { translationRepository.isTranslationAllowed() } returns MutableStateFlow(true)
        coEvery { translationRepository.enableConsentForLocalTranslation() } returns Unit
        coEvery { translationRepository.disableConsentForLocalTranslation() } returns Unit
        coEvery { translationRepository.clearTargetLanguageTag() } returns Unit
        coEvery { translationRepository.setTargetLanguageTag(any()) } returns Unit
        coEvery { translationRepository.downloadLanguageModels(any()) } returns flowOf(LanguageDownloadState.Completed)

        isLocalMessageControllerUnderTest = MessageDetailController(
            application = mockContext,
            orderId = ORDER_ID,
            isLocalMessage = true,
            getRepliedMessagesUseCase = getRepliedMessagesUseCase,
            getMessageUsingOrderIdUseCase = getMessageUsingOrderIdUseCase,
            getInternalMessagesUseCase = getInternalMessagesUseCase,
            setInternalMessageIsReadUseCase = setInternalMessageIsReadUseCase,
            updateCommunicationConsumedStatusUseCase = updateCommunicationConsumedStatusUseCase,
            updateInvoicesByOrderIdAndTaskIdUseCase = updateInvoicesByOrderIdAndTaskIdUseCase,
            getPharmacyByTelematikIdUseCase = getPharmacyByTelematikIdUseCase,
            getProfileByOrderIdUseCase = getProfileByOrderIdUseCase,
            getTranslationConsentUseCase = getTranslationConsentUseCase,
            isTargetLanguageSetUseCase = isTargetLanguageSetUseCase,
            translateTextUseCase = translateTextUseCase,
            toggleTranslationConsentUseCase = toggleTranslationConsentUseCase,
            downloadedLanguagesUseCase = downloadLanguageModelUseCase
        )
        controllerUnderTest = MessageDetailController(
            application = mockContext,
            orderId = ORDER_ID,
            isLocalMessage = false,
            getRepliedMessagesUseCase = getRepliedMessagesUseCase,
            getMessageUsingOrderIdUseCase = getMessageUsingOrderIdUseCase,
            getInternalMessagesUseCase = getInternalMessagesUseCase,
            setInternalMessageIsReadUseCase = setInternalMessageIsReadUseCase,
            updateCommunicationConsumedStatusUseCase = updateCommunicationConsumedStatusUseCase,
            updateInvoicesByOrderIdAndTaskIdUseCase = updateInvoicesByOrderIdAndTaskIdUseCase,
            getPharmacyByTelematikIdUseCase = getPharmacyByTelematikIdUseCase,
            getProfileByOrderIdUseCase = getProfileByOrderIdUseCase,
            getTranslationConsentUseCase = getTranslationConsentUseCase,
            isTargetLanguageSetUseCase = isTargetLanguageSetUseCase,
            translateTextUseCase = translateTextUseCase,
            toggleTranslationConsentUseCase = toggleTranslationConsentUseCase,
            downloadedLanguagesUseCase = downloadLanguageModelUseCase
        )
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @After
    fun tearDown() {
        Dispatchers.resetMain()
        clearMocks(
            communicationRepository,
            profileRepository,
            invoiceRepository,
            internalMessagesRepository,
            messageResources,
            buildConfigInformation,
            localMessageRepository,
            pharmacyRepository,
            changeLogLocalDataSource
        )
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `when orderId is not provided`() {
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
        val expectedMessages = listOf(MOCK_MESSAGE)
        coEvery { pharmacyRepository.searchPharmacyByTelematikId(any()) } returns Result.success(
            FhirPharmacyErpModelCollection(PharmacyVzdService.FHIRVZD, 1, "", listOf(PHARMACY_DATA_FHIR))
        )
        every { communicationRepository.loadRepliedCommunications(listOf(TASK_ID), any()) } returns flowOf(listOf(COMMUNICATION_DATA_WITH_TASK_ID))
        every { communicationRepository.loadDispReqCommunications(ORDER_ID) } returns flowOf(listOf(COMMUNICATION_DATA_WITH_TASK_ID))
        every { communicationRepository.loadPharmacies() } returns flowOf(
            listOf(
                CachedPharmacy(
                    name = "Apotheke Adelheid Ulmendorfer TEST-ONLY",
                    telematikId = TELEMATIK_ID
                )
            )
        )
        every { communicationRepository.loadSyncedByTaskId(TASK_ID) } returns flowOf(MOCK_SYNCED_TASK_DATA_01_NEW)

        testScope.runTest {
            controllerUnderTest.init()
            advanceUntilIdle()
            val messagesResult = controllerUnderTest.messages.first()
            val orderStateResult = controllerUnderTest.order.first()
            val pharmacyStateResult = controllerUnderTest.pharmacy.first()
            val actualMessages = messagesResult.data
            assertEquals(expectedMessages, actualMessages)
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
            FhirPharmacyErpModelCollection(
                PharmacyVzdService.FHIRVZD,
                0,
                "",
                emptyList()
            )
        )
        every { communicationRepository.taskIdsByOrder(ORDER_ID) } returns flowOf(emptyList())
        every { communicationRepository.loadRepliedCommunications(emptyList(), "") } returns flowOf(emptyList())
        every { communicationRepository.loadDispReqCommunications(ORDER_ID) } returns flowOf(emptyList())
        every { communicationRepository.loadPharmacies() } returns flowOf(emptyList())
        every { communicationRepository.loadSyncedByTaskId(any()) } returns flowOf(null)
        coEvery { communicationRepository.setCommunicationStatus(any(), any()) } returns Unit

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
        coEvery { internalMessagesRepository.getLastUpdatedVersion() } returns flowOf(WELCOME_MESSAGE_VERSION)
        coEvery { getInternalMessagesUseCase.invoke(WELCOME_MESSAGE_LANG) } returns flowOf(listOf(welcomeMessage.toInAppMessage()))
        every { getMessageUsingOrderIdUseCase(ORDER_ID) } returns flowOf(null)
        coEvery { internalMessagesRepository.setInternalMessagesAsRead() } returns Unit
        coEvery { changeLogLocalDataSource.getInternalMessageInCurrentLanguage(welcomeMessage) } returns welcomeMessage
        coEvery { internalMessagesRepository.updateInternalMessage(welcomeMessage) } returns Unit
        coEvery { internalMessagesRepository.getInternalMessages() } returns flowOf(listOf(welcomeMessage))

        testScope.runTest {
            isLocalMessageControllerUnderTest.init()
            advanceUntilIdle()
            val messageList = isLocalMessageControllerUnderTest._localMessages.value

            assertNotNull(messageList)
            assertEquals(messageList, listOf(welcomeMessage.toInAppMessage()))
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `fetch inapp messages from local database return empty list`() {
        coEvery { internalMessagesRepository.setInternalMessagesAsRead() } returns Unit
        coEvery { buildConfigInformation.versionName() } returns (WELCOME_MESSAGE_VERSION)
        coEvery { internalMessagesRepository.getLastUpdatedVersion() } returns flowOf(WELCOME_MESSAGE_VERSION)
        coEvery { localMessageRepository.getChangeLogsAsInternalMessage() } returns emptyList()
        coEvery { internalMessagesRepository.getInternalMessages() } returns flowOf(emptyList())

        testScope.runTest {
            isLocalMessageControllerUnderTest.init()
            advanceUntilIdle()
            val messageList = isLocalMessageControllerUnderTest._localMessages.value
            assertNotNull(messageList)
            kotlin.test.assertEquals(true, messageList.isEmpty())
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `fetch inapp messages from local database return welcomeMessage`() {
        coEvery { internalMessagesRepository.setInternalMessagesAsRead() } returns Unit
        coEvery { buildConfigInformation.versionName() } returns (WELCOME_MESSAGE_VERSION)
        coEvery { internalMessagesRepository.getLastUpdatedVersion() } returns flowOf(WELCOME_MESSAGE_VERSION)
        coEvery { messageResources.messageFrom } returns (WELCOME_MESSAGE_ID)
        coEvery { messageResources.welcomeMessage } returns (WELCOME_MESSAGE_TEXT)
        coEvery { messageResources.welcomeMessageTag } returns (WELCOME_MESSAGE_TAG)
        coEvery { messageResources.getMessageTag(any()) } returns (WELCOME_MESSAGE_GET_MESSAGE_TAG)
        coEvery { internalMessagesRepository.getInternalMessages() } returns flowOf(listOf(welcomeMessage))
        coEvery { getInternalMessagesUseCase.invoke(WELCOME_MESSAGE_LANG) } returns flowOf(listOf(welcomeMessage.toInAppMessage()))
        coEvery { changeLogLocalDataSource.getInternalMessageInCurrentLanguage(welcomeMessage) } returns welcomeMessage
        coEvery { internalMessagesRepository.updateInternalMessage(welcomeMessage) } returns Unit

        testScope.runTest {
            isLocalMessageControllerUnderTest.init()
            advanceUntilIdle()
            val messageList = isLocalMessageControllerUnderTest.localMessages.value
            assertNotNull(messageList)
            assertEquals(listOf(welcomeMessage.toInAppMessage()), messageList)
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `consumeAllMessages updates communication statuses correctly`() {
        coEvery { pharmacyRepository.searchPharmacyByTelematikId(any()) } returns Result.success(
            FhirPharmacyErpModelCollection(PharmacyVzdService.FHIRVZD, 1, "", listOf(PHARMACY_DATA_FHIR))
        )
        every { communicationRepository.loadAllRepliedCommunications(listOf(TASK_ID)) } returns flowOf(listOf(COMMUNICATION_DATA_WITH_TASK_ID))
        every { communicationRepository.loadDispReqCommunications(ORDER_ID) } returns flowOf(listOf(COMMUNICATION_DATA_WITH_TASK_ID))
        every { communicationRepository.loadPharmacies() } returns flowOf(
            listOf(
                CachedPharmacy(
                    name = "Apotheke Adelheid Ulmendorfer TEST-ONLY",
                    telematikId = TELEMATIK_ID
                )
            )
        )
        coEvery { internalMessagesRepository.setInternalMessagesAsRead() } returns Unit
        every { communicationRepository.taskIdsByOrder(ORDER_ID) } returns flowOf(listOf(TASK_ID))
        every { communicationRepository.loadRepliedCommunications(any(), any()) } returns flowOf(listOf(COMMUNICATION_DATA_WITH_TASK_ID))
        every { communicationRepository.loadSyncedByTaskId(TASK_ID) } returns flowOf(MOCK_SYNCED_TASK_DATA_01_NEW)
        coEvery { communicationRepository.setCommunicationStatus(any(), any()) } just Runs
        coEvery { invoiceRepository.updateInvoiceCommunicationStatus(TASK_ID, true) } just Runs

        testScope.runTest {
            controllerUnderTest.init()
            advanceUntilIdle()
            controllerUnderTest.consumeAllMessages {}
            advanceUntilIdle()

            coVerify(exactly = 1) {
                updateCommunicationConsumedStatusUseCase(
                    CommunicationIdentifier.Communication(
                        COMMUNICATION_ID
                    )
                )
            }
            coVerify(exactly = 1) {
                updateCommunicationConsumedStatusUseCase(
                    CommunicationIdentifier.Order(
                        ORDER_ID
                    )
                )
            }
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `translateText sets progress and gives error on no consent`() = testScope.runTest {
        val communicationId = "comm-2"
        val inputText = "Hallo"

        coEvery { translationRepository.getTargetLanguageTag() } returns flowOf("en")
        coEvery { translationRepository.isTranslationAllowed() } returns flowOf(false)

        controllerUnderTest.translateText(communicationId, inputText) {
            fail("Success callback should not be called")
        }

        advanceUntilIdle()

        // translation progress should be cleared
        val inProgress = controllerUnderTest.translationInProgress.value
        assert(inProgress[communicationId] == false)
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `translateText sets progress and gives error on no target language`() = testScope.runTest {
        val communicationId = "comm-2"
        val inputText = "Hallo"

        coEvery { translationRepository.getTargetLanguageTag() } returns flowOf(null)
        coEvery { translationRepository.isTranslationAllowed() } returns flowOf(true)

        controllerUnderTest.translateText(communicationId, inputText) {
            fail("Success callback should not be called")
        }

        advanceUntilIdle()

        // translation progress should be cleared
        val inProgress = controllerUnderTest.translationInProgress.value
        assert(inProgress[communicationId] == false)
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `translateText sets in-progress flag on loading`() = testScope.runTest {
        val communicationId = "comm-3"
        val inputText = "Hallo"

        coEvery { translationRepository.getTargetLanguageTag() } returns flowOf("en")
        coEvery { translationRepository.isTranslationAllowed() } returns flowOf(true)

        controllerUnderTest.translateText(communicationId, inputText) {}

        advanceUntilIdle()

        val final = controllerUnderTest.translationInProgress.value
        assert(final[communicationId] == true)

        val inProgressDuring = controllerUnderTest.translationInProgress.value[communicationId]
        assertNotNull(inProgressDuring)
    }
}
