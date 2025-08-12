/*
 * Copyright (Change Date see Readme), gematik GmbH
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
 * In case of changes by gematik GmbH find details in the "Readme" file.
 *
 * See the Licence for the specific language governing permissions and limitations under the Licence.
 *
 * *******
 *
 * For additional notes and disclaimer from gematik and in case of changes by gematik find details in the "Readme" file.
 */

package de.gematik.ti.erp.app.settings.ui.screens

import android.content.Intent
import android.os.Build
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.ScaffoldState
import androidx.compose.material.rememberScaffoldState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavController
import de.gematik.ti.erp.app.MainActivity
import de.gematik.ti.erp.app.Requirement
import de.gematik.ti.erp.app.TestTag
import de.gematik.ti.erp.app.analytics.navigation.TrackingScreenRoutes
import de.gematik.ti.erp.app.core.R
import de.gematik.ti.erp.app.base.BaseActivity
import de.gematik.ti.erp.app.cardunlock.navigation.CardUnlockRoutes
import de.gematik.ti.erp.app.core.LocalActivity
import de.gematik.ti.erp.app.debugsettings.navigation.ShowcaseScreensRoutes
import de.gematik.ti.erp.app.demomode.DemoModeIntent
import de.gematik.ti.erp.app.demomode.DemoModeObserver
import de.gematik.ti.erp.app.demomode.startAppWithDemoMode
import de.gematik.ti.erp.app.demomode.startAppWithNormalMode
import de.gematik.ti.erp.app.info.BuildConfigInformation
import de.gematik.ti.erp.app.mainscreen.navigation.MainNavigationScreens
import de.gematik.ti.erp.app.medicationplan.navigation.MedicationPlanRoutes
import de.gematik.ti.erp.app.navigation.Screen
import de.gematik.ti.erp.app.orderhealthcard.navigation.OrderHealthCardRoutes
import de.gematik.ti.erp.app.profiles.navigation.ProfileRoutes
import de.gematik.ti.erp.app.profiles.usecase.model.ProfilesUseCaseData.Profile
import de.gematik.ti.erp.app.settings.model.ContactClickActions
import de.gematik.ti.erp.app.settings.model.DebugClickActions
import de.gematik.ti.erp.app.settings.model.ExploreClickActions
import de.gematik.ti.erp.app.settings.model.HealthCardClickActions
import de.gematik.ti.erp.app.settings.model.LegalClickActions
import de.gematik.ti.erp.app.settings.model.PersonalSettingsClickActions
import de.gematik.ti.erp.app.settings.model.SettingsActions
import de.gematik.ti.erp.app.settings.navigation.SettingsRoutes
import de.gematik.ti.erp.app.settings.presentation.SettingStatesData
import de.gematik.ti.erp.app.settings.presentation.rememberSettingsController
import de.gematik.ti.erp.app.settings.ui.components.AboutSection
import de.gematik.ti.erp.app.settings.ui.components.AllowScreenshotDialogWithListener
import de.gematik.ti.erp.app.settings.ui.components.ContactSection
import de.gematik.ti.erp.app.settings.ui.components.DebugSection
import de.gematik.ti.erp.app.settings.ui.components.ExploreSection
import de.gematik.ti.erp.app.settings.ui.components.HealthCardSection
import de.gematik.ti.erp.app.settings.ui.components.LegalSection
import de.gematik.ti.erp.app.settings.ui.components.OrganDonationRegisterDialog
import de.gematik.ti.erp.app.settings.ui.components.PersonalSettingsSection
import de.gematik.ti.erp.app.settings.ui.components.ProfileSection
import de.gematik.ti.erp.app.settings.ui.preview.LocalIsPreviewMode
import de.gematik.ti.erp.app.settings.ui.preview.SettingsScreenPreviewData
import de.gematik.ti.erp.app.settings.ui.preview.SettingsScreenPreviewProvider
import de.gematik.ti.erp.app.theme.SizeDefaults
import de.gematik.ti.erp.app.translation.navigation.TranslationRoutes
import de.gematik.ti.erp.app.utils.SpacerLarge
import de.gematik.ti.erp.app.utils.buildFeedbackBodyWithDeviceInfo
import de.gematik.ti.erp.app.utils.compose.AnimatedElevationScaffold
import de.gematik.ti.erp.app.utils.compose.ComposableEvent
import de.gematik.ti.erp.app.utils.compose.LightDarkLongPreview
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
        val listState = rememberLazyListState()
        val scaffoldState = rememberScaffoldState()
        val hasValidDigas by settingsController.hasValidDigas.collectAsStateWithLifecycle()

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

        val healthPortalUrl = stringResource(id = R.string.pres_detail_health_portal_description_url)
        val communityAddress = stringResource(R.string.settings_contact_community_address)
        val phoneNumber = stringResource(R.string.settings_contact_hotline_number)
        val mailAddress = stringResource(R.string.settings_contact_mail_address)
        val subject = stringResource(R.string.settings_feedback_mail_subject)
        val surveyAddress = stringResource(R.string.settings_contact_survey_address)
        val digaSurveyAddress = stringResource(R.string.diga_settings_feedback_address)
        val accessibilityStatementAddress = stringResource(R.string.settings_accessibility_statement_address)
        val body = buildFeedbackBodyWithDeviceInfo(
            darkMode = buildConfig.inDarkTheme(),
            versionName = buildConfig.versionName(),
            language = buildConfig.language(),
            phoneModel = buildConfig.model(),
            nfcInfo = buildConfig.nfcInformation(context)
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
            personalSettingsClickActions = PersonalSettingsClickActions(
                onToggleEnableZoom = { enabled ->
                    settingsController.onToggleEnableZoom(enabled)
                },
                onToggleScreenshots = { allow ->
                    settingsController.onAllowScreenshots(allow)
                },
                onClickLanguageSettings = {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        navController.navigate(SettingsRoutes.SettingsLanguageScreen.path())
                    } else {
                        val intent = Intent(Settings.ACTION_LOCALE_SETTINGS).apply { flags = Intent.FLAG_ACTIVITY_NEW_TASK }
                        context.startActivity(intent)
                    }
                },
                onClickProductImprovementSettings = {
                    navController.navigate(SettingsRoutes.SettingsProductImprovementsScreen.path())
                },
                onClickDeviceSecuritySettings = {
                    navController.navigate(SettingsRoutes.SettingsAppSecurityScreen.path())
                },
                onClickMedicationPlan = {
                    navController.navigate(MedicationPlanRoutes.MedicationPlanScheduleListScreen.path())
                }
            ),
            exploreClickActions = ExploreClickActions(
                onClickForum = {
                    context.handleIntent(provideWebIntent(communityAddress))
                },
                onToggleDemoMode = {
                    if (isDemoMode) {
                        localActivity?.let { DemoModeIntent.startAppWithNormalMode<MainActivity>(it) }
                    } else {
                        localActivity?.let { DemoModeIntent.startAppWithDemoMode<MainActivity>(it) }
                    }
                },
                onClickGesundBund = {
                    context.handleIntent(provideWebIntent(healthPortalUrl))
                },
                onClickOrganDonationRegister = {
                    organDonationDialogEvent.trigger(Unit)
                }
            ),
            contactClickActions = ContactClickActions(
                onClickCall = { context.handleIntent(providePhoneIntent(phoneNumber)) },
                onClickMail = {
                    openMailClient(context, mailAddress, body, subject)
                },
                onClickPoll = {
                    context.handleIntent(provideWebIntent(surveyAddress))
                },
                onClickDigaPoll = {
                    context.handleIntent(provideWebIntent(digaSurveyAddress))
                }
            ),
            legalClickActions = LegalClickActions(
                onClickLegalNotice = { navController.navigate(SettingsRoutes.SettingsLegalNoticeScreen.path()) },
                onClickDataProtection = {
                    navController.navigate(SettingsRoutes.SettingsDataProtectionScreen.path())
                },
                onClickOpenSourceLicences = {
                    navController.navigate(SettingsRoutes.SettingsOpenSourceLicencesScreen.path())
                },
                onClickAdditionalLicences = {
                    navController.navigate(SettingsRoutes.SettingsAdditionalLicencesScreen.path())
                },
                onClickTermsOfUse = {
                    navController.navigate(SettingsRoutes.SettingsTermsOfUseScreen.path())
                },
                onClickAccessibilityStatement = {
                    context.handleIntent(provideWebIntent(accessibilityStatementAddress))
                }
            ),
            debugClickActions = DebugClickActions(
                onClickDebug = { navController.navigate(MainNavigationScreens.Debug.path()) },
                onClickBottomSheetShowcase = {
                    navController.navigate(ShowcaseScreensRoutes.BottomSheetShowcaseScreen.path())
                },
                onClickTranslation = {
                    navController.navigate(TranslationRoutes.TranslationSettingsScreen.path())
                },
                onClickDemoTracking = { navController.navigate(TrackingScreenRoutes.subGraphName()) }
            ),
            onClickEditProfile = { profileId ->
                navController.navigate(ProfileRoutes.ProfileScreen.path(profileId = profileId))
            }
        )

        SettingsScreenScaffold(
            listState = listState,
            scaffoldState = scaffoldState,
            isDemoMode = isDemoMode,
            localActivity = localActivity,
            buildConfig = buildConfig,
            profilesState = settingsController.profiles.collectAsStateWithLifecycle().value,
            zoomState = settingsController.zoomState.collectAsStateWithLifecycle(),
            screenShotsState = settingsController.screenShotsState.collectAsStateWithLifecycle(),
            settingsActions = settingsActions,
            hasValidDigas = hasValidDigas
        )
    }
}

