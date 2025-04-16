/*
 * Copyright 2025, gematik GmbH
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

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.Divider
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowRight
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.style.TextOverflow
import de.gematik.ti.erp.app.TestTag
import de.gematik.ti.erp.app.features.R
import de.gematik.ti.erp.app.messages.model.CommunicationProfile
import de.gematik.ti.erp.app.messages.model.InAppMessage
import de.gematik.ti.erp.app.theme.AppTheme
import de.gematik.ti.erp.app.theme.PaddingDefaults
import de.gematik.ti.erp.app.theme.SizeDefaults
import de.gematik.ti.erp.app.utils.SpacerTiny
import de.gematik.ti.erp.app.utils.SpacerXXXLarge
import de.gematik.ti.erp.app.utils.compose.ErrorScreenComponent
import de.gematik.ti.erp.app.utils.compose.UiStateMachine
import de.gematik.ti.erp.app.utils.compose.annotatedPluralsResource
import de.gematik.ti.erp.app.utils.compose.fullscreen.Center
import de.gematik.ti.erp.app.utils.extensions.DateTimeUtils
import de.gematik.ti.erp.app.utils.uistate.UiState
import java.time.format.DateTimeFormatter

@Composable
internal fun Orders(
    modifier: Modifier = Modifier,
    listState: LazyListState,
    ordersData: UiState<List<InAppMessage>>,
    dateFormatter: DateTimeFormatter = DateTimeUtils.dateFormatter,
    onClickOrder: (orderId: String, isLocalMessage: Boolean) -> Unit,
    onClickRetry: () -> Unit
) {
    Box(modifier = Modifier.fillMaxSize()) {
        UiStateMachine(
            state = ordersData,
            onEmpty = {
                Center {
                    NoOrders { onClickRetry() }
                }
            },
            onLoading = {
                Center {
                    MessagesLoadingShimmer()
                }
            },
            onError = {
                ErrorScreenComponent(
                    onClickRetry = onClickRetry
                )
            }
        ) { orders ->
            LazyColumn(
                modifier = modifier.testTag(TestTag.Orders.Content),
                state = listState
            ) {
                orders.forEachIndexed { index, order ->
                    item {
                        val date = messageTimeStateParser(timeState = order.timeState, dateFormatter = dateFormatter)
                        Order(
                            pharmacy = order.from,
                            date = date,
                            hasUnreadMessages = order.isUnread,
                            prescriptionsCount = order.prescriptionsCount,
                            text = order.text ?: "",
                            onClick = {
                                onClickOrder(order.id, order.messageProfile == CommunicationProfile.InApp)
                            }
                        )
                        if (index < orders.size - 1) {
                            Divider(
                                Modifier.padding(start = PaddingDefaults.Medium)
                            )
                        }
                    }
                }
                item {
                    SpacerXXXLarge()
                }
            }
        }
    }
}

@Composable
private fun Order(
    modifier: Modifier = Modifier,
    pharmacy: String,
    date: String,
    text: String,
    hasUnreadMessages: Boolean,
    prescriptionsCount: Int,
    onClick: () -> Unit
) {
    Row(
        modifier = modifier
            .clickable(onClick = onClick)
            .padding(PaddingDefaults.Medium)
            .fillMaxWidth()
            .testTag(TestTag.Orders.OrderListItem),
        horizontalArrangement = Arrangement.spacedBy(PaddingDefaults.Small),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(Modifier.weight(1f)) {
            Text(
                text = pharmacy,
                style = AppTheme.typography.subtitle1,
                color = AppTheme.colors.neutral900,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            SpacerTiny()
            Text(
                text = text,
                style = AppTheme.typography.subtitle2l,
                color = AppTheme.colors.neutral900,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            SpacerTiny()
            date?.let {
                Text(
                    text = date,
                    style = AppTheme.typography.body2l,
                    color = AppTheme.colors.neutral600
                )
            }
        }
        when {
            hasUnreadMessages -> NewMessageLabel()
            else -> if (prescriptionsCount != 0) {
                PrescriptionCountLabel(prescriptionsCount)
            }
        }

        Icon(
            imageVector = Icons.AutoMirrored.Rounded.KeyboardArrowRight,
            contentDescription = null,
            tint = AppTheme.colors.neutral400
        )
    }
}

@Composable
private fun NewMessageLabel() {
    Box(
        Modifier
            .clip(CircleShape)
            .background(AppTheme.colors.primary100)
            .padding(horizontal = PaddingDefaults.Small, vertical = SizeDefaults.threeSeventyFifth),
        contentAlignment = Alignment.Center
    ) {
        Text(
            stringResource(R.string.orders_label_new),
            style = AppTheme.typography.caption2,
            color = AppTheme.colors.primary900
        )
    }
}

@Composable
private fun PrescriptionCountLabel(count: Int) {
    Box(
        Modifier
            .clip(CircleShape)
            .background(AppTheme.colors.neutral100)
            .padding(horizontal = PaddingDefaults.Small, vertical = SizeDefaults.threeSeventyFifth),
        contentAlignment = Alignment.Center
    ) {
        Text(
            annotatedPluralsResource(
                R.plurals.orders_plurals_label_nr_of_prescriptions,
                count,
                AnnotatedString(count.toString())
            ),
            style = AppTheme.typography.caption2,
            color = AppTheme.colors.neutral600
        )
    }
}
