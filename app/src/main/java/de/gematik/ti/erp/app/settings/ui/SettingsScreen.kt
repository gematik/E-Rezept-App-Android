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
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.AlertDialog
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.Divider
import androidx.compose.material.DropdownMenu
import androidx.compose.material.DropdownMenuItem
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
import androidx.compose.material.icons.outlined.ContentCopy
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.Fingerprint
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Mail
import androidx.compose.material.icons.outlined.OpenInBrowser
import androidx.compose.material.icons.outlined.PersonOutline
import androidx.compose.material.icons.outlined.PrivacyTip
import androidx.compose.material.icons.outlined.Security
import androidx.compose.material.icons.outlined.VpnKey
import androidx.compose.material.icons.outlined.Wysiwyg
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Camera
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.ModelTraining
import androidx.compose.material.icons.rounded.MoreVert
import androidx.compose.material.icons.rounded.Phone
import androidx.compose.material.icons.rounded.PhoneAndroid
import androidx.compose.material.icons.rounded.RadioButtonUnchecked
import androidx.compose.material.icons.rounded.ZoomIn
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import de.gematik.ti.erp.app.BuildConfig
import de.gematik.ti.erp.app.LegalNoticeWithScaffold
import de.gematik.ti.erp.app.R
import de.gematik.ti.erp.app.db.entities.SettingsAuthenticationMethod
import de.gematik.ti.erp.app.debug.ui.DebugScreenWrapper
import de.gematik.ti.erp.app.orderhealthcard.ui.HealthCardContactOrderScreen
import de.gematik.ti.erp.app.settings.usecase.SettingsUseCase
import de.gematik.ti.erp.app.terms.DataProtectionScreen
import de.gematik.ti.erp.app.terms.TermsOfUseScreen
import de.gematik.ti.erp.app.theme.AppTheme
import de.gematik.ti.erp.app.theme.PaddingDefaults
import de.gematik.ti.erp.app.userauthentication.ui.BiometricPrompt
import de.gematik.ti.erp.app.utils.compose.CommonAlertDialog
import de.gematik.ti.erp.app.utils.compose.HintCard
import de.gematik.ti.erp.app.utils.compose.HintCardDefaults
import de.gematik.ti.erp.app.utils.compose.HintCloseButton
import de.gematik.ti.erp.app.utils.compose.HintSmallImage
import de.gematik.ti.erp.app.utils.compose.LabeledSwitch
import de.gematik.ti.erp.app.utils.compose.NavigationAnimation
import de.gematik.ti.erp.app.utils.compose.NavigationBarMode
import de.gematik.ti.erp.app.utils.compose.NavigationMode
import de.gematik.ti.erp.app.utils.compose.NavigationTopAppBar
import de.gematik.ti.erp.app.utils.compose.Spacer24
import de.gematik.ti.erp.app.utils.compose.Spacer32
import de.gematik.ti.erp.app.utils.compose.Spacer4
import de.gematik.ti.erp.app.utils.compose.SpacerMedium
import de.gematik.ti.erp.app.utils.compose.SpacerSmall
import de.gematik.ti.erp.app.utils.compose.handleIntent
import de.gematik.ti.erp.app.utils.compose.navigationModeState
import de.gematik.ti.erp.app.utils.compose.providePhoneIntent
import de.gematik.ti.erp.app.utils.compose.provideWebIntent
import de.gematik.ti.erp.app.utils.compose.testId
import java.util.Locale
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.withContext

enum class SettingsScrollTo {
    None,
    Authentication,
    DemoMode
}

