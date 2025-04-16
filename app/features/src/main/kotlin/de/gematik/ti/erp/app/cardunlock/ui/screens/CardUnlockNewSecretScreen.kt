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

package de.gematik.ti.erp.app.cardunlock.ui.screens

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
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Visibility
import androidx.compose.material.icons.rounded.VisibilityOff
import androidx.compose.material3.TextFieldColors
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
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
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavController
import de.gematik.ti.erp.app.cardunlock.navigation.CardUnlockRoutes
import de.gematik.ti.erp.app.cardunlock.navigation.CardUnlockScreen
import de.gematik.ti.erp.app.cardunlock.presentation.CardUnlockGraphController
import de.gematik.ti.erp.app.cardwall.ui.components.CardWallScaffold
import de.gematik.ti.erp.app.cardwall.ui.screens.PIN_RANGE
import de.gematik.ti.erp.app.cardwall.ui.screens.PinInputField
import de.gematik.ti.erp.app.features.R
import de.gematik.ti.erp.app.theme.AppTheme
import de.gematik.ti.erp.app.theme.PaddingDefaults
import de.gematik.ti.erp.app.theme.SizeDefaults
import de.gematik.ti.erp.app.utils.SpacerLarge
import de.gematik.ti.erp.app.utils.SpacerMedium
import de.gematik.ti.erp.app.utils.SpacerTiny
import de.gematik.ti.erp.app.utils.SpacerXXLarge
import de.gematik.ti.erp.app.utils.compose.ErezeptOutlineText
import de.gematik.ti.erp.app.utils.compose.HintCard
import de.gematik.ti.erp.app.utils.compose.HintSmallImage
import de.gematik.ti.erp.app.utils.compose.LightDarkPreview
import de.gematik.ti.erp.app.utils.compose.NavigationBarMode
import de.gematik.ti.erp.app.utils.compose.erezeptTextFieldColors
import de.gematik.ti.erp.app.utils.compose.preview.CardUnlockNewSecretScreenPreviewData
import de.gematik.ti.erp.app.utils.compose.preview.CardUnlockNewSecretScreenPreviewDataProvider
import de.gematik.ti.erp.app.utils.compose.preview.PreviewAppTheme
import de.gematik.ti.erp.app.utils.compose.rememberContentPadding
import de.gematik.ti.erp.app.utils.compose.scrollOnFocus

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
    val contentPadding by rememberContentPadding(innerPadding)

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

    ErezeptOutlineText(
        modifier = modifier,
        value = repeatedPin,
        onValueChange = {
            if (it.matches(secretRegex)) {
                onRepeatedPinChange(it)
            }
        },
        label = label,
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
            erezeptTextFieldColors(unfocusedLabelColor = AppTheme.colors.neutral400)
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
    erezeptTextFieldColors(
        focussedBorderColor = color.copy(
            alpha = ContentAlpha.high
        ),
        focussedLabelColor = color.copy(
            alpha = ContentAlpha.high
        ),
        unfocusedBorderColor = color.copy(alpha = ContentAlpha.high),
        unfocusedLabelColor = color.copy(
            alpha = ContentAlpha.high
        ),
        focusedTrailingIconColor = color.copy(
            alpha = ContentAlpha.high
        )
    )

@LightDarkPreview
@Composable
fun CardUnlockNewSecretScreenPreview(
    @PreviewParameter(CardUnlockNewSecretScreenPreviewDataProvider::class) data: CardUnlockNewSecretScreenPreviewData
) {
    val lazyListState = rememberLazyListState()
    PreviewAppTheme {
        CardUnlockNewSecretScreenContent(
            newPin = data.pin,
            innerPadding = PaddingValues(PaddingDefaults.Medium),
            lazyListState = lazyListState,
            repeatedNewPin = data.repeatedNewPin,
            isConsistent = data.isConsistent,
            pinRange = PIN_RANGE,
            onNewPinChange = {},
            onRepeatedPinChange = {},
            onNext = {}
        )
    }
}
