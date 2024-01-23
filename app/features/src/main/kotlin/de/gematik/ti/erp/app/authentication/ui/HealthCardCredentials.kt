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

package de.gematik.ti.erp.app.authentication.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.Icon
import androidx.compose.material.IconToggleButton
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Text
import androidx.compose.material.TextFieldDefaults
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Visibility
import androidx.compose.material.icons.rounded.VisibilityOff
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import de.gematik.ti.erp.app.features.R
import de.gematik.ti.erp.app.theme.AppTheme
import de.gematik.ti.erp.app.theme.PaddingDefaults
import de.gematik.ti.erp.app.utils.compose.PrimaryButton

private val PinRegex = """^\d{0,8}$""".toRegex()
private val PinCorrectRegex = """^\d{6,8}$""".toRegex()

@Composable
internal fun HealthCardCredentials(
    modifier: Modifier,
    onNext: (pin: String) -> Unit
) {
    var pin by remember { mutableStateOf("") }
    var pinVisible by remember { mutableStateOf(false) }
    val pinCorrect by remember {
        derivedStateOf { pin.matches(PinCorrectRegex) }
    }

    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(PaddingDefaults.Large)
    ) {
        Text(
            stringResource(R.string.mini_cdw_intro_description),
            style = AppTheme.typography.body2l
        )
        OutlinedTextField(
            modifier = Modifier.fillMaxWidth(),
            value = pin,
            onValueChange = {
                if (it.matches(PinRegex)) {
                    pin = it
                }
            },
            label = { Text(stringResource(R.string.mini_cdw_pin_input_label)) },
            placeholder = { Text(stringResource(R.string.mini_cdw_pin_input_placeholder)) },
            visualTransformation = if (pinVisible) {
                VisualTransformation.None
            } else {
                PasswordVisualTransformation()
            },
            keyboardOptions = KeyboardOptions(
                autoCorrect = false,
                keyboardType = KeyboardType.NumberPassword
            ),
            shape = RoundedCornerShape(8.dp),
            colors = TextFieldDefaults.outlinedTextFieldColors(
                unfocusedLabelColor = AppTheme.colors.neutral400,
                placeholderColor = AppTheme.colors.neutral400,
                trailingIconColor = AppTheme.colors.neutral400
            ),
            keyboardActions = KeyboardActions {
                onNext(pin)
            },
            trailingIcon = {
                IconToggleButton(
                    checked = pinVisible,
                    onCheckedChange = { pinVisible = it }
                ) {
                    Icon(
                        if (pinVisible) {
                            Icons.Rounded.Visibility
                        } else {
                            Icons.Rounded.VisibilityOff
                        },
                        null
                    )
                }
            }
        )
        PrimaryButton(
            onClick = { onNext(pin) },
            enabled = pinCorrect,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(stringResource(R.string.mini_cdw_pin_next))
        }
    }
}
