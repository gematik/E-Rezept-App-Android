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

package de.gematik.ti.erp.app.messages.usecase

import de.gematik.ti.erp.app.messages.domain.usecase.UpdateCommunicationByCommunicationIdUseCase
import de.gematik.ti.erp.app.messages.mocks.MessageMocks
import de.gematik.ti.erp.app.messages.repository.CommunicationRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test

@ExperimentalCoroutinesApi
class UpdateCommunicationByCommunicationIdUseCaseTest {

    private val dispatcher = StandardTestDispatcher()

    private val communicationRepository: CommunicationRepository = mockk()

    @InjectMockKs
    private lateinit var useCase: UpdateCommunicationByCommunicationIdUseCase

    @Before
    fun setup() {
        useCase = UpdateCommunicationByCommunicationIdUseCase(
            repository = communicationRepository,
            dispatcher = dispatcher
        )
    }

    @Test
    fun `invoke should update communication status`() = runTest(dispatcher) {
        val communicationId = MessageMocks.MOCK_DISP_REQ_COMMUNICATION_01.communicationId
        coEvery {
            communicationRepository.setCommunicationStatus(communicationId, true)
        } returns Unit

        useCase(communicationId)

        coVerify(exactly = 1) {
            communicationRepository.setCommunicationStatus(communicationId, true)
        }
    }
}
