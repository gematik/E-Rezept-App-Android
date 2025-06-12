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

import android.app.KeyguardManager
import android.content.Context
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.SizeTransform
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.ContentAlpha
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInParent
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavController
import de.gematik.ti.erp.app.Requirement
import de.gematik.ti.erp.app.TestTag
import de.gematik.ti.erp.app.authentication.presentation.deviceBiometricStatus
import de.gematik.ti.erp.app.authentication.presentation.deviceDeviceSecurityStatus
import de.gematik.ti.erp.app.authentication.presentation.deviceHasAuthenticationMethodEnabled
import de.gematik.ti.erp.app.authentication.presentation.deviceSupportsAuthenticationMethod
import de.gematik.ti.erp.app.app_core.R
import de.gematik.ti.erp.app.mainscreen.ui.TextTabRow
import de.gematik.ti.erp.app.navigation.Screen
import de.gematik.ti.erp.app.onboarding.model.OnboardingAuthTab
import de.gematik.ti.erp.app.onboarding.navigation.OnboardingRoutes
import de.gematik.ti.erp.app.onboarding.navigation.finishOnboardingAsSuccessAndOpenPrescriptions
import de.gematik.ti.erp.app.onboarding.presentation.OnboardingGraphController
import de.gematik.ti.erp.app.settings.model.SettingsData
import de.gematik.ti.erp.app.settings.ui.preview.SetAppPasswordParameter
import de.gematik.ti.erp.app.settings.ui.preview.SetAppPasswordParameterProvider
import de.gematik.ti.erp.app.theme.AppTheme
import de.gematik.ti.erp.app.theme.PaddingDefaults
import de.gematik.ti.erp.app.utils.SpacerMedium
import de.gematik.ti.erp.app.utils.SpacerTiny
import de.gematik.ti.erp.app.utils.compose.ConfirmationPasswordTextField
import de.gematik.ti.erp.app.utils.compose.LightDarkPreview
import de.gematik.ti.erp.app.utils.compose.PasswordStrength
import de.gematik.ti.erp.app.utils.compose.PasswordTextField
import de.gematik.ti.erp.app.utils.compose.presentation.PasswordFieldsData
import de.gematik.ti.erp.app.utils.compose.presentation.rememberPasswordFieldsController
import de.gematik.ti.erp.app.utils.compose.preview.PreviewAppTheme
import de.gematik.ti.erp.app.utils.compose.scrollOnFocus
import de.gematik.ti.erp.app.utils.extensions.BuildConfigExtension

private const val POS_OF_ANIMATED_CONTENT_ITEM = 3
private const val SUCCESS_SCORE = 9
private const val MEDIOCRE_SCORE = 3
private const val FAILURE_SCORE = 0

