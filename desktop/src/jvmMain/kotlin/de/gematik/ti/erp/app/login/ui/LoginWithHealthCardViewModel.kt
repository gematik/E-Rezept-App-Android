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

package de.gematik.ti.erp.app.login.ui

import de.gematik.ti.erp.app.DispatchProvider
import de.gematik.ti.erp.app.DownloadUseCase
import de.gematik.ti.erp.app.cardwall.AuthenticationState
import de.gematik.ti.erp.app.cardwall.AuthenticationUseCase
import de.gematik.ti.erp.app.nfc.model.card.NfcHealthCard
import de.gematik.ti.erp.app.smartcard.CardFactory
import de.gematik.ti.erp.app.smartcard.CardReader
import io.github.aakira.napier.Napier
import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.transform
import kotlinx.coroutines.withContext
import org.kodein.di.bindings.ScopeCloseable
import java.util.concurrent.Executors

class LoginWithHealthCardViewModel(
    private val dispatchProvider: DispatchProvider,
    private val authenticationUseCase: AuthenticationUseCase,
    private val downloadUseCase: DownloadUseCase
) : ScopeCloseable {
    private val executor = Executors.newSingleThreadExecutor()
    private val cardReaderDispatcher = executor.asCoroutineDispatcher()

    suspend fun waitForAnyReader() {
        withContext(cardReaderDispatcher) {
            var reader: CardReader? = null
            do {
                try {
                    reader = CardFactory.instance.readers.firstOrNull()
                    if (reader == null) {
                        delay(1000)
                    }
                } catch (e: Exception) {
                    Napier.e("", e)
                    delay(2000)
                }
            } while (reader == null)
        }
    }

    fun authenticate(can: String, pin: String): Flow<AuthenticationState> =
        authenticationUseCase.authenticateWithHealthCard(
            can = can,
            pin = pin,
            flow {
                var reader: CardReader?
                do {
                    reader = CardFactory.instance.readers.find { it.isCardPresent }

                    delay(1000)
                } while (reader == null)

                emit(NfcHealthCard.connect(reader))
            }.flowOn(cardReaderDispatcher)
        )
            .flowOn(cardReaderDispatcher)
            .transform { state ->
                if (state.isFinal()) {
                    downloadUseCase.update()
                        .onFailure {
                            Napier.e("Downloading resources failed", it)
                            emit(AuthenticationState.IDPCommunicationFailed)
                        }
                        .onSuccess {
                            emit(state)
                        }
                } else {
                    emit(state)
                }
            }
            .flowOn(dispatchProvider.IO)

    override fun close() {
        executor.shutdownNow()
    }
}
