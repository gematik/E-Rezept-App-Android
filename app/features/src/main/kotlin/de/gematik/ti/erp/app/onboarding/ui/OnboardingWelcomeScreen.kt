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

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.FixedScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavController
import de.gematik.ti.erp.app.TestTag
import de.gematik.ti.erp.app.core.R
import de.gematik.ti.erp.app.navigation.Screen
import de.gematik.ti.erp.app.navigation.navigateAndClearStack
import de.gematik.ti.erp.app.onboarding.navigation.OnboardingRoutes.OnboardingDataProtectionAndTermsOfUseOverviewScreen
import de.gematik.ti.erp.app.onboarding.navigation.finishOnboardingAsSuccessAndOpenPrescriptions
import de.gematik.ti.erp.app.onboarding.presentation.OnboardingGraphController
import de.gematik.ti.erp.app.theme.AppTheme
import de.gematik.ti.erp.app.theme.PaddingDefaults
import de.gematik.ti.erp.app.theme.SizeDefaults
import de.gematik.ti.erp.app.utils.compose.LightDarkAllPreview
import de.gematik.ti.erp.app.utils.compose.PrimaryButtonSmall
import de.gematik.ti.erp.app.utils.compose.preview.PreviewAppTheme
import de.gematik.ti.erp.app.utils.extensions.BuildConfigExtension
import de.gematik.ti.erp.app.utils.extensions.BuildConfigExtension.isDebug

private const val FLAG_PADDING = 10

// Should be replaced by a splash screen and come as the first one in the app
class OnboardingWelcomeScreen(
    override val navController: NavController,
    override val navBackStackEntry: NavBackStackEntry,
    private val graphController: OnboardingGraphController
) : Screen() {
    @Composable
    override fun Content() {
        LaunchedEffect(Unit) {
            // `gemSpec_eRp_FdV A_20203` default settings are not allow screenshots
            // (on debug builds should be allowed for testing)
            if (isDebug) {
                graphController.allowScreenshots(true)
            }
        }

        OnboardingWelcomeScreenContent(onStartClick = {
            navController.navigateAndClearStack(OnboardingDataProtectionAndTermsOfUseOverviewScreen.path())
        })

        if (BuildConfigExtension.isInternalDebug) {
            SkipOnBoardingButton {
                graphController.createProfileOnSkipOnboarding()
                navController.finishOnboardingAsSuccessAndOpenPrescriptions()
            }
        }
    }
}

@Composable
internal fun OnboardingWelcomeScreenContent(
    onStartClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .testTag(TestTag.Onboarding.WelcomeScreen)
            .systemBarsPadding()
    ) {
        FlaggedGematikLogo()
        OnboardingImages(modifier = Modifier.fillMaxSize())
        WelcomeMessage(onStartClick, modifier = Modifier.align(alignment = Alignment.Center))
    }
}

@Composable
fun FlaggedGematikLogo() {
    Row(
        modifier = Modifier
            .padding(top = PaddingDefaults.Medium),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Image(
            modifier = Modifier.padding(end = FLAG_PADDING.dp),
            painter = painterResource(R.drawable.ic_onboarding_logo_flag),
            contentDescription = null
        )
        Icon(
            painter = painterResource(R.drawable.ic_onboarding_logo_gematik),
            contentDescription = null,
            tint = AppTheme.colors.primary900
        )
    }
}

@Composable
private fun ErezeptLogo(
    modifier: Modifier = Modifier
) {
    Image(
        modifier = modifier,
        painter = painterResource(R.drawable.erp_logo),
        contentDescription = null
    )
}

@Composable
private fun WelcomeMessage(
    onStartClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
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
                    bottom = PaddingDefaults.XLarge
                )
        )
        PrimaryButtonSmall(
            modifier = Modifier.align(Alignment.CenterHorizontally),
            shape = RoundedCornerShape(SizeDefaults.triple),
            onClick = onStartClick
        ) {
            Text(stringResource(R.string.on_boarding_page_1_button))
        }
    }
}

@Suppress("MagicNumber")
@Composable
private fun OnboardingImages(modifier: Modifier) {
    BoxWithConstraints(modifier = modifier) {
        val picsScale = FixedScale(1f) // used to call calculateContentScale(this.maxWidth, maxHeight)

        Image(
            painter = painterResource(id = R.drawable.apotheker),
            contentDescription = null,
            contentScale = picsScale,
            modifier = Modifier
                .align(Alignment.TopStart)
                .offset(y = this.maxHeight * 0.1f)
        )

        val apothekerin1Y = if (maxHeight > maxWidth) maxHeight * 0.61f else maxHeight * 0.45f
        Image(
            painter = painterResource(id = R.drawable.apothekerin_1),
            contentDescription = null,
            contentScale = picsScale,
            modifier = Modifier
                .align(Alignment.TopStart)
                .offset(y = apothekerin1Y)
        )

        val alterMannR1X = if (maxHeight > maxWidth) maxWidth * 0.3f else maxWidth * 0.19f
        Image(
            painter = painterResource(id = R.drawable.alter_mann_r1),
            contentDescription = null,
            contentScale = picsScale,
            modifier = Modifier
                .align(Alignment.BottomStart)
                .offset(x = alterMannR1X)

        )

        val alteFrau1X = if (maxHeight > maxWidth) maxWidth * 0.36f else maxWidth * 0.24f
        Image(
            painter = painterResource(id = R.drawable.alte_frau_1),
            contentDescription = null,
            contentScale = picsScale,
            modifier = Modifier
                .align(Alignment.BottomStart)
                .offset(alteFrau1X)
        )

        Image(
            painter = painterResource(id = R.drawable.mann_im_rollstuhl_1),
            contentDescription = null,
            contentScale = picsScale,
            modifier = Modifier
                .align(Alignment.BottomStart)
                .offset(x = maxWidth * 0.07f)
        )

        Image(
            painter = painterResource(id = R.drawable.aerztin_1),
            contentDescription = null,
            contentScale = picsScale,
            modifier = Modifier
                .align(Alignment.BottomStart)
        )

        // right side -----------------------------------

        Image(
            painter = painterResource(id = R.drawable.arzt_1),
            contentDescription = null,
            contentScale = picsScale,
            modifier = Modifier
                .align(Alignment.BottomEnd)
        )

        val babyR21X = if (maxHeight > maxWidth) maxWidth * 0.6f else maxWidth * 0.8f

        Image(
            painter = painterResource(id = R.drawable.baby_r21),
            contentDescription = null,
            contentScale = picsScale,
            modifier = Modifier
                .align(Alignment.BottomStart)
                .offset(x = babyR21X)
        )

        Image(
            painter = painterResource(id = R.drawable.junge_1),
            contentDescription = null,
            contentScale = picsScale,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .offset(y = maxHeight * -0.15f)
        )

        Image(
            painter = painterResource(id = R.drawable.maedchen),
            contentDescription = null,
            contentScale = picsScale,
            modifier = Modifier
                .align(Alignment.CenterEnd)
        )
    }
}

@LightDarkAllPreview
@Composable
fun OnboardingWelcomeScreenContentPreviews() {
    PreviewAppTheme {
        OnboardingWelcomeScreenContent(onStartClick = { })
    }
}

@Preview(locale = "ar", showSystemUi = true)
@Composable
fun OnboardingWelcomeScreenContentRightLeftPreviews() {
    PreviewAppTheme {
        OnboardingWelcomeScreenContent(onStartClick = { })
    }
}
