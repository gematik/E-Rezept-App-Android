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

package de.gematik.ti.erp.app.userauthentication.ui

import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.OnLifecycleEvent
import de.gematik.ti.erp.app.db.entities.SettingsAuthenticationMethod
import de.gematik.ti.erp.app.settings.usecase.SettingsUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combineTransform
import kotlinx.coroutines.flow.distinctUntilChanged
import javax.inject.Inject
import javax.inject.Singleton

sealed class AuthenticationModeAndMethod {
    object None : AuthenticationModeAndMethod()
    object Authenticated : AuthenticationModeAndMethod()
    data class AuthenticationRequired(val method: SettingsAuthenticationMethod, val nrOfFailedAuthentications: Int) :
        AuthenticationModeAndMethod()
}

@Singleton
class AuthenticationUseCase @Inject constructor(
    private val settingsUseCase: SettingsUseCase
) : LifecycleObserver {
    private enum class Lifecycle {
        Started, Running, Stopped
    }

    private val authRequired = MutableStateFlow(false)
    private val lifecycle = MutableStateFlow(Lifecycle.Started)

    val authenticationModeAndMethod =
        combineTransform(lifecycle, authRequired, settingsUseCase.settings) { lifecycle, authRequired, settings ->
            @Suppress("deprecation")
            when (lifecycle) {
                Lifecycle.Started -> {
                    this@AuthenticationUseCase.authRequired.value = when (settings.authenticationMethod) {
                        SettingsAuthenticationMethod.None,
                        SettingsAuthenticationMethod.Unspecified -> false
                        else -> true
                    }
                    this@AuthenticationUseCase.lifecycle.value = Lifecycle.Running
                }
                Lifecycle.Running -> {
                    when (settings.authenticationMethod) {
                        SettingsAuthenticationMethod.None,
                        SettingsAuthenticationMethod.Unspecified ->
                            emit(AuthenticationModeAndMethod.Authenticated)
                        else -> if (authRequired) {
                            emit(
                                AuthenticationModeAndMethod.AuthenticationRequired(
                                    settings.authenticationMethod,
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

    suspend fun isPasswordValid(password: String): Boolean =
        settingsUseCase.isPasswordValid(password)

    fun requireAuthentication() {
        authRequired.value = true
    }

    fun authenticated() {
        authRequired.value = false
    }

    suspend fun incrementNumberOfAuthenticationFailures() =
        settingsUseCase.incrementNumberOfAuthenticationFailures()

    suspend fun resetNumberOfAuthenticationFailures() =
        settingsUseCase.resetNumberOfAuthenticationFailures()

    @OnLifecycleEvent(androidx.lifecycle.Lifecycle.Event.ON_START)
    fun onStartApp() {
        lifecycle.value = Lifecycle.Started
    }

    @OnLifecycleEvent(androidx.lifecycle.Lifecycle.Event.ON_STOP)
    fun onStopApp() {
        lifecycle.value = Lifecycle.Stopped
    }
}
