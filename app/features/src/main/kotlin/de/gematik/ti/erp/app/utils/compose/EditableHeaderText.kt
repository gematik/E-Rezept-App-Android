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

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.Text
import androidx.compose.material.TextField
import androidx.compose.material.TextFieldDefaults
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import de.gematik.ti.erp.app.features.R
import de.gematik.ti.erp.app.theme.AppTheme

const val TextMinLength = 1
const val MaxLines = 3

@Composable
fun EditableHeaderTextField(
    text: String,
    textMinLength: Int = TextMinLength,
    onSaveText: (String) -> Unit
) {
    var isEditing by remember { mutableStateOf(false) }
    var name by remember { mutableStateOf(text) }

    if (isEditing) {
        EditableTextField(
            text = text,
            textMinLength = textMinLength
        ) {
            onSaveText(it)
            isEditing = false
            name = it
        }
    } else {
        HeaderText(name) {
            isEditing = true
        }
    }
}

@Composable
private fun HeaderText(name: String, onClickEdit: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().wrapContentWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            modifier = Modifier.weight(WEIGHT_1_0).wrapContentWidth(),
            text = name,
            style = AppTheme.typography.h5,
            textAlign = TextAlign.Center,
            maxLines = MaxLines,
            overflow = TextOverflow.Ellipsis
        )
        EditIconButton { onClickEdit() }
    }
}

@Composable
private fun EditIconButton(
    onClick: () -> Unit
) {
    IconButton(
        onClick = onClick
    ) {
        Icon(Icons.Outlined.Edit, null, tint = AppTheme.colors.neutral600)
    }
}

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun EditableTextField(
    modifier: Modifier = Modifier,
    text: String,
    textMinLength: Int,
    onDoneClicked: (String) -> Unit
) {
    var name by remember { mutableStateOf(TextFieldValue(text)) }
    val focusManager = LocalFocusManager.current
    val focusRequester = remember { FocusRequester() }
    val keyboardController = LocalSoftwareKeyboardController.current
    var isError by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
    }

    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        TextField(
            modifier = Modifier.focusRequester(focusRequester),
            colors = basicTextFieldColors,
            textStyle = AppTheme.typography.h5.copy(textAlign = TextAlign.Center),
            keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Done),
            isError = isError,
            value = name,
            onValueChange = {
                name = it
                isError = name.text.length < textMinLength
            },
            keyboardActions = KeyboardActions(
                onDone = {
                    if (!isError) {
                        focusManager.clearFocus()
                        keyboardController?.hide()
                        onDoneClicked(name.text.trim())
                    }
                }
            )
        )
        AnimatedVisibility(isError) {
            SpacerTiny()
            ErrorText(
                modifier = Modifier.testTag(ErrorTextTag),
                text = stringResource(R.string.empty_scanned_prescription_name)
            )
        }
    }
}

private val basicTextFieldColors
    @Composable
    get() = TextFieldDefaults.textFieldColors(
        backgroundColor = Color.Unspecified,
        unfocusedIndicatorColor = Color.Unspecified
    )
