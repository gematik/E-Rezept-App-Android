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

package de.gematik.ti.erp.app.cardwall

import de.gematik.ti.erp.app.card.model.command.ResponseException
import de.gematik.ti.erp.app.card.model.command.ResponseStatus
import de.gematik.ti.erp.app.card.model.exchange.retrieveCertificate
import de.gematik.ti.erp.app.card.model.exchange.signChallenge
import de.gematik.ti.erp.app.card.model.exchange.verifyPin
import de.gematik.ti.erp.app.cardwall.model.nfc.exchange.establishTrustedChannel
import de.gematik.ti.erp.app.idp.usecase.IdpUseCase
import de.gematik.ti.erp.app.nfc.model.card.NfcCardChannel
import de.gematik.ti.erp.app.nfc.model.card.NfcCardSecureChannel
import io.github.aakira.napier.Napier
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.ProducerScope
import kotlinx.coroutines.channels.consume
import kotlinx.coroutines.channels.consumeEach
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.launch
import java.io.IOException

enum class AuthenticationState {
    None,

    // initial state
    AuthenticationFlowInitialized,

    HealthCardCommunicationChannelReady,
    HealthCardCommunicationTrustedChannelEstablished,
    HealthCardCommunicationCertificateLoaded,
    HealthCardCommunicationFinished,

    IDPCommunicationFinished,

    // final state
    AuthenticationFlowFinished,

    // nfc failure
    HealthCardCommunicationInterrupted,

    // health card failure states
    HealthCardCardAccessNumberWrong,
    HealthCardPin2RetriesLeft,
    HealthCardPin1RetryLeft,
    HealthCardBlocked,

    // IDP failure states
    IDPCommunicationFailed,

    // secure element failure
    SecureElementCryptographyFailed;

    fun isFailure() =
        when (this) {
            HealthCardCommunicationInterrupted,
            HealthCardCardAccessNumberWrong,
            HealthCardPin2RetriesLeft,
            HealthCardPin1RetryLeft,
            HealthCardBlocked,
            IDPCommunicationFailed,
            SecureElementCryptographyFailed -> true
            else -> false
        }

    fun isInProgress() =
        when (this) {
            AuthenticationFlowInitialized,
            HealthCardCommunicationChannelReady,
            HealthCardCommunicationTrustedChannelEstablished,
            HealthCardCommunicationCertificateLoaded,
            HealthCardCommunicationFinished,
            IDPCommunicationFinished
            -> true
            else -> false
        }

    fun isNotInProgress() = !isInProgress()

    fun isFinal() = this == AuthenticationFlowFinished
    fun isReady() = this == None
}

