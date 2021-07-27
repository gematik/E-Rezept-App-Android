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

import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.annotation.StringRes
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.Animatable
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.FloatingActionButton
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Scaffold
import androidx.compose.material.Surface
import androidx.compose.material.SwipeableState
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
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
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import de.gematik.ti.erp.app.R
import de.gematik.ti.erp.app.core.AppModel
import de.gematik.ti.erp.app.core.LocalFragmentNavController
import de.gematik.ti.erp.app.settings.ui.AllowAnalyticsScreen
import de.gematik.ti.erp.app.settings.ui.SettingsViewModel
import de.gematik.ti.erp.app.theme.AppTheme
import de.gematik.ti.erp.app.theme.PaddingDefaults
import de.gematik.ti.erp.app.utils.compose.LabeledSwitch
import de.gematik.ti.erp.app.utils.compose.NavigationAnimation
import de.gematik.ti.erp.app.utils.compose.Spacer16
import de.gematik.ti.erp.app.utils.compose.Spacer24
import de.gematik.ti.erp.app.utils.compose.Spacer4
import de.gematik.ti.erp.app.utils.compose.Spacer40
import de.gematik.ti.erp.app.utils.compose.Spacer8
import de.gematik.ti.erp.app.utils.compose.annotatedStringBold
import de.gematik.ti.erp.app.utils.compose.annotatedStringResource
import de.gematik.ti.erp.app.utils.compose.navigationModeState
import de.gematik.ti.erp.app.utils.compose.testId
import kotlinx.coroutines.launch
import java.util.Locale
import kotlin.math.max
import kotlin.math.roundToInt

sealed class OnboardingNavigationScreens(
    val route: String
) {
    object Onboarding : OnboardingNavigationScreens("Onboarding")
    object AllowAnalytics : OnboardingNavigationScreens("AllowAnalytics")
}

private const val MAX_PAGES = 5
private const val TOS_AND_DATA_PAGE = 3

