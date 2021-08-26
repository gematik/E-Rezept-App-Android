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

package de.gematik.ti.erp.app.cardwall.usecase

import android.nfc.TagLostException
import android.os.Build
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import androidx.annotation.RequiresApi
import de.gematik.ti.erp.app.cardwall.model.nfc.card.NfcCardChannel
import de.gematik.ti.erp.app.cardwall.model.nfc.card.NfcCardSecureChannel
import de.gematik.ti.erp.app.cardwall.model.nfc.command.ResponseException
import de.gematik.ti.erp.app.cardwall.model.nfc.command.ResponseStatus
import de.gematik.ti.erp.app.cardwall.model.nfc.exchange.establishTrustedChannel
import de.gematik.ti.erp.app.cardwall.model.nfc.exchange.retrieveCertificate
import de.gematik.ti.erp.app.cardwall.model.nfc.exchange.signChallenge
import de.gematik.ti.erp.app.cardwall.model.nfc.exchange.verifyPin
import de.gematik.ti.erp.app.idp.secureRandomInstance
import de.gematik.ti.erp.app.idp.usecase.AltAuthenticationCryptoException
import de.gematik.ti.erp.app.idp.usecase.IdpUseCase
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.ProducerScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.retry
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.launch
import timber.log.Timber
import java.io.IOException
import java.security.KeyPairGenerator
import java.security.KeyStore
import java.security.spec.ECGenParameterSpec
import javax.inject.Inject