class OnboardingSelectAppLoginScreen(
    override val navController: NavController,
    override val navBackStackEntry: NavBackStackEntry,
    private val graphController: OnboardingGraphController
) : Screen() {

    @Composable
    override fun Content() {
        val passwordFieldsController = rememberPasswordFieldsController()
        val passwordFieldsState by passwordFieldsController.passwordFieldsState.collectAsStateWithLifecycle()
        val lazyListState = rememberLazyListState()
        var selectedTab by remember { mutableStateOf(OnboardingAuthTab.Biometric) }
        val context = LocalContext.current

        val deviceSupportsDeviceSecurity by remember {
            mutableStateOf(
                deviceSupportsAuthenticationMethod(context.deviceDeviceSecurityStatus())
            )
        }
        val keyguardManager = context.getSystemService(Context.KEYGUARD_SERVICE) as KeyguardManager
        val deviceHasDeviceSecurityEnabled by remember {
            mutableStateOf(
                keyguardManager.isDeviceSecure or deviceHasAuthenticationMethodEnabled(context.deviceDeviceSecurityStatus())
            )
        }
        val deviceHasBiometryEnabled by remember {
            mutableStateOf(
                deviceHasAuthenticationMethodEnabled(context.deviceBiometricStatus())
            )
        }

        OnboardingScreenContent(
            selectedTab = selectedTab,
            passwordFieldsState = passwordFieldsState,
            lazyListState = lazyListState,
            deviceSupportsDeviceSecurity = deviceSupportsDeviceSecurity,
            deviceHasDeviceSecurityEnabled = deviceHasDeviceSecurityEnabled,
            deviceHasBiometryEnabled = deviceHasBiometryEnabled,
            onPasswordChange = passwordFieldsController::onPasswordChange,
            onRepeatedPasswordChange = passwordFieldsController::onRepeatedPasswordChange,
            onTabChange = {
                when (it) {
                    0 -> selectedTab = OnboardingAuthTab.Biometric
                    1 -> selectedTab = OnboardingAuthTab.Password
                }
            },
            onChoosePassword = {
                graphController.onChooseAuthentication(
                    authentication = SettingsData.Authentication(
                        deviceSecurity = false,
                        failedAuthenticationAttempts = 0,
                        password = SettingsData.Authentication.Password(passwordFieldsState.password)
                    )
                )
                navController.navigate(OnboardingRoutes.OnboardingAnalyticsPreviewScreen.path())
            },
            onChooseDeviceSecurity = {
                navController.navigate(OnboardingRoutes.BiometricScreen.path())
            },
            onSkip = {
                graphController.createProfileOnSkipOnboarding()
                navController.finishOnboardingAsSuccessAndOpenPrescriptions()
            }
        )
    }
}

@Composable
fun OnboardingScreenContent(
    selectedTab: OnboardingAuthTab,
    passwordFieldsState: PasswordFieldsData,
    lazyListState: LazyListState,
    deviceSupportsDeviceSecurity: Boolean,
    deviceHasDeviceSecurityEnabled: Boolean,
    deviceHasBiometryEnabled: Boolean,
    onPasswordChange: (String) -> Unit,
    onRepeatedPasswordChange: (String) -> Unit,
    onTabChange: (Int) -> Unit,
    onChoosePassword: () -> Unit,
    onChooseDeviceSecurity: () -> Unit,
    onSkip: () -> Unit
) {
    OnboardingScreenScaffold(
        modifier = Modifier
            .testTag(TestTag.Onboarding.CredentialsScreen)
            .fillMaxSize(),
        state = lazyListState,
        bottomBar = {
            OnboardingBottomBar(
                info = when (selectedTab) {
                    OnboardingAuthTab.Password -> null
                    OnboardingAuthTab.Biometric -> if (deviceSupportsDeviceSecurity) {
                        stringResource(R.string.onboarding_auth_biometric_info)
                    } else {
                        stringResource(R.string.auth_no_biometry_info)
                    }
                },
                buttonText = when (selectedTab) {
                    OnboardingAuthTab.Password -> stringResource(R.string.onboarding_bottom_button_save)
                    OnboardingAuthTab.Biometric -> stringResource(R.string.onboarding_bottom_button_choose)
                },
                buttonEnabled = when (selectedTab) {
                    OnboardingAuthTab.Password -> passwordFieldsState.passwordIsValidAndConsistent
                    OnboardingAuthTab.Biometric -> deviceSupportsDeviceSecurity
                },
                buttonModifier = Modifier.testTag(TestTag.Onboarding.NextButton),
                onButtonClick = {
                    when (selectedTab) {
                        OnboardingAuthTab.Password -> onChoosePassword()
                        OnboardingAuthTab.Biometric -> onChooseDeviceSecurity()
                    }
                }
            )
        }
    ) {
        onboardingSelectAppLoginContent(
            selectedTab = selectedTab,
            passwordFieldsState = passwordFieldsState,
            deviceHasDeviceSecurityEnabled = deviceHasDeviceSecurityEnabled,
            deviceHasBiometryEnabled = deviceHasBiometryEnabled,
            lazyListState = lazyListState,
            onPasswordChange = onPasswordChange,
            onRepeatedPasswordChange = onRepeatedPasswordChange,
            onTabChange = onTabChange,
            onChoosePassword = onChoosePassword
        )
    }

    if (BuildConfigExtension.isNonReleaseMode) {
        SkipOnBoardingButton(onSkip)
    }
}

