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

import de.gematik.ti.erp.app.authentication.model.AuthenticationResult
import de.gematik.ti.erp.app.authentication.model.Biometric
import de.gematik.ti.erp.app.authentication.model.External
import de.gematik.ti.erp.app.authentication.model.HealthCard
import de.gematik.ti.erp.app.authentication.model.InitialAuthenticationData
import de.gematik.ti.erp.app.authentication.model.None
import de.gematik.ti.erp.app.authentication.usecase.ChooseAuthenticationDataUseCase
import de.gematik.ti.erp.app.base.NetworkStatusTracker
import de.gematik.ti.erp.app.cardwall.model.CardWallEventData
import de.gematik.ti.erp.app.cardwall.model.GidNavigationData
import de.gematik.ti.erp.app.idp.api.models.IdpScope
import de.gematik.ti.erp.app.profile.repository.ProfileIdentifier
import de.gematik.ti.erp.app.profiles.presentation.GetProfileByIdController
import de.gematik.ti.erp.app.profiles.usecase.GetActiveProfileUseCase
import de.gematik.ti.erp.app.profiles.usecase.GetProfileByIdUseCase
import de.gematik.ti.erp.app.profiles.usecase.GetProfilesUseCase
import de.gematik.ti.erp.app.profiles.usecase.model.ProfilesUseCaseData
import de.gematik.ti.erp.app.utils.compose.ComposableEvent
import io.github.aakira.napier.Napier
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

private const val TAG = "AuthenticationController"

/**
 * Include this controller with your viewmodel if you want the screen using the viewmodel to be able to allow the user to login.
 * * [showCardWallIntroScreenEvent], the screen should navigate to the cardwall screen.
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
    private val networkStatusTracker: NetworkStatusTracker,
    private val biometricAuthenticator: BiometricAuthenticator,
    val chooseAuthenticationNavigationEvents: ChooseAuthenticationNavigationEvents = ChooseAuthenticationNavigationEvents(),
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
    protected val biometricAuthenticationSuccessEvent = ComposableEvent<AuthReason>()

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
        profile: ProfilesUseCaseData.Profile,
        useBiometricPairingScope: Boolean = false,
        authenticationReason: AuthReason = AuthReason.SUBMIT
    ) {
        controllerScope.launch {
            chooseAuthenticationDataUseCase(profile.id).first { authenticationData: InitialAuthenticationData ->
                when (authenticationData) {
                    is Biometric -> {
                        Napier.i(tag = TAG, message = "trigger biometric authentication")
                        biometricAuthenticator.authenticate(
                            id = profile.id,
                            scope = if (useBiometricPairingScope) IdpScope.BiometricPairing else IdpScope.Default
                        ).collectLatest { result ->
                            when (result) {
                                is AuthenticationResult.IdpCommunicationUpdate.IdpCommunicationSuccess -> {
                                    refreshActiveProfile()
                                    biometricAuthenticationSuccessEvent.trigger(authenticationReason)
                                }

                                is AuthenticationResult.BiometricResult.BiometricError -> {
                                    Napier.e(tag = TAG, message = "Biometric authentication error: ${result.error}, code: ${result.errorCode}")
                                    isProfileRefreshingEvent.trigger(false)
                                }

                                is AuthenticationResult.Error -> handleAuthenticationError(
                                    profileId = profile.id,
                                    error = result
                                )

                                is AuthenticationResult.BiometricResult.BiometricStarted,
                                is AuthenticationResult.BiometricResult.BiometricSuccess
                                -> {
                                    // inform that the biometric can be successful only when the network is available
                                    if (networkStatusTracker.networkStatus.first()) {
                                        isProfileRefreshingEvent.trigger(true)
                                    }
                                }

                                is AuthenticationResult.IdpCommunicationUpdate.IdpCommunicationStarted,
                                AuthenticationResult.IdpCommunicationUpdate.IdpCommunicationUpdated
                                -> {
                                    // do nothing right now
                                }
                            }
                        }
                    }

                    is External -> when (profile.insurance.insuranceType) {
                        ProfilesUseCaseData.InsuranceType.GKV -> chooseAuthenticationNavigationEvents.showCardWallIntroScreenWithGidEvent.trigger(
                            GidNavigationData(profile.id, authenticationData.authenticatorId, authenticationData.authenticatorName)
                        )

                        ProfilesUseCaseData.InsuranceType.PKV -> chooseAuthenticationNavigationEvents.showCardWallGidListScreenWithGidEvent.trigger(
                            GidNavigationData(profile.id, authenticationData.authenticatorId, authenticationData.authenticatorName)
                        )

                        ProfilesUseCaseData.InsuranceType.BUND -> chooseAuthenticationNavigationEvents.showCardWallCanScreenEvent.trigger(
                            profile.id
                        )

                        ProfilesUseCaseData.InsuranceType.NONE -> chooseAuthenticationNavigationEvents.showCardWallSelectInsuranceScreenEvent.trigger(
                            profile.id
                        ) // can't be reached
                    }

                    is HealthCard -> chooseAuthenticationNavigationEvents.showCardWallWithFilledCanEvent.trigger(
                        CardWallEventData(
                            profile.id,
                            authenticationData.can
                        )
                    )

                    is None -> when (profile.insurance.insuranceType) {
                        ProfilesUseCaseData.InsuranceType.NONE -> chooseAuthenticationNavigationEvents.showCardWallSelectInsuranceScreenEvent.trigger(
                            profile.id
                        )

                        ProfilesUseCaseData.InsuranceType.GKV -> chooseAuthenticationNavigationEvents.showCardWallIntroScreenEvent.trigger(profile.id)
                        ProfilesUseCaseData.InsuranceType.PKV -> chooseAuthenticationNavigationEvents.showCardWallGidListScreenEvent.trigger(profile.id)
                        ProfilesUseCaseData.InsuranceType.BUND -> chooseAuthenticationNavigationEvents.showCardWallCanScreenEvent.trigger(profile.id)
                    }
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
                refreshActiveProfile()
                chooseAuthenticationNavigationEvents.biometricAuthenticationResetErrorEvent.trigger(error)
            }

            else -> {
                chooseAuthenticationNavigationEvents.biometricAuthenticationOtherErrorEvent.trigger(error)
            }
        }
    }
}
