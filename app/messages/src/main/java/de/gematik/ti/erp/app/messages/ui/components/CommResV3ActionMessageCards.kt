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
import androidx.compose.material.icons.automirrored.outlined.DirectionsWalk
import androidx.compose.material.icons.automirrored.outlined.ReceiptLong
import androidx.compose.material.icons.outlined.AttachFile
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.HourglassTop
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import de.gematik.ti.erp.app.core.R
import de.gematik.ti.erp.app.theme.AppTheme

// Invoice
// CommResV3 design: https://www.figma.com/design/Xg4X8ULc7fwxzkxaCnxW0I/%F0%9F%A4%96-eRezept-Android?node-id=41291-17439&m=dev
@Composable
internal fun InvoiceMessageCard(
    time: String,
    onClickOpenInvoice: () -> Unit
) {
    CommResV3MessageCard(
        cardIcon = Icons.AutoMirrored.Outlined.ReceiptLong,
        cardIconTint = AppTheme.colors.green900,
        cardIconBackgroundColor = AppTheme.colors.green100,
        time = time,
        title = stringResource(R.string.message_card_invoice_title),
        content = {
            CommResV3TextContent(
                text = stringResource(R.string.message_card_invoice_content)
            )
        },
        actionContent = {
            CommResV3ActionContent(
                text = stringResource(R.string.message_card_invoice_button)
            ) {
                onClickOpenInvoice()
            }
        }
    )
}

@Composable
internal fun DeletedInvoiceMessageCard(
    time: String
) {
    CommResV3MessageCard(
        cardIcon = Icons.AutoMirrored.Outlined.ReceiptLong,
        cardIconTint = AppTheme.colors.green900,
        cardIconBackgroundColor = AppTheme.colors.green100,
        time = time,
        title = stringResource(R.string.message_card_invoice_title),
        content = {
            CommResV3TextContent(
                text = stringResource(R.string.message_card_invoice_content)
            )
        },
        actionContent = {
            CommResV3ActionContent(
                enabled = false,
                text = stringResource(R.string.message_card_invoice_deleted_button),
                tint = AppTheme.colors.red700
            ) {}
        }
    )
}

// PickUpCode
// CommResV3 design: https://www.figma.com/design/Xg4X8ULc7fwxzkxaCnxW0I/%F0%9F%A4%96-eRezept-Android?node-id=41291-17467&m=dev
@Composable
internal fun PickUpCodeMessageCard(
    time: String,
    message: String?,
    onClickOpenPickUpCode: () -> Unit
) {
    CommResV3MessageCard(
        cardIcon = Icons.AutoMirrored.Outlined.DirectionsWalk,
        cardIconTint = AppTheme.colors.green900,
        cardIconBackgroundColor = AppTheme.colors.green100,
        time = time,
        title = stringResource(R.string.message_card_pickupcode_title),
        content = {
            CommResV3TextContent(
                text = stringResource(R.string.message_card_pickupcode_content)
            )
        },
        hintContent = {
            message?.let {
                CommResV3HintContent(message)
            }
        },
        actionContent = {
            CommResV3ActionContent(
                text = stringResource(R.string.message_card_pickupcode_button)
            ) {
                onClickOpenPickUpCode()
            }
        }
    )
}

// ReservationState
// CommResV3 design: https://www.figma.com/design/Xg4X8ULc7fwxzkxaCnxW0I/%F0%9F%A4%96-eRezept-Android?node-id=41291-18980&m=dev
// Design contains pickUpCode which is not part of the message. strings should be changed
@Composable
internal fun ImmediatlyReservationStateMessageCard(
    time: String
) {
    CommResV3MessageCard(
        cardIcon = Icons.AutoMirrored.Outlined.DirectionsWalk,
        cardIconTint = AppTheme.colors.green900,
        cardIconBackgroundColor = AppTheme.colors.green100,
        time = time,
        title = stringResource(R.string.message_card_reservation_state_immediatly_title),
        content = {
            CommResV3TextContent(
                text = stringResource(R.string.message_card_reservation_state_immediatly_content)
            )
        }
    )
}

