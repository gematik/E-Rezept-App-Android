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

package de.gematik.ti.erp.app.pharmacy.usecase.mapper

import de.gematik.ti.erp.app.pharmacy.model.DEFAULT_RADIUS_IN_KM
import de.gematik.ti.erp.app.pharmacy.model.LocationModeErpModel
import de.gematik.ti.erp.app.pharmacy.model.PharmacyMapsSearchDataErpModel
import de.gematik.ti.erp.app.pharmacy.model.PharmacySearchDataErpModel
import de.gematik.ti.erp.app.pharmacy.usecase.model.LocationFilter
import de.gematik.ti.erp.app.pharmacy.usecase.model.PharmacyFilter
import de.gematik.ti.erp.app.pharmacy.usecase.model.TextFilter.Companion.toTextFilter

fun PharmacySearchDataErpModel.toPharmacyFilter(): PharmacyFilter {
    val location = locationMode as? LocationModeErpModel.Enabled
    return PharmacyFilter.create(
        locationFilter = location?.let {
            LocationFilter(
                latitude = it.coordinates.latitude,
                longitude = it.coordinates.longitude
            )
        },
        textFilter = name.toTextFilter(),
        courier = filter.deliveryService,
        shipment = filter.onlineService,
        pickup = filter.pickup,
        availableServiceCodes = filter.availableServices,
        onSiteFeatureCodes = filter.onSiteFeatures
    )
}

fun PharmacyMapsSearchDataErpModel.toPharmacyFilter(forcedRadius: Double?): PharmacyFilter {
    val location = locationMode as? LocationModeErpModel.Enabled
    val coords = coordinates
    return PharmacyFilter.create(
        locationFilter = when {
            location != null ->
                LocationFilter(
                    latitude = location.coordinates.latitude,
                    longitude = location.coordinates.longitude,
                    radius = forcedRadius ?: DEFAULT_RADIUS_IN_KM
                )

            coords != null ->
                LocationFilter(
                    latitude = coords.latitude,
                    longitude = coords.longitude,
                    radius = forcedRadius ?: DEFAULT_RADIUS_IN_KM
                )

            else -> null
        },
        textFilter = name.toTextFilter(),
        courier = filter.deliveryService,
        shipment = filter.onlineService,
        pickup = filter.pickup,
        availableServiceCodes = filter.availableServices,
        onSiteFeatureCodes = filter.onSiteFeatures
    )
}
