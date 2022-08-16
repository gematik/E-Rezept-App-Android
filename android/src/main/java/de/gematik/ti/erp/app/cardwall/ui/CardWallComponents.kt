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

import android.content.Intent
import android.nfc.Tag
import android.os.Build
import android.provider.Settings
import androidx.biometric.BiometricManager
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Card
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Fingerprint
import androidx.compose.material.icons.rounded.RadioButtonUnchecked
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat.startActivity
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navOptions
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.isImeVisible
import androidx.compose.foundation.layout.navigationBarsPadding
import de.gematik.ti.erp.app.R
import de.gematik.ti.erp.app.TestTag
import de.gematik.ti.erp.app.cardunlock.ui.UnlockEgKScreen
import de.gematik.ti.erp.app.cardwall.domain.biometric.deviceStrongBiometricStatus
import de.gematik.ti.erp.app.cardwall.domain.biometric.hasDeviceStrongBox
import de.gematik.ti.erp.app.cardwall.domain.biometric.isDeviceSupportsBiometric
import de.gematik.ti.erp.app.cardwall.ui.model.CardWallData
import de.gematik.ti.erp.app.cardwall.ui.model.CardWallNavigation
import de.gematik.ti.erp.app.core.LocalActivity
import de.gematik.ti.erp.app.orderhealthcard.ui.HealthCardContactOrderScreen
import de.gematik.ti.erp.app.profiles.repository.ProfileIdentifier
import de.gematik.ti.erp.app.theme.AppTheme
import de.gematik.ti.erp.app.theme.PaddingDefaults
import de.gematik.ti.erp.app.analytics.TrackNavigationChanges
import de.gematik.ti.erp.app.utils.compose.AnimatedElevationScaffold
import de.gematik.ti.erp.app.utils.compose.CommonAlertDialog
import de.gematik.ti.erp.app.utils.compose.HintCard
import de.gematik.ti.erp.app.utils.compose.HintSmallImage
import de.gematik.ti.erp.app.utils.compose.NavigationAnimation
import de.gematik.ti.erp.app.utils.compose.NavigationBarMode
import de.gematik.ti.erp.app.utils.compose.NavigationMode
import de.gematik.ti.erp.app.utils.compose.SpacerMedium
import de.gematik.ti.erp.app.utils.compose.SpacerSmall
import de.gematik.ti.erp.app.utils.compose.SpacerXXLarge
import de.gematik.ti.erp.app.utils.compose.annotatedLinkString
import de.gematik.ti.erp.app.utils.compose.annotatedStringResource
import de.gematik.ti.erp.app.utils.compose.navigationModeState
import org.kodein.di.compose.rememberViewModel

