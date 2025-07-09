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

package de.gematik.ti.erp.app.pharmacy.navigation

import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavType
import androidx.navigation.navArgument
import de.gematik.ti.erp.app.navigation.NavigationRouteNames
import de.gematik.ti.erp.app.navigation.NavigationRoutes
import de.gematik.ti.erp.app.navigation.Routes
import de.gematik.ti.erp.app.navigation.fromNavigationString
import de.gematik.ti.erp.app.navigation.toNavigationString
import de.gematik.ti.erp.app.pharmacy.navigation.PharmacyRoutes.EMPTY_TASK_ID
import de.gematik.ti.erp.app.pharmacy.usecase.model.PharmacyUseCaseData

object PharmacyRoutes : NavigationRoutes {

    override fun subGraphName(): String = "pharmacies"

    const val EMPTY_TASK_ID = ""

    const val PHARMACY_NAV_NEARBY_FILTER = "nearByFilter"
    const val PHARMACY_NAV_WITH_START_BUTTON = "showStartSearchButton"
    const val PHARMACY_NAV_SELECTED_PHARMACY = "Pharmacy"
    const val PHARMACY_NAV_SHOW_BACK_ON_START_SCREEN = "ShowBackOnStartScreen"
    const val PHARMACY_NAV_TASK_ID = "taskId"

    object PharmacyStartScreen : Routes(
        path = NavigationRouteNames.PharmacyStartScreen.name,
        navArgument(PHARMACY_NAV_SHOW_BACK_ON_START_SCREEN) { type = NavType.BoolType },
        navArgument(PHARMACY_NAV_TASK_ID) { type = NavType.StringType }
    ) {
        fun path(taskId: String) =
            path(
                PHARMACY_NAV_SHOW_BACK_ON_START_SCREEN to false,
                PHARMACY_NAV_TASK_ID to taskId
            )
    }

    object PharmacyStartScreenModal : Routes(
        path = NavigationRouteNames.PharmacyStartScreenModal.name,
        navArgument(PHARMACY_NAV_SHOW_BACK_ON_START_SCREEN) { type = NavType.BoolType },
        navArgument(PHARMACY_NAV_TASK_ID) { type = NavType.StringType }
    ) {
        fun path(taskId: String) =
            path(
                PHARMACY_NAV_SHOW_BACK_ON_START_SCREEN to true,
                PHARMACY_NAV_TASK_ID to taskId
            )
    }

    object PharmacyFilterSheetScreen : Routes(
        path = NavigationRouteNames.PharmacyFilterSheetScreen.name,
        navArgument(PHARMACY_NAV_NEARBY_FILTER) { type = NavType.BoolType },
        navArgument(PHARMACY_NAV_WITH_START_BUTTON) { type = NavType.BoolType }
    ) {
        fun path(showNearbyFilter: Boolean, navigateWithSearchButton: Boolean) =
            path(PHARMACY_NAV_NEARBY_FILTER to showNearbyFilter, PHARMACY_NAV_WITH_START_BUTTON to navigateWithSearchButton)
    }

    object PharmacySearchListScreen : Routes(
        NavigationRouteNames.PharmacySearchListScreen.name,
        navArgument(PHARMACY_NAV_TASK_ID) { type = NavType.StringType }
    ) {
        fun path(taskId: String) =
            path(
                PHARMACY_NAV_TASK_ID to taskId
            )
    }

    object PharmacySearchMapsScreen : Routes(
        NavigationRouteNames.PharmacySearchMapsScreen.name,
        navArgument(PHARMACY_NAV_TASK_ID) { type = NavType.StringType }
    ) {
        fun path(taskId: String) =
            path(
                PHARMACY_NAV_TASK_ID to taskId
            )
    }

    object PharmacyDetailsFromPharmacyScreen : Routes(
        path = NavigationRouteNames.PharmacyDetailsFromMessageScreen.name,
        navArgument(PHARMACY_NAV_SELECTED_PHARMACY) { type = NavType.StringType },
        navArgument(PHARMACY_NAV_TASK_ID) { type = NavType.StringType }
    ) {
        fun path(pharmacy: PharmacyUseCaseData.Pharmacy, taskId: String): String =
            PharmacyDetailsFromPharmacyScreen.path(
                PHARMACY_NAV_SELECTED_PHARMACY to pharmacy.toNavigationString(),
                PHARMACY_NAV_TASK_ID to taskId
            )
    }

    object PharmacyDetailsFromMessageScreen : Routes(
        path = NavigationRouteNames.PharmacyDetailsFromPharmacyScreen.name,
        navArgument(PHARMACY_NAV_SELECTED_PHARMACY) { type = NavType.StringType },
        navArgument(PHARMACY_NAV_TASK_ID) { type = NavType.StringType }
    ) {
        fun path(pharmacy: PharmacyUseCaseData.Pharmacy, taskId: String): String =
            PharmacyDetailsFromMessageScreen.path(
                PHARMACY_NAV_SELECTED_PHARMACY to pharmacy.toNavigationString(),
                PHARMACY_NAV_TASK_ID to taskId
            )
    }
}

class PharmacyRouteBackStackEntryArguments(
    private val navBackStackEntry: NavBackStackEntry
) {
    fun getPharmacy(): PharmacyUseCaseData.Pharmacy? =
        navBackStackEntry.arguments?.let { bundle ->
            bundle.getString(PharmacyRoutes.PHARMACY_NAV_SELECTED_PHARMACY)?.let {
                fromNavigationString<PharmacyUseCaseData.Pharmacy>(it)
            }
        }

    fun getTaskId(): String =
        navBackStackEntry.arguments?.getString(PharmacyRoutes.PHARMACY_NAV_TASK_ID) ?: EMPTY_TASK_ID
}
