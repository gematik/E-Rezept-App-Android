/*
 * Copyright (c) 2021 gematik GmbH
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

package de.gematik.ti.erp.app.settings.ui

import android.widget.Toast
import androidx.biometric.BiometricManager
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.animateColor
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.repeatable
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.updateTransition
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.AlertDialog
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.Divider
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.LocalContentColor
import androidx.compose.material.LocalTextStyle
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ContactMail
import androidx.compose.material.icons.outlined.Face
import androidx.compose.material.icons.outlined.Fingerprint
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.OpenInBrowser
import androidx.compose.material.icons.outlined.PersonOutline
import androidx.compose.material.icons.outlined.PrivacyTip
import androidx.compose.material.icons.outlined.Wysiwyg
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Camera
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.LockOpen
import androidx.compose.material.icons.rounded.ModelTraining
import androidx.compose.material.icons.rounded.MoreVert
import androidx.compose.material.icons.rounded.Phone
import androidx.compose.material.icons.rounded.PhoneAndroid
import androidx.compose.material.icons.rounded.RadioButtonUnchecked
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import de.gematik.ti.erp.app.BuildConfig
import de.gematik.ti.erp.app.LegalNoticeWithScaffold
import de.gematik.ti.erp.app.R
import de.gematik.ti.erp.app.core.AppModel
import de.gematik.ti.erp.app.terms.DataTermsScreen
import de.gematik.ti.erp.app.terms.TermsOfUseScreen
import de.gematik.ti.erp.app.theme.AppTheme
import de.gematik.ti.erp.app.theme.PaddingDefaults
import de.gematik.ti.erp.app.utils.compose.CommonAlertDialog
import de.gematik.ti.erp.app.utils.compose.LabeledSwitch
import de.gematik.ti.erp.app.utils.compose.NavigationAnimation
import de.gematik.ti.erp.app.utils.compose.NavigationBarMode
import de.gematik.ti.erp.app.utils.compose.NavigationTopAppBar
import de.gematik.ti.erp.app.utils.compose.Spacer4
import de.gematik.ti.erp.app.utils.compose.SpacerMedium
import de.gematik.ti.erp.app.utils.compose.SpacerSmall
import de.gematik.ti.erp.app.utils.compose.handleIntent
import de.gematik.ti.erp.app.utils.compose.navigationModeState
import de.gematik.ti.erp.app.utils.compose.providePhoneIntent
import de.gematik.ti.erp.app.utils.compose.provideWebIntent
import de.gematik.ti.erp.app.utils.compose.testId
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import java.util.Locale

private val framePadding = 16.dp

@Composable
fun SettingsScreen(
    scrollTo: SettingsScrollTo,
    settingsVM: SettingsViewModel
) {
    val navController = rememberNavController()

    val navigationMode by navController.navigationModeState(SettingsNavigationScreens.Settings.route)
    NavHost(
        navController,
        startDestination = SettingsNavigationScreens.Settings.route
    ) {
        composable(SettingsNavigationScreens.Settings.route) {
            NavigationAnimation(navigationMode) {
                SettingsScreenWithScaffold(
                    scrollTo,
                    navController,
                    settingsVM
                )
            }
        }
        composable(SettingsNavigationScreens.Terms.route) {
            NavigationAnimation(navigationMode) {
                TermsOfUseScreen(navController)
            }
        }
        composable(SettingsNavigationScreens.Imprint.route) {
            NavigationAnimation(navigationMode) {
                LegalNoticeWithScaffold(
                    navController
                )
            }
        }
        composable(SettingsNavigationScreens.DataProtection.route) {
            NavigationAnimation(navigationMode) {
                DataTermsScreen(
                    navController
                )
            }
        }
        composable(SettingsNavigationScreens.OpenSourceLicences.route) {
            NavigationAnimation(navigationMode) {
                Licences(
                    navController
                )
            }
        }
        composable(SettingsNavigationScreens.AllowAnalytics.route) {
            NavigationAnimation(navigationMode) {
                AllowAnalyticsScreen(
                    settingsVM,
                    navController
                )
            }
        }
        composable(SettingsNavigationScreens.FeedbackForm.route) {
            NavigationAnimation(navigationMode) {
                FeedbackForm(
                    navController
                )
            }
        }
    }
}

@Composable
private fun SettingsScreenWithScaffold(
    scrollTo: SettingsScrollTo,
    navController: NavController,
    settingsVM: SettingsViewModel
) {
    val frNavController = AppModel.frNavController

    Scaffold(
        topBar = {
            NavigationTopAppBar(
                NavigationBarMode.Close,
                stringResource(R.string.settings_headline)
            ) { frNavController.popBackStack() }
        }
    ) {
        val listState = rememberLazyListState()
        var showAllowScreenShotsAlert by remember { mutableStateOf(false) }

        LaunchedEffect(Unit) {
            delay(200)
            when (scrollTo) {
                SettingsScrollTo.None -> {
                    /* noop */
                }
                SettingsScrollTo.Authentication -> listState.animateScrollToItem(2)
                SettingsScrollTo.DemoMode -> listState.animateScrollToItem(1)
            }
        }

        LazyColumn(state = listState) {
            item {
                Column {
                    if (BuildConfig.DEBUG) {
                        DebugMenuSection(frNavController)
                        SettingsDivider()
                    }
                    // HealthCardSection(settingsVM.screenState.healthCardUsers)
                    // SettingsDivider()
                }
            }
            item {
                DemoSection(
                    settingsVM.screenState.demoModeActive,
                    highlighted = scrollTo == SettingsScrollTo.DemoMode,
                ) {
                    if (it) {
                        settingsVM.onActivateDemoMode()
                    } else {
                        settingsVM.onDeactivateDemoMode()
                    }
                }
                SettingsDivider()
            }
            item {
                AuthenticationSection(settingsVM.screenState.authenticationMode) {
                    settingsVM.onSelectAuthenticationMode(it)
                }
                SettingsDivider()
            }
            item {
                val context = LocalContext.current
                val disAllowToast = stringResource(R.string.settings_tracking_disallow_info)
                AnalyticsSection(
                    navController,
                    settingsVM.screenState.analyticsAllowed
                ) {
                    if (!it) {
                        settingsVM.onTrackingDisallowed()
                        Toast.makeText(context, disAllowToast, Toast.LENGTH_SHORT).show()
                    }
                }
                SettingsDivider()
            }

            item {
                AllowScreenShotsSection(
                    settingsVM.screenState.screenShotsAllowed
                ) {
                    settingsVM.onSwitchAllowScreenshots(it)
                    showAllowScreenShotsAlert = true
                }
                SettingsDivider()
            }
            item {
                ContactSection(onClickFeedback = { navController.navigate(SettingsNavigationScreens.FeedbackForm.route) })
                SettingsDivider()
            }
            item {
                LegalSection(navController)
            }
            item {
                DeleteButton {
                    settingsVM.logout()
                }
                SettingsDivider()
            }

            item {
                AboutSection(Modifier.padding(top = 76.dp))
            }
        }
        if (showAllowScreenShotsAlert) {
            RestartAlert { showAllowScreenShotsAlert = false }
        }
    }
}

