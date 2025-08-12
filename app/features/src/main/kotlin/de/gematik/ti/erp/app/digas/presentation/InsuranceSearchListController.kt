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

package de.gematik.ti.erp.app.digas.presentation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import de.gematik.ti.erp.app.base.Controller
import de.gematik.ti.erp.app.digas.domain.usecase.FetchInsuranceListUseCase
import de.gematik.ti.erp.app.digas.ui.model.InsuranceUiModel
import de.gematik.ti.erp.app.utils.uistate.UiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.kodein.di.compose.rememberInstance

class InsuranceSearchListController(
    private val fetchInsuranceListUseCase: FetchInsuranceListUseCase
) : Controller() {
    private val _searchFieldValue = MutableStateFlow("")
    private val _healthInsuranceList = MutableStateFlow<UiState<List<InsuranceUiModel>>>(
        UiState.Loading()
    )

    val searchFieldValue: StateFlow<String> = _searchFieldValue
    val healthInsuranceList: StateFlow<UiState<List<InsuranceUiModel>>> = _healthInsuranceList

    init {
        getHealthInsuranceList()
    }

    private fun getHealthInsuranceList() {
        controllerScope.launch {
            _searchFieldValue.collect { search ->
                _healthInsuranceList.update { UiState.Loading() }
                runCatching {
                    fetchInsuranceListUseCase.invoke(search)
                }.fold(
                    onSuccess = { insuranceList ->
                        if (insuranceList.isEmpty()) {
                            _healthInsuranceList.update { UiState.Empty() }
                        } else {
                            _healthInsuranceList.update { UiState.Data(insuranceList) }
                        }
                    },
                    onFailure = { error ->
                        _healthInsuranceList.update { UiState.Error(error) }
                    }
                )
            }
        }
    }

    fun onSearchFieldValueChange(value: String) {
        _searchFieldValue.update { value }
    }

    fun onRemoveSearchFieldValue() {
        _searchFieldValue.update { "" }
    }
}

@Composable
fun rememberInsuranceListController(): InsuranceSearchListController {
    val fetchInsuranceListUseCase by rememberInstance<FetchInsuranceListUseCase>()

    return remember {
        InsuranceSearchListController(
            fetchInsuranceListUseCase = fetchInsuranceListUseCase
        )
    }
}
