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

package de.gematik.ti.erp.app.order.messges.presentation

import android.content.Context
import app.cash.turbine.test
import de.gematik.ti.erp.app.analytics.tracker.Tracker
import de.gematik.ti.erp.app.info.BuildConfigInformation
import de.gematik.ti.erp.app.invoice.repository.InvoiceRepository
import de.gematik.ti.erp.app.messages.domain.model.InternalMessageResources
import de.gematik.ti.erp.app.messages.domain.repository.ChangeLogLocalDataSource
import de.gematik.ti.erp.app.messages.domain.usecase.GetInternalMessagesUseCase
import de.gematik.ti.erp.app.messages.domain.usecase.GetMessagesUseCase
import de.gematik.ti.erp.app.messages.presentation.MessageListController
import de.gematik.ti.erp.app.messages.repository.CommunicationRepository
import de.gematik.ti.erp.app.messages.repository.InternalMessagesRepository
import de.gematik.ti.erp.app.mocks.order.model.CACHED_PHARMACY
import de.gematik.ti.erp.app.mocks.order.model.COMMUNICATION_DATA
import de.gematik.ti.erp.app.mocks.order.model.IN_APP_MESSAGE_TEXT
import de.gematik.ti.erp.app.mocks.order.model.TASK_ID
import de.gematik.ti.erp.app.mocks.order.model.WELCOME_MESSAGE_VERSION
import de.gematik.ti.erp.app.mocks.order.model.welcomeMessage
import de.gematik.ti.erp.app.mocks.profile.api.API_MOCK_PROFILE
import de.gematik.ti.erp.app.pharmacy.repository.PharmacyRepository
import de.gematik.ti.erp.app.profiles.repository.ProfileRepository
import de.gematik.ti.erp.app.utils.uistate.UiState.Companion.isDataState
import de.gematik.ti.erp.app.utils.uistate.UiState.Companion.isEmptyState
import de.gematik.ti.erp.app.utils.uistate.UiState.Companion.isErrorState
import de.gematik.ti.erp.app.utils.uistate.UiState.Companion.isLoadingState
import io.mockk.MockKAnnotations
import io.mockk.Runs
import io.mockk.clearMocks
import io.mockk.coEvery
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
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
import org.junit.After
import org.junit.Before
import org.junit.Test
import kotlin.test.assertEquals

class MessageListControllerTest {
    private val communicationRepository: CommunicationRepository = mockk()
    private val profileRepository: ProfileRepository = mockk()
    private val invoiceRepository: InvoiceRepository = mockk()
    private val internalMessagesRepository: InternalMessagesRepository = mockk()
    private val pharmacyRepository: PharmacyRepository = mockk()
    private val changeLogLocalDataSource: ChangeLogLocalDataSource = mockk()

    private val messageResources: InternalMessageResources = mockk()
    private val buildConfigInformation: BuildConfigInformation = mockk()

    private val dispatcher = StandardTestDispatcher()
    private val testScope = TestScope(dispatcher)
    private val mockContext = mockk<Context>()

    private lateinit var controllerUnderTest: MessageListController

    private lateinit var getMessagesUseCase: GetMessagesUseCase
    private lateinit var getInternalMessagesUseCase: GetInternalMessagesUseCase
    private lateinit var tracker: Tracker

    @OptIn(ExperimentalCoroutinesApi::class)
    @Before
    fun setup() {
        Dispatchers.setMain(dispatcher)
        MockKAnnotations.init(this)
        getMessagesUseCase = mockk()
        getInternalMessagesUseCase = mockk()
        tracker = mockk()

        coEvery {
            profileRepository.profiles()
        } returns flowOf(listOf(API_MOCK_PROFILE, API_MOCK_PROFILE.copy(id = "2", active = false)))
        coEvery { communicationRepository.loadPharmacies() } returns flowOf(listOf(CACHED_PHARMACY))
        coEvery { communicationRepository.hasUnreadDispenseMessage(any(), any()) } returns flowOf(true)
        coEvery { communicationRepository.hasUnreadRepliedMessages(any(), any()) } returns flowOf(true)
        coEvery { communicationRepository.loadSyncedByTaskId(any()) } returns flowOf(null)
        coEvery { communicationRepository.loadScannedByTaskId(any()) } returns flowOf(null)
        coEvery { communicationRepository.taskIdsByOrder(any()) } returns flowOf(listOf(TASK_ID))
        coEvery { communicationRepository.loadDispReqCommunicationsByProfileId(any()) } returns flowOf(listOf(COMMUNICATION_DATA))
        coEvery { communicationRepository.loadRepliedCommunications(any(), any()) } returns flowOf(emptyList())
        coEvery { communicationRepository.loadDispReqCommunications(any()) } returns flowOf(emptyList())
        coEvery { internalMessagesRepository.getInternalMessages() } returns flowOf(listOf(welcomeMessage))
        coEvery { invoiceRepository.hasUnreadInvoiceMessages(any()) } returns flowOf(false)
        coEvery { pharmacyRepository.savePharmacyToCache(any()) } returns Unit
        every { pharmacyRepository.loadCachedPharmacies() } returns flowOf(emptyList())
        every { mockContext.getString(any()) } returns IN_APP_MESSAGE_TEXT
        every { mockContext.resources.configuration.locales[0].language } returns "de"
        coEvery { tracker.trackEvent(any()) } just Runs

        getMessagesUseCase = GetMessagesUseCase(communicationRepository, invoiceRepository, profileRepository, pharmacyRepository, dispatcher)
        getInternalMessagesUseCase = GetInternalMessagesUseCase(
            internalMessagesRepository,
            changeLogLocalDataSource
        )

        controllerUnderTest =
            MessageListController(
                getMessagesUseCase = getMessagesUseCase,
                getInternalMessagesUseCase = getInternalMessagesUseCase,
                context = mockContext,
                tracker = tracker
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
            buildConfigInformation
        )
    }

