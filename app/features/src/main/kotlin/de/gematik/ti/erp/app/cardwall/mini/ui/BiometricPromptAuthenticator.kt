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

package de.gematik.ti.erp.app.cardwall.mini.ui

import androidx.appcompat.app.AppCompatActivity
import androidx.biometric.BiometricPrompt
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import de.gematik.ti.erp.app.authentication.model.PromptAuthenticator
import de.gematik.ti.erp.app.cardwall.usecase.AuthenticationState
import de.gematik.ti.erp.app.features.R
import de.gematik.ti.erp.app.profiles.repository.ProfileIdentifier
import de.gematik.ti.erp.app.userauthentication.observer.BiometricPromptBuilder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.channels.trySendBlocking
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

@Stable
class BiometricPromptAuthenticator(
    private val bridge: AuthenticationBridge,
    private val promptInfo: BiometricPrompt.PromptInfo,
    private val biometricPromptBuilder: BiometricPromptBuilder
) : PromptAuthenticator {
    private val cancelRequest = Channel<Unit>(Channel.RENDEZVOUS)

    @Stable
    sealed interface Error {
        data object RemoteCommunicationFailed : Error
        class RemoteCommunicationAltAuthNotSuccessful(val profileId: ProfileIdentifier) : Error
        data object RemoteCommunicationInvalidCertificate : Error
        data object RemoteCommunicationInvalidOCSP : Error
    }

    var showError: Error? by mutableStateOf(null)
        private set

    fun resetErrorState() {
        showError = null
    }

    suspend fun removeAuthentication(profileId: ProfileIdentifier) {
        bridge.doRemoveAuthentication(profileId)
    }

    override fun authenticate(
        profileId: ProfileIdentifier,
        scope: PromptAuthenticator.AuthScope
    ): Flow<PromptAuthenticator.AuthResult> = callbackFlow {
        launch {
            cancelRequest.receive()
            send(PromptAuthenticator.AuthResult.Cancelled)
            cancel()
        }
        val prompt = biometricPromptBuilder.buildBiometricPrompt(
            onSuccess = {
                trySendBlocking(PromptAuthenticator.AuthResult.Authenticated)
                channel.close()
            },
            onError = { _: String, _: Int ->
                trySendBlocking(PromptAuthenticator.AuthResult.Cancelled)
                channel.close()
            }
        )

        prompt.authenticate(promptInfo)

        awaitClose {
            prompt.cancelAuthentication()
        }
    }.flowOn(Dispatchers.Main)
        .map { authResult: PromptAuthenticator.AuthResult ->
            if (authResult == PromptAuthenticator.AuthResult.Authenticated) {
                bridge.doSecureElementAuthentication(
                    profileId = profileId,
                    scope = scope
                ).first { authState ->
                    authState.isFailure() || authState.isFinal()
                }.let { authState ->
                    when {
                        authState.isNotAuthenticatedFailure() ->
                            PromptAuthenticator.AuthResult.UserNotAuthenticated

                        authState.isFailure() -> {
                            showError = when (authState) {
                                AuthenticationState.IDPCommunicationAltAuthNotSuccessful -> Error.RemoteCommunicationAltAuthNotSuccessful(profileId)
                                AuthenticationState.IDPCommunicationFailed -> Error.RemoteCommunicationFailed
                                AuthenticationState.IDPCommunicationInvalidCertificate -> Error.RemoteCommunicationInvalidCertificate
                                AuthenticationState.IDPCommunicationInvalidOCSPResponseOfHealthCardCertificate -> Error.RemoteCommunicationInvalidOCSP
                                else -> null
                            }
                            PromptAuthenticator.AuthResult.Cancelled
                        }

                        authState.isFinal() ->
                            PromptAuthenticator.AuthResult.Authenticated

                        else ->
                            error("unreachable")
                    }
                }
            } else {
                authResult
            }
        }

    override suspend fun cancelAuthentication() {
        cancelRequest.trySend(Unit)
    }
}

@Composable
fun rememberBiometricPromptAuthenticator(
    bridge: AuthenticationBridge
): BiometricPromptAuthenticator {
    val activity = LocalContext.current as AppCompatActivity
    val biometricPromptBuilder = remember { BiometricPromptBuilder(activity) }
    val biometricPromptInfo = biometricPromptBuilder.buildPromptInfoWithBestSecureOption(
        title = stringResource(R.string.auth_prompt_headline),
        description = stringResource(R.string.alternate_auth_info),
        negativeButton = stringResource(R.string.auth_prompt_cancel)
    )
    return remember {
        BiometricPromptAuthenticator(bridge, biometricPromptInfo, biometricPromptBuilder)
    }
}
