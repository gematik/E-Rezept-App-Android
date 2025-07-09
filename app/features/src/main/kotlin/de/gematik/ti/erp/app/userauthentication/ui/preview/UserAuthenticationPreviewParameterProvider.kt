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

package de.gematik.ti.erp.app.userauthentication.ui.preview

import androidx.biometric.BiometricPrompt
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import de.gematik.ti.erp.app.settings.model.SettingsData
import de.gematik.ti.erp.app.userauthentication.presentation.AuthenticationStateData
import de.gematik.ti.erp.app.utils.uistate.UiState

data class UserAuthenticationPreviewParameter(
    val name: String,
    val authenticationState: AuthenticationStateData.AuthenticationState,
    val uiState: UiState<AuthenticationStateData.AuthenticationState>,
    val timeout: Long,
    val showPasswordLogin: Boolean,
    val enteredPasswordError: Boolean,
    val enteredPassword: String
)

val AuthMethodBiometry = SettingsData.Authentication(
    deviceSecurity = true,
    failedAuthenticationAttempts = 0,
    password = null,
    authenticationTimeOutSystemUptime = null
)
val AuthMethodPassword = SettingsData.Authentication(
    deviceSecurity = false,
    failedAuthenticationAttempts = 0,
    password = SettingsData.Authentication.Password("password"),
    authenticationTimeOutSystemUptime = null
)
val AuthMethodBoth = SettingsData.Authentication(
    deviceSecurity = true,
    failedAuthenticationAttempts = 0,
    password = SettingsData.Authentication.Password("password"),
    authenticationTimeOutSystemUptime = null
)
val AuthMethodBiometryError = SettingsData.Authentication(
    deviceSecurity = true,
    failedAuthenticationAttempts = 5,
    password = null,
    authenticationTimeOutSystemUptime = null
)
val AuthMethodPasswordError = SettingsData.Authentication(
    deviceSecurity = false,
    failedAuthenticationAttempts = 40,
    password = SettingsData.Authentication.Password("password"),
    authenticationTimeOutSystemUptime = 40L
)
val AuthMethodBothError = SettingsData.Authentication(
    deviceSecurity = true,
    failedAuthenticationAttempts = 5,
    password = SettingsData.Authentication.Password("password"),
    authenticationTimeOutSystemUptime = 30L
)
val authenticationErrorLockOut = AuthenticationStateData.AuthenticationError(
    "biometric error lock out",
    BiometricPrompt.ERROR_LOCKOUT
)
val authenticationErrorLockOutPermanent = AuthenticationStateData.AuthenticationError(
    "biometric error lock out permanent",
    BiometricPrompt.ERROR_LOCKOUT_PERMANENT
)

class UserAuthenticationPreviewParameterProvider : PreviewParameterProvider<UserAuthenticationPreviewParameter> {
    override val values: Sequence<UserAuthenticationPreviewParameter>
        get() = sequenceOf(
            UserAuthenticationPreviewParameter(
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
            UserAuthenticationPreviewParameter(
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
            UserAuthenticationPreviewParameter(
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
            UserAuthenticationPreviewParameter(
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
            UserAuthenticationPreviewParameter(
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
            UserAuthenticationPreviewParameter(
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
