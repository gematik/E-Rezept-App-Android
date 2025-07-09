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

package de.gematik.ti.erp.app.messages.mappers

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.res.stringResource
import de.gematik.ti.erp.app.app_core.R
import de.gematik.ti.erp.app.datetime.rememberErpTimeFormatter
import de.gematik.ti.erp.app.messages.domain.model.OrderUseCaseData

@Composable
fun OrderUseCaseData.Message.getReplyMessageTitle(): String? {
    return handleMessageType(
        message = this,
        onPickupCode = { stringResource(R.string.orders_show_code) },
        onLinkWithMessage = { stringResource(R.string.orders_show_cart) },
        onLinkOnly = { stringResource(R.string.orders_show_cart) },
        onTextOnly = { null },
        onEmpty = { null }
    )
}

enum class ReplyMessageType {
    Text,
    Generated
}

@Composable
fun OrderUseCaseData.Message.getReplyMessageDescription(): Pair<ReplyMessageType, String> {
    return handleMessageType(
        message = this,
        onPickupCode = {
            content
                ?.takeIf { it.isNotEmpty() }
                ?.let { ReplyMessageType.Text to it }
                ?: (ReplyMessageType.Generated to stringResource(R.string.order_pickup_general_message))
        },
        onLinkWithMessage = { ReplyMessageType.Text to content.orEmpty() },
        onLinkOnly = { ReplyMessageType.Generated to stringResource(R.string.order_message_link) },
        onTextOnly = { ReplyMessageType.Text to content.orEmpty() },
        onEmpty = { ReplyMessageType.Generated to stringResource(R.string.order_message_empty) }
    )
}

@Composable
fun OrderUseCaseData.Message.getSentOnTime(): String {
    val formatter = rememberErpTimeFormatter()

    val date = remember(this) { formatter.date(sentOn) }
    val time = remember(this) { formatter.time(sentOn) }

    return stringResource(R.string.orders_timestamp, date, time)
}

@Composable
private fun <T> handleMessageType(
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
