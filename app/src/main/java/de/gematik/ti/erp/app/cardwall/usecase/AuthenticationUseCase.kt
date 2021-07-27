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

import android.os.Build
import androidx.annotation.RequiresApi
import de.gematik.ti.erp.app.cardwall.model.nfc.card.NfcCardChannel
import kotlinx.coroutines.flow.Flow

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
            IDPCommunicationFinished, -> true
            else -> false
        }

    fun isNotInProgress() = !isInProgress()

    fun isFinal() = this == AuthenticationFlowFinished
    fun isReady() = this == None
}

interface AuthenticationUseCase {
    fun authenticateWithHealthCard(
        can: String,
        pin: String,
        cardChannel: Flow<NfcCardChannel>
    ): Flow<AuthenticationState>

    @RequiresApi(Build.VERSION_CODES.P)
    fun pairDeviceWithHealthCardAndSecureElement(
        can: String,
        pin: String,
        cardChannel: Flow<NfcCardChannel>
    ): Flow<AuthenticationState>

    fun authenticateWithSecureElement(): Flow<AuthenticationState>
}
