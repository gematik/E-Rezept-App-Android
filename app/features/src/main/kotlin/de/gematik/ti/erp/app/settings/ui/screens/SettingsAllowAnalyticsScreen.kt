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

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.semantics
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavController
import de.gematik.ti.erp.app.Requirement
import de.gematik.ti.erp.app.TestTag
import de.gematik.ti.erp.app.features.R
import de.gematik.ti.erp.app.navigation.Screen
import de.gematik.ti.erp.app.onboarding.ui.OnboardingBottomBar
import de.gematik.ti.erp.app.settings.presentation.rememberAnalyticsSettingsController
import de.gematik.ti.erp.app.theme.AppTheme
import de.gematik.ti.erp.app.theme.PaddingDefaults
import de.gematik.ti.erp.app.utils.compose.AnimatedElevationScaffold
import de.gematik.ti.erp.app.utils.compose.LightDarkPreview
import de.gematik.ti.erp.app.utils.compose.NavigationBarMode
import de.gematik.ti.erp.app.utils.compose.annotatedStringBold
import de.gematik.ti.erp.app.utils.compose.annotatedStringResource
import de.gematik.ti.erp.app.utils.compose.preview.PreviewAppTheme
import de.gematik.ti.erp.app.utils.compose.shortToast

@Requirement(
    "A_19091-01#5",
    "A_19092-01#2",
    sourceSpecification = "gemSpec_eRp_FdV",
    rationale = "Display opt-in for analytics. Full app functionality is available also without opting in."
)
class SettingsAllowAnalyticsScreen(
    override val navController: NavController,
    override val navBackStackEntry: NavBackStackEntry
) : Screen() {
    @Composable
    override fun Content() {
        val settingsController = rememberAnalyticsSettingsController()
        val context = LocalContext.current
        val allowStars = stringResource(R.string.settings_tracking_allow_emoji)
        val allowText = annotatedStringResource(
            R.string.settings_tracking_allow_info,
            annotatedStringBold(allowStars)
        ).toString()
        val disAllowToast = stringResource(R.string.settings_tracking_disallow_info)
        val topBarTitle = stringResource(R.string.settings_tracking_allow_title)

        SettingsAllowAnalyticsScaffoldContent(
            allowText = allowText,
            disAllowToast = disAllowToast,
            topBarTitle = topBarTitle,
            onBack = {
                settingsController.changeAnalyticsAllowedState(false)
                context.shortToast(disAllowToast)
                navController.popBackStack()
            },
            onAllowClick = {
                settingsController.changeAnalyticsAllowedState(true)
                context.shortToast(allowText)
                navController.popBackStack()
            }
        )
    }
}

@Composable
fun SettingsAllowAnalyticsScaffoldContent(
    allowText: String,
    disAllowToast: String,
    topBarTitle: String,
    onBack: () -> Unit,
    onAllowClick: () -> Unit,
    contentPadding: PaddingValues = PaddingValues()
) {
    val lazyListState = rememberLazyListState()
    val context = LocalContext.current
    AnimatedElevationScaffold(
        modifier = Modifier.navigationBarsPadding(),
        navigationMode = NavigationBarMode.Back,
        topBarTitle = topBarTitle,
        onBack = {
            context.shortToast(disAllowToast)
            onBack()
        },
        listState = lazyListState,
        bottomBar = {
            @Requirement(
                "A_19091-01#4",
                sourceSpecification = "gemSpec_eRp_FdV",
                rationale = "User confirms the opt in"
            )
            OnboardingBottomBar(
                buttonText = stringResource(R.string.settings_tracking_allow_button),
                onButtonClick = {
                    context.shortToast(allowText)
                    onAllowClick()
                },
                buttonEnabled = true,
                info = null,
                buttonModifier = Modifier.testTag(TestTag.Onboarding.Analytics.AcceptAnalyticsButton)
            )
        }
    ) {
        LazyColumn(
            state = lazyListState,
            modifier = Modifier
                .wrapContentSize()
                .padding(horizontal = PaddingDefaults.Medium)
                .padding(bottom = contentPadding.calculateBottomPadding())
                .testTag(TestTag.Onboarding.Analytics.ScreenContent)
        ) {
            item {
                @Requirement(
                    "A_19090-01#3",
                    sourceSpecification = "gemSpec_eRp_FdV",
                    rationale = "Display explanation of data processing for analytics opt-in"
                )
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .wrapContentHeight()
                        .semantics(mergeDescendants = true) {}
                ) {
                    Text(
                        stringResource(R.string.settings_tracking_dialog_title),
                        style = AppTheme.typography.h6,
                        modifier = Modifier.padding(
                            top = PaddingDefaults.Medium,
                            bottom = PaddingDefaults.Large
                        )
                    )
                    Text(
                        stringResource(R.string.settings_tracking_dialog_text_1),
                        style = AppTheme.typography.body1,
                        modifier = Modifier.padding(bottom = PaddingDefaults.Small)
                    )
                    Text(
                        stringResource(R.string.settings_tracking_dialog_text_2),
                        style = AppTheme.typography.body1,
                        modifier = Modifier.padding(bottom = PaddingDefaults.Small)
                    )
                    Text(
                        stringResource(R.string.settings_tracking_dialog_text_3),
                        style = AppTheme.typography.body1,
                        modifier = Modifier.padding(bottom = PaddingDefaults.Medium)
                    )
                }
            }
        }
    }
}

@LightDarkPreview
@Composable
fun SettingsAllowAnalyticsScaffoldContentPreview() {
    PreviewAppTheme {
        SettingsAllowAnalyticsScaffoldContent(
            allowText = stringResource(R.string.settings_tracking_allow_emoji),
            disAllowToast = stringResource(R.string.settings_tracking_disallow_info),
            topBarTitle = stringResource(R.string.settings_tracking_allow_title),
            onBack = {},
            onAllowClick = {}
        )
    }
}
