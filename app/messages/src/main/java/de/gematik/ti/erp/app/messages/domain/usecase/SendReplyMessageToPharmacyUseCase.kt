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
import de.gematik.ti.erp.app.fhir.communication.CommunicationDispenseRequest.createCommunicationReplyToPharmacyRequest
import de.gematik.ti.erp.app.fhir.communication.model.FhirDispenseCommunicationEntryErpModel
import de.gematik.ti.erp.app.fhir.communication.parser.CommunicationParser
import de.gematik.ti.erp.app.fhir.communication.parser.CommunicationPayloadParser
import de.gematik.ti.erp.app.fhir.constant.communication.FhirCommunicationConstants
import de.gematik.ti.erp.app.messages.domain.model.OrderUseCaseData
import de.gematik.ti.erp.app.messages.repository.CommunicationRepository
import de.gematik.ti.erp.app.pharmacy.repository.ShippingContactRepository
import de.gematik.ti.erp.app.prescription.repository.TaskOperationsRepository
import de.gematik.ti.erp.app.profile.repository.ProfileIdentifier
import de.gematik.ti.erp.app.utils.formatPhoneForBackend
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.withContext
import kotlinx.datetime.Clock
import java.util.UUID

class SendReplyMessageToPharmacyUseCase(
    private val taskOperationsRepository: TaskOperationsRepository,
    private val communicationRepository: CommunicationRepository,
    private val communicationParser: CommunicationParser,
    private val shippingContactRepository: ShippingContactRepository,
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO
) {
    suspend operator fun invoke(
        profileId: ProfileIdentifier,
        order: OrderUseCaseData.OrderDetail,
        message: String
    ): Result<Unit> = withContext(dispatcher) {
        val trimmedMessage = message.trim()
        if (trimmedMessage.isBlank()) {
            return@withContext Result.failure(IllegalArgumentException("Reply message must not be blank"))
        }

        val prescription = order.taskDetailedBundles.firstNotNullOfOrNull { it.prescription }
            ?: return@withContext Result.failure(IllegalStateException("No prescription available for reply"))

        val shippingPhone = shippingContactRepository.shippingContact()
            .firstOrNull()
            ?.phone
            ?.takeIf { it.isNotBlank() }
        val phoneToUse = shippingPhone.orEmpty()

        val (flowTypeCode, flowTypeDisplay) = FhirCommunicationConstants.determineFlowType(prescription.taskId)
        val payload = InfoAvailabilityRequestPayloadErpModel(
            version = 3,
            communicationType = CommunicationTypeErpModel.Text,
            transactionID = UUID.randomUUID().toString(),
            phone = phoneToUse.formatPhoneForBackend(),
            text = trimmedMessage
        )

        val communication = createCommunicationReplyToPharmacyRequest(
            orderId = order.orderId,
            taskId = prescription.taskId,
            accessCode = prescription.accessCode,
            recipientId = order.pharmacy.id,
            payloadContent = payload,
            flowTypeCode = flowTypeCode,
            flowTypeDisplay = flowTypeDisplay
        )

        taskOperationsRepository.redeem(
            profileId = profileId,
            communication = communication,
            accessCode = prescription.accessCode
        ).mapCatching { response ->
            val responseModel = communicationParser.extractSingle(response) as? FhirDispenseCommunicationEntryErpModel
                ?: error("Failed to parse sent communication response")

            communicationRepository.saveCommunications(
                listOf(
                    responseModel.toCommunicationErpModel(
                        profileId = profileId,
                        fallbackOrderId = order.orderId,
                        fallbackTaskId = prescription.taskId,
                        fallbackRecipient = order.pharmacy.id,
                        fallbackPayload = payload
                    )
                )
            )
            Unit
        }
    }
}

private fun FhirDispenseCommunicationEntryErpModel.toCommunicationErpModel(
    profileId: ProfileIdentifier,
    fallbackOrderId: String,
    fallbackTaskId: String,
    fallbackRecipient: String,
    fallbackPayload: InfoAvailabilityRequestPayloadErpModel
): CommunicationErpModel =
    CommunicationErpModel(
        communicationId = id,
        orderId = orderId ?: fallbackOrderId,
        taskId = taskId ?: fallbackTaskId,
        senderTelematikId = sender?.identifier.orEmpty(),
        consumed = false,
        payload = payload
            ?.let { CommunicationPayloadParser.extract(it, isRequest = true) }
            ?: fallbackPayload,
        profile = CommunicationErpModel.CommunicationProfile.ErxCommunicationDispReq,
        recipient = recipient?.identifier ?: fallbackRecipient,
        profileId = profileId,
        timeStamp = sent?.value ?: Clock.System.now(),
        pharmacyName = pharmacyName
    )
