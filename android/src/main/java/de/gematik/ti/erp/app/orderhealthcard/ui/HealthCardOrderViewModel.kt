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

package de.gematik.ti.erp.app.orderhealthcard.ui

import dagger.hilt.android.lifecycle.HiltViewModel
import de.gematik.ti.erp.app.Route
import de.gematik.ti.erp.app.core.BaseViewModel
import de.gematik.ti.erp.app.orderhealthcard.ui.model.HealthCardOrderViewModelData
import de.gematik.ti.erp.app.orderhealthcard.usecase.HealthCardOrderUseCase
import de.gematik.ti.erp.app.orderhealthcard.usecase.model.HealthCardOrderUseCaseData
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import javax.inject.Inject

object HealthCardOrderNavigationScreens {
    object HealthCardOrder : Route("HealthCardOrder")
    object HealthCardOrderInsuranceCompanies : Route("HealthCardOrderInsuranceCompanies")
}

@HiltViewModel
class HealthCardOrderViewModel @Inject constructor(
    private val healthCardOrderUseCase: HealthCardOrderUseCase
) : BaseViewModel() {
    val defaultState = HealthCardOrderViewModelData.State(
        companies = emptyList(),
        selectedCompany = null,
        selectedOption = HealthCardOrderViewModelData.ContactInsuranceOption.None,
    )

    private val state = MutableStateFlow(defaultState)

    fun screenState(): Flow<HealthCardOrderViewModelData.State> =
        state.combine(healthCardOrderUseCase.healthInsuranceOrderContacts()) { state, companies ->
            state.copy(companies = companies)
        }

    fun onSelectInsuranceCompany(company: HealthCardOrderUseCaseData.HealthInsuranceCompany) {
        state.value = state.value.copy(
            selectedCompany = company,
            selectedOption = HealthCardOrderViewModelData.ContactInsuranceOption.None
        )
    }

    fun onSelectContactOption(option: HealthCardOrderViewModelData.ContactInsuranceOption) {
        state.value = state.value.copy(selectedOption = option)
    }
}
