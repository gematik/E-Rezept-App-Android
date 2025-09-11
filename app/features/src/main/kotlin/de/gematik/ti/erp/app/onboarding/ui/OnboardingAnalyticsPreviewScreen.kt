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

import androidx.activity.compose.BackHandler
import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AccessibilityNew
import androidx.compose.material.icons.outlined.BugReport
import androidx.compose.material.icons.sharp.AutoFixHigh
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.onClick
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavController
import de.gematik.ti.erp.app.Requirement
import de.gematik.ti.erp.app.TestTag
import de.gematik.ti.erp.app.core.R
import de.gematik.ti.erp.app.navigation.Screen
import de.gematik.ti.erp.app.onboarding.navigation.OnboardingRoutes
import de.gematik.ti.erp.app.onboarding.navigation.finishOnboardingAsSuccessAndOpenPrescriptions
import de.gematik.ti.erp.app.onboarding.presentation.OnboardingGraphController
import de.gematik.ti.erp.app.preview.LightDarkPreview
import de.gematik.ti.erp.app.semantics.semanticsHeading
import de.gematik.ti.erp.app.theme.AppTheme
import de.gematik.ti.erp.app.theme.PaddingDefaults
import de.gematik.ti.erp.app.utils.SpacerLarge
import de.gematik.ti.erp.app.utils.SpacerMedium
import de.gematik.ti.erp.app.utils.SpacerSmall
import de.gematik.ti.erp.app.utils.SpacerXXLarge
import de.gematik.ti.erp.app.utils.compose.ClickText
import de.gematik.ti.erp.app.utils.compose.ClickableText
import de.gematik.ti.erp.app.utils.compose.annotatedStringBold
import de.gematik.ti.erp.app.utils.compose.annotatedStringResource
import de.gematik.ti.erp.app.utils.compose.preview.PreviewAppTheme
import de.gematik.ti.erp.app.utils.compose.shortToast

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
        val context = LocalContext.current
        val allowStars = stringResource(R.string.settings_tracking_allow_emoji)
        val allowText = annotatedStringResource(
            R.string.settings_tracking_allow_info,
            annotatedStringBold(allowStars)
        ).text
        val disallowText = stringResource(R.string.settings_tracking_disallow_info)
        val currentStep by graphController.currentStep.collectAsStateWithLifecycle(3)
        BackHandler {}
        OnboardingScreenScaffold(
            currentStep = currentStep,
            onAnalyticsCheckChanged = {
                navController.navigate(OnboardingRoutes.AllowAnalyticsScreen.path())
            },
            onClickAccept = {
                context.shortToast(allowText)
                graphController.changeAnalyticsState(true)
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
}

@Composable
fun OnboardingScreenScaffold(
    currentStep: Int,
    onAnalyticsCheckChanged: () -> Unit,
    onClickAccept: () -> Unit,
    onClickReject: () -> Unit
) {
    OnboardingScreenScaffold(
        state = rememberLazyListState(),
        topBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = PaddingDefaults.Medium)
            ) {
                SpacerXXLarge()
                OnboardingProgressIndicator(currentStep = currentStep)
                SpacerXXLarge()
            }
        },
        bottomBar = {
            Column {
                OnboardingBottomBar(
                    info = stringResource(R.string.onboarding_analytics_bottom_text),
                    buttonText = stringResource(R.string.onboarding_analytics_agree_button),
                    buttonEnabled = true,
                    includeBottomSpacer = false,
                    modifier = Modifier.testTag(TestTag.Onboarding.AnalyticsPreviewScreen.AllowButton),
                    onButtonClick = onClickAccept
                )
                OnboardingBottomBar(
                    info = null,
                    buttonText = stringResource(R.string.onboarding_analytics_reject_button),
                    modifier = Modifier.testTag(TestTag.Onboarding.AnalyticsPreviewScreen.DenyButton),
                    buttonEnabled = true,
                    onButtonClick = onClickReject
                )
            }
        },
        modifier = Modifier
            .fillMaxSize()
    ) {
        onboardingAnalyticsPreviewContent(
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
            style = AppTheme.typography.subtitle2
        )
    }
}

private fun LazyListScope.onboardingAnalyticsPreviewContent(
    onAnalyticsCheckChanged: () -> Unit
) {
    item {
        Column {
            Text(
                modifier = Modifier.semanticsHeading(),
                text = stringResource(R.string.onboarding_analytics_consent_title),
                style = AppTheme.typography.h5,
                fontWeight = FontWeight.W700,
                textAlign = TextAlign.Start
            )

            SpacerSmall()

            AccessibleOnboardingClickableText(
                textWithPlaceholdersRes = R.string.onboarding_analytics_consent_text,
                textStyle = AppTheme.typography.body1l,
                clickText = ClickText(
                    text = stringResource(R.string.onboarding_analytics_link_text),
                    onClick = { onAnalyticsCheckChanged() }
                ),
                linkTextStyle = SpanStyle(
                    color = AppTheme.colors.primary700,
                    textDecoration = TextDecoration.Underline
                )
            )

            SpacerLarge()

            AnalyticsInfoList()
        }
    }
}

@Composable
private fun AnalyticsInfoList() {
    Column {
        AnalyticsInfo(
            icon = Icons.Sharp.AutoFixHigh,

            text = stringResource(R.string.onboarding_analytics_optimize_user_experience_text)
        )

        Spacer(modifier = Modifier.height(PaddingDefaults.Medium))

        AnalyticsInfo(

            icon = Icons.Outlined.AccessibilityNew,
            text = stringResource(R.string.onboarding_analytics_reduce_barriers_text)
        )

        Spacer(modifier = Modifier.height(PaddingDefaults.Medium))

        AnalyticsInfo(
            icon = Icons.Outlined.BugReport,
            text = stringResource(R.string.onboarding_analytics_resolve_errors_text)
        )
    }
}

@Composable
private fun AccessibleOnboardingClickableText(
    modifier: Modifier = Modifier,
    @StringRes textWithPlaceholdersRes: Int,
    clickText: ClickText,
    textStyle: TextStyle,
    linkTextStyle: SpanStyle = SpanStyle(color = AppTheme.colors.primary700),
    text: String = stringResource(id = textWithPlaceholdersRes)
) {
    val accessibleText = remember(text, clickText.text) {
        text.replace(clickText.text, clickText.text)
    }

    ClickableText(
        modifier = modifier.semantics {
            this.contentDescription = accessibleText
            this.role = Role.Button
            onClick {
                clickText.onClick()
                true
            }
        },
        clickText = clickText,
        textStyle = textStyle,
        linkTextStyle = linkTextStyle,
        text = text
    )
}

@LightDarkPreview
@Composable
fun OnboardingAnalyticsPreviewContentPreview() {
    PreviewAppTheme {
        OnboardingScreenScaffold(
            currentStep = 3,
            onAnalyticsCheckChanged = {},
            onClickAccept = {},
            onClickReject = {}
        )
    }
}
