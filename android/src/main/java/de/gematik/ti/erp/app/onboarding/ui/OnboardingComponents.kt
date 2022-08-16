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

package de.gematik.ti.erp.app.onboarding.ui

import androidx.activity.compose.BackHandler
import androidx.annotation.FloatRange
import androidx.annotation.StringRes
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.SpringSpec
import androidx.compose.foundation.Image
import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Button
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.FloatingActionButton
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Scaffold
import androidx.compose.material.Switch
import androidx.compose.material.Text
import androidx.compose.material.contentColorFor
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowForward
import androidx.compose.material.icons.rounded.BugReport
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.LiveHelp
import androidx.compose.material.icons.rounded.RadioButtonUnchecked
import androidx.compose.material.icons.rounded.ReceiptLong
import androidx.compose.material.icons.rounded.SaveAlt
import androidx.compose.material.icons.rounded.Send
import androidx.compose.material.icons.rounded.Timeline
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.layout
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.disabled
import androidx.compose.ui.semantics.focused
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.compose.foundation.layout.statusBarsPadding
import com.google.accompanist.pager.ExperimentalPagerApi
import com.google.accompanist.pager.HorizontalPager
import com.google.accompanist.pager.PagerDefaults
import com.google.accompanist.pager.PagerState
import com.google.accompanist.pager.rememberPagerState
import de.gematik.ti.erp.app.BuildKonfig
import de.gematik.ti.erp.app.R
import de.gematik.ti.erp.app.Route
import de.gematik.ti.erp.app.TestTag
import de.gematik.ti.erp.app.mainscreen.ui.MainNavigationScreens
import de.gematik.ti.erp.app.settings.model.SettingsData
import de.gematik.ti.erp.app.settings.ui.AllowAnalyticsScreen
import de.gematik.ti.erp.app.settings.ui.AllowBiometryScreen
import de.gematik.ti.erp.app.settings.ui.SettingsViewModel
import de.gematik.ti.erp.app.theme.AppTheme
import de.gematik.ti.erp.app.theme.PaddingDefaults
import de.gematik.ti.erp.app.utils.compose.BottomAppBar
import de.gematik.ti.erp.app.utils.compose.NavigationAnimation
import de.gematik.ti.erp.app.utils.compose.OutlinedDebugButton
import de.gematik.ti.erp.app.utils.compose.Spacer4
import de.gematik.ti.erp.app.utils.compose.SpacerMedium
import de.gematik.ti.erp.app.utils.compose.SpacerSmall
import de.gematik.ti.erp.app.utils.compose.SpacerXXLarge
import de.gematik.ti.erp.app.utils.compose.annotatedStringBold
import de.gematik.ti.erp.app.utils.compose.annotatedStringResource
import de.gematik.ti.erp.app.utils.compose.createToastShort
import de.gematik.ti.erp.app.utils.compose.minimalSystemBarsPadding
import de.gematik.ti.erp.app.utils.compose.navigationModeState
import de.gematik.ti.erp.app.utils.compose.visualTestTag
import de.gematik.ti.erp.app.webview.URI_DATA_TERMS
import de.gematik.ti.erp.app.webview.URI_TERMS_OF_USE
import de.gematik.ti.erp.app.webview.WebViewScreen
import dev.chrisbanes.snapper.ExperimentalSnapperApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.Locale
import kotlin.math.max
import kotlin.math.roundToInt

object OnboardingNavigationScreens {
    object Onboarding : Route("Onboarding")
    object Analytics : Route("Analytics")
    object TermsOfUse : Route("TermsOfUse")
    object DataProtection : Route("DataProtection")
    object Biometry : Route("Biometry")
}

private const val MAX_PAGES = 5
private const val WELCOME_PAGE = 0
private const val FEATURE_PAGE = 1
private const val SECURE_APP_PAGE = 2
private const val ANALYTICS_PAGE = 3
private const val TOS_AND_DATA_PAGE = 4
private const val DELAY = 100L

val OnboardingFabPadding = 128.dp

