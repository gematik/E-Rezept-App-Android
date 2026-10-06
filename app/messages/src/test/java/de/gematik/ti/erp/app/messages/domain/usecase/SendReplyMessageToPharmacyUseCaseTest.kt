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

package de.gematik.ti.erp.app.messages.domain.usecase

import de.gematik.ti.erp.app.communication.model.CommunicationErpModel
import de.gematik.ti.erp.app.communication.model.payload.CommunicationTypeErpModel
import de.gematik.ti.erp.app.communication.model.payload.InfoAvailabilityRequestPayloadErpModel
import de.gematik.ti.erp.app.fhir.communication.model.FhirDispenseCommunicationEntryErpModel
import de.gematik.ti.erp.app.fhir.communication.model.support.CommunicationParticipantErpModel
import de.gematik.ti.erp.app.fhir.communication.parser.CommunicationParser
import de.gematik.ti.erp.app.fhir.temporal.FhirTemporal
import de.gematik.ti.erp.app.messages.repository.CommunicationRepository
import de.gematik.ti.erp.app.mocks.order.model.MOCK_PROFILE
import de.gematik.ti.erp.app.mocks.order.model.ORDER_DETAIL
import de.gematik.ti.erp.app.pharmacy.repository.ShippingContactRepository
import de.gematik.ti.erp.app.prescription.repository.TaskOperationsRepository
import de.gematik.ti.erp.app.shippingInfo.model.ShippingInfoErpModel
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.Instant
import kotlinx.serialization.json.JsonNull
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertTrue

class SendReplyMessageToPharmacyUseCaseTest {
    private val taskOperationsRepository = mockk<TaskOperationsRepository>()
    private val communicationRepository = mockk<CommunicationRepository>()
    private val communicationParser = mockk<CommunicationParser>()
    private val shippingContactRepository = mockk<ShippingContactRepository>()
    private val dispatcher = StandardTestDispatcher()

    private val useCase = SendReplyMessageToPharmacyUseCase(
        taskOperationsRepository = taskOperationsRepository,
        communicationRepository = communicationRepository,
        communicationParser = communicationParser,
        shippingContactRepository = shippingContactRepository,
        dispatcher = dispatcher
    )

    @Test
    fun `invoke sends and persists reply message`() = runTest(dispatcher) {
        val message = "Bitte geben Sie mir Bescheid."
        val sentAt = Instant.parse("2025-01-15T08:30:00Z")
        val response = JsonNull
        val communicationSlot = slot<List<CommunicationErpModel>>()

        every { shippingContactRepository.shippingContact() } returns flowOf(null)
        coEvery {
            taskOperationsRepository.redeem(
                profileId = MOCK_PROFILE.id,
                communication = any(),
                accessCode = ORDER_DETAIL.taskDetailedBundles.first().prescription?.accessCode.orEmpty()
            )
        } returns Result.success(response)
        every { communicationParser.extractSingle(response) } returns FhirDispenseCommunicationEntryErpModel(
            id = "reply-1",
            profile = "https://gematik.de/fhir/erp/StructureDefinition/GEM_ERP_PR_Communication_DispReq",
            taskId = ORDER_DETAIL.taskDetailedBundles.first().prescription?.taskId,
            sender = CommunicationParticipantErpModel(identifier = "X123456789"),
            recipient = CommunicationParticipantErpModel(identifier = ORDER_DETAIL.pharmacy.id),
            orderId = ORDER_DETAIL.orderId,
            sent = FhirTemporal.Instant(sentAt),
            payload = null
        )
        coEvery { communicationRepository.saveCommunications(capture(communicationSlot)) } returns 1

        val result = useCase(
            profileId = MOCK_PROFILE.id,
            order = ORDER_DETAIL,
            message = message
        )

        assertTrue(result.isSuccess)
        assertEquals(1, communicationSlot.captured.size)

        val savedCommunication = communicationSlot.captured.single()
        assertEquals("reply-1", savedCommunication.communicationId)
        assertEquals(ORDER_DETAIL.orderId, savedCommunication.orderId)
        assertEquals(ORDER_DETAIL.taskDetailedBundles.first().prescription?.taskId, savedCommunication.taskId)
        assertEquals(ORDER_DETAIL.pharmacy.id, savedCommunication.recipient)
        assertEquals(MOCK_PROFILE.id, savedCommunication.profileId)
        assertEquals(sentAt, savedCommunication.timeStamp)
        val payload = assertIs<InfoAvailabilityRequestPayloadErpModel>(savedCommunication.payload)
        assertEquals(CommunicationTypeErpModel.Text, payload.communicationType)
        assertEquals("", payload.phone)
        assertEquals(message, payload.text)

        coVerify(exactly = 1) {
            taskOperationsRepository.redeem(
                profileId = MOCK_PROFILE.id,
                communication = any(),
                accessCode = ORDER_DETAIL.taskDetailedBundles.first().prescription?.accessCode.orEmpty()
            )
        }
        coVerify(exactly = 1) { communicationRepository.saveCommunications(any()) }
    }

