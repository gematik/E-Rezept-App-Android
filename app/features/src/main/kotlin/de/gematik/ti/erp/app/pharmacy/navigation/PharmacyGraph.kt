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

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.navigation
import de.gematik.ti.erp.app.navigation.renderBottomSheet
import de.gematik.ti.erp.app.navigation.renderComposable
import de.gematik.ti.erp.app.pharmacy.presentation.PharmacyGraphController
import de.gematik.ti.erp.app.pharmacy.ui.PharmacyFilterSheetScreen
import de.gematik.ti.erp.app.pharmacy.ui.PharmacyStartScreen
import org.kodein.di.DI
import org.kodein.di.instance

fun NavGraphBuilder.pharmacyGraph(
    dependencyInjector: DI,
    startDestination: String = PharmacyRoutes.PharmacyStartScreen.route,
    navController: NavController
) {
    val controller by dependencyInjector.instance<PharmacyGraphController>()

    navigation(
        startDestination = startDestination,
        route = PharmacyRoutes.subGraphName()
    ) {
        renderComposable(
            route = PharmacyRoutes.PharmacyStartScreen.route,
            arguments = PharmacyRoutes.PharmacyStartScreen.arguments
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
        // TODO: PharmacySearchListScreen
        // TODO: PharmacySearchMapsScreen
        // TODO: PharmacyOrderOverviewScreen
        // TODO: PharmacyEditShippingContactScreen
        // TODO: PharmacyPrescriptionSelectionScreen
    }
}
