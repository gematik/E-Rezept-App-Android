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

package de.gematik.ti.erp.app.messages.presentation

import android.app.Application
import de.gematik.ti.erp.app.base.NetworkStatusTracker
import de.gematik.ti.erp.app.fhir.FhirPharmacyErpModelCollection
import de.gematik.ti.erp.app.fhir.pharmacy.type.PharmacyVzdService
import de.gematik.ti.erp.app.info.BuildConfigInformation
import de.gematik.ti.erp.app.invoice.repository.InvoiceRepository
import de.gematik.ti.erp.app.messages.domain.usecase.GetDispenseMessagesByTaskIdUseCase
import de.gematik.ti.erp.app.messages.domain.usecase.GetProfileByOrderIdUseCase
import de.gematik.ti.erp.app.messages.domain.usecase.GetProfileByTaskIdUseCase
import de.gematik.ti.erp.app.messages.domain.usecase.GetReplyMessagesByTaskIdUseCase
import de.gematik.ti.erp.app.messages.domain.usecase.UpdateCommunicationConsumedStatusUseCase
import de.gematik.ti.erp.app.messages.domain.usecase.UpdateInvoicesByTaskIdUseCase
import de.gematik.ti.erp.app.messages.repository.CommunicationRepository
import de.gematik.ti.erp.app.mocks.order.model.COMMUNICATION_DATA_WITH_TASK_ID_ERP
import de.gematik.ti.erp.app.mocks.order.model.COMMUNICATION_ID
import de.gematik.ti.erp.app.mocks.order.model.IN_APP_MESSAGE_TEXT
import de.gematik.ti.erp.app.mocks.order.model.MOCK_MESSAGE
import de.gematik.ti.erp.app.mocks.order.model.MOCK_PROFILE
import de.gematik.ti.erp.app.mocks.order.model.MOCK_SYNCED_TASK_DATA_01_NEW
import de.gematik.ti.erp.app.mocks.order.model.ORDER_ID
import de.gematik.ti.erp.app.mocks.order.model.TASK_ID
import de.gematik.ti.erp.app.mocks.order.model.WELCOME_MESSAGE_TIMESTAMP
import de.gematik.ti.erp.app.mocks.pharmacy.model.PHARMACY_DATA_FHIR
import de.gematik.ti.erp.app.pharmacy.repository.PharmacyRepository
import de.gematik.ti.erp.app.pharmacy.usecase.GetPharmacyByTelematikIdUseCase
import de.gematik.ti.erp.app.profiles.repository.ProfileRepository
import de.gematik.ti.erp.app.translation.domain.model.LanguageDownloadState
import de.gematik.ti.erp.app.translation.repository.TranslationModelManager
import de.gematik.ti.erp.app.translation.repository.TranslationRepository
import de.gematik.ti.erp.app.translation.usecase.DownloadLanguageModelUseCase
import de.gematik.ti.erp.app.translation.usecase.GetTranslationConsentUseCase
import de.gematik.ti.erp.app.translation.usecase.IsTargetLanguageSetUseCase
import de.gematik.ti.erp.app.translation.usecase.ToggleTranslationConsentUseCase
import de.gematik.ti.erp.app.translation.usecase.TranslateTextUseCase
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
import junit.framework.TestCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestCoroutineScheduler
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
import kotlin.collections.emptyList
import kotlin.test.assertNotNull
import kotlin.test.fail

class UnknownOrderMessageDetailControllerTest {
    private val profileRepository: ProfileRepository = mockk()
    private val communicationRepository: CommunicationRepository = mockk()
    private val invoiceRepository: InvoiceRepository = mockk()
    private val buildConfigInformation: BuildConfigInformation = mockk()
    private val pharmacyRepository: PharmacyRepository = mockk()
    private val translationRepository: TranslationRepository = mockk()
    private val testScheduler = TestCoroutineScheduler()
    private val dispatcher = StandardTestDispatcher(testScheduler)
    private val testScope = TestScope(dispatcher)
    private val mockContext = mockk<Application>()

    private lateinit var controllerUnderTest: UnknownOrderMessageDetailController
    private val clock = mockk<Clock>()

    private lateinit var getProfileByTaskIdUseCase: GetProfileByTaskIdUseCase
    private lateinit var getReplyMessagesByTaskIdUseCase: GetReplyMessagesByTaskIdUseCase
    private lateinit var getDispenseMessagesByTaskIdUseCase: GetDispenseMessagesByTaskIdUseCase
    private lateinit var updateCommunicationConsumedStatusUseCase: UpdateCommunicationConsumedStatusUseCase
    private lateinit var updateInvoicesByTaskIdUseCase: UpdateInvoicesByTaskIdUseCase
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

        getProfileByTaskIdUseCase = GetProfileByTaskIdUseCase(
            communicationRepository = communicationRepository,
            dispatcher = dispatcher
        )

