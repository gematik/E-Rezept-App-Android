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

package de.gematik.ti.erp.app.redeem.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import de.gematik.ti.erp.app.pharmacy.mapper.calculateServiceState
import de.gematik.ti.erp.app.pharmacy.model.OrderOptionErpModel
import de.gematik.ti.erp.app.pharmacy.ui.components.PharmacyOrderOptionCard
import de.gematik.ti.erp.app.pharmacy.ui.components.PharmacyOrderOptionCardType
import de.gematik.ti.erp.app.pharmacy.model.PharmacyDetailsErpModel
import de.gematik.ti.erp.app.pharmacy.model.PrescriptionInOrderErpModel
import de.gematik.ti.erp.app.preview.LightDarkPreview
import de.gematik.ti.erp.app.preview.PreviewTheme
import de.gematik.ti.erp.app.redeem.ui.preview.RedeemOverviewScreenPreviewParameter
import de.gematik.ti.erp.app.theme.PaddingDefaults

@Composable
internal fun OrderSelectedServicesRow(
    selectedOption: OrderOptionErpModel?,
    pharmacy: PharmacyDetailsErpModel?,
    prescriptions: List<PrescriptionInOrderErpModel>,
    onServiceSelected: (OrderOptionErpModel) -> Unit
) {
    val serviceState = pharmacy?.calculateServiceState(
        prescriptions = prescriptions
    )

    Row(
        modifier = Modifier
            .wrapContentSize()
            .height(IntrinsicSize.Min),
        horizontalArrangement = Arrangement.spacedBy(PaddingDefaults.Medium)
    ) {
        if (serviceState?.pickup?.visible == true) {
            PharmacyOrderOptionCard(
                modifier = Modifier.fillMaxHeight(),
                isSelected = selectedOption == OrderOptionErpModel.Pickup,
                isServiceEnabled = serviceState.pickup.enabled,
                type = PharmacyOrderOptionCardType.Flat,
                isError = false,
                image = serviceState.pickupImage,
                text = serviceState.pickupText
            ) {
                onServiceSelected(OrderOptionErpModel.Pickup)
            }
        }
        if (serviceState?.delivery?.visible == true) {
            PharmacyOrderOptionCard(
                modifier = Modifier.fillMaxHeight(),
                isSelected = selectedOption == OrderOptionErpModel.Delivery,
                isServiceEnabled = serviceState.delivery.enabled,
                type = PharmacyOrderOptionCardType.Flat,
                isError = false,
                image = serviceState.deliveryImage,
                text = serviceState.deliveryText
            ) {
                onServiceSelected(OrderOptionErpModel.Delivery)
            }
        }
        if (serviceState?.online?.visible == true) {
            PharmacyOrderOptionCard(
                modifier = Modifier.fillMaxHeight(),
                isSelected = selectedOption == OrderOptionErpModel.Online,
                isServiceEnabled = serviceState.online.enabled,
                type = PharmacyOrderOptionCardType.Flat,
                isError = false,
                image = serviceState.onlineImage,
                text = serviceState.onlineText
            ) {
                onServiceSelected(OrderOptionErpModel.Online)
            }
        }
    }
}

@LightDarkPreview
@Composable
private fun OrderRowPreview() {
    PreviewTheme {
        OrderSelectedServicesRow(
            selectedOption = OrderOptionErpModel.Pickup,
            prescriptions = emptyList(),
            pharmacy = RedeemOverviewScreenPreviewParameter.pharmacyPreviewData
        ) {
        }
    }
}
