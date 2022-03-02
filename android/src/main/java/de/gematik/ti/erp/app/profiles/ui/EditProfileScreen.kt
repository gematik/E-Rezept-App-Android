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

package de.gematik.ti.erp.app.profiles.ui

import android.widget.Toast
import androidx.annotation.StringRes
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.Card
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CloudQueue
import androidx.compose.material.icons.outlined.Done
import androidx.compose.material.icons.outlined.VpnKey
import androidx.compose.material.icons.rounded.Devices
import androidx.compose.material.icons.rounded.LockOpen
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.produceState
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.semantics

import androidx.compose.ui.text.font.FontWeight

import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.google.accompanist.insets.LocalWindowInsets
import com.google.accompanist.insets.rememberInsetsPaddingValues
import de.gematik.ti.erp.app.R
import de.gematik.ti.erp.app.cardwall.domain.biometric.deviceStrongBiometricStatus
import de.gematik.ti.erp.app.cardwall.domain.biometric.hasDeviceStrongBox
import de.gematik.ti.erp.app.cardwall.domain.biometric.isDeviceSupportsBiometric
import de.gematik.ti.erp.app.db.entities.ProfileColorNames
import de.gematik.ti.erp.app.profiles.usecase.model.ProfilesUseCaseData
import de.gematik.ti.erp.app.settings.ui.AddProfileDialog
import de.gematik.ti.erp.app.settings.ui.SettingsScreen
import de.gematik.ti.erp.app.settings.ui.SettingsViewModel
import de.gematik.ti.erp.app.theme.AppTheme
import de.gematik.ti.erp.app.theme.PaddingDefaults
import de.gematik.ti.erp.app.utils.compose.AnimatedElevationScaffold
import de.gematik.ti.erp.app.utils.compose.CommonAlertDialog
import de.gematik.ti.erp.app.utils.compose.HintCard
import de.gematik.ti.erp.app.utils.compose.HintCardDefaults
import de.gematik.ti.erp.app.utils.compose.HintSmallImage
import de.gematik.ti.erp.app.utils.compose.HintTextActionButton
import de.gematik.ti.erp.app.utils.compose.InputField
import de.gematik.ti.erp.app.utils.compose.LabeledSwitch
import de.gematik.ti.erp.app.utils.compose.LabeledSwitchWithLink
import de.gematik.ti.erp.app.utils.compose.Spacer16
import de.gematik.ti.erp.app.utils.compose.Spacer24
import de.gematik.ti.erp.app.utils.compose.Spacer4
import de.gematik.ti.erp.app.utils.compose.Spacer40
import de.gematik.ti.erp.app.utils.compose.SpacerMedium
import de.gematik.ti.erp.app.utils.compose.SpacerTiny
import de.gematik.ti.erp.app.utils.compose.annotatedStringResource
import de.gematik.ti.erp.app.utils.firstCharOfForeNameSurName
import de.gematik.ti.erp.app.utils.sanitizeProfileName
import kotlinx.coroutines.delay
import java.util.Locale

@Composable
fun EditProfileScreen(
    state: SettingsScreen.State,
    profile: ProfilesUseCaseData.Profile,
    settingsViewModel: SettingsViewModel,
    onRemoveProfile: (newProfileName: String?) -> Unit,
    onBack: () -> Unit,
    mainNavController: NavController,
) {
    val navController = rememberNavController()

    EditProfileNavGraph(
        state = state,
        navController = navController,
        onBack = onBack,
        profile = profile,
        settingsViewModel = settingsViewModel,
        onRemoveProfile = onRemoveProfile,
        mainNavController = mainNavController
    )
}

