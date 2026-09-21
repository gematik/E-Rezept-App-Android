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

package de.gematik.ti.erp.app.pharmacy.model

import de.gematik.ti.erp.app.Requirement

@Requirement(
    "A_20285#5",
    sourceSpecification = "gemSpec_eRp_FdV",
    rationale = "The initial filter[PharmacySearchFilterErpModel] are based on the UI of the app which is then mapped into the filter[PharmacyFilter]."
)
data class SearchFilterErpModel(
    val nearBy: Boolean = false,
    val pickup: Boolean = false,
    val deliveryService: Boolean = false,
    val onlineService: Boolean = false,
    val openNow: Boolean = false,
    val recentlyUsed: Boolean = false,
    val onSiteFeatures: Set<String> = emptySet(),
    val availableServices: Set<String> = emptySet()
) {
    fun isAnySet(): Boolean =
        nearBy || pickup || deliveryService || onlineService || openNow || recentlyUsed ||
            onSiteFeatures.isNotEmpty() || availableServices.isNotEmpty()
}

sealed class LocationModeErpModel {
    data object Disabled : LocationModeErpModel()

    data class Enabled(
        val coordinates: PositionErpModel,
        val radiusInMeter: Double = DEFAULT_RADIUS_IN_KM
    ) : LocationModeErpModel()
}

data class PharmacySearchDataErpModel(
    val name: String,
    val filter: SearchFilterErpModel,
    val locationMode: LocationModeErpModel
)

data class PharmacyMapsSearchDataErpModel(
    val name: String,
    val filter: SearchFilterErpModel,
    val locationMode: LocationModeErpModel,
    val coordinates: PositionErpModel?
)

const val DEFAULT_RADIUS_IN_KM = 100.0
