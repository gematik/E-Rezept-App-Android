/*
 * Copyright (Change Date see Readme), gematik GmbH
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
 * In case of changes by gematik GmbH find details in the "Readme" file.
 *
 * See the Licence for the specific language governing permissions and limitations under the Licence.
 *
 * *******
 *
 * For additional notes and disclaimer from gematik and in case of changes by gematik find details in the "Readme" file.
 */

package de.gematik.ti.erp.app.cardunlock.usecase

import android.nfc.TagLostException
import androidx.compose.runtime.Stable
import de.gematik.ti.erp.app.card.model.command.ResponseException
import de.gematik.ti.erp.app.cardwall.model.nfc.card.NfcCardChannel
import de.gematik.ti.erp.app.cardwall.model.nfc.card.NfcCardSecureChannel
import de.gematik.ti.erp.app.card.model.command.ResponseStatus
import de.gematik.ti.erp.app.card.model.command.UnlockMethod
import de.gematik.ti.erp.app.card.model.exchange.unlockEgk
import de.gematik.ti.erp.app.cardwall.model.nfc.exchange.establishTrustedChannel
import kotlinx.coroutines.channels.ProducerScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.flow.first
import io.github.aakira.napier.Napier
import java.io.IOException

@Stable
enum class UnlockEgkState {
    None,
    UnlockFlowInitialized,
    HealthCardCommunicationInterrupted,
    HealthCardCommunicationChannelReady,
    HealthCardCardAccessNumberWrong,
    HealthCardPukRetriesLeft,
    HealthCardPinRetriesLeft,
    HealthCardPukBlocked,
    HealthCardPasswordBlocked,
    HealthCardCommunicationTrustedChannelEstablished,
    MemoryFailure,
    SecurityStatusNotSatisfied,
    PasswordNotFound,
    PasswordNotUsable,
    HealthCardCommunicationFinished;

    var retriesLeft: Int = -1

    @Stable
    fun isFailure() =
        when (this) {
            HealthCardPasswordBlocked,
            HealthCardPukRetriesLeft,
            HealthCardCardAccessNumberWrong,
            HealthCardCommunicationInterrupted,
            MemoryFailure,
            SecurityStatusNotSatisfied,
            PasswordNotFound,
            PasswordNotUsable,
            HealthCardPukBlocked -> true
            else -> false
        }

    @Stable
    fun isInProgress() =
        when (this) {
            HealthCardCommunicationChannelReady,
            HealthCardCommunicationTrustedChannelEstablished -> true

            else -> false
        }

    @Stable
    fun isReady() = this == None
}

class UnlockEgkUseCase {
    operator fun invoke(
        unlockMethod: String,
        can: String,
        puk: String,
        oldSecret: String,
        newSecret: String,
        cardChannel: Flow<NfcCardChannel>
    ): Flow<UnlockEgkState> =
        channelFlow {
            send(UnlockEgkState.UnlockFlowInitialized)
            cardChannel.first().use { nfcChannel ->
                send(UnlockEgkState.HealthCardCommunicationChannelReady)
                try {
                    healthCardCommunication(unlockMethod, nfcChannel, can, puk, oldSecret, newSecret)
                } catch (expected: Exception) {
                    val state = handleException(unlockMethod, expected)
                    send(state)
                }
            }
        }

