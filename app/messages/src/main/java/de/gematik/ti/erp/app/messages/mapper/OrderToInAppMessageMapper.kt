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
import de.gematik.ti.erp.app.communication.model.payload.CommunicationAvailabilityResponseErpModel
import de.gematik.ti.erp.app.communication.model.payload.CommunicationDeliveryStatusErpModel
import de.gematik.ti.erp.app.communication.model.payload.CommunicationReplyDeliveryStatusPayloadErpModel
import de.gematik.ti.erp.app.communication.model.payload.CommunicationReplyReservationStatusPayloadErpModel
import de.gematik.ti.erp.app.communication.model.payload.CommunicationSupplyOptionTypeErpModel
import de.gematik.ti.erp.app.core.R
import de.gematik.ti.erp.app.messages.domain.model.MessagesStringProvider
import de.gematik.ti.erp.app.messages.domain.model.OrderUseCaseData
import de.gematik.ti.erp.app.messages.model.InAppMessage
import de.gematik.ti.erp.app.messages.model.InAppMessageStatus
import de.gematik.ti.erp.app.messages.model.LastMessage
import de.gematik.ti.erp.app.task.model.TaskErpModel
import de.gematik.ti.erp.app.task.model.TaskStateErpModel
import de.gematik.ti.erp.app.task.model.TaskStatusEnum
import de.gematik.ti.erp.app.timestate.getTimeState
import kotlinx.datetime.Instant

