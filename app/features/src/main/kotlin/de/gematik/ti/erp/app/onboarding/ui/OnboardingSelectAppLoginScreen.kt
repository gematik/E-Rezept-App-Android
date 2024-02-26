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
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavController
import de.gematik.ti.erp.app.TestTag
import de.gematik.ti.erp.app.features.R
import de.gematik.ti.erp.app.mainscreen.ui.TextTabRow
import de.gematik.ti.erp.app.navigation.Screen
import de.gematik.ti.erp.app.onboarding.model.OnboardingAuthTab
import de.gematik.ti.erp.app.onboarding.model.OnboardingSecureAppMethod
import de.gematik.ti.erp.app.onboarding.model.OnboardingSecureAppMethod.Companion.toAuthenticationMode
import de.gematik.ti.erp.app.onboarding.model.OnboardingSecureAppMethod.None
import de.gematik.ti.erp.app.onboarding.model.OnboardingSecureAppMethod.Password
import de.gematik.ti.erp.app.onboarding.navigation.OnboardingRoutes
import de.gematik.ti.erp.app.onboarding.navigation.finishOnboardingAsSuccessAndOpenPrescriptions
import de.gematik.ti.erp.app.onboarding.presentation.rememberOnboardingController
import de.gematik.ti.erp.app.pharmacy.ui.scrollOnFocus
import de.gematik.ti.erp.app.theme.AppTheme
import de.gematik.ti.erp.app.theme.PaddingDefaults
import de.gematik.ti.erp.app.utils.compose.ConfirmationPasswordTextField
import de.gematik.ti.erp.app.utils.compose.LightDarkPreview
import de.gematik.ti.erp.app.utils.compose.PasswordStrength
import de.gematik.ti.erp.app.utils.compose.PasswordTextField
import de.gematik.ti.erp.app.utils.compose.PreviewAppTheme
import de.gematik.ti.erp.app.utils.compose.SpacerMedium
import de.gematik.ti.erp.app.utils.compose.SpacerTiny
import de.gematik.ti.erp.app.utils.compose.validatePasswordScore
import de.gematik.ti.erp.app.utils.extensions.BuildConfigExtension

private const val POS_OF_ANIMATED_CONTENT_ITEM = 3
private const val SUCCESS_SCORE = 9
private const val MEDIOCRE_SCORE = 3
private const val FAILURE_SCORE = 0