@Composable
fun OnboardingScreen(settingsViewModel: SettingsViewModel, onSaveNewUser: () -> Unit) {
    val navController = rememberNavController()

    val navigationMode by navController.navigationModeState(OnboardingNavigationScreens.Onboarding.route)
    NavHost(
        navController,
        startDestination = OnboardingNavigationScreens.Onboarding.route
    ) {
        composable(OnboardingNavigationScreens.Onboarding.route) {
            NavigationAnimation(navigationMode) {
                OnboardingScreenWithScaffold(settingsViewModel, navController, onSaveNewUser)
            }
        }
        composable(OnboardingNavigationScreens.AllowAnalytics.route) {
            NavigationAnimation(navigationMode) {
                AllowAnalyticsScreen(
                    settingsViewModel,
                    navController
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
private fun OnboardingScreenWithScaffold(
    settingsViewModel: SettingsViewModel,
    navController: NavController,
    onSaveNewUser: () -> Unit
) {
    var tosAndDataToggled by remember { mutableStateOf(false) }

    val state = rememberSwipeableState(
        initialValue = 0,
        confirmStateChange = {
            it <= TOS_AND_DATA_PAGE || (it > TOS_AND_DATA_PAGE && tosAndDataToggled)
        }
    )
    var pageSize by remember { mutableStateOf(IntSize(1, 1)) }
    val pageWidth = pageSize.width.toFloat()
    val anchors = (0 until MAX_PAGES).map {
        pageWidth * it.toFloat() to it
    }.toMap()

    val scope = rememberCoroutineScope()
    BackHandler(enabled = state.currentValue > 0) {
        scope.launch {
            state.animateTo(max(0, state.currentValue - 1))
        }
    }

    Scaffold {
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
                var page1Height by remember { mutableStateOf(1) }
                var page2Height by remember { mutableStateOf(1) }

                Row(
                    modifier = Modifier
                        .offset {
                            IntOffset(-(state.offset.value).roundToInt(), 0)
                        }
                        .wrapContentWidth(align = Alignment.Start, unbounded = true)
                ) {
                    with(LocalDensity.current) {
                        val mod = Modifier.width(pageSize.width.toDp())

                        OnboardingPage1(
                            mod
                                .semantics { focused = state.currentValue == 0 }
                                .onSizeChanged { page1Height = it.height }
                        )
                        OnboardingPage2(
                            mod
                                .semantics { focused = state.currentValue == 1 }
                                .onSizeChanged { page2Height = it.height }
                        )
                        OnboardingPage3(mod.semantics { focused = state.currentValue == 2 })
                        OnboardingPage4(
                            mod.semantics { focused = state.currentValue == 3 },
                            state.currentValue != TOS_AND_DATA_PAGE
                        ) {
                            tosAndDataToggled = it
                        }
                        OnboardingPage5(
                            mod.semantics { focused = state.currentValue == 4 }, settingsViewModel, navController
                        )
                    }
                }

                with(LocalDensity.current) {
                    val maxContentHeight = max(page1Height, page2Height).toDp()
                    val w = pageSize.width.toDp() * 2f * 1.3f
                    val h = pageSize.height.toDp() - maxContentHeight

                    var peopleHeight by remember { mutableStateOf(0) }

                    if (h > 100.dp) {
                        Box(
                            modifier = Modifier
                                .offset {
                                    IntOffset(
                                        -(state.offset.value * 1.3f + (peopleHeight / 8)).roundToInt(),
                                        0
                                    )
                                }
                                .align(Alignment.BottomStart)
                                .wrapContentWidth(align = Alignment.Start, unbounded = true)
                        ) {

                            PeopleLayer(
                                Modifier
                                    .size(w, h)
                                    .onSizeChanged { peopleHeight = it.height }
                            )
                        }
                    }
                }
            }

            Surface(
                shape = RoundedCornerShape(8.dp),
                color = AppTheme.colors.neutral100.copy(alpha = 0.5f),
                modifier = Modifier
                    .padding(bottom = 24.dp)
                    .align(Alignment.BottomCenter)
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.padding(8.dp)
                ) {
                    repeat(MAX_PAGES) {
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = if (state.currentValue == it) {
                                AppTheme.colors.primary500
                            } else {
                                AppTheme.colors.neutral300
                            },
                            modifier = Modifier
                                .size(8.dp)
                        ) {}
                    }
                }
            }

            NextButton(
                Modifier.testId("onb_btn_next"),
                tosAndDataToggled,
                state,
                onSaveNewUser
            )
        }
    }
}

@OptIn(ExperimentalMaterialApi::class, ExperimentalAnimationApi::class)
@Composable
private fun BoxScope.NextButton(
    modifier: Modifier = Modifier,
    tosAndDataToggled: Boolean,
    state: SwipeableState<Int>,
    onSaveNewUser: () -> Unit
) {
    val frNavCtr = AppModel.frNavController
    val cdnFabNext = stringResource(R.string.on_boarding_cdn_btn_next)
    val cdnFabAccept = stringResource(R.string.on_boarding_cdn_btn_accept)

    val currentPage = state.currentValue

    val backgroundColor =
        if (tosAndDataToggled || currentPage != TOS_AND_DATA_PAGE) {
            MaterialTheme.colors.secondary
        } else {
            AppTheme.colors.neutral300
        }

    val contentColor = if (tosAndDataToggled || currentPage != TOS_AND_DATA_PAGE) {
        contentColorFor(backgroundColor)
    } else {
        AppTheme.colors.neutral500
    }

    val coroutineScope = rememberCoroutineScope()
    FloatingActionButton(
        onClick = {
            if (currentPage == TOS_AND_DATA_PAGE && tosAndDataToggled) {
                coroutineScope.launch {
                    state.animateTo(currentPage + 1)
                }
            } else if (currentPage == MAX_PAGES - 1) {
                onSaveNewUser()
                frNavCtr.navigate(OnboardingFragmentDirections.actionOnboardingFragmentToMainScreenFragment())
            } else if (currentPage != TOS_AND_DATA_PAGE) {
                coroutineScope.launch {
                    state.animateTo(currentPage + 1)
                }
            }
        },
        backgroundColor = backgroundColor,
        contentColor = contentColor,
        modifier = modifier
            .padding(bottom = 64.dp, end = 24.dp)
            .align(Alignment.BottomEnd)
            .semantics {
                contentDescription = when (currentPage >= TOS_AND_DATA_PAGE - 1) {
                    true -> cdnFabAccept
                    false -> cdnFabNext
                }
                if (!tosAndDataToggled && currentPage == TOS_AND_DATA_PAGE) {
                    disabled()
                }
            }
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
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
            AnimatedVisibility(
                visible = currentPage == MAX_PAGES - 1
            ) {
                Row {
                    Spacer4()
                    Text(
                        stringResource(R.string.on_boarding_page_5_next).uppercase(
                            Locale.getDefault()
                        )
                    )
                }
            }
        }
    }
}

@Composable
private fun PeopleLayer(modifier: Modifier) {
    val p1Group = painterResource(R.drawable.onboarding_boygrannygranpa)
    val p2Pharmacist = painterResource(R.drawable.onboarding_pharmacist)

    val p1GroupAcc = stringResource(R.string.on_boarding_page_1_acc_image)
    val p2PharmacistAcc = stringResource(R.string.on_boarding_page_2_acc_image)

    Row(modifier = modifier) {
        Image(
            p1Group,
            p1GroupAcc,
            alignment = Alignment.BottomStart,
            modifier = Modifier
                .weight(0.5f)
                .fillMaxHeight()
        )
        Image(
            p2Pharmacist,
            p2PharmacistAcc,
            alignment = Alignment.BottomStart,
            modifier = Modifier
                .weight(0.5f)
                .fillMaxHeight()
        )
    }
}

