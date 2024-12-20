/*
 * Copyright 2024, gematik GmbH
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

package de.gematik.ti.erp.app.settings.ui.screens

import android.content.Context
import android.content.Intent
import android.os.Build
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material.Divider
import androidx.compose.material.Icon
import androidx.compose.material.LocalContentColor
import androidx.compose.material.LocalTextStyle
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.HelpOutline
import androidx.compose.material.icons.automirrored.outlined.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.outlined.Wysiwyg
import androidx.compose.material.icons.outlined.AllInclusive
import androidx.compose.material.icons.outlined.Code
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.LockOpen
import androidx.compose.material.icons.outlined.Mail
import androidx.compose.material.icons.outlined.People
import androidx.compose.material.icons.outlined.Poll
import androidx.compose.material.icons.outlined.PrivacyTip
import androidx.compose.material.icons.outlined.Source
import androidx.compose.material.icons.outlined.TireRepair
import androidx.compose.material.icons.outlined.TrackChanges
import androidx.compose.material.icons.rounded.PersonOutline
import androidx.compose.material.icons.rounded.Phone
import androidx.compose.material.icons.rounded.PhoneAndroid
import androidx.compose.material.icons.rounded.VolunteerActivism
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavController
import de.gematik.ti.erp.app.BuildKonfig
import de.gematik.ti.erp.app.MainActivity
import de.gematik.ti.erp.app.Requirement
import de.gematik.ti.erp.app.TestTag
import de.gematik.ti.erp.app.analytics.navigation.TrackingScreenRoutes
import de.gematik.ti.erp.app.base.BaseActivity
import de.gematik.ti.erp.app.card.model.command.UnlockMethod
import de.gematik.ti.erp.app.cardunlock.navigation.CardUnlockRoutes
import de.gematik.ti.erp.app.core.LocalActivity
import de.gematik.ti.erp.app.debugsettings.navigation.ShowcaseScreensRoutes
import de.gematik.ti.erp.app.demomode.DemoModeIntent
import de.gematik.ti.erp.app.demomode.DemoModeObserver
import de.gematik.ti.erp.app.demomode.startAppWithDemoMode
import de.gematik.ti.erp.app.demomode.startAppWithNormalMode
import de.gematik.ti.erp.app.features.R
import de.gematik.ti.erp.app.info.BuildConfigInformation
import de.gematik.ti.erp.app.mainscreen.navigation.MainNavigationScreens
import de.gematik.ti.erp.app.medicationplan.navigation.MedicationPlanRoutes
import de.gematik.ti.erp.app.navigation.Screen
import de.gematik.ti.erp.app.orderhealthcard.navigation.OrderHealthCardRoutes
import de.gematik.ti.erp.app.profiles.navigation.ProfileRoutes
import de.gematik.ti.erp.app.profiles.repository.ProfileIdentifier
import de.gematik.ti.erp.app.profiles.ui.components.Avatar
import de.gematik.ti.erp.app.profiles.usecase.model.ProfilesUseCaseData.Profile
import de.gematik.ti.erp.app.settings.model.DebugClickActions
import de.gematik.ti.erp.app.settings.model.HealthCardClickActions
import de.gematik.ti.erp.app.settings.model.LegalClickActions
import de.gematik.ti.erp.app.settings.model.SettingsActions
import de.gematik.ti.erp.app.settings.navigation.SettingsNavigationScreens
import de.gematik.ti.erp.app.settings.presentation.SettingStatesData
import de.gematik.ti.erp.app.settings.presentation.rememberSettingsController
import de.gematik.ti.erp.app.settings.ui.components.AllowScreenshotDialogWithListener
import de.gematik.ti.erp.app.settings.ui.components.GlobalSettingsSection
import de.gematik.ti.erp.app.settings.ui.components.OrganDonationRegisterDialog
import de.gematik.ti.erp.app.settings.ui.preview.LocalIsPreviewMode
import de.gematik.ti.erp.app.settings.ui.preview.SettingsScreenPreviewData
import de.gematik.ti.erp.app.settings.ui.preview.SettingsScreenPreviewProvider
import de.gematik.ti.erp.app.theme.AppTheme
import de.gematik.ti.erp.app.theme.PaddingDefaults
import de.gematik.ti.erp.app.theme.SizeDefaults
import de.gematik.ti.erp.app.utils.SpacerLarge
import de.gematik.ti.erp.app.utils.SpacerMedium
import de.gematik.ti.erp.app.utils.SpacerSmall
import de.gematik.ti.erp.app.utils.SpacerTiny
import de.gematik.ti.erp.app.utils.buildFeedbackBodyWithDeviceInfo
import de.gematik.ti.erp.app.utils.compose.ComposableEvent
import de.gematik.ti.erp.app.utils.compose.LabelButton
import de.gematik.ti.erp.app.utils.compose.LightDarkPreview
import de.gematik.ti.erp.app.utils.compose.OutlinedDebugButton
import de.gematik.ti.erp.app.utils.compose.handleIntent
import de.gematik.ti.erp.app.utils.compose.preview.PreviewAppTheme
import de.gematik.ti.erp.app.utils.compose.providePhoneIntent
import de.gematik.ti.erp.app.utils.compose.provideWebIntent
import de.gematik.ti.erp.app.utils.extensions.BuildConfigExtension
import de.gematik.ti.erp.app.utils.extensions.LocalDialog
import de.gematik.ti.erp.app.utils.openMailClient
import org.kodein.di.compose.rememberInstance

class SettingsScreen(
    override val navController: NavController,
    override val navBackStackEntry: NavBackStackEntry
) : Screen() {
    @Composable
    override fun Content() {
        val buildConfig by rememberInstance<BuildConfigInformation>()
        val organDonationDialogEvent = ComposableEvent<Unit>()
        val context = LocalContext.current
        val dialog = LocalDialog.current
        val localActivity = LocalActivity.current as? ComponentActivity
        val demoModeObserver = localActivity as? DemoModeObserver
        val isDemoMode = demoModeObserver?.isDemoMode() ?: false
        val settingsController = rememberSettingsController()

        val isMedicationPlanEnabled by settingsController.isMedicationPlanEnabled.collectAsStateWithLifecycle()

        AllowScreenshotDialogWithListener(
            dialog = dialog,
            event = settingsController.allowScreenshotsEvent,
            onAllowScreenshots = {
                settingsController.confirmAllowScreenshots()
            }
        )

        settingsController.intentEvent.listen {
            context.handleIntent(provideWebIntent(it))
        }

        OrganDonationRegisterDialog(
            dialog = dialog,
            event = organDonationDialogEvent,
            createIntent = { settingsController.createOrganDonationRegisterIntent() }
        )

        val settingsActions = SettingsActions(
            healthCardClickActions = HealthCardClickActions(
                onClickUnlockEgk = { unlockMethod ->
                    navController.navigate(
                        CardUnlockRoutes.CardUnlockIntroScreen.path(unlockMethod = unlockMethod.name)
                    )
                },
                onClickOrderHealthCard = {
                    navController.navigate(OrderHealthCardRoutes.OrderHealthCardSelectInsuranceCompanyScreen.path())
                }
            ),
            legalClickActions = LegalClickActions(
                onClickLegalNotice = { navController.navigate(SettingsNavigationScreens.SettingsLegalNoticeScreen.path()) },
                onClickDataProtection = {
                    navController.navigate(SettingsNavigationScreens.SettingsDataProtectionScreen.path())
                },
                onClickOpenSourceLicences = {
                    navController.navigate(SettingsNavigationScreens.SettingsOpenSourceLicencesScreen.path())
                },
                onClickAdditionalLicences = {
                    navController.navigate(SettingsNavigationScreens.SettingsAdditionalLicencesScreen.path())
                },
                onClickTermsOfUse = { navController.navigate(SettingsNavigationScreens.SettingsTermsOfUseScreen.path()) }
            ),
            debugClickActions = DebugClickActions(
                onClickDebug = { navController.navigate(MainNavigationScreens.Debug.path()) },
                onClickBottomSheetShowcase = {
                    navController.navigate(ShowcaseScreensRoutes.BottomSheetShowcaseScreen.path())
                },
                onClickDemoTracking = { navController.navigate(TrackingScreenRoutes.subGraphName()) }
            ),
            onEnableZoom = settingsController::onEnableZoom,
            onDisableZoom = settingsController::onDisableZoom,
            onAllowScreenshots = { allow ->
                settingsController.onAllowScreenshots(allow)
            },
            onClickMedicationPlan = {
                navController.navigate(MedicationPlanRoutes.MedicationPlanList.path())
            },
            onClickProductImprovementSettings = {
                navController.navigate(SettingsNavigationScreens.SettingsProductImprovementsScreen.path())
            },
            onClickDeviceSecuritySettings = {
                navController.navigate(SettingsNavigationScreens.SettingsAppSecurityScreen.path())
            },
            onClickLanguageSettings = {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    navController.navigate(SettingsNavigationScreens.SettingsLanguageScreen.path())
                } else {
                    val intent = Intent(Settings.ACTION_LOCALE_SETTINGS).apply { flags = Intent.FLAG_ACTIVITY_NEW_TASK }
                    context.startActivity(intent)
                }
            },
            onClickDemoModeEnd = {
                localActivity?.let { DemoModeIntent.startAppWithNormalMode<MainActivity>(it) }
            },
            onClickDemoMode = {
                localActivity?.let { DemoModeIntent.startAppWithDemoMode<MainActivity>(it) }
            },
            onClickEditProfile = { profileId ->
                navController.navigate(ProfileRoutes.ProfileScreen.path(profileId = profileId))
            },

            onClickOrganDonationRegister = {
                organDonationDialogEvent.trigger(Unit)
            }
        )

        SettingsScreenScaffold(
            listState = rememberLazyListState(),
            isDemoMode = isDemoMode,
            localActivity = localActivity,
            buildConfig = buildConfig,
            profilesState = settingsController.profiles.collectAsStateWithLifecycle().value,
            context = context,
            zoomState = settingsController.zoomState.collectAsStateWithLifecycle(),
            screenShotsState = settingsController.screenShotsState.collectAsStateWithLifecycle(),
            isMedicationPlanEnabled = isMedicationPlanEnabled,
            settingsActions = settingsActions
        )
    }
}

@Composable
private fun SettingsScreenScaffold(
    listState: LazyListState,
    isDemoMode: Boolean,
    localActivity: ComponentActivity?,
    buildConfig: BuildConfigInformation,
    profilesState: List<Profile>,
    context: Context,
    zoomState: State<SettingStatesData.ZoomState>,
    screenShotsState: State<Boolean>,
    settingsActions: SettingsActions,
    isMedicationPlanEnabled: Boolean
) {
    val padding = (localActivity as? BaseActivity)?.applicationInnerPadding

    Scaffold(
        modifier = Modifier
            .testTag(TestTag.Settings.SettingsScreen)
            .statusBarsPadding()
    ) { contentPadding ->
        SettingsScreenContent(
            contentPadding = padding?.combineWithInnerScaffold(contentPadding) ?: PaddingValues(),
            profilesState = profilesState,
            listState = listState,
            isDemoMode = isDemoMode,
            buildConfig = buildConfig,
            context = context,
            isMedicationPlanEnabled = isMedicationPlanEnabled,
            zoomState = zoomState,
            screenShotsState = screenShotsState,
            settingsActions = settingsActions
        )
    }
}

@Composable
private fun SettingsScreenContent(
    contentPadding: PaddingValues,
    profilesState: List<Profile>,
    listState: LazyListState,
    isDemoMode: Boolean,
    buildConfig: BuildConfigInformation,
    context: Context,
    zoomState: State<SettingStatesData.ZoomState>,
    isMedicationPlanEnabled: Boolean,
    screenShotsState: State<Boolean>,
    settingsActions: SettingsActions

) {
    LazyColumn(
        modifier = Modifier.testTag("settings_screen"),
        contentPadding = contentPadding,
        state = listState
    ) {
        @Requirement(
            "O.Source_8#1",
            sourceSpecification = "BSI-eRp-ePA",
            rationale = "Debug menu is shown only for debug builds."
        )
        if (BuildConfigExtension.isInternalDebug) {
            item { DebugMenuSection(settingsActions.debugClickActions.onClickDebug) }
        }

        item {
            ProfileSection(profilesState, settingsActions.onClickEditProfile)
            Divider(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = PaddingDefaults.Small)
            )
        }

        if (!isDemoMode) {
            item {
                HealthCardSection(settingsActions.healthCardClickActions)
                Divider(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = PaddingDefaults.Small)
                )
            }
        }

        item {
            GlobalSettingsSection(
                isDemoMode = isDemoMode,
                zoomState = zoomState,
                screenShotState = screenShotsState,
                onEnableZoom = settingsActions.onEnableZoom,
                onDisableZoom = settingsActions.onDisableZoom,
                isMedicationPlanEnabled = isMedicationPlanEnabled,
                onAllowScreenshots = { settingsActions.onAllowScreenshots(true) },
                onDisallowScreenshots = { settingsActions.onAllowScreenshots(false) },
                settingsActions = settingsActions,
                onClickMedicationPlan = settingsActions.onClickMedicationPlan
            )
            Divider(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = PaddingDefaults.Small)
            )
        }

        item {
            ExploreSection(
                onClickOrganDonationRegister = {
                    settingsActions.onClickOrganDonationRegister()
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
            LegalSection(settingsActions.legalClickActions)
        }

        @Requirement(
            "O.Source_8#2",
            sourceSpecification = "BSI-eRp-ePA",
            rationale = "Debug menu is shown only for debug builds."
        )
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
                    text = "Debug settings",
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
                    settingsActions.debugClickActions.onClickDebug()
                }
            }
            item {
                LabelButton(
                    Icons.Outlined.AllInclusive,
                    "Bottom sheet showcase"
                ) {
                    settingsActions.debugClickActions.onClickBottomSheetShowcase()
                }
            }
            item {
                Text(
                    text = "Local Tracking",
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
                    Icons.Outlined.TrackChanges,
                    "Tracking Debug",
                    modifier = Modifier.testTag("tracking-debug")
                ) {
                    settingsActions.debugClickActions.onClickDemoTracking()
                }
            }
        }

        item {
            AboutSection(modifier = Modifier.padding(top = 76.dp), buildVersionName = buildConfig.versionName())
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
            modifier = Modifier.size(SizeDefaults.sixfold),
            emptyIcon = Icons.Rounded.PersonOutline,
            profile = profile,
            iconModifier = Modifier.size(SizeDefaults.doubleHalf)
        )
        SpacerMedium()
        Text(
            modifier = Modifier.weight(1f),
            text = profile.name,
            style = AppTheme.typography.body1
        )
        Icon(Icons.AutoMirrored.Outlined.KeyboardArrowRight, null, tint = AppTheme.colors.neutral400)
    }
}

@Composable
private fun HealthCardSection(
    healthCardClickActions: HealthCardClickActions
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
            healthCardClickActions.onClickOrderHealthCard()
        }

        LabelButton(
            Icons.AutoMirrored.Outlined.HelpOutline,
            stringResource(R.string.health_card_section_unlock_card_forgot_pin)
        ) {
            healthCardClickActions.onClickUnlockEgk(UnlockMethod.ResetRetryCounterWithNewSecret)
        }

        LabelButton(
            painterResource(R.drawable.ic_reset_pin),
            stringResource(R.string.health_card_section_unlock_card_reset_pin)
        ) {
            healthCardClickActions.onClickUnlockEgk(UnlockMethod.ChangeReferenceData)
        }

        LabelButton(
            Icons.Outlined.LockOpen,
            stringResource(R.string.health_card_section_unlock_card_no_reset)
        ) {
            healthCardClickActions.onClickUnlockEgk(UnlockMethod.ResetRetryCounter)
        }
    }
}

@Composable
private fun DebugMenuSection(onClickDebug: () -> Unit) {
    if (!LocalIsPreviewMode.current) {
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
}

@Composable
private fun LegalSection(
    legalClickActions: LegalClickActions
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
            legalClickActions.onClickLegalNotice()
        }
        @Requirement(
            "O.Arch_9#3",
            sourceSpecification = "BSI-eRp-ePA",
            rationale = "Display data protection within settings"
        )
        LabelButton(
            Icons.Outlined.PrivacyTip,
            stringResource(R.string.settings_legal_dataprotection),
            modifier = Modifier.testTag("settings/privacy")
        ) {
            legalClickActions.onClickDataProtection()
        }
        LabelButton(
            Icons.AutoMirrored.Outlined.Wysiwyg,
            stringResource(R.string.settings_legal_tos),
            modifier = Modifier.testTag("settings/tos")
        ) {
            legalClickActions.onClickTermsOfUse()
        }
        LabelButton(
            Icons.Outlined.Code,
            stringResource(R.string.settings_legal_licences),
            modifier = Modifier.testTag("settings/licences")
        ) {
            legalClickActions.onClickOpenSourceLicences()
        }
        LabelButton(
            Icons.Outlined.Source,
            stringResource(R.string.settings_licence_pharmacy_search),
            modifier = Modifier.testTag("settings/additional_licences")
        ) {
            legalClickActions.onClickAdditionalLicences()
        }
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
                Icon(Icons.Rounded.PhoneAndroid, null, modifier = Modifier.size(SizeDefaults.double))
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
fun ExploreSection(onClickOrganDonationRegister: () -> Unit) {
    val context = LocalContext.current
    SpacerMedium()
    Text(
        text = stringResource(R.string.settings_explore_headline),
        style = AppTheme.typography.h6,
        modifier = Modifier.padding(horizontal = PaddingDefaults.Medium)
    )
    SpacerSmall()
    LabelButton(
        icon = Icons.Rounded.VolunteerActivism,
        text = stringResource(R.string.organ_donation_menu_entry),
        onClick = { onClickOrganDonationRegister() }
    )
    SpacerSmall()
    val communityAddress = stringResource(R.string.settings_contact_community_address)
    LabelButton(
        icon = Icons.Outlined.People,
        text = stringResource(R.string.settings_contact_community_label),
        onClick = {
            context.handleIntent(provideWebIntent(communityAddress))
        }
    )
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
        val surveyAddress = stringResource(R.string.settings_contact_survey_address)
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
            icon = Icons.Outlined.Poll,
            text = stringResource(R.string.settings_contact_feedback),
            onClick = {
                context.handleIntent(provideWebIntent(surveyAddress))
            }
        )
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
        SpacerMedium()
    }
}

@LightDarkPreview
@Composable
fun SettingsScreenPreview(
    @PreviewParameter(SettingsScreenPreviewProvider::class) previewData: SettingsScreenPreviewData
) {
    val lazyListState = rememberLazyListState()

    CompositionLocalProvider(LocalIsPreviewMode provides true) {
        PreviewAppTheme {
            val settingsActions = SettingsActions(
                healthCardClickActions = HealthCardClickActions(
                    onClickUnlockEgk = {},
                    onClickOrderHealthCard = {}
                ),
                legalClickActions = LegalClickActions(
                    onClickLegalNotice = {},
                    onClickDataProtection = {},
                    onClickOpenSourceLicences = {},
                    onClickAdditionalLicences = {},
                    onClickTermsOfUse = {}
                ),
                debugClickActions = DebugClickActions(
                    onClickDebug = {},
                    onClickBottomSheetShowcase = {},
                    onClickDemoTracking = {}
                ),
                onEnableZoom = { previewData.zoomState.value = SettingStatesData.ZoomState(zoomEnabled = true) },
                onDisableZoom = { previewData.zoomState.value = SettingStatesData.ZoomState(zoomEnabled = false) },
                onAllowScreenshots = { previewData.screenShotsState.value = it },
                onClickProductImprovementSettings = {},
                onClickDeviceSecuritySettings = {},
                onClickLanguageSettings = {},
                onClickDemoModeEnd = {},
                onClickDemoMode = {},
                onClickEditProfile = {},
                onClickMedicationPlan = {},
                onClickOrganDonationRegister = { }
            )

            SettingsScreenContent(
                contentPadding = PaddingValues(SizeDefaults.zero),
                profilesState = previewData.profiles,
                listState = lazyListState,
                isDemoMode = false,
                buildConfig = previewData.buildConfig,
                context = LocalContext.current,
                zoomState = previewData.zoomState,
                screenShotsState = previewData.screenShotsState,
                settingsActions = settingsActions,
                isMedicationPlanEnabled = false
            )
        }
    }
}
