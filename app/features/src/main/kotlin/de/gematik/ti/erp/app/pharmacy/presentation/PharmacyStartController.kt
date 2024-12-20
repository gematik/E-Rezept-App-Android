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

package de.gematik.ti.erp.app.pharmacy.presentation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import de.gematik.ti.erp.app.base.Controller
import de.gematik.ti.erp.app.pharmacy.model.OverviewPharmacyData
import de.gematik.ti.erp.app.pharmacy.model.SelectedFavouritePharmacyState
import de.gematik.ti.erp.app.pharmacy.usecase.DeleteOverviewPharmacyUseCase
import de.gematik.ti.erp.app.pharmacy.usecase.GetPharmacyByTelematikIdUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.kodein.di.compose.rememberInstance

class PharmacyStartController(
    private val getPharmacyByTelematikIdUseCase: GetPharmacyByTelematikIdUseCase,
    private val deleteOverviewPharmacyUseCase: DeleteOverviewPharmacyUseCase
) : Controller() {

    private val selectedPharmacyByTelematikId = MutableStateFlow<OverviewPharmacyData.OverviewPharmacy?>(null)
    private val _selectedPharmacyState = MutableStateFlow<SelectedFavouritePharmacyState>(SelectedFavouritePharmacyState.Idle)

    val selectedPharmacyState: StateFlow<SelectedFavouritePharmacyState> = _selectedPharmacyState.asStateFlow()

    fun onPharmacySelected(pharmacy: OverviewPharmacyData.OverviewPharmacy) {
        selectedPharmacyByTelematikId.value = pharmacy
        getPharmacy()
    }

    fun getPharmacy() {
        selectedPharmacyByTelematikId.value?.let {
            getPharmacyByTelematikId(it.telematikId)
        }
    }

    fun clearSelectedPharmacy() {
        controllerScope.launch {
            selectedPharmacyByTelematikId.value?.let {
                deleteOverviewPharmacyUseCase(it)
                selectedPharmacyByTelematikId.value = null
            }
        }
    }

    private fun getPharmacyByTelematikId(telematikId: String) {
        controllerScope.launch {
            _selectedPharmacyState.value = SelectedFavouritePharmacyState.Loading
            getPharmacyByTelematikIdUseCase(telematikId)
                .fold(
                    onSuccess = { pharmacy ->
                        if (pharmacy != null) {
                            _selectedPharmacyState.value = SelectedFavouritePharmacyState.Data(pharmacy)
                        } else {
                            _selectedPharmacyState.value = SelectedFavouritePharmacyState.Missing
                        }
                    },
                    onFailure = {
                        _selectedPharmacyState.value = SelectedFavouritePharmacyState.Error(it)
                    }
                )
        }
    }

    fun resetSelectedPharmacyState() {
        _selectedPharmacyState.value = SelectedFavouritePharmacyState.Idle
    }
}

@Composable
fun rememberPharmacyStartController(): PharmacyStartController {
    val getPharmacyByTelematikIdUseCase by rememberInstance<GetPharmacyByTelematikIdUseCase>()
    val deleteOverviewPharmacyUseCase by rememberInstance<DeleteOverviewPharmacyUseCase>()
    return remember {
        PharmacyStartController(
            getPharmacyByTelematikIdUseCase = getPharmacyByTelematikIdUseCase,
            deleteOverviewPharmacyUseCase = deleteOverviewPharmacyUseCase
        )
    }
}
