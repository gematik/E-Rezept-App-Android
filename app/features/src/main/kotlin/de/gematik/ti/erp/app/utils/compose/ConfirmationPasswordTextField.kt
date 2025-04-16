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

package de.gematik.ti.erp.app.utils.compose

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import de.gematik.ti.erp.app.features.R
import de.gematik.ti.erp.app.theme.AppTheme
import de.gematik.ti.erp.app.utils.compose.preview.PreviewAppTheme

@Composable
fun ConfirmationPasswordTextField(
    modifier: Modifier,
    value: String,
    passwordIsValidAndConsistent: Boolean,
    repeatedPasswordHasError: Boolean,
    onValueChange: (String) -> Unit,
    onSubmit: () -> Unit
) {
    PasswordTextField(
        modifier = modifier,
        value = value,
        onValueChange = onValueChange,
        isConsistent = passwordIsValidAndConsistent,
        isError = repeatedPasswordHasError,
        onSubmit = {
            if (!repeatedPasswordHasError && passwordIsValidAndConsistent) {
                onSubmit()
            }
        },
        allowAutofill = true,
        allowVisiblePassword = true,
        label = {
            Text(stringResource(R.string.settings_password_repeat))
        },
        colors = if (passwordIsValidAndConsistent) {
            erezeptTextFieldColors(
                focussedBorderColor = AppTheme.colors.green600,
                focussedLabelColor = AppTheme.colors.green600,
                unfocusedBorderColor = AppTheme.colors.green600,
                unfocusedLabelColor = AppTheme.colors.green600,
                focusedTrailingIconColor = AppTheme.colors.green600
            )
        } else {
            erezeptTextFieldColors()
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
            passwordIsValidAndConsistent = false,
            repeatedPasswordHasError = false,
            onSubmit = {},
            onValueChange = {}
        )
    }
}