@Composable
private fun OnboardingPage1(modifier: Modifier) {
    val flag = painterResource(R.drawable.ic_onboarding_logo_flag)
    val gematik = painterResource(R.drawable.ic_onboarding_logo_gematik)
    val header = stringResource(R.string.app_name)
    val body = stringResource(R.string.on_boarding_page_1_header)

    Column(
        modifier = modifier
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

        Text(
            text = header,
            style = MaterialTheme.typography.h4,
            color = AppTheme.colors.primary900,
            fontWeight = FontWeight.W700,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .padding(top = 56.dp)
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
    }
}

@Composable
private fun OnboardingPage2(modifier: Modifier) {
    val header = stringResource(R.string.on_boarding_page_2_header)
    val body = stringResource(R.string.on_boarding_page_2_info)
    Column(
        modifier = modifier
    ) {
        Text(
            text = header,
            style = MaterialTheme.typography.h6,
            color = AppTheme.colors.primary900,
            modifier = Modifier
                .padding(top = 40.dp, start = 24.dp, end = 24.dp, bottom = 8.dp)
                .testId("onb_txt_welcome_title")
        )
        Text(
            text = body,
            style = MaterialTheme.typography.body1,
            modifier = Modifier
                .padding(bottom = 56.dp, start = 24.dp, end = 24.dp)
        )
    }
}

@Composable
private fun OnboardingPage3(modifier: Modifier) {
    val image = painterResource(R.drawable.onboarding_healthcard)
    val header = stringResource(R.string.on_boarding_page_3_header)

    val imageAcc = stringResource(R.string.on_boarding_page_3_acc_image)
    Column(
        modifier = modifier
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
private fun OnboardingPage4(
    modifier: Modifier,
    disableToggles: Boolean,
    onBothToggled: (Boolean) -> Unit,
) {
    val header = stringResource(R.string.on_boarding_page_4_header)
    val info = stringResource(R.string.on_boarding_page_4_info)

    val frNavController = LocalFragmentNavController.current

    Column(
        modifier = modifier
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
                    if (!disableToggles) {
                        checkedDataProtection = it
                    }
                },
                onClickInfo = {
                    frNavController.navigate(OnboardingFragmentDirections.actionOnboardingFragmentToDataTermsFragment())
                }
            )
            OnboardingToggle(
                stringResource(R.string.on_boarding_page_4_info_tos),
                stringResource(R.string.onb_accept_tos),
                toggleTestId = "onb_btn_accept_terms_of_use",
                checked = checkedTos,
                onCheckedChange = {
                    if (!disableToggles) {
                        checkedTos = it
                    }
                },
                onClickInfo = {
                    frNavController.navigate(OnboardingFragmentDirections.actionOnboardingFragmentToTermsOfUseFragment())
                }
            )
        }
    }
}

@Composable
private fun OnboardingPage5(
    modifier: Modifier,
    settingsViewModel: SettingsViewModel,
    navController: NavController
) {
    val header = stringResource(R.string.on_boarding_page_5_header)
    val subHeader = stringResource(R.string.on_boarding_page_5_sub_header)

    Column(
        modifier = modifier
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
        Row(
            modifier = Modifier
                .padding(horizontal = PaddingDefaults.Large)
                .background(color = AppTheme.colors.neutral100, shape = RoundedCornerShape(16.dp))
        ) {
            val context = LocalContext.current
            val disAllowToast = stringResource(R.string.settings_tracking_disallow_info)
            AnalyticsToggle(settingsViewModel.screenState.analyticsAllowed, navController) {
                if (!it) {
                    settingsViewModel.onTrackingDisallowed()
                    Toast.makeText(context, disAllowToast, Toast.LENGTH_SHORT).show()
                }
            }
        }
        Spacer8()
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
    navController: NavController,
    onCheckedChange: (Boolean) -> Unit
) {
    var showAllowAnalytics by remember { mutableStateOf(false) }

    val labelText = stringResource(R.string.on_boarding_page_5_label)

    LabeledSwitch(
        checked = analyticsAllowed,
        onCheckedChange = {
            showAllowAnalytics = it
            if (!it) {
                onCheckedChange(false)
            }
        },
        modifier = Modifier.padding(horizontal = PaddingDefaults.Large)
    ) {
        Text(
            labelText,
            style = MaterialTheme.typography.subtitle1,
            modifier = Modifier.padding(end = PaddingDefaults.Medium)
        )
    }

    if (showAllowAnalytics) {
        navController.navigate(OnboardingNavigationScreens.AllowAnalytics.route)
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
