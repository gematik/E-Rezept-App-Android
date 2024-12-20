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

package de.gematik.ti.erp.app.utils.compose

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.selection.LocalTextSelectionColors
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Coronavirus
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldColors
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.input.VisualTransformation
import de.gematik.ti.erp.app.theme.AppTheme
import de.gematik.ti.erp.app.theme.PaddingDefaults
import de.gematik.ti.erp.app.theme.SizeDefaults
import de.gematik.ti.erp.app.utils.SpacerMedium
import de.gematik.ti.erp.app.utils.compose.preview.PreviewAppTheme

/**
 * OutlinedTextField with eRezept design
 */
@Composable
fun ErezeptOutlineText(
    modifier: Modifier = Modifier,
    value: TextFieldValue,
    onValueChange: (TextFieldValue) -> Unit,
    label: @Composable (() -> Unit)? = null,
    placeholder: @Composable (() -> Unit)? = null,
    singleLine: Boolean = false,
    enabled: Boolean = true,
    readOnly: Boolean = false,
    isError: Boolean = false,
    minLines: Int = 1,
    maxLines: Int = if (singleLine) 1 else Int.MAX_VALUE,
    interactionSource: MutableInteractionSource = remember { MutableInteractionSource() },
    visualTransformation: VisualTransformation = VisualTransformation.None,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    keyboardActions: KeyboardActions = KeyboardActions.Default,
    shape: Shape = OutlinedTextFieldDefaults.shape,
    colors: TextFieldColors = erezeptTextFieldColors(),
    leadingIcon: @Composable (() -> Unit)? = null,
    trailingIcon: @Composable (() -> Unit)? = null,
    prefix: @Composable (() -> Unit)? = null,
    suffix: @Composable (() -> Unit)? = null,
    supportingText: @Composable (() -> Unit)? = null
) {
    OutlinedTextField(
        modifier = modifier,
        value = value,
        onValueChange = onValueChange,
        singleLine = singleLine,
        enabled = enabled,
        readOnly = readOnly,
        isError = isError,
        label = label,
        placeholder = placeholder,
        interactionSource = interactionSource,
        visualTransformation = visualTransformation,
        keyboardOptions = keyboardOptions,
        keyboardActions = keyboardActions,
        maxLines = maxLines,
        minLines = minLines,
        shape = shape,
        colors = colors,
        prefix = prefix,
        suffix = suffix,
        supportingText = supportingText,
        trailingIcon = trailingIcon,
        leadingIcon = leadingIcon
    )
}

@Composable
fun ErezeptOutlineText(
    modifier: Modifier = Modifier,
    value: String,
    onValueChange: (String) -> Unit,
    label: @Composable (() -> Unit)? = null,
    placeholder: @Composable (() -> Unit)? = null,
    singleLine: Boolean = false,
    enabled: Boolean = true,
    readOnly: Boolean = false,
    isError: Boolean = false,
    minLines: Int = 1,
    maxLines: Int = if (singleLine) 1 else Int.MAX_VALUE,
    interactionSource: MutableInteractionSource = remember { MutableInteractionSource() },
    visualTransformation: VisualTransformation = VisualTransformation.None,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    keyboardActions: KeyboardActions = KeyboardActions.Default,
    shape: Shape = OutlinedTextFieldDefaults.shape,
    colors: TextFieldColors = erezeptTextFieldColors(),
    leadingIcon: @Composable (() -> Unit)? = null,
    trailingIcon: @Composable (() -> Unit)? = null,
    prefix: @Composable (() -> Unit)? = null,
    suffix: @Composable (() -> Unit)? = null,
    supportingText: @Composable (() -> Unit)? = null
) {
    OutlinedTextField(
        modifier = modifier,
        value = value,
        onValueChange = onValueChange,
        singleLine = singleLine,
        enabled = enabled,
        readOnly = readOnly,
        isError = isError,
        label = label,
        placeholder = placeholder,
        interactionSource = interactionSource,
        visualTransformation = visualTransformation,
        keyboardOptions = keyboardOptions,
        keyboardActions = keyboardActions,
        maxLines = maxLines,
        minLines = minLines,
        shape = shape,
        colors = colors,
        prefix = prefix,
        suffix = suffix,
        supportingText = supportingText,
        leadingIcon = leadingIcon,
        trailingIcon = trailingIcon
    )
}

