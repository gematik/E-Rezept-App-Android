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

package de.gematik.ti.erp.app.userauthentication.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ChevronLeft
import androidx.compose.material.icons.rounded.ChevronRight
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import de.gematik.ti.erp.app.app_core.R
import de.gematik.ti.erp.app.theme.AppTheme
import de.gematik.ti.erp.app.theme.PaddingDefaults
import de.gematik.ti.erp.app.theme.SizeDefaults
import de.gematik.ti.erp.app.userauthentication.model.UserAuthenticationActions
import de.gematik.ti.erp.app.userauthentication.presentation.AuthenticationStateData
import de.gematik.ti.erp.app.utils.SpacerLarge
import de.gematik.ti.erp.app.utils.SpacerMedium
import de.gematik.ti.erp.app.utils.SpacerTiny
import de.gematik.ti.erp.app.utils.compose.PasswordTextField
import de.gematik.ti.erp.app.utils.compose.PrimaryButton
import de.gematik.ti.erp.app.utils.compose.annotatedPluralsResource
import de.gematik.ti.erp.app.utils.isNotNullOrEmpty

@Composable
internal fun UserAuthenticationLoginSection(
    authenticationState: AuthenticationStateData.AuthenticationState,
    timeout: Long,
    enteredPassword: String,
    enteredPasswordError: Boolean,
    showPasswordLogin: Boolean,
    userAuthenticationActions: UserAuthenticationActions
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(PaddingDefaults.Medium),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            stringResource(R.string.auth_title),
            style = AppTheme.typography.h5,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )
        SpacerTiny()
        Text(
            stringResource(R.string.auth_body),
            style = AppTheme.typography.body1l,
            textAlign = TextAlign.Center
        )
        SpacerLarge()

        when {
            authenticationState.authentication.methodIsDeviceSecurity -> {
                PrimaryButton(
                    onClick = userAuthenticationActions.onAuthenticateWithDeviceSecurity,
                    shape = RoundedCornerShape(SizeDefaults.one),
                    contentPadding = PaddingValues(
                        horizontal = PaddingDefaults.Large,
                        vertical = PaddingDefaults.ShortMedium
                    )
                ) {
                    Text(
                        stringResource(R.string.auth_button)
                    )
                }
            }
            authenticationState.authentication.methodIsPassword -> {
                PasswordLoginSection(
                    timeout = timeout,
                    enteredPassword = enteredPassword,
                    enteredPasswordError = enteredPasswordError,
                    onAuthenticate = userAuthenticationActions.onAuthenticateWithPassword,
                    onChangePassword = userAuthenticationActions.onChangeEnteredPassword,
                    onRemovePasswordError = userAuthenticationActions.onRemovePasswordError
                )
            }
            authenticationState.authentication.bothMethodsAvailable -> {
                if (showPasswordLogin) {
                    PasswordLoginSection(
                        timeout = timeout,
                        enteredPassword = enteredPassword,
                        enteredPasswordError = enteredPasswordError,
                        onAuthenticate = userAuthenticationActions.onAuthenticateWithPassword,
                        onChangePassword = userAuthenticationActions.onChangeEnteredPassword,
                        onRemovePasswordError = userAuthenticationActions.onRemovePasswordError
                    )
                    SpacerMedium()
                    TextButton(onClick = userAuthenticationActions.onHidePasswordLogin) {
                        Icon(Icons.Rounded.ChevronLeft, null)
                        Text(stringResource(R.string.back))
                    }
                } else {
                    PrimaryButton(
                        onClick = userAuthenticationActions.onAuthenticateWithDeviceSecurity,
                        shape = RoundedCornerShape(SizeDefaults.one),
                        contentPadding = PaddingValues(
                            horizontal = PaddingDefaults.Large,
                            vertical = PaddingDefaults.ShortMedium
                        )
                    ) {
                        Text(
                            stringResource(R.string.auth_button)
                        )
                    }
                    SpacerMedium()
                    TextButton(onClick = userAuthenticationActions.onShowPasswordLogin) {
                        Text(
                            text = stringResource(id = R.string.auth_alternative_button),
                            textAlign = TextAlign.Center
                        )
                        Icon(Icons.Rounded.ChevronRight, null)
                    }
                }
            }
        }
    }
}

@Composable
private fun PasswordLoginSection(
    timeout: Long,
    enteredPassword: String,
    enteredPasswordError: Boolean,
    onRemovePasswordError: () -> Unit,
    onChangePassword: (String) -> Unit,
    onAuthenticate: () -> Unit
) {
    SpacerMedium()
    PasswordLoginTextField(
        timeout = timeout,
        enteredPassword = enteredPassword,
        enteredPasswordError = enteredPasswordError,
        onChangePassword = onChangePassword,
        onAuthenticate = onAuthenticate,
        onRemovePasswordError = onRemovePasswordError
    )
    SpacerLarge()
    PrimaryButton(
        enabled = timeout == 0L && enteredPassword.isNotNullOrEmpty(),
        onClick = { onAuthenticate() },
        contentPadding = PaddingValues(
            horizontal = PaddingDefaults.Large,
            vertical = PaddingDefaults.ShortMedium
        )
    ) {
        Text(stringResource(R.string.auth_next_button))
    }
}

@Composable
private fun PasswordLoginTextField(
    timeout: Long,
    enteredPassword: String,
    enteredPasswordError: Boolean,
    onRemovePasswordError: () -> Unit,
    onChangePassword: (String) -> Unit,
    onAuthenticate: () -> Unit
) {
    PasswordTextField(
        modifier = Modifier
            .testTag("password_prompt_password_field")
            .fillMaxWidth()
            .heightIn(min = SizeDefaults.sevenfold),
        value = enteredPassword,
        onValueChange = {
            onRemovePasswordError()
            onChangePassword(it)
        },
        enabled = timeout == 0L,
        isError = enteredPasswordError,
        allowAutofill = true,
        allowVisiblePassword = true,
        label = {
            Text(stringResource(R.string.auth_password_dialog_label))
        },
        supportingText = {
            if (enteredPasswordError || timeout > 0) {
                val message = buildString {
                    if (enteredPasswordError) {
                        append(stringResource(R.string.wrong_password))
                        append(" ")
                    }
                    if (timeout > 0) {
                        append(
                            annotatedPluralsResource(
                                R.plurals.wrong_password_timeout_description,
                                timeout.toInt(),
                                AnnotatedString(timeout.toString())
                            )
                        )
                    } else {
                        append(stringResource(R.string.try_again))
                    }
                }
                Text(
                    message,
                    style = AppTheme.typography.caption1,
                    color = AppTheme.colors.red700
                )
            }
        },
        onSubmit = {
            if (timeout == 0L && enteredPassword.isNotNullOrEmpty()) {
                onAuthenticate()
            }
        }
    )
}
