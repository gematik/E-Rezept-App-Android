/*
 * Copyright 2025, gematik GmbH
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

package de.gematik.ti.erp.app.debugsettings.ui.components

import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color

@Composable
fun EditablePathComponentSetButton(
    modifier: Modifier = Modifier,
    label: String,
    text: String,
    active: Boolean,
    onValueChange: (String, Boolean) -> Unit,
    onClick: () -> Unit
) {
    val color = if (active) Color.Green else Color.Red
    val buttonText = if (active) "SAVED" else "SET"
    EditablePathComponentWithControl(
        modifier = modifier,
        label = label,
        textFieldValue = text,
        onValueChange = onValueChange,
        content = {
            Button(
                onClick = onClick,
                colors = ButtonDefaults.buttonColors(backgroundColor = color),
                enabled = !active

            ) {
                Text(text = buttonText)
            }
        }
    )
}
