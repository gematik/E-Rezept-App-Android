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

package de.gematik.ti.erp.app.onboarding.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavController
import de.gematik.ti.erp.app.Requirement
import de.gematik.ti.erp.app.TestTag
import de.gematik.ti.erp.app.core.R
import de.gematik.ti.erp.app.navigation.Screen
import de.gematik.ti.erp.app.onboarding.navigation.finishOnboardingAsSuccessAndOpenPrescriptions
import de.gematik.ti.erp.app.onboarding.presentation.OnboardingGraphController
import de.gematik.ti.erp.app.shared.ui.screens.components.AllowAnalyticsContent
import de.gematik.ti.erp.app.utils.compose.AnimatedElevationScaffold
import de.gematik.ti.erp.app.utils.compose.LightDarkPreview
import de.gematik.ti.erp.app.utils.compose.NavigationBarMode
import de.gematik.ti.erp.app.utils.compose.annotatedStringBold
import de.gematik.ti.erp.app.utils.compose.annotatedStringResource
import de.gematik.ti.erp.app.utils.compose.preview.PreviewAppTheme
import de.gematik.ti.erp.app.utils.compose.preview.TestScaffold
import de.gematik.ti.erp.app.utils.compose.shortToast

@Requirement(
    "A_19091-01#3",
    "A_19092-01#3",
    sourceSpecification = "gemSpec_eRp_FdV",
    rationale = "Display opt-in for analytics. Full app functionality is available also without opting in."
)
class OnboardingAllowAnalyticsScreen(
    override val navController: NavController,
    override val navBackStackEntry: NavBackStackEntry,
    private val graphController: OnboardingGraphController
) : Screen() {
    @Composable
    override fun Content() {
        val context = LocalContext.current
        val isCreatingProfile by graphController.isCreatingProfile.collectAsStateWithLifecycle()
        val allowStars = stringResource(R.string.settings_tracking_allow_emoji)
        val allowText = annotatedStringResource(
            R.string.settings_tracking_allow_info,
            annotatedStringBold(allowStars)
        ).text
        val disallowText = stringResource(R.string.settings_tracking_disallow_info)
        val lazyListState = rememberLazyListState()

        AnimatedElevationScaffold(
            modifier = Modifier.navigationBarsPadding(),
            backLabel = stringResource(R.string.back),
            closeLabel = stringResource(R.string.cancel),
            navigationMode = NavigationBarMode.Back,
            topBarTitle = "",
            onBack = {
                if (!isCreatingProfile) {
                    graphController.changeAnalyticsState(false)
                    context.shortToast(disallowText)
                    navController.popBackStack()
                }
            },
            listState = lazyListState,
            bottomBar = {
                @Requirement(
                    "O.Data_6#4",
                    sourceSpecification = "BSI-eRp-ePA",
                    rationale = "...user opts-in for analytics."
                )
                UserConfirmationBottomBar(
                    isCreatingProfile = isCreatingProfile,
                    onClickAccept = {
                        graphController.changeAnalyticsState(true)
                        context.shortToast(allowText)
                        graphController.createProfile()
                        navController.finishOnboardingAsSuccessAndOpenPrescriptions()
                    },
                    onClickReject = {
                        graphController.changeAnalyticsState(false)
                        context.shortToast(disallowText)
                        graphController.createProfile()
                        navController.finishOnboardingAsSuccessAndOpenPrescriptions()
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
    "A_19091-01#2",
    sourceSpecification = "gemSpec_eRp_FdV",
    rationale = "User confirms the opt in"
)
@Composable
private fun UserConfirmationBottomBar(
    isCreatingProfile: Boolean = false,
    onClickAccept: () -> Unit,
    onClickReject: () -> Unit
) {
    Column {
        OnboardingBottomBar(
            info = null,
            buttonText = stringResource(R.string.onboarding_analytics_agree_button),
            buttonEnabled = !isCreatingProfile,
            includeBottomSpacer = false,
            modifier = Modifier.testTag(TestTag.Onboarding.Analytics.AcceptAnalyticsButton),
            onButtonClick = onClickAccept
        )
        OnboardingBottomBar(
            info = null,
            buttonText = stringResource(R.string.onboarding_analytics_reject_button),
            modifier = Modifier.testTag(TestTag.Onboarding.RejectButton),
            buttonEnabled = !isCreatingProfile,
            onButtonClick = onClickReject
        )
    }
}

@LightDarkPreview
@Composable
fun AllowAnalyticsScreenScaffoldPreview() {
    val lazyListState = rememberLazyListState()

    PreviewAppTheme {
        TestScaffold(
            navigationMode = NavigationBarMode.Back,
            topBarTitle = "",
            bottomBar = {
                UserConfirmationBottomBar(onClickAccept = { }, onClickReject = { })
            }
        ) {
            AllowAnalyticsContent(
                lazyListState = lazyListState,
                paddingValues = it
            )
        }
    }
}
