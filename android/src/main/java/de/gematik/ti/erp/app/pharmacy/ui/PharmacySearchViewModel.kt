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
import de.gematik.ti.erp.app.pharmacy.usecase.PharmacyOverviewUseCase
import de.gematik.ti.erp.app.pharmacy.usecase.PharmacySearchUseCase
import de.gematik.ti.erp.app.pharmacy.usecase.model.PharmacyUseCaseData
import de.gematik.ti.erp.app.profiles.usecase.ProfilesUseCase
import de.gematik.ti.erp.app.profiles.usecase.activeProfile
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.transform
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

sealed class PharmacySearchUi {
    class Pharmacy(val pharmacy: PharmacyUseCaseData.Pharmacy) : PharmacySearchUi()
}

class PharmacySearchViewModel(
    private val useCase: PharmacySearchUseCase,
    private val pharmacyOverviewUseCase: PharmacyOverviewUseCase,
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
        val selectedPharmacy: PharmacyUseCaseData.Pharmacy?,
        val isMarkedAsFavorite: Boolean
    )

    private val navState = MutableStateFlow(
        NavState(
            unSelectedPrescriptions = setOf(),
            selectedOrderOption = null,
            selectedPharmacy = null,
            isMarkedAsFavorite = false
        )
    )

    fun detailScreenState() = navState.transform {
        if (it.selectedPharmacy != null) {
            emit(
                PharmacyScreenData.DetailScreenState(
                    it.selectedPharmacy,
                    it.isMarkedAsFavorite
                )
            )
        }
    }
    suspend fun saveOrUpdateFavoritePharmacy(pharmacy: PharmacyUseCaseData.Pharmacy) {
        viewModelScope.launch(dispatchers.IO) {
            pharmacyOverviewUseCase.saveOrUpdateFavoritePharmacy(pharmacy)
        }
    }

    suspend fun deleteFavoritePharmacy(pharmacy: PharmacyUseCaseData.Pharmacy) {
        viewModelScope.launch(dispatchers.IO) {
            pharmacyOverviewUseCase.deleteFavoritePharmacy(pharmacy)
        }
    }

    suspend fun onSelectPharmacy(pharmacy: PharmacyUseCaseData.Pharmacy) {
        val isMarkedAsFavorite = pharmacyOverviewUseCase.isPharmacyInFavorites(pharmacy.telematikId)
        navState.update {
            it.copy(selectedPharmacy = pharmacy, isMarkedAsFavorite = isMarkedAsFavorite)
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

    suspend fun isPharmacyInFavorites(telematikId: String): Boolean = withContext(dispatchers.IO) {
        pharmacyOverviewUseCase.isPharmacyInFavorites(telematikId)
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
