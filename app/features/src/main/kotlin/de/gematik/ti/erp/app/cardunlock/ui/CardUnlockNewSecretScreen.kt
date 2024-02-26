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

package de.gematik.ti.erp.app.cardunlock.ui

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
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
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavController
import de.gematik.ti.erp.app.cardunlock.navigation.CardUnlockRoutes
import de.gematik.ti.erp.app.cardunlock.navigation.CardUnlockScreen
import de.gematik.ti.erp.app.cardunlock.presentation.CardUnlockGraphController
import de.gematik.ti.erp.app.cardwall.ui.components.CardWallScaffold
import de.gematik.ti.erp.app.cardwall.ui.PIN_RANGE
import de.gematik.ti.erp.app.cardwall.ui.PinInputField
import de.gematik.ti.erp.app.features.R
import de.gematik.ti.erp.app.pharmacy.ui.scrollOnFocus
import de.gematik.ti.erp.app.theme.AppTheme
import de.gematik.ti.erp.app.theme.PaddingDefaults
import de.gematik.ti.erp.app.theme.SizeDefaults
import de.gematik.ti.erp.app.utils.compose.HintCard
import de.gematik.ti.erp.app.utils.compose.HintSmallImage
import de.gematik.ti.erp.app.utils.compose.LightDarkPreview
import de.gematik.ti.erp.app.utils.compose.NavigationBarMode
import de.gematik.ti.erp.app.utils.compose.PreviewAppTheme
import de.gematik.ti.erp.app.utils.compose.SpacerLarge
import de.gematik.ti.erp.app.utils.compose.SpacerMedium
import de.gematik.ti.erp.app.utils.compose.SpacerTiny
import de.gematik.ti.erp.app.utils.compose.SpacerXXLarge

const val InputFieldPosition = 2
class CardUnlockNewSecretScreen(
    override val navController: NavController,
    override val navBackStackEntry: NavBackStackEntry,
    override val graphController: CardUnlockGraphController
) : CardUnlockScreen() {
    @Composable
    override fun Content() {
        val newPin by graphController.newPin.collectAsStateWithLifecycle()
        var repeatedNewPin by remember { mutableStateOf("") }
        val isConsistent by remember(newPin, repeatedNewPin) {
            derivedStateOf {
                repeatedNewPin.isNotBlank() && newPin == repeatedNewPin
            }
        }

        val lazyListState = rememberLazyListState()
        CardWallScaffold(
            modifier = Modifier.testTag("cardWall/secretScreen"),
            backMode = NavigationBarMode.Back,
            title = stringResource(R.string.unlock_egk_top_bar_title_change_secret),
            nextEnabled = newPin.length in PIN_RANGE && isConsistent,
            listState = lazyListState,
            onNext = {
                navController.navigate(
                    CardUnlockRoutes.CardUnlockEgkScreen.path()
                )
            },
            onBack = {
                navController.popBackStack()
            },
            nextText = stringResource(R.string.unlock_egk_next),
            actions = {
                TextButton(onClick = {
                    graphController.reset()
                    navController.popBackStack(CardUnlockRoutes.CardUnlockIntroScreen.route, inclusive = true)
                }) {
                    Text(stringResource(R.string.cancel))
                }
            }
        ) { innerPadding ->
            CardUnlockNewSecretScreenContent(
                newPin = newPin,
                innerPadding = innerPadding,
                lazyListState = lazyListState,
                repeatedNewPin = repeatedNewPin,
                isConsistent = isConsistent,
                pinRange = PIN_RANGE,
                onNewPinChange = graphController::setNewPin,
                onRepeatedPinChange = { repeatedNewPin = it },
                onNext = {
                    navController.navigate(
                        CardUnlockRoutes.CardUnlockEgkScreen.path()
                    )
                }
            )
        }
    }
}

