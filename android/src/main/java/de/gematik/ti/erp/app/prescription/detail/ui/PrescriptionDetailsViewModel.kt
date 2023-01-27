/*
 * Copyright (c) 2023 gematik GmbH
 * 
 * Licensed under the EUPL, Version 1.2 or – as soon they will be approved by
 * the European Commission - subsequent versions of the EUPL (the Licence);
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at:
 * 
 *     https://joinup.ec.europa.eu/software/page/eupl
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the Licence is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Licence for the specific language governing permissions and
 * limitations under the Licence.
 * 
 */

package de.gematik.ti.erp.app.prescription.detail.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import de.gematik.ti.erp.app.DispatchProvider
import de.gematik.ti.erp.app.prescription.detail.ui.model.PrescriptionData
import de.gematik.ti.erp.app.prescription.usecase.PrescriptionUseCase
import de.gematik.ti.erp.app.profiles.repository.ProfileIdentifier
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

class PrescriptionDetailsViewModel(
    val prescriptionUseCase: PrescriptionUseCase,
    private val dispatchers: DispatchProvider
) : ViewModel(), DeletePrescriptionsBridge {

    suspend fun screenState(taskId: String): Flow<PrescriptionData.Prescription> =
        prescriptionUseCase.generatePrescriptionDetails(taskId)

    fun redeemScannedTask(taskId: String, redeem: Boolean) {
        viewModelScope.launch(dispatchers.IO) {
            prescriptionUseCase.redeemScannedTask(taskId, redeem)
        }
    }

    override suspend fun deletePrescription(profileId: ProfileIdentifier, taskId: String): Result<Unit> =
        prescriptionUseCase.deletePrescription(profileId = profileId, taskId = taskId)
}
