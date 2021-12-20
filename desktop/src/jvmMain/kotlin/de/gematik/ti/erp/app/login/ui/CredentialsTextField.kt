package de.gematik.ti.erp.app.login.ui

import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.runtime.Composable
import androidx.compose.runtime.currentRecomposeScope
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun CredentialsTextField(
    value: String,
    valuePattern: Regex,
    onValueChange: (String) -> Unit,
    onEnter: () -> Unit,
    modifier: Modifier = Modifier,
    keyboardOptions: KeyboardOptions = KeyboardOptions(
        keyboardType = KeyboardType.NumberPassword,
        imeAction = ImeAction.Next
    ),
    keyboardActions: KeyboardActions = KeyboardActions.Default,
    decorationBox: @Composable (innerTextField: @Composable () -> Unit) -> Unit
) {
    val textValue = TextFieldValue(
        text = value,
        selection = TextRange(value.length)
    )

    val scope = currentRecomposeScope

    BasicTextField(
        value = textValue,
        onValueChange = {
            if (it.text.matches(valuePattern)) {
                onValueChange(it.text)
            } else {
                // internal state of BasicTextField is otherwise not updated
                scope.invalidate()
            }
        },
        keyboardOptions = keyboardOptions,
        keyboardActions = keyboardActions,
        singleLine = true,
        modifier = modifier
            .onPreviewKeyEvent {
                if (it.key == Key.Enter) {
                    onEnter()
                }
                false
            },
        decorationBox = decorationBox
    )
}
