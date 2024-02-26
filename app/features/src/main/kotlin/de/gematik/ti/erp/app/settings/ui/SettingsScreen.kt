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

@file:Suppress("MaximumLineLength")

package de.gematik.ti.erp.app.settings.ui

import android.content.Context
import androidx.activity.ComponentActivity
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.LazyListState
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
import androidx.compose.material.icons.outlined.ChecklistRtl
import androidx.compose.material.icons.outlined.Code
import androidx.compose.material.icons.outlined.HelpOutline
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.KeyboardArrowRight
import androidx.compose.material.icons.outlined.LockOpen
import androidx.compose.material.icons.outlined.Mail
import androidx.compose.material.icons.outlined.PrivacyTip
import androidx.compose.material.icons.outlined.Security
import androidx.compose.material.icons.outlined.SettingsInputComposite
import androidx.compose.material.icons.outlined.Source
import androidx.compose.material.icons.outlined.Timeline
import androidx.compose.material.icons.outlined.TireRepair
import androidx.compose.material.icons.outlined.Wysiwyg
import androidx.compose.material.icons.rounded.PersonOutline
import androidx.compose.material.icons.rounded.Phone
import androidx.compose.material.icons.rounded.PhoneAndroid
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
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
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavController
import de.gematik.ti.erp.app.BuildKonfig
import de.gematik.ti.erp.app.MainActivity
import de.gematik.ti.erp.app.Requirement
import de.gematik.ti.erp.app.TestTag
import de.gematik.ti.erp.app.analytics.navigation.TrackingScreenRoutes
import de.gematik.ti.erp.app.card.model.command.UnlockMethod
import de.gematik.ti.erp.app.cardunlock.navigation.CardUnlockRoutes
import de.gematik.ti.erp.app.core.LocalActivity
import de.gematik.ti.erp.app.demomode.DemoModeIntent
import de.gematik.ti.erp.app.demomode.DemoModeObserver
import de.gematik.ti.erp.app.demomode.startAppWithDemoMode
import de.gematik.ti.erp.app.demomode.startAppWithNormalMode
import de.gematik.ti.erp.app.features.R
import de.gematik.ti.erp.app.info.BuildConfigInformation
import de.gematik.ti.erp.app.mainscreen.navigation.MainNavigationScreens
import de.gematik.ti.erp.app.navigation.Screen
import de.gematik.ti.erp.app.pharmacy.navigation.PharmacyRoutes
import de.gematik.ti.erp.app.profiles.navigation.ProfileRoutes
import de.gematik.ti.erp.app.profiles.presentation.ProfileController
import de.gematik.ti.erp.app.profiles.presentation.rememberProfileController
import de.gematik.ti.erp.app.profiles.repository.ProfileIdentifier
import de.gematik.ti.erp.app.profiles.ui.Avatar
import de.gematik.ti.erp.app.profiles.usecase.model.ProfilesUseCaseData.Profile
import de.gematik.ti.erp.app.profiles.usecase.model.ProfilesUseCaseData.Profile.Companion.containsProfileWithName
import de.gematik.ti.erp.app.settings.navigation.SettingsNavigationScreens
import de.gematik.ti.erp.app.theme.AppTheme
import de.gematik.ti.erp.app.theme.PaddingDefaults
import de.gematik.ti.erp.app.utils.buildFeedbackBodyWithDeviceInfo
import de.gematik.ti.erp.app.utils.compose.AlertDialog
import de.gematik.ti.erp.app.utils.compose.OutlinedDebugButton
import de.gematik.ti.erp.app.utils.compose.SpacerLarge
import de.gematik.ti.erp.app.utils.compose.SpacerMedium
import de.gematik.ti.erp.app.utils.compose.SpacerSmall
import de.gematik.ti.erp.app.utils.compose.SpacerTiny
import de.gematik.ti.erp.app.utils.compose.handleIntent
import de.gematik.ti.erp.app.utils.compose.providePhoneIntent
import de.gematik.ti.erp.app.utils.extensions.BuildConfigExtension
import de.gematik.ti.erp.app.utils.extensions.LocalSnackbar
import de.gematik.ti.erp.app.utils.extensions.sanitizeProfileName
import de.gematik.ti.erp.app.utils.openMailClient
import org.kodein.di.compose.rememberInstance
import java.util.Locale

