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
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
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
import de.gematik.ti.erp.app.semantics.semanticsHeading
import de.gematik.ti.erp.app.theme.AppTheme
import de.gematik.ti.erp.app.theme.PaddingDefaults
import de.gematik.ti.erp.app.theme.SizeDefaults
import de.gematik.ti.erp.app.utils.SpacerLarge
import de.gematik.ti.erp.app.utils.SpacerMedium
import de.gematik.ti.erp.app.utils.SpacerSmall
import de.gematik.ti.erp.app.utils.SpacerXXLarge
import de.gematik.ti.erp.app.utils.compose.LightDarkPreview
import de.gematik.ti.erp.app.utils.compose.SecondaryButton
import de.gematik.ti.erp.app.utils.compose.preview.PreviewAppTheme
import de.gematik.ti.erp.app.utils.extensions.BuildConfigExtension

class OnboardingDataProtectionAndTermsOfUseOverviewScreen(
    override val navController: NavController,
    override val navBackStackEntry: NavBackStackEntry,
    private val graphController: OnboardingGraphController
) : Screen() {

    @Requirement(
        "O.Arch_9#1",
        sourceSpecification = "BSI-eRp-ePA",
        rationale = "Display data protection as part of the onboarding"
    )
    @Requirement(
        "O.Purp_3#3",
        sourceSpecification = "BSI-eRp-ePA",
        rationale = "User acceptance for terms of use and data protection as part of the onboarding"
    )
    @Composable
    override fun Content() {
        val lazyListState = rememberLazyListState()
        val currentStep by graphController.currentStep.collectAsStateWithLifecycle(1)
        BackHandler {}
        OnboardingDataProtectionAndTermsOfUseOverviewScreenContent(
            lazyListState = lazyListState,
            currentStep = currentStep,
            onClickOnboardingBottomBar = {
                graphController.nextStep()
                navController.navigate(OnboardingRoutes.OnboardingSelectAppLoginScreen.path())
            },
            onClickOpenDataProtectionButton = {
                navController.navigate(OnboardingRoutes.DataProtectionScreen.path())
            },
            onClickOpenTermsOfUseButton = {
                navController.navigate(OnboardingRoutes.TermsOfUseScreen.path())
            }
        )

        if (BuildConfigExtension.isInternalDebug) {
            SkipOnBoardingButton {
                graphController.createProfileOnSkipOnboarding()
                navController.finishOnboardingAsSuccessAndOpenPrescriptions()
            }
        }
    }
}

@Composable
private fun OnboardingDataProtectionAndTermsOfUseOverviewScreenContent(
    lazyListState: LazyListState,
    currentStep: Int,
    onClickOnboardingBottomBar: () -> Unit,
    onClickOpenDataProtectionButton: () -> Unit,
    onClickOpenTermsOfUseButton: () -> Unit
) {
    OnboardingScreenScaffold(
        state = lazyListState,
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
            OnboardingBottomBar(
                modifier = Modifier
                    .testTag(TestTag.Onboarding.NextButton)
                    .fillMaxWidth(),
                info = null,
                buttonText = stringResource(R.string.onboarding_bottom_app_safety_button_next),
                buttonEnabled = true,
                onButtonClick = onClickOnboardingBottomBar
            )
        },
        modifier = Modifier
            .testTag(TestTag.Onboarding.DataTermsScreen)
            .fillMaxSize()
    ) {
        dataProtectionAndTermsOfUseOverviewScreenContent(
            onClickOpenDataProtectionButton = onClickOpenDataProtectionButton,
            onClickOpenTermsOfUseButton = onClickOpenTermsOfUseButton
        )
    }
}

private fun LazyListScope.dataProtectionAndTermsOfUseOverviewScreenContent(
    onClickOpenDataProtectionButton: () -> Unit,
    onClickOpenTermsOfUseButton: () -> Unit
) {
    item {
        Image(
            painter = painterResource(R.drawable.paragraph),
            contentDescription = null,
            alignment = Alignment.CenterStart,
            modifier = Modifier.fillMaxWidth()
        )
        SpacerXXLarge()
    }
    item {
        Text(
            text = stringResource(R.string.onb_page_4_header),
            style = AppTheme.typography.h5,
            fontWeight = FontWeight.W700,
            textAlign = TextAlign.Start,
            modifier = Modifier.padding(bottom = PaddingDefaults.Small, top = PaddingDefaults.Tiny).semanticsHeading()
        )
        SpacerSmall()
    }

    item {
        Text(
            text = stringResource(R.string.onboarding_data_and_terms_use_use_text),
            style = AppTheme.typography.body1,
            textAlign = TextAlign.Start,
            color = AppTheme.colors.neutral600
        )
        SpacerLarge()
    }
    item {
        @Requirement(
            "O.Purp_3#1",
            sourceSpecification = "BSI-eRp-ePA",
            rationale = "Button to navigate to data protection screen"
        )
        (
            SecondaryButton(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag(TestTag.Onboarding.DataTerms.OpenDataProtectionButton),
                onClick = onClickOpenDataProtectionButton,
                shape = RoundedCornerShape(SizeDefaults.triple),
                colors = ButtonDefaults.buttonColors(
                    backgroundColor = AppTheme.colors.neutral000,
                    contentColor = AppTheme.colors.primary700
                ),
                border = BorderStroke(SizeDefaults.eighth, AppTheme.colors.primary700),
                contentPadding = PaddingValues(PaddingDefaults.Medium)
            ) {
                Text(stringResource(R.string.onboarding_data_button))
            }
            )
        SpacerMedium()
    }
    item {
        @Requirement(
            "O.Purp_3#2",
            sourceSpecification = "BSI-eRp-ePA",
            rationale = "Button to navigate to terms of use screen"
        )
        (
            SecondaryButton(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag(TestTag.Onboarding.DataTerms.OpenDataProtectionButton),
                onClick = onClickOpenTermsOfUseButton,
                shape = RoundedCornerShape(SizeDefaults.triple),
                colors = ButtonDefaults.buttonColors(
                    backgroundColor = AppTheme.colors.neutral000,
                    contentColor = AppTheme.colors.primary700
                ),
                border = BorderStroke(SizeDefaults.eighth, AppTheme.colors.primary700),
                contentPadding = PaddingValues(
                    horizontal = PaddingDefaults.Medium,
                    vertical = SizeDefaults.double
                )
            ) {
                Text(stringResource(R.string.onboarding_terms_button))
            }
            )
        SpacerXXLarge()
    }
}

@LightDarkPreview
@Composable
fun OnboardingDataProtectionAndTermsOfUseOverviewScreenContentPreview() {
    PreviewAppTheme {
        OnboardingDataProtectionAndTermsOfUseOverviewScreenContent(
            lazyListState = rememberLazyListState(),
            currentStep = 1,
            onClickOnboardingBottomBar = {},
            onClickOpenDataProtectionButton = {},
            onClickOpenTermsOfUseButton = {}
        )
    }
}
