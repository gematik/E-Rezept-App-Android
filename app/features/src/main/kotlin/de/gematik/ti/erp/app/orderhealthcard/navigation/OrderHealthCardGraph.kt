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

package de.gematik.ti.erp.app.orderhealthcard.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.navigation
import de.gematik.ti.erp.app.navigation.renderComposable
import de.gematik.ti.erp.app.orderhealthcard.presentation.OrderHealthCardGraphController
import de.gematik.ti.erp.app.orderhealthcard.ui.OrderHealthCardSelectInsuranceCompanyScreen
import de.gematik.ti.erp.app.orderhealthcard.ui.OrderHealthCardSelectMethodScreen
import de.gematik.ti.erp.app.orderhealthcard.ui.OrderHealthCardSelectOptionScreen
import org.kodein.di.DI
import org.kodein.di.instance

fun NavGraphBuilder.orderHealthCardGraph(
    dependencyInjector: DI,
    startDestination: String = OrderHealthCardRoutes.OrderHealthCardSelectInsuranceCompanyScreen.route,
    navController: NavController
) {
    val controller by dependencyInjector.instance<OrderHealthCardGraphController>()

    navigation(
        startDestination = startDestination,
        route = OrderHealthCardRoutes.subGraphName()
    ) {
        renderComposable(
            route = OrderHealthCardRoutes.OrderHealthCardSelectInsuranceCompanyScreen.route
        ) { navEntry ->
            OrderHealthCardSelectInsuranceCompanyScreen(
                navController = navController,
                navBackStackEntry = navEntry,
                graphController = controller
            )
        }
        renderComposable(
            route = OrderHealthCardRoutes.OrderHealthCardSelectMethodScreen.route
        ) { navEntry ->
            OrderHealthCardSelectMethodScreen(
                navController = navController,
                navBackStackEntry = navEntry,
                graphController = controller
            )
        }
        renderComposable(
            route = OrderHealthCardRoutes.OrderHealthCardSelectOptionScreen.route
        ) { navEntry ->
            OrderHealthCardSelectOptionScreen(
                navController = navController,
                navBackStackEntry = navEntry,
                graphController = controller
            )
        }
    }
}
