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

package de.gematik.ti.erp.app.analytics.ui

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavController
import de.gematik.ti.erp.app.Requirement
import de.gematik.ti.erp.app.TestTag
import de.gematik.ti.erp.app.features.R
import de.gematik.ti.erp.app.navigation.Screen
import de.gematik.ti.erp.app.onboarding.presentation.rememberOnboardingController
import de.gematik.ti.erp.app.onboarding.ui.OnboardingBottomBar
import de.gematik.ti.erp.app.theme.AppTheme
import de.gematik.ti.erp.app.theme.PaddingDefaults
import de.gematik.ti.erp.app.utils.compose.AnimatedElevationScaffold
import de.gematik.ti.erp.app.utils.compose.LightDarkPreview
import de.gematik.ti.erp.app.utils.compose.NavigationBarMode
import de.gematik.ti.erp.app.utils.compose.PreviewAppTheme
import de.gematik.ti.erp.app.utils.compose.SpacerLarge
import de.gematik.ti.erp.app.utils.compose.SpacerMedium
import de.gematik.ti.erp.app.utils.compose.SpacerSmall
import de.gematik.ti.erp.app.utils.compose.annotatedStringBold
import de.gematik.ti.erp.app.utils.compose.annotatedStringResource
import de.gematik.ti.erp.app.utils.compose.shortToast

@Requirement(
    "A_19087",
    "A_19088#1",
    "A_19091#1",
    "A_19092",
    "A_19181-01#1",
    sourceSpecification = "gemSpec_eRp_FdV",
    rationale = "Display opt-in for analytics. Full app functionality is available also without opting in."
)
class OnboardingAllowAnalyticsScreen(
    override val navController: NavController,
    override val navBackStackEntry: NavBackStackEntry
) : Screen() {
    @Composable
    override fun Content() {
        val onboardingController = rememberOnboardingController()
        val context = LocalContext.current
        val allowStars = stringResource(R.string.settings_tracking_allow_emoji)
        val allowText = annotatedStringResource(
            R.string.settings_tracking_allow_info,
            annotatedStringBold(allowStars)
        ).text
        val disallowText = stringResource(R.string.settings_tracking_disallow_info)
        val lazyListState = rememberLazyListState()

        AnimatedElevationScaffold(
            modifier = Modifier.navigationBarsPadding(),
            navigationMode = NavigationBarMode.Back,
            topBarTitle = stringResource(R.string.settings_tracking_allow_title),
            onBack = {
                onboardingController.changeAnalyticsState(false)
                context.shortToast(disallowText)
                navController.popBackStack()
            },
            listState = lazyListState,
            bottomBar = {
                UserConfirmationBottomBar(
                    onClick = {
                        onboardingController.changeAnalyticsState(true)
                        context.shortToast(allowText)
                        navController.popBackStack()
                    }
                )
            }
        ) {
            AllowAnalyticsContent(
                lazyListState = lazyListState,
                paddingValues = it
            )
        }
    }
}

@Requirement(
    "A_19091#2",
    sourceSpecification = "gemSpec_eRp_FdV",
    rationale = "User confirms the opt in"
)
@Composable
private fun UserConfirmationBottomBar(
    onClick: () -> Unit
) {
    OnboardingBottomBar(
        buttonText = stringResource(R.string.settings_tracking_allow_button),
        onButtonClick = onClick,
        buttonEnabled = true,
        info = null,
        buttonModifier = Modifier.testTag(TestTag.Onboarding.Analytics.AcceptAnalyticsButton)
    )
}

@Requirement(
    "A_19089#1",
    "A_19090",
    sourceSpecification = "gemSpec_eRp_FdV",
    rationale = "Display explanation of data processing for analytics opt-in"
)
@Composable
private fun AllowAnalyticsContent(
    lazyListState: LazyListState,
    paddingValues: PaddingValues
) {
    LazyColumn(
        state = lazyListState,
        modifier = Modifier
            .wrapContentSize()
            .padding(horizontal = PaddingDefaults.Medium)
            .padding(bottom = paddingValues.calculateBottomPadding())
            .testTag(TestTag.Onboarding.Analytics.ScreenContent)
    ) {
        item {
            SpacerMedium()
        }
        item {
            Text(
                stringResource(R.string.settings_tracking_dialog_title),
                style = AppTheme.typography.h6
            )
        }
        item {
            SpacerLarge()
        }
        item {
            Text(
                stringResource(R.string.settings_tracking_dialog_text_1),
                style = AppTheme.typography.body1
            )
        }
        item {
            SpacerSmall()
        }
        item {
            Text(
                stringResource(R.string.settings_tracking_dialog_text_2),
                style = AppTheme.typography.body1
            )
        }
        item {
            SpacerSmall()
        }
        item {
            Text(
                stringResource(R.string.settings_tracking_dialog_text_3),
                style = AppTheme.typography.body1
            )
        }
        item {
            SpacerMedium()
        }
    }
}

@LightDarkPreview
@Composable
fun AllowAnalyticsContentPreview() {
    val state = rememberLazyListState()
    PreviewAppTheme {
        AllowAnalyticsContent(
            lazyListState = state,
            paddingValues = PaddingValues(horizontal = PaddingDefaults.Medium)
        )
    }
}

@LightDarkPreview
@Composable
fun UserConfirmationBottomBarPreview() {
    PreviewAppTheme {
        UserConfirmationBottomBar(
            onClick = {}
        )
    }
}
