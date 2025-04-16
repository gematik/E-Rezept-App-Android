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

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import de.gematik.ti.erp.app.MainActivity
import de.gematik.ti.erp.app.Requirement
import de.gematik.ti.erp.app.authentication.model.HealthCard
import de.gematik.ti.erp.app.authentication.model.PromptAuthenticator
import de.gematik.ti.erp.app.base.onNfcNotEnabled
import de.gematik.ti.erp.app.cardwall.usecase.AuthenticationState
import de.gematik.ti.erp.app.profiles.repository.ProfileIdentifier
import de.gematik.ti.erp.app.profiles.usecase.model.ProfilesUseCaseData
import kotlinx.coroutines.cancel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.receiveAsFlow

@Stable
class HealthCardPromptAuthenticator(
    val activity: MainActivity,
    private val bridge: AuthenticationBridge
) : PromptAuthenticator {
    private sealed interface Request {
        class CredentialsEntered(val pin: String) : Request
        object Cancel : Request
    }

    private val requestChannel = Channel<Request>(Channel.RENDEZVOUS)

    internal sealed interface State {
        object None : State
        object EnterCredentials : State

        sealed interface ReadState : State {
            object Searching : ReadState

            sealed interface Reading : ReadState {
                object Reading00 : Reading
                object Reading25 : Reading
                object Reading50 : Reading
                object Reading75 : Reading
                object Success : Reading
            }

            sealed interface Error : ReadState {
                object NfcDisabled : Error
                object TagLost : Error
                object RemoteCommunicationFailed : Error
                object CardAccessNumberWrong : Error
                class PersonalIdentificationWrong(val retriesLeft: Int) : Error
                object HealthCardBlocked : Error
                object RemoteCommunicationInvalidCertificate : Error
                object RemoteCommunicationInvalidOCSP : Error
            }
        }
    }

    internal var state by mutableStateOf<State>(State.None)
        private set

    var profile by mutableStateOf<ProfilesUseCaseData.Profile?>(null)
        private set

    private val tagFlow = activity.nfcTagFlow
        .filter {
            // only let interrupted communications through
            !(state is State.ReadState.Error && state!=State.ReadState.Error.TagLost)
        }

    override fun authenticate(
        profileId: ProfileIdentifier,
        scope: PromptAuthenticator.AuthScope
    ): Flow<PromptAuthenticator.AuthResult> = channelFlow {
        val requestChannelFlow = requestChannel.receiveAsFlow()

        when (val authFor = bridge.authenticateFor(profileId)) {
            is HealthCard -> {
                state = State.EnterCredentials
                profile = authFor.profile

                requestChannelFlow.collectLatest { req ->
                    when (req) {
                        Request.Cancel -> {
                            send(PromptAuthenticator.AuthResult.Cancelled)
                            cancel()
                        }

                        is Request.CredentialsEntered -> {
                            state = State.ReadState.Searching

                            tagFlow.onNfcNotEnabled { state = State.ReadState.Error.NfcDisabled }
                                .collectLatest { tag ->
                                    bridge.doHealthCardAuthentication(
                                        profileId = profileId,
                                        scope = scope,
                                        can = authFor.can,
                                        pin = req.pin,
                                        tag = tag
                                    ).collect {
                                        it.emitAuthState()
                                        if (it.isFinal()) {
                                            send(PromptAuthenticator.AuthResult.Authenticated)
                                            cancel()
                                        }
                                    }
                                }
                        }
                    }
                }
            }

            else -> {
                send(PromptAuthenticator.AuthResult.Cancelled)
            }
        }
    }.onCompletion {
        state = State.None
        profile = null
    }

    @Requirement(
        "A_20605#2",
        sourceSpecification = "gemSpec_IDP_Frontend",
        rationale = "Propagates IDP auth states to the user."
    )
    @Requirement(
        "A_19937#3",
        sourceSpecification = "gemSpec_IDP_Frontend",
        rationale = "Propagates IDP auth states to the user."
    )
    @Suppress("CyclomaticComplexMethod")
    private fun AuthenticationState.emitAuthState() {
        when {
            isInProgress() -> {
                when (this) {
                    AuthenticationState.HealthCardCommunicationChannelReady ->
                        state = State.ReadState.Reading.Reading00

                    AuthenticationState.HealthCardCommunicationTrustedChannelEstablished ->
                        state = State.ReadState.Reading.Reading25

                    AuthenticationState.HealthCardCommunicationFinished ->
                        state = State.ReadState.Reading.Reading50

                    AuthenticationState.IDPCommunicationFinished ->
                        state = State.ReadState.Reading.Reading75

                    else -> {}
                }
            }

            @Requirement(
                "A_20079#2",
                sourceSpecification = "gemSpec_IDP_Frontend",
                rationale = "Propagates IDP auth states to the user."
            )
            @Requirement(
                "A_20605#1",
                sourceSpecification = "gemSpec_IDP_Frontend",
                rationale = "Decoding errors as RemoteCommunication errors."
            )
            isFailure() -> {
                state = when (this) {
                    AuthenticationState.HealthCardCommunicationInterrupted ->
                        State.ReadState.Error.TagLost

                    AuthenticationState.HealthCardCardAccessNumberWrong ->
                        State.ReadState.Error.CardAccessNumberWrong

                    AuthenticationState.HealthCardPin2RetriesLeft ->
                        State.ReadState.Error.PersonalIdentificationWrong(2)

                    AuthenticationState.HealthCardPin1RetryLeft ->
                        State.ReadState.Error.PersonalIdentificationWrong(1)

                    AuthenticationState.HealthCardBlocked ->
                        State.ReadState.Error.HealthCardBlocked

                    AuthenticationState.IDPCommunicationFailed ->
                        State.ReadState.Error.RemoteCommunicationFailed

                    AuthenticationState.IDPCommunicationInvalidCertificate ->
                        State.ReadState.Error.RemoteCommunicationInvalidCertificate

                    AuthenticationState.IDPCommunicationInvalidOCSPResponseOfHealthCardCertificate ->
                        State.ReadState.Error.RemoteCommunicationInvalidOCSP

                    else ->
                        State.ReadState.Error.TagLost
                }
            }
        }
    }

    override suspend fun cancelAuthentication() {
        requestChannel.trySend(Request.Cancel)
    }

    internal suspend fun onCancel() {
        requestChannel.send(Request.Cancel)
    }

    internal suspend fun onCredentialsEntered(pin: String) {
        requestChannel.send(Request.CredentialsEntered(pin))
    }
}

@Composable
fun rememberHealthCardPromptAuthenticator(
    bridge: AuthenticationBridge
): HealthCardPromptAuthenticator {
    val activity = LocalContext.current as MainActivity
    return remember {
        HealthCardPromptAuthenticator(activity, bridge)
    }
}
