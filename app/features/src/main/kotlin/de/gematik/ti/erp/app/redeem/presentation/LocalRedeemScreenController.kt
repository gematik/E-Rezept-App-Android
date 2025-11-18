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

package de.gematik.ti.erp.app.redeem.presentation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.remember
import de.gematik.ti.erp.app.authentication.presentation.AuthReason
import de.gematik.ti.erp.app.authentication.presentation.BiometricAuthenticator
import de.gematik.ti.erp.app.authentication.presentation.ChooseAuthenticationController
import de.gematik.ti.erp.app.authentication.usecase.ChooseAuthenticationDataUseCase
import de.gematik.ti.erp.app.base.NetworkStatusTracker
import de.gematik.ti.erp.app.base.usecase.IsFeatureToggleEnabledUseCase
import de.gematik.ti.erp.app.core.LocalBiometricAuthenticator
import de.gematik.ti.erp.app.database.datastore.featuretoggle.EU_REDEEM
import de.gematik.ti.erp.app.pharmacy.usecase.model.PharmacyUseCaseData
import de.gematik.ti.erp.app.profile.repository.ProfileIdentifier
import de.gematik.ti.erp.app.profiles.usecase.GetActiveProfileUseCase
import de.gematik.ti.erp.app.profiles.usecase.GetProfileByIdUseCase
import de.gematik.ti.erp.app.profiles.usecase.GetProfilesUseCase
import de.gematik.ti.erp.app.redeem.model.DMCode
import de.gematik.ti.erp.app.redeem.usecase.GetDMCodesForLocalRedeemUseCase
import de.gematik.ti.erp.app.redeem.usecase.GetRedeemableTasksForDmCodesUseCase
import de.gematik.ti.erp.app.redeem.usecase.HasEuRedeemablePrescriptionsUseCase
import de.gematik.ti.erp.app.redeem.usecase.RedeemScannedTasksUseCase
import de.gematik.ti.erp.app.utils.compose.ComposableEvent
import de.gematik.ti.erp.app.utils.uistate.UiState
import io.github.aakira.napier.Napier
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import org.kodein.di.compose.rememberInstance

@Suppress("ConstructorParameterNaming")
@Stable
class LocalRedeemScreenController(
    getProfileByIdUseCase: GetProfileByIdUseCase,
    getProfilesUseCase: GetProfilesUseCase,
    chooseAuthenticationDataUseCase: ChooseAuthenticationDataUseCase,
    networkStatusTracker: NetworkStatusTracker,
    biometricAuthenticator: BiometricAuthenticator,
    private val taskId: String,
    private val getActiveProfileUseCase: GetActiveProfileUseCase,
    private val getRedeemableTasksForDmCodesUseCase: GetRedeemableTasksForDmCodesUseCase,
    private val getDMCodesForLocalRedeemUseCase: GetDMCodesForLocalRedeemUseCase,
    private val redeemScannedTasksUseCase: RedeemScannedTasksUseCase,
    private val hasEuRedeemablePrescriptionsUseCase: HasEuRedeemablePrescriptionsUseCase,
    isFeatureToggleEnabledUseCase: IsFeatureToggleEnabledUseCase,
    private val _prescriptionOrders: MutableStateFlow<List<PharmacyUseCaseData.PrescriptionInOrder>> =
        MutableStateFlow(emptyList()),
    private val _showSingleCodes: MutableStateFlow<Boolean> = MutableStateFlow(false),
    private val _dmCodes: MutableStateFlow<UiState<List<DMCode>>> = MutableStateFlow(UiState.Loading())
) : ChooseAuthenticationController(
    getProfileByIdUseCase = getProfileByIdUseCase,
    getProfilesUseCase = getProfilesUseCase,
    chooseAuthenticationDataUseCase = chooseAuthenticationDataUseCase,
    networkStatusTracker = networkStatusTracker,
    biometricAuthenticator = biometricAuthenticator,
    getActiveProfileUseCase = getActiveProfileUseCase,
    onActiveProfileSuccess = { profile, coroutineScope ->
        coroutineScope.launch {
            runCatching {
                getRedeemableTasksForDmCodesUseCase(profile.id)
            }.fold(
                onSuccess = { flowList ->
                    val list = flowList.first()
                    if (list.isEmpty()) {
                        _dmCodes.value = UiState.Empty()
                        Napier.e { "no redeemable prescriptions found" }
                    } else {
                        if (taskId.isNotEmpty()) {
                            _prescriptionOrders.value = list.filter { it.taskId == taskId }
                        } else {
                            _prescriptionOrders.value = list
                        }
                    }
                },
                onFailure = {
                    Napier.e { "active prescriptions not found $it" }
                }
            )
            runCatching {
                getDMCodesForLocalRedeemUseCase.invoke(_prescriptionOrders, _showSingleCodes)
            }.fold(
                onSuccess = {
                    val list = it.first()
                    if (list.isEmpty()) {
                        _dmCodes.value = UiState.Empty()
                    } else {
                        _dmCodes.value = UiState.Data(list)
                    }
                },
                onFailure = {
                    _dmCodes.value = UiState.Error(it)
                }
            )
        }
    },
    onActiveProfileFailure = { throwable, scope ->
        scope.launch {
            Napier.e { "active profile not found $throwable" }
        }
    }
) {
    val showSingleCodes: StateFlow<Boolean> = _showSingleCodes
    val dmCodes: StateFlow<UiState<List<DMCode>>> = _dmCodes
    val prescriptionOrders: StateFlow<List<PharmacyUseCaseData.PrescriptionInOrder>> = _prescriptionOrders
    private val _activeProfileId: MutableStateFlow<ProfileIdentifier> = MutableStateFlow("")
    val activeProfileId: StateFlow<ProfileIdentifier> = _activeProfileId

    val euRedeemFeatureFlag: StateFlow<Boolean> =
        isFeatureToggleEnabledUseCase(EU_REDEEM)
            .stateIn(
                controllerScope,
                SharingStarted.WhileSubscribed(),
                false
            )

    @OptIn(ExperimentalCoroutinesApi::class)
    val hasEuRedeemablePrescriptions: StateFlow<Boolean> =
        activeProfile
            .map { it.data?.id }
            .distinctUntilChanged()
            .flatMapLatest { id ->
                id?.let { hasEuRedeemablePrescriptionsUseCase(it) } ?: flowOf(false)
            }
            .stateIn(
                controllerScope,
                SharingStarted.WhileSubscribed(),
                false
            )

    val onBiometricAuthenticationSuccessEvent = ComposableEvent<AuthReason>()

    init {
        biometricAuthenticationSuccessEvent.listen(controllerScope) { reason ->
            onBiometricAuthenticationSuccessEvent.trigger(reason)
        }
    }

    fun onRedeemInEuAbroadClick(): Boolean =
        activeProfile.value.data?.let { profile ->
            profile.isSSOTokenValid().also { authenticated ->
                if (!authenticated) chooseAuthenticationMethod(profile)
            }
        } ?: false

    fun switchSingleCode() {
        _showSingleCodes.value = !_showSingleCodes.value
    }

    fun redeemPrescriptions() {
        controllerScope.launch {
            redeemScannedTasksUseCase(
                prescriptionOrders.first().map { it.taskId }
            )
        }
    }

    fun refreshDmCodes() {
        _dmCodes.value = UiState.Loading()
        getActiveProfile()
        getRedeemableTasks()
        getDmCodes()
    }

    private fun getActiveProfile() {
        controllerScope.launch {
            runCatching {
                getActiveProfileUseCase().first()
            }.fold(
                onSuccess = {
                    _activeProfileId.value = it.id
                },
                onFailure = {
                    _dmCodes.value = UiState.Error(it)
                }
            )
        }
    }

    private fun getRedeemableTasks() {
        controllerScope.launch {
            runCatching {
                getRedeemableTasksForDmCodesUseCase(activeProfileId.value)
            }.fold(
                onSuccess = { flowList ->
                    val list = flowList.first()
                    if (list.isEmpty()) {
                        _dmCodes.value = UiState.Empty()
                        Napier.e { "no redeemable prescriptions found" }
                    } else {
                        if (taskId.isNotEmpty()) {
                            _prescriptionOrders.value = list.filter { it.taskId == taskId }
                        } else {
                            _prescriptionOrders.value = list
                        }
                    }
                },
                onFailure = {
                    Napier.e { "active prescriptions not found $it" }
                }
            )
        }
    }

    fun getDmCodes() {
        controllerScope.launch {
            runCatching {
                getDMCodesForLocalRedeemUseCase.invoke(_prescriptionOrders, _showSingleCodes)
            }.fold(
                onSuccess = {
                    val list = it.first()
                    if (list.isEmpty()) {
                        _dmCodes.value = UiState.Empty()
                    } else {
                        _dmCodes.value = UiState.Data(list)
                    }
                },
                onFailure = {
                    _dmCodes.value = UiState.Error(it)
                }
            )
        }
    }
}