class AuthenticationUseCaseProduction @Inject constructor(
    private val idpUseCase: IdpUseCase
) : AuthenticationUseCase {

    override fun authenticateWithHealthCard(
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
                .onEach { Timber.d("AuthenticationState: $it") }
                .catch { cause ->
                    Timber.e(cause)
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

    @RequiresApi(Build.VERSION_CODES.P)
    override fun pairDeviceWithHealthCardAndSecureElement(
        can: String,
        pin: String,
        cardChannel: Flow<NfcCardChannel>
    ) = flow {
        var retry: Boolean
        do {
            retry = false

            val aliasOfSecureElementEntry = ByteArray(32).apply {
                secureRandomInstance().nextBytes(this)
            }

            alternatePairingFlowWithSecureElement(
                can,
                pin,
                aliasOfSecureElementEntry,
                cardChannel
            )
                .onEach { Timber.d("AuthenticationState: $it") }
                .catch { cause ->
                    handleException(cause).let {
                        emit(it)

                        if (it == AuthenticationState.HealthCardCommunicationInterrupted) {
                            delay(1000)
                            retry = true
                        } else {
                            try {
                                KeyStore.getInstance("AndroidKeyStore")
                                    .apply { load(null) }
                                    .deleteEntry(aliasOfSecureElementEntry.decodeToString())
                            } catch (e: Exception) {
                                Timber.e(e, "Couldn't remove key from keystore on failure; expected to happen.")
                            }
                        }
                    }
                }
                .collect {
                    emit(it)
                }
        } while (retry)
    }

    override fun authenticateWithSecureElement() =
        alternateAuthenticationFlowWithSecureElement()
            .onEach { Timber.d("AuthenticationState: $it") }
            .catch { cause ->
                emit(handleException(cause))
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

            val healthCardCertificate = MutableSharedFlow<ByteArray>()
            val secureChannel = MutableSharedFlow<NfcCardSecureChannel>()

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
                                healthCardCertificate.first().also {
                                    send(AuthenticationState.HealthCardCommunicationCertificateLoaded)
                                }
                            },
                            {
                                secureChannel.first().signChallenge(it).also {
                                    send(AuthenticationState.HealthCardCommunicationFinished)
                                }
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
                            healthCardCertificate,
                            secureChannel,
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

    @RequiresApi(Build.VERSION_CODES.P)
    @OptIn(ExperimentalCoroutinesApi::class)
    private fun alternatePairingFlowWithSecureElement(
        can: String,
        pin: String,
        aliasOfSecureElementEntry: ByteArray,
        cardChannel: Flow<NfcCardChannel>
    ) = channelFlow {
        send(AuthenticationState.AuthenticationFlowInitialized)

        cardChannel.first().use { nfcChannel ->
            send(AuthenticationState.HealthCardCommunicationChannelReady)

            val sePublicKey = try {
                val keyPairGenerator = KeyPairGenerator.getInstance(
                    KeyProperties.KEY_ALGORITHM_EC,
                    "AndroidKeyStore"
                )

                val parameterSpec = KeyGenParameterSpec.Builder(
                    aliasOfSecureElementEntry.decodeToString(),
                    KeyProperties.PURPOSE_SIGN
                ).apply {
                    setInvalidatedByBiometricEnrollment(true)
                    setUserAuthenticationRequired(true)
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                        setUserAuthenticationParameters(60, KeyProperties.AUTH_BIOMETRIC_STRONG)
                    } else {
                        setUserAuthenticationValidityDurationSeconds(60)
                    }
                    setIsStrongBoxBacked(true)
                    setDigests(KeyProperties.DIGEST_SHA256)

                    setAlgorithmParameterSpec(ECGenParameterSpec("secp256r1"))
                }.build()

                keyPairGenerator.initialize(parameterSpec)
                val keyPair = keyPairGenerator.generateKeyPair()

                keyPair.public
            } catch (e: Exception) {
                throw AuthenticationException(AuthenticationExceptionKind.SecureElementFailure)
            }

            val healthCardCertificate = MutableSharedFlow<ByteArray>()
            val secureChannel = MutableSharedFlow<NfcCardSecureChannel>(2)

            joinAll(
                launch {
                    try {
                        idpUseCase.alternatePairingFlowWithSecureElement(
                            publicKeyOfSecureElementEntry = sePublicKey,
                            aliasOfSecureElementEntry = aliasOfSecureElementEntry,
                            {
                                healthCardCertificate.first().also {
                                    send(AuthenticationState.HealthCardCommunicationCertificateLoaded)
                                }
                            },
                            {
                                secureChannel.first().signChallenge(it).also {
                                    send(AuthenticationState.HealthCardCommunicationFinished)
                                }
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
                            healthCardCertificate,
                            secureChannel,
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

    private fun handleAsyncExceptions(e: Throwable, kind: AuthenticationExceptionKind) {
        if (e.suppressed.isNotEmpty()) {
            throw e.suppressed.first()
        } else {
            Timber.e(e)
            when (e) {
                is CancellationException,
                is AuthenticationException,
                is ResponseException -> throw e
                else -> throw AuthenticationException(kind)
            }
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    private fun alternateAuthenticationFlowWithSecureElement() = channelFlow {
        send(AuthenticationState.AuthenticationFlowInitialized)

        try {
            idpUseCase.alternateAuthenticationFlowWithSecureElement()
            send(AuthenticationState.IDPCommunicationFinished)
        } catch (e: Exception) {
            when (e) {
                is AltAuthenticationCryptoException -> throw AuthenticationException(AuthenticationExceptionKind.SecureElementFailure)
                else -> throw AuthenticationException(AuthenticationExceptionKind.IDPCommunicationFailed)
            }
        }

        send(AuthenticationState.AuthenticationFlowFinished)
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    private suspend fun ProducerScope<AuthenticationState>.healthCardCommunication(
        channel: NfcCardChannel,
        certificate: MutableSharedFlow<ByteArray>,
        secureChannel: MutableSharedFlow<NfcCardSecureChannel>,
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

        certificate.emit(secChannel.retrieveCertificate())
        // TODO: find a better way
        delay(1000)

        when (secChannel.verifyPin(pin)) {
            ResponseStatus.SUCCESS -> secureChannel.emit(secChannel)
            ResponseStatus.WRONG_SECRET_WARNING_COUNT_02 ->
                throw AuthenticationException(AuthenticationExceptionKind.HealthCardPin2RetriesLeft)
            ResponseStatus.WRONG_SECRET_WARNING_COUNT_01 ->
                throw AuthenticationException(AuthenticationExceptionKind.HealthCardPin1RetryLeft)
            else -> {
                throw AuthenticationException(AuthenticationExceptionKind.HealthCardBlocked)
            }
        }
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
            is TagLostException, is IOException -> {
                Timber.e(e, "IO Exception / NFC TAG was lost")
                AuthenticationState.HealthCardCommunicationInterrupted
            }
            else -> {
                Timber.e(e, "Unknown exception")
                // soft fail
                AuthenticationState.HealthCardCommunicationInterrupted
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

private suspend fun <T> throwAsAuthException(
    kind: AuthenticationExceptionKind,
    call: suspend () -> T
): T {
    return try {
        call()
    } catch (e: Exception) {
        throw AuthenticationException(kind, e)
    }
}