@Composable
fun CardWallScreen(
    mainNavController: NavController,
    onResumeCardWall: () -> Unit,
    profileId: ProfileIdentifier
) {
    val viewModel: CardWallViewModel by rememberViewModel()

    val navController = rememberNavController()

    val state by viewModel.state(profileId).collectAsState(viewModel.defaultState)

    val startDestination = when {
        state.cardAccessNumber.isNotBlank() -> CardWallNavigation.PersonalIdentificationNumber.path()
        state.hardwareRequirementsFulfilled -> CardWallNavigation.Intro.path()
        else -> CardWallNavigation.MissingCapabilities.path()
    }

    val context = LocalContext.current
    val biometricMode = remember { deviceStrongBiometricStatus(context) }

    val navigationMode by navController.navigationModeState(
        startDestination = startDestination,
        intercept = { previousRoute: String?, currentRoute: String? ->
            if (previousRoute == CardWallNavigation.OrderHealthCard.route && currentRoute == CardWallNavigation.Intro.route) {
                NavigationMode.Closed
            } else {
                null
            }
        }
    )

    val onRetryCan = {
        navController.navigate(CardWallNavigation.CardAccessNumber.path()) {
            popUpTo(CardWallNavigation.CardAccessNumber.path()) { inclusive = true }
        }
    }

    val onRetryPin = {
        navController.navigate(CardWallNavigation.PersonalIdentificationNumber.path()) {
            popUpTo(CardWallNavigation.PersonalIdentificationNumber.path()) {
                inclusive = true
            }
        }
    }

    val onUnlockEgk = {
        navController.navigate(CardWallNavigation.UnlockEgk.path()) {
            popUpTo(CardWallNavigation.UnlockEgk.path()) {
                inclusive = true
            }
        }
    }

    val onBack: () -> Unit = {
        if (navController.currentDestination?.route == startDestination) {
            mainNavController.popBackStack()
        } else {
            navController.popBackStack()
        }
    }

    TrackNavigationChanges(navController)

    var cardAccessNumber by rememberSaveable(state.cardAccessNumber) { mutableStateOf(state.cardAccessNumber) }
    var personalIdentificationNumber by rememberSaveable(state.personalIdentificationNumber) { mutableStateOf(state.personalIdentificationNumber) }
    var selectedAuthenticationMethod by rememberSaveable(state.selectedAuthenticationMethod) { mutableStateOf(state.selectedAuthenticationMethod) }

    NavHost(
        navController,
        startDestination = startDestination
    ) {
        composable(CardWallNavigation.Intro.route) {
            NavigationAnimation(mode = navigationMode) {
                CardWallIntroScaffold(
                    onNext = { navController.navigate(CardWallNavigation.CardAccessNumber.path()) },
                    actions = {
                        TextButton(onClick = { onResumeCardWall() }) {
                            Text(stringResource(R.string.cancel))
                        }
                    },
                    onClickAlternateAuthentication = { navController.navigate(CardWallNavigation.ExternalAuthenticator.path()) },
                    onClickOrderNow = { navController.navigate(CardWallNavigation.OrderHealthCard.path()) }
                )
            }
        }

        composable(CardWallNavigation.OrderHealthCard.route) {
            HealthCardContactOrderScreen(onBack = onBack)
        }

        composable(CardWallNavigation.MissingCapabilities.route) {
            CardWallMissingCapabilities()
        }

        composable(
            CardWallNavigation.CardAccessNumber.route,
            CardWallNavigation.CardAccessNumber.arguments
        ) {
            NavigationAnimation(mode = navigationMode) {
                CardAccessNumber(
                    onClickLearnMore = { navController.navigate(CardWallNavigation.OrderHealthCard.path()) },
                    can = cardAccessNumber,
                    screenTitle = stringResource(R.string.cdw_top_bar_title),
                    onCanChange = { cardAccessNumber = it },
                    onCancel = { onResumeCardWall() },
                    onNext = {
                        navController.navigate(CardWallNavigation.PersonalIdentificationNumber.path())
                    },
                    nextText = stringResource(R.string.unlock_egk_next)
                )
            }
        }

        composable(CardWallNavigation.PersonalIdentificationNumber.route) {
            NavigationAnimation(mode = navigationMode) {
                PersonalIdentificationNumberScreen(
                    navMode = navigationMode,
                    secret = personalIdentificationNumber,
                    onPinChange = { personalIdentificationNumber = it },
                    onCancel = { onResumeCardWall() },
                    onBack = { onBack() },
                    onClickNoPinReceived = { navController.navigate(CardWallNavigation.OrderHealthCard.path()) }
                ) {
                    val deviceSupportsBiometric = isDeviceSupportsBiometric(biometricMode)
                    val deviceSupportsStrongbox = hasDeviceStrongBox(context)
                    if (deviceSupportsBiometric &&
                        deviceSupportsStrongbox
                    ) {
                        navController.navigate(CardWallNavigation.AuthenticationSelection.path())
                    } else {
                        selectedAuthenticationMethod = CardWallData.AuthenticationMethod.HealthCard
                        navController.navigate(CardWallNavigation.Authentication.path())
                    }
                }
            }
        }

        composable(CardWallNavigation.AuthenticationSelection.route) {
            NavigationAnimation(mode = navigationMode) {
                AuthenticationSelection(
                    selectedAuthMode = selectedAuthenticationMethod,
                    onSelectAuthMode = { selectedAuthenticationMethod = it },
                    onSelectAlternativeOption = {
                        navController.navigate(
                            CardWallNavigation.AlternativeOption.path()
                        )
                    },
                    onCancel = { onResumeCardWall() },
                    onBack = onBack
                ) {
                    navController.navigate(
                        CardWallNavigation.Authentication.path()
                    )
                }
            }
        }

        composable(CardWallNavigation.AlternativeOption.route) {
            NavigationAnimation(mode = navigationMode) {
                AlternativeOptionInfoScreen(
                    onCancel = {
                        selectedAuthenticationMethod = CardWallData.AuthenticationMethod.None
                        onBack()
                    },
                    onAccept = {
                        navController.navigate(
                            CardWallNavigation.Authentication.path(),
                            navOptions = navOptions {
                                popUpTo(CardWallNavigation.AuthenticationSelection.route)
                            }
                        )
                    }
                )
            }
        }

        composable(CardWallNavigation.Authentication.route) {
            NavigationAnimation(mode = navigationMode) {
                CardWallNfcInstructionScreen(
                    viewModel = viewModel,
                    profileId = state.activeProfileId,
                    authenticationMethod = selectedAuthenticationMethod,
                    cardAccessNumber = cardAccessNumber,
                    personalIdentificationNumber = personalIdentificationNumber,
                    onNext = {
                        onResumeCardWall()
                    },
                    onRetryCan = {
                        navController.navigate(CardWallNavigation.CardAccessNumber.path()) {
                            popUpTo(CardWallNavigation.CardAccessNumber.path()) { inclusive = true }
                        }
                    },
                    onRetryPin = {
                        navController.navigate(CardWallNavigation.PersonalIdentificationNumber.path()) {
                            popUpTo(CardWallNavigation.PersonalIdentificationNumber.path()) {
                                inclusive = true
                            }
                        }
                    },
                    onUnlockEgk = onUnlockEgk,
                    onClickTroubleshooting = {
                        navController.navigate(CardWallNavigation.TroubleshootingPageA.path())
                    },
                    onBack = onBack
                )
            }
        }

        composable(CardWallNavigation.ExternalAuthenticator.route) {
            NavigationAnimation(mode = navigationMode) {
                ExternalAuthenticatorListScreen(
                    profileId = state.activeProfileId,
                    onNext = onResumeCardWall,
                    onCancel = { onResumeCardWall() },
                    onBack = onBack
                )
            }
        }

        composable(CardWallNavigation.TroubleshootingPageA.route) {
            NavigationAnimation(mode = navigationMode) {
                CardWallTroubleshootingPageA(
                    profileId = state.activeProfileId,
                    viewModel = viewModel,
                    onFinal = onResumeCardWall,
                    onBack = onBack,
                    onNext = { navController.navigate(CardWallNavigation.TroubleshootingPageB.path()) },
                    authenticationMethod = selectedAuthenticationMethod,
                    cardAccessNumber = cardAccessNumber,
                    personalIdentificationNumber = personalIdentificationNumber,
                    onRetryCan = onRetryCan,
                    onRetryPin = onRetryPin,
                    onUnlockEgk = onUnlockEgk
                )
            }
        }

        composable(CardWallNavigation.TroubleshootingPageB.route) {
            NavigationAnimation(mode = navigationMode) {
                CardWallTroubleshootingPageB(
                    profileId = state.activeProfileId,
                    viewModel = viewModel,
                    onFinal = onResumeCardWall,
                    onBack = onBack,
                    onNext = { navController.navigate(CardWallNavigation.TroubleshootingPageC.path()) },
                    authenticationMethod = selectedAuthenticationMethod,
                    cardAccessNumber = cardAccessNumber,
                    personalIdentificationNumber = personalIdentificationNumber,
                    onRetryCan = onRetryCan,
                    onRetryPin = onRetryPin,
                    onUnlockEgk = onUnlockEgk
                )
            }
        }

        composable(CardWallNavigation.TroubleshootingPageC.route) {
            NavigationAnimation(mode = navigationMode) {
                CardWallTroubleshootingPageC(
                    profileId = state.activeProfileId,
                    viewModel = viewModel,
                    onFinal = onResumeCardWall,
                    onBack = onBack,
                    onNext = { navController.navigate(CardWallNavigation.TroubleshootingNoSuccessPage.path()) },
                    authenticationMethod = selectedAuthenticationMethod,
                    cardAccessNumber = cardAccessNumber,
                    personalIdentificationNumber = personalIdentificationNumber,
                    onRetryCan = onRetryCan,
                    onRetryPin = onRetryPin,
                    onUnlockEgk = onUnlockEgk
                )
            }
        }

        composable(CardWallNavigation.TroubleshootingNoSuccessPage.route) {
            NavigationAnimation(mode = navigationMode) {
                CardWallTroubleshootingNoSuccessPage(
                    onBack = onBack,
                    onNext = onResumeCardWall
                )
            }
        }

        composable(CardWallNavigation.UnlockEgk.route) {
            NavigationAnimation(mode = navigationMode) {
                UnlockEgKScreen(
                    changeSecret = false,
                    navController = mainNavController,
                    onClickLearnMore = { navController.navigate(CardWallNavigation.OrderHealthCard.path()) }
                )
            }
        }
    }
}

