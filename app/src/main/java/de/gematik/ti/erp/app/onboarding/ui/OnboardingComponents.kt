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

package de.gematik.ti.erp.app.onboarding.ui

import android.os.Parcelable
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.annotation.StringRes
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.Animatable
import androidx.compose.foundation.Image
import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.Orientation
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
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.BottomAppBar
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.Divider
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.FloatingActionButton
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Scaffold
import androidx.compose.material.SwipeableState
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
import androidx.compose.material.icons.rounded.Timeline
import androidx.compose.material.rememberSwipeableState
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.material.swipeable
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
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
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onSizeChanged
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
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import de.gematik.ti.erp.app.R
import de.gematik.ti.erp.app.Route
import de.gematik.ti.erp.app.core.LocalActivity
import de.gematik.ti.erp.app.db.entities.SettingsAuthenticationMethod
import de.gematik.ti.erp.app.mainscreen.ui.MainNavigationScreens
import de.gematik.ti.erp.app.settings.ui.AllowAnalyticsScreen
import de.gematik.ti.erp.app.settings.ui.ConfirmationPasswordTextField
import de.gematik.ti.erp.app.settings.ui.PasswordStrength
import de.gematik.ti.erp.app.settings.ui.PasswordTextField
import de.gematik.ti.erp.app.settings.ui.SettingsViewModel
import de.gematik.ti.erp.app.terms.DataProtectionScreen
import de.gematik.ti.erp.app.terms.TermsOfUseScreen
import de.gematik.ti.erp.app.theme.AppTheme
import de.gematik.ti.erp.app.theme.PaddingDefaults
import de.gematik.ti.erp.app.userauthentication.ui.BiometricPrompt
import de.gematik.ti.erp.app.utils.compose.CommonAlertDialog
import de.gematik.ti.erp.app.utils.compose.LargeButton
import de.gematik.ti.erp.app.utils.compose.NavigationAnimation
import de.gematik.ti.erp.app.utils.compose.Spacer16
import de.gematik.ti.erp.app.utils.compose.Spacer24
import de.gematik.ti.erp.app.utils.compose.Spacer4
import de.gematik.ti.erp.app.utils.compose.Spacer40
import de.gematik.ti.erp.app.utils.compose.SpacerMedium
import de.gematik.ti.erp.app.utils.compose.SpacerSmall
import de.gematik.ti.erp.app.utils.compose.SpacerTiny
import de.gematik.ti.erp.app.utils.compose.annotatedStringBold
import de.gematik.ti.erp.app.utils.compose.annotatedStringResource
import de.gematik.ti.erp.app.utils.compose.navigationModeState
import de.gematik.ti.erp.app.utils.compose.testId
import kotlinx.coroutines.launch
import kotlinx.parcelize.Parcelize
import java.util.Locale
import kotlin.math.max
import kotlin.math.roundToInt

object OnboardingNavigationScreens {
    object Onboarding : Route("Onboarding")
    object Analytics : Route("Analytics")
    object TermsOfUse : Route("TermsOfUse")
    object DataProtection : Route("DataProtection")
}

private const val MAX_PAGES = 5
private const val WELCOME_PAGE = 0
private const val FEATURE_PAGE = 1
private const val SECURE_APP_PAGE = 2
private const val ANALYTICS_PAGE = 3
private const val TOS_AND_DATA_PAGE = 4

