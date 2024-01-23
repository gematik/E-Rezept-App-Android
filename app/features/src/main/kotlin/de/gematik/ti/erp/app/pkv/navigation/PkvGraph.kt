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

package de.gematik.ti.erp.app.pkv.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.navigation
import de.gematik.ti.erp.app.navigation.renderComposable
import de.gematik.ti.erp.app.pkv.ui.InvoiceDetailsScreen
import de.gematik.ti.erp.app.pkv.ui.InvoiceExpandedDetailsScreen
import de.gematik.ti.erp.app.pkv.ui.InvoiceListScreen
import de.gematik.ti.erp.app.pkv.ui.InvoiceLocalCorrectionScreen
import de.gematik.ti.erp.app.pkv.ui.InvoiceShareScreen

fun NavGraphBuilder.pkvGraph(
    startDestination: String = PkvRoutes.InvoiceListScreen.route,
    navController: NavController
) {
    navigation(
        startDestination = startDestination,
        route = PkvRoutes.subGraphName()
    ) {
        renderComposable(
            route = PkvRoutes.InvoiceListScreen.route,
            arguments = PkvRoutes.InvoiceListScreen.arguments
        ) { navEntry ->
            InvoiceListScreen(
                navController = navController,
                navBackStackEntry = navEntry
            )
        }
        renderComposable(
            route = PkvRoutes.InvoiceDetailsScreen.route,
            arguments = PkvRoutes.InvoiceDetailsScreen.arguments
        ) { navEntry ->
            InvoiceDetailsScreen(
                navController = navController,
                navBackStackEntry = navEntry
            )
        }
        renderComposable(
            route = PkvRoutes.InvoiceExpandedDetailsScreen.route,
            arguments = PkvRoutes.InvoiceExpandedDetailsScreen.arguments
        ) { navEntry ->
            InvoiceExpandedDetailsScreen(
                navController = navController,
                navBackStackEntry = navEntry
            )
        }
        renderComposable(
            route = PkvRoutes.InvoiceLocalCorrectionScreen.route,
            arguments = PkvRoutes.InvoiceLocalCorrectionScreen.arguments
        ) { navEntry ->
            InvoiceLocalCorrectionScreen(
                navController = navController,
                navBackStackEntry = navEntry
            )
        }
        renderComposable(
            route = PkvRoutes.InvoiceShareScreen.route,
            arguments = PkvRoutes.InvoiceShareScreen.arguments
        ) { navEntry ->
            InvoiceShareScreen(
                navController = navController,
                navBackStackEntry = navEntry
            )
        }
    }
}
