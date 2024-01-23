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

package de.gematik.ti.erp.app.utils.compose

import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Switch
import androidx.compose.material.SwitchDefaults
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import de.gematik.ti.erp.app.theme.AppTheme
import de.gematik.ti.erp.app.theme.PaddingDefaults

@Composable
fun SwitchWithText(
    modifier: Modifier = Modifier,
    checkedThumbColor: Color = AppTheme.colors.neutral900,
    uncheckedThumbColor: Color = AppTheme.colors.neutral100,
    text: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(PaddingDefaults.Medium))
            .background(AppTheme.colors.neutral100, shape = RoundedCornerShape(16.dp))
            .fillMaxWidth()
            .padding(PaddingDefaults.Medium),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(PaddingDefaults.Small)
    ) {
        Switch(
            colors = SwitchDefaults.colors(
                checkedThumbColor = checkedThumbColor,
                uncheckedThumbColor = uncheckedThumbColor
            ),
            checked = checked,
            onCheckedChange = onCheckedChange,
            enabled = true,
            interactionSource = remember { MutableInteractionSource() }
        )
        SpacerSmall()
        Text(
            modifier = Modifier.fillMaxWidth(),
            style = AppTheme.typography.subtitle2,
            color = AppTheme.colors.neutral999,
            text = text
        )
    }
}

@LightDarkPreview
@Composable
fun SwitchWithTextPreview() {
    PreviewAppTheme {
        SwitchWithText(
            text = "Text added to be shown with a switch",
            checked = false,
            onCheckedChange = {}
        )
    }
}
