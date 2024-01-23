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

package de.gematik.ti.erp.app.mlkit.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.navigation
import de.gematik.ti.erp.app.mlkit.ui.MlKitInformationScreen
import de.gematik.ti.erp.app.mlkit.ui.MlKitScreen
import de.gematik.ti.erp.app.navigation.renderComposable

fun NavGraphBuilder.mlKitGraph(
    startDestination: String = MlKitRoutes.MlKitScreen.route,
    navController: NavController
) {
    navigation(
        startDestination = startDestination,
        route = MlKitRoutes.subGraphName()
    ) {
        renderComposable(
            route = MlKitRoutes.MlKitScreen.route,
            arguments = MlKitRoutes.MlKitScreen.arguments
        ) { navEntry ->
            MlKitScreen(
                navController = navController,
                navBackStackEntry = navEntry
            )
        }
        renderComposable(
            route = MlKitRoutes.MlKitInformationScreen.route,
            arguments = MlKitRoutes.MlKitInformationScreen.arguments
        ) { navEntry ->
            MlKitInformationScreen(
                navController = navController,
                navBackStackEntry = navEntry
            )
        }
    }
}
