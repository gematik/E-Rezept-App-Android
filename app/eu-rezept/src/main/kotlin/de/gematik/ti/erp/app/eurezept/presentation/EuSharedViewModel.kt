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
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavController
import de.gematik.ti.erp.app.eurezept.domain.model.Country
import de.gematik.ti.erp.app.eurezept.domain.model.EuPrescription
import de.gematik.ti.erp.app.eurezept.domain.model.EuRedemptionDetails
import de.gematik.ti.erp.app.eurezept.domain.model.PrescriptionFilter
import de.gematik.ti.erp.app.eurezept.domain.usecase.GenerateEuAccessCodeUseCase
import de.gematik.ti.erp.app.eurezept.domain.usecase.GetEuPrescriptionConsentUseCase
import de.gematik.ti.erp.app.eurezept.domain.usecase.GetEuPrescriptionsUseCase
import de.gematik.ti.erp.app.eurezept.navigation.EuRoutes
import de.gematik.ti.erp.app.eurezept.ui.model.EuAccessCodeGenerationError
import de.gematik.ti.erp.app.profiles.model.ProfileValidityResult.Companion.fold
import de.gematik.ti.erp.app.profiles.model.ProfileValidityResult.Companion.withValidSSOToken
import de.gematik.ti.erp.app.profiles.presentation.GetActiveProfileController
import de.gematik.ti.erp.app.profiles.usecase.GetActiveProfileUseCase
import de.gematik.ti.erp.app.utils.uistate.UiState
import de.gematik.ti.erp.app.viewmodel.rememberGraphScopedViewModel
import io.github.aakira.napier.Napier
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.transformLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.time.delay
import org.kodein.di.compose.rememberInstance
import java.time.Duration

internal abstract class EuSharedViewModel(
    getActiveProfileUseCase: GetActiveProfileUseCase
) : GetActiveProfileController(getActiveProfileUseCase) {

    // Prescription selection state
    abstract val selectedPrescriptions: StateFlow<List<EuPrescription>>
    abstract val selectedCountry: StateFlow<Country?>
    abstract val euRedemptionCode: StateFlow<UiState<EuRedemptionDetails>>
    abstract val isRedeemEnabled: StateFlow<Boolean>

    abstract val isRedemptionInProgress: StateFlow<Boolean>

    abstract fun generateEuAccessCode(
        onSuccessfulCodeGeneration: (() -> Unit)? = null,
        onFailure: ((EuAccessCodeGenerationError) -> Unit)? = null
    )

    abstract fun setSelectedPrescriptions(prescriptions: List<EuPrescription>)
    abstract fun setSelectedCountry(country: Country?)
    abstract fun setEuRedemptionCode(code: EuRedemptionDetails?)
    abstract fun clearSelection()
}