@Composable
private fun SettingsScreenScaffold(
    listState: LazyListState,
    scaffoldState: ScaffoldState,
    isDemoMode: Boolean,
    localActivity: ComponentActivity?,
    buildConfig: BuildConfigInformation,
    profilesState: List<Profile>,
    zoomState: State<SettingStatesData.ZoomState>,
    screenShotsState: State<Boolean>,
    settingsActions: SettingsActions,
    hasValidDigas: Boolean
) {
    val padding = (localActivity as? BaseActivity)?.applicationInnerPadding

    AnimatedElevationScaffold(
        topBarTitle = stringResource(R.string.main_settings_acc),
        listState = listState,
        scaffoldState = scaffoldState,
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
            zoomState = zoomState,
            screenShotsState = screenShotsState,
            settingsActions = settingsActions,
            hasValidDigas = hasValidDigas
        )
    }
}

@Composable
private fun SettingsScreenContent(
    contentPadding: PaddingValues,
    profilesState: List<Profile>,
    listState: LazyListState,
    isDemoMode: Boolean,
    isDebug: Boolean = BuildConfigExtension.isInternalDebug,
    buildConfig: BuildConfigInformation,
    zoomState: State<SettingStatesData.ZoomState>,
    screenShotsState: State<Boolean>,
    settingsActions: SettingsActions,
    hasValidDigas: Boolean
) {
    LazyColumn(
        modifier = Modifier.testTag("settings_screen"),
        contentPadding = contentPadding,
        state = listState
    ) {
        @Requirement(
            "O.Source_8#2",
            sourceSpecification = "BSI-eRp-ePA",
            rationale = "Debug menu is shown only for debug builds."
        )
        if (isDebug) {
            item {
                DebugSection(debugClickActions = settingsActions.debugClickActions)
                SpacerLarge()
            }
        }

        item {
            ProfileSection(profilesState, settingsActions.onClickEditProfile)
            SpacerLarge()
        }

        if (!isDemoMode) {
            item {
                HealthCardSection(settingsActions.healthCardClickActions)
                SpacerLarge()
            }
        }

        item {
            PersonalSettingsSection(
                zoomState = zoomState,
                screenShotState = screenShotsState,
                isDemoMode = isDemoMode,
                personalSettingsClickActions = settingsActions.personalSettingsClickActions
            )
            SpacerLarge()
        }

        item {
            ExploreSection(
                isDemoMode = isDemoMode,
                exploreClickActions = settingsActions.exploreClickActions
            )
            SpacerLarge()
        }

        item {
            ContactSection(
                contactClickActions = settingsActions.contactClickActions,
                hasValidDigas = hasValidDigas
            )
            SpacerLarge()
        }

        item {
            LegalSection(settingsActions.legalClickActions)
            SpacerLarge()
        }

        item {
            AboutSection(buildVersionName = buildConfig.versionName())
            SpacerLarge()
        }
    }
}

