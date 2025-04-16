/*
 * Copyright 2025, gematik GmbH
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

package de.gematik.ti.erp.app.debugsettings.pharamcy.service.selection.presentation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import de.gematik.ti.erp.app.debugsettings.pharamcy.service.selection.usecase.GetShowTelematikIdStateUseCase
import de.gematik.ti.erp.app.debugsettings.pharamcy.service.selection.usecase.PharmacyBackendServiceSelectionUseCase
import de.gematik.ti.erp.app.debugsettings.pharamcy.service.selection.usecase.PharmacyBackendServiceSelectionUseCase.Operation.LoadPharmacyBackendService
import de.gematik.ti.erp.app.debugsettings.pharamcy.service.selection.usecase.PharmacyBackendServiceSelectionUseCase.Operation.SavePharmacyBackendService
import de.gematik.ti.erp.app.debugsettings.pharamcy.service.selection.usecase.PharmacyGetSearchAccessTokenUseCase
import de.gematik.ti.erp.app.debugsettings.pharamcy.service.selection.usecase.PharmacySearchAccessTokenModifierUseCase
import de.gematik.ti.erp.app.debugsettings.pharamcy.service.selection.usecase.PharmacySearchAccessTokenModifierUseCase.Operation.ClearSearchAccessToken
import de.gematik.ti.erp.app.debugsettings.pharamcy.service.selection.usecase.PharmacySearchAccessTokenModifierUseCase.Operation.UpdateSearchAccessToken
import de.gematik.ti.erp.app.debugsettings.pharamcy.service.selection.usecase.ToggleShowTelematikIdStateUseCase
import de.gematik.ti.erp.app.fhir.pharmacy.type.PharmacyVzdService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.kodein.di.compose.rememberInstance

class PharmacyBackendServiceSelectionViewModel(
    private val backEndSelection: PharmacyBackendServiceSelectionUseCase,
    private val getSearchAccessToken: PharmacyGetSearchAccessTokenUseCase,
    private val modifySearchAccessToken: PharmacySearchAccessTokenModifierUseCase,
    private val getShowTelematikIdState: GetShowTelematikIdStateUseCase,
    private val toggleShowTelematikIdState: ToggleShowTelematikIdStateUseCase
) : ViewModel() {

    private val _selectedService = MutableStateFlow<PharmacyVzdService?>(null)
    val selectedService = _selectedService.asStateFlow()

    val searchAccessToken = getSearchAccessToken.invoke()

    val showTelematikId = getShowTelematikIdState.invoke()

    fun loadPharmacyBackendService() {
        viewModelScope.launch {
            _selectedService.value = backEndSelection.invoke(LoadPharmacyBackendService)
        }
    }

    fun savePharmacyBackendService(type: PharmacyVzdService) =
        viewModelScope.launch {
            backEndSelection.invoke(SavePharmacyBackendService(type))
            _selectedService.value = type
        }

    fun saveNewSearchAccessToken(token: String) {
        viewModelScope.launch {
            modifySearchAccessToken.invoke(UpdateSearchAccessToken(token))
        }
    }

    fun clearSearchAccessToken() {
        viewModelScope.launch {
            modifySearchAccessToken.invoke(ClearSearchAccessToken)
        }
    }

    fun toggleShowTelematikIdVisibility(state: Boolean) {
        viewModelScope.launch {
            toggleShowTelematikIdState.invoke(state)
        }
    }
}

@Composable
fun pharmacyBackendServiceSelectionViewModel(): PharmacyBackendServiceSelectionViewModel {
    val selectionUseCase by rememberInstance<PharmacyBackendServiceSelectionUseCase>()
    val getSearchAccessToken by rememberInstance<PharmacyGetSearchAccessTokenUseCase>()
    val modifySearchAccessToken by rememberInstance<PharmacySearchAccessTokenModifierUseCase>()
    val getShowTelematikIdState by rememberInstance<GetShowTelematikIdStateUseCase>()
    val toggleShowTelematikIdState by rememberInstance<ToggleShowTelematikIdStateUseCase>()

    return remember {
        PharmacyBackendServiceSelectionViewModel(
            selectionUseCase,
            getSearchAccessToken,
            modifySearchAccessToken,
            getShowTelematikIdState,
            toggleShowTelematikIdState
        )
    }
}
