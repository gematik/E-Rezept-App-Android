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

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import de.gematik.ti.erp.app.core.R
import de.gematik.ti.erp.app.task.model.TaskErpModel

// Ordered / DispenseRequest
// CommResV3 design: https://www.figma.com/design/Xg4X8ULc7fwxzkxaCnxW0I/%F0%9F%A4%96-eRezept-Android?node-id=41291-17437&m=dev
@Composable
internal fun OrderedReservationMessageCard(
    date: String,
    pharmacyName: String,
    prescriptions: List<TaskErpModel>,
    onClickPrescription: (String) -> Unit,
    onClickPharmacy: () -> Unit
) {
    CommResV3MessageCard(
        cardImage = painterResource(R.drawable.pharmacy_small),
        time = date,
        title = stringResource(R.string.message_card_ordered_reservation_title),
        content = {
            CommResV3ClickableTextContent(
                leadingText = stringResource(R.string.message_card_ordered_reservation_content_leading_text),
                pharmacyName = pharmacyName,
                trailingText = stringResource(R.string.message_card_ordered_reservation_content_trailing_text),
                onClickPharmacy = onClickPharmacy
            )
        },
        prescriptionsContent = {
            CommResV3OrderedPrescriptionsContent(
                prescriptions = prescriptions,
                onClickPrescription = onClickPrescription
            )
        }
    )
}

@Composable
internal fun OrderedShippingMessageCard(
    date: String,
    pharmacyName: String,
    prescriptions: List<TaskErpModel>,
    onClickPrescription: (String) -> Unit,
    onClickPharmacy: () -> Unit
) {
    CommResV3MessageCard(
        cardImage = painterResource(R.drawable.truck_small),
        time = date,
        title = stringResource(R.string.message_card_ordered_shipping_title),
        content = {
            CommResV3ClickableTextContent(
                leadingText = stringResource(R.string.message_card_ordered_reservation_content_leading_text),
                pharmacyName = pharmacyName,
                trailingText = stringResource(R.string.message_card_ordered_reservation_content_trailing_text),
                onClickPharmacy = onClickPharmacy
            )
        },
        prescriptionsContent = {
            CommResV3OrderedPrescriptionsContent(
                prescriptions = prescriptions,
                onClickPrescription = onClickPrescription
            )
        }
    )
}

@Composable
internal fun OrderedDeliveryMessageCard(
    date: String,
    pharmacyName: String,
    prescriptions: List<TaskErpModel>,
    onClickPrescription: (String) -> Unit,
    onClickPharmacy: () -> Unit
) {
    CommResV3MessageCard(
        cardImage = painterResource(R.drawable.delivery_car_small),
        time = date,
        title = stringResource(R.string.message_card_ordered_delivery_title),
        content = {
            CommResV3ClickableTextContent(
                leadingText = stringResource(R.string.message_card_ordered_reservation_content_leading_text),
                pharmacyName = pharmacyName,
                trailingText = stringResource(R.string.message_card_ordered_reservation_content_trailing_text),
                onClickPharmacy = onClickPharmacy
            )
        },
        prescriptionsContent = {
            CommResV3OrderedPrescriptionsContent(
                prescriptions = prescriptions,
                onClickPrescription = onClickPrescription
            )
        }
    )
}