@Requirement(
    "O.Resi_1#1",
    sourceSpecification = "BSI-eRp-ePA",
    rationale = "Selection of secure app login method in onboarding process"
)
private fun LazyListScope.onboardingSelectAppLoginContent(
    selectedTab: OnboardingAuthTab,
    onTabChange: (Int) -> Unit,
    passwordFieldsState: PasswordFieldsData,
    deviceHasDeviceSecurityEnabled: Boolean,
    deviceHasBiometryEnabled: Boolean,
    lazyListState: LazyListState,
    onPasswordChange: (String) -> Unit,
    onRepeatedPasswordChange: (String) -> Unit,
    onChoosePassword: () -> Unit
) {
    item {
        Image(
            painterResource(R.drawable.developer),
            contentDescription = null,
            alignment = Alignment.CenterStart,
            modifier = Modifier
                .padding(top = PaddingDefaults.XXLarge)
                .fillMaxWidth()
        )
    }
    item {
        Text(
            text = stringResource(R.string.on_boarding_secure_app_page_header),
            style = AppTheme.typography.h4,
            fontWeight = FontWeight.W700,
            textAlign = TextAlign.Start,
            modifier = Modifier.padding(
                bottom = PaddingDefaults.XLarge,
                top = PaddingDefaults.XXLarge
            )
        )
    }
    item {
        TextTabRow(
            selectedTabIndex = selectedTab.index,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = PaddingDefaults.XXLarge),
            backGroundColor = MaterialTheme.colors.background,
            onClick = onTabChange,
            tabs = listOf(
                if (!deviceHasBiometryEnabled && deviceHasDeviceSecurityEnabled) { stringResource(id = R.string.settings_app_security_device_security) } else {
                    stringResource(
                        R.string.onboarding_secure_app_biometric
                    )
                },
                stringResource(R.string.onboarding_secure_app_password)
            ),
            testTags = listOf(
                TestTag.Onboarding.Credentials.BiometricTab,
                TestTag.Onboarding.Credentials.PasswordTab
            )
        )
    }
    item {
        AnimatedContent(
            label = "AnimatedContent",
            targetState = selectedTab,
            transitionSpec = {
                if (targetState.index > initialState.index) {
                    slideHorizontal()
                } else {
                    slideHorizontal()
                }.using(SizeTransform(clip = false))
            }
        ) { targetTab ->
            when (targetTab) {
                OnboardingAuthTab.Password -> {
                    PasswordAuthentication(
                        passwordFieldsState = passwordFieldsState,
                        lazyListState = lazyListState,
                        onPasswordChange = onPasswordChange,
                        onRepeatedPasswordChange = onRepeatedPasswordChange,
                        onNext = onChoosePassword
                    )
                }

                OnboardingAuthTab.Biometric -> {
                    // TODO add some animated image here
                }
            }
        }
        SpacerMedium()
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
                .scrollOnFocus(POS_OF_ANIMATED_CONTENT_ITEM, lazyListState, offsetFirstPassword)
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
                .scrollOnFocus(POS_OF_ANIMATED_CONTENT_ITEM, lazyListState, offsetSecondPassword)
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
        OnboardingScreenContent(
            selectedTab = OnboardingAuthTab.Password,
            passwordFieldsState = parameter.passwordFieldsState,
            lazyListState = lazyListState,
            deviceSupportsDeviceSecurity = false,
            deviceHasBiometryEnabled = false,
            deviceHasDeviceSecurityEnabled = false,
            onPasswordChange = {},
            onRepeatedPasswordChange = {},
            onTabChange = {},
            onChoosePassword = {},
            onChooseDeviceSecurity = {},
            onSkip = {}
        )
    }
}
