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
import de.gematik.ti.erp.app.communication.model.payload.CommunicationReplyLinkPayloadErpModel
import de.gematik.ti.erp.app.communication.model.payload.CommunicationReplyPayloadV1ErpModel
import de.gematik.ti.erp.app.communication.model.payload.CommunicationReplyPickupCodeDMCPayloadErpModel
import de.gematik.ti.erp.app.communication.model.payload.CommunicationReplyPickupCodeHRPayloadErpModel
import de.gematik.ti.erp.app.communication.model.payload.CommunicationReplyTextPayloadErpModel
import de.gematik.ti.erp.app.communication.model.payload.CommunicationSupplyOptionTypeErpModel
import de.gematik.ti.erp.app.communication.model.payload.DispenseRequestCommunicationPayloadV1ErpModel
import de.gematik.ti.erp.app.communication.model.payload.DispenseRequestCommunicationPayloadV3ErpModel
import de.gematik.ti.erp.app.communication.model.payload.DispenseRequestDeliveryPayloadErpModel
import de.gematik.ti.erp.app.communication.model.payload.DispenseRequestReservationPayloadErpModel
import de.gematik.ti.erp.app.communication.model.payload.DispenseRequestShipmentPayloadErpModel
import de.gematik.ti.erp.app.communication.model.payload.InfoAvailabilityRequestPayloadErpModel
import de.gematik.ti.erp.app.messages.domain.model.OrderUseCaseData
import kotlinx.datetime.Clock

fun OrderUseCaseData.OrderDetail.toCommResV3OrderModel(
    messages: List<OrderUseCaseData.Message>,
    sentReplyMessages: List<CommunicationErpModel> = emptyList()
): OrderUseCaseData.OrderErpModel {
    val tasks = taskDetailedBundles.mapNotNull { it.prescription }
    val requestSeen = !hasUnreadMessages

    val cards = buildList {
        add(
            OrderUseCaseData.OrderCardErpModel.DispenseRequestCard(
                sentOn = sentOn,
                pharmacy = pharmacy,
                tasks = tasks,
                payload = requestPayload,
                hasSeen = requestSeen
            )
        )

        requestPayload
            ?.takeIf { it.userVisibleText() != null }
            ?.let { payload ->
                add(
                    OrderUseCaseData.OrderCardErpModel.UserReplyCard(
                        sentOn = sentOn,
                        payload = payload,
                        hasSeen = requestSeen
                    )
                )
            }

        val initialTransactionId = requestPayload?.transactionID

        sentReplyMessages
            .filterNot { initialTransactionId != null && it.payload?.transactionID == initialTransactionId }
            .toGroupedUserReplyCards()
            .forEach(::add)

        taskDetailedBundles.forEach { taskBundle ->
            val invoiceTimestamp = taskBundle.invoiceInfo.invoiceSentOn ?: return@forEach
            if (taskBundle.invoiceInfo.hasInvoice) {
                add(
                    OrderUseCaseData.OrderCardErpModel.InvoiceCard(
                        sentOn = invoiceTimestamp,
                        invoiceInfo = taskBundle.invoiceInfo,
                        tasks = listOfNotNull(taskBundle.prescription),
                        hasSeen = taskBundle.invoiceInfo.consumed
                    )
                )
            }
        }

        messages.forEach { message ->
            if (message.payloads.isNotEmpty() || message.type == OrderUseCaseData.Message.Type.Empty) {
                add(
                    OrderUseCaseData.OrderCardErpModel.PharmacyReplyCard(
                        message = message,
                        payloads = message.payloads,
                        sentOn = message.sentOn,
                        hasSeen = message.consumed
                    )
                )
            }
        }
    }.sortedWith(compareBy({ it.sentOn }, { it.renderOrder() }))

    return OrderUseCaseData.OrderErpModel(
        orderId = orderId,
        tasks = tasks,
        pharmacy = pharmacy,
        cards = cards
    )
}

fun OrderUseCaseData.OrderCardErpModel.renderOrder(): Int =
    when (this) {
        is OrderUseCaseData.OrderCardErpModel.DispenseRequestCard -> 0
        is OrderUseCaseData.OrderCardErpModel.UserReplyCard -> 1
        is OrderUseCaseData.OrderCardErpModel.PharmacyReplyCard -> 2
        is OrderUseCaseData.OrderCardErpModel.InvoiceCard -> 3
    }

fun CommunicationPayloadErpModel?.dispenseSupplyOption(): CommunicationSupplyOptionTypeErpModel =
    when (this) {
        is DispenseRequestCommunicationPayloadV1ErpModel -> supplyOptionsType
        is DispenseRequestCommunicationPayloadV3ErpModel -> supplyOptionsType
        is DispenseRequestReservationPayloadErpModel -> supplyOptionsType
        is DispenseRequestDeliveryPayloadErpModel -> supplyOptionsType
        is DispenseRequestShipmentPayloadErpModel -> supplyOptionsType
        else -> CommunicationSupplyOptionTypeErpModel.UNKNOWN
    }

