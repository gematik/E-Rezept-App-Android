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

package de.gematik.ti.erp.app.userauthentication.presentation

import android.os.SystemClock
import androidx.appcompat.app.AppCompatActivity
import androidx.biometric.BiometricPrompt
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.remember
import androidx.compose.ui.res.stringResource
import de.gematik.ti.erp.app.Requirement
import de.gematik.ti.erp.app.authentication.observer.BiometricPromptBuilder
import de.gematik.ti.erp.app.base.Controller
import de.gematik.ti.erp.app.core.LocalActivity
import de.gematik.ti.erp.app.core.R
import de.gematik.ti.erp.app.settings.model.SettingsData
import de.gematik.ti.erp.app.userauthentication.observer.AuthenticationModeAndMethod
import de.gematik.ti.erp.app.userauthentication.observer.InactivityTimeoutObserver
import de.gematik.ti.erp.app.userauthentication.presentation.AuthenticationStateData.AuthenticationState
import de.gematik.ti.erp.app.userauthentication.usecase.ResetAuthenticationTimeOutSystemUptimeUseCase
import de.gematik.ti.erp.app.userauthentication.usecase.SetAuthenticationTimeOutSystemUptimeUseCase
import de.gematik.ti.erp.app.utils.uistate.UiState
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.kodein.di.compose.rememberInstance
import kotlin.math.max
import kotlin.math.min

private const val PASSWORD_TRIES_TILL_TIMEOUT = 5
private const val PASSWORD_TIMEOUT_SECONDS = 5
private const val MAX_FIBONACCI_FACTOR = 8L // 8x5 = 40 sec max timeout
private const val MILLISECONDS_SECONDS_FACTOR = 1000L

class UserAuthenticationController(
    private val inactivityTimeoutObserver: InactivityTimeoutObserver,
    private val setAuthenticationTimeOutSystemUptimeUseCase: SetAuthenticationTimeOutSystemUptimeUseCase,
    private val resetAuthenticationTimeOutSystemUptimeUseCase: ResetAuthenticationTimeOutSystemUptimeUseCase,
    private val biometricPromptBuilder: BiometricPromptBuilder,
    private val promptInfo: BiometricPrompt.PromptInfo
) : Controller() {
    private val _authentication = MutableStateFlow(AuthenticationStateData.defaultAuthenticationState)
    private val _showPasswordLogin = MutableStateFlow(false)
    private val _enteredPassword: MutableStateFlow<String> = MutableStateFlow("")
    private val _enteredPasswordError = MutableStateFlow(false)

    val authenticationState: StateFlow<AuthenticationState> = _authentication
    val showPasswordLogin: StateFlow<Boolean> = _showPasswordLogin
    val enteredPassword: StateFlow<String> = _enteredPassword
    val enteredPasswordError: StateFlow<Boolean> = _enteredPasswordError
    val uiState = _authentication.map {
        when {
            it.authentication.failedAuthenticationAttempts == 0 -> UiState.Empty()
            it.authenticationError != null -> UiState.Error(it)
            else -> {
                UiState.Data(it)
            }
        }
    }

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
                                failedAuthenticationAttempts = 0,
                                authenticationTimeOutSystemUptime = null
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

    // general authentication
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
        "O.Auth_7#3",
        sourceSpecification = "BSI-eRp-ePA",
        rationale = "Increments the number of authentication failures when the user fails to authenticate."
    )
    private fun onFailedAuthentication() {
        controllerScope.launch {
            inactivityTimeoutObserver.incrementNumberOfAuthenticationFailures()
        }
    }

    // device specific authentication
    fun onAuthenticateWithDeviceSecurity(
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
                onFailedAuthentication()
                _authentication.update {
                    it.copy(
                        authenticationError = AuthenticationStateData.AuthenticationError(
                            name = errorMessage,
                            code = errorCode
                        )
                    )
                }
            }
        )
        prompt.authenticate(promptInfo)
    }

    // password authentication
    fun onChangeEnteredPassword(password: String) {
        _enteredPassword.value = password
    }

    fun onShowPasswordLogin() {
        _showPasswordLogin.update { true }
    }

    fun onHidePasswordLogin() {
        _showPasswordLogin.update { false }
    }

    fun onRemovePasswordError() {
        _enteredPasswordError.update { false }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    val authenticationTimeOut =
        _authentication.flatMapLatest {
            val timeout = calculateAuthenticationTimeOut(it)
            var remainingTimeout = timeout
            flow {
                while (remainingTimeout > 0L) {
                    emit(remainingTimeout--)
                    delay(MILLISECONDS_SECONDS_FACTOR)
                    if (remainingTimeout == 0L) {
                        resetAuthenticationTimeOutSystemUptimeUseCase.invoke()
                    }
                }
                emit(0L)
            }
        }.stateIn(scope = controllerScope, started = SharingStarted.Eagerly, initialValue = 0L)

    fun onAuthenticateWithPassword(
        onSuccessLeaveAuthScreen: () -> Unit
    ) {
        controllerScope.launch {
            @Requirement(
                "O.Pass_4#1",
                sourceSpecification = "BSI-eRp-ePA",
                rationale = "If the password is not correct, the function onFailedAuthentication is called."
            )
            when {
                inactivityTimeoutObserver.isPasswordValid(_enteredPassword.value) -> {
                    onSuccessfulAuthentication(
                        onSuccessLeaveAuthScreen = onSuccessLeaveAuthScreen
                    )
                }

                else -> {
                    onFailedAuthentication()
                    _enteredPasswordError.update { true }
                    if (checkTriggerAuthenticationTimeOut()) {
                        setAuthenticationTimeOutSystemUptimeUseCase.invoke(SystemClock.elapsedRealtime())
                    }
                }
            }
        }
    }

    private fun checkTriggerAuthenticationTimeOut(): Boolean =
        (_authentication.value.authentication.failedAuthenticationAttempts + 1) > PASSWORD_TRIES_TILL_TIMEOUT // db update takes too long therefore + 1

    suspend fun calculateAuthenticationTimeOut(it: AuthenticationState): Long =
        it.authentication.authenticationTimeOutSystemUptime?.let { savedSystemUptime ->
            val currentSystemUptime = SystemClock.elapsedRealtime()
            val fibonacciBasedTimeOutModifier = min(
                fibonacci().elementAt(
                    max(it.authentication.failedAuthenticationAttempts - PASSWORD_TRIES_TILL_TIMEOUT, 0)
                ),
                MAX_FIBONACCI_FACTOR
            )
            val totalTimeout = PASSWORD_TIMEOUT_SECONDS * MILLISECONDS_SECONDS_FACTOR * fibonacciBasedTimeOutModifier
            val currentTimeOut = ((savedSystemUptime + totalTimeout) - currentSystemUptime) / MILLISECONDS_SECONDS_FACTOR
            when {
                savedSystemUptime > currentSystemUptime -> {
                    setAuthenticationTimeOutSystemUptimeUseCase.invoke(currentSystemUptime)
                }

                currentTimeOut <= 0 -> {
                    resetAuthenticationTimeOutSystemUptimeUseCase.invoke()
                }

                else -> {}
            }
            currentTimeOut
        } ?: 0

    private fun fibonacci(): Sequence<Long> {
        return generateSequence(Pair(0L, 1L)) { Pair(it.second, it.first + it.second) }
            .map { it.first }
    }
}