class OrderToInAppMessageMapper(
    private val stringProvider: MessagesStringProvider
) {
    fun map(order: OrderUseCaseData.Order): InAppMessage {
        val messageText = getMessageText(
            latestCommunicationMessage = order.latestCommunicationMessage,
            pharmacy = order.pharmacy.name,
            invoiceInfo = order.invoiceInfo,
            sentOn = order.sentOn
        )
        val taskStatus = order.prescriptions.firstNotNullOfOrNull { (it as? TaskErpModel.Synced)?.status }
        val isProvided = order.prescriptions.any { (it as? TaskErpModel.Synced)?.state() is TaskStateErpModel.Provided }
        val messageProfile = order.latestCommunicationMessage?.profile
            ?: CommunicationErpModel.CommunicationProfile.ErxCommunicationDispReq
        val orderStatus = determineOrderStatus(
            taskStatus = taskStatus,
            isProvided = isProvided,
            messageProfile = messageProfile,
            latestCommunicationMessage = order.latestCommunicationMessage,
            messageText = messageText,
            supplyOption = order.supplyOption
        )

        return InAppMessage(
            id = order.orderId,
            from = order.pharmacy.name,
            timeState = getTimeState(order.sentOn),
            text = messageText,
            prescriptionsCount = order.prescriptions.size,
            tag = "",
            isUnread = order.hasUnreadMessages,
            lastMessage = order.latestCommunicationMessage,
            messageProfile = messageProfile,
            taskId = order.prescriptions.firstOrNull()?.taskId,
            supplyOption = order.supplyOption,
            taskStatus = taskStatus,
            orderStatus = orderStatus
        )
    }

    private fun determineOrderStatus(
        taskStatus: TaskStatusEnum?,
        isProvided: Boolean,
        messageProfile: CommunicationErpModel.CommunicationProfile,
        latestCommunicationMessage: LastMessage?,
        messageText: String?,
        supplyOption: CommunicationSupplyOptionTypeErpModel?
    ): InAppMessageStatus {
        val payload = latestCommunicationMessage?.payload
        val reservationPayload = latestCommunicationMessage?.latestReservationPayload ?: (payload as? CommunicationReplyReservationStatusPayloadErpModel)
        val deliveryPayload = latestCommunicationMessage?.latestDeliveryPayload ?: (payload as? CommunicationReplyDeliveryStatusPayloadErpModel)

        if (taskStatus == TaskStatusEnum.InProgress) {
            if (deliveryPayload != null && deliveryPayload.deliveryStatus == CommunicationDeliveryStatusErpModel.Incident) {
                return InAppMessageStatus.NOT_AVAILABLE
            }

            if (reservationPayload != null) {
                when (reservationPayload.readyForCollection) {
                    CommunicationAvailabilityResponseErpModel.NextDay,
                    CommunicationAvailabilityResponseErpModel.NextDayAM,
                    CommunicationAvailabilityResponseErpModel.NextDayPM -> {
                        if (supplyOption == CommunicationSupplyOptionTypeErpModel.DELIVERY ||
                            supplyOption == CommunicationSupplyOptionTypeErpModel.SHIPMENT
                        ) {
                            return InAppMessageStatus.PENDING
                        } else {
                            return InAppMessageStatus.READY_TOMORROW
                        }
                    }

                    CommunicationAvailabilityResponseErpModel.Immediately,
                    CommunicationAvailabilityResponseErpModel.SameDay -> {
                        return InAppMessageStatus.READY_FOR_PICKUP
                    }

                    else -> Unit
                }
            }
        }

        if (taskStatus == TaskStatusEnum.Completed) {
            return InAppMessageStatus.PICKED_UP
        }

        if (isProvided) {
            return InAppMessageStatus.READY_FOR_PICKUP
        }

        val normalizedText = messageText.orEmpty().lowercase()
        val detailsContent = latestCommunicationMessage?.lastMessageDetails?.content.orEmpty().lowercase()
        val reservationStatus = detailsContent.toReservationAvailabilityStatus()

        // 1. Initial request sent ("Warte auf Antwort" in PrescriptionList -> "Bestellt" in MessageList)
        if (messageProfile == CommunicationErpModel.CommunicationProfile.ErxCommunicationDispReq &&
            taskStatus != TaskStatusEnum.InProgress
        ) {
            return InAppMessageStatus.ORDERED
        }

        // 2. Reservation status replies drive the compact list status directly
        when (reservationStatus) {
            CommunicationAvailabilityResponseErpModel.Immediately,
            CommunicationAvailabilityResponseErpModel.SameDay -> return InAppMessageStatus.READY_FOR_PICKUP

            CommunicationAvailabilityResponseErpModel.NextDay,
            CommunicationAvailabilityResponseErpModel.NextDayAM,
            CommunicationAvailabilityResponseErpModel.NextDayPM -> return InAppMessageStatus.READY_TOMORROW

            CommunicationAvailabilityResponseErpModel.NotAvailable -> return InAppMessageStatus.NOT_AVAILABLE
            CommunicationAvailabilityResponseErpModel.Unknown -> return InAppMessageStatus.PENDING
            null -> Unit
        }

        // 3. Check for explicit "not available" or "not deliverable"
        if (normalizedText.contains("nicht verfügbar") ||
            normalizedText.contains("nicht verfugbar") ||
            normalizedText.contains("not available") ||
            normalizedText.contains("nicht lieferbar")
        ) {
            return InAppMessageStatus.NOT_AVAILABLE
        }

        // 4. Check for "Ready Tomorrow"
        if (messageProfile == CommunicationErpModel.CommunicationProfile.ErxCommunicationReply &&
            (normalizedText.contains("morgen") || normalizedText.contains("tomorrow"))
        ) {
            return InAppMessageStatus.READY_TOMORROW
        }

        // 5. Check for explicit pickup codes or "ready for pickup" or technical "Ready" status ("Einlösbar" -> "Abholbereit")
        if (latestCommunicationMessage?.lastMessageDetails?.pickUpCodeDMC != null ||
            latestCommunicationMessage?.lastMessageDetails?.pickUpCodeHR != null ||
            normalizedText.contains("abholbereit") ||
            normalizedText.contains("ready for pickup") ||
            normalizedText.contains("einlösbar") ||
            taskStatus == TaskStatusEnum.Ready
        ) {
            return InAppMessageStatus.READY_FOR_PICKUP
        }

        // 6. Check for "in progress" / "Wird bearbeitet" in PrescriptionList -> "Wird verpackt" in MessageList
        if (detailsContent.contains("unknown") ||
            detailsContent.contains("abholinfo=unknown") ||
            normalizedText.contains("abholinfo=unknown") ||
            normalizedText.contains("bearbeitet") ||
            normalizedText.contains("in progress") ||
            taskStatus == TaskStatusEnum.InProgress
        ) {
            return InAppMessageStatus.PENDING
        }

        // 7. Fallback for replies without a reservation-status payload stays on "Wird verpackt"
        if (messageProfile == CommunicationErpModel.CommunicationProfile.ErxCommunicationReply) {
            return InAppMessageStatus.PENDING
        }

        return InAppMessageStatus.ORDERED
    }

    private fun getMessageText(
        latestCommunicationMessage: LastMessage?,
        pharmacy: String,
        invoiceInfo: OrderUseCaseData.InvoiceInfo,
        sentOn: Instant
    ): String = when {
        invoiceInfo.hasInvoice && invoiceInfo.invoiceSentOn == sentOn ->
            stringProvider.getString(R.string.cost_receipt_is_ready, invoiceInfo.medicationName ?: "")

        latestCommunicationMessage?.profile == CommunicationErpModel.CommunicationProfile.ErxCommunicationReply ->
            getReplyMessageText(latestCommunicationMessage)

        latestCommunicationMessage?.profile == CommunicationErpModel.CommunicationProfile.ErxCommunicationDispReq ->
            stringProvider.getString(
                R.string.orders_prescription_sent_to,
                latestCommunicationMessage.lastMessageDetails.content ?: pharmacy
            )

        else -> stringProvider.getString(R.string.order_message_empty)
    }

    private fun getReplyMessageText(latestCommunicationMessage: LastMessage): String {
        val reservationPayload = latestCommunicationMessage.latestReservationPayload
            ?: (latestCommunicationMessage.payload as? CommunicationReplyReservationStatusPayloadErpModel)

        if (reservationPayload != null) {
            when (reservationPayload.readyForCollection) {
                CommunicationAvailabilityResponseErpModel.NextDay,
                CommunicationAvailabilityResponseErpModel.NextDayAM,
                CommunicationAvailabilityResponseErpModel.NextDayPM -> {
                    return stringProvider.getString(R.string.message_card_reservation_state_nextday_am_title)
                }

                CommunicationAvailabilityResponseErpModel.Immediately,
                CommunicationAvailabilityResponseErpModel.SameDay -> {
                    return stringProvider.getString(R.string.message_card_pickupcode_title)
                }

                else -> Unit
            }
        }

        val lastMessageDetails = latestCommunicationMessage.lastMessageDetails
        return when {
            (lastMessageDetails.pickUpCodeDMC != null || lastMessageDetails.pickUpCodeHR != null) ->
                lastMessageDetails.content?.takeIf { it.isNotEmpty() }
                    ?: stringProvider.getString(R.string.order_pickup_general_message)

            lastMessageDetails.link != null ->
                lastMessageDetails.content ?: stringProvider.getString(R.string.order_message_link)

            else ->
                lastMessageDetails.content ?: stringProvider.getString(R.string.order_message_empty)
        }
    }

    private fun String.toReservationAvailabilityStatus(): CommunicationAvailabilityResponseErpModel? =
        CommunicationAvailabilityResponseErpModel.entries.find { it.name.equals(trim(), ignoreCase = true) }
}
