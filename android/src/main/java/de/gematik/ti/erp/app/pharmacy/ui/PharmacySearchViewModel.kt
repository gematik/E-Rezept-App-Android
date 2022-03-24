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
import android.os.Parcelable
import androidx.core.content.ContextCompat
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import androidx.paging.filter
import androidx.paging.insertHeaderItem
import androidx.paging.map
import com.google.android.gms.location.LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY
import com.google.android.gms.location.LocationServices
import com.google.android.gms.tasks.CancellationTokenSource
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import de.gematik.ti.erp.app.DispatchProvider
import de.gematik.ti.erp.app.common.usecase.HintUseCase
import de.gematik.ti.erp.app.common.usecase.model.PharmacyScreenHintEnableLocation
import de.gematik.ti.erp.app.demo.usecase.DemoUseCase
import de.gematik.ti.erp.app.pharmacy.repository.model.DeliveryPharmacyService
import de.gematik.ti.erp.app.pharmacy.repository.model.Location
import de.gematik.ti.erp.app.pharmacy.repository.model.isOpenAt
import de.gematik.ti.erp.app.pharmacy.ui.model.PharmacyScreenData
import de.gematik.ti.erp.app.pharmacy.usecase.PharmacySearchUseCase
import de.gematik.ti.erp.app.pharmacy.usecase.model.PharmacyUseCaseData
import de.gematik.ti.erp.app.prescription.repository.RemoteRedeemOption
import de.gematik.ti.erp.app.profiles.usecase.ProfilesUseCase
import java.time.OffsetDateTime
import javax.inject.Inject
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.shareIn
import kotlinx.coroutines.flow.transform
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.supervisorScope
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull
import kotlinx.parcelize.Parcelize

private const val waitForLocationUpdate = 5000L

sealed class PharmacySearchUi {
    class Pharmacy(val pharmacy: PharmacyUseCaseData.Pharmacy) : PharmacySearchUi()
    object LocationHint : PharmacySearchUi()
}

private const val navStateKey = "pharmacyNavState"

