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

package de.gematik.ti.erp.app.mainscreen.presentation

import androidx.compose.runtime.Stable
import de.gematik.ti.erp.app.core.GidResultIntent
import de.gematik.ti.erp.app.idp.model.error.DecryptAccessTokenError
import de.gematik.ti.erp.app.idp.model.error.SingleSignOnTokenError
import de.gematik.ti.erp.app.idp.model.error.UniversalLinkError
import de.gematik.ti.erp.app.idp.usecase.AuthenticateWithExternalHealthInsuranceAppUseCase
import de.gematik.ti.erp.app.mainscreen.presentation.AuthenticationHandlerState.Idle
import io.github.aakira.napier.Napier
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import org.kodein.di.LazyDelegate
import java.net.URI

@Stable
sealed interface AuthenticationHandlerState {
    data object Idle : AuthenticationHandlerState
    data object Loading : AuthenticationHandlerState
    data object Success : AuthenticationHandlerState
    data object SsoTokenNotSaved : AuthenticationHandlerState
    data object AuthTokenNotSaved : AuthenticationHandlerState
    data object Failure : AuthenticationHandlerState
}

@Stable
class ExternalAuthenticationHandler(
    idpUseCase: LazyDelegate<AuthenticateWithExternalHealthInsuranceAppUseCase>
) {
    private val idpUseCase by idpUseCase

    private val handlerState: MutableStateFlow<AuthenticationHandlerState> = MutableStateFlow(Idle)

    val state: StateFlow<AuthenticationHandlerState> = handlerState

    /**
     * Handles an incoming intent. Updates the [handlerState] to Success if it can be handled.
     */
    suspend fun handle(intent: GidResultIntent) =
        try {
            handlerState.value = AuthenticationHandlerState.Loading
            Napier.i("Authenticate with GID ...")
            idpUseCase.invoke(URI(intent.uriData))
            handlerState.value = AuthenticationHandlerState.Success
            intent.resultChannel.send(intent.uriData) // the result is sent here, after the idpUseCase saves the token
            Napier.i("... authenticated")
        } catch (e: Throwable) {
            when (e) {
                is DecryptAccessTokenError -> handlerState.value = AuthenticationHandlerState.AuthTokenNotSaved
                is SingleSignOnTokenError -> handlerState.value = AuthenticationHandlerState.SsoTokenNotSaved
                is UniversalLinkError -> handlerState.value = AuthenticationHandlerState.Failure
                else -> handlerState.value = AuthenticationHandlerState.Failure
            }
        }

    fun resetState() {
        handlerState.value = Idle
    }
}
