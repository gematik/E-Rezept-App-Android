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

package de.gematik.ti.erp.app.redeem.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.navigation
import de.gematik.ti.erp.app.navigation.renderComposable
import de.gematik.ti.erp.app.redeem.ui.screens.PrescriptionSelectionScreen
import de.gematik.ti.erp.app.redeem.presentation.OnlineRedeemGraphController
import de.gematik.ti.erp.app.redeem.ui.screens.HowToRedeemScreen
import de.gematik.ti.erp.app.redeem.ui.screens.LocalRedeemScreen
import de.gematik.ti.erp.app.redeem.ui.screens.OnlineRedeemPreferencesScreen
import de.gematik.ti.erp.app.redeem.ui.screens.RedeemEditShippingContactScreen
import de.gematik.ti.erp.app.redeem.ui.screens.RedeemOrderOverviewScreen
import org.kodein.di.DI
import org.kodein.di.instance

@Suppress("LongMethod")
fun NavGraphBuilder.redeemGraph(
    dependencyInjector: DI,
    startDestination: String = RedeemRoutes.RedeemMethodSelection.route,
    navController: NavController
) {
    val onlineRedeemController by dependencyInjector.instance<OnlineRedeemGraphController>()

    navigation(
        startDestination = startDestination,
        route = RedeemRoutes.subGraphName()
    ) {
        renderComposable(
            route = RedeemRoutes.RedeemMethodSelection.route,
            arguments = RedeemRoutes.RedeemMethodSelection.arguments
        ) { navEntry ->
            HowToRedeemScreen(
                navController = navController,
                navBackStackEntry = navEntry,
                controller = onlineRedeemController
            )
        }

        renderComposable(
            route = RedeemRoutes.RedeemLocal.route,
            arguments = RedeemRoutes.RedeemLocal.arguments
        ) { navEntry ->
            LocalRedeemScreen(
                navController = navController,
                navBackStackEntry = navEntry
            )
        }

        renderComposable(
            route = RedeemRoutes.RedeemOnlinePreferences.route,
            arguments = RedeemRoutes.RedeemOnlinePreferences.arguments
        ) { navEntry ->
            OnlineRedeemPreferencesScreen(
                navController = navController,
                navBackStackEntry = navEntry,
                controller = onlineRedeemController
            )
        }

        renderComposable(
            route = RedeemRoutes.RedeemPrescriptionSelection.route,
            arguments = RedeemRoutes.RedeemPrescriptionSelection.arguments
        ) { navEntry ->
            PrescriptionSelectionScreen(
                navController = navController,
                navBackStackEntry = navEntry,
                controller = onlineRedeemController
            )
        }
        renderComposable(
            route = RedeemRoutes.RedeemOrderOverviewScreen.route,
            arguments = RedeemRoutes.RedeemOrderOverviewScreen.arguments
        ) {
            RedeemOrderOverviewScreen(
                navController = navController,
                navBackStackEntry = it,
                graphController = onlineRedeemController
            )
        }
        renderComposable(
            route = RedeemRoutes.RedeemEditShippingContactScreen.route,
            arguments = RedeemRoutes.RedeemEditShippingContactScreen.arguments
        ) {
            RedeemEditShippingContactScreen(
                navController = navController,
                navBackStackEntry = it,
                graphController = onlineRedeemController
            )
        }
    }
}
