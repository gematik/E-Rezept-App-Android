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
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.ContentAlpha
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInParent
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.PreviewParameter
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
import de.gematik.ti.erp.app.settings.model.SettingsData
import de.gematik.ti.erp.app.settings.ui.preview.SetAppPasswordParameter
import de.gematik.ti.erp.app.settings.ui.preview.SetAppPasswordParameterProvider
import de.gematik.ti.erp.app.theme.AppTheme
import de.gematik.ti.erp.app.theme.PaddingDefaults
import de.gematik.ti.erp.app.utils.SpacerTiny
import de.gematik.ti.erp.app.utils.SpacerXXLarge
import de.gematik.ti.erp.app.utils.compose.ConfirmationPasswordTextField
import de.gematik.ti.erp.app.utils.compose.LightDarkPreview
import de.gematik.ti.erp.app.utils.compose.PasswordStrength
import de.gematik.ti.erp.app.utils.compose.PasswordTextField
import de.gematik.ti.erp.app.utils.compose.presentation.PasswordFieldsData
import de.gematik.ti.erp.app.utils.compose.presentation.rememberPasswordFieldsController
import de.gematik.ti.erp.app.utils.compose.preview.PreviewAppTheme
import de.gematik.ti.erp.app.utils.compose.scrollOnFocus
import de.gematik.ti.erp.app.utils.extensions.BuildConfigExtension

private const val POS_OF_PASSWORD_CONTENT_ITEM = 2

class OnboardingPasswordAuthenticationScreen(
    override val navController: NavController,
    override val navBackStackEntry: NavBackStackEntry,
    private val graphController: OnboardingGraphController
) : Screen() {

    @Composable
    override fun Content() {
        val passwordFieldsController = rememberPasswordFieldsController()
        passwordFieldsController ?: return
        val lazyListState = rememberLazyListState()
        val currentStep by graphController.currentStep.collectAsStateWithLifecycle(2)
        val passwordFieldsState by passwordFieldsController.passwordFieldsState.collectAsStateWithLifecycle()
        val onBack by rememberUpdatedState { navController.popBackStack() }
        BackHandler { onBack() }
        OnboardingPasswordScreenScaffold(
            passwordFieldsState = passwordFieldsState,
            lazyListState = lazyListState,
            currentStep = currentStep,
            onPasswordChange = passwordFieldsController::onPasswordChange,
            onRepeatedPasswordChange = passwordFieldsController::onRepeatedPasswordChange,
            onChoosePassword = {
                graphController.onChooseAuthentication(
                    authentication = SettingsData.Authentication(
                        deviceSecurity = false,
                        failedAuthenticationAttempts = 0,
                        password = SettingsData.Authentication.Password(passwordFieldsState.password),
                        authenticationTimeOutSystemUptime = null
                    )
                )
                graphController.nextStep()
                navController.navigate(OnboardingRoutes.OnboardingAnalyticsPreviewScreen.path())
            },
            onCancel = {
                navController.popBackStack()
            },
            onSkip = {
                graphController.createProfileOnSkipOnboarding()
                navController.finishOnboardingAsSuccessAndOpenPrescriptions()
            }
        )
    }
}

