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

package de.gematik.ti.erp.app.pharmacy.presentation

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.LocationManager
import android.os.Build
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.paging.PagingData
import androidx.paging.cachedIn
import androidx.paging.filter
import androidx.paging.map
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.tasks.CancellationTokenSource
import de.gematik.ti.erp.app.Requirement
import de.gematik.ti.erp.app.fhir.model.DeliveryPharmacyService
import de.gematik.ti.erp.app.fhir.model.Location
import de.gematik.ti.erp.app.fhir.model.isOpenAt
import de.gematik.ti.erp.app.pharmacy.model.OverviewPharmacyData
import de.gematik.ti.erp.app.pharmacy.usecase.GetOverviewPharmaciesUseCase
import de.gematik.ti.erp.app.pharmacy.usecase.PharmacyMapsUseCase
import de.gematik.ti.erp.app.pharmacy.usecase.PharmacyOverviewUseCase
import de.gematik.ti.erp.app.pharmacy.usecase.PharmacySearchUseCase
import de.gematik.ti.erp.app.pharmacy.usecase.model.PharmacyUseCaseData
import de.gematik.ti.erp.app.pharmacy.usecase.model.PharmacyUseCaseData.LocationMode
import de.gematik.ti.erp.app.prescription.ui.PrescriptionServiceState
import de.gematik.ti.erp.app.prescription.ui.catchAndTransformRemoteExceptions
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.shareIn
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import org.kodein.di.compose.rememberInstance

private const val WaitForLocationUpdate = 2500L
private const val DefaultRadiusInMeter = 999 * 1000.0

@Stable
class PharmacySearchController(
    private val context: Context,
    private val getOverviewPharmaciesUseCase: GetOverviewPharmaciesUseCase,
    private val pharmacyMapsUseCase: PharmacyMapsUseCase,
    private val pharmacyOverviewUseCase: PharmacyOverviewUseCase,
    private val pharmacySearchUseCase: PharmacySearchUseCase,
    private val scope: CoroutineScope
) {

    private val overviewPharmacies by lazy {
        getOverviewPharmaciesUseCase().stateIn(scope, SharingStarted.Lazily, emptyList())
    }

    val overviewPharmaciesState
        @Composable
        get() = overviewPharmacies.collectAsStateWithLifecycle()

    @Stable
    sealed interface State : PrescriptionServiceState {
        @Stable
        data object Loading : State

        @Stable
        data class Pharmacies(val pharmacies: List<PharmacyUseCaseData.Pharmacy>) : State
    }

    private val searchChannelFlow = MutableStateFlow<PharmacyUseCaseData.SearchData?>(null)

    var isLoading by mutableStateOf(false)
        private set

    var searchState by mutableStateOf(PharmacySearchStateData.defaultSearchData)
        private set

    @OptIn(ExperimentalCoroutinesApi::class)
    val pharmacySearchFlow: Flow<PagingData<PharmacyUseCaseData.Pharmacy>> =
        searchChannelFlow
            .filterNotNull()
            .onEach {
                // if we receive an empty list as the first page and the last searchPagingItems state was already populated with results,
                // the continues loading won't work; this short timeout is an ugly workaround to this issue
                delay(100)
                searchState = it
            }
            .flatMapLatest { searchData ->
                isLoading = true

                pharmacySearchUseCase.searchPharmacies(searchData)
                    .map { pagingData ->
                        pagingData.map { it.updateDistanceForEnabledLocation(searchData.locationMode) }
                            .filter { it.providesDeliveryService(searchData.filter.deliveryService) }
                            .filter { it.hasOpeningHours(searchData.filter.openNow) }
                    }.cachedIn(scope)
            }
            .onEach {
                isLoading = false
            }
            .flowOn(Dispatchers.IO)
            .shareIn(
                scope,
                SharingStarted.WhileSubscribed(),
                1
            )

    @OptIn(ExperimentalCoroutinesApi::class)
    val pharmacyMapsFlow: Flow<PrescriptionServiceState> =
        searchChannelFlow
            .filterNotNull()
            .onEach {
                searchState = it
            }
            .flatMapLatest { searchData ->
                flow {
                    emit(State.Loading)

                    val pharmacies = pharmacyMapsUseCase.searchPharmacies(searchData)

                    pharmacies
                        .map { it.updateDistanceForEnabledLocation(searchData.locationMode) }
                        .filter { it.providesDeliveryService(searchData.filter.deliveryService) }
                        .filter { it.hasOpeningHours(searchData.filter.openNow) }
                        .also {
                            emit(State.Pharmacies(it))
                        }
                }.catchAndTransformRemoteExceptions()
            }
            .onEach {
                isLoading = when (it) {
                    is State.Loading ->
                        true

                    else ->
                        false
                }
            }
            .flowOn(Dispatchers.IO)

    enum class SearchQueryResult {
        Send, NoLocationPermission, NoLocationFound, NoLocationServicesEnabled
    }

    private val lock = Mutex()

    suspend fun search(
        name: String,
        filter: PharmacyUseCaseData.Filter,
        location: Location? = null,
        radiusInMeter: Double = DefaultRadiusInMeter
    ): SearchQueryResult = withContext(Dispatchers.IO) {
        lock.withLock {
            try {
                isLoading = true

                val hasLocationPermission = anyLocationPermissionGranted(context)
                val hasLocationServiceEnabled = isLocationServiceEnabled(context)

                val currentLocation =
                    location ?: if (hasLocationPermission && hasLocationServiceEnabled && filter.nearBy) {
                        queryLocation(context)
                    } else {
                        null
                    }

                val locationMode = currentLocation?.let {
                    LocationMode.Enabled(it, radiusInMeter)
                } ?: LocationMode.Disabled

                val locationError = if (location == null && filter.nearBy) {
                    when {
                        !hasLocationServiceEnabled -> SearchQueryResult.NoLocationServicesEnabled
                        !hasLocationPermission -> SearchQueryResult.NoLocationPermission
                        locationMode == LocationMode.Disabled -> SearchQueryResult.NoLocationFound
                        else -> null
                    }
                } else {
                    null
                }

                when {
                    locationError != null -> {
                        locationError
                    }

                    else -> {
                        val isNearBy = locationMode is LocationMode.Enabled && location == null
                        searchChannelFlow.emit(
                            PharmacyUseCaseData.SearchData(
                                name = name,
                                filter = filter.copy(nearBy = isNearBy),
                                locationMode = locationMode
                            )
                        )
                        SearchQueryResult.Send
                    }
                }
            } finally {
                isLoading = false
            }
        }
    }
    suspend fun deleteOverviewPharmacy(overviewPharmacy: OverviewPharmacyData.OverviewPharmacy) {
        pharmacyOverviewUseCase.deleteOverviewPharmacy(overviewPharmacy)
    }

    suspend fun findPharmacyByTelematikIdState(
        telematikId: String
    ) = flowOf(pharmacyOverviewUseCase.searchPharmacyByTelematikId(telematikId))

    private fun PharmacyUseCaseData.Pharmacy.updateDistanceForEnabledLocation(locationMode: LocationMode) =
        when (locationMode) {
            is LocationMode.Enabled -> copy(distance = location?.minus(locationMode.location))
            else -> this
        }

    private fun PharmacyUseCaseData.Pharmacy.providesDeliveryService(isDeliveryServiceFiltered: Boolean) =
        when {
            isDeliveryServiceFiltered -> provides.any { it is DeliveryPharmacyService }
            else -> true
        }

    private fun PharmacyUseCaseData.Pharmacy.hasOpeningHours(isOpenNow: Boolean) =
        if (isOpenNow) {
            openingHours?.isOpenAt(
                Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
            ) ?: false
        } else {
            true
        }
}