@Composable
fun PersonalIdentificationNumberScreen(
    navMode: NavigationMode,
    secret: String,
    onPinChange: (String) -> Unit,
    onCancel: () -> Unit,
    onClickNoPinReceived: () -> Unit,
    onBack: () -> Unit,
    next: (String) -> Unit
) {
    CardWallSecretScreen(
        navMode = navMode,
        secret = secret,
        secretRange = 6..8,
        onSecretChange = onPinChange,
        screenTitle = stringResource(R.string.cdw_top_bar_title),
        next = next,
        nextText = stringResource(R.string.cdw_forward),
        onCancel = onCancel,
        onBack = onBack,
        onClickNoPinReceived = onClickNoPinReceived
    )
}

@Suppress("ComplexMethod")
@Composable
private fun AuthenticationSelection(
    selectedAuthMode: CardWallData.AuthenticationMethod,
    onSelectAuthMode: (CardWallData.AuthenticationMethod) -> Unit,
    onSelectAlternativeOption: () -> Unit,
    onCancel: () -> Unit,
    onBack: () -> Unit,
    onNext: () -> Unit
) {
    val context = LocalContext.current
    val biometricMode by produceState(initialValue = BiometricManager.BIOMETRIC_STATUS_UNKNOWN) {
        value = deviceStrongBiometricStatus(context)
    }
    var showEnrollBiometricDialog by remember { mutableStateOf(false) }
    var showWillBeLoggedOfHint by remember { mutableStateOf(false) }

    val enrollBiometricIntent = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
        Intent(Settings.ACTION_BIOMETRIC_ENROLL)
    } else {
        Intent(Settings.ACTION_APPLICATION_SETTINGS)
    }

    val lazyListState = rememberLazyListState()
    CardHandlingScaffold(
        modifier = Modifier.testTag("cardWall/authenticationSelection"),
        title = stringResource(R.string.cdw_top_bar_title),
        nextEnabled = selectedAuthMode != CardWallData.AuthenticationMethod.None,
        onNext = onNext,
        onBack = onBack,
        listState = lazyListState,
        nextText = stringResource(R.string.cdw_next),
        actions = @Composable {
            TextButton(onClick = onCancel) {
                Text(stringResource(R.string.cancel))
            }
        }
    ) {
        LazyColumn(
            state = lazyListState
        ) {
            item {
                Text(
                    stringResource(R.string.cdw_selection_title),
                    style = AppTheme.typography.h5,
                    modifier = Modifier.padding(PaddingDefaults.Medium)
                )
                SpacerXXLarge()

                SelectableCard(
                    modifier = Modifier.testTag(TestTag.CardWall.StoreCredentials.Save),
                    selected = selectedAuthMode == CardWallData.AuthenticationMethod.Alternative,
                    startIcon = Icons.Rounded.Check,
                    text = stringResource(R.string.cdw_selection_save)
                ) {
                    showWillBeLoggedOfHint = false
                    if (biometricMode == BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED) {
                        showEnrollBiometricDialog = true
                    } else {
                        onSelectAuthMode(CardWallData.AuthenticationMethod.Alternative)
                        onSelectAlternativeOption()
                    }
                }

                SpacerMedium()

                SelectableCard(
                    modifier = Modifier
                        .testTag(TestTag.CardWall.StoreCredentials.DontSave),
                    selected = selectedAuthMode == CardWallData.AuthenticationMethod.HealthCard,
                    startIcon = Icons.Rounded.Close,
                    text = stringResource(R.string.cdw_selection_save_not)
                ) {
                    onSelectAuthMode(CardWallData.AuthenticationMethod.HealthCard)
                    showWillBeLoggedOfHint = true
                }
                SpacerXXLarge()
                if (showWillBeLoggedOfHint) {
                    SpacerMedium()
                    HintCard(
                        modifier = Modifier.padding(horizontal = PaddingDefaults.Medium),
                        image = {
                            HintSmallImage(painterResource(R.drawable.information), null, it)
                        },
                        title = { Text(stringResource(R.string.cdw_selection_hint_title)) },
                        body = { Text(stringResource(R.string.cdw_selection_hint_info_)) }
                    )
                }
            }
        }
    }

    if (showEnrollBiometricDialog) {
        CommonAlertDialog(
            icon = Icons.Rounded.Fingerprint,
            header = stringResource(R.string.enroll_biometric_dialog_header),
            info = stringResource(R.string.enroll_biometric_dialog_info),
            cancelText = stringResource(R.string.enroll_biometric_dialog_cancel),
            actionText = stringResource(R.string.enroll_biometric_dialog_settings),
            onCancel = { showEnrollBiometricDialog = false }
        ) {
            startActivity(context, enrollBiometricIntent, null)
        }
    }
}

