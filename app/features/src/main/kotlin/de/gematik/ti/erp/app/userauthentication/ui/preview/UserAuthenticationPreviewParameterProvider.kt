/*
 * Copyright 2024, gematik GmbH
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
 * In case of changes by gematik find details in the "Readme" file.
 *
 * See the Licence for the specific language governing permissions and limitations under the Licence.
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
    val uiState: UiState<AuthenticationStateData.AuthenticationState>
)

val AuthMethodBiometry = SettingsData.Authentication(
    deviceSecurity = true,
    failedAuthenticationAttempts = 0,
    password = null
)
val AuthMethodPassword = SettingsData.Authentication(
    deviceSecurity = false,
    failedAuthenticationAttempts = 0,
    password = SettingsData.Authentication.Password("password")
)
val AuthMethodBoth = SettingsData.Authentication(
    deviceSecurity = true,
    failedAuthenticationAttempts = 0,
    password = SettingsData.Authentication.Password("password")
)
val AuthMethodBiometryError = SettingsData.Authentication(
    deviceSecurity = true,
    failedAuthenticationAttempts = 5,
    password = null
)
val AuthMethodPasswordError = SettingsData.Authentication(
    deviceSecurity = false,
    failedAuthenticationAttempts = 5,
    password = SettingsData.Authentication.Password("password")
)
val AuthMethodBothError = SettingsData.Authentication(
    deviceSecurity = true,
    failedAuthenticationAttempts = 5,
    password = SettingsData.Authentication.Password("password")
)
val biometricErrorLockOut = AuthenticationStateData.BiometricError(
    "biometric error lock out",
    BiometricPrompt.ERROR_LOCKOUT
)
val biometricErrorLockOutPermanent = AuthenticationStateData.BiometricError(
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
                uiState = UiState.Empty()
            ),
            UserAuthenticationPreviewParameter(
                name = "UiStateNoErrorPassword",
                authenticationState = AuthenticationStateData.AuthenticationState(
                    authentication = AuthMethodPassword
                ),
                uiState = UiState.Empty()
            ),
            UserAuthenticationPreviewParameter(
                name = "UiStateNoErrorBoth",
                authenticationState = AuthenticationStateData.AuthenticationState(
                    authentication = AuthMethodBoth
                ),
                uiState = UiState.Empty()
            ),
            UserAuthenticationPreviewParameter(
                name = "UiStateErrorBiometryLockOut",
                authenticationState = AuthenticationStateData.AuthenticationState(
                    authentication = AuthMethodBiometryError,
                    biometricError = biometricErrorLockOut
                ),
                uiState = UiState.Error(
                    AuthenticationStateData.AuthenticationState(
                        authentication = AuthMethodBiometryError,
                        biometricError = biometricErrorLockOut
                    )
                )
            ),
            UserAuthenticationPreviewParameter(
                name = "UiStateErrorBothLockOutPermanent",
                authenticationState = AuthenticationStateData.AuthenticationState(
                    authentication = AuthMethodBothError,
                    biometricError = biometricErrorLockOutPermanent
                ),
                uiState = UiState.Error(
                    AuthenticationStateData.AuthenticationState(
                        authentication = AuthMethodBothError,
                        biometricError = biometricErrorLockOutPermanent
                    )
                )
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
                )
            )
        )
}
