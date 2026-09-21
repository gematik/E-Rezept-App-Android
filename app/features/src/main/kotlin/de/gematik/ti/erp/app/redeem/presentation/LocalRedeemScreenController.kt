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
import de.gematik.ti.erp.app.pharmacy.model.PrescriptionInOrderErpModel
import de.gematik.ti.erp.app.profiles.usecase.GetActiveProfileUseCase
import de.gematik.ti.erp.app.profiles.usecase.GetProfileByIdUseCase
import de.gematik.ti.erp.app.profiles.usecase.GetProfilesUseCase
import de.gematik.ti.erp.app.redeem.model.DMCode
import de.gematik.ti.erp.app.redeem.ui.model.LocalRedeemTab
import de.gematik.ti.erp.app.redeem.usecase.GetDMCodesForLocalRedeemUseCase
import de.gematik.ti.erp.app.redeem.usecase.GetRedeemableTasksForDmCodesUseCase
import de.gematik.ti.erp.app.redeem.usecase.HasEuRedeemablePrescriptionsUseCase
import de.gematik.ti.erp.app.redeem.usecase.RedeemScannedTasksUseCase
import de.gematik.ti.erp.app.utils.compose.ComposableEvent
import de.gematik.ti.erp.app.utils.uistate.UiState
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
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
    private val _selectedTab: MutableStateFlow<LocalRedeemTab> = MutableStateFlow(LocalRedeemTab.MultiCode)
) : ChooseAuthenticationController(
    getProfileByIdUseCase = getProfileByIdUseCase,
    getProfilesUseCase = getProfilesUseCase,
    chooseAuthenticationDataUseCase = chooseAuthenticationDataUseCase,
    networkStatusTracker = networkStatusTracker,
    biometricAuthenticator = biometricAuthenticator,
    getActiveProfileUseCase = getActiveProfileUseCase
) {
    val selectedTab: StateFlow<LocalRedeemTab> = _selectedTab

    @OptIn(ExperimentalCoroutinesApi::class)
    val prescriptionOrders: StateFlow<List<PrescriptionInOrderErpModel>> by lazy {
        activeProfile
            .map { it.data?.id }
            .distinctUntilChanged()
            .flatMapLatest { profileId ->
                profileId?.let {
                    getRedeemableTasksForDmCodesUseCase(profileId)
                } ?: flowOf(emptyList())
            }
            .map { list ->
                if (taskId.isNotEmpty()) {
                    list.filter { it.taskId == taskId }
                } else {
                    list
                }
            }
            .stateIn(controllerScope, SharingStarted.WhileSubscribed(), emptyList())
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    val dmCodes: StateFlow<UiState<List<DMCode>>> by lazy {
        getDMCodesForLocalRedeemUseCase(prescriptionOrders, _selectedTab)
            .map { list ->
                if (list.isEmpty()) UiState.Empty() else UiState.Data(list)
            }
            .catch { emit(UiState.Error(it)) }
            .stateIn(controllerScope, SharingStarted.WhileSubscribed(), UiState.Loading())
    }

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

    fun onSelectTab(index: Int) {
        _selectedTab.update {
            LocalRedeemTab.entries.first { it.index == index }
        }
    }

    fun redeemPrescriptions() {
        controllerScope.launch {
            redeemScannedTasksUseCase(
                prescriptionOrders.value.map { it.taskId }
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
