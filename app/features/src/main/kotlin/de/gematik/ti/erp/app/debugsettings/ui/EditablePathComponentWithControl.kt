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

package de.gematik.ti.erp.app.debugsettings.ui

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Text
import androidx.compose.material.TextField
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import de.gematik.ti.erp.app.theme.PaddingDefaults

@Composable
fun EditablePathComponentWithControl(
    modifier: Modifier,
    label: String,
    textFieldValue: String,
    onValueChange: (String, Boolean) -> Unit,
    content: @Composable ((Boolean) -> Unit) -> Unit
) {
    Row(verticalAlignment = Alignment.CenterVertically, modifier = modifier.fillMaxWidth()) {
        TextField(
            value = textFieldValue,
            onValueChange = { onValueChange(it, false) },
            label = { Text(label) },
            maxLines = 3,
            modifier = Modifier
                .weight(1f)
                .padding(end = PaddingDefaults.Medium)
        )

        content { onValueChange(textFieldValue, it) }
    }
}
