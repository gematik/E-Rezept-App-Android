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

package de.gematik.ti.erp.app.prescription.detail.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.navigation
import de.gematik.ti.erp.app.navigation.renderComposable
import de.gematik.ti.erp.app.prescription.detail.ui.PrescriptionDetailAccidentInfoScreen
import de.gematik.ti.erp.app.prescription.detail.ui.PrescriptionDetailIngredientsScreen
import de.gematik.ti.erp.app.prescription.detail.ui.PrescriptionDetailMedicationOverviewScreen
import de.gematik.ti.erp.app.prescription.detail.ui.PrescriptionDetailMedicationScreen
import de.gematik.ti.erp.app.prescription.detail.ui.PrescriptionDetailOrganizationScreen
import de.gematik.ti.erp.app.prescription.detail.ui.PrescriptionDetailPatientScreen
import de.gematik.ti.erp.app.prescription.detail.ui.PrescriptionDetailPrescriberScreen
import de.gematik.ti.erp.app.prescription.detail.ui.PrescriptionDetailScreen
import de.gematik.ti.erp.app.prescription.detail.ui.PrescriptionDetailTechnicalInformationScreen

@Suppress("LongMethod")
fun NavGraphBuilder.prescriptionDetailGraph(
    startDestination: String = PrescriptionDetailRoutes.PrescriptionDetailScreen.route,
    navController: NavController
) {
    navigation(
        startDestination = startDestination,
        route = PrescriptionDetailRoutes.subGraphName()
    ) {
        renderComposable(
            route = PrescriptionDetailRoutes.PrescriptionDetailScreen.route,
            arguments = PrescriptionDetailRoutes.PrescriptionDetailScreen.arguments
        ) { navEntry ->
            PrescriptionDetailScreen(
                navController = navController,
                navBackStackEntry = navEntry
            )
        }
        renderComposable(
            route = PrescriptionDetailRoutes.PrescriptionDetailMedicationOverviewScreen.route,
            arguments = PrescriptionDetailRoutes.PrescriptionDetailMedicationOverviewScreen.arguments
        ) {
                navEntry ->
            PrescriptionDetailMedicationOverviewScreen(
                navController = navController,
                navBackStackEntry = navEntry
            )
        }
        renderComposable(
            route = PrescriptionDetailRoutes.PrescriptionDetailMedicationScreen.route,
            arguments = PrescriptionDetailRoutes.PrescriptionDetailMedicationScreen.arguments
        ) { navEntry ->
            PrescriptionDetailMedicationScreen(
                navController = navController,
                navBackStackEntry = navEntry
            )
        }
        renderComposable(
            route = PrescriptionDetailRoutes.PrescriptionDetailIngredientsScreen.route,
            arguments = PrescriptionDetailRoutes.PrescriptionDetailIngredientsScreen.arguments
        ) {
                navEntry ->
            PrescriptionDetailIngredientsScreen(
                navController = navController,
                navBackStackEntry = navEntry
            )
        }
        renderComposable(
            route = PrescriptionDetailRoutes.PrescriptionDetailPatientScreen.route,
            arguments = PrescriptionDetailRoutes.PrescriptionDetailPatientScreen.arguments
        ) {
                navEntry ->
            PrescriptionDetailPatientScreen(
                navController = navController,
                navBackStackEntry = navEntry
            )
        }
        renderComposable(
            route = PrescriptionDetailRoutes.PrescriptionDetailPrescriberScreen.route,
            arguments = PrescriptionDetailRoutes.PrescriptionDetailPrescriberScreen.arguments
        ) {
                navEntry ->
            PrescriptionDetailPrescriberScreen(
                navController = navController,
                navBackStackEntry = navEntry
            )
        }
        renderComposable(
            route = PrescriptionDetailRoutes.PrescriptionDetailAccidentInfoScreen.route,
            arguments = PrescriptionDetailRoutes.PrescriptionDetailAccidentInfoScreen.arguments
        ) {
                navEntry ->
            PrescriptionDetailAccidentInfoScreen(
                navController = navController,
                navBackStackEntry = navEntry
            )
        }
        renderComposable(
            route = PrescriptionDetailRoutes.PrescriptionDetailOrganizationScreen.route,
            arguments = PrescriptionDetailRoutes.PrescriptionDetailOrganizationScreen.arguments
        ) {
                navEntry ->
            PrescriptionDetailOrganizationScreen(
                navController = navController,
                navBackStackEntry = navEntry
            )
        }
        renderComposable(
            route = PrescriptionDetailRoutes.PrescriptionDetailTechnicalInformationScreen.route,
            arguments = PrescriptionDetailRoutes.PrescriptionDetailTechnicalInformationScreen.arguments
        ) {
                navEntry ->
            PrescriptionDetailTechnicalInformationScreen(
                navController = navController,
                navBackStackEntry = navEntry
            )
        }
    }
}