    private fun handleException(unlockMethod: String, e: Throwable): UnlockEgkState =
        when (e) {
            is ResponseException -> {
                @Suppress("MagicNumber")
                if (unlockMethod == UnlockMethod.ChangeReferenceData.name) {
                    when (e.responseStatus) {
                        ResponseStatus.AUTHENTICATION_FAILURE -> UnlockEgkState.HealthCardCardAccessNumberWrong
                        ResponseStatus.PASSWORD_BLOCKED -> UnlockEgkState.HealthCardPasswordBlocked
                        ResponseStatus.MEMORY_FAILURE -> UnlockEgkState.MemoryFailure
                        ResponseStatus.SECURITY_STATUS_NOT_SATISFIED -> UnlockEgkState.SecurityStatusNotSatisfied
                        ResponseStatus.PASSWORD_NOT_FOUND -> UnlockEgkState.PasswordNotFound
                        ResponseStatus.PASSWORD_NOT_USABLE -> UnlockEgkState.PasswordNotUsable

                        ResponseStatus.WRONG_SECRET_WARNING_COUNT_03 ->
                            retriesLeft(UnlockEgkState.HealthCardPinRetriesLeft, 3)

                        ResponseStatus.WRONG_SECRET_WARNING_COUNT_02 ->
                            retriesLeft(UnlockEgkState.HealthCardPinRetriesLeft, 2)

                        ResponseStatus.WRONG_SECRET_WARNING_COUNT_01 ->
                            retriesLeft(UnlockEgkState.HealthCardPinRetriesLeft, 1)
                        else -> UnlockEgkState.HealthCardCommunicationInterrupted
                    }
                } else {
                    when (e.responseStatus) {
                        ResponseStatus.AUTHENTICATION_FAILURE -> UnlockEgkState.HealthCardCardAccessNumberWrong
                        ResponseStatus.PUK_BLOCKED -> UnlockEgkState.HealthCardPukBlocked
                        ResponseStatus.PASSWORD_BLOCKED -> UnlockEgkState.HealthCardPasswordBlocked
                        ResponseStatus.MEMORY_FAILURE -> UnlockEgkState.MemoryFailure
                        ResponseStatus.SECURITY_STATUS_NOT_SATISFIED -> UnlockEgkState.SecurityStatusNotSatisfied
                        ResponseStatus.PASSWORD_NOT_FOUND -> UnlockEgkState.PasswordNotFound
                        ResponseStatus.PASSWORD_NOT_USABLE -> UnlockEgkState.PasswordNotUsable

                        ResponseStatus.WRONG_SECRET_WARNING_COUNT_09 ->
                            retriesLeft(UnlockEgkState.HealthCardPukRetriesLeft, 9)

                        ResponseStatus.WRONG_SECRET_WARNING_COUNT_08 ->
                            retriesLeft(UnlockEgkState.HealthCardPukRetriesLeft, 8)

                        ResponseStatus.WRONG_SECRET_WARNING_COUNT_07 ->
                            retriesLeft(UnlockEgkState.HealthCardPukRetriesLeft, 7)

                        ResponseStatus.WRONG_SECRET_WARNING_COUNT_06 ->
                            retriesLeft(UnlockEgkState.HealthCardPukRetriesLeft, 6)

                        ResponseStatus.WRONG_SECRET_WARNING_COUNT_05 ->
                            retriesLeft(UnlockEgkState.HealthCardPukRetriesLeft, 5)

                        ResponseStatus.WRONG_SECRET_WARNING_COUNT_04 ->
                            retriesLeft(UnlockEgkState.HealthCardPukRetriesLeft, 4)

                        ResponseStatus.WRONG_SECRET_WARNING_COUNT_03 ->
                            retriesLeft(UnlockEgkState.HealthCardPukRetriesLeft, 3)

                        ResponseStatus.WRONG_SECRET_WARNING_COUNT_02 ->
                            retriesLeft(UnlockEgkState.HealthCardPukRetriesLeft, 2)

                        ResponseStatus.WRONG_SECRET_WARNING_COUNT_01 ->
                            retriesLeft(UnlockEgkState.HealthCardPukRetriesLeft, 1)

                        else -> UnlockEgkState.HealthCardCommunicationInterrupted
                    }
                }
            }

            is TagLostException, is IOException -> {
                Napier.e("IO Exception / NFC TAG was lost", e)
                UnlockEgkState.HealthCardCommunicationInterrupted
            }

            else -> UnlockEgkState.HealthCardCommunicationInterrupted
        }
}