@Composable
fun rememberLocalRedeemScreenController(taskId: String): LocalRedeemScreenController {
    val networkStatusTracker by rememberInstance<NetworkStatusTracker>()
    val biometricAuthenticator = LocalBiometricAuthenticator.current
    val getProfilesUseCase by rememberInstance<GetProfilesUseCase>()
    val getProfileByIdUseCase by rememberInstance<GetProfileByIdUseCase>()
    val chooseAuthenticationDataUseCase by rememberInstance<ChooseAuthenticationDataUseCase>()
    val getActiveProfileUseCase by rememberInstance<GetActiveProfileUseCase>()
    val getRedeemableTasksForDmCodesUseCase by rememberInstance<GetRedeemableTasksForDmCodesUseCase>()
    val getDMCodesForLocalRedeemUseCase by rememberInstance<GetDMCodesForLocalRedeemUseCase>()
    val redeemScannedTasksUseCase by rememberInstance<RedeemScannedTasksUseCase>()
    val isFeatureToggleEnabledUseCase by rememberInstance<IsFeatureToggleEnabledUseCase>()
    val hasEuRedeemablePrescriptionsUseCase by rememberInstance<HasEuRedeemablePrescriptionsUseCase>()
    return remember {
        LocalRedeemScreenController(
            getProfileByIdUseCase = getProfileByIdUseCase,
            getProfilesUseCase = getProfilesUseCase,
            chooseAuthenticationDataUseCase = chooseAuthenticationDataUseCase,
            networkStatusTracker = networkStatusTracker,
            biometricAuthenticator = biometricAuthenticator,
            taskId = taskId,
            getActiveProfileUseCase = getActiveProfileUseCase,
            getRedeemableTasksForDmCodesUseCase = getRedeemableTasksForDmCodesUseCase,
            getDMCodesForLocalRedeemUseCase = getDMCodesForLocalRedeemUseCase,
            redeemScannedTasksUseCase = redeemScannedTasksUseCase,
            isFeatureToggleEnabledUseCase = isFeatureToggleEnabledUseCase,
            hasEuRedeemablePrescriptionsUseCase = hasEuRedeemablePrescriptionsUseCase
        )
    }
}
