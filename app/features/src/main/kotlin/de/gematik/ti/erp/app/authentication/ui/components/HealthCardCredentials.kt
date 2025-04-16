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

package de.gematik.ti.erp.app.authentication.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Visibility
import androidx.compose.material.icons.rounded.VisibilityOff
import androidx.compose.material3.Icon
import androidx.compose.material3.IconToggleButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import de.gematik.ti.erp.app.features.R
import de.gematik.ti.erp.app.theme.AppTheme
import de.gematik.ti.erp.app.theme.PaddingDefaults
import de.gematik.ti.erp.app.theme.SizeDefaults
import de.gematik.ti.erp.app.utils.compose.ErezeptOutlineText
import de.gematik.ti.erp.app.utils.compose.PrimaryButton
import de.gematik.ti.erp.app.utils.compose.erezeptTextFieldColors
import de.gematik.ti.erp.app.utils.extensions.ErezeptKeyboardOptions

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
        ErezeptOutlineText(
            modifier = Modifier.fillMaxWidth(),
            value = pin,
            onValueChange = {
                if (it.matches(PinRegex)) {
                    pin = it
                }
            },
            label = stringResource(R.string.mini_cdw_pin_input_label),
            placeholder = stringResource(R.string.mini_cdw_pin_input_placeholder),
            keyboardOptions = ErezeptKeyboardOptions.numberPassword,
            visualTransformation = when {
                pinVisible -> VisualTransformation.None
                else -> PasswordVisualTransformation()
            },
            keyboardActions = KeyboardActions {
                onNext(pin)
            },
            shape = RoundedCornerShape(SizeDefaults.one),
            colors = erezeptTextFieldColors(),
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
