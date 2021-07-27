/*
 * Copyright (c) 2021 gematik GmbH
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

import android.content.SharedPreferences
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import de.gematik.ti.erp.app.DispatchProvider
import de.gematik.ti.erp.app.api.Result
import de.gematik.ti.erp.app.db.entities.AuditEventSimple
import de.gematik.ti.erp.app.db.entities.LowDetailEventSimple
import de.gematik.ti.erp.app.di.NetworkSecureSharedPreferences
import de.gematik.ti.erp.app.prescription.detail.ui.model.UIPrescriptionDetail
import de.gematik.ti.erp.app.prescription.usecase.PrescriptionUseCase
import java.time.OffsetDateTime
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

@HiltViewModel
class PrescriptionDetailsViewModel @Inject constructor(
    val prescriptionUseCase: PrescriptionUseCase,
    @NetworkSecureSharedPreferences private val securePrefs: SharedPreferences,
    private val dispatchProvider: DispatchProvider
) : ViewModel() {
    suspend fun detailedPrescription(taskId: String): UIPrescriptionDetail =
        prescriptionUseCase.generatePrescriptionDetails(taskId)

    fun auditEvents(taskId: String): Flow<List<AuditEventSimple>> =
        prescriptionUseCase.loadAuditEvents(taskId).flowOn(dispatchProvider.unconfined())

    fun deletePrescription(taskId: String, isRemoteTask: Boolean): Result<Unit> {
        // TODO find better way than runBlocking
        return runBlocking(dispatchProvider.io()) {
            when (val r = prescriptionUseCase.deletePrescription(taskId, isRemoteTask)) {
                is Result.Error -> r
                is Result.Success -> {
                    prescriptionUseCase.deleteLowDetailEvents(taskId)
                    r
                }
            }
        }
    }

    fun onSwitchRedeemed(taskId: String, redeem: Boolean, all: Boolean, protocolText: String) {
        viewModelScope.launch(dispatchProvider.io()) {
            prescriptionUseCase.redeem(listOf(taskId), redeem, all)

            prescriptionUseCase.saveLowDetailEvent(
                LowDetailEventSimple(
                    protocolText,
                    OffsetDateTime.now(),
                    taskId
                )
            )
        }
    }

    suspend fun loadLowDetailEvents(taskId: String): Flow<List<LowDetailEventSimple>> {
        return prescriptionUseCase.loadLowDetailEvents(taskId)
            .flowOn(dispatchProvider.unconfined())
    }
}