@Composable
fun ErezeptOutlineText(
    modifier: Modifier = Modifier,
    value: TextFieldValue,
    onValueChange: (TextFieldValue) -> Unit,
    label: String? = null,
    placeholder: String? = null,
    singleLine: Boolean = false,
    enabled: Boolean = true,
    readOnly: Boolean = false,
    isError: Boolean = false,
    minLines: Int = 1,
    maxLines: Int = if (singleLine) 1 else Int.MAX_VALUE,
    interactionSource: MutableInteractionSource = remember { MutableInteractionSource() },
    visualTransformation: VisualTransformation = VisualTransformation.None,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    keyboardActions: KeyboardActions = KeyboardActions.Default,
    shape: Shape = OutlinedTextFieldDefaults.shape,
    colors: TextFieldColors = erezeptTextFieldColors(),
    leadingIcon: @Composable (() -> Unit)? = null,
    trailingIcon: @Composable (() -> Unit)? = null,
    prefix: @Composable (() -> Unit)? = null,
    suffix: @Composable (() -> Unit)? = null,
    supportingText: @Composable (() -> Unit)? = null
) {
    OutlinedTextField(
        modifier = modifier,
        value = value,
        onValueChange = onValueChange,
        singleLine = singleLine,
        enabled = enabled,
        readOnly = readOnly,
        isError = isError,
        label = { label?.let { Text(it) } },
        placeholder = { placeholder?.let { Text(it) } },
        interactionSource = interactionSource,
        visualTransformation = visualTransformation,
        keyboardOptions = keyboardOptions,
        keyboardActions = keyboardActions,
        maxLines = maxLines,
        minLines = minLines,
        shape = shape,
        colors = colors,
        prefix = prefix,
        suffix = suffix,
        supportingText = supportingText,
        trailingIcon = trailingIcon,
        leadingIcon = leadingIcon
    )
}

@Composable
fun ErezeptOutlineText(
    modifier: Modifier = Modifier,
    value: String,
    onValueChange: (String) -> Unit,
    label: String? = null,
    placeholder: String? = null,
    singleLine: Boolean = false,
    enabled: Boolean = true,
    readOnly: Boolean = false,
    isError: Boolean = false,
    minLines: Int = 1,
    maxLines: Int = if (singleLine) 1 else Int.MAX_VALUE,
    interactionSource: MutableInteractionSource = remember { MutableInteractionSource() },
    visualTransformation: VisualTransformation = VisualTransformation.None,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    keyboardActions: KeyboardActions = KeyboardActions.Default,
    shape: Shape = OutlinedTextFieldDefaults.shape,
    colors: TextFieldColors = erezeptTextFieldColors(),
    leadingIcon: @Composable (() -> Unit)? = null,
    trailingIcon: @Composable (() -> Unit)? = null,
    prefix: @Composable (() -> Unit)? = null,
    suffix: @Composable (() -> Unit)? = null,
    supportingText: @Composable (() -> Unit)? = null
) {
    OutlinedTextField(
        modifier = modifier,
        value = value,
        onValueChange = onValueChange,
        singleLine = singleLine,
        enabled = enabled,
        readOnly = readOnly,
        isError = isError,
        label = { label?.let { Text(it) } },
        placeholder = { placeholder?.let { Text(it) } },
        interactionSource = interactionSource,
        visualTransformation = visualTransformation,
        keyboardOptions = keyboardOptions,
        keyboardActions = keyboardActions,
        maxLines = maxLines,
        minLines = minLines,
        shape = shape,
        colors = colors,
        prefix = prefix,
        suffix = suffix,
        supportingText = supportingText,
        trailingIcon = trailingIcon,
        leadingIcon = leadingIcon
    )
}

@Composable
fun erezeptButtonColors(
    containerColor: Color = AppTheme.colors.primary500,
    contentColor: Color = AppTheme.colors.neutral000,
    disabledContainerColor: Color = AppTheme.colors.neutral500,
    disabledContentColor: Color = AppTheme.colors.neutral000
) = ButtonDefaults.outlinedButtonColors(
    containerColor = containerColor,
    contentColor = contentColor,
    disabledContainerColor = disabledContainerColor,
    disabledContentColor = disabledContentColor
)

