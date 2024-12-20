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
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
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
import de.gematik.ti.erp.app.features.R
import de.gematik.ti.erp.app.navigation.Screen
import de.gematik.ti.erp.app.onboarding.navigation.OnboardingRoutes
import de.gematik.ti.erp.app.onboarding.navigation.finishOnboardingAsSuccessAndOpenPrescriptions
import de.gematik.ti.erp.app.onboarding.presentation.OnboardingGraphController
import de.gematik.ti.erp.app.theme.AppTheme
import de.gematik.ti.erp.app.theme.PaddingDefaults
import de.gematik.ti.erp.app.utils.SpacerMedium
import de.gematik.ti.erp.app.utils.SpacerXXLarge
import de.gematik.ti.erp.app.utils.compose.LightDarkPreview
import de.gematik.ti.erp.app.utils.compose.SecondaryButton
import de.gematik.ti.erp.app.utils.compose.SwitchLeftWithText
import de.gematik.ti.erp.app.utils.compose.preview.BooleanPreviewParameterProvider
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
    @Composable
    override fun Content() {
        var accepted by rememberSaveable { mutableStateOf(false) }
        val lazyListState = rememberLazyListState()

        OnboardingDataProtectionAndTermsOfUseOverviewScreenContent(
            lazyListState = lazyListState,
            isAccepted = accepted,
            onAcceptanceStateChanged = { accepted = it },
            onClickOnboardingBottomBar = {
                navController.navigate(OnboardingRoutes.OnboardingSelectAppLoginScreen.path())
            },
            onClickOpenDataProtectionButton = {
                navController.navigate(OnboardingRoutes.DataProtectionScreen.path())
            },
            onClickOpenTermsOfUseButton = {
                navController.navigate(OnboardingRoutes.TermsOfUseScreen.path())
            }
        )

        if (BuildConfigExtension.isNonReleaseMode) {
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
    isAccepted: Boolean,
    onAcceptanceStateChanged: (Boolean) -> Unit,
    onClickOnboardingBottomBar: () -> Unit,
    onClickOpenDataProtectionButton: () -> Unit,
    onClickOpenTermsOfUseButton: () -> Unit
) {
    OnboardingScreenScaffold(
        state = lazyListState,
        bottomBar = {
            OnboardingBottomBar(
                modifier = Modifier.fillMaxWidth(),
                info = null,
                buttonText = stringResource(R.string.onboarding_bottom_button_accept),
                buttonEnabled = isAccepted,
                buttonModifier = Modifier.testTag(TestTag.Onboarding.NextButton),
                onButtonClick = onClickOnboardingBottomBar
            )
        },
        modifier = Modifier
            .testTag(TestTag.Onboarding.DataTermsScreen)
            .fillMaxSize()
    ) {
        dataProtectionAndTermsOfUseOverviewScreenContent(
            isAccepted = isAccepted,
            onCheckedChange = onAcceptanceStateChanged,
            onClickOpenDataProtectionButton = onClickOpenDataProtectionButton,
            onClickOpenTermsOfUseButton = onClickOpenTermsOfUseButton
        )
    }
}

private fun LazyListScope.dataProtectionAndTermsOfUseOverviewScreenContent(
    isAccepted: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    onClickOpenDataProtectionButton: () -> Unit,
    onClickOpenTermsOfUseButton: () -> Unit
) {
    item {
        SpacerXXLarge()
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
            style = AppTheme.typography.h4,
            fontWeight = FontWeight.W700,
            textAlign = TextAlign.Start,
            modifier = Modifier.padding(bottom = PaddingDefaults.Medium, top = PaddingDefaults.XXLarge)
        )
        SpacerMedium()
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
                onClick = onClickOpenDataProtectionButton
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
                    .testTag(TestTag.Onboarding.DataTerms.OpenTermsOfUseButton),
                onClick = onClickOpenTermsOfUseButton
            ) {
                Text(stringResource(R.string.onboarding_terms_button))
            }
            )
        SpacerXXLarge()
    }
    item {
        @Requirement(
            "O.Purp_3#3",
            sourceSpecification = "BSI-eRp-ePA",
            rationale = "User acceptance for terms of use and dar´ta protection as part of the onboarding"
        )
        SwitchLeftWithText(
            modifier =
            Modifier.testTag(TestTag.Onboarding.DataTerms.AcceptDataTermsSwitch).semantics {
                toggleableState = ToggleableState.Off
            },
            text = stringResource(R.string.onboarding_data_terms_info),
            checked = isAccepted,
            onCheckedChange = onCheckedChange
        )
        SpacerMedium()
    }
}

@LightDarkPreview
@Composable
fun OnboardingDataProtectionAndTermsOfUseOverviewScreenContentPreview(
    @PreviewParameter(BooleanPreviewParameterProvider::class) isAccepted: Boolean
) {
    PreviewAppTheme {
        OnboardingDataProtectionAndTermsOfUseOverviewScreenContent(
            lazyListState = rememberLazyListState(),
            isAccepted = isAccepted,
            onAcceptanceStateChanged = {},
            onClickOnboardingBottomBar = {},
            onClickOpenDataProtectionButton = {},
            onClickOpenTermsOfUseButton = {}
        )
    }
}
