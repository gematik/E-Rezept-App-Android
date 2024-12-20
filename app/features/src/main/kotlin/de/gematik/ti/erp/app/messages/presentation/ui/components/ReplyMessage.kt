/*
 * Copyright 2024, gematik GmbH
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

package de.gematik.ti.erp.app.messages.presentation.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.InlineTextContent
import androidx.compose.foundation.text.appendInlineContent
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowRight
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.Placeholder
import androidx.compose.ui.text.PlaceholderVerticalAlign
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.unit.em
import de.gematik.ti.erp.app.features.R
import de.gematik.ti.erp.app.messages.domain.model.OrderUseCaseData
import de.gematik.ti.erp.app.theme.AppTheme
import de.gematik.ti.erp.app.theme.PaddingDefaults
import de.gematik.ti.erp.app.theme.SizeDefaults
import de.gematik.ti.erp.app.utils.SpacerMedium
import de.gematik.ti.erp.app.utils.SpacerTiny
import de.gematik.ti.erp.app.utils.compose.DynamicText
import de.gematik.ti.erp.app.utils.extensions.DateTimeUtils
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toJavaLocalDateTime
import kotlinx.datetime.toLocalDateTime
import java.time.format.DateTimeFormatter

@Composable
internal fun ReplyMessage(
    message: OrderUseCaseData.Message,
    isFirstMessage: Boolean,
    isLastMessage: Boolean,
    dateFormatter: DateTimeFormatter = DateTimeUtils.dateFormatter,
    timeFormatter: DateTimeFormatter = DateTimeUtils.timeFormatter,
    onClick: () -> Unit
) {
    val localDateTime = remember(message) {
        message.sentOn.toLocalDateTime(TimeZone.currentSystemDefault()).toJavaLocalDateTime()
    }

    val date = remember(localDateTime) {
        dateFormatter.format(localDateTime)
    }

    val time = remember(localDateTime) {
        timeFormatter.format(localDateTime)
    }

    val replyMessageTitle = getTitleForMessageType(message)
    val replyMessageDescription = getDescriptionForMessageType(message)

    Row(
        Modifier
            .drawConnectedLine(
                drawTop = !isFirstMessage,
                drawBottom = !isLastMessage
            )
            .clickable(
                onClick = onClick,
                enabled = message.type != OrderUseCaseData.Message.Type.Text
            )
    ) {
        Spacer(Modifier.width(SizeDefaults.sixfold))
        Column(
            Modifier
                .weight(1f)
                .padding(PaddingDefaults.Medium)
        ) {
            SpacerMedium()
            Text(
                stringResource(R.string.orders_timestamp, date, time),
                style = AppTheme.typography.subtitle2
            )
            SpacerTiny()
            Text(
                text = replyMessageDescription,
                style = AppTheme.typography.body2l
            )
            replyMessageTitle?.let {
                SpacerTiny()
                AnnotatedInfoText(info = it)
            }
        }
    }
}

@Composable
private fun AnnotatedInfoText(info: String) {
    val annotatedText = buildAnnotatedString {
        append(info)
        append(" ")
        appendInlineContent("button", "button")
    }

    val inlineContent = mapOf(
        "button" to InlineTextContent(
            Placeholder(
                width = 0.em,
                height = 0.em,
                placeholderVerticalAlign = PlaceholderVerticalAlign.TextCenter
            )
        ) {
            Icon(
                Icons.AutoMirrored.Rounded.KeyboardArrowRight,
                contentDescription = null,
                tint = AppTheme.colors.primary600
            )
        }
    )

    DynamicText(
        text = annotatedText,
        style = AppTheme.typography.body2,
        color = AppTheme.colors.primary600,
        inlineContent = inlineContent
    )
}
