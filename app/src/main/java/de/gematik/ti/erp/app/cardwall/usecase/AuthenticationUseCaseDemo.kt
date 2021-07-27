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

import de.gematik.ti.erp.app.cardwall.model.nfc.card.NfcCardChannel
import de.gematik.ti.erp.app.demo.usecase.DemoUseCase
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.onEach
import javax.inject.Inject

private const val DEMO_TIMEOUT = 700L

class AuthenticationUseCaseDemo @Inject constructor(
    private val demoUseCase: DemoUseCase
) : AuthenticationUseCase {

    override fun authenticateWithHealthCard(
        can: String,
        pin: String,
        cardChannel: Flow<NfcCardChannel>
    ): Flow<AuthenticationState> = flow {
        val states = listOf(
            AuthenticationState.AuthenticationFlowInitialized,
            AuthenticationState.HealthCardCommunicationChannelReady,
            AuthenticationState.HealthCardCommunicationTrustedChannelEstablished,
            AuthenticationState.HealthCardCommunicationCertificateLoaded,
            AuthenticationState.HealthCardCommunicationFinished,
            AuthenticationState.IDPCommunicationFinished,
            AuthenticationState.AuthenticationFlowFinished
        )

        states.forEach {
            val stepTimeout = if (it == AuthenticationState.HealthCardCommunicationChannelReady) {
                DEMO_TIMEOUT * 5L
            } else {
                DEMO_TIMEOUT
            }
            emit(it)
            delay(stepTimeout)
        }
    }.onEach {
        if (it.isFinal()) {
            demoUseCase.authTokenReceived.value = true
        }
    }.catch {
        emit(AuthenticationState.HealthCardCommunicationInterrupted)
    }

    override fun pairDeviceWithHealthCardAndSecureElement(
        can: String,
        pin: String,
        cardChannel: Flow<NfcCardChannel>
    ): Flow<AuthenticationState> = authenticateWithHealthCard(can, pin, cardChannel)

    override fun authenticateWithSecureElement(): Flow<AuthenticationState> =
        authenticateWithHealthCard("", "", emptyFlow())
}
