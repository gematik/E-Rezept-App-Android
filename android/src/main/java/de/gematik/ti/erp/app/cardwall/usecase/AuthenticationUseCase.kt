/*
 * Copyright (c) 2023 gematik GmbH
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
import android.security.keystore.KeyPermanentlyInvalidatedException
import android.security.keystore.UserNotAuthenticatedException
import androidx.annotation.RequiresApi
import androidx.compose.runtime.Stable
import de.gematik.ti.erp.app.api.ApiCallException
import de.gematik.ti.erp.app.card.model.command.ResponseException
import de.gematik.ti.erp.app.cardwall.model.nfc.card.NfcCardChannel
import de.gematik.ti.erp.app.cardwall.model.nfc.card.NfcCardSecureChannel
import de.gematik.ti.erp.app.card.model.command.ResponseStatus
import de.gematik.ti.erp.app.cardwall.model.nfc.exchange.establishTrustedChannel
import de.gematik.ti.erp.app.card.model.exchange.retrieveCertificate
import de.gematik.ti.erp.app.card.model.exchange.signChallenge
import de.gematik.ti.erp.app.card.model.exchange.verifyPin
import de.gematik.ti.erp.app.idp.api.models.IdpScope
import de.gematik.ti.erp.app.idp.usecase.AltAuthenticationCryptoException
import de.gematik.ti.erp.app.idp.usecase.IdpUseCase
import de.gematik.ti.erp.app.profiles.repository.KVNRAlreadyAssignedException
import de.gematik.ti.erp.app.profiles.repository.ProfileIdentifier
import java.io.IOException
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.ProducerScope
import kotlinx.coroutines.channels.consume
import kotlinx.coroutines.channels.consumeEach
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.launch
import org.json.JSONException
import org.json.JSONObject
import io.github.aakira.napier.Napier
import java.security.PublicKey

@Stable
sealed class AuthenticationState {
    object None : AuthenticationState()

    // initial state
    object AuthenticationFlowInitialized : AuthenticationState()

    object HealthCardCommunicationChannelReady : AuthenticationState()
    object HealthCardCommunicationTrustedChannelEstablished : AuthenticationState()
    object HealthCardCommunicationCertificateLoaded : AuthenticationState()
    object HealthCardCommunicationFinished : AuthenticationState()

    object IDPCommunicationFinished : AuthenticationState()

    // final state
    object AuthenticationFlowFinished : AuthenticationState()

    // nfc failure
    object HealthCardCommunicationInterrupted : AuthenticationState()

    // health card failure states
    object HealthCardCardAccessNumberWrong : AuthenticationState()
    object HealthCardPin2RetriesLeft : AuthenticationState()
    object HealthCardPin1RetryLeft : AuthenticationState()
    object HealthCardBlocked : AuthenticationState()

    // IDP failure states
    object IDPCommunicationFailed : AuthenticationState()

    object UserNotAuthenticated : AuthenticationState()

    object IDPCommunicationAltAuthNotSuccessful : AuthenticationState()
    object IDPCommunicationInvalidCertificate : AuthenticationState()
    object IDPCommunicationInvalidOCSPResponseOfHealthCardCertificate : AuthenticationState()

    // profile failure
    class InsuranceIdentifierAlreadyExists(
        val inActiveProfile: Boolean,
        val profileName: String,
        val insuranceIdentifier: String
    ) : AuthenticationState()

    // secure element failure
    object SecureElementCryptographyFailed : AuthenticationState()

    @Stable
    fun isFailure() =
        when (this) {
            HealthCardCommunicationInterrupted,
            HealthCardCardAccessNumberWrong,
            HealthCardPin2RetriesLeft,
            HealthCardPin1RetryLeft,
            HealthCardBlocked,
            IDPCommunicationFailed,
            IDPCommunicationAltAuthNotSuccessful,
            IDPCommunicationInvalidCertificate,
            IDPCommunicationInvalidOCSPResponseOfHealthCardCertificate,
            UserNotAuthenticated,
            is InsuranceIdentifierAlreadyExists,
            SecureElementCryptographyFailed -> true
            else -> false
        }

    @Stable
    fun isNotAuthenticatedFailure() = this == UserNotAuthenticated

    @Stable
    fun isInProgress() =
        when (this) {
            AuthenticationFlowInitialized,
            HealthCardCommunicationChannelReady,
            HealthCardCommunicationTrustedChannelEstablished,
            HealthCardCommunicationCertificateLoaded,
            HealthCardCommunicationFinished,
            IDPCommunicationFinished -> true
            else -> false
        }

    @Stable
    fun isFinal() = this == AuthenticationFlowFinished

    @Stable
    fun isReady() = this == None
}

/**
 * Error codes returned by the IDP as an error JSON: `{ "gematik_code" : "..." }`.
 */
