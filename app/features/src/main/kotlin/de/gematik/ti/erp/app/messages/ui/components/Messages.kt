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

package de.gematik.ti.erp.app.messages.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Divider
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.Icon
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowRight
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextOverflow
import de.gematik.ti.erp.app.TestTag
import de.gematik.ti.erp.app.features.R
import de.gematik.ti.erp.app.messages.domain.model.InAppMessage
import de.gematik.ti.erp.app.messages.domain.model.OrderUseCaseData
import de.gematik.ti.erp.app.messages.ui.model.MessageDetailCombinedMessage
import de.gematik.ti.erp.app.messages.ui.model.MessageType.DISPENSE
import de.gematik.ti.erp.app.messages.ui.model.MessageType.INVOICE
import de.gematik.ti.erp.app.messages.ui.model.MessageType.IN_APP
import de.gematik.ti.erp.app.messages.ui.model.MessageType.REPLY
import de.gematik.ti.erp.app.prescription.usecase.model.Prescription
import de.gematik.ti.erp.app.prescriptionId
import de.gematik.ti.erp.app.theme.AppTheme
import de.gematik.ti.erp.app.theme.PaddingDefaults
import de.gematik.ti.erp.app.theme.SizeDefaults
import de.gematik.ti.erp.app.utils.SpacerMedium
import de.gematik.ti.erp.app.utils.SpacerXXLarge
import de.gematik.ti.erp.app.utils.compose.UiStateMachine
import de.gematik.ti.erp.app.utils.uistate.UiState
import kotlinx.datetime.Instant

@Suppress("LongMethod", "CyclomaticComplexMethod")
@OptIn(ExperimentalMaterialApi::class)
@Composable
internal fun Messages(
    modifier: Modifier = Modifier,
    listState: LazyListState,
    order: UiState<OrderUseCaseData.OrderDetail>,
    messages: List<OrderUseCaseData.Message>,
    inAppMessages: List<InAppMessage?>,
    onClickReplyMessage: (OrderUseCaseData.Message) -> Unit,
    onClickPrescription: (String) -> Unit,
    onClickInvoiceMessage: (String) -> Unit,
    onClickPharmacy: () -> Unit
) {
    val combinedMessages = remember(messages, order, inAppMessages) {
        combineMessagesAndSort(messages, order, inAppMessages)
    }
    LazyColumn(
        modifier = Modifier
            .testTag(TestTag.Orders.Details.Content)
            .then(modifier),
        state = listState,
        contentPadding = WindowInsets.navigationBars.only(WindowInsetsSides.Bottom)
            .asPaddingValues()
    ) {
        item {
            SpacerMedium()
            Text(
                stringResource(R.string.messages_history_title),
                style = AppTheme.typography.h6,
                modifier = Modifier.padding(horizontal = PaddingDefaults.Medium)
            )
        }

        combinedMessages.forEachIndexed { index, displayMessage ->
            val isFirstMessage = index == 0
            val isLastMessage = index == combinedMessages.size - 1

            when (displayMessage.type) {
                REPLY -> {
                    displayMessage.message?.let { message ->
                        item {
                            ReplyMessage(
                                message = message,
                                isFirstMessage = isFirstMessage,
                                isLastMessage = isLastMessage,
                                onClick = { onClickReplyMessage(message) }
                            )
                        }
                    }
                }

                INVOICE -> {
                    item {
                        InvoiceMessage(
                            prescriptionName = displayMessage.orderDetail?.taskDetailedBundles?.firstOrNull()?.prescription?.name ?: "",
                            invoiceDate = displayMessage.orderDetail?.taskDetailedBundles?.firstOrNull()?.invoiceInfo?.invoiceSentOn,
                            isFirstMessage = isFirstMessage,
                            onClickCostReceiptDetail = {
                                displayMessage.orderDetail?.taskDetailedBundles?.firstOrNull()?.prescription?.taskId?.let {
                                    onClickInvoiceMessage(it)
                                }
                            }
                        )
                    }
                }

                DISPENSE -> {
                    item {
                        DispenseMessage(
                            pharmacyName = displayMessage.orderDetail?.pharmacy?.name ?: "",
                            orderSentOn = displayMessage.timestamp,
                            isOnlyMessage = isFirstMessage && isLastMessage,
                            onClickPharmacy = onClickPharmacy,
                            taskDetails = displayMessage.orderDetail?.taskDetailedBundles
                        )
                    }
                }

                IN_APP -> {
                    displayMessage.message?.let { message ->
                        item {
                            InAppMessage(
                                message = message,
                                isFirstMessage = isFirstMessage,
                                isLastMessage = isLastMessage
                            )
                        }
                    }
                }
            }
        }
        if (combinedMessages.isNotEmpty() && combinedMessages[0].type != IN_APP) {
            item {
                SpacerXXLarge()
                Divider(color = AppTheme.colors.neutral300)
                SpacerXXLarge()
                Text(
                    text = stringResource(R.string.messages_cart_title),
                    style = AppTheme.typography.h6,
                    modifier = Modifier.padding(horizontal = PaddingDefaults.Medium)
                )
            }
            item {
                UiStateMachine(
                    state = order,
                    onContent = { orderDetail ->
                        Column(
                            Modifier.padding(PaddingDefaults.Medium),
                            verticalArrangement = Arrangement.spacedBy(PaddingDefaults.Small)
                        ) {
                            orderDetail.taskDetailedBundles.forEachIndexed { index, taskBundle ->
                                Surface(
                                    modifier = Modifier
                                        .testTag(TestTag.Orders.Details.PrescriptionListItem)
                                        .semantics {
                                            prescriptionId = orderDetail.taskDetailedBundles[index].prescription?.taskId
                                        },
                                    shape = RoundedCornerShape(SizeDefaults.one),
                                    border = BorderStroke(SizeDefaults.eighth, AppTheme.colors.neutral300),
                                    color = AppTheme.colors.neutral050,
                                    onClick = {
                                        orderDetail.taskDetailedBundles[index].prescription?.taskId?.let { taskId ->
                                            onClickPrescription(taskId)
                                        }
                                    }
                                ) {
                                    Row(
                                        Modifier.padding(PaddingDefaults.Medium),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        val name = when (taskBundle.prescription) {
                                            is Prescription.ScannedPrescription -> taskBundle.prescription.name
                                            is Prescription.SyncedPrescription -> taskBundle.prescription.name ?: ""
                                            else -> ""
                                        }
                                        Text(
                                            name,
                                            style = AppTheme.typography.subtitle1,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis,
                                            modifier = Modifier.weight(1f)
                                        )
                                        SpacerMedium()
                                        Icon(
                                            Icons.AutoMirrored.Rounded.KeyboardArrowRight,
                                            contentDescription = null,
                                            tint = AppTheme.colors.neutral400
                                        )
                                    }
                                }
                            }
                        }
                    }
                )
            }
        }
    }
}

