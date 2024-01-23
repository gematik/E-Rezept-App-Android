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

package de.gematik.ti.erp.app.onboarding.ui

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Icon
import androidx.compose.material.Switch
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.FlashOn
import androidx.compose.material.icons.rounded.PersonPin
import androidx.compose.material.icons.rounded.Star
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import de.gematik.ti.erp.app.BuildKonfig
import de.gematik.ti.erp.app.Requirement
import de.gematik.ti.erp.app.Route
import de.gematik.ti.erp.app.TestTag
import de.gematik.ti.erp.app.features.BuildConfig
import de.gematik.ti.erp.app.features.R
import de.gematik.ti.erp.app.mainscreen.navigation.MainNavigationScreens
import de.gematik.ti.erp.app.onboarding.model.OnboardingSecureAppMethod
import de.gematik.ti.erp.app.settings.model.SettingsData
import de.gematik.ti.erp.app.settings.ui.AllowAnalyticsScreen
import de.gematik.ti.erp.app.settings.ui.AllowBiometryScreen
import de.gematik.ti.erp.app.settings.ui.SettingsController
import de.gematik.ti.erp.app.theme.AppTheme
import de.gematik.ti.erp.app.theme.PaddingDefaults
import de.gematik.ti.erp.app.utils.compose.NavigationAnimation
import de.gematik.ti.erp.app.utils.compose.OutlinedDebugButton
import de.gematik.ti.erp.app.utils.compose.SecondaryButton
import de.gematik.ti.erp.app.utils.compose.SpacerMedium
import de.gematik.ti.erp.app.utils.compose.SpacerSmall
import de.gematik.ti.erp.app.utils.compose.SpacerXXLarge
import de.gematik.ti.erp.app.utils.compose.navigationModeState
import de.gematik.ti.erp.app.utils.compose.shortToast
import de.gematik.ti.erp.app.utils.compose.visualTestTag
import de.gematik.ti.erp.app.webview.URI_DATA_TERMS
import de.gematik.ti.erp.app.webview.URI_TERMS_OF_USE
import de.gematik.ti.erp.app.webview.WebViewScreen
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.max
import kotlin.math.min

object OnboardingNavigationScreens {
    object Onboarding : Route("onboarding")
    object Analytics : Route("onboarding_analytics")
    object TermsOfUse : Route("onboarding_termsOfUse")
    object DataProtection : Route("onboarding_dataProtection")
    object Biometry : Route("onboarding_biometry")
}

private enum class OnboardingPages(val index: Int) {
    Welcome(index = 0),
    DataProtection(index = 1),
    SecureApp(index = 2),
    Analytics(index = 3);

    companion object {
        val MaxPage = OnboardingPages.values().size - 1

        fun pageOf(index: Int) =
            OnboardingPages.values().find {
                it.index == min(MaxPage, max(0, index))
            }!!
    }
}

