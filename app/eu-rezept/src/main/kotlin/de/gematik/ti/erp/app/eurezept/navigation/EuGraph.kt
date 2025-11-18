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

package de.gematik.ti.erp.app.eurezept.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.navigation
import de.gematik.ti.erp.app.eurezept.presentation.euSharedViewModel
import de.gematik.ti.erp.app.eurezept.ui.screens.EuAvailabilityScreen
import de.gematik.ti.erp.app.eurezept.ui.screens.EuConsentScreen
import de.gematik.ti.erp.app.eurezept.ui.screens.EuCountrySelectionScreen
import de.gematik.ti.erp.app.eurezept.ui.screens.EuInstructionsScreen
import de.gematik.ti.erp.app.eurezept.ui.screens.EuPrescriptionSelectionScreen
import de.gematik.ti.erp.app.eurezept.ui.screens.EuRedeemOverviewScreen
import de.gematik.ti.erp.app.eurezept.ui.screens.EuRedemptionCodeScreen
import de.gematik.ti.erp.app.navigation.renderComposable

fun NavGraphBuilder.euGraph(
    startDestination: String = EuRoutes.EuConsentScreen.route,
    navController: NavController
) {
    navigation(
        startDestination = startDestination,
        route = EuRoutes.subGraphName()
    ) {
        renderComposable(
            route = EuRoutes.EuConsentScreen.route,
            arguments = EuRoutes.EuConsentScreen.arguments
        ) { navEntry ->
            EuConsentScreen(
                navController = navController,
                navBackStackEntry = navEntry
            )
        }

        renderComposable(
            route = EuRoutes.EuRedeemScreen.route,
            arguments = EuRoutes.EuRedeemScreen.arguments
        ) { navEntry ->
            EuRedeemOverviewScreen(
                navController = navController,
                navBackStackEntry = navEntry,
                graphController = euSharedViewModel(navController, navEntry)
            )
        }

        renderComposable(
            route = EuRoutes.EuCountrySelectionScreen.route,
            arguments = EuRoutes.EuCountrySelectionScreen.arguments
        ) { navEntry ->
            EuCountrySelectionScreen(
                navController = navController,
                navBackStackEntry = navEntry,
                graphController = euSharedViewModel(navController, navEntry)
            )
        }

        renderComposable(
            route = EuRoutes.EuPrescriptionSelectionScreen.route,
            arguments = EuRoutes.EuPrescriptionSelectionScreen.arguments
        ) { navEntry ->
            EuPrescriptionSelectionScreen(
                navController = navController,
                navBackStackEntry = navEntry,
                graphController = euSharedViewModel(navController, navEntry)
            )
        }

        renderComposable(
            route = EuRoutes.EuAvailabilityScreen.route,
            arguments = EuRoutes.EuAvailabilityScreen.arguments
        ) { navEntry ->
            EuAvailabilityScreen(
                navController = navController,
                navBackStackEntry = navEntry
            )
        }
        renderComposable(
            route = EuRoutes.EuInstructionsScreen.route,
            arguments = EuRoutes.EuInstructionsScreen.arguments
        ) { navEntry ->
            EuInstructionsScreen(
                navController = navController,
                navBackStackEntry = navEntry,
                graphController = euSharedViewModel(navController, navEntry)
            )
        }
        renderComposable(
            route = EuRoutes.EuRedemptionCodeScreen.route,
            arguments = EuRoutes.EuRedemptionCodeScreen.arguments
        ) { navEntry ->
            EuRedemptionCodeScreen(
                navController = navController,
                navBackStackEntry = navEntry,
                graphController = euSharedViewModel(navController, navEntry)
            )
        }
    }
}
