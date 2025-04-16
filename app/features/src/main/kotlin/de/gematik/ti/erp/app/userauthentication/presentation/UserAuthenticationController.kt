/*
 * Copyright 2025, gematik GmbH
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

package de.gematik.ti.erp.app.userauthentication.presentation

import androidx.appcompat.app.AppCompatActivity
import androidx.biometric.BiometricPrompt
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.remember
import androidx.compose.ui.res.stringResource
import de.gematik.ti.erp.app.Requirement
import de.gematik.ti.erp.app.base.Controller
import de.gematik.ti.erp.app.core.LocalActivity
import de.gematik.ti.erp.app.features.R
import de.gematik.ti.erp.app.settings.model.SettingsData
import de.gematik.ti.erp.app.userauthentication.observer.AuthenticationModeAndMethod
import de.gematik.ti.erp.app.userauthentication.observer.BiometricPromptBuilder
import de.gematik.ti.erp.app.userauthentication.observer.InactivityTimeoutObserver
import de.gematik.ti.erp.app.userauthentication.presentation.AuthenticationStateData.AuthenticationState
import de.gematik.ti.erp.app.utils.compose.ComposableEvent
import de.gematik.ti.erp.app.utils.uistate.UiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.kodein.di.compose.rememberInstance

class AuthenticationController(
    private val inactivityTimeoutObserver: InactivityTimeoutObserver,
    private val biometricPromptBuilder: BiometricPromptBuilder,
    private val promptInfo: BiometricPrompt.PromptInfo
) : Controller() {
    val authenticationWithPasswordEvent = ComposableEvent<Unit>()

    private val _authentication = MutableStateFlow(AuthenticationStateData.defaultAuthenticationState)

    init {
        controllerScope.launch {
            inactivityTimeoutObserver.authenticationModeAndMethod
                .map {
                    when (it) {
                        is AuthenticationModeAndMethod.AuthenticationRequired -> AuthenticationState(
                            it.authentication
                        )
                        else -> AuthenticationState( // only reached in failure state
                            SettingsData.Authentication(
                                password = null,
                                deviceSecurity = false,
                                failedAuthenticationAttempts = 0
                            )
                        )
                    }
                }
                .collect { authenticationState ->
                    _authentication.update {
                        it.copy(
                            authentication = authenticationState.authentication
                        )
                    }
                }
        }
    }

    val authenticationState: StateFlow<AuthenticationState> = _authentication

    val uiState = _authentication.map {
        when {
            it.authentication.failedAuthenticationAttempts == 0 -> UiState.Empty()
            it.biometricError != null -> UiState.Error(it)
            else -> {
                UiState.Data(it)
            }
        }
    }

    private val _password: MutableStateFlow<String> = MutableStateFlow("")

    fun onChangePassword(password: String) {
        _password.value = password
    }

    fun onClickAuthenticate(
        onSuccessLeaveAuthScreen: () -> Unit
    ) {
        when {
            authenticationState.value.authentication.methodIsPassword -> {
                authenticationWithPasswordEvent.trigger(Unit)
            }
            else -> {
                onAuthenticateWithDeviceSecurity(onSuccessLeaveAuthScreen)
            }
        }
    }

    fun onAuthenticateWithPassword(
        onSuccessLeaveAuthScreen: () -> Unit
    ) {
        controllerScope.launch {
            @Requirement(
                "O.Pass_4#1",
                sourceSpecification = "BSI-eRp-ePA",
                rationale = "If the password is not correct, the function onFailedAuthentication is called."
            )
            if (inactivityTimeoutObserver.isPasswordValid(_password.value)) {
                onSuccessfulAuthentication(
                    onSuccessLeaveAuthScreen = onSuccessLeaveAuthScreen
                )
            } else {
                onFailedAuthentication()
            }
        }
    }

    private fun onAuthenticateWithDeviceSecurity(
        onSuccessLeaveAuthScreen: () -> Unit
    ) {
        val prompt = biometricPromptBuilder.buildBiometricPrompt(
            onSuccess = {
                onSuccessfulAuthentication(
                    onSuccessLeaveAuthScreen = onSuccessLeaveAuthScreen
                )
            },
            onFailure = {
                onFailedAuthentication()
            },
            onError = { errorMessage, errorCode ->
                _authentication.update {
                    it.copy(
                        biometricError = AuthenticationStateData.BiometricError(
                            name = errorMessage,
                            code = errorCode
                        )
                    )
                }
            }
        )
        prompt.authenticate(promptInfo)
    }

    fun onSuccessfulAuthentication(
        onSuccessLeaveAuthScreen: () -> Unit
    ) {
        controllerScope.launch {
            inactivityTimeoutObserver.resetNumberOfAuthenticationFailures()
            inactivityTimeoutObserver.authenticated()
        }
        onSuccessLeaveAuthScreen()
    }

    @Requirement(
        "O.Pass_4#2",
        sourceSpecification = "BSI-eRp-ePA",
        rationale = "Increments the number of authentication failures when the user fails to authenticate."
    )
    private fun onFailedAuthentication() {
        controllerScope.launch {
            inactivityTimeoutObserver.incrementNumberOfAuthenticationFailures()
        }
    }
}

@Composable
fun rememberAuthenticationController(): AuthenticationController {
    val inactivityTimeoutObserver by rememberInstance<InactivityTimeoutObserver>()
    val activity = LocalActivity.current
    val biometricPromptBuilder = remember { BiometricPromptBuilder(activity as AppCompatActivity) }
    val promptInfo = biometricPromptBuilder.buildPromptInfoWithAllAuthenticatorsAvailable(
        title = stringResource(R.string.auth_prompt_headline),
        description = stringResource(R.string.alternate_auth_info)
    )

    return remember {
        AuthenticationController(
            inactivityTimeoutObserver = inactivityTimeoutObserver,
            biometricPromptBuilder = biometricPromptBuilder,
            promptInfo = promptInfo
        )
    }
}

object AuthenticationStateData {
    @Immutable
    data class AuthenticationState(
        val authentication: SettingsData.Authentication,
        val biometricError: BiometricError? = null
    ) : Throwable()

    val defaultAuthenticationState = AuthenticationState(
        authentication = SettingsData.Authentication(
            password = null,
            deviceSecurity = false,
            failedAuthenticationAttempts = 0
        ),
        biometricError = null
    )

    data class BiometricError(
        val name: String? = null,
        val code: Int? = null
    )
}
