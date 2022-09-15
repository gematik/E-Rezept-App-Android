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

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.runtime.Stable
import de.gematik.ti.erp.app.cardwall.model.nfc.card.NfcCardChannel
import kotlinx.coroutines.flow.Flow

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
            IDPCommunicationInvalidCertificate,
            is InsuranceIdentifierAlreadyExists,
            SecureElementCryptographyFailed -> true
            else -> false
        }

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

    suspend fun isCanAvailable(): Boolean
}