@Composable
fun OnboardingScreen(
    mainNavController: NavController,
    settingsController: SettingsController
) {
    val navController = rememberNavController()
    val coroutineScope = rememberCoroutineScope()

    var allowAnalytics by rememberSaveable { mutableStateOf(false) }
    var secureMethod by rememberSaveable { mutableStateOf<OnboardingSecureAppMethod>(OnboardingSecureAppMethod.None) }

    val navigationMode by navController.navigationModeState(OnboardingNavigationScreens.Onboarding.route)

    NavHost(
        navController,
        startDestination = OnboardingNavigationScreens.Onboarding.route
    ) {
        composable(OnboardingNavigationScreens.Onboarding.route) {
            NavigationAnimation(mode = navigationMode) {
                OnboardingScreenWithScaffold(
                    navController,
                    secureMethod = secureMethod,
                    onSecureMethodChange = {
                        secureMethod = it
                    },
                    allowTracking = allowAnalytics,
                    onAllowTracking = {
                        allowAnalytics = it
                    },
                    onSaveNewUser = { allowTracking, defaultProfileName, secureMethod ->
                        coroutineScope.launch(Dispatchers.Main) {
                            settingsController.onboardingSucceeded(
                                authenticationMode = when (secureMethod) {
                                    is OnboardingSecureAppMethod.DeviceSecurity ->
                                        SettingsData.AuthenticationMode.DeviceSecurity

                                    is OnboardingSecureAppMethod.Password ->
                                        SettingsData.AuthenticationMode.Password(
                                            password = requireNotNull(secureMethod.checkedPassword)
                                        )

                                    else -> error("Illegal state. Authentication must be set")
                                },
                                defaultProfileName = defaultProfileName,
                                allowTracking = allowTracking
                            )

                            mainNavController.navigate(MainNavigationScreens.Prescriptions.path()) {
                                launchSingleTop = true
                                popUpTo(MainNavigationScreens.Onboarding.path()) {
                                    inclusive = true
                                }
                            }
                        }
                    }
                )
            }
        }
        composable(OnboardingNavigationScreens.Analytics.route) {
            NavigationAnimation(mode = navigationMode) {
                AllowAnalyticsScreen(
                    onBack = { navController.popBackStack() },
                    onAllowAnalytics = { allowAnalytics = it }
                )
            }
        }
        composable(OnboardingNavigationScreens.Biometry.route) {
            NavigationAnimation(mode = navigationMode) {
                AllowBiometryScreen(
                    onBack = { navController.popBackStack() },
                    onNext = { navController.popBackStack() },
                    onSecureMethodChange = { secureMethod = it }
                )
            }
        }
        composable(OnboardingNavigationScreens.TermsOfUse.route) {
            NavigationAnimation(mode = navigationMode) {
                @Requirement(
                    "O.Purp_1#1",
                    "O.Arch_8#5",
                    "O.Plat_11#5",
                    sourceSpecification = "BSI-eRp-ePA",
                    rationale = "Display terms of use as part of the onboarding. Webview containing local html without javascript" // ktlint-disable max-line-length
                )
                WebViewScreen(
                    modifier = Modifier.testTag(TestTag.Onboarding.TermsOfUseScreen),
                    title = stringResource(R.string.onb_terms_of_use),
                    onBack = { navController.popBackStack() },
                    url = URI_TERMS_OF_USE
                )
            }
        }
        composable(OnboardingNavigationScreens.DataProtection.route) {
            NavigationAnimation(mode = navigationMode) {
                @Requirement(
                    "O.Purp_1#2",
                    "O.Arch_8#6",
                    "O.Plat_11#6",
                    sourceSpecification = "BSI-eRp-ePA",
                    rationale = "Display data privacy as part of the onboarding. Webview containing local html without javascript." // ktlint-disable max-line-length
                )
                WebViewScreen(
                    modifier = Modifier.testTag(TestTag.Onboarding.DataProtectionScreen),
                    title = stringResource(R.string.onb_data_consent),
                    onBack = { navController.popBackStack() },
                    url = URI_DATA_TERMS
                )
            }
        }
    }
}

@Composable
private fun OnboardingScreenWithScaffold(
    navController: NavController,
    secureMethod: OnboardingSecureAppMethod,
    onSecureMethodChange: (OnboardingSecureAppMethod) -> Unit,
    allowTracking: Boolean,
    onAllowTracking: (Boolean) -> Unit,
    onSaveNewUser: (
        allowTracking: Boolean,
        defaultProfileName: String,
        secureAppMethod: OnboardingSecureAppMethod
    ) -> Unit
) {
    val defaultProfileName = stringResource(R.string.onboarding_default_profile_name)

    Box {
        var page by rememberSaveable { mutableStateOf(OnboardingPages.Welcome) }

        LaunchedEffect(secureMethod) {
            if (secureMethod is OnboardingSecureAppMethod.DeviceSecurity && page == OnboardingPages.SecureApp) {
                page = OnboardingPages.Analytics
            }
        }

        BackHandler(enabled = page.index > 1) {
            page = OnboardingPages.pageOf(page.index - 1)
        }

        OnboardingPages(
            page,
            navController,
            defaultProfileName,
            secureMethod,
            onSaveNewUser,
            allowTracking,
            onAllowTracking,
            onSecureMethodChange
        ) {
            page = it
        }

        if (BuildKonfig.INTERNAL && BuildConfig.DEBUG) {
            SkipOnBoardingButton(onClick = {
                onSaveNewUser(
                    false,
                    defaultProfileName,
                    OnboardingSecureAppMethod.Password("a", "a", 9)
                )
            })
        }
    }
}