private suspend fun ProducerScope<UnlockEgkState>.healthCardCommunication(
    unlockMethod: String,
    channel: NfcCardChannel,
    can: String,
    puk: String,
    oldSecret: String,
    newSecret: String
) {
    val paceKey = channel.establishTrustedChannel(can)

    val secChannel = NfcCardSecureChannel(
        channel.isExtendedLengthSupported,
        channel.card,
        paceKey
    )

    send(UnlockEgkState.HealthCardCommunicationTrustedChannelEstablished)

    val response = secChannel.unlockEgk(
        unlockMethod = unlockMethod,
        puk = puk,
        oldSecret = oldSecret,
        newSecret = newSecret
    )

    send(
        @Suppress("MagicNumber")
        if (unlockMethod == UnlockMethod.ChangeReferenceData.name) {
            when (response) {
                ResponseStatus.SUCCESS -> UnlockEgkState.HealthCardCommunicationFinished
                ResponseStatus.WRONG_SECRET_WARNING_COUNT_03 ->
                    retriesLeft(UnlockEgkState.HealthCardPinRetriesLeft, 3)
                ResponseStatus.WRONG_SECRET_WARNING_COUNT_02 ->
                    retriesLeft(UnlockEgkState.HealthCardPinRetriesLeft, 2)
                ResponseStatus.WRONG_SECRET_WARNING_COUNT_01 ->
                    retriesLeft(UnlockEgkState.HealthCardPinRetriesLeft, 1)
                ResponseStatus.MEMORY_FAILURE -> UnlockEgkState.MemoryFailure
                ResponseStatus.SECURITY_STATUS_NOT_SATISFIED -> UnlockEgkState.SecurityStatusNotSatisfied
                ResponseStatus.PASSWORD_NOT_FOUND -> UnlockEgkState.PasswordNotFound
                ResponseStatus.PASSWORD_NOT_USABLE -> UnlockEgkState.PasswordNotUsable

                else -> UnlockEgkState.HealthCardPasswordBlocked
            }
        } else {
            when (response) {
                ResponseStatus.SUCCESS -> UnlockEgkState.HealthCardCommunicationFinished
                ResponseStatus.MEMORY_FAILURE -> UnlockEgkState.MemoryFailure
                ResponseStatus.SECURITY_STATUS_NOT_SATISFIED -> UnlockEgkState.SecurityStatusNotSatisfied
                ResponseStatus.PASSWORD_NOT_FOUND -> UnlockEgkState.PasswordNotFound
                ResponseStatus.PASSWORD_NOT_USABLE -> UnlockEgkState.PasswordNotUsable

                ResponseStatus.WRONG_SECRET_WARNING_COUNT_09 ->
                    retriesLeft(UnlockEgkState.HealthCardPukRetriesLeft, 9)
                ResponseStatus.WRONG_SECRET_WARNING_COUNT_08 ->
                    retriesLeft(UnlockEgkState.HealthCardPukRetriesLeft, 8)
                ResponseStatus.WRONG_SECRET_WARNING_COUNT_07 ->
                    retriesLeft(UnlockEgkState.HealthCardPukRetriesLeft, 7)
                ResponseStatus.WRONG_SECRET_WARNING_COUNT_06 ->
                    retriesLeft(UnlockEgkState.HealthCardPukRetriesLeft, 6)
                ResponseStatus.WRONG_SECRET_WARNING_COUNT_05 ->
                    retriesLeft(UnlockEgkState.HealthCardPukRetriesLeft, 5)
                ResponseStatus.WRONG_SECRET_WARNING_COUNT_04 ->
                    retriesLeft(UnlockEgkState.HealthCardPukRetriesLeft, 4)
                ResponseStatus.WRONG_SECRET_WARNING_COUNT_03 ->
                    retriesLeft(UnlockEgkState.HealthCardPukRetriesLeft, 3)
                ResponseStatus.WRONG_SECRET_WARNING_COUNT_02 ->
                    retriesLeft(UnlockEgkState.HealthCardPukRetriesLeft, 2)
                ResponseStatus.WRONG_SECRET_WARNING_COUNT_01 ->
                    retriesLeft(UnlockEgkState.HealthCardPukRetriesLeft, 1)
                else -> UnlockEgkState.HealthCardPukBlocked
            }
        }
    )
}

private fun retriesLeft(state: UnlockEgkState, n: Int) =
    state.apply {
        this.retriesLeft = n
    }