@Composable
fun OnboardingPasswordScreenScaffold(
    passwordFieldsState: PasswordFieldsData,
    lazyListState: LazyListState,
    currentStep: Int,
    onPasswordChange: (String) -> Unit,
    onRepeatedPasswordChange: (String) -> Unit,
    onChoosePassword: () -> Unit,
    onCancel: () -> Unit,
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
        bottomBar = {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                OnboardingBottomBar(
                    info = null,
                    buttonText = stringResource(R.string.onboarding_bottom_button_save),
                    buttonEnabled = passwordFieldsState.passwordIsValidAndConsistent,
                    modifier = Modifier.testTag(TestTag.Onboarding.NextButton),
                    buttonStyle = OnboardingButtonStyle.Filled,
                    onButtonClick = onChoosePassword,
                    includeBottomSpacer = false
                )
                OnboardingBottomBar(
                    info = null,
                    buttonText = stringResource(R.string.back),
                    buttonEnabled = true,
                    buttonStyle = OnboardingButtonStyle.Outline(showLeadingIcon = true),
                    onButtonClick = { onCancel() }
                )
            }
        }
    ) {
        onboardingPasswordContent(
            passwordFieldsState = passwordFieldsState,
            lazyListState = lazyListState,
            onPasswordChange = onPasswordChange,
            onRepeatedPasswordChange = onRepeatedPasswordChange,
            onChoosePassword = onChoosePassword
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
private fun LazyListScope.onboardingPasswordContent(
    passwordFieldsState: PasswordFieldsData,
    lazyListState: LazyListState,
    onPasswordChange: (String) -> Unit,
    onRepeatedPasswordChange: (String) -> Unit,
    onChoosePassword: () -> Unit
) {
    item {
        Text(
            modifier = Modifier.semanticsHeading(),
            text = stringResource(R.string.onboarding_secure_app_password),
            style = AppTheme.typography.h5,
            textAlign = TextAlign.Start
        )
    }
    item {
        Text(
            text = stringResource(R.string.settings_password_body),
            style = AppTheme.typography.body1l,
            modifier = Modifier.padding(
                top = PaddingDefaults.Small
            )
        )
    }
    item {
        SpacerXXLarge()
        PasswordAuthentication(
            passwordFieldsState = passwordFieldsState,
            lazyListState = lazyListState,
            onPasswordChange = onPasswordChange,
            onRepeatedPasswordChange = onRepeatedPasswordChange,
            onNext = onChoosePassword
        )
    }
}

@Composable
private fun PasswordAuthentication(
    passwordFieldsState: PasswordFieldsData,
    lazyListState: LazyListState,
    onPasswordChange: (String) -> Unit,
    onRepeatedPasswordChange: (String) -> Unit,
    onNext: () -> Unit
) {
    var offsetFirstPassword by remember { mutableIntStateOf(0) }
    var offsetSecondPassword by remember { mutableIntStateOf(0) }

    val focusManager = LocalFocusManager.current

    Column(
        modifier = Modifier.wrapContentSize()
    ) {
        PasswordTextField(
            modifier = Modifier
                .testTag(TestTag.Onboarding.Credentials.PasswordFieldA)
                .fillMaxWidth()
                .scrollOnFocus(POS_OF_PASSWORD_CONTENT_ITEM, lazyListState, offsetFirstPassword)
                .onGloballyPositioned { offsetFirstPassword = it.positionInParent().y.toInt() }
                .padding(bottom = PaddingDefaults.Tiny),
            value = passwordFieldsState.password,
            onValueChange = onPasswordChange,
            onSubmit = {
                if (passwordFieldsState.passwordEvaluation.isStrongEnough) {
                    focusManager.moveFocus(FocusDirection.Down)
                }
            },
            allowAutofill = true,
            allowVisiblePassword = true,
            label = {
                Text(stringResource(R.string.settings_password_entry))
            }
        )
        @Requirement(
            "O.Pass_1#2",
            sourceSpecification = "BSI-eRp-ePA",
            rationale = "Usage of password strength evaluation to ensure a secure password for onboarding"
        )
        @Requirement(
            "O.Pass_2#2",
            sourceSpecification = "BSI-eRp-ePA",
            rationale = "Shows password strength within the onboarding process"
        )
        PasswordStrength(
            modifier = Modifier
                .testTag(TestTag.Onboarding.Credentials.PasswordStrengthCheck)
                .fillMaxWidth()
                .padding(bottom = PaddingDefaults.Medium),
            passwordEvaluation = passwordFieldsState.passwordEvaluation
        )
        ConfirmationPasswordTextField(
            modifier = Modifier
                .testTag(TestTag.Onboarding.Credentials.PasswordFieldB)
                .fillMaxWidth()
                .scrollOnFocus(POS_OF_PASSWORD_CONTENT_ITEM, lazyListState, offsetSecondPassword)
                .onGloballyPositioned { offsetSecondPassword = it.positionInParent().y.toInt() },
            value = passwordFieldsState.repeatedPassword,
            onValueChange = onRepeatedPasswordChange,
            repeatedPasswordHasError = passwordFieldsState.repeatedPasswordHasError,
            passwordIsValidAndConsistent = passwordFieldsState.passwordIsValidAndConsistent,
            onSubmit = {
                focusManager.clearFocus()
                onNext()
            }
        )
        if (passwordFieldsState.repeatedPasswordHasError) {
            SpacerTiny()
            Text(
                stringResource(R.string.not_matching_entries),
                style = AppTheme.typography.caption1,
                color = AppTheme.colors.red600.copy(alpha = ContentAlpha.high)
            )
        }
    }
}

@LightDarkPreview
@Composable
fun PasswordAuthenticationPreview(
    @PreviewParameter(SetAppPasswordParameterProvider::class) parameter: SetAppPasswordParameter
) {
    val lazyListState = rememberLazyListState()
    PreviewAppTheme {
        OnboardingPasswordScreenScaffold(
            passwordFieldsState = parameter.passwordFieldsState,
            lazyListState = lazyListState,
            currentStep = 2,
            onPasswordChange = {},
            onRepeatedPasswordChange = {},
            onChoosePassword = {},
            onCancel = {},
            onSkip = {},
            showSkipButton = false
        )
    }
}
