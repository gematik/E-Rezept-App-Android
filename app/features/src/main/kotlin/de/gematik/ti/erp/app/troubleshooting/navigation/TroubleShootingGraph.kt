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

package de.gematik.ti.erp.app.troubleshooting.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.navigation
import de.gematik.ti.erp.app.navigation.renderComposable
import de.gematik.ti.erp.app.troubleshooting.ui.screens.TroubleShootingDeviceOnTopScreen
import de.gematik.ti.erp.app.troubleshooting.ui.screens.TroubleShootingFindNfcPositionScreen
import de.gematik.ti.erp.app.troubleshooting.ui.screens.TroubleShootingIntroScreen
import de.gematik.ti.erp.app.troubleshooting.ui.screens.TroubleShootingNoSuccessScreen

fun NavGraphBuilder.troubleShootingGraph(
    startDestination: String = TroubleShootingRoutes.TroubleShootingIntroScreen.route,
    navController: NavController
) {
    navigation(
        startDestination = startDestination,
        route = TroubleShootingRoutes.subGraphName()
    ) {
        renderComposable(
            route = TroubleShootingRoutes.TroubleShootingIntroScreen.route,
            arguments = TroubleShootingRoutes.TroubleShootingIntroScreen.arguments
        ) { navEntry ->
            TroubleShootingIntroScreen(
                navController = navController,
                navBackStackEntry = navEntry
            )
        }
        renderComposable(
            route = TroubleShootingRoutes.TroubleShootingDeviceOnTopScreen.route,
            arguments = TroubleShootingRoutes.TroubleShootingDeviceOnTopScreen.arguments
        ) { navEntry ->
            TroubleShootingDeviceOnTopScreen(
                navController = navController,
                navBackStackEntry = navEntry
            )
        }
        renderComposable(
            route = TroubleShootingRoutes.TroubleShootingFindNfcPositionScreen.route,
            arguments = TroubleShootingRoutes.TroubleShootingFindNfcPositionScreen.arguments
        ) { navEntry ->
            TroubleShootingFindNfcPositionScreen(
                navController = navController,
                navBackStackEntry = navEntry
            )
        }
        renderComposable(
            route = TroubleShootingRoutes.TroubleShootingNoSuccessScreen.route,
            arguments = TroubleShootingRoutes.TroubleShootingNoSuccessScreen.arguments
        ) { navEntry ->
            TroubleShootingNoSuccessScreen(
                navController = navController,
                navBackStackEntry = navEntry
            )
        }
    }
}
