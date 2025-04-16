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

package de.gematik.ti.erp.app.cardwall.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.selection.DisableSelection
import androidx.compose.material.Icon
import androidx.compose.material.IconToggleButton
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Visibility
import androidx.compose.material.icons.rounded.VisibilityOff
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavController
import de.gematik.ti.erp.app.Requirement
import de.gematik.ti.erp.app.TestTag
import de.gematik.ti.erp.app.authentication.presentation.deviceDeviceSecurityStatus
import de.gematik.ti.erp.app.authentication.presentation.deviceHardwareBackedKeystoreStatus
import de.gematik.ti.erp.app.authentication.presentation.deviceSupportsAuthenticationMethod
import de.gematik.ti.erp.app.cardwall.navigation.CardWallRoutes
import de.gematik.ti.erp.app.cardwall.navigation.CardWallScreen
import de.gematik.ti.erp.app.cardwall.presentation.CardWallGraphController
import de.gematik.ti.erp.app.cardwall.ui.components.CardWallScaffold
import de.gematik.ti.erp.app.cardwall.ui.preview.CardWallPinScreenPreviewData
import de.gematik.ti.erp.app.cardwall.ui.preview.CardWallPinScreenPreviewParameterProvider
import de.gematik.ti.erp.app.features.R
import de.gematik.ti.erp.app.orderhealthcard.navigation.OrderHealthCardRoutes
import de.gematik.ti.erp.app.theme.AppTheme
import de.gematik.ti.erp.app.theme.SizeDefaults
import de.gematik.ti.erp.app.utils.SpacerSmall
import de.gematik.ti.erp.app.utils.SpacerTiny
import de.gematik.ti.erp.app.utils.SpacerXXLarge
import de.gematik.ti.erp.app.utils.compose.ClickableTaggedText
import de.gematik.ti.erp.app.utils.compose.ErezeptOutlineText
import de.gematik.ti.erp.app.utils.compose.LightDarkPreview
import de.gematik.ti.erp.app.utils.compose.annotatedLinkStringLight
import de.gematik.ti.erp.app.utils.compose.annotatedStringResource
import de.gematik.ti.erp.app.utils.compose.preview.PreviewAppTheme
import de.gematik.ti.erp.app.utils.compose.rememberContentPadding
import de.gematik.ti.erp.app.utils.compose.scrollOnFocus
import de.gematik.ti.erp.app.utils.extensions.disableCopyPasteFromKeyboard
import de.gematik.ti.erp.app.utils.isNotNullOrEmpty
import de.gematik.ti.erp.app.utils.letNotNullOnCondition

val PIN_RANGE = 6..8

@Requirement(
    "O.Data_6#8",
    sourceSpecification = "BSI-eRp-ePA",
    rationale = ".. the screen where PIN is entered from."
)
@Requirement(
    "O.Purp_2#3",
    sourceSpecification = "BSI-eRp-ePA",
    rationale = "The Pin is used to verify that the user is the owner of the eGK. (has the knowledge of the PIN)"
)
class CardWallPinScreen(
    override val navController: NavController,
    override val navBackStackEntry: NavBackStackEntry,
    override val graphController: CardWallGraphController
) : CardWallScreen() {
    @Composable
    override fun Content() {
        val canNumber = navBackStackEntry.arguments?.getString(CardWallRoutes.CARD_WALL_NAV_CAN)
        val profileId = navBackStackEntry.arguments?.getString(CardWallRoutes.CARD_WALL_PIN_NAV_PROFILE_ID)

        // set the CAN and profileId in the graphController when its starting this screen from the navigation
        letNotNullOnCondition(
            first = canNumber,
            second = profileId,
            condition = {
                canNumber.isNotNullOrEmpty() && profileId.isNotNullOrEmpty()
            }
        ) { can, id ->
            graphController.setCardAccessNumber(can)
            graphController.setProfileId(id)
        }

        val pin by graphController.pin.collectAsStateWithLifecycle()
        val lazyListState = rememberLazyListState()
        val context = LocalContext.current
        val biometricState = remember { context.deviceDeviceSecurityStatus() }
        val onNext = {
            val deviceSupportsBiometrics = deviceSupportsAuthenticationMethod(biometricState)

            if (context.deviceHardwareBackedKeystoreStatus() && deviceSupportsBiometrics) {
                navController.navigate(CardWallRoutes.CardWallSaveCredentialsScreen.path())
            } else {
                navController.navigate(CardWallRoutes.CardWallReadCardScreen.path())
            }
        }
        val onBack: () -> Unit = {
            graphController.resetPin()
            navController.popBackStack()
        }
        val onExit: () -> Unit = {
            graphController.reset()
            navController.popBackStack(CardWallRoutes.subGraphName(), inclusive = true)
        }
        BackHandler { onBack() }

        CardWallScaffold(
            modifier = Modifier.testTag(TestTag.CardWall.PIN.PinScreen),
            onBack = onBack,
            title = stringResource(R.string.cdw_top_bar_title),
            nextEnabled = pin.length in PIN_RANGE,
            onNext = onNext,
            listState = lazyListState,
            nextText = stringResource(R.string.unlock_egk_next),
            actions = {
                TextButton(
                    onClick = onExit
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
                    navController.navigate(OrderHealthCardRoutes.OrderHealthCardSelectInsuranceCompanyScreen.path())
                }
            )
        }
    }
}

