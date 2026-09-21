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

package de.gematik.ti.erp.app.material3.components.switchs

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.selection.toggleable
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import de.gematik.ti.erp.app.preview.LightDarkPreview
import de.gematik.ti.erp.app.theme.AppTheme
import de.gematik.ti.erp.app.theme.PaddingDefaults

@Composable
fun LabeledSwitch(
    text: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .toggleable(
                value = checked,
                onValueChange = onCheckedChange,
                enabled = enabled,
                role = Role.Switch
            )
            .padding(horizontal = PaddingDefaults.Medium, vertical = PaddingDefaults.Medium),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = text,
            style = AppTheme.typography.body1,
            color = if (enabled) AppTheme.colors.neutral900 else AppTheme.colors.neutral600,
            modifier = Modifier.weight(1f)
        )
        GemSwitch(
            checked = checked,
            enabled = enabled,
            onCheckedChange = null
        )
    }
}

@LightDarkPreview
@Composable
fun LabeledSwitchPreview() {
    AppTheme {
        Surface {
            Column {
                LabeledSwitch(
                    text = "Enabled and Checked",
                    checked = true,
                    onCheckedChange = {}
                )
                LabeledSwitch(
                    text = "Enabled and Unchecked",
                    checked = false,
                    onCheckedChange = {}
                )
                LabeledSwitch(
                    text = "Disabled and Checked",
                    checked = true,
                    enabled = false,
                    onCheckedChange = {}
                )
                LabeledSwitch(
                    text = "Disabled and Unchecked",
                    checked = false,
                    enabled = false,
                    onCheckedChange = {}
                )
            }
        }
    }
}
