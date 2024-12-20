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

package de.gematik.ti.erp.app.messages.presentation.ui.preview

import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import de.gematik.ti.erp.app.messages.domain.model.OrderUseCaseData
import de.gematik.ti.erp.app.messages.presentation.ui.preview.MessageSheetsPreviewData.EMPTY_MESSAGE_PREVIEW
import de.gematik.ti.erp.app.messages.presentation.ui.preview.MessageSheetsPreviewData.PICK_DMC_CODE_MESSAGE_PREVIEW
import de.gematik.ti.erp.app.messages.presentation.ui.preview.MessageSheetsPreviewData.PICK_DMC_HR_CODE_MESSAGE_NO_MSG_PAYLOAD_WITH_LINK_PREVIEW
import de.gematik.ti.erp.app.messages.presentation.ui.preview.MessageSheetsPreviewData.PICK_DMC_HR_CODE_MESSAGE_PREVIEW
import de.gematik.ti.erp.app.messages.presentation.ui.preview.MessageSheetsPreviewData.PICK_DMC_HR_CODE_MESSAGE_WITH_MSG_PAYLOAD_PREVIEW
import de.gematik.ti.erp.app.messages.presentation.ui.preview.MessageSheetsPreviewData.PICK_DMC_HR_CODE_MESSAGE_WITH_MSG_PAYLOAD_WITH_LINK_PREVIEW
import de.gematik.ti.erp.app.messages.presentation.ui.preview.MessageSheetsPreviewData.PICK_HR_CODE_MESSAGE_PREVIEW
import de.gematik.ti.erp.app.messages.presentation.ui.preview.MessageSheetsPreviewData.orderMessageWithEmptyMessage
import de.gematik.ti.erp.app.messages.presentation.ui.preview.MessageSheetsPreviewData.orderMessageWithOnlyMessage
import de.gematik.ti.erp.app.messages.presentation.ui.preview.MessageSheetsPreviewData.orderMessageWithOnlyURL
import de.gematik.ti.erp.app.messages.presentation.ui.preview.MessageSheetsPreviewData.orderMessageWithPickUpCode
import de.gematik.ti.erp.app.prescription.usecase.model.Prescription
import de.gematik.ti.erp.app.utils.uistate.UiState
import kotlinx.datetime.Instant

data class OrderMessageDetail(
    val orderDetail: OrderUseCaseData.OrderDetail,
    val message: OrderUseCaseData.Message
)

class MessagesPreviewParameterProvider : PreviewParameterProvider<OrderUseCaseData.Message?> {
    override val values = sequenceOf(
        EMPTY_MESSAGE_PREVIEW,
        PICK_DMC_CODE_MESSAGE_PREVIEW,
        PICK_HR_CODE_MESSAGE_PREVIEW,
        PICK_DMC_HR_CODE_MESSAGE_PREVIEW,
        PICK_DMC_HR_CODE_MESSAGE_WITH_MSG_PAYLOAD_PREVIEW,
        PICK_DMC_HR_CODE_MESSAGE_WITH_MSG_PAYLOAD_WITH_LINK_PREVIEW,
        PICK_DMC_HR_CODE_MESSAGE_NO_MSG_PAYLOAD_WITH_LINK_PREVIEW
    )
}

class MessageOrderDetailPreviewParameterProvider : PreviewParameterProvider<UiState<List<OrderMessageDetail>>> {

    override val values: Sequence<UiState<List<OrderMessageDetail>>> = sequenceOf(
        UiState.Loading(),
        UiState.Empty(),
        UiState.Error(Throwable("Error")),
        UiState.Data(
            listOf(
                orderMessageWithPickUpCode,
                orderMessageWithOnlyURL,
                orderMessageWithOnlyMessage,
                orderMessageWithEmptyMessage
            )
        )
    )
}

object MessageSheetsPreviewData {

    private val time: Instant = Instant.parse("2023-06-14T10:15:30Z")

