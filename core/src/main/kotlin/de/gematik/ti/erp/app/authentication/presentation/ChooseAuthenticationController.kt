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
import de.gematik.ti.erp.app.authentication.usecase.ChooseAuthenticationDataUseCase
import de.gematik.ti.erp.app.base.NetworkStatusTracker
import de.gematik.ti.erp.app.cardwall.model.CardWallEventData
import de.gematik.ti.erp.app.cardwall.model.GidNavigationData
import de.gematik.ti.erp.app.idp.api.models.IdpScope
import de.gematik.ti.erp.app.profile.model.InsuranceType
import de.gematik.ti.erp.app.profile.model.ProfileErpModel
import de.gematik.ti.erp.app.profile.repository.ProfileIdentifier
import de.gematik.ti.erp.app.profiles.presentation.GetProfileByIdController
import de.gematik.ti.erp.app.profiles.usecase.GetActiveProfileUseCase
import de.gematik.ti.erp.app.profiles.usecase.GetProfileByIdUseCase
import de.gematik.ti.erp.app.profiles.usecase.GetProfilesUseCase
import de.gematik.ti.erp.app.userauthentication.model.UserAuthenticationErpModel
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
    override val onSelectedProfileSuccess: ((ProfileErpModel, CoroutineScope) -> Unit)? = null,
    override val onSelectedProfileFailure: ((Throwable, CoroutineScope) -> Unit)? = null,
    override val onActiveProfileSuccess: ((ProfileErpModel, CoroutineScope) -> Unit)? = null,
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
    protected fun Flow<ProfileErpModel>.onBiometricAuthentication(): Flow<ProfileErpModel?> =
        flatMapLatest { profile ->
            chooseAuthenticationDataUseCase(profile.id)
                .map { authenticationData ->
                    when (authenticationData) {
                        is UserAuthenticationErpModel.HealthCardWithSavedCredentials -> profile
                        else -> null
                    }
                }
        }

    fun chooseAuthenticationMethod(
        profile: ProfileErpModel,
        useBiometricPairingScope: Boolean = false,
        authenticationReason: AuthReason = AuthReason.SUBMIT
    ) {
        controllerScope.launch {
            chooseAuthenticationDataUseCase(profile.id).first { userAuthenticationErpModel: UserAuthenticationErpModel ->
                when (userAuthenticationErpModel) {
                    is UserAuthenticationErpModel.HealthCardWithSavedCredentials -> {
                        Napier.i(tag = TAG, message = "trigger biometric authentication")
                        biometricAuthenticator.authenticate(
                            id = profile.id,
                            scope = if (useBiometricPairingScope) IdpScope.BiometricPairing else IdpScope.Default
                        ).collectLatest { result ->
                            when (result) {
                                is AuthenticationResult.IdpCommunicationUpdate.IdpCommunicationSuccess -> {
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

                    is UserAuthenticationErpModel.External -> when (profile.insuranceData.insuranceType) {
                        InsuranceType.GKV -> chooseAuthenticationNavigationEvents.showCardWallIntroScreenWithGidEvent.trigger(
                            GidNavigationData(
                                profile.id,
                                userAuthenticationErpModel.externalAuthenticatorId,
                                userAuthenticationErpModel.externalAuthenticatorName
                            )
                        )

                        InsuranceType.PKV -> chooseAuthenticationNavigationEvents.showCardWallGidListScreenWithGidEvent.trigger(
                            GidNavigationData(
                                profile.id,
                                userAuthenticationErpModel.externalAuthenticatorId,
                                userAuthenticationErpModel.externalAuthenticatorName
                            )
                        )

                        InsuranceType.BUND -> chooseAuthenticationNavigationEvents.showCardWallIntroScreenWithGidEvent.trigger(
                            GidNavigationData(
                                profile.id,
                                userAuthenticationErpModel.externalAuthenticatorId,
                                userAuthenticationErpModel.externalAuthenticatorName
                            )
                        )

                        InsuranceType.NONE -> chooseAuthenticationNavigationEvents.showCardWallSelectInsuranceScreenEvent.trigger(
                            profile.id
                        ) // can't be reached
                    }

                    is UserAuthenticationErpModel.HealthCard -> chooseAuthenticationNavigationEvents.showCardWallWithFilledCanEvent.trigger(
                        CardWallEventData(
                            profile.id,
                            userAuthenticationErpModel.cardAccessNumber
                        )
                    )

                    is UserAuthenticationErpModel.NotInitialized -> when (profile.insuranceData.insuranceType) {
                        InsuranceType.NONE -> chooseAuthenticationNavigationEvents.showCardWallSelectInsuranceScreenEvent.trigger(
                            profile.id
                        )

                        InsuranceType.GKV -> chooseAuthenticationNavigationEvents.showCardWallIntroScreenEvent.trigger(profile.id)
                        InsuranceType.PKV -> chooseAuthenticationNavigationEvents.showCardWallGidListScreenEvent.trigger(profile.id)
                        InsuranceType.BUND -> chooseAuthenticationNavigationEvents.showCardWallCanScreenEvent.trigger(profile.id)
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
                chooseAuthenticationNavigationEvents.biometricAuthenticationResetErrorEvent.trigger(error)
            }

            else -> {
                chooseAuthenticationNavigationEvents.biometricAuthenticationOtherErrorEvent.trigger(error)
            }
        }
    }
}