@Composable
fun SettingsScreen(
    scrollTo: SettingsScrollTo,
    mainNavController: NavController,
    settingsViewModel: SettingsViewModel = hiltViewModel()
) {
    val navController = rememberNavController()

    val navigationMode by navController.navigationModeState(
        SettingsNavigationScreens.Settings.route,
        intercept = { previousRoute: String?, currentRoute: String? ->
            if (previousRoute == SettingsNavigationScreens.OrderHealthCard.route && currentRoute == SettingsNavigationScreens.Settings.route) {
                NavigationMode.Closed
            } else {
                null
            }
        }
    )
    NavHost(
        navController,
        startDestination = SettingsNavigationScreens.Settings.path()
    ) {
        composable(SettingsNavigationScreens.Settings.route) {
            NavigationAnimation(mode = navigationMode) {
                SettingsScreenWithScaffold(
                    scrollTo,
                    mainNavController = mainNavController,
                    navController = navController,
                    settingsViewModel = settingsViewModel
                )
            }
        }
        composable(SettingsNavigationScreens.Debug.route) {
            NavigationAnimation(mode = navigationMode) {
                DebugScreenWrapper(navController)
            }
        }
        composable(SettingsNavigationScreens.Terms.route) {
            NavigationAnimation(mode = navigationMode) {
                TermsOfUseScreen(navController)
            }
        }
        composable(SettingsNavigationScreens.Imprint.route) {
            NavigationAnimation(mode = navigationMode) {
                LegalNoticeWithScaffold(
                    navController
                )
            }
        }
        composable(SettingsNavigationScreens.DataProtection.route) {
            NavigationAnimation(mode = navigationMode) {
                DataProtectionScreen(
                    navController
                )
            }
        }
        composable(SettingsNavigationScreens.OpenSourceLicences.route) {
            NavigationAnimation(mode = navigationMode) {
                Licences(
                    navController
                )
            }
        }
        composable(SettingsNavigationScreens.AllowAnalytics.route) {
            NavigationAnimation(mode = navigationMode) {
                AllowAnalyticsScreen {
                    if (it) {
                        settingsViewModel.onTrackingAllowed()
                    } else {
                        settingsViewModel.onTrackingDisallowed()
                    }
                    navController.popBackStack()
                }
            }
        }
        composable(SettingsNavigationScreens.FeedbackForm.route) {
            NavigationAnimation(mode = navigationMode) {
                FeedbackForm(
                    navController
                )
            }
        }
        composable(SettingsNavigationScreens.Password.route) {
            NavigationAnimation(mode = navigationMode) {
                SecureAppWithPassword(
                    navController,
                    settingsViewModel
                )
            }
        }
        composable(SettingsNavigationScreens.Token.route) {
            NavigationAnimation(mode = navigationMode) {
                TokenScreen(
                    navController,
                    settingsViewModel
                )
            }
        }
        composable(SettingsNavigationScreens.OrderHealthCard.route) {
            HealthCardContactOrderScreen(onBack = { navController.popBackStack() })
        }
    }
}

