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

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import de.gematik.ti.erp.app.features.R
import de.gematik.ti.erp.app.theme.AppTheme
import de.gematik.ti.erp.app.theme.PaddingDefaults
import de.gematik.ti.erp.app.utils.compose.SpacerTiny
import de.gematik.ti.erp.app.utils.compose.annotatedStringResource
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toJavaLocalDateTime
import kotlinx.datetime.toLocalDateTime
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

@Composable
internal fun DispenseMessage(
    hasReplyMessages: Boolean,
    pharmacyName: String,
    orderSentOn: Instant
) {
    val date = remember(orderSentOn) {
        val dateFormatter = DateTimeFormatter.ofLocalizedDate(FormatStyle.SHORT)
        dateFormatter.format(orderSentOn.toLocalDateTime(TimeZone.currentSystemDefault()).toJavaLocalDateTime())
    }
    val time = remember(orderSentOn) {
        val dateFormatter = DateTimeFormatter.ofLocalizedTime(FormatStyle.SHORT)
        dateFormatter.format(orderSentOn.toLocalDateTime(TimeZone.currentSystemDefault()).toJavaLocalDateTime())
    }

    Row(
        Modifier.drawConnectedLine(
            drawTop = true,
            drawBottom = false,
            topDashed = !hasReplyMessages
        )
    ) {
        Spacer(Modifier.width(48.dp))
        Column(
            Modifier
                .weight(1f)
                .padding(PaddingDefaults.Medium)
        ) {
            SpacerTiny()
            Text(
                stringResource(R.string.orders_timestamp, date, time),
                style = AppTheme.typography.subtitle2
            )

            val highlightedPharmacyName = buildAnnotatedString {
                withStyle(SpanStyle(color = AppTheme.colors.primary600)) {
                    append(pharmacyName)
                }
            }
            Text(
                text = annotatedStringResource(
                    R.string.orders_prescription_sent_to,
                    highlightedPharmacyName
                ),
                style = AppTheme.typography.body2l
            )
        }
    }
}