@Composable
fun AlternativeOptionInfoScreen(onCancel: () -> Unit, onAccept: () -> Unit) {
    CardWallInfoScaffold(
        topColor = MaterialTheme.colors.background,
        onNext = onAccept,
        onCancel = onCancel
    ) {
        Column(modifier = Modifier.padding(PaddingDefaults.Medium)) {
            Text(
                stringResource(R.string.cdw_info_header),
                style = AppTheme.typography.h5
            )
            SpacerSmall()
            Text(
                stringResource(R.string.cdw_info_first),
                style = AppTheme.typography.body1
            )
            SpacerSmall()
            Text(
                stringResource(R.string.cdw_info_second),
                style = AppTheme.typography.body1
            )
            SpacerSmall()
            Text(
                stringResource(R.string.cdw_info_third),
                style = AppTheme.typography.body1
            )
        }
    }
}

@Composable
fun AlternativeInfoBottomBar(onNext: () -> Unit) {
    Surface(
        color = MaterialTheme.colors.surface,
        elevation = 4.dp
    ) {
        Column(
            Modifier
                .navigationBarsPadding()
                .fillMaxWidth()
        ) {
            PrimaryButton(
                onClick = onNext,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag(TestTag.CardWall.SecurityAcceptance.AcceptButton)
                    .padding(
                        horizontal = 76.dp,
                        vertical = PaddingDefaults.Small + PaddingDefaults.Tiny
                    )
            ) {
                Text(
                    stringResource(R.string.cdw_info_accept)
                )
            }
        }
    }
}