@Composable
private fun SettingsScreenWithScaffold(
    scrollTo: SettingsScrollTo,
    mainNavController: NavController,
    navController: NavController,
    settingsViewModel: SettingsViewModel
) {
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
                SettingsScrollTo.None -> {
                    /* noop */
                }
                SettingsScrollTo.Authentication -> listState.animateScrollToItem(2)
                SettingsScrollTo.DemoMode -> listState.animateScrollToItem(1)
            }
        }

        var tokenAvailable by remember { (mutableStateOf(false)) }

        LaunchedEffect(Unit) {
            settingsViewModel.getToken().apply {
                tokenAvailable = this.accessToken != null || this.singleSignOnToken != null
            }
        }

        val multiProfile by produceState(initialValue = false) {
            settingsViewModel.profilesOn().collect {
                value = it
            }
        }

        LazyColumn(modifier = Modifier.testTag("settings_screen"), state = listState) {
            item {
                Column {
                    if (BuildConfig.DEBUG) {
                        DebugMenuSection(navController)
                        SettingsDivider()
                    }
                    OrderHealthCardHint(
                        modifier = Modifier.fillMaxWidth(),
                        onClick = {
                            navController.navigate(SettingsNavigationScreens.OrderHealthCard.path())
                        }
                    )
                    SettingsDivider()
                    if (multiProfile) {
                        ProfileSection(settingsViewModel)
                        SettingsDivider()
                    }
                }
            }
            item {
                DemoSection(
                    settingsViewModel.screenState.demoModeActive,
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
                AccessibilitySection(zoomChecked = settingsViewModel.screenState.zoomEnabled) {
                    when (it) {
                        true -> settingsViewModel.onEnableZoom()
                        false -> settingsViewModel.onDisableZoom()
                    }
                }
                SettingsDivider()
            }
            item {
                AuthenticationSection(settingsViewModel.screenState.authenticationMode) {
                    when (it) {
                        SettingsScreen.AuthenticationMode.Password -> navController.navigate("Password")
                        else -> settingsViewModel.onSelectDeviceSecurityAuthenticationMode()
                    }
                }
                SettingsDivider()
            }
            item {
                val context = LocalContext.current
                val disAllowToast = stringResource(R.string.settings_tracking_disallow_info)
                AnalyticsSection(
                    settingsViewModel.screenState.analyticsAllowed
                ) {
                    if (!it) {
                        settingsViewModel.onTrackingDisallowed()
                        Toast.makeText(context, disAllowToast, Toast.LENGTH_SHORT).show()
                    } else {
                        navController.navigate(SettingsNavigationScreens.AllowAnalytics.path())
                    }
                }
                SettingsDivider()
            }

            item {
                AllowScreenShotsSection(
                    settingsViewModel.screenState.screenShotsAllowed
                ) {
                    settingsViewModel.onSwitchAllowScreenshots(it)
                    showAllowScreenShotsAlert = true
                }
                SettingsDivider()
            }
            item {
                TokenSection(tokenAvailable) {
                    navController.navigate(SettingsNavigationScreens.Token.path())
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
                DeleteButton {
                    settingsViewModel.logout()
                    tokenAvailable = false
                }
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
    viewModel: SettingsViewModel
) {

    val profiles = viewModel.screenState.uiProfiles
    var showAddProfilesHint by remember { mutableStateOf(profiles.size < 2 && !viewModel.addProfilesHintShown) }

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

        var showEditProfileNameDialog by remember {
            mutableStateOf(false)
        }
        var selectedProfile by remember { mutableStateOf(SettingsScreen.UIProfile(-1, "", false)) }

        profiles.forEach {
            val visibleState = remember { MutableTransitionState(false) }
            AnimatedVisibility(
                visibleState = visibleState.apply { targetState = true },
                enter = fadeIn() + slideInVertically(),
                exit = fadeOut(),
            ) {
                Profile(
                    it.name,
                    it.active,
                    { viewModel.activateProfile(it.name) }
                ) {
                    ProfileDropDownItem(
                        Icons.Outlined.Edit,
                        stringResource(R.string.settings_profile_edit_name)
                    ) {
                        selectedProfile = it
                        showEditProfileNameDialog = true
                    }

                    ProfileDropDownItem(
                        Icons.Outlined.Delete,
                        stringResource(R.string.settings_profile_delete)
                    ) {

                        viewModel.removeProfile(it.name)
                    }
                }
            }
        }

        if (showEditProfileNameDialog) {
            EditProfileNameDialog(
                selectedProfile,
                onEdit = {
                    viewModel.updateProfileName(
                        selectedProfile,
                        it
                    ); showEditProfileNameDialog = false
                },
                onDismissRequest = { showEditProfileNameDialog = false }
            )
        }
    }

    var showAddProfileDialog by remember {
        mutableStateOf(false)
    }

    if (showAddProfileDialog) {
        AddProfileDialog(
            onEdit = { viewModel.addProfile(it); showAddProfileDialog = false },
            onDismissRequest = { showAddProfileDialog = false }
        )
    }

    Spacer32()
    AddProfile {
        showAddProfileDialog = true
    }

    if (showAddProfilesHint) {
        Spacer32()
        HintCard(
            modifier = Modifier.padding(horizontal = PaddingDefaults.Medium),
            properties = HintCardDefaults.properties(
                backgroundColor = AppTheme.colors.neutral000,
                border = BorderStroke(0.5.dp, AppTheme.colors.neutral300),
                elevation = 0.dp
            ),
            image = {
                HintSmallImage(
                    painterResource(R.drawable.ic_info),
                    innerPadding = it
                )
            },
            title = { Text(stringResource(R.string.settings_add_profiles_hint_title)) },
            body = { Text(stringResource(R.string.settings_add_profiles_hint_info)) },
            close = {
                HintCloseButton(it) {
                    viewModel.onAddProfilesHintShown()
                    showAddProfilesHint = false
                }
            }
        )
    }
    Spacer24()
}

@Composable
private fun Profile(
    name: String,
    selected: Boolean,
    onClick: () -> Unit,
    content: @Composable () -> Unit,
) {

    val color = if (selected) {
        AppTheme.colors.green600
    } else {
        AppTheme.colors.primary500
    }
    var showMenu by remember { mutableStateOf(false) }
    Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = PaddingDefaults.Medium)
            .clickable {
                onClick()
            }
    ) {
        Icon(Icons.Outlined.PersonOutline, null, tint = color)
        Text(
            name, style = MaterialTheme.typography.body1,
            modifier = Modifier
                .weight(1.0f)
        )
        IconButton(onClick = { showMenu = !showMenu }) {
            Icon(Icons.Rounded.MoreVert, null, tint = AppTheme.colors.neutral400)
        }
        DropdownMenu(
            expanded = showMenu,
            onDismissRequest = { showMenu = false }
        ) {
            content()
        }
    }
}

