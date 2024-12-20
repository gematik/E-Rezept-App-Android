/*
 * Copyright 2024, gematik GmbH
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
 * In case of changes by gematik find details in the "Readme" file.
 *
 * See the Licence for the specific language governing permissions and limitations under the Licence.
 */

package de.gematik.ti.erp.app.prescription.detail.presentation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.remember
import de.gematik.ti.erp.app.base.presentation.GetActiveProfileController
import de.gematik.ti.erp.app.featuretoggle.FeatureToggleManager
import de.gematik.ti.erp.app.featuretoggle.Features
import de.gematik.ti.erp.app.medicationplan.model.MedicationSchedule
import de.gematik.ti.erp.app.medicationplan.usecase.LoadMedicationScheduleByTaskIdUseCase
import de.gematik.ti.erp.app.prescription.model.PrescriptionData
import de.gematik.ti.erp.app.prescription.usecase.DeletePrescriptionUseCase
import de.gematik.ti.erp.app.prescription.usecase.GetPrescriptionByTaskIdUseCase
import de.gematik.ti.erp.app.prescription.usecase.RedeemScannedTaskUseCase
import de.gematik.ti.erp.app.prescription.usecase.UpdateScannedTaskNameUseCase
import de.gematik.ti.erp.app.profiles.repository.ProfileIdentifier
import de.gematik.ti.erp.app.profiles.usecase.GetActiveProfileUseCase
import de.gematik.ti.erp.app.profiles.usecase.model.ProfilesUseCaseData
import de.gematik.ti.erp.app.utils.uistate.UiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import org.kodein.di.compose.rememberInstance

@Suppress("ConstructorParameterNaming")
@Stable
class PrescriptionDetailController(
    getActiveProfileUseCase: GetActiveProfileUseCase,
    featureToggleManager: FeatureToggleManager,
    private val taskId: String,
    private val redeemScannedTaskUseCase: RedeemScannedTaskUseCase,
    private val deletePrescriptionUseCase: DeletePrescriptionUseCase,
    private val loadMedicationScheduleByTaskIdUseCase: LoadMedicationScheduleByTaskIdUseCase,
    private val getPrescriptionByTaskIdUseCase: GetPrescriptionByTaskIdUseCase,
    private val updateScannedTaskNameUseCase: UpdateScannedTaskNameUseCase,
    private val _profilePrescription:
        MutableStateFlow<UiState<Pair<ProfilesUseCaseData.Profile, PrescriptionData.Prescription>>> =
            MutableStateFlow(UiState.Loading()),
    val profilePrescription: StateFlow<UiState<Pair<ProfilesUseCaseData.Profile, PrescriptionData.Prescription>>> =
        _profilePrescription
) : GetActiveProfileController(
    getActiveProfileUseCase = getActiveProfileUseCase,
    onSuccess = { profile, scope ->
        scope.launch {
            runCatching {
                getPrescriptionByTaskIdUseCase(taskId).first()
            }.fold(
                onSuccess = {
                    _profilePrescription.value = UiState.Data(profile to it)
                },
                onFailure = {
                    _profilePrescription.value = UiState.Error(it)
                }
            )
        }
    },
    onFailure = { error, _ ->
        _profilePrescription.value = UiState.Error(error)
    }
) {

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

    val isMedicationPlanEnabled: StateFlow<Boolean> =
        featureToggleManager.isFeatureEnabled(Features.MEDICATION_PLAN)
            .stateIn(
                controllerScope,
                SharingStarted.WhileSubscribed(),
                false
            )

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
        profileId: ProfileIdentifier,
        taskId: String
    ) = controllerScope.launch {
        deletePrescriptionUseCase(profileId, taskId, false).first().apply {
            _prescriptionDeleted.value = this as DeletePrescriptionUseCase.DeletePrescriptionState
        }
    }

    fun deletePrescriptionFromLocal(
        profileId: ProfileIdentifier,
        taskId: String
    ) {
        controllerScope.launch {
            deletePrescriptionUseCase(profileId, taskId, true).first().apply {
                _prescriptionDeleted.value = this as DeletePrescriptionUseCase.DeletePrescriptionState
            }
        }
    }

    fun resetDeletePrescriptionState() {
        _prescriptionDeleted.value = DeletePrescriptionUseCase.DeletePrescriptionState.ValidState.NotDeleted
    }
}

@Composable
fun rememberPrescriptionDetailController(taskId: String): PrescriptionDetailController {
    val getPrescriptionByTaskIdUseCase by rememberInstance<GetPrescriptionByTaskIdUseCase>()
    val redeemScannedTaskUseCase by rememberInstance<RedeemScannedTaskUseCase>()
    val deletePrescriptionUseCase by rememberInstance<DeletePrescriptionUseCase>()
    val loadMedicationScheduleByTaskIdUseCase by rememberInstance<LoadMedicationScheduleByTaskIdUseCase>()
    val updateScannedTaskNameUseCase by rememberInstance<UpdateScannedTaskNameUseCase>()
    val getActiveProfileUseCase by rememberInstance<GetActiveProfileUseCase>()
    val featureToggleManager by rememberInstance<FeatureToggleManager>()
    return remember {
        PrescriptionDetailController(
            taskId = taskId,
            getPrescriptionByTaskIdUseCase = getPrescriptionByTaskIdUseCase,
            redeemScannedTaskUseCase = redeemScannedTaskUseCase,
            deletePrescriptionUseCase = deletePrescriptionUseCase,
            loadMedicationScheduleByTaskIdUseCase = loadMedicationScheduleByTaskIdUseCase,
            updateScannedTaskNameUseCase = updateScannedTaskNameUseCase,
            getActiveProfileUseCase = getActiveProfileUseCase,
            featureToggleManager = featureToggleManager
        )
    }
}
