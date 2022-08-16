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

package de.gematik.ti.erp.app.settings.ui

import android.content.Context
import android.os.Build
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.Animatable
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.Divider
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.LocalContentColor
import androidx.compose.material.LocalTextStyle
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Code
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.Fingerprint
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.LockOpen
import androidx.compose.material.icons.outlined.Mail
import androidx.compose.material.icons.outlined.OpenInBrowser
import androidx.compose.material.icons.outlined.PrivacyTip
import androidx.compose.material.icons.outlined.Security
import androidx.compose.material.icons.outlined.Source
import androidx.compose.material.icons.outlined.Wysiwyg
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Camera
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.ModelTraining
import androidx.compose.material.icons.rounded.Phone
import androidx.compose.material.icons.rounded.PhoneAndroid
import androidx.compose.material.icons.rounded.RadioButtonUnchecked
import androidx.compose.material.icons.rounded.ZoomIn
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import de.gematik.ti.erp.app.BuildConfig
import de.gematik.ti.erp.app.BuildKonfig
import de.gematik.ti.erp.app.R
import de.gematik.ti.erp.app.TestTag
import de.gematik.ti.erp.app.profiles.ui.Avatar
import de.gematik.ti.erp.app.profiles.ui.ProfileSettingsViewModel
import de.gematik.ti.erp.app.profiles.ui.connectionText
import de.gematik.ti.erp.app.profiles.ui.connectionTextColor
import de.gematik.ti.erp.app.profiles.usecase.model.ProfilesUseCaseData
import de.gematik.ti.erp.app.settings.model.SettingsData
import de.gematik.ti.erp.app.theme.AppTheme
import de.gematik.ti.erp.app.theme.PaddingDefaults
import de.gematik.ti.erp.app.userauthentication.ui.BiometricPrompt
import de.gematik.ti.erp.app.utils.compose.AcceptDialog
import de.gematik.ti.erp.app.utils.compose.AlertDialog
import de.gematik.ti.erp.app.utils.compose.AnimatedElevationScaffold
import de.gematik.ti.erp.app.utils.compose.CommonAlertDialog
import de.gematik.ti.erp.app.utils.compose.LabeledSwitch
import de.gematik.ti.erp.app.utils.compose.NavigationBarMode
import de.gematik.ti.erp.app.utils.compose.NavigationMode
import de.gematik.ti.erp.app.utils.compose.OutlinedDebugButton
import de.gematik.ti.erp.app.utils.compose.Spacer4
import de.gematik.ti.erp.app.utils.compose.SpacerLarge
import de.gematik.ti.erp.app.utils.compose.SpacerMedium
import de.gematik.ti.erp.app.utils.compose.SpacerSmall
import de.gematik.ti.erp.app.utils.compose.SpacerTiny
import de.gematik.ti.erp.app.utils.compose.createToastShort
import de.gematik.ti.erp.app.utils.compose.handleIntent
import de.gematik.ti.erp.app.utils.compose.navigationModeState
import de.gematik.ti.erp.app.utils.compose.provideEmailIntent
import de.gematik.ti.erp.app.utils.compose.providePhoneIntent
import de.gematik.ti.erp.app.utils.compose.provideWebIntent
import de.gematik.ti.erp.app.utils.dateTimeShortText
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.Locale

@Composable
fun SettingsScreen(
    scrollTo: SettingsScrollTo,
    mainNavController: NavController,
    settingsViewModel: SettingsViewModel,
    profileSettingsViewModel: ProfileSettingsViewModel
) {
    val settingsNavController = rememberNavController()

    val navigationMode by settingsNavController.navigationModeState(
        SettingsNavigationScreens.Settings.route,
        intercept = { previousRoute: String?, currentRoute: String? ->
            if (previousRoute == SettingsNavigationScreens.OrderHealthCard.route && currentRoute == SettingsNavigationScreens.Settings.route) {
                NavigationMode.Closed
            } else {
                null
            }
        }
    )

    SettingsNavGraph(
        settingsNavController,
        navigationMode,
        scrollTo,
        mainNavController,
        settingsViewModel,
        profileSettingsViewModel
    )
}

