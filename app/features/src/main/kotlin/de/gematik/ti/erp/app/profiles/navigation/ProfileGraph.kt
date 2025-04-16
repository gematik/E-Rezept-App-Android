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

package de.gematik.ti.erp.app.profiles.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.navigation
import de.gematik.ti.erp.app.navigation.renderBottomSheet
import de.gematik.ti.erp.app.navigation.renderComposable
import de.gematik.ti.erp.app.navigation.slideInDown
import de.gematik.ti.erp.app.navigation.slideOutUp
import de.gematik.ti.erp.app.profiles.ui.screens.ProfileAuditEventsScreen
import de.gematik.ti.erp.app.profiles.ui.screens.ProfileEditNameBottomSheetScreen
import de.gematik.ti.erp.app.profiles.ui.screens.ProfileEditPictureBottomSheetScreen
import de.gematik.ti.erp.app.profiles.ui.screens.ProfileEditPictureScreen
import de.gematik.ti.erp.app.profiles.ui.screens.ProfileImageCameraScreen
import de.gematik.ti.erp.app.profiles.ui.screens.ProfileImageCropperScreen
import de.gematik.ti.erp.app.profiles.ui.screens.ProfileImageEmojiScreen
import de.gematik.ti.erp.app.profiles.ui.screens.ProfilePairedDevicesScreen
import de.gematik.ti.erp.app.profiles.ui.screens.ProfileScreen

fun NavGraphBuilder.profileGraph(
    startDestination: String = ProfileRoutes.ProfileScreen.route,
    navController: NavController
) {
    navigation(
        startDestination = startDestination,
        route = ProfileRoutes.subGraphName()
    ) {
        renderComposable(
            stackEnterAnimation = { slideInDown() },
            stackExitAnimation = { slideOutUp() },
            popExitAnimation = { slideOutUp() },
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
        renderBottomSheet(
            route = ProfileRoutes.ProfileEditPictureBottomSheetScreen.route,
            arguments = ProfileRoutes.ProfileEditPictureBottomSheetScreen.arguments
        ) { navEntry ->
            ProfileEditPictureBottomSheetScreen(
                navController = navController,
                navBackStackEntry = navEntry
            )
        }
        renderBottomSheet(
            route = ProfileRoutes.ProfileEditNameBottomSheetScreen.route,
            arguments = ProfileRoutes.ProfileEditNameBottomSheetScreen.arguments
        ) { navEntry ->
            ProfileEditNameBottomSheetScreen(
                navController = navController,
                navBackStackEntry = navEntry
            )
        }
        renderBottomSheet(
            route = ProfileRoutes.ProfileAddNameBottomSheetScreen.route,
            arguments = ProfileRoutes.ProfileAddNameBottomSheetScreen.arguments
        ) { navEntry ->
            ProfileEditNameBottomSheetScreen(
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
            route = ProfileRoutes.ProfileImageEmojiScreen.route,
            arguments = ProfileRoutes.ProfileImageEmojiScreen.arguments
        ) { navEntry ->
            ProfileImageEmojiScreen(
                navController = navController,
                navBackStackEntry = navEntry
            )
        }
        renderComposable(
            route = ProfileRoutes.ProfileImageCameraScreen.route,
            arguments = ProfileRoutes.ProfileImageCameraScreen.arguments
        ) { navEntry ->
            ProfileImageCameraScreen(
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