@Composable
fun EditProfileScreen(
    profileId: Int,
    settingsViewModel: SettingsViewModel,
    onBack: () -> Unit,
    mainNavController: NavController,
) {
    val state by produceState(initialValue = SettingsScreen.defaultState) {
        settingsViewModel.screenState().collect {
            value = it
        }
    }

    state.profileById(profileId)?.let { profile ->
        EditProfileScreen(
            state = state,
            onBack = onBack,
            profile = profile,
            settingsViewModel = settingsViewModel,
            onRemoveProfile = {
                settingsViewModel.removeProfile(profile, it)
                onBack()
            },
            mainNavController = mainNavController
        )
    }
}

@Composable
fun EditProfileScreenContent(
    onBack: () -> Unit,
    selectedProfile: ProfilesUseCaseData.Profile,
    state: SettingsScreen.State,
    settingsViewModel: SettingsViewModel,
    onRemoveProfile: (newProfileName: String?) -> Unit,
    onClickToken: () -> Unit,
    ssoTokenValid: Boolean = false,
    onClickLogIn: () -> Unit,
    onClickAuditEvents: () -> Unit
) {
    val listState = rememberLazyListState()
    val isFeatureBioLogin by produceState(false) {
        settingsViewModel.isFeatureBioLoginEnabled().collect { value = it }
    }

    AnimatedElevationScaffold(
        topBarTitle = stringResource(R.string.edit_profile_title),
        listState = listState,
        onBack = onBack,
    ) {
        var showAddDefaultProfileDialog by remember { mutableStateOf(false) }

        LazyColumn(
            modifier = Modifier.testTag("edit_profile_screen"),
            state = listState,
            contentPadding = rememberInsetsPaddingValues(
                insets = LocalWindowInsets.current.navigationBars,
                applyStart = false,
                applyTop = false,
                applyEnd = false,
                applyBottom = true
            )
        ) {
            if (!selectedProfile.connected()) {
                item {
                    ConnectProfileHint(onClickLogIn = onClickLogIn)
                }
            }
            item {
                ColorAndProfileNameSection(
                    profile = selectedProfile,
                    state = state,
                    onChangeProfileName = {
                        settingsViewModel.updateProfileName(selectedProfile, it)
                    },
                    onSelectProfileColor = {
                        settingsViewModel.updateProfileColor(selectedProfile, it)
                    }
                )
            }
            if (isFeatureBioLogin) item { LoginSection() }
            item { SecuritySection(onClickToken, onClickAuditEvents, selectedProfile.ssoToken != null) }
            item {
                if (ssoTokenValid) {
                    LogoutButton(onClick = {
                        settingsViewModel.logout(selectedProfile)
                    })
                } else
                    LoginButton(
                        onClick = { onClickLogIn() }
                    )
            }
            item {
                RemoveProfileSection(
                    onClickRemoveProfile = {
                        if (state.uiProfiles.size == 1) {
                            showAddDefaultProfileDialog = true
                        } else {
                            onRemoveProfile(null)
                        }
                    }
                )
            }
        }

        if (showAddDefaultProfileDialog) {
            AddProfileDialog(
                state = state,
                wantRemoveLastProfile = true,
                onEdit = { showAddDefaultProfileDialog = false; onRemoveProfile(it) },
                onDismissRequest = { showAddDefaultProfileDialog = false }
            )
        }
    }
}

@Composable
fun ConnectProfileHint(onClickLogIn: () -> Unit) {
    HintCard(
        modifier = Modifier.padding(PaddingDefaults.Medium),
        properties = HintCardDefaults.properties(
            backgroundColor = AppTheme.colors.primary100,
            border = BorderStroke(0.0.dp, AppTheme.colors.primary100),
            elevation = 0.dp
        ),
        image = {
            HintSmallImage(
                painterResource(R.drawable.connect_profile),
                innerPadding = it
            )
        },
        title = { Text(stringResource(R.string.connect_profile_header)) },
        body = { Text(stringResource(R.string.connect_profile_info)) },
        action = {
            HintTextActionButton(
                text = stringResource(R.string.connect_profile_connect),
                onClick = onClickLogIn,
            )
        }
    ) {
    }
}