class AuthenticationUseCase(
    private val idpUseCase: IdpUseCase
) {
    fun authenticateWithHealthCard(
        can: String,
        pin: String,
        cardChannel: Flow<NfcCardChannel>
    ) = flow {
        var retry: Boolean
        do {
            retry = false
            authenticationFlowWithHealthCard(
                can,
                pin,
                cardChannel
            )
                .onEach { Napier.d("AuthenticationState: $it") }
                .catch { cause ->
                    Napier.e("authenticationFlowWithHealthCard failed", cause)
                    handleException(cause).let {
                        emit(it)

                        delay(1000)
                        retry = it == AuthenticationState.HealthCardCommunicationInterrupted
                    }
                }
                .collect {
                    emit(it)
                }
        } while (retry)
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    private fun authenticationFlowWithHealthCard(
        can: String,
        pin: String,
        cardChannel: Flow<NfcCardChannel>
    ) = channelFlow {
        send(AuthenticationState.AuthenticationFlowInitialized)

        cardChannel.first().use { nfcChannel ->
            send(AuthenticationState.HealthCardCommunicationChannelReady)

            val healthCardCertificateChannel = Channel<ByteArray>()
            val signChannel = Channel<ByteArray>()
            val responseChannel = Channel<ByteArray>()

            //
            //                  + - IDP communication --------- + ------------- + -- + -------- +
            //                 /                                ^               |    ^           \
            // - start flow - +                          Health card cert   Sign challenge        + - end flow -
            //                 \                                |               v    |           /
            //                  + - Health card communication - + ------------- + -- + -------- +
            //

            joinAll(
                launch {
                    try {
                        idpUseCase.authenticationFlowWithHealthCard(
                            {
                                healthCardCertificateChannel.consume { receive() }
                            },
                            {
                                signChannel.send(it)
                                signChannel.close()
                                responseChannel.consume { receive() }
                            }
                        )
                        send(AuthenticationState.IDPCommunicationFinished)
                    } catch (e: Exception) {
                        handleAsyncExceptions(e, AuthenticationExceptionKind.IDPCommunicationFailed)
                    }
                },
                launch {
                    try {
                        healthCardCommunication(
                            nfcChannel,
                            healthCardCertificateChannel,
                            signChannel,
                            responseChannel,
                            can = can,
                            pin = pin
                        )
                    } catch (e: Exception) {
                        handleAsyncExceptions(e, AuthenticationExceptionKind.HealthCardCommunicationFailed)
                    }
                }
            )
        }

        send(AuthenticationState.AuthenticationFlowFinished)
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    private suspend fun ProducerScope<AuthenticationState>.healthCardCommunication(
        channel: NfcCardChannel,
        healthCardCertificateChannel: Channel<ByteArray>,
        signChannel: Channel<ByteArray>, // `signChannel` is required to be closed by its caller
        responseChannel: Channel<ByteArray>,
        can: String,
        pin: String
    ) {
        val paceKey = channel.establishTrustedChannel(can)

        val secChannel = NfcCardSecureChannel(
            channel.isExtendedLengthSupported,
            channel.card,
            paceKey
        )
        send(AuthenticationState.HealthCardCommunicationTrustedChannelEstablished)

        healthCardCertificateChannel.send(secChannel.retrieveCertificate())
        send(AuthenticationState.HealthCardCommunicationCertificateLoaded)

        when (secChannel.verifyPin(pin)) {
            ResponseStatus.SUCCESS -> {
                signChannel.consumeEach {
                    responseChannel.send(
                        secChannel.signChallenge(it)
                    )
                }
            }
            ResponseStatus.WRONG_SECRET_WARNING_COUNT_02 ->
                throw AuthenticationException(AuthenticationExceptionKind.HealthCardPin2RetriesLeft)
            ResponseStatus.WRONG_SECRET_WARNING_COUNT_01 ->
                throw AuthenticationException(AuthenticationExceptionKind.HealthCardPin1RetryLeft)
            else -> {
                throw AuthenticationException(AuthenticationExceptionKind.HealthCardBlocked)
            }
        }

        send(AuthenticationState.HealthCardCommunicationFinished)
    }

    private fun handleException(e: Throwable): AuthenticationState =
        when (e) {
            is CancellationException -> throw e
            is AuthenticationException -> {
                when (e.kind) {
                    AuthenticationExceptionKind.IDPCommunicationFailed ->
                        AuthenticationState.IDPCommunicationFailed

                    AuthenticationExceptionKind.HealthCardBlocked ->
                        AuthenticationState.HealthCardBlocked
                    AuthenticationExceptionKind.HealthCardPin1RetryLeft ->
                        AuthenticationState.HealthCardPin1RetryLeft
                    AuthenticationExceptionKind.HealthCardPin2RetriesLeft ->
                        AuthenticationState.HealthCardPin2RetriesLeft
                    AuthenticationExceptionKind.HealthCardCommunicationFailed ->
                        AuthenticationState.HealthCardCommunicationInterrupted
                    AuthenticationExceptionKind.SecureElementFailure ->
                        AuthenticationState.SecureElementCryptographyFailed
                }
            }
            is ResponseException -> {
                when (e.responseStatus) {
                    ResponseStatus.AUTHENTICATION_FAILURE -> AuthenticationState.HealthCardCardAccessNumberWrong
                    else -> AuthenticationState.HealthCardCommunicationInterrupted
                }
            }
            is IOException -> {
                Napier.e("IO Exception / NFC TAG was lost", e)
                AuthenticationState.HealthCardCommunicationInterrupted
            }
            else -> {
                Napier.e("Unknown exception", e)
                // soft fail
                AuthenticationState.HealthCardCommunicationInterrupted
            }
        }

    private fun handleAsyncExceptions(e: Throwable, kind: AuthenticationExceptionKind) {
        if (e.suppressed.isNotEmpty()) {
            throw e.suppressed.first()
        } else {
            Napier.e("async exception", e)
            when (e) {
                is CancellationException,
                is AuthenticationException,
                is ResponseException -> throw e
                else -> throw AuthenticationException(kind)
            }
        }
    }

    private enum class AuthenticationExceptionKind {
        IDPCommunicationFailed,

        HealthCardBlocked,
        HealthCardPin1RetryLeft,
        HealthCardPin2RetriesLeft,
        HealthCardCommunicationFailed,

        SecureElementFailure,
    }

    private class AuthenticationException : IllegalStateException {
        var kind: AuthenticationExceptionKind
            private set

        constructor(kind: AuthenticationExceptionKind, cause: Throwable) : super(kind.name, cause) {
            this.kind = kind
        }

        constructor(kind: AuthenticationExceptionKind) : super(kind.name) {
            this.kind = kind
        }
    }
}
