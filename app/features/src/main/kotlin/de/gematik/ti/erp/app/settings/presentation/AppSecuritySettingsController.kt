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

package de.gematik.ti.erp.app.settings.presentation

import android.app.KeyguardManager
import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import androidx.biometric.BiometricPrompt
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import de.gematik.ti.erp.app.base.Controller
import de.gematik.ti.erp.app.core.LocalActivity
import de.gematik.ti.erp.app.features.R
import de.gematik.ti.erp.app.settings.model.SettingsData
import de.gematik.ti.erp.app.settings.usecase.DisableDeviceSecurityUseCase
import de.gematik.ti.erp.app.settings.usecase.GetAuthenticationUseCase
import de.gematik.ti.erp.app.settings.usecase.EnableDeviceSecurityUseCase
import de.gematik.ti.erp.app.settings.usecase.ResetPasswordUseCase
import de.gematik.ti.erp.app.userauthentication.observer.BiometricPromptBuilder
import de.gematik.ti.erp.app.utils.compose.ComposableEvent
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import org.kodein.di.compose.rememberInstance

class AppSecuritySettingsController(
    private val enableDeviceSecurityUseCase: EnableDeviceSecurityUseCase,
    private val disableDeviceSecurityUseCase: DisableDeviceSecurityUseCase,
    private val resetPasswordUseCase: ResetPasswordUseCase,
    private val getAuthenticationUseCase: GetAuthenticationUseCase,
    private val biometricPromptBuilder: BiometricPromptBuilder,
    private val promptInfo: BiometricPrompt.PromptInfo,
    private val keyguardManager: KeyguardManager
) : Controller() {
    val events = AppSecuritySettingsEvents()

    val authenticationState by lazy {
        getAuthenticationUseCase().stateIn(
            controllerScope,
            started = SharingStarted.WhileSubscribed(),
            initialValue = defaultAuthentication
        )
    }

    fun onSwitchDeviceSecurityAuthentication(isChecked: Boolean) {
        controllerScope.launch {
            when {
                !keyguardManager.isDeviceSecure -> {
                    events.enrollBiometryEvent.trigger(Unit)
                }
                isChecked -> {
                    onAuthenticateWithDeviceSecurity()
                }
                !isChecked -> {
                    disableDeviceSecurityUseCase()
                }
            }
        }
    }

    private fun onAuthenticateWithDeviceSecurity() {
        val prompt = biometricPromptBuilder.buildBiometricPrompt(
            onSuccess = {
                onEnableDeviceSecurity()
            },
            onError = { errorMessage, errorCode ->
                // handle error
            }
        )
        prompt.authenticate(promptInfo)
    }

    private fun onEnableDeviceSecurity() {
        controllerScope.launch {
            enableDeviceSecurityUseCase()
        }
    }

    fun onSwitchPasswordAuthentication(
        isChecked: Boolean
    ) {
        controllerScope.launch {
            when (isChecked) {
                true -> {
                    events.openPasswordScreenEvent.trigger(Unit)
                }
                false -> {
                    resetPasswordUseCase()
                }
            }
        }
    }
}

val defaultAuthentication = SettingsData.Authentication(
    password = null,
    deviceSecurity = false,
    failedAuthenticationAttempts = 0
)

data class AppSecuritySettingsEvents(
    val enrollBiometryEvent: ComposableEvent<Unit> = ComposableEvent<Unit>(),
    val openPasswordScreenEvent: ComposableEvent<Unit> = ComposableEvent<Unit>()
)

@Composable
fun rememberAppSecuritySettingsController(): AppSecuritySettingsController {
    val enableDeviceSecurityUseCase by rememberInstance<EnableDeviceSecurityUseCase>()
    val disableDeviceSecurityUseCase by rememberInstance<DisableDeviceSecurityUseCase>()
    val resetPasswordUseCase by rememberInstance<ResetPasswordUseCase>()
    val getAuthenticationUseCase by rememberInstance<GetAuthenticationUseCase>()

    val activity = LocalActivity.current
    val biometricPromptBuilder = remember { BiometricPromptBuilder(activity as AppCompatActivity) }
    val promptInfo = biometricPromptBuilder.buildPromptInfoWithAllAuthenticatorsAvailable(
        title = stringResource(R.string.auth_prompt_headline),
        description = stringResource(R.string.alternate_auth_info)
    )

    val context = LocalContext.current
    val keyguardManager = context.getSystemService(Context.KEYGUARD_SERVICE) as KeyguardManager

    return remember {
        AppSecuritySettingsController(
            enableDeviceSecurityUseCase = enableDeviceSecurityUseCase,
            disableDeviceSecurityUseCase = disableDeviceSecurityUseCase,
            resetPasswordUseCase = resetPasswordUseCase,
            getAuthenticationUseCase = getAuthenticationUseCase,
            biometricPromptBuilder = biometricPromptBuilder,
            promptInfo = promptInfo,
            keyguardManager = keyguardManager
        )
    }
}
