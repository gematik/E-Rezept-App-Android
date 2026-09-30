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

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Check
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import de.gematik.ti.erp.app.core.R
import de.gematik.ti.erp.app.theme.AppTheme

// Completed
// CommResV3 design: https://www.figma.com/design/Xg4X8ULc7fwxzkxaCnxW0I/%F0%9F%A4%96-eRezept-Android?node-id=41291-17438&m=dev
// will be a message created on device, marked by the user
@Composable
internal fun RedeemedReservationMessageCard(
    time: String
) {
    CommResV3MessageCard(
        cardIcon = Icons.Outlined.Check,
        cardIconTint = AppTheme.colors.green900,
        cardIconBackgroundColor = AppTheme.colors.green100,
        time = time,
        title = stringResource(R.string.message_card_completed_reservation_title),
        content = {
            CommResV3TextContent(
                text = stringResource(R.string.message_card_completed_reservation_content)
            )
        }
    )
}

@Composable
internal fun RedeemedShippingMessageCard(
    time: String
) {
    CommResV3MessageCard(
        cardIcon = Icons.Outlined.Check,
        cardIconTint = AppTheme.colors.green900,
        cardIconBackgroundColor = AppTheme.colors.green100,
        time = time,
        title = stringResource(R.string.message_card_completed_shipping_title),
        content = {
            CommResV3TextContent(
                text = stringResource(R.string.message_card_completed_shipping_content)
            )
        }
    )
}

@Composable
internal fun RedeemedDeliveryMessageCard(
    time: String
) {
    CommResV3MessageCard(
        cardIcon = Icons.Outlined.Check,
        cardIconTint = AppTheme.colors.green900,
        cardIconBackgroundColor = AppTheme.colors.green100,
        time = time,
        title = stringResource(R.string.message_card_completed_delivery_title),
        content = {
            CommResV3TextContent(
                text = stringResource(R.string.message_card_completed_delivery_content)
            )
        }
    )
}
