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

package de.gematik.ti.erp.app.prescription.detail.presentation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import de.gematik.ti.erp.app.consent.usecase.GrantConsentUseCase
import de.gematik.ti.erp.app.prescription.detail.ui.DeletePrescriptionsBridge
import de.gematik.ti.erp.app.prescription.usecase.GeneratePrescriptionDetailsUseCase
import de.gematik.ti.erp.app.prescription.usecase.PrescriptionUseCase
import de.gematik.ti.erp.app.prescription.usecase.UpdateScannedTaskNameUseCase
import de.gematik.ti.erp.app.profiles.repository.ProfileIdentifier
import de.gematik.ti.erp.app.profiles.usecase.GetActiveProfileUseCase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import org.kodein.di.compose.rememberInstance

@Stable
class PrescriptionDetailController(
    private val taskId: String,
    private val prescriptionUseCase: PrescriptionUseCase,
    private val activeProfileUseCase: GetActiveProfileUseCase,
    private val grantConsentUseCase: GrantConsentUseCase,
    private val generatePrescriptionDetailsUseCase: GeneratePrescriptionDetailsUseCase,
    private val updateScannedTaskNameUseCase: UpdateScannedTaskNameUseCase,
    private val scope: CoroutineScope
) : DeletePrescriptionsBridge {

    private val prescription by lazy {
        generatePrescriptionDetailsUseCase(taskId).stateIn(scope, SharingStarted.Lazily, null)
    }

    private val activeProfile by lazy {
        activeProfileUseCase.invoke()
    }

    fun redeemScannedTask(taskId: String, redeem: Boolean) {
        scope.launch {
            prescriptionUseCase.redeemScannedTask(taskId, redeem)
        }
    }

    fun updateScannedTaskName(taskId: String, name: String) {
        scope.launch {
            updateScannedTaskNameUseCase.invoke(taskId, name)
        }
    }

    override suspend fun deletePrescription(profileId: ProfileIdentifier, taskId: String): Result<Unit> =
        prescriptionUseCase.deletePrescription(profileId = profileId, taskId = taskId)

    fun grantConsent() {
        scope.launch {
            val profile = activeProfile.first()
            grantConsentUseCase.invoke(profile.id, profile.insurance.insuranceIdentifier)
        }
    }

    val prescriptionState
        @Composable
        get() = prescription.collectAsStateWithLifecycle()

    val activeProfileState
        @Composable
        get() = activeProfile.collectAsStateWithLifecycle(initialValue = null)
}

@Composable
fun rememberPrescriptionDetailController(taskId: String): PrescriptionDetailController {
    val generatePrescriptionDetailsUseCase by rememberInstance<GeneratePrescriptionDetailsUseCase>()
    val prescriptionUseCase by rememberInstance<PrescriptionUseCase>()
    val updateScannedTaskNameUseCase by rememberInstance<UpdateScannedTaskNameUseCase>()
    val activeProfileUseCase by rememberInstance<GetActiveProfileUseCase>()
    val grantConsentUseCase by rememberInstance<GrantConsentUseCase>()
    val scope = rememberCoroutineScope()
    return remember {
        PrescriptionDetailController(
            taskId = taskId,
            generatePrescriptionDetailsUseCase = generatePrescriptionDetailsUseCase,
            prescriptionUseCase = prescriptionUseCase,
            updateScannedTaskNameUseCase = updateScannedTaskNameUseCase,
            activeProfileUseCase = activeProfileUseCase,
            grantConsentUseCase = grantConsentUseCase,
            scope = scope
        )
    }
}
