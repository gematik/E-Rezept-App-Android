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
import de.gematik.ti.erp.app.demo.usecase.DemoUseCase
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class AuthenticationUseCaseDelegate @Inject constructor(
    private val demoDelegate: AuthenticationUseCaseDemo,
    private val productionDelegate: AuthenticationUseCaseProduction,
    private val demoUseCase: DemoUseCase
) : AuthenticationUseCase {
    private val delegate: AuthenticationUseCase
        get() = if (demoUseCase.isDemoModeActive) demoDelegate else productionDelegate

    override fun authenticateWithHealthCard(
        can: String,
        pin: String,
        cardChannel: Flow<NfcCardChannel>
    ): Flow<AuthenticationState> =
        delegate.authenticateWithHealthCard(can, pin, cardChannel)

    @RequiresApi(Build.VERSION_CODES.P)
    override fun pairDeviceWithHealthCardAndSecureElement(
        can: String,
        pin: String,
        cardChannel: Flow<NfcCardChannel>
    ): Flow<AuthenticationState> =
        delegate.pairDeviceWithHealthCardAndSecureElement(can, pin, cardChannel)

    override fun authenticateWithSecureElement(): Flow<AuthenticationState> =
        delegate.authenticateWithSecureElement()
}
