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

import androidx.biometric.BiometricManager
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.animateColor
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.repeatable
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.updateTransition
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.Divider
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.LocalContentColor
import androidx.compose.material.LocalTextStyle
import androidx.compose.material.MaterialTheme
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Code
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.Fingerprint
import androidx.compose.material.icons.outlined.Info
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.google.accompanist.insets.LocalWindowInsets
import com.google.accompanist.insets.rememberInsetsPaddingValues
import de.gematik.ti.erp.app.BuildConfig
import de.gematik.ti.erp.app.BuildKonfig
import de.gematik.ti.erp.app.R
import de.gematik.ti.erp.app.db.entities.SettingsAuthenticationMethod
import de.gematik.ti.erp.app.profiles.ui.Avatar
import de.gematik.ti.erp.app.profiles.ui.connectionText
import de.gematik.ti.erp.app.profiles.ui.connectionTextColor
import de.gematik.ti.erp.app.profiles.ui.profileColor
import de.gematik.ti.erp.app.profiles.usecase.model.ProfilesUseCaseData
import de.gematik.ti.erp.app.theme.AppTheme
import de.gematik.ti.erp.app.theme.PaddingDefaults
import de.gematik.ti.erp.app.userauthentication.ui.BiometricPrompt
import de.gematik.ti.erp.app.utils.compose.AcceptDialog
import de.gematik.ti.erp.app.utils.compose.AlertDialog
import de.gematik.ti.erp.app.utils.compose.CommonAlertDialog
import de.gematik.ti.erp.app.utils.compose.LabeledSwitch
import de.gematik.ti.erp.app.utils.compose.NavigationBarMode
import de.gematik.ti.erp.app.utils.compose.NavigationMode
import de.gematik.ti.erp.app.utils.compose.NavigationTopAppBar
import de.gematik.ti.erp.app.utils.compose.OutlinedDebugButton
import de.gematik.ti.erp.app.utils.compose.Spacer24
import de.gematik.ti.erp.app.utils.compose.Spacer4
import de.gematik.ti.erp.app.utils.compose.SpacerMedium
import de.gematik.ti.erp.app.utils.compose.SpacerSmall
import de.gematik.ti.erp.app.utils.compose.SpacerTiny
import de.gematik.ti.erp.app.utils.compose.createToastShort
import de.gematik.ti.erp.app.utils.compose.handleIntent
import de.gematik.ti.erp.app.utils.compose.navigationModeState
import de.gematik.ti.erp.app.utils.compose.providePhoneIntent
import de.gematik.ti.erp.app.utils.compose.provideWebIntent
import java.util.Locale
import de.gematik.ti.erp.app.utils.dateTimeShortText
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Composable
fun SettingsScreen(
    scrollTo: SettingsScrollTo,
    mainNavController: NavController,
    settingsViewModel: SettingsViewModel = hiltViewModel()
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
        settingsViewModel
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

    Scaffold(
        topBar = {
            NavigationTopAppBar(
                NavigationBarMode.Close,
                stringResource(R.string.settings_headline)
            ) { mainNavController.popBackStack() }
        }
    ) {
        val listState = rememberLazyListState()
        var showAllowScreenShotsAlert by remember { mutableStateOf(false) }

        LaunchedEffect(Unit) {
            delay(200)
            when (scrollTo) {
                // TODO: find another way to scoll to Item
                SettingsScrollTo.None -> {
                    /* noop */
                }
                SettingsScrollTo.Authentication -> listState.animateScrollToItem(5)
                SettingsScrollTo.DemoMode -> listState.animateScrollToItem(3)
                SettingsScrollTo.Profiles -> listState.animateScrollToItem(2)
            }
        }

        LazyColumn(
            modifier = Modifier.testTag("settings_screen"),
            contentPadding = rememberInsetsPaddingValues(
                insets = LocalWindowInsets.current.navigationBars,
                applyBottom = true
            ),
            state = listState
        ) {
            item {
                if (BuildKonfig.INTERNAL) {
                    DebugMenuSection(navController)
                    SettingsDivider()
                }
            }
            item {
                OrderHealthCardHint(
                    modifier = Modifier.fillMaxWidth(),
                    onClick = {
                        navController.navigate(SettingsNavigationScreens.OrderHealthCard.path())
                    }
                )
                SettingsDivider()
            }
            item {
                ProfileSection(state, settingsViewModel, navController)
                SettingsDivider()
            }
            item {
                DemoSection(
                    state.demoModeActive,
                    highlighted = scrollTo == SettingsScrollTo.DemoMode,
                ) {
                    if (it) {
                        settingsViewModel.onActivateDemoMode()
                    } else {
                        settingsViewModel.onDeactivateDemoMode()
                    }
                }
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
                        SettingsScreen.AuthenticationMode.Password -> navController.navigate("Password")
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
                    state.screenShotsAllowed
                ) {
                    settingsViewModel.onSwitchAllowScreenshots(it)
                    showAllowScreenShotsAlert = true
                }
                SettingsDivider()
            }

            item {
                ContactSection(onClickFeedback = { navController.navigate(SettingsNavigationScreens.FeedbackForm.path()) })
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
    navController: NavController,
) {
    val profiles = state.uiProfiles

    var showAddProfileDialog by remember { mutableStateOf(false) }
    val allowAddProfiles by produceState(initialValue = false) {
        viewModel.allowAddProfiles().collect {
            value = it
        }
    }

    Column {
        Text(
            text = stringResource(R.string.settings_profiles_headline),
            style = MaterialTheme.typography.h6,
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
                demoModeActive = state.demoModeActive,
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

    val context = LocalContext.current
    val demoToastText = stringResource(R.string.function_not_availlable_on_demo_mode)
    val addProfilesNotAllowedText = stringResource(R.string.settings_add_profile_not_allowed)

    AddProfile(onClick = {
        if (!state.demoModeActive && allowAddProfiles)
            showAddProfileDialog = true
        else {
            if (!allowAddProfiles) createToastShort(context, addProfilesNotAllowedText)
            else createToastShort(context, demoToastText)
        }
    })
    Spacer24()
}

@Composable
private fun ProfileCard(
    demoModeActive: Boolean,
    profile: ProfilesUseCaseData.Profile,
    onSwitchProfile: () -> Unit,
    onClickEdit: () -> Unit,
) {
    val colors = profileColor(profileColorNames = profile.color)
    val profileSsoToken = profile.ssoToken

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable {
                if (!demoModeActive) {
                    onSwitchProfile()
                }
            },
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            modifier = Modifier
                .weight(1f)
                .padding(PaddingDefaults.Medium)
        ) {
            Avatar(Modifier.size(36.dp), profile.name, colors, null, active = profile.active)

            SpacerSmall()

            Column {
                Text(
                    profile.name, style = MaterialTheme.typography.body1,
                )

                val lastAuthenticatedDateText =
                    remember(profile.lastAuthenticated) { profile.lastAuthenticated?.let { dateTimeShortText(it) } }
                val connectedText = connectionText(profileSsoToken, lastAuthenticatedDateText)
                val connectedColor = connectionTextColor(profileSsoToken)

                Text(
                    connectedText, style = AppTheme.typography.captionl,
                    color = connectedColor,
                )
            }
        }

        val context = LocalContext.current
        val demoToastText = stringResource(R.string.function_not_availlable_on_demo_mode)

        IconButton(
            onClick = {
                if (!demoModeActive) {
                    onClickEdit()
                } else {
                    createToastShort(context, demoToastText)
                }
            }
        ) {
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
        style = MaterialTheme.typography.body1,
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
                style = MaterialTheme.typography.subtitle1,
            )
        },
        properties = DialogProperties(dismissOnClickOutside = false),
        onDismissRequest = onDismissRequest,
        text = {
            Column {
                Text(
                    infoText,
                    style = MaterialTheme.typography.body2
                )
                Box(modifier = Modifier.padding(top = 12.dp)) {
                    OutlinedTextField(
                        value = textValue,
                        singleLine = true,
                        onValueChange = {
                            textValue = it.trimStart()
                            duplicated = state.containsProfileWithName(textValue)
                        },
                        keyboardOptions = KeyboardOptions(
                            autoCorrect = true,
                            keyboardType = KeyboardType.Text,
                            imeAction = ImeAction.Done
                        ),
                        keyboardActions = KeyboardActions {
                            if (textValue.isNotEmpty()) {
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
                        style = MaterialTheme.typography.caption,
                        modifier = Modifier.padding(start = PaddingDefaults.Medium)
                    )
                }
            }
        },
        buttons = {
            TextButton(onClick = { onDismissRequest() }) {
                Text(stringResource(R.string.cancel).uppercase(Locale.getDefault()))
            }
            TextButton(onClick = {
                if (!duplicated && textValue.isNotEmpty()) {
                    onEdit(textValue)
                }
            }) {
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
            modifier = Modifier.padding(PaddingDefaults.Medium),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(
                text = stringResource(R.string.settings_demo_headline),
                style = MaterialTheme.typography.h6,
                modifier = Modifier.testTag("stg_txt_header_demo_mode")
            )
            Text(
                text = stringResource(R.string.settings_demo_info),
                style = AppTheme.typography.body2l
            )
        }
        LabeledSwitch(
            checked = demoChecked,
            onCheckedChange = onDemoChange,
            modifier = Modifier.testTag("stg_btn_demo_mode")
        ) {
            Text(
                modifier = Modifier.weight(1.0f),
                text = stringResource(R.string.settings_demo_toggle),
                style = MaterialTheme.typography.body1
            )
        }
    }
}

@Composable
private fun AccessibilitySection(
    modifier: Modifier = Modifier,
    zoomChecked: Boolean,
    onZoomChange: (Boolean) -> Unit,
) {
    Column(modifier = modifier) {
        Column(
            modifier = Modifier.padding(PaddingDefaults.Medium),
            verticalArrangement = Arrangement.spacedBy(PaddingDefaults.Small),
        ) {
            Text(
                text = stringResource(R.string.settings_accessibility_headline),
                style = MaterialTheme.typography.h6
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

    var showBiometricPrompt by rememberSaveable { mutableStateOf(false) }

    if (showBiometricPrompt) {
        BiometricPrompt(
            authenticationMethod = SettingsAuthenticationMethod.DeviceSecurity,
            title = stringResource(R.string.auth_prompt_headline),
            description = "",
            negativeButton = stringResource(R.string.auth_prompt_cancel),
            onAuthenticated = {
                onClickProtectionMode(SettingsScreen.AuthenticationMode.DeviceSecurity)
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
                style = MaterialTheme.typography.h6
            )
            Text(
                text = stringResource(R.string.settings_appprotection_info),
                style = MaterialTheme.typography.body2
            )
        }

        AuthenticationModeCard(
            Icons.Outlined.Fingerprint,
            checked = authenticationMode == SettingsScreen.AuthenticationMode.DeviceSecurity,
            headline = stringResource(R.string.settings_appprotection_device_security_header),
            info = stringResource(R.string.settings_appprotection_device_security_info),
            deviceSecurity = true,
        ) {
            showBiometricPrompt = true
        }

        AuthenticationModeCard(
            Icons.Outlined.Security,
            checked = authenticationMode == SettingsScreen.AuthenticationMode.Password,
            headline = stringResource(R.string.settings_appprotection_mode_password_headline),
            info = stringResource(R.string.settings_appprotection_mode_password_info)
        ) {
            onClickProtectionMode(SettingsScreen.AuthenticationMode.Password)
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
                style = MaterialTheme.typography.body1,
            )
            Text(
                text = info,
                style = AppTheme.typography.body2l
            )
        }

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
            .testTag("stg_btn_debug_menu")
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
                style = MaterialTheme.typography.h6
            )
            Text(
                text = stringResource(R.string.settings_tracking_info),
                style = MaterialTheme.typography.body2
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
    onAllowScreenshotsChange: (Boolean) -> Unit,
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
            style = MaterialTheme.typography.h6,
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
        Icon(icon, null, tint = AppTheme.colors.primary500)
        Text(text, style = MaterialTheme.typography.body1)
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
                stringResource(R.string.about_buildhash, BuildKonfig.GIT_HASH)
            )
        }
    }
}

@Composable
private fun LogoutButton(onClick: () -> Unit) {
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
private fun ContactSection(onClickFeedback: () -> Unit) {
    val context = LocalContext.current
    val contactHeader = stringResource(R.string.settings_contact_headline)

    Column {
        val phoneNumber = stringResource(R.string.settings_contact_hotline_number)
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
            icon = Icons.Outlined.Mail,
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

@Composable
fun secureOptionEnabled(): Boolean {
    val context = LocalContext.current

    return produceState(false) {
        withContext(Dispatchers.Main) {
            val biometricManager = BiometricManager.from(context)
            value = secureOptionEnabled(biometricManager)
        }
    }.value
}

private fun secureOptionEnabled(biometricManager: BiometricManager): Boolean {

    when (biometricManager.canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_STRONG)) {
        BiometricManager.BIOMETRIC_SUCCESS -> return true
        BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED -> return false
    }
    when (biometricManager.canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_WEAK)) {
        BiometricManager.BIOMETRIC_SUCCESS -> return true
        BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED -> return false
    }
    when (biometricManager.canAuthenticate(BiometricManager.Authenticators.DEVICE_CREDENTIAL)) {
        BiometricManager.BIOMETRIC_SUCCESS -> return true
        BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED -> return false
    }
    return false
}
