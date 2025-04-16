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

package de.gematik.ti.erp.app.cardwall.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.navigation
import de.gematik.ti.erp.app.cardwall.presentation.CardWallGraphController
import de.gematik.ti.erp.app.cardwall.ui.screens.CardWallCanScreen
import de.gematik.ti.erp.app.cardwall.ui.screens.CardWallGidHelpScreen
import de.gematik.ti.erp.app.cardwall.ui.screens.CardWallGidListScreen
import de.gematik.ti.erp.app.cardwall.ui.screens.CardWallIntroScreen
import de.gematik.ti.erp.app.cardwall.ui.screens.CardWallPinScreen
import de.gematik.ti.erp.app.cardwall.ui.screens.CardWallReadCardScreen
import de.gematik.ti.erp.app.cardwall.ui.screens.CardWallSaveCredentialsInfoScreen
import de.gematik.ti.erp.app.cardwall.ui.screens.CardWallSaveCredentialsScreen
import de.gematik.ti.erp.app.navigation.renderComposable
import org.kodein.di.DI
import org.kodein.di.instance

fun NavGraphBuilder.cardWallGraph(
    dependencyInjector: DI,
    startDestination: String = CardWallRoutes.CardWallIntroScreen.route,
    navController: NavController
) {
    val controller by dependencyInjector.instance<CardWallGraphController>()

    navigation(
        startDestination = startDestination,
        route = CardWallRoutes.subGraphName()
    ) {
        renderComposable(
            route = CardWallRoutes.CardWallIntroScreen.route,
            arguments = CardWallRoutes.CardWallIntroScreen.arguments
        ) { navEntry ->
            CardWallIntroScreen(
                navController = navController,
                navBackStackEntry = navEntry,
                graphController = controller
            )
        }
        renderComposable(
            route = CardWallRoutes.CardWallCanScreen.route,
            arguments = CardWallRoutes.CardWallCanScreen.arguments
        ) { navEntry ->
            CardWallCanScreen(
                navController = navController,
                navBackStackEntry = navEntry,
                graphController = controller
            )
        }
        renderComposable(
            route = CardWallRoutes.CardWallPinScreen.route,
            arguments = CardWallRoutes.CardWallPinScreen.arguments
        ) { navEntry ->
            CardWallPinScreen(
                navController = navController,
                navBackStackEntry = navEntry,
                graphController = controller
            )
        }
        renderComposable(
            route = CardWallRoutes.CardWallSaveCredentialsScreen.route,
            arguments = CardWallRoutes.CardWallSaveCredentialsScreen.arguments
        ) { navEntry ->
            CardWallSaveCredentialsScreen(
                navController = navController,
                navBackStackEntry = navEntry,
                graphController = controller
            )
        }
        renderComposable(
            route = CardWallRoutes.CardWallSaveCredentialsInfoScreen.route,
            arguments = CardWallRoutes.CardWallSaveCredentialsInfoScreen.arguments
        ) { navEntry ->
            CardWallSaveCredentialsInfoScreen(
                navController = navController,
                navBackStackEntry = navEntry,
                graphController = controller
            )
        }
        renderComposable(
            route = CardWallRoutes.CardWallReadCardScreen.route,
            arguments = CardWallRoutes.CardWallReadCardScreen.arguments
        ) { navEntry ->
            CardWallReadCardScreen(
                navController = navController,
                navBackStackEntry = navEntry,
                graphController = controller
            )
        }
        renderComposable(
            route = CardWallRoutes.CardWallGidListScreen.route,
            arguments = CardWallRoutes.CardWallGidListScreen.arguments
        ) { navEntry ->
            CardWallGidListScreen(
                navController = navController,
                navBackStackEntry = navEntry,
                graphController = controller
            )
        }
        renderComposable(
            route = CardWallRoutes.CardWallGidHelpScreen.route
        ) { navEntry ->
            CardWallGidHelpScreen(
                navController = navController,
                navBackStackEntry = navEntry,
                graphController = controller
            )
        }
    }
}
