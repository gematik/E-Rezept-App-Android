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

package de.gematik.ti.erp.app.userauthentication.observer

import androidx.appcompat.app.AppCompatActivity
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import io.github.aakira.napier.Napier

open class BiometricPromptBuilder(val activity: AppCompatActivity) {
    private val executor = ContextCompat.getMainExecutor(activity)
    private val biometricManager = BiometricManager.from(activity)
    private val authenticators = bestSecureOption(biometricManager)

    private fun authenticationCallback(
        onSuccess: () -> Unit,
        onFailure: (() -> Unit)? = null,
        onError: ((String, Int) -> Unit)? = null
    ): BiometricPrompt.AuthenticationCallback = object : BiometricPrompt.AuthenticationCallback() {
        override fun onAuthenticationFailed() {
            super.onAuthenticationFailed()
            Napier.i(
                tag = "BiometricPrompt",
                message = "authentication failed"
            )
            onFailure?.invoke()
        }

        override fun onAuthenticationError(errorCode: Int, systemMessage: CharSequence) {
            super.onAuthenticationError(errorCode, systemMessage)
            Napier.i(
                tag = "BiometricPrompt",
                message = "authentication error $systemMessage"
            )
            onError?.invoke(systemMessage.toString(), errorCode)
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

    fun buildPromptInfoWithBestSecureOption(
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
        .setConfirmationRequired(false)
        .build()

    fun buildPromptInfoWithAllAuthenticatorsAvailable(
        title: String,
        description: String = ""
    ): BiometricPrompt.PromptInfo = BiometricPrompt.PromptInfo.Builder()
        .setTitle(title)
        .setDescription(description)
        .setAllowedAuthenticators(
            BiometricManager.Authenticators.BIOMETRIC_STRONG
                or BiometricManager.Authenticators.BIOMETRIC_WEAK
                or BiometricManager.Authenticators.DEVICE_CREDENTIAL
        )
        .setConfirmationRequired(false)
        .build()

    fun buildBiometricPrompt(
        onSuccess: () -> Unit,
        onFailure: (() -> Unit)? = null,
        onError: ((String, Int) -> Unit)? = null
    ): BiometricPrompt = BiometricPrompt(activity, executor, authenticationCallback(onSuccess, onFailure, onError))

    @Suppress("ReturnCount")
    private fun bestSecureOption(biometricManager: BiometricManager): Int {
        if (biometricManager.canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_STRONG) ==
            BiometricManager.BIOMETRIC_SUCCESS
        ) {
            return BiometricManager.Authenticators.BIOMETRIC_STRONG
        }

        if (biometricManager.canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_WEAK) ==
            BiometricManager.BIOMETRIC_SUCCESS
        ) {
            return BiometricManager.Authenticators.BIOMETRIC_WEAK
        }
        if (biometricManager.canAuthenticate(BiometricManager.Authenticators.DEVICE_CREDENTIAL) ==
            BiometricManager.BIOMETRIC_SUCCESS
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
