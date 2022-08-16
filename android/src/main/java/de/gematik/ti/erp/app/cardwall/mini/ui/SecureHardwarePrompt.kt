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

package de.gematik.ti.erp.app.cardwall.mini.ui

import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import de.gematik.ti.erp.app.R
import de.gematik.ti.erp.app.profiles.repository.ProfileIdentifier
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
import io.github.aakira.napier.Napier

@Stable
class SecureHardwarePromptAuthenticator(
    val activity: FragmentActivity,
    private val bridge: AuthenticationBridge,
    private val promptInfo: BiometricPrompt.PromptInfo
) : PromptAuthenticator {
    private val executor = ContextCompat.getMainExecutor(activity)
    private val cancelRequest = Channel<Unit>(Channel.RENDEZVOUS)

    override fun authenticate(
        profileId: ProfileIdentifier,
        scope: PromptAuthenticator.AuthScope
    ): Flow<PromptAuthenticator.AuthResult> = callbackFlow {
        launch {
            cancelRequest.receive()
            send(PromptAuthenticator.AuthResult.Cancelled)
            cancel()
        }

        val prompt = BiometricPrompt(
            activity,
            executor,
            object : BiometricPrompt.AuthenticationCallback() {
                override fun onAuthenticationSucceeded(
                    result: BiometricPrompt.AuthenticationResult
                ) {
                    super.onAuthenticationSucceeded(result)

                    trySendBlocking(PromptAuthenticator.AuthResult.Authenticated)

                    channel.close()
                }

                override fun onAuthenticationError(
                    errCode: Int,
                    errString: CharSequence
                ) {
                    super.onAuthenticationError(errCode, errString)

                    Napier.e("Failed to authenticate: $errString")

                    trySendBlocking(PromptAuthenticator.AuthResult.Cancelled)

                    channel.close()
                }
            }
        )

        prompt.authenticate(promptInfo)

        awaitClose {
            prompt.cancelAuthentication()
        }
    }.flowOn(Dispatchers.Main)
        .map {
            if (it == PromptAuthenticator.AuthResult.Authenticated) {
                bridge.doSecureElementAuthentication(
                    profileId = profileId,
                    scope = scope
                ).first { authState ->
                    authState.isFailure() || authState.isFinal()
                }.let { authState ->
                    when {
                        authState.isNotAuthenticatedFailure() ->
                            PromptAuthenticator.AuthResult.UserNotAuthenticated
                        authState.isFailure() ->
                            PromptAuthenticator.AuthResult.Cancelled
                        authState.isFinal() ->
                            PromptAuthenticator.AuthResult.Authenticated
                        else ->
                            error("unreachable")
                    }
                }
            } else {
                it
            }
        }

    override suspend fun cancelAuthentication() {
        cancelRequest.trySend(Unit)
    }
}

@Composable
fun rememberSecureHardwarePromptAuthenticator(
    bridge: AuthenticationBridge
): SecureHardwarePromptAuthenticator {
    val activity = LocalContext.current as FragmentActivity
    val title = stringResource(R.string.alternate_auth_header)
    val description = stringResource(R.string.alternate_auth_info)
    val negativeButton = stringResource(R.string.cancel)
    val promptInfo = remember {
        BiometricPrompt.PromptInfo.Builder()
            .setTitle(title)
            .setDescription(description)
            .setNegativeButtonText(negativeButton)
            .setAllowedAuthenticators(
                BiometricManager.Authenticators.BIOMETRIC_STRONG
            )
            .build()
    }
    return remember {
        SecureHardwarePromptAuthenticator(activity, bridge, promptInfo)
    }
}
