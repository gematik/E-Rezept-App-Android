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

package de.gematik.ti.erp.app.prescription.detail.presentation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import de.gematik.ti.erp.app.authentication.presentation.AuthReason
import de.gematik.ti.erp.app.authentication.presentation.BiometricAuthenticator
import de.gematik.ti.erp.app.authentication.presentation.ChooseAuthenticationController
import de.gematik.ti.erp.app.authentication.usecase.ChooseAuthenticationDataUseCase
import de.gematik.ti.erp.app.base.NetworkStatusTracker
import de.gematik.ti.erp.app.base.usecase.IsFeatureToggleEnabledUseCase
import de.gematik.ti.erp.app.core.LocalBiometricAuthenticator
import de.gematik.ti.erp.app.database.datastore.featuretoggle.EU_REDEEM
import de.gematik.ti.erp.app.medicationplan.model.MedicationSchedule
import de.gematik.ti.erp.app.medicationplan.usecase.GetMedicationScheduleByTaskIdUseCase
import de.gematik.ti.erp.app.prescription.model.PrescriptionData
import de.gematik.ti.erp.app.prescription.usecase.DeletePrescriptionUseCase
import de.gematik.ti.erp.app.prescription.usecase.GetPrescriptionByTaskIdUseCase
import de.gematik.ti.erp.app.prescription.usecase.RedeemScannedTaskUseCase
import de.gematik.ti.erp.app.prescription.usecase.UpdateScannedTaskNameUseCase
import de.gematik.ti.erp.app.profiles.ui.extension.extract
import de.gematik.ti.erp.app.profiles.usecase.GetActiveProfileUseCase
import de.gematik.ti.erp.app.profiles.usecase.GetProfileByIdUseCase
import de.gematik.ti.erp.app.profiles.usecase.GetProfilesUseCase
import de.gematik.ti.erp.app.profiles.usecase.model.ProfilesUseCaseData
import de.gematik.ti.erp.app.utils.compose.ComposableEvent
import de.gematik.ti.erp.app.utils.compose.ComposableEvent.Companion.trigger
import de.gematik.ti.erp.app.utils.uistate.UiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.kodein.di.compose.rememberInstance

class PrescriptionDetailController(
    getProfileByIdUseCase: GetProfileByIdUseCase,
    getProfilesUseCase: GetProfilesUseCase,
    getActiveProfileUseCase: GetActiveProfileUseCase,
    chooseAuthenticationDataUseCase: ChooseAuthenticationDataUseCase,
    networkStatusTracker: NetworkStatusTracker,
    biometricAuthenticator: BiometricAuthenticator,

    private val taskId: String,
    private val redeemScannedTaskUseCase: RedeemScannedTaskUseCase,
    private val deletePrescriptionUseCase: DeletePrescriptionUseCase,
    private val loadMedicationScheduleByTaskIdUseCase: GetMedicationScheduleByTaskIdUseCase,
    private val getPrescriptionByTaskIdUseCase: GetPrescriptionByTaskIdUseCase,
    private val updateScannedTaskNameUseCase: UpdateScannedTaskNameUseCase,
    private val isFeatureToggleEnabledUseCase: IsFeatureToggleEnabledUseCase,
    private val _profilePrescription:
        MutableStateFlow<UiState<Pair<ProfilesUseCaseData.Profile, PrescriptionData.Prescription>>> =
            MutableStateFlow(UiState.Loading()),
    val profilePrescription: StateFlow<UiState<Pair<ProfilesUseCaseData.Profile, PrescriptionData.Prescription>>> =
        _profilePrescription
) : ChooseAuthenticationController(
    getProfileByIdUseCase = getProfileByIdUseCase,
    getProfilesUseCase = getProfilesUseCase,
    getActiveProfileUseCase = getActiveProfileUseCase,
    chooseAuthenticationDataUseCase = chooseAuthenticationDataUseCase,
    networkStatusTracker = networkStatusTracker,
    biometricAuthenticator = biometricAuthenticator,
    onActiveProfileSuccess = { profile, scope ->
        scope.launch {
            runCatching { getPrescriptionByTaskIdUseCase(taskId).first() }.fold(
                onSuccess = { _profilePrescription.value = UiState.Data(profile to it) },
                onFailure = { _profilePrescription.value = UiState.Error(it) }
            )
        }
    },
    onActiveProfileFailure = { error, _ ->
        _profilePrescription.value = UiState.Error(error)
    }
) {
    private val _euRedeemFeatureFlag: MutableStateFlow<Boolean> = MutableStateFlow(false)
    val euRedeemFeatureFlag: StateFlow<Boolean> = _euRedeemFeatureFlag

    val onBiometricAuthenticationSubmitSuccessEvent = ComposableEvent<Unit>()
    val onBiometricAuthenticationDeletedSuccessEvent = ComposableEvent<Unit>()

    init {
        biometricAuthenticationSuccessEvent.listen(controllerScope) { reason ->
            when (reason) {
                AuthReason.SUBMIT -> {
                    onBiometricAuthenticationSubmitSuccessEvent.trigger()
                }

                AuthReason.DELETED -> {
                    onBiometricAuthenticationDeletedSuccessEvent.trigger()
                }
            }
        }
        controllerScope.launch {
            isFeatureToggleEnabledUseCase.invoke(EU_REDEEM).collect { isEnabled ->
                _euRedeemFeatureFlag.update { isEnabled }
            }
        }
    }

    fun onRedeemInEuAbroadClick(): Boolean =
        activeProfile.value.data?.let { profile ->
            profile.isSSOTokenValid().also { authenticated ->
                if (!authenticated) chooseAuthenticationMethod(profile)
            }
        } ?: false

    val medicationSchedule: StateFlow<MedicationSchedule?> = loadMedicationScheduleByTaskIdUseCase(taskId)
        .stateIn(controllerScope, SharingStarted.WhileSubscribed(), null)

    private val _prescriptionDeleted by lazy {
        MutableStateFlow<DeletePrescriptionUseCase.DeletePrescriptionState>(
            DeletePrescriptionUseCase.DeletePrescriptionState.ValidState.NotDeleted
        )
    }

    val prescriptionDeleted: StateFlow<DeletePrescriptionUseCase.DeletePrescriptionState> by lazy {
        _prescriptionDeleted
    }

    fun redeemScannedTask(
        taskId: String,
        redeem: Boolean
    ) {
        controllerScope.launch {
            redeemScannedTaskUseCase(taskId, redeem)
            refreshActiveProfile()
        }
    }

    fun updateScannedTaskName(
        taskId: String,
        name: String
    ) {
        controllerScope.launch {
            updateScannedTaskNameUseCase(taskId, name)
            refreshActiveProfile()
        }
    }

    fun deletePrescription(
        isAuthenticationSuccess: Boolean = false
    ) = controllerScope.launch {
        activeProfile.extract()?.let { profile ->
            if (isAuthenticationSuccess || profile.isSSOTokenValid()) {
                deletePrescriptionUseCase(profile.id, taskId, false).first().apply {
                    _prescriptionDeleted.value = this as DeletePrescriptionUseCase.DeletePrescriptionState
                }
            } else {
                chooseAuthenticationMethod(profile, authenticationReason = AuthReason.DELETED)
                _profilePrescription.value = UiState.Loading()
            }
        }
    }

    fun deletePrescriptionFromLocal() {
        controllerScope.launch {
            activeProfile.extract()?.let { profile ->
                deletePrescriptionUseCase(profile.id, taskId, true).first().apply {
                    _prescriptionDeleted.value = this as DeletePrescriptionUseCase.DeletePrescriptionState
                }
            }
        }
    }

    fun resetDeletePrescriptionState() {
        _prescriptionDeleted.value = DeletePrescriptionUseCase.DeletePrescriptionState.ValidState.NotDeleted
    }
}

