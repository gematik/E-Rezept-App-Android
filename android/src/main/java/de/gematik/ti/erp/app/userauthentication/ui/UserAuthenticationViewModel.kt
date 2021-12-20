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

import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import de.gematik.ti.erp.app.core.BaseViewModel
import de.gematik.ti.erp.app.db.entities.SettingsAuthenticationMethod
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import javax.inject.Inject

data class UserAuthenticationScreenState(
    val authenticationMethod: SettingsAuthenticationMethod,
    val nrOfAuthFailures: Int
)

@HiltViewModel
class UserAuthenticationViewModel @Inject constructor(
    private val authUseCase: AuthenticationUseCase
) : BaseViewModel() {
    var defaultState = UserAuthenticationScreenState(
        authenticationMethod = SettingsAuthenticationMethod.Unspecified,
        nrOfAuthFailures = 0
    )

    fun screenState() =
        authUseCase.authenticationModeAndMethod.map {
            when (it) {
                AuthenticationModeAndMethod.None,
                AuthenticationModeAndMethod.Authenticated -> UserAuthenticationScreenState(
                    SettingsAuthenticationMethod.Unspecified,
                    0
                )
                is AuthenticationModeAndMethod.AuthenticationRequired -> UserAuthenticationScreenState(
                    it.method,
                    it.nrOfFailedAuthentications
                )
            }
        }

    suspend fun isPasswordValid(password: String): Boolean =
        authUseCase.isPasswordValid(password)

    fun onAuthenticated() {
        viewModelScope.launch {
            authUseCase.resetNumberOfAuthenticationFailures()
        }
        authUseCase.authenticated()
    }

    fun onFailedAuthentication() =
        viewModelScope.launch {
            authUseCase.incrementNumberOfAuthenticationFailures()
        }
}