fun CommunicationPayloadErpModel.userVisibleText(): String? =
    when (this) {
        is DispenseRequestCommunicationPayloadV3ErpModel -> text?.takeUnless { it.isBlank() }
        is DispenseRequestReservationPayloadErpModel -> text.takeUnless { it.isBlank() }
        is DispenseRequestDeliveryPayloadErpModel -> text.takeUnless { it.isBlank() }
        is DispenseRequestShipmentPayloadErpModel -> text.takeUnless { it.isBlank() }
        is InfoAvailabilityRequestPayloadErpModel -> text.takeUnless { it.isBlank() }
        is CommunicationReplyTextPayloadErpModel -> text.takeUnless { it.isBlank() }
        else -> null
    }

private fun List<CommunicationErpModel>.toGroupedUserReplyCards(): List<OrderUseCaseData.OrderCardErpModel.UserReplyCard> =
    filter { it.payload is InfoAvailabilityRequestPayloadErpModel }
        .distinctBy { it.communicationId.ifEmpty { it.payload?.transactionID ?: it.timeStamp.toString() } }
        .mapNotNull { communication ->
            val payload = communication.payload?.takeIf { it.userVisibleText() != null } ?: return@mapNotNull null

            OrderUseCaseData.OrderCardErpModel.UserReplyCard(
                sentOn = communication.timeStamp ?: Clock.System.now(),
                payload = payload,
                hasSeen = communication.consumed
            )
        }

fun CommunicationPayloadErpModel.pickupMessageHint(): String? =
    when (this) {
        is CommunicationReplyPickupCodeDMCPayloadErpModel -> text?.takeUnless { it.isBlank() }
        is CommunicationReplyPickupCodeHRPayloadErpModel -> text?.takeUnless { it.isBlank() }
        else -> null
    }

fun CommunicationPayloadErpModel.toCommResV3Payloads(): List<CommunicationPayloadErpModel> =
    when (this) {
        is CommunicationReplyPayloadV1ErpModel -> toCommResV3PayloadsFromLegacyV1()
        is CommunicationReplyLinkPayloadErpModel -> {
            val nonBlankText = text.takeUnless { it.isBlank() }
            if (version == 1 && nonBlankText != null) {
                listOf(
                    CommunicationReplyTextPayloadErpModel(
                        version = version,
                        transactionID = transactionID,
                        text = nonBlankText
                    ),
                    copy(text = "")
                )
            } else {
                listOf(this)
            }
        }

        is CommunicationReplyPickupCodeHRPayloadErpModel -> {
            val nonBlankText = text?.takeUnless { it.isBlank() }
            if (version == 1 && nonBlankText != null) {
                listOf(
                    CommunicationReplyTextPayloadErpModel(
                        version = version,
                        transactionID = transactionID,
                        text = nonBlankText
                    ),
                    copy(text = null)
                )
            } else {
                listOf(this)
            }
        }

        is CommunicationReplyPickupCodeDMCPayloadErpModel -> {
            val nonBlankText = text?.takeUnless { it.isBlank() }
            if (version == 1 && nonBlankText != null) {
                listOf(
                    CommunicationReplyTextPayloadErpModel(
                        version = version,
                        transactionID = transactionID,
                        text = nonBlankText
                    ),
                    copy(text = null)
                )
            } else {
                listOf(this)
            }
        }

        else -> listOf(this)
    }

private fun CommunicationReplyPayloadV1ErpModel.toCommResV3PayloadsFromLegacyV1(): List<CommunicationPayloadErpModel> {
    val renderPayloads = buildList {
        infoText
            ?.takeUnless { it.isBlank() }
            ?.let { text ->
                add(
                    CommunicationReplyTextPayloadErpModel(
                        version = version,
                        transactionID = transactionID.orEmpty(),
                        text = text
                    )
                )
            }

        url
            ?.takeUnless { it.isBlank() }
            ?.let { nonBlankUrl ->
                add(
                    CommunicationReplyLinkPayloadErpModel(
                        version = version,
                        transactionID = transactionID.orEmpty(),
                        text = "",
                        url = nonBlankUrl
                    )
                )
            }

        pickUpCodeHR
            ?.takeUnless { it.isBlank() }
            ?.let { nonBlankCode ->
                add(
                    CommunicationReplyPickupCodeHRPayloadErpModel(
                        version = version,
                        transactionID = transactionID.orEmpty(),
                        pickUpCode = nonBlankCode,
                        text = null
                    )
                )
            }

        pickUpCodeDMC
            ?.takeUnless { it.isBlank() }
            ?.let { nonBlankCode ->
                add(
                    CommunicationReplyPickupCodeDMCPayloadErpModel(
                        version = version,
                        transactionID = transactionID.orEmpty(),
                        pickUpCodeDmc = nonBlankCode,
                        text = null
                    )
                )
            }
    }

    return renderPayloads.ifEmpty { listOf(this) }
}
