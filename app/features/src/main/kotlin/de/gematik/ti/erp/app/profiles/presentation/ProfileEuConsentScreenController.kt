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

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import de.gematik.ti.erp.app.consent.usecase.RevokeConsentUseCase
import de.gematik.ti.erp.app.core.R
import de.gematik.ti.erp.app.eurezept.domain.usecase.GetEuPrescriptionConsentUseCase
import de.gematik.ti.erp.app.eurezept.domain.usecase.GrantEuPrescriptionConsentUseCase
import de.gematik.ti.erp.app.eurezept.ui.model.EuConsentNavigationEvent
import de.gematik.ti.erp.app.eurezept.ui.model.EuConsentViewState
import de.gematik.ti.erp.app.fhir.consent.model.ConsentCategory
import de.gematik.ti.erp.app.profiles.usecase.GetActiveProfileUseCase
import de.gematik.ti.erp.app.profiles.usecase.model.ProfilesUseCaseData
import de.gematik.ti.erp.app.utils.uistate.UiState
import de.gematik.ti.erp.app.utils.uistate.UiState.Companion.isDataState
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.kodein.di.compose.rememberInstance

@Stable
class ProfileEuConsentScreenController(
    private val getEuPrescriptionConsentUseCase: GetEuPrescriptionConsentUseCase,
    private val grantEuPrescriptionConsentUseCase: GrantEuPrescriptionConsentUseCase,
    private val revokeEuConsentUseCase: RevokeConsentUseCase,
    getActiveProfileUseCase: GetActiveProfileUseCase,
    private val _consentViewState: MutableStateFlow<UiState<EuConsentViewState>> = MutableStateFlow(UiState.Loading()),
    val consentViewState: StateFlow<UiState<EuConsentViewState>> = _consentViewState.asStateFlow(),
    private val context: Context
) : GetActiveProfileController(
    getActiveProfileUseCase = getActiveProfileUseCase,
    onSuccess = { profile, scope ->
        scope.launch {
            _consentViewState.update { UiState.Loading() }
            val result = getEuPrescriptionConsentUseCase(profile.id)

            result.fold(
                onSuccess = { consent ->
                    _consentViewState.update {
                        UiState.Data(EuConsentViewState(consentData = consent))
                    }
                },
                onFailure = { error ->
                    _consentViewState.update { UiState.Error(error) }
                }
            )
        }
    },
    onFailure = { error, _ ->
        _consentViewState.update { UiState.Error(error) }
    }
) {
    private val _navigationEvents = MutableSharedFlow<EuConsentNavigationEvent>()
    val navigationEvents: SharedFlow<EuConsentNavigationEvent> = _navigationEvents.asSharedFlow()

    fun retryLoadingConsent() {
        refreshActiveProfile()
    }

    fun onConsentAccepted() {
        val currentConsentState = _consentViewState.value
        val currentProfileState = activeProfile.value

        if (currentConsentState.isLoading ||
            currentConsentState.data == null ||
            !currentProfileState.isDataState
        ) {
            return
        }

        controllerScope.launch {
            _consentViewState.update { currentState ->
                currentState.data?.let { data ->
                    UiState.Data(
                        data.copy(
                            isGrantingConsent = true,
                            grantConsentError = null
                        )
                    )
                } ?: currentState
            }

            val profile = currentProfileState.data
            val grantResult = profile?.let { grantEuPrescriptionConsentUseCase(it) }

            grantResult?.let {
                if (it.isSuccess) {
                    reloadConsentAfterGrant(profile)
                } else {
                    val error = grantResult.exceptionOrNull()
                    _consentViewState.update {
                        UiState.Error(error ?: Exception(context.getString(R.string.eu_consent_unknown_grant_error)))
                    }
                }
            }
        }
    }

    private suspend fun reloadConsentAfterGrant(profile: ProfilesUseCaseData.Profile) {
        val result = getEuPrescriptionConsentUseCase(profile.id)

        result.fold(
            onSuccess = { consent ->
                val isConsentActive = consent.isActive()
                _consentViewState.update {
                    UiState.Data(
                        EuConsentViewState(
                            consentData = consent,
                            isGrantingConsent = false,
                            grantConsentError = null
                        )
                    )
                }
                if (isConsentActive) {
                    handleConsentGranted()
                }
            },
            onFailure = { error ->
                _consentViewState.update { currentState ->
                    currentState.data?.let { data ->
                        UiState.Data(
                            data.copy(
                                isGrantingConsent = false,
                                grantConsentError = error
                            )
                        )
                    } ?: UiState.Error(error)
                }
            }
        )
    }

    fun onRevokeEuConsent() {
        val currentConsentState = _consentViewState.value
        val currentProfileState = activeProfile.value

        if (currentConsentState.isLoading ||
            currentConsentState.data == null ||
            !currentProfileState.isDataState
        ) {
            return
        }

        val profile = currentProfileState.data ?: return

        controllerScope.launch {
            _consentViewState.update { currentState ->
                currentState.data?.let { data ->
                    UiState.Data(
                        data.copy(
                            isRevokingConsent = true,
                            revokeConsentError = null
                        )
                    )
                } ?: currentState
            }

            try {
                revokeEuConsentUseCase(profile.id, ConsentCategory.EUCONSENT).collect { state ->
                    reloadConsentAfterRevoke(profile)
                }
            } catch (error: Exception) {
                _consentViewState.update { currentState ->
                    currentState.data?.let { data ->
                        UiState.Data(
                            data.copy(
                                isRevokingConsent = false,
                                revokeConsentError = error
                            )
                        )
                    } ?: UiState.Error(error)
                }
            }
        }
    }

    private suspend fun reloadConsentAfterRevoke(profile: ProfilesUseCaseData.Profile) {
        val result = getEuPrescriptionConsentUseCase(profile.id)

        result.fold(
            onSuccess = { consent ->
                _consentViewState.update {
                    UiState.Data(
                        EuConsentViewState(
                            consentData = consent,
                            isRevokingConsent = false,
                            revokeConsentError = null
                        )
                    )
                }
                handleConsentRevoked()
            },
            onFailure = { error ->
                _consentViewState.update { currentState ->
                    currentState.data?.let { data ->
                        UiState.Data(
                            data.copy(
                                isRevokingConsent = false,
                                revokeConsentError = error
                            )
                        )
                    } ?: UiState.Error(error)
                }
            }
        )
    }

    private suspend fun handleConsentGranted() {
        _navigationEvents.emit(EuConsentNavigationEvent.NavigateToRedeem)
    }

    private suspend fun handleConsentRevoked() {
        _navigationEvents.emit(EuConsentNavigationEvent.NavigateBack)
    }

    fun onBackPressed() {
        controllerScope.launch {
            _navigationEvents.emit(EuConsentNavigationEvent.NavigateBack)
        }
    }

    fun onCancelFlow() {
        controllerScope.launch {
            _navigationEvents.emit(EuConsentNavigationEvent.CancelFlow)
        }
    }

    fun onDeclineConsent() {
        controllerScope.launch {
            _navigationEvents.emit(EuConsentNavigationEvent.NavigateBack)
        }
    }
}

@Composable
fun rememberProfileEuConsentController(): ProfileEuConsentScreenController {
    val getEuPrescriptionConsentUseCase by rememberInstance<GetEuPrescriptionConsentUseCase>()
    val grantEuPrescriptionConsentUseCase by rememberInstance<GrantEuPrescriptionConsentUseCase>()
    val revokeEuConsentUseCase by rememberInstance<RevokeConsentUseCase>()
    val getActiveProfileUseCase by rememberInstance<GetActiveProfileUseCase>()
    val context = LocalContext.current

    return remember {
        ProfileEuConsentScreenController(
            getEuPrescriptionConsentUseCase = getEuPrescriptionConsentUseCase,
            grantEuPrescriptionConsentUseCase = grantEuPrescriptionConsentUseCase,
            getActiveProfileUseCase = getActiveProfileUseCase,
            revokeEuConsentUseCase = revokeEuConsentUseCase,
            context = context
        )
    }
}
