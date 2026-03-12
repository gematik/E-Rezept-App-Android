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

package de.gematik.ti.erp.app.pkv.consent.presentation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.remember
import de.gematik.ti.erp.app.api.ApiCallException
import de.gematik.ti.erp.app.api.HttpErrorState
import de.gematik.ti.erp.app.base.Controller
import de.gematik.ti.erp.app.consent.model.ConsentState
import de.gematik.ti.erp.app.consent.model.ConsentState.Companion.isConsentGranted
import de.gematik.ti.erp.app.consent.model.ConsentState.Companion.isNotGranted
import de.gematik.ti.erp.app.consent.usecase.GetConsentUseCase
import de.gematik.ti.erp.app.consent.usecase.GrantConsentUseCase
import de.gematik.ti.erp.app.consent.usecase.RevokeConsentUseCase
import de.gematik.ti.erp.app.consent.usecase.SaveGrantConsentDrawerShownUseCase
import de.gematik.ti.erp.app.fhir.consent.model.ConsentCategory
import de.gematik.ti.erp.app.pkv.consent.model.ConsentViewState
import de.gematik.ti.erp.app.profile.repository.ProfileIdentifier
import de.gematik.ti.erp.app.profiles.usecase.model.ProfilesUseCaseData
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.kodein.di.compose.rememberInstance

@Stable
class ConsentController(
    private val getConsentUseCase: GetConsentUseCase,
    private val grantConsentUseCase: GrantConsentUseCase,
    private val revokeConsentUseCase: RevokeConsentUseCase,
    private val saveGrantConsentDrawerShownUseCase: SaveGrantConsentDrawerShownUseCase
) : Controller() {
    private sealed interface RetryAction {
        data class FetchConsent(val profileId: ProfileIdentifier) : RetryAction
        data class GrantConsent(val profile: ProfilesUseCaseData.Profile) : RetryAction
        data class RevokeConsent(val profileId: ProfileIdentifier) : RetryAction
    }

    private var lastRetryAction: RetryAction? = null

    private val _consentViewState by lazy {
        MutableStateFlow(
            ConsentViewState(
                state = ConsentState.ValidState.UnknownConsent,
                errorState = HttpErrorState.Unknown
            )
        )
    }

    val consentViewState: StateFlow<ConsentViewState> = _consentViewState

    val isConsentGranted: StateFlow<Boolean> = consentViewState.map { it.state.isConsentGranted() }
        .stateIn(controllerScope, started = SharingStarted.Eagerly, initialValue = false)

    val isConsentNotGranted by lazy {
        consentViewState.map { it.state.isNotGranted() }
            .stateIn(
                controllerScope,
                started = SharingStarted.WhileSubscribed(),
                initialValue = false
            )
    }

    private fun setState(state: ConsentState) {
        _consentViewState.update { it.copy(state = state, errorState = HttpErrorState.Unknown) }
    }

    private fun setError(error: HttpErrorState) {
        _consentViewState.update { it.copy(errorState = error) }
    }

    private fun clearError() {
        _consentViewState.update { it.copy(errorState = HttpErrorState.Unknown) }
    }

    fun getChargeConsent(profileId: ProfileIdentifier) {
        lastRetryAction = RetryAction.FetchConsent(profileId)
        val previousState = consentViewState.value.state
        _consentViewState.update { it.copy(state = ConsentState.ValidState.Loading, errorState = HttpErrorState.Unknown) }

        controllerScope.launch {
            getConsentUseCase(profileId, ConsentCategory.PKVCONSENT.code).first().fold(
                onSuccess = { consent ->
                    val newState: ConsentState = if (consent.isActive()) {
                        ConsentState.ValidState.Granted
                    } else {
                        ConsentState.ValidState.NotGranted
                    }
                    setState(newState)
                },
                onFailure = { throwable ->
                    // Restore state so UI doesn't stay stuck in Loading
                    _consentViewState.update { it.copy(state = previousState) }
                    val httpError = (throwable as? ApiCallException)?.state ?: HttpErrorState.Unknown
                    setError(httpError)
                }
            )
        }
    }

    fun grantChargeConsent(profile: ProfilesUseCaseData.Profile) {
        lastRetryAction = RetryAction.GrantConsent(profile)
        clearError()
        controllerScope.launch {
            grantConsentUseCase(profile, ConsentCategory.PKVCONSENT).fold(
                onSuccess = {
                    setState(ConsentState.ValidState.Granted)
                },
                onFailure = { throwable ->
                    val httpError = (throwable as? ApiCallException)?.state ?: HttpErrorState.Unknown
                    setError(httpError)
                }
            )
        }
    }

    fun revokeChargeConsent(
        profileId: ProfileIdentifier,
        onSuccess: () -> Unit
    ) {
        lastRetryAction = RetryAction.RevokeConsent(profileId)
        clearError()
        controllerScope.launch {
            revokeConsentUseCase(profileId).fold(
                onSuccess = {
                    setState(ConsentState.ValidState.Revoked)
                    onSuccess()
                },
                onFailure = { throwable ->
                    val httpError = (throwable as? ApiCallException)?.state ?: HttpErrorState.Unknown
                    setError(httpError)
                }
            )
        }
    }

    fun onRetry(profile: ProfilesUseCaseData.Profile) {
        when (val action = lastRetryAction) {
            is RetryAction.FetchConsent -> getChargeConsent(action.profileId)
            is RetryAction.GrantConsent -> grantChargeConsent(action.profile)
            is RetryAction.RevokeConsent -> revokeChargeConsent(action.profileId) { }
            null -> {
                // Fallback for first-run / legacy callers
                when (consentViewState.value.state) {
                    is ConsentState.ValidState.UnknownConsent -> getChargeConsent(profile.id)
                    is ConsentState.ValidState.NotGranted -> grantChargeConsent(profile)
                    else -> Unit
                }
            }
        }
    }

    fun saveConsentDrawerShown(profileId: ProfileIdentifier) {
        controllerScope.launch {
            saveGrantConsentDrawerShownUseCase.invoke(profileId)
        }
    }
}

@Composable
fun rememberConsentController(): ConsentController {
    val getConsentUseCase by rememberInstance<GetConsentUseCase>()
    val grantConsentUseCase by rememberInstance<GrantConsentUseCase>()
    val revokeConsentUseCase by rememberInstance<RevokeConsentUseCase>()
    val saveGrantConsentDrawerShownUseCase by rememberInstance<SaveGrantConsentDrawerShownUseCase>()

    return remember {
        ConsentController(
            getConsentUseCase = getConsentUseCase,
            grantConsentUseCase = grantConsentUseCase,
            revokeConsentUseCase = revokeConsentUseCase,
            saveGrantConsentDrawerShownUseCase = saveGrantConsentDrawerShownUseCase
        )
    }
}