@Preview("SelectableCard")
@Composable
private fun PreviewSelectableCard() {
    AppTheme {
        SelectableCard(
            startIcon = Icons.Rounded.Check,
            text = "Info here",
            selected = true
        )
    }
}

@Composable
fun SelectableCard(
    modifier: Modifier = Modifier,
    selected: Boolean = false,
    startIcon: ImageVector,
    text: String,
    onCardSelected: () -> Unit = {}
) {
    val checkIcon = if (selected) {
        Icons.Rounded.CheckCircle
    } else {
        Icons.Rounded.RadioButtonUnchecked
    }

    val checkIconTint = if (selected) {
        AppTheme.colors.primary600
    } else {
        AppTheme.colors.neutral400
    }

    val cardBorderStroke = if (selected) {
        BorderStroke(2.dp, AppTheme.colors.primary600)
    } else {
        BorderStroke(1.dp, AppTheme.colors.neutral300)
    }

    Card(
        border = cardBorderStroke,
        backgroundColor = AppTheme.colors.neutral000,
        modifier = modifier
            .padding(horizontal = PaddingDefaults.Medium)
            .fillMaxWidth(),
        shape = RoundedCornerShape(8.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(
                    enabled = true,
                    onClick = onCardSelected
                )
                .padding(PaddingDefaults.Medium),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                startIcon,
                null,
                tint = AppTheme.colors.primary600
            )

            Text(
                text,
                style = AppTheme.typography.subtitle1,
                modifier = modifier
                    .weight(1f)
                    .padding(start = PaddingDefaults.Medium)
            )

            Icon(
                checkIcon,
                null,
                tint = checkIconTint
            )
        }
    }
}

