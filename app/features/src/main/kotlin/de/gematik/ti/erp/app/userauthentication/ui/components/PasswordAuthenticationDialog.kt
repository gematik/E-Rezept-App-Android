/*
 * Copyright 2024, gematik GmbH
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

package de.gematik.ti.erp.app.userauthentication.ui.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import de.gematik.ti.erp.app.features.R
import de.gematik.ti.erp.app.theme.AppTheme
import de.gematik.ti.erp.app.theme.SizeDefaults
import de.gematik.ti.erp.app.utils.SpacerMedium
import de.gematik.ti.erp.app.utils.compose.ErezeptAlertDialog
import de.gematik.ti.erp.app.utils.compose.PasswordTextField

@Composable
internal fun PasswordAuthenticationDialog(
    onChangePassword: (String) -> Unit,
    onAuthenticate: () -> Unit,
    onCancel: () -> Unit
) {
    var password by remember { mutableStateOf("") }
    ErezeptAlertDialog(
        title = stringResource(id = R.string.auth_password_dialog_title),
        body = {
            Text(
                text = stringResource(id = R.string.auth_password_dialog_body),
                modifier = Modifier.fillMaxWidth(),
                style = AppTheme.typography.body2
            )
            SpacerMedium()
            PasswordTextField(
                modifier = Modifier
                    .testTag("password_prompt_password_field")
                    .fillMaxWidth()
                    .heightIn(min = SizeDefaults.sevenfold),
                value = password,
                onValueChange = {
                    password = it
                },
                allowAutofill = true,
                allowVisiblePassword = true,
                label = {
                    Text(stringResource(R.string.auth_password_dialog_label))
                },
                onSubmit = {
                    onChangePassword(password)
                    onAuthenticate()
                }
            )
        },
        onConfirmRequest = {
            onChangePassword(password)
            onAuthenticate()
        },
        onDismissRequest = onCancel
    )
}
