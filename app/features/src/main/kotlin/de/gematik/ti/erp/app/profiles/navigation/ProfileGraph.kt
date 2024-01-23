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

package de.gematik.ti.erp.app.profiles.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.navigation
import de.gematik.ti.erp.app.navigation.renderComposable
import de.gematik.ti.erp.app.profiles.ui.ProfileAuditEventsScreen
import de.gematik.ti.erp.app.profiles.ui.ProfileEditPictureScreen
import de.gematik.ti.erp.app.profiles.ui.ProfileImageCropperScreen
import de.gematik.ti.erp.app.profiles.ui.ProfilePairedDevicesScreen
import de.gematik.ti.erp.app.profiles.ui.ProfileScreen
import de.gematik.ti.erp.app.profiles.ui.ProfileTokenScreen

fun NavGraphBuilder.profileGraph(
    startDestination: String = ProfileRoutes.ProfileScreen.route,
    navController: NavController
) {
    navigation(
        startDestination = startDestination,
        route = ProfileRoutes.subGraphName()
    ) {
        renderComposable(
            route = ProfileRoutes.ProfileScreen.route,
            arguments = ProfileRoutes.ProfileScreen.arguments
        ) { navEntry ->
            ProfileScreen(
                navController = navController,
                navBackStackEntry = navEntry
            )
        }
        renderComposable(
            route = ProfileRoutes.ProfileEditPictureScreen.route,
            arguments = ProfileRoutes.ProfileEditPictureScreen.arguments
        ) { navEntry ->
            ProfileEditPictureScreen(
                navController = navController,
                navBackStackEntry = navEntry
            )
        }
        renderComposable(
            route = ProfileRoutes.ProfileImageCropperScreen.route,
            arguments = ProfileRoutes.ProfileImageCropperScreen.arguments
        ) { navEntry ->
            ProfileImageCropperScreen(
                navController = navController,
                navBackStackEntry = navEntry
            )
        }
        renderComposable(
            route = ProfileRoutes.ProfileTokenScreen.route,
            arguments = ProfileRoutes.ProfileTokenScreen.arguments
        ) { navEntry ->
            ProfileTokenScreen(
                navController = navController,
                navBackStackEntry = navEntry
            )
        }
        renderComposable(
            route = ProfileRoutes.ProfileAuditEventsScreen.route,
            arguments = ProfileRoutes.ProfileAuditEventsScreen.arguments
        ) { navEntry ->
            ProfileAuditEventsScreen(
                navController = navController,
                navBackStackEntry = navEntry
            )
        }
        renderComposable(
            route = ProfileRoutes.ProfilePairedDevicesScreen.route,
            arguments = ProfileRoutes.ProfilePairedDevicesScreen.arguments
        ) { navEntry ->
            ProfilePairedDevicesScreen(
                navController = navController,
                navBackStackEntry = navEntry
            )
        }
    }
}