fun combineMessagesAndSort(
    messages: List<OrderUseCaseData.Message>,
    order: UiState<OrderUseCaseData.OrderDetail>,
    inAppMessages: List<InAppMessage?>
): List<MessageDetailCombinedMessage> {
    val messageItems = messages.map {
        MessageDetailCombinedMessage(
            type = REPLY,
            message = it,
            timestamp = it.sentOn,
            prescriptions = it.prescriptions
        )
    }

    val invoiceItems = order.data?.taskDetailedBundles
        ?.filter { it.invoiceInfo.hasInvoice }
        ?.mapNotNull { taskBundle ->
            taskBundle.invoiceInfo.invoiceSentOn?.let {
                MessageDetailCombinedMessage(
                    type = INVOICE,
                    orderDetail = order.data,
                    timestamp = it
                )
            }
        }.orEmpty()

    val dispenseItem = order.data?.let { orderDetail ->
        MessageDetailCombinedMessage(
            type = DISPENSE,
            orderDetail = orderDetail,
            timestamp = orderDetail.sentOn
        )
    }

    val inAppMessageItems = inAppMessages.map { local ->
        MessageDetailCombinedMessage(
            type = IN_APP,
            message = OrderUseCaseData.Message(
                content = local?.text.orEmpty(),
                additionalInfo = local?.tag.orEmpty(),
                sentOn = Instant.parse(local?.timeState?.timestamp.toString()),
                communicationId = local?.id.orEmpty(),
                link = null,
                consumed = true,
                pickUpCodeDMC = null,
                pickUpCodeHR = null,
                prescriptions = emptyList()
            ),
            timestamp = Instant.parse(local?.timeState?.timestamp.toString())
        )
    }

    return (inAppMessageItems + messageItems + invoiceItems + listOfNotNull(dispenseItem)).sortedByDescending { it.timestamp }
}
