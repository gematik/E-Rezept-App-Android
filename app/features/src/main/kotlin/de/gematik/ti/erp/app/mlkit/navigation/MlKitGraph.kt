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