@Composable
fun SettingsScreenWithScaffold(
    scrollTo: SettingsScrollTo,
    mainNavController: NavController,
    navController: NavController,
    settingsViewModel: SettingsViewModel
) {
    val state by produceState(SettingsScreen.defaultState) {
        settingsViewModel.screenState().collect {
            value = it
        }
    }
    val listState = rememberLazyListState()
    AnimatedElevationScaffold(
        modifier = Modifier.testTag(TestTag.Settings.SettingsScreen),
        navigationMode = NavigationBarMode.Close,
        topBarTitle = stringResource(R.string.settings_headline),
        onBack = { mainNavController.popBackStack() },
        listState = listState
    ) {
        var showAllowScreenShotsAlert by remember { mutableStateOf(false) }

        LaunchedEffect(Unit) {
            delay(200)
            when (scrollTo) {
                // TODO: find another way to scoll to Item
                SettingsScrollTo.None -> {
                    /* noop */
                }
                SettingsScrollTo.Authentication -> listState.animateScrollToItem(4)
                SettingsScrollTo.Profiles -> listState.animateScrollToItem(1)
                else -> {}
            }
        }

        LazyColumn(
            modifier = Modifier.testTag("settings_screen"),
            contentPadding = WindowInsets.navigationBars.only(WindowInsetsSides.Bottom).asPaddingValues(),
            state = listState
        ) {
            item {
                if (BuildKonfig.INTERNAL) {
                    DebugMenuSection(navController)
                    SettingsDivider()
                }
            }
            item {
                ProfileSection(state, settingsViewModel, navController)
                SettingsDivider()
            }
            item {
                HealthCardSection(
                    onClickUnlockEgk = { changeSecret ->
                        navController.navigate(SettingsNavigationScreens.UnlockEgk.path(changeSecret = changeSecret))
                    },
                    onClickOrderHealthCard = {
                        navController.navigate(SettingsNavigationScreens.OrderHealthCard.path())
                    }
                )
                SettingsDivider()
            }
            item {
                AccessibilitySection(zoomChecked = state.zoomEnabled) {
                    when (it) {
                        true -> settingsViewModel.onEnableZoom()
                        false -> settingsViewModel.onDisableZoom()
                    }
                }
                SettingsDivider()
            }
            item {
                val coroutineScope = rememberCoroutineScope()
                AuthenticationSection(state.authenticationMode) {
                    when (it) {
                        is SettingsData.AuthenticationMode.Password -> navController.navigate("Password")
                        else -> coroutineScope.launch { settingsViewModel.onSelectDeviceSecurityAuthenticationMode() }
                    }
                }
                SettingsDivider()
            }
            item {
                val context = LocalContext.current
                val disAllowToast = stringResource(R.string.settings_tracking_disallow_info)
                AnalyticsSection(
                    state.analyticsAllowed
                ) {
                    if (!it) {
                        settingsViewModel.onTrackingDisallowed()
                        createToastShort(context, disAllowToast)
                    } else {
                        navController.navigate(SettingsNavigationScreens.AllowAnalytics.path())
                    }
                }
                SettingsDivider()
            }

            item {
                AllowScreenShotsSection(
                    state.screenshotsAllowed
                ) {
                    settingsViewModel.onSwitchAllowScreenshots(it)
                    showAllowScreenShotsAlert = true
                }
                SettingsDivider()
            }

            item {
                ContactSection()
                SettingsDivider()
            }
            item {
                LegalSection(navController)
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
private fun ProfileSection(
    state: SettingsScreen.State,
    viewModel: SettingsViewModel,
    navController: NavController
) {
    val profiles = state.profiles

    var showAddProfileDialog by remember { mutableStateOf(false) }

    Column {
        Text(
            text = stringResource(R.string.settings_profiles_headline),
            style = AppTheme.typography.h6,
            modifier = Modifier
                .padding(
                    start = PaddingDefaults.Medium,
                    end = PaddingDefaults.Medium,
                    top = PaddingDefaults.Medium,
                    bottom = PaddingDefaults.Medium / 2
                )
                .testTag("Profiles")
        )

        profiles.forEach { profile ->
            ProfileCard(
                profile = profile,
                onSwitchProfile = { viewModel.switchProfile(profile) },
                onClickEdit = { navController.navigate(SettingsNavigationScreens.EditProfile.path(profileId = profile.id)) }
            )
        }
    }

    if (showAddProfileDialog) {
        AddProfileDialog(
            state = state,
            onEdit = { viewModel.addProfile(it); showAddProfileDialog = false },
            onDismissRequest = { showAddProfileDialog = false }
        )
    }

    AddProfile(onClick = { showAddProfileDialog = true })
    SpacerLarge()
}

@Composable
private fun ProfileCard(
    profile: ProfilesUseCaseData.Profile,
    onSwitchProfile: () -> Unit,
    onClickEdit: () -> Unit
) {
    val profileSsoToken = profile.ssoTokenScope?.token

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable {
                onSwitchProfile()
            },
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            modifier = Modifier
                .weight(1f)
                .padding(PaddingDefaults.Medium)
        ) {
            Avatar(Modifier.size(36.dp), profile, null, active = profile.active)

            SpacerSmall()

            Column {
                Text(
                    profile.name,
                    style = AppTheme.typography.body1
                )

                val lastAuthenticatedDateText =
                    remember(profile.lastAuthenticated) { profile.lastAuthenticated?.let { dateTimeShortText(it) } }
                val connectedText = connectionText(profileSsoToken, lastAuthenticatedDateText)
                val connectedColor = connectionTextColor(profileSsoToken)

                Text(
                    connectedText,
                    style = AppTheme.typography.caption1l,
                    color = connectedColor
                )
            }
        }

        IconButton(onClick = onClickEdit) {
            Icon(Icons.Outlined.Edit, null, tint = AppTheme.colors.neutral400)
        }

        SpacerTiny()
    }
}

@Composable
private fun AddProfile(
    onClick: () -> Unit
) {
    TextButton(onClick = { onClick() }, contentPadding = PaddingValues(PaddingDefaults.Medium)) {
        Icon(Icons.Rounded.Add, null)
        SpacerSmall()
        Text(
            stringResource(R.string.settings_add_profile),
            style = AppTheme.typography.body1,
            modifier = Modifier.weight(1.0f)
        )
    }
}

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun AddProfileDialog(
    state: SettingsScreen.State,
    wantRemoveLastProfile: Boolean = false,
    onEdit: (text: String) -> Unit,
    onDismissRequest: () -> Unit
) {
    var textValue by remember { mutableStateOf("") }
    var duplicated by remember { mutableStateOf(false) }

    val title = if (wantRemoveLastProfile) {
        stringResource(R.string.profile_edit_name_for_default)
    } else {
        stringResource(R.string.profile_edit_name)
    }

    val infoText = if (wantRemoveLastProfile) {
        stringResource(R.string.profile_edit_name_for_default_info)
    } else {
        stringResource(R.string.profile_edit_name_info)
    }

    AlertDialog(
        title = {
            Text(
                title,
                style = AppTheme.typography.subtitle1
            )
        },
        properties = DialogProperties(dismissOnClickOutside = false),
        onDismissRequest = onDismissRequest,
        text = {
            Column {
                Text(
                    infoText,
                    style = AppTheme.typography.body2
                )
                Box(modifier = Modifier.padding(top = 12.dp)) {
                    OutlinedTextField(
                        value = textValue,
                        singleLine = true,
                        onValueChange = {
                            textValue = it.trimStart()
                            duplicated = state.containsProfileWithName(textValue) && !wantRemoveLastProfile
                        },
                        keyboardOptions = KeyboardOptions(
                            autoCorrect = true,
                            keyboardType = KeyboardType.Text,
                            imeAction = ImeAction.Done
                        ),
                        keyboardActions = KeyboardActions {
                            if (!duplicated && textValue.isNotEmpty()) {
                                onEdit(textValue)
                            }
                        },
                        placeholder = { Text(stringResource(R.string.profile_edit_name_place_holder)) },
                        isError = duplicated
                    )
                }
                if (duplicated) {
                    Text(
                        stringResource(R.string.edit_profile_duplicated_profile_name),
                        color = AppTheme.colors.red600,
                        style = AppTheme.typography.caption1,
                        modifier = Modifier.padding(start = PaddingDefaults.Medium)
                    )
                }
            }
        },
        buttons = {
            TextButton(onClick = { onDismissRequest() }) {
                Text(stringResource(R.string.cancel).uppercase(Locale.getDefault()))
            }
            TextButton(
                enabled = !duplicated && textValue.isNotEmpty(),
                onClick = {
                    onEdit(textValue)
                }
            ) {
                Text(stringResource(R.string.ok).uppercase(Locale.getDefault()))
            }
        }
    )

    val keyboardController = LocalSoftwareKeyboardController.current
    DisposableEffect(Unit) {
        onDispose {
            keyboardController?.hide()
        }
    }
}

@Composable
fun HealthCardSection(onClickUnlockEgk: (changeSecret: Boolean) -> Unit, onClickOrderHealthCard: () -> Unit) {
    Column {
        Text(
            text = stringResource(R.string.health_card_section_header),
            style = AppTheme.typography.h6,
            modifier = Modifier.padding(
                start = PaddingDefaults.Medium,
                end = PaddingDefaults.Medium,
                bottom = PaddingDefaults.Medium / 2,
                top = PaddingDefaults.Medium
            )
        )

        LabelButton(
            Icons.Outlined.LockOpen,
            stringResource(R.string.health_card_section_unlock_card_no_reset)
        ) {
            onClickUnlockEgk(false)
        }

        LabelButton(
            painterResource(R.drawable.ic_reset_pin),
            stringResource(R.string.health_card_section_unlock_card_reset_pin)
        ) {
            onClickUnlockEgk(true)
        }

        LabelButton(
            painterResource(R.drawable.ic_order_egk),
            stringResource(R.string.health_card_section_order_card)
        ) {
            onClickOrderHealthCard()
        }
    }
}

@Composable
private fun AccessibilitySection(
    modifier: Modifier = Modifier,
    zoomChecked: Boolean,
    onZoomChange: (Boolean) -> Unit
) {
    Column(modifier = modifier) {
        Column(
            modifier = Modifier.padding(PaddingDefaults.Medium),
            verticalArrangement = Arrangement.spacedBy(PaddingDefaults.Small)
        ) {
            Text(
                text = stringResource(R.string.settings_accessibility_headline),
                style = AppTheme.typography.h6
            )
        }
        LabeledSwitch(
            checked = zoomChecked,
            onCheckedChange = onZoomChange,
            icon = Icons.Rounded.ZoomIn,
            header = stringResource(R.string.settings_accessibility_zoom_toggle),
            description = stringResource(R.string.settings_accessibility_zoom_info)
        )
    }
}

@Composable
private fun AuthenticationSection(
    authenticationMode: SettingsData.AuthenticationMode,
    modifier: Modifier = Modifier,
    onClickProtectionMode: (SettingsData.AuthenticationMode) -> Unit
) {
    var showBiometricPrompt by rememberSaveable { mutableStateOf(false) }

    if (showBiometricPrompt) {
        BiometricPrompt(
            authenticationMethod = SettingsData.AuthenticationMode.DeviceSecurity,
            title = stringResource(R.string.auth_prompt_headline),
            description = "",
            negativeButton = stringResource(R.string.auth_prompt_cancel),
            onAuthenticated = {
                onClickProtectionMode(SettingsData.AuthenticationMode.DeviceSecurity)
                showBiometricPrompt = false
            },
            onCancel = {
                showBiometricPrompt = false
            },
            onAuthenticationError = {
                showBiometricPrompt = false
            },
            onAuthenticationSoftError = {
            }
        )
    }

    Column(modifier = modifier) {
        Column(
            modifier = Modifier.padding(PaddingDefaults.Medium),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = stringResource(R.string.settings_appprotection_headline),
                style = AppTheme.typography.h6
            )
            Text(
                text = stringResource(R.string.settings_appprotection_info),
                style = AppTheme.typography.body2
            )
        }

        AuthenticationModeCard(
            Icons.Outlined.Fingerprint,
            checked = authenticationMode == SettingsData.AuthenticationMode.DeviceSecurity,
            headline = stringResource(R.string.settings_appprotection_device_security_header),
            info = stringResource(R.string.settings_appprotection_device_security_info),
            deviceSecurity = true
        ) {
            showBiometricPrompt = true
        }

        AuthenticationModeCard(
            Icons.Outlined.Security,
            checked = authenticationMode is SettingsData.AuthenticationMode.Password,
            headline = stringResource(R.string.settings_appprotection_mode_password_headline),
            info = stringResource(R.string.settings_appprotection_mode_password_info)
        ) {
            // TODO; use enum
            onClickProtectionMode(SettingsData.AuthenticationMode.Password(""))
        }
    }
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
private fun AuthenticationModeCard(
    icon: ImageVector,
    checked: Boolean,
    headline: String,
    info: String,
    deviceSecurity: Boolean = false,
    enabled: Boolean = true,
    onClick: () -> Unit
) {
    var showAllowDeviceSecurity by remember { mutableStateOf(false) }

    if (deviceSecurity && showAllowDeviceSecurity && !checked) {
        CommonAlertDialog(
            header = stringResource(R.string.settings_biometric_dialog_title),
            info = stringResource(R.string.settings_biometric_dialog_text),
            actionText = stringResource(R.string.settings_device_security_allow),
            cancelText = stringResource(R.string.cancel),
            onCancel = { showAllowDeviceSecurity = false },
            onClickAction = {
                onClick()
                showAllowDeviceSecurity = false
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
                    if (deviceSecurity) {
                        showAllowDeviceSecurity = true
                    } else {
                        onClick()
                    }
                },
                enabled = enabled
            )
            .padding(PaddingDefaults.Medium)
    ) {
        Icon(icon, null, tint = AppTheme.colors.primary500)
        Column(modifier = Modifier.weight(1.0f)) {
            Text(
                text = headline,
                style = AppTheme.typography.body1
            )
            Text(
                text = info,
                style = AppTheme.typography.body2l
            )
        }

        Box(modifier = Modifier.align(Alignment.CenterVertically)) {
            Icon(
                Icons.Rounded.RadioButtonUnchecked,
                null,
                tint = AppTheme.colors.neutral400
            )
            Icon(
                Icons.Rounded.CheckCircle,
                null,
                tint = AppTheme.colors.primary600,
                modifier = Modifier.alpha(alpha.value)
            )
        }
    }
}

@Composable
private fun DebugMenuSection(navController: NavController) {
    OutlinedDebugButton(
        text = stringResource(id = R.string.debug_menu),
        onClick = { navController.navigate(SettingsNavigationScreens.Debug.path()) },
        modifier = Modifier
            .fillMaxWidth()
            .padding(
                start = PaddingDefaults.Medium,
                end = PaddingDefaults.Medium,
                bottom = PaddingDefaults.Medium / 2,
                top = PaddingDefaults.Medium
            )
            .testTag(TestTag.Settings.DebugMenuButton)
    )
}

@Composable
private fun AnalyticsSection(
    analyticsAllowed: Boolean,
    modifier: Modifier = Modifier,
    onCheckedChange: (Boolean) -> Unit
) {
    Column {
        Column(
            modifier = modifier.padding(PaddingDefaults.Medium),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = stringResource(R.string.settings_tracking_headline),
                style = AppTheme.typography.h6
            )
            Text(
                text = stringResource(R.string.settings_tracking_info),
                style = AppTheme.typography.body2
            )
        }
        LabeledSwitch(
            checked = analyticsAllowed,
            onCheckedChange = onCheckedChange,
            modifier = modifier
                .testTag("settings/trackingToggle"),
            icon = Icons.Rounded.ModelTraining,
            header = stringResource(R.string.settings_tracking_toggle_text),
            description = stringResource(R.string.settings_tracking_description)
        )
    }
}

@Composable
private fun AllowScreenShotsSection(
    allowScreenshots: Boolean,
    modifier: Modifier = Modifier,
    onAllowScreenshotsChange: (Boolean) -> Unit
) {
    LabeledSwitch(
        checked = !allowScreenshots,
        onCheckedChange = {
            onAllowScreenshotsChange(!it)
        },
        modifier = modifier
            .testTag("settings/allowScreenshotsToggle"),
        icon = Icons.Rounded.Camera,
        header = stringResource(R.string.settings_screenshots_toggle_text),
        description = stringResource(R.string.settings_screenshots_description)
    )
}

@Composable
private fun RestartAlert(onDismissRequest: () -> Unit) {
    val title = stringResource(R.string.settings_screenshots_alert_headline)
    val message = stringResource(R.string.settings_screenshots_alert_info)
    val confirmText = stringResource(R.string.settings_screenshots_button_text)

    AcceptDialog(
        header = title,
        onClickAccept = onDismissRequest,
        info = message,
        acceptText = confirmText
    )
}

@Composable
private fun LegalSection(navController: NavController) {
    Column {
        Text(
            text = stringResource(R.string.settings_legal_headline),
            style = AppTheme.typography.h6,
            modifier = Modifier.padding(
                start = PaddingDefaults.Medium,
                end = PaddingDefaults.Medium,
                bottom = PaddingDefaults.Medium / 2,
                top = PaddingDefaults.Medium
            )
        )
        LabelButton(
            Icons.Outlined.Info,
            stringResource(R.string.settings_legal_imprint),
            modifier = Modifier.testTag("settings/imprint")
        ) {
            navController.navigate(SettingsNavigationScreens.Imprint.route)
        }
        LabelButton(
            Icons.Outlined.PrivacyTip,
            stringResource(R.string.settings_legal_dataprotection),
            modifier = Modifier.testTag("settings/privacy")
        ) {
            navController.navigate(SettingsNavigationScreens.DataProtection.route)
        }
        LabelButton(
            Icons.Outlined.Wysiwyg,
            stringResource(R.string.settings_legal_tos),
            modifier = Modifier.testTag("settings/tos")
        ) {
            navController.navigate(SettingsNavigationScreens.Terms.route)
        }
        LabelButton(
            Icons.Outlined.Code,
            stringResource(R.string.settings_legal_licences),
            modifier = Modifier.testTag("settings/licences")
        ) {
            navController.navigate(SettingsNavigationScreens.OpenSourceLicences.route)
        }
        LabelButton(
            Icons.Outlined.Source,
            stringResource(R.string.settings_licence_pharmacy_search),
            modifier = Modifier.testTag("settings/additional_licences")
        ) {
            navController.navigate(SettingsNavigationScreens.AdditionalLicences.route)
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
            .padding(PaddingDefaults.Medium)
            .semantics(mergeDescendants = true) {}
    ) {
        Icon(icon, null, tint = AppTheme.colors.primary600)
        Text(text, style = AppTheme.typography.body1)
    }
}

@Composable
private fun LabelButton(
    icon: Painter,
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
            .padding(PaddingDefaults.Medium)
            .semantics(mergeDescendants = true) {}
    ) {
        Image(painter = icon, contentDescription = null)
        Text(text, style = AppTheme.typography.body1)
    }
}

@Composable
private fun AboutSection(modifier: Modifier) {
    Column(
        modifier = modifier
            .padding(PaddingDefaults.Medium)
            .fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        CompositionLocalProvider(
            LocalTextStyle provides AppTheme.typography.body2,
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
                stringResource(R.string.about_buildhash, BuildKonfig.GIT_HASH)
            )
        }
    }
}