sealed class ToggleAuth {
    data class ToggleByUser(val value: Boolean) : ToggleAuth()
    data class ToggleByHealthCard(val tag: Tag) : ToggleAuth()
}

@Composable
fun CardHandlingScaffold(
    modifier: Modifier = Modifier,
    title: String,
    onBack: (() -> Unit)? = null,
    onNext: (() -> Unit)?,
    nextEnabled: Boolean = true,
    nextText: String,
    backMode: NavigationBarMode = NavigationBarMode.Back,
    actions: @Composable RowScope.() -> Unit = {},
    listState: LazyListState,
    content: @Composable (PaddingValues) -> Unit
) {
    val activity = LocalActivity.current
    AnimatedElevationScaffold(
        topBarTitle = title,
        navigationMode = backMode,
        actions = actions,
        onBack = if (onBack == null) {
            { activity.onBackPressed() }
        } else {
            onBack
        },
        bottomBar = {
            if (onNext != null) {
                CardWallBottomBar(onNext = onNext, nextEnabled = nextEnabled, nextText = nextText)
            }
        },
        modifier = modifier.imePadding(),
        topBarColor = MaterialTheme.colors.surface,
        listState = listState,
        content = content
    )
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun CardWallBottomBar(
    onNext: () -> Unit,
    nextEnabled: Boolean,
    nextText: String = stringResource(R.string.cdw_next)
) {
    Surface(
        color = MaterialTheme.colors.surface,
        elevation = 4.dp
    ) {
        val isKeyboardVisible = WindowInsets.isImeVisible

        Column(
            Modifier
                .then(if (isKeyboardVisible) Modifier.navigationBarsPadding() else Modifier)
                .fillMaxWidth()
        ) {
            PrimaryButton(
                onClick = onNext,
                enabled = nextEnabled,
                modifier = Modifier
                    .testTag(TestTag.CardWall.ContinueButton)
                    .padding(
                        horizontal = PaddingDefaults.Medium,
                        vertical = PaddingDefaults.ShortMedium
                    )
                    .align(Alignment.End)
            ) {
                Text(
                    nextText
                )
            }
        }
    }
}

@Composable
fun CardWallIntroBottomBar(
    onClickAlternateAuthentication: () -> Unit,
    onNext: () -> Unit
) {
    Surface(
        color = MaterialTheme.colors.surface,
        elevation = 4.dp
    ) {
        Column(
            modifier = Modifier
                .navigationBarsPadding()
                .fillMaxWidth()
                .padding(PaddingDefaults.ShortMedium),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            PrimaryButtonLarge(
                onClick = onNext,
                modifier = Modifier
                    .testTag(TestTag.CardWall.ContinueButton)
            ) {
                Text(
                    stringResource(R.string.cdw_next)
                )
            }
            SpacerMedium()
            Text(
                annotatedStringResource(
                    R.string.cdw_intro_alternate_auth_info,
                    annotatedLinkString(
                        stringResource(R.string.cdw_intro_alternate_auth_info_link),
                        stringResource(R.string.cdw_intro_alternate_auth_info_link)
                    )
                ),
                style = AppTheme.typography.body2l.merge(TextStyle(textAlign = TextAlign.Center)),
                modifier = Modifier.clickable(
                    onClick = onClickAlternateAuthentication,
                    indication = null,
                    interactionSource = remember { MutableInteractionSource() }
                )
            )
        }
    }
}
