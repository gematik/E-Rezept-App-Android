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

package de.gematik.ti.erp.app.troubleshooting.ui.components

import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Lightbulb
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.unit.dp
import de.gematik.ti.erp.app.features.R
import de.gematik.ti.erp.app.theme.AppTheme
import de.gematik.ti.erp.app.theme.PaddingDefaults
import de.gematik.ti.erp.app.utils.compose.ClickableTaggedText
import de.gematik.ti.erp.app.utils.compose.PrimaryButton
import de.gematik.ti.erp.app.utils.compose.SecondaryButton
import de.gematik.ti.erp.app.utils.compose.SpacerMedium
import de.gematik.ti.erp.app.utils.compose.SpacerSmall

@Composable
fun RowScope.TroubleShootingNextTipButton(
    onClick: () -> Unit
) =
    SecondaryButton(
        onClick = onClick,
        modifier = Modifier
            .padding(horizontal = PaddingDefaults.Medium, vertical = 12.dp)
            .weight(1f)
    ) {
        Icon(Icons.Outlined.Lightbulb, null)
        SpacerSmall()
        Text(stringResource(R.string.cdw_troubleshooting_next_tip_button))
    }

@Composable
fun RowScope.TroubleShootingNextButton(
    onClick: () -> Unit
) =
    SecondaryButton(
        onClick = onClick,
        modifier = Modifier
            .padding(horizontal = PaddingDefaults.Medium, vertical = 12.dp)
            .weight(1f)
    ) {
        Text(stringResource(R.string.cdw_troubleshooting_next_button))
    }

@Composable
fun RowScope.TroubleShootingCloseButton(
    onClick: () -> Unit
) =
    PrimaryButton(
        onClick = onClick,
        modifier = Modifier
            .padding(horizontal = PaddingDefaults.Medium, vertical = 12.dp)
            .weight(1f)
    ) {
        Text(stringResource(R.string.cdw_troubleshooting_close_button))
    }

@Composable
fun ColumnScope.TroubleShootingTryMeButton(
    onClick: () -> Unit
) =
    PrimaryButton(
        onClick = onClick,
        modifier = Modifier.align(Alignment.CenterHorizontally)
    ) {
        Text(stringResource(R.string.cdw_troubleshooting_try_me_button))
    }

@Composable
fun TroubleShootingTip(
    text: String
) =
    TroubleShootingTip(AnnotatedString(text)) { _, _ -> }

@Composable
fun TroubleShootingTip(
    text: AnnotatedString,
    onClickText: (tag: String, item: String) -> Unit
) {
    Row(Modifier.fillMaxWidth()) {
        Icon(Icons.Rounded.CheckCircle, null, tint = AppTheme.colors.green600)
        SpacerMedium()
        ClickableTaggedText(
            text = text,
            style = AppTheme.typography.body1,
            onClick = {
                onClickText(it.tag, it.item)
            }
        )
    }
}

@Composable
fun TroubleShootingContactUsButton(
    modifier: Modifier,
    onClick: () -> Unit
) =
    SecondaryButton(
        onClick = onClick,
        modifier = modifier
    ) {
        Icon(Icons.Rounded.Edit, null)
        SpacerSmall()
        Text(stringResource(R.string.cdw_troubleshooting_contact_us_button))
    }
