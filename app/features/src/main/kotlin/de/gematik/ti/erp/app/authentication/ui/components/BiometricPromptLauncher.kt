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

package de.gematik.ti.erp.app.authentication.ui.components

import androidx.biometric.BiometricPrompt
import de.gematik.ti.erp.app.authentication.model.AuthenticationResult
import de.gematik.ti.erp.app.authentication.model.AuthenticationResult.BiometricResult.BiometricStarted
import de.gematik.ti.erp.app.authentication.model.AuthenticationResult.BiometricResult.BiometricSuccess
import de.gematik.ti.erp.app.userauthentication.observer.BiometricPromptBuilder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.channels.trySendBlocking
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.launch

fun biometricPromptLauncher(
    promptInfo: BiometricPrompt.PromptInfo,
    biometricPromptBuilder: BiometricPromptBuilder
): Flow<AuthenticationResult.BiometricResult> =
    callbackFlow {
        launch {
            send(BiometricStarted)
            cancel()
        }
        val prompt = biometricPromptBuilder.buildBiometricPrompt(
            onSuccess = {
                trySendBlocking(BiometricSuccess)
            },
            onError = { error, code ->
                trySendBlocking(
                    AuthenticationResult.BiometricResult.BiometricError(
                        error = error,
                        errorCode = code
                    )
                )
            }
        )
        prompt.authenticate(promptInfo)
        awaitClose { prompt.cancelAuthentication() }
    }.flowOn(Dispatchers.Main)
