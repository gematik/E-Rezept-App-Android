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

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.KeyboardArrowRight
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import de.gematik.ti.erp.app.preview.LightDarkPreview
import de.gematik.ti.erp.app.preview.PreviewTheme
import de.gematik.ti.erp.app.theme.AppTheme
import de.gematik.ti.erp.app.theme.SizeDefaults

@Composable
internal fun MessageActionButton(
    text: String,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    tint: Color = AppTheme.colors.primary700,
    onClick: () -> Unit
) {
    val clickableModifier =
        if (enabled) {
            modifier
                .clickable { onClick() }
                .semantics { role = Role.Button }
        } else {
            modifier // no clickable
        }

    Row(modifier = clickableModifier) {
        Text(
            text = text,
            style = AppTheme.typography.body2,
            color = tint
        )
        if (enabled) {
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
}

@LightDarkPreview
@Composable
private fun MessageActionButtonPreview() {
    PreviewTheme {
        Column {
            MessageActionButton(
                text = "Button",
                onClick = {}
            )
            MessageActionButton(
                text = "Button",
                enabled = false,
                tint = AppTheme.colors.red700,
                onClick = {}
            )
        }
    }
}
