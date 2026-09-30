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
import androidx.compose.material.icons.outlined.Euro
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import de.gematik.ti.erp.app.communication.model.payload.CommunicationReplyPaymentInfoPayloadErpModel
import de.gematik.ti.erp.app.communication.model.payload.PaymentMethod
import de.gematik.ti.erp.app.core.R
import de.gematik.ti.erp.app.theme.AppTheme
import de.gematik.ti.erp.app.utils.SpacerMedium

// Payment
// CommResV3 design: https://www.figma.com/design/Xg4X8ULc7fwxzkxaCnxW0I/%F0%9F%A4%96-eRezept-Android?node-id=41291-17443&m=dev
@Composable
internal fun PaymentInfoMessageCard(
    time: String,
    payload: CommunicationReplyPaymentInfoPayloadErpModel,
    onClickPaymentMethod: (String) -> Unit,
    showTime: Boolean = true
) {
    val isPaymentRequired = payload.totalAmount > 0
    PaymentMessageCard(
        time = time,
        title = stringResource(
            if (isPaymentRequired) R.string.message_card_payment_required_title
            else R.string.message_card_payment_not_required_title
        ),
        description = stringResource(R.string.message_card_payment_amount_description),
        isPaymentRequired = isPaymentRequired,
        paymentSummaryTitle = if (!isPaymentRequired) stringResource(R.string.message_card_payment_free_label) else null,
        paymentSummaryText = if (isPaymentRequired) {
            stringResource(R.string.invoice_details_cost, "%.2f".format(payload.totalAmount).replace(".", ","))
        } else {
            stringResource(R.string.message_card_payment_free_amount)
        },
        paymentNotice = payload.text,
        paymentMethods = payload.paymentMethods.mapNotNull { method ->
            method.toPaymentMethodTileOrNull(onClickPaymentMethod = onClickPaymentMethod)
        },
        showTime = showTime
    )
}

@Composable
internal fun PaymentMessageCard(
    time: String,
    title: String,
    description: String,
    isPaymentRequired: Boolean,
    paymentSummaryTitle: String? = null,
    paymentSummaryText: String,
    paymentNotice: String? = null,
    paymentMethods: List<PaymentMethodTile> = emptyList(),
    hint: String? = null,
    showTime: Boolean = true
) {
    CommResV3MessageCard(
        cardIcon = Icons.Outlined.Euro,
        cardIconTint = if (isPaymentRequired) AppTheme.colors.red700 else AppTheme.colors.green700,
        cardIconBackgroundColor = if (isPaymentRequired) AppTheme.colors.red100 else AppTheme.colors.green100,
        time = time,
        title = title,
        showTime = showTime,
        content = {
            CommResV3TextContent(text = description)
            SpacerMedium()
            CommResV3PaymentContent(
                title = paymentSummaryTitle,
                details = paymentSummaryText
            )
            paymentNotice?.let {
                SpacerMedium()
                CommResV3TextContent(text = paymentNotice)
            }
            if (paymentMethods.isNotEmpty()) {
                SpacerMedium()
                CommResV3PaymentMethodContent(paymentMethods = paymentMethods)
            }
        },
        hintContent = hint?.let {
            { CommResV3HintContent(hint = hint) }
        }
    )
}

@Composable
private fun PaymentMethod.toPaymentMethodTileOrNull(
    onClickPaymentMethod: (String) -> Unit
): PaymentMethodTile? {
    val url = url ?: return null
    val normalizedMethod = paymentMethod.lowercase()
    val (label, type) = when (normalizedMethod) {
        "paypal" -> stringResource(R.string.message_card_payment_method_paypal) to PaymentMethodTileType.PayPal
        "cash" -> stringResource(R.string.message_card_payment_method_cash) to PaymentMethodTileType.Cash
        "bankaccount", "bank account", "stripe" -> stringResource(R.string.message_card_payment_method_bankaccount) to PaymentMethodTileType.BankAccount
        "creditcard", "credit card", "mastercard", "maestro", "visa" -> stringResource(R.string.message_card_payment_method_creditcard) to
            PaymentMethodTileType.Creditcard

        else -> paymentMethod to PaymentMethodTileType.Unknown
    }

    return PaymentMethodTile(
        label = label,
        type = type,
        onClick = { onClickPaymentMethod(url) }
    )
}