@Composable
fun ProfileDropDownItem(
    icon: ImageVector,
    text: String,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    DropdownMenuItem(onClick = onClick) {
        Icon(
            icon,
            tint = AppTheme.colors.neutral600,
            contentDescription = text,
            modifier = modifier.size(18.dp)
        )
        Spacer(modifier = modifier.width(8.dp))
        Text(
            text = text,
            color = AppTheme.colors.neutral900,
            style = MaterialTheme.typography.body1
        )
    }
}

@Composable
private fun AddProfile(
    onClick: () -> Unit
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = PaddingDefaults.Medium)
    ) {
        Icon(Icons.Rounded.Add, null, tint = AppTheme.colors.primary500)
        Text(
            stringResource(R.string.settings_add_profile),
            style = MaterialTheme.typography.body1,
            color = AppTheme.colors.primary600,
            modifier = Modifier.weight(1.0f)
        )
    }
}

@Composable
private fun EditProfileNameDialog(
    profile: SettingsScreen.UIProfile,
    onEdit: (text: String) -> Unit,
    onDismissRequest: () -> Unit
) {

    var textValue by remember { mutableStateOf(profile.name) }

    AlertDialog(
        title = {
            Text(
                stringResource(R.string.profile_edit_name),
                style = MaterialTheme.typography.subtitle1,
            )
        },
        onDismissRequest = onDismissRequest,
        text = {
            Column() {
                Text(
                    stringResource(R.string.profile_edit_name_info),
                    style = MaterialTheme.typography.body2
                )
                Box(modifier = Modifier.padding(top = 12.dp)) {
                    OutlinedTextField(
                        value = textValue,
                        onValueChange = {
                            textValue = it
                        },
                    )
                }
            }
        },
        buttons = {
            Row(Modifier.padding(bottom = 12.dp, start = 12.dp, end = 12.dp)) {
                Spacer(modifier = Modifier.weight(1.0f))
                TextButton(onClick = { onDismissRequest() }) {
                    Text(stringResource(R.string.cancel).uppercase(Locale.getDefault()))
                }
                TextButton(onClick = { onEdit(textValue) }) {
                    Text(stringResource(R.string.ok).uppercase(Locale.getDefault()))
                }
            }
        },
    )
}

