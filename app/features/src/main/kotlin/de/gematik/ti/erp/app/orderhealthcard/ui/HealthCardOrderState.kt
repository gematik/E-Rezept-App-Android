/*
 * Copyright (c) 2024 gematik GmbH
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

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.remember
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import de.gematik.ti.erp.app.navigation.Routes
import de.gematik.ti.erp.app.orderhealthcard.usecase.HealthCardOrderUseCase
import de.gematik.ti.erp.app.orderhealthcard.usecase.model.HealthCardOrderUseCaseData
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import org.kodein.di.compose.rememberInstance

object HealthCardOrderNavigationScreens {
    object HealthCardOrder : Routes("contactInsuranceCompany")
    object SelectOrderOption : Routes("contactInsuranceCompany_selectReason")
    object HealthCardOrderContact : Routes("contactInsuranceCompany_selectMethod")
}

class HealthCardOrderState(
    healthCardOrderUseCase: HealthCardOrderUseCase
) {

    private var selectedCompanyFlow: MutableStateFlow<HealthCardOrderUseCaseData.HealthInsuranceCompany?> =
        MutableStateFlow(null)

    private var selectedOptionFlow = MutableStateFlow(HealthCardOrderStateData.ContactInsuranceOption.NotChosen)

    private var healthCardOrderStateFlow = combine(
        healthCardOrderUseCase.healthInsuranceOrderContacts,
        selectedCompanyFlow,
        selectedOptionFlow
    ) {
            companies, company, option ->
        HealthCardOrderStateData.HealthCardOrderState(companies, company, option)
    }

    val state
        @Composable
        get() = healthCardOrderStateFlow.collectAsStateWithLifecycle(
            HealthCardOrderStateData.defaultHealthCardOrderState
        )

    fun onSelectInsuranceCompany(company: HealthCardOrderUseCaseData.HealthInsuranceCompany) {
        selectedCompanyFlow.value = company
    }

    fun onSelectContactOption(option: HealthCardOrderStateData.ContactInsuranceOption) {
        selectedOptionFlow.value = option
    }
}

@Composable
fun rememberHealthCardOrderState(): HealthCardOrderState {
    val healthCardOrderUseCase by rememberInstance<HealthCardOrderUseCase>()
    return remember {
        HealthCardOrderState(
            healthCardOrderUseCase
        )
    }
}

object HealthCardOrderStateData {
    @Immutable
    data class HealthCardOrderState(
        val companies: List<HealthCardOrderUseCaseData.HealthInsuranceCompany>,
        val selectedCompany: HealthCardOrderUseCaseData.HealthInsuranceCompany?,
        val selectedOption: ContactInsuranceOption
    )

    val defaultHealthCardOrderState = HealthCardOrderState(
        companies = emptyList(),
        selectedCompany = null,
        selectedOption = ContactInsuranceOption.NotChosen
    )

    enum class ContactInsuranceOption {
        WithHealthCardAndPin, PinOnly, NotChosen
    }
}
