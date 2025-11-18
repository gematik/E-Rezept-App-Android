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
import de.gematik.ti.erp.app.authentication.presentation.AuthReason
import de.gematik.ti.erp.app.authentication.presentation.BiometricAuthenticator
import de.gematik.ti.erp.app.authentication.presentation.ChooseAuthenticationController
import de.gematik.ti.erp.app.authentication.usecase.ChooseAuthenticationDataUseCase
import de.gematik.ti.erp.app.base.NetworkStatusTracker
import de.gematik.ti.erp.app.core.LocalBiometricAuthenticator
import de.gematik.ti.erp.app.eurezept.domain.model.EuPrescription
import de.gematik.ti.erp.app.eurezept.domain.model.EuRedeemError
import de.gematik.ti.erp.app.eurezept.domain.model.PrescriptionFilter
import de.gematik.ti.erp.app.eurezept.domain.usecase.GetEuPrescriptionsUseCase
import de.gematik.ti.erp.app.eurezept.domain.usecase.ToggleIsEuRedeemableByPatientAuthorizationUseCase
import de.gematik.ti.erp.app.eurezept.ui.model.EuPrescriptionStatus
import de.gematik.ti.erp.app.profiles.model.ProfileValidityResult.Companion.fold
import de.gematik.ti.erp.app.profiles.model.ProfileValidityResult.Companion.withValidSSOToken
import de.gematik.ti.erp.app.profiles.usecase.GetActiveProfileUseCase
import de.gematik.ti.erp.app.profiles.usecase.GetProfileByIdUseCase
import de.gematik.ti.erp.app.profiles.usecase.GetProfilesUseCase
import de.gematik.ti.erp.app.utils.compose.ComposableEvent
import de.gematik.ti.erp.app.utils.compose.ComposableEvent.Companion.trigger
import de.gematik.ti.erp.app.utils.uistate.UiState
import io.github.aakira.napier.Napier
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEmpty
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.kodein.di.compose.rememberInstance

/**
 * Controller for managing the selection and marking of EU prescriptions for redemption.
 *
 * This controller handles loading, marking, and toggling the redeemable state of EU prescriptions,
 * as well as authentication and error handling for patient authorization.
 *
 * @property getEuPrescriptionsUseCase Use case to fetch all EU prescriptions.
 * @property toggleIsEuRedeemableByPatientAuthorizationUseCase Use case to toggle the redeemable state of a prescription.
 * @property getProfileByIdUseCase Use case to fetch a profile by its ID.
 * @property getProfilesUseCase Use case to fetch all profiles.
 * @property getActiveProfileUseCase Use case to fetch the active profile.
 * @property chooseAuthenticationDataUseCase Use case for authentication data selection.
 * @property networkStatusTracker Tracks the network status for online/offline handling.
 * @property biometricAuthenticator Handles biometric authentication.
 */
