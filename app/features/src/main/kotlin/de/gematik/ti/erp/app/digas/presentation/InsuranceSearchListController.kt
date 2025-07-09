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
import androidx.compose.runtime.State
import androidx.compose.runtime.remember
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import de.gematik.ti.erp.app.base.Controller
import de.gematik.ti.erp.app.digas.domain.usecase.FetchInsuranceListUseCase
import de.gematik.ti.erp.app.digas.ui.model.InsuranceUiModel
import de.gematik.ti.erp.app.utils.uistate.UiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import org.kodein.di.compose.rememberInstance

class InsuranceSearchListController(
    searchTerm: String,
    fetchInsuranceListUseCase: FetchInsuranceListUseCase
) : Controller() {
    private val searchFieldValue = MutableStateFlow(searchTerm)

    val insuranceList: StateFlow<UiState<List<InsuranceUiModel>>> =
        fetchInsuranceListUseCase(searchFieldValue.value).stateIn(
            controllerScope,
            started = SharingStarted.WhileSubscribed(),
            initialValue = UiState.Loading()
        )

    fun onSearchFieldValue(value: String) {
        searchFieldValue.value = value
    }

    val insurancesState: State<UiState<List<InsuranceUiModel>>>
        @Composable
        get() = insuranceList.collectAsStateWithLifecycle(initialValue = UiState.Loading())
}

@Composable
fun rememberInsuranceListController(
    searchFieldValue: String = ""
): InsuranceSearchListController {
    val fetchInsuranceListUseCase by rememberInstance<FetchInsuranceListUseCase>()

    return remember(searchFieldValue) {
        InsuranceSearchListController(
            searchTerm = searchFieldValue,
            fetchInsuranceListUseCase = fetchInsuranceListUseCase
        )
    }
}
