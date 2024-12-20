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

package de.gematik.ti.erp.app.authentication.model

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import de.gematik.ti.erp.app.authentication.model.AuthenticationResult.IdpCommunicationUpdate.IdpCommunicationSuccess
import de.gematik.ti.erp.app.authentication.presentation.BiometricAuthenticator
import de.gematik.ti.erp.app.base.presentation.GetProfileByIdController
import de.gematik.ti.erp.app.core.LocalBiometricAuthenticator
import de.gematik.ti.erp.app.idp.api.models.IdpScope
import de.gematik.ti.erp.app.idp.usecase.ChooseAuthenticationDataUseCase
import de.gematik.ti.erp.app.profiles.repository.ProfileIdentifier
import de.gematik.ti.erp.app.profiles.usecase.GetActiveProfileUseCase
import de.gematik.ti.erp.app.profiles.usecase.GetProfileByIdUseCase
import de.gematik.ti.erp.app.profiles.usecase.GetProfilesUseCase
import de.gematik.ti.erp.app.profiles.usecase.model.ProfilesUseCaseData
import de.gematik.ti.erp.app.utils.compose.ComposableEvent
import de.gematik.ti.erp.app.utils.compose.ComposableEvent.Companion.trigger
import io.github.aakira.napier.Napier
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import org.kodein.di.compose.rememberInstance

private const val TAG = "AuthenticationController"

/**
 * Include this controller with your viewmodel if you want the screen using the viewmodel to be able to allow the user to login.
 * * [showCardWallEvent], the screen should navigate to the cardwall screen.
 * * [biometricAuthenticationSuccessEvent], the user has logged in using biometric authentication.
 * * [biometricAuthenticationResetErrorEvent], the user has tried to login using biometric authentication but the authentication failed.
 * Now the user has to re-authenticate and biometrics won't be called this time
 * * [biometricAuthenticationOtherErrorEvent], the user has tried to login using biometric authentication but the authentication failed.
 * Now the user has to re-authenticate and biometrics will be called again
 */
