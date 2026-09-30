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

package de.gematik.ti.erp.app.messages.ui.components

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import de.gematik.ti.erp.app.TestTag
import de.gematik.ti.erp.app.column.PrescriptionListForMessages
import de.gematik.ti.erp.app.core.R
import de.gematik.ti.erp.app.core.complexAutoSaver
import de.gematik.ti.erp.app.messages.domain.model.OrderUseCaseData
import de.gematik.ti.erp.app.messages.ui.model.DispenseMessageUiModel.Companion.toDispenseMessage
import de.gematik.ti.erp.app.messages.ui.model.InvoiceMessageUiModel.Companion.toInvoiceMessage
import de.gematik.ti.erp.app.messages.ui.model.MessageDetailBundle
import de.gematik.ti.erp.app.messages.ui.model.MessageType.DISPENSE
import de.gematik.ti.erp.app.messages.ui.model.MessageType.EU_REDEEM
import de.gematik.ti.erp.app.messages.ui.model.MessageType.INVOICE
import de.gematik.ti.erp.app.messages.ui.model.MessageType.IN_APP
import de.gematik.ti.erp.app.messages.ui.model.MessageType.REPLY
import de.gematik.ti.erp.app.messages.ui.model.ReplyMessageUiModel.Companion.toReplyMessage
import de.gematik.ti.erp.app.semantics.semanticsHeading
import de.gematik.ti.erp.app.theme.AppTheme
import de.gematik.ti.erp.app.theme.PaddingDefaults
import de.gematik.ti.erp.app.utils.SpacerMedium
import de.gematik.ti.erp.app.utils.compose.UiStateMachine
import de.gematik.ti.erp.app.utils.uistate.UiState

@Suppress("LongMethod", "CyclomaticComplexMethod")
@OptIn(ExperimentalMaterialApi::class)
@Composable
internal fun MessageDetailContent(
    modifier: Modifier = Modifier,
    listState: LazyListState,
    innerPadding: PaddingValues,
    order: UiState<OrderUseCaseData.OrderDetail>,
    messages: List<OrderUseCaseData.Message>,
    isTranslationInProgress: Map<String, Boolean>,
    isTranslationsAllowed: Boolean,
    showTranslationFeature: Boolean,
    onClickReplyMessage: (OrderUseCaseData.Message) -> Unit,
    onClickPrescription: (String) -> Unit,
    onClickInvoiceMessage: (String) -> Unit,
    onClickTranslation: (String, String) -> Unit,
    onClickPharmacy: () -> Unit
) {
    val combinedMessages = rememberSaveable(messages, order, saver = complexAutoSaver()) {
        combineMessagesAndSort(messages, order)
    }
    LazyColumn(
        modifier = Modifier
            .testTag(TestTag.Orders.Details.Content)
            .then(modifier),
        state = listState,
        contentPadding = innerPadding
    ) {
        item {
            SpacerMedium()
            Text(
                stringResource(R.string.messages_history_title),
                style = AppTheme.typography.h6,
                modifier = Modifier.padding(horizontal = PaddingDefaults.Medium).semanticsHeading()
            )
        }

        combinedMessages.forEachIndexed { index, displayMessage ->
            val isFirstMessage = index == 0
            val isLastMessage = index == combinedMessages.size - 1

            when (displayMessage.type) {
                REPLY -> {
                    displayMessage.message?.let { message ->
                        val isCurrentMessageBeingTranslated = isTranslationInProgress[message.communicationId] ?: false
                        item {
                            ReplyMessage(
                                item = message.toReplyMessage(isFirstMessage, isLastMessage),
                                isTranslationsAllowed = isTranslationsAllowed,
                                showTranslationFeature = showTranslationFeature,
                                isTranslationInProgress = isCurrentMessageBeingTranslated,
                                onClick = { onClickReplyMessage(message) },
                                onClickTranslation = {
                                    onClickTranslation(message.communicationId, it)
                                }
                            )
                        }
                    }
                }

                INVOICE -> {
                    item {
                        InvoiceMessage(
                            item = displayMessage.taskBundle?.toInvoiceMessage(isFirstMessage),
                            onClickCostReceiptDetail = onClickInvoiceMessage
                        )
                    }
                }

                DISPENSE -> {
                    displayMessage.orderDetail?.let { message ->
                        item {
                            DispenseMessage(
                                item = message.toDispenseMessage(isFirstMessage && isLastMessage),
                                onClickPharmacy = onClickPharmacy
                            )
                        }
                    }
                }

                IN_APP -> {
                }

                EU_REDEEM -> {
                }
            }
        }
        if (combinedMessages.isNotEmpty()) {
            item {
                MessagePrescriptionDividerWithTitle()
            }
            item {
                UiStateMachine(
                    state = order,
                    onContent = { orderDetail ->
                        PrescriptionListForMessages(
                            items = orderDetail.taskDetailedBundles.map { it.prescription?.taskId.orEmpty() },
                            onName = { taskId ->
                                val prescription = orderDetail.taskDetailedBundles
                                    .firstOrNull { it.prescription?.taskId == taskId }
                                    ?.prescription

                                prescription?.name?.takeIf { it.isNotBlank() }
                                    ?: prescription?.medicationName()
                                    ?: ""
                            },
                            onClick = onClickPrescription
                        )
                    }
                )
            }
        }
    }
}

fun combineMessagesAndSort(
    messages: List<OrderUseCaseData.Message>,
    order: UiState<OrderUseCaseData.OrderDetail>
): List<MessageDetailBundle> {
    val messageItems = messages.map {
        MessageDetailBundle(
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
                MessageDetailBundle(
                    type = INVOICE,
                    orderDetail = order.data,
                    taskBundle = taskBundle,
                    timestamp = it
                )
            }
        }.orEmpty()

    val dispenseItem = order.data?.let { orderDetail ->
        MessageDetailBundle(
            type = DISPENSE,
            orderDetail = orderDetail,
            timestamp = orderDetail.sentOn
        )
    }

    return (messageItems + invoiceItems + listOfNotNull(dispenseItem)).sortedByDescending { it.timestamp }
}