internal class DefaultEuSharedViewModel(
    private val getEuPrescriptionConsentUseCase: GetEuPrescriptionConsentUseCase,
    private val generateEuAccessCodeUseCase: GenerateEuAccessCodeUseCase,
    private val getEuPrescriptionsUseCase: GetEuPrescriptionsUseCase,
    getActiveProfileUseCase: GetActiveProfileUseCase
) : EuSharedViewModel(
    getActiveProfileUseCase = getActiveProfileUseCase
) {
    private val _selectedPrescriptions = MutableStateFlow<List<EuPrescription>>(emptyList())
    override val selectedPrescriptions: StateFlow<List<EuPrescription>> = _selectedPrescriptions.asStateFlow()

    private val _selectedCountry = MutableStateFlow<Country?>(null)
    override val selectedCountry: StateFlow<Country?> = _selectedCountry.asStateFlow()

    private val _isRedemptionInProgress = MutableStateFlow(false)
    override val isRedemptionInProgress: StateFlow<Boolean> = _isRedemptionInProgress.asStateFlow()

    private val _euRedemptionCode = MutableStateFlow<UiState<EuRedemptionDetails>>(UiState.Loading())

    @OptIn(ExperimentalCoroutinesApi::class)
    override val euRedemptionCode: StateFlow<UiState<EuRedemptionDetails>> = _euRedemptionCode.transformLatest { state ->
        while (currentCoroutineContext().isActive) {
            emit(
                state.data?.let {
                    UiState.Data(it.copy(isExpired = it.calculateIsExpired()))
                } ?: state
            )
            delay(Duration.ofSeconds(TIME_TO_RE_EMIT))
        }
    }.stateIn(
        scope = controllerScope,
        initialValue = UiState.Loading(),
        started = SharingStarted.Eagerly
    )

    override val isRedeemEnabled: StateFlow<Boolean> = combine(
        selectedPrescriptions,
        selectedCountry
    ) { prescriptions, country ->
        prescriptions.isNotEmpty() && country != null
    }.stateIn(
        scope = controllerScope,
        started = SharingStarted.WhileSubscribed(),
        initialValue = false
    )

    init {
        loadMarkedEuPrescriptions()
    }

    private fun loadMarkedEuPrescriptions() {
        controllerScope.launch {
            try {
                getEuPrescriptionsUseCase(PrescriptionFilter.EU_REDEEMABLE_ONLY)
                    .collect { euPrescriptions ->
                        _selectedPrescriptions.update { euPrescriptions.filter { it.isMarkedAsEuRedeemableByPatientAuthorization } }
                    }
            } catch (e: Exception) {
                Napier.e { "failed to load maarked EuPrescriptions ${e.message}" }
            }
        }
    }

    override fun generateEuAccessCode(
        onSuccessfulCodeGeneration: (() -> Unit)?,
        onFailure: ((EuAccessCodeGenerationError) -> Unit)?
    ) {
        try {
            controllerScope.launch {
                activeProfile.withValidSSOToken()
                    .fold(
                        onValid = { profile ->
                            _isRedemptionInProgress.update { true }
                            getEuPrescriptionConsentUseCase.invoke(profile.id)
                                .fold(
                                    onSuccess = { consent ->
                                        if (consent.isActive()) {
                                            generateEuAccessCodeUseCase.invoke(
                                                profile = profile,
                                                countryCode = selectedCountry.value?.code ?: "",
                                                relatedTaskIds = selectedPrescriptions.value.map { it.id }
                                            ).fold(
                                                onSuccess = { code ->
                                                    _isRedemptionInProgress.update { false }
                                                    _euRedemptionCode.update { UiState.Data(code) }
                                                    onSuccessfulCodeGeneration?.invoke()
                                                },
                                                onFailure = { error ->
                                                    onFailure?.invoke(EuAccessCodeGenerationError.ErrorGeneratingCode)
                                                    _isRedemptionInProgress.update { false }
                                                    _euRedemptionCode.update { UiState.Error(error) }
                                                }
                                            )
                                        } else {
                                            onFailure?.invoke(EuAccessCodeGenerationError.MissingConsent)
                                            _isRedemptionInProgress.update { false }
                                            Napier.e { "Content is missing, cannot generate code" }
                                        }
                                    },
                                    onFailure = { error ->
                                        onFailure?.invoke(EuAccessCodeGenerationError.MissingConsent)
                                        _isRedemptionInProgress.update { false }
                                        _euRedemptionCode.update { UiState.Error(error) }
                                    }
                                )
                        },
                        onInvalid = {
                            onFailure?.invoke(EuAccessCodeGenerationError.ErrorWithInvalidProfile)
                            _isRedemptionInProgress.update { false }
                            _euRedemptionCode.update { UiState.Error(IllegalStateException("Invalid profile")) }
                        }
                    )
            }
        } catch (e: Exception) {
            Napier.e(e) { "Error while generating code ${e.message}" }
            onFailure?.invoke(EuAccessCodeGenerationError.ErrorWithInvalidProfile)
            _isRedemptionInProgress.update { false }
            _euRedemptionCode.update { UiState.Error(e) }
        }
    }

    override fun setEuRedemptionCode(code: EuRedemptionDetails?) {
        _euRedemptionCode.update { UiState.Data(code) }
    }

    override fun setSelectedPrescriptions(prescriptions: List<EuPrescription>) {
        _selectedPrescriptions.update { prescriptions }
    }

    override fun setSelectedCountry(country: Country?) {
        _selectedCountry.update { country }
    }

    override fun clearSelection() {
        _selectedPrescriptions.update { emptyList() }
        _selectedCountry.update { null }
        _euRedemptionCode.update { UiState.Empty() }
    }
}

@Composable
internal fun euSharedViewModel(
    navController: NavController,
    entry: NavBackStackEntry
): EuSharedViewModel {
    val getActiveProfileUseCase by rememberInstance<GetActiveProfileUseCase>()
    val getEuPrescriptionConsentUseCase by rememberInstance<GetEuPrescriptionConsentUseCase>()
    val generateEuAccessCodeUseCase by rememberInstance<GenerateEuAccessCodeUseCase>()
    val getEuPrescriptionsUseCase by rememberInstance<GetEuPrescriptionsUseCase>()

    return rememberGraphScopedViewModel(
        navController = navController,
        navEntry = entry,
        graphRoute = EuRoutes.subGraphName()
    ) {
        DefaultEuSharedViewModel(
            getActiveProfileUseCase = getActiveProfileUseCase,
            getEuPrescriptionConsentUseCase = getEuPrescriptionConsentUseCase,
            generateEuAccessCodeUseCase = generateEuAccessCodeUseCase,
            getEuPrescriptionsUseCase = getEuPrescriptionsUseCase
        )
    }
}
