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
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavController
import de.gematik.ti.erp.app.Requirement
import de.gematik.ti.erp.app.TestTag
import de.gematik.ti.erp.app.authentication.observer.BiometricPromptBuilder
import de.gematik.ti.erp.app.authentication.presentation.enrollBiometricsIntent
import de.gematik.ti.erp.app.authentication.presentation.enrollDeviceSecurityIntent
import de.gematik.ti.erp.app.button.SelectionCard
import de.gematik.ti.erp.app.core.LocalActivity
import de.gematik.ti.erp.app.core.R
import de.gematik.ti.erp.app.navigation.Screen
import de.gematik.ti.erp.app.onboarding.model.OnboardingAuthScenario
import de.gematik.ti.erp.app.onboarding.navigation.OnboardingRoutes
import de.gematik.ti.erp.app.onboarding.navigation.finishOnboardingAsSuccessAndOpenPrescriptions
import de.gematik.ti.erp.app.onboarding.presentation.OnboardingGraphController
import de.gematik.ti.erp.app.onboarding.ui.components.OnboardingBiometricDialog
import de.gematik.ti.erp.app.onboarding.ui.components.OnboardingDeviceSecurityDialog
import de.gematik.ti.erp.app.onboarding.ui.preview.AuthScenarioPreviewData
import de.gematik.ti.erp.app.onboarding.ui.preview.AuthScenarioPreviewParameterProvider
import de.gematik.ti.erp.app.semantics.semanticsHeading
import de.gematik.ti.erp.app.settings.model.SettingsData
import de.gematik.ti.erp.app.theme.AppTheme
import de.gematik.ti.erp.app.theme.PaddingDefaults
import de.gematik.ti.erp.app.utils.SpacerXXLarge
import de.gematik.ti.erp.app.utils.compose.ComposableEvent
import de.gematik.ti.erp.app.utils.compose.ComposableEvent.Companion.trigger
import de.gematik.ti.erp.app.utils.compose.LightDarkPreview
import de.gematik.ti.erp.app.utils.compose.preview.PreviewAppTheme
import de.gematik.ti.erp.app.utils.extensions.BuildConfigExtension
import de.gematik.ti.erp.app.utils.extensions.LocalDialog

class OnboardingSelectAppLoginScreen(
    override val navController: NavController,
    override val navBackStackEntry: NavBackStackEntry,
    private val graphController: OnboardingGraphController
) : Screen() {

    @Composable
    override fun Content() {
        val lazyListState = rememberLazyListState()
        val context = LocalContext.current
        val dialog = LocalDialog.current
        val currentStep by graphController.currentStep.collectAsStateWithLifecycle(2)
        val lifecycleOwner = LocalLifecycleOwner.current

        val activity = LocalActivity.current
        val biometricPromptBuilder =
            remember { BiometricPromptBuilder(activity as AppCompatActivity) }
        val promptInfo = biometricPromptBuilder.buildPromptInfoWithAllAuthenticatorsAvailable(
            title = stringResource(R.string.auth_prompt_headline),
            description = stringResource(R.string.alternate_auth_info)
        )

        val prompt = remember(biometricPromptBuilder) {
            biometricPromptBuilder.buildBiometricPrompt(
                onSuccess = {
                    graphController.onChooseAuthentication(
                        authentication = SettingsData.Authentication(
                            deviceSecurity = true,
                            failedAuthenticationAttempts = 0,
                            password = null,
                            authenticationTimeOutSystemUptime = null
                        )
                    )
                    graphController.nextStep()
                    navController.navigate(OnboardingRoutes.OnboardingAnalyticsPreviewScreen.path())
                }
            )
        }

        DisposableEffect(lifecycleOwner) {
            val observer = LifecycleEventObserver { _, event ->
                if (event == Lifecycle.Event.ON_RESUME) {
                    graphController.refreshAuthScenario()
                }
            }
            lifecycleOwner.lifecycle.addObserver(observer)
            onDispose {
                lifecycleOwner.lifecycle.removeObserver(observer)
            }
        }

        val showDeviceSecurityDialogEvent: ComposableEvent<Unit> = ComposableEvent()
        val showBiometricDialogEvent: ComposableEvent<Unit> = ComposableEvent()
        BackHandler {}
        OnboardingScreenContent(
            lazyListState = lazyListState,
            currentStep = currentStep,
            authScenario = graphController.authScenario,
            onClickPassword = {
                navController.navigate(OnboardingRoutes.OnboardingPasswordAuthenticationScreen.path())
            },
            onClickDeviceSecurity = {
                when (graphController.authScenario) {
                    OnboardingAuthScenario.DEVICE_CREDENTIALS_ENABLED -> {
                        prompt.authenticate(promptInfo)
                    }

                    OnboardingAuthScenario.BIOMETRIC_ENABLED -> {
                        prompt.authenticate(promptInfo)
                    }

                    OnboardingAuthScenario.DEVICE_CREDENTIALS_NOT_ENABLED -> {
                        showDeviceSecurityDialogEvent.trigger()
                    }

                    OnboardingAuthScenario.BIOMETRIC_NOT_ENABLED -> {
                        showBiometricDialogEvent.trigger()
                    }
                }
            },
            onSkip = {
                graphController.createProfileOnSkipOnboarding()
                navController.finishOnboardingAsSuccessAndOpenPrescriptions()
            }
        )

        OnboardingDeviceSecurityDialog(
            dialogScaffold = dialog,
            event = showDeviceSecurityDialogEvent
        ) {
            val securityIntent = enrollDeviceSecurityIntent()
            context.startActivity(securityIntent)
        }

        OnboardingBiometricDialog(
            dialogScaffold = dialog,
            event = showBiometricDialogEvent
        ) {
            val enrollIntent = enrollBiometricsIntent()
            context.startActivity(enrollIntent)
        }
    }
}

