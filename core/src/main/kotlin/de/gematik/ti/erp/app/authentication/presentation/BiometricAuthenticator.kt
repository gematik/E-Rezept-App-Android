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

package de.gematik.ti.erp.app.authentication.presentation

import android.annotation.SuppressLint
import androidx.appcompat.app.AppCompatActivity
import androidx.biometric.BiometricPrompt
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import de.gematik.ti.erp.app.authentication.model.AuthenticationResult
import de.gematik.ti.erp.app.authentication.model.AuthenticationResult.BiometricResult
import de.gematik.ti.erp.app.authentication.model.AuthenticationResult.BiometricResult.BiometricSuccess
import de.gematik.ti.erp.app.authentication.model.AuthenticationResult.IdpCommunicationError.AuthenticationNotSuccessful
import de.gematik.ti.erp.app.authentication.model.AuthenticationResult.IdpCommunicationError.CommunicationFailure
import de.gematik.ti.erp.app.authentication.model.AuthenticationResult.IdpCommunicationError.IllegalStateError
import de.gematik.ti.erp.app.authentication.model.AuthenticationResult.IdpCommunicationError.InvalidCertificate
import de.gematik.ti.erp.app.authentication.model.AuthenticationResult.IdpCommunicationError.InvalidOCSP
import de.gematik.ti.erp.app.authentication.model.AuthenticationResult.IdpCommunicationError.SecureElementFailure
import de.gematik.ti.erp.app.authentication.model.AuthenticationResult.IdpCommunicationError.UserNotAuthenticated
import de.gematik.ti.erp.app.authentication.model.AuthenticationResult.IdpCommunicationUpdate.IdpCommunicationStarted
import de.gematik.ti.erp.app.authentication.model.AuthenticationResult.IdpCommunicationUpdate.IdpCommunicationSuccess
import de.gematik.ti.erp.app.authentication.model.AuthenticationResult.IdpCommunicationUpdate.IdpCommunicationUpdated
import de.gematik.ti.erp.app.authentication.model.BiometricMethod
import de.gematik.ti.erp.app.authentication.observer.BiometricPromptBuilder
import de.gematik.ti.erp.app.authentication.observer.BiometricStateProviderHolder
import de.gematik.ti.erp.app.authentication.ui.components.biometricPromptLauncher
import de.gematik.ti.erp.app.cardwall.usecase.AuthenticationState
import de.gematik.ti.erp.app.cardwall.usecase.AuthenticationUseCase
import de.gematik.ti.erp.app.cardwall.usecase.RemoveAuthenticationUseCase
import de.gematik.ti.erp.app.core.R
import de.gematik.ti.erp.app.idp.api.models.IdpScope
import de.gematik.ti.erp.app.profile.repository.ProfileIdentifier
import io.github.aakira.napier.Napier
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapConcat
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import org.kodein.di.compose.rememberInstance

@Stable
class BiometricAuthenticator(
    private val authenticationUseCase: AuthenticationUseCase,
    private val removeAuthenticationUseCase: RemoveAuthenticationUseCase,
    private val promptInfo: BiometricPrompt.PromptInfo,
    private val biometricPromptBuilder: BiometricPromptBuilder
) : Authenticator() {

    @OptIn(ExperimentalCoroutinesApi::class)
    override fun authenticate(
        id: ProfileIdentifier,
        scope: IdpScope
    ): Flow<AuthenticationResult> {
        return biometricPromptLauncher(
            promptInfo = promptInfo,
            biometricPromptBuilder = biometricPromptBuilder
        )
            .flatMapConcat { biometricResult: BiometricResult ->
                when (biometricResult) {
                    BiometricSuccess -> {
                        authenticationUseCase.authenticateWithSecureElement(id, scope)
                            .map { result: AuthenticationState ->
                                when (result) {
                                    AuthenticationState.AuthenticationFlowFinished -> IdpCommunicationSuccess
                                    AuthenticationState.AuthenticationFlowInitialized -> IdpCommunicationStarted
                                    AuthenticationState.IDPCommunicationFinished -> IdpCommunicationUpdated
                                    AuthenticationState.IDPCommunicationAltAuthNotSuccessful -> AuthenticationNotSuccessful
                                    AuthenticationState.IDPCommunicationFailed -> CommunicationFailure
                                    AuthenticationState.IDPCommunicationInvalidCertificate -> InvalidCertificate
                                    AuthenticationState.IDPCommunicationInvalidOCSPResponseOfHealthCardCertificate -> InvalidOCSP
                                    AuthenticationState.SecureElementCryptographyFailed -> SecureElementFailure
                                    AuthenticationState.UserNotAuthenticated -> UserNotAuthenticated
                                    else -> IllegalStateError(result) // should never happen
                                }
                            }
                    }

                    else -> {
                        Napier.d { "Biometric authentication not success $biometricResult" }
                        flowOf(biometricResult)
                    }
                }
            }
    }

    override fun removeAuthentication(id: ProfileIdentifier) {
        controllerScope.launch { removeAuthenticationUseCase.invoke(id) }
    }
}

@SuppressLint("ContextCastToActivity")
@Composable
fun rememberBiometricAuthenticator(): BiometricAuthenticator {
    val context = LocalContext.current
    val activity = context as AppCompatActivity

    val authenticationUseCase by rememberInstance<AuthenticationUseCase>()
    val removeAuthenticationUseCase by rememberInstance<RemoveAuthenticationUseCase>()
    val biometricPromptBuilder = remember { BiometricPromptBuilder(activity) }

    val title = stringResource(R.string.auth_prompt_headline)
    val description = stringResource(R.string.alternate_auth_info)
    val negativeButton = stringResource(R.string.auth_prompt_cancel)

    // Track the current method type (Strong, Weak, Device)
    val biometricMethod = remember { mutableStateOf(BiometricMethod.None) }

    LaunchedEffect(Unit) {
        BiometricStateProviderHolder.provider.biometricStateChangedFlow.collect { state ->
            Napier.i(tag = "Biometric") { "Biometric state changed, refreshing authenticator: $state" }
            biometricMethod.value = state
        }
    }

    // PromptInfo is rebuilt on method change
    val promptInfo = remember(biometricMethod.value) {
        biometricPromptBuilder.buildPromptInfoDynamically(
            title = title,
            description = description,
            negativeButton = negativeButton,
            method = biometricMethod.value // pass enum
        )
    }

    return remember(promptInfo) {
        BiometricAuthenticator(
            authenticationUseCase = authenticationUseCase,
            removeAuthenticationUseCase = removeAuthenticationUseCase,
            promptInfo = promptInfo,
            biometricPromptBuilder = biometricPromptBuilder
        )
    }
}
