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
import de.gematik.ti.erp.app.info.BuildConfigInformation
import de.gematik.ti.erp.app.messages.domain.model.InternalMessageResources
import de.gematik.ti.erp.app.messages.domain.repository.ChangeLogLocalDataSource
import de.gematik.ti.erp.app.messages.domain.usecase.GetInternalMessagesUseCase
import de.gematik.ti.erp.app.messages.domain.usecase.SetInternalMessageAsReadUseCase
import de.gematik.ti.erp.app.messages.mapper.toInAppMessage
import de.gematik.ti.erp.app.messages.repository.InternalMessagesRepository
import de.gematik.ti.erp.app.mocks.order.model.IN_APP_MESSAGE_TEXT
import de.gematik.ti.erp.app.mocks.order.model.WELCOME_MESSAGE_GET_MESSAGE_TAG
import de.gematik.ti.erp.app.mocks.order.model.WELCOME_MESSAGE_ID
import de.gematik.ti.erp.app.mocks.order.model.WELCOME_MESSAGE_LANG
import de.gematik.ti.erp.app.mocks.order.model.WELCOME_MESSAGE_TAG
import de.gematik.ti.erp.app.mocks.order.model.WELCOME_MESSAGE_TEXT
import de.gematik.ti.erp.app.mocks.order.model.WELCOME_MESSAGE_TIMESTAMP
import de.gematik.ti.erp.app.mocks.order.model.WELCOME_MESSAGE_VERSION
import de.gematik.ti.erp.app.mocks.order.model.welcomeMessage
import de.gematik.ti.erp.app.profiles.repository.ProfileRepository
import de.gematik.ti.erp.app.profiles.usecase.GetActiveProfileUseCase
import io.mockk.MockKAnnotations
import io.mockk.clearMocks
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import io.mockk.spyk
import junit.framework.TestCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
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
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class InternalMessageDetailControllerTest {
    private val profileRepository: ProfileRepository = mockk()

    private val internalMessagesRepository: InternalMessagesRepository = mockk()
    private val messageResources: InternalMessageResources = mockk()
    private val buildConfigInformation: BuildConfigInformation = mockk()
    private val changeLogLocalDataSource: ChangeLogLocalDataSource = mockk()
    private val testScheduler = TestCoroutineScheduler()
    private val dispatcher = StandardTestDispatcher(testScheduler)
    private val testScope = TestScope(dispatcher)
    private val mockContext = mockk<Application>()
    private lateinit var isLocalMessageControllerUnderTest: InternalMessageDetailScreenController
    private val clock = mockk<Clock>()

    private lateinit var getActiveProfileUseCase: GetActiveProfileUseCase
    private lateinit var getInternalMessagesUseCase: GetInternalMessagesUseCase
    private lateinit var setInternalMessageAsReadUseCase: SetInternalMessageAsReadUseCase

    @OptIn(ExperimentalCoroutinesApi::class)
    @Before
    fun setUp() {
        Dispatchers.setMain(dispatcher)
        MockKAnnotations.init(this)

        getActiveProfileUseCase = GetActiveProfileUseCase(
            repository = profileRepository,
            dispatcher = dispatcher
        )
        setInternalMessageAsReadUseCase = SetInternalMessageAsReadUseCase(
            internalMessagesRepository = internalMessagesRepository

        )
        getInternalMessagesUseCase = GetInternalMessagesUseCase(
            internalMessagesRepository = internalMessagesRepository,
            changeLogLocalDataSource = changeLogLocalDataSource,
            dispatcher = dispatcher
        )
        getInternalMessagesUseCase =
            spyk(GetInternalMessagesUseCase(internalMessagesRepository, changeLogLocalDataSource, dispatcher))

        every { mockContext.getString(any()) } returns IN_APP_MESSAGE_TEXT
        every { mockContext.resources.configuration.locales[0].language } returns "de"
        every { clock.now() } returns Instant.Companion.parse(WELCOME_MESSAGE_TIMESTAMP)
        coEvery { internalMessagesRepository.getInternalMessages() } returns flowOf(emptyList())

        isLocalMessageControllerUnderTest = InternalMessageDetailScreenController(
            application = mockContext,
            getInternalMessagesUseCase = getInternalMessagesUseCase,
            setInternalMessageAsReadUseCase = setInternalMessageAsReadUseCase
        )
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @After
    fun tearDown() {
        Dispatchers.resetMain()
        clearMocks(
            profileRepository,
            internalMessagesRepository,
            messageResources,
            buildConfigInformation,
            changeLogLocalDataSource
        )
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `fetch inapp messages from local database`() {
        coEvery { internalMessagesRepository.getLastUpdatedVersion() } returns flowOf(
            WELCOME_MESSAGE_VERSION
        )
        coEvery { getInternalMessagesUseCase.invoke(WELCOME_MESSAGE_LANG) } returns flowOf(
            listOf(
                welcomeMessage
            )
        )
        coEvery { internalMessagesRepository.setInternalMessagesAsRead() } returns Unit
        coEvery { changeLogLocalDataSource.getInternalMessageInCurrentLanguage(welcomeMessage) } returns welcomeMessage
        coEvery { internalMessagesRepository.updateInternalMessage(welcomeMessage) } returns Unit
        coEvery { internalMessagesRepository.getInternalMessages() } returns flowOf(
            listOf(
                welcomeMessage
            )
        )

        isLocalMessageControllerUnderTest = InternalMessageDetailScreenController(
            application = mockContext,
            getInternalMessagesUseCase = getInternalMessagesUseCase,
            setInternalMessageAsReadUseCase = setInternalMessageAsReadUseCase
        )

        testScope.runTest {
            advanceUntilIdle()
            var messageList = isLocalMessageControllerUnderTest.internalMessages.first()
            advanceUntilIdle()
            messageList = isLocalMessageControllerUnderTest.internalMessages.first()
            assertNotNull(messageList)
            TestCase.assertEquals(listOf(welcomeMessage.toInAppMessage()), messageList)
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `fetch inapp messages from local database return empty list`() {
        coEvery { internalMessagesRepository.setInternalMessagesAsRead() } returns Unit
        coEvery { buildConfigInformation.versionName() } returns (WELCOME_MESSAGE_VERSION)
        coEvery { internalMessagesRepository.getLastUpdatedVersion() } returns flowOf(
            WELCOME_MESSAGE_VERSION
        )
        coEvery { changeLogLocalDataSource.getChangeLogsAsInternalMessage() } returns emptyList()
        coEvery { internalMessagesRepository.getInternalMessages() } returns flowOf(emptyList())

        isLocalMessageControllerUnderTest = InternalMessageDetailScreenController(
            application = mockContext,
            getInternalMessagesUseCase = getInternalMessagesUseCase,
            setInternalMessageAsReadUseCase = setInternalMessageAsReadUseCase
        )

        testScope.runTest {
            advanceUntilIdle()
            val messageList = isLocalMessageControllerUnderTest.internalMessages.first()
            assertNotNull(messageList)
            assertEquals(true, messageList.isEmpty())
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `fetch inapp messages from local database return welcomeMessage`() {
        coEvery { internalMessagesRepository.setInternalMessagesAsRead() } returns Unit
        coEvery { buildConfigInformation.versionName() } returns (WELCOME_MESSAGE_VERSION)
        coEvery { internalMessagesRepository.getLastUpdatedVersion() } returns flowOf(
            WELCOME_MESSAGE_VERSION
        )
        coEvery { messageResources.messageFrom } returns (WELCOME_MESSAGE_ID)
        coEvery { messageResources.welcomeMessage } returns (WELCOME_MESSAGE_TEXT)
        coEvery { messageResources.welcomeMessageTag } returns (WELCOME_MESSAGE_TAG)
        coEvery { messageResources.getMessageTag(any()) } returns (WELCOME_MESSAGE_GET_MESSAGE_TAG)
        coEvery { internalMessagesRepository.getInternalMessages() } returns flowOf(
            listOf(
                welcomeMessage
            )
        )
        coEvery { getInternalMessagesUseCase.invoke(WELCOME_MESSAGE_LANG) } returns flowOf(
            listOf(
                welcomeMessage
            )
        )
        coEvery { changeLogLocalDataSource.getInternalMessageInCurrentLanguage(welcomeMessage) } returns welcomeMessage
        coEvery { internalMessagesRepository.updateInternalMessage(welcomeMessage) } returns Unit

        isLocalMessageControllerUnderTest = InternalMessageDetailScreenController(
            application = mockContext,
            getInternalMessagesUseCase = getInternalMessagesUseCase,
            setInternalMessageAsReadUseCase = setInternalMessageAsReadUseCase
        )

        testScope.runTest {
            advanceUntilIdle()
            var messageList = isLocalMessageControllerUnderTest.internalMessages.first()
            advanceUntilIdle()
            messageList = isLocalMessageControllerUnderTest.internalMessages.first()
            assertNotNull(messageList)
            TestCase.assertEquals(listOf(welcomeMessage.toInAppMessage()), messageList)
        }
    }
}
