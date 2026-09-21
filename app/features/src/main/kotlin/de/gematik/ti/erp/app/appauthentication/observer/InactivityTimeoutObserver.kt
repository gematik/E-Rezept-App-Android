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

package de.gematik.ti.erp.app.appauthentication.observer

import androidx.compose.runtime.Stable
import androidx.lifecycle.Lifecycle.Event
import androidx.lifecycle.Lifecycle.Event.ON_CREATE
import androidx.lifecycle.Lifecycle.Event.ON_START
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import de.gematik.ti.erp.app.Requirement
import de.gematik.ti.erp.app.appauthentication.model.AppAuthenticationErpModel
import de.gematik.ti.erp.app.appauthentication.model.AppAuthenticationMethodErpModel
import de.gematik.ti.erp.app.appauthentication.repository.AppAuthenticationRepository
import de.gematik.ti.erp.app.timeouts.repository.TimeoutRepository
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
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@Stable
sealed class AuthenticationModeAndMethod {
    data object None : AuthenticationModeAndMethod()
    data object Authenticated : AuthenticationModeAndMethod()
    data class AuthenticationRequired(val authentication: AppAuthenticationErpModel) :
        AuthenticationModeAndMethod()
}

private const val RESET_TIMEOUT = -1L

@Requirement(
    "O.Auth_9#1",
    "O.Plat_9#2",
    sourceSpecification = "BSI-eRp-ePA",
    rationale = "A Timer is observing the app lifecycle to notice a change in app state."
)
class InactivityTimeoutObserver( // TODO: Move to different package
    private val repository: AppAuthenticationRepository,
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
            repository.getAppAuthenticationErpModel()
        ) { lifecycle, authRequired, authenticationErpModel ->
            unspecifiedAuthentication = authenticationErpModel.authenticationMethod is AppAuthenticationMethodErpModel.NotInitialised

            when (lifecycle) {
                Lifecycle.Created -> {
                    this@InactivityTimeoutObserver.authRequired.update {
                        authenticationErpModel.authenticationMethod !is AppAuthenticationMethodErpModel.NotInitialised
                    }
                    this@InactivityTimeoutObserver.lifecycle.update { Lifecycle.Running }
                }

                Lifecycle.Started -> {
                    this@InactivityTimeoutObserver.lifecycle.update { Lifecycle.Running }
                }

                Lifecycle.Running -> {
                    if (authenticationErpModel.authenticationMethod is AppAuthenticationMethodErpModel.NotInitialised) {
                        emit(AuthenticationModeAndMethod.Authenticated)
                    } else {
                        if (authRequired) {
                            emit(
                                AuthenticationModeAndMethod.AuthenticationRequired(
                                    authentication = authenticationErpModel
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
                        currentTimeout !in 1..timeout
                    }
                    .collectLatest { timeout ->
                        currentTimeout = timeout
                        if (timeout > 0) {
                            delay(timeout)
                            requireAuthentication()
                            @Requirement(
                                "O.Auth_9#2",
                                sourceSpecification = "BSI-eRp-ePA",
                                rationale = "The timer is reset on user interaction",
                                codeLines = 2
                            )
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

    @Requirement(
        "O.Auth_7#1",
        sourceSpecification = "BSI-eRp-ePA",
        rationale = "Password check is done in a secure way.",
        codeLines = 10
    )
    suspend fun isPasswordValid(password: String): Boolean =
        repository.getAppAuthenticationErpModel().map {
            when (
                it.authenticationMethod
            ) {
                is AppAuthenticationMethodErpModel.Password -> {
                    (it.authenticationMethod as AppAuthenticationMethodErpModel.Password).password.isValid(password)
                }
                is AppAuthenticationMethodErpModel.DeviceSecurity -> {
                    false
                }
                is AppAuthenticationMethodErpModel.Both -> {
                    (it.authenticationMethod as AppAuthenticationMethodErpModel.Both).password.isValid(password)
                }
                is AppAuthenticationMethodErpModel.NotInitialised -> {
                    false
                }
            }
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
        repository.incrementNumberOfAuthenticationFailures()

    suspend fun resetNumberOfAuthenticationFailures() =
        repository.resetNumberOfAuthenticationFailures()

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
