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

package de.gematik.ti.erp.app.userauthentication.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import de.gematik.ti.erp.app.settings.model.SettingsData
import kotlinx.coroutines.flow.map
import org.kodein.di.compose.rememberInstance

class AuthenticationController(
    private val authUseCase: AuthenticationUseCase
) {
    private val authenticationFlow =
        authUseCase.authenticationModeAndMethod.map {
            when (it) {
                AuthenticationModeAndMethod.None,
                AuthenticationModeAndMethod.Authenticated -> AuthenticationStateData.AuthenticationState(
                    SettingsData.AuthenticationMode.Unspecified,
                    0
                )
                is AuthenticationModeAndMethod.AuthenticationRequired -> AuthenticationStateData.AuthenticationState(
                    it.method,
                    it.nrOfFailedAuthentications
                )
            }
        }

    val authenticationState
        @Composable
        get() = authenticationFlow.collectAsState(AuthenticationStateData.defaultAuthenticationState)

    suspend fun isPasswordValid(password: String): Boolean =
        authUseCase.isPasswordValid(password)

    suspend fun onAuthenticated() {
        authUseCase.resetNumberOfAuthenticationFailures()
        authUseCase.authenticated()
    }

    suspend fun onFailedAuthentication() = authUseCase.incrementNumberOfAuthenticationFailures()
}

@Composable
fun rememberAuthenticationController(): AuthenticationController {
    val authenticationUseCase by rememberInstance<AuthenticationUseCase>()
    return remember { AuthenticationController(authenticationUseCase) }
}

object AuthenticationStateData {
    @Immutable
    data class AuthenticationState(
        val authenticationMethod: SettingsData.AuthenticationMode,
        val nrOfAuthFailures: Int
    )

    val defaultAuthenticationState = AuthenticationState(
        authenticationMethod = SettingsData.AuthenticationMode.Unspecified,
        nrOfAuthFailures = 0
    )
}