@Composable
internal fun SameDayReservationStateMessageCard(
    time: String
) {
    CommResV3MessageCard(
        cardIcon = Icons.AutoMirrored.Outlined.DirectionsWalk,
        cardIconTint = AppTheme.colors.green900,
        cardIconBackgroundColor = AppTheme.colors.green100,
        time = time,
        title = stringResource(R.string.message_card_reservation_state_sameday_title),
        content = {
            CommResV3TextContent(
                text = stringResource(R.string.message_card_reservation_state_sameday_content)
            )
        }
    )
}

@Composable
internal fun NextDayReservationStateMessageCard(
    time: String
) {
    CommResV3MessageCard(
        cardIcon = Icons.AutoMirrored.Outlined.DirectionsWalk,
        cardIconTint = AppTheme.colors.green900,
        cardIconBackgroundColor = AppTheme.colors.green100,
        time = time,
        title = stringResource(R.string.message_card_reservation_state_nextday_title),
        content = {
            CommResV3TextContent(
                text = stringResource(R.string.message_card_reservation_state_nextday_content)
            )
        }
    )
}

@Composable
internal fun NextDayAMReservationStateMessageCard(
    time: String
) {
    CommResV3MessageCard(
        cardIcon = Icons.AutoMirrored.Outlined.DirectionsWalk,
        cardIconTint = AppTheme.colors.green900,
        cardIconBackgroundColor = AppTheme.colors.green100,
        time = time,
        title = stringResource(R.string.message_card_reservation_state_nextday_am_title),
        content = {
            CommResV3TextContent(
                text = stringResource(R.string.message_card_reservation_state_nextday_am_content)
            )
        }
    )
}

@Composable
internal fun NextDayPMReservationStateMessageCard(
    time: String
) {
    CommResV3MessageCard(
        cardIcon = Icons.AutoMirrored.Outlined.DirectionsWalk,
        cardIconTint = AppTheme.colors.green900,
        cardIconBackgroundColor = AppTheme.colors.green100,
        time = time,
        title = stringResource(R.string.message_card_reservation_state_nextday_pm_title),
        content = {
            CommResV3TextContent(
                text = stringResource(R.string.message_card_reservation_state_nextday_pm_content)
            )
        }
    )
}

@Composable
internal fun UnknownReservationStateMessageCard(
    time: String
) {
    CommResV3MessageCard(
        cardIcon = Icons.Outlined.HourglassTop,
        cardIconTint = AppTheme.colors.yellow900,
        cardIconBackgroundColor = AppTheme.colors.yellow100,
        time = time,
        title = stringResource(R.string.message_card_reservation_state_unknown_title),
        content = {
            CommResV3TextContent(
                text = stringResource(R.string.message_card_reservation_state_unknown_content)
            )
        }
    )
}

@Composable
internal fun NotAvailableReservationStateMessageCard(
    time: String
) {
    CommResV3MessageCard(
        cardIcon = Icons.Outlined.Close,
        cardIconTint = AppTheme.colors.red900,
        cardIconBackgroundColor = AppTheme.colors.red100,
        time = time,
        title = stringResource(R.string.message_card_reservation_state_not_available_title),
        content = {
            CommResV3TextContent(
                text = stringResource(R.string.message_card_reservation_state_not_available_content)
            )
        }
    )
}

// Link
// CommResV3 design: https://www.figma.com/design/Xg4X8ULc7fwxzkxaCnxW0I/%F0%9F%A4%96-eRezept-Android?node-id=41291-17441&m=dev
@Composable
internal fun LinkMessageCard(
    time: String,
    message: String?,
    onClickOpenLink: () -> Unit
) {
    CommResV3MessageCard(
        cardIcon = Icons.Outlined.AttachFile,
        cardIconTint = AppTheme.colors.primary900,
        cardIconBackgroundColor = AppTheme.colors.primary100,
        time = time,
        title = stringResource(R.string.message_card_link_title),
        content = {
            CommResV3TextContent(
                text = stringResource(R.string.message_card_link_content)
            )
        },
        hintContent = {
            message?.let {
                CommResV3HintContent(message)
            }
        },
        actionContent = {
            CommResV3ActionContent(
                text = stringResource(R.string.message_card_link_button)
            ) {
                onClickOpenLink()
            }
        }
    )
}