@Composable
fun ReturningUserSecureAppOnboardingScreen(
    mainNavController: NavController,
    settingsViewModel: SettingsViewModel,
    secureMethod: OnboardingSecureAppMethod,
    onSecureMethodChange: (OnboardingSecureAppMethod) -> Unit
) {
    val enabled = when (secureMethod) {
        is OnboardingSecureAppMethod.DeviceSecurity -> true
        is OnboardingSecureAppMethod.Password -> (secureMethod as? OnboardingSecureAppMethod.Password)?.let {
            it.checkedPassword != null
        } ?: false
        else -> false
    }

    val coroutineScope = rememberCoroutineScope()
    Scaffold(
        modifier = Modifier.statusBarsPadding(),
        bottomBar = {
            BottomAppBar(backgroundColor = MaterialTheme.colors.surface) {
                Spacer(modifier = Modifier.weight(1f))
                Button(
                    enabled = enabled,
                    onClick = {
                        coroutineScope.launch {
                            when (val sm = secureMethod) {
                                is OnboardingSecureAppMethod.DeviceSecurity ->
                                    settingsViewModel.onSelectDeviceSecurityAuthenticationMode()
                                is OnboardingSecureAppMethod.Password ->
                                    settingsViewModel.onSelectPasswordAsAuthenticationMode(
                                        requireNotNull(sm.checkedPassword)
                                    )
                                else -> error("Illegal state. Authentication must be set")
                            }
                            mainNavController.navigate(MainNavigationScreens.Prescriptions.path()) {
                                launchSingleTop = true
                                popUpTo(MainNavigationScreens.ReturningUserSecureAppOnboarding.path()) {
                                    inclusive = true
                                }
                            }
                        }
                    },
                    shape = RoundedCornerShape(PaddingDefaults.Small)
                ) {
                    Text(stringResource(R.string.ok).uppercase(Locale.getDefault()))
                }
                SpacerMedium()
            }
        }
    ) { innerPadding ->
        OnboardingSecureApp(
            Modifier.padding(innerPadding),
            secureMethod = secureMethod,
            onSecureMethodChange = onSecureMethodChange,
            onNext = {},
            onOpenBiometricScreen = { mainNavController.navigate(MainNavigationScreens.Biometry.path()) }
        )
    }
}

