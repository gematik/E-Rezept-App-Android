/*
 * Copyright 2024, gematik GmbH
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

package de.gematik.ti.erp.app.pkv.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.navigation
import de.gematik.ti.erp.app.navigation.renderComposable
import de.gematik.ti.erp.app.navigation.slideInDown
import de.gematik.ti.erp.app.navigation.slideInRight
import de.gematik.ti.erp.app.navigation.slideOutLeft
import de.gematik.ti.erp.app.navigation.slideOutUp
import de.gematik.ti.erp.app.pkv.ui.screens.InvoiceDetailsScreen
import de.gematik.ti.erp.app.pkv.ui.screens.InvoiceExpandedDetailsScreen
import de.gematik.ti.erp.app.pkv.ui.screens.InvoiceListScreen
import de.gematik.ti.erp.app.pkv.ui.screens.InvoiceLocalCorrectionScreen
import de.gematik.ti.erp.app.pkv.ui.screens.InvoiceShareScreen

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
            arguments = PkvRoutes.InvoiceListScreen.arguments,
            stackEnterAnimation = { slideInDown() },
            stackExitAnimation = { slideOutUp() }
        ) { navEntry ->
            InvoiceListScreen(
                navController = navController,
                navBackStackEntry = navEntry
            )
        }
        renderComposable(
            route = PkvRoutes.InvoiceDetailsScreen.route,
            arguments = PkvRoutes.InvoiceDetailsScreen.arguments,
            stackEnterAnimation = { slideInDown() },
            stackExitAnimation = { slideOutUp() }
        ) { navEntry ->
            InvoiceDetailsScreen(
                navController = navController,
                navBackStackEntry = navEntry
            )
        }
        renderComposable(
            route = PkvRoutes.InvoiceExpandedDetailsScreen.route,
            arguments = PkvRoutes.InvoiceExpandedDetailsScreen.arguments,
            stackEnterAnimation = { slideInRight() },
            stackExitAnimation = { slideOutLeft() }
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