internal class EuPrescriptionSelectionController(
    private val getEuPrescriptionsUseCase: GetEuPrescriptionsUseCase,
    private val toggleIsEuRedeemableByPatientAuthorizationUseCase: ToggleIsEuRedeemableByPatientAuthorizationUseCase,
    getProfileByIdUseCase: GetProfileByIdUseCase,
    getProfilesUseCase: GetProfilesUseCase,
    getActiveProfileUseCase: GetActiveProfileUseCase,
    chooseAuthenticationDataUseCase: ChooseAuthenticationDataUseCase,
    networkStatusTracker: NetworkStatusTracker,
    biometricAuthenticator: BiometricAuthenticator
) : ChooseAuthenticationController(
    getProfileByIdUseCase = getProfileByIdUseCase,
    getProfilesUseCase = getProfilesUseCase,
    getActiveProfileUseCase = getActiveProfileUseCase,
    chooseAuthenticationDataUseCase = chooseAuthenticationDataUseCase,
    networkStatusTracker = networkStatusTracker,
    biometricAuthenticator = biometricAuthenticator
) {
    /**
     * Event triggered when marking a prescription as redeemable by patient authorization fails.
     */
    val markAsEuRedeemableByPatientAuthorizationErrorEvent: ComposableEvent<EuRedeemError> = ComposableEvent()

    /**
     * Holds the UI state for the list of EU prescriptions.
     */
    private val _uiState: MutableStateFlow<UiState<List<EuPrescription>>> = MutableStateFlow(UiState.Loading())

    /**
     * Public state flow exposing the UI state for the list of EU prescriptions.
     */
    val uiState: StateFlow<UiState<List<EuPrescription>>> = _uiState

    /**
     * State flow containing only the prescriptions marked as redeemable by patient authorization.
     */
    val markedEuPrescriptions: StateFlow<List<EuPrescription>> =
        _uiState.map {
            it.data?.filter { euPrescription ->
                euPrescription.isMarkedAsEuRedeemableByPatientAuthorization
            } ?: emptyList()
        }.stateIn(
            scope = controllerScope,
            started = SharingStarted.WhileSubscribed(),
            initialValue = emptyList()
        )

    /**
     * Event triggered on successful biometric authentication for a given reason.
     */
    val onBiometricAuthenticationSuccessEvent = ComposableEvent<Unit>()

    init {
        biometricAuthenticationSuccessEvent.listen(controllerScope) { reason ->
            if (reason == AuthReason.SUBMIT) {
                onBiometricAuthenticationSuccessEvent.trigger()
            }
        }

        controllerScope.launch {
            activeProfile.collect {
                it.data?.let { profile ->
                    if (profile.isSSOTokenValid()) {
                        getEuPrescriptions()
                    } else {
                        chooseAuthenticationMethod(profile)
                    }
                }
            }
        }
    }

    /**
     * Updates the status of a prescription in the UI state list.
     *
     * @param id The ID of the prescription to update.
     * @param status The new status to set for the prescription.
     */
    private fun MutableStateFlow<UiState<List<EuPrescription>>>.setEUPrescriptionStatus(
        id: String,
        status: EuPrescriptionStatus
    ) = update { screenUiState ->
        val list = screenUiState.data ?: return@update screenUiState
        val updated = list.map { item ->
            if (item.id == id) {
                when (status) {
                    EuPrescriptionStatus.Loading -> item.copy(
                        isLoading = true,
                        isMarkedAsError = false
                    )

                    EuPrescriptionStatus.Idle -> item.copy(
                        isLoading = false,
                        isMarkedAsError = false
                    )

                    EuPrescriptionStatus.Error -> item.copy(
                        isLoading = false,
                        isMarkedAsError = true
                    )
                }
            } else item
        }
        screenUiState.copy(data = updated)
    }

    /**
     * Marks a prescription as loading in the UI state list.
     *
     * @param id The ID of the prescription to mark as loading.
     */
    private fun MutableStateFlow<UiState<List<EuPrescription>>>.markEUPrescriptionLoading(id: String) =
        setEUPrescriptionStatus(id, EuPrescriptionStatus.Loading)

    /**
     * Clears the loading state of a prescription in the UI state list.
     *
     * @param id The ID of the prescription to clear loading state for.
     */
    private fun MutableStateFlow<UiState<List<EuPrescription>>>.clearEUPrescriptionLoading(id: String) =
        setEUPrescriptionStatus(id, EuPrescriptionStatus.Idle)

    /**
     * Marks a prescription as having an error in the UI state list.
     *
     * @param id The ID of the prescription to mark as error.
     */
    private fun MutableStateFlow<UiState<List<EuPrescription>>>.markEUPrescriptionError(id: String) =
        setEUPrescriptionStatus(id, EuPrescriptionStatus.Error)

    /**
     * Loads all EU prescriptions and updates the UI state accordingly.
     *
     * On success, updates the UI state with the loaded prescriptions.
     * On failure, updates the UI state with the error.
     */
    fun getEuPrescriptions() {
        controllerScope.launch {
            try {
                _uiState.update { UiState.Loading() }
                getEuPrescriptionsUseCase(PrescriptionFilter.ALL)
                    .onEmpty {
                        _uiState.update { UiState.Empty() }
                    }.collect { euPrescriptions ->
                        _uiState.update { UiState.Data(euPrescriptions) }
                    }
            } catch (e: Exception) {
                Napier.e { "failed to load EuPrescriptions" }
                _uiState.update { UiState.Error(e) }
            }
        }
    }

    /**
     * Toggles the selection state of a prescription for EU redemption.
     *
     * If the profile is valid, attempts to toggle the redeemable state and updates the UI state accordingly.
     * On failure, marks the prescription as error and triggers an error event.
     * If the profile is invalid, triggers authentication.
     *
     * @param euPrescription The prescription to toggle selection for.
     */
    fun togglePrescriptionSelection(euPrescription: EuPrescription) {
        controllerScope.launch {
            activeProfile.withValidSSOToken().fold(
                onValid = { _ ->
                    _uiState.markEUPrescriptionLoading(euPrescription.id)
                    toggleIsEuRedeemableByPatientAuthorizationUseCase.invoke(
                        taskId = euPrescription.id,
                        profileId = euPrescription.profileIdentifier,
                        isEuRedeemableByPatientAuthorization = !euPrescription.isMarkedAsEuRedeemableByPatientAuthorization
                    ).fold(
                        onSuccess = {
                            _uiState.clearEUPrescriptionLoading(euPrescription.id)
                        },
                        onFailure = { error ->
                            _uiState.markEUPrescriptionError(euPrescription.id)
                            markAsEuRedeemableByPatientAuthorizationErrorEvent.trigger(
                                payload = EuRedeemError(
                                    euPrescription = euPrescription,
                                    error = error
                                )
                            )
                        }
                    )
                },
                onInvalid = { invalidProfile ->
                    chooseAuthenticationMethod(invalidProfile)
                }
            )
        }
    }
}

