/*
 * Copyright (c) 2021 gematik GmbH
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

package de.gematik.ti.erp.app.settings.ui

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.BottomAppBar
import androidx.compose.material.Button
import androidx.compose.material.ContentAlpha
import androidx.compose.material.Icon
import androidx.compose.material.IconToggleButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.material.TextFieldColors
import androidx.compose.material.TextFieldDefaults
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material.icons.outlined.VisibilityOff
import androidx.compose.material.icons.rounded.Check
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.getValue
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.autofill.AutofillNode
import androidx.compose.ui.autofill.AutofillType
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.layout.boundsInWindow
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalAutofill
import androidx.compose.ui.platform.LocalAutofillTree
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.ProgressBarRangeInfo
import androidx.compose.ui.semantics.progressBarRangeInfo
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.nulabinc.zxcvbn.Zxcvbn
import de.gematik.ti.erp.app.R
import de.gematik.ti.erp.app.theme.AppTheme
import de.gematik.ti.erp.app.theme.PaddingDefaults
import de.gematik.ti.erp.app.utils.compose.NavigationBarMode
import de.gematik.ti.erp.app.utils.compose.NavigationTopAppBar
import de.gematik.ti.erp.app.utils.compose.SpacerMedium
import de.gematik.ti.erp.app.utils.compose.SpacerTiny
import de.gematik.ti.erp.app.utils.compose.annotatedStringResource
import java.util.Locale

@Composable
fun SecureAppWithPassword(navController: NavController, viewModel: SettingsViewModel) {
    var password by remember { mutableStateOf("") }
    var repeatedPassword by remember { mutableStateOf("") }
    val focusRequester = FocusRequester.Default

    Scaffold(
        topBar = {
            NavigationTopAppBar(
                NavigationBarMode.Back,
                headline = stringResource(R.string.settings_password_headline),
            ) { navController.popBackStack() }
        },
        bottomBar = {
            BottomAppBar(backgroundColor = MaterialTheme.colors.surface) {
                Spacer(modifier = Modifier.weight(1f))
                Button(
                    onClick = {
                        viewModel.onSelectPasswordAsAuthenticationMode(password)
                        navController.popBackStack()
                    },
                    enabled = password.isNotBlank() && password == repeatedPassword,
                    shape = RoundedCornerShape(PaddingDefaults.Small)
                ) {
                    Text(stringResource(R.string.settings_password_save).uppercase(Locale.getDefault()))
                }
                SpacerMedium()
            }
        }
    ) { innerPadding ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(PaddingDefaults.Medium)
        ) {
            PasswordTextField(
                modifier = Modifier.fillMaxWidth(),
                value = password,
                onValueChange = {
                    repeatedPassword = ""
                    password = it
                },
                allowAutofill = true,
                allowVisiblePassword = true,
                label = {
                    Text(stringResource(R.string.settings_password_enter_password))
                },
                onSubmit = { focusRequester.requestFocus() }
            )
            SpacerTiny()
            PasswordStrength(modifier = Modifier.fillMaxWidth(), password = password)

            SpacerMedium()

            ConfirmationPasswordTextField(
                modifier = Modifier
                    .fillMaxWidth()
                    .focusRequester(focusRequester),
                password = password,
                value = repeatedPassword,
                onValueChange = {
                    repeatedPassword = it
                },
                onSubmit = {
                    if (password.isNotBlank() && password == repeatedPassword) {
                        viewModel.onSelectPasswordAsAuthenticationMode(password)
                        navController.popBackStack()
                    }
                }
            )
        }
    }
}

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun PasswordTextField(
    modifier: Modifier,
    value: String,
    onValueChange: (String) -> Unit,
    onSubmit: () -> Unit,
    isConsistend: Boolean = false,
    isError: Boolean = false,
    allowAutofill: Boolean = false,
    allowVisiblePassword: Boolean = false,
    label: @Composable (() -> Unit)? = null,
    colors: TextFieldColors = TextFieldDefaults.outlinedTextFieldColors()
) {
    var passwordVisible by remember { mutableStateOf(false) }

    val autofillModifier = if (allowAutofill) {
        val autofill = LocalAutofill.current
        val autofillNode = AutofillNode(listOf(AutofillType.Password)) { onValueChange(it) }
        LocalAutofillTree.current += autofillNode

        Modifier
            .onGloballyPositioned {
                autofillNode.boundingBox = it.boundsInWindow()
            }
            .onFocusChanged { focusState ->
                autofill?.run {
                    if (focusState.isFocused) {
                        requestAutofillForNode(autofillNode)
                    } else {
                        cancelAutofillForNode(autofillNode)
                    }
                }
            }
    } else {
        Modifier
    }

    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier
            .heightIn(min = 56.dp)
            .then(autofillModifier),
        singleLine = true,
        keyboardOptions = KeyboardOptions(autoCorrect = true, keyboardType = KeyboardType.Password),
        keyboardActions = KeyboardActions {
            if (!isError && value.isNotEmpty()) {
                onSubmit()
            }
        },
        visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
        trailingIcon = {
            if (allowVisiblePassword) {
                IconToggleButton(
                    checked = passwordVisible,
                    onCheckedChange = { passwordVisible = it }
                ) {
                    when (passwordVisible) {
                        true -> Icon(
                            Icons.Outlined.VisibilityOff,
                            stringResource(R.string.settings_password_acc_show_password_toggle)
                        )
                        false -> Icon(
                            Icons.Outlined.Visibility,
                            stringResource(R.string.settings_password_acc_show_password_toggle)
                        )
                    }
                }
            } else if (isConsistend) {
                Icon(
                    Icons.Rounded.Check,
                    stringResource(R.string.consistent_password),
                    tint = AppTheme.colors.green600
                )
            }
        },
        isError = isError,
        label = label,
        shape = RoundedCornerShape(8.dp),
        colors = colors
    )
}

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun ConfirmationPasswordTextField(
    modifier: Modifier,
    password: String,
    value: String,
    onValueChange: (String) -> Unit,
    onSubmit: () -> Unit
) {
    val isError = password.isNotBlank() &&
        value.isNotBlank() &&
        !password.startsWith(value)

    val isConsistent = password.isNotBlank() && password == value

    PasswordTextField(
        modifier = modifier,
        value = value,
        onValueChange = onValueChange,
        isConsistend = isConsistent,
        isError = isError,
        onSubmit = onSubmit,
        allowAutofill = false,
        allowVisiblePassword = false,
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
                )
            )
        } else {
            TextFieldDefaults.outlinedTextFieldColors()
        }
    )
}

@Composable
fun PasswordStrength(
    modifier: Modifier,
    password: String
) {
    val zxcvbn = remember { Zxcvbn() }
    val strength = remember(password) { zxcvbn.measure(password) }
    val barLength by animateFloatAsState(
        when (strength.score) {
            1 -> 0.1f
            2 -> 0.3f
            3 -> 0.6f
            4 -> 1.0f
            else -> 0.05f
        }
    )
    val barColor by animateColorAsState(
        when (strength.score) {
            2 -> AppTheme.colors.yellow500
            3 -> AppTheme.colors.yellow500
            4 -> AppTheme.colors.green500
            else -> AppTheme.colors.red500
        }
    )

    Column(
        modifier = modifier
            .semantics(true) {
                progressBarRangeInfo = ProgressBarRangeInfo(
                    current = strength.score.toFloat(),
                    range = 0f..4f,
                    steps = 1
                )
            }
    ) {
        val suggestions = strength.feedback.suggestions.joinToString("\n").trim()
        Text(
            annotatedStringResource(
                R.string.settings_password_suggestions,
                if (suggestions.isBlank()) {
                    stringResource(R.string.settings_password_hint)
                } else {
                    suggestions
                }
            ),
            style = AppTheme.typography.captionl
        )

        SpacerMedium()
        Box(
            modifier = Modifier
                .background(color = AppTheme.colors.neutral100, shape = RoundedCornerShape(6.dp))
                .fillMaxWidth()
        ) {
            Spacer(
                modifier = Modifier
                    .background(color = barColor, shape = RoundedCornerShape(6.dp))
                    .fillMaxWidth(barLength)
                    .height(6.dp)
            )
        }
        SpacerTiny()
        Text(
            stringResource(R.string.settings_password_strength),
            style = AppTheme.typography.captionl
        )
    }
}
