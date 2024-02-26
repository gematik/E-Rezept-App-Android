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

import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.TextFieldColors
import androidx.compose.material.TextFieldDefaults
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material.icons.outlined.VisibilityOff
import androidx.compose.material.icons.rounded.Check
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.autofill.AutofillNode
import androidx.compose.ui.autofill.AutofillType
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.layout.boundsInWindow
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalAutofill
import androidx.compose.ui.platform.LocalAutofillTree
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import de.gematik.ti.erp.app.Requirement
import de.gematik.ti.erp.app.features.R

@Requirement(
    "O.Data_10#1",
    "O.Data_11#1",
    sourceSpecification = "BSI-eRp-ePA",
    rationale = "Password field using the  keyboard type password. Copying the content is not possible " +
        "with this type. Autocorrect is disallowed. It`s not possible to disable third party keyboards."
)
@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun PasswordTextField(
    modifier: Modifier,
    value: String,
    onValueChange: (String) -> Unit,
    onSubmit: () -> Unit,
    isConsistent: Boolean = false,
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
    val passwordIsNotVisible = stringResource(R.string.password_is_not_visible)
    val passwordIsVisible = stringResource(R.string.password_is_visible)

    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier
            .heightIn(min = 56.dp)
            .then(autofillModifier)
            .semantics {
                contentDescription = if (passwordVisible) {
                    passwordIsVisible
                } else {
                    passwordIsNotVisible
                }
            },
        singleLine = true,
        keyboardOptions = KeyboardOptions(autoCorrect = false, keyboardType = KeyboardType.Password),
        keyboardActions = KeyboardActions {
            onSubmit()
        },
        visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
        trailingIcon = {
            if (isConsistent) {
                Icon(
                    Icons.Rounded.Check,
                    stringResource(R.string.consistent_password)
                )
            } else if (allowVisiblePassword) {
                IconButton(
                    onClick = { passwordVisible = !passwordVisible }
                ) {
                    when (passwordVisible) {
                        true -> Icon(
                            Icons.Outlined.Visibility,
                            stringResource(R.string.settings_password_acc_show_password_toggle)
                        )
                        false -> Icon(
                            Icons.Outlined.VisibilityOff,
                            stringResource(R.string.settings_password_acc_show_password_toggle)
                        )
                    }
                }
            }
        },
        isError = isError,
        label = label,
        shape = RoundedCornerShape(8.dp),
        colors = colors
    )
}

@LightDarkPreview
@Composable
fun PasswordTextFieldPreview() {
    PreviewAppTheme {
        PasswordTextField(
            modifier = Modifier,
            value = "",
            onSubmit = {},
            onValueChange = {}

        )
    }
}
