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

package de.gematik.ti.erp.app.digas.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.navigation
import de.gematik.ti.erp.app.digas.presentation.digasSharedViewModel
import de.gematik.ti.erp.app.digas.ui.model.ErrorScreenDataWithoutRetry
import de.gematik.ti.erp.app.digas.ui.screen.DigaContributionInfoBottomSheetScreen
import de.gematik.ti.erp.app.digas.ui.screen.DigaDescriptionScreen
import de.gematik.ti.erp.app.digas.ui.screen.DigaFeedbackPromptScreen
import de.gematik.ti.erp.app.digas.ui.screen.DigaHelpAndSupportBottomSheetScreen
import de.gematik.ti.erp.app.digas.ui.screen.DigasMainScreen
import de.gematik.ti.erp.app.digas.ui.screen.HowLongDigaValidBottomSheetScreen
import de.gematik.ti.erp.app.digas.ui.screen.DigaInsuranceSearchListScreen
import de.gematik.ti.erp.app.navigation.renderBottomSheet
import de.gematik.ti.erp.app.navigation.renderComposable
import de.gematik.ti.erp.app.navigation.slideInDown
import de.gematik.ti.erp.app.navigation.slideInRight
import de.gematik.ti.erp.app.navigation.slideOutLeft
import de.gematik.ti.erp.app.navigation.slideOutUp

fun NavGraphBuilder.digasGraph(
    startDestination: String = DigasRoutes.DigasMainScreen.route,
    navController: NavController
) {
    navigation(
        startDestination = startDestination,
        route = DigasRoutes.subGraphName()
    ) {
        renderComposable(
            stackEnterAnimation = { slideInDown() },
            stackExitAnimation = { slideOutUp() },
            popExitAnimation = { slideOutUp() },
            route = DigasRoutes.DigasMainScreen.route,
            arguments = DigasRoutes.DigasMainScreen.arguments
        ) { navEntry ->
            DigasMainScreen(
                navController = navController,
                navBackStackEntry = navEntry,
                sharedViewModel = digasSharedViewModel(navController, navEntry),
                errorScreenData = ErrorScreenDataWithoutRetry()
            )
        }

        renderComposable(
            stackEnterAnimation = { slideInRight() },
            stackExitAnimation = { slideOutLeft() },
            popExitAnimation = { slideOutLeft() },
            route = DigasRoutes.DigasDescriptionScreen.route,
            arguments = DigasRoutes.DigasDescriptionScreen.arguments
        ) { navEntry ->
            DigaDescriptionScreen(
                navController = navController,
                navBackStackEntry = navEntry
            )
        }

        renderBottomSheet(
            route = DigasRoutes.DigasValidityBottomSheetScreen.route,
            arguments = DigasRoutes.DigasValidityBottomSheetScreen.arguments
        ) { navEntry ->
            HowLongDigaValidBottomSheetScreen(
                navController = navController,
                navBackStackEntry = navEntry
            )
        }
        renderBottomSheet(
            route = DigasRoutes.DigaSupportBottomSheetScreen.route,
            arguments = DigasRoutes.DigaSupportBottomSheetScreen.arguments
        ) { navEntry ->
            DigaHelpAndSupportBottomSheetScreen(
                navController = navController,
                navBackStackEntry = navEntry
            )
        }
        renderBottomSheet(
            route = DigasRoutes.DigaContributionInfoBottomSheetScreen.route,
            arguments = DigasRoutes.DigaContributionInfoBottomSheetScreen.arguments
        ) { navEntry ->
            DigaContributionInfoBottomSheetScreen(
                navController = navController,
                navBackStackEntry = navEntry
            )
        }
        renderBottomSheet(
            route = DigasRoutes.DigaFeedbackPromptScreen.route,
            arguments = DigasRoutes.DigaFeedbackPromptScreen.arguments
        ) { navEntry ->
            DigaFeedbackPromptScreen(
                navController = navController,
                navBackStackEntry = navEntry
            )
        }

        renderComposable(
            stackEnterAnimation = { slideInRight() },
            stackExitAnimation = { slideOutLeft() },
            popExitAnimation = { slideOutLeft() },
            route = DigasRoutes.InsuranceSearchListScreen.route,
            arguments = DigasRoutes.InsuranceSearchListScreen.arguments
        ) { navEntry ->
            DigaInsuranceSearchListScreen(
                navController = navController,
                navBackStackEntry = navEntry,
                sharedViewModel = digasSharedViewModel(navController, navEntry),
                errorScreenData = ErrorScreenDataWithoutRetry()
            )
        }
    }
}