@Composable
private fun SettingsDivider() =
    Divider(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 8.dp, bottom = 8.dp)
    )

@OptIn(ExperimentalAnimationApi::class)
@Composable
private fun HealthCardSection(
    users: List<SettingsScreen.HealthCardUser>
) {
    Column {
        Text(
            text = stringResource(R.string.settings_ehealthcard_headline),
            style = MaterialTheme.typography.h6,
            modifier = Modifier.padding(
                start = framePadding,
                end = framePadding,
                top = framePadding,
                bottom = framePadding / 2
            )
        )

        val userUnknown = stringResource(R.string.settings_user_unknown)
        users.forEach {
            val visibleState = remember { MutableTransitionState(false) }
            AnimatedVisibility(
                visibleState = visibleState.apply { targetState = true },
                enter = fadeIn() + slideInVertically(),
                exit = fadeOut(),
            ) {
                HealthCardUser(it.name ?: userUnknown) {}
            }
        }
        AddHealthCardUser {}
    }
}

@Composable
private fun HealthCardUser(
    name: String,
    onMenuClick: () -> Unit
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .padding(framePadding)
    ) {
        Icon(Icons.Outlined.PersonOutline, null, tint = AppTheme.colors.primary500)
        Text(name, style = MaterialTheme.typography.body1, modifier = Modifier.weight(1.0f))
        IconButton(onClick = onMenuClick) {
            Icon(Icons.Rounded.MoreVert, null, tint = AppTheme.colors.neutral400)
        }
    }
}

@Composable
private fun AddHealthCardUser(
    onClick: () -> Unit
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(framePadding)
    ) {
        Icon(Icons.Rounded.Add, null, tint = AppTheme.colors.primary500)
        Text(
            stringResource(R.string.settings_ehealthcard_add_ehealthcard),
            style = MaterialTheme.typography.body1,
            color = AppTheme.colors.primary600,
            modifier = Modifier.weight(1.0f)
        )
    }
}

