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

import de.gematik.ti.erp.app.CoroutineTestRule
import de.gematik.ti.erp.app.messages.domain.usecase.SaveLocalCommunicationUseCase
import de.gematik.ti.erp.app.messages.mocks.MessageMocks.MOCK_PHARMACY_O1
import de.gematik.ti.erp.app.messages.mocks.MessageMocks.MOCK_TASK_ID_01
import de.gematik.ti.erp.app.messages.mocks.MessageMocks.MOCK_TRANSACTION_ID
import de.gematik.ti.erp.app.messages.repository.CommunicationRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@ExperimentalCoroutinesApi
class SaveLocalCommunicationUseCaseTest {

    @get:Rule
    val coroutineRule = CoroutineTestRule()

    private val dispatcher = StandardTestDispatcher()

    private val communicationRepository: CommunicationRepository = mockk()

    @InjectMockKs
    private lateinit var useCase: SaveLocalCommunicationUseCase

    @Before
    fun setup() {
        coEvery {
            communicationRepository.saveLocalCommunication(
                MOCK_TASK_ID_01,
                MOCK_PHARMACY_O1.telematikId,
                MOCK_TRANSACTION_ID
            )
        } returns Unit

        useCase = SaveLocalCommunicationUseCase(
            repository = communicationRepository,
            dispatcher = dispatcher
        )
    }

    @Test
    fun `invoke should save local communication`() = runTest(dispatcher) {
        val mockTaskId = MOCK_TASK_ID_01
        val mockPharmacyId = MOCK_PHARMACY_O1.telematikId

        useCase.invoke(mockTaskId, mockPharmacyId, MOCK_TRANSACTION_ID)

        coVerify(exactly = 1) {
            communicationRepository.saveLocalCommunication(mockTaskId, mockPharmacyId, MOCK_TRANSACTION_ID)
        }
    }
}
