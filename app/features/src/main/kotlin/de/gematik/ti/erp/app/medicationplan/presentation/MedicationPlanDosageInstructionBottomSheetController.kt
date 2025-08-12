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

package de.gematik.ti.erp.app.medicationplan.presentation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.remember
import de.gematik.ti.erp.app.base.Controller
import de.gematik.ti.erp.app.medicationplan.model.MedicationPlanDosageInstruction
import de.gematik.ti.erp.app.medicationplan.usecase.GetDosageInstructionByTaskIdUseCase
import de.gematik.ti.erp.app.utils.uistate.UiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import org.kodein.di.compose.rememberInstance

@Stable
open class MedicationPlanDosageInstructionBottomSheetController(
    private val getDosageInstructionByTaskIdUseCase: GetDosageInstructionByTaskIdUseCase,
    private val taskId: String
) : Controller() {
    private val _dosageInstruction = MutableStateFlow<UiState<MedicationPlanDosageInstruction>>(
        UiState.Loading()
    )
    val dosageInstruction: StateFlow<UiState<MedicationPlanDosageInstruction>> = _dosageInstruction

    init {
        controllerScope.launch {
            runCatching {
                getDosageInstructionByTaskIdUseCase(taskId).first()
            }.onSuccess {
                _dosageInstruction.value = UiState.Data(it)
            }.onFailure {
                _dosageInstruction.value = UiState.Error(it)
            }
        }
    }
}

@Composable
fun rememberMedicationPlanDosageInstructionBottomSheetController(
    taskId: String
): MedicationPlanDosageInstructionBottomSheetController {
    val getDosageInstructionByTaskIdUseCase by rememberInstance<GetDosageInstructionByTaskIdUseCase>()
    return remember(taskId) {
        MedicationPlanDosageInstructionBottomSheetController(
            getDosageInstructionByTaskIdUseCase = getDosageInstructionByTaskIdUseCase,
            taskId = taskId
        )
    }
}
