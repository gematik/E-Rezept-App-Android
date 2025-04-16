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

package de.gematik.ti.erp.app.orderhealthcard.presentation

import de.gematik.ti.erp.app.base.Controller
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class OrderHealthCardGraphController : Controller() {
    private var _selectedInsuranceCompany: MutableStateFlow<HealthInsuranceCompany?> =
        MutableStateFlow(null)
    private var _selectedContactOption = MutableStateFlow(OrderHealthCardContactOption.NotChosen)

    val selectedInsuranceCompany: StateFlow<HealthInsuranceCompany?> = _selectedInsuranceCompany
    val selectedContactOption: StateFlow<OrderHealthCardContactOption> = _selectedContactOption

    init {
        reset()
    }

    fun reset() {
        controllerScope.launch {
            _selectedInsuranceCompany.value = null
            _selectedContactOption.value = OrderHealthCardContactOption.NotChosen
        }
    }

    fun setInsuranceCompany(company: HealthInsuranceCompany) {
        _selectedInsuranceCompany.value = company
    }

    fun setContactOption(option: OrderHealthCardContactOption) {
        _selectedContactOption.value = option
    }
}

enum class OrderHealthCardContactOption {
    WithHealthCardAndPin, PinOnly, NotChosen
}
