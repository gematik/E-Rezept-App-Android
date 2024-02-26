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

package de.gematik.ti.erp.app.onboarding.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavController
import de.gematik.ti.erp.app.Requirement
import de.gematik.ti.erp.app.TestTag
import de.gematik.ti.erp.app.features.R
import de.gematik.ti.erp.app.navigation.Screen
import de.gematik.ti.erp.app.onboarding.navigation.OnboardingRoutes
import de.gematik.ti.erp.app.onboarding.navigation.finishOnboardingAsSuccessAndOpenPrescriptions
import de.gematik.ti.erp.app.onboarding.presentation.rememberOnboardingController
import de.gematik.ti.erp.app.theme.AppTheme
import de.gematik.ti.erp.app.theme.PaddingDefaults
import de.gematik.ti.erp.app.utils.compose.LightDarkPreview
import de.gematik.ti.erp.app.utils.compose.PreviewAppTheme
import de.gematik.ti.erp.app.utils.compose.SecondaryButton
import de.gematik.ti.erp.app.utils.compose.SpacerMedium
import de.gematik.ti.erp.app.utils.compose.SpacerXXLarge
import de.gematik.ti.erp.app.utils.compose.SwitchWithText
import de.gematik.ti.erp.app.utils.compose.visualTestTag
import de.gematik.ti.erp.app.utils.extensions.BuildConfigExtension

@Requirement(
    "A_19184",
    "A_20194",
    "A_19980",
    "A_19981",
    sourceSpecification = "gemSpec_eRp_FdV",
    rationale = "Displays terms of service and privacy statement to the user."
)
class OnboardingDataProtectionAndTermsOfUseOverviewScreen(
    override val navController: NavController,
    override val navBackStackEntry: NavBackStackEntry
) : Screen() {

    @Composable
    override fun Content() {
        var accepted by rememberSaveable { mutableStateOf(false) }

        val controller = rememberOnboardingController()

        OnboardingScaffold(
            state = rememberLazyListState(),
            bottomBar = {
                OnboardingBottomBar(
                    modifier = Modifier.fillMaxWidth(),
                    info = null,
                    buttonText = stringResource(R.string.onboarding_bottom_button_accept),
                    buttonEnabled = accepted,
                    buttonModifier = Modifier.testTag(TestTag.Onboarding.NextButton),
                    onButtonClick = {
                        navController.navigate(OnboardingRoutes.OnboardingSelectAppLoginScreen.path())
                    }
                )
            },
            modifier = Modifier
                .visualTestTag(TestTag.Onboarding.DataTermsScreen)
                .fillMaxSize()
        ) {
            dataProtectionAndTermsOfUseOverviewScreenContent(
                isAccepted = accepted,
                onCheckedChange = {
                    accepted = it
                },
                onClickOpenDataProtectionButton = {
                    navController.navigate(OnboardingRoutes.DataProtectionScreen.path())
                },
                onClickOpenTermsOfUseButton = {
                    navController.navigate(OnboardingRoutes.TermsOfUseScreen.path())
                }
            )
        }

        if (BuildConfigExtension.isNonReleaseMode) {
            SkipOnBoardingButton {
                controller.createProfileOnSkipOnboarding()
                navController.finishOnboardingAsSuccessAndOpenPrescriptions()
            }
        }
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
            "O.Arch_9",
            sourceSpecification = "BSI-eRp-ePA",
            rationale = "Display data protection as part of the onboarding"
        )
        @Requirement(
            "A_19980#1",
            "A_19981#1",
            sourceSpecification = "gemSpec_eRp_FdV",
            rationale = "Display data protection as part of the onboarding"
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
            rationale = "Display terms of use as part of the onboarding"
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
        @Requirement(
            "A_19980#2",
            "A_19981#2",
            sourceSpecification = "gemSpec_eRp_FdV",
            rationale = "The user is informed and required to accept this information via the data protection " +
                "statement. Related data and services are listed in sections 5."
        )
        SwitchWithText(
            modifier = Modifier.testTag(TestTag.Onboarding.DataTerms.AcceptDataTermsSwitch),
            text = stringResource(R.string.onboarding_data_terms_info),
            checked = isAccepted,
            onCheckedChange = onCheckedChange
        )
        SpacerMedium()
    }
}

@LightDarkPreview
@Composable
fun DataProtectionAndTermsOfUseOverviewScreenContentFalsePreview() {
    PreviewAppTheme {
        LazyColumn {
            dataProtectionAndTermsOfUseOverviewScreenContent(
                isAccepted = false,
                onCheckedChange = {},
                onClickOpenTermsOfUseButton = {},
                onClickOpenDataProtectionButton = {}
            )
        }
    }
}

@LightDarkPreview
@Composable
fun DataProtectionAndTermsOfUseOverviewScreenContentTruePreview() {
    PreviewAppTheme {
        LazyColumn {
            dataProtectionAndTermsOfUseOverviewScreenContent(
                isAccepted = true,
                onCheckedChange = {},
                onClickOpenTermsOfUseButton = {},
                onClickOpenDataProtectionButton = {}
            )
        }
    }
}
