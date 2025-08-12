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

package de.gematik.ti.erp.app.pharmacy.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import de.gematik.ti.erp.app.Requirement
import de.gematik.ti.erp.app.pharmacy.mapper.calculateServiceState
import de.gematik.ti.erp.app.pharmacy.model.PharmacyScreenData
import de.gematik.ti.erp.app.pharmacy.usecase.model.PharmacyUseCaseData.Pharmacy
import de.gematik.ti.erp.app.theme.PaddingDefaults

@Requirement(
    "A_24579#1",
    sourceSpecification = "gemSpec_eRp_FdV",
    rationale = "Implementation of the order selection for the pharmacy screen."
)
@Composable
internal fun OrderSelection(
    pharmacy: Pharmacy,
    onOrderClicked: (Pharmacy, PharmacyScreenData.OrderOption) -> Unit
) {
    val pharmacyServiceState = pharmacy.calculateServiceState()

    Row(
        modifier = Modifier
            .height(IntrinsicSize.Min)
            .fillMaxSize(),
        horizontalArrangement = Arrangement.spacedBy(PaddingDefaults.Medium)
    ) {
        if (pharmacyServiceState.pickup.visible) {
            PharmacyOrderOptionCard(
                isServiceEnabled = pharmacyServiceState.pickup.enabled,
                image = pharmacyServiceState.pickupImage,
                text = pharmacyServiceState.pickupText,
                type = PharmacyOrderOptionCardType.Long,
                showDisabledToast = true,
                onClick = { onOrderClicked(pharmacy, PharmacyScreenData.OrderOption.Pickup) }
            )
        }

        if (pharmacyServiceState.delivery.visible) {
            PharmacyOrderOptionCard(
                isServiceEnabled = pharmacyServiceState.delivery.enabled,
                image = pharmacyServiceState.deliveryImage,
                text = pharmacyServiceState.deliveryText,
                type = PharmacyOrderOptionCardType.Long,
                showDisabledToast = true,
                onClick = { onOrderClicked(pharmacy, PharmacyScreenData.OrderOption.Delivery) }
            )
        }

        if (pharmacyServiceState.online.visible) {
            PharmacyOrderOptionCard(
                isServiceEnabled = pharmacyServiceState.online.enabled,
                image = pharmacyServiceState.onlineImage,
                text = pharmacyServiceState.onlineText,
                type = PharmacyOrderOptionCardType.Long,
                showDisabledToast = true,
                onClick = { onOrderClicked(pharmacy, PharmacyScreenData.OrderOption.Online) }
            )
        }
    }
}
