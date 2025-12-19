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

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchColors
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import de.gematik.ti.erp.app.material3.components.switchs.GemSwitchDefaults.gemSwitchColors
import de.gematik.ti.erp.app.preview.LightDarkPreview
import de.gematik.ti.erp.app.theme.AppTheme

// Material 3 Lib needs to migrate to materialTheme
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GemSwitch(
    checked: Boolean,
    onCheckedChange: ((Boolean) -> Unit)?,
    modifier: Modifier = Modifier,
    thumbContent: (@Composable () -> Unit)? = null,
    enabled: Boolean = true,
    colors: SwitchColors = gemSwitchColors(),
    interactionSource: MutableInteractionSource = remember { MutableInteractionSource() }
) {
    Switch(
        checked = checked,
        onCheckedChange = onCheckedChange,
        modifier = modifier,
        thumbContent = thumbContent,
        enabled = enabled,
        colors = colors,
        interactionSource = interactionSource
    )
}

@Composable
@LightDarkPreview
fun GemSwitchPreview() {
    AppTheme() {
        Surface(color = AppTheme.colors.green100) {
            Column() {
                GemSwitch(enabled = false, checked = true, onCheckedChange = {})
                GemSwitch(checked = true, onCheckedChange = {})
                GemSwitch(enabled = false, checked = false, onCheckedChange = {})
                GemSwitch(checked = false, onCheckedChange = {})
            }
        }
    }
}
