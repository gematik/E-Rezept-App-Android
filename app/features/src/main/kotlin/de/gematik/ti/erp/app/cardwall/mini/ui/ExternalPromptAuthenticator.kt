/*
 * Copyright 2024, gematik GmbH
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

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import de.gematik.ti.erp.app.authentication.model.External
import de.gematik.ti.erp.app.authentication.model.PromptAuthenticator
import de.gematik.ti.erp.app.core.IntentHandler
import de.gematik.ti.erp.app.idp.model.error.GematikResponseError
import de.gematik.ti.erp.app.profiles.repository.ProfileIdentifier
import de.gematik.ti.erp.app.profiles.usecase.model.ProfilesUseCaseData
import io.github.aakira.napier.Napier
import kotlinx.coroutines.cancel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.receiveAsFlow

@Stable
class ExternalPromptAuthenticator(
    private val intentHandler: IntentHandler,
    private val bridge: AuthenticationBridge
) : PromptAuthenticator {
    private sealed interface Request {
        data object InsuranceSelected : Request
        data object Cancel : Request
    }

    private val requestChannel = Channel<Request>(Channel.RENDEZVOUS)

    @Stable
    internal sealed interface State {
        data object None : State
        data class SelectInsurance(val authenticatorName: String) : State
    }

    internal var state by mutableStateOf<State>(State.None)

    var profile by mutableStateOf<ProfilesUseCaseData.Profile?>(null)
        private set

    private var isInProgress: Boolean = false

    // TODO : Do better exception handling
    override fun authenticate(
        profileId: ProfileIdentifier,
        scope: PromptAuthenticator.AuthScope
    ): Flow<PromptAuthenticator.AuthResult> = channelFlow {
        when (val initialAuthenticationData = bridge.authenticateFor(profileId)) {
            is External -> {
                state = State.SelectInsurance(initialAuthenticationData.authenticatorName)
                profile = initialAuthenticationData.profile

                requestChannel.receiveAsFlow().collectLatest {
                    when (it) {
                        Request.Cancel -> {
                            send(PromptAuthenticator.AuthResult.Cancelled)
                            cancel()
                        }

                        is Request.InsuranceSelected -> {
                            Napier.d("GiD: doExternalAuthentication for $initialAuthenticationData")
                            bridge.doExternalAuthentication(
                                profileId = profileId,
                                scope = scope,
                                authenticatorId = initialAuthenticationData.authenticatorId,
                                authenticatorName = initialAuthenticationData.authenticatorName
                            ).fold(
                                onSuccess = { redirect ->
                                    intentHandler.tryStartingExternalHealthInsuranceAuthenticationApp(
                                        redirect = redirect,
                                        onSuccess = {
                                            Napier.d { "External health insurance app started" }
                                        },
                                        onFailure = {
                                            cancel()
                                            Napier.e { "External health insurance app not installed" }
                                        }
                                    )
                                },
                                onFailure = { throwable ->
                                    if (throwable is GematikResponseError) {
                                        Napier.e { "Wrong url sent from idp" }
                                        send(PromptAuthenticator.AuthResult.RedirectLinkNotRight)
                                    } else {
                                        Napier.e("doExternalAuthentication failed", throwable)
                                        send(PromptAuthenticator.AuthResult.Cancelled)
                                    }
                                    // TODO error handling
                                    cancel()
                                }
                            )
                            Napier.d("Fast-track/GiD: wait for instant of $initialAuthenticationData")
                        }
                    }
                }
            }

            else -> {
                send(PromptAuthenticator.AuthResult.Cancelled)
            }
        }
    }.onStart {
        isInProgress = true
    }.onCompletion {
        isInProgress = false
        state = State.None
        profile = null
    }

    internal suspend fun onInsuranceSelected() {
        requestChannel.send(Request.InsuranceSelected)
    }

    internal suspend fun onCancel() {
        requestChannel.send(Request.Cancel)
    }

    override suspend fun cancelAuthentication() {
        requestChannel.send(Request.Cancel)
    }
}

@Composable
fun rememberExternalPromptAuthenticator(
    bridge: AuthenticationBridge,
    intentHandler: IntentHandler
): ExternalPromptAuthenticator {
    return remember {
        ExternalPromptAuthenticator(intentHandler, bridge)
    }
}
