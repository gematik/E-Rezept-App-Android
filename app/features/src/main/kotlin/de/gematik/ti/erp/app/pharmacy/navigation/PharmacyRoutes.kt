/*
 * Copyright (c) 2024 gematik GmbH
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

package de.gematik.ti.erp.app.pharmacy.navigation

import androidx.navigation.NavType
import androidx.navigation.navArgument
import de.gematik.ti.erp.app.navigation.NavigationRouteNames
import de.gematik.ti.erp.app.navigation.NavigationRoutes
import de.gematik.ti.erp.app.navigation.Routes

object PharmacyRoutes : NavigationRoutes {

    override fun subGraphName(): String = "pharmacies"

    const val NearByFilterArgument = "nearByFilter"
    const val ShowStartSearchButton = "showStartSearchButton"

    object PharmacyStartScreen : Routes(NavigationRouteNames.PharmacyStartScreen.name)
    object PharmacyFilterSheetScreen : Routes(
        path = NavigationRouteNames.PharmacyFilterSheetScreen.name,
        navArgument(NearByFilterArgument) { type = NavType.BoolType },
        navArgument(ShowStartSearchButton) { type = NavType.BoolType }
    ) {
        fun path(showNearbyFilter: Boolean, showButton: Boolean) =
            path(NearByFilterArgument to showNearbyFilter, ShowStartSearchButton to showButton)
    }
    object PharmacySearchListScreen : Routes(NavigationRouteNames.PharmacySearchListScreen.name)
    object PharmacySearchMapsScreen : Routes(NavigationRouteNames.PharmacySearchMapsScreen.name)
    object PharmacyOrderOverviewScreen : Routes(NavigationRouteNames.PharmacyOrderOverviewScreen.name)
    object PharmacyEditShippingContactScreen : Routes(NavigationRouteNames.PharmacyEditShippingContactScreen.name)
    object PharmacyPrescriptionSelectionScreen : Routes(NavigationRouteNames.PharmacyPrescriptionSelectionScreen.name)
}