@Composable
fun SecuritySection(
    onClickToken: () -> Unit,
    onClickAuditEvents: () -> Unit,
    tokenAvailable: Boolean
) {
    MenuHeadline(stringResource(R.string.settings_appprotection_headline))
    SecurityTokenSubSection(tokenAvailable, onClickToken)
    SecurityAuditEventsSubSection(onClickAuditEvents)
}

@Composable
fun LoginSection() {
    val context = LocalContext.current
    val deviceBioStatus = deviceStrongBiometricStatus(context)
    val isStrongBoxAndBiometric = isDeviceSupportsBiometric(deviceBioStatus) && hasDeviceStrongBox(context)
    val checked = false // todo: ERA-4389 - Check Saved Bio-Strong data on repo
    val onCheckedChange: (Boolean) -> Unit = {} // todo: ERA-4389 - toggleOn = open Cardwall for save bioStrong data on IDP; toggleOff = ERA-4388
    val onConnectedDevicesClicked: () -> Unit = {} // todo: ERA-4389 - Geräte mit gemerkten Zugangsdaten anzeigen
    val uriHandler = LocalUriHandler.current
    val uri = stringResource(R.string.settings_faq_link)

    MenuHeadline(stringResource(R.string.settings_login_headline))
    if (isStrongBoxAndBiometric) {
        LabeledSwitch(
            enabled = isStrongBoxAndBiometric,
            checked = checked,
            onCheckedChange = onCheckedChange,
            icon = Icons.Rounded.LockOpen,
            header = stringResource(R.string.settings_login_data_save),
            description = null,
        )
    } else {
        LabeledSwitchWithLink(
            enabled = isStrongBoxAndBiometric,
            checked = checked,
            onCheckedChange = onCheckedChange,
            icon = Icons.Rounded.LockOpen,
            header = stringResource(R.string.settings_login_data_save),
            description = stringResource(R.string.settings_login_no_bio_and_strongbox_device),
            link = stringResource(R.string.settings_faq),
            onClickLink = { uriHandler.openUri(uri) }
        )
    }

    Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.Top,
        modifier = Modifier
            .fillMaxWidth()
            .clickable(
                onClick = onConnectedDevicesClicked
            )
            .padding(PaddingDefaults.Medium)
            .semantics(mergeDescendants = true) {},
    ) {
        Icon(Icons.Rounded.Devices, null, tint = AppTheme.colors.primary600)
        Text(stringResource(R.string.settings_login_connected_devices), style = MaterialTheme.typography.body1)
    }
}

@Composable
fun SecurityAuditEventsSubSection(onClickAuditEvents: () -> Unit) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.Top,
        modifier = Modifier
            .fillMaxWidth()
            .clickable(
                onClick = {
                    onClickAuditEvents()
                }
            )
            .padding(PaddingDefaults.Medium)
            .semantics(mergeDescendants = true) {},
    ) {
        Icon(Icons.Outlined.CloudQueue, null, tint = AppTheme.colors.primary500)
        Column {
            Text(
                stringResource(
                    R.string.settings_show_audit_events
                ),
                style = MaterialTheme.typography.body1
            )
            Text(
                stringResource(
                    R.string.settings_show_audit_events_info
                ),
                style = AppTheme.typography.body2l
            )
        }
    }
}

@Composable
fun SecurityTokenSubSection(tokenAvailable: Boolean, onClick: () -> Unit) {
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
        verticalAlignment = Alignment.Top,
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
            .semantics(mergeDescendants = true) {},
    ) {
        Icon(Icons.Outlined.VpnKey, null, tint = iconColor)
        Column {
            Text(
                stringResource(
                    R.string.settings_show_token
                ),
                style = MaterialTheme.typography.body1, color = textColor
            )
            Text(
                stringResource(
                    R.string.settings_show_token_info
                ),
                style = AppTheme.typography.body2l
            )
        }
    }
}

