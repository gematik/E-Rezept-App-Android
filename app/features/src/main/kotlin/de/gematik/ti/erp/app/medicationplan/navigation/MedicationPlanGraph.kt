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

package de.gematik.ti.erp.app.medicationplan.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.navigation
import de.gematik.ti.erp.app.medicationplan.ui.MedicationPlanDosageInfoBottomSheetScreen
import de.gematik.ti.erp.app.medicationplan.ui.MedicationPlanScheduleScreen
import de.gematik.ti.erp.app.medicationplan.ui.ScheduleDateRangeScreen
import de.gematik.ti.erp.app.medicationplan.ui.MedicationListScheduleScreen
import de.gematik.ti.erp.app.medicationplan.ui.components.MedicationNotificationSuccessScreen
import de.gematik.ti.erp.app.navigation.renderBottomSheet
import de.gematik.ti.erp.app.navigation.renderComposable
import de.gematik.ti.erp.app.navigation.slideInDown
import de.gematik.ti.erp.app.navigation.slideInRight
import de.gematik.ti.erp.app.navigation.slideOutLeft
import de.gematik.ti.erp.app.navigation.slideOutUp

fun NavGraphBuilder.medicationPlanGraph(
    startDestination: String = MedicationPlanRoutes.MedicationPlanList.route,
    navController: NavController
) {
    navigation(
        startDestination = startDestination,
        route = MedicationPlanRoutes.subGraphName()
    ) {
        renderComposable(
            route = MedicationPlanRoutes.MedicationPlanList.route,
            arguments = MedicationPlanRoutes.MedicationPlanList.arguments,
            stackEnterAnimation = { slideInDown() },
            stackExitAnimation = { slideOutUp() }
        ) {
            MedicationListScheduleScreen(
                navController = navController,
                navBackStackEntry = it
            )
        }
        renderComposable(
            route = MedicationPlanRoutes.MedicationPlanPerPrescription.route,
            arguments = MedicationPlanRoutes.MedicationPlanPerPrescription.arguments,
            stackEnterAnimation = { slideInRight() },
            stackExitAnimation = { slideOutLeft() }
        ) {
            MedicationPlanScheduleScreen(
                navController = navController,
                navBackStackEntry = it
            )
        }

        renderComposable(
            route = MedicationPlanRoutes.ScheduleDateRange.route,
            arguments = MedicationPlanRoutes.ScheduleDateRange.arguments,
            stackEnterAnimation = { slideInRight() },
            stackExitAnimation = { slideOutLeft() }
        ) {
            ScheduleDateRangeScreen(
                navController = navController,
                navBackStackEntry = it
            )
        }
        renderComposable(
            route = MedicationPlanRoutes.MedicationPlanNotificationSuccess.route,
            arguments = MedicationPlanRoutes.MedicationPlanNotificationSuccess.arguments
        ) {
            MedicationNotificationSuccessScreen(
                navController = navController,
                navBackStackEntry = it
            )
        }
        renderBottomSheet(
            route = MedicationPlanRoutes.MedicationPlanDosageInfo.route,
            arguments = MedicationPlanRoutes.MedicationPlanDosageInfo.arguments
        ) {
            MedicationPlanDosageInfoBottomSheetScreen(
                navController = navController,
                navBackStackEntry = it
            )
        }
    }
}
