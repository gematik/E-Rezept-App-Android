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

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.Divider
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.KeyboardArrowRight
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import de.gematik.ti.erp.app.TestTag
import de.gematik.ti.erp.app.orders.presentation.OrderController
import de.gematik.ti.erp.app.orders.presentation.rememberOrderState
import de.gematik.ti.erp.app.profiles.usecase.model.ProfilesUseCaseData
import de.gematik.ti.erp.app.profiles.usecase.model.ProfilesUseCaseData.Profile.Companion.connectionState
import de.gematik.ti.erp.app.theme.AppTheme
import de.gematik.ti.erp.app.theme.PaddingDefaults
import de.gematik.ti.erp.app.utils.compose.SpacerTiny
import de.gematik.ti.erp.app.utils.compose.timeDescription

@Composable
internal fun Orders(
    activeProfile: ProfilesUseCaseData.Profile,
    onClickOrder: (orderId: String) -> Unit,
    onClickRefresh: () -> Unit,
    onElevateTopBar: (Boolean) -> Unit
) {
    val listState = rememberLazyListState()
    val orderState = rememberOrderState(activeProfile.id)
    val orders by orderState.orders

    LaunchedEffect(Unit) {
        snapshotFlow {
            listState.firstVisibleItemIndex > 0 || listState.firstVisibleItemScrollOffset > 0
        }.collect {
            onElevateTopBar(it)
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag(TestTag.Orders.Content),
        state = listState
    ) {
        when (orderState.state) {
            OrderController.States.LoadingOrders -> {
                // keep empty
                item {}
            }

            OrderController.States.HasOrders -> {
                orders.forEachIndexed { index, order ->
                    item {
                        val sentOn by timeDescription(order.sentOn)
                        Order(
                            pharmacy = order.pharmacy.pharmacyName(),
                            time = sentOn,
                            hasUnreadMessages = order.hasUnreadMessages,
                            nrOfPrescriptions = order.taskIds.size,
                            onClick = {
                                onClickOrder(order.orderId)
                            }
                        )
                        if (index < orders.size - 1) {
                            Divider(Modifier.padding(start = PaddingDefaults.Medium))
                        }
                    }
                }
            }

            OrderController.States.NoOrders -> {
                item {
                    val connectionState = activeProfile.connectionState()
                    OrderEmptyScreen(connectionState, onClickRefresh = onClickRefresh)
                }
            }
        }
    }
}

@Composable
private fun Order(
    pharmacy: String,
    time: String,
    hasUnreadMessages: Boolean,
    nrOfPrescriptions: Int,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .clickable(onClick = onClick)
            .padding(PaddingDefaults.Medium)
            .fillMaxWidth()
            .testTag(TestTag.Orders.OrderListItem),
        horizontalArrangement = Arrangement.spacedBy(PaddingDefaults.Small),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(Modifier.weight(1f)) {
            Text(pharmacy, style = AppTheme.typography.subtitle1)
            SpacerTiny()
            Text(time, style = AppTheme.typography.body2l)
        }
        if (hasUnreadMessages) {
            NewLabel()
        } else {
            PrescriptionLabel(nrOfPrescriptions)
        }
        Icon(Icons.Rounded.KeyboardArrowRight, contentDescription = null, tint = AppTheme.colors.neutral400)
    }
}
