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

package de.gematik.ti.erp.app.mainscreen.presentation

import androidx.compose.runtime.Stable
import de.gematik.ti.erp.app.Requirement
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
    @Requirement(
        "O.Plat_10#2",
        sourceSpecification = "BSI-eRp-ePA",
        rationale = "handle incoming intent"
    )
    suspend fun handle(value: String) =
        try {
            handlerState.value = AuthenticationHandlerState.Loading
            Napier.d("Authenticate external ...")
            idpUseCase.invoke(URI(value))
            handlerState.value = AuthenticationHandlerState.Success
            Napier.d("... authenticated")
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
