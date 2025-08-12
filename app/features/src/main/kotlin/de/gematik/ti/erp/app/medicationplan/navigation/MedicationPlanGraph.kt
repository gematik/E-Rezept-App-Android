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

package de.gematik.ti.erp.app.medicationplan.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.navigation
import de.gematik.ti.erp.app.medicationplan.ui.MedicationPlanDosageInstructionBottomSheetScreen
import de.gematik.ti.erp.app.medicationplan.ui.MedicationPlanScheduleDetailScreen
import de.gematik.ti.erp.app.medicationplan.ui.MedicationPlanScheduleDurationAndIntervalScreen
import de.gematik.ti.erp.app.medicationplan.ui.MedicationPlanScheduleListScreen
import de.gematik.ti.erp.app.medicationplan.ui.components.MedicationPlanNotificationScreen
import de.gematik.ti.erp.app.navigation.renderBottomSheet
import de.gematik.ti.erp.app.navigation.renderComposable
import de.gematik.ti.erp.app.navigation.slideInDown
import de.gematik.ti.erp.app.navigation.slideInRight
import de.gematik.ti.erp.app.navigation.slideOutLeft
import de.gematik.ti.erp.app.navigation.slideOutUp

fun NavGraphBuilder.medicationPlanGraph(
    startDestination: String = MedicationPlanRoutes.MedicationPlanScheduleListScreen.route,
    navController: NavController
) {
    navigation(
        startDestination = startDestination,
        route = MedicationPlanRoutes.subGraphName()
    ) {
        renderComposable(
            route = MedicationPlanRoutes.MedicationPlanScheduleListScreen.route,
            arguments = MedicationPlanRoutes.MedicationPlanScheduleListScreen.arguments,
            stackEnterAnimation = { slideInDown() },
            stackExitAnimation = { slideOutUp() }
        ) {
            MedicationPlanScheduleListScreen(
                navController = navController,
                navBackStackEntry = it
            )
        }
        renderComposable(
            route = MedicationPlanRoutes.MedicationPlanScheduleDetailScreen.route,
            arguments = MedicationPlanRoutes.MedicationPlanScheduleDetailScreen.arguments,
            stackEnterAnimation = { slideInRight() },
            stackExitAnimation = { slideOutLeft() }
        ) {
            MedicationPlanScheduleDetailScreen(
                navController = navController,
                navBackStackEntry = it
            )
        }

        renderComposable(
            route = MedicationPlanRoutes.MedicationPlanScheduleDurationAndIntervalScreen.route,
            arguments = MedicationPlanRoutes.MedicationPlanScheduleDurationAndIntervalScreen.arguments,
            stackEnterAnimation = { slideInRight() },
            stackExitAnimation = { slideOutLeft() }
        ) {
            MedicationPlanScheduleDurationAndIntervalScreen(
                navController = navController,
                navBackStackEntry = it
            )
        }
        renderComposable(
            route = MedicationPlanRoutes.MedicationPlanNotificationScreen.route,
            arguments = MedicationPlanRoutes.MedicationPlanNotificationScreen.arguments
        ) {
            MedicationPlanNotificationScreen(
                navController = navController,
                navBackStackEntry = it
            )
        }
        renderBottomSheet(
            route = MedicationPlanRoutes.MedicationPlanDosageInstructionBottomSheetScreen.route,
            arguments = MedicationPlanRoutes.MedicationPlanDosageInstructionBottomSheetScreen.arguments
        ) {
            MedicationPlanDosageInstructionBottomSheetScreen(
                navController = navController,
                navBackStackEntry = it
            )
        }
    }
}