/**
 * Remembers and provides an instance of [EuPrescriptionSelectionController] for use in Compose.
 *
 * @return Remembered [EuPrescriptionSelectionController] instance.
 */
@Composable
internal fun rememberEuPrescriptionSelectionController(): EuPrescriptionSelectionController {
    val biometricAuthenticator = LocalBiometricAuthenticator.current
    val networkStatusTracker by rememberInstance<NetworkStatusTracker>()
    val getEuPrescriptionsUseCase by rememberInstance<GetEuPrescriptionsUseCase>()
    val getActiveProfileUseCase by rememberInstance<GetActiveProfileUseCase>()
    val getProfileByIdUseCase by rememberInstance<GetProfileByIdUseCase>()
    val getProfilesUseCase by rememberInstance<GetProfilesUseCase>()
    val chooseAuthenticationDataUseCase by rememberInstance<ChooseAuthenticationDataUseCase>()
    val toggleIsEuRedeemableByPatientAuthorizationUseCase by rememberInstance<ToggleIsEuRedeemableByPatientAuthorizationUseCase>()
    return remember {
        EuPrescriptionSelectionController(
            getEuPrescriptionsUseCase = getEuPrescriptionsUseCase,
            toggleIsEuRedeemableByPatientAuthorizationUseCase = toggleIsEuRedeemableByPatientAuthorizationUseCase,
            getProfileByIdUseCase = getProfileByIdUseCase,
            getProfilesUseCase = getProfilesUseCase,
            getActiveProfileUseCase = getActiveProfileUseCase,
            chooseAuthenticationDataUseCase = chooseAuthenticationDataUseCase,
            networkStatusTracker = networkStatusTracker,
            biometricAuthenticator = biometricAuthenticator
        )
    }
}