@Composable
private fun DemoSection(
    demoChecked: Boolean,
    modifier: Modifier = Modifier,
    highlighted: Boolean,
    onDemoChange: (Boolean) -> Unit,
) {
    var toggle by remember { mutableStateOf(false) }
    val transition = updateTransition(targetState = toggle, label = "DemoSectionTransition")

    val color by transition.animateColor(
        transitionSpec = {
            repeatable(
                5,
                tween(1000),
                RepeatMode.Reverse
            )
        },
        label = "DemoSectionColorAnimation"
    ) {
        if (it) AppTheme.colors.yellow300
        else MaterialTheme.colors.background
    }

    LaunchedEffect(highlighted) {
        toggle = highlighted
    }

    Column(modifier = modifier.background(color)) {
        Column(
            modifier = Modifier.padding(framePadding),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(
                text = stringResource(R.string.settings_demo_headline),
                style = MaterialTheme.typography.h6,
                modifier = Modifier.testId("stg_txt_header_demo_mode")
            )
            Text(
                text = stringResource(R.string.settings_demo_info),
                style = AppTheme.typography.body2l
            )
        }
        LabeledSwitch(
            checked = demoChecked,
            onCheckedChange = onDemoChange,
            modifier = Modifier.testId("stg_btn_demo_mode")
        ) {
            Text(
                modifier = Modifier.weight(1.0f),
                text = stringResource(R.string.settings_demo_toggle),
                style = MaterialTheme.typography.body1
            )
        }
    }
}

@Preview
@Composable
private fun DemoSectionPreview() {
    AppTheme {
        DemoSection(true, Modifier, false) {}
    }
}

@Composable
private fun AuthenticationSection(
    authenticationMode: SettingsScreen.AuthenticationMode,
    modifier: Modifier = Modifier,
    onClickProtectionMode: (SettingsScreen.AuthenticationMode) -> Unit
) {

    Column(modifier = modifier) {
        Column(
            modifier = Modifier.padding(framePadding),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = stringResource(R.string.settings_appprotection_headline),
                style = MaterialTheme.typography.h6
            )
            Text(
                text = stringResource(R.string.settings_appprotection_info),
                style = MaterialTheme.typography.body2
            )
        }

        // Spacer8()
        // CurrentLevelOfAppProtectionCard(authenticationMode)
        // Spacer8()

        //
        // biometric modes - exclusive
        //

        val context = LocalContext.current
        var biometricMode by remember { mutableStateOf(0) }
        var deviceCredentialsActive by remember { mutableStateOf(false) }

        LaunchedEffect(context) {
            withContext(Dispatchers.Main) {
                val biometricManager = BiometricManager.from(context)

                biometricMode =
                    when (BiometricManager.BIOMETRIC_SUCCESS) {
                        biometricManager.canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_STRONG) ->
                            BiometricManager.Authenticators.BIOMETRIC_STRONG
                        biometricManager.canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_WEAK) ->
                            BiometricManager.Authenticators.BIOMETRIC_WEAK
                        else -> 0
                    }

                deviceCredentialsActive = when (biometricManager.canAuthenticate(BiometricManager.Authenticators.DEVICE_CREDENTIAL)) {
                    BiometricManager.BIOMETRIC_SUCCESS -> true else -> false
                }
            }
        }

        when (biometricMode) {
            BiometricManager.Authenticators.BIOMETRIC_STRONG ->
                AuthenticationModeCard(
                    Icons.Outlined.Fingerprint,
                    checked = authenticationMode == SettingsScreen.AuthenticationMode.Biometrics,
                    headline = stringResource(R.string.settings_appprotection_mode_biometric_strong_headline),
                    info = stringResource(R.string.settings_appprotection_mode_biometric_strong_info),
                    biometric = true,
                ) {
                    onClickProtectionMode(SettingsScreen.AuthenticationMode.Biometrics)
                }
            BiometricManager.Authenticators.BIOMETRIC_WEAK ->
                AuthenticationModeCard(
                    Icons.Outlined.Face,
                    checked = authenticationMode == SettingsScreen.AuthenticationMode.Biometrics,
                    headline = stringResource(R.string.settings_appprotection_mode_biometric_strong_headline),
                    info = stringResource(R.string.settings_appprotection_mode_biometric_strong_info),
                    biometric = true,
                ) {
                    onClickProtectionMode(SettingsScreen.AuthenticationMode.Biometrics)
                }
        }

        if (deviceCredentialsActive) {
            AuthenticationModeCard(
                Icons.Outlined.Face,
                checked = authenticationMode == SettingsScreen.AuthenticationMode.DeviceCredentials,
                headline = stringResource(R.string.settings_appprotection_mode_biometric_credentials_headline),
                info = stringResource(R.string.settings_appprotection_mode_biometric_credentials_info),
                biometric = true,
            ) {
                onClickProtectionMode(SettingsScreen.AuthenticationMode.DeviceCredentials)
            }
        }

        //
        // password
        //