abstract class ChooseAuthenticationController(
    profileId: ProfileIdentifier? = null,
    getProfileByIdUseCase: GetProfileByIdUseCase,
    getProfilesUseCase: GetProfilesUseCase,
    getActiveProfileUseCase: GetActiveProfileUseCase,
    private val chooseAuthenticationDataUseCase: ChooseAuthenticationDataUseCase,
    private val biometricAuthenticator: BiometricAuthenticator,
    override val onSelectedProfileSuccess: ((ProfilesUseCaseData.Profile, CoroutineScope) -> Unit)? = null,
    override val onSelectedProfileFailure: ((Throwable, CoroutineScope) -> Unit)? = null,
    override val onActiveProfileSuccess: ((ProfilesUseCaseData.Profile, CoroutineScope) -> Unit)? = null,
    override val onActiveProfileFailure: ((Throwable, CoroutineScope) -> Unit)? = null
) : GetProfileByIdController(
    selectedProfileId = profileId,
    getProfilesUseCase = getProfilesUseCase,
    getProfileByIdUseCase = getProfileByIdUseCase,
    getActiveProfileUseCase = getActiveProfileUseCase,
    onSelectedProfileSuccess = onSelectedProfileSuccess,
    onSelectedProfileFailure = onSelectedProfileFailure,
    onActiveProfileSuccess = onActiveProfileSuccess,
    onActiveProfileFailure = onActiveProfileFailure
) {
    val showCardWallEvent = ComposableEvent<ProfileIdentifier>()
    val showCardWallWithFilledCanEvent = ComposableEvent<CardWallEventData>()
    val showGidEvent = ComposableEvent<GidEventData>()
    protected val biometricAuthenticationSuccessEvent = ComposableEvent<Unit>()
    protected val biometricAuthenticationResetErrorEvent = ComposableEvent<AuthenticationResult.Error>()
    protected val biometricAuthenticationOtherErrorEvent = ComposableEvent<AuthenticationResult.Error>()

    @OptIn(ExperimentalCoroutinesApi::class)
    protected fun Flow<ProfilesUseCaseData.Profile>.onBiometricAuthentication(): Flow<ProfilesUseCaseData.Profile?> =
        flatMapLatest { profile ->
            chooseAuthenticationDataUseCase(profile.id)
                .map { authenticationData ->
                    when (authenticationData) {
                        is Biometric -> profile
                        else -> null
                    }
                }
        }

    fun chooseAuthenticationMethod(
        profileId: ProfileIdentifier,
        useBiometricPairingScope: Boolean = false
    ) {
        controllerScope.launch {
            chooseAuthenticationDataUseCase(profileId).first { authenticationData: InitialAuthenticationData ->
                when (authenticationData) {
                    is Biometric -> {
                        Napier.i(tag = TAG, message = "trigger biometric authentication")
                        biometricAuthenticator.authenticate(
                            id = profileId,
                            scope = if (useBiometricPairingScope) IdpScope.BiometricPairing else IdpScope.Default
                        ).collectLatest { result ->
                            when (result) {
                                is IdpCommunicationSuccess -> {
                                    refreshCombinedProfile()
                                    refreshActiveProfile()
                                    biometricAuthenticationSuccessEvent.trigger()
                                }

                                is AuthenticationResult.BiometricResult.BiometricError -> {
                                    onRefreshProfileAction.trigger(false)
                                }

                                is AuthenticationResult.Error -> handleAuthenticationError(
                                    profileId = profileId,
                                    error = result
                                )

                                is AuthenticationResult.BiometricResult.BiometricStarted,
                                is AuthenticationResult.BiometricResult.BiometricSuccess -> {
                                    onRefreshProfileAction.trigger(true)
                                }

                                is AuthenticationResult.IdpCommunicationUpdate.IdpCommunicationStarted,
                                AuthenticationResult.IdpCommunicationUpdate.IdpCommunicationUpdated -> {
                                    // do nothing right now
                                }
                            }
                        }
                    }

                    is External -> showGidEvent.trigger(GidEventData(profileId, authenticationData.authenticatorId, authenticationData.authenticatorName))
                    is HealthCard -> showCardWallWithFilledCanEvent.trigger(CardWallEventData(profileId, authenticationData.can))
                    is None -> showCardWallEvent.trigger(profileId)
                }
                true
            }
        }
    }

    private fun handleAuthenticationError(
        error: AuthenticationResult.Error,
        profileId: ProfileIdentifier
    ) {
        when (error) {
            is AuthenticationResult.Error.ResetError -> {
                Napier.i(tag = TAG) { "Removing authentication data from database" }
                biometricAuthenticator.removeAuthentication(profileId)
                refreshCombinedProfile()
                refreshActiveProfile()
                biometricAuthenticationResetErrorEvent.trigger(error)
            }

            else -> {
                biometricAuthenticationOtherErrorEvent.trigger(error)
            }
        }
    }
}

@Composable
fun rememberChooseAuthenticationController(): ChooseAuthenticationController {
    val getProfileByIdUseCase by rememberInstance<GetProfileByIdUseCase>()
    val getProfilesUseCase by rememberInstance<GetProfilesUseCase>()
    val getActiveProfileUseCase by rememberInstance<GetActiveProfileUseCase>()
    val chooseAuthenticationDataUseCase by rememberInstance<ChooseAuthenticationDataUseCase>()
    val biometricAuthenticator = LocalBiometricAuthenticator.current

    return remember {
        object : ChooseAuthenticationController(
            getActiveProfileUseCase = getActiveProfileUseCase,
            getProfileByIdUseCase = getProfileByIdUseCase,
            getProfilesUseCase = getProfilesUseCase,
            chooseAuthenticationDataUseCase = chooseAuthenticationDataUseCase,
            biometricAuthenticator = biometricAuthenticator
        ) {}
    }
}
