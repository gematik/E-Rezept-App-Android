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

package de.gematik.ti.erp.app.order.messges.presentation

import android.content.Context
import app.cash.turbine.test
import de.gematik.ti.erp.app.analytics.tracker.Tracker
import de.gematik.ti.erp.app.changelogs.InAppMessageRepository
import de.gematik.ti.erp.app.featuretoggle.model.NewFeature
import de.gematik.ti.erp.app.featuretoggle.repository.NewFeaturesRepository
import de.gematik.ti.erp.app.featuretoggle.usecase.IsNewFeatureSeenUseCase
import de.gematik.ti.erp.app.featuretoggle.usecase.MarkNewFeatureSeenUseCase
import de.gematik.ti.erp.app.info.BuildConfigInformation
import de.gematik.ti.erp.app.invoice.repository.InvoiceRepository
import de.gematik.ti.erp.app.messages.domain.model.InAppMessage
import de.gematik.ti.erp.app.messages.domain.model.InAppMessageResources
import de.gematik.ti.erp.app.messages.domain.repository.InAppLocalMessageRepository
import de.gematik.ti.erp.app.messages.domain.usecase.FetchInAppMessageUseCase
import de.gematik.ti.erp.app.messages.domain.usecase.FetchWelcomeMessageUseCase
import de.gematik.ti.erp.app.messages.domain.usecase.GetMessagesUseCase
import de.gematik.ti.erp.app.messages.presentation.MessageListController
import de.gematik.ti.erp.app.messages.repository.CommunicationRepository
import de.gematik.ti.erp.app.mocks.order.model.CACHED_PHARMACY
import de.gematik.ti.erp.app.mocks.order.model.COMMUNICATION_DATA
import de.gematik.ti.erp.app.mocks.order.model.IN_APP_MESSAGE_TEXT
import de.gematik.ti.erp.app.mocks.order.model.TASK_ID
import de.gematik.ti.erp.app.mocks.order.model.WELCOME_MESSAGE_FROM
import de.gematik.ti.erp.app.mocks.order.model.WELCOME_MESSAGE_TAG
import de.gematik.ti.erp.app.mocks.order.model.WELCOME_MESSAGE_TEXT
import de.gematik.ti.erp.app.mocks.order.model.WELCOME_MESSAGE_TIMESTAMP
import de.gematik.ti.erp.app.mocks.order.model.WELCOME_MESSAGE_VERSION
import de.gematik.ti.erp.app.mocks.order.model.inAppMessages
import de.gematik.ti.erp.app.mocks.order.model.inAppMessagesFiltered
import de.gematik.ti.erp.app.mocks.order.model.internalEntity
import de.gematik.ti.erp.app.mocks.order.model.welcomeMessage
import de.gematik.ti.erp.app.mocks.profile.api.API_MOCK_PROFILE
import de.gematik.ti.erp.app.profiles.repository.ProfileRepository
import de.gematik.ti.erp.app.profiles.usecase.GetProfilesUseCase
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
    private val inAppMessageRepository: InAppMessageRepository = mockk()

    private val messageResources: InAppMessageResources = mockk()
    private val localMessageRepository: InAppLocalMessageRepository = mockk()
    private val buildConfigInformation: BuildConfigInformation = mockk()
    private val newFeatureRepository: NewFeaturesRepository = mockk()

    private val dispatcher = StandardTestDispatcher()
    private val testScope = TestScope(dispatcher)
    private val mockContext = mockk<Context>()

    private lateinit var controllerUnderTest: MessageListController

    private lateinit var getMessagesUseCase: GetMessagesUseCase
    private lateinit var isNewFeatureSeenUseCase: IsNewFeatureSeenUseCase
    private lateinit var markNewFeatureSeenUseCase: MarkNewFeatureSeenUseCase
    private lateinit var getProfilesUseCase: GetProfilesUseCase
    private lateinit var fetchInAppMessageUseCase: FetchInAppMessageUseCase
    private lateinit var fetchWelcomeMessageUseCase: FetchWelcomeMessageUseCase
    private lateinit var tracker: Tracker

    @OptIn(ExperimentalCoroutinesApi::class)
    @Before
    fun setup() {
        Dispatchers.setMain(dispatcher)
        MockKAnnotations.init(this)
        getMessagesUseCase = mockk()
        getProfilesUseCase = mockk()
        fetchInAppMessageUseCase = mockk()
        isNewFeatureSeenUseCase = mockk()
        tracker = mockk()
        markNewFeatureSeenUseCase = spyk(MarkNewFeatureSeenUseCase(newFeatureRepository, dispatcher))

        coEvery {
            profileRepository.profiles()
        } returns flowOf(listOf(API_MOCK_PROFILE, API_MOCK_PROFILE.copy(id = "2", active = false)))
        coEvery { communicationRepository.loadPharmacies() } returns flowOf(listOf(CACHED_PHARMACY))
        coEvery { communicationRepository.hasUnreadDispenseMessage(any(), any()) } returns flowOf(true)
        coEvery { communicationRepository.hasUnreadRepliedMessages(any(), any()) } returns flowOf(true)
        coEvery { communicationRepository.downloadMissingPharmacy(any()) } returns Result.success(null)
        coEvery { communicationRepository.loadSyncedByTaskId(any()) } returns flowOf(null)
        coEvery { communicationRepository.loadScannedByTaskId(any()) } returns flowOf(null)
        coEvery { communicationRepository.taskIdsByOrder(any()) } returns flowOf(listOf(TASK_ID))
        coEvery { communicationRepository.loadDispReqCommunicationsByProfileId(any()) } returns flowOf(listOf(COMMUNICATION_DATA))
        coEvery { communicationRepository.loadRepliedCommunications(any(), any()) } returns flowOf(emptyList())
        coEvery { communicationRepository.loadDispReqCommunications(any()) } returns flowOf(emptyList())
        coEvery { inAppMessageRepository.inAppMessages } returns flowOf(internalEntity)
        coEvery { localMessageRepository.getInternalMessages() } returns flowOf(inAppMessages)
        coEvery { newFeatureRepository.setDefaults() } returns Unit
        coEvery { newFeatureRepository.isNewFeatureSeen(NewFeature.ORDERS_SCREEN_NO_PROFILE_BAR) } returns false
        coEvery { newFeatureRepository.markFeatureSeen(NewFeature.ORDERS_SCREEN_NO_PROFILE_BAR) } returns Unit
        coEvery { invoiceRepository.hasUnreadInvoiceMessages(any()) } returns flowOf(false)
        every { mockContext.getString(any()) } returns IN_APP_MESSAGE_TEXT
        coEvery { tracker.trackEvent(any()) } just Runs

        getMessagesUseCase = GetMessagesUseCase(communicationRepository, invoiceRepository, profileRepository, dispatcher)
        getProfilesUseCase = GetProfilesUseCase(profileRepository, dispatcher)
        isNewFeatureSeenUseCase = IsNewFeatureSeenUseCase(newFeatureRepository, dispatcher)
        markNewFeatureSeenUseCase = spyk(MarkNewFeatureSeenUseCase(newFeatureRepository, dispatcher))
        fetchInAppMessageUseCase = spyk(
            FetchInAppMessageUseCase(
                inAppMessageRepository,
                localMessageRepository,
                messageResources,
                buildConfigInformation
            )
        )
        fetchWelcomeMessageUseCase = spyk(FetchWelcomeMessageUseCase(inAppMessageRepository, buildConfigInformation, messageResources))
        controllerUnderTest =
            MessageListController(
                getMessagesUseCase = getMessagesUseCase,
                getProfilesUseCase = getProfilesUseCase,
                fetchInAppMessageUseCase = fetchInAppMessageUseCase,
                fetchWelcomeMessageUseCase = fetchWelcomeMessageUseCase,
                isNewFeatureSeenUseCase = isNewFeatureSeenUseCase,
                markNewFeatureSeenUseCase = markNewFeatureSeenUseCase,
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
            inAppMessageRepository,
            messageResources,
            localMessageRepository,
            buildConfigInformation,
            newFeatureRepository
        )
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `fetch orders from local database and check if the new feature was seen on init`() {
        coEvery { inAppMessageRepository.showWelcomeMessage } returns flowOf(value = false)
        coEvery { inAppMessageRepository.lastVersion } returns flowOf(null)
        coEvery { buildConfigInformation.versionName() } returns ("")
        coEvery { communicationRepository.taskIdsByOrder(any()) } returns flowOf(listOf(TASK_ID))
        coEvery { messageResources.messageFrom } returns (WELCOME_MESSAGE_FROM)
        coEvery { messageResources.welcomeMessage } returns (WELCOME_MESSAGE_TEXT)
        coEvery { messageResources.welcomeMessageTag } returns (WELCOME_MESSAGE_TAG)
        coEvery { messageResources.getMessageTag(any()) } returns (WELCOME_MESSAGE_TAG)
        coEvery { buildConfigInformation.versionName() } returns (WELCOME_MESSAGE_VERSION)
        coEvery { fetchWelcomeMessageUseCase.getCurrentTimeAsString() } returns (WELCOME_MESSAGE_TIMESTAMP)

        testScope.runTest {
            advanceUntilIdle()
            val orders = controllerUnderTest.messagesList.first()

            // orders check
            val data: List<InAppMessage>? = orders.data
            assertEquals(inAppMessagesFiltered, data)
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `do not show the feature change bar if only one profile is present`() {
        coEvery { inAppMessageRepository.showWelcomeMessage } returns flowOf(false)
        coEvery { inAppMessageRepository.lastVersion } returns flowOf(WELCOME_MESSAGE_VERSION)
        coEvery { buildConfigInformation.versionName() } returns (WELCOME_MESSAGE_VERSION)
        coEvery { profileRepository.profiles() } returns flowOf(listOf(API_MOCK_PROFILE))
        testScope.runTest {
            advanceUntilIdle()
            val isOrderFeatureChangeSeen = controllerUnderTest.isMessagesListFeatureChangeSeen.first()

            // if more than one profile is present, only then the actual value is used
            assertEquals(true, isOrderFeatureChangeSeen)
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `show the feature change bar if more than one profile is present`() {
        coEvery { inAppMessageRepository.showWelcomeMessage } returns flowOf(false)
        coEvery { inAppMessageRepository.lastVersion } returns flowOf(WELCOME_MESSAGE_VERSION)
        coEvery { buildConfigInformation.versionName() } returns (WELCOME_MESSAGE_VERSION)
        coEvery { profileRepository.profiles() } returns flowOf(listOf(API_MOCK_PROFILE, API_MOCK_PROFILE.copy(id = "2", active = false)))
        testScope.runTest {
            advanceUntilIdle()
            controllerUnderTest.isMessagesListFeatureChangeSeen.test {
                val isOrderFeatureChangeSeen = awaitItem()
                // if more than one profile is present, only then the actual value is used
                assertEquals(false, isOrderFeatureChangeSeen)
            }
        }
    }

    @Test
    fun `start the controller on loading state`() {
        coEvery { inAppMessageRepository.showWelcomeMessage } returns flowOf(false)
        coEvery { inAppMessageRepository.lastVersion } returns flowOf(WELCOME_MESSAGE_VERSION)
        coEvery { buildConfigInformation.versionName() } returns (WELCOME_MESSAGE_VERSION)
        testScope.runTest {
            val orders = controllerUnderTest.messagesList.first()
            // orders check
            assert(orders.isLoadingState)
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `show error state on exception`() {
        coEvery { inAppMessageRepository.showWelcomeMessage } returns flowOf(false)
        coEvery { inAppMessageRepository.lastVersion } returns flowOf(WELCOME_MESSAGE_VERSION)
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
        coEvery { inAppMessageRepository.showWelcomeMessage } returns flowOf(false)
        coEvery { inAppMessageRepository.lastVersion } returns flowOf(WELCOME_MESSAGE_VERSION)
        coEvery { buildConfigInformation.versionName() } returns (WELCOME_MESSAGE_VERSION)
        coEvery { inAppMessageRepository.inAppMessages } returns flowOf(emptyList())
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
        coEvery { inAppMessageRepository.showWelcomeMessage } returns flowOf(false)
        coEvery { inAppMessageRepository.lastVersion } returns flowOf(WELCOME_MESSAGE_VERSION)
        coEvery { buildConfigInformation.versionName() } returns (WELCOME_MESSAGE_VERSION)
        coEvery { inAppMessageRepository.inAppMessages } returns flowOf(emptyList())
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
        coEvery { inAppMessageRepository.showWelcomeMessage } returns flowOf(false)
        coEvery { communicationRepository.taskIdsByOrder(any()) } returns flowOf(emptyList())
        coEvery { fetchWelcomeMessageUseCase.invoke() } returns flowOf(welcomeMessage)
        coEvery { fetchInAppMessageUseCase.invoke() } returns flowOf(inAppMessages)
        testScope.runTest {
            advanceUntilIdle()
            val orders = controllerUnderTest.messagesList.first()
            // orders check
            assertEquals(true, orders.isDataState)
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `check that the repository call is made on user clicking on the new feature label`() {
        coEvery { inAppMessageRepository.showWelcomeMessage } returns flowOf(false)
        coEvery { inAppMessageRepository.lastVersion } returns flowOf(WELCOME_MESSAGE_VERSION)
        coEvery { buildConfigInformation.versionName() } returns (WELCOME_MESSAGE_VERSION)
        testScope.runTest {
            advanceUntilIdle()
            controllerUnderTest.markProfileTopBarRemovedChangeSeen()
        }
        coVerify(exactly = 1) { markNewFeatureSeenUseCase.invoke(NewFeature.ORDERS_SCREEN_NO_PROFILE_BAR) }
        coVerify(exactly = 1) { newFeatureRepository.markFeatureSeen(NewFeature.ORDERS_SCREEN_NO_PROFILE_BAR) }
    }
}
