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

package de.gematik.ti.erp.app.debugsettings.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.navigation
import de.gematik.ti.erp.app.debugsettings.ui.BottomSheetSampleScreen
import de.gematik.ti.erp.app.debugsettings.ui.BottomSheetSampleScreen.BottomSheetSampleHeight.AdaptableHeight
import de.gematik.ti.erp.app.debugsettings.ui.BottomSheetSampleScreen.BottomSheetSampleHeight.FullScreenHeight
import de.gematik.ti.erp.app.debugsettings.ui.BottomSheetSampleScreen.BottomSheetSampleHeight.SmallHeight
import de.gematik.ti.erp.app.debugsettings.ui.SampleOverviewScreen
import de.gematik.ti.erp.app.navigation.renderBottomSheet
import de.gematik.ti.erp.app.navigation.renderComposable
import de.gematik.ti.erp.app.navigation.slideInDown
import de.gematik.ti.erp.app.navigation.slideOutUp

/**
 * This graph shows the different UI components that are possible in the app.
 * The different dimensions and states of them.
 * Every new screen added here is an example of a new component and some information about it
 */
fun NavGraphBuilder.exampleScreensGraph(
    navController: NavController
) {
    navigation(
        startDestination = SampleScreenRoutes.SampleOverviewsScreen.route,
        route = SampleScreenRoutes.subGraphName()
    ) {
        renderComposable(
            route = SampleScreenRoutes.SampleOverviewsScreen.route,
            stackEnterAnimation = { slideInDown() },
            stackExitAnimation = { slideOutUp() }
        ) {
            SampleOverviewScreen(
                navController,
                it
            )
        }
        renderBottomSheet(
            route = SampleScreenRoutes.BottomSheetSampleScreen.route
        ) {
            BottomSheetSampleScreen(
                navController = navController,
                navBackStackEntry = it,
                height = AdaptableHeight
            )
        }
        renderBottomSheet(
            route = SampleScreenRoutes.BottomSheetSampleSmallScreen.route
        ) {
            BottomSheetSampleScreen(
                navController = navController,
                navBackStackEntry = it,
                height = SmallHeight
            )
        }
        renderBottomSheet(
            route = SampleScreenRoutes.BottomSheetSampleLargeScreen.route
        ) {
            BottomSheetSampleScreen(
                navController = navController,
                navBackStackEntry = it,
                height = FullScreenHeight
            )
        }
    }
}
