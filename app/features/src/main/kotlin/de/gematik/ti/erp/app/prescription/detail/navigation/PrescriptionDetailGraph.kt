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

package de.gematik.ti.erp.app.prescription.detail.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.navigation
import de.gematik.ti.erp.app.navigation.renderBottomSheet
import de.gematik.ti.erp.app.navigation.renderComposable
import de.gematik.ti.erp.app.navigation.slideInDown
import de.gematik.ti.erp.app.navigation.slideInRight
import de.gematik.ti.erp.app.navigation.slideOutLeft
import de.gematik.ti.erp.app.navigation.slideOutUp
import de.gematik.ti.erp.app.prescription.detail.ui.HowLongValidBottomSheetScreen
import de.gematik.ti.erp.app.prescription.detail.ui.PrescriptionDetailAccidentInfoScreen
import de.gematik.ti.erp.app.prescription.detail.ui.PrescriptionDetailBottomSheetScreen
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
            stackEnterAnimation = { slideInDown() },
            stackExitAnimation = { slideOutUp() },
            popExitAnimation = { slideOutUp() },
            route = PrescriptionDetailRoutes.PrescriptionDetailScreen.route,
            arguments = PrescriptionDetailRoutes.PrescriptionDetailScreen.arguments
        ) { navEntry ->
            PrescriptionDetailScreen(
                navController = navController,
                navBackStackEntry = navEntry
            )
        }
        renderComposable(
            stackEnterAnimation = { slideInRight() },
            stackExitAnimation = { slideOutLeft() },
            popExitAnimation = { slideOutLeft() },
            route = PrescriptionDetailRoutes.PrescriptionDetailMedicationOverviewScreen.route,
            arguments = PrescriptionDetailRoutes.PrescriptionDetailMedicationOverviewScreen.arguments
        ) { navEntry ->
            PrescriptionDetailMedicationOverviewScreen(
                navController = navController,
                navBackStackEntry = navEntry
            )
        }
        renderComposable(
            stackEnterAnimation = { slideInRight() },
            stackExitAnimation = { slideOutLeft() },
            popExitAnimation = { slideOutLeft() },
            route = PrescriptionDetailRoutes.PrescriptionDetailMedicationScreen.route,
            arguments = PrescriptionDetailRoutes.PrescriptionDetailMedicationScreen.arguments
        ) { navEntry ->
            PrescriptionDetailMedicationScreen(
                navController = navController,
                navBackStackEntry = navEntry
            )
        }
        renderComposable(
            stackEnterAnimation = { slideInRight() },
            stackExitAnimation = { slideOutLeft() },
            popExitAnimation = { slideOutLeft() },
            route = PrescriptionDetailRoutes.PrescriptionDetailIngredientsScreen.route,
            arguments = PrescriptionDetailRoutes.PrescriptionDetailIngredientsScreen.arguments
        ) { navEntry ->
            PrescriptionDetailIngredientsScreen(
                navController = navController,
                navBackStackEntry = navEntry
            )
        }
        renderComposable(
            stackEnterAnimation = { slideInRight() },
            stackExitAnimation = { slideOutLeft() },
            popExitAnimation = { slideOutLeft() },
            route = PrescriptionDetailRoutes.PrescriptionDetailPatientScreen.route,
            arguments = PrescriptionDetailRoutes.PrescriptionDetailPatientScreen.arguments
        ) { navEntry ->
            PrescriptionDetailPatientScreen(
                navController = navController,
                navBackStackEntry = navEntry
            )
        }
        renderComposable(
            stackEnterAnimation = { slideInRight() },
            stackExitAnimation = { slideOutLeft() },
            popExitAnimation = { slideOutLeft() },
            route = PrescriptionDetailRoutes.PrescriptionDetailPrescriberScreen.route,
            arguments = PrescriptionDetailRoutes.PrescriptionDetailPrescriberScreen.arguments
        ) { navEntry ->
            PrescriptionDetailPrescriberScreen(
                navController = navController,
                navBackStackEntry = navEntry
            )
        }
        renderComposable(
            stackEnterAnimation = { slideInRight() },
            stackExitAnimation = { slideOutLeft() },
            popExitAnimation = { slideOutLeft() },
            route = PrescriptionDetailRoutes.PrescriptionDetailAccidentInfoScreen.route,
            arguments = PrescriptionDetailRoutes.PrescriptionDetailAccidentInfoScreen.arguments
        ) { navEntry ->
            PrescriptionDetailAccidentInfoScreen(
                navController = navController,
                navBackStackEntry = navEntry
            )
        }
        renderComposable(
            stackEnterAnimation = { slideInRight() },
            stackExitAnimation = { slideOutLeft() },
            popExitAnimation = { slideOutLeft() },
            route = PrescriptionDetailRoutes.PrescriptionDetailOrganizationScreen.route,
            arguments = PrescriptionDetailRoutes.PrescriptionDetailOrganizationScreen.arguments
        ) { navEntry ->
            PrescriptionDetailOrganizationScreen(
                navController = navController,
                navBackStackEntry = navEntry
            )
        }
        renderComposable(
            stackEnterAnimation = { slideInRight() },
            stackExitAnimation = { slideOutLeft() },
            popExitAnimation = { slideOutLeft() },
            route = PrescriptionDetailRoutes.PrescriptionDetailTechnicalInformationScreen.route,
            arguments = PrescriptionDetailRoutes.PrescriptionDetailTechnicalInformationScreen.arguments
        ) { navEntry ->
            PrescriptionDetailTechnicalInformationScreen(
                navController = navController,
                navBackStackEntry = navEntry
            )
        }
        renderBottomSheet(
            route = PrescriptionDetailRoutes.SelPayerPrescriptionBottomSheetScreen.route,
            arguments = PrescriptionDetailRoutes.SelPayerPrescriptionBottomSheetScreen.arguments
        ) { navEntry ->
            PrescriptionDetailBottomSheetScreen(
                navController = navController,
                navBackStackEntry = navEntry
            )
        }
        renderBottomSheet(
            route = PrescriptionDetailRoutes.AdditionalFeeNotExemptBottomSheetScreen.route,
            arguments = PrescriptionDetailRoutes.AdditionalFeeNotExemptBottomSheetScreen.arguments
        ) { navEntry ->
            PrescriptionDetailBottomSheetScreen(
                navController = navController,
                navBackStackEntry = navEntry
            )
        }
        renderBottomSheet(
            route = PrescriptionDetailRoutes.AdditionalFeeExemptBottomSheetScreen.route,
            arguments = PrescriptionDetailRoutes.AdditionalFeeExemptBottomSheetScreen.arguments
        ) { navEntry ->
            PrescriptionDetailBottomSheetScreen(
                navController = navController,
                navBackStackEntry = navEntry
            )
        }
        renderBottomSheet(
            route = PrescriptionDetailRoutes.FailureBottomSheetScreen.route,
            arguments = PrescriptionDetailRoutes.FailureBottomSheetScreen.arguments
        ) { navEntry ->
            PrescriptionDetailBottomSheetScreen(
                navController = navController,
                navBackStackEntry = navEntry
            )
        }
        renderBottomSheet(
            route = PrescriptionDetailRoutes.ScannedBottomSheetScreen.route,
            arguments = PrescriptionDetailRoutes.ScannedBottomSheetScreen.arguments
        ) { navEntry ->
            PrescriptionDetailBottomSheetScreen(
                navController = navController,
                navBackStackEntry = navEntry
            )
        }
        renderBottomSheet(
            route = PrescriptionDetailRoutes.DirectAssignmentBottomSheetScreen.route,
            arguments = PrescriptionDetailRoutes.DirectAssignmentBottomSheetScreen.arguments
        ) { navEntry ->
            PrescriptionDetailBottomSheetScreen(
                navController = navController,
                navBackStackEntry = navEntry
            )
        }
        renderBottomSheet(
            route = PrescriptionDetailRoutes.SubstitutionAllowedBottomSheetScreen.route,
            arguments = PrescriptionDetailRoutes.SubstitutionAllowedBottomSheetScreen.arguments
        ) { navEntry ->
            PrescriptionDetailBottomSheetScreen(
                navController = navController,
                navBackStackEntry = navEntry
            )
        }
        renderBottomSheet(
            route = PrescriptionDetailRoutes.SubstitutionNotAllowedBottomSheetScreen.route,
            arguments = PrescriptionDetailRoutes.SubstitutionNotAllowedBottomSheetScreen.arguments
        ) { navEntry ->
            PrescriptionDetailBottomSheetScreen(
                navController = navController,
                navBackStackEntry = navEntry
            )
        }
        renderBottomSheet(
            route = PrescriptionDetailRoutes.EmergencyFeeExemptBottomSheetScreen.route,
            arguments = PrescriptionDetailRoutes.EmergencyFeeExemptBottomSheetScreen.arguments
        ) { navEntry ->
            PrescriptionDetailBottomSheetScreen(
                navController = navController,
                navBackStackEntry = navEntry
            )
        }
        renderBottomSheet(
            route = PrescriptionDetailRoutes.EmergencyFeeNotExemptBottomSheetScreen.route,
            arguments = PrescriptionDetailRoutes.EmergencyFeeNotExemptBottomSheetScreen.arguments
        ) { navEntry ->
            PrescriptionDetailBottomSheetScreen(
                navController = navController,
                navBackStackEntry = navEntry
            )
        }
        renderBottomSheet(
            route = PrescriptionDetailRoutes.HowLongValidBottomSheetScreen.route,
            arguments = PrescriptionDetailRoutes.HowLongValidBottomSheetScreen.arguments
        ) { navEntry ->
            HowLongValidBottomSheetScreen(
                navController = navController,
                navBackStackEntry = navEntry
            )
        }
    }
}