class OnboardingSelectAppLoginScreen(
    override val navController: NavController,
    override val navBackStackEntry: NavBackStackEntry
) : Screen() {

    @Composable
    override fun Content() {
        val controller = rememberOnboardingController()
        val secureAppMethod by controller.secureAppMethod
        val profileName = stringResource(R.string.onboarding_default_profile_name)
        val lazyListState = rememberLazyListState()
        var selectedTab by remember { mutableStateOf(OnboardingAuthTab.Biometric) }

        OnboardingScaffold(
            modifier = Modifier
                .testTag(TestTag.Onboarding.CredentialsScreen)
                .fillMaxSize(),
            state = lazyListState,
            bottomBar = {
                OnboardingBottomBar(
                    info = when (selectedTab) {
                        OnboardingAuthTab.Password -> null
                        OnboardingAuthTab.Biometric -> stringResource(R.string.onboarding_auth_biometric_info)
                    },
                    buttonText = when (selectedTab) {
                        OnboardingAuthTab.Password -> stringResource(R.string.onboarding_bottom_button_save)
                        OnboardingAuthTab.Biometric -> stringResource(R.string.onboarding_bottom_button_choose)
                    },
                    buttonEnabled = when (selectedTab) {
                        OnboardingAuthTab.Password -> (secureAppMethod as? Password)?.checkedPassword != null
                        OnboardingAuthTab.Biometric -> true
                    },
                    buttonModifier = Modifier.testTag(TestTag.Onboarding.NextButton),
                    onButtonClick = {
                        when (selectedTab) {
                            OnboardingAuthTab.Password -> {
                                controller.onSaveOnboardingData(
                                    authenticationMode = secureAppMethod.toAuthenticationMode(),
                                    profileName = profileName
                                )
                                navController.navigate(OnboardingRoutes.OnboardingAnalyticsPreviewScreen.path())
                            }
                            OnboardingAuthTab.Biometric -> {
                                navController.navigate(OnboardingRoutes.BiometricScreen.path())
                            }
                        }
                    }
                )
            }
        ) {
            onboardingSelectAppLoginContent(
                selectedTab = selectedTab,
                secureMethod = secureAppMethod,
                lazyListState = lazyListState,
                onSecureMethodChange = controller::updateAuthenticationMode,
                onTabChange = {
                    when (it) {
                        0 -> selectedTab = OnboardingAuthTab.Biometric
                        1 -> selectedTab = OnboardingAuthTab.Password
                    }
                    controller.updateAuthenticationMode(None)
                },
                onNext = {
                    controller.onSaveOnboardingData(
                        authenticationMode = secureAppMethod.toAuthenticationMode(),
                        profileName = profileName
                    )
                    navController.navigate(OnboardingRoutes.OnboardingAnalyticsPreviewScreen.path())
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

private fun LazyListScope.onboardingSelectAppLoginContent(
    selectedTab: OnboardingAuthTab,
    onTabChange: (Int) -> Unit,
    secureMethod: OnboardingSecureAppMethod,
    lazyListState: LazyListState,
    onSecureMethodChange: (OnboardingSecureAppMethod) -> Unit,
    onNext: () -> Unit
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
                        secureMethod = secureMethod,
                        lazyListState = lazyListState,
                        onSecureMethodChange = onSecureMethodChange,
                        onNext = onNext
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
    secureMethod: OnboardingSecureAppMethod,
    lazyListState: LazyListState,
    onSecureMethodChange: (OnboardingSecureAppMethod) -> Unit,
    onNext: () -> Unit
) {
    var offsetFirstPassword by remember { mutableIntStateOf(0) }
    var offsetSecondPassword by remember { mutableIntStateOf(0) }

    val password =
        remember(secureMethod) { (secureMethod as? Password)?.password ?: "" }
    val repeatedPassword =
        remember(secureMethod) {
            (secureMethod as? Password)?.repeatedPassword ?: ""
        }
    val passwordScore =
        remember(secureMethod) {
            (secureMethod as? Password)?.score ?: 0
        }

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
            value = password,
            onValueChange = {
                if (it.isEmpty()) {
                    onSecureMethodChange(None)
                } else {
                    onSecureMethodChange(
                        Password(
                            password = it,
                            repeatedPassword = repeatedPassword,
                            score = passwordScore
                        )
                    )
                }
            },
            onSubmit = {
                if (validatePasswordScore(passwordScore)) {
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
                    Password(
                        password = password,
                        repeatedPassword = repeatedPassword,
                        score = it
                    )
                )
            }
        )
        ConfirmationPasswordTextField(
            modifier = Modifier
                .testTag(TestTag.Onboarding.Credentials.PasswordFieldB)
                .fillMaxWidth()
                .scrollOnFocus(POS_OF_ANIMATED_CONTENT_ITEM, lazyListState, offsetSecondPassword)
                .onGloballyPositioned { offsetSecondPassword = it.positionInParent().y.toInt() },
            password = password,
            value = repeatedPassword,
            passwordScore = passwordScore,
            onValueChange = {
                onSecureMethodChange(
                    Password(
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
                color = AppTheme.colors.red600.copy(alpha = ContentAlpha.high)
            )
        }
    }
}

@LightDarkPreview
@Composable
fun PasswordAuthenticationSuccessfulPreview() {
    val lazyListState = rememberLazyListState()
    PreviewAppTheme {
        PasswordAuthentication(
            secureMethod = Password(
                "azerbaijan@89Atropates",
                "azerbaijan@89Atropates",
                SUCCESS_SCORE
            ),
            lazyListState = lazyListState,
            onSecureMethodChange = {},
            onNext = {}
        )
    }
}

@LightDarkPreview
@Composable
fun PasswordAuthenticationFailurePreview() {
    val lazyListState = rememberLazyListState()
    PreviewAppTheme {
        PasswordAuthentication(
            secureMethod = Password(
                "azerbaijan@89",
                "",
                MEDIOCRE_SCORE
            ),
            lazyListState = lazyListState,
            onSecureMethodChange = {},
            onNext = {}
        )
    }
}

@LightDarkPreview
@Composable
fun PasswordAuthenticationEmptyPreview() {
    val lazyListState = rememberLazyListState()
    PreviewAppTheme {
        PasswordAuthentication(
            secureMethod = Password(
                "",
                "",
                FAILURE_SCORE
            ),
            lazyListState = lazyListState,
            onSecureMethodChange = {},
            onNext = {}
        )
    }
}
