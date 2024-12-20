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

package de.gematik.ti.erp.app.prescription.detail.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Card
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowForward
import androidx.compose.material.icons.rounded.ModelTraining
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import de.gematik.ti.erp.app.features.R
import de.gematik.ti.erp.app.theme.AppTheme
import de.gematik.ti.erp.app.theme.PaddingDefaults
import de.gematik.ti.erp.app.utils.SpacerSmall

@Composable
fun InvoiceCard(
    title: String,
    body: String,
    icon: @Composable () -> Unit,
    button: @Composable () -> Unit = {}
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .wrapContentHeight(),
        shape = RoundedCornerShape(16.dp),
        backgroundColor = AppTheme.colors.neutral000,
        elevation = 0.dp,
        border = BorderStroke(1.dp, AppTheme.colors.primary300)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(PaddingDefaults.Medium),
            horizontalArrangement = Arrangement.spacedBy(PaddingDefaults.Medium),
            verticalAlignment = Alignment.Top
        ) {
            icon()
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.Start,
                verticalArrangement = Arrangement.spacedBy(PaddingDefaults.Tiny)
            ) {
                Text(
                    title,
                    style = AppTheme.typography.subtitle1
                )
                Text(
                    body,
                    style = AppTheme.typography.body2l
                )
                button()
            }
        }
    }
}

@Composable
fun NoConsentGrantedCard(
    onClickAllowInvoices: () -> Unit
) {
    InvoiceCard(
        title = stringResource(R.string.invoice_card_no_consent_title),
        body = stringResource(R.string.invoice_card_no_consent_body),
        icon = {
            Icon(
                Icons.Rounded.ModelTraining,
                null,
                modifier = Modifier.size(40.dp),
                tint = AppTheme.colors.primary600
            )
        },
        button = {
            TextButton(
                onClick = { onClickAllowInvoices() },
                contentPadding = PaddingValues(0.dp)
            ) {
                Text(
                    stringResource(R.string.invoice_card_no_consent_button_text),
                    style = AppTheme.typography.subtitle1
                )
                SpacerSmall()
                Icon(
                    Icons.AutoMirrored.Rounded.ArrowForward,
                    null
                )
            }
        }
    )
}

@Composable
fun NoInvoiceConsentGrantedCard() {
    InvoiceCard(
        title = stringResource(R.string.invoice_card_consent_no_invoice_title),
        body = stringResource(R.string.invoice_card_consent_no_invoice_body),
        icon = { CircularEmojiIcon() } // lightbulb
    )
}

@Composable
private fun CircularEmojiIcon(
    emoji: String = stringResource(R.string.lightbulb)
) {
    Box(
        modifier = Modifier
            .size(40.dp)
            .clip(CircleShape)
            .background(AppTheme.colors.primary200),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = emoji,
            style = AppTheme.typography.subtitle1,
            fontWeight = FontWeight.Bold
        )
    }
}
