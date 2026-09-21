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

package de.gematik.ti.erp.app.appauthentication.ui.preview

import androidx.biometric.BiometricPrompt
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import de.gematik.ti.erp.app.appauthentication.model.AppAuthenticationErpModel
import de.gematik.ti.erp.app.appauthentication.model.AppAuthenticationFailureErpModel
import de.gematik.ti.erp.app.appauthentication.model.AppAuthenticationMethodErpModel
import de.gematik.ti.erp.app.appauthentication.model.AppAuthenticationPasswordErpModel
import de.gematik.ti.erp.app.appauthentication.presentation.AuthenticationStateData
import de.gematik.ti.erp.app.utils.uistate.UiState

data class AppAuthenticationPreviewParameter(
    val name: String,
    val authenticationState: AuthenticationStateData.AuthenticationState,
    val uiState: UiState<AuthenticationStateData.AuthenticationState>,
    val timeout: Long,
    val showPasswordLogin: Boolean,
    val enteredPasswordError: Boolean,
    val enteredPassword: String
)

val AuthMethodBiometry = AppAuthenticationErpModel(
    authenticationMethod =
    AppAuthenticationMethodErpModel.DeviceSecurity,
    authenticationFailure = AppAuthenticationFailureErpModel(
        failedAttempts = 0,
        timeOutSystemUptime = null
    )
)
val AuthMethodPassword = AppAuthenticationErpModel(
    authenticationMethod =
    AppAuthenticationMethodErpModel.Password(
        AppAuthenticationPasswordErpModel.fromPassword("password")
    ),
    authenticationFailure = AppAuthenticationFailureErpModel(
        failedAttempts = 0,
        timeOutSystemUptime = null
    )
)
val AuthMethodBoth = AppAuthenticationErpModel(
    authenticationMethod =
    AppAuthenticationMethodErpModel.Both(
        AppAuthenticationPasswordErpModel.fromPassword("password")
    ),
    authenticationFailure = AppAuthenticationFailureErpModel(
        failedAttempts = 0,
        timeOutSystemUptime = null
    )
)
val AuthMethodBiometryError = AppAuthenticationErpModel(
    authenticationMethod =
    AppAuthenticationMethodErpModel.DeviceSecurity,
    authenticationFailure = AppAuthenticationFailureErpModel(
        failedAttempts = 5,
        timeOutSystemUptime = null
    )
)
val AuthMethodPasswordError = AppAuthenticationErpModel(
    authenticationMethod =
    AppAuthenticationMethodErpModel.Password(
        AppAuthenticationPasswordErpModel.fromPassword("password")
    ),
    authenticationFailure = AppAuthenticationFailureErpModel(
        failedAttempts = 40,
        timeOutSystemUptime = 40
    )
)
val AuthMethodBothError = AppAuthenticationErpModel(
    authenticationMethod =
    AppAuthenticationMethodErpModel.Both(
        AppAuthenticationPasswordErpModel.fromPassword("password")
    ),
    authenticationFailure = AppAuthenticationFailureErpModel(
        failedAttempts = 5,
        timeOutSystemUptime = 30
    )
)
val authenticationErrorLockOut = AuthenticationStateData.AuthenticationError(
    "biometric error lock out",
    BiometricPrompt.ERROR_LOCKOUT
)
val authenticationErrorLockOutPermanent = AuthenticationStateData.AuthenticationError(
    "biometric error lock out permanent",
    BiometricPrompt.ERROR_LOCKOUT_PERMANENT
)

class AppAuthenticationPreviewParameterProvider : PreviewParameterProvider<AppAuthenticationPreviewParameter> {
    override val values: Sequence<AppAuthenticationPreviewParameter>
        get() = sequenceOf(
            AppAuthenticationPreviewParameter(
                name = "UiStateNoErrorBiometry",
                authenticationState = AuthenticationStateData.AuthenticationState(
                    authentication = AuthMethodBiometry
                ),
                uiState = UiState.Empty(),
                timeout = 0L,
                enteredPassword = "",
                showPasswordLogin = false,
                enteredPasswordError = false
            ),
            AppAuthenticationPreviewParameter(
                name = "UiStateNoErrorPassword",
                authenticationState = AuthenticationStateData.AuthenticationState(
                    authentication = AuthMethodPassword
                ),
                uiState = UiState.Empty(),
                timeout = 0L,
                enteredPassword = "",
                showPasswordLogin = false,
                enteredPasswordError = false
            ),
            AppAuthenticationPreviewParameter(
                name = "UiStateNoErrorBoth",
                authenticationState = AuthenticationStateData.AuthenticationState(
                    authentication = AuthMethodBoth
                ),
                uiState = UiState.Empty(),
                timeout = 0L,
                enteredPassword = "",
                showPasswordLogin = false,
                enteredPasswordError = false
            ),
            AppAuthenticationPreviewParameter(
                name = "UiStateErrorBiometryLockOut",
                authenticationState = AuthenticationStateData.AuthenticationState(
                    authentication = AuthMethodBiometryError,
                    authenticationError = authenticationErrorLockOut
                ),
                uiState = UiState.Error(
                    AuthenticationStateData.AuthenticationState(
                        authentication = AuthMethodBiometryError,
                        authenticationError = authenticationErrorLockOut
                    )
                ),
                timeout = 0L,
                enteredPassword = "",
                showPasswordLogin = false,
                enteredPasswordError = false
            ),
            AppAuthenticationPreviewParameter(
                name = "UiStateErrorBothLockOutPassword",
                authenticationState = AuthenticationStateData.AuthenticationState(
                    authentication = AuthMethodBothError,
                    authenticationError = authenticationErrorLockOutPermanent
                ),
                uiState = UiState.Error(
                    AuthenticationStateData.AuthenticationState(
                        authentication = AuthMethodBothError,
                        authenticationError = authenticationErrorLockOutPermanent
                    )
                ),
                timeout = 40L,
                enteredPassword = "greg",
                showPasswordLogin = true,
                enteredPasswordError = true
            ),
            AppAuthenticationPreviewParameter(
                name = "UiStateErrorPassword",
                authenticationState = AuthenticationStateData.AuthenticationState(
                    authentication = AuthMethodPasswordError
                ),
                uiState = UiState.Error(
                    AuthenticationStateData.AuthenticationState(
                        authentication = AuthMethodPasswordError
                    )
                ),
                timeout = 40L,
                enteredPassword = "jzt",
                showPasswordLogin = true,
                enteredPasswordError = true
            )
        )
}
