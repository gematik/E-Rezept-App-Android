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

package de.gematik.ti.erp.app.pharmacy.presentation

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.State
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import de.gematik.ti.erp.app.base.Controller
import de.gematik.ti.erp.app.permissions.isLocationPermissionAndServiceEnabled
import de.gematik.ti.erp.app.permissions.isLocationServiceEnabled
import de.gematik.ti.erp.app.pharmacy.model.OverviewPharmacyData
import de.gematik.ti.erp.app.pharmacy.presentation.FilterType.Companion.getUpdatedFilter
import de.gematik.ti.erp.app.pharmacy.repository.datasource.PreviewMapCoordinatesDataSource.Companion.berlinCoordinates
import de.gematik.ti.erp.app.pharmacy.usecase.GetLocationUseCase
import de.gematik.ti.erp.app.pharmacy.usecase.GetLocationUseCase.LocationResult
import de.gematik.ti.erp.app.pharmacy.usecase.GetOrderStateUseCase
import de.gematik.ti.erp.app.pharmacy.usecase.GetOverviewPharmaciesUseCase
import de.gematik.ti.erp.app.pharmacy.usecase.GetPreviewMapCoordinatesUseCase
import de.gematik.ti.erp.app.pharmacy.usecase.SetPreviewMapCoordinatesUseCase
import de.gematik.ti.erp.app.pharmacy.usecase.model.PharmacyUseCaseData
import de.gematik.ti.erp.app.pharmacy.usecase.model.PharmacyUseCaseData.Coordinates
import de.gematik.ti.erp.app.profiles.usecase.GetActiveProfileUseCase
import de.gematik.ti.erp.app.utils.compose.ComposableEvent
import de.gematik.ti.erp.app.utils.compose.ComposableEvent.Companion.trigger
import io.github.aakira.napier.Napier
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.updateAndGet
import kotlinx.coroutines.launch

abstract class PharmacyGraphController : Controller() {
    abstract val permissionDeniedEvent: ComposableEvent<Unit>
    abstract val serviceDisabledEvent: ComposableEvent<Unit>
    abstract val locationNotFoundEvent: ComposableEvent<Unit>
    abstract val askLocationPermissionEvent: ComposableEvent<Unit>
    abstract val locationProvidedEvent: ComposableEvent<Unit>
    abstract val locationLoadingEvent: ComposableEvent<Boolean>

    abstract fun init(context: Context)
    abstract fun updateFilter(type: FilterType, clearLocation: Boolean = false)
    abstract fun onLocationPermissionResult(isLocationGranted: Boolean)
    abstract fun forceLocationFalse()
    abstract fun forceLocationFalseWithSelectedCoordinates(selectedCoordinates: Coordinates)
    abstract fun checkLocationServiceAndPermission(context: Context)

    @Composable
    abstract fun filter(): State<PharmacyUseCaseData.Filter>

    @Composable
    abstract fun coordinates(): State<Coordinates?>

    @Composable
    abstract fun favouritePharmacies(): State<List<OverviewPharmacyData.OverviewPharmacy>>

    @Composable
    abstract fun previewCoordinates(): State<Coordinates>

    @Composable
    abstract fun hasRedeemableOrders(): State<Boolean>

    @Composable
    abstract fun isDirectRedeemEnabled(): State<Boolean>

    abstract fun reset()
}

