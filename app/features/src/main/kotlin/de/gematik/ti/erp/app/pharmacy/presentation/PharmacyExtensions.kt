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

import com.google.android.gms.maps.model.LatLng
import de.gematik.ti.erp.app.pharmacy.usecase.model.PharmacyUseCaseData
import de.gematik.ti.erp.app.pharmacy.usecase.model.PharmacyUseCaseData.isOpenAt
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

internal const val WILDCARD = ""
internal fun PharmacyUseCaseData.Pharmacy.location(locationMode: PharmacyUseCaseData.LocationMode) =
    when (locationMode) {
        is PharmacyUseCaseData.LocationMode.Enabled -> copy(
            distance = coordinates?.minus(locationMode.coordinates)
        )
        else -> this
    }

internal fun PharmacyUseCaseData.Pharmacy.deliveryService(isDeliveryServiceFiltered: Boolean) =
    when {
        isDeliveryServiceFiltered -> provides.any { it is PharmacyUseCaseData.PharmacyService.DeliveryPharmacyService }
        else -> true
    }

internal fun PharmacyUseCaseData.Pharmacy.onlineService(isOnlineServiceFiltered: Boolean) =
    when {
        isOnlineServiceFiltered -> provides.any { it is PharmacyUseCaseData.PharmacyService.OnlinePharmacyService }
        else -> true
    }

internal fun PharmacyUseCaseData.Pharmacy.isOpenNow(isOpenNow: Boolean) =
    if (isOpenNow) {
        openingHours?.isOpenAt(
            Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
        ) ?: false
    } else {
        true
    }

internal fun PharmacyUseCaseData.Coordinates.toLatLng() = LatLng(latitude, longitude)

internal fun PharmacyUseCaseData.LocationMode.Enabled.toLatLng() = coordinates.toLatLng()

internal fun LatLng.toCoordinates() = PharmacyUseCaseData.Coordinates(latitude, longitude)
