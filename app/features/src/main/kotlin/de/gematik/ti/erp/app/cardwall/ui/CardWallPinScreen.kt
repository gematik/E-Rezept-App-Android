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

package de.gematik.ti.erp.app.cardwall.ui

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.Icon
import androidx.compose.material.IconToggleButton
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.material.TextFieldDefaults
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Visibility
import androidx.compose.material.icons.rounded.VisibilityOff
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavController
import de.gematik.ti.erp.app.Requirement
import de.gematik.ti.erp.app.TestTag
import de.gematik.ti.erp.app.cardwall.domain.biometric.deviceStrongBiometricStatus
import de.gematik.ti.erp.app.cardwall.domain.biometric.hasDeviceStrongBox
import de.gematik.ti.erp.app.cardwall.domain.biometric.isDeviceSupportsBiometric
import de.gematik.ti.erp.app.cardwall.navigation.CardWallRoutes
import de.gematik.ti.erp.app.cardwall.navigation.CardWallScreen
import de.gematik.ti.erp.app.cardwall.presentation.CardWallGraphController
import de.gematik.ti.erp.app.cardwall.ui.components.CardWallScaffold
import de.gematik.ti.erp.app.features.R
import de.gematik.ti.erp.app.mainscreen.navigation.MainNavigationScreens
import de.gematik.ti.erp.app.pharmacy.ui.scrollOnFocus
import de.gematik.ti.erp.app.theme.AppTheme
import de.gematik.ti.erp.app.theme.PaddingDefaults
import de.gematik.ti.erp.app.theme.SizeDefaults
import de.gematik.ti.erp.app.utils.compose.ClickableTaggedText
import de.gematik.ti.erp.app.utils.compose.SpacerSmall
import de.gematik.ti.erp.app.utils.compose.SpacerTiny
import de.gematik.ti.erp.app.utils.compose.SpacerXXLarge
import de.gematik.ti.erp.app.utils.compose.annotatedLinkStringLight
import de.gematik.ti.erp.app.utils.compose.annotatedStringResource
import de.gematik.ti.erp.app.utils.compose.visualTestTag

val PIN_RANGE = 6..8

@Requirement(
    "O.Purp_2#3",
    "O.Data_6#3",
    sourceSpecification = "BSI-eRp-ePA",
    rationale = "PIN is used for eGK connection."
)
class CardWallPinScreen(
    override val navController: NavController,
    override val navBackStackEntry: NavBackStackEntry,
    override val graphController: CardWallGraphController
) : CardWallScreen() {
    @Composable
    override fun Content() {
        val pin by graphController.pin.collectAsStateWithLifecycle()
        val lazyListState = rememberLazyListState()
        val context = LocalContext.current
        val biometricMode = remember { deviceStrongBiometricStatus(context) }
        val onNext = {
            val deviceSupportsBiometric = isDeviceSupportsBiometric(biometricMode)

            @Requirement(
                "O.Biom_2#1",
                "O.Biom_3#1",
                sourceSpecification = "BSI-eRp-ePA",
                rationale = "Only if device has strongbox, the user can select authentication with " +
                    "biometric prompt"
            )
            val deviceSupportsStrongbox = hasDeviceStrongBox(context)
            if (deviceSupportsBiometric &&
                deviceSupportsStrongbox
            ) {
                navController.navigate(CardWallRoutes.CardWallSaveCredentialsScreen.path())
            } else {
                navController.navigate(CardWallRoutes.CardWallReadCardScreen.path())
            }
        }
        CardWallScaffold(
            modifier = Modifier.testTag(TestTag.CardWall.PIN.PinScreen),
            onBack = {
                navController.popBackStack()
            },
            title = stringResource(R.string.cdw_top_bar_title),
            nextEnabled = pin.length in PIN_RANGE,
            onNext = onNext,
            listState = lazyListState,
            nextText = stringResource(R.string.unlock_egk_next),
            actions = {
                TextButton(
                    onClick = {
                        graphController.reset()
                        navController.popBackStack(CardWallRoutes.CardWallIntroScreen.route, inclusive = true)
                    }
                ) {
                    Text(stringResource(R.string.cancel))
                }
            }
        ) { innerPadding ->
            CardWallPinScreenContent(
                lazyListState = lazyListState,
                innerPadding = innerPadding,
                pin = pin,
                onNext = onNext,
                onPinChange = { graphController.setPersonalIdentificationNumber(it) },
                onClickNoPinReceived = {
                    navController.navigate(MainNavigationScreens.OrderHealthCard.path())
                }
            )
        }
    }
}

@Composable
private fun CardWallPinScreenContent(
    lazyListState: LazyListState,
    innerPadding: PaddingValues,
    onNext: () -> Unit,
    pin: String,
    onPinChange: (String) -> Unit,
    onClickNoPinReceived: () -> Unit
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
                modifier = Modifier.testTag(TestTag.CardWall.PIN.OrderEgkButton),
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
            PinInputField(
                modifier = Modifier
                    .fillMaxWidth()
                    .scrollOnFocus(to = 3, lazyListState),
                pinRange = PIN_RANGE,
                onPinChange = onPinChange,
                pin = pin,
                onNext = onNext,
                infoText = annotatedStringResource(
                    R.string.cdw_pin_length_info,
                    PIN_RANGE.first.toString(),
                    PIN_RANGE.last.toString()
                ).text
            )
        }
    }
}

@Requirement(
    "O.Data_10",
    "O.Data_11",
    sourceSpecification = "BSI-eRp-ePA",
    rationale = "Password fields using the  keyboard type numberPassword. Copying the content is not possible " +
        "with this type. Autocorrect is disallowed. It`s not possible to disable third party keyboards."
)
@Composable
fun PinInputField(
    modifier: Modifier,
    pinRange: IntRange,
    onPinChange: (String) -> Unit,
    pin: String,
    label: String = stringResource(R.string.cdw_pin_label),
    isConsistent: Boolean = true,
    infoText: String,
    onNext: () -> Unit
) {
    val secretRegexString = "^\\d{0,${pinRange.last}}$"
    val secretRegex = secretRegexString.toRegex()
    var secretVisible by remember { mutableStateOf(false) }
    OutlinedTextField(
        modifier = modifier.visualTestTag(TestTag.CardWall.PIN.PINField),
        value = pin,
        onValueChange = {
            if (it.matches(secretRegex)) {
                onPinChange(it)
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
        colors = TextFieldDefaults.outlinedTextFieldColors(
            unfocusedLabelColor = AppTheme.colors.neutral400,
            placeholderColor = AppTheme.colors.neutral400,
            trailingIconColor = AppTheme.colors.neutral400
        ),
        keyboardActions = KeyboardActions {
            if (isConsistent && pin.length in pinRange) {
                onNext()
            }
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
    SpacerTiny()
    Text(
        text = infoText,
        style = AppTheme.typography.caption1l
    )
}