@Composable
private fun MenuHeadline(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.h6,
        modifier = Modifier.padding(PaddingDefaults.Medium),
    )
}

@Composable
private fun LoginButton(onClick: () -> Unit) {
    LoginLogoutButton(
        onClick = onClick,
        buttonText = R.string.login_profile,
        buttonDescription = R.string.login_description,
        contentColor = AppTheme.colors.primary700
    )
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

    LoginLogoutButton(
        onClick = { dialogVisible = true },
        buttonText = R.string.logout_profile,
        buttonDescription = R.string.logout_description,
        contentColor = AppTheme.colors.red700
    )
}

@Composable
private fun LoginLogoutButton(
    onClick: () -> Unit,
    @StringRes buttonText: Int,
    @StringRes buttonDescription: Int,
    contentColor: Color
) {
    Button(
        onClick = { onClick() },
        modifier = Modifier
            .padding(
                start = 16.dp,
                end = 16.dp,
                top = 32.dp,
                bottom = 16.dp
            )
            .fillMaxWidth(),
        colors = ButtonDefaults.buttonColors(
            backgroundColor = AppTheme.colors.neutral050,
            contentColor = contentColor
        )
    ) {
        Text(
            stringResource(buttonText).uppercase(Locale.getDefault()),
            modifier = Modifier.padding(
                start = 16.dp,
                end = 16.dp,
                top = 8.dp,
                bottom = 8.dp
            )
        )
    }
    Text(
        stringResource(buttonDescription),
        modifier = Modifier.padding(
            start = PaddingDefaults.Medium,
            end = PaddingDefaults.Medium,
            bottom = PaddingDefaults.Small
        ),
        style = AppTheme.typography.body2l,
        textAlign = TextAlign.Center
    )
}

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun ColorAndProfileNameSection(
    profile: ProfilesUseCaseData.Profile,
    state: SettingsScreen.State,
    onChangeProfileName: (String) -> Unit,
    onSelectProfileColor: (ProfileColorNames) -> Unit
) {
    val currentSelectedColors = profileColor(profileColorNames = profile.color)

    var profileName by rememberSaveable(profile.name) { mutableStateOf(profile.name) }
    var profileNameError by remember { mutableStateOf(false) }
    val initials = remember(profile.name) { firstCharOfForeNameSurName(profile.name) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(PaddingDefaults.Medium)
    ) {
        SpacerTiny()
        Surface(
            modifier = Modifier
                .size(140.dp)
                .align(Alignment.CenterHorizontally),
            shape = CircleShape,
            color = currentSelectedColors.backGroundColor
        ) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = initials,
                    style = MaterialTheme.typography.body2,
                    color = currentSelectedColors.textColor,
                    textAlign = TextAlign.Center,
                    fontWeight = FontWeight.Bold,
                    fontSize = 60.sp,
                )
            }
        }

        LaunchedEffect(profileName) {
            if (!profileNameError) {
                delay(500)
                onChangeProfileName(profileName)
            }
        }

        val keyboardController = LocalSoftwareKeyboardController.current

        Spacer40()
        InputField(
            modifier = Modifier
                .testTag("editProfile/profile_text_input")
                .fillMaxWidth(),
            value = profileName,
            onValueChange = {
                profileName = sanitizeProfileName(it.trimStart())
                profileNameError = profileName.isEmpty() ||
                    (
                        profileName.trim() != profile.name && state.containsProfileWithName(
                            profileName
                        )
                        )
            },
            onSubmit = {
                if (!profileNameError) {
                    onChangeProfileName(profileName)
                    keyboardController?.hide()
                }
            },
            isError = profileNameError,
        )

        val errorText = if (profileName.isEmpty()) {
            stringResource(R.string.edit_profile_empty_profile_name)
        } else {
            stringResource(R.string.edit_profile_duplicated_profile_name)
        }

        if (profileNameError) {
            Spacer4()
            Text(
                text = errorText,
                color = AppTheme.colors.red600,
                style = MaterialTheme.typography.caption,
                modifier = Modifier.padding(start = PaddingDefaults.Medium)
            )
        }
        SpacerMedium()
        ProfileConnectedCard(profile.insuranceInformation)
        Spacer40()
        Text(
            stringResource(R.string.edit_profile_background_color),
            style = MaterialTheme.typography.h6
        )

        Spacer24()
        Row(
            horizontalArrangement = Arrangement.spacedBy(PaddingDefaults.Medium),
            modifier = Modifier.align(Alignment.CenterHorizontally)
        ) {
            ProfileColorNames.values().forEach {
                val currentValueColors = profileColor(profileColorNames = it)
                ColorSelector(
                    profileColorName = it,
                    selected = currentValueColors == currentSelectedColors,
                    onSelectColor = onSelectProfileColor
                )
            }
        }
        Spacer16()
        Row(modifier = Modifier.align(Alignment.CenterHorizontally)) {
            Text(
                currentSelectedColors.colorName,
                style = AppTheme.typography.body2l
            )
        }
    }
}