class SettingsScreen(
    override val navController: NavController,
    override val navBackStackEntry: NavBackStackEntry
) : Screen() {
    @Composable
    override fun Content() {
        val buildConfig by rememberInstance<BuildConfigInformation>()
        val context = LocalContext.current
        val localActivity = LocalActivity.current
        val demoModeObserver = localActivity as? DemoModeObserver
        val isDemomode = demoModeObserver?.isDemoMode() ?: false
        val profilesController = rememberProfileController()
        val profilesState by profilesController.getProfilesState()
        val listState = rememberLazyListState()

        Scaffold(
            modifier = Modifier
                .testTag(TestTag.Settings.SettingsScreen)
                .statusBarsPadding()
        ) { contentPadding ->
            SettingsScreenContent(
                contentPadding = contentPadding,
                listState = listState,
                onClickUnlockEgk = { unlockMethod ->
                    navController.navigate(
                        CardUnlockRoutes.CardUnlockIntroScreen.path(
                            unlockMethod = unlockMethod.name
                        )
                    )
                },
                onClickOrderHealthCard = {
                    navController.navigate(MainNavigationScreens.OrderHealthCard.path())
                },
                onClickAccessibilitySettings = {
                    navController.navigate(SettingsNavigationScreens.SettingsAccessibilityScreen.path())
                },
                onClickProductImprovementSettings = {
                    navController.navigate(SettingsNavigationScreens.SettingsProductImprovementsScreen.path())
                },
                onClickDeviceSecuritySettings = {
                    navController.navigate(SettingsNavigationScreens.SettingsDeviceSecurityScreen.path())
                },
                onClickLegalNotice = {
                    navController.navigate(SettingsNavigationScreens.SettingsLegalNoticeScreen.path())
                },
                onClickDataProtection = {
                    navController.navigate(SettingsNavigationScreens.SettingsDataProtectionScreen.path())
                },
                onClickOpenSourceLicences = {
                    navController.navigate(SettingsNavigationScreens.SettingsOpenSourceLicencesScreen.path())
                },
                onClickAdditionalLicences = {
                    navController.navigate(SettingsNavigationScreens.SettingsAdditionalLicencesScreen.path())
                },
                onClickTermsOfUse = {
                    navController.navigate(SettingsNavigationScreens.SettingsTermsOfUseScreen.path())
                },
                onClickDebug = {
                    navController.navigate(MainNavigationScreens.Debug.path())
                },
                onClickEditProfile = {
                    navController.navigate(ProfileRoutes.ProfileScreen.path(profileId = it))
                },
                onClickSample = {
                    navController.navigate(PharmacyRoutes.subGraphName())
                },
                onClickDemoTracking = {
                    navController.navigate(TrackingScreenRoutes.subGraphName())
                },
                isDemomode = isDemomode,
                localActivity = localActivity,
                buildConfig = buildConfig,
                profilesState = profilesState,
                context = context
            )
        }
    }
}