@OptIn(ExperimentalAnimationApi::class)
@Composable
private fun OnboardingPages(
    page: OnboardingPages,
    navController: NavController,
    defaultProfileName: String,
    secureMethod: OnboardingSecureAppMethod,
    onSaveNewUser: (
        allowTracking: Boolean,
        defaultProfileName: String,
        secureAppMethod: OnboardingSecureAppMethod
    ) -> Unit,
    allowTracking: Boolean,
    onAllowTracking: (Boolean) -> Unit,
    onSecureMethodChange: (OnboardingSecureAppMethod) -> Unit,
    onNextPage: (OnboardingPages) -> Unit
) {
    val context = LocalContext.current

    AnimatedContent(
        modifier = Modifier.fillMaxSize(),
        label = "",
        targetState = page,
        transitionSpec = {
            val isInitialStateWelcome = initialState == OnboardingPages.Welcome
            val isTargetStatePageOne = targetState == OnboardingPages.pageOf(1)
            when {
                isInitialStateWelcome && isTargetStatePageOne -> fade()
                initialState.index > targetState.index -> slideRight()
                else -> slideLeft()
            }
        }
    ) {
        when (it) {
            OnboardingPages.Welcome -> {
                OnboardingWelcome { onNextPage(OnboardingPages.DataProtection) }
            }

            OnboardingPages.DataProtection -> {
                OnboardingPageTerms(
                    navController = navController
                ) { onNextPage(OnboardingPages.SecureApp) }
            }

            OnboardingPages.SecureApp -> {
                OnboardingSecureApp(
                    secureMethod = secureMethod,
                    onSecureMethodChange = onSecureMethodChange,
                    onOpenBiometricScreen = {
                        navController.navigate(OnboardingNavigationScreens.Biometry.path())
                    },
                    onNextPage = { onNextPage(OnboardingPages.Analytics) }
                )
            }

            OnboardingPages.Analytics -> {
                val disAllowToast = stringResource(R.string.settings_tracking_disallow_info)
                OnboardingPageAnalytics(
                    allowAnalytics = allowTracking,
                    onAllowAnalytics = { allow ->
                        if (!allow) {
                            onAllowTracking(false)
                            context.shortToast(disAllowToast)
                        } else {
                            navController.navigate(OnboardingNavigationScreens.Analytics.path())
                        }
                    },
                    onNextPage = {
                        onSaveNewUser(allowTracking, defaultProfileName, secureMethod)
                    }
                )
            }
        }
    }
}

@Composable
fun SkipOnBoardingButton(onClick: () -> Unit) {
    Box(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .systemBarsPadding()
                .padding(PaddingDefaults.Medium)
        ) {
            OutlinedDebugButton(
                "SKIP",
                onClick = onClick
            )
        }
    }
}

@Composable
private fun OnboardingWelcome(
    onNextPage: () -> Unit
) {
    LaunchedEffect(Unit) {
        delay(timeMillis = 1770)
        onNextPage()
    }

    Column(
        modifier = Modifier
            .testTag(TestTag.Onboarding.WelcomeScreen)
            .padding(horizontal = PaddingDefaults.Medium)
            .systemBarsPadding()
    ) {
        Row(
            modifier = Modifier
                .padding(
                    top = PaddingDefaults.Medium
                )
                .align(Alignment.Start),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Image(
                painterResource(R.drawable.ic_onboarding_logo_flag),
                null,
                modifier = Modifier.padding(end = 10.dp)
            )
            Icon(
                painterResource(R.drawable.ic_onboarding_logo_gematik),
                null,
                tint = AppTheme.colors.primary900
            )
        }
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight()
                .semantics(mergeDescendants = true) {}
        ) {
            Image(
                painterResource(R.drawable.erp_logo),
                null,
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .padding(top = PaddingDefaults.Large)
            )
            Text(
                text = stringResource(R.string.app_name),
                style = AppTheme.typography.h4,
                fontWeight = FontWeight.W700,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .padding(
                        top = PaddingDefaults.Medium,
                        bottom = PaddingDefaults.Small
                    )
            )
            Text(
                text = stringResource(R.string.on_boarding_page_1_header),
                style = AppTheme.typography.subtitle1l,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .padding(
                        bottom = PaddingDefaults.XXLarge
                    )
            )
        }

        @Suppress("MagicNumber")
        Image(
            painterResource(R.drawable.onboarding_boygrannygranpa),
            null,
            alignment = Alignment.BottomStart,
            modifier = Modifier
                .fillMaxSize()
                .offset(x = (-60).dp)
        )
    }
}

