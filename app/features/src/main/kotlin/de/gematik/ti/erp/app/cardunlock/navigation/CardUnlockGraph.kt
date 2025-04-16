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

package de.gematik.ti.erp.app.cardunlock.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.navigation
import de.gematik.ti.erp.app.cardunlock.presentation.CardUnlockGraphController
import de.gematik.ti.erp.app.cardunlock.ui.screens.CardUnlockCanScreen
import de.gematik.ti.erp.app.cardunlock.ui.screens.CardUnlockEgkScreen
import de.gematik.ti.erp.app.cardunlock.ui.screens.CardUnlockIntroScreen
import de.gematik.ti.erp.app.cardunlock.ui.screens.CardUnlockNewSecretScreen
import de.gematik.ti.erp.app.cardunlock.ui.screens.CardUnlockOldSecretScreen
import de.gematik.ti.erp.app.cardunlock.ui.screens.CardUnlockPukScreen
import de.gematik.ti.erp.app.navigation.renderComposable
import org.kodein.di.DI
import org.kodein.di.instance

fun NavGraphBuilder.cardUnlockGraph(
    dependencyInjector: DI,
    startDestination: String = CardUnlockRoutes.CardUnlockIntroScreen.route,
    navController: NavController
) {
    val controller by dependencyInjector.instance<CardUnlockGraphController>()

    navigation(
        startDestination = startDestination,
        route = CardUnlockRoutes.subGraphName()
    ) {
        renderComposable(
            route = CardUnlockRoutes.CardUnlockIntroScreen.route,
            arguments = CardUnlockRoutes.CardUnlockIntroScreen.arguments
        ) { navEntry ->
            CardUnlockIntroScreen(
                navController = navController,
                navBackStackEntry = navEntry,
                graphController = controller
            )
        }
        renderComposable(
            route = CardUnlockRoutes.CardUnlockCanScreen.route,
            arguments = CardUnlockRoutes.CardUnlockCanScreen.arguments

        ) { navEntry ->
            CardUnlockCanScreen(
                navController = navController,
                navBackStackEntry = navEntry,
                graphController = controller
            )
        }
        renderComposable(
            route = CardUnlockRoutes.CardUnlockPukScreen.route,
            arguments = CardUnlockRoutes.CardUnlockPukScreen.arguments
        ) { navEntry ->
            CardUnlockPukScreen(
                navController = navController,
                navBackStackEntry = navEntry,
                graphController = controller
            )
        }
        renderComposable(
            route = CardUnlockRoutes.CardUnlockOldSecretScreen.route,
            arguments = CardUnlockRoutes.CardUnlockOldSecretScreen.arguments
        ) { navEntry ->
            CardUnlockOldSecretScreen(
                navController = navController,
                navBackStackEntry = navEntry,
                graphController = controller
            )
        }
        renderComposable(
            route = CardUnlockRoutes.CardUnlockNewSecretScreen.route,
            arguments = CardUnlockRoutes.CardUnlockNewSecretScreen.arguments
        ) { navEntry ->
            CardUnlockNewSecretScreen(
                navController = navController,
                navBackStackEntry = navEntry,
                graphController = controller
            )
        }
        renderComposable(
            route = CardUnlockRoutes.CardUnlockEgkScreen.route,
            arguments = CardUnlockRoutes.CardUnlockEgkScreen.arguments
        ) { navEntry ->
            CardUnlockEgkScreen(
                navController = navController,
                navBackStackEntry = navEntry,
                graphController = controller
            )
        }
    }
}
