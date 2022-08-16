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

package de.gematik.ti.erp.app.userauthentication.ui

import androidx.compose.runtime.Stable
import androidx.lifecycle.Lifecycle.Event
import androidx.lifecycle.Lifecycle.Event.ON_START
import androidx.lifecycle.Lifecycle.Event.ON_STOP
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import de.gematik.ti.erp.app.DispatchProvider
import de.gematik.ti.erp.app.settings.model.SettingsData
import de.gematik.ti.erp.app.settings.model.SettingsData.AuthenticationMode.Password
import de.gematik.ti.erp.app.settings.usecase.SettingsUseCase
import io.github.aakira.napier.Napier
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.trySendBlocking
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.flow.combineTransform
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.shareIn
import kotlinx.coroutines.launch
import java.time.Duration

@Stable
sealed class AuthenticationModeAndMethod {
    object None : AuthenticationModeAndMethod()
    object Authenticated : AuthenticationModeAndMethod()
    data class AuthenticationRequired(val method: SettingsData.AuthenticationMode, val nrOfFailedAuthentications: Int) :
        AuthenticationModeAndMethod()
}

private val InactivityTimeout = Duration.ofMinutes(10)

// tag::AuthenticationUseCase[]
class AuthenticationUseCase(
    private val settingsUseCase: SettingsUseCase,
    private val dispatchers: DispatchProvider
) : LifecycleEventObserver {
    private enum class Lifecycle {
        Started, Running, Stopped
    }

    private val scope = CoroutineScope(dispatchers.Default)

    private val authRequired = MutableStateFlow(false)
    private val lifecycle = MutableStateFlow(Lifecycle.Started)
    private val timerResetChannel = Channel<Unit>(Channel.CONFLATED)

    private fun authenticationFlow() =
        combineTransform(
            lifecycle,
            authRequired,
            settingsUseCase.authenticationMode,
            settingsUseCase.general
        ) { lifecycle, authRequired, authenticationMode, settings ->
            @Suppress("deprecation")
            when (lifecycle) {
                Lifecycle.Started -> {
                    this@AuthenticationUseCase.authRequired.value = when (authenticationMode) {
                        SettingsData.AuthenticationMode.None,
                        SettingsData.AuthenticationMode.Unspecified -> false
                        else -> true
                    }
                    this@AuthenticationUseCase.lifecycle.value = Lifecycle.Running
                }
                Lifecycle.Running -> {
                    when (authenticationMode) {
                        SettingsData.AuthenticationMode.None,
                        SettingsData.AuthenticationMode.Unspecified ->
                            emit(AuthenticationModeAndMethod.Authenticated)
                        else -> if (authRequired) {
                            emit(
                                AuthenticationModeAndMethod.AuthenticationRequired(
                                    authenticationMode,
                                    settings.authenticationFails
                                )
                            )
                        } else {
                            emit(AuthenticationModeAndMethod.Authenticated)
                        }
                    }
                }
                Lifecycle.Stopped -> emit(AuthenticationModeAndMethod.None)
            }
        }.distinctUntilChanged()

    val authenticationModeAndMethod: Flow<AuthenticationModeAndMethod> =
        channelFlow {
            val timer = suspend {
                Napier.d { "Restarted inactivity timer for $InactivityTimeout" }
                delay(InactivityTimeout.toMillis())
                requireAuthentication()
            }

            launch {
                var timerJob: Job? = null
                for (ignored in timerResetChannel) {
                    timerJob?.cancel()
                    timerJob = launch { timer() }
                }
            }

            Napier.d { "Started authentication flow" }

            authenticationFlow()
                .collect {
                    if (it == AuthenticationModeAndMethod.Authenticated) {
                        timerResetChannel.send(Unit)
                    }

                    Napier.d { "Current authentication mode $it" }

                    send(it)
                }
        }.flowOn(dispatchers.Default)
            .shareIn(scope = scope, started = SharingStarted.Lazily, replay = 1)
    // end::AuthenticationUseCase[]

    suspend fun isPasswordValid(password: String): Boolean =
        settingsUseCase.authenticationMode.map {
            (it as? Password)?.isValid(password) ?: false
        }.first()

    fun resetInactivityTimer() {
        timerResetChannel.trySendBlocking(Unit)
    }

    fun authenticated() {
        authRequired.value = false
    }

    private fun requireAuthentication() {
        authRequired.value = true
    }

    suspend fun incrementNumberOfAuthenticationFailures() =
        settingsUseCase.incrementNumberOfAuthenticationFailures()

    suspend fun resetNumberOfAuthenticationFailures() =
        settingsUseCase.resetNumberOfAuthenticationFailures()

    override fun onStateChanged(source: LifecycleOwner, event: Event) {
        when (event) {
            ON_START -> lifecycle.value = Lifecycle.Started
            ON_STOP -> lifecycle.value = Lifecycle.Stopped
            else -> {}
        }
    }
}
