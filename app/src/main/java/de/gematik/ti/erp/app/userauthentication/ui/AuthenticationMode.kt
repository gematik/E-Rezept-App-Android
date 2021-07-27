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

import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.OnLifecycleEvent
import de.gematik.ti.erp.app.db.entities.SettingsAuthenticationMethod
import de.gematik.ti.erp.app.settings.usecase.SettingsUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combineTransform
import javax.inject.Inject
import javax.inject.Singleton

sealed class AuthenticationModeAndMethod {
    object Authenticated : AuthenticationModeAndMethod()
    data class AuthenticationRequired(val method: SettingsAuthenticationMethod) :
        AuthenticationModeAndMethod()
}

@Singleton
class AuthenticationMode @Inject constructor(
    settingsUseCase: SettingsUseCase
) : LifecycleObserver {
    private val authRequired = MutableStateFlow(false)
    private val isRestarted = MutableStateFlow(false)

    val authenticationModeAndMethod =
        combineTransform(isRestarted, authRequired, settingsUseCase.settings) { isRestarted, authRequired, settings ->
            if (isRestarted) {
                this@AuthenticationMode.authRequired.value = when (settings.authenticationMethod) {
                    SettingsAuthenticationMethod.None,
                    SettingsAuthenticationMethod.Unspecified -> false
                    else -> true
                }
                this@AuthenticationMode.isRestarted.value = false
            } else {
                when (settings.authenticationMethod) {
                    SettingsAuthenticationMethod.None,
                    SettingsAuthenticationMethod.Unspecified ->
                        emit(AuthenticationModeAndMethod.Authenticated)
                    else -> if (authRequired) {
                        emit(AuthenticationModeAndMethod.AuthenticationRequired(settings.authenticationMethod))
                    } else {
                        emit(AuthenticationModeAndMethod.Authenticated)
                    }
                }
            }
        }

    fun requireAuthentication() {
        authRequired.value = true
    }

    fun authenticated() {
        authRequired.value = false
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_START)
    fun onStartApp() {
        isRestarted.value = true
    }
}
