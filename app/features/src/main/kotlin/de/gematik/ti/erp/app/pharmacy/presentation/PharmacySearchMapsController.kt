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

package de.gematik.ti.erp.app.pharmacy.presentation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import de.gematik.ti.erp.app.base.Controller
import de.gematik.ti.erp.app.pharmacy.usecase.PharmacyMapsUseCase
import de.gematik.ti.erp.app.pharmacy.usecase.model.DEFAULT_RADIUS_IN_KM
import de.gematik.ti.erp.app.pharmacy.usecase.model.PharmacyUseCaseData
import de.gematik.ti.erp.app.pharmacy.usecase.model.PharmacyUseCaseData.Coordinates
import de.gematik.ti.erp.app.utils.compose.ComposableEvent
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted.Companion.Lazily
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.shareIn
import org.kodein.di.compose.rememberInstance

class PharmacySearchMapsController(
    pharmacyFilter: PharmacyUseCaseData.Filter,
    coordinates: Coordinates?,
    private val pharmacyMapsUseCase: PharmacyMapsUseCase
) : Controller() {

    private val defaultSearch by lazy {
        PharmacyUseCaseData.MapsSearchData(
            name = WILDCARD,
            filter = pharmacyFilter,
            locationMode = coordinates?.let {
                PharmacyUseCaseData.LocationMode.Enabled(it)
            } ?: PharmacyUseCaseData.LocationMode.Disabled,
            coordinates = coordinates
        )
    }

    private val searchParams by lazy { MutableStateFlow(defaultSearch) }

    private val cameraRadius = MutableStateFlow(DEFAULT_RADIUS_IN_KM)

    val areMapsLoadingEvent = ComposableEvent<Boolean>()

    @OptIn(ExperimentalCoroutinesApi::class)
    private val pharmacies by lazy {
        searchParams.onEach {
            searchParams.value = it.copy(
                locationMode = (it.locationMode as? PharmacyUseCaseData.LocationMode.Enabled)
                    ?.copy(radiusInMeter = cameraRadius.value) ?: it.locationMode
            )
        }.flatMapLatest { searchParams ->
            areMapsLoadingEvent.trigger(true)
            flow {
                runCatching {
                    pharmacyMapsUseCase.invoke(searchParams, cameraRadius.value)
                        .map { it.location(searchParams.locationMode) }
                        .filter { it.deliveryService(searchParams.filter.deliveryService) }
                        .filter { it.onlineService(searchParams.filter.onlineService) }
                        .filter { it.isOpenNow(searchParams.filter.openNow) }
                }
                    .onSuccess {
                        areMapsLoadingEvent.trigger(false)
                        emit(it)
                    }
                    .onFailure {
                        areMapsLoadingEvent.trigger(false)
                    }
            }.shareIn(
                scope = controllerScope,
                started = Lazily,
                replay = 1
            )
        }
    }

    fun onCameraRadiusChanged(radius: Double) {
        cameraRadius.value = radius
    }

    val cameraRadiusState
        @Composable
        get() = cameraRadius.collectAsStateWithLifecycle()

    val pharmaciesState
        @Composable
        get() = pharmacies.collectAsStateWithLifecycle(emptyList())

    val searchParamState
        @Composable
        get() = searchParams.collectAsStateWithLifecycle()
}

@Composable
fun rememberPharmacySearchMapsController(
    pharmacyFilter: PharmacyUseCaseData.Filter,
    coordinates: Coordinates?
): PharmacySearchMapsController {
    val mapsUseCase by rememberInstance<PharmacyMapsUseCase>()

    return remember(pharmacyFilter, coordinates) {
        PharmacySearchMapsController(
            pharmacyFilter = pharmacyFilter,
            coordinates = coordinates,
            pharmacyMapsUseCase = mapsUseCase
        )
    }
}