@Composable
private fun AddProfileDialog(onEdit: (text: String) -> Unit, onDismissRequest: () -> Unit) {

    var textValue by remember { mutableStateOf("") }

    AlertDialog(
        title = {
            Text(
                stringResource(R.string.profile_edit_name),
                style = MaterialTheme.typography.subtitle1,
            )
        },
        onDismissRequest = onDismissRequest,
        text = {
            Column() {
                Text(
                    stringResource(R.string.profile_edit_name_info),
                    style = MaterialTheme.typography.body2
                )
                Box(modifier = Modifier.padding(top = 12.dp)) {
                    OutlinedTextField(
                        value = textValue,
                        onValueChange = {
                            textValue = it
                        },
                        placeholder = { Text(stringResource(R.string.profile_edit_name_place_holder)) }
                    )
                }
            }
        },
        buttons = {
            Row(Modifier.padding(bottom = 12.dp, start = 12.dp, end = 12.dp)) {
                Spacer(modifier = Modifier.weight(1.0f))
                TextButton(onClick = { onDismissRequest() }) {
                    Text(stringResource(R.string.cancel).uppercase(Locale.getDefault()))
                }
                TextButton(onClick = { onEdit(textValue) }) {
                    Text(stringResource(R.string.ok).uppercase(Locale.getDefault()))
                }
            }
        },
    )
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
    Text(
        text = stringResource(id = R.string.debug_menu),
        style = MaterialTheme.typography.h6,
        modifier = Modifier
            .padding(
                start = PaddingDefaults.Medium,
                end = PaddingDefaults.Medium,
                bottom = PaddingDefaults.Medium / 2,
                top = PaddingDefaults.Medium
            )
            .clickable {
                navController.navigate(SettingsNavigationScreens.Debug.path())
            }
            .testId("stg_btn_debug_menu")
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
                .testId("settings/trackingToggle"),
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
            .testId("settings/allowScreenshotsToggle"),
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
                start = PaddingDefaults.Medium,
                end = PaddingDefaults.Medium,
                bottom = PaddingDefaults.Medium / 2,
                top = PaddingDefaults.Medium
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
private fun ContactSection(onClickFeedback: () -> Unit) {
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

@Composable
private fun TokenSection(tokenAvailable: Boolean, onClick: () -> Unit) {

    val context = LocalContext.current
    val noTokenAvailableText = stringResource(R.string.settings_no_active_token)

    val iconColor = if (tokenAvailable) {
        AppTheme.colors.primary500
    } else {
        AppTheme.colors.primary300
    }

    val textColor = if (tokenAvailable) {
        AppTheme.colors.neutral999
    } else {
        AppTheme.colors.neutral600
    }
    Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .clickable(
                onClick = {
                    if (tokenAvailable) {
                        onClick()
                    } else {
                        Toast
                            .makeText(context, noTokenAvailableText, Toast.LENGTH_SHORT)
                            .show()
                    }
                }
            )
            .padding(PaddingDefaults.Medium)
            .semantics(mergeDescendants = true) {}
    ) {
        Icon(Icons.Outlined.VpnKey, null, tint = iconColor)
        Text(
            stringResource(
                R.string.settings_show_token
            ),
            style = MaterialTheme.typography.body1, color = textColor
        )
    }
}

@Composable
fun TokenScreen(navController: NavHostController, settingsViewModel: SettingsViewModel) {
    val header = stringResource(id = R.string.token_headline)

    Scaffold(
        topBar = {
            NavigationTopAppBar(
                NavigationBarMode.Back,
                headline = header,
                onClick = { navController.popBackStack() }
            )
        },
    ) { innerPadding ->

        var token by remember {
            mutableStateOf(SettingsUseCase.Token())
        }

        LaunchedEffect(Unit) {
            token = settingsViewModel.getToken()
        }

        val accessTokenTitle = stringResource(id = R.string.access_token_title)
        val accessTokenText = token.accessToken

        val singleSignOnTokenTitle = stringResource(id = R.string.single_sign_on_token_title)
        val singleSignOnTokenText = token.singleSignOnToken?.token

        LazyColumn(modifier = Modifier.padding(vertical = PaddingDefaults.Medium)) {
            item {
                TokenLabel(
                    title = accessTokenTitle,
                    text = accessTokenText ?: stringResource(id = R.string.no_access_token),
                    tokenAvailable = token.accessToken != null
                )
                Divider(modifier = Modifier.padding(start = PaddingDefaults.Medium))
                TokenLabel(
                    title = singleSignOnTokenTitle,
                    text = singleSignOnTokenText
                        ?: stringResource(id = R.string.no_single_sign_on_token),
                    tokenAvailable = token.singleSignOnToken != null
                )
            }
        }
    }
}

@Composable
private fun TokenLabel(title: String, text: String, tokenAvailable: Boolean) {

    val clipboardManager = LocalClipboardManager.current
    val context = LocalContext.current
    val copied = stringResource(R.string.copied)
    val description = if (tokenAvailable) {
        stringResource(R.string.copy_content_description)
    } else {
        stringResource(R.string.copied)
    }

    val mod = if (tokenAvailable) {
        Modifier
            .clickable(onClick = {
                if (tokenAvailable) {
                    clipboardManager.setText(androidx.compose.ui.text.AnnotatedString(text))
                    Toast
                        .makeText(context, "$title $copied", Toast.LENGTH_SHORT)
                        .show()
                }
            })
            .semantics { contentDescription = description }
    } else {
        Modifier
    }

    Row(
        modifier = mod
    ) {
        Row(
            modifier = Modifier
                .sizeIn(maxHeight = 200.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(PaddingDefaults.Medium)
                    .weight(1f)
            ) {
                Text(title, style = MaterialTheme.typography.subtitle1)
                LazyColumn() {
                    item {
                        Text(text, style = MaterialTheme.typography.body2)
                    }
                }
            }

            if (tokenAvailable) {
                Column(
                    modifier = Modifier
                        .align(Alignment.CenterVertically)
                        .padding(end = PaddingDefaults.Medium)
                ) {
                    Icon(Icons.Outlined.ContentCopy, null, tint = AppTheme.colors.neutral400)
                }
            }
        }
    }
}
