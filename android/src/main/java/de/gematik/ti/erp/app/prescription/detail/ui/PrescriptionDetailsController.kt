/*
 * Copyright (c) 2024 gematik GmbH
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

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.remember
import de.gematik.ti.erp.app.prescription.detail.ui.model.PrescriptionData
import de.gematik.ti.erp.app.prescription.usecase.PrescriptionUseCase
import de.gematik.ti.erp.app.profiles.repository.ProfileIdentifier
import kotlinx.coroutines.flow.Flow
import org.kodein.di.compose.rememberInstance

@Stable
class PrescriptionDetailsController(
    val prescriptionUseCase: PrescriptionUseCase
) : DeletePrescriptionsBridge {
    suspend fun prescriptionDetailsFlow(taskId: String): Flow<PrescriptionData.Prescription> =
        prescriptionUseCase.generatePrescriptionDetails(taskId)

    suspend fun redeemScannedTask(taskId: String, redeem: Boolean) {
        prescriptionUseCase.redeemScannedTask(taskId, redeem)
    }

    override suspend fun deletePrescription(profileId: ProfileIdentifier, taskId: String): Result<Unit> =
        prescriptionUseCase.deletePrescription(profileId = profileId, taskId = taskId)
}

@Composable
fun rememberPrescriptionDetailsController(): PrescriptionDetailsController {
    val prescriptionUseCase by rememberInstance<PrescriptionUseCase>()

    return remember {
        PrescriptionDetailsController(prescriptionUseCase)
    }
}