    @Test
    fun `invoke uses shipping contact phone if available`() = runTest(dispatcher) {
        val customPhone = "+49 170 1234567"
        val expectedFormattedPhone = "00491701234567"
        val shippingInfo = ShippingInfoErpModel.EmptyShippingInfoErpModel.copy(phone = customPhone)
        every { shippingContactRepository.shippingContact() } returns flowOf(shippingInfo)

        val customUseCase = SendReplyMessageToPharmacyUseCase(
            taskOperationsRepository = taskOperationsRepository,
            communicationRepository = communicationRepository,
            communicationParser = communicationParser,
            shippingContactRepository = shippingContactRepository,
            dispatcher = dispatcher
        )

        val message = "Test"
        val sentAt = Instant.parse("2025-01-15T08:30:00Z")
        val response = JsonNull
        val communicationSlot = slot<List<CommunicationErpModel>>()

        coEvery {
            taskOperationsRepository.redeem(
                profileId = MOCK_PROFILE.id,
                communication = any(),
                accessCode = ORDER_DETAIL.taskDetailedBundles.first().prescription?.accessCode.orEmpty()
            )
        } returns Result.success(response)
        every { communicationParser.extractSingle(response) } returns FhirDispenseCommunicationEntryErpModel(
            id = "reply-2",
            profile = "https://gematik.de/fhir/erp/StructureDefinition/GEM_ERP_PR_Communication_DispReq",
            taskId = ORDER_DETAIL.taskDetailedBundles.first().prescription?.taskId,
            sender = CommunicationParticipantErpModel(identifier = "X123456789"),
            recipient = CommunicationParticipantErpModel(identifier = ORDER_DETAIL.pharmacy.id),
            orderId = ORDER_DETAIL.orderId,
            sent = FhirTemporal.Instant(sentAt),
            payload = null
        )
        coEvery { communicationRepository.saveCommunications(capture(communicationSlot)) } returns 1

        val result = customUseCase(
            profileId = MOCK_PROFILE.id,
            order = ORDER_DETAIL,
            message = message
        )

        assertTrue(result.isSuccess)
        val payload = assertIs<InfoAvailabilityRequestPayloadErpModel>(communicationSlot.captured.single().payload)
        assertEquals(expectedFormattedPhone, payload.phone)
    }

    @Test
    fun `invoke rejects blank reply message`() = runTest(dispatcher) {
        val result = useCase(
            profileId = MOCK_PROFILE.id,
            order = ORDER_DETAIL,
            message = "   "
        )

        assertTrue(result.isFailure)
        coVerify(exactly = 0) { taskOperationsRepository.redeem(any(), any(), any()) }
        coVerify(exactly = 0) { communicationRepository.saveCommunications(any()) }
    }
}