@Composable
fun rememberAuthenticationController(): UserAuthenticationController {
    val inactivityTimeoutObserver by rememberInstance<InactivityTimeoutObserver>()
    val resetAuthenticationTimeOutSystemUptimeUseCase by rememberInstance<ResetAuthenticationTimeOutSystemUptimeUseCase>()
    val setAuthenticationTimeOutSystemUptimeUseCase by rememberInstance<SetAuthenticationTimeOutSystemUptimeUseCase>()
    val activity = LocalActivity.current
    val biometricPromptBuilder = remember { BiometricPromptBuilder(activity as AppCompatActivity) }
    val promptInfo = biometricPromptBuilder.buildPromptInfoWithAllAuthenticatorsAvailable(
        title = stringResource(R.string.auth_prompt_headline),
        description = stringResource(R.string.alternate_auth_info)
    )

    return remember {
        UserAuthenticationController(
            inactivityTimeoutObserver = inactivityTimeoutObserver,
            resetAuthenticationTimeOutSystemUptimeUseCase = resetAuthenticationTimeOutSystemUptimeUseCase,
            setAuthenticationTimeOutSystemUptimeUseCase = setAuthenticationTimeOutSystemUptimeUseCase,
            biometricPromptBuilder = biometricPromptBuilder,
            promptInfo = promptInfo
        )
    }
}

object AuthenticationStateData {
    @Immutable
    data class AuthenticationState(
        val authentication: SettingsData.Authentication,
        val authenticationError: AuthenticationError? = null
    ) : Throwable()

    val defaultAuthenticationState = AuthenticationState(
        authentication = SettingsData.Authentication(
            password = null,
            deviceSecurity = false,
            failedAuthenticationAttempts = 0,
            authenticationTimeOutSystemUptime = null
        ),
        authenticationError = null
    )

    data class AuthenticationError(
        val name: String? = null,
        val code: Int? = null
    )
}