@Composable
fun OnboardingScreenContent(
    lazyListState: LazyListState,
    currentStep: Int,
    authScenario: OnboardingAuthScenario,
    onClickDeviceSecurity: () -> Unit,
    onClickPassword: () -> Unit,
    onSkip: () -> Unit,
    showSkipButton: Boolean = BuildConfigExtension.isInternalDebug
) {
    OnboardingScreenScaffold(
        modifier = Modifier
            .testTag(TestTag.Onboarding.CredentialsScreen)
            .fillMaxSize(),
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
        bottomBar = {}
    ) {
        onboardingSelectAppLoginContent(
            authScenario = authScenario,
            onClickDeviceSecurity = onClickDeviceSecurity,
            onClickPassword = onClickPassword
        )
    }

    if (showSkipButton) {
        SkipOnBoardingButton(onSkip)
    }
}

@Requirement(
    "O.Resi_1#1",
    sourceSpecification = "BSI-eRp-ePA",
    rationale = "Selection of secure app login method in onboarding process"
)
private fun LazyListScope.onboardingSelectAppLoginContent(
    authScenario: OnboardingAuthScenario,
    onClickDeviceSecurity: () -> Unit,
    onClickPassword: () -> Unit
) {
    item {
        Text(
            modifier = Modifier.semanticsHeading(),
            text = stringResource(R.string.onboarding_select_app_login_screen_header),
            style = AppTheme.typography.h5,
            fontWeight = FontWeight.W900
        )

        Text(
            text = stringResource(R.string.onboarding_select_app_login_screen_info),
            style = AppTheme.typography.subtitle1,
            fontWeight = FontWeight.W100,
            modifier = Modifier.padding(top = PaddingDefaults.Small)
        )

        Text(
            text = stringResource(R.string.cdw_intro_auth_prior),
            style = AppTheme.typography.subtitle2,
            fontWeight = FontWeight.Bold,
            color = AppTheme.colors.primary700,
            modifier = Modifier
                .offset(x = PaddingDefaults.Medium)
                .semanticsHeading()
                .padding(top = PaddingDefaults.Medium)
        )

        val (securityTitle, securityDescription) = when (authScenario) {
            OnboardingAuthScenario.DEVICE_CREDENTIALS_ENABLED -> {
                stringResource(R.string.settings_app_security_device_security) to
                    stringResource(R.string.onboarding_select_app_login_device_credentials_enabled_info)
            }

            OnboardingAuthScenario.BIOMETRIC_ENABLED -> {
                stringResource(R.string.onboarding_secure_app_biometric) to
                    stringResource(R.string.onboarding_select_app_login_biometric_info)
            }

            OnboardingAuthScenario.DEVICE_CREDENTIALS_NOT_ENABLED -> {
                stringResource(R.string.settings_app_security_device_security) to
                    stringResource(R.string.onboarding_select_app_login_device_credentials_not_enabled_info)
            }

            OnboardingAuthScenario.BIOMETRIC_NOT_ENABLED -> {
                stringResource(R.string.onboarding_secure_app_biometric) to
                    stringResource(R.string.onboarding_select_app_login_biometric_not_enabled_info)
            }
        }

        // Security option (biometric/device security)
        SelectionCard(
            data = SelectionCard(
                title = securityTitle,
                description = securityDescription,
                isRecommended = true,
                showIcon = false
            ),
            onClick = onClickDeviceSecurity
        )

        // Password option - always available
        SelectionCard(
            modifier = Modifier.padding(top = PaddingDefaults.Small),
            data = SelectionCard(
                title = stringResource(R.string.onboarding_secure_app_password),
                description = stringResource(R.string.onboarding_select_app_login_password_info),
                isRecommended = false,
                showIcon = true
            ),
            onClick = onClickPassword
        )
    }
}

@LightDarkPreview
@Composable
fun OnboardingSelectAppLoginPreview(
    @PreviewParameter(AuthScenarioPreviewParameterProvider::class) previewData: AuthScenarioPreviewData
) {
    val lazyListState = rememberLazyListState()
    PreviewAppTheme {
        OnboardingScreenContent(
            lazyListState = lazyListState,
            authScenario = previewData.authScenario,
            currentStep = 2,
            onClickDeviceSecurity = {},
            onClickPassword = {},
            onSkip = {},
            showSkipButton = false
        )
    }
}
