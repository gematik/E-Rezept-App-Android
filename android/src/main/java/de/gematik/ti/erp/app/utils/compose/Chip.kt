/*
 * Copyright (c) 2021 gematik GmbH
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
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Cancel
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import de.gematik.ti.erp.app.theme.AppTheme

@Composable
fun Chip(
    text: String,
    modifier: Modifier = Modifier,
    checked: Boolean,
    closable: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    val color = if (checked) AppTheme.colors.primary600 else AppTheme.colors.neutral200
    val textColor = if (checked && !closable) AppTheme.colors.neutral000 else AppTheme.colors.neutral999
    Row(
        modifier = modifier
            .clip(CircleShape)
            .toggleable(
                checked,
                role = Role.Checkbox,
                onValueChange = onCheckedChange,
                interactionSource = remember { MutableInteractionSource() },
                indication = rememberRipple()
            )
            .background(color = color, shape = CircleShape)
            .padding(vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Spacer(modifier = Modifier.width(12.dp))
        Text(text, style = MaterialTheme.typography.caption, color = textColor)
        if (closable && !checked) {
            SpacerSmall()
            Icon(
                Icons.Rounded.Cancel,
                null,
                tint = AppTheme.colors.neutral400,
                modifier = Modifier.size(16.dp)
            )
            SpacerSmall()
        } else {
            Spacer(modifier = Modifier.width(12.dp))
        }
    }
}

@Preview
@Composable
private fun ChipPreview() {
    AppTheme {
        Row {
            Chip("E-Rezept-Ready", Modifier, false, true) {}
            Chip("E-Rezept-Ready", Modifier, true, false) {}
            Chip("E-Rezept-Ready", Modifier, false, false) {}
        }
    }
}