@Composable
fun rememberPrescriptionDetailController(taskId: String): PrescriptionDetailController {
    val networkStatusTracker by rememberInstance<NetworkStatusTracker>()
    val biometricAuthenticator = LocalBiometricAuthenticator.current
    val getProfilesUseCase by rememberInstance<GetProfilesUseCase>()
    val getProfileByIdUseCase by rememberInstance<GetProfileByIdUseCase>()
    val chooseAuthenticationDataUseCase by rememberInstance<ChooseAuthenticationDataUseCase>()
    val getPrescriptionByTaskIdUseCase by rememberInstance<GetPrescriptionByTaskIdUseCase>()
    val redeemScannedTaskUseCase by rememberInstance<RedeemScannedTaskUseCase>()
    val deletePrescriptionUseCase by rememberInstance<DeletePrescriptionUseCase>()
    val loadMedicationScheduleByTaskIdUseCase by rememberInstance<GetMedicationScheduleByTaskIdUseCase>()
    val updateScannedTaskNameUseCase by rememberInstance<UpdateScannedTaskNameUseCase>()
    val getActiveProfileUseCase by rememberInstance<GetActiveProfileUseCase>()
    val isFeatureToggleEnabledUseCase by rememberInstance<IsFeatureToggleEnabledUseCase>()

    return remember {
        PrescriptionDetailController(
            getProfileByIdUseCase = getProfileByIdUseCase,
            getProfilesUseCase = getProfilesUseCase,
            getActiveProfileUseCase = getActiveProfileUseCase,
            chooseAuthenticationDataUseCase = chooseAuthenticationDataUseCase,
            networkStatusTracker = networkStatusTracker,
            biometricAuthenticator = biometricAuthenticator,
            taskId = taskId,
            getPrescriptionByTaskIdUseCase = getPrescriptionByTaskIdUseCase,
            redeemScannedTaskUseCase = redeemScannedTaskUseCase,
            deletePrescriptionUseCase = deletePrescriptionUseCase,
            loadMedicationScheduleByTaskIdUseCase = loadMedicationScheduleByTaskIdUseCase,
            updateScannedTaskNameUseCase = updateScannedTaskNameUseCase,
            isFeatureToggleEnabledUseCase = isFeatureToggleEnabledUseCase
        )
    }
}
