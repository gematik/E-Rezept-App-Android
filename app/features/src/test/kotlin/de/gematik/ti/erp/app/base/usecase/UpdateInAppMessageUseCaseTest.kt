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

package de.gematik.ti.erp.app.base.usecase

import de.gematik.ti.erp.app.changelogs.InAppMessageRepository
import de.gematik.ti.erp.app.info.BuildConfigInformation
import de.gematik.ti.erp.app.messages.domain.repository.InAppLocalMessageRepository
import de.gematik.ti.erp.app.mocks.order.model.inAppMessagesVersion
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.runs
import io.mockk.verify
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test

class UpdateInAppMessageUseCaseTest {
    private val inAppMessageRepository: InAppMessageRepository = mockk()
    private val localMessageRepository: InAppLocalMessageRepository = mockk()
    private val buildConfigInformation: BuildConfigInformation = mockk()
    private lateinit var updateInAppMessageUseCase: UpdateInAppMessageUseCase

    @Before
    fun setUp() {
        updateInAppMessageUseCase = UpdateInAppMessageUseCase(
            inAppMessageRepository,
            localMessageRepository,
            buildConfigInformation
        )
    }

    @Test
    fun `invoke does not update when lastUpdatedVersion is greater than or equal to currentVersion`() = runTest {
        val versionWithRC = "1.0.0"
        val lastUpdatedVersion = "1.5.0"

        every { buildConfigInformation.versionName() } returns versionWithRC
        every { inAppMessageRepository.lastUpdatedVersion } returns flowOf(lastUpdatedVersion)

        updateInAppMessageUseCase.invoke()

        verify(exactly = 0) { localMessageRepository.getInternalMessages() }
        coVerify(exactly = 0) { inAppMessageRepository.updateChangeLogs(any(), any(), any()) }
    }

    @Test
    fun `invoke does not update when internal messages is empty`() = runTest {
        val versionWithRC = "2.0.0"
        val lastUpdatedVersion = "1.0.0"

        every { buildConfigInformation.versionName() } returns versionWithRC
        every { inAppMessageRepository.lastUpdatedVersion } returns flowOf(lastUpdatedVersion)
        every { localMessageRepository.getInternalMessages() } returns flowOf(emptyList())

        updateInAppMessageUseCase.invoke()

        verify(exactly = 1) { localMessageRepository.getInternalMessages() }
        coVerify(exactly = 0) { inAppMessageRepository.updateChangeLogs(any(), any(), any()) }
    }

    @Test
    fun `invoke does update when new internal messages are found`() = runTest {
        val versionWithRC = "2.0.0"
        val lastUpdatedVersion = "1.0.0"

        every { buildConfigInformation.versionName() } returns versionWithRC
        every { inAppMessageRepository.lastUpdatedVersion } returns flowOf(lastUpdatedVersion)
        every { localMessageRepository.getInternalMessages() } returns flowOf(inAppMessagesVersion)
        coEvery { inAppMessageRepository.updateChangeLogs(any(), any(), any()) } just runs

        updateInAppMessageUseCase.invoke()
        verify(exactly = 1) { localMessageRepository.getInternalMessages() }
        coVerify(exactly = 1) { inAppMessageRepository.updateChangeLogs(any(), any(), any()) }
    }

    @Test
    fun `invoke does not update when no new internal messages are found`() = runTest {
        val versionWithRC = "3.0.0"
        val lastUpdatedVersion = "2.0.0"

        every { buildConfigInformation.versionName() } returns versionWithRC
        every { inAppMessageRepository.lastUpdatedVersion } returns flowOf(lastUpdatedVersion)
        every { localMessageRepository.getInternalMessages() } returns flowOf(emptyList())

        updateInAppMessageUseCase.invoke()

        verify(exactly = 1) { localMessageRepository.getInternalMessages() }
        coVerify(exactly = 0) { inAppMessageRepository.updateChangeLogs(any(), any(), any()) }
    }
}
