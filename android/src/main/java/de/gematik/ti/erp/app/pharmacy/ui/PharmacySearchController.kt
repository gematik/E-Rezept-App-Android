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

package de.gematik.ti.erp.app.pharmacy.ui

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.LocationManager
import android.os.Build
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import androidx.paging.PagingData
import androidx.paging.cachedIn
import androidx.paging.filter
import androidx.paging.map
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.tasks.CancellationTokenSource
import de.gematik.ti.erp.app.fhir.model.DeliveryPharmacyService
import de.gematik.ti.erp.app.fhir.model.Location
import de.gematik.ti.erp.app.fhir.model.isOpenAt
import de.gematik.ti.erp.app.pharmacy.usecase.PharmacyMapsUseCase
import de.gematik.ti.erp.app.pharmacy.usecase.PharmacySearchUseCase
import de.gematik.ti.erp.app.pharmacy.usecase.model.PharmacyUseCaseData
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
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.shareIn
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull
import org.kodein.di.compose.rememberInstance
import java.time.OffsetDateTime

private const val WaitForLocationUpdate = 2500L
private const val DefaultRadiusInMeter = 999 * 1000.0

private val DefaultSearchData = PharmacyUseCaseData.SearchData(
    name = "",
    filter = PharmacyUseCaseData.Filter(),
    locationMode = PharmacyUseCaseData.LocationMode.Disabled
)

@Stable
class PharmacySearchController(
    private val context: Context,
    private val mapsUseCase: PharmacyMapsUseCase,
    private val searchUseCase: PharmacySearchUseCase,
    coroutineScope: CoroutineScope
) {
    @Stable
    sealed interface State : PrescriptionServiceState {
        @Stable
        object Loading : State

        @Stable
        data class Pharmacies(val pharmacies: List<PharmacyUseCaseData.Pharmacy>) : State
    }

    private val searchChannelFlow = MutableStateFlow<PharmacyUseCaseData.SearchData?>(null)

    var isLoading by mutableStateOf(false)
        private set

    var searchState by mutableStateOf(DefaultSearchData)
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

                searchUseCase.searchPharmacies(searchData)
                    .map { pagingData ->
                        if (searchData.locationMode is PharmacyUseCaseData.LocationMode.Enabled) {
                            pagingData.map {
                                it.copy(
                                    distance = it.location?.minus(searchData.locationMode.location)
                                )
                            }
                        } else {
                            pagingData
                        }.filter { pharmacy ->
                            if (searchData.filter.deliveryService) {
                                when {
                                    searchData.filter.deliveryService &&
                                        pharmacy.provides.any { it is DeliveryPharmacyService } -> true

                                    else -> false
                                }
                            } else {
                                true
                            }
                        }.filter {
                            if (searchData.filter.openNow) {
                                when {
                                    it.openingHours == null -> false
                                    it.openingHours.isOpenAt(OffsetDateTime.now()) -> true
                                    else -> false
                                }
                            } else {
                                true
                            }
                        }
                    }.cachedIn(coroutineScope)
            }
            .onEach {
                isLoading = false
            }
            .flowOn(Dispatchers.IO)
            .shareIn(
                coroutineScope,
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

                    val pharmacies = mapsUseCase.searchPharmacies(searchData)

                    if (searchData.locationMode is PharmacyUseCaseData.LocationMode.Enabled) {
                        pharmacies.map {
                            it.copy(
                                distance = it.location?.minus(searchData.locationMode.location)
                            )
                        }
                    } else {
                        pharmacies
                    }.filter { pharmacy ->
                        if (searchData.filter.deliveryService) {
                            when {
                                searchData.filter.deliveryService &&
                                    pharmacy.provides.any { it is DeliveryPharmacyService } -> true

                                else -> false
                            }
                        } else {
                            true
                        }
                    }.filter {
                        if (searchData.filter.openNow) {
                            when {
                                it.openingHours == null -> false
                                it.openingHours.isOpenAt(OffsetDateTime.now()) -> true
                                else -> false
                            }
                        } else {
                            true
                        }
                    }.also {
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
                    PharmacyUseCaseData.LocationMode.Enabled(it, radiusInMeter)
                } ?: PharmacyUseCaseData.LocationMode.Disabled

                val locationError = if (location == null && filter.nearBy) {
                    when {
                        !hasLocationServiceEnabled -> SearchQueryResult.NoLocationServicesEnabled
                        !hasLocationPermission -> SearchQueryResult.NoLocationPermission
                        locationMode == PharmacyUseCaseData.LocationMode.Disabled -> SearchQueryResult.NoLocationFound
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
                        searchChannelFlow.emit(
                            PharmacyUseCaseData.SearchData(
                                name = name,
                                filter = filter.copy(
                                    nearBy = locationMode is PharmacyUseCaseData.LocationMode.Enabled &&
                                        location == null
                                ),
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
    val scope = rememberCoroutineScope()
    return remember {
        PharmacySearchController(
            context = context,
            mapsUseCase = pharmacyMapsUseCase,
            searchUseCase = pharmacySearchUseCase,
            coroutineScope = scope
        )
    }
}

val locationPermissions = arrayOf(
    Manifest.permission.ACCESS_FINE_LOCATION,
    Manifest.permission.ACCESS_COARSE_LOCATION
)

private fun anyLocationPermissionGranted(context: Context) =
    locationPermissions.any {
        ContextCompat.checkSelfPermission(
            context,
            it
        ) == PackageManager.PERMISSION_GRANTED
    }