//        AuthenticationModeCard(
//            Icons.Rounded.Security,
//            checked = authenticationMode == SettingsScreen.AuthenticationMode.Password,
//            headline = stringResource(R.string.settings_appprotection_mode_password_headline),
//            info = stringResource(R.string.settings_appprotection_mode_password_info),
//        ) {
//            onClickProtectionMode(SettingsScreen.AuthenticationMode.Password)
//        }

        //
        // no additional protection
        //

        AuthenticationModeCard(
            Icons.Rounded.LockOpen,
            checked = authenticationMode == SettingsScreen.AuthenticationMode.None,
            headline = stringResource(R.string.settings_appprotection_mode_none_headline),
            info = stringResource(R.string.settings_appprotection_mode_none_info),
        ) {
            onClickProtectionMode(SettingsScreen.AuthenticationMode.None)
        }
    }
}

// @Composable
// private fun CurrentLevelOfAppProtectionCard(
//    mode: SettingsScreen.AuthenticationMode
// ) {
//    val locked = Icons.Rounded.Lock
//    val unlocked = Icons.Rounded.LockOpen
//
//    Card(
//        modifier = Modifier.padding(start = framePadding, end = framePadding),
//        shape = RoundedCornerShape(8.dp),
//        border = BorderStroke(0.5.dp, AppTheme.colors.neutral300)
//    ) {
//        Row(modifier = Modifier.padding(16.dp)) {
//            Column(modifier = Modifier.weight(1.0f)) {
//                Text(
//                    text = stringResource(R.string.settings_appprotection_levelcard_headline),
//                    style = MaterialTheme.typography.subtitle1,
//                )
//                Text(
//                    text = stringResource(R.string.settings_appprotection_levelcard_info),
//                    style = AppTheme.typography.body2l
//                )
//            }
//            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
//                Icon(Icons.Rounded.Lock, null, modifier = Modifier.size(24.dp))
//            }
//        }
//    }
// }

@OptIn(ExperimentalMaterialApi::class)
@Composable
private fun AuthenticationModeCard(
    icon: ImageVector,
    checked: Boolean,
    headline: String,
    info: String,
    biometric: Boolean = false,
    onClick: () -> Unit
) {

    var showAllowBiometric by remember { mutableStateOf(false) }

    if (biometric && showAllowBiometric) {
        CommonAlertDialog(
            header = stringResource(R.string.settings_biometric_dialog_title),
            info = stringResource(R.string.settings_biometric_dialog_text),
            actionText = stringResource(R.string.settings_tracking_allow),
            cancelText = stringResource(R.string.cancel),
            onCancel = { showAllowBiometric = false },
            onClickAction = {
                onClick()
                showAllowBiometric = false
            }
        )
    }

    val alpha = remember { Animatable(0.0f) }

    LaunchedEffect(checked) {
        if (checked) {
            alpha.animateTo(1.0f)
        } else {
            alpha.animateTo(0.0f)
        }
    }

    Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier
            .clickable(
                onClick = {
                    if (biometric) {
                        showAllowBiometric = true
                    } else {
                        onClick()
                    }
                }
            )
            .padding(framePadding)
    ) {
        Icon(icon, null, tint = AppTheme.colors.primary500)
        Column(modifier = Modifier.weight(1.0f)) {
            Text(
                text = headline,
                style = MaterialTheme.typography.body1,
            )
            Text(
                text = info,
                style = AppTheme.typography.body2l
            )
        }
        // if (checked) {

        // /} else {
        Box(modifier = Modifier.align(Alignment.CenterVertically)) {
            Icon(
                Icons.Rounded.RadioButtonUnchecked, null,
                tint = AppTheme.colors.neutral400
            )
            Icon(
                Icons.Rounded.CheckCircle, null,
                tint = AppTheme.colors.primary600,
                modifier = Modifier.alpha(alpha.value)
            )
        }
        // }
    }
}

