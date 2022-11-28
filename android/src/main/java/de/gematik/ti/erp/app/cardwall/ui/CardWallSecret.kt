/*
 * Copyright (c) 2022 gematik GmbH
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

package de.gematik.ti.erp.app.cardwall.ui

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.ContentAlpha
import androidx.compose.material.Icon
import androidx.compose.material.IconToggleButton
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.material.TextFieldColors
import androidx.compose.material.TextFieldDefaults
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Visibility
import androidx.compose.material.icons.rounded.VisibilityOff
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import de.gematik.ti.erp.app.R
import de.gematik.ti.erp.app.TestTag
import de.gematik.ti.erp.app.pharmacy.ui.scrollOnFocus
import de.gematik.ti.erp.app.theme.AppTheme
import de.gematik.ti.erp.app.theme.PaddingDefaults
import de.gematik.ti.erp.app.utils.compose.ClickableTaggedText
import de.gematik.ti.erp.app.utils.compose.NavigationBarMode
import de.gematik.ti.erp.app.utils.compose.NavigationMode
import de.gematik.ti.erp.app.utils.compose.SpacerSmall
import de.gematik.ti.erp.app.utils.compose.SpacerXXLarge
import de.gematik.ti.erp.app.utils.compose.annotatedLinkStringLight
import de.gematik.ti.erp.app.utils.compose.visualTestTag

@Composable
fun CardWallSecretScreen(
    navMode: NavigationMode,
    secret: String,
    secretRange: IntRange,
    screenTitle: String,
    onSecretChange: (String) -> Unit,
    onCancel: () -> Unit,
    nextText: String,
    next: (String) -> Unit,
    onBack: () -> Unit,
    onClickNoPinReceived: () -> Unit
) {
    val lazyListState = rememberLazyListState()
    CardHandlingScaffold(
        modifier = Modifier.testTag("cardWall/secretScreen"),
        backMode = when (navMode) {
            NavigationMode.Forward,
            NavigationMode.Back,
            NavigationMode.Closed -> NavigationBarMode.Back
            NavigationMode.Open -> NavigationBarMode.Close
        },
        title = screenTitle,
        nextEnabled = secret.length in secretRange,
        onNext = { next(secret) },
        nextText = nextText,
        listState = lazyListState,
        onBack = { onBack() },
        actions = {
            TextButton(onClick = onCancel) {
                Text(stringResource(R.string.cancel))
            }
        }
    ) { innerPadding ->
        val contentPadding by derivedStateOf {
            PaddingValues(
                top = PaddingDefaults.Medium,
                bottom = PaddingDefaults.Medium + innerPadding.calculateBottomPadding(),
                start = PaddingDefaults.Medium,
                end = PaddingDefaults.Medium
            )
        }
        LazyColumn(
            state = lazyListState,
            modifier = Modifier.fillMaxSize(),
            contentPadding = contentPadding
        ) {
            item {
                Text(
                    stringResource(R.string.cdw_pin_title),
                    style = AppTheme.typography.h5
                )

                SpacerSmall()
            }
            item {
                Text(
                    stringResource(R.string.cdw_pin_info),
                    style = AppTheme.typography.body1
                )
                SpacerSmall()
            }
            item {
                ClickableTaggedText(
                    text = annotatedLinkStringLight(
                        uri = "",
                        text = stringResource(R.string.cdw_no_pin_received)
                    ),
                    onClick = { onClickNoPinReceived() },
                    style = AppTheme.typography.body2
                )
                SpacerXXLarge()
            }
            item {
                SecretInputField(
                    modifier = Modifier
                        .fillMaxWidth()
                        .scrollOnFocus(to = 3, lazyListState),
                    secretRange = secretRange,
                    onSecretChange = onSecretChange,
                    secret = secret,
                    label = stringResource(R.string.cdw_pin_label),
                    next = next
                )
            }
        }
    }
}

@Composable
fun SecretInputField(
    modifier: Modifier,
    secretRange: IntRange,
    onSecretChange: (String) -> Unit,
    secret: String,
    label: String,
    next: (String) -> Unit
) {
    val secretRegex = """^\d{0,${secretRange.last}}$""".toRegex()
    var secretVisible by remember { mutableStateOf(false) }

    OutlinedTextField(
        modifier = modifier.visualTestTag(TestTag.CardWall.PIN.PINField),
        value = secret,
        onValueChange = {
            if (it.matches(secretRegex)) {
                onSecretChange(it)
            }
        },
        label = { Text(label) },
        visualTransformation = if (secretVisible) {
            VisualTransformation.None
        } else {
            PasswordVisualTransformation()
        },
        keyboardOptions = KeyboardOptions(
            autoCorrect = false,
            keyboardType = KeyboardType.NumberPassword,
            imeAction = ImeAction.Next
        ),
        shape = RoundedCornerShape(8.dp),
        colors = TextFieldDefaults.outlinedTextFieldColors(
            unfocusedLabelColor = AppTheme.colors.neutral400,
            placeholderColor = AppTheme.colors.neutral400,
            trailingIconColor = AppTheme.colors.neutral400
        ),
        keyboardActions = KeyboardActions {
            next(secret)
        },
        trailingIcon = {
            IconToggleButton(
                checked = secretVisible,
                onCheckedChange = { secretVisible = it }
            ) {
                Icon(
                    if (secretVisible) {
                        Icons.Rounded.Visibility
                    } else {
                        Icons.Rounded.VisibilityOff
                    },
                    null
                )
            }
        }
    )
}

@Composable
fun ConformationSecretInputField(
    modifier: Modifier,
    secretRange: IntRange,
    onSecretChange: (String) -> Unit,
    secret: String,
    repeatedSecret: String,
    label: String,
    isConsistent: Boolean,
    next: (String) -> Unit
) {
    val secretRegex = """^\d{0,${secretRange.last}}$""".toRegex()
    var secretVisible by remember { mutableStateOf(false) }

    OutlinedTextField(
        modifier = modifier,
        value = repeatedSecret,
        onValueChange = {
            if (it.matches(secretRegex)) {
                onSecretChange(it)
            }
        },
        label = { Text(label) },
        visualTransformation = if (secretVisible) {
            VisualTransformation.None
        } else {
            PasswordVisualTransformation()
        },
        keyboardOptions = KeyboardOptions(
            autoCorrect = false,
            keyboardType = KeyboardType.NumberPassword
        ),
        shape = RoundedCornerShape(8.dp),
        colors =
        if (repeatedSecret.isEmpty()) {
            TextFieldDefaults.outlinedTextFieldColors(
                unfocusedLabelColor = AppTheme.colors.neutral400,
                placeholderColor = AppTheme.colors.neutral400,
                trailingIconColor = AppTheme.colors.neutral400
            )
        } else {
            if (isConsistent) {
                textFieldColor(AppTheme.colors.green600)
            } else {
                textFieldColor(AppTheme.colors.red500)
            }
        },
        keyboardActions = KeyboardActions {
            next(secret)
        },
        trailingIcon = {
            if (isConsistent) {
                Icon(
                    Icons.Rounded.Check,
                    stringResource(R.string.consistent_password)
                )
            } else {
                IconToggleButton(
                    checked = secretVisible,
                    onCheckedChange = { secretVisible = it }
                ) {
                    Icon(
                        if (secretVisible) {
                            Icons.Rounded.Visibility
                        } else {
                            Icons.Rounded.VisibilityOff
                        },
                        null
                    )
                }
            }
        }
    )
}

@Composable
private fun textFieldColor(color: Color): TextFieldColors {
    return TextFieldDefaults.outlinedTextFieldColors(
        focusedBorderColor = color.copy(
            alpha = ContentAlpha.high
        ),
        focusedLabelColor = color.copy(
            alpha = ContentAlpha.high
        ),
        unfocusedBorderColor = color.copy(alpha = ContentAlpha.high),
        unfocusedLabelColor = color.copy(
            alpha = ContentAlpha.high
        ),
        trailingIconColor = color.copy(
            alpha = ContentAlpha.high
        )
    )
}
