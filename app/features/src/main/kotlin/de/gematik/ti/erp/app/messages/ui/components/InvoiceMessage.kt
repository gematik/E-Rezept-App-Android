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

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.KeyboardArrowRight
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import de.gematik.ti.erp.app.features.R
import de.gematik.ti.erp.app.theme.AppTheme
import de.gematik.ti.erp.app.theme.PaddingDefaults
import de.gematik.ti.erp.app.theme.SizeDefaults
import de.gematik.ti.erp.app.utils.SpacerMedium
import de.gematik.ti.erp.app.utils.SpacerTiny
import de.gematik.ti.erp.app.utils.compose.annotatedStringResource
import de.gematik.ti.erp.app.utils.extensions.DateTimeUtils
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toJavaLocalDateTime
import kotlinx.datetime.toLocalDateTime

@Composable
internal fun InvoiceMessage(
    prescriptionName: String?,
    invoiceDate: Instant?,
    isFirstMessage: Boolean,

    onClickCostReceiptDetail: () -> Unit
) {
    val date = remember(invoiceDate) {
        DateTimeUtils.dateFormatter.format(invoiceDate?.toLocalDateTime(TimeZone.currentSystemDefault())?.toJavaLocalDateTime()) // extention for Instant
    }
    val time = remember(invoiceDate) {
        DateTimeUtils.timeFormatter.format(invoiceDate?.toLocalDateTime(TimeZone.currentSystemDefault())?.toJavaLocalDateTime()) // extention for Instant
    }

    // State for dynamically tracking the circle position
    val circleYPositionState = remember { mutableFloatStateOf(0f) }

    Row(
        Modifier.drawConnectedLine(
            drawFilledTop = !isFirstMessage,
            drawFilledBottom = true, // invoice is never the last message in the list
            circleYPosition = { circleYPositionState.floatValue }
        )
    ) {
        Spacer(Modifier.width(SizeDefaults.triple))
        Column(
            Modifier
                .weight(1f)
                .padding(PaddingDefaults.Medium)
        ) {
            prescriptionName?.let {
                SpacerMedium()
                Text(
                    stringResource(R.string.orders_timestamp, date, time),
                    style = AppTheme.typography.subtitle2,
                    modifier = Modifier.calculateVerticalCenter(
                        onCenterCalculated = { circleYPositionState.floatValue = it }
                    )
                )
                Column(modifier = Modifier.padding(top = PaddingDefaults.Small, bottom = PaddingDefaults.Tiny)) {
                    InfoChip(it)
                }
                Text(
                    text = annotatedStringResource(
                        R.string.cost_receipt_is_ready,
                        it
                    ),
                    style = AppTheme.typography.body2l
                )
            }
            SpacerTiny()
            CostReceiptDetail(
                onClick = { onClickCostReceiptDetail() }
            )
        }
    }
}

@Composable
fun CostReceiptDetail(
    onClick: () -> Unit
) {
    Row(modifier = Modifier.clickable(onClick = onClick)) {
        Text(
            text = stringResource(R.string.show_cost_receipt),
            style = AppTheme.typography.body2l,
            color = AppTheme.colors.primary700
        )
        Icon(
            modifier = Modifier
                .size(24.dp)
                .align(Alignment.CenterVertically),
            imageVector = Icons.Outlined.KeyboardArrowRight,
            contentDescription = null,
            tint = AppTheme.colors.primary700
        )
    }
}
