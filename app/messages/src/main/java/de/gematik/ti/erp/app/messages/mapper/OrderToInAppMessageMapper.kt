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
import de.gematik.ti.erp.app.core.R
import de.gematik.ti.erp.app.messages.domain.model.MessagesStringProvider
import de.gematik.ti.erp.app.messages.domain.model.OrderUseCaseData
import de.gematik.ti.erp.app.messages.model.InAppMessage
import de.gematik.ti.erp.app.messages.model.LastMessage
import de.gematik.ti.erp.app.messages.model.LastMessageDetails
import de.gematik.ti.erp.app.timestate.getTimeState
import kotlinx.datetime.Instant

class OrderToInAppMessageMapper(
    private val stringProvider: MessagesStringProvider
) {
    fun map(order: OrderUseCaseData.Order): InAppMessage =
        InAppMessage(
            id = order.orderId,
            from = order.pharmacy.name,
            timeState = getTimeState(order.sentOn),
            text = getMessageText(
                latestCommunicationMessage = order.latestCommunicationMessage,
                pharmacy = order.pharmacy.name,
                invoiceInfo = order.invoiceInfo,
                sentOn = order.sentOn
            ),
            prescriptionsCount = order.prescriptions.size,
            tag = "",
            isUnread = order.hasUnreadMessages,
            lastMessage = order.latestCommunicationMessage,
            messageProfile = order.latestCommunicationMessage?.profile
                ?: CommunicationErpModel.CommunicationProfile.ErxCommunicationDispReq,
            taskId = order.prescriptions.firstOrNull()?.taskId
        )

    private fun getMessageText(
        latestCommunicationMessage: LastMessage?,
        pharmacy: String,
        invoiceInfo: OrderUseCaseData.InvoiceInfo,
        sentOn: Instant
    ): String = when {
        invoiceInfo.hasInvoice && invoiceInfo.invoiceSentOn == sentOn ->
            stringProvider.getString(R.string.cost_receipt_is_ready, invoiceInfo.medicationName ?: "")

        latestCommunicationMessage?.profile == CommunicationErpModel.CommunicationProfile.ErxCommunicationReply ->
            getReplyMessageText(latestCommunicationMessage.lastMessageDetails)

        latestCommunicationMessage?.profile == CommunicationErpModel.CommunicationProfile.ErxCommunicationDispReq ->
            stringProvider.getString(
                R.string.orders_prescription_sent_to,
                latestCommunicationMessage.lastMessageDetails.content ?: pharmacy
            )

        else -> stringProvider.getString(R.string.order_message_empty)
    }

    private fun getReplyMessageText(lastMessageDetails: LastMessageDetails): String = when {
        (lastMessageDetails.pickUpCodeDMC != null || lastMessageDetails.pickUpCodeHR != null) ->
            lastMessageDetails.content?.takeIf { it.isNotEmpty() }
                ?: stringProvider.getString(R.string.order_pickup_general_message)

        lastMessageDetails.link != null ->
            lastMessageDetails.content ?: stringProvider.getString(R.string.order_message_link)

        else ->
            lastMessageDetails.content ?: stringProvider.getString(R.string.order_message_empty)
    }
}
