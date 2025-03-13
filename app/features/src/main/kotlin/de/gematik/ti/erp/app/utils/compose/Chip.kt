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

package de.gematik.ti.erp.app.utils.compose

import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import de.gematik.ti.erp.app.theme.AppTheme
import de.gematik.ti.erp.app.theme.PaddingDefaults
import de.gematik.ti.erp.app.utils.SpacerSmall

@Composable
fun Chip(
    text: String,
    modifier: Modifier = Modifier,
    checked: Boolean,
    closable: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    val textColor = if (checked) AppTheme.colors.neutral000 else AppTheme.colors.neutral600
    val backgroundColor = if (checked) AppTheme.colors.primary700 else AppTheme.colors.neutral100
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .toggleable(
                checked,
                role = Role.Checkbox,
                onValueChange = onCheckedChange,
                interactionSource = remember { MutableInteractionSource() },
                indication = ripple()
            )
            .background(color = backgroundColor, shape = RoundedCornerShape(8.dp))
            .padding(horizontal = PaddingDefaults.ShortMedium, vertical = PaddingDefaults.ShortMedium / 2),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text, style = AppTheme.typography.subtitle2, color = textColor)
        if (closable && !checked) {
            SpacerSmall()
            Icon(
                Icons.Rounded.Close,
                null,
                tint = AppTheme.colors.neutral600,
                modifier = Modifier.size(16.dp)
            )
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
