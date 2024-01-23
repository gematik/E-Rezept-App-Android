/*
 * Copyright (c) 2024 gematik GmbH
 * 
 * Licensed under the EUPL, Version 1.2 or – as soon they will be approved by
 * the European Commission - subsequent versions of the EUPL (the Licence);
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at:
 * 
 *     https://joinup.ec.europa.eu/software/page/eupl
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the Licence is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Licence for the specific language governing permissions and
 * limitations under the Licence.
 * 
 */

package de.gematik.ti.erp.app.orders.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Divider
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.Icon
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.KeyboardArrowRight
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import de.gematik.ti.erp.app.TestTag
import de.gematik.ti.erp.app.features.R
import de.gematik.ti.erp.app.orders.presentation.MessageController
import de.gematik.ti.erp.app.orders.usecase.model.OrderUseCaseData
import de.gematik.ti.erp.app.prescription.usecase.model.Prescription
import de.gematik.ti.erp.app.prescriptionId
import de.gematik.ti.erp.app.theme.AppTheme
import de.gematik.ti.erp.app.theme.PaddingDefaults
import de.gematik.ti.erp.app.utils.compose.SpacerMedium
import de.gematik.ti.erp.app.utils.compose.SpacerXXLarge
import io.github.aakira.napier.Napier

@Suppress("LongMethod")
@OptIn(ExperimentalMaterialApi::class)
@Composable
internal fun Messages(
    listState: LazyListState,
    messageController: MessageController,
    onClickMessage: (OrderUseCaseData.Message) -> Unit,
    onClickPrescription: (String) -> Unit,
    onClickInvoiceMessage: (String) -> Unit
) {
    val order by messageController.order
    val messages by messageController.messages

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag(TestTag.Orders.Details.Content),
        state = listState,
        contentPadding = WindowInsets.navigationBars.only(WindowInsetsSides.Bottom)
            .asPaddingValues()
    ) {
        item {
            SpacerMedium()
            Text(
                stringResource(R.string.orders_history_title),
                style = AppTheme.typography.h6,
                modifier = Modifier.padding(horizontal = PaddingDefaults.Medium)
            )
            SpacerMedium()
        }

        when (messageController.state) {
            MessageController.States.HasMessages -> {
                Napier.d { "checking has message: " }
                messages.forEachIndexed { index, message ->
                    item {
                        Napier.d { "checking invoices! = ${message.hasInvoice}" }
                        ReplyMessage(
                            message = message,
                            isFirstMessage = index == 0,
                            onClick = {
                                onClickMessage(message)
                            }
                        )
                    }
                }
            }

            else -> {}
        }

        order?.let { orderDetail: OrderUseCaseData.OrderDetail ->
            itemsIndexed(
                items = orderDetail.taskDetailedBundles
            ) { _, taskBundle ->
                if (taskBundle.hasInvoice) {
                    InvoiceMessage(
                        taskId = taskBundle.taskId,
                        prescriptionName = taskBundle.prescription?.name ?: "",
                        prescriptionDate = taskBundle.prescription?.startedOn,
                        onClickCostReceiptDetail = {
                            onClickInvoiceMessage(taskBundle.taskId)
                        }
                    )
                }
            }
            item {
                DispenseMessage(
                    hasReplyMessages = messages.isNotEmpty(),
                    pharmacyName = orderDetail.pharmacy.name,
                    orderSentOn = orderDetail.sentOn
                )
                SpacerXXLarge()
            }
        }

        item {
            Divider(color = AppTheme.colors.neutral300)
            SpacerXXLarge()
            Text(
                stringResource(R.string.orders_cart_title),
                style = AppTheme.typography.h6,
                modifier = Modifier.padding(horizontal = PaddingDefaults.Medium)
            )
        }

        order?.let {
            item(key = "prescriptions") {
                Column(
                    Modifier.padding(PaddingDefaults.Medium),
                    verticalArrangement = Arrangement.spacedBy(PaddingDefaults.Small)
                ) {
                    it.taskDetailedBundles.forEachIndexed { index, taskBundle ->
                        Surface(
                            modifier = Modifier
                                .testTag(TestTag.Orders.Details.PrescriptionListItem)
                                .semantics {
                                    prescriptionId = it.taskDetailedBundles[index].taskId
                                },
                            shape = RoundedCornerShape(8.dp),
                            border = BorderStroke(1.dp, AppTheme.colors.neutral300),
                            color = AppTheme.colors.neutral050,
                            onClick = {
                                onClickPrescription(it.taskDetailedBundles[index].taskId)
                            }
                        ) {
                            Row(
                                Modifier.padding(PaddingDefaults.Medium),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                val titlePrepend =
                                    stringResource(R.string.pres_details_scanned_medication)

                                val name = when (taskBundle.prescription) {
                                    is Prescription.ScannedPrescription ->
                                        taskBundle.prescription.name
                                            ?: "$titlePrepend ${taskBundle.prescription.index}"

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
                                    Icons.Rounded.KeyboardArrowRight,
                                    contentDescription = null,
                                    tint = AppTheme.colors.neutral400
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
