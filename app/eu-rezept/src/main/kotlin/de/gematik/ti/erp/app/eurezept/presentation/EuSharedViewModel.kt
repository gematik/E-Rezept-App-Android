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

import androidx.compose.runtime.Composable
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavController
import de.gematik.ti.erp.app.eurezept.domin.model.Country
import de.gematik.ti.erp.app.eurezept.domin.model.EuPrescription
import de.gematik.ti.erp.app.eurezept.navigation.EuRoutes
import de.gematik.ti.erp.app.profiles.presentation.GetActiveProfileController
import de.gematik.ti.erp.app.profiles.usecase.GetActiveProfileUseCase
import de.gematik.ti.erp.app.viewmodel.rememberGraphScopedViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.kodein.di.compose.rememberInstance

internal abstract class EuSharedViewModel(
    getActiveProfileUseCase: GetActiveProfileUseCase
) : GetActiveProfileController(getActiveProfileUseCase) {

    abstract val selectedPrescriptions: StateFlow<List<EuPrescription>>
    abstract val selectedCountry: StateFlow<Country?>
    abstract val isLoading: StateFlow<Boolean>
    abstract val isRedeemEnabled: StateFlow<Boolean>

    abstract fun setSelectedPrescriptions(prescriptions: List<EuPrescription>)
    abstract fun setSelectedCountry(country: Country?)
    abstract fun clearSelection()
    abstract fun onRedeem()
    abstract fun reset()
}

internal class DefaultEuSharedViewModel(
    getActiveProfileUseCase: GetActiveProfileUseCase
) : EuSharedViewModel(
    getActiveProfileUseCase = getActiveProfileUseCase
) {

    private val _selectedPrescriptions = MutableStateFlow<List<EuPrescription>>(emptyList())
    override val selectedPrescriptions: StateFlow<List<EuPrescription>> = _selectedPrescriptions.asStateFlow()

    private val _selectedCountry = MutableStateFlow<Country?>(null)
    override val selectedCountry: StateFlow<Country?> = _selectedCountry.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    override val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    override val isRedeemEnabled: StateFlow<Boolean> = combine(
        selectedPrescriptions,
        selectedCountry,
        isLoading
    ) { prescriptions, country, loading ->
        prescriptions.isNotEmpty() && country != null && !loading
    }.stateIn(
        scope = controllerScope,
        started = SharingStarted.WhileSubscribed(),
        initialValue = false
    )

    override fun setSelectedPrescriptions(prescriptions: List<EuPrescription>) {
        _selectedPrescriptions.update { prescriptions }
    }

    override fun setSelectedCountry(country: Country?) {
        _selectedCountry.update { country }
    }

    override fun clearSelection() {
        _selectedPrescriptions.update { emptyList() }
        _selectedCountry.update { null }
    }

    override fun onRedeem() {
        controllerScope.launch {
            // EU prescription redemption logic
        }
    }

    override fun reset() {
        clearSelection()
        _isLoading.update { false }
    }

    override fun onCleared() {
        super.onCleared()
        reset()
    }
}

@Composable
internal fun euSharedViewModel(
    navController: NavController,
    entry: NavBackStackEntry
): EuSharedViewModel {
    val getActiveProfileUseCase by rememberInstance<GetActiveProfileUseCase>()

    return rememberGraphScopedViewModel(
        navController = navController,
        navEntry = entry,
        graphRoute = EuRoutes.subGraphName()
    ) {
        DefaultEuSharedViewModel(
            getActiveProfileUseCase = getActiveProfileUseCase
        )
    }
}
