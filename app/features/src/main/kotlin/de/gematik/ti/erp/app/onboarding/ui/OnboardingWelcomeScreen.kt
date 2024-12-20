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

package de.gematik.ti.erp.app.onboarding.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.material.Icon
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavController
import de.gematik.ti.erp.app.TestTag
import de.gematik.ti.erp.app.features.R
import de.gematik.ti.erp.app.navigation.Screen
import de.gematik.ti.erp.app.navigation.navigateAndClearStack
import de.gematik.ti.erp.app.onboarding.navigation.OnboardingRoutes.OnboardingDataProtectionAndTermsOfUseOverviewScreen
import de.gematik.ti.erp.app.onboarding.navigation.finishOnboardingAsSuccessAndOpenPrescriptions
import de.gematik.ti.erp.app.onboarding.presentation.OnboardingGraphController
import de.gematik.ti.erp.app.theme.AppTheme
import de.gematik.ti.erp.app.theme.PaddingDefaults
import de.gematik.ti.erp.app.utils.compose.LightDarkPreview
import de.gematik.ti.erp.app.utils.compose.preview.PreviewAppTheme
import de.gematik.ti.erp.app.utils.extensions.BuildConfigExtension.isDebugOrMinifiedDebug
import de.gematik.ti.erp.app.utils.extensions.BuildConfigExtension.isNonReleaseMode
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

private const val WELCOME_TIMEOUT: Long = 1770
private const val DEBUG_WELCOME_TIMEOUT: Long = 3000
private const val NEGATIVE_OFFSET = -60
private const val FLAG_PADDING = 10

// Should be replaced by a splash screen and come as the first one in the app
class OnboardingWelcomeScreen(
    override val navController: NavController,
    override val navBackStackEntry: NavBackStackEntry,
    private val graphController: OnboardingGraphController
) : Screen() {
    @Composable
    override fun Content() {
        var job: Job? by remember { mutableStateOf(null) }

        DelayedNavigation(
            onJobCreated = { job = it },
            action = {
                navController.navigateAndClearStack(OnboardingDataProtectionAndTermsOfUseOverviewScreen.path())
            }
        )

        LaunchedEffect(Unit) {
            // `gemSpec_eRp_FdV A_20203` default settings are not allow screenshots
            // (on debug builds should be allowed for testing)
            if (isDebugOrMinifiedDebug) {
                graphController.allowScreenshots(true)
            }
        }

        OnboardingWelcomeScreenContent()

        if (isNonReleaseMode) {
            SkipOnBoardingButton {
                job?.cancel()
                graphController.createProfileOnSkipOnboarding()
                navController.finishOnboardingAsSuccessAndOpenPrescriptions()
            }
        }
    }

    @Composable
    private fun DelayedNavigation(
        onJobCreated: (Job) -> Unit,
        action: () -> Unit
    ) {
        val coroutineScope = rememberCoroutineScope()
        val delayTime = if (isNonReleaseMode) DEBUG_WELCOME_TIMEOUT else WELCOME_TIMEOUT

        DisposableEffect(Unit) {
            val job = coroutineScope.launch {
                delay(delayTime)
                action.invoke()
            }
            onJobCreated(job)
            onDispose {
                job.cancel()
            }
        }
    }
}

@Composable
internal fun OnboardingWelcomeScreenContent() {
    Surface {
        Column(
            modifier = Modifier
                .testTag(TestTag.Onboarding.WelcomeScreen)
                .padding(horizontal = PaddingDefaults.Medium)
                .systemBarsPadding()
        ) {
            Row(
                modifier = Modifier
                    .padding(top = PaddingDefaults.Medium)
                    .align(Alignment.Start),
                verticalAlignment = Alignment.CenterVertically
            ) {
                FlagLogo(modifier = Modifier.padding(end = FLAG_PADDING.dp))
                GematikLogo()
            }
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentHeight()
                    .semantics(
                        mergeDescendants = true,
                        properties = {}
                    )
            ) {
                ErezeptLogo(
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
            OnboardingBabyAndGrandpaImage(
                modifier = Modifier
                    .fillMaxSize()
                    .offset(x = (NEGATIVE_OFFSET).dp)
            )
        }
    }
}

@Composable
private fun FlagLogo(
    modifier: Modifier
) {
    Image(
        modifier = modifier,
        painter = painterResource(R.drawable.ic_onboarding_logo_flag),
        contentDescription = "onboarding-logo-flag"
    )
}

@Composable
private fun GematikLogo() {
    Icon(
        painter = painterResource(R.drawable.ic_onboarding_logo_gematik),
        contentDescription = "onboarding-logo-gematik",
        tint = AppTheme.colors.primary900
    )
}

@Composable
private fun ErezeptLogo(
    modifier: Modifier
) {
    Image(
        modifier = modifier,
        painter = painterResource(R.drawable.erp_logo),
        contentDescription = "erezept-logo"
    )
}

@Composable
private fun OnboardingBabyAndGrandpaImage(
    modifier: Modifier
) {
    Image(
        modifier = modifier,
        painter = painterResource(R.drawable.onboarding_boygrannygranpa),
        contentDescription = "baby-and-grandpa-image",
        alignment = Alignment.BottomStart
    )
}

@LightDarkPreview
@Composable
fun OnboardingWelcomeScreenContentPreview() {
    PreviewAppTheme {
        OnboardingWelcomeScreenContent()
    }
}
