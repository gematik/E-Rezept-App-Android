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

package de.gematik.ti.erp.app.messages.mapper

import de.gematik.ti.erp.app.communication.model.CommunicationErpModel
import de.gematik.ti.erp.app.communication.model.payload.CommunicationPayloadErpModel
import de.gematik.ti.erp.app.communication.model.payload.CommunicationReplyDeliveryStatusPayloadErpModel
import de.gematik.ti.erp.app.communication.model.payload.CommunicationReplyLinkPayloadErpModel
import de.gematik.ti.erp.app.communication.model.payload.CommunicationReplyPayloadV1ErpModel
import de.gematik.ti.erp.app.communication.model.payload.CommunicationReplyPaymentInfoPayloadErpModel
import de.gematik.ti.erp.app.communication.model.payload.CommunicationReplyPickupCodeDMCPayloadErpModel
import de.gematik.ti.erp.app.communication.model.payload.CommunicationReplyPickupCodeHRPayloadErpModel
import de.gematik.ti.erp.app.communication.model.payload.CommunicationReplyReservationStatusPayloadErpModel
import de.gematik.ti.erp.app.communication.model.payload.CommunicationReplyTextPayloadErpModel
import de.gematik.ti.erp.app.messages.domain.model.OrderUseCaseData
import de.gematik.ti.erp.app.messages.model.LastMessageDetails
import de.gematik.ti.erp.app.task.model.TaskErpModel
import io.github.aakira.napier.Napier
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant

fun CommunicationErpModel.toOrderDetail(
    taskDetailedBundles: List<OrderUseCaseData.TaskDetailedBundle>,
    pharmacyName: String?
) =
    OrderUseCaseData.OrderDetail(
        orderId = orderId,
        taskDetailedBundles = taskDetailedBundles,
        sentOn = timeStamp ?: Clock.System.now(),
        pharmacy = OrderUseCaseData.Pharmacy(name = pharmacyName ?: "", id = this.recipient),
        requestPayload = payload
    )

fun CommunicationErpModel.toMessage(taskIds: List<String>, isTaskIdCountMatching: Boolean): OrderUseCaseData.Message {
    val parsedPayload = payload
    val defaultValues = OrderUseCaseData.Message(
        communicationId = communicationId,
        sentOn = timeStamp ?: Clock.System.now(),
        content = null,
        pickUpCodeDMC = null,
        pickUpCodeHR = null,
        link = null,
        consumed = consumed,
        prescriptions = emptyList<TaskErpModel?>(),
        taskIds = taskIds,
        isTaskIdCountMatching = isTaskIdCountMatching,
        payloads = emptyList()
    )

    return when (parsedPayload) {
        null -> {
            Napier.d { "No payload, default message" }
            defaultValues
        }
        is CommunicationReplyPayloadV1ErpModel -> defaultValues.copy(
            content = parsedPayload.infoText?.takeUnless { it.isBlank() },
            pickUpCodeDMC = parsedPayload.pickUpCodeDMC?.takeUnless { it.isBlank() },
            pickUpCodeHR = parsedPayload.pickUpCodeHR?.takeUnless { it.isBlank() },
            link = parsedPayload.url?.takeUnless { it.isBlank() }?.takeIf { isValidUrl(it) }
        )
        is CommunicationReplyTextPayloadErpModel -> defaultValues.copy(
            content = parsedPayload.text.takeUnless { it.isBlank() }
        )
        is CommunicationReplyLinkPayloadErpModel -> defaultValues.copy(
            content = parsedPayload.text.takeUnless { it.isBlank() },
            link = parsedPayload.url.takeUnless { it.isBlank() }?.takeIf { isValidUrl(it) }
        )
        is CommunicationReplyPickupCodeHRPayloadErpModel -> defaultValues.copy(
            content = parsedPayload.text?.takeUnless { it.isBlank() },
            pickUpCodeHR = parsedPayload.pickUpCode.takeUnless { it.isBlank() }
        )
        is CommunicationReplyPickupCodeDMCPayloadErpModel -> defaultValues.copy(
            content = parsedPayload.text?.takeUnless { it.isBlank() },
            pickUpCodeDMC = parsedPayload.pickUpCodeDmc.takeUnless { it.isBlank() }
        )
        is CommunicationReplyReservationStatusPayloadErpModel -> defaultValues.copy(
            content = parsedPayload.readyForCollection.name
        )
        is CommunicationReplyDeliveryStatusPayloadErpModel -> defaultValues.copy(
            content = parsedPayload.text?.takeUnless { it.isBlank() } ?: parsedPayload.deliveryStatus.name
        )
        is CommunicationReplyPaymentInfoPayloadErpModel -> defaultValues.copy(
            content = parsedPayload.text?.takeUnless { it.isBlank() } ?: "Betrag: ${"%.2f".format(parsedPayload.totalAmount / 100.0).replace(".", ",")} €",
            link = parsedPayload.paymentMethods.firstNotNullOfOrNull { it.url }?.takeIf { isValidUrl(it) }
        )
        else -> defaultValues
    }
}