        getReplyMessagesByTaskIdUseCase = spyk(
            GetReplyMessagesByTaskIdUseCase(
                communicationRepository = communicationRepository,
                dispatcher = dispatcher
            )
        )
        getDispenseMessagesByTaskIdUseCase = spyk(
            GetDispenseMessagesByTaskIdUseCase(
                communicationRepository = communicationRepository,
                invoiceRepository = invoiceRepository,
                pharmacyRepository = pharmacyRepository,
                dispatcher = dispatcher
            )
        )
        updateCommunicationConsumedStatusUseCase = spyk(
            UpdateCommunicationConsumedStatusUseCase(
                repository = communicationRepository,
                dispatcher = dispatcher
            )
        )
        updateInvoicesByTaskIdUseCase = UpdateInvoicesByTaskIdUseCase(
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

        translateTextUseCase = TranslateTextUseCase(
            modelManager = mockk<TranslationModelManager>(relaxed = true),
            repository = translationRepository,
            networkStatusTracker = mockk<NetworkStatusTracker>(relaxed = true)
        )

        getTranslationConsentUseCase =
            GetTranslationConsentUseCase(repository = translationRepository)

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
        every { communicationRepository.hasUnreadDispenseMessage(any(), any()) } returns flowOf(
            false
        )
        every { communicationRepository.loadDispReqCommunicationsByTaskId(any()) } returns flowOf(emptyList())
        every { communicationRepository.loadAllRepliedCommunications(any()) } returns flowOf(emptyList())
        every { communicationRepository.loadSyncedByTaskId(any()) } returns flowOf(null)
        every { communicationRepository.loadScannedByTaskId(any()) } returns flowOf(null)
        every { invoiceRepository.invoiceByTaskId(any()) } returns flowOf(null)
        every { invoiceRepository.hasUnreadInvoiceMessages(any()) } returns flowOf(false)
        coEvery { communicationRepository.profileByTaskId(any()) } returns flowOf(MOCK_PROFILE)
        coEvery { pharmacyRepository.findLocalPharmacyByTelematikId(any()) } returns null
        coEvery { pharmacyRepository.searchPharmacyByTelematikId(any()) } returns Result.failure(Exception())
        coEvery { communicationRepository.updatePharmacyName(any(), any()) } returns Unit
        coEvery { translationRepository.getTargetLanguageTag() } returns flowOf("de")
        coEvery { translationRepository.isTranslationAllowed() } returns MutableStateFlow(true)
        coEvery { translationRepository.enableConsentForLocalTranslation() } returns Unit
        coEvery { translationRepository.disableConsentForLocalTranslation() } returns Unit
        coEvery { translationRepository.clearTargetLanguageTag() } returns Unit
        coEvery { translationRepository.setTargetLanguageTag(any()) } returns Unit
        coEvery { translationRepository.downloadLanguageModels(any()) } returns flowOf(
            LanguageDownloadState.Completed
        )

        controllerUnderTest = UnknownOrderMessageDetailController(
            application = mockContext,
            taskId = TASK_ID,
            updateCommunicationConsumedStatusUseCase = updateCommunicationConsumedStatusUseCase,
            getPharmacyByTelematikIdUseCase = getPharmacyByTelematikIdUseCase,
            getTranslationConsentUseCase = getTranslationConsentUseCase,
            isTargetLanguageSetUseCase = isTargetLanguageSetUseCase,
            translateTextUseCase = translateTextUseCase,
            toggleTranslationConsentUseCase = toggleTranslationConsentUseCase,
            downloadedLanguagesUseCase = downloadLanguageModelUseCase,
            getReplyMessagesByTaskIdUseCase = getReplyMessagesByTaskIdUseCase,
            getDispenseMessagesByTaskIdUseCase = getDispenseMessagesByTaskIdUseCase,
            updateInvoicesByTaskIdUseCase = updateInvoicesByTaskIdUseCase,
            getProfileByTaskIdUseCase = getProfileByTaskIdUseCase
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
            buildConfigInformation,
            pharmacyRepository
        )
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `when taskId is not provided`() {
        every { getReplyMessagesByTaskIdUseCase(any()) } returns kotlinx.coroutines.flow.MutableSharedFlow()
        every { getDispenseMessagesByTaskIdUseCase(any()) } throws IllegalArgumentException("taskId is blank")

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
    fun `when taskId is provided and returns data successfully`() {
        val expectedMessages = listOf(MOCK_MESSAGE)
        coEvery {
            pharmacyRepository.searchPharmacyByTelematikId(any())
        } throws IllegalArgumentException("Pharmacy error")
        every {
            communicationRepository.loadAllRepliedCommunications(
                listOf(TASK_ID)
            )
        } returns flowOf(listOf(COMMUNICATION_DATA_WITH_TASK_ID_ERP.copy(pharmacyName = "Apotheke Adelheid Ulmendorfer TEST-ONLY")))
        every { communicationRepository.loadDispReqCommunicationsByTaskId(TASK_ID) } returns flowOf(
            emptyList()
        )
        every { communicationRepository.loadSyncedByTaskId(TASK_ID) } returns flowOf(
            MOCK_SYNCED_TASK_DATA_01_NEW
        )

        testScope.runTest {
            controllerUnderTest.init()
            advanceUntilIdle()
            val messagesResult = controllerUnderTest.messages.first()
            val orderStateResult = controllerUnderTest.order.first()
            val pharmacyStateResult = controllerUnderTest.pharmacy.first()
            val actualMessages = messagesResult.data
            assertNotNull(actualMessages)
            TestCase.assertEquals(expectedMessages.size, actualMessages.size)
            TestCase.assertEquals(expectedMessages.first().communicationId, actualMessages.first().communicationId)
            assert(orderStateResult.isEmptyState)
            assert(pharmacyStateResult.isLoadingState)
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `when taskId is provided but messages and order data are not found`() {
        every { getReplyMessagesByTaskIdUseCase(TASK_ID) } returns flowOf(emptyList())
        every { getDispenseMessagesByTaskIdUseCase(TASK_ID) } returns flowOf(null)
        coEvery { pharmacyRepository.searchPharmacyByTelematikId(any()) } returns Result.success(
            FhirPharmacyErpModelCollection(
                PharmacyVzdService.FHIRVZD,
                0,
                "",
                emptyList()
            )
        )
        every { communicationRepository.taskIdsByOrder(ORDER_ID) } returns flowOf(emptyList())
        every { communicationRepository.loadRepliedCommunications(any<String>(), any<String>()) } returns flowOf(
            emptyList()
        )
        every { communicationRepository.loadDispReqCommunications(ORDER_ID) } returns flowOf(
            emptyList()
        )
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
    fun `when taskId is provided and use cases throw errors`() {
        every { getReplyMessagesByTaskIdUseCase(TASK_ID) } throws IllegalArgumentException("Messages error")
        every { getDispenseMessagesByTaskIdUseCase(TASK_ID) } throws IllegalArgumentException("Order error")
        coEvery {
            pharmacyRepository.searchPharmacyByTelematikId(any())
        } throws IllegalArgumentException("Pharmacy error")

        testScope.runTest {
            controllerUnderTest.init()
            advanceUntilIdle()
            val orderStateResult = controllerUnderTest.order.first()
            val messageStateResult = controllerUnderTest.messages.first()
            val pharmacyStateResult = controllerUnderTest.pharmacy.first()

            assert(orderStateResult.isErrorState)
            assert(messageStateResult.isErrorState)
            assert(pharmacyStateResult.isErrorState)
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `consumeAllMessages updates communication statuses correctly`() {
        coEvery { pharmacyRepository.searchPharmacyByTelematikId(any()) } returns Result.success(
            FhirPharmacyErpModelCollection(
                PharmacyVzdService.FHIRVZD,
                1,
                "",
                listOf(PHARMACY_DATA_FHIR)
            )
        )
        every { communicationRepository.loadAllRepliedCommunications(listOf(TASK_ID)) } returns flowOf(
            listOf(COMMUNICATION_DATA_WITH_TASK_ID_ERP)
        )
        every { communicationRepository.loadDispReqCommunicationsByTaskId(TASK_ID) } returns flowOf(
            listOf(
                COMMUNICATION_DATA_WITH_TASK_ID_ERP
            )
        )
        every { communicationRepository.loadDispReqCommunications(ORDER_ID) } returns flowOf(
            listOf(
                COMMUNICATION_DATA_WITH_TASK_ID_ERP
            )
        )
        every { communicationRepository.taskIdsByOrder(ORDER_ID) } returns flowOf(listOf(TASK_ID))
        every { communicationRepository.loadRepliedCommunications(any<String>(), any<String>()) } returns flowOf(
            listOf(COMMUNICATION_DATA_WITH_TASK_ID_ERP)
        )
        every { communicationRepository.loadSyncedByTaskId(TASK_ID) } returns flowOf(
            MOCK_SYNCED_TASK_DATA_01_NEW
        )
        coEvery { communicationRepository.setCommunicationStatus(any(), any()) } just Runs
        coEvery { invoiceRepository.updateInvoiceCommunicationStatus(any(), any()) } just Runs

        testScope.runTest {
            controllerUnderTest.init()
            advanceUntilIdle()
            controllerUnderTest.consumeAllMessages {}
            advanceUntilIdle()

            coVerify(exactly = 1) {
                updateCommunicationConsumedStatusUseCase(
                    UpdateCommunicationConsumedStatusUseCase.Companion.CommunicationIdentifier.Communication(
                        COMMUNICATION_ID
                    )
                )
            }
            coVerify(exactly = 1) {
                updateCommunicationConsumedStatusUseCase(
                    UpdateCommunicationConsumedStatusUseCase.Companion.CommunicationIdentifier.Order(
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
        assert(final[communicationId] == false)

        advanceUntilIdle()

        val inProgressDuring = controllerUnderTest.translationInProgress.value[communicationId]
        assertNotNull(inProgressDuring)
    }
}
