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

import android.location.Location
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import de.gematik.ti.erp.app.base.Controller
import de.gematik.ti.erp.app.eurezept.domain.model.Country
import de.gematik.ti.erp.app.eurezept.domain.usecase.GetAllEuCountriesUseCase
import de.gematik.ti.erp.app.eurezept.domain.usecase.LocationBasedCountryDetectionUseCase
import de.gematik.ti.erp.app.shared.usecase.GetLocationUseCase
import de.gematik.ti.erp.app.utils.compose.ComposableEvent
import de.gematik.ti.erp.app.utils.compose.ComposableEvent.Companion.trigger
import de.gematik.ti.erp.app.utils.uistate.UiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.kodein.di.compose.rememberInstance
import kotlin.collections.isNotEmpty

sealed class CountrySelectionUiState {
    data class Content(val countries: List<Country>) : CountrySelectionUiState()
}

internal class EuCountrySelectionController(
    private val getAllEuCountriesUseCase: GetAllEuCountriesUseCase,
    private val getLocationUseCase: GetLocationUseCase,
    private val locationBasedCountryDetectionUseCase: LocationBasedCountryDetectionUseCase
) : Controller() {

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery
    private val _isDetectedCountrySupported = MutableStateFlow(true)
    val isDetectedCountrySupported = _isDetectedCountrySupported.asStateFlow()

    private val _uiState = MutableStateFlow<UiState<List<Country>>>(UiState.Loading())
    val uiState: StateFlow<UiState<List<Country>>> = _uiState.asStateFlow()

    private var allCountries: List<Country> = emptyList()

    val permissionDeniedEvent = ComposableEvent<Unit>()
    val serviceDisabledEvent = ComposableEvent<Unit>()
    val locationNotFoundEvent = ComposableEvent<Unit>()
    val countryNotFoundEvent = ComposableEvent<Unit>()
    val geocoderNotAvailableEvent = ComposableEvent<Unit>()
    val noCountriesAvailableEvent = ComposableEvent<Unit>()

    init {
        loadCountries()
    }

    fun onRetry() {
        loadCountries()
    }

    private fun loadCountries() {
        controllerScope.launch {
            try {
                _uiState.update { UiState.Loading() }

                allCountries = getAllEuCountriesUseCase.invoke()

                if (allCountries.isEmpty()) {
                    noCountriesAvailableEvent.trigger()
                    _uiState.update { UiState.Empty() }
                } else {
                    applySearchFilter()
                }
            } catch (error: Throwable) {
                _uiState.update { UiState.Error(error) }
            }
        }
    }

    private fun applySearchFilter() {
        val filteredCountries = getAllEuCountriesUseCase.filterCountries(
            countries = allCountries,
            query = _searchQuery.value
        )

        _uiState.update {
            if (filteredCountries.isEmpty()) {
                UiState.Empty()
            } else {
                UiState.Data(filteredCountries)
            }
        }
    }

    fun updateSearchQuery(query: String = "") {
        _searchQuery.update { query }
        if (allCountries.isNotEmpty()) {
            applySearchFilter()
        }
    }

    fun onResetToContent() {
        _isDetectedCountrySupported.update { true }
    }

    fun onLocationPermissionResult(isLocationGranted: Boolean, onResult: (Country?) -> Unit) {
        controllerScope.launch {
            if (allCountries.isNotEmpty()) {
                if (isLocationGranted) {
                    getLocationUseCase.invoke()
                        .onStart {
                            _uiState.update { UiState.Loading() }
                        }
                        .onCompletion {
                            if (_uiState.value.isLoading) {
                                applySearchFilter()
                            }
                        }
                        .collectLatest { locationResult ->
                            when (locationResult) {
                                is GetLocationUseCase.LocationResult.Success -> {
                                    detectCountryFromLocation(locationResult.location, allCountries, onResult)
                                }

                                GetLocationUseCase.LocationResult.ServiceDisabled -> {
                                    onLocationServiceDisabled()
                                }

                                GetLocationUseCase.LocationResult.PermissionDenied -> {
                                    onPermissionDenied()
                                }

                                GetLocationUseCase.LocationResult.LocationNotFound -> {
                                    locationNotFoundEvent.trigger()
                                }

                                is GetLocationUseCase.LocationResult.LocationSearchFailed -> {
                                    locationNotFoundEvent.trigger()
                                }

                                GetLocationUseCase.LocationResult.GettingLocation -> {
                                    // Already in loading state from onStart, nothing to do
                                }
                            }
                        }
                } else {
                    onPermissionDenied()
                    onResult(null)
                }
            }
        }
    }

    private suspend fun detectCountryFromLocation(
        location: Location,
        countries: List<Country>,
        onResult: (Country?) -> Unit
    ) {
        locationBasedCountryDetectionUseCase.detectCountryFromLocation(location, countries)
            .collectLatest { result ->
                when (result) {
                    is LocationBasedCountryDetectionUseCase.CountryDetectionResult.Success -> {
                        onResult(result.country)
                    }

                    LocationBasedCountryDetectionUseCase.CountryDetectionResult.GeocoderNotAvailable -> {
                        geocoderNotAvailableEvent.trigger()
                    }

                    LocationBasedCountryDetectionUseCase.CountryDetectionResult.CountryNotFound -> {
                        countryNotFoundEvent.trigger()
                    }

                    is LocationBasedCountryDetectionUseCase.CountryDetectionResult.CountryNotInEU -> {
                        applySearchFilter()
                        _isDetectedCountrySupported.update { false }
                    }

                    is LocationBasedCountryDetectionUseCase.CountryDetectionResult.CountryDetectionFailed -> {
                        locationNotFoundEvent.trigger()
                    }
                }
            }
    }

    private fun onPermissionDenied() {
        permissionDeniedEvent.trigger()
    }

    private fun onLocationServiceDisabled() {
        serviceDisabledEvent.trigger()
    }
}

@Composable
internal fun rememberEuCountrySelectionController(): EuCountrySelectionController {
    val getAllEuCountriesUseCase by rememberInstance<GetAllEuCountriesUseCase>()
    val getLocationUseCase by rememberInstance<GetLocationUseCase>()
    val locationBasedCountryDetectionUseCase by rememberInstance<LocationBasedCountryDetectionUseCase>()

    return remember {
        EuCountrySelectionController(
            getAllEuCountriesUseCase = getAllEuCountriesUseCase,
            getLocationUseCase = getLocationUseCase,
            locationBasedCountryDetectionUseCase = locationBasedCountryDetectionUseCase
        )
    }
}
