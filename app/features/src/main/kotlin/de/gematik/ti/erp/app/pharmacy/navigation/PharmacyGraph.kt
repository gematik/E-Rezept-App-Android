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

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.navigation
import de.gematik.ti.erp.app.navigation.renderBottomSheet
import de.gematik.ti.erp.app.navigation.renderComposable
import de.gematik.ti.erp.app.navigation.slideInDown
import de.gematik.ti.erp.app.navigation.slideOutUp
import de.gematik.ti.erp.app.pharmacy.presentation.PharmacyGraphController
import de.gematik.ti.erp.app.pharmacy.ui.screens.PharmacyDetailsFromMessageScreen
import de.gematik.ti.erp.app.pharmacy.ui.screens.PharmacyDetailsFromPharmacyScreen
import de.gematik.ti.erp.app.pharmacy.ui.screens.PharmacyFilterSheetScreen
import de.gematik.ti.erp.app.pharmacy.ui.screens.PharmacySearchListScreen
import de.gematik.ti.erp.app.pharmacy.ui.screens.PharmacySearchMapsScreen
import de.gematik.ti.erp.app.pharmacy.ui.screens.PharmacyStartScreen
import org.kodein.di.DI
import org.kodein.di.instance

fun NavGraphBuilder.pharmacyGraph(
    dependencyInjector: DI,
    startDestination: String = PharmacyRoutes.PharmacyStartScreen.path(""),
    navController: NavController
) {
    val controller by dependencyInjector.instance<PharmacyGraphController>()

    navigation(
        startDestination = startDestination,
        route = PharmacyRoutes.subGraphName()
    ) {
        renderComposable(
            stackEnterAnimation = { slideInDown() },
            stackExitAnimation = { slideOutUp() },
            popExitAnimation = { slideOutUp() },
            route = PharmacyRoutes.PharmacyStartScreen.route,
            arguments = PharmacyRoutes.PharmacyStartScreen.arguments
        ) { navEntry ->
            PharmacyStartScreen(
                navController = navController,
                navBackStackEntry = navEntry,
                graphController = controller
            )
        }
        // same screen as above, it is duplicated for the navigation graph so that it is called
        // in a modal flow and bottom sheet is not shown
        renderComposable(
            route = PharmacyRoutes.PharmacyStartScreenModal.route,
            arguments = PharmacyRoutes.PharmacyStartScreenModal.arguments
        ) { navEntry ->
            PharmacyStartScreen(
                navController = navController,
                navBackStackEntry = navEntry,
                graphController = controller
            )
        }
        renderBottomSheet(
            route = PharmacyRoutes.PharmacyFilterSheetScreen.route,
            arguments = PharmacyRoutes.PharmacyFilterSheetScreen.arguments
        ) { navEntry ->
            PharmacyFilterSheetScreen(
                navController = navController,
                navBackStackEntry = navEntry,
                graphController = controller
            )
        }
        renderComposable(
            route = PharmacyRoutes.PharmacySearchListScreen.route,
            arguments = PharmacyRoutes.PharmacySearchListScreen.arguments
        ) {
            PharmacySearchListScreen(
                navController = navController,
                navBackStackEntry = it,
                graphController = controller
            )
        }
        renderComposable(
            route = PharmacyRoutes.PharmacySearchMapsScreen.route,
            arguments = PharmacyRoutes.PharmacySearchMapsScreen.arguments
        ) {
            PharmacySearchMapsScreen(
                navController = navController,
                navBackStackEntry = it,
                graphController = controller
            )
        }
        renderBottomSheet(
            route = PharmacyRoutes.PharmacyDetailsFromPharmacyScreen.route,
            arguments = PharmacyRoutes.PharmacyDetailsFromPharmacyScreen.arguments
        ) {
            PharmacyDetailsFromPharmacyScreen(
                navController = navController,
                navBackStackEntry = it,
                graphController = controller
            )
        }
        renderBottomSheet(
            route = PharmacyRoutes.PharmacyDetailsFromMessageScreen.route,
            arguments = PharmacyRoutes.PharmacyDetailsFromMessageScreen.arguments
        ) {
            PharmacyDetailsFromMessageScreen(
                navController = navController,
                navBackStackEntry = it
            )
        }
    }
}
