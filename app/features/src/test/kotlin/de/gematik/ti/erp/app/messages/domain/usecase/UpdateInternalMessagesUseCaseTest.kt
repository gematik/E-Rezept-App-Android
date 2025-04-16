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

import de.gematik.ti.erp.app.messages.repository.InternalMessagesRepository
import de.gematik.ti.erp.app.info.BuildConfigInformation
import de.gematik.ti.erp.app.messages.domain.repository.ChangeLogLocalDataSource
import de.gematik.ti.erp.app.messages.model.getTimeState
import de.gematik.ti.erp.app.mocks.order.model.welcomeMessage
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.runs
import io.mockk.verify
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.Instant
import org.junit.Before
import org.junit.Test

class UpdateInternalMessagesUseCaseTest {
    private val internalMessagesRepository: InternalMessagesRepository = mockk()
    private val localMessageRepository: ChangeLogLocalDataSource = mockk()
    private val buildConfigInformation: BuildConfigInformation = mockk()
    private lateinit var updateInternalMessagesUseCase: UpdateInternalMessagesUseCase

    @Before
    fun setUp() {
        updateInternalMessagesUseCase = UpdateInternalMessagesUseCase(
            internalMessagesRepository,
            localMessageRepository,
            buildConfigInformation
        )
    }

    @Test
    fun `invoke does not update when lastUpdatedVersion is greater than or equal to currentVersion`() = runTest {
        val versionWithRC = "1.0.0"
        val lastUpdatedVersion = "1.5.0"

        every { buildConfigInformation.versionName() } returns versionWithRC
        every { internalMessagesRepository.getLastUpdatedVersion() } returns flowOf(lastUpdatedVersion)
        every { localMessageRepository.getChangeLogsAsInternalMessage() } returns listOf(welcomeMessage)
        updateInternalMessagesUseCase.invoke()

        verify(exactly = 1) { localMessageRepository.getChangeLogsAsInternalMessage() }
        coVerify(exactly = 0) { internalMessagesRepository.saveInternalMessage(any()) }
    }

    @Test
    fun `invoke does not update when internal messages is empty`() = runTest {
        val versionWithRC = "2.0.0"
        val lastUpdatedVersion = "1.28.4"

        every { buildConfigInformation.versionName() } returns versionWithRC
        every { internalMessagesRepository.getLastUpdatedVersion() } returns flowOf(lastUpdatedVersion)
        every { localMessageRepository.getChangeLogsAsInternalMessage() } returns emptyList()
        every { localMessageRepository.createWelcomeMessage(lastUpdatedVersion, any()) } returns welcomeMessage

        updateInternalMessagesUseCase.invoke()

        verify(exactly = 1) { localMessageRepository.getChangeLogsAsInternalMessage() }
        coVerify(exactly = 0) { internalMessagesRepository.saveInternalMessage(any()) }
    }

    @Test
    fun `invoke does update when new internal messages are found`() = runTest {
        val versionWithRC = "2.0.0"
        val lastUpdatedVersion = "1.28.4"

        every { buildConfigInformation.versionName() } returns versionWithRC
        every { internalMessagesRepository.getLastUpdatedVersion() } returns flowOf(lastUpdatedVersion)
        every { localMessageRepository.getChangeLogsAsInternalMessage() } returns listOf(welcomeMessage)
        every { localMessageRepository.createWelcomeMessage(lastUpdatedVersion, getTimeState(Instant.DISTANT_PAST)) } returns welcomeMessage
        coEvery { internalMessagesRepository.saveInternalMessage(any()) } just runs

        updateInternalMessagesUseCase.invoke()
        verify(exactly = 1) { localMessageRepository.getChangeLogsAsInternalMessage() }
        coVerify(exactly = 1) { internalMessagesRepository.saveInternalMessage(any()) }
    }

    @Test
    fun `invoke does not update when no new internal messages are found`() = runTest {
        val versionWithRC = "3.0.0"
        val lastUpdatedVersion = "2.0.0"

        every { buildConfigInformation.versionName() } returns versionWithRC
        every { internalMessagesRepository.getLastUpdatedVersion() } returns flowOf(lastUpdatedVersion)
        every { localMessageRepository.getChangeLogsAsInternalMessage() } returns listOf(welcomeMessage)

        updateInternalMessagesUseCase.invoke()

        verify(exactly = 1) { localMessageRepository.getChangeLogsAsInternalMessage() }
        coVerify(exactly = 0) { internalMessagesRepository.saveInternalMessage(any()) }
    }
}
