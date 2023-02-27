/*
 * Copyright (c) 2023 gematik GmbH
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

import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import de.gematik.ti.erp.app.core.LocalActivity
import de.gematik.ti.erp.app.utils.compose.createToastShort

// tag::BiometricPromptAndBestSecureOption[]

@Composable
fun BiometricPrompt(
    title: String,
    description: String,
    negativeButton: String,
    onAuthenticated: () -> Unit,
    onCancel: () -> Unit,
    onAuthenticationError: () -> Unit,
    onAuthenticationSoftError: () -> Unit
) {
    val activity = LocalActivity.current as FragmentActivity
    val context = LocalContext.current

    val executor = remember { ContextCompat.getMainExecutor(activity) }
    val biometricManager = remember { BiometricManager.from(context) }

    val callback = remember {
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
                    createToastShort(context, errString.toString())
                    onAuthenticationError()
                }
            }

            override fun onAuthenticationFailed() {
                super.onAuthenticationFailed()
                onAuthenticationSoftError()
            }
        }
    }

    val promptInfo = remember {
        val secureOption = bestSecureOption(biometricManager)

        BiometricPrompt.PromptInfo.Builder()
            .setTitle(title)
            .setDescription(description)
            .apply {
                if ((secureOption and BiometricManager.Authenticators.DEVICE_CREDENTIAL) == 0) {
                    setNegativeButtonText(negativeButton)
                }
            }.setAllowedAuthenticators(
                secureOption
            )
            .build()
    }

    val biometricPrompt = remember { BiometricPrompt(activity, executor, callback) }

    DisposableEffect(biometricPrompt) {
        biometricPrompt.authenticate(promptInfo)

        onDispose {
            biometricPrompt.cancelAuthentication()
        }
    }
}

private fun bestSecureOption(biometricManager: BiometricManager): Int {
    when (biometricManager.canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_STRONG)) {
        BiometricManager.BIOMETRIC_SUCCESS,
        BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED -> return BiometricManager.Authenticators.BIOMETRIC_STRONG
    }
    when (biometricManager.canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_WEAK)) {
        BiometricManager.BIOMETRIC_SUCCESS,
        BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED -> return BiometricManager.Authenticators.BIOMETRIC_WEAK
    }
    when (biometricManager.canAuthenticate(BiometricManager.Authenticators.DEVICE_CREDENTIAL)) {
        BiometricManager.BIOMETRIC_SUCCESS,
        BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED -> return BiometricManager.Authenticators.DEVICE_CREDENTIAL
    }
    return if (android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.R) {
        BiometricManager.Authenticators.DEVICE_CREDENTIAL or BiometricManager.Authenticators.BIOMETRIC_WEAK
    } else {
        BiometricManager.Authenticators.DEVICE_CREDENTIAL
    }
}

// end::BiometricPromptAndBestSecureOption[]
