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
import de.gematik.ti.erp.app.base.Controller
import de.gematik.ti.erp.app.prescription.usecase.GetPrescriptionByTaskIdUseCase
import de.gematik.ti.erp.app.task.model.TaskErpModel
import de.gematik.ti.erp.app.task.model.TeratogenicPrescriptionErpModel
import de.gematik.ti.erp.app.utils.uistate.UiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.kodein.di.compose.rememberInstance

class PrescriptionDetailTeratogenicPrescriptionController(
    private val taskId: String,
    private val getPrescriptionByTaskIdUseCase: GetPrescriptionByTaskIdUseCase
) : Controller() {

    private val _teratogenicPrescription =
        MutableStateFlow<UiState<TeratogenicPrescriptionErpModel>>(UiState.Loading())
    val teratogenicPrescription: StateFlow<UiState<TeratogenicPrescriptionErpModel>> =
        _teratogenicPrescription

    init {
        loadTeratogenicPrescription()
    }

    private fun loadTeratogenicPrescription() {
        controllerScope.launch {
            runCatching {
                getPrescriptionByTaskIdUseCase(taskId).first()
            }.onSuccess { prescription ->
                when (prescription) {
                    is TaskErpModel.Synced.Prescription -> {
                        _teratogenicPrescription.update {
                            prescription.medicationRequest?.teratogenicPrescription?.let {
                                UiState.Data(it)
                            } ?: UiState.Empty()
                        }
                    }
                    else -> {
                        _teratogenicPrescription.update {
                            UiState.Empty()
                        }
                    }
                }
            }.onFailure { error ->
                _teratogenicPrescription.value = UiState.Error(error)
            }
        }
    }
}

@Composable
fun rememberPrescriptionDetailTeratogenicPrescriptionController(
    taskId: String
): PrescriptionDetailTeratogenicPrescriptionController {
    val getPrescriptionByTaskIdUseCase by rememberInstance<GetPrescriptionByTaskIdUseCase>()
    return remember {
        PrescriptionDetailTeratogenicPrescriptionController(
            taskId = taskId,
            getPrescriptionByTaskIdUseCase = getPrescriptionByTaskIdUseCase
        )
    }
}