@Suppress("LongParameterList")
@Composable
private fun SettingsScreenContent(
    contentPadding: PaddingValues,
    listState: LazyListState,
    onClickUnlockEgk: (UnlockMethod) -> Unit,
    onClickOrderHealthCard: () -> Unit,
    onClickAccessibilitySettings: () -> Unit,
    onClickProductImprovementSettings: () -> Unit,
    onClickDeviceSecuritySettings: () -> Unit,
    onClickLegalNotice: () -> Unit,
    onClickDataProtection: () -> Unit,
    onClickOpenSourceLicences: () -> Unit,
    onClickAdditionalLicences: () -> Unit,
    onClickTermsOfUse: () -> Unit,
    onClickDebug: () -> Unit,
    onClickEditProfile: (ProfileIdentifier) -> Unit,
    onClickSample: () -> Unit,
    onClickDemoTracking: () -> Unit,
    profilesState: List<Profile>,
    isDemomode: Boolean,
    localActivity: ComponentActivity,
    buildConfig: BuildConfigInformation,
    context: Context
) {
    val snackbar = LocalSnackbar.current
    LazyColumn(
        modifier = Modifier.testTag("settings_screen"),
        contentPadding = contentPadding,
        state = listState
    ) {
        @Requirement(
            "O.Source_8",
            "O.Source_9",
            "O.Source_11",
            sourceSpecification = "BSI-eRp-ePA",
            rationale = "Debug options are not accessible in the production version. All other debug mechanisms, including logging, are disabled in the build pipeline." // ktlint-disable max-line-length
        )
        if (BuildConfigExtension.isInternalDebug) {
            item {
                DebugMenuSection(onClickDebug)
            }
        }
        item {
            ProfileSection(profilesState, onClickEditProfile)
            Divider(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = PaddingDefaults.Small)
            )
        }
        if (!isDemomode) {
            item {
                HealthCardSection(
                    onClickUnlockEgk = { unlockMethod ->
                        onClickUnlockEgk(unlockMethod)
                    },
                    onClickOrderHealthCard = {
                        onClickOrderHealthCard()
                    }
                )
                Divider(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = PaddingDefaults.Small)
                )
            }
        }
        item {
            GlobalSettingsSection(
                isDemomode = isDemomode,
                onClickAccessibilitySettings = {
                    onClickAccessibilitySettings()
                },
                onClickProductImprovementSettings = {
                    onClickProductImprovementSettings()
                },
                onClickDeviceSecuritySettings = {
                    onClickDeviceSecuritySettings()
                },
                onClickDemoModeEnd = {
                    DemoModeIntent.startAppWithNormalMode<MainActivity>(localActivity)
                },
                onClickDemoMode = {
                    DemoModeIntent.startAppWithDemoMode<MainActivity>(localActivity)
                }
            )
            Divider(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = PaddingDefaults.Small)
            )
        }
        item {
            ContactSection(
                darkMode = buildConfig.inDarkTheme(),
                language = buildConfig.language(),
                versionName = buildConfig.versionName(),
                nfcInfo = buildConfig.nfcInformation(context),
                phoneModel = buildConfig.model()
            )
            Divider(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = PaddingDefaults.Small)
            )
        }
        item {
            LegalSection(
                onClickLegalNotice,
                onClickDataProtection,
                onClickOpenSourceLicences,
                onClickAdditionalLicences,
                onClickTermsOfUse
            )
        }
        if (BuildConfigExtension.isInternalDebug) {
            item {
                Divider(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = PaddingDefaults.Small)
                )
            }
            item {
                Text(
                    text = "Debug section",
                    style = AppTheme.typography.h6,
                    modifier = Modifier.padding(
                        start = PaddingDefaults.Medium,
                        end = PaddingDefaults.Medium,
                        bottom = PaddingDefaults.Medium / 2,
                        top = PaddingDefaults.Medium
                    )
                )
            }
            item {
                LabelButton(
                    Icons.Outlined.TireRepair,
                    "Debug section",
                    modifier = Modifier.testTag("debug-section")
                ) {
                    snackbar.show("TODO: Debug section comes here")
                }
            }
            item {
                LabelButton(
                    Icons.Outlined.SettingsInputComposite,
                    "Ui Components",
                    modifier = Modifier.testTag("ui-components")
                ) {
                    onClickSample()
                }
            }
            item {
                LabelButton(
                    Icons.Outlined.ChecklistRtl,
                    "Tracking Debug",
                    modifier = Modifier.testTag("tracking-debug")
                ) {
                    onClickDemoTracking()
                }
            }
        }
        item {
            AboutSection(
                modifier = Modifier.padding(top = 76.dp),
                buildVersionName = buildConfig.versionName()
            )
        }
    }
}

