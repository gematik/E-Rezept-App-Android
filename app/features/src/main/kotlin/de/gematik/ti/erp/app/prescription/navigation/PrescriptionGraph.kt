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

package de.gematik.ti.erp.app.prescription.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.navigation
import de.gematik.ti.erp.app.navigation.renderComposable
import de.gematik.ti.erp.app.prescription.ui.PrescriptionScanScreen

@Suppress("LongMethod")
fun NavGraphBuilder.prescriptionGraph(
    startDestination: String = PrescriptionRoutes.PrescriptionScanScreen.route, // TODO Change to PrescriptionScreen
    navController: NavController
) {
    navigation(
        startDestination = startDestination,
        route = PrescriptionRoutes.subGraphName()
    ) {
        renderComposable(
            route = PrescriptionRoutes.PrescriptionScanScreen.route,
            arguments = PrescriptionRoutes.PrescriptionScanScreen.arguments
        ) { navEntry ->
            PrescriptionScanScreen(
                navController = navController,
                navBackStackEntry = navEntry
            )
        }
    }
}
