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

package de.gematik.ti.erp.app.userauthentication.observer

import androidx.appcompat.app.AppCompatActivity
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import de.gematik.ti.erp.app.Requirement
import io.github.aakira.napier.Napier

@Requirement(
    "A_21584",
    sourceSpecification = "gemSpec_IDP_Frontend",
    rationale = "Only biometric means provided by the operating system are used."
)
open class BiometricPromptBuilder(val activity: AppCompatActivity) {
    private val executor = ContextCompat.getMainExecutor(activity)
    private val biometricManager = BiometricManager.from(activity)
    private val authenticators = fetchAuthenticators(biometricManager)

    private fun authenticationCallback(
        onSuccess: () -> Unit,
        onFailure: () -> Unit,
        onError: () -> Unit
    ): BiometricPrompt.AuthenticationCallback = object : BiometricPrompt.AuthenticationCallback() {
        override fun onAuthenticationFailed() {
            super.onAuthenticationFailed()
            Napier.i(
                tag = "BiometricPrompt",
                message = "authentication failed"
            )
            onFailure()
        }

        override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
            super.onAuthenticationError(errorCode, errString)
            Napier.i(
                tag = "BiometricPrompt",
                message = "authentication error $errString"
            )
            onError()
        }

        override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
            super.onAuthenticationSucceeded(result)
            Napier.i(
                tag = "BiometricPrompt",
                message = "authentication success $result"
            )
            onSuccess()
        }
    }

    fun buildPromptInfo(
        title: String,
        description: String = "",
        negativeButton: String
    ): BiometricPrompt.PromptInfo = BiometricPrompt.PromptInfo.Builder()
        .setTitle(title)
        .setDescription(description)
        .apply {
            if ((authenticators and BiometricManager.Authenticators.DEVICE_CREDENTIAL) == 0) {
                setNegativeButtonText(negativeButton)
            }
        }.setAllowedAuthenticators(authenticators)
        .build()

    fun buildBiometricPrompt(
        onSuccess: () -> Unit,
        onFailure: () -> Unit,
        onError: () -> Unit
    ): BiometricPrompt = BiometricPrompt(activity, executor, authenticationCallback(onSuccess, onFailure, onError))

    @Requirement(
        "A_21582",
        sourceSpecification = "gemSpec_IDP_Frontend",
        rationale = "Selection of the best available authentication option on the device."
    )
    @Suppress("ReturnCount")
    private fun fetchAuthenticators(biometricManager: BiometricManager): Int {
        if (biometricManager.canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_STRONG) ==
            BiometricManager.BIOMETRIC_SUCCESS ||
            biometricManager.canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_STRONG) ==
            BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED
        ) {
            return BiometricManager.Authenticators.BIOMETRIC_STRONG
        }

        if (biometricManager.canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_WEAK) ==
            BiometricManager.BIOMETRIC_SUCCESS ||
            biometricManager.canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_WEAK) ==
            BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED
        ) {
            return BiometricManager.Authenticators.BIOMETRIC_WEAK
        }
        if (biometricManager.canAuthenticate(BiometricManager.Authenticators.DEVICE_CREDENTIAL) ==
            BiometricManager.BIOMETRIC_SUCCESS ||
            biometricManager.canAuthenticate(BiometricManager.Authenticators.DEVICE_CREDENTIAL) ==
            BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED
        ) {
            return BiometricManager.Authenticators.DEVICE_CREDENTIAL
        }

        return if (android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.R) {
            BiometricManager.Authenticators.DEVICE_CREDENTIAL or BiometricManager.Authenticators.BIOMETRIC_WEAK
        } else {
            BiometricManager.Authenticators.DEVICE_CREDENTIAL
        }
    }
}