@HiltViewModel
class PharmacySearchViewModel @Inject constructor(
    @ApplicationContext
    private val context: Context,
    private val useCase: PharmacySearchUseCase,
    private val profilesUseCase: ProfilesUseCase,
    private val hintUseCase: HintUseCase,
    private val dispatcher: DispatchProvider,
    private val savedStateHandle: SavedStateHandle,
    private val demoUseCase: DemoUseCase,
) : ViewModel() {
    private val searchChannel = Channel<PharmacyUseCaseData.SearchData>()
    private var searchState = MutableStateFlow<PharmacyUseCaseData.SearchData?>(null)

    @OptIn(ExperimentalCoroutinesApi::class)
    val pharmacySearchFlow: Flow<PagingData<PharmacySearchUi>> =
        searchChannel
            .receiveAsFlow()
            .onEach {
                // if we receive an empty list as the first page and the last searchPagingItems state was already populated with results,
                // the continues loading won't work; this short timeout is an ugly workaround to this issue
                delay(100)
                searchState.value = it

                if (it.locationMode is PharmacyUseCaseData.LocationMode.Enabled) {
                    cancelLocationHint()
                }
            }
            .flatMapLatest { searchData ->
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
                                    searchData.filter.deliveryService && pharmacy.provides.any { it is DeliveryPharmacyService } -> true
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
                        }.insertHeaderItem(item = PharmacySearchUi.LocationHint)
                    }
                    .cachedIn(viewModelScope)
            }.shareIn(
                viewModelScope,
                SharingStarted.WhileSubscribed(),
                1
            )

    @SuppressLint("MissingPermission")
    @OptIn(ExperimentalCoroutinesApi::class)
    private suspend fun queryLocation(): Location? = withTimeoutOrNull(waitForLocationUpdate) {
        suspendCancellableCoroutine { continuation ->
            val cancelTokenSource = CancellationTokenSource()

            continuation.invokeOnCancellation { cancelTokenSource.cancel() }

            LocationServices
                .getFusedLocationProviderClient(context)
                .getCurrentLocation(PRIORITY_BALANCED_POWER_ACCURACY, cancelTokenSource.token)
                .addOnFailureListener {
                    continuation.cancel()
                }
                .addOnSuccessListener {
                    continuation.resume(Location(longitude = it.longitude, latitude = it.latitude), null)
                }
        }
    }

    @Parcelize
    private data class NavState(
        // orders identified by their taskId
        val unSelectedPrescriptions: Set<String>,
        val selectedOrderOption: PharmacyScreenData.OrderOption?,
        val selectedPharmacy: PharmacyUseCaseData.Pharmacy?
    ) : Parcelable

    private val navState = MutableStateFlow(
        savedStateHandle.get(navStateKey) ?: NavState(
            unSelectedPrescriptions = setOf(),
            selectedOrderOption = null,
            selectedPharmacy = null
        )
    )

    init {
        viewModelScope.launch(dispatcher.unconfined()) {
            val searchData = useCase.previousSearch.map {
                it.copy(locationMode = if (anyLocationPermissionGranted(context)) it.locationMode else PharmacyUseCaseData.LocationMode.Disabled)
            }.first()

            searchPharmacies(
                searchData.name,
                searchData.filter,
                searchData.locationMode is PharmacyUseCaseData.LocationMode.EnabledWithoutPosition
            )
        }
        viewModelScope.launch {
            navState.collect {
                savedStateHandle.set(navStateKey, it)
            }
        }
    }

    /**
     * Returns `true` if a position couldn't be queried.
     */
    suspend fun searchPharmacies(
        name: String,
        filter: PharmacyUseCaseData.Filter,
        withLocationEnabled: Boolean
    ): Boolean = withContext(dispatcher.unconfined()) {
        val locationMode = if (withLocationEnabled) {
            queryLocation()
                ?.let { PharmacyUseCaseData.LocationMode.Enabled(it) }
                ?: PharmacyUseCaseData.LocationMode.Disabled
        } else {
            PharmacyUseCaseData.LocationMode.Disabled
        }
        searchChannel.send(PharmacyUseCaseData.SearchData(name, filter, locationMode))

        withLocationEnabled && locationMode is PharmacyUseCaseData.LocationMode.Disabled
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    fun screenState(): Flow<PharmacyUseCaseData.State> = flow {
        emitAll(
            combine(hintUseCase.cancelledHints, searchState.filterNotNull()) { cancelledHints, search ->
                PharmacyUseCaseData.State(
                    search = search,
                    PharmacyScreenHintEnableLocation !in cancelledHints
                )
            }
        )
    }

    fun cancelLocationHint() {
        hintUseCase.cancelHint(PharmacyScreenHintEnableLocation)
    }

    fun detailScreenState() = navState.transform {
        if (it.selectedPharmacy != null) {
            emit(
                PharmacyScreenData.DetailScreenState(
                    it.selectedPharmacy
                )
            )
        }
    }

    fun onSelectPharmacy(pharmacy: PharmacyUseCaseData.Pharmacy) {
        navState.update {
            it.copy(selectedPharmacy = pharmacy)
        }
    }

    fun orderScreenState(taskIds: List<String>): Flow<PharmacyScreenData.OrderScreenState> =
        combine(
            profilesUseCase.profiles.map {
                it.find { profile ->
                    profile.active
                }!!
            },
            if (demoUseCase.isDemoModeActive) getDemoOrders(taskIds) else useCase.prescriptionDetailsForOrdering(taskIds),
            navState.filter { it.selectedPharmacy != null && it.selectedOrderOption != null }
        ) { activeProfile, state, navState ->
            PharmacyScreenData.OrderScreenState(
                activeProfile = activeProfile,
                contact = state.contact,
                prescriptions = state.prescriptions.map {
                    Pair(it, it.taskId !in navState.unSelectedPrescriptions)
                },
                selectedPharmacy = navState.selectedPharmacy!!,
                orderOption = navState.selectedOrderOption!!
            )
        }

    private fun getDemoOrders(taskIds: List<String>): Flow<PharmacyUseCaseData.OrderState> {
        return flow {
            taskIds.map { taskIdToOrder ->
                demoUseCase.demoTasks.value.find { demoTasks -> demoTasks.taskId == taskIdToOrder }!!
                    .let {
                        PharmacyUseCaseData.PrescriptionOrder(
                            it.taskId,
                            it.accessCode ?: "",
                            it.medicationText ?: "",
                            true
                        )
                    }
            }.apply {
                emit(PharmacyUseCaseData.OrderState(this, demoUseCase.demoContact))
            }
        }
    }

    fun onSelectOrderOption(option: PharmacyScreenData.OrderOption) {
        navState.update {
            it.copy(selectedOrderOption = option)
        }
    }

    fun onSelectOrder(order: PharmacyUseCaseData.PrescriptionOrder) {
        navState.update {
            it.copy(unSelectedPrescriptions = it.unSelectedPrescriptions - order.taskId)
        }
    }

    fun onDeselectOrder(order: PharmacyUseCaseData.PrescriptionOrder) {
        navState.update {
            it.copy(unSelectedPrescriptions = it.unSelectedPrescriptions + order.taskId)
        }
    }

    fun onSaveContact(contact: PharmacyUseCaseData.ShippingContact) {
        viewModelScope.launch(dispatcher.default()) {
            useCase.saveShippingContact(contact)
        }
    }

    suspend fun triggerOrderInPharmacy(state: PharmacyScreenData.OrderScreenState): Result<Unit> {
        return if (demoUseCase.isDemoModeActive)
            orderDemoRecipe(state)
        else
            orderRecipe(state)
    }

    private fun orderDemoRecipe(state: PharmacyScreenData.OrderScreenState): Result<Unit> {
        demoUseCase.demoTasks.update { demoTasks ->
            demoTasks.map { task ->
                if (state.prescriptions.any { it.first.taskId == task.taskId })
                    task.copy(redeemedOn = OffsetDateTime.now())
                else
                    task
            }
        }
        return Result.success(Unit)
    }

    private suspend fun orderRecipe(state: PharmacyScreenData.OrderScreenState): Result<Unit> {
        return supervisorScope {
            withContext(dispatcher.io()) {
                val redeemOption = state.orderOption
                val telematikId = state.selectedPharmacy.telematikId
                val contact = requireNotNull(state.contact)

                val result = state.prescriptions.filter { it.second }
                    .map { (prescription, _) ->
                        async {
                            useCase.redeemPrescription(
                                profileName = state.activeProfile.name,
                                redeemOption = when (redeemOption) {
                                    PharmacyScreenData.OrderOption.ReserveInPharmacy -> RemoteRedeemOption.Local
                                    PharmacyScreenData.OrderOption.CourierDelivery -> RemoteRedeemOption.Delivery
                                    PharmacyScreenData.OrderOption.MailDelivery -> RemoteRedeemOption.Shipment
                                },
                                order = prescription,
                                contact = contact,
                                pharmacyTelematikId = telematikId
                            )
                        }
                    }
                    .awaitAll()
                    .find { it.isFailure }

                result?.let { Result.failure(it.exceptionOrNull()!!) } ?: Result.success(Unit)
            }
        }
    }

    fun isDemoMode() = demoUseCase.isDemoModeActive
}

val locationPermissions = arrayOf(
    Manifest.permission.ACCESS_FINE_LOCATION,
    Manifest.permission.ACCESS_COARSE_LOCATION
)

fun anyLocationPermissionGranted(context: Context) =
    locationPermissions.any {
        ContextCompat.checkSelfPermission(
            context,
            it
        ) == PackageManager.PERMISSION_GRANTED
    }
