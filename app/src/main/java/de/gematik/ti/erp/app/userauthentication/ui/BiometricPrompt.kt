/*
 * Copyright (c) 2021 gematik GmbH
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

import android.widget.Toast
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.findFragment
import de.gematik.ti.erp.app.db.entities.SettingsAuthenticationMethod

@Composable
fun BiometricPrompt(
    authenticationMode: SettingsAuthenticationMethod,
    title: String,
    description: String,
    negativeButton: String,
    onAuthenticated: () -> Unit,
    onCancel: () -> Unit,
    onAuthenticationError: () -> Unit
) {
    val view = LocalView.current
    val fr = remember { view.findFragment<Fragment>() }

    LaunchedEffect(fr) {
        nativeBiometricPrompt(
            authenticationMode,
            fr, title, description, negativeButton,
            object : BiometricPrompt.AuthenticationCallback() {

                override fun onAuthenticationSucceeded(
                    result: BiometricPrompt.AuthenticationResult
                ) {
                    super.onAuthenticationSucceeded(result)

                    onAuthenticated()
                }

                override fun onAuthenticationError(
                    errCode: Int,
                    errString: CharSequence
                ) {
                    super.onAuthenticationError(errCode, errString)

                    if (errCode == BiometricPrompt.ERROR_USER_CANCELED ||
                        errCode == BiometricPrompt.ERROR_NEGATIVE_BUTTON ||
                        errCode == BiometricPrompt.ERROR_CANCELED
                    ) {
                        onCancel()
                    } else {
                        Toast.makeText(fr.requireContext(), errString, Toast.LENGTH_LONG).show()
                        onAuthenticationError()
                    }
                }

                override fun onAuthenticationFailed() {
                    super.onAuthenticationFailed()
                    onCancel()
                }
            }
        )
    }
}

private fun nativeBiometricPrompt(
    authenticationMode: SettingsAuthenticationMethod,
    fr: Fragment,
    title: String,
    description: String,
    negativeButton: String,
    callback: BiometricPrompt.AuthenticationCallback
) {
    val executor = ContextCompat.getMainExecutor(fr.requireContext())

    val promptInfo = if (authenticationMode == SettingsAuthenticationMethod.DeviceCredentials) {
        BiometricPrompt.PromptInfo.Builder()
            .setTitle(title)
            .setDescription(description)
            .setAllowedAuthenticators(
                BiometricManager.Authenticators.DEVICE_CREDENTIAL
            )
            .build()
    } else {
        BiometricPrompt.PromptInfo.Builder()
            .setTitle(title)
            .setDescription(description)
            .setNegativeButtonText(negativeButton)
            .setAllowedAuthenticators(
                BiometricManager.Authenticators.BIOMETRIC_STRONG
            )
            .setAllowedAuthenticators(
                BiometricManager.Authenticators.BIOMETRIC_WEAK
            )
            .build()
    }

    val biometricPrompt = BiometricPrompt(fr, executor, callback)

    biometricPrompt.authenticate(promptInfo)
}
