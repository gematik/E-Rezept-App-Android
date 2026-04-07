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

package de.gematik.ti.erp.app.utils.compose

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import de.gematik.ti.erp.app.preview.LightDarkPreview
import de.gematik.ti.erp.app.theme.AppTheme
import de.gematik.ti.erp.app.theme.PaddingDefaults
import de.gematik.ti.erp.app.theme.SizeDefaults
import de.gematik.ti.erp.app.utils.compose.preview.PreviewAppTheme

@Composable
fun FilterSheetChip(
    text: String,
    checked: Boolean,
    modifier: Modifier = Modifier,
    onCheckedChange: (Boolean) -> Unit
) {
    FilterChip(
        modifier = modifier,
        selected = checked,
        onClick = { onCheckedChange(!checked) },
        label = {
            Text(
                text = text,
                style = AppTheme.typography.body2,
                color = if (checked) AppTheme.colors.primary900 else AppTheme.colors.neutral700,
                modifier = Modifier.padding(vertical = PaddingDefaults.Small)
            )
        },
        leadingIcon = if (checked) {
            {
                Icon(
                    imageVector = Icons.Rounded.Check,
                    contentDescription = null,
                    tint = AppTheme.colors.primary900,
                    modifier = Modifier.size(SizeDefaults.doubleQuarter)
                )
            }
        } else null,
        colors = FilterChipDefaults.filterChipColors(
            containerColor = AppTheme.colors.neutral000,
            selectedContainerColor = AppTheme.colors.primary200,
            labelColor = AppTheme.colors.neutral700,
            selectedLabelColor = AppTheme.colors.primary900
        ),
        border = FilterChipDefaults.filterChipBorder(
            enabled = true,
            selected = checked,
            borderColor = AppTheme.colors.neutral700,
            selectedBorderColor = AppTheme.colors.primary900,
            borderWidth = SizeDefaults.eighth,
            selectedBorderWidth = SizeDefaults.eighth
        ),
        shape = CircleShape
    )
}

@LightDarkPreview
@Composable
private fun FilterSheetChipCheckedPreview() {
    PreviewAppTheme {
        FilterSheetChip(
            text = "Checked",
            checked = true,
            onCheckedChange = {}
        )
    }
}

@LightDarkPreview
@Composable
private fun FilterSheetChipUncheckedPreview() {
    PreviewAppTheme {
        FilterSheetChip(
            text = "Unchecked",
            checked = false,
            onCheckedChange = {}
        )
    }
}
