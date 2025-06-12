/*
 * Copyright 2025, gematik GmbH
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

package de.gematik.ti.erp.app.onboarding.ui

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.FlashOn
import androidx.compose.material.icons.rounded.PersonPin
import androidx.compose.material.icons.rounded.Star
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.toggleableState
import androidx.compose.ui.state.ToggleableState
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavController
import de.gematik.ti.erp.app.Requirement
import de.gematik.ti.erp.app.TestTag
import de.gematik.ti.erp.app.app_core.R
import de.gematik.ti.erp.app.navigation.Screen
import de.gematik.ti.erp.app.onboarding.navigation.OnboardingRoutes
import de.gematik.ti.erp.app.onboarding.navigation.finishOnboardingAsSuccessAndOpenPrescriptions
import de.gematik.ti.erp.app.onboarding.presentation.OnboardingGraphController
import de.gematik.ti.erp.app.theme.AppTheme
import de.gematik.ti.erp.app.theme.PaddingDefaults
import de.gematik.ti.erp.app.utils.SpacerMedium
import de.gematik.ti.erp.app.utils.SpacerXXLarge
import de.gematik.ti.erp.app.utils.compose.LightDarkPreview
import de.gematik.ti.erp.app.utils.compose.SwitchLeftWithText
import de.gematik.ti.erp.app.utils.compose.preview.BooleanPreviewParameterProvider
import de.gematik.ti.erp.app.utils.compose.preview.PreviewAppTheme

@Requirement(
    "O.Purp_3#4",
    sourceSpecification = "BSI-eRp-ePA",
    rationale = "User information and acceptance/deny for analytics usage"
)
class OnboardingAnalyticsPreviewScreen(
    override val navController: NavController,
    override val navBackStackEntry: NavBackStackEntry,
    private val graphController: OnboardingGraphController
) : Screen() {
    @Composable
    override fun Content() {
        val isAnalyticsAllowedState by graphController.isAnalyticsAllowedState
        var isAnalyticsAllowed by remember(isAnalyticsAllowedState) { mutableStateOf(isAnalyticsAllowedState) }

        OnboardingScreenScaffold(
            isAnalyticsAllowed = isAnalyticsAllowed,
            onAnalyticsCheckChanged = {
                isAnalyticsAllowed = it
                graphController.changeAnalyticsState(it)
                if (it) {
                    navController.navigate(OnboardingRoutes.AllowAnalyticsScreen.path())
                }
            },
            onButtonClick = {
                graphController.createProfile()
                navController.finishOnboardingAsSuccessAndOpenPrescriptions()
            }
        )
    }
}

@Composable
fun OnboardingScreenScaffold(
    isAnalyticsAllowed: Boolean,
    onAnalyticsCheckChanged: (Boolean) -> Unit,
    onButtonClick: () -> Unit
) {
    OnboardingScreenScaffold(
        state = rememberLazyListState(),
        bottomBar = {
            OnboardingBottomBar(
                info = stringResource(R.string.onboarding_analytics_bottom_you_can_change),
                buttonText = stringResource(R.string.onboarding_bottom_button_next),
                buttonEnabled = true,
                buttonModifier = Modifier.testTag(TestTag.Onboarding.NextButton),
                onButtonClick = onButtonClick
            )
        },
        modifier = Modifier
            .fillMaxSize()
    ) {
        onboardingAnalyticsPreviewContent(
            isAnalyticsAllowed = isAnalyticsAllowed,
            onAnalyticsCheckChanged = onAnalyticsCheckChanged
        )
    }
}

@Composable
private fun AnalyticsInfo(
    icon: ImageVector,
    text: String
) {
    Row(Modifier.fillMaxWidth()) {
        Icon(icon, null, tint = AppTheme.colors.primary700)
        SpacerMedium()
        Text(
            text = text,
            style = AppTheme.typography.body1
        )
    }
}

private fun LazyListScope.onboardingAnalyticsPreviewContent(
    isAnalyticsAllowed: Boolean,
    onAnalyticsCheckChanged: (Boolean) -> Unit
) {
    item {
        SpacerXXLarge()
        Text(
            text = stringResource(R.string.onb_page_5_header),
            style = AppTheme.typography.h4,
            fontWeight = FontWeight.W700,
            textAlign = TextAlign.Start,
            modifier =
            Modifier
                .padding(
                    top = PaddingDefaults.XXLarge,
                    bottom = PaddingDefaults.Large
                )
        )
        SpacerXXLarge()
    }
    item {
        Text(
            text = stringResource(R.string.onboarding_analytics_we_want),
            style = AppTheme.typography.subtitle1
        )
    }
    item {
        SpacerMedium()
    }
    item {
        AnalyticsInfo(
            icon = Icons.Rounded.Star,
            text = stringResource(R.string.onboarding_analytics_ww_usability)
        )
    }
    item {
        SpacerMedium()
    }
    item {
        AnalyticsInfo(
            icon = Icons.Rounded.FlashOn,
            text = stringResource(R.string.onboarding_analytics_ww_errors)
        )
    }
    item {
        SpacerMedium()
    }
    item {
        AnalyticsInfo(
            icon = Icons.Rounded.PersonPin,
            text = stringResource(R.string.onboarding_analytics_ww_anon)
        )
    }
    item {
        SpacerMedium()
    }
    item {
        SwitchLeftWithText(
            modifier =
            Modifier.testTag(TestTag.Onboarding.AnalyticsSwitch).semantics {
                toggleableState = ToggleableState.Off
            },
            text = stringResource(R.string.on_boarding_page_5_label),
            checked = isAnalyticsAllowed,
            onCheckedChange = onAnalyticsCheckChanged
        )
        SpacerMedium()
    }
}

@LightDarkPreview
@Composable
fun OnboardingAnalyticsPreviewContentPreview(
    @PreviewParameter(BooleanPreviewParameterProvider::class) isAnalyticsAllowed: Boolean
) {
    PreviewAppTheme {
        OnboardingScreenScaffold(
            isAnalyticsAllowed = isAnalyticsAllowed,
            onAnalyticsCheckChanged = {},
            onButtonClick = {}
        )
    }
}
