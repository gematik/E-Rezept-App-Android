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

package de.gematik.ti.erp.app.redeem.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.navigation
import de.gematik.ti.erp.app.navigation.renderComposable
import de.gematik.ti.erp.app.redeem.presentation.OnlineRedeemGraphController
import de.gematik.ti.erp.app.redeem.ui.screens.HowToRedeemScreen
import de.gematik.ti.erp.app.redeem.ui.screens.LocalRedeemScreen
import de.gematik.ti.erp.app.redeem.ui.screens.OnlineRedeemPreferencesScreen
import de.gematik.ti.erp.app.redeem.ui.screens.PrescriptionSelectionScreen
import de.gematik.ti.erp.app.redeem.ui.screens.RedeemEditShippingContactScreen
import de.gematik.ti.erp.app.redeem.ui.screens.RedeemOrderOverviewScreen
import org.kodein.di.DI
import org.kodein.di.instance

/**
 * Defines the navigation graph for the redemption process.
 *
 * This function sets up a subgraph for all screens related to the redemption process.
 * It utilizes `renderComposable` to define each screen and its route.
 *
 * @param dependencyInjector The dependency injection container providing required instances.
 * @param startDestination The initial screen of the redemption process. Defaults to [RedeemRoutes.RedeemMethodSelection].
 * @param navController The navigation controller responsible for managing navigation between screens.
 */
@Suppress("LongMethod")
fun NavGraphBuilder.redeemGraph(
    dependencyInjector: DI,
    startDestination: String = RedeemRoutes.RedeemMethodSelection.route,
    navController: NavController
) {
    /**
     * Retrieves the controller for handling online redemption processes.
     * This controller is shared across multiple screens in this subgraph.
     */
    val onlineRedeemController by dependencyInjector.instance<OnlineRedeemGraphController>()

    navigation(
        startDestination = startDestination,
        route = RedeemRoutes.subGraphName()
    ) {
        /**
         * Screen: [HowToRedeemScreen]
         *
         * This screen allows the user to select a redemption method.
         * It acts as the entry point to decide between redeeming from the app or using the data-matrix code.
         *
         * [RedeemRoutes.RedeemMethodSelection] provides the route and arguments for this screen.
         */
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

        /**
         * Screen: [LocalRedeemScreen]
         *
         * This screen handles the redemption process for local pharmacies.
         * It allows users to redeem prescriptions in person through a data-matrix code.
         *
         * [RedeemRoutes.RedeemLocal] provides the route and arguments for this screen.
         */
        renderComposable(
            route = RedeemRoutes.RedeemLocal.route,
            arguments = RedeemRoutes.RedeemLocal.arguments
        ) { navEntry ->
            LocalRedeemScreen(
                navController = navController,
                navBackStackEntry = navEntry
            )
        }

        /**
         * Screen: [OnlineRedeemPreferencesScreen]
         *
         * This screen lets users set preferences for online redemption.
         * The user can select all the prescriptions that are possible or select a subset by deciding on this screen.
         *
         * [RedeemRoutes.RedeemOnlinePreferences] provides the route and arguments for this screen.
         */
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

        /**
         * Screen: [PrescriptionSelectionScreen]
         *
         * This screen allows users to select prescriptions to redeem.
         * It retrieves available prescriptions and displays them for selection.
         *
         * [RedeemRoutes.RedeemPrescriptionSelection] provides the route and arguments for this screen.
         */
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

        /**
         * Screen: [RedeemOrderOverviewScreen]
         *
         * Displays an overview of the user's redemption order before finalizing the process.
         * Users can review selected prescriptions, pharmacy, address.
         * If the user is not logged in, they will be taken to the login process from here.
         *
         * [RedeemRoutes.RedeemOrderOverviewScreen] provides the route and arguments for this screen.
         */
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

        /**
         * Screen: [RedeemEditShippingContactScreen]
         *
         * Allows the user to modify the shipping address or contact information
         * before finalizing the redemption order.
         *
         * [RedeemRoutes.RedeemEditShippingContactScreen] provides the route and arguments for this screen.
         */
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