@Composable
fun ProfileConnectedCard(insuranceInformation: ProfilesUseCaseData.ProfileInsuranceInformation) {

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        backgroundColor = AppTheme.colors.neutral100,
        contentColor = AppTheme.colors.neutral999,
        elevation = 0.dp,
    ) {
        val textStyle = AppTheme.typography.body2l

        if (insuranceInformation.insurantName != null && insuranceInformation.insuranceIdentifier != null && insuranceInformation.insuranceName != null) {
            Column(modifier = Modifier.padding(PaddingDefaults.Medium)) {
                Text(
                    stringResource(
                        R.string.profile_connected
                    ),
                    style = MaterialTheme.typography.body1
                )
                Text(insuranceInformation.insurantName, style = textStyle)
                Text(insuranceInformation.insuranceIdentifier, style = textStyle)
                Text(insuranceInformation.insuranceName, style = textStyle)
            }
        } else {
            Column(modifier = Modifier.padding(PaddingDefaults.Medium)) {
                Text(
                    stringResource(R.string.profile_not_connected),
                    textAlign = TextAlign.Center,
                    style = textStyle
                )
            }
        }
    }
}

@Composable
fun createProfileColor(colors: ProfileColorNames): ProfileColor {
    return profileColor(profileColorNames = colors)
}

@Composable
fun ColorSelector(
    profileColorName: ProfileColorNames,
    selected: Boolean,
    onSelectColor: (ProfileColorNames) -> Unit,
) {
    val colors = createProfileColor(profileColorName)
    val contentDescription = annotatedStringResource(
        R.string.edit_profile_color_selected,
        profileColorName.name
    ).toString()

    Surface(
        modifier = Modifier
            .size(40.dp),
        shape = CircleShape,
        color = colors.backGroundColor
    ) {
        Row(
            modifier = Modifier.clickable(onClick = { onSelectColor(profileColorName) }),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (selected) {
                Icon(
                    Icons.Outlined.Done,
                    contentDescription,
                    tint = colors.textColor,
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }
}

@Composable
fun RemoveProfileSection(onClickRemoveProfile: () -> Unit) {
    var dialogVisible by remember { mutableStateOf(false) }
    if (dialogVisible) {

        CommonAlertDialog(
            header = stringResource(id = R.string.remove_profile_header),
            info = stringResource(R.string.remove_profile_detail_message),
            actionText = stringResource(R.string.remove_profile_yes),
            cancelText = stringResource(R.string.remove_profile_no),
            onCancel = { dialogVisible = false },
            onClickAction = {
                onClickRemoveProfile()
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
            stringResource(R.string.remove_profile).uppercase(Locale.getDefault()),
            modifier = Modifier.padding(
                start = 16.dp,
                end = 16.dp,
                top = 8.dp,
                bottom = 8.dp
            )
        )
    }
}