enum class IDPErrorCodes(val code: String) {
    AltAuthNotSuccessful("2000"),
    InvalidHealthCardCertificate("2020"),
    InvalidOCSPResponseOfHealthCardCertificate("2021"),
    Unknown("-");

    companion object {
        fun valueOfCode(code: String) = values().find { it.code == code } ?: Unknown
    }
}

class AuthenticationUseCase(
    private val idpUseCase: IdpUseCase
) {

    fun authenticateWithHealthCard(
        profileId: ProfileIdentifier,
        scope: IdpScope = IdpScope.Default,
        can: String,
        pin: String,
        cardChannel: Flow<NfcCardChannel>
    ) =
        authenticationFlowWithHealthCard(
            profileId,
            scope,
            can,
            pin,
            cardChannel
        )
            .onEach { Napier.d("AuthenticationState: $it") }
            .catch { cause ->
                Napier.e("Authentication error", cause)
                handleException(cause).let {
                    emit(it)
                }
            }

    @RequiresApi(Build.VERSION_CODES.P)
    fun pairDeviceWithHealthCardAndSecureElement(
        profileId: ProfileIdentifier,
        can: String,
        pin: String,
        publicKeyOfSecureElementEntry: PublicKey,
        aliasOfSecureElementEntry: ByteArray,
        cardChannel: Flow<NfcCardChannel>
    ): Flow<AuthenticationState> {
        return alternatePairingFlowWithSecureElement(
            profileId = profileId,
            can = can,
            pin = pin,
            sePublicKey = publicKeyOfSecureElementEntry,
            aliasOfSecureElementEntry = aliasOfSecureElementEntry,
            cardChannel = cardChannel
        )
            .onEach { Napier.d("AuthenticationState: $it") }
            .catch { cause ->
                emit(handleException(cause))
            }
    }

    fun authenticateWithSecureElement(profileId: ProfileIdentifier, scope: IdpScope) =
        alternateAuthenticationFlowWithSecureElement(profileId, scope)
            .onEach { Napier.d("AuthenticationState: $it") }
            .catch { cause ->
                Napier.e("Authentication error", cause)
                emit(handleException(cause))
            }

    private fun authenticationFlowWithHealthCard(
        profileId: ProfileIdentifier,
        scope: IdpScope,
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
                    handleAsyncExceptions(AuthenticationExceptionKind.IDPCommunicationFailed) {
                        idpUseCase.authenticationFlowWithHealthCard(
                            profileId = profileId,
                            scope = scope,
                            cardAccessNumber = can,
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
                    }
                },
                launch {
                    handleAsyncExceptions(AuthenticationExceptionKind.HealthCardCommunicationFailed) {
                        healthCardCommunication(
                            nfcChannel,
                            healthCardCertificateChannel,
                            signChannel,
                            responseChannel,
                            can = can,
                            pin = pin
                        )
                    }
                }
            )
        }

        send(AuthenticationState.AuthenticationFlowFinished)
    }

    @RequiresApi(Build.VERSION_CODES.P)
    private fun alternatePairingFlowWithSecureElement(
        profileId: ProfileIdentifier,
        can: String,
        pin: String,
        sePublicKey: PublicKey,
        aliasOfSecureElementEntry: ByteArray,
        cardChannel: Flow<NfcCardChannel>
    ) = channelFlow {
        send(AuthenticationState.AuthenticationFlowInitialized)

        cardChannel.first().use { nfcChannel ->
            send(AuthenticationState.HealthCardCommunicationChannelReady)

            // FIXME

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
                    handleAsyncExceptions(AuthenticationExceptionKind.IDPCommunicationFailed) {
                        idpUseCase.alternatePairingFlowWithSecureElement(
                            profileId = profileId,
                            cardAccessNumber = can,
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
                    }
                },
                launch {
                    handleAsyncExceptions(AuthenticationExceptionKind.HealthCardCommunicationFailed) {
                        healthCardCommunication(
                            nfcChannel,
                            healthCardCertificateChannel,
                            signChannel,
                            responseChannel,
                            can = can,
                            pin = pin
                        )
                    }
                }
            )
        }

        send(AuthenticationState.AuthenticationFlowFinished)
    }

    private inline fun handleAsyncExceptions(kind: AuthenticationExceptionKind, block: () -> Unit) {
        try {
            block()
        } catch (expected: Exception) {
            handleAsyncExceptions(expected, kind)
        }
    }

    private fun handleAsyncExceptions(e: Throwable, kind: AuthenticationExceptionKind) {
        if (e.suppressed.isNotEmpty()) {
            throw e.suppressed.first()
        }

        Napier.e("Authentication error", e)
        when (e) {
            is CancellationException,
            is AuthenticationException,
            is ResponseException -> throw e
            else -> {
                when (e) {
                    is ApiCallException ->
                        handleApiCallException(e, kind)
                    is KVNRAlreadyAssignedException ->
                        throw AuthenticationException(
                            kind = AuthenticationExceptionKind.InsuranceIdentifierAlreadyAssigned,
                            cause = e
                        )
                    else ->
                        throw AuthenticationException(kind)
                }
            }
        }
    }

    private fun handleApiCallException(e: ApiCallException, kind: AuthenticationExceptionKind) {
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
            IDPErrorCodes.AltAuthNotSuccessful ->
                throw AuthenticationException(AuthenticationExceptionKind.IDPCommunicationAltAuthNotSuccessful)
            IDPErrorCodes.InvalidHealthCardCertificate ->
                throw AuthenticationException(AuthenticationExceptionKind.IDPCommunicationInvalidCertificate)
            IDPErrorCodes.InvalidOCSPResponseOfHealthCardCertificate ->
                throw AuthenticationException(
                    AuthenticationExceptionKind.IDPCommunicationInvalidOCSPResponseOfHealthCardCertificate
                )
            else ->
                throw AuthenticationException(kind)
        }
    }

    private fun alternateAuthenticationFlowWithSecureElement(profileId: ProfileIdentifier, scope: IdpScope) =
        channelFlow {
            send(AuthenticationState.AuthenticationFlowInitialized)

            try {
                idpUseCase.alternateAuthenticationFlowWithSecureElement(profileId = profileId, scope = scope)
                send(AuthenticationState.IDPCommunicationFinished)
            } catch (e: Exception) {
                Napier.e("Authentication error", e)
                when (e) {
                    is KeyPermanentlyInvalidatedException,
                    is UserNotAuthenticatedException ->
                        throw AuthenticationException(AuthenticationExceptionKind.UserNotAuthenticated)
                    is AltAuthenticationCryptoException ->
                        throw AuthenticationException(AuthenticationExceptionKind.SecureElementFailure)
                    is ApiCallException ->
                        handleApiCallException(e, AuthenticationExceptionKind.IDPCommunicationFailed)
                    else ->
                        throw AuthenticationException(AuthenticationExceptionKind.IDPCommunicationFailed)
                }
            }

            send(AuthenticationState.AuthenticationFlowFinished)
        }

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
                    AuthenticationExceptionKind.IDPCommunicationAltAuthNotSuccessful ->
                        AuthenticationState.IDPCommunicationAltAuthNotSuccessful
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
                    AuthenticationExceptionKind.UserNotAuthenticated -> AuthenticationState.UserNotAuthenticated
                }
            }
            is ResponseException -> {
                when (e.responseStatus) {
                    ResponseStatus.AUTHENTICATION_FAILURE -> AuthenticationState.HealthCardCardAccessNumberWrong
                    else -> AuthenticationState.HealthCardCommunicationInterrupted
                }
            }
            is TagLostException, is IOException -> {
                Napier.e("IO Exception / NFC TAG was lost", e)
                AuthenticationState.HealthCardCommunicationInterrupted
            }
            else -> {
                Napier.e("Unknown exception", e)
                // soft fail
                AuthenticationState.HealthCardCommunicationInterrupted
            }
        }
}

private enum class AuthenticationExceptionKind {
    IDPCommunicationFailed,
    IDPCommunicationAltAuthNotSuccessful,
    IDPCommunicationInvalidCertificate,
    IDPCommunicationInvalidOCSPResponseOfHealthCardCertificate,

    HealthCardBlocked,
    HealthCardPin1RetryLeft,
    HealthCardPin2RetriesLeft,
    HealthCardCommunicationFailed,

    InsuranceIdentifierAlreadyAssigned,

    SecureElementFailure,
    UserNotAuthenticated,
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
