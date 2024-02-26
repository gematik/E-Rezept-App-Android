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

package de.gematik.ti.erp.app.utils.compose

import androidx.compose.material.ContentAlpha
import androidx.compose.material.Text
import androidx.compose.material.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import de.gematik.ti.erp.app.features.R
import de.gematik.ti.erp.app.theme.AppTheme

@Composable
fun ConfirmationPasswordTextField(
    modifier: Modifier,
    password: String,
    passwordScore: Int,
    value: String,
    onValueChange: (String) -> Unit,
    onSubmit: () -> Unit
) {
    val isError = remember(password, value) {
        password.isNotBlank() &&
            value.isNotBlank() &&
            !password.startsWith(value)
    }

    val isConsistent = remember(password, value) {
        password.isNotBlank() && password == value && validatePasswordScore(passwordScore)
    }

    PasswordTextField(
        modifier = modifier,
        value = value,
        onValueChange = onValueChange,
        isConsistent = isConsistent,
        isError = isError,
        onSubmit = {
            if (!isError && isConsistent) {
                onSubmit()
            }
        },
        allowAutofill = true,
        allowVisiblePassword = true,
        label = {
            Text(stringResource(R.string.settings_password_repeat_password))
        },
        colors = if (isConsistent) {
            TextFieldDefaults.outlinedTextFieldColors(
                focusedBorderColor = AppTheme.colors.green600.copy(
                    alpha = ContentAlpha.high
                ),
                focusedLabelColor = AppTheme.colors.green600.copy(
                    alpha = ContentAlpha.high
                ),
                unfocusedBorderColor = AppTheme.colors.green600.copy(alpha = ContentAlpha.high),
                unfocusedLabelColor = AppTheme.colors.green600.copy(
                    alpha = ContentAlpha.high
                ),
                trailingIconColor = AppTheme.colors.green600.copy(
                    alpha = ContentAlpha.high
                )
            )
        } else {
            TextFieldDefaults.outlinedTextFieldColors()
        }
    )
}

@LightDarkPreview
@Composable
fun ConfirmationPasswordTextFieldPreview() {
    PreviewAppTheme {
        ConfirmationPasswordTextField(
            modifier = Modifier,
            value = "",
            password = "",
            passwordScore = 1,
            onSubmit = {},
            onValueChange = {}
        )
    }
}