class DefaultPharmacyGraphController(
    private val getActiveProfileUseCase: GetActiveProfileUseCase,
    private val getOverviewPharmaciesUseCase: GetOverviewPharmaciesUseCase,
    private val getOrderStateUseCase: GetOrderStateUseCase,
    private val getLocationUseCase: GetLocationUseCase,
    private val setPreviewMapCoordinatesUseCase: SetPreviewMapCoordinatesUseCase,
    private val getPreviewMapCoordinatesUseCase: GetPreviewMapCoordinatesUseCase
) : PharmacyGraphController() {

    private val _coordinates = MutableStateFlow<Coordinates?>(null)

    private val _activeProfile by lazy {
        getActiveProfileUseCase()
            .stateIn(controllerScope, SharingStarted.WhileSubscribed(0, 0), null)
    }

    private val _orderState by lazy { getOrderStateUseCase() }

    @OptIn(ExperimentalCoroutinesApi::class)
    private val _hasRedeemableOrders by lazy {
        _orderState.map { it.prescriptionsInOrder.isNotEmpty() }.mapLatest { return@mapLatest it }
    }

    private val _filter = MutableStateFlow(PharmacyUseCaseData.Filter())

    private val _isDirectRedeemEnabled = _activeProfile
        .mapNotNull { (it?.lastAuthenticated == null) }

    private val _favouritePharmacies by lazy {
        getOverviewPharmaciesUseCase().stateIn(controllerScope, SharingStarted.Lazily, emptyList())
    }

    private fun onPermissionDenied() {
        forceLocationFalse()
        permissionDeniedEvent.trigger()
    }

    private fun onLocationNotFound() {
        forceLocationFalse()
        locationNotFoundEvent.trigger()
    }

    private fun onLocationServiceDisabled() {
        forceLocationFalse()
        serviceDisabledEvent.trigger()
    }

    @ReadOnlyComposable
    @Composable
    @Suppress("ComposableNaming")
    private fun Context.resetPreviewOnLocationNotAllowed() {
        val isDenied = isLocationPermissionAndServiceEnabled().not()
        if (isDenied) {
            setPreviewMapCoordinatesUseCase.invoke(null)
        }
    }

    init {
        updateIsDirectRedeemEnabledOnFilter()
    }

    // Location events
    override val permissionDeniedEvent = ComposableEvent<Unit>()

    override val serviceDisabledEvent = ComposableEvent<Unit>()

    override val locationNotFoundEvent = ComposableEvent<Unit>()

    override val askLocationPermissionEvent = ComposableEvent<Unit>()

    override val locationProvidedEvent = ComposableEvent<Unit>()

    override val locationLoadingEvent = ComposableEvent<Boolean>()

    override fun init(context: Context) {
        updateIsDirectRedeemEnabledOnFilter()
        updateLocation(context)
        _filter.value = PharmacyUseCaseData.Filter()
    }

    private fun updateLocation(context: Context) {
        controllerScope.launch {
            if (context.isLocationPermissionAndServiceEnabled()) {
                getLocationUseCase.invoke().collectLatest { result ->
                    if (result is LocationResult.Success) {
                        val coordinatesResult = Coordinates(
                            latitude = result.location.latitude,
                            longitude = result.location.longitude
                        )
                        _coordinates.value = coordinatesResult
                        setPreviewMapCoordinatesUseCase.invoke(coordinatesResult)
                    } else {
                        Napier.d(tag = "LocationResults") { "$result" }
                    }
                }
            }
        }
    }

    private fun updateIsDirectRedeemEnabledOnFilter() {
        controllerScope.launch {
            combine(_hasRedeemableOrders, _isDirectRedeemEnabled) { hasOrders, isEnabled ->
                if (hasOrders && isEnabled) {
                    updateFilter(type = FilterType.DIRECT_REDEEM)
                }
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        reset()
    }

    override fun updateFilter(type: FilterType, clearLocation: Boolean) {
        _filter.value = _filter.updateAndGet { type.getUpdatedFilter(_filter.value) }
        if (clearLocation) {
            _coordinates.value = null
        }
    }

    override fun forceLocationFalse() {
        _filter.value = _filter.value.copy(nearBy = false)
        setPreviewMapCoordinatesUseCase.invoke(null)
        _coordinates.value = null
    }

    override fun forceLocationFalseWithSelectedCoordinates(selectedCoordinates: Coordinates) {
        _filter.value = _filter.value.copy(nearBy = false)
        setPreviewMapCoordinatesUseCase.invoke(selectedCoordinates)
        _coordinates.value = selectedCoordinates
    }

    override fun checkLocationServiceAndPermission(context: Context) {
        val isLocationServiceEnabled = context.isLocationServiceEnabled()
        if (isLocationServiceEnabled) {
            askLocationPermissionEvent.trigger()
        } else {
            onLocationServiceDisabled()
        }
    }

    override fun onLocationPermissionResult(isLocationGranted: Boolean) {
        controllerScope.launch {
            if (isLocationGranted) {
                getLocationUseCase.invoke()
                    .onStart {
                        locationLoadingEvent.trigger(true)
                        _filter.value = _filter.value.copy(nearBy = true)
                    }
                    .onCompletion {
                        locationLoadingEvent.trigger(false)
                    }
                    .collectLatest { result ->
                        when (result) {
                            LocationResult.GettingLocation -> Unit // Already handled in on start
                            is LocationResult.LocationSearchFailed, LocationResult.LocationNotFound -> {
                                _filter.value = _filter.value.copy(nearBy = false)
                                onLocationNotFound()
                            }

                            LocationResult.PermissionDenied -> {
                                _filter.value = _filter.value.copy(nearBy = false)
                                onPermissionDenied()
                            }

                            LocationResult.ServiceDisabled -> {
                                _filter.value = _filter.value.copy(nearBy = false)
                                onLocationServiceDisabled()
                            }

                            is LocationResult.Success -> {
                                val coordinatesResult = Coordinates(
                                    latitude = result.location.latitude,
                                    longitude = result.location.longitude
                                )
                                _coordinates.value = coordinatesResult
                                setPreviewMapCoordinatesUseCase.invoke(coordinatesResult)
                                locationProvidedEvent.trigger()
                            }
                        }
                    }
            } else {
                onPermissionDenied()
            }
        }
    }

    override fun reset() {
        _filter.value = PharmacyUseCaseData.Filter()
    }

    @Composable
    override fun filter(): State<PharmacyUseCaseData.Filter> {
        return _filter.collectAsStateWithLifecycle(PharmacyUseCaseData.Filter())
    }

    @Composable
    override fun coordinates() = _coordinates.collectAsStateWithLifecycle(null)

    @Composable
    override fun favouritePharmacies() = _favouritePharmacies.collectAsStateWithLifecycle(emptyList())

    @Composable
    override fun previewCoordinates(): State<Coordinates> {
        LocalContext.current.resetPreviewOnLocationNotAllowed()
        return getPreviewMapCoordinatesUseCase()
            .collectAsStateWithLifecycle(berlinCoordinates)
    }

    @Composable
    override fun isDirectRedeemEnabled() = _isDirectRedeemEnabled.collectAsStateWithLifecycle(false)

    @Composable
    override fun hasRedeemableOrders() = _hasRedeemableOrders.collectAsStateWithLifecycle(false)
}
