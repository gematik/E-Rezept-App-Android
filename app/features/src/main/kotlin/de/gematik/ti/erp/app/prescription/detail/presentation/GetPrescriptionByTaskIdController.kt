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
import androidx.compose.runtime.remember
import de.gematik.ti.erp.app.base.Controller
import de.gematik.ti.erp.app.prescription.model.PrescriptionData
import de.gematik.ti.erp.app.prescription.usecase.GetPrescriptionByTaskIdUseCase
import de.gematik.ti.erp.app.utils.uistate.UiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import org.kodein.di.compose.rememberInstance

open class GetPrescriptionByTaskIdController(
    private val taskId: String,
    private val getPrescriptionByTaskIdUseCase: GetPrescriptionByTaskIdUseCase
) : Controller() {
    private val _prescription = MutableStateFlow<UiState<PrescriptionData.Prescription>>(UiState.Loading())
    val prescription: StateFlow<UiState<PrescriptionData.Prescription>> = _prescription

    init {
        initPrescription()
    }

    private fun initPrescription() {
        controllerScope.launch {
            runCatching {
                getPrescriptionByTaskIdUseCase(taskId).first()
            }.onSuccess {
                _prescription.value = UiState.Data(it)
            }.onFailure {
                _prescription.value = UiState.Error(it)
            }
        }
    }
}

@Composable
fun rememberGetPrescriptionByTaskIdController(
    taskId: String
): GetPrescriptionByTaskIdController {
    val getPrescriptionByTaskIdUseCase by rememberInstance<GetPrescriptionByTaskIdUseCase>()
    return remember {
        GetPrescriptionByTaskIdController(
            taskId = taskId,
            getPrescriptionByTaskIdUseCase = getPrescriptionByTaskIdUseCase
        )
    }
}
