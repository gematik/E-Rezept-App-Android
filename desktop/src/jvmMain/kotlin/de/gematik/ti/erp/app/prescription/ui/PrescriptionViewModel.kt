/*
 * Copyright (c) 2022 gematik GmbH
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

package de.gematik.ti.erp.app.prescription.ui

import de.gematik.ti.erp.app.core.DispatchersProvider
import de.gematik.ti.erp.app.prescription.ui.model.PrescriptionScreenData
import de.gematik.ti.erp.app.prescription.usecase.PrescriptionUseCase
import de.gematik.ti.erp.app.prescription.usecase.model.PrescriptionUseCaseData
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.onSubscription
import kotlinx.coroutines.flow.transform
import kotlinx.coroutines.flow.transformLatest
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.kodein.di.bindings.ScopeCloseable

class PrescriptionViewModel(
    private val dispatchersProvider: DispatchersProvider,
    private val prescriptionUseCase: PrescriptionUseCase,
) : ScopeCloseable {
    private val deleteScope = CoroutineScope(dispatchersProvider.io())
    private val deleteResult = MutableSharedFlow<Result<Unit>>()
    private val selectedPrescription = MutableSharedFlow<String?>()
    private val prescriptionType = MutableStateFlow(PrescriptionUseCase.PrescriptionType.NotDispensed)

    val defaultState =
        PrescriptionScreenData.State(emptyList(), PrescriptionUseCase.PrescriptionType.NotDispensed, null, emptyList())

    @OptIn(ExperimentalCoroutinesApi::class)
    fun screenState(): Flow<PrescriptionScreenData.State> {
        return prescriptionType.flatMapLatest { type ->
            prescriptionUseCase.prescriptions(type).flatMapLatest { prescriptions ->
                selectedPrescription.onSubscription {
                    emit(prescriptions.firstOrNull()?.taskId)
                }.transformLatest { selected ->
                    if (selected == null) {
                        emit(
                            PrescriptionScreenData.State(
                                prescriptions = prescriptions,
                                prescriptionsType = type,
                                selectedPrescription = null,
                                selectedPrescriptionAudits = emptyList(),
                            )
                        )
                    } else {
                        emitAll(
                            combine(
                                prescriptionUseCase.prescriptionDetails(selected),
                                prescriptionUseCase.audits(selected)
                            ) { details, audits ->
                                PrescriptionScreenData.State(
                                    prescriptions = prescriptions,
                                    prescriptionsType = type,
                                    selectedPrescription = details,
                                    selectedPrescriptionAudits = audits,
                                )
                            }
                        )
                    }
                }
            }
        }
    }

    fun deleteState() = deleteResult.transform {
        if (it.isFailure) {
            it.exceptionOrNull()?.let {
                emit(PrescriptionScreenData.DeleteState(it.message))
            }
        }
    }

    suspend fun onSelectPrescription(prescription: PrescriptionUseCaseData.Prescription) {
        selectedPrescription.emit(prescription.taskId)
    }

    suspend fun onSelectDispensed() {
        prescriptionType.emit(PrescriptionUseCase.PrescriptionType.Dispensed)
    }

    suspend fun onSelectNotDispensed() {
        prescriptionType.emit(PrescriptionUseCase.PrescriptionType.NotDispensed)
    }

    suspend fun update() = withContext(dispatchersProvider.io()) {
        prescriptionUseCase.update()
    }

    fun deletePrescription(prescription: PrescriptionUseCaseData.Prescription) {
        deleteScope.launch {
            val r = prescriptionUseCase.delete(prescription.taskId)
            if (r.isSuccess) {
                prescriptionUseCase.update()
            }
            deleteResult.emit(r)
        }
    }

    override fun close() {
        deleteScope.cancel()
    }
}