@Requirement(
    "O.Purp_3#4",
    sourceSpecification = "BSI-eRp-ePA",
    rationale = "User information and acceptance/deny for analytics usage"
)
@Composable
private fun OnboardingPageAnalytics(
    allowAnalytics: Boolean,
    onAllowAnalytics: (Boolean) -> Unit,
    onNextPage: () -> Unit
) {
    OnboardingScaffold(
        state = rememberLazyListState(),
        bottomBar = {
            OnboardingBottomBar(
                info = stringResource(R.string.onboarding_analytics_bottom_you_can_change),
                buttonText = stringResource(R.string.onboarding_bottom_button_next),
                buttonEnabled = true,
                buttonModifier = Modifier.testTag(TestTag.Onboarding.NextButton),
                onButtonClick = onNextPage
            )
        },
        modifier = Modifier
            .visualTestTag(TestTag.Onboarding.AnalyticsScreen)
            .fillMaxSize()
    ) {
        item {
            SpacerXXLarge()
            Text(
                text = stringResource(R.string.onb_page_5_header),
                style = AppTheme.typography.h4,
                fontWeight = FontWeight.W700,
                textAlign = TextAlign.Start,
                modifier = Modifier
                    .padding(
                        top = PaddingDefaults.XXLarge,
                        bottom = PaddingDefaults.Large
                    )
            )
            SpacerXXLarge()
        }
        item {
            Column(verticalArrangement = Arrangement.spacedBy(PaddingDefaults.Medium)) {
                Text(
                    text = stringResource(R.string.onboarding_analytics_we_want),
                    style = AppTheme.typography.subtitle1
                )
                AnalyticsInfo(
                    icon = Icons.Rounded.Star,
                    text = stringResource(R.string.onboarding_analytics_ww_usability)
                )
                AnalyticsInfo(
                    icon = Icons.Rounded.FlashOn,
                    text = stringResource(R.string.onboarding_analytics_ww_errors)
                )
                AnalyticsInfo(
                    icon = Icons.Rounded.PersonPin,
                    text = stringResource(R.string.onboarding_analytics_ww_anon)
                )
            }
            SpacerXXLarge()
        }
        item {
            AnalyticsToggle(allowAnalytics, onAllowAnalytics)
            SpacerMedium()
        }
    }
}

