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

package de.gematik.ti.erp.app.appupdate.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import de.gematik.ti.erp.app.appupdate.ui.AppUpdateScreen
import de.gematik.ti.erp.app.navigation.renderComposable

fun NavGraphBuilder.appUpdateGraph(
    navController: NavController
) {
    renderComposable(
        route = AppUpdateRoutes.AppUpdateScreen.route
    ) {
        AppUpdateScreen(
            navController = navController,
            navBackStackEntry = it
        )
    }
}

@Composable
fun AppUpdateNavHost(navController: NavHostController) {
    NavHost(
        navController,
        startDestination = AppUpdateRoutes.AppUpdateScreen.route
    ) {
        appUpdateGraph(navController)
    }
}
