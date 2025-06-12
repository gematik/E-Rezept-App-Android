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
import androidx.appcompat.app.AppCompatActivity
import androidx.biometric.BiometricManager
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.semantics
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavController
import de.gematik.ti.erp.app.authentication.presentation.deviceBiometricStatus
import de.gematik.ti.erp.app.authentication.presentation.deviceDeviceSecurityStatus
import de.gematik.ti.erp.app.authentication.presentation.deviceHasAuthenticationMethodEnabled
import de.gematik.ti.erp.app.authentication.ui.components.EnrollBiometricDialog
import de.gematik.ti.erp.app.core.LocalActivity
import de.gematik.ti.erp.app.app_core.R
import de.gematik.ti.erp.app.navigation.Screen
import de.gematik.ti.erp.app.onboarding.navigation.OnboardingRoutes
import de.gematik.ti.erp.app.onboarding.presentation.OnboardingGraphController
import de.gematik.ti.erp.app.settings.model.SettingsData
import de.gematik.ti.erp.app.theme.AppTheme
import de.gematik.ti.erp.app.theme.PaddingDefaults
import de.gematik.ti.erp.app.userauthentication.observer.BiometricPromptBuilder
import de.gematik.ti.erp.app.utils.compose.AnimatedElevationScaffold
import de.gematik.ti.erp.app.utils.compose.ComposableEvent
import de.gematik.ti.erp.app.utils.compose.LightDarkPreview
import de.gematik.ti.erp.app.utils.compose.NavigationBarMode
import de.gematik.ti.erp.app.utils.compose.preview.PreviewAppTheme
import de.gematik.ti.erp.app.utils.extensions.LocalDialog

class BiometryScreen(
    override val navController: NavController,
    override val navBackStackEntry: NavBackStackEntry,
    private val graphController: OnboardingGraphController
) : Screen() {

    @Composable
    override fun Content() {
        val lazyListState = rememberLazyListState()

        val activity = LocalActivity.current
        val biometricPromptBuilder = remember { BiometricPromptBuilder(activity as AppCompatActivity) }
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
                            password = null
                        )
                    )
                    navController.navigate(OnboardingRoutes.OnboardingAnalyticsPreviewScreen.path())
                }
            )
        }

        val context = LocalContext.current
        val dialog = LocalDialog.current
        val showEnrollBiometricEvent = ComposableEvent<Unit>()
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

        BiometricScreenScaffold(
            lazyListState = lazyListState,
            onBottomBarButtonClick = {
                if (context.deviceDeviceSecurityStatus() == BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED) {
                    showEnrollBiometricEvent.trigger(Unit)
                } else {
                    prompt.authenticate(promptInfo)
                }
            },
            deviceHasDeviceSecurityEnabled = deviceHasDeviceSecurityEnabled,
            deviceHasBiometryEnabled = deviceHasBiometryEnabled,
            onBack = { navController.popBackStack() }
        )

        EnrollBiometricDialog(
            context = context,
            dialog = dialog,
            title = stringResource(R.string.enroll_biometric_dialog_header),
            body = stringResource(R.string.enroll_biometric_dialog_body),
            event = showEnrollBiometricEvent
        )
    }
}

@Composable
fun BiometricScreenScaffold(
    lazyListState: LazyListState,
    onBottomBarButtonClick: () -> Unit,
    deviceHasDeviceSecurityEnabled: Boolean,
    deviceHasBiometryEnabled: Boolean,
    onBack: () -> Unit
) {
    AnimatedElevationScaffold(
        modifier = Modifier.navigationBarsPadding(),
        navigationMode = NavigationBarMode.Close,
        bottomBar = {
            OnboardingBottomBar(
                buttonText = stringResource(R.string.settings_device_security_allow),
                onButtonClick = onBottomBarButtonClick,
                buttonEnabled = true,
                info = null,
                buttonModifier = Modifier
            )
        },
        topBarTitle = stringResource(R.string.settings_biometric_dialog_headline),
        listState = lazyListState,
        onBack = onBack
    ) {
        BiometricScreenContent(
            lazyListState = lazyListState,
            deviceHasDeviceSecurityEnabled = deviceHasDeviceSecurityEnabled,
            deviceHasBiometryEnabled = deviceHasBiometryEnabled
        )
    }
}

@Composable
fun BiometricScreenContent(
    lazyListState: LazyListState,
    deviceHasDeviceSecurityEnabled: Boolean,
    deviceHasBiometryEnabled: Boolean
) {
    LazyColumn(
        state = lazyListState,
        modifier = Modifier
            .wrapContentSize()
            .padding(horizontal = PaddingDefaults.Medium)
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
                if (deviceHasDeviceSecurityEnabled && !deviceHasBiometryEnabled) {
                    Text(
                        text = stringResource(R.string.settings_device_security_dialog_text),
                        style = AppTheme.typography.body1,
                        modifier = Modifier.padding(
                            bottom = PaddingDefaults.Small
                        )
                    )
                } else {
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

@LightDarkPreview()
@Composable
fun BiometryScreenPreview() {
    PreviewAppTheme {
        BiometricScreenScaffold(
            lazyListState = rememberLazyListState(),
            onBottomBarButtonClick = {},
            deviceHasBiometryEnabled = true,
            deviceHasDeviceSecurityEnabled = true,
            onBack = {}
        )
    }
}