@Requirement(
    "A_19184",
    "A_20194",
    "A_19980",
    "A_19981",
    sourceSpecification = "gemSpec_eRp_FdV",
    rationale = "Displays terms of service and privacy statement to the user."
)
@Composable
private fun OnboardingPageTerms(
    navController: NavController,
    onNextPage: () -> Unit
) {
    var accepted by rememberSaveable { mutableStateOf(false) }

    OnboardingScaffold(
        state = rememberLazyListState(),
        bottomBar = {
            OnboardingBottomBar(
                modifier = Modifier.fillMaxWidth(),
                info = null,
                buttonText = stringResource(R.string.onboarding_bottom_button_accept),
                buttonEnabled = accepted,
                buttonModifier = Modifier.testTag(TestTag.Onboarding.NextButton),
                onButtonClick = onNextPage
            )
        },
        modifier = Modifier
            .visualTestTag(TestTag.Onboarding.DataTermsScreen)
            .fillMaxSize()
    ) {
        item {
            SpacerXXLarge()
            Image(
                painter = painterResource(R.drawable.paragraph),
                contentDescription = null,
                alignment = Alignment.CenterStart,
                modifier = Modifier.fillMaxWidth()
            )
            SpacerXXLarge()
        }
        item {
            Text(
                text = stringResource(R.string.onb_page_4_header),
                style = AppTheme.typography.h4,
                fontWeight = FontWeight.W700,
                textAlign = TextAlign.Start,
                modifier = Modifier.padding(bottom = PaddingDefaults.Medium, top = PaddingDefaults.XXLarge)
            )
            SpacerMedium()
        }
        item {
            @Requirement(
                "O.Purp_3#1",
                "O.Arch_9",
                sourceSpecification = "BSI-eRp-ePA",
                rationale = "Display data protection as part of the onboarding"
            )
            @Requirement(
                "A_19980#1",
                "A_19981#1",
                sourceSpecification = "gemSpec_eRp_FdV",
                rationale = "Display data protection as part of the onboarding"
            )
            SecondaryButton(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag(TestTag.Onboarding.DataTerms.OpenDataProtectionButton),
                onClick = {
                    navController.navigate(OnboardingNavigationScreens.DataProtection.path())
                }
            ) {
                Text(stringResource(R.string.onboarding_data_button))
            }
            SpacerMedium()
        }
        item {
            @Requirement(
                "O.Purp_3#2",
                sourceSpecification = "BSI-eRp-ePA",
                rationale = "Display terms of use as part of the onboarding"
            )
            SecondaryButton(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag(TestTag.Onboarding.DataTerms.OpenTermsOfUseButton),
                onClick = {
                    navController.navigate(OnboardingNavigationScreens.TermsOfUse.path())
                }
            ) {
                Text(stringResource(R.string.onboarding_terms_button))
            }
            SpacerXXLarge()
        }
        item {
            @Requirement(
                "O.Purp_3#3",
                sourceSpecification = "BSI-eRp-ePA",
                rationale = "User acceptance for terms of use and dar´ta protection as part of the onboarding"
            )
            DataTermsToggle(
                accepted = accepted,
                onCheckedChange = {
                    accepted = it
                }
            )
            SpacerMedium()
        }
    }
}

@Composable
private fun AnalyticsInfo(icon: ImageVector, text: String) {
    Row(Modifier.fillMaxWidth()) {
        Icon(icon, null, tint = AppTheme.colors.primary600)
        SpacerMedium()
        Text(
            text = text,
            style = AppTheme.typography.body1
        )
    }
}

@Composable
private fun AnalyticsToggle(
    analyticsAllowed: Boolean,
    onCheckedChange: (Boolean) -> Unit
) =
    LargeToggle(
        modifier = Modifier.testTag(TestTag.Onboarding.AnalyticsSwitch),
        text = stringResource(R.string.on_boarding_page_5_label),
        checked = analyticsAllowed,
        onCheckedChange = onCheckedChange
    )

@Requirement(
    "A_19980#2",
    "A_19981#2",
    sourceSpecification = "gemSpec_eRp_FdV",
    rationale = "The user is informed and required to accept this information via the data protection statement. Related data and services are listed in sections 5." // ktlint-disable max-line-length
)
@Composable
private fun DataTermsToggle(
    accepted: Boolean,
    onCheckedChange: (Boolean) -> Unit
) =
    LargeToggle(
        modifier = Modifier.testTag(TestTag.Onboarding.DataTerms.AcceptDataTermsSwitch),
        text = stringResource(R.string.onboarding_data_terms_info),
        checked = accepted,
        onCheckedChange = onCheckedChange
    )

@Composable
private fun LargeToggle(
    modifier: Modifier = Modifier,
    text: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(PaddingDefaults.Medium))
            .background(AppTheme.colors.neutral100, shape = RoundedCornerShape(16.dp))
            .fillMaxWidth()
            .toggleable(
                value = checked,
                onValueChange = onCheckedChange,
                enabled = true,
                role = Role.Switch,
                interactionSource = remember { MutableInteractionSource() },
                indication = LocalIndication.current
            )
            .padding(PaddingDefaults.Medium),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(PaddingDefaults.Small)
    ) {
        Switch(
            checked = checked,
            onCheckedChange = null
        )
        SpacerSmall()
        Text(
            text = text,
            style = AppTheme.typography.subtitle2,
            modifier = Modifier.weight(1f)
        )
    }
}
