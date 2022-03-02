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

package de.gematik.ti.erp.app.cardwall.usecase

import android.nfc.TagLostException
import android.os.Build
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import androidx.annotation.RequiresApi
import de.gematik.ti.erp.app.api.ApiCallException
import de.gematik.ti.erp.app.cardwall.model.nfc.card.NfcCardChannel
import de.gematik.ti.erp.app.cardwall.model.nfc.card.NfcCardSecureChannel
import de.gematik.ti.erp.app.cardwall.model.nfc.command.ResponseException
import de.gematik.ti.erp.app.cardwall.model.nfc.command.ResponseStatus
import de.gematik.ti.erp.app.cardwall.model.nfc.exchange.establishTrustedChannel
import de.gematik.ti.erp.app.cardwall.model.nfc.exchange.retrieveCertificate
import de.gematik.ti.erp.app.cardwall.model.nfc.exchange.signChallenge
import de.gematik.ti.erp.app.cardwall.model.nfc.exchange.verifyPin
import de.gematik.ti.erp.app.idp.usecase.AltAuthenticationCryptoException
import de.gematik.ti.erp.app.idp.usecase.IdpUseCase
import de.gematik.ti.erp.app.profiles.repository.KVNRAlreadyAssignedException
import de.gematik.ti.erp.app.profiles.usecase.ProfilesUseCase
import de.gematik.ti.erp.app.secureRandomInstance
import java.io.IOException
import java.security.KeyPairGenerator
import java.security.KeyStore
import java.security.spec.ECGenParameterSpec
import javax.inject.Inject
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.ProducerScope
import kotlinx.coroutines.channels.consume
import kotlinx.coroutines.channels.consumeEach
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.launch
import org.json.JSONException
import org.json.JSONObject
import timber.log.Timber

/**
 * Error codes returned by the IDP as an error JSON: `{ "gematik_code" : "..." }`.
 */
enum class IDPErrorCodes(val code: String) {
    InvalidHealthCardCertificate("2020"),
    InvalidOCSPResponseOfHealthCardCertificate("2021"),
    Unknown("-");

    companion object {
        fun valueOfCode(code: String) = values().find { it.code == code } ?: Unknown
    }
}

