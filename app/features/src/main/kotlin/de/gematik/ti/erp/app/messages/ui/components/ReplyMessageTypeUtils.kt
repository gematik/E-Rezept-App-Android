/*
 * Copyright 2025, gematik GmbH
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
 * In case of changes by gematik find details in the "Readme" file.
 *
 * See the Licence for the specific language governing permissions and limitations under the Licence.
 */

package de.gematik.ti.erp.app.messages.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import de.gematik.ti.erp.app.app_core.R
import de.gematik.ti.erp.app.messages.domain.model.OrderUseCaseData

@Composable
fun getTitleForMessageType(message: OrderUseCaseData.Message): String? {
    return handleMessageType(
        message = message,
        onPickupCode = { stringResource(R.string.orders_show_code) },
        onLinkWithMessage = { stringResource(R.string.orders_show_cart) },
        onLinkOnly = { stringResource(R.string.orders_show_cart) },
        onTextOnly = { null },
        onEmpty = { null }
    )
}

@Composable
fun getDescriptionForMessageType(message: OrderUseCaseData.Message): String {
    return handleMessageType(
        message = message,
        onPickupCode = {
            message.content?.takeIf { it.isNotEmpty() }
                ?: stringResource(R.string.order_pickup_general_message)
        },
        onLinkWithMessage = { message.content.orEmpty() },
        onLinkOnly = { stringResource(R.string.order_message_link) },
        onTextOnly = { message.content.orEmpty() },
        onEmpty = { stringResource(R.string.order_message_empty) }
    )
}

@Composable
fun <T> handleMessageType(
    message: OrderUseCaseData.Message,
    onPickupCode: @Composable () -> T,
    onLinkWithMessage: @Composable () -> T,
    onLinkOnly: @Composable () -> T,
    onTextOnly: () -> T,
    onEmpty: @Composable () -> T
): T {
    return when {
        message.pickUpCodeDMC != null || message.pickUpCodeHR != null -> onPickupCode()
        message.link != null && message.content != null -> onLinkWithMessage()
        message.link != null -> onLinkOnly()
        message.content != null -> onTextOnly()
        else -> onEmpty()
    }
}
