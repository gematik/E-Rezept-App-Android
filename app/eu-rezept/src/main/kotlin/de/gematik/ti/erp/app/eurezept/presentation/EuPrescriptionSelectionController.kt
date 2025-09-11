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

package de.gematik.ti.erp.app.eurezept.presentation

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import de.gematik.ti.erp.app.base.Controller
import de.gematik.ti.erp.app.core.R
import de.gematik.ti.erp.app.eurezept.domin.model.EuAvailabilityInfo
import de.gematik.ti.erp.app.eurezept.domin.model.EuPrescription
import de.gematik.ti.erp.app.eurezept.domin.model.PrescriptionFilter
import de.gematik.ti.erp.app.eurezept.domin.usecase.GetEuPrescriptionsUseCase
import de.gematik.ti.erp.app.prescription.model.PrescriptionType
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.kodein.di.compose.rememberInstance

internal class EuPrescriptionSelectionController(
    private val getEuPrescriptionsUseCase: GetEuPrescriptionsUseCase,
    private val context: Context,
    private val initialSelectedPrescriptions: List<EuPrescription> = emptyList()
) : Controller() {
    private val _selectedPrescriptions = MutableStateFlow<List<EuPrescription>>(emptyList())
    val selectedPrescriptions: StateFlow<List<EuPrescription>> = _selectedPrescriptions.asStateFlow()

    val selectedPrescriptionIds: StateFlow<Set<String>> = selectedPrescriptions.map { prescriptions ->
        prescriptions.map { it.id }.toSet()
    }.stateIn(
        scope = controllerScope,
        started = SharingStarted.WhileSubscribed(),
        initialValue = emptySet()
    )

    private val _hasAppliedInitialSelection = MutableStateFlow(false)

    val availableEuRedeemablePrescriptions: StateFlow<List<EuPrescription>> =
        getEuPrescriptionsUseCase(PrescriptionFilter.EU_REDEEMABLE_ONLY)
            .stateIn(
                scope = controllerScope,
                started = SharingStarted.WhileSubscribed(),
                initialValue = emptyList()
            )

    val prescriptions: StateFlow<List<EuPrescription>> =
        getEuPrescriptionsUseCase(PrescriptionFilter.ALL)
            .stateIn(
                scope = controllerScope,
                started = SharingStarted.WhileSubscribed(),
                initialValue = emptyList()
            )

    // Only EU prescriptions from the current selection
    val selectedAvailableEuRedeemablePrescriptions: StateFlow<List<EuPrescription>> = combine(
        selectedPrescriptions,
        availableEuRedeemablePrescriptions
    ) { selected, available ->
        selected.filter { selectedPrescription ->
            available.any { it.id == selectedPrescription.id }
        }
    }.stateIn(
        scope = controllerScope,
        started = SharingStarted.WhileSubscribed(),
        initialValue = emptyList()
    )

    init {
        controllerScope.launch {
            availableEuRedeemablePrescriptions.collect { available ->
                if (!_hasAppliedInitialSelection.value && available.isNotEmpty()) {
                    if (initialSelectedPrescriptions.isNotEmpty()) {
                        // Filter prescriptions that are still available and EU-redeemable
                        val validSelectedPrescriptions = initialSelectedPrescriptions.filter { selectedPrescription ->
                            available.any { availablePrescription ->
                                availablePrescription.id == selectedPrescription.id
                            }
                        }
                        _selectedPrescriptions.update { validSelectedPrescriptions }
                    } else {
                        _selectedPrescriptions.update { emptyList() }
                    }
                    _hasAppliedInitialSelection.update { true }
                }
            }
        }
    }

    fun togglePrescriptionSelection(prescriptionId: String) {
        val availablePrescriptions = availableEuRedeemablePrescriptions.value

        _selectedPrescriptions.update { currentSelection ->
            if (currentSelection.any { it.id == prescriptionId }) {
                currentSelection.filterNot { it.id == prescriptionId }
            } else {
                availablePrescriptions.find { it.id == prescriptionId }?.let { prescription ->
                    currentSelection + prescription
                } ?: currentSelection
            }
        }
    }

    fun getAvailabilityInfo(prescription: EuPrescription): EuAvailabilityInfo {
        return when (prescription.type) {
            PrescriptionType.EuRezeptTask -> {
                EuAvailabilityInfo(
                    isAvailable = true,
                    expiryDate = prescription.expiryDate
                )
            }
            PrescriptionType.ScannedTask -> {
                EuAvailabilityInfo(
                    isAvailable = false,
                    reason = context.getString(R.string.eu_prescription_selection_scanned_not_available)
                )
            }
            PrescriptionType.SyncedTask -> {
                EuAvailabilityInfo(
                    isAvailable = false,
                    reason = context.getString(R.string.eu_prescription_selection_freetext_not_available)
                )
            }
        }
    }
}

@Composable
internal fun rememberEuPrescriptionSelectionController(
    initialSelectedPrescriptions: List<EuPrescription> = emptyList()
): EuPrescriptionSelectionController {
    val getEuPrescriptionsUseCase by rememberInstance<GetEuPrescriptionsUseCase>()
    val context = LocalContext.current
    return remember(initialSelectedPrescriptions) {
        EuPrescriptionSelectionController(
            getEuPrescriptionsUseCase = getEuPrescriptionsUseCase,
            context = context,
            initialSelectedPrescriptions = initialSelectedPrescriptions
        )
    }
}