@Composable
private fun DebugMenuSection(frNavController: NavController) {
    Text(
        text = stringResource(id = R.string.debug_menu),
        style = MaterialTheme.typography.h6,
        modifier = Modifier
            .padding(
                start = framePadding,
                end = framePadding,
                bottom = framePadding / 2,
                top = framePadding
            )
            .clickable {
                frNavController.navigate(R.id.action_settingsFragment_to_debugFragment)
            }
            .testId("stg_btn_debug_menu")
    )
}

@Composable
private fun AnalyticsSection(
    navController: NavController,
    analyticsAllowed: Boolean,
    modifier: Modifier = Modifier,
    onCheckedChange: (Boolean) -> Unit
) {
    var showAllowAnalytics by remember { mutableStateOf(false) }

    Column {
        Column(
            modifier = modifier.padding(framePadding),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = stringResource(R.string.settings_tracking_headline),
                style = MaterialTheme.typography.h6
            )
            Text(
                text = stringResource(R.string.settings_tracking_info),
                style = MaterialTheme.typography.body2
            )
        }
        LabeledSwitch(
            checked = analyticsAllowed,
            onCheckedChange = {
                showAllowAnalytics = it
                if (!it) {
                    onCheckedChange(false)
                }
            },
            modifier = modifier
                .testId("settings/trackingToggle")
        ) {
            Row(
                modifier = modifier
                    .weight(1.0f)
            ) {
                Icon(Icons.Rounded.ModelTraining, null, tint = AppTheme.colors.primary500)
                Column(
                    modifier = modifier
                        .weight(1.0f)
                        .padding(start = 8.dp, end = 8.dp)
                ) {
                    Text(
                        text = stringResource(R.string.settings_tracking_toggle_text),
                        style = MaterialTheme.typography.body1,
                    )
                    Text(
                        text = stringResource(R.string.settings_tracking_description),
                        style = AppTheme.typography.body2l
                    )
                }
            }
        }

        if (showAllowAnalytics) {
            navController.navigate(SettingsNavigationScreens.AllowAnalytics.route)
        }
    }
}

@Composable
private fun AllowScreenShotsSection(
    allowScreenshots: Boolean,
    modifier: Modifier = Modifier,
    onAllowScreenshotsChange: (Boolean) -> Unit,
) {

    Column {
        LabeledSwitch(
            checked = !allowScreenshots,
            onCheckedChange = {
                onAllowScreenshotsChange(!it)
            },
            modifier = modifier
                .testId("settings/allowScreenshotsToggle")
        ) {
            Row(
                modifier = modifier
                    .weight(1.0f)
            ) {
                Icon(Icons.Rounded.Camera, null, tint = AppTheme.colors.primary500)
                Column(
                    modifier = modifier
                        .weight(1.0f)
                        .padding(start = 8.dp, end = 8.dp)
                ) {
                    Text(
                        text = stringResource(R.string.settings_screenshots_toggle_text),
                        style = MaterialTheme.typography.body1,
                    )
                    Text(
                        text = stringResource(R.string.settings_screenshots_description),
                        style = AppTheme.typography.body2l
                    )
                }
            }
        }
    }
}

@Composable
private fun RestartAlert(onDismissRequest: () -> Unit) {
    val title = stringResource(R.string.settings_screenshots_alert_headline)
    val message = stringResource(R.string.settings_screenshots_alert_info)
    val confirmText = stringResource(R.string.settings_screenshots_button_text)

    AlertDialog(
        title = {
            Text(text = title)
        },
        onDismissRequest = onDismissRequest,
        text = {
            Text(
                message,
                style = AppTheme.typography.body2l
            )
        },
        buttons = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp)
            ) {
                TextButton(modifier = Modifier.weight(1f), onClick = { onDismissRequest() }) {
                    Text(confirmText.uppercase(Locale.getDefault()))
                }
            }
        },
    )
}