@Composable
private fun ContactSection() {
    val context = LocalContext.current
    val contactHeader = stringResource(R.string.settings_contact_headline)

    Column {
        val phoneNumber = stringResource(R.string.settings_contact_hotline_number)
        val surveyAddress = stringResource(R.string.settings_contact_survey_address)
        val mailAddress = stringResource(R.string.settings_contact_mail_address)
        val subject = stringResource(R.string.settings_feedback_mail_subject)
        val body = buildFeedbackBodyWithDeviceInfo()

        SpacerMedium()
        Text(
            text = contactHeader,
            style = AppTheme.typography.h6,
            modifier = Modifier.padding(horizontal = PaddingDefaults.Medium)
        )
        SpacerSmall()

        LabelButton(
            icon = Icons.Outlined.Mail,
            text = stringResource(R.string.settings_contact_feedback_form),
            onClick = {
                openMailClient(context, mailAddress, body, subject)
            }
        )
        LabelButton(
            icon = Icons.Outlined.OpenInBrowser,
            text = stringResource(R.string.settings_contact_feedback),
            onClick = { context.handleIntent(provideWebIntent(surveyAddress)) }
        )
        LabelButton(
            icon = Icons.Rounded.Phone,
            text = stringResource(R.string.settings_contact_hotline),
            onClick = { context.handleIntent(providePhoneIntent(phoneNumber)) }
        )
        Text(
            text = stringResource(R.string.settings_contact_technical_support_description),
            style = AppTheme.typography.body2l,
            modifier = Modifier.padding(horizontal = PaddingDefaults.Medium)
        )
    }
}

fun openMailClient(
    context: Context,
    address: String,
    body: String,
    subject: String
) = context.handleIntent(
    provideEmailIntent(
        address = address,
        body = body,
        subject = subject
    )
)

@Suppress("MaxLineLength")
@Composable
fun buildFeedbackBodyWithDeviceInfo(
    title: String = stringResource(R.string.settings_feedback_mail_title),
    userHint: String = stringResource(R.string.seetings_feedback_form_additional_data_info),
    darkMode: Boolean = isSystemInDarkTheme()
): String = """$title
      |
      |
      |
      |$userHint
      |
      |Systeminformationen
      |
      |Betriebssystem: Android ${Build.VERSION.RELEASE} (SDK ${Build.VERSION.SDK_INT}) (PATCH ${Build.VERSION.SECURITY_PATCH})
      |Modell: ${Build.MANUFACTURER} ${Build.MODEL} (${Build.PRODUCT})
      |App Version: ${BuildConfig.VERSION_NAME} (${BuildKonfig.GIT_HASH})
      |DarkMode: ${if (darkMode) "an" else "aus"}
      |Sprache: ${Locale.getDefault().displayName}
      |
""".trimMargin()
