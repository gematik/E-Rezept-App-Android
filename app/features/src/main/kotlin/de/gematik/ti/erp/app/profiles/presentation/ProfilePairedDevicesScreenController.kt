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

package de.gematik.ti.erp.app.profiles.presentation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.remember
import de.gematik.ti.erp.app.api.NoInternetException
import de.gematik.ti.erp.app.authentication.model.AuthenticationResult
import de.gematik.ti.erp.app.authentication.presentation.BiometricAuthenticator
import de.gematik.ti.erp.app.authentication.presentation.ChooseAuthenticationController
import de.gematik.ti.erp.app.authentication.usecase.ChooseAuthenticationDataUseCase
import de.gematik.ti.erp.app.base.NetworkStatusTracker
import de.gematik.ti.erp.app.base.NetworkStatusTracker.Companion.isNetworkAvailable
import de.gematik.ti.erp.app.core.LocalBiometricAuthenticator
import de.gematik.ti.erp.app.idp.model.IdpData
import de.gematik.ti.erp.app.idp.usecase.RefreshFlowException.Companion.isUserActionNotRequired
import de.gematik.ti.erp.app.idp.usecase.RefreshFlowException.Companion.isUserActionRequired
import de.gematik.ti.erp.app.profile.repository.ProfileIdentifier
import de.gematik.ti.erp.app.profiles.model.ProfilePairedDevicesErrorState.CannotLoadPairedDevicesError
import de.gematik.ti.erp.app.profiles.model.ProfilePairedDevicesErrorState.NoInternetError
import de.gematik.ti.erp.app.profiles.model.ProfilePairedDevicesErrorState.UserNotLoggedInWithBiometricsError
import de.gematik.ti.erp.app.profiles.usecase.DeletePairedDevicesUseCase
import de.gematik.ti.erp.app.profiles.usecase.GetActiveProfileUseCase
import de.gematik.ti.erp.app.profiles.usecase.GetPairedDevicesUseCase
import de.gematik.ti.erp.app.profiles.usecase.GetProfileByIdUseCase
import de.gematik.ti.erp.app.profiles.usecase.GetProfilesUseCase
import de.gematik.ti.erp.app.profiles.usecase.model.PairedDevice
import de.gematik.ti.erp.app.profiles.usecase.model.ProfilesUseCaseData
import de.gematik.ti.erp.app.utils.compose.ComposableEvent
import de.gematik.ti.erp.app.utils.uistate.UiState
import io.github.aakira.napier.Napier
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.retry
import kotlinx.coroutines.launch
import org.kodein.di.compose.rememberInstance

private const val ASK_FOR_AUTH_REQUIRED_ATTEMPTS = 1L

