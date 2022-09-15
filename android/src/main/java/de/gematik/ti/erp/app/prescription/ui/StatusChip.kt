/*
 * Copyright (c) 2022 gematik GmbH
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

package de.gematik.ti.erp.app.prescription.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.DoneAll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import de.gematik.ti.erp.app.R
import de.gematik.ti.erp.app.theme.AppTheme
import de.gematik.ti.erp.app.utils.compose.SpacerSmall

@Composable
fun StatusChip(
    text: String,
    textColor: Color,
    backgroundColor: Color,
    icon: (@Composable () -> Unit)? = null
) {
    val shape = RoundedCornerShape(8.dp)
    Row(
        Modifier
            .background(backgroundColor, shape)
            .clip(shape)
            .padding(vertical = 6.dp, horizontal = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text, style = MaterialTheme.typography.subtitle2, color = textColor)
        icon?.let {
            SpacerSmall()
            it()
        }
    }
}

@Composable
fun StatusChip(
    text: String,
    icon: ImageVector,
    textColor: Color,
    backgroundColor: Color,
    iconColor: Color
) =
    StatusChip(
        text = text,
        icon = { Icon(icon, tint = iconColor, contentDescription = null) },
        textColor = textColor,
        backgroundColor = backgroundColor
    )

@Preview
@Composable
private fun StatusChipPreview() {
    AppTheme {
        StatusChip(
            text = "Einlösbar",
            icon = Icons.Rounded.Check,
            textColor = AppTheme.colors.green900,
            backgroundColor = AppTheme.colors.green100,
            iconColor = AppTheme.colors.green500
        )
    }
}

@Composable
fun ReadyStatusChip() =
    StatusChip(
        text = stringResource(R.string.prescription_status_ready),
        icon = Icons.Rounded.Check,
        textColor = AppTheme.colors.green900,
        backgroundColor = AppTheme.colors.green100,
        iconColor = AppTheme.colors.green500
    )

@Composable
fun InProgressStatusChip() =
    StatusChip(
        text = stringResource(R.string.prescription_status_in_progress),
        icon = Icons.Rounded.Check,
        textColor = AppTheme.colors.yellow900,
        backgroundColor = AppTheme.colors.yellow100,
        iconColor = AppTheme.colors.yellow500
    )

@Composable
fun CompletedStatusChip() =
    StatusChip(
        text = stringResource(R.string.prescription_status_completed),
        icon = Icons.Rounded.DoneAll,
        textColor = AppTheme.colors.neutral600,
        backgroundColor = AppTheme.colors.neutral100,
        iconColor = AppTheme.colors.neutral500
    )

@Composable
fun UnknownStatusChip() =
    StatusChip(
        text = stringResource(R.string.prescription_status_unknown),
        textColor = AppTheme.colors.neutral600,
        backgroundColor = AppTheme.colors.neutral100,
    )
