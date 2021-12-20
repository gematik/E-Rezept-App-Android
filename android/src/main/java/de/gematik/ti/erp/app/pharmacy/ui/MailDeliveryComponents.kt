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

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.items
import androidx.compose.material.ContentAlpha
import androidx.compose.material.Icon
import androidx.compose.material.LocalContentAlpha
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationCity
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import de.gematik.ti.erp.app.R
import de.gematik.ti.erp.app.pharmacy.usecase.model.UIPrescriptionOrder
import de.gematik.ti.erp.app.prescription.repository.RemoteRedeemOption
import de.gematik.ti.erp.app.theme.AppTheme
import de.gematik.ti.erp.app.utils.compose.Spacer16
import de.gematik.ti.erp.app.utils.compose.Spacer24
import de.gematik.ti.erp.app.utils.compose.Spacer48
import kotlinx.coroutines.flow.collect

@Composable
fun MailDelivery(
    navigation: NavController,
    viewModel: PharmacySearchViewModel,
    taskIds: List<String>,
    pharmacyName: String,
    telematikId: String
) {
    val prescriptions by produceState(initialValue = listOf<UIPrescriptionOrder>()) {
        taskIds.takeIf { it.isNotEmpty() }?.let { ids ->
            viewModel.fetchSelectedOrders(ids).collect { value = it }
        }
    }
    val header = stringResource(id = R.string.pharm_mail_header)
    val alpha = if (viewModel.uiState.loading) ContentAlpha.medium else ContentAlpha.high
    HeaderWithScaffold(
        navController = navigation,
        viewModel = viewModel,
        telematikId = telematikId,
        redeemOption = RemoteRedeemOption.Shipment,
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
                            Icons.Default.LocationCity,
                            contentDescription = "",
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
                Spacer24()
            }
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