private fun isLocationServiceEnabled(context: Context): Boolean {
    val lm = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
        lm.isLocationEnabled
    } else {
        true
    }
}

suspend fun queryLocation(context: Context): Location? =
    queryNativeLocation(context)?.let {
        Location(longitude = it.longitude, latitude = it.latitude)
    }

@SuppressLint("MissingPermission")
@OptIn(ExperimentalCoroutinesApi::class)
suspend fun queryNativeLocation(context: Context): android.location.Location? =
    withTimeoutOrNull(WaitForLocationUpdate) {
        if (anyLocationPermissionGranted(context)) {
            suspendCancellableCoroutine { continuation ->
                val cancelTokenSource = CancellationTokenSource()

                continuation.invokeOnCancellation { cancelTokenSource.cancel() }

                LocationServices
                    .getFusedLocationProviderClient(context)
                    .getCurrentLocation(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY, cancelTokenSource.token)
                    .addOnFailureListener {
                        continuation.cancel()
                    }
                    .addOnSuccessListener {
                        continuation.resume(it, null)
                    }
            }
        } else {
            null
        }
    }

@Composable
fun rememberPharmacySearchController(): PharmacySearchController {
    val context = LocalContext.current
    val pharmacyMapsUseCase by rememberInstance<PharmacyMapsUseCase>()
    val pharmacySearchUseCase by rememberInstance<PharmacySearchUseCase>()
    val pharmacyOverviewUseCase by rememberInstance<PharmacyOverviewUseCase>()
    val getOverviewPharmaciesUseCase by rememberInstance<GetOverviewPharmaciesUseCase>()
    val scope = rememberCoroutineScope()
    return remember {
        PharmacySearchController(
            context = context,
            pharmacyMapsUseCase = pharmacyMapsUseCase,
            pharmacySearchUseCase = pharmacySearchUseCase,
            pharmacyOverviewUseCase = pharmacyOverviewUseCase,
            getOverviewPharmaciesUseCase = getOverviewPharmaciesUseCase,
            scope = scope
        )
    }
}

val locationPermissions = arrayOf(
    Manifest.permission.ACCESS_FINE_LOCATION,
    Manifest.permission.ACCESS_COARSE_LOCATION
)

@Requirement(
    "O.Plat_3#3",
    sourceSpecification = "BSI-eRp-ePA",
    rationale = "platform dialog for ACCESS_FINE_LOCATION and ACCESS_COARSE_LOCATION"
)
private fun anyLocationPermissionGranted(context: Context) =
    locationPermissions.any {
        ContextCompat.checkSelfPermission(
            context,
            it
        ) == PackageManager.PERMISSION_GRANTED
    }

object PharmacySearchStateData {
    @Immutable
    data class PharmacySearchOverviewState(
        val overviewPharmacies: List<OverviewPharmacyData.OverviewPharmacy>
    )

    val defaultSearchData = PharmacyUseCaseData.SearchData(
        name = "",
        filter = PharmacyUseCaseData.Filter(),
        locationMode = LocationMode.Disabled
    )
}