    val ORDER_DETAIL_PREVIEW = OrderUseCaseData.OrderDetail(
        orderId = "123",
        taskDetailedBundles = listOf(
            OrderUseCaseData.TaskDetailedBundle(
                invoiceInfo = OrderUseCaseData.InvoiceInfo(
                    hasInvoice = false,
                    invoiceSentOn = time
                ),
                prescription = Prescription.ScannedPrescription(
                    taskId = "123",
                    name = "Prescription",
                    redeemedOn = time,
                    scannedOn = time,
                    index = 1,
                    communications = emptyList()
                )
            )
        ),
        sentOn = time,
        pharmacy = OrderUseCaseData.Pharmacy("123", "Pharmacy from OrderDetail"),
        hasUnreadMessages = false
    )

    val PICK_HR_CODE_MESSAGE_PREVIEW = OrderUseCaseData.Message(
        communicationId = "123",
        sentOn = time,
        message = null,
        pickUpCodeDMC = null,
        pickUpCodeHR = "pickUpCodeHR",
        link = null,
        consumed = false
    )

    val PICK_DMC_HR_CODE_MESSAGE_PREVIEW = OrderUseCaseData.Message(
        communicationId = "123",
        sentOn = time,
        message = null,
        pickUpCodeDMC = "pickUpCodeDMC",
        pickUpCodeHR = "pickUpCodeHR",
        link = null,
        consumed = false
    )

    val PICK_DMC_HR_CODE_MESSAGE_WITH_MSG_PAYLOAD_PREVIEW = OrderUseCaseData.Message(
        communicationId = "123",
        sentOn = time,
        message = "message from the pharmacy!",
        pickUpCodeDMC = "pickUpCodeDMC",
        pickUpCodeHR = "pickUpCodeHR",
        link = null,
        consumed = false
    )

    val PICK_DMC_HR_CODE_MESSAGE_WITH_MSG_PAYLOAD_WITH_LINK_PREVIEW = OrderUseCaseData.Message(
        communicationId = "123",
        sentOn = time,
        message = "message from the pharmacy!",
        pickUpCodeDMC = "pickUpCodeDMC",
        pickUpCodeHR = "pickUpCodeHR",
        link = null,
        consumed = false
    )

    val PICK_DMC_HR_CODE_MESSAGE_NO_MSG_PAYLOAD_WITH_LINK_PREVIEW = OrderUseCaseData.Message(
        communicationId = "123",
        sentOn = time,
        message = null,
        pickUpCodeDMC = "pickUpCodeDMC",
        pickUpCodeHR = "pickUpCodeHR",
        link = null,
        consumed = false
    )

    val PICK_DMC_CODE_MESSAGE_PREVIEW = OrderUseCaseData.Message(
        communicationId = "123",
        sentOn = time,
        message = null,
        pickUpCodeDMC = "pickUpCodeDMC",
        pickUpCodeHR = null,
        link = "https://this.is.a.link.de",
        consumed = false
    )

    val MESSAGE_WITH_URL_PREVIEW = OrderUseCaseData.Message(
        communicationId = "123",
        sentOn = time,
        message = null,
        pickUpCodeDMC = null,
        pickUpCodeHR = null,
        link = "https://this.is.a.link.de",
        consumed = false
    )

    val MESSAGE_WITH_Only_Message_PREVIEW = OrderUseCaseData.Message(
        communicationId = "123",
        sentOn = time,
        message = "message from the pharmacy!",
        pickUpCodeDMC = null,
        pickUpCodeHR = null,
        link = null,
        consumed = false
    )

    val EMPTY_MESSAGE_PREVIEW = OrderUseCaseData.Message(
        communicationId = "123",
        sentOn = time,
        message = null,
        pickUpCodeDMC = null,
        pickUpCodeHR = null,
        link = null,
        consumed = false
    )

    val orderMessageWithPickUpCode =
        OrderMessageDetail(
            orderDetail = ORDER_DETAIL_PREVIEW,
            message = PICK_DMC_CODE_MESSAGE_PREVIEW

        )

    val orderMessageWithOnlyURL =
        OrderMessageDetail(
            orderDetail = ORDER_DETAIL_PREVIEW,
            message = MESSAGE_WITH_URL_PREVIEW

        )

    val orderMessageWithOnlyMessage =
        OrderMessageDetail(
            orderDetail = ORDER_DETAIL_PREVIEW,
            message = MESSAGE_WITH_Only_Message_PREVIEW
        )

    val orderMessageWithEmptyMessage =
        OrderMessageDetail(
            orderDetail = ORDER_DETAIL_PREVIEW,
            message = EMPTY_MESSAGE_PREVIEW
        )
}
