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

package de.gematik.ti.erp.app.prescription.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.navigation
import de.gematik.ti.erp.app.navigation.renderBottomSheet
import de.gematik.ti.erp.app.navigation.renderComposable
import de.gematik.ti.erp.app.navigation.slideInDown
import de.gematik.ti.erp.app.navigation.slideOutUp
import de.gematik.ti.erp.app.prescription.ui.PrescriptionScanScreen
import de.gematik.ti.erp.app.prescription.ui.screen.GrantConsentBottomSheetScreen
import de.gematik.ti.erp.app.prescription.ui.screen.PrescriptionsArchiveScreen
import de.gematik.ti.erp.app.prescription.ui.screen.PrescriptionsScreen
import de.gematik.ti.erp.app.prescription.ui.screen.WelcomeDrawerBottomSheetScreen

@Suppress("LongMethod")
fun NavGraphBuilder.prescriptionGraph(
    startDestination: String = PrescriptionRoutes.PrescriptionsScreen.route,
    navController: NavController
) {
    navigation(
        startDestination = startDestination,
        route = PrescriptionRoutes.subGraphName()
    ) {
        renderComposable(
            stackEnterAnimation = { slideInDown() },
            stackExitAnimation = { slideOutUp() },
            popExitAnimation = { slideOutUp() },
            route = PrescriptionRoutes.PrescriptionsScreen.route,
            arguments = PrescriptionRoutes.PrescriptionsScreen.arguments
        ) { navEntry ->
            PrescriptionsScreen(
                navController = navController,
                navBackStackEntry = navEntry
            )
        }
        renderComposable(
            route = PrescriptionRoutes.PrescriptionsArchiveScreen.route,
            arguments = PrescriptionRoutes.PrescriptionsArchiveScreen.arguments
        ) { navEntry ->
            PrescriptionsArchiveScreen(navController, navEntry)
        }
        renderComposable(
            route = PrescriptionRoutes.PrescriptionScanScreen.route,
            arguments = PrescriptionRoutes.PrescriptionScanScreen.arguments
        ) { navEntry ->
            PrescriptionScanScreen(
                navController = navController,
                navBackStackEntry = navEntry
            )
        }
        renderBottomSheet(
            route = PrescriptionRoutes.WelcomeDrawerBottomSheetScreen.route,
            arguments = PrescriptionRoutes.WelcomeDrawerBottomSheetScreen.arguments
        ) { navEntry ->
            WelcomeDrawerBottomSheetScreen(
                navController = navController,
                navBackStackEntry = navEntry
            )
        }
        renderBottomSheet(
            route = PrescriptionRoutes.GrantConsentBottomSheetScreen.route,
            arguments = PrescriptionRoutes.GrantConsentBottomSheetScreen.arguments
        ) { navEntry ->
            GrantConsentBottomSheetScreen(navController, navEntry)
        }
    }
}
