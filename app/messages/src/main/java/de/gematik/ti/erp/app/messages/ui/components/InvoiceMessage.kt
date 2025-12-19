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

import android.content.res.Configuration
import android.os.LocaleList
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.KeyboardArrowRight
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.stringResource
import de.gematik.ti.erp.app.core.R
import de.gematik.ti.erp.app.datetime.annotatedStringResource
import de.gematik.ti.erp.app.messages.ui.model.InvoiceMessageUiModel
import de.gematik.ti.erp.app.preview.LightDarkPreview
import de.gematik.ti.erp.app.preview.PreviewTheme
import de.gematik.ti.erp.app.theme.AppTheme
import de.gematik.ti.erp.app.theme.PaddingDefaults
import de.gematik.ti.erp.app.theme.SizeDefaults
import de.gematik.ti.erp.app.utils.SpacerTiny
import java.util.Locale

@Composable
internal fun InvoiceMessage(
    item: InvoiceMessageUiModel?,
    onClickCostReceiptDetail: (String) -> Unit
) {
    MessageTimeline(
        drawFilledTop = item?.isFirstMessage == false,
        drawFilledBottom = true, // Invoices always have content below
        isClickable = false,
        timestamp = {
            Text(
                text = stringResource(R.string.orders_timestamp, item?.date ?: "", item?.time ?: ""),
                style = AppTheme.typography.subtitle2
            )
        },

        content = {
            item?.name?.let { name ->
                Column(
                    modifier = Modifier
                        .padding(
                            top = PaddingDefaults.Small,
                            bottom = PaddingDefaults.Tiny
                        )
                ) {
                    InfoChip(name)
                }

                Text(
                    text = annotatedStringResource(
                        R.string.cost_receipt_is_ready,
                        name
                    ),
                    style = AppTheme.typography.body2
                )
            }

            SpacerTiny()

            MessageActionButton(
                text = stringResource(R.string.show_cost_receipt)
            ) {
                item?.taskId?.let { taskId -> onClickCostReceiptDetail(taskId) }
            }
        }
    )
}

@Composable
fun CostReceiptDetail(
    onClick: () -> Unit
) {
    Row(modifier = Modifier.clickable(onClick = onClick)) {
        Text(
            text = stringResource(R.string.show_cost_receipt),
            style = AppTheme.typography.body2,
            color = AppTheme.colors.primary700
        )
        Icon(
            modifier = Modifier
                .size(SizeDefaults.triple)
                .align(Alignment.CenterVertically),
            imageVector = Icons.AutoMirrored.Outlined.KeyboardArrowRight,
            contentDescription = null,
            tint = AppTheme.colors.primary700
        )
    }
}

@LightDarkPreview
@Composable
private fun InvoiceMessagePreview() {
    val fakeConfig = Configuration().apply {
        setLocales(
            LocaleList(Locale.ENGLISH)
        )
    }

    CompositionLocalProvider(LocalConfiguration provides fakeConfig) {
        PreviewTheme {
            Column(Modifier.padding(SizeDefaults.double)) {
                InvoiceMessage(
                    item = InvoiceMessageUiModel(
                        name = "Ibuprofen 600 mg",
                        taskId = "123",
                        date = "13.02.2025",
                        time = "10:00",
                        isFirstMessage = true
                    ),
                    onClickCostReceiptDetail = {}
                )

                InvoiceMessage(
                    item = InvoiceMessageUiModel(
                        name = "Pantoprazol 20 mg",
                        taskId = "456",
                        date = "12.02.2025",
                        time = "10:00",
                        isFirstMessage = false
                    ),
                    onClickCostReceiptDetail = {}
                )
            }
        }
    }
}
