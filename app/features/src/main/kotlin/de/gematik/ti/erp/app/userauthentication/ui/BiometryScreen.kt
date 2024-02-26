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

package de.gematik.ti.erp.app.userauthentication.ui

import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.semantics
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavController
import de.gematik.ti.erp.app.core.LocalActivity
import de.gematik.ti.erp.app.features.R
import de.gematik.ti.erp.app.navigation.Screen
import de.gematik.ti.erp.app.onboarding.model.OnboardingSecureAppMethod
import de.gematik.ti.erp.app.onboarding.model.OnboardingSecureAppMethod.Companion.toAuthenticationMode
import de.gematik.ti.erp.app.onboarding.navigation.OnboardingRoutes
import de.gematik.ti.erp.app.onboarding.presentation.rememberOnboardingController
import de.gematik.ti.erp.app.onboarding.ui.OnboardingBottomBar
import de.gematik.ti.erp.app.theme.AppTheme
import de.gematik.ti.erp.app.theme.PaddingDefaults
import de.gematik.ti.erp.app.userauthentication.observer.BiometricPromptBuilder
import de.gematik.ti.erp.app.utils.compose.AnimatedElevationScaffold
import de.gematik.ti.erp.app.utils.compose.NavigationBarMode

class BiometryScreen(
    override val navController: NavController,
    override val navBackStackEntry: NavBackStackEntry
) : Screen() {

    @Composable
    override fun Content() {
        val controller = rememberOnboardingController()

        val lazyListState = rememberLazyListState()
        val profileName = stringResource(R.string.onboarding_default_profile_name)
        val activity = LocalActivity.current

        val biometricPromptBuilder = remember { BiometricPromptBuilder(activity as AppCompatActivity) }

        val infoBuilder = biometricPromptBuilder.buildPromptInfo(
            title = stringResource(R.string.auth_prompt_headline),
            negativeButton = stringResource(R.string.auth_prompt_cancel)
        )

        val prompt = remember(biometricPromptBuilder) {
            biometricPromptBuilder.buildBiometricPrompt(
                onSuccess = {
                    controller.onSaveOnboardingData(
                        authenticationMode = OnboardingSecureAppMethod.DeviceSecurity.toAuthenticationMode(),
                        profileName = profileName
                    )
                    navController.navigate(OnboardingRoutes.OnboardingAnalyticsPreviewScreen.path())
                }
            )
        }

        AnimatedElevationScaffold(
            modifier = Modifier.navigationBarsPadding(),
            navigationMode = NavigationBarMode.Close,
            bottomBar = {
                OnboardingBottomBar(
                    buttonText = stringResource(R.string.settings_device_security_allow),
                    onButtonClick = {
                        prompt.authenticate(infoBuilder)
                    },
                    buttonEnabled = true,
                    info = null,
                    buttonModifier = Modifier
                )
            },
            topBarTitle = stringResource(R.string.settings_biometric_dialog_headline),
            listState = lazyListState,
            onBack = { navController.popBackStack() }
        ) {
            LazyColumn(
                state = lazyListState,
                modifier = Modifier
                    .wrapContentSize()
                    .padding(
                        horizontal = PaddingDefaults.Medium
                    )
            ) {
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .wrapContentHeight()
                            .semantics(mergeDescendants = true) {}
                    ) {
                        Text(
                            stringResource(R.string.settings_biometric_dialog_title),
                            style = AppTheme.typography.h6,
                            modifier = Modifier.padding(
                                top = PaddingDefaults.Medium,
                                bottom = PaddingDefaults.Large
                            )
                        )

                        Text(
                            text = stringResource(R.string.settings_biometric_dialog_text),
                            style = AppTheme.typography.body1,
                            modifier = Modifier.padding(
                                bottom = PaddingDefaults.Small
                            )
                        )
                    }
                }
            }
        }
    }
}