    @Test
    fun `start the controller on loading state`() {
        coEvery { internalMessagesRepository.getLastUpdatedVersion() } returns flowOf(WELCOME_MESSAGE_VERSION)
        coEvery { buildConfigInformation.versionName() } returns (WELCOME_MESSAGE_VERSION)
        coEvery { changeLogLocalDataSource.getInternalMessageInCurrentLanguage(welcomeMessage) } returns welcomeMessage
        coEvery { internalMessagesRepository.updateInternalMessage(welcomeMessage) } returns Unit

        testScope.runTest {
            val orders = controllerUnderTest.messagesList.first()
            // orders check
            assert(orders.isLoadingState)
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `show error state on exception`() {
        coEvery { internalMessagesRepository.getLastUpdatedVersion() } returns flowOf(WELCOME_MESSAGE_VERSION)
        coEvery { buildConfigInformation.versionName() } returns (WELCOME_MESSAGE_VERSION)
        coEvery { communicationRepository.loadScannedByTaskId(any()) } throws Exception("Test exception")
        testScope.runTest {
            advanceUntilIdle()
            val orders = controllerUnderTest.messagesList.first()
            // orders check
            assert(orders.isErrorState)
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `show empty state on no communications`() {
        coEvery { internalMessagesRepository.getLastUpdatedVersion() } returns flowOf(WELCOME_MESSAGE_VERSION)
        coEvery { buildConfigInformation.versionName() } returns (WELCOME_MESSAGE_VERSION)
        coEvery { internalMessagesRepository.getInternalMessages() } returns flowOf(emptyList())
        coEvery { communicationRepository.loadDispReqCommunicationsByProfileId(any()) } returns flowOf(emptyList())
        testScope.runTest {
            controllerUnderTest.messagesList.test {
                advanceUntilIdle()
                val orders = awaitItem()
                assertEquals(true, orders.isLoadingState)
                val ordersLoaded = awaitItem()
                assertEquals(true, ordersLoaded.isEmptyState)
            }
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `show empty state on no profiles`() {
        coEvery { internalMessagesRepository.getLastUpdatedVersion() } returns flowOf(WELCOME_MESSAGE_VERSION)
        coEvery { buildConfigInformation.versionName() } returns (WELCOME_MESSAGE_VERSION)
        coEvery { internalMessagesRepository.getInternalMessages() } returns flowOf(emptyList())
        coEvery { profileRepository.profiles() } returns flowOf(emptyList())
        testScope.runTest {
            advanceUntilIdle()
            val orders = controllerUnderTest.messagesList.first()
            // orders check
            assertEquals(true, orders.isEmptyState)
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `show data state on no task-ids`() {
        coEvery { communicationRepository.taskIdsByOrder(any()) } returns flowOf(emptyList())
        coEvery { internalMessagesRepository.getInternalMessages() } returns flowOf(listOf(welcomeMessage))
        coEvery { changeLogLocalDataSource.getInternalMessageInCurrentLanguage(welcomeMessage) } returns welcomeMessage
        coEvery { internalMessagesRepository.updateInternalMessage(welcomeMessage) } returns Unit

        testScope.runTest {
            advanceUntilIdle()
            val orders = controllerUnderTest.messagesList.first()
            // orders check
            assertEquals(true, orders.isDataState)
        }
    }
}
