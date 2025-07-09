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

package de.gematik.ti.erp.app.messages.usecase

import de.gematik.ti.erp.app.messages.domain.usecase.UpdateCommunicationConsumedStatusUseCase
import de.gematik.ti.erp.app.messages.mocks.MessageMocks
import de.gematik.ti.erp.app.messages.repository.CommunicationRepository
import io.mockk.coEvery
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import kotlin.test.assertEquals

@ExperimentalCoroutinesApi
class UpdateCommunicationByOrderIdUseCaseTest {

    private val dispatcher = StandardTestDispatcher()

    private val repository: CommunicationRepository = mockk()

    @InjectMockKs
    private lateinit var useCase: UpdateCommunicationConsumedStatusUseCase

    @Before
    fun setup() {
        coEvery {
            repository.loadDispReqCommunications(any())
        } returns flowOf(
            listOf(
                MessageMocks.MOCK_DISP_REQ_COMMUNICATION_01,
                MessageMocks.MOCK_DISP_REQ_COMMUNICATION_02
            )
        )

        coEvery {
            repository.setCommunicationStatus(any(), any())
        } returns Unit

        useCase = UpdateCommunicationConsumedStatusUseCase(repository, dispatcher)
    }

    @Test
    fun `invoke should update communication status`() = runTest(dispatcher) {
        val communicationIdsCaptured = mutableListOf<String>()

        coEvery {
            repository.loadDispReqCommunications(any())
        } returns flowOf(
            listOf(
                MessageMocks.MOCK_DISP_REQ_COMMUNICATION_01,
                MessageMocks.MOCK_DISP_REQ_COMMUNICATION_02
            )
        )
        coEvery {
            repository.taskIdsByOrder(any())
        } returns flowOf(listOf(MessageMocks.MOCK_TASK_ID_01, MessageMocks.MOCK_TASK_ID_02))

        coEvery {
            repository.loadAllRepliedCommunications(any())
        } returns flowOf(listOf())

        coEvery {
            repository.setCommunicationStatus(capture(communicationIdsCaptured), true)
        } returns Unit

        useCase(UpdateCommunicationConsumedStatusUseCase.Companion.CommunicationIdentifier.Order(MessageMocks.MOCK_ORDER_ID))

        assertEquals(
            communicationIdsCaptured,
            listOf(
                MessageMocks.MOCK_DISP_REQ_COMMUNICATION_01.communicationId,
                MessageMocks.MOCK_DISP_REQ_COMMUNICATION_02.communicationId
            )
        )
    }
}
