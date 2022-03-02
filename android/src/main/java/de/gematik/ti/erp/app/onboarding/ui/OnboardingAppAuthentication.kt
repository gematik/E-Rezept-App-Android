/*
 * Copyright (c) 2022 gematik GmbH
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

import android.os.Parcelable
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.with
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons.Rounded
import androidx.compose.material.icons.rounded.Check
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import com.google.accompanist.insets.imePadding
import de.gematik.ti.erp.app.R
import de.gematik.ti.erp.app.db.entities.SettingsAuthenticationMethod.DeviceSecurity
import de.gematik.ti.erp.app.mainscreen.ui.TextTabRow
import de.gematik.ti.erp.app.settings.ui.ConfirmationPasswordTextField
import de.gematik.ti.erp.app.settings.ui.PasswordStrength
import de.gematik.ti.erp.app.settings.ui.PasswordTextField
import de.gematik.ti.erp.app.settings.ui.checkPassword
import de.gematik.ti.erp.app.theme.AppTheme
import de.gematik.ti.erp.app.theme.PaddingDefaults
import de.gematik.ti.erp.app.userauthentication.ui.BiometricPrompt
import de.gematik.ti.erp.app.utils.compose.CommonAlertDialog
import de.gematik.ti.erp.app.utils.compose.LargeButton
import de.gematik.ti.erp.app.utils.compose.SpacerLarge
import de.gematik.ti.erp.app.utils.compose.SpacerMedium
import de.gematik.ti.erp.app.utils.compose.SpacerSmall
import de.gematik.ti.erp.app.utils.compose.SpacerTiny
import de.gematik.ti.erp.app.utils.compose.SpacerXXLarge
import java.util.Locale
import kotlinx.parcelize.Parcelize

sealed class OnboardingSecureAppMethod {
    @Parcelize
    data class Password(val password: String, val repeatedPassword: String, val score: Int) :
        OnboardingSecureAppMethod(),
        Parcelable {
        val checkedPassword: String?
            get() =
                if (checkPassword(password, repeatedPassword, score)) {
                    password
                } else {
                    null
                }
    }

    @Parcelize
    object DeviceSecurity : OnboardingSecureAppMethod(), Parcelable

    @Parcelize
    object None : OnboardingSecureAppMethod(), Parcelable
}

private enum class AuthTab(val index: Int) {
    Password(1), Biometric(0)
}

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun OnboardingSecureApp(
    modifier: Modifier,
    isReturningUser: Boolean = false,
    secureMethod: OnboardingSecureAppMethod,
    onSecureMethodChange: (OnboardingSecureAppMethod) -> Unit,
    onNext: () -> Unit
) {
    val header = stringResource(R.string.on_boarding_secure_app_page_header)
    val info = stringResource(R.string.on_boarding_secure_app_page_info)

    var selectedTab by remember { mutableStateOf(AuthTab.Biometric) }

    Column(
        modifier = modifier
            .testTag("onboarding/secureAppPage")
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = PaddingDefaults.Large, vertical = PaddingDefaults.XXLarge)
    ) {

        if (isReturningUser) {
            Image(
                painterResource(R.drawable.laptop_woman_blue),
                null,
                contentScale = ContentScale.FillWidth,
                modifier = Modifier.fillMaxSize()
            )
            SpacerMedium()
        }

        Text(
            text = header,
            style = MaterialTheme.typography.h6,
            color = AppTheme.colors.primary900,
            textAlign = TextAlign.Center
        )

        SpacerMedium()

        Text(
            text = info,
            style = MaterialTheme.typography.body1,
            color = AppTheme.colors.neutral999,
        )

        SpacerLarge()

        TextTabRow(
            selectedTabIndex = selectedTab.index,
            modifier = Modifier.fillMaxWidth(),
            onClick = {
                when (it) {
                    0 -> {
                        selectedTab = AuthTab.Biometric
                    }
                    1 -> {
                        selectedTab = AuthTab.Password
                    }
                }
                onSecureMethodChange(OnboardingSecureAppMethod.None)
            },
            tabs = listOf(
                stringResource(R.string.onboarding_secure_app_biometric),
                stringResource(R.string.onboarding_secure_app_password)
            )
        )

        SpacerXXLarge()

        AnimatedContent(
            targetState = selectedTab,
            transitionSpec = {
                if (targetState.index > initialState.index) {
                    slideInHorizontally { width -> width } + fadeIn() with
                        slideOutHorizontally { width -> -width } + fadeOut()
                } else {
                    slideInHorizontally { height -> -height } + fadeIn() with
                        slideOutHorizontally { width -> width } + fadeOut()
                }.using(
                    SizeTransform(clip = false)
                )
            }
        ) { targetTab ->
            when (targetTab) {
                AuthTab.Password -> PasswordAuthentication(
                    secureMethod = secureMethod,
                    onSecureMethodChange = onSecureMethodChange,
                    onNext = onNext
                )
                AuthTab.Biometric -> BiometricAuthentication(
                    secureMethod = secureMethod,
                    onSecureMethodChange = onSecureMethodChange
                )
            }
        }
    }
}

@Composable
private fun PasswordAuthentication(
    secureMethod: OnboardingSecureAppMethod,
    onSecureMethodChange: (OnboardingSecureAppMethod) -> Unit,
    onNext: () -> Unit
) {
    val password =
        remember(secureMethod) { (secureMethod as? OnboardingSecureAppMethod.Password)?.password ?: "" }
    val repeatedPassword =
        remember(secureMethod) {
            (secureMethod as? OnboardingSecureAppMethod.Password)?.repeatedPassword ?: ""
        }
    val passwordScore =
        remember(secureMethod) {
            (secureMethod as? OnboardingSecureAppMethod.Password)?.score ?: 0
        }

    Column(
        Modifier.imePadding()
    ) {
        val focusRequester = FocusRequester.Default
        val focusManager = LocalFocusManager.current

        PasswordTextField(
            modifier = Modifier
                .testTag("onboarding/secure_text_input_1")
                .fillMaxWidth(),
            value = password,
            onValueChange = {
                if (it.isEmpty()) {
                    onSecureMethodChange(OnboardingSecureAppMethod.None)
                } else {
                    onSecureMethodChange(
                        OnboardingSecureAppMethod.Password(
                            password = it,
                            repeatedPassword = "",
                            score = passwordScore
                        )
                    )
                }
            },
            onSubmit = { focusRequester.requestFocus() },
            allowAutofill = true,
            allowVisiblePassword = true,
            label = {
                Text(stringResource(R.string.settings_password_enter_password))
            }
        )
        SpacerTiny()
        PasswordStrength(
            modifier = Modifier.fillMaxWidth(),
            password = password,
            onScoreChange = {
                onSecureMethodChange(
                    OnboardingSecureAppMethod.Password(
                        password = password,
                        repeatedPassword = repeatedPassword,
                        score = it
                    )
                )
            }
        )

        SpacerMedium()

        ConfirmationPasswordTextField(
            modifier = Modifier
                .testTag("onboarding/secure_text_input_2")
                .fillMaxWidth()
                .focusRequester(focusRequester),
            password = password,
            value = repeatedPassword,
            passwordScore = passwordScore,
            onValueChange = {
                onSecureMethodChange(
                    OnboardingSecureAppMethod.Password(
                        password = password,
                        repeatedPassword = it,
                        score = passwordScore
                    )
                )
            },
            onSubmit = {
                focusManager.clearFocus()
                onNext()
            }
        )
    }
}

@Composable
private fun BiometricAuthentication(
    secureMethod: OnboardingSecureAppMethod,
    onSecureMethodChange: (OnboardingSecureAppMethod) -> Unit
) {
    Column {
        var showBiometricPrompt by rememberSaveable { mutableStateOf(false) }
        var showAcceptDeviceAuthenticationInfo by rememberSaveable { mutableStateOf(false) }

        if (showAcceptDeviceAuthenticationInfo) {
            CommonAlertDialog(
                header = stringResource(R.string.settings_biometric_dialog_title),
                info = stringResource(R.string.settings_biometric_dialog_text),
                actionText = stringResource(R.string.settings_device_security_allow),
                cancelText = stringResource(R.string.cancel),
                onCancel = { showAcceptDeviceAuthenticationInfo = false },
                onClickAction = {
                    showBiometricPrompt = true
                    showAcceptDeviceAuthenticationInfo = false
                }
            )
        }

        if (showBiometricPrompt) {
            BiometricPrompt(
                authenticationMethod = DeviceSecurity,
                title = stringResource(R.string.auth_prompt_headline),
                description = "",
                negativeButton = stringResource(R.string.auth_prompt_cancel),
                onAuthenticated = {
                    showBiometricPrompt = false
                    onSecureMethodChange(OnboardingSecureAppMethod.DeviceSecurity)
                },
                onCancel = {
                    showBiometricPrompt = false
                },
                onAuthenticationError = {
                    showBiometricPrompt = false
                },
                onAuthenticationSoftError = {
                }
            )
        }

        val buttonColors = if (secureMethod == OnboardingSecureAppMethod.DeviceSecurity) {
            ButtonDefaults.buttonColors(
                backgroundColor = AppTheme.colors.green600,
                contentColor = AppTheme.colors.neutral000
            )
        } else {
            ButtonDefaults.buttonColors()
        }

        LargeButton(
            onClick = {
                showAcceptDeviceAuthenticationInfo = true
            },
            colors = buttonColors
        ) {
            if (secureMethod == OnboardingSecureAppMethod.DeviceSecurity) {
                Icon(Rounded.Check, null)
                SpacerSmall()
                Text(
                    stringResource(R.string.onboarding_secure_app_button_best_chosen).uppercase(
                        Locale.getDefault()
                    )
                )
            } else {
                Text(stringResource(R.string.onboarding_secure_app_button_best).uppercase(Locale.getDefault()))
            }
        }
        SpacerSmall()
        Text(
            stringResource(R.string.onboarding_secure_app_button_best_info),
            style = AppTheme.typography.body2l
        )
    }
}
