/*
 * Copyright (c) 2021 gematik GmbH
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

package de.gematik.ti.erp.app.pharmacy.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.ContentAlpha
import androidx.compose.material.Icon
import androidx.compose.material.LocalContentAlpha
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationCity
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import de.gematik.ti.erp.app.R
import de.gematik.ti.erp.app.pharmacy.usecase.model.UIPrescriptionOrder
import de.gematik.ti.erp.app.prescription.repository.RemoteRedeemOption
import de.gematik.ti.erp.app.theme.AppTheme
import de.gematik.ti.erp.app.utils.compose.HintActionButton
import de.gematik.ti.erp.app.utils.compose.HintCard
import de.gematik.ti.erp.app.utils.compose.HintLargeImage
import de.gematik.ti.erp.app.utils.compose.Spacer16
import de.gematik.ti.erp.app.utils.compose.Spacer24
import de.gematik.ti.erp.app.utils.compose.Spacer48
import de.gematik.ti.erp.app.utils.compose.handleIntent
import de.gematik.ti.erp.app.utils.compose.providePhoneIntent
import kotlinx.coroutines.flow.collect

@Composable
fun CourierDelivery(
    navigation: NavController,
    viewModel: PharmacySearchViewModel,
    taskIds: List<String>,
    pharmacyName: String,
    telematikId: String,
    pharmacyPhone: String
) {
    val prescriptions by produceState(initialValue = listOf<UIPrescriptionOrder>()) {
        taskIds.takeIf { it.isNotEmpty() }?.let { ids ->
            viewModel.fetchSelectedOrders(ids).collect { value = it }
        }
    }
    val header = stringResource(id = R.string.pharm_courier_header)
    val alpha = if (viewModel.uiState.loading) ContentAlpha.medium else ContentAlpha.high
    val context = LocalContext.current
    HeaderWithScaffold(
        navController = navigation,
        viewModel = viewModel,
        telematikId = telematikId,
        redeemOption = RemoteRedeemOption.Delivery,
        header = header,
        uiState = viewModel.uiState
    ) {
        item {
            DescriptionHeader(pharmacyName = pharmacyName)
            Spacer48()
        }
        item {
            CompositionLocalProvider(LocalContentAlpha provides alpha) {
                Text(
                    text = stringResource(id = R.string.pharm_reserve_delivery_address),
                    style = MaterialTheme.typography.h6
                )
            }
            Spacer16()
        }
        item {
            if (prescriptions.isNotEmpty()) {
                DeliveryAddress(alpha) {
                    Row(modifier = Modifier.padding(16.dp)) {
                        Icon(
                            Icons.Default.LocationCity, contentDescription = "",
                            tint = AppTheme.colors.neutral600
                        )
                        Spacer16()
                        Column {
                            Text(
                                text = prescriptions[0].patientName,
                                style = MaterialTheme.typography.body1,
                                color = AppTheme.colors.neutral600
                            )
                            Text(
                                text = prescriptions[0].address,
                                style = MaterialTheme.typography.body1,
                                color = AppTheme.colors.neutral600
                            )
                        }
                    }
                }
                Spacer16()
            }
        }
        item {
            HintCard(
                image = {
                    HintLargeImage(
                        painterResource(R.drawable.calling_lady),
                        innerPadding = it
                    )
                },
                title = { Text(stringResource(R.string.pharm_delivery_card_help)) },
                body = { Text(stringResource(R.string.pharm_delivery_card_message)) },
                action = {
                    HintActionButton(stringResource(R.string.pharm_delivery_card_call)) {
                        context.handleIntent(providePhoneIntent(pharmacyPhone))
                    }
                }
            )
            Spacer24()
        }
        item {
            PrescriptionHeader(alpha)
            Spacer16()
        }
        items(items = prescriptions) { prescription ->
            PrescriptionOrder(
                prescription,
                toggleContentDescription = "contentDescription",
                alpha,
                viewModel::toggleOrder,
            )
        }
        item {
            Spacer48()
        }
    }
}

@Composable
fun DeliveryAddress(contentAlpha: Float, content: @Composable () -> Unit) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        border = BorderStroke(1.dp, AppTheme.colors.neutral500),
    ) {
        CompositionLocalProvider(LocalContentAlpha provides contentAlpha) {
            content()
        }
    }
}
