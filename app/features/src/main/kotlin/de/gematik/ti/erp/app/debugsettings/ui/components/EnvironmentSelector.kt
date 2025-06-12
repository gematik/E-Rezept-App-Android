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

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.material.Button
import androidx.compose.material.RadioButton
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import de.gematik.ti.erp.app.debugsettings.data.Environment
import de.gematik.ti.erp.app.app_core.R
import de.gematik.ti.erp.app.theme.AppTheme
import de.gematik.ti.erp.app.theme.PaddingDefaults
import de.gematik.ti.erp.app.utils.SpacerXXLarge

@Composable
fun EnvironmentSelector(
    currentSelectedEnvironment: Environment,
    onSelectEnvironment: (environment: Environment) -> Unit,
    onSaveEnvironment: () -> Unit
) {
    var selectedEnvironment by remember { mutableStateOf(currentSelectedEnvironment) }

    Column(
        modifier = Modifier
            .navigationBarsPadding()
            .fillMaxWidth()
            .selectableGroup()
    ) {
        SpacerXXLarge()
        Text(
            text = stringResource(R.string.debug_select_environment),
            style = AppTheme.typography.h6,
            modifier = Modifier.padding(PaddingDefaults.Medium)
        )

        Environment.entries.forEach {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        selectedEnvironment = it
                        onSelectEnvironment(it)
                    }
            ) {
                Row(
                    modifier = Modifier.padding(
                        horizontal = PaddingDefaults.Medium,
                        vertical = PaddingDefaults.Small
                    ),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RadioButton(
                        modifier = Modifier.size(32.dp),
                        selected = selectedEnvironment == it,
                        onClick = {
                            selectedEnvironment = it
                            onSelectEnvironment(it)
                        }
                    )
                    Text(it.name)
                }
            }
        }
        Row(modifier = Modifier.padding(PaddingDefaults.Medium)) {
            Button(modifier = Modifier.fillMaxWidth(), onClick = { onSaveEnvironment() }) {
                Text(text = stringResource(R.string.debug_save_environment))
            }
            Spacer(modifier = Modifier.navigationBarsPadding())
        }
    }
}
