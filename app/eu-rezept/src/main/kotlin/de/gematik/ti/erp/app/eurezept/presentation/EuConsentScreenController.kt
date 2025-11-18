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

package de.gematik.ti.erp.app.eurezept.presentation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import de.gematik.ti.erp.app.authentication.presentation.BiometricAuthenticator
import de.gematik.ti.erp.app.authentication.presentation.ChooseAuthenticationController
import de.gematik.ti.erp.app.authentication.usecase.ChooseAuthenticationDataUseCase
import de.gematik.ti.erp.app.base.NetworkStatusTracker
import de.gematik.ti.erp.app.core.LocalBiometricAuthenticator
import de.gematik.ti.erp.app.eurezept.domain.usecase.GetEuPrescriptionConsentUseCase
import de.gematik.ti.erp.app.eurezept.domain.usecase.GrantEuPrescriptionConsentUseCase
import de.gematik.ti.erp.app.eurezept.ui.model.EuConsentNavigationEvent
import de.gematik.ti.erp.app.eurezept.ui.model.EuConsentViewState
import de.gematik.ti.erp.app.profiles.usecase.GetActiveProfileUseCase
import de.gematik.ti.erp.app.profiles.usecase.GetProfileByIdUseCase
import de.gematik.ti.erp.app.profiles.usecase.GetProfilesUseCase
import de.gematik.ti.erp.app.profiles.usecase.model.ProfilesUseCaseData
import de.gematik.ti.erp.app.utils.uistate.UiState
import de.gematik.ti.erp.app.utils.uistate.UiState.Companion.isDataState
import de.gematik.ti.erp.app.utils.uistate.UiState.Companion.isErrorState
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.kodein.di.compose.rememberInstance

internal class EuConsentScreenController(
    private val getEuPrescriptionConsentUseCase: GetEuPrescriptionConsentUseCase,
    private val grantEuPrescriptionConsentUseCase: GrantEuPrescriptionConsentUseCase,
    getProfilesUseCase: GetProfilesUseCase,
    getActiveProfileUseCase: GetActiveProfileUseCase,
    chooseAuthenticationDataUseCase: ChooseAuthenticationDataUseCase,
    networkStatusTracker: NetworkStatusTracker,
    biometricAuthenticator: BiometricAuthenticator,
    getProfileByIdUseCase: GetProfileByIdUseCase
) : ChooseAuthenticationController(
    getProfileByIdUseCase = getProfileByIdUseCase,
    getProfilesUseCase = getProfilesUseCase,
    getActiveProfileUseCase = getActiveProfileUseCase,
    chooseAuthenticationDataUseCase = chooseAuthenticationDataUseCase,
    networkStatusTracker = networkStatusTracker,
    biometricAuthenticator = biometricAuthenticator
) {
    private val _consentViewState = MutableStateFlow<UiState<EuConsentViewState>>(UiState.Loading())
    val consentViewState: StateFlow<UiState<EuConsentViewState>> = _consentViewState.asStateFlow()

    private val _navigationEvents = MutableSharedFlow<EuConsentNavigationEvent>()
    val navigationEvents: SharedFlow<EuConsentNavigationEvent> = _navigationEvents.asSharedFlow()

    init {
        biometricAuthenticationSuccessEvent.listen(controllerScope) {
            observeActiveProfileAndLoadConsent()
        }
        observeActiveProfileAndLoadConsent()
    }

    private fun observeActiveProfileAndLoadConsent() {
        controllerScope.launch {
            activeProfile.collect { state ->
                when {
                    state.isDataState -> state.data?.let { profile ->
                        if (profile.isSSOTokenValid()) {
                            loadConsentData(profile)
                        } else {
                            chooseAuthenticationMethod(profile)
                        }
                    }

                    state.isErrorState -> state.error?.let { err ->
                        _consentViewState.update { UiState.Error(err) }
                    }
                }
            }
        }
    }

    private suspend fun loadConsentData(profile: ProfilesUseCaseData.Profile) {
        _consentViewState.update { UiState.Loading() }
        val result = getEuPrescriptionConsentUseCase.invoke(profile.id)
        result.fold(
            onSuccess = { consent ->
                if (consent.isActive()) {
                    _navigationEvents.emit(EuConsentNavigationEvent.NavigateToRedeem)
                }
                _consentViewState.update { UiState.Data(EuConsentViewState(consentData = consent)) }
            },
            onFailure = { error ->
                _consentViewState.update { UiState.Error(error) }
            }
        )
    }

    fun retryLoadingConsent() = controllerScope.launch {
        activeProfile.value.data?.let { profile ->
            loadConsentData(profile)
        } ?: refreshActiveProfile()
    }

    fun onConsentAccepted() {
        val profile = activeProfile.value.data ?: return
        val data = _consentViewState.value.takeIf { !it.isLoading }?.data ?: return

        controllerScope.launch {
            _consentViewState.update { UiState.Data(data.copy(isGrantingConsent = true, grantConsentError = null)) }
            grantEuPrescriptionConsentUseCase(profile)
                .onSuccess { onConsentGrantedSuccessfully(data) }
                .onFailure { e -> _consentViewState.update { UiState.Error(e) } }
        }
    }

    fun onDeclineConsent() {
        controllerScope.launch {
            _navigationEvents.emit(EuConsentNavigationEvent.NavigateBack)
        }
    }

    private suspend fun onConsentGrantedSuccessfully(previousData: EuConsentViewState) {
        _consentViewState.update {
            UiState.Data(
                previousData.copy(
                    isGrantingConsent = false,
                    grantConsentError = null
                )
            )
        }
        _navigationEvents.emit(EuConsentNavigationEvent.NavigateToRedeem)
    }

    fun onBackPressed() {
        controllerScope.launch { _navigationEvents.emit(EuConsentNavigationEvent.NavigateBack) }
    }

    fun onCancelFlow() {
        controllerScope.launch { _navigationEvents.emit(EuConsentNavigationEvent.CancelFlow) }
    }
}

@Composable
internal fun rememberEuConsentScreenController(): EuConsentScreenController {
    val biometricAuthenticator = LocalBiometricAuthenticator.current
    val networkStatusTracker by rememberInstance<NetworkStatusTracker>()
    val getProfilesUseCase by rememberInstance<GetProfilesUseCase>()
    val chooseAuthenticationDataUseCase by rememberInstance<ChooseAuthenticationDataUseCase>()
    val getEuPrescriptionConsentUseCase by rememberInstance<GetEuPrescriptionConsentUseCase>()
    val grantEuPrescriptionConsentUseCase by rememberInstance<GrantEuPrescriptionConsentUseCase>()
    val getActiveProfileUseCase by rememberInstance<GetActiveProfileUseCase>()
    val getProfileByIdUseCase by rememberInstance<GetProfileByIdUseCase>()

    return remember {
        EuConsentScreenController(
            getEuPrescriptionConsentUseCase = getEuPrescriptionConsentUseCase,
            grantEuPrescriptionConsentUseCase = grantEuPrescriptionConsentUseCase,
            getActiveProfileUseCase = getActiveProfileUseCase,
            chooseAuthenticationDataUseCase = chooseAuthenticationDataUseCase,
            networkStatusTracker = networkStatusTracker,
            biometricAuthenticator = biometricAuthenticator,
            getProfilesUseCase = getProfilesUseCase,
            getProfileByIdUseCase = getProfileByIdUseCase
        )
    }
}
