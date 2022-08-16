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
import android.content.Context
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import de.gematik.ti.erp.app.DispatchProvider
import de.gematik.ti.erp.app.pharmacy.ui.model.PharmacyScreenData
import de.gematik.ti.erp.app.pharmacy.usecase.OftenUsedPharmaciesUseCase
import de.gematik.ti.erp.app.pharmacy.usecase.PharmacySearchUseCase
import de.gematik.ti.erp.app.pharmacy.usecase.model.PharmacyUseCaseData
import de.gematik.ti.erp.app.prescription.repository.RemoteRedeemOption
import de.gematik.ti.erp.app.profiles.usecase.ProfilesUseCase
import de.gematik.ti.erp.app.profiles.usecase.activeProfile
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.transform
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.supervisorScope
import kotlinx.coroutines.withContext
import java.util.UUID

sealed class PharmacySearchUi {
    class Pharmacy(val pharmacy: PharmacyUseCaseData.Pharmacy) : PharmacySearchUi()
}

class PharmacySearchViewModel(
    private val useCase: PharmacySearchUseCase,
    private val oftenUseCase: OftenUsedPharmaciesUseCase,
    private val profilesUseCase: ProfilesUseCase,
    private val dispatchers: DispatchProvider
) : ViewModel() {

    @OptIn(ExperimentalCoroutinesApi::class)
    fun hasRedeemableTasks(): Flow<Boolean> =
        profilesUseCase.profiles.map { it.activeProfile() }.flatMapLatest {
            useCase.hasRedeemableTasks(it.id)
        }

    private data class NavState(
        // orders identified by their taskId
        val unSelectedPrescriptions: Set<String>,
        val selectedOrderOption: PharmacyScreenData.OrderOption?,
        val selectedPharmacy: PharmacyUseCaseData.Pharmacy?
    )

    private val navState = MutableStateFlow(
        NavState(
            unSelectedPrescriptions = setOf(),
            selectedOrderOption = null,
            selectedPharmacy = null
        )
    )

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

    @OptIn(ExperimentalCoroutinesApi::class)
    fun orderScreenState(): Flow<PharmacyScreenData.OrderScreenState> =
        profilesUseCase.profiles.map { it.activeProfile() }.flatMapLatest { activeProfile ->
            combine(
                useCase.prescriptionDetailsForOrdering(activeProfile.id),
                navState.filter { it.selectedPharmacy != null && it.selectedOrderOption != null }
            ) { state, navState ->
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
        viewModelScope.launch {
            useCase.saveShippingContact(contact)
        }
    }

    suspend fun triggerOrderInPharmacy(state: PharmacyScreenData.OrderScreenState): Result<Unit> =
        orderRecipe(state)

    private suspend fun orderRecipe(state: PharmacyScreenData.OrderScreenState): Result<Unit> {
        return supervisorScope {
            withContext(dispatchers.IO) {
                val redeemOption = state.orderOption
                val telematikId = state.selectedPharmacy.telematikId
                val contact = requireNotNull(state.contact)
                val orderId = UUID.randomUUID()

                val result = state.prescriptions.filter { it.second }
                    .map { (prescription, _) ->
                        async {
                            useCase.redeemPrescription(
                                orderId = orderId,
                                profileId = state.activeProfile.id,
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
                oftenUseCase.saveOrUpdateUsedPharmacies(state.selectedPharmacy)
                result?.let { Result.failure(it.exceptionOrNull()!!) } ?: Result.success(Unit)
            }
        }
    }
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
