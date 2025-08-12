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

package de.gematik.ti.erp.app.pharmacy.model

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import de.gematik.ti.erp.app.core.R

data class PharmacyOrderServiceState(
    val pickup: PharmacyServiceState,
    val delivery: PharmacyServiceState,
    val online: PharmacyServiceState
) {
    val pickupText: String
        @Composable
        get() = stringResource(R.string.pharmacy_order_opt_collect_two_lines)

    val pickupImage: Painter
        @Composable
        get() = painterResource(R.drawable.pharmacy_small)

    val deliveryText: String
        @Composable
        get() = stringResource(R.string.pharmacy_order_opt_delivery_two_lines)

    val deliveryImage: Painter
        @Composable
        get() = painterResource(R.drawable.delivery_car_small)

    val onlineText: String
        @Composable
        get() = stringResource(R.string.pharmacy_order_opt_mail_two_lines)

    val onlineImage: Painter
        @Composable
        get() = painterResource(R.drawable.truck_small)
}