@Composable
fun ReturningUserSecureAppOnboardingScreen(
    mainNavController: NavController,
    settingsViewModel: SettingsViewModel = hiltViewModel()
) {
    var secureMethod by rememberSaveable { mutableStateOf<SecureAppMethod>(SecureAppMethod.None) }
    val enabled = when {
        secureMethod is SecureAppMethod.DeviceSecurity -> true
        secureMethod is SecureAppMethod.Password -> (secureMethod as? SecureAppMethod.Password)?.let {
            it.checkedPassword != null
        } ?: false
        else -> false
    }

    Scaffold(
        bottomBar = {
            BottomAppBar(backgroundColor = MaterialTheme.colors.surface) {
                Spacer(modifier = Modifier.weight(1f))
                Button(
                    enabled = enabled,
                    onClick = {
                        when (val sm = secureMethod) {
                            is SecureAppMethod.DeviceSecurity ->
                                settingsViewModel.onSelectDeviceSecurityAuthenticationMode()
                            is SecureAppMethod.Password ->
                                settingsViewModel.onSelectPasswordAsAuthenticationMode(requireNotNull(sm.checkedPassword))
                            else -> error("Illegal state. Authentication must be set")
                        }
                        mainNavController.navigate(MainNavigationScreens.Prescriptions.path()) {
                            launchSingleTop = true
                            popUpTo(MainNavigationScreens.ReturningUserSecureAppOnboardingScreen.path()) {
                                inclusive = true
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
            isReturningUser = true,
            onSecureMethodChange = {
                secureMethod = it
            }
        )
    }
}

@Composable
fun OnboardingScreen(
    mainNavController: NavController,
    settingsViewModel: SettingsViewModel = hiltViewModel(
        LocalActivity.current
    )
) {
    val navController = rememberNavController()

    var allowTracking by rememberSaveable { mutableStateOf(false) }

    val navigationMode by navController.navigationModeState(OnboardingNavigationScreens.Onboarding.route)
    NavHost(
        navController,
        startDestination = OnboardingNavigationScreens.Onboarding.route
    ) {
        composable(OnboardingNavigationScreens.Onboarding.route) {
            NavigationAnimation(navigationMode) {
                OnboardingScreenWithScaffold(
                    navController,
                    allowTracking = allowTracking,
                    onAllowTracking = {
                        allowTracking = it
                    },
                    onSaveNewUser = { allowTracking, secureMethod ->
                        when (secureMethod) {
                            is SecureAppMethod.DeviceSecurity ->
                                settingsViewModel.onSelectDeviceSecurityAuthenticationMode()
                            is SecureAppMethod.Password ->
                                settingsViewModel.onSelectPasswordAsAuthenticationMode(requireNotNull(secureMethod.checkedPassword))
                            else -> error("Illegal state. Authentication must be set")
                        }

                        settingsViewModel.isNewUser = false

                        if (allowTracking) {
                            settingsViewModel.onTrackingAllowed()
                        } else {
                            settingsViewModel.onTrackingDisallowed()
                        }
                        mainNavController.navigate(MainNavigationScreens.Prescriptions.path()) {
                            launchSingleTop = true
                            popUpTo(MainNavigationScreens.Onboarding.path()) {
                                inclusive = true
                            }
                        }
                    }
                )
            }
        }
        composable(OnboardingNavigationScreens.Analytics.route) {
            NavigationAnimation(navigationMode) {
                AllowAnalyticsScreen {
                    allowTracking = it
                    navController.popBackStack()
                }
            }
        }
        composable(OnboardingNavigationScreens.TermsOfUse.route) {
            NavigationAnimation(navigationMode) {
                TermsOfUseScreen(navController)
            }
        }
        composable(OnboardingNavigationScreens.DataProtection.route) {
            NavigationAnimation(navigationMode) {
                DataProtectionScreen(navController)
            }
        }
    }
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
private fun OnboardingScreenWithScaffold(
    navController: NavController,
    allowTracking: Boolean,
    onAllowTracking: (Boolean) -> Unit,
    onSaveNewUser: (Boolean, SecureAppMethod) -> Unit
) {
    val context = LocalContext.current

    var tosAndDataToggled by remember { mutableStateOf(false) }
    var secureMethod by rememberSaveable { mutableStateOf<SecureAppMethod>(SecureAppMethod.None) }

    val state = rememberSwipeableState(initialValue = 0)

    var pageSize by remember { mutableStateOf(IntSize(1, 1)) }
    val pageWidth = pageSize.width.toFloat()

    val maxPages = when (secureMethod) {
        is SecureAppMethod.Password -> (secureMethod as? SecureAppMethod.Password)?.let {
            if (it.checkedPassword != null) MAX_PAGES else SECURE_APP_PAGE + 1
        } ?: SECURE_APP_PAGE + 1
        is SecureAppMethod.DeviceSecurity -> MAX_PAGES
        else -> SECURE_APP_PAGE + 1
    }

    val anchors = (0 until maxPages).map {
        pageWidth * it.toFloat() to it
    }.toMap()

    val scope = rememberCoroutineScope()
    BackHandler(enabled = state.currentValue > 0) {
        scope.launch {
            state.animateTo(max(0, state.currentValue - 1))
        }
    }

    Scaffold(modifier = Modifier.testTag("screen_onboarding")) {
        Box {
            Box(
                modifier = Modifier
                    .swipeable(
                        state = state,
                        anchors = anchors,
                        orientation = Orientation.Horizontal,
                        reverseDirection = true
                    )
                    .onSizeChanged {
                        pageSize = it
                    }
                    .fillMaxSize()
            ) {
                Row(
                    modifier = Modifier
                        .offset {
                            IntOffset(-(state.offset.value).roundToInt(), 0)
                        }
                        .wrapContentWidth(align = Alignment.Start, unbounded = true)
                ) {
                    val mod = with(LocalDensity.current) {
                        Modifier.width(pageSize.width.toDp())
                    }

                    OnboardingWelcome(
                        mod.semantics { focused = state.currentValue == 0 },
                        state
                    )
                    OnboardingAppFeatures(mod.semantics { focused = state.currentValue == 2 })
                    OnboardingSecureApp(
                        mod,
                        secureMethod = secureMethod,
                        onSecureMethodChange = {
                            secureMethod = it
                        }
                    )

                    val disAllowToast = stringResource(R.string.settings_tracking_disallow_info)
                    OnboardingPageAnalytics(
                        mod.semantics { focused = state.currentValue == 4 },
                        allowTracking = allowTracking,
                        onAllowTracking = {
                            if (!it) {
                                onAllowTracking(false)
                                Toast.makeText(context, disAllowToast, Toast.LENGTH_SHORT).show()
                            } else {
                                navController.navigate(OnboardingNavigationScreens.Analytics.path())
                            }
                        }
                    )
                    OnboardingPageTerms(
                        mod.semantics { focused = state.currentValue == 3 },
                        navController,
                    ) {
                        tosAndDataToggled = it
                    }
                }
            }

            BottomPager(state)

            OnboardingNextButton(
                Modifier.testId("onb_btn_next"),
                secureMethod,
                tosAndDataToggled,
                state,
                onSaveNewUser = {
                    onSaveNewUser(allowTracking, secureMethod)
                }
            )
        }
    }
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
private fun BoxScope.BottomPager(swipeableState: SwipeableState<Int>) {
    Box(
        modifier = Modifier
            .padding(bottom = 24.dp)
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
        val fraction =
            swipeableState.progress.from + (swipeableState.progress.to - swipeableState.progress.from) * swipeableState.progress.fraction
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

@OptIn(ExperimentalMaterialApi::class, ExperimentalAnimationApi::class)
@Composable
private fun BoxScope.OnboardingNextButton(
    modifier: Modifier = Modifier,
    secureMethod: SecureAppMethod,
    tosAndDataToggled: Boolean,
    swipeableState: SwipeableState<Int>,
    onSaveNewUser: () -> Unit
) {
    val currentPage = swipeableState.currentValue

    val enabled = when {
        currentPage == WELCOME_PAGE || currentPage == FEATURE_PAGE || currentPage == ANALYTICS_PAGE -> true
        currentPage == SECURE_APP_PAGE && secureMethod is SecureAppMethod.DeviceSecurity -> true
        currentPage == SECURE_APP_PAGE && secureMethod is SecureAppMethod.Password -> secureMethod.checkedPassword != null
        tosAndDataToggled && currentPage == TOS_AND_DATA_PAGE -> true
        else -> false
    }

    val coroutineScope = rememberCoroutineScope()
    NextButton(
        onNext = {
            when {
                currentPage == TOS_AND_DATA_PAGE && tosAndDataToggled -> onSaveNewUser()
                currentPage == TOS_AND_DATA_PAGE -> {
                }
                else -> coroutineScope.launch {
                    swipeableState.animateTo(currentPage + 1)
                }
            }
        },
        enabled = enabled,
        modifier = modifier.align(Alignment.BottomEnd)
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
                    stringResource(R.string.on_boarding_page_4_next).uppercase(
                        Locale.getDefault()
                    )
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterialApi::class, ExperimentalAnimationApi::class)
@Composable
private fun NextButton(
    modifier: Modifier = Modifier,
    enabled: Boolean,
    onNext: () -> Unit,
    content: @Composable RowScope.() -> Unit
) {
    val backgroundColor =
        if (enabled) {
            MaterialTheme.colors.secondary
        } else {
            AppTheme.colors.neutral300
        }

    val contentColor = if (enabled) {
        contentColorFor(backgroundColor)
    } else {
        AppTheme.colors.neutral500
    }

    FloatingActionButton(
        onClick = {
            if (enabled) {
                onNext()
            }
        },
        backgroundColor = backgroundColor,
        contentColor = contentColor,
        modifier = modifier
            .testTag("onboarding/next")
            .padding(bottom = 64.dp, end = 24.dp)
            .semantics {
                if (!enabled) {
                    disabled()
                }
            }
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
private fun PeopleLayer(modifier: Modifier) {
    Row(modifier = modifier) {
        Image(
            painterResource(R.drawable.onboarding_boygrannygranpa),
            stringResource(R.string.on_boarding_page_1_acc_image),
            alignment = Alignment.BottomStart,
            modifier = Modifier.fillMaxSize()
        )
    }
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
private fun OnboardingWelcome(modifier: Modifier, swipeableState: SwipeableState<Int>) {
    val flag = painterResource(R.drawable.ic_onboarding_logo_flag)
    val gematik = painterResource(R.drawable.ic_onboarding_logo_gematik)
    val eRpLogo = painterResource(R.drawable.erp_logo)
    val header = stringResource(R.string.app_name)
    val body = stringResource(R.string.on_boarding_page_1_headline)

    Column(
        modifier = modifier.testTag("onboarding/page1")
    ) {
        Row(
            modifier = Modifier
                .padding(start = 24.dp, top = 40.dp)
                .align(Alignment.Start),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Image(flag, null, modifier = Modifier.padding(end = 10.dp))
            Icon(gematik, null, tint = AppTheme.colors.primary900)
        }

        Image(
            eRpLogo, null,
            modifier = Modifier.align(Alignment.CenterHorizontally)
                .padding(top = PaddingDefaults.XLarge)
                .testId("onb_img_erp_logo")
        )

        Text(
            text = header,
            style = MaterialTheme.typography.h4,
            color = AppTheme.colors.primary900,
            fontWeight = FontWeight.W700,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .padding(top = PaddingDefaults.Small)
                .testId("onb_txt_start_title")
        )
        Text(
            text = body,
            style = MaterialTheme.typography.subtitle1,
            color = AppTheme.colors.neutral600,
            fontWeight = FontWeight.W500,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .padding(bottom = 56.dp)
        )

        var peopleHeight by remember { mutableStateOf(0) }

        PeopleLayer(
            Modifier
                .offset {
                    IntOffset(
                        -(swipeableState.offset.value * 1.3f + (peopleHeight / 8)).roundToInt(),
                        0
                    )
                }
                .onSizeChanged { peopleHeight = it.height }
        )
    }
}

@Composable
private fun OnboardingAppFeatures(modifier: Modifier) {
    val image = painterResource(R.drawable.woman_red_shirt_circle_blue)
    val header = stringResource(R.string.on_boarding_page_3_header)

    val imageAcc = stringResource(R.string.on_boarding_page_3_acc_image)
    Column(
        modifier = modifier
            .testTag("onboarding/page3")
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        Image(
            image,
            imageAcc,
            alignment = Alignment.Center,
            modifier = Modifier
                .padding(top = 40.dp)
                .fillMaxWidth()
                .align(Alignment.CenterHorizontally)
        )

        Text(
            text = header,
            style = MaterialTheme.typography.h6,
            color = AppTheme.colors.primary900,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .padding(start = 24.dp, end = 24.dp)
                .testId("onb_txt_features_title")
        )
        Column(
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.padding(top = 8.dp, start = 24.dp, end = 24.dp, bottom = 136.dp)
        ) {
            OnboardingCheck(stringResource(R.string.on_boarding_page_3_info_check_1))
            OnboardingCheck(stringResource(R.string.on_boarding_page_3_info_check_2))
            OnboardingCheck(stringResource(R.string.on_boarding_page_3_info_check_3))
        }
    }
}

@Composable
private fun OnboardingCheck(text: String) {
    Row {
        Icon(Icons.Rounded.CheckCircle, null, tint = AppTheme.colors.green600)
        Spacer16()
        Text(text, style = MaterialTheme.typography.body1)
    }
}

@Composable
private fun OnboardingPageAnalytics(
    modifier: Modifier,
    allowTracking: Boolean,
    onAllowTracking: (Boolean) -> Unit
) {
    val header = stringResource(R.string.on_boarding_page_5_header)
    val subHeader = stringResource(R.string.on_boarding_page_5_sub_header)

    Column(
        modifier = modifier
            .testTag("onboarding/page5")
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        Text(
            text = header,
            style = MaterialTheme.typography.h6,
            color = AppTheme.colors.primary900,
            modifier = Modifier
                .padding(top = 40.dp, start = 24.dp, end = 24.dp, bottom = PaddingDefaults.Small)
                .testId("onb_txt_tracking_headline")
        )
        Text(
            text = subHeader,
            style = MaterialTheme.typography.subtitle1,
            color = AppTheme.colors.neutral999,
            modifier = Modifier
                .padding(
                    top = PaddingDefaults.Medium,
                    start = 24.dp,
                    end = 24.dp,
                    bottom = PaddingDefaults.Small
                )
        )

        val stringBold = stringResource(R.string.on_boarding_page_5_anonym)
        AnalyticsInfo(
            icon = Icons.Rounded.Timeline,
            id = R.string.on_boarding_page_5_info_1,
            stringBold = stringBold
        )
        Spacer16()
        AnalyticsInfo(
            icon = Icons.Rounded.BugReport,
            id = R.string.on_boarding_page_5_info_2,
            stringBold = stringBold
        )
        Spacer16()
        AnalyticsInfo(
            icon = Icons.Rounded.LiveHelp,
            id = R.string.on_boarding_page_5_info_3,
            stringBold = ""
        )
        Spacer40()
        AnalyticsToggle(allowTracking, onAllowTracking)
        SpacerSmall()
        Text(
            stringResource(R.string.on_boarding_page_5_label_info),
            style = AppTheme.typography.body2l,
            color = AppTheme.colors.neutral600,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = PaddingDefaults.Large)
        )
    }
}

@Composable
private fun OnboardingPageTerms(
    modifier: Modifier,
    navController: NavController,
    onBothToggled: (Boolean) -> Unit,
) {
    val header = stringResource(R.string.on_boarding_page_4_header)
    val info = stringResource(R.string.on_boarding_page_4_info)

    Column(
        modifier = modifier
            .testTag("onboarding/page4")
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        Text(
            text = header,
            style = MaterialTheme.typography.h6,
            color = AppTheme.colors.primary900,
            modifier = Modifier
                .padding(top = 40.dp, start = 24.dp, end = 24.dp, bottom = 8.dp)
                .testId("onb_txt_legal_info_title")
        )
        Text(
            text = info,
            style = MaterialTheme.typography.body1,
            modifier = Modifier
                .padding(start = 24.dp, end = 24.dp)
        )

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

        Spacer24()

        Column(
            modifier = Modifier.padding(
                start = 24.dp,
                end = 24.dp,
                bottom = 136.dp
            )
        ) {
            OnboardingToggle(
                stringResource(R.string.on_boarding_page_4_info_dataprotection),
                stringResource(R.string.onb_accept_data),
                toggleTestId = "onb_btn_accept_privacy",
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
                toggleTestId = "onb_btn_accept_terms_of_use",
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
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        modifier = Modifier.padding(horizontal = PaddingDefaults.Large)
    ) {
        Icon(icon, null, tint = AppTheme.colors.primary500)
        Column(modifier = Modifier.weight(1.0f)) {
            Text(
                text = annotatedStringResource(
                    id,
                    annotatedStringBold(stringBold)
                ),
                style = MaterialTheme.typography.body1
            )
        }
    }
}

@Composable
private fun AnalyticsToggle(
    analyticsAllowed: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    val labelText = stringResource(R.string.on_boarding_page_5_label)

    Row(
        modifier = Modifier
            .padding(horizontal = PaddingDefaults.Large)
            .clip(RoundedCornerShape(16.dp))
            .background(AppTheme.colors.neutral100, shape = RoundedCornerShape(16.dp))
            .fillMaxWidth()
            .toggleable(
                value = analyticsAllowed,
                onValueChange = onCheckedChange,
                enabled = true,
                role = Role.Switch,
                interactionSource = remember { MutableInteractionSource() },
                indication = LocalIndication.current
            )
            .padding(PaddingDefaults.Medium)
            .semantics(true) {},
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            labelText,
            style = MaterialTheme.typography.subtitle1,
            modifier = Modifier.weight(1f)
        )
        SpacerSmall()
        Switch(
            checked = analyticsAllowed,
            onCheckedChange = null,
        )
    }
}

private sealed class SecureAppMethod {
    @Parcelize
    data class Password(val password: String, val repeatedPassword: String) : SecureAppMethod(), Parcelable {
        val checkedPassword: String?
            get() =
                if (password == repeatedPassword && password.isNotEmpty()) {
                    password
                } else {
                    null
                }
    }

    @Parcelize
    object DeviceSecurity : SecureAppMethod(), Parcelable

    @Parcelize
    object None : SecureAppMethod(), Parcelable
}

@OptIn(ExperimentalAnimationApi::class)
@Composable
private fun OnboardingSecureApp(
    modifier: Modifier,
    isReturningUser: Boolean = false,
    secureMethod: SecureAppMethod,
    onSecureMethodChange: (SecureAppMethod) -> Unit
) {
    val password = remember(secureMethod) { (secureMethod as? SecureAppMethod.Password)?.password ?: "" }
    val repeatedPassword =
        remember(secureMethod) { (secureMethod as? SecureAppMethod.Password)?.repeatedPassword ?: "" }

    var passwordFieldIsFocused by remember { mutableStateOf(false) }
    val extendPassword = passwordFieldIsFocused || password.isNotEmpty()

    val header = stringResource(R.string.on_boarding_secure_app_page_header)
    val info = stringResource(R.string.on_boarding_secure_app_page_info)

    Column(
        modifier = modifier
            .testTag("onboarding/secureAppPage")
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = PaddingDefaults.Large, vertical = PaddingDefaults.XXLarge)
    ) {

        if (isReturningUser) {
            Image(
                painterResource(R.drawable.laptop_woman_blue),
                null,
                contentScale = ContentScale.FillWidth,
                modifier = Modifier.fillMaxSize()
            )
            SpacerMedium()
        }

        Text(
            text = header,
            style = MaterialTheme.typography.h6,
            color = AppTheme.colors.primary900,
            textAlign = TextAlign.Center
        )

        if (isReturningUser) {
            SpacerMedium()

            Text(
                text = info,
                style = MaterialTheme.typography.body1,
                color = AppTheme.colors.neutral999,
            )
        }

        Spacer(modifier = Modifier.height(PaddingDefaults.XXLarge))

        val focusRequester = FocusRequester.Default
        val focusManager = LocalFocusManager.current

        PasswordTextField(
            modifier = Modifier
                .fillMaxWidth()
                .onFocusChanged {
                    passwordFieldIsFocused = it.isFocused
                },
            value = password,
            onValueChange = {
                if (it.isEmpty()) {
                    onSecureMethodChange(SecureAppMethod.None)
                } else {
                    onSecureMethodChange(SecureAppMethod.Password(password = it, repeatedPassword = ""))
                }
            },
            onSubmit = { focusRequester.requestFocus() },
            allowAutofill = true,
            allowVisiblePassword = true,
            label = {
                Text(stringResource(R.string.settings_password_enter_password))
            }
        )
        AnimatedVisibility(visible = extendPassword) {
            Column {
                SpacerTiny()
                PasswordStrength(modifier = Modifier.fillMaxWidth(), password = password)

                SpacerMedium()

                ConfirmationPasswordTextField(
                    modifier = Modifier.fillMaxWidth().focusRequester(focusRequester),
                    password = password,
                    value = repeatedPassword,
                    onValueChange = {
                        onSecureMethodChange(SecureAppMethod.Password(password = password, repeatedPassword = it))
                    },
                    onSubmit = { focusManager.clearFocus() }
                )
            }
        }
        Row(
            modifier = Modifier.padding(vertical = PaddingDefaults.XXLarge),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Divider(modifier = Modifier.weight(0.5f))
            Text(
                stringResource(R.string.onboarding_secure_app_or).uppercase(Locale.getDefault()),
                modifier = Modifier.padding(horizontal = 12.dp),
                style = AppTheme.typography.body2l,
                fontWeight = FontWeight.Medium
            )
            Divider(modifier = Modifier.weight(0.5f))
        }

        var showBiometricPrompt by rememberSaveable { mutableStateOf(false) }
        var showAcceptDeviceAuthenticationInfo by rememberSaveable { mutableStateOf(false) }

        if (showAcceptDeviceAuthenticationInfo) {
            CommonAlertDialog(
                header = stringResource(R.string.settings_biometric_dialog_title),
                info = stringResource(R.string.settings_biometric_dialog_text),
                actionText = stringResource(R.string.settings_device_security_allow),
                cancelText = stringResource(R.string.cancel),
                onCancel = { showAcceptDeviceAuthenticationInfo = false },
                onClickAction = {
                    showBiometricPrompt = true
                    showAcceptDeviceAuthenticationInfo = false
                }
            )
        }

        if (showBiometricPrompt) {
            BiometricPrompt(
                authenticationMethod = SettingsAuthenticationMethod.DeviceSecurity,
                title = stringResource(R.string.auth_prompt_headline),
                description = "",
                negativeButton = stringResource(R.string.auth_prompt_cancel),
                onAuthenticated = {
                    onSecureMethodChange(SecureAppMethod.DeviceSecurity)
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

        val buttonColors = if (secureMethod == SecureAppMethod.DeviceSecurity) {
            ButtonDefaults.buttonColors(
                backgroundColor = AppTheme.colors.green600,
                contentColor = AppTheme.colors.neutral000
            )
        } else {
            ButtonDefaults.buttonColors()
        }

        LargeButton(
            onClick = {
                showAcceptDeviceAuthenticationInfo = true
            },
            colors = buttonColors
        ) {
            if (secureMethod == SecureAppMethod.DeviceSecurity) {
                Icon(Icons.Rounded.Check, null)
                SpacerSmall()
                Text(stringResource(R.string.onboarding_secure_app_button_best_chosen).uppercase(Locale.getDefault()))
            } else {
                Text(stringResource(R.string.onboarding_secure_app_button_best).uppercase(Locale.getDefault()))
            }
        }
        SpacerSmall()
        Text(stringResource(R.string.onboarding_secure_app_button_best_info), style = AppTheme.typography.body2l)
    }
}

@Composable
private fun OnboardingToggle(
    which: String,
    toggleContentDescription: String,
    toggleTestId: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    onClickInfo: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 24.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        val info = annotatedStringResource(
            R.string.on_boarding_page_4_info_accept_info,
            buildAnnotatedString {
                pushStringAnnotation("CLICKABLE", "")
                pushStyle(SpanStyle(color = AppTheme.colors.primary500))
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
            style = MaterialTheme.typography.body1,
            modifier = Modifier
                .weight(1f)
                .clickable(
                    onClickLabel = which,
                    indication = null,
                    interactionSource = remember { MutableInteractionSource() },
                    onClick = onClickInfo
                )
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
                        radius = 24.dp
                    )
                )
                .testTag(toggleTestId)
                .testId(toggleTestId)
                .semantics {
                    contentDescription = toggleContentDescription
                },
            contentAlignment = Alignment.Center
        ) {
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