private fun List<CommunicationErpModel>.groupByPayloadAndTaskId(expectedTaskIdCount: Int): List<OrderUseCaseData.Message> {
    val replies = this.filter { it.profile == CommunicationErpModel.CommunicationProfile.ErxCommunicationReply }
    val others = this.filter { it.profile != CommunicationErpModel.CommunicationProfile.ErxCommunicationReply }
    val otherMessage = others.map { it.toMessage(emptyList<String>(), false) }

    // Replies with a null payload carry no information that lets us safely correlate them across tasks,
    // so each is kept as its own message tied to just its own task-id (no merging, no cross-task dedupe).
    val (nullPayloadReplies, parsedPayloadReplies) = replies.partition { it.payload == null }

    val nullPayloadMessages = nullPayloadReplies.map { communication ->
        communication.toMessage(
            taskIds = listOf(communication.taskId),
            isTaskIdCountMatching = expectedTaskIdCount == 1
        )
    }

    // Replies with a non-null payload that has equivalent content across multiple tasks of the same order are merged
    // into a single message listing all the task-ids that share it. Different payloads are kept separate.
    val parsedPayloadMessages = parsedPayloadReplies.groupBy { it.payload?.toGroupingKey() }.flatMap { (_, communications) ->
        val repliedTaskIds = communications.distinctBy { it.taskId }.map { it.taskId }
        val payloads = communications
            .mapNotNull { it.payload }
            .flatMap(CommunicationPayloadErpModel::toCommResV3Payloads)
            .distinctBy { it.toGroupingKey() }
        val representativeCommunication = communications.maxByOrNull { it.timeStamp ?: Instant.DISTANT_PAST } ?: communications.first()
        listOf(
            representativeCommunication.toMessage(
                taskIds = repliedTaskIds,
                isTaskIdCountMatching = repliedTaskIds.size == expectedTaskIdCount
            ).copy(payloads = payloads)
        )
    }

    val groupedRepliesMessage = nullPayloadMessages + parsedPayloadMessages
    return otherMessage + groupedRepliesMessage
}

fun CommunicationErpModel?.generatePreviewMessage(pharmacyName: String): LastMessageDetails? {
    return when (this?.profile) {
        CommunicationErpModel.CommunicationProfile.ErxCommunicationDispReq -> LastMessageDetails(
            content = pharmacyName,
            pickUpCodeDMC = null,
            pickUpCodeHR = null,
            link = null
        )

        CommunicationErpModel.CommunicationProfile.ErxCommunicationReply -> {
            val messageData = toMessage(emptyList(), false)
            LastMessageDetails(
                content = messageData.content,
                pickUpCodeDMC = messageData.pickUpCodeDMC,
                pickUpCodeHR = messageData.pickUpCodeHR,
                link = messageData.link
            )
        }

        else -> null
    }
}

private fun CommunicationPayloadErpModel.toGroupingKey(): Any = when (this) {
    is CommunicationReplyPayloadV1ErpModel -> listOf(supplyOptionsType, infoText, url, pickUpCodeHR, pickUpCodeDMC)
    is CommunicationReplyTextPayloadErpModel -> text
    is CommunicationReplyLinkPayloadErpModel -> listOf(text, url)
    is CommunicationReplyReservationStatusPayloadErpModel -> readyForCollection
    is CommunicationReplyPickupCodeHRPayloadErpModel -> listOf(pickUpCode, text)
    is CommunicationReplyPickupCodeDMCPayloadErpModel -> listOf(pickUpCodeDmc, text)
    is CommunicationReplyDeliveryStatusPayloadErpModel -> listOf(deliveryStatus, inTransportPosition, inTransportETA, text)
    is CommunicationReplyPaymentInfoPayloadErpModel -> listOf(paymentMethods, totalAmount, text)
    else -> this
}

fun List<CommunicationErpModel>.groupByPayloadForPreview(): List<CommunicationErpModel> {
    val replies = this.filter { it.profile == CommunicationErpModel.CommunicationProfile.ErxCommunicationReply }
    val others = this.filter { it.profile != CommunicationErpModel.CommunicationProfile.ErxCommunicationReply }

    val groupedReplies = replies.groupBy { it.payload?.toGroupingKey() }.mapNotNull { (_, communications) ->
        communications.maxByOrNull { it.timeStamp ?: Instant.DISTANT_PAST }
    }

    return others + groupedReplies
}

fun List<CommunicationErpModel>.toMessageList(orderDetail: OrderUseCaseData.OrderDetail): List<OrderUseCaseData.Message> {
    val groupedMessages = this.groupByPayloadAndTaskId(orderDetail.taskDetailedBundles.size)

    return groupedMessages.map { message ->
        message.copy(
            prescriptions = message.taskIds.map { taskId ->
                orderDetail.taskDetailedBundles.find { it.prescription?.taskId == taskId }?.prescription
            }
        )
    }
}

/**
 * Check if a URL is valid and uses the HTTPS scheme.
 */
private fun isValidUrl(url: String): Boolean =
    url.matches("^https://.*".toRegex())
