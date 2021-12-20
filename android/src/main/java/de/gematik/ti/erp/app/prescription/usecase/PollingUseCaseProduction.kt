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

package de.gematik.ti.erp.app.prescription.usecase

import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.OnLifecycleEvent
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import java.time.Duration
import javax.inject.Inject
import javax.inject.Singleton

// TODO read from build config
private val REFRESH_TIMEOUT = Duration.ofMinutes(10).toMillis()

@Singleton
class PollingUseCaseProduction @Inject constructor() : PollingUseCase, LifecycleObserver {
    private val doInstantRefresh = MutableSharedFlow<Unit>()
    private var appRunning = false
    private var appRunningWakeEvent = Channel<Unit>()

    @OptIn(ExperimentalCoroutinesApi::class)
    override val doRefresh: Flow<Unit> = channelFlow {
        launch {
            while (true) {
                if (appRunning) {
                    send(Unit)
                    delay(REFRESH_TIMEOUT)
                } else {
                    appRunningWakeEvent.receive()
                }
            }
        }
        doInstantRefresh.collect {
            send(Unit)
        }
    }

    override suspend fun refreshNow() {
        doInstantRefresh.emit(Unit)
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_START)
    fun onStartApp() {
        appRunning = true
        appRunningWakeEvent.trySend(Unit)
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_STOP)
    fun onStopApp() {
        appRunning = false
    }
}
