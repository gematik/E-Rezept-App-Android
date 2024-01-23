/*
 * Copyright (c) 2024 gematik GmbH
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

package de.gematik.ti.erp.app.userauthentication.observer

import androidx.compose.runtime.Stable
import androidx.lifecycle.Lifecycle.Event
import androidx.lifecycle.Lifecycle.Event.ON_CREATE
import androidx.lifecycle.Lifecycle.Event.ON_START
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import de.gematik.ti.erp.app.Requirement
import de.gematik.ti.erp.app.timeouts.repository.TimeoutRepository
import de.gematik.ti.erp.app.settings.model.SettingsData
import de.gematik.ti.erp.app.settings.model.SettingsData.AuthenticationMode.Password
import de.gematik.ti.erp.app.settings.usecase.SettingsUseCase
import io.github.aakira.napier.Napier
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combineTransform
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.shareIn
import kotlinx.coroutines.launch
import java.time.Duration

@Stable
sealed class AuthenticationModeAndMethod {
    data object None : AuthenticationModeAndMethod()
    data object Authenticated : AuthenticationModeAndMethod()
    data class AuthenticationRequired(val method: SettingsData.AuthenticationMode, val nrOfFailedAuthentications: Int) :
        AuthenticationModeAndMethod()
}

private const val RESET_TIMEOUT = -1L

// tag::AuthenticationUseCase[]
@Requirement(
    "O.Auth_8",
    sourceSpecification = "BSI-eRp-ePA",
    rationale = "A Timer is used to measure the time a user is inactive. Every user interaction resets the timer."
)
class InactivityTimeoutObserver(
    private val settingsUseCase: SettingsUseCase,
    private val timeoutRepo: TimeoutRepository,
    dispatcher: CoroutineDispatcher = Dispatchers.Default
) : LifecycleEventObserver {
    private enum class Lifecycle {
        Created, Started, Running, Paused
    }

    private val scope = CoroutineScope(dispatcher)

    private val authRequired = MutableStateFlow(false)
    private val lifecycle = MutableStateFlow(Lifecycle.Created)

    // Replay is required so that it keeps a buffer if the app goes in the background
    private val inactivityTimerChannel = MutableSharedFlow<Long>(replay = 1)

    private var unspecifiedAuthentication = false

    private fun authenticationFlow() =
        combineTransform(
            lifecycle,
            authRequired,
            settingsUseCase.authenticationMode,
            settingsUseCase.general
        ) { lifecycle, authRequired, authenticationMode, settings ->
            unspecifiedAuthentication = authenticationMode is SettingsData.AuthenticationMode.Unspecified

            when (lifecycle) {
                Lifecycle.Created -> {
                    this@InactivityTimeoutObserver.authRequired.value = when (authenticationMode) {
                        SettingsData.AuthenticationMode.Unspecified -> false
                        else -> true
                    }
                    this@InactivityTimeoutObserver.lifecycle.value = Lifecycle.Running
                }
                Lifecycle.Started -> {
                    this@InactivityTimeoutObserver.lifecycle.value = Lifecycle.Running
                }
                Lifecycle.Running -> {
                    when (authenticationMode) {
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
                Lifecycle.Paused -> emit(AuthenticationModeAndMethod.None)
            }
        }.distinctUntilChanged()

    val authenticationModeAndMethod: Flow<AuthenticationModeAndMethod> =
        channelFlow {
            launch {
                var currentTimeout: Long = RESET_TIMEOUT
                inactivityTimerChannel
                    .filter { timeout ->
                        currentTimeout <= 0 || timeout <= currentTimeout
                    }
                    .collectLatest { timeout ->
                        currentTimeout = timeout
                        if (timeout > 0) {
                            Napier.i { "Restarted inactivity timer for ${Duration.ofMillis(timeout)}" }
                            delay(timeout)
                            requireAuthentication()
                            currentTimeout = RESET_TIMEOUT
                        } else {
                            inactivityTimerChannel.tryEmit(timeoutRepo.getInactivityTimeout().inWholeMilliseconds)
                        }
                    }
            }

            authenticationFlow()
                .collect {
                    if (it == AuthenticationModeAndMethod.Authenticated) {
                        inactivityTimerChannel.tryEmit(timeoutRepo.getInactivityTimeout().inWholeMilliseconds)
                    }
                    send(it)
                }
        }.flowOn(dispatcher)
            // need replay for buffer
            .shareIn(scope = scope, started = SharingStarted.Lazily, replay = 1)
    // end::AuthenticationUseCase[]

    suspend fun isPasswordValid(password: String): Boolean =
        settingsUseCase.authenticationMode.map {
            (it as? Password)?.isValid(password) ?: false
        }.first()

    fun resetInactivityTimer() {
        inactivityTimerChannel.tryEmit(timeoutRepo.getInactivityTimeout().inWholeMilliseconds)
    }

    fun authenticated() {
        authRequired.value = false
    }

    private fun requireAuthentication() {
        if (!unspecifiedAuthentication) {
            Napier.i { "Authentication required" }
            authRequired.value = true
        }
    }

    /**
     * This [forceRequireAuthentication] is called when the pause timeout has run
     */
    fun forceRequireAuthentication() {
        Napier.i { "Authentication required" }
        authRequired.value = true
    }

    suspend fun incrementNumberOfAuthenticationFailures() =
        settingsUseCase.incrementNumberOfAuthenticationFailures()

    suspend fun resetNumberOfAuthenticationFailures() =
        settingsUseCase.resetNumberOfAuthenticationFailures()

    override fun onStateChanged(source: LifecycleOwner, event: Event) {
        Napier.i { "Application lifecycle event state: $event" }
        when (event) {
            ON_CREATE -> lifecycle.value = Lifecycle.Created
            ON_START -> {
                if (lifecycle.value != Lifecycle.Created) {
                    lifecycle.value = Lifecycle.Started
                }
                inactivityTimerChannel.tryEmit(RESET_TIMEOUT)
            }
            else -> {
                // Pause timeout is being handled in the [BaseActivity]
            }
        }
    }
}