@Composable
private fun GlobalSettingsSection(
    isDemomode: Boolean,
    onClickAccessibilitySettings: () -> Unit,
    onClickProductImprovementSettings: () -> Unit,
    onClickDeviceSecuritySettings: () -> Unit,
    onClickDemoModeEnd: () -> Unit,
    onClickDemoMode: () -> Unit

) {
    Column {
        Text(
            text = stringResource(R.string.settings_personal_settings_header),
            style = AppTheme.typography.h6,
            modifier = Modifier.padding(
                start = PaddingDefaults.Medium,
                end = PaddingDefaults.Medium,
                bottom = PaddingDefaults.Small,
                top = PaddingDefaults.Medium
            )
        )
        if (isDemomode) {
            LabelButton(
                icon = painterResource(R.drawable.magic_wand_filled),
                stringResource(R.string.demo_mode_settings_end_title)
            ) {
                onClickDemoModeEnd()
            }
        } else {
            LabelButton(
                icon = painterResource(R.drawable.magic_wand_filled),
                stringResource(R.string.demo_mode_settings_title)
            ) {
                onClickDemoMode()
            }
        }
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
private fun ProfileSection(
    profiles: List<Profile>,
    onClickEditProfile: (ProfileIdentifier) -> Unit
) {
    Column {
        Text(
            text = stringResource(R.string.settings_profiles_headline),
            style = AppTheme.typography.h6,
            modifier = Modifier
                .padding(
                    start = PaddingDefaults.Medium,
                    end = PaddingDefaults.Medium,
                    top = PaddingDefaults.Medium,
                    bottom = PaddingDefaults.Small
                )
                .testTag("Profiles")
        )

        profiles.forEach { profile ->
            ProfileCard(
                profile = profile,
                onClickEdit = { onClickEditProfile(profile.id) }
            )
        }
    }
    SpacerLarge()
}

@Composable
private fun ProfileCard(
    profile: Profile,
    onClickEdit: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(role = Role.Button) {
                onClickEdit()
            }
            .padding(horizontal = PaddingDefaults.Medium, vertical = PaddingDefaults.ShortMedium)
            .testTag(TestTag.Settings.ProfileButton),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Avatar(
            modifier = Modifier.size(48.dp),
            emptyIcon = Icons.Rounded.PersonOutline,
            profile = profile,
            ssoStatusColor = null,
            iconModifier = Modifier.size(20.dp)
        )
        SpacerMedium()
        Text(
            modifier = Modifier.weight(1f),
            text = profile.name,
            style = AppTheme.typography.body1
        )
        Icon(Icons.Outlined.KeyboardArrowRight, null, tint = AppTheme.colors.neutral400)
    }
}

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun ProfileNameDialog(
    initialProfileName: String = "",
    profileController: ProfileController,
    wantRemoveLastProfile: Boolean = false,
    onEdit: (text: String) -> Unit,
    onDismissRequest: () -> Unit
) {
    val profiles by profileController.getProfilesState()
    var textValue by remember { mutableStateOf(initialProfileName) }
    var duplicated by remember { mutableStateOf(false) }

    val title = if (wantRemoveLastProfile) {
        stringResource(R.string.profile_edit_name_for_default)
    } else {
        stringResource(R.string.profile_edit_name)
    }

    val infoText = if (wantRemoveLastProfile) {
        stringResource(R.string.profile_edit_name_for_default_info)
    } else if (initialProfileName.isNotEmpty()) {
        stringResource(R.string.profile_edit_name_for_rename_info)
    } else {
        stringResource(R.string.profile_edit_name_info)
    }

    AlertDialog(
        modifier = Modifier.testTag(TestTag.Settings.AddProfileDialog.Modal),
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
                Box(modifier = Modifier.padding(top = PaddingDefaults.ShortMedium)) {
                    OutlinedTextField(
                        modifier = Modifier.testTag(TestTag.Settings.AddProfileDialog.ProfileNameTextField),
                        value = textValue,
                        singleLine = true,
                        onValueChange = {
                            val isExistingProfileName = profiles.containsProfileWithName(textValue)
                            val isNotInitialProfileName = textValue.trim() != initialProfileName
                            textValue = it.trimStart().sanitizeProfileName()
                            duplicated = isNotInitialProfileName && isExistingProfileName && !wantRemoveLastProfile
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
            TextButton(
                modifier = Modifier.testTag(TestTag.Settings.AddProfileDialog.CancelButton),
                onClick = { onDismissRequest() }
            ) {
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
private fun HealthCardSection(
    onClickUnlockEgk: (unlockMethod: UnlockMethod) -> Unit,
    onClickOrderHealthCard: () -> Unit
) {
    Column {
        Text(
            text = stringResource(R.string.health_card_section_header),
            style = AppTheme.typography.h6,
            modifier = Modifier.padding(
                start = PaddingDefaults.Medium,
                end = PaddingDefaults.Medium,
                bottom = PaddingDefaults.Small,
                top = PaddingDefaults.Medium
            )
        )

        LabelButton(
            modifier = Modifier.testTag(TestTag.Settings.OrderNewCardButton),
            icon = painterResource(R.drawable.ic_order_egk),
            text = stringResource(R.string.health_card_section_order_card)
        ) {
            onClickOrderHealthCard()
        }

        LabelButton(
            Icons.Outlined.HelpOutline,
            stringResource(R.string.health_card_section_unlock_card_forgot_pin)
        ) {
            onClickUnlockEgk(UnlockMethod.ResetRetryCounterWithNewSecret)
        }

        LabelButton(
            painterResource(R.drawable.ic_reset_pin),
            stringResource(R.string.health_card_section_unlock_card_reset_pin)
        ) {
            onClickUnlockEgk(UnlockMethod.ChangeReferenceData)
        }

        LabelButton(
            Icons.Outlined.LockOpen,
            stringResource(R.string.health_card_section_unlock_card_no_reset)
        ) {
            onClickUnlockEgk(UnlockMethod.ResetRetryCounter)
        }
    }
}

@Composable
private fun DebugMenuSection(onClickDebug: () -> Unit) {
    OutlinedDebugButton(
        text = stringResource(id = R.string.debug_menu),
        onClick = { onClickDebug() },
        modifier = Modifier
            .fillMaxWidth()
            .padding(
                start = PaddingDefaults.Medium,
                end = PaddingDefaults.Medium,
                bottom = PaddingDefaults.Small,
                top = PaddingDefaults.Medium
            )
            .testTag(TestTag.Settings.DebugMenuButton)
    )
}

@Composable
private fun LegalSection(
    onClickLegalNotice: () -> Unit,
    onClickDataProtection: () -> Unit,
    onClickOpenSourceLicences: () -> Unit,
    onClickAdditionalLicences: () -> Unit,
    onClickTermsOfUse: () -> Unit
) {
    Column {
        Text(
            text = stringResource(R.string.settings_legal_headline),
            style = AppTheme.typography.h6,
            modifier = Modifier.padding(
                start = PaddingDefaults.Medium,
                end = PaddingDefaults.Medium,
                bottom = PaddingDefaults.Small,
                top = PaddingDefaults.Medium
            )
        )
        LabelButton(
            Icons.Outlined.Info,
            stringResource(R.string.settings_legal_imprint),
            modifier = Modifier.testTag("settings/imprint")
        ) {
            onClickLegalNotice()
        }
        @Requirement(
            "O.Arch_9",
            sourceSpecification = "BSI-eRp-ePA",
            rationale = "Display data protection within settings"
        )
        LabelButton(
            Icons.Outlined.PrivacyTip,
            stringResource(R.string.settings_legal_dataprotection),
            modifier = Modifier.testTag("settings/privacy")
        ) {
            onClickDataProtection()
        }
        LabelButton(
            Icons.Outlined.Wysiwyg,
            stringResource(R.string.settings_legal_tos),
            modifier = Modifier.testTag("settings/tos")
        ) {
            onClickTermsOfUse()
        }
        LabelButton(
            Icons.Outlined.Code,
            stringResource(R.string.settings_legal_licences),
            modifier = Modifier.testTag("settings/licences")
        ) {
            onClickOpenSourceLicences()
        }
        LabelButton(
            Icons.Outlined.Source,
            stringResource(R.string.settings_licence_pharmacy_search),
            modifier = Modifier.testTag("settings/additional_licences")
        ) {
            onClickAdditionalLicences()
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
fun LabelButton(
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
private fun AboutSection(
    modifier: Modifier,
    buildVersionName: String
) {
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
                SpacerTiny()
                Text(
                    stringResource(R.string.about_version, buildVersionName)
                )
            }
            SpacerTiny()
            Text(
                stringResource(R.string.about_buildhash, BuildKonfig.GIT_HASH)
            )
        }
    }
}

@Composable
private fun ContactSection(
    darkMode: String,
    versionName: String,
    language: String,
    phoneModel: String,
    nfcInfo: String
) {
    val context = LocalContext.current
    val contactHeader = stringResource(R.string.settings_contact_headline)

    Column {
        val phoneNumber = stringResource(R.string.settings_contact_hotline_number)
        val mailAddress = stringResource(R.string.settings_contact_mail_address)
        val subject = stringResource(R.string.settings_feedback_mail_subject)
        val body = buildFeedbackBodyWithDeviceInfo(
            darkMode = darkMode,
            versionName = versionName,
            language = language,
            phoneModel = phoneModel,
            nfcInfo = nfcInfo
        )
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