@Composable
private fun CardUnlockNewSecretScreenContent(
    newPin: String,
    innerPadding: PaddingValues,
    lazyListState: LazyListState,
    repeatedNewPin: String,
    isConsistent: Boolean,
    pinRange: IntRange,
    onNewPinChange: (String) -> Unit,
    onRepeatedPinChange: (String) -> Unit,
    onNext: () -> Unit
) {
    val contentPadding by remember(innerPadding) {
        derivedStateOf {
            PaddingValues(
                top = PaddingDefaults.Medium,
                bottom = PaddingDefaults.Medium + innerPadding.calculateBottomPadding(),
                start = PaddingDefaults.Medium,
                end = PaddingDefaults.Medium
            )
        }
    }
    LazyColumn(
        state = lazyListState,
        modifier = Modifier.fillMaxSize(),
        contentPadding = contentPadding
    ) {
        item {
            Text(
                stringResource(R.string.unlock_egk_new_secret_title),
                style = AppTheme.typography.h5
            )
            SpacerMedium()
        }
        item {
            PinInputField(
                modifier = Modifier
                    .fillMaxWidth()
                    .scrollOnFocus(1, lazyListState),
                pinRange = pinRange,
                onPinChange = onNewPinChange,
                isConsistent = isConsistent,
                pin = newPin,
                label = stringResource(R.string.unlock_egk_choose_new_secret_label),
                onNext = onNext,
                infoText = stringResource(R.string.unlock_egk_new_secret_info)
            )
        }
        item {
            SpacerLarge()
            ConformationPinInputField(
                modifier = Modifier
                    .fillMaxWidth()
                    .scrollOnFocus(InputFieldPosition, lazyListState),
                pinRange = pinRange,
                repeatedPin = repeatedNewPin,
                onRepeatedPinChange = onRepeatedPinChange,
                pin = newPin,
                isConsistent = isConsistent,
                label = stringResource(R.string.unlock_egk_repeat_secret_label),
                onNext = onNext
            )
            if (repeatedNewPin.isNotBlank() && !isConsistent) {
                SpacerTiny()
                Text(
                    stringResource(R.string.not_matching_entries),
                    style = AppTheme.typography.caption1,
                    color = AppTheme.colors.red600.copy(
                        alpha = ContentAlpha.high
                    )
                )
            }
        }
        item {
            SpacerXXLarge()
            HintCard(
                modifier = Modifier,
                image = {
                    HintSmallImage(
                        painterResource(R.drawable.information),
                        innerPadding = it
                    )
                },
                title = { Text(stringResource(R.string.unlock_egk_new_secret_extra_content_title)) },
                body = { Text(stringResource(R.string.unlock_egk_new_secret_extra_content_info)) }
            )
            SpacerMedium()
        }
    }
}

@Composable
fun ConformationPinInputField(
    modifier: Modifier,
    pinRange: IntRange,
    onRepeatedPinChange: (String) -> Unit,
    pin: String,
    repeatedPin: String,
    label: String,
    isConsistent: Boolean,
    onNext: () -> Unit
) {
    val secretRegexString = "^\\d{0,${pinRange.last}}$"
    val secretRegex = secretRegexString.toRegex()
    var secretVisible by remember { mutableStateOf(false) }

    OutlinedTextField(
        modifier = modifier,
        value = repeatedPin,
        onValueChange = {
            if (it.matches(secretRegex)) {
                onRepeatedPinChange(it)
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
        shape = RoundedCornerShape(SizeDefaults.one),
        colors =
        if (repeatedPin.isEmpty()) {
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
            if (isConsistent && pin.length in pinRange) {
                onNext()
            }
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
private fun textFieldColor(color: Color): TextFieldColors =
    TextFieldDefaults.outlinedTextFieldColors(
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

@LightDarkPreview
@Composable
fun SecretChangeLazyColumnPreview() {
    val newSecret by rememberSaveable { mutableStateOf("") }
    val repeatedNewSecret by remember { mutableStateOf("") }
    val isConsistent = true
    val lazyListState = rememberLazyListState()
    val secretRange = PIN_RANGE
    PreviewAppTheme {
        CardUnlockNewSecretScreenContent(
            newPin = newSecret,
            innerPadding = PaddingValues(PaddingDefaults.Medium),
            lazyListState = lazyListState,
            repeatedNewPin = repeatedNewSecret,
            isConsistent = isConsistent,
            pinRange = secretRange,
            onNewPinChange = {},
            onRepeatedPinChange = {},
            onNext = {}
        )
    }
}