@Composable
private fun LegalSection(navController: NavController) {
    Column {
        Text(
            text = stringResource(R.string.settings_legal_headline),
            style = MaterialTheme.typography.h6,
            modifier = Modifier.padding(
                start = framePadding,
                end = framePadding,
                bottom = framePadding / 2,
                top = framePadding
            )
        )
        LabelButton(
            Icons.Outlined.Info,
            stringResource(R.string.settings_legal_imprint),
            modifier = Modifier.testId("settings/imprint")
        ) {
            navController.navigate(SettingsNavigationScreens.Imprint.route)
        }
        LabelButton(
            Icons.Outlined.PrivacyTip,
            stringResource(R.string.settings_legal_dataprotection),
            modifier = Modifier.testId("settings/privacy")
        ) {
            navController.navigate(SettingsNavigationScreens.DataProtection.route)
        }
        LabelButton(
            Icons.Outlined.Wysiwyg,
            stringResource(R.string.settings_legal_tos),
            modifier = Modifier.testId("settings/tos")
        ) {
            navController.navigate(SettingsNavigationScreens.Terms.route)
        }
        LabelButton(
            Icons.Outlined.PrivacyTip,
            stringResource(R.string.settings_legal_licences),
            modifier = Modifier.testId("settings/licences")
        ) {
            navController.navigate(SettingsNavigationScreens.OpenSourceLicences.route)
        }
    }
}

@Composable
private fun LabelButton(
    icon: ImageVector,
    text: String,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(framePadding)
            .semantics(mergeDescendants = true) {}
    ) {
        Icon(icon, null, tint = AppTheme.colors.primary500)
        Text(text, style = MaterialTheme.typography.body1)
    }
}

@Composable
private fun AboutSection(modifier: Modifier) {
    Column(
        modifier = modifier
            .padding(framePadding)
            .fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        CompositionLocalProvider(
            LocalTextStyle provides MaterialTheme.typography.body2,
            LocalContentColor provides AppTheme.colors.neutral600
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Rounded.PhoneAndroid, null, modifier = Modifier.size(16.dp))
                Spacer4()
                Text(
                    stringResource(R.string.about_version, BuildConfig.VERSION_NAME)
                )
            }
            Spacer4()
            Text(
                stringResource(R.string.about_buildhash, BuildConfig.GIT_HASH)
            )
        }
    }
}

@Composable
private fun DeleteButton(onClick: () -> Unit) {
    var dialogVisible by remember { mutableStateOf(false) }
    if (dialogVisible) {

        CommonAlertDialog(
            header = stringResource(id = R.string.logout_detail_header),
            info = stringResource(R.string.logout_detail_message),
            actionText = stringResource(R.string.logout_delete_yes),
            cancelText = stringResource(R.string.logout_delete_no),
            onCancel = { dialogVisible = false },
            onClickAction = {
                onClick()
                dialogVisible = false
            }
        )
    }

    Button(
        onClick = { dialogVisible = true },
        modifier = Modifier
            .padding(
                start = 16.dp,
                end = 16.dp,
                top = 32.dp,
                bottom = 16.dp
            )
            .fillMaxWidth(),
        colors = ButtonDefaults.buttonColors(
            backgroundColor = AppTheme.colors.red600,
            contentColor = AppTheme.colors.neutral000
        )
    ) {
        Text(
            stringResource(R.string.logout).uppercase(Locale.getDefault()),
            modifier = Modifier.padding(
                start = 16.dp,
                end = 16.dp,
                top = 8.dp,
                bottom = 8.dp
            )
        )
    }
    Text(
        stringResource(R.string.logout_description),
        modifier = Modifier.padding(
            start = PaddingDefaults.Medium,
            end = PaddingDefaults.Medium,
            bottom = PaddingDefaults.Small
        ),
        style = AppTheme.typography.body2l,
        textAlign = TextAlign.Center
    )
}

@Composable
fun ContactSection(onClickFeedback: () -> Unit) {
    val context = LocalContext.current
    val contactHeader = stringResource(R.string.settings_contact_headline)

    Column {
        val phoneNumber = stringResource(R.string.settings_contact_hotline_number)
        val mailAddress = stringResource(R.string.settings_contact_mail_address)
        val feedbackAddress = stringResource(R.string.settings_contact_feedback_adress)
        SpacerMedium()
        Text(
            text = contactHeader,
            style = MaterialTheme.typography.h6,
            modifier = Modifier.padding(horizontal = PaddingDefaults.Medium)
        )
        SpacerSmall()
        LabelButton(
            icon = Icons.Rounded.Phone,
            text = stringResource(R.string.settings_contact_hotline),
            onClick = { context.handleIntent(providePhoneIntent(phoneNumber)) }
        )
        LabelButton(
            icon = Icons.Outlined.ContactMail,
            text = stringResource(R.string.settings_contact_feedback_form),
            onClick = { onClickFeedback() }
        )
        LabelButton(
            icon = Icons.Outlined.OpenInBrowser,
            text = stringResource(R.string.settings_contact_feedback),
            onClick = { context.handleIntent(provideWebIntent(feedbackAddress)) }
        )
    }
}