@Stable
class ProfilePairedDevicesScreenController(
    private val profileId: ProfileIdentifier,
    getProfileByIdUseCase: GetProfileByIdUseCase,
    getActiveProfileUseCase: GetActiveProfileUseCase,
    getProfilesUseCase: GetProfilesUseCase,
    chooseAuthenticationDataUseCase: ChooseAuthenticationDataUseCase,
    biometricAuthenticator: BiometricAuthenticator,
    private val getPairedDevicesUseCase: GetPairedDevicesUseCase,
    private val deletePairedDevicesUseCase: DeletePairedDevicesUseCase,
    private val refreshTrigger: MutableSharedFlow<ProfilesUseCaseData.Profile> = MutableSharedFlow(),
    private val networkStatusTracker: NetworkStatusTracker
) : ChooseAuthenticationController(
    profileId = profileId,
    getProfileByIdUseCase = getProfileByIdUseCase,
    getActiveProfileUseCase = getActiveProfileUseCase,
    getProfilesUseCase = getProfilesUseCase,
    chooseAuthenticationDataUseCase = chooseAuthenticationDataUseCase,
    biometricAuthenticator = biometricAuthenticator,
    networkStatusTracker = networkStatusTracker,
    onSelectedProfileSuccess = { profile, coroutineScope ->
        coroutineScope.launch {
            refreshTrigger.emit(profile)
        }
    }
) {
    init {
        observePairedDevices()

        biometricAuthenticationSuccessEvent.listen(controllerScope) { refreshPairedDevices() }

        biometricAuthenticationResetErrorEvent.listen(controllerScope) { error ->
            _pairedDevices.value = UiState.Error<List<PairedDevice>>(CannotLoadPairedDevicesError)
            showAuthenticationErrorDialog.trigger(error)
        }

        biometricAuthenticationOtherErrorEvent.listen(controllerScope) { error ->
            _pairedDevices.value = UiState.Error<List<PairedDevice>>(CannotLoadPairedDevicesError)
            showAuthenticationErrorDialog.trigger(error)
        }
    }

    private val isNotBiometricAuthentication = MutableStateFlow(false)
    private val _pairedDevices: MutableStateFlow<UiState<List<PairedDevice>>> = MutableStateFlow(
        UiState.Loading()
    )

    val showAuthenticationErrorDialog = ComposableEvent<AuthenticationResult.Error>()

    val pairedDevices: StateFlow<UiState<List<PairedDevice>>> = _pairedDevices.asStateFlow()

    @OptIn(ExperimentalCoroutinesApi::class)
    private fun observePairedDevices() {
        controllerScope.launch {
            refreshTrigger
                .onEach { UiState.Loading<List<PairedDevice>>() }
                .onBiometricAuthentication()
                .flatMapLatest { biometricProfile ->
                    if (biometricProfile != null) {
                        isNotBiometricAuthentication.value = false
                        // If profile is not null, continue with the flow since user has used biometric authentication
                        flowOf(biometricProfile)
                            .withUniqueToken()
                            .loadPairedDevices()
                            .retry(ASK_FOR_AUTH_REQUIRED_ATTEMPTS) { throwable ->
                                if (throwable.isUserActionRequired()) { // to ask for biometric authentication
                                    Napier.i { "need bio-auth for paired devices, ${throwable.message}" }
                                    activeProfile.value.data?.let {
                                        chooseAuthenticationMethod(
                                            profile = it,
                                            useBiometricPairingScope = true
                                        )
                                    }
                                    true
                                } else {
                                    Napier.e { "error loading paired devices ${throwable.message}" }
                                    UiState.Error<List<PairedDevice>>(CannotLoadPairedDevicesError)
                                    false
                                }
                            }
                            .map { UiState.Data(it) }
                    } else {
                        // since the code will not go into the catch loop, we need to check for internet failure here
                        checkNetworkBeforeBiometricError()
                    }
                }
                .catch { exception ->
                    Napier.e { "error on loading paired devices ${exception.stackTraceToString()}" }
                    if (exception.isUserActionNotRequired()) { // currently asking user for biometric authentication
                        _pairedDevices.value = when (exception) {
                            is NoInternetException -> UiState.Error<List<PairedDevice>>(NoInternetError)
                            else -> UiState.Error<List<PairedDevice>>(CannotLoadPairedDevicesError)
                        }
                    }
                }.collectLatest {
                    Napier.d { "collected state $it" }
                    _pairedDevices.value = it
                }
        }
    }

    fun refreshPairedDevices() {
        controllerScope.launch {
            observePairedDevices()
        }
    }

    fun deletePairedDevice(device: PairedDevice) {
        controllerScope.launch {
            _pairedDevices.value = UiState.Loading()
            deletePairedDevicesUseCase.invoke(profileId, device)
                .onSuccess { _ ->
                    refreshPairedDevices()
                }
        }
    }

    // the errors thrown here are to be caught in the catch block
    private fun checkNetworkBeforeBiometricError(): Flow<UiState<List<PairedDevice>>> =
        if (networkStatusTracker.isNetworkAvailable()) {
            isNotBiometricAuthentication.value = true
            flowOf(UiState.Error(UserNotLoggedInWithBiometricsError))
        } else {
            flowOf(UiState.Error(NoInternetError))
        }

    private fun Flow<ProfilesUseCaseData.Profile>.withUniqueToken(): Flow<IdpData.TokenWithKeyStoreAliasScope> =
        map { profile -> profile.ssoTokenScope as? IdpData.TokenWithKeyStoreAliasScope }
            .filterNotNull().distinctUntilChanged()

    @OptIn(ExperimentalCoroutinesApi::class)
    private fun Flow<IdpData.TokenWithKeyStoreAliasScope>.loadPairedDevices(): Flow<List<PairedDevice>> =
        flatMapLatest { token ->
            getPairedDevicesUseCase.invoke(
                profileId = profileId,
                keyStoreAlias = token.aliasOfSecureElementEntryBase64()
            )
        }
}

@Composable
fun rememberProfilePairedDevicesScreenController(
    profileId: ProfileIdentifier
): ProfilePairedDevicesScreenController {
    val getProfileByIdUseCase by rememberInstance<GetProfileByIdUseCase>()
    val getActiveProfileUseCase by rememberInstance<GetActiveProfileUseCase>()
    val getProfilesUseCase by rememberInstance<GetProfilesUseCase>()
    val chooseAuthenticationDataUseCase by rememberInstance<ChooseAuthenticationDataUseCase>()
    val getPairedDevicesUseCase by rememberInstance<GetPairedDevicesUseCase>()
    val deletePairedDevicesUseCase by rememberInstance<DeletePairedDevicesUseCase>()
    val networkStatusTracker by rememberInstance<NetworkStatusTracker>()
    val biometricAuthenticator = LocalBiometricAuthenticator.current

    return remember(profileId) {
        ProfilePairedDevicesScreenController(
            profileId = profileId,
            getProfilesUseCase = getProfilesUseCase,
            getProfileByIdUseCase = getProfileByIdUseCase,
            getActiveProfileUseCase = getActiveProfileUseCase,
            chooseAuthenticationDataUseCase = chooseAuthenticationDataUseCase,
            biometricAuthenticator = biometricAuthenticator,
            getPairedDevicesUseCase = getPairedDevicesUseCase,
            deletePairedDevicesUseCase = deletePairedDevicesUseCase,
            networkStatusTracker = networkStatusTracker
        )
    }
}
