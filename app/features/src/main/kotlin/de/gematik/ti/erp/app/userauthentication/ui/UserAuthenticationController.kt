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
import de.gematik.ti.erp.app.userauthentication.observer.AuthenticationModeAndMethod
import de.gematik.ti.erp.app.userauthentication.observer.InactivityTimeoutObserver
import de.gematik.ti.erp.app.userauthentication.ui.AuthenticationStateData.AuthenticationState
import kotlinx.coroutines.flow.map
import org.kodein.di.compose.rememberInstance

class AuthenticationController(
    private val inactivityTimeoutObserver: InactivityTimeoutObserver
) {
    private val authenticationFlow =
        inactivityTimeoutObserver.authenticationModeAndMethod
            .map {
                when (it) {
                    AuthenticationModeAndMethod.None,
                    AuthenticationModeAndMethod.Authenticated -> AuthenticationState(
                        SettingsData.AuthenticationMode.Unspecified,
                        0
                    )

                    is AuthenticationModeAndMethod.AuthenticationRequired -> AuthenticationState(
                        it.method,
                        it.nrOfFailedAuthentications
                    )
                }
            }

    val authenticationState
        @Composable
        get() = authenticationFlow.collectAsState(AuthenticationStateData.defaultAuthenticationState)

    suspend fun isPasswordValid(password: String): Boolean =
        inactivityTimeoutObserver.isPasswordValid(password)

    suspend fun onAuthenticated() {
        inactivityTimeoutObserver.resetNumberOfAuthenticationFailures()
        inactivityTimeoutObserver.authenticated()
    }

    suspend fun onFailedAuthentication() = inactivityTimeoutObserver.incrementNumberOfAuthenticationFailures()
}

@Composable
fun rememberAuthenticationController(): AuthenticationController {
    val inactivityTimeoutObserver by rememberInstance<InactivityTimeoutObserver>()
    return remember { AuthenticationController(inactivityTimeoutObserver) }
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