@OptIn(ExperimentalPagerApi::class)
@Composable
fun OnboardingScreen(
    mainNavController: NavController,
    settingsViewModel: SettingsViewModel
) {
    val state = rememberPagerState(initialPage = 0)
    val scope = rememberCoroutineScope()
    val navController = rememberNavController()
    val coroutineScope = rememberCoroutineScope()
    var allowTracking by rememberSaveable { mutableStateOf(false) }
    var secureMethod by rememberSaveable { mutableStateOf<OnboardingSecureAppMethod>(OnboardingSecureAppMethod.None) }
    val navigationMode by navController.navigationModeState(OnboardingNavigationScreens.Onboarding.route)

    val maxPages = remember(secureMethod) {
        when (secureMethod) {
            is OnboardingSecureAppMethod.Password -> (secureMethod as? OnboardingSecureAppMethod.Password)?.let {
                if (it.checkedPassword != null) MAX_PAGES else SECURE_APP_PAGE + 1
            } ?: (SECURE_APP_PAGE + 1)
            is OnboardingSecureAppMethod.DeviceSecurity -> MAX_PAGES
            else -> SECURE_APP_PAGE + 1
        }
    }

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
                    state = state,
                    maxPages = maxPages,
                    allowTracking = allowTracking,
                    onAllowTracking = {
                        allowTracking = it
                    },
                    onSaveNewUser = { allowTracking, defaultProfileName, secureMethod ->
                        coroutineScope.launch(Dispatchers.Main) {
                            settingsViewModel.onboardingSucceeded(
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
                    onAllowAnalytics = { allowTracking = it }
                )
            }
        }
        composable(OnboardingNavigationScreens.Biometry.route) {
            NavigationAnimation(mode = navigationMode) {
                AllowBiometryScreen(
                    onBack = { navController.popBackStack() },
                    onNext = {
                        navController.popBackStack()
                        if (state.currentPage == SECURE_APP_PAGE) {
                            scope.launch {
                                delay(DELAY) // composable needs time to recalculate maxPages
                                state.animateScrollToPage(state.currentPage + 1)
                            }
                        }
                    },
                    onSecureMethodChange = { secureMethod = it }
                )
            }
        }
        composable(OnboardingNavigationScreens.TermsOfUse.route) {
            NavigationAnimation(mode = navigationMode) {
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

@Suppress("LongMethod")
@OptIn(ExperimentalPagerApi::class, ExperimentalSnapperApi::class, ExperimentalMaterialApi::class)
@Composable
private fun OnboardingScreenWithScaffold(
    navController: NavController,
    secureMethod: OnboardingSecureAppMethod,
    onSecureMethodChange: (OnboardingSecureAppMethod) -> Unit,
    allowTracking: Boolean,
    maxPages: Int,
    state: PagerState,
    onAllowTracking: (Boolean) -> Unit,
    onSaveNewUser: (
        allowTracking: Boolean,
        defaultProfileName: String,
        secureAppMethod: OnboardingSecureAppMethod
    ) -> Unit
) {
    val context = LocalContext.current
    var tosAndDataToggled by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    BackHandler(enabled = state.currentPage > 0) {
        scope.launch {
            state.animateScrollToPage(max(0, state.currentPage - 1))
        }
    }

    val doNextPage = remember { Channel<Unit>(Channel.CONFLATED) }

    LaunchedEffect(state.pageCount) {
        for (ignored in doNextPage) {
            if (state.currentPage + 1 < state.pageCount) {
                state.animateScrollToPage(state.currentPage + 1)
            }
        }
    }

    val focusManager = LocalFocusManager.current
    LaunchedEffect(state.currentPage) {
        focusManager.clearFocus()
    }

    Scaffold(
        modifier = Modifier
            .visualTestTag("screen_onboarding")
            .minimalSystemBarsPadding()
    ) {
        Box {
            HorizontalPager(
                count = maxPages,
                modifier = Modifier
                    .testTag(TestTag.Onboarding.Pager)
                    .fillMaxSize(),
                state = state,
                flingBehavior = PagerDefaults.flingBehavior(
                    state = state,
                    snapAnimationSpec = SpringSpec()
                ),
                key = {
                    it
                }
            ) { page ->
                when (page) {
                    WELCOME_PAGE -> {
                        OnboardingWelcome(
                            Modifier.semantics { focused = state.currentPage == WELCOME_PAGE },
                            state
                        )
                    }
                    FEATURE_PAGE -> {
                        OnboardingAppFeatures(
                            Modifier.semantics {
                                focused = state.currentPage == FEATURE_PAGE
                            }
                        )
                    }

                    SECURE_APP_PAGE -> {
                        OnboardingSecureApp(
                            Modifier.semantics { focused = state.currentPage == SECURE_APP_PAGE },
                            secureMethod = secureMethod,
                            onSecureMethodChange = onSecureMethodChange,
                            onOpenBiometricScreen = {
                                navController.navigate(OnboardingNavigationScreens.Biometry.path())
                            },
                            onNext = {
                                scope.launch { doNextPage.send(Unit) }
                            }
                        )
                    }
                    ANALYTICS_PAGE -> {
                        val disAllowToast = stringResource(R.string.settings_tracking_disallow_info)
                        OnboardingPageAnalytics(
                            Modifier.semantics { focused = state.currentPage == ANALYTICS_PAGE },
                            allowTracking = allowTracking,
                            onAllowTracking = {
                                if (!it) {
                                    onAllowTracking(false)
                                    createToastShort(context, disAllowToast)
                                } else {
                                    navController.navigate(OnboardingNavigationScreens.Analytics.path())
                                }
                            }
                        )
                    }
                    TOS_AND_DATA_PAGE -> {
                        OnboardingPageTerms(
                            Modifier.semantics { focused = state.currentPage == TOS_AND_DATA_PAGE },
                            navController
                        ) {
                            tosAndDataToggled = it
                        }
                    }
                }
            }
            BottomPageIndicator(state)
            val currentPageIsSecureApp = state.currentPage == SECURE_APP_PAGE
            val secureMethodIsEmpty = secureMethod is OnboardingSecureAppMethod.None ||
                (secureMethod is OnboardingSecureAppMethod.Password && secureMethod.checkedPassword == null)
            val defaultProfileName = stringResource(R.string.onboarding_default_profile_name)
            if (!(currentPageIsSecureApp && secureMethodIsEmpty)) {
                OnboardingNextButton(
                    secureMethod = secureMethod,
                    tosAndDataToggled = tosAndDataToggled,
                    currentPage = state.currentPage,
                    onNextPage = {
                        scope.launch { doNextPage.send(Unit) }
                    },
                    onSaveNewUser = {
                        onSaveNewUser(allowTracking, defaultProfileName, secureMethod)
                    }
                )
            }

            if (BuildKonfig.INTERNAL) {
                Row(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(PaddingDefaults.Medium)
                ) {
                    OutlinedDebugButton(
                        "SKIP",
                        onClick = {
                            onSaveNewUser(false, defaultProfileName, OnboardingSecureAppMethod.Password("a", "a", 9))
                        }
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalPagerApi::class)
@Composable
private fun BoxScope.BottomPageIndicator(pagerState: PagerState) {
    val pagerAccessibilityDescription = annotatedStringResource(
        R.string.on_boarding_pager_acc_description,
        (pagerState.currentPage + 1).toString(),
        MAX_PAGES.toString()
    ).toString()
    Box(
        modifier = Modifier
            .padding(bottom = 24.dp)
            .semantics { contentDescription = pagerAccessibilityDescription }
            .clip(CircleShape)
            .background(AppTheme.colors.neutral100.copy(alpha = 0.5f))
            .align(Alignment.BottomCenter)
            .padding(PaddingDefaults.Small)
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(PaddingDefaults.Small)
        ) {
            repeat(MAX_PAGES) {
                Dot(color = AppTheme.colors.neutral300)
            }
        }

        val fraction = pagerState.currentPage + pagerState.currentPageOffset
        val offsetX = with(LocalDensity.current) {
            val gap = PaddingDefaults.Small.roundToPx()
            val size = 8.dp.roundToPx()
            (fraction * (size + gap)).toDp()
        }
        Dot(modifier = Modifier.offset(x = offsetX), color = AppTheme.colors.primary500)
    }
}

@Composable
private fun Dot(modifier: Modifier = Modifier, color: Color) {
    Box(
        modifier = modifier
            .clip(CircleShape)
            .background(color)
            .size(8.dp)
    )
}

@Composable
private fun BoxScope.OnboardingNextButton(
    modifier: Modifier = Modifier,
    tosAndDataToggled: Boolean,
    secureMethod: OnboardingSecureAppMethod,
    currentPage: Int,
    onNextPage: () -> Unit,
    onSaveNewUser: () -> Unit
) {
    val enabled = when {
        currentPage == WELCOME_PAGE || currentPage == FEATURE_PAGE || currentPage == ANALYTICS_PAGE -> true
        currentPage == SECURE_APP_PAGE && secureMethod is OnboardingSecureAppMethod.DeviceSecurity -> true
        currentPage == SECURE_APP_PAGE && secureMethod is OnboardingSecureAppMethod.Password -> secureMethod.checkedPassword != null
        tosAndDataToggled && currentPage == TOS_AND_DATA_PAGE -> true
        else -> false
    }
    val onBoardingNextButton = stringResource(R.string.on_boarding_cdn_btn_next)
    NextButton(
        onNext = {
            when {
                currentPage == TOS_AND_DATA_PAGE && tosAndDataToggled -> onSaveNewUser()
                currentPage == TOS_AND_DATA_PAGE -> {
                }
                else -> onNextPage()
            }
        },
        enabled = enabled,
        modifier = modifier
            .align(Alignment.BottomEnd)
            .semantics {
                contentDescription = if (currentPage != TOS_AND_DATA_PAGE) {
                    onBoardingNextButton
                } else {
                    ""
                }
            }
    ) {
        Crossfade(targetState = currentPage == TOS_AND_DATA_PAGE) {
            when (it) {
                true -> Icon(Icons.Rounded.Check, null)
                false -> Icon(Icons.Rounded.ArrowForward, null)
            }
        }
        AnimatedVisibility(
            visible = currentPage == TOS_AND_DATA_PAGE
        ) {
            Row {
                Spacer4()
                Text(
                    stringResource(R.string.on_boarding_page_4_next)
                )
            }
        }
    }
}

@Composable
private fun NextButton(
    modifier: Modifier = Modifier,
    enabled: Boolean,
    onNext: () -> Unit,
    content: @Composable RowScope.() -> Unit
) {
    val backgroundColor =
        if (enabled) {
            AppTheme.colors.primary600
        } else {
            AppTheme.colors.neutral300
        }

    val contentColor = if (enabled) {
        contentColorFor(backgroundColor)
    } else {
        AppTheme.colors.neutral600
    }

    FloatingActionButton(
        onClick = {
            if (enabled) {
                onNext()
            }
        },
        shape = RoundedCornerShape(PaddingDefaults.Medium),
        backgroundColor = backgroundColor,
        contentColor = contentColor,
        modifier = modifier
            .padding(
                bottom = 64.dp,
                end = PaddingDefaults.Medium
            )
            .semantics {
                if (!enabled) {
                    disabled()
                }
            }
            .visualTestTag(TestTag.Onboarding.NextButton)
    ) {
        Row(
            modifier = Modifier.padding(PaddingDefaults.Medium),
            verticalAlignment = Alignment.CenterVertically
        ) {
            content()
        }
    }
}

@Composable
private fun PeopleLayer(
    modifier: Modifier = Modifier,
    @FloatRange(from = -1.0, to = 1.0) relativePageOffset: Float
) {
    Row(
        modifier = modifier
            .layout { measurable, constraints ->
                val p = measurable.measure(constraints)

                layout(constraints.maxWidth, constraints.maxHeight) {
                    p.place(
                        x = -(p.height / 8f + p.height * relativePageOffset / 3f).roundToInt(),
                        y = 0
                    )
                }
            }
    ) {
        Image(
            painterResource(R.drawable.onboarding_boygrannygranpa),
            null,
            alignment = Alignment.BottomStart,
            modifier = Modifier.fillMaxSize()
        )
    }
}

@OptIn(ExperimentalPagerApi::class)
@Composable
private fun OnboardingWelcome(modifier: Modifier, pagerState: PagerState) {
    Column(
        modifier = modifier
            .visualTestTag(TestTag.Onboarding.WelcomeScreen)
            .padding(horizontal = PaddingDefaults.Medium)
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
        val offset by derivedStateOf {
            if (pagerState.currentPage == WELCOME_PAGE) pagerState.currentPageOffset else 0f
        }

        PeopleLayer(
            relativePageOffset = offset
        )
    }
}

@Composable
private fun OnboardingAppFeatures(
    modifier: Modifier
) {
    OnboardingLazyColumn(
        state = rememberLazyListState(),
        modifier = modifier
            .visualTestTag(TestTag.Onboarding.FeatureScreen)
            .fillMaxSize()
    ) {
        item {
            Image(
                painter = painterResource(R.drawable.woman_red_shirt_overlapping),
                null,
                alignment = Alignment.CenterStart,
                modifier = Modifier
                    .padding(
                        top = PaddingDefaults.XXLarge,
                        bottom = PaddingDefaults.XXLarge
                    )
                    .fillMaxWidth()
            )
        }
        item {
            Column(
                modifier = Modifier
                    .wrapContentSize()
                    .semantics(mergeDescendants = true) {}
            ) {
                Text(
                    text = stringResource(R.string.onb_page_3_header),
                    style = AppTheme.typography.h4,
                    fontWeight = FontWeight.W700,
                    textAlign = TextAlign.Start
                )
                Column(
                    verticalArrangement = Arrangement.spacedBy(PaddingDefaults.Medium),
                    modifier = Modifier.padding(
                        top = PaddingDefaults.Medium
                    )
                ) {
                    OnboardingFeatureItem(
                        icon = Icons.Rounded.Send,
                        text = stringResource(R.string.onb_page_3_info_check_1)
                    )
                    OnboardingFeatureItem(
                        icon = Icons.Rounded.ReceiptLong,
                        text = stringResource(R.string.on_boarding_page_3_info_check_2)
                    )
                    OnboardingFeatureItem(
                        icon = Icons.Rounded.SaveAlt,
                        text = stringResource(R.string.on_boarding_page_3_info_check_3)
                    )
                }
            }
        }
    }
}

@Composable
private fun OnboardingFeatureItem(icon: ImageVector, text: String) {
    Row {
        Icon(
            icon,
            null,
            tint = AppTheme.colors.primary600
        )
        SpacerMedium()
        Text(
            text,
            style = AppTheme.typography.body1,
            textAlign = TextAlign.Start
        )
    }
}

@Composable
private fun OnboardingPageAnalytics(
    modifier: Modifier,
    allowTracking: Boolean,
    onAllowTracking: (Boolean) -> Unit
) {
    OnboardingLazyColumn(
        state = rememberLazyListState(),
        modifier = modifier
            .visualTestTag(TestTag.Onboarding.AnalyticsScreen)
            .fillMaxSize()
    ) {
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentHeight()
                    .semantics(mergeDescendants = true) {}
            ) {
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

                val stringBold = stringResource(R.string.on_boarding_page_5_anonym)
                AnalyticsInfo(
                    icon = Icons.Rounded.Timeline,
                    id = R.string.on_boarding_page_5_info_1,
                    stringBold = stringBold
                )
                SpacerMedium()
                AnalyticsInfo(
                    icon = Icons.Rounded.BugReport,
                    id = R.string.on_boarding_page_5_info_2,
                    stringBold = stringBold
                )
                SpacerMedium()
                AnalyticsInfo(
                    icon = Icons.Rounded.LiveHelp,
                    id = R.string.on_boarding_page_5_info_3,
                    stringBold = ""
                )
            }
        }
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentHeight()
                    .semantics(mergeDescendants = true) {}
            ) {
                SpacerXXLarge()
                AnalyticsToggle(allowTracking, onAllowTracking)
                SpacerSmall()
                Text(
                    stringResource(R.string.on_boarding_page_5_label_info),
                    style = AppTheme.typography.body2l,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

@Composable
private fun OnboardingPageTerms(
    modifier: Modifier,
    navController: NavController,
    onBothToggled: (Boolean) -> Unit
) {
    OnboardingLazyColumn(
        state = rememberLazyListState(),
        modifier = modifier
            .visualTestTag(TestTag.Onboarding.DataTermsScreen)
            .fillMaxSize()
    ) {
        item {
            Image(
                painter = painterResource(R.drawable.paragraph),
                contentDescription = null,
                alignment = Alignment.CenterStart,
                modifier = Modifier
                    .padding(
                        top = PaddingDefaults.XXLarge
                    )
                    .fillMaxWidth()
            )
        }
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentHeight()
                    .semantics(mergeDescendants = true) {}
            ) {
                Text(
                    text = stringResource(R.string.onb_page_4_header),
                    style = AppTheme.typography.h4,
                    fontWeight = FontWeight.W700,
                    textAlign = TextAlign.Start,
                    modifier = Modifier.padding(bottom = PaddingDefaults.Medium, top = PaddingDefaults.XXLarge)
                )
                Text(
                    text = stringResource(R.string.on_boarding_page_4_info),
                    style = AppTheme.typography.body1,
                    modifier = Modifier
                        .padding(bottom = PaddingDefaults.XXLarge)
                )
            }
            var checkedDataProtection by rememberSaveable { mutableStateOf(false) }
            var checkedTos by rememberSaveable { mutableStateOf(false) }

            DisposableEffect(checkedDataProtection, checkedTos) {
                if (checkedDataProtection && checkedTos) {
                    onBothToggled(true)
                } else {
                    onBothToggled(false)
                }
                onDispose { }
            }

            OnboardingToggle(
                stringResource(R.string.on_boarding_page_4_info_dataprotection),
                stringResource(R.string.onb_accept_data),
                toggleTestTag = TestTag.Onboarding.DataTerms.DataProtectionSwitch,
                clickTestTag = TestTag.Onboarding.DataTerms.OpenDataProtectionButton,
                checked = checkedDataProtection,
                onCheckedChange = {
                    checkedDataProtection = it
                },
                onClickInfo = {
                    navController.navigate(OnboardingNavigationScreens.DataProtection.path())
                }
            )

            OnboardingToggle(
                stringResource(R.string.on_boarding_page_4_info_tos),
                stringResource(R.string.onb_accept_tos),
                toggleTestTag = TestTag.Onboarding.DataTerms.TermsOfUseSwitch,
                clickTestTag = TestTag.Onboarding.DataTerms.OpenTermsOfUseButton,
                checked = checkedTos,
                onCheckedChange = {
                    checkedTos = it
                },
                onClickInfo = {
                    navController.navigate(OnboardingNavigationScreens.TermsOfUse.path())
                }
            )
        }
    }
}

@Composable
private fun AnalyticsInfo(icon: ImageVector, @StringRes id: Int, stringBold: String) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(PaddingDefaults.Medium)
    ) {
        Icon(icon, null, tint = AppTheme.colors.primary600)
        Column(modifier = Modifier.weight(1.0f)) {
            Text(
                text = annotatedStringResource(
                    id,
                    annotatedStringBold(stringBold)
                ),
                style = AppTheme.typography.body1
            )
        }
    }
}

@Composable
private fun AnalyticsToggle(
    analyticsAllowed: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(PaddingDefaults.Medium))
            .background(AppTheme.colors.neutral100, shape = RoundedCornerShape(PaddingDefaults.Medium))
            .fillMaxWidth()
            .toggleable(
                value = analyticsAllowed,
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
        Text(
            text = stringResource(R.string.on_boarding_page_5_label),
            style = AppTheme.typography.subtitle2,
            modifier = Modifier.weight(1f)
        )
        SpacerSmall()
        Switch(
            checked = analyticsAllowed,
            onCheckedChange = null
        )
    }
}

@Composable
private fun OnboardingToggle(
    which: String,
    toggleContentDescription: String,
    toggleTestTag: String,
    clickTestTag: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    onClickInfo: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .semantics(mergeDescendants = true) {}
            .padding(bottom = PaddingDefaults.Large),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        val info = annotatedStringResource(
            R.string.on_boarding_page_4_info_accept_info,
            buildAnnotatedString {
                pushStringAnnotation("CLICKABLE", "")
                pushStyle(SpanStyle(color = AppTheme.colors.primary600))
                append(which)
                pop()
                pop()
            }
        )

        val alpha = remember { Animatable(0.0f) }

        LaunchedEffect(checked) {
            if (checked) {
                alpha.animateTo(1.0f)
            } else {
                alpha.animateTo(0.0f)
            }
        }

        Text(
            text = info,
            style = AppTheme.typography.body1,
            modifier = Modifier
                .weight(1f)
                .clickable(
                    onClickLabel = which,
                    indication = null,
                    interactionSource = remember { MutableInteractionSource() },
                    onClick = onClickInfo
                )
                .visualTestTag(clickTestTag)
        )

        Box(
            modifier = Modifier
                .align(Alignment.CenterVertically)
                .size(48.dp)
                .toggleable(
                    value = checked,
                    onValueChange = onCheckedChange,
                    role = Role.Checkbox,
                    interactionSource = remember { MutableInteractionSource() },
                    indication = rememberRipple(
                        bounded = false,
                        radius = PaddingDefaults.Large
                    )
                )
                .visualTestTag(toggleTestTag)
                .semantics {
                    contentDescription = toggleContentDescription
                },
            contentAlignment = Alignment.Center
        ) {
            Icon(
                Icons.Rounded.RadioButtonUnchecked,
                null,
                tint = AppTheme.colors.neutral300
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
