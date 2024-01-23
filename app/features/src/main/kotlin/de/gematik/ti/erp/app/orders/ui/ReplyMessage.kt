/*
 * Copyright (c) 2024 gematik GmbH
 * 
 * Licensed under the EUPL, Version 1.2 or – as soon they will be approved by
 * the European Commission - subsequent versions of the EUPL (the Licence);
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at:
 * 
 *     https://joinup.ec.europa.eu/software/page/eupl
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the Licence is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Licence for the specific language governing permissions and
 * limitations under the Licence.
 * 
 */

package de.gematik.ti.erp.app.orders.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.InlineTextContent
import androidx.compose.foundation.text.appendInlineContent
import androidx.compose.material.Divider
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.KeyboardArrowRight
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.Placeholder
import androidx.compose.ui.text.PlaceholderVerticalAlign
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import de.gematik.ti.erp.app.TestTag
import de.gematik.ti.erp.app.features.R
import de.gematik.ti.erp.app.orders.usecase.model.OrderUseCaseData
import de.gematik.ti.erp.app.theme.AppTheme
import de.gematik.ti.erp.app.theme.PaddingDefaults
import de.gematik.ti.erp.app.utils.compose.DynamicText
import de.gematik.ti.erp.app.utils.compose.SpacerTiny
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toJavaLocalDateTime
import kotlinx.datetime.toLocalDateTime
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

@Composable
internal fun ReplyMessage(
    message: OrderUseCaseData.Message,
    isFirstMessage: Boolean,
    onClick: () -> Unit
) {
    val info = when (message.type) {
        OrderUseCaseData.Message.Type.Link -> stringResource(R.string.orders_show_cart)
        OrderUseCaseData.Message.Type.Code -> stringResource(R.string.orders_show_code)
        OrderUseCaseData.Message.Type.Text -> null
        else -> stringResource(R.string.orders_show_general_message)
    }
    val description = when (message.type) {
        OrderUseCaseData.Message.Type.Text -> message.message ?: ""
        else -> null
    }

    val date = remember(message) {
        val dateFormatter = DateTimeFormatter.ofLocalizedDate(FormatStyle.SHORT)
        dateFormatter.format(
            message.sentOn.toLocalDateTime(TimeZone.currentSystemDefault()).toJavaLocalDateTime()
        )
    }
    val time = remember(message) {
        val dateFormatter = DateTimeFormatter.ofLocalizedTime(FormatStyle.SHORT)
        dateFormatter.format(
            message.sentOn.toLocalDateTime(TimeZone.currentSystemDefault()).toJavaLocalDateTime()
        )
    }

    Column(
        Modifier
            .drawConnectedLine(
                drawTop = !isFirstMessage,
                drawBottom = true,
                topDashed = false
            )
            .clickable(
                onClick = onClick,
                enabled = message.type != OrderUseCaseData.Message.Type.Text
            )
            .fillMaxWidth()
            .testTag(TestTag.Orders.Details.MessageListItem)
    ) {
        Row {
            Spacer(Modifier.width(48.dp))
            Column(
                Modifier
                    .weight(1f)
                    .padding(PaddingDefaults.Medium)
            ) {
                Text(
                    stringResource(R.string.orders_timestamp, date, time),
                    style = AppTheme.typography.subtitle2
                )
                description?.let {
                    SpacerTiny()
                    Text(
                        text = it,
                        style = AppTheme.typography.body2l
                    )
                }
                info?.let {
                    SpacerTiny()
                    val txt = buildAnnotatedString {
                        append(it)
                        append(" ")
                        appendInlineContent("button", "button")
                    }
                    val c = mapOf(
                        "button" to InlineTextContent(
                            Placeholder(
                                width = 0.em,
                                height = 0.em,
                                placeholderVerticalAlign = PlaceholderVerticalAlign.TextCenter
                            )
                        ) {
                            Icon(
                                Icons.Rounded.KeyboardArrowRight,
                                contentDescription = null,
                                tint = AppTheme.colors.primary600
                            )
                        }
                    )
                    DynamicText(
                        txt,
                        style = AppTheme.typography.body2,
                        color = AppTheme.colors.primary600,
                        inlineContent = c
                    )
                }
            }
        }
        Divider(Modifier.padding(start = 64.dp))
    }
}
