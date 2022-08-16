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

import android.annotation.SuppressLint
import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
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
import de.gematik.ti.erp.app.pharmacy.usecase.PharmacySearchUseCase
import de.gematik.ti.erp.app.pharmacy.usecase.model.PharmacyUseCaseData
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.shareIn
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull
import org.kodein.di.compose.rememberInstance
import java.time.OffsetDateTime

private const val WaitForLocationUpdate = 5000L

private val DefaultSearchData = PharmacyUseCaseData.SearchData(
    name = "",
    filter = PharmacyUseCaseData.Filter(),
    locationMode = PharmacyUseCaseData.LocationMode.Disabled
)

@Stable
class PharmacySearchController(
    private val context: Context,
    private val useCase: PharmacySearchUseCase,
    coroutineScope: CoroutineScope
) {
    private val searchChannel = Channel<PharmacyUseCaseData.SearchData>(capacity = 1)

    var isLoading by mutableStateOf(false)
        private set

    var searchState by mutableStateOf(DefaultSearchData)
        private set

    @OptIn(ExperimentalCoroutinesApi::class)
    val pharmacySearchFlow: Flow<PagingData<PharmacySearchUi>> =
        searchChannel
            .receiveAsFlow()
            .onEach {
                // if we receive an empty list as the first page and the last searchPagingItems state was already populated with results,
                // the continues loading won't work; this short timeout is an ugly workaround to this issue
                delay(100)
                searchState = it
            }
            .flatMapLatest { searchData ->
                isLoading = true

                useCase.searchPharmacies(searchData)
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
                        }.map<PharmacyUseCaseData.Pharmacy, PharmacySearchUi> {
                            PharmacySearchUi.Pharmacy(it)
                        }
                    }.cachedIn(coroutineScope)
            }
            .onEach {
                isLoading = false
            }
            .flowOn(Dispatchers.IO)
            .shareIn(
                coroutineScope,
                SharingStarted.Lazily,
                1
            )

    enum class SearchQueryResult {
        Send, NoLocationPermission, NoLocationFound
    }

    suspend fun search(
        name: String,
        filter: PharmacyUseCaseData.Filter
    ): SearchQueryResult = withContext(Dispatchers.IO) {
        val hasLocationPermission = anyLocationPermissionGranted(context)
        val locationMode = if (hasLocationPermission && filter.nearBy) {
            queryLocation()
                ?.let { PharmacyUseCaseData.LocationMode.Enabled(it) }
                ?: PharmacyUseCaseData.LocationMode.Disabled
        } else {
            PharmacyUseCaseData.LocationMode.Disabled
        }

        when {
            !hasLocationPermission && filter.nearBy ->
                SearchQueryResult.NoLocationPermission
            locationMode == PharmacyUseCaseData.LocationMode.Disabled && filter.nearBy ->
                SearchQueryResult.NoLocationFound
            else -> {
                isLoading = true

                searchChannel.send(
                    PharmacyUseCaseData.SearchData(
                        name = name,
                        filter = filter.copy(nearBy = locationMode is PharmacyUseCaseData.LocationMode.Enabled),
                        locationMode = locationMode
                    )
                )

                SearchQueryResult.Send
            }
        }
    }

    @SuppressLint("MissingPermission")
    @OptIn(ExperimentalCoroutinesApi::class)
    private suspend fun queryLocation(): Location? = withTimeoutOrNull(WaitForLocationUpdate) {
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
                        continuation.resume(Location(longitude = it.longitude, latitude = it.latitude), null)
                    }
            }
        } else {
            null
        }
    }
}

@Composable
fun rememberPharmacySearchController(): PharmacySearchController {
    val context = LocalContext.current
    val pharmacySearchUseCase by rememberInstance<PharmacySearchUseCase>()
    val scope = rememberCoroutineScope()
    return remember {
        PharmacySearchController(
            context = context,
            useCase = pharmacySearchUseCase,
            coroutineScope = scope
        )
    }
}
