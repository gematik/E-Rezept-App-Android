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
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.Divider
import androidx.compose.material.Icon
import androidx.compose.material.LocalContentColor
import androidx.compose.material.LocalTextStyle
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AccessibilityNew
import androidx.compose.material.icons.outlined.Code
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.KeyboardArrowRight
import androidx.compose.material.icons.outlined.LockOpen
import androidx.compose.material.icons.outlined.Mail
import androidx.compose.material.icons.outlined.PrivacyTip
import androidx.compose.material.icons.outlined.Security
import androidx.compose.material.icons.outlined.Source
import androidx.compose.material.icons.outlined.Timeline
import androidx.compose.material.icons.outlined.Wysiwyg
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Phone
import androidx.compose.material.icons.rounded.PhoneAndroid
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
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
import de.gematik.ti.erp.app.mainscreen.ui.MainNavigationScreens
import de.gematik.ti.erp.app.profiles.ui.Avatar
import de.gematik.ti.erp.app.profiles.usecase.model.ProfilesUseCaseData
import de.gematik.ti.erp.app.theme.AppTheme
import de.gematik.ti.erp.app.theme.PaddingDefaults
import de.gematik.ti.erp.app.utils.compose.AlertDialog
import de.gematik.ti.erp.app.utils.compose.OutlinedDebugButton
import de.gematik.ti.erp.app.utils.compose.Spacer4
import de.gematik.ti.erp.app.utils.compose.SpacerLarge
import de.gematik.ti.erp.app.utils.compose.SpacerMedium
import de.gematik.ti.erp.app.utils.compose.SpacerSmall
import de.gematik.ti.erp.app.utils.compose.handleIntent
import de.gematik.ti.erp.app.utils.compose.navigationModeState
import de.gematik.ti.erp.app.utils.compose.provideEmailIntent
import de.gematik.ti.erp.app.utils.compose.providePhoneIntent
import java.util.Locale

@Composable
fun SettingsScreen(
    mainNavController: NavController,
    settingsViewModel: SettingsViewModel
) {
    val settingsNavController = rememberNavController()

    val navigationMode by settingsNavController.navigationModeState(SettingsNavigationScreens.Settings.route)

    SettingsNavGraph(
        settingsNavController = settingsNavController,
        navigationMode = navigationMode,
        mainNavController = mainNavController,
        settingsViewModel = settingsViewModel
    )
}

@Composable
fun SettingsScreenWithScaffold(
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

    Scaffold(
        modifier = Modifier
            .testTag(TestTag.Settings.SettingsScreen)
            .statusBarsPadding()
    ) { contentPadding ->

        LazyColumn(
            modifier = Modifier.testTag("settings_screen"),
            contentPadding = contentPadding,
            state = listState
        ) {
            if (BuildKonfig.INTERNAL) {
                item {
                    DebugMenuSection(mainNavController)
                    SettingsDivider()
                }
            }
            item {
                ProfileSection(state, settingsViewModel, mainNavController)
                SettingsDivider()
            }
            item {
                HealthCardSection(
                    onClickUnlockEgk = { changeSecret ->
                        mainNavController.navigate(MainNavigationScreens.UnlockEgk.path(changeSecret = changeSecret))
                    },
                    onClickOrderHealthCard = {
                        mainNavController.navigate(MainNavigationScreens.OrderHealthCard.path())
                    }
                )
                SettingsDivider()
            }
            item {
                GlobalSettingsSection(
                    onClickAccessibilitySettings = {
                        navController.navigate(SettingsNavigationScreens.AccessibilitySettings.path())
                    },
                    onClickProductImprovementSettings = {
                        navController.navigate(SettingsNavigationScreens.ProductImprovementSettings.path())
                    },
                    onClickDeviceSecuritySettings = {
                        navController.navigate(SettingsNavigationScreens.DeviceSecuritySettings.path())
                    }
                )
                SettingsDivider()
            }
            item {
                ContactSection()
                SettingsDivider()
            }
            item {
                LegalSection(mainNavController)
            }
            item {
                AboutSection(Modifier.padding(top = 76.dp))
            }
        }
    }
}

