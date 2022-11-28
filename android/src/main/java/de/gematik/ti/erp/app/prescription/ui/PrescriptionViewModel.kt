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

import androidx.lifecycle.viewModelScope
import de.gematik.ti.erp.app.DispatchProvider
import androidx.lifecycle.ViewModel
import de.gematik.ti.erp.app.prescription.ui.model.PrescriptionScreenData
import de.gematik.ti.erp.app.prescription.usecase.PrescriptionUseCase
import de.gematik.ti.erp.app.profiles.usecase.ProfilesUseCase
import de.gematik.ti.erp.app.profiles.usecase.activeProfile
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.launch

class PrescriptionViewModel(
    private val prescriptionUseCase: PrescriptionUseCase,
    private val profilesUseCase: ProfilesUseCase,
    private val dispatchers: DispatchProvider
) : ViewModel() {
    private val timeTrigger = MutableSharedFlow<Unit>()

    init {
        viewModelScope.launch {
            while (true) {
                delay(timeMillis = 1000L * 60L)
                timeTrigger.emit(Unit)
            }
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    fun screenState(): Flow<PrescriptionScreenData.State> =
        profilesUseCase.profiles.map { it.activeProfile() }.flatMapLatest { activeProfile ->
            val prescriptionFlow = combine(
                prescriptionUseCase.scannedActiveRecipes(activeProfile.id),
                timeTrigger
                    .onStart { emit(Unit) }
                    .flatMapLatest { prescriptionUseCase.syncedActiveRecipes(activeProfile.id) }
                    .distinctUntilChanged()
            ) { lowDetail, fullDetail ->
                (lowDetail + fullDetail)
            }

            combine(
                prescriptionFlow,
                prescriptionUseCase.redeemedPrescriptions(activeProfile.id)
            ) { prescriptions, redeemed ->
                // TODO: split redeemed & unredeemed
                PrescriptionScreenData.State(
                    prescriptions = prescriptions,
                    redeemedPrescriptions = redeemed
                )
            }
        }.distinctUntilChanged().flowOn(dispatchers.Default)
}