class AuthenticationUseCaseProduction @Inject constructor(
    private val idpUseCase: IdpUseCase,
    private val profilesUseCase: ProfilesUseCase
) : AuthenticationUseCase {

    override fun authenticateWithHealthCard(
        can: String,
        pin: String,
        cardChannel: Flow<NfcCardChannel>
    ) =
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
                }
            }

    @RequiresApi(Build.VERSION_CODES.P)
    override fun pairDeviceWithHealthCardAndSecureElement(
        can: String,
        pin: String,
        cardChannel: Flow<NfcCardChannel>
    ): Flow<AuthenticationState> {
        val aliasOfSecureElementEntry = ByteArray(32).apply {
            secureRandomInstance().nextBytes(this)
        }

        return alternatePairingFlowWithSecureElement(
            can,
            pin,
            aliasOfSecureElementEntry,
            cardChannel
        )
            .onEach { Timber.d("AuthenticationState: $it") }
            .catch { cause ->
                handleException(cause).let {
                    emit(it)

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

    override fun authenticateWithSecureElement() =
        profilesUseCase.activeProfileName().flatMapLatest { activeProfileName ->
            alternateAuthenticationFlowWithSecureElement(activeProfileName)
                .onEach { Timber.d("AuthenticationState: $it") }
                .catch { cause ->
                    emit(handleException(cause))
                }
        }

    override suspend fun isCanAvailable(): Boolean = idpUseCase.isCanAvailable()

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

    @RequiresApi(Build.VERSION_CODES.P)
    @OptIn(ExperimentalCoroutinesApi::class)
    @Suppress("Deprecation")
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

            val healthCardCertificateChannel = Channel<ByteArray>()
            val signChannel = Channel<ByteArray>()
            val responseChannel = Channel<ByteArray>()

            //
            //                  + - IDP communication --------- + ------------- + -- + --------- + -- + ------- +
            //                 /                                ^               |    ^           |    ^          \
            // - start flow - +                          Health card cert   Sign challenge   Sign challenge       + - end flow -
            //                 \                                |               v    |           v    |          /
            //                  + - Health card communication - + ------------- + -- + --------- + -- + ------- +
            //

            var signingsLeft = 2
            joinAll(
                launch {
                    try {
                        idpUseCase.alternatePairingFlowWithSecureElement(
                            publicKeyOfSecureElementEntry = sePublicKey,
                            aliasOfSecureElementEntry = aliasOfSecureElementEntry,
                            {
                                healthCardCertificateChannel.consume { receive() }
                            },
                            {
                                signChannel.send(it)
                                responseChannel.receive().also {
                                    signingsLeft--
                                    if (signingsLeft == 0) {
                                        signChannel.close()
                                    }
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

    private fun handleAsyncExceptions(e: Throwable, kind: AuthenticationExceptionKind) {
        if (e.suppressed.isNotEmpty()) {
            throw e.suppressed.first()
        } else {
            Timber.e(e)
            when (e) {
                is CancellationException,
                is AuthenticationException,
                is ResponseException -> throw e
                else -> {
                    if (e is ApiCallException) {
                        val code = e.response.errorBody()
                            ?.let {
                                try {
                                    JSONObject(it.string())["gematik_code"] as? String
                                } catch (_: JSONException) {
                                    null
                                }
                            }
                            ?.let { IDPErrorCodes.valueOfCode(it) }

                        when (code) {
                            IDPErrorCodes.InvalidHealthCardCertificate ->
                                throw AuthenticationException(AuthenticationExceptionKind.IDPCommunicationInvalidCertificate)
                            IDPErrorCodes.InvalidOCSPResponseOfHealthCardCertificate ->
                                throw AuthenticationException(AuthenticationExceptionKind.IDPCommunicationInvalidOCSPResponseOfHealthCardCertificate)
                            else ->
                                throw AuthenticationException(kind)
                        }
                    } else if (e is KVNRAlreadyAssignedException) {
                        throw AuthenticationException(AuthenticationExceptionKind.InsuranceIdentifierAlreadyAssigned, e)
                    } else {
                        throw AuthenticationException(kind)
                    }
                }
            }
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    private fun alternateAuthenticationFlowWithSecureElement(profileName: String) = channelFlow {
        send(AuthenticationState.AuthenticationFlowInitialized)

        try {
            idpUseCase.alternateAuthenticationFlowWithSecureElement(profileName)
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
                    AuthenticationExceptionKind.IDPCommunicationInvalidCertificate ->
                        AuthenticationState.IDPCommunicationInvalidCertificate

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
                    AuthenticationExceptionKind.IDPCommunicationInvalidOCSPResponseOfHealthCardCertificate ->
                        AuthenticationState.IDPCommunicationInvalidOCSPResponseOfHealthCardCertificate

                    AuthenticationExceptionKind.InsuranceIdentifierAlreadyAssigned -> {
                        val alreadyAssignedException = (e.cause!! as KVNRAlreadyAssignedException)
                        AuthenticationState.InsuranceIdentifierAlreadyExists(
                            inActiveProfile = alreadyAssignedException.isActiveProfile,
                            profileName = alreadyAssignedException.inProfile,
                            insuranceIdentifier = alreadyAssignedException.insuranceIdentifier
                        )
                    }
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
    IDPCommunicationInvalidCertificate,
    IDPCommunicationInvalidOCSPResponseOfHealthCardCertificate,

    HealthCardBlocked,
    HealthCardPin1RetryLeft,
    HealthCardPin2RetriesLeft,
    HealthCardCommunicationFailed,

    InsuranceIdentifierAlreadyAssigned,

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
