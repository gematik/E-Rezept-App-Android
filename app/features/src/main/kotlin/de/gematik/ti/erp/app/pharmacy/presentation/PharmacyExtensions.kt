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

import com.google.android.gms.maps.model.LatLng
import de.gematik.ti.erp.app.pharmacy.model.LocationModeErpModel
import de.gematik.ti.erp.app.pharmacy.model.PharmacyDetailsErpModel
import de.gematik.ti.erp.app.pharmacy.model.PharmacyServiceErpModel
import de.gematik.ti.erp.app.pharmacy.model.PositionErpModel
import de.gematik.ti.erp.app.pharmacy.model.isOpenAt
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

internal const val WILDCARD = ""
internal fun PharmacyDetailsErpModel.location(locationMode: LocationModeErpModel) =
    when (locationMode) {
        is LocationModeErpModel.Enabled -> copy(
            distance = coordinates?.minus(locationMode.coordinates)
        )

        else -> this
    }

internal fun PharmacyDetailsErpModel.deliveryService(isDeliveryServiceFiltered: Boolean) =
    when {
        isDeliveryServiceFiltered -> provides.any { it is PharmacyServiceErpModel.DeliveryPharmacyServiceErpModel }
        else -> true
    }

internal fun PharmacyDetailsErpModel.onlineService(isOnlineServiceFiltered: Boolean) =
    when {
        isOnlineServiceFiltered -> provides.any { it is PharmacyServiceErpModel.OnlinePharmacyServiceErpModel }
        else -> true
    }

internal fun PharmacyDetailsErpModel.isOpenNow(isOpenNow: Boolean): Boolean =
    if (isOpenNow) {
        openingHours?.isOpenAt(
            Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
        ) ?: false
    } else {
        true
    }

internal fun PharmacyDetailsErpModel.recentlyUsed(
    isRecentlyUsedFiltered: Boolean,
    oftenUsedTelematikIds: Set<String>
) =
    when {
        isRecentlyUsedFiltered -> telematikId in oftenUsedTelematikIds
        else -> true
    }

internal fun PositionErpModel.toLatLng() = LatLng(latitude, longitude)

internal fun LocationModeErpModel.Enabled.toLatLng() = coordinates.toLatLng()

internal fun LatLng.toCoordinates() = PositionErpModel(latitude, longitude)

internal fun PharmacyDetailsErpModel.hasAllOnSiteFeatures(codes: Set<String>) =
    if (codes.isEmpty()) true else codes.all { code -> onSiteFeatures.any { it.code == code } }

internal fun PharmacyDetailsErpModel.hasAllAvailableServices(codes: Set<String>) =
    if (codes.isEmpty()) true else codes.all { code -> availableServices.any { it.code == code } }
