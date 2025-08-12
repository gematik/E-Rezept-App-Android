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
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import de.gematik.ti.erp.app.pharmacy.mapper.calculateServiceState
import de.gematik.ti.erp.app.pharmacy.model.PharmacyScreenData
import de.gematik.ti.erp.app.pharmacy.ui.components.PharmacyOrderOptionCard
import de.gematik.ti.erp.app.pharmacy.ui.components.PharmacyOrderOptionCardType
import de.gematik.ti.erp.app.pharmacy.usecase.model.PharmacyUseCaseData.Pharmacy
import de.gematik.ti.erp.app.preview.LightDarkPreview
import de.gematik.ti.erp.app.preview.PreviewTheme
import de.gematik.ti.erp.app.redeem.ui.preview.RedeemOverviewScreenPreviewParameter
import de.gematik.ti.erp.app.theme.PaddingDefaults

@Composable
internal fun OrderSelectedServicesRow(
    selectedOption: PharmacyScreenData.OrderOption?,
    pharmacy: Pharmacy?,
    onServiceSelected: (PharmacyScreenData.OrderOption) -> Unit
) {
    val serviceState = pharmacy?.calculateServiceState()

    Row(
        modifier = Modifier
            .height(IntrinsicSize.Min)
            .fillMaxSize(),
        horizontalArrangement = Arrangement.spacedBy(PaddingDefaults.Medium)
    ) {
        if (serviceState?.pickup?.visible == true) {
            PharmacyOrderOptionCard(
                isSelected = selectedOption == PharmacyScreenData.OrderOption.Pickup,
                isServiceEnabled = serviceState.pickup.enabled,
                type = PharmacyOrderOptionCardType.Flat,
                isError = false,
                image = serviceState.pickupImage,
                text = serviceState.pickupText
            ) {
                onServiceSelected(PharmacyScreenData.OrderOption.Pickup)
            }
        }
        if (serviceState?.delivery?.visible == true) {
            PharmacyOrderOptionCard(
                isSelected = selectedOption == PharmacyScreenData.OrderOption.Delivery,
                isServiceEnabled = serviceState.delivery.enabled,
                type = PharmacyOrderOptionCardType.Flat,
                isError = false,
                image = serviceState.deliveryImage,
                text = serviceState.deliveryText
            ) {
                onServiceSelected(PharmacyScreenData.OrderOption.Delivery)
            }
        }
        if (serviceState?.online?.visible == true) {
            PharmacyOrderOptionCard(
                isSelected = selectedOption == PharmacyScreenData.OrderOption.Online,
                isServiceEnabled = serviceState.online.enabled,
                type = PharmacyOrderOptionCardType.Flat,
                isError = false,
                image = serviceState.onlineImage,
                text = serviceState.onlineText
            ) {
                onServiceSelected(PharmacyScreenData.OrderOption.Online)
            }
        }
    }
}

@LightDarkPreview
@Composable
private fun OrderRowPreview() {
    PreviewTheme {
        OrderSelectedServicesRow(
            selectedOption = PharmacyScreenData.OrderOption.Pickup,
            pharmacy = RedeemOverviewScreenPreviewParameter.pharmacyPreviewData
        ) {
        }
    }
}