@LightDarkLongPreview
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
                personalSettingsClickActions = PersonalSettingsClickActions(
                    onToggleEnableZoom = {},
                    onToggleScreenshots = {},
                    onClickLanguageSettings = {},
                    onClickProductImprovementSettings = {},
                    onClickDeviceSecuritySettings = {},
                    onClickMedicationPlan = {}
                ),
                exploreClickActions = ExploreClickActions(
                    onClickForum = {},
                    onToggleDemoMode = {},
                    onClickGesundBund = {},
                    onClickOrganDonationRegister = {}
                ),
                contactClickActions = ContactClickActions(
                    onClickCall = {},
                    onClickMail = {},
                    onClickPoll = {},
                    onClickDigaPoll = {}
                ),
                legalClickActions = LegalClickActions(
                    onClickLegalNotice = {},
                    onClickDataProtection = {},
                    onClickOpenSourceLicences = {},
                    onClickAdditionalLicences = {},
                    onClickTermsOfUse = {},
                    onClickAccessibilityStatement = {}
                ),
                debugClickActions = DebugClickActions(
                    onClickDebug = {},
                    onClickBottomSheetShowcase = {},
                    onClickDemoTracking = {},
                    onClickTranslation = {}
                ),
                onClickEditProfile = {}
            )

            SettingsScreenContent(
                contentPadding = PaddingValues(SizeDefaults.zero),
                profilesState = previewData.profiles,
                listState = lazyListState,
                isDebug = false,
                isDemoMode = false,
                buildConfig = previewData.buildConfig,
                zoomState = previewData.zoomState,
                screenShotsState = previewData.screenShotsState,
                settingsActions = settingsActions,
                hasValidDigas = false
            )
        }
    }
}