@Requirement(
    "O.Data_6#3",
    sourceSpecification = "BSI-eRp-ePA",
    rationale = "PIN is used for eGK connection."
)
@Composable
private fun CardWallPinScreenContent(
    lazyListState: LazyListState,
    innerPadding: PaddingValues,
    onNext: () -> Unit,
    pin: String,
    onPinChange: (String) -> Unit,
    onClickNoPinReceived: () -> Unit
) {
    val contentPadding by rememberContentPadding(innerPadding)

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
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag(TestTag.CardWall.PIN.OrderEgkButton),
                contentAlignment = Alignment.CenterEnd
            ) {
                ClickableTaggedText(
                    text = annotatedLinkStringLight(
                        uri = "",
                        text = stringResource(R.string.cdw_no_pin_received)
                    ),
                    onClick = { onClickNoPinReceived() },
                    style = AppTheme.typography.body2
                )
            }
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
    "O.Data_11#2",
    sourceSpecification = "BSI-eRp-ePA",
    rationale = "Password fields using the  keyboard type numberPassword. " +
        "Autocorrect is disallowed. It`s not possible to disable third party keyboards."
)
@Requirement(
    "O.Data_10#4",
    sourceSpecification = "BSI-eRp-ePA",
    rationale = "..using the autofill disabled keyboard options for pin entry.",
    codeLines = 30
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
    DisableSelection {
        ErezeptOutlineText(
            modifier = modifier.disableCopyPasteFromKeyboard(),
            value = pin,
            onValueChange = {
                if (it.matches(secretRegex)) {
                    onPinChange(it)
                }
            },
            label = label,
            visualTransformation = if (secretVisible) {
                VisualTransformation.None
            } else {
                PasswordVisualTransformation()
            },
            keyboardOptions = autofillDisabledNumberKeyboardOptions(),
            shape = RoundedCornerShape(SizeDefaults.one),
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
    }
    SpacerTiny()
    Text(
        text = infoText,
        style = AppTheme.typography.caption1l
    )
}

@Requirement(
    "O.Data_10#3",
    sourceSpecification = "BSI-eRp-ePA",
    rationale = "By disabling autofill we eliminate any recordings that can be done while typing the keyboard."
)
private fun autofillDisabledNumberKeyboardOptions() =
    KeyboardOptions(
        autoCorrect = false,
        keyboardType = KeyboardType.NumberPassword,
        imeAction = ImeAction.Next
    )

@LightDarkPreview
@Composable
fun CardWallPinScreenPreview(
    @PreviewParameter(
        CardWallPinScreenPreviewParameterProvider::class
    ) previewData: CardWallPinScreenPreviewData
) {
    PreviewAppTheme {
        CardWallScaffold(
            onBack = { },
            title = stringResource(R.string.cdw_top_bar_title),
            nextEnabled = previewData.pin.length in PIN_RANGE,
            onNext = { },
            listState = rememberLazyListState(),
            nextText = stringResource(R.string.unlock_egk_next)
        ) { innerPadding ->
            CardWallPinScreenContent(
                lazyListState = rememberLazyListState(),
                innerPadding = innerPadding,
                pin = previewData.pin,
                onNext = { },
                onPinChange = { },
                onClickNoPinReceived = { }
            )
        }
    }
}