@Composable
fun erezeptTextFieldColors(
    textColor: Color = AppTheme.colors.neutral900,
    focusedTrailingIconColor: Color = AppTheme.colors.neutral400,
    focussedLabelColor: Color = AppTheme.colors.primary400,
    unfocusedLabelColor: Color = AppTheme.colors.neutral400,
    focussedBorderColor: Color = AppTheme.colors.primary400,
    unfocusedBorderColor: Color = AppTheme.colors.neutral400,
    disabledBorderColor: Color = AppTheme.colors.primary100,
    errorBorderColor: Color = AppTheme.colors.red400,
    focusedPlaceholderColor: Color = AppTheme.colors.neutral600,
    unfocusedPlaceholderColor: Color = AppTheme.colors.neutral400,
    focusedContainerColor: Color = AppTheme.colors.neutral000,
    unfocusedContainerColor: Color = AppTheme.colors.neutral000,
    disabledContainerColor: Color = AppTheme.colors.neutral000,
    errorContainerColor: Color = AppTheme.colors.neutral000,
    focusedLeadingIconColor: Color = AppTheme.colors.neutral600,
    unfocusedLeadingIconColor: Color = AppTheme.colors.neutral400,
    disabledLeadingIconColor: Color = AppTheme.colors.neutral300,
    errorLeadingIconColor: Color = AppTheme.colors.red400
) =
    TextFieldColors(
        focusedTextColor = textColor,
        unfocusedTextColor = AppTheme.colors.neutral600,
        disabledTextColor = AppTheme.colors.neutral300,
        errorTextColor = AppTheme.colors.red400,
        focusedContainerColor = focusedContainerColor,
        unfocusedContainerColor = unfocusedContainerColor,
        disabledContainerColor = disabledContainerColor,
        errorContainerColor = errorContainerColor,
        cursorColor = AppTheme.colors.neutral400,
        errorCursorColor = AppTheme.colors.red400,
        textSelectionColors = LocalTextSelectionColors.current,
        focusedIndicatorColor = focussedBorderColor,
        unfocusedIndicatorColor = unfocusedBorderColor,
        disabledIndicatorColor = disabledBorderColor,
        errorIndicatorColor = errorBorderColor,
        focusedLeadingIconColor = focusedLeadingIconColor,
        unfocusedLeadingIconColor = unfocusedLeadingIconColor,
        disabledLeadingIconColor = disabledLeadingIconColor,
        errorLeadingIconColor = errorLeadingIconColor,
        focusedTrailingIconColor = focusedTrailingIconColor,
        unfocusedTrailingIconColor = AppTheme.colors.neutral400,
        disabledTrailingIconColor = AppTheme.colors.neutral300,
        errorTrailingIconColor = AppTheme.colors.red400,
        focusedLabelColor = focussedLabelColor,
        unfocusedLabelColor = unfocusedLabelColor,
        disabledLabelColor = AppTheme.colors.neutral300,
        errorLabelColor = AppTheme.colors.red400,
        focusedPlaceholderColor = focusedPlaceholderColor,
        unfocusedPlaceholderColor = unfocusedPlaceholderColor,
        disabledPlaceholderColor = AppTheme.colors.neutral100,
        errorPlaceholderColor = AppTheme.colors.red400,
        focusedSupportingTextColor = AppTheme.colors.neutral600,
        unfocusedSupportingTextColor = AppTheme.colors.neutral400,
        disabledSupportingTextColor = AppTheme.colors.neutral100,
        errorSupportingTextColor = AppTheme.colors.red400,
        focusedPrefixColor = AppTheme.colors.neutral600,
        unfocusedPrefixColor = AppTheme.colors.neutral400,
        disabledPrefixColor = AppTheme.colors.neutral100,
        errorPrefixColor = AppTheme.colors.red400,
        focusedSuffixColor = AppTheme.colors.neutral600,
        unfocusedSuffixColor = AppTheme.colors.neutral400,
        disabledSuffixColor = AppTheme.colors.neutral100,
        errorSuffixColor = AppTheme.colors.red400
    )

@LightDarkPreview
@Composable
fun PreviewErezeptOutlineText() {
    PreviewAppTheme {
        Column(Modifier.padding(PaddingDefaults.Medium)) {
            ErezeptOutlineText(
                value = "Value",
                onValueChange = {},
                label = "Label Text",
                placeholder = "Placeholder",
                singleLine = true,
                readOnly = false,
                isError = false,
                minLines = 1,
                maxLines = 1,
                colors = erezeptTextFieldColors()
            )
            SpacerMedium()
            ErezeptOutlineText(
                value = "Value",
                onValueChange = {},
                label = "Label Text",
                placeholder = "Placeholder",
                singleLine = true,
                readOnly = false,
                isError = true,
                minLines = 1,
                maxLines = 1,
                colors = erezeptTextFieldColors()
            )
            SpacerMedium()
            ErezeptOutlineText(
                value = "Value",
                onValueChange = {},
                label = "Label Text",
                placeholder = "Placeholder",
                singleLine = true,
                readOnly = false,
                isError = false,
                minLines = 1,
                maxLines = 1,
                trailingIcon = {
                    Box(Modifier.padding(SizeDefaults.one)) {
                        Icon(Icons.Rounded.Coronavirus, null)
                    }
                },
                colors = erezeptTextFieldColors()
            )
        }
    }
}
