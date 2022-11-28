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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.ContentAlpha
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInParent
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import de.gematik.ti.erp.app.R
import de.gematik.ti.erp.app.TestTag
import de.gematik.ti.erp.app.mainscreen.ui.TextTabRow
import de.gematik.ti.erp.app.pharmacy.ui.scrollOnFocus
import de.gematik.ti.erp.app.settings.ui.ConfirmationPasswordTextField
import de.gematik.ti.erp.app.settings.ui.PasswordStrength
import de.gematik.ti.erp.app.settings.ui.PasswordTextField
import de.gematik.ti.erp.app.settings.ui.checkPassword
import de.gematik.ti.erp.app.settings.ui.checkPasswordScore
import de.gematik.ti.erp.app.theme.AppTheme
import de.gematik.ti.erp.app.theme.PaddingDefaults
import de.gematik.ti.erp.app.utils.compose.SpacerMedium
import de.gematik.ti.erp.app.utils.compose.SpacerTiny
import de.gematik.ti.erp.app.utils.compose.visualTestTag
import kotlinx.parcelize.Parcelize

private const val POS_OF_ANIMATED_CONTENT_ITEM = 3

@Immutable
sealed class OnboardingSecureAppMethod {
    @Immutable
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

@Immutable
private enum class AuthTab(val index: Int) {
    Password(1), Biometric(0)
}

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun OnboardingSecureApp(
    secureMethod: OnboardingSecureAppMethod,
    onSecureMethodChange: (OnboardingSecureAppMethod) -> Unit,
    onNextPage: () -> Unit,
    onOpenBiometricScreen: () -> Unit
) {
    val lazyListState = rememberLazyListState()
    var selectedTab by remember { mutableStateOf(AuthTab.Biometric) }

    OnboardingScaffold(
        modifier = Modifier
            .testTag(TestTag.Onboarding.CredentialsScreen)
            .fillMaxSize(),
        state = lazyListState,
        bottomBar = {
            OnboardingBottomBar(
                info = when (selectedTab) {
                    AuthTab.Password -> null
                    AuthTab.Biometric -> stringResource(R.string.onboarding_auth_biometric_info)
                },
                buttonText = when (selectedTab) {
                    AuthTab.Password -> stringResource(R.string.onboarding_bottom_button_save)
                    AuthTab.Biometric -> stringResource(R.string.onboarding_bottom_button_choose)
                },
                buttonEnabled = when (selectedTab) {
                    AuthTab.Password -> (secureMethod as? OnboardingSecureAppMethod.Password)?.checkedPassword != null
                    AuthTab.Biometric -> true
                },
                buttonModifier = Modifier.testTag(TestTag.Onboarding.NextButton),
                onButtonClick = {
                    when (selectedTab) {
                        AuthTab.Password ->
                            onNextPage()
                        AuthTab.Biometric ->
                            onOpenBiometricScreen()
                    }
                }
            )
        }
    ) {
        item {
            Image(
                painterResource(R.drawable.developer),
                contentDescription = null,
                alignment = Alignment.CenterStart,
                modifier = Modifier
                    .padding(
                        top = PaddingDefaults.XXLarge
                    )
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
                ),
                testTags = listOf(
                    TestTag.Onboarding.Credentials.BiometricTab,
                    TestTag.Onboarding.Credentials.PasswordTab
                )
            )
        }
        item {
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
                    AuthTab.Password -> {
                        PasswordAuthentication(
                            secureMethod = secureMethod,
                            lazyListState = lazyListState,
                            onSecureMethodChange = onSecureMethodChange,
                            onNext = onNextPage
                        )
                    }
                    AuthTab.Biometric -> {
                    }
                }
            }
            SpacerMedium()
        }
    }
}

@Composable
private fun PasswordAuthentication(
    secureMethod: OnboardingSecureAppMethod,
    lazyListState: LazyListState,
    onSecureMethodChange: (OnboardingSecureAppMethod) -> Unit,
    onNext: () -> Unit
) {
    var offsetFirstPassword by remember { mutableStateOf(0) }
    var offsetSecondPassword by remember { mutableStateOf(0) }

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

    val focusManager = LocalFocusManager.current

    Column(
        modifier = Modifier.wrapContentSize()
    ) {
        PasswordTextField(
            modifier = Modifier
                .visualTestTag(TestTag.Onboarding.Credentials.PasswordFieldA)
                .fillMaxWidth()
                .scrollOnFocus(POS_OF_ANIMATED_CONTENT_ITEM, lazyListState, offsetFirstPassword)
                .onGloballyPositioned { offsetFirstPassword = it.positionInParent().y.toInt() }
                .padding(bottom = PaddingDefaults.Tiny),
            value = password,
            onValueChange = {
                if (it.isEmpty()) {
                    onSecureMethodChange(OnboardingSecureAppMethod.None)
                } else {
                    onSecureMethodChange(
                        OnboardingSecureAppMethod.Password(
                            password = it,
                            repeatedPassword = repeatedPassword,
                            score = passwordScore
                        )
                    )
                }
            },
            onSubmit = {
                if (checkPasswordScore(passwordScore)) {
                    focusManager.moveFocus(FocusDirection.Down)
                }
            },
            allowAutofill = true,
            allowVisiblePassword = true,
            label = {
                Text(stringResource(R.string.settings_password_enter))
            }
        )
        PasswordStrength(
            modifier = Modifier
                .testTag(TestTag.Onboarding.Credentials.PasswordStrengthCheck)
                .fillMaxWidth()
                .padding(bottom = PaddingDefaults.Medium),
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
        ConfirmationPasswordTextField(
            modifier = Modifier
                .visualTestTag(TestTag.Onboarding.Credentials.PasswordFieldB)
                .fillMaxWidth()
                .scrollOnFocus(POS_OF_ANIMATED_CONTENT_ITEM, lazyListState, offsetSecondPassword)
                .onGloballyPositioned { offsetSecondPassword = it.positionInParent().y.toInt() },
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
        if (repeatedPassword.isNotBlank() && repeatedPassword != password) {
            SpacerTiny()
            Text(
                stringResource(R.string.not_matching_entries),
                style = AppTheme.typography.caption1,
                color = AppTheme.colors.red600.copy(
                    alpha = ContentAlpha.high
                )
            )
        }
    }
}
