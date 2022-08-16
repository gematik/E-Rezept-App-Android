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

package de.gematik.ti.erp.app.cardunlock.usecase

import android.nfc.TagLostException
import androidx.compose.runtime.Stable
import de.gematik.ti.erp.app.card.model.command.ResponseException
import de.gematik.ti.erp.app.cardwall.model.nfc.card.NfcCardChannel
import de.gematik.ti.erp.app.cardwall.model.nfc.card.NfcCardSecureChannel
import de.gematik.ti.erp.app.card.model.command.ResponseStatus
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
    HealthCardPukBlocked,
    HealthCardCommunicationTrustedChannelEstablished,
    HealthCardCommunicationFinished;

    var pukRetriesLeft: Int = -1

    @Stable
    fun isFailure() =
        when (this) {
            HealthCardPukRetriesLeft,
            HealthCardCardAccessNumberWrong,
            HealthCardCommunicationInterrupted,
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
    fun unlockEgk(
        changeSecret: Boolean,
        can: String,
        puk: String,
        newSecret: String,
        cardChannel: Flow<NfcCardChannel>
    ): Flow<UnlockEgkState> =
        channelFlow {
            send(UnlockEgkState.UnlockFlowInitialized)
            cardChannel.first().use { nfcChannel ->
                send(UnlockEgkState.HealthCardCommunicationChannelReady)
                try {
                    healthCardCommunication(changeSecret, nfcChannel, can, puk, newSecret)
                } catch (expected: Exception) {
                    val state = handleException(expected)
                    send(state)
                }
            }
        }

    private fun handleException(e: Throwable): UnlockEgkState =
        when (e) {
            is ResponseException -> {
                @Suppress("MagicNumber")
                when (e.responseStatus) {
                    ResponseStatus.AUTHENTICATION_FAILURE -> UnlockEgkState.HealthCardCardAccessNumberWrong
                    ResponseStatus.PUK_BLOCKED -> UnlockEgkState.HealthCardPukBlocked
                    ResponseStatus.WRONG_SECRET_WARNING_COUNT_09 -> retriesLeft(9)
                    ResponseStatus.WRONG_SECRET_WARNING_COUNT_08 -> retriesLeft(8)
                    ResponseStatus.WRONG_SECRET_WARNING_COUNT_07 -> retriesLeft(7)
                    ResponseStatus.WRONG_SECRET_WARNING_COUNT_06 -> retriesLeft(6)
                    ResponseStatus.WRONG_SECRET_WARNING_COUNT_05 -> retriesLeft(5)
                    ResponseStatus.WRONG_SECRET_WARNING_COUNT_04 -> retriesLeft(4)
                    ResponseStatus.WRONG_SECRET_WARNING_COUNT_03 -> retriesLeft(3)
                    ResponseStatus.WRONG_SECRET_WARNING_COUNT_02 -> retriesLeft(2)
                    ResponseStatus.WRONG_SECRET_WARNING_COUNT_01 -> retriesLeft(1)
                    else -> UnlockEgkState.HealthCardCommunicationInterrupted
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
    changeSecret: Boolean,
    channel: NfcCardChannel,
    can: String,
    puk: String,
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
        changeSecret = changeSecret,
        puk = puk,
        newSecret = newSecret
    )

    println("Response: $response")

    send(
        @Suppress("MagicNumber")

        when (response) {
            ResponseStatus.SUCCESS -> UnlockEgkState.HealthCardCommunicationFinished
            ResponseStatus.WRONG_SECRET_WARNING_COUNT_09 -> retriesLeft(9)
            ResponseStatus.WRONG_SECRET_WARNING_COUNT_08 -> retriesLeft(8)
            ResponseStatus.WRONG_SECRET_WARNING_COUNT_07 -> retriesLeft(7)
            ResponseStatus.WRONG_SECRET_WARNING_COUNT_06 -> retriesLeft(6)
            ResponseStatus.WRONG_SECRET_WARNING_COUNT_05 -> retriesLeft(5)
            ResponseStatus.WRONG_SECRET_WARNING_COUNT_04 -> retriesLeft(4)
            ResponseStatus.WRONG_SECRET_WARNING_COUNT_03 -> retriesLeft(3)
            ResponseStatus.WRONG_SECRET_WARNING_COUNT_02 -> retriesLeft(2)
            ResponseStatus.WRONG_SECRET_WARNING_COUNT_01 -> retriesLeft(1)
            else -> UnlockEgkState.HealthCardPukBlocked
        }
    )
}

private fun retriesLeft(n: Int) =
    UnlockEgkState.HealthCardPukRetriesLeft.apply {
        this.pukRetriesLeft = n
    }