@Composable
fun GlobalSettingsSection(
    onClickAccessibilitySettings: () -> Unit,
    onClickProductImprovementSettings: () -> Unit,
    onClickDeviceSecuritySettings: () -> Unit

) {
    Column {
        Text(
            text = stringResource(R.string.settings_personal_settings_header),
            style = AppTheme.typography.h6,
            modifier = Modifier.padding(
                start = PaddingDefaults.Medium,
                end = PaddingDefaults.Medium,
                bottom = PaddingDefaults.Medium / 2,
                top = PaddingDefaults.Medium
            )
        )
        LabelButton(
            Icons.Outlined.AccessibilityNew,
            stringResource(R.string.settings_accessibility_header)
        ) {
            onClickAccessibilitySettings()
        }
        LabelButton(
            Icons.Outlined.Timeline,
            stringResource(R.string.settings_product_improvement_header)
        ) {
            onClickProductImprovementSettings()
        }
        LabelButton(
            Icons.Outlined.Security,
            stringResource(R.string.settings_device_security_header)
        ) {
            onClickDeviceSecuritySettings()
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
                onClickEdit = { navController.navigate(MainNavigationScreens.EditProfile.path(profileId = profile.id)) }
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
    onClickEdit: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(role = Role.Button) {
                onClickEdit()
            }
            .padding(horizontal = PaddingDefaults.Medium, vertical = PaddingDefaults.ShortMedium),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Avatar(Modifier.size(48.dp), profile, null)
        SpacerMedium()
        Text(
            modifier = Modifier.weight(1f),
            text = profile.name,
            style = AppTheme.typography.body1
        )
        Icon(Icons.Outlined.KeyboardArrowRight, null, tint = AppTheme.colors.neutral400)
    }
}

@Composable
private fun AddProfile(
    onClick: () -> Unit
) {
    TextButton(
        modifier = Modifier.testTag(TestTag.Settings.AddProfileButton),
        onClick = { onClick() },
        contentPadding = PaddingValues(PaddingDefaults.Medium)
    ) {
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
                        modifier = Modifier.testTag(TestTag.Settings.AddProfileDialog.ProfileNameTextField),
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
                modifier = Modifier.testTag(TestTag.Settings.AddProfileDialog.ConfirmButton),
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
private fun DebugMenuSection(navController: NavController) {
    OutlinedDebugButton(
        text = stringResource(id = R.string.debug_menu),
        onClick = { navController.navigate(MainNavigationScreens.Debug.path()) },
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
            navController.navigate(MainNavigationScreens.Imprint.route)
        }
        LabelButton(
            Icons.Outlined.PrivacyTip,
            stringResource(R.string.settings_legal_dataprotection),
            modifier = Modifier.testTag("settings/privacy")
        ) {
            navController.navigate(MainNavigationScreens.DataProtection.route)
        }
        LabelButton(
            Icons.Outlined.Wysiwyg,
            stringResource(R.string.settings_legal_tos),
            modifier = Modifier.testTag("settings/tos")
        ) {
            navController.navigate(MainNavigationScreens.Terms.route)
        }
        LabelButton(
            Icons.Outlined.Code,
            stringResource(R.string.settings_legal_licences),
            modifier = Modifier.testTag("settings/licences")
        ) {
            navController.navigate(MainNavigationScreens.OpenSourceLicences.route)
        }
        LabelButton(
            Icons.Outlined.Source,
            stringResource(R.string.settings_licence_pharmacy_search),
            modifier = Modifier.testTag("settings/additional_licences")
        ) {
            navController.navigate(MainNavigationScreens.AdditionalLicences.route)
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
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(PaddingDefaults.Medium)
            .semantics(mergeDescendants = true) {}
    ) {
        Icon(icon, null, tint = AppTheme.colors.primary600)
        SpacerMedium()
        Text(
            modifier = Modifier.weight(1f),
            text = text,
            style = AppTheme.typography.body1
        )
        Icon(Icons.Outlined.KeyboardArrowRight, null, tint = AppTheme.colors.neutral400)
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
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(PaddingDefaults.Medium)
            .semantics(mergeDescendants = true) {}
    ) {
        Image(painter = icon, contentDescription = null)
        SpacerMedium()
        Text(
            modifier = Modifier.weight(1f),
            text = text,
            style = AppTheme.typography.body1
        )
        Icon(Icons.Outlined.KeyboardArrowRight, null, tint = AppTheme.colors.neutral400)
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
