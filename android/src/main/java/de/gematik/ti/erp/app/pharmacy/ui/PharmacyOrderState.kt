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

package de.gematik.ti.erp.app.pharmacy.ui

import androidx.annotation.VisibleForTesting
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import de.gematik.ti.erp.app.DispatchProvider
import de.gematik.ti.erp.app.core.complexAutoSaver
import de.gematik.ti.erp.app.pharmacy.ui.model.PharmacyScreenData
import de.gematik.ti.erp.app.pharmacy.usecase.PharmacySearchUseCase
import de.gematik.ti.erp.app.pharmacy.usecase.model.PharmacyUseCaseData
import de.gematik.ti.erp.app.profiles.ui.LocalProfileHandler
import de.gematik.ti.erp.app.profiles.usecase.model.ProfilesUseCaseData
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.shareIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.kodein.di.compose.rememberInstance

@Stable
class PharmacyOrderState(
    val profile: ProfilesUseCaseData.Profile,
    private val useCase: PharmacySearchUseCase,
    private val scope: CoroutineScope
) {

    var selectedPharmacy: PharmacyUseCaseData.Pharmacy? by mutableStateOf(null)
        private set

    var selectedOrderOption: PharmacyScreenData.OrderOption? by mutableStateOf(null)
        private set

    fun onSelectPharmacy(pharmacy: PharmacyUseCaseData.Pharmacy, orderOption: PharmacyScreenData.OrderOption) {
        selectedPharmacy = pharmacy
        selectedOrderOption = orderOption
    }

    private var unSelectedPrescriptions: MutableStateFlow<List<String>> = MutableStateFlow(emptyList())

    fun onSelectPrescription(order: PharmacyUseCaseData.PrescriptionOrder) {
        unSelectedPrescriptions.update {
            it - order.taskId
        }
    }

    fun onDeselectPrescription(order: PharmacyUseCaseData.PrescriptionOrder) {
        unSelectedPrescriptions.update {
            it + order.taskId
        }
    }

    private val prescriptionOrderFlow =
        useCase
            .prescriptionDetailsForOrdering(profile.id)
            .shareIn(scope, SharingStarted.Lazily, 1)

    private val hasRedeemableTasksFlow =
        prescriptionOrderFlow.map { it.prescriptions.isNotEmpty() }

    val hasRedeemableTasks
        @Composable
        get() = hasRedeemableTasksFlow.collectAsState(false)

    @VisibleForTesting
    val orderFlow =
        combine(
            unSelectedPrescriptions,
            prescriptionOrderFlow
        ) { unSelectedPrescriptions, prescriptionOrder ->
            prescriptionOrder.copy(
                prescriptions = prescriptionOrder.prescriptions.filter {
                    it.taskId !in unSelectedPrescriptions
                }
            )
        }

    val order
        @Composable
        get() = orderFlow.collectAsState(PharmacyUseCaseData.OrderState.Empty)

    private val prescriptionFlow =
        prescriptionOrderFlow.map {
            it.prescriptions
        }

    val prescriptions
        @Composable
        get() = prescriptionFlow.collectAsState(emptyList())

    fun onSaveContact(contact: PharmacyUseCaseData.ShippingContact) {
        scope.launch {
            useCase.saveShippingContact(contact)
        }
    }

    fun onResetPharmacySelection() {
        selectedPharmacy = null
        selectedOrderOption = null
    }

    fun onResetPrescriptionSelection() {
        unSelectedPrescriptions.value = emptyList()
    }
}

@Composable
fun rememberPharmacyOrderState(): PharmacyOrderState {
    val activeProfile = LocalProfileHandler.current.activeProfile
    val useCase by rememberInstance<PharmacySearchUseCase>()
    val dispatchProvider by rememberInstance<DispatchProvider>()
    return rememberSaveable(
        activeProfile,
        saver = complexAutoSaver()
    ) {
        PharmacyOrderState(
            activeProfile,
            useCase,
            CoroutineScope(dispatchProvider.Default)
        )
    }
}
