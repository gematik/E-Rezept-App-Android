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

package de.gematik.ti.erp.app.userauthentication.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.navigation
import de.gematik.ti.erp.app.navigation.renderComposable
import de.gematik.ti.erp.app.userauthentication.ui.screens.UserAuthenticationScreen

fun NavGraphBuilder.userAuthenticationGraph(
    startDestination: String = UserAuthenticationRoutes.UserAuthenticationScreen.route,
    navController: NavController
) {
    navigation(
        startDestination = startDestination,
        route = UserAuthenticationRoutes.subGraphName()
    ) {
        renderComposable(
            route = UserAuthenticationRoutes.UserAuthenticationScreen.route,
            arguments = UserAuthenticationRoutes.UserAuthenticationScreen.arguments
        ) { navEntry ->
            UserAuthenticationScreen(
                navController = navController,
                navBackStackEntry = navEntry
            )
        }
    }
}
